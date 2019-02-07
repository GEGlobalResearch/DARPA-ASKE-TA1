package com.research.ge.darpa.aske.text.extraction;


import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.research.ge.darpa.aske.util.ConceptTagging;
import com.research.ge.darpa.aske.util.NLPServices;

public class TagCorpus {
	
	private static List<String> trainSentences = new ArrayList<String>();
	private static List<String> testSentences = new ArrayList<String>();
	
	/**
	 * The main method that drives the initial annotation process with UIMA ConceptMapper
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		TagCorpus tagCorpus = new TagCorpus();
		NLPServices nlp = new NLPServices();
		
		String dirPath = "..\\dataset\\nasa-website";
		
		//String annotatedFilePath = dirPath + "\\annotations\\annotatedSentences.txt"; // + "\\annotations\\annotatedSentences.txt";
		
		String trainFile = dirPath + "\\annotations\\trainAnnotations.txt";
		String testFile = dirPath + "\\annotations\\testAnnotations.txt";
		
		/** Alternate method to annotate (without train and test split) **/
		//HashMap<String, List<String>> corpus = tagCorpus.loadData(dirPath);
		
		tagCorpus.getTrainAndTest(dirPath);
		
		String ontologyFile = "..\\\\models-from-text-sadl-model\\\\OwlModels\\\\PhysicalQuantities.owl";
		ConceptTagging conceptTagging = new ConceptTagging(ontologyFile);
		
		List<String> trainingAnnotations = tagCorpus.processSentences(trainSentences, conceptTagging);
		List<String> testAnnotations = tagCorpus.processSentences(testSentences, conceptTagging);
		
		//annotated sentences to disk
		tagCorpus.saveAnnotationsToDisk(trainFile, trainingAnnotations);
		tagCorpus.saveAnnotationsToDisk(testFile, testAnnotations);
	}
	
	/**
	 * Uses UIMA ConceptMapper to annotate at the sentence level.
	 * Identifies and tags scientific concepts with IOB tags
	 * @param sentences - List of sentences to be annotated
	 * @param conceptTagging - Instance of the ConceptTagging class that implements service calls to UIMA ConcetpMapper
	 * @return List of annotated sentences with IOB tags. Identified concepts are prepended with B-CONCEPT_ or I-CONCEPT_
	 * @throws Exception
	 */
	private List<String> processSentences(List<String> sentences, ConceptTagging conceptTagging) throws Exception {
		List<String> annotatedSentences = new ArrayList<String>();
		
		for(String sent : sentences) {
			List<String> allTags = conceptTagging.runConceptTagging(sent);
			Set<String> tags = new HashSet<String>(allTags);
			
			//Add B-Concept_ I-Concept_ for tagged concepts 
			//Tokenize the returned concept surface text. 
			//First token will be prepended by B-CONCEPT_ and subsequent will be prepended by I-CONCEPT_
			//Then replace the tokens in the text
			
			for(String tag : tags) {
				String [] tagToks = tag.split(" ");
				String replaceText = "B-CONCEPT_" + tagToks[0];
				sent = sent.replace(tagToks[0], replaceText);
				
				for(int idx = 1; idx < tagToks.length; idx++) {
					replaceText = "I-CONCEPT_" + tagToks[idx];
					sent = sent.replace(tagToks[idx], replaceText);
				}
			}
			annotatedSentences.add(sent);
		}
		
		return annotatedSentences;
		
	}
	
	/**
	 * Depricated. Alternate way to tag the corpus at the Document Level. Will be removed
	 * @param corpus - HashMap of document name and list of paragraphs for each document
	 * @param nlp - Instance of the NLPServices class to intitiate services such as Paragraphs to Text
	 * @param conceptTagging - conceptTagging - Instance of the ConceptTagging class that implements service calls to UIMA ConcetpMapper
	 * @throws Exception
	 */
	private void processCorpus(HashMap<String, List<String>> corpus, NLPServices nlp, ConceptTagging conceptTagging) throws Exception {
		List<String> annotatedSentences = new ArrayList<String>();
		for(String doc : corpus.keySet()) {
			List<String> contents = corpus.get(doc);
			
			for(String text : contents) {
				if(!text.isEmpty()) {
					List<String> sentences = nlp.textToSentences(text);
					
					//sentCount += sentences.size();
					
					for(String sent : sentences) {
						List<String> allTags = conceptTagging.runConceptTagging(sent);
						Set<String> tags = new HashSet<String>(allTags);
						
						//Add B-Concept_ I-Concept_ for tagged concepts 
						//Tokenize the returned concept surface text. 
						//First token will be prepended by B-CONCEPT_ and subsequent will be prepended by I-CONCEPT_
						//Then replace the tokens in the text
						
						//tagCount += allTags.size();
						
						for(String tag : tags) {
							//String tmp = tag;
							String [] tagToks = tag.split(" ");
							String replaceText = "B-CONCEPT_" + tagToks[0];
							sent = sent.replace(tagToks[0], replaceText);
							
							for(int idx = 1; idx < tagToks.length; idx++) {
								replaceText = "I-CONCEPT_" + tagToks[idx];
								sent = sent.replace(tagToks[idx], replaceText);
							}
						}
						annotatedSentences.add(sent);
						//System.out.println(sent);
					}
				}
			}
		}
	}

	
	/**
	 * Saves the IOB tags annotated sentences to disk
	 * @param annotatedFilePath - Path for the file to be saved
	 * @param annotatedSentences - List of IOB tags annotated sentences
	 * @throws IOException
	 */
	private void saveAnnotationsToDisk(String annotatedFilePath, List<String> annotatedSentences) throws IOException {
		PrintWriter pw = new PrintWriter(new FileWriter(annotatedFilePath));
		for(String annotatedSent : annotatedSentences) {
			pw.println(annotatedSent);
		}
		pw.close();
	}

	/**
	 * This method reads the NASA corpus in a HashMap as a pair of document name with list of paragraphs in each document
	 * @param dirPath - NASA corpus directory path
	 * @return HashMap of document name as key and paragraphs list as value
	 * @throws IOException
	 */
	private HashMap<String, List<String>> loadData(String dirPath) throws IOException {
		File dir = new File(dirPath);
		HashMap<String, List<String>> corpus = new HashMap<String, List<String>>();
		for(File file : dir.listFiles()) {
			if(!file.isDirectory()) {
				List<String> data = Files.readAllLines(Paths.get(file.getAbsolutePath()));
				corpus.put(file.getName(), data);
			}
		}
		return corpus;
	}
	
	/**
	 * Method loads the NASA corpus, splits paragraphs into sentences.
	 * Splits the list of sentences randomly into train and test
	 * @param dirPath - NASA corpus directory path
	 * @throws Exception
	 */
	private void getTrainAndTest(String dirPath) throws Exception {
		File dir = new File(dirPath);
		List<String> content = new ArrayList<String>();
		for(File file : dir.listFiles()) {
			if(!file.isDirectory()) {
				content.addAll(Files.readAllLines(Paths.get(file.getAbsolutePath())));
			}
		}
		
		NLPServices nlp = new NLPServices();
		List<String> sentences = new ArrayList<String>();
		
		for(String text : content) {
			sentences.addAll(nlp.textToSentences(text));
		}
		
		int trainSize = (2* sentences.size())/3;
		Random rand = new Random();
		
		List<Integer> processedIndex = new ArrayList<Integer>();
		
		while(trainSentences.size() != trainSize) {
			int idx = rand.nextInt(sentences.size());
			if(!processedIndex.contains(idx)) {
				trainSentences.add(sentences.get(idx));
				processedIndex.add(idx);
			}
		}
		
		for(int idx = 0; idx < sentences.size(); idx++) {
			if(!processedIndex.contains(idx)) {
				testSentences.add(sentences.get(idx));
			}
		}
	}
}

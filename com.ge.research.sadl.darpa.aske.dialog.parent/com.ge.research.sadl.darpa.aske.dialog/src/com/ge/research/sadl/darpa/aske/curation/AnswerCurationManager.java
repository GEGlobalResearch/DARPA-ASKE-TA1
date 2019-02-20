package com.ge.research.sadl.darpa.aske.curation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;
import java.util.Map;

import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.processing.imports.AnswerExtractionProcessor;
import com.ge.research.sadl.darpa.aske.processing.imports.SadlModelGenerator;
import com.ge.research.sadl.reasoner.ConfigurationException;

public class AnswerCurationManager {
	private String owlModelsFolder;
	private IConfigurationManagerForIDE projectConfigurationManager;
	private Map<String, String> preferences = null;
	private AnswerExtractionProcessor extractionProcessor = null;
	
	public AnswerCurationManager (String modelFolder, IConfigurationManagerForIDE configMgr, Map<String,String> prefs) {
		setOwlModelsFolder(modelFolder);
		setProjectConfigurationManager(configMgr);
		setPreferences(prefs);
	}

	public String getOwlModelsFolder() {
		return owlModelsFolder;
	}

	private void setOwlModelsFolder(String owlModelsFolder) {
		this.owlModelsFolder = owlModelsFolder;
	}

	public IConfigurationManagerForIDE getProjectConfigurationManager() {
		return projectConfigurationManager;
	}

	private void setProjectConfigurationManager(IConfigurationManagerForIDE projectConfigurationManager) {
		this.projectConfigurationManager = projectConfigurationManager;
	}

	private Map<String, String> getPreferences() {
		return preferences;
	}

	private void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}

	public Object getTextProcessor() {
		// TODO Auto-generated method stub
		return null;
	}

	public AnswerExtractionProcessor getExtractionProcessor() {
		if (extractionProcessor == null) {
			extractionProcessor = new AnswerExtractionProcessor(this, getPreferences());
		}
		return extractionProcessor;
	}

	public void setExtractionProcessor(AnswerExtractionProcessor extractionProcessor) {
		this.extractionProcessor = extractionProcessor;
	}

	/**
	 * Method to process a set of imports of text and/or code
	 * @throws IOException
	 * @throws ConfigurationException 
	 */
	public void processImports() throws IOException, ConfigurationException {
		
		SadlModelGenerator smg = new SadlModelGenerator();
		List<File> textFiles = getExtractionProcessor().getTextProcessor().getTextFiles();
		if (textFiles != null) {
			for (File f : textFiles) {
				String content = readFileToString(f);
				getExtractionProcessor().getTextProcessor().process(content, null);
			}
		}
		
		List<File> codeFiles = getExtractionProcessor().getCodeExtractor().getCodeFiles();
		if (codeFiles != null) {
			for (File f : codeFiles) {
				String content = readFileToString(f);
				getExtractionProcessor().getCodeExtractor().process(content);
				
		    	String ontologyRootUri = "http://sadl.org/Temperature.sadl";	// this comes from the selection in the import Wizard
				String newContent = smg.generateSadlModel(getExtractionProcessor().getCodeExtractor(), ontologyRootUri );
				if(newContent != null) {
					getExtractionProcessor().addNewSadlContent(newContent);
				}
			}
		}
	}

	/**
	 * Method to read file content into a string
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public String readFileToString(File file) throws IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(file));
 
		char[] buf = new char[10];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
			System.out.println(numRead);
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
 
		reader.close();
 
		return  fileData.toString();	
	}
	
}

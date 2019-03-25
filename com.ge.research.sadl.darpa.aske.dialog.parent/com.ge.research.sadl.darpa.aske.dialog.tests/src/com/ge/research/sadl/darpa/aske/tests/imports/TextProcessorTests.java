package com.ge.research.sadl.darpa.aske.tests.imports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;

import org.junit.Before;
import org.junit.Test;

import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.curation.DialogAnswerProviderConsoleForTest;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.SaveAsSadl;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.IDialogAnswerProvider;
import com.ge.research.sadl.darpa.aske.processing.imports.TextProcessor;
import com.ge.research.sadl.reasoner.ConfigurationException;

public class TextProcessorTests {

	private String codeExtractionProjectModelFolder;
	private String domainProjectModelFolder;

	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() throws MalformedURLException, UnsupportedEncodingException {
		TextProcessor tp = new TextProcessor(null);
		tp.process(null, "a^2 = R * T * gamma", null);
	}

	@Test
	public void test2() throws MalformedURLException, UnsupportedEncodingException {
		TextProcessor tp = new TextProcessor(null);
		tp.process(null, "The speed of sound is a concept known in physics ", null);
	}

	@Test
	public void test3() throws IOException {
		System.out.println(new File(".").getAbsoluteFile().getAbsolutePath());
		File sourceFile = new File(new File(".").getAbsolutePath() + "/resources/Sound.txt");
		String javaContent = readFile(sourceFile);
		TextProcessor tp = new TextProcessor(null);
		tp.process(null, javaContent, null);
	}

	@Test
	public void test4() throws ConfigurationException, IOException {
		System.out.println(new File(".").getAbsoluteFile().getAbsolutePath());
		File sourceFile = new File(new File(".").getAbsolutePath() + "/resources/Sound.txt");
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null);
		acm.getExtractionProcessor().getCodeExtractor().setCodeModelFolder(getExtractionProjectModelFolder());
		
		IDialogAnswerProvider dapcft = new DialogAnswerProviderConsoleForTest();
		cm.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, dapcft);
		
		String defaultCodeModelPrefix = "Sound";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		acm.getExtractionProcessor().getCodeExtractor().setDefaultCodeModelPrefix(defaultCodeModelPrefix);
		acm.getExtractionProcessor().getCodeExtractor().setDefaultCodeModelName(defaultCodeModelName);
		
		String genFolder = new File(cm.getModelFolder()).getParent() + 
				"/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT;
		new File(genFolder).mkdirs();
		String owlFileName = genFolder + "/" + defaultCodeModelPrefix + ".owl";

		acm.getExtractionProcessor().getTextProcessor().addFile(sourceFile);
		acm.processImports(defaultCodeModelPrefix + ".owl", SaveAsSadl.SaveAsSadl.SaveAsSadl);
		String javaContent = readFile(sourceFile);
		TextProcessor tp = new TextProcessor(null);
		tp.process(null, javaContent, null);
	}

	private String readFile(File file) throws IOException {
	    BufferedReader reader = new BufferedReader(new FileReader (file));
	    String         line = null;
	    StringBuilder  stringBuilder = new StringBuilder();
	    String         ls = System.getProperty("line.separator");

	    try {
	        while((line = reader.readLine()) != null) {
	            stringBuilder.append(line);
	            stringBuilder.append(ls);
	        }

	        return stringBuilder.toString();
	    } finally {
	        reader.close();
	    }
	}

	private String getExtractionProjectModelFolder() {
		return codeExtractionProjectModelFolder;
	}

	private void setExtractionProjectModelFolder(String extractionProjectModelFolder) {
		this.codeExtractionProjectModelFolder = extractionProjectModelFolder;
	}

	private String getDomainProjectModelFolder() {
		return domainProjectModelFolder;
	}

	private void setDomainProjectModelFolder(String outputProjectModelFolder) {
		this.domainProjectModelFolder = outputProjectModelFolder;
	}
}

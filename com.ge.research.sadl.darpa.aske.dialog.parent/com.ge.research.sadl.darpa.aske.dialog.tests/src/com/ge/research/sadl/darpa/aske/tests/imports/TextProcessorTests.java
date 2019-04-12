/********************************************************************** 
 * Note: This license has also been called the "New BSD License" or 
 * "Modified BSD License". See also the 2-clause BSD License.
 *
 * Copyright © 2018-2019 - General Electric Company, All Rights Reserved
 * 
 * Project: ANSWER, developed with the support of the Defense Advanced 
 * Research Projects Agency (DARPA) under Agreement  No.  HR00111990006. 
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 ***********************************************************************/
package com.ge.research.sadl.darpa.aske.tests.imports;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.SaveAsSadl;
import com.ge.research.sadl.darpa.aske.curation.DialogAnswerProviderConsoleForTest;
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

	@Ignore
	@Test
	public void test() throws ConfigurationException, IOException {
		TextProcessor tp = new TextProcessor(new AnswerCurationManager(domainProjectModelFolder, 
				ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(domainProjectModelFolder, null), null), null);
		tp.process(null, "a^2 = R * T * gamma", null);
	}

	@Ignore
	@Test
	public void test2() throws ConfigurationException, IOException {
		TextProcessor tp = new TextProcessor(null, null);
		tp.process(null, "The speed of sound is a concept known in physics ", null);
	}

	@Ignore
	@Test
	public void test3() throws IOException, ConfigurationException {
		System.out.println(new File(".").getAbsoluteFile().getAbsolutePath());
		File sourceFile = new File(new File(".").getAbsolutePath() + "/resources/Sound.txt");
		String javaContent = readFile(sourceFile);
		TextProcessor tp = new TextProcessor(null, null);
		tp.process(null, javaContent, null);
	}

	@Ignore
	@Test
	public void test4() throws ConfigurationException, IOException {
		System.out.println(new File(".").getAbsoluteFile().getAbsolutePath());
		File sourceFile = new File(new File(".").getAbsolutePath() + "/resources/Sound.txt");
		File domainProjectFolder = new File(sourceFile.getParentFile() + "/TestSadlProject");
		File domainModelFolder = new File(domainProjectFolder.getAbsoluteFile() + "/OwlModels");
		setDomainProjectModelFolder(domainModelFolder.getCanonicalPath());
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
		acm.processImports(SaveAsSadl.SaveAsSadl.SaveAsSadl);
		String javaContent = readFile(sourceFile);
		TextProcessor tp = new TextProcessor(null, null);
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

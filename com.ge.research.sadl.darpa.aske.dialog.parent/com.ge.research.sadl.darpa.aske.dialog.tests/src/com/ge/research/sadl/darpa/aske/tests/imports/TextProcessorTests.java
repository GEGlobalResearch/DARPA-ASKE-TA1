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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.junit.Before;
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
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.ResultSet;

public class TextProcessorTests {

	private String textExtractionProjectModelFolder;
	private String domainProjectModelFolder;
	private File textExtractionPrjFolder;

	@Before
	public void setUp() throws Exception {
		File projectRoot = new File("resources/M5Snapshot");
		String codeExtractionKbRoot = projectRoot.getCanonicalPath();
		setTextExtractionPrjFolder(new File(codeExtractionKbRoot));
		setExtractionProjectModelFolder(getTextExtractionPrjFolder().getCanonicalPath() + "/OwlModels");
		
		setDomainProjectModelFolder(getExtractionProjectModelFolder());
	}

//	@Ignore
	@Test
	public void test() throws ConfigurationException, IOException, InvalidInputException {
		File sourceFile = new File(new File(".").getAbsolutePath() + "/resources/");
		File domainProjectFolder = new File(sourceFile + "/TestSadlProject");
		File domainModelFolder = new File(domainProjectFolder.getAbsoluteFile() + "/OwlModels");
		setDomainProjectModelFolder(domainModelFolder.getCanonicalPath());
		TextProcessor tp = new TextProcessor(new AnswerCurationManager(domainProjectModelFolder, 
				ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(domainProjectModelFolder, null), null), null);
		tp.setTextmodelPrefix("sos");
		String localityURI = "http://darpa.aske.ta1.ge/sostest";
		tp.setTextmodelName(localityURI);
		int[] result = tp.processText(localityURI, "a^2 = R * T * gamma", localityURI);
		assertNotNull(result);
		assertEquals(0, result[0]);
		assertEquals(1, result[1]);
		System.out.println("nc=" + result[0] + ", neq=" + result[1]);

		List<String[]> results = tp.processName("T", localityURI);
		for (String[] use : results) {
			System.out.println("Variable '" + use[0] + "' used in equation '" + use[1] + "'");
		}
		
//		tp.processUnitExtraciton("ft/sec", localityURI);
	}

//	@Ignore
	@Test
	public void test2() throws ConfigurationException, IOException, InvalidInputException {
		File sourceFile = new File(new File(".").getAbsolutePath() + "/resources/");
		File domainProjectFolder = new File(sourceFile + "/TestSadlProject");
		File domainModelFolder = new File(domainProjectFolder.getAbsoluteFile() + "/OwlModels");
		setDomainProjectModelFolder(domainModelFolder.getCanonicalPath());
		TextProcessor tp = new TextProcessor(new AnswerCurationManager(domainProjectModelFolder, 
				ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(domainProjectModelFolder, null), null), null);
		tp.setTextmodelPrefix("sos");
		String localityURI = "http://darpa.aske.ta1.ge/sostest";
		tp.setTextmodelName(localityURI);
		int[] result = tp.processText(localityURI, "The speed of sound is a concept known in physics ", localityURI);
		assertNotNull(result);
//		assertEquals(0, result[0]);
//		assertEquals(1, result[1]);
		System.out.println("nc=" + result[0] + ", neq=" + result[1]);
		List<String[]> results = tp.processName("speed", localityURI);
		for (String[] use : results) {
			System.out.println("Variable '" + use[0] + "' used in equation '" + use[1] + "'");
		}
	}

//	@Ignore
	@Test
	public void test3() throws IOException, ConfigurationException, InvalidInputException {
		File textFile = new File(getTextExtractionPrjFolder() + "/ExtractedModels/Sources/Sound.txt");
		String javaContent = readFile(textFile);
		TextProcessor tp = new TextProcessor(new AnswerCurationManager(getDomainProjectModelFolder(), 
				ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null), null), null);
		tp.setTextmodelPrefix("sos");
		tp.setTextmodelName("http://darpa.aske.ta1.ge/sostest");
		String localityURI = "http://darpa.aske.ta1.ge/sostest";
		tp.setTextmodelName(localityURI);
		int[] result = tp.processText(localityURI, javaContent, localityURI);
		System.out.println("nc=" + result[0] + ", neq=" + result[1]);
		List<String[]> results = tp.processName("T", localityURI);
		for (String[] use : results) {
			System.out.println("Variable '" + use[0] + "' used in equation '" + use[1] + "'");
		}
	}

//	@Ignore
	@Test
	public void test4() throws ConfigurationException, IOException {
		File textFile = new File(getTextExtractionPrjFolder() + "/ExtractedModels/Sources/Sound.txt");
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null);
		acm.getExtractionProcessor().getTextProcessor().setTextModelFolder(getExtractionProjectModelFolder());
		
		IDialogAnswerProvider dapcft = new DialogAnswerProviderConsoleForTest();
		cm.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, dapcft);
		
		String defaultTextModelPrefix = "Sound";
		String defaultTextModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultTextModelPrefix;
		acm.getExtractionProcessor().getTextProcessor().setDefaultTextModelPrefix(defaultTextModelPrefix);
		acm.getExtractionProcessor().getTextProcessor().setDefaultTextModelName(defaultTextModelName);
		
		String genFolder = new File(acm.getExtractionProcessor().getTextProcessor().getTextModelFolder()).getParent() + 
		"/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT;
		new File(genFolder).mkdirs();

		acm.getExtractionProcessor().getTextProcessor().addFile(textFile);
		acm.processImports(SaveAsSadl.SaveAsSadl);

		String owlFileName = genFolder + "/" + textFile.getName() + ".owl";
		String sadlFileName = owlFileName + ".sadl";
		String sadlFileContent = readFile(new File(sadlFileName));
		System.out.println(sadlFileContent);

		String query = "select ?eq ?lg ?sc where {?eq <rdf:type> <ExternalEquation> . ?eq <expression> ?scrbn . \r\n" + 
				"		?scrbn <language> ?lg . ?scrbn <script> ?sc }";
		try {
			ResultSet rs =acm.getExtractionProcessor().getTextProcessor().executeSparqlQuery(query);
			System.out.println(rs.toStringWithIndent(5));
		} catch (ReasonerNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryCancelledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		return textExtractionProjectModelFolder;
	}

	private void setExtractionProjectModelFolder(String extractionProjectModelFolder) {
		this.textExtractionProjectModelFolder = extractionProjectModelFolder;
	}

	private String getDomainProjectModelFolder() {
		return domainProjectModelFolder;
	}

	private void setDomainProjectModelFolder(String outputProjectModelFolder) {
		this.domainProjectModelFolder = outputProjectModelFolder;
	}

	private File getTextExtractionPrjFolder() {
		return textExtractionPrjFolder;
	}

	private void setTextExtractionPrjFolder(File textExtractionPrjFolder) {
		this.textExtractionPrjFolder = textExtractionPrjFolder;
	}
}

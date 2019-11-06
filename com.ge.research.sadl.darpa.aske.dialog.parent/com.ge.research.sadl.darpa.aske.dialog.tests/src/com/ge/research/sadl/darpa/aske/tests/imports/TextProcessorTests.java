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
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
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
import com.ge.research.sadl.darpa.aske.processing.imports.TextProcessingServiceInterface.EquationVariableContextResponse;
import com.ge.research.sadl.darpa.aske.processing.imports.TextProcessor;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.ge.research.sadl.utils.ResourceManager;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

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
				ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(domainProjectModelFolder, null), null, null), null);
		tp.setTextModelPrefix("sos");
		String localityURI = "http://darpa.aske.ta1.ge/sostest";
		tp.setTextModelName(localityURI);
		String msg = tp.clearGraph(localityURI);
		System.out.println("Clear graph response: " + msg);
		int[] result = tp.processText(localityURI, "a^2 = R * T * gamma", localityURI, null);
		assertNotNull(result);
		assertEquals(0, result[0]);
		assertEquals(1, result[1]);
		System.out.println("nc=" + result[0] + ", neq=" + result[1]);
		EquationVariableContextResponse results = tp.equationVariableContext("T", localityURI);
		if (results != null) {
			System.out.println(results.getMessage());
			for (String[] use : results.getResults()) {
				assertTrue(use!= null && use.length == 2);
				System.out.println("Parameter '" + use[1] + "' used in '" + use[0] + "'");
			}
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
				ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(domainProjectModelFolder, null), null, null), null);
		tp.setTextModelPrefix("sos");
		String localityURI = "http://darpa.aske.ta1.ge/sostest";
		tp.setTextModelName(localityURI);
		String msg = tp.clearGraph(localityURI);
		System.out.println("Clear graph response: " + msg);
		int[] result = tp.processText(localityURI, "The speed of sound is a concept known in physics ", localityURI, "sos");
		assertNotNull(result);
//		assertEquals(1, result[0]);
//		assertEquals(0, result[1]);
		System.out.println("nc=" + result[0] + ", neq=" + result[1]);
		EquationVariableContextResponse results = tp.equationVariableContext("speed", localityURI);
		System.out.println(results.getMessage());
		assertTrue(results.getResults().isEmpty());
	}

//	@Ignore
	@Test
	public void test3() throws IOException, ConfigurationException, InvalidInputException {
		File textFile = new File(getTextExtractionPrjFolder() + "/ExtractedModels/Sources/Sound.txt");
		String javaContent = readFile(textFile);
		TextProcessor tp = new TextProcessor(new AnswerCurationManager(getDomainProjectModelFolder(), 
				ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null), null, null), null);
		tp.setTextModelPrefix("sos");
		tp.setTextModelName("http://darpa.aske.ta1.ge/sostest");
		String localityURI = "http://darpa.aske.ta1.ge/sostest";
		tp.setTextModelName(localityURI);
		String msg = tp.clearGraph(localityURI);
		System.out.println("Clear graph response: " + msg);
		int[] result = tp.processText(localityURI, javaContent, localityURI, "sos");
		System.out.println("nc=" + result[0] + ", neq=" + result[1]);
		EquationVariableContextResponse results = tp.equationVariableContext("T", localityURI);
		System.out.println(results.getMessage());
		assertFalse(results.getMessage().isEmpty());
		for (String[] use : results.getResults()) {
			assertTrue(use!= null && use.length == 2);
			System.out.println("Parameter '" + use[1] + "' used in '" + use[0] + "'");
		}
	}

//	@Ignore
	@Test
	public void test4() throws ConfigurationException, IOException, InvalidNameException {
		File textFile = new File(getTextExtractionPrjFolder() + "/ExtractedModels/Sources/Sound.txt");
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
		String domainModelName = "http://sadl.org/SpeedOfSound.sadl";
		acm.setDomainModel(cm.loadOntModel(new SadlUtils().fileUrlToFileName(cm.getAltUrlFromPublicUri(domainModelName)), true));
		
		IDialogAnswerProvider dapcft = new DialogAnswerProviderConsoleForTest();
		cm.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, dapcft);
		
		String defaultTextModelPrefix = "Sound";
		String defaultTextModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultTextModelPrefix;
		acm.getExtractionProcessor().getTextProcessor().setTextModelPrefix(defaultTextModelPrefix);
		acm.getExtractionProcessor().getTextProcessor().setTextModelName(defaultTextModelName);
		
		String genFolder = new File(acm.getOwlModelsFolder()).getParent() + 
		"/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT;
		new File(genFolder).mkdirs();

		acm.getExtractionProcessor().getTextProcessor().addFile(textFile);
		SaveAsSadl sas = SaveAsSadl.DoNotSaveAsSadl;
//		SaveAsSadl sas = SaveAsSadl.SaveAsSadl;
//		SaveAsSadl sas = SaveAsSadl.AskUserSaveAsSadl;
		String owlFileName = genFolder + "/" + textFile.getName() + ".owl";
		String sadlFileName = owlFileName + ".sadl";
		File sf = new File(sadlFileName);
		if (sf.exists()) {
			sf.delete();
		}
		String msg = acm.getExtractionProcessor().getTextProcessor().clearGraph(acm.getExtractionProcessor().getTextProcessor().getTextModelName());
		System.out.println("Clear graph response: " + msg);
		acm.processImports(sas); 
		
//		System.err.println("The extracted model:");
//		acm.getExtractionProcessor().getTextModel().write(System.err, "N3");

		if (!sas.equals(SaveAsSadl.DoNotSaveAsSadl)) {
			if (sf.exists()) {
				String sadlFileContent = readFile(new File(sadlFileName));
				System.out.println(sadlFileContent);
			}
		}

		String query = "select distinct ?eq ?lg ?sc where {?eq <rdf:type> <ExternalEquation> . ?eq <expression> ?scrbn . \r\n" + 
				"		?scrbn <language> ?lg . ?scrbn <script> ?sc }";
		String anEquation = null;
		try {
			ResultSet rs =acm.getExtractionProcessor().getTextProcessor().executeSparqlQuery(query);
			System.out.println("Equations in text extraction model: (" + (rs != null ? rs.getRowCount() : 0) + ")");
			if (rs != null) {
				rs.setShowNamespaces(false);
				System.out.println(rs.toStringWithIndent(5));
				for (int r = 0; r < rs.getRowCount(); r++) {
					if (rs.getResultAt(r, 1).toString().equals("Python-TF")) {
						anEquation = rs.getResultAt(r, 0).toString();
						break;
					}
				}
			}
			else {
				System.out.println("   none");
			}
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
		
		if (anEquation != null) {
			List<Object> params = new ArrayList<Object>();		// TODO change to parameterized query
			params.add(anEquation);
			String query2 = "select ?arg ?argName ?argtyp where {<" + anEquation + "> <arguments>/<rdf:rest>*/<rdf:first> ?arg . ?arg <localDescriptorName> ?argName . OPTIONAL{?arg <dataType> ?argtyp}}";
			try {
				System.out.println("Equation arguments for " + anEquation + ":");
				ResultSet rs =acm.getExtractionProcessor().getTextProcessor().executeSparqlQuery(query2);
				if (rs != null) {
					rs.setShowNamespaces(false);
					System.out.println(rs.toStringWithIndent(5));
					for (int c = 0; c < rs.getColumnCount(); c++) {
						String param = rs.getResultAt(0, c).toString();
						EquationVariableContextResponse pnResults = acm.getExtractionProcessor().getTextProcessor().equationVariableContext(param, acm.getExtractionProcessor().getTextModelName());
						if (pnResults != null) {
							System.out.println(pnResults.getMessage());
							for (String[] use : pnResults.getResults()) {
								assertTrue(use!= null && use.length == 4);
								System.out.println(pnResults);
							}
						}
					}	
				}
				else {
					System.out.println("   none");
				}
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
			} catch (InvalidInputException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// now look for variable information
		
		try {
			EquationVariableContextResponse evcr = acm.getTextProcessor().equationVariableContext("R", acm.getLocalityOfFileExtract(textFile.getCanonicalPath()));
			if (evcr != null) {
				System.out.println(evcr.toString());
			}
		} catch (InvalidInputException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			EquationVariableContextResponse evcr = acm.getTextProcessor().equationVariableContext("T", acm.getLocalityOfFileExtract(textFile.getCanonicalPath()));
			if (evcr != null) {
				System.out.println(evcr.toString());
			}
		} catch (InvalidInputException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Test
	public void testReadN3FromService() throws IOException {
		File n3File = new File(getTextExtractionPrjFolder().getParent() + "/MiscFiles/SoundTxtExtract.n3");
		Model m = getModel(n3File, "N3");
		assertNotNull(m);
	}

	public Model getModel(File f, String format) throws IOException {
		if (f.exists()) {
		    FileInputStream in = new FileInputStream( f );
			Model m = ModelFactory.createDefaultModel().read( in, "", format );
	    	if (m instanceof Model) {
	    		return m;
	    	}
		}
		{
			System.err.println("File '" + f.getCanonicalPath() + "' does not exist.");
		}
	    return null;
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

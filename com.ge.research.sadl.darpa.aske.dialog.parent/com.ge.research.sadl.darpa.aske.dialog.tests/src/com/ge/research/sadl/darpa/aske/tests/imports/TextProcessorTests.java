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
import static org.junit.Assert.assertTrue;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.jena.ontology.OntModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.vocabulary.RDFS;
import org.eclipse.jdt.core.compiler.InvalidInputException;
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
import com.ge.research.sadl.darpa.aske.processing.imports.AnswerExtractionException;
import com.ge.research.sadl.darpa.aske.processing.imports.TextProcessingServiceInterface;
import com.ge.research.sadl.darpa.aske.processing.imports.TextProcessingServiceInterface.EquationVariableContextResponse;
import com.ge.research.sadl.darpa.aske.processing.imports.TextProcessingServiceInterface.UnitExtractionResponse;
import com.ge.research.sadl.darpa.aske.processing.imports.TextProcessor;
import com.ge.research.sadl.reasoner.AmbiguousNameException;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.IConfigurationManagerForEditing.Scope;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.utils.SadlUtils;

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

	@Ignore("this test requires REST services at default URL")
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
		int[] result = tp.processText(localityURI, "a^2 = R * T * gamma", localityURI, null, null, true);
		assertNotNull(result);
		assertEquals(0, result[0]);
		assertEquals(1, result[1]);
		System.out.println("nc=" + result[0] + ", neq=" + result[1]);
		EquationVariableContextResponse results = tp.equationVariableContext("T", localityURI);
		if (results != null) {
			System.out.println(results.getMessage());
			assertTrue(results.getResults().isEmpty());
			for (String[] use : results.getResults()) {
				assertTrue(use!= null && use.length == 4);
				System.out.println("Parameter '" + use[1] + "' used in '" + use[0] + "'");
			}
		}
		
//		tp.processUnitExtraciton("ft/sec", localityURI);
	}

	@Ignore("this test requires REST services at default URL")
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
		int[] result = tp.processText(localityURI, "The speed of sound is a concept known in physics ", localityURI, localityURI, "sos", true);
		assertNotNull(result);
//		assertEquals(1, result[0]);
//		assertEquals(0, result[1]);
		System.out.println("nc=" + result[0] + ", neq=" + result[1]);
		EquationVariableContextResponse results = tp.equationVariableContext("speed", localityURI);
		System.out.println(results.getMessage());
		assertTrue(results.getResults().isEmpty());
	}

	@Ignore("this test requires REST services at default URL")
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
		int[] result = tp.processText(localityURI, javaContent, localityURI, localityURI, "sos", true);
		System.out.println("nc=" + result[0] + ", neq=" + result[1]);
		EquationVariableContextResponse results = tp.equationVariableContext("T", localityURI);
		System.out.println(results.getMessage());
		assertFalse(results.getMessage().isEmpty());
		for (String[] use : results.getResults()) {
			assertTrue(use!= null && use.length == 4);
			System.out.println("Parameter '" + use[1] + "' used in '" + use[0] + "'");
			if (use[2] != null) {
				System.out.println("   concept labeled '" + use[2] + "' in external concept '" + use[3] + "'");
			}
		}
	}

	@Ignore("this test requires REST services at default URL")
	@Test
	public void test4() throws ConfigurationException, IOException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException, AnswerExtractionException, InvalidInputException, AmbiguousNameException {
		File textFile = new File(getTextExtractionPrjFolder() + "/ExtractedModels/Sources/Sound.txt");
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
		String domainModelName = "http://sadl.org/SpeedOfSound.sadl";
		acm.setDomainModelName(domainModelName);
		acm.setDomainModel(cm.loadOntModel(new SadlUtils().fileUrlToFileName(cm.getAltUrlFromPublicUri(domainModelName)), true));
		
		IDialogAnswerProvider dapcft = new DialogAnswerProviderConsoleForTest();
		cm.addPrivateKeyMapValueByResource(DialogConstants.DIALOG_ANSWER_PROVIDER, acm.getResource().getURI(), dapcft);
		
		String defaultTextModelPrefix = "Sound";
		String defaultTextModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultTextModelPrefix;
		acm.getExtractionProcessor().getTextProcessor().setTextModelPrefix(defaultTextModelPrefix);
		acm.getExtractionProcessor().getTextProcessor().setTextModelName(defaultTextModelName);
		
		String genFolder = new File(acm.getOwlModelsFolder()).getParent() + 
		"/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT;
		new File(genFolder).mkdirs();

		acm.getExtractionProcessor().getTextProcessor().addFile(textFile);
//		SaveAsSadl sas = SaveAsSadl.DoNotSaveAsSadl;
		SaveAsSadl sas = SaveAsSadl.SaveAsSadl;
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

		String query = "select distinct ?eq ?lg ?sc where {?eq <rdf:type> <ExternalEquation> . ?eq <expression> ?scrbn . \n" + 
				"		?scrbn <language> ?lg . ?scrbn <script> ?sc }";
		List<String> equations = null;
		try {
			ResultSet rs =acm.getExtractionProcessor().getTextProcessor().executeSparqlQuery(query);
			System.out.println("Equations in text extraction model: (" + (rs != null ? rs.getRowCount() : 0) + ")");
			if (rs != null) {
				equations = new ArrayList<String>();
				rs.setShowNamespaces(false);
				System.out.println(rs.toStringWithIndent(5));
				for (int r = 0; r < rs.getRowCount(); r++) {
					if (rs.getResultAt(r, 1).toString().equals(DialogConstants.TF_PYTHON_LANGUAGE)) {
						equations.add(rs.getResultAt(r, 0).toString());
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
		
		if (equations != null) {
			for (String anEquation : equations) {
				List<Object> params = new ArrayList<Object>();		// TODO change to parameterized query
				params.add(anEquation);
				List<EquationVariableContextResponse> evcrs = new ArrayList<EquationVariableContextResponse>();
				String query2 = "select ?arg ?argName ?argtyp where {<" + anEquation + "> <arguments>/<rdf:rest>*/<rdf:first> ?arg . ?arg <localDescriptorName> ?argName . OPTIONAL{?arg <dataType> ?argtyp}}";
				try {
					System.out.println("Equation arguments for " + anEquation + ":");
					ResultSet rs =acm.getExtractionProcessor().getTextProcessor().executeSparqlQuery(query2);
					if (rs != null) {
						rs.setShowNamespaces(false);
						System.out.println(rs.toStringWithIndent(5));
						for (int r = 0; r < rs.getRowCount(); r++) {
							String param = rs.getResultAt(r, 1).toString();
							EquationVariableContextResponse pnResults = acm.getExtractionProcessor().getTextProcessor().equationVariableContext(param, acm.getExtractionProcessor().getTextModelName());
							if (pnResults != null) {
								System.out.println(pnResults.getMessage());
								for (String[] use : pnResults.getResults()) {
									assertTrue(use!= null && use.length == 4);
								}
								System.out.println(pnResults);
								evcrs.add(pnResults);
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
				
				Map<String, TextProcessor.MergedEquationVariableContext> mevcs = acm.getExtractionProcessor().getTextProcessor().unifyEquationVariableContentResponses(evcrs);
				if (mevcs != null) {
					Set<String> uris = mevcs.keySet();
					for (String uri : uris) {
						TextProcessor.MergedEquationVariableContext conceptMevc = mevcs.get(uri);
						System.out.println(conceptMevc);
					}
				}
			}
		}
		
		// now look for variable information
		
		try {
			EquationVariableContextResponse evcr = acm.getTextProcessor().equationVariableContext("R", acm.getLocalityURI());
			if (evcr != null) {
				System.out.println(evcr.toString());
			}
		} catch (InvalidInputException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			EquationVariableContextResponse evcr = acm.getTextProcessor().equationVariableContext("T", acm.getLocalityURI());
			if (evcr != null) {
				System.out.println(evcr.toString());
			}
		} catch (InvalidInputException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	@Ignore("this test requires REST services at default URL")
	@Test
	public void test5() throws IOException, ConfigurationException, InvalidInputException {
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
		int[] result = tp.processText(localityURI, javaContent, localityURI, localityURI, "sos", true);
		System.out.println("nc=" + result[0] + ", neq=" + result[1]);
		EquationVariableContextResponse results = tp.equationVariableContext("a", localityURI);
		System.out.println(results.getMessage());
		assertFalse(results.getMessage().isEmpty());
		for (String[] use : results.getResults()) {
			assertTrue(use!= null && use.length == 4);
			System.out.println("Variable '" + use[1] + "' used in '" + use[0] + "'");
			if (use[2] != null) {
				System.out.println("   concept labeled '" + use[2] + "' in external concept '" + use[3] + "'");
			}
			else {
				System.out.println("     no concept label and external concept found.");
			}
			if (use[3] == null) {
				// try the LHS of the equation
				if (use[0].indexOf("=") > 0) {
					String lhs = use[0].substring(0, use[0].indexOf("=")).trim();
					EquationVariableContextResponse results2 = tp.equationVariableContext(lhs, localityURI);
					assertFalse(results2.getMessage().isEmpty());
					if (results2.getResults().size() > 0) {
						for (String[] use2 : results2.getResults()) {
							assertTrue(use2!= null && use2.length == 4);
							System.out.println("Equation LHS '" + use2[1] + "' used in '" + use2[0] + "'");
							if (use2[2] != null) {
								System.out.println("   concept labeled '" + use2[2] + "' in external concept '" + use2[3] + "'");
							}
							else {
								System.out.println("    no concept label and external concept found for LHS of equation");
							}
						}
					}
					else {
						System.out.println("No result for LHS of equation = '" + lhs + "'");
					}
				}
			}
		}
	}
	
	@Ignore("this test requires REST services at default URL")
	@Test
	public void test6() throws IOException, ConfigurationException, InvalidInputException {
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
		int[] result = tp.processText(localityURI, javaContent, localityURI, localityURI, "sos", true);
		System.out.println("nc=" + result[0] + ", neq=" + result[1]);
		List<UnitExtractionResponse> uresult = tp.unitExtraction("variable measurement degree Celsius", localityURI);
		assertNotNull(uresult);
		for (UnitExtractionResponse ur : uresult) {
			System.out.println("result=" + ur.toString());
			assertTrue(ur.getRelatedConceptName().equals("temperature"));
			assertTrue(ur.getRelatedConceptURI().equals("http://purl.obolibrary.org/obo/UO_0000005"));
			assertTrue(ur.getUnitName().equals("degree Celsius"));
			assertTrue(ur.getUnitText().equals("degree Celsius"));
			assertTrue(ur.getUnitURI().equals("http://purl.obolibrary.org/obo/UO_0000027"));
		}
	}

	@Ignore("this test requires REST services at default URL")
	@Test
	public void test7() throws ConfigurationException, IOException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException, AnswerExtractionException, InvalidInputException, AmbiguousNameException {
		File textFile = new File(getTextExtractionPrjFolder() + "/ExtractedModels/Sources/Isentrop.txt");
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
		String domainModelName = "http://sadl.org/SpeedOfSound.sadl";
		acm.setDomainModelName(domainModelName);
		acm.setDomainModel(cm.loadOntModel(new SadlUtils().fileUrlToFileName(cm.getAltUrlFromPublicUri(domainModelName)), true));
		
		IDialogAnswerProvider dapcft = new DialogAnswerProviderConsoleForTest();
		cm.addPrivateKeyMapValueByResource(DialogConstants.DIALOG_ANSWER_PROVIDER, acm.getResource().getURI(), dapcft);
		
		String defaultTextModelPrefix = "Isentrop";
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
		acm.processImports(sas); 							// ***** this is where the work happens *****
		OntModel tm = acm.getExtractionProcessor().getTextModel();
		tm.write(System.out, "N3");
		
		String sadlContent = acm.getExtractionProcessor().getGeneratedSadlContent();
		System.out.println("\n\n*****  New Dialog editor content *********");
		System.out.println(sadlContent);
//		System.err.println("The extracted model:");
//		acm.getExtractionProcessor().getTextModel().write(System.err, "N3");

		if (!sas.equals(SaveAsSadl.DoNotSaveAsSadl)) {
			if (sf.exists()) {
				String sadlFileContent = readFile(new File(sadlFileName));
				System.out.println(sadlFileContent);
			}
		}

//		String query = "select distinct ?eq ?lg ?sc where {?eq <rdf:type> <ExternalEquation> . ?eq <expression> ?scrbn . \n" + 
//				"		?scrbn <language> ?lg . ?scrbn <script> ?sc }";
//		List<String> equations = null;
//		try {
//			ResultSet rs =acm.getExtractionProcessor().getTextProcessor().executeSparqlQuery(query);
//			System.out.println("Equations in text extraction model: (" + (rs != null ? rs.getRowCount() : 0) + ")");
//			if (rs != null) {
//				equations = new ArrayList<String>();
//				rs.setShowNamespaces(false);
//				System.out.println(rs.toStringWithIndent(5));
//				for (int r = 0; r < rs.getRowCount(); r++) {
//					if (rs.getResultAt(r, 1).toString().equals(DialogConstants.TF_PYTHON_LANGUAGE)) {
//						equations.add(rs.getResultAt(r, 0).toString());
//					}
//				}
//			}
//			else {
//				System.out.println("   none");
//			}
//		} catch (ReasonerNotFoundException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (InvalidNameException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (QueryParseException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (QueryCancelledException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
	}

	@Ignore ("This has spaces missing and is known to cause issues in the service")
	@Test
	public void testTextToTriples_01() throws ConfigurationException, IOException {
		String content = 
				"As a gas is forced through a tube, the gas molecules are deflectedby the \n" + 
				"walls of the tube. If the speed of the gas is much less thanthe speed of \n" + 
				"sound of the gas, thedensityof the gas remains constant and the velocity of \n" + 
				"the flow increases.However, as the speed of the flow approaches thespeed of \n" + 
				"soundwe must considercompressibility effectson the gas. The density of the \n" + 
				"gas varies fromone location to the next.Considering flow through a tube, as \n" + 
				"shown inthe figure, if the flow is very gradually compressed (area \n" + 
				"decreases) and thengradually expanded (area increases), the flow conditions \n" + 
				"return to theiroriginal values. We say that such a process is reversible\n" + 
				".From a consideration of thesecond lawof thermodynamics,a reversible flow \n" + 
				"maintains a constant value ofentropy.Engineers call this type of flow an \n" + 
				"isentropic flow;a combination of the Greek word \"iso\" (same) and entropy.\n" + 
				"\n" + 
				"Isentropic flows occur when the change in flow variables is smalland \n" + 
				"gradual, such as the ideal flow through thenozzleshown above.The generation \n" + 
				"ofsound wavesis an isentropic process. Asupersonic flow thatis turned while \n" + 
				"the flow area increases is also isentropic.We call this an isentropic\n" + 
				"expansionbecause of the area increase.If a supersonic flow is \n" + 
				"turnedabruptly and the flow area decreases,shock wavesare generated and the \n" + 
				"flow is irreversible.The isentropic relations are no longervalid and the \n" + 
				"flow isgoverned by the oblique or normalshock relations.\n" + 
				"\n" + 
				"On this slide we have collected many of the important equationswhich \n" + 
				"describe an isentropic flow. We begin with the definitionof the Mach number \n" + 
				"since thisparameterappearsin many of the isentropic flow equations.The Mach \n" + 
				"number M isthe ratio of the speed of the flow v to the speed of sound a.\n" + 
				"\n" + 
				"Eq #1:\n" + 
				"\n" + 
				"M = v / a\n" + 
				"\n" + 
				"Thespeed of sound, in turn, depends on thedensity r, thepressure, p, the\n" + 
				"temperature, T,and theratio of specific heats gam:\n" + 
				"\n" + 
				"Eq #2:\n" + 
				"\n" + 
				"a = sqrt(gam * p / r) = sqrt (gam * R * T)\n" + 
				"\n" + 
				"where R is the gas constant from theequations of state. If we begin with \n" + 
				"theentropy equations for a gas, it can beshownthat the pressure and density \n" + 
				"of an isentropic flow are related as follows:\n" + 
				"\n" + 
				"Eq #3:\n" + 
				"\n" + 
				"p / r^gam = constant\n" + 
				"\n" + 
				"We can determine thevalue of the constant by defining total conditions to \n" + 
				"be thepressure and density when the flow is brought to rest \n" + 
				"isentropically.The \"t\" subscript used in many of these equations stands for \n" + 
				"\"totalconditions\". (You probably already have some idea of total \n" + 
				"conditionsfrom experience with Bernoulli's equation).\n" + 
				"\n" + 
				"Eq #3:\n" + 
				"\n" + 
				"p / r^gam = constant = pt / rt^gam\n" + 
				"\n" + 
				"Using the equation of state, we can easilyderivethe following relations \n" + 
				"from equation (3):\n" + 
				"\n" + 
				"Eq #4:\n" + 
				"\n" + 
				"p / pt = (r / rt)^gam = (T / Tt)^[gam/(gam-1)]\n" + 
				"\n" + 
				"The dynamic pressure q is defined to be:\n" + 
				"\n" + 
				"Eq #5:\n" + 
				"\n" + 
				"q = (r * v^2) / 2 = (gam * p * M^2) / 2\n" + 
				"\n" + 
				"Using the conservation ofmass,momentum, andenergyand the definition of\n" + 
				"total enthalpyin the flow, we canderive the following relations:\n" + 
				"\n" + 
				"Eq #6:\n" + 
				"\n" + 
				"p / pt = [1 + M^2 * (gam-1)/2]^-[gam/(gam-1)]\n" + 
				"\n" + 
				"Eq #7:\n" + 
				"\n" + 
				"T / Tt = [1 + M^2 * (gam-1)/2]^-1\n" + 
				"\n" + 
				"Eq #8:\n" + 
				"\n" + 
				"r / rt = [1 + M^2 * (gam-1)/2]^-[1/(gam-1)]\n" + 
				"\n" + 
				"Then considering thecompressible mass flow equation.we can derive:\n" + 
				"\n" + 
				"Eq #9:\n" + 
				"\n" + 
				"A / A* = {[1 + M^2 * \n" + 
				"(gam-1)/2]^[(gam+1)/(gam-1)/2]}*{[(gam+1)/2]^-[(gam+1)/(gam-1)/2]} / M\n" + 
				"\n" + 
				"The starred conditions occurwhen the flow is choked and the Mach number is \n" + 
				"equal to one.Notice the important role that the Mach number plays in all \n" + 
				"theequations on the right side of this slide. If the Mach number of theflow \n" + 
				"is determined, all of the other flow relations can bedetermined. Similarly, \n" + 
				"determining any flow relation (pressure ratiofor example) will fix the Mach \n" + 
				"number and set all the other flowconditions.\n" + 
				"\n" + 
				"Here is a JavaScript program that solves the equations given on this slide.\n" + 
				"\n" + 
				"\n" + 
				"You select an input variable by using the choice button labeled \n" + 
				"InputVariable. Next to the selection, you then type in the valueof the \n" + 
				"selected variable. When you hit the red COMPUTE button,the output values \n" + 
				"change. Some of the variables (like the area ratio) are doublevalued. This \n" + 
				"means that for the same area ratio, there is a subsonicand a supersonic \n" + 
				"solution. The choice button at the right top selectsthe solution that is \n" + 
				"presented.The variable \"Wcor/A\" is thecorrected airflow per unit area\n" + 
				"function which can be derived from thecompressible mass flow.This variable \n" + 
				"is only a function of the Mach number of the flow. TheMach angle and\n" + 
				"Prandtl-Meyer angleare also functions of the Mach number.These additional \n" + 
				"variables are used in the design of high speedinlets, nozzles and ducts.\n" + 
				"\n" + 
				"If you are an experienced user of this calculator, you can use asleek \n" + 
				"versionof the program which loads faster on your computer and does not \n" + 
				"include these instructions.You can also download your own copy of the \n" + 
				"program to run off-line by clicking on this button:\n";
		TextProcessor tp = new TextProcessor(new AnswerCurationManager(domainProjectModelFolder, 
				ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(domainProjectModelFolder, null), null, null), null);
		tp.setTextModelPrefix("sos");
		String localityURI = "http://darpa.aske.ta1.ge/sostest";
		tp.setTextModelName(localityURI);
		String msg = tp.clearGraph(localityURI);
		System.out.println("Clear graph response: " + msg);
		int[] result = tp.processText(localityURI, content, localityURI, localityURI, "sos", true);
		assertNotNull(result);
//		assertEquals(0, result[0]);
//		assertEquals(1, result[1]);
		System.out.println("nc=" + result[0] + ", neq=" + result[1]);

	}
	
	@Ignore("this test requires REST services at default URL")
	@Test
	public void testUploadDomainOntologyToService() throws IOException {
		String baseServiceUri = "http://vesuvius063.crd.ge.com:4200";
		String baseUri = "http://sadl.org/ScientificConcepts2.sadl";
		String localityUri = "http://darpa.aske.ta1.ge/sc2";
		File sourceFile = new File(new File(".").getAbsolutePath() + "/resources/");
		File domainProjectFolder = new File(sourceFile + "/M5Snapshot");
		File domainModelFolder = new File(domainProjectFolder.getAbsoluteFile() + "/OwlModels");
		File domainOntologyPath = new File(domainModelFolder.getCanonicalPath() + "/ScientificConcepts2.owl");
		assertTrue(domainOntologyPath.exists());
		SadlUtils su = new SadlUtils();
		String ontologyAsString = su.fileToString(domainOntologyPath);
		TextProcessingServiceInterface tpsi = new TextProcessingServiceInterface(baseServiceUri);
		String response = tpsi.uploadDomainOntology(localityUri, baseUri, ontologyAsString);
		System.out.println(response);
	}
	
	@Ignore("this test requires REST services at default URL")
	@Test
	public void test6WithDomainOntology() throws IOException, ConfigurationException, InvalidInputException {
		String baseServiceUri = "http://vesuvius063.crd.ge.com:4200";
		String localityURI = "http://darpa.aske.ta1.ge/sostest";
		TextProcessingServiceInterface tpsi = new TextProcessingServiceInterface(baseServiceUri);
		String msg = tpsi.clearGraph(localityURI);
		System.out.println("Clear graph response: " + msg);

		String baseUri = "http://sadl.org/ScientificConcepts2.sadl";
		File sourceFile = new File(new File(".").getAbsolutePath() + "/resources/");
		File domainProjectFolder = new File(sourceFile + "/M5Snapshot");
		File domainModelFolder = new File(domainProjectFolder.getAbsoluteFile() + "/OwlModels");
		File domainOntologyPath = new File(domainModelFolder.getCanonicalPath() + "/ScientificConcepts2.owl");
		assertTrue(domainOntologyPath.exists());
		SadlUtils su = new SadlUtils();
		String ontologyAsString = su.fileToString(domainOntologyPath);
		String response = tpsi.uploadDomainOntology(localityURI, baseUri, ontologyAsString);
		System.out.println("Upload domain ontology response: " + response);
		
		File textFile = new File(getTextExtractionPrjFolder() + "/ExtractedModels/Sources/Sound.txt");
		String textContent = readFile(textFile);

		int[] result = tpsi.processText(textContent, localityURI);
		if (result != null) {
			System.out.println("nc=" + result[0] + ", neq=" + result[1]);
		}
		
		List<UnitExtractionResponse> uresult = tpsi.unitExtraction("variable measurement degree Celsius", localityURI);
		assertNotNull(uresult);
		for (UnitExtractionResponse ur : uresult) {
			System.out.println("result=" + ur.toString());
			assertTrue(ur.getRelatedConceptName().equals("temperature"));
			assertTrue(ur.getRelatedConceptURI().equals("http://purl.obolibrary.org/obo/UO_0000005"));
			assertTrue(ur.getUnitName().equals("degree Celsius"));
			assertTrue(ur.getUnitText().equals("degree Celsius"));
			assertTrue(ur.getUnitURI().equals("http://purl.obolibrary.org/obo/UO_0000027"));
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

	@Ignore("this test requires REST services at default URL")
	@Test
	public void testSeeAlso_01() throws ConfigurationException, IOException {
		String content = 
				"The force f is defined as mass m divided by acceleration a. The equation is given as f = m*a.";
		TextProcessor tp = new TextProcessor(new AnswerCurationManager(domainProjectModelFolder, 
				ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(domainProjectModelFolder, null), null, null), null);
		tp.setTextModelPrefix("sos");
		String localityURI = "http://darpa.aske.ta1.ge/sostest/";
		tp.setTextModelName(localityURI);
		String msg = tp.clearGraph(localityURI);
		System.out.println("Clear graph response: " + msg);
		int[] result = tp.processText(localityURI, content, localityURI, localityURI, null, true);
		assertNotNull(result);
//		assertEquals(0, result[0]);
//		assertEquals(1, result[1]);
		System.out.println("nc=" + result[0] + ", neq=" + result[1]);
		OntModel om = tp.getTextModel(localityURI);
		String[] grph = tp.retrieveGraph(localityURI);
		assertTrue(grph.length == 3);
		OntModel newModel = tp.getTextModelConfigMgr().getOntModel(localityURI, grph[2], Scope.INCLUDEIMPORTS, grph[1]);	
		assertNotNull(newModel);
		StmtIterator stmtitr = newModel.listStatements(null, RDFS.seeAlso, (RDFNode)null);
		int cntr = 0;
		List<String> stmtList = new ArrayList<String>();
		while (stmtitr.hasNext()) {
			String stmt = stmtitr.next().toString();
			stmtList.add(stmt);
			System.out.println(stmt);
			cntr++;
		}
		assertEquals(3, cntr);
		Collections.sort(stmtList); 
		assertEquals("[http://darpa.aske.ta1.ge/sostest/acceleration, http://www.w3.org/2000/01/rdf-schema#seeAlso, http://www.wikidata.org/entity/Q11376]", stmtList.get(0));
		assertEquals("[http://darpa.aske.ta1.ge/sostest/force, http://www.w3.org/2000/01/rdf-schema#seeAlso, http://www.wikidata.org/entity/Q11402]", stmtList.get(1));
		assertEquals("[http://darpa.aske.ta1.ge/sostest/mass, http://www.w3.org/2000/01/rdf-schema#seeAlso, http://www.wikidata.org/entity/Q11423]", stmtList.get(2));
	}

//	@Test
//	public void testWithDomainOntolgoy_02() throws ConfigurationException, IOException {
//		String content = 
//				"The force f is defined as mass m divided by acceleration a. The equation is given as f = m*a.";
//		File textFile = new File(getTextExtractionPrjFolder() + "/ExtractedModels/Sources/Isentrop.txt");
//		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
//		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
//		String domainModelName = "http://sadl.org/SpeedOfSound.sadl";
//		acm.setDomainModelName(domainModelName);
//		acm.setDomainModel(cm.loadOntModel(new SadlUtils().fileUrlToFileName(cm.getAltUrlFromPublicUri(domainModelName)), true));
//		
//		IDialogAnswerProvider dapcft = new DialogAnswerProviderConsoleForTest();
//		cm.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, dapcft);
//		
//		String defaultTextModelPrefix = "Isentrop";
//		String defaultTextModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultTextModelPrefix;
//		acm.getExtractionProcessor().getTextProcessor().setTextModelPrefix(defaultTextModelPrefix);
//		acm.getExtractionProcessor().getTextProcessor().setTextModelName(defaultTextModelName);
//		
//		String genFolder = new File(acm.getOwlModelsFolder()).getParent() + 
//		"/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT;
//		new File(genFolder).mkdirs();
//
//		acm.getExtractionProcessor().getTextProcessor().addFile(textFile);
//		SaveAsSadl sas = SaveAsSadl.DoNotSaveAsSadl;
////		SaveAsSadl sas = SaveAsSadl.SaveAsSadl;
////		SaveAsSadl sas = SaveAsSadl.AskUserSaveAsSadl;
//		String owlFileName = genFolder + "/" + textFile.getName() + ".owl";
//		String sadlFileName = owlFileName + ".sadl";
//		File sf = new File(sadlFileName);
//		if (sf.exists()) {
//			sf.delete();
//		}
//		String msg = acm.getExtractionProcessor().getTextProcessor().clearGraph(acm.getExtractionProcessor().getTextProcessor().getTextModelName());
//		System.out.println("Clear graph response: " + msg);
//		
//		acm.getExtractionProcessor().getTextProcessor().addDomainOntology(dialogModelName, domainModel)
//		acm.processImports(sas); 							// ***** this is where the work happens *****
//		OntModel tm = acm.getExtractionProcessor().getTextModel();
//		tm.write(System.out, "N3");
//		tp.setTextModelPrefix("sos");
//		String localityURI = "http://darpa.aske.ta1.ge/sostest/";
//		tp.setTextModelName(localityURI);
//		String msg = tp.clearGraph(localityURI);
//		
//		System.out.println("Clear graph response: " + msg);
//		int[] result = tp.processText(localityURI, content, localityURI, localityURI, null, true);
//		assertNotNull(result);
////		assertEquals(0, result[0]);
////		assertEquals(1, result[1]);
//		System.out.println("nc=" + result[0] + ", neq=" + result[1]);
//		OntModel om = tp.getTextModel(localityURI);
//		String[] grph = tp.retrieveGraph(localityURI);
//		assertTrue(grph.length == 3);
//		OntModel newModel = tp.getTextModelConfigMgr().getOntModel(localityURI, grph[2], Scope.INCLUDEIMPORTS, grph[1]);	
//		assertNotNull(newModel);
//		StmtIterator stmtitr = newModel.listStatements(null, RDFS.seeAlso, (RDFNode)null);
//		int cntr = 0;
//		List<String> stmtList = new ArrayList<String>();
//		while (stmtitr.hasNext()) {
//			String stmt = stmtitr.next().toString();
//			stmtList.add(stmt);
//			System.out.println(stmt);
//			cntr++;
//		}
//		assertEquals(3, cntr);
//		Collections.sort(stmtList); 
//		assertEquals("[http://darpa.aske.ta1.ge/sostest/acceleration, http://www.w3.org/2000/01/rdf-schema#seeAlso, http://www.wikidata.org/entity/Q11376]", stmtList.get(0));
//		assertEquals("[http://darpa.aske.ta1.ge/sostest/force, http://www.w3.org/2000/01/rdf-schema#seeAlso, http://www.wikidata.org/entity/Q11402]", stmtList.get(1));
//		assertEquals("[http://darpa.aske.ta1.ge/sostest/mass, http://www.w3.org/2000/01/rdf-schema#seeAlso, http://www.wikidata.org/entity/Q11423]", stmtList.get(2));
//	}

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

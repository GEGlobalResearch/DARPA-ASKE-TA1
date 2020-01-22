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
package com.ge.research.sadl.darpa.aske.tests;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.diagnostics.Severity;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.validation.Issue;
import org.eclipse.xtext.xbase.lib.IterableExtensions;
import org.eclipse.xtext.xbase.lib.Functions.Function1;
import org.eclipse.xtext.xbase.lib.Procedures.Procedure3;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.SaveAsSadl;
import com.ge.research.sadl.darpa.aske.curation.DialogAnswerProviderConsoleForTest;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.IDialogAnswerProvider;
import com.ge.research.sadl.darpa.aske.processing.JenaBasedDialogModelProcessor;
import com.ge.research.sadl.darpa.aske.processing.SaveContent;
import com.ge.research.sadl.darpa.aske.processing.imports.AnswerExtractionException;
import com.ge.research.sadl.darpa.aske.processing.imports.IModelFromCodeExtractor;
import com.ge.research.sadl.darpa.aske.processing.imports.JavaModelExtractorJP;
import com.ge.research.sadl.jena.IJenaBasedModelProcessor;
import com.ge.research.sadl.jena.JenaBasedSadlModelProcessor;
import com.ge.research.sadl.owl2sadl.OwlImportException;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.vocabulary.RDF;

public class JavaImportJPTests extends AbstractDialogTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(JavaImportJPTests.class);

	private String codeExtractionProjectModelFolder;
	private String domainProjectModelFolder;
	private String codeExtractionKbRoot;

	private String speedOfSoundPath;
	private String scientificConcepts2Path;
	
	@BeforeClass
	public static void init() throws Exception
	{
		// Log4J junit configuration.
		LOGGER.info("INFO TEST");
		LOGGER.debug("DEBUG TEST");
		LOGGER.error("ERROR TEST");
	}

	@Before
	public void setUp() throws Exception {
		File projectRoot = new File("resources/M5Snapshot");
		setCodeExtractionKbRoot(projectRoot.getCanonicalPath());
		File codeExtractionPrjFolder = new File(getCodeExtractionKbRoot());
		assertTrue(codeExtractionPrjFolder.exists());
		setExtractionProjectModelFolder(getCodeExtractionKbRoot() + "/OwlModels");
		setScientificConcepts2(getCodeExtractionKbRoot() + "/ScientificConcepts2.sadl");
		setSpeedOfSoundPath(getCodeExtractionKbRoot() + "/SpeedOfSound.sadl");
		setDomainProjectModelFolder(getExtractionProjectModelFolder());
	}

	@Test
	public void test_01() throws ConfigurationException, IOException {
		String javaContent = 
				"/**\r\n" + 
				" * Copyright 2018 General Electric Company\r\n" + 
				" */\r\n" + 
				"package com.ge.research.answer.java;\r\n" + 
				"\r\n" + 
				"/**\r\n" + 
				" * Class to provide temperature conversions between different units\r\n" + 
				" * @author 200005201\r\n" + 
				" *\r\n" + 
				" */\r\n" + 
				"public class TemperatureConversion {\r\n" + 
				"	/**\r\n" + 
				"	 * Method to convert from F to C\r\n" + 
				"	 * @param tf -- temperature in degrees F\r\n" + 
				"	 * @return -- temperature in degrees C\r\n" + 
				"	 */\r\n" + 
				"	public float convertFtoC(float tf) {\r\n" + 
				"		return (float) ((tf - 32.0) * 9.0 / 5.0);\r\n" + 
				"	}\r\n" + 
				"	\r\n" + 
				"	/**\r\n" + 
				"	 * Method to convert from C to F\r\n" + 
				"	 * @param tc -- temperature in degrees C\r\n" + 
				"	 * @return -- temperature in degrees F\r\n" + 
				"	 */\r\n" + 
				"	public float convertCtoF(float tc) {\r\n" + 
				"		return tc * 5 / 9 + 32;\r\n" + 
				"	}\r\n" + 
				"}\r\n";
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
		IModelFromCodeExtractor jme = acm.getCodeExtractor();
		acm.setOwlModelsFolder(getExtractionProjectModelFolder());
		String defaultCodeModelPrefix = "Temp";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
//		jme.setCodeModelPrefix(defaultCodeModelPrefix);
//		jme.setCodeModelName(defaultCodeModelName);
		jme.setIncludeSerialization(false);
		assertTrue(jme.process("TemperatureConversion class", javaContent, defaultCodeModelName, defaultCodeModelPrefix));
		OntModel codeModel = acm.getExtractionProcessor().getCodeModel();
		codeModel.write(System.out);
	}

	@Test
	public void test_02() throws ConfigurationException, IOException {
		String javaContent = 
				"/*\r\n" + 
				" * Copyright 2018 General Electric Company\r\n" + 
				" */\r\n" + 
				"package com.ge.research.answer.java;\r\n" + 
				"\r\n" + 
				"/*\r\n" + 
				" * Class to provide temperature conversions between different units\r\n" + 
				" * @author 200005201\r\n" + 
				" *\r\n" + 
				" */\r\n" + 
				"public class TemperatureConversion {\r\n" + 
				"	/**\r\n" + 
				"	 * Method to convert from F to C\r\n" + 
				"	 * @param tf -- temperature in degrees F\r\n" + 
				"	 * @return -- temperature in degrees C\r\n" + 
				"	 */\r\n" + 
				"	public float convertFtoC(float tf) {\r\n" + 
				"		return (float) ((tf - 32.0) * 9.0 / 5.0);\r\n" + 
				"	}\r\n" + 
				"	\r\n" + 
				"	/**\r\n" + 
				"	 * Method to convert from C to F\r\n" + 
				"	 * @param tc -- temperature in degrees C\r\n" + 
				"	 * @return -- temperature in degrees F\r\n" + 
				"	 */\r\n" + 
				"	public float convertCtoF(float tc) {\r\n" + 
				"		return tc * 5 / 9 + 32;\r\n" + 
				"	}\r\n" + 
				"}\r\n";
		
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
		IModelFromCodeExtractor jme = new JavaModelExtractorJP(acm, null);
		acm.setOwlModelsFolder(getExtractionProjectModelFolder());
		String defaultCodeModelPrefix = "temp";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		jme.setCodeModelPrefix(defaultCodeModelPrefix);
		jme.setCodeModelName(defaultCodeModelName);
		jme.setIncludeSerialization(false);
		assertTrue(jme.process("TemperatureConversion class", javaContent, null, null));
	}

	@Test
	public void test_03() throws ConfigurationException, IOException {
		String javaContent = 
				"package com.research.ge.darpa.answer.imports;\r\n" + 
				"\r\n" + 
				"/**\r\n" + 
				" * Class to compute various properties of a physical object\r\n" + 
				" * @author 200005201\r\n" + 
				" *\r\n" + 
				" */\r\n" + 
				"public class PhysicalOjbect {\r\n" + 
				"	/**\r\n" + 
				"	 * Method to computer the force on an object of constant mass. \r\n" + 
				"	 * @param mass --  mass of the object\r\n" + 
				"	 * @param massUnit -- unit of mass value\r\n" + 
				"	 * @param acceleration -- acceleration of the object\r\n" + 
				"	 * @param accelerationUnit -- unit of acceleration\r\n" + 
				"	 * @return -- force on the object\r\n" + 
				"	 * @throws MissingUnitsException\r\n" + 
				"	 */\r\n" + 
				"	public float force(float mass, String massUnit, float acceleration, String accelerationUnit) throws MissingUnitsException {\r\n" + 
				"		if (massUnit == null || accelerationUnit == null) {\r\n" + 
				"			throw new MissingUnitsException(\"Units cannot be null\");\r\n" + 
				"		}\r\n" + 
				"		if (massUnit != null && massUnit.equalsIgnoreCase(\"kg\")) {\r\n" + 
				"			if (accelerationUnit != null) {\r\n" + 
				"				if (accelerationUnit.equals(\"m/sec2\")) {\r\n" + 
				"					return mass * acceleration;\r\n" + 
				"				}\r\n" + 
				"			}\r\n" + 
				"		}\r\n" + 
				"		throw new MissingUnitsException(\"Mass must be in 'kg', acceleration in 'm/sec2'\");\r\n" + 
				"	}\r\n" + 
				"}\r\n" + 
				"";
		
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
		IModelFromCodeExtractor jme = new JavaModelExtractorJP(acm, null);
		acm.setOwlModelsFolder(getExtractionProjectModelFolder());
		String defaultCodeModelPrefix = "physicalobject";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		jme.setCodeModelPrefix(defaultCodeModelPrefix);
		jme.setCodeModelName(defaultCodeModelName);
		assertTrue(jme.process("PhysicalObject class", javaContent, defaultCodeModelName, defaultCodeModelPrefix));
	}
	
	@Test
	public void test_04() throws IOException, ConfigurationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException {
		File sourceFile = new File(getCodeExtractionKbRoot() + "/ExtractedModels/Sources/Isentrop.java");
		assertTrue(sourceFile.exists());
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
//		IModelFromCodeExtractor jme = new JavaModelExtractorJP(acm, null);
		acm.setOwlModelsFolder(getExtractionProjectModelFolder());
		String defaultCodeModelPrefix = "Isentrop";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		acm.getCodeExtractor().setCodeModelPrefix(defaultCodeModelPrefix);
		acm.getCodeExtractor().setCodeModelName(defaultCodeModelName);
		acm.getCodeExtractor().setIncludeSerialization(false);
		acm.getCodeExtractor().addCodeFile(sourceFile);
		assertEquals(1, acm.processImports(SaveAsSadl.SaveAsSadl.DoNotSaveAsSadl));
		System.out.println("\n\n\n*** SADL content generated for Dialog window: ***");
		System.out.print(acm.getExtractionProcessor().getGeneratedSadlContent());
		
		try {
			String comPuteQuery = "select ?p ?v where {<Isentrop.comPute> ?p ?v}";
			ResultSet cqrs = acm.getCodeExtractor().executeSparqlQuery(comPuteQuery);
			cqrs.setShowNamespaces(false);
			System.out.println(cqrs.toString());
		}
		catch (Throwable t) {
			
		}
		String allMethods = "select ?m where {?m <rdf:type> <Method>}";
		ResultSet amrs = acm.getCodeExtractor().executeSparqlQuery(allMethods);
		amrs.setShowNamespaces(false);
		System.out.println(amrs.toString());
		String containedInQ = "select ?x ?y where {?x <containedIn> ?y}";
		ResultSet cirs = acm.getCodeExtractor().executeSparqlQuery(containedInQ);
		cirs.setShowNamespaces(false);
		System.out.println(cirs.toString());
	}
	
	@Test
	public void test_05() throws IOException, ConfigurationException, OwlImportException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException {
		File codeFile = new File(getCodeExtractionKbRoot() + "/ExtractedModels/Sources/Mach.java");
		assertTrue(codeFile.exists());
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
		acm.setOwlModelsFolder(getExtractionProjectModelFolder());
		
		IDialogAnswerProvider dapcft = new DialogAnswerProviderConsoleForTest();
		cm.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, dapcft);
		
		boolean includeSerialization = false;
		
		String defaultCodeModelPrefix = includeSerialization ? "MachSz" : "Mach";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		acm.getExtractionProcessor().getCodeExtractor().setCodeModelPrefix(defaultCodeModelPrefix);
		acm.getExtractionProcessor().getCodeExtractor().setCodeModelName(defaultCodeModelName);
		
		String genFolder = new File(acm.getOwlModelsFolder()).getParent() + 
				"/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT;
		new File(genFolder).mkdirs();
//		String owlFileName = genFolder + "/" + defaultCodeModelPrefix + ".owl";

		acm.getExtractionProcessor().getCodeExtractor().addCodeFile(codeFile);
		acm.getExtractionProcessor().getCodeExtractor().setIncludeSerialization(includeSerialization);
		acm.processImports(SaveAsSadl.DoNotSaveAsSadl);
//		acm.processImports(SaveAsSadl.SaveAsSadl);
//		acm.processImports(SaveAsSadl.AskUserSaveAsSadl);

		// Test extraction of methods
//		String query = "select ?m ?b ?e ?s where {?m <rdf:type> <Method> . ?m <doesComputation> true . OPTIONAL {?m <beginsAt> ?b . ?m <endsAt> ?e . ?m <serialization> ?s} .\r\n" + 
//				"		MINUS {\r\n" + 
//				"			{?ref <codeBlock> ?m . ?ref <isImplicit> true}\r\n" + 
//				"			UNION {?m <rdf:type> <ExternalMethod>} } } order by ?m";
//		String query = "select ?m ?comp ?b ?e ?s where {?m <rdf:type> <Method> . ?m <doesComputation> ?comp . OPTIONAL {?m <beginsAt> ?b . ?m <endsAt> ?e . ?m <serialization> ?s} .\r\n" + 
//				"		MINUS {\r\n" + 
//				"			?m <rdf:type> <ExternalMethod>} } order by ?m";
		String query = "select ?m ?comp ?b ?e ?s where {?m <rdf:type> <Method> . "
				+ "OPTIONAL{?m <doesComputation> ?comp} . "
				+ "OPTIONAL{?m <beginsAt> ?b . ?m <endsAt> ?e} . "
				+ "OPTIONAL{?m <serialization> ?s} } order by ?m";
		try {
			ResultSet rs =acm.getCodeExtractor().executeSparqlQuery(query);			
			System.out.println(rs.toStringWithIndent(5));
			int rows = rs.getRowCount();
			assertEquals(13, rows);
			String firstMethod = rs.getResultAt(0, 0).toString();
			assertTrue(firstMethod != null && firstMethod.equals("http://com.ge.research.sadl.darpa.aske.answer/Mach_java#Mach.CAL_GAM"));
			Object script = rs.getResultAt(0, 3);
			String firstMethodScript = script != null ? script.toString() : null;
			if (includeSerialization) {
				assertTrue(firstMethodScript.equals("public double CAL_GAM(double T, double G, double Q) {\r\n" + 
					"    return (1 + (G - 1) / (1 + (G - 1) * (Math.pow((Q / T), 2) * Math.exp(Q / T) / Math.pow((Math.exp(Q / T) - 1), 2))));\r\n" + 
					"}"));
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
		
		// Test extraction of constants
		String cQuery = "select ?c ?v ?u where {?c <rdf:type> <ConstantVariable> . ?c <constantValue> ?uq . ?uq <value> ?v . OPTIONAL{?uq <unit> ?u}} order by ?c";
		try {
			ResultSet crs = acm.getCodeExtractor().executeSparqlQuery(cQuery);
			assertNotNull(crs);
			System.out.println(crs.toString());
			assertTrue(crs.getRowCount() == 9);
			crs.setShowNamespaces(false);
			assertTrue(crs.getResultAt(0, 0).toString().equals("Mach.Q"));
			assertTrue(crs.getResultAt(1, 0).toString().equals("Mach.R"));
			assertTrue(crs.getResultAt(3, 0).toString().equals("Mach.gama"));
			assertTrue(crs.getResultAt(6, 0).toString().equals("Mach.rgas"));
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
	
	@Test
	public void test_06() throws IOException, ConfigurationException, OwlImportException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException {
		// remove OWL and SADL files
		File owlF = new File(getCodeExtractionKbRoot() + "/ExtractedModels\\Mach.java.owl");
		
		if (owlF.exists()) {
			owlF.delete();
			assertFalse(owlF.exists());
		}
		File sadlF = new File(getCodeExtractionKbRoot() + "\\ExtractedModels\\Mach.java.owl.sadl");
		if (sadlF.exists()) {
			sadlF.delete();
			assertFalse(sadlF.exists());
		}
		
		File codeFile = new File(getCodeExtractionKbRoot() + "/ExtractedModels/Sources/Mach.java");
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
		acm.setOwlModelsFolder(getExtractionProjectModelFolder());
		
		IDialogAnswerProvider dapcft = new DialogAnswerProviderConsoleForTest();
		cm.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, dapcft);
		
		boolean includeSerialization = false; //true;
		
		String defaultCodeModelPrefix = includeSerialization ? "MachSz" : "Mach";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		acm.getExtractionProcessor().getCodeExtractor().setCodeModelPrefix(defaultCodeModelPrefix);
		acm.getExtractionProcessor().getCodeExtractor().setCodeModelName(defaultCodeModelName);
		
		String genFolder = new File(acm.getOwlModelsFolder()).getParent() + 
				"/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT;
		new File(genFolder).mkdirs();
//		String owlFileName = genFolder + "/" + defaultCodeModelPrefix + ".owl";

		acm.getExtractionProcessor().getCodeExtractor().addCodeFile(codeFile);
		acm.getExtractionProcessor().getCodeExtractor().setIncludeSerialization(includeSerialization);
//		acm.processImports(SaveAsSadl.AskUserSaveAsSadl);
//		acm.processImports(SaveAsSadl.SaveAsSadl);
		acm.processImports(SaveAsSadl.DoNotSaveAsSadl);
		assertTrue(owlF.exists() || sadlF.exists());
		String sadlContent = acm.getExtractionProcessor().getGeneratedSadlContent();
		System.out.println("\n\n*****  New Dialog editor content *********");
		System.out.println(sadlContent);
	}
	
	@Test
	public void test_07() throws IOException, ConfigurationException, OwlImportException, QueryParseException, QueryCancelledException, ReasonerNotFoundException, InvalidNameException, AnswerExtractionException {
		// test save command given an OWL file generated from a .dialog file is available as input.
		File owlF = new File(getCodeExtractionKbRoot() + "\\OwlModels\\test2.dialog.owl");
		assertTrue(owlF.exists());
		
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
		OntModel om = cm.loadOntModel(owlF.getCanonicalPath(), true);
		String equationToBuildUri = cm.getBaseUriFromOwlFile(owlF.getCanonicalPath()) + "#Mach.CAL_SOS";
		Resource resource = null;
		String modelName = om.getNsPrefixMap().get("");
		SaveContent sc = new SaveContent(null, Agent.USER);
		sc.setSourceEquationUri(equationToBuildUri);
		try {
			String result = acm.processSaveRequest(resource, om, modelName, sc );
			fail("Headless test should not be able to save extraction");
		}
		catch(IOException e) {
			
		}
	}

	@Test
	public void test_08() throws IOException, ConfigurationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException {
		File codeFile = new File(getCodeExtractionKbRoot() + "/ExtractedModels/Sources/Turbo.java");
		assertTrue(codeFile.exists());
		// remove OWL and SADL files
		File owlF = new File(getCodeExtractionKbRoot() + "/ExtractedModels\\Turbo.java.owl");
		
		if (owlF.exists()) {
			owlF.delete();
			assertFalse(owlF.exists());
		}
		File sadlF = new File(getCodeExtractionKbRoot() + "\\ExtractedModels\\Turbo.java.owl.sadl");
		if (sadlF.exists()) {
			sadlF.delete();
			assertFalse(sadlF.exists());
		}
		
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
		acm.setOwlModelsFolder(getExtractionProjectModelFolder());
		
		IDialogAnswerProvider dapcft = new DialogAnswerProviderConsoleForTest();
		cm.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, dapcft);
		
		boolean includeSerialization = false; //true;
		
		String defaultCodeModelPrefix = includeSerialization ? "TurboSz" : "Turbo";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		acm.getExtractionProcessor().getCodeExtractor().setCodeModelPrefix(defaultCodeModelPrefix);
		acm.getExtractionProcessor().getCodeExtractor().setCodeModelName(defaultCodeModelName);
		
		String genFolder = new File(acm.getOwlModelsFolder()).getParent() + 
				"/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT;
		new File(genFolder).mkdirs();
//		String owlFileName = genFolder + "/" + defaultCodeModelPrefix + ".owl";

		acm.getExtractionProcessor().getCodeExtractor().addCodeFile(codeFile);
		acm.getExtractionProcessor().getCodeExtractor().setIncludeSerialization(includeSerialization);
//		acm.processImports(SaveAsSadl.AskUserSaveAsSadl);
		acm.processImports(SaveAsSadl.DoNotSaveAsSadl);
		assertTrue(owlF.exists());
		OntModel om = acm.getCodeExtractor().getCurrentCodeModel();
		StmtIterator stmtItr = om.listStatements(null, RDF.type, om.getOntClass(DialogConstants.CODE_EXTRACTION_MODEL_URI + "#Method"));
		while (stmtItr.hasNext()) {
			System.out.println(stmtItr.next().toString());
		}
		
		String sadlContent = acm.getExtractionProcessor().getGeneratedSadlContent();
		System.out.println("\n\n*****  New Dialog editor content *********");
		System.out.println(sadlContent);
	}

	@Test
	public void test_09() throws IOException, ConfigurationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException {
	    this.sadl(getContent(getScientificConcepts2Path())); 
	    this.sadl(getContent(getSpeedOfSoundPath()));

		File codeFile = new File(getCodeExtractionKbRoot() + "/ExtractedModels/Sources/Turbo.java");
		assertTrue(codeFile.exists());
		// remove OWL and SADL files
		File owlF = new File(getCodeExtractionKbRoot() + "/ExtractedModels\\Turbo.java.owl");

		// start of Dialog file for extraction
		StringBuilder dialogModelContent = new StringBuilder("uri \"http://darpa.aske.ge/test_09\" alias test_09.\r\n" + 
				"import \"http://sadl.org/SpeedOfSound.sadl\".\r\n" + 
				"target model \"http://sadl.org/SpeedOfSound.sadl\" alias sos.\r\n");
		if (owlF.exists()) {
			owlF.delete();
			assertFalse(owlF.exists());
		}
		File sadlF = new File(getCodeExtractionKbRoot() + "\\ExtractedModels\\Turbo.java.owl.sadl");
		if (sadlF.exists()) {
			sadlF.delete();
			assertFalse(sadlF.exists());
		}
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
		acm.setOwlModelsFolder(getExtractionProjectModelFolder());
		
		IDialogAnswerProvider dapcft = new DialogAnswerProviderConsoleForTest();
		cm.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, dapcft);
		
		boolean includeSerialization = true;
		
		String defaultCodeModelPrefix = includeSerialization ? "TurboSz" : "Turbo";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		acm.getExtractionProcessor().getCodeExtractor().setCodeModelPrefix(defaultCodeModelPrefix);
		acm.getExtractionProcessor().getCodeExtractor().setCodeModelName(defaultCodeModelName);
		
		String genFolder = new File(acm.getOwlModelsFolder()).getParent() + 
				"/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT;
		new File(genFolder).mkdirs();
//		String owlFileName = genFolder + "/" + defaultCodeModelPrefix + ".owl";

		acm.getExtractionProcessor().getCodeExtractor().addCodeFile(codeFile);
		acm.getExtractionProcessor().getCodeExtractor().setIncludeSerialization(includeSerialization);
//		acm.processImports(SaveAsSadl.AskUserSaveAsSadl);
		acm.processImports(SaveAsSadl.DoNotSaveAsSadl);
		assertTrue(owlF.exists());
		String sadlContent = acm.getExtractionProcessor().getGeneratedSadlContent();
		dialogModelContent.append(sadlContent);
		dialogModelContent.append("\nSave all.\n");
		
	    final Procedure3<OntModel, List<Issue>, IJenaBasedModelProcessor> _function2 = (OntModel ontModel, List<Issue> issues, IJenaBasedModelProcessor processor) -> {
	        Assert.assertNotNull(ontModel);
	        final Function1<Issue, Boolean> _function_1 = (Issue it) -> {
	          Severity _severity = it.getSeverity();
	          return Boolean.valueOf((_severity == Severity.ERROR));
	        };
	        final Iterable<Issue> errors = IterableExtensions.<Issue>filter(issues, _function_1);
	        Assert.assertEquals(0, IterableExtensions.size(errors));      
	        String modelName = null;
			SaveContent sc = null;
			try {
				if (processor instanceof JenaBasedDialogModelProcessor) {
					AnswerCurationManager acm2 = ((JenaBasedDialogModelProcessor)processor).getAnswerCurationManager();
					acm2.setOwlModelsFolder(getExtractionProjectModelFolder());

					IConfigurationManagerForIDE cm2 = acm2.getConfigurationManager();
							
					cm.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, dapcft);
					
//					acm.processSaveRequest(((JenaBasedSadlModelProcessor)processor).getCurrentResource(), ontModel, modelName, sc);
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
	      };
		this.assertValidatesTo(dialogModelContent, _function2);	
	}
	
	@Test
	public void test_10() throws IOException, ConfigurationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException {
		File sourceFile = new File(getCodeExtractionKbRoot() + "/ExtractedModels/Sources/TurboAnnotated.java");
		assertTrue(sourceFile.exists());
		
		File owlF = new File(getCodeExtractionKbRoot() + "/ExtractedModels\\TurboAnnotated.java.owl");
		
		if (owlF.exists()) {
			owlF.delete();
			assertFalse(owlF.exists());
		}
		File sadlF = new File(getCodeExtractionKbRoot() + "\\ExtractedModels\\TurboAnnotated.java.owl.sadl");
		if (sadlF.exists()) {
			sadlF.delete();
			assertFalse(sadlF.exists());
		}

		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
		acm.setOwlModelsFolder(getExtractionProjectModelFolder());
		String defaultCodeModelPrefix = "TurboAnno";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		acm.getExtractionProcessor().getCodeExtractor().setCodeModelPrefix(defaultCodeModelPrefix);
		acm.getExtractionProcessor().getCodeExtractor().setCodeModelName(defaultCodeModelName);
		acm.getExtractionProcessor().getCodeExtractor().setIncludeSerialization(false);
		String genFolder = new File(acm.getOwlModelsFolder()).getParent() + 
				"/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT;
		new File(genFolder).mkdirs();
//		String owlFileName = genFolder + "/" + defaultCodeModelPrefix + ".owl";

		acm.getExtractionProcessor().getCodeExtractor().addCodeFile(sourceFile);
//		acm.processImports(SaveAsSadl.AskUserSaveAsSadl);
		acm.processImports(SaveAsSadl.DoNotSaveAsSadl);
		assertTrue(owlF.exists());
	}
	
	@Test
	public void test_11() throws ConfigurationException, IOException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException {
		String javaContent = 
				"public class Test_11 {\r\n" + 
				"    public double getAir(double mach, double gamma) {\r\n" + 
				"    /* Utility to get the corrected airflow per area given the Mach number */\r\n" + 
				"      double number,fac1,fac2;\r\n" + 
				"      fac2 = (gamma+1.0)/(2.0*(gamma-1.0)) ;\r\n" + 
				"      fac1 = Math.pow((1.0+.5*(gamma-1.0)*mach*mach),fac2);\r\n" + 
				"      number =  .50161*Math.sqrt(gamma) * mach/ fac1 ;\r\n" + 
				"      return(number) ;\r\n" + 
				"    }\r\n}\r\n";
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
		acm.setOwlModelsFolder(getExtractionProjectModelFolder());
		String defaultCodeModelPrefix = "getair";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		acm.getExtractionProcessor().getCodeExtractor().setCodeModelPrefix(defaultCodeModelPrefix);
		acm.getExtractionProcessor().getCodeExtractor().setCodeModelName(defaultCodeModelName);
		acm.getExtractionProcessor().getCodeExtractor().setIncludeSerialization(true);
		String genFolder = new File(acm.getOwlModelsFolder()).getParent() + 
				"/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT;
		new File(genFolder).mkdirs();
		File aFile = new File("c:/tmp/test_11.java");
		new SadlUtils().stringToFile(aFile, javaContent, false);
		acm.getExtractionProcessor().getCodeExtractor().addCodeFile(aFile);

		acm.processImports(SaveAsSadl.DoNotSaveAsSadl);
		OntModel codeModel = acm.getExtractionProcessor().getCodeModel();
		codeModel.write(System.out);
	}

	private CharSequence getContent(String path) throws IOException {
		File f = new File(path);
		assertTrue(f.exists());
		SadlUtils su = new SadlUtils();
		return su.fileToString(f);
	}

	String getCodeExtractionKbRoot() {
		return codeExtractionKbRoot;
	}

	void setCodeExtractionKbRoot(String codeExtractionKbRoot) {
		this.codeExtractionKbRoot = codeExtractionKbRoot;
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

	private void setSpeedOfSoundPath(String path) {
		this.speedOfSoundPath = path;	
	}

	private String getSpeedOfSoundPath() {
		return speedOfSoundPath;
	}

	private void setScientificConcepts2(String path) {
		this.setScientificConcepts2Path(path);
	}

	private String getScientificConcepts2Path() {
		return scientificConcepts2Path;
	}

	private void setScientificConcepts2Path(String scientificConcepts2Path) {
		this.scientificConcepts2Path = scientificConcepts2Path;
	}

}

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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.curation.DialogAnswerProviderConsoleForTest;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.SaveAsSadl;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.IDialogAnswerProvider;
import com.ge.research.sadl.darpa.aske.processing.imports.IModelFromCodeExtractor;
import com.ge.research.sadl.darpa.aske.processing.imports.JavaModelExtractorJP;
import com.ge.research.sadl.owl2sadl.OwlImportException;
import com.ge.research.sadl.owl2sadl.OwlToSadl;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.ResultSet;
import com.hp.hpl.jena.ontology.OntModel;

public class JavaImportJPTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(JavaImportJPTests.class);

	private String codeExtractionProjectModelFolder;
	private String domainProjectModelFolder;
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
		String codeExtractionKbRoot = "C:/Users/200005201/sadl3-master6/git/DARPA-ASKE-TA1/Ontology/M5";
		File codeExtractionPrjFolder = new File(codeExtractionKbRoot);
		setExtractionProjectModelFolder(codeExtractionPrjFolder.getCanonicalPath() + "/OwlModels");
		
		String domainModelKbRoot = "C:/Users/200005201/sadl3-master6/git/DARPA-ASKE-TA1/Ontology/M5";
		File outputPrjFolder = new File(domainModelKbRoot);
		setDomainProjectModelFolder(outputPrjFolder.getCanonicalPath() + "/OwlModels");
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
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null);
		IModelFromCodeExtractor jme = new JavaModelExtractorJP(acm, null);
		jme.setCodeModelFolder(getExtractionProjectModelFolder());
		String defaultCodeModelPrefix = "Temp";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		jme.setDefaultCodeModelPrefix(defaultCodeModelPrefix);
		jme.setDefaultCodeModelName(defaultCodeModelName);
		jme.setIncludeSerialization(false);
		jme.process("TemperatureConversion class", javaContent);
		
//		String content = smg.generateSadlModel(jme, "http://sadl.org/Temperature.sadl");
//		System.out.println("SADL Model Output:\n" + content);
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
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null);
		IModelFromCodeExtractor jme = new JavaModelExtractorJP(acm, null);
		jme.setCodeModelFolder(getExtractionProjectModelFolder());
		String defaultCodeModelPrefix = "temp";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		jme.setDefaultCodeModelPrefix(defaultCodeModelPrefix);
		jme.setDefaultCodeModelName(defaultCodeModelName);
		jme.setIncludeSerialization(false);
		jme.process("TemperatureConversion class", javaContent);
//		String content = smg.generateSadlModel(jme, "http://sadl.org/Temperature.sadl");
//		System.out.println("SADL Model Output:\n" + content);
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
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null);
		IModelFromCodeExtractor jme = new JavaModelExtractorJP(acm, null);
		jme.setCodeModelFolder(getExtractionProjectModelFolder());
		String defaultCodeModelPrefix = "physicalobject";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		jme.setDefaultCodeModelPrefix(defaultCodeModelPrefix);
		jme.setDefaultCodeModelName(defaultCodeModelName);
		jme.process("PhysicalObject class", javaContent);
//		String content = smg.generateSadlModel(jme, "http://sadl.org/Temperature.sadl");
//		System.out.println("SADL Model Output:\n" + content);
	}
	
	@Test
	public void test_04() throws IOException, ConfigurationException {
		System.out.println(new File(".").getAbsoluteFile().getAbsolutePath());
		File sourceFile = new File(new File(".").getAbsolutePath() + "/resources/Isentrop.java");
//		ClassLoader classLoader = getClass().getClassLoader();
//		File sourceFile = new File(classLoader.getResource("/Isentrop.java").getFile());		
		String javaContent = readFile(sourceFile);
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null);
		IModelFromCodeExtractor jme = new JavaModelExtractorJP(acm, null);
		jme.setCodeModelFolder(getExtractionProjectModelFolder());
		String defaultCodeModelPrefix = "Isentrop";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		jme.setDefaultCodeModelPrefix(defaultCodeModelPrefix);
		jme.setDefaultCodeModelName(defaultCodeModelName);
		jme.setIncludeSerialization(false);
		jme.process("Isentrop.java", javaContent);
//		String content = smg.generateSadlModel(jme, "http://sadl.org/Temperature.sadl");
//		System.out.println("SADL Model Output:\n" + content);
	}
	
	@Test
	public void test_05() throws IOException, ConfigurationException, OwlImportException {
		System.out.println(new File(".").getAbsoluteFile().getAbsolutePath());
		File codeFile = new File(new File(".").getAbsolutePath() + "/resources/Mach.java");
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null);
		acm.getExtractionProcessor().getCodeExtractor().setCodeModelFolder(getExtractionProjectModelFolder());
		
		IDialogAnswerProvider dapcft = new DialogAnswerProviderConsoleForTest();
		cm.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, dapcft);
		
		boolean includeSerialization = true; //false; //true;
		
		String defaultCodeModelPrefix = includeSerialization ? "MachSz" : "Mach";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		acm.getExtractionProcessor().getCodeExtractor().setDefaultCodeModelPrefix(defaultCodeModelPrefix);
		acm.getExtractionProcessor().getCodeExtractor().setDefaultCodeModelName(defaultCodeModelName);
		
		String genFolder = new File(acm.getExtractionProcessor().getCodeExtractor().getCodeModelFolder()).getParent() + 
				"/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT;
		new File(genFolder).mkdirs();
//		String owlFileName = genFolder + "/" + defaultCodeModelPrefix + ".owl";

		acm.getExtractionProcessor().getCodeExtractor().addCodeFile(codeFile);
		acm.getExtractionProcessor().getCodeExtractor().setIncludeSerialization(includeSerialization);
		acm.processImports(SaveAsSadl.SaveAsSadl);
		
		String query = "select ?m ?b ?e ?s where {?m <rdf:type> <Method> . ?m <doesComputation> true . OPTIONAL {?m <beginsAt> ?b . ?m <endsAt> ?e . ?m <serialization> ?s} .\r\n" + 
				"		MINUS {\r\n" + 
				"			{?ref <codeBlock> ?m . ?ref <isImplicit> true}\r\n" + 
				"			UNION {?m <rdf:type> <ExternalMethod>} } }";
		try {
			ResultSet rs =acm.getCodeExtractor().executeSparqlQuery(query);
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
		
		
//		jme.process("Mach.java", javaContent, includeSerialization);
//		String content = smg.generateSadlModel(jme, "http://sadl.org/Temperature.sadl");
//		System.out.println("SADL Model Output:\n" + content);
//		cm.saveOwlFile(acm.getExtractionProcessor().getCodeModel(), 
//				acm.getExtractionProcessor().getCodeModelName(), owlFileName);
//		OwlToSadl ots = new OwlToSadl(acm.getExtractionProcessor().getCodeModel());
//		String sadlFN = owlFileName + ".sadl";
//		File sf = new File(sadlFN);
//		if (sf.exists()) {
//			sf.delete();
//		}
//		ots.saveSadlModel(owlFileName + ".sadl");
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

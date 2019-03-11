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
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.SaveAsSadl;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.imports.JavaModelExtractorJP;
import com.ge.research.sadl.owl2sadl.OwlImportException;
import com.ge.research.sadl.owl2sadl.OwlToSadl;
import com.ge.research.sadl.reasoner.ConfigurationException;

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
		String codeExtractionKbRoot = "C:/Users/200005201/sadl3-master6/git/DARPA-ASKE-TA1/Ontology/M5-CodeModel";
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
		JavaModelExtractorJP jme = new JavaModelExtractorJP(acm, null);
		jme.setCodeModelFolder(getExtractionProjectModelFolder());
		String defaultCodeModelPrefix = "Temp";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		jme.setDefaultCodeModelPrefix(defaultCodeModelPrefix);
		jme.setDefaultCodeModelName(defaultCodeModelName);
		jme.process("TemperatureConversion class", javaContent,false);
		
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
		JavaModelExtractorJP jme = new JavaModelExtractorJP(acm, null);
		jme.setCodeModelFolder(getExtractionProjectModelFolder());
		String defaultCodeModelPrefix = "temp";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		jme.setDefaultCodeModelPrefix(defaultCodeModelPrefix);
		jme.setDefaultCodeModelName(defaultCodeModelName);
		jme.process("TemperatureConversion class", javaContent, false);
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
		JavaModelExtractorJP jme = new JavaModelExtractorJP(acm, null);
		jme.setCodeModelFolder(getExtractionProjectModelFolder());
		String defaultCodeModelPrefix = "physicalobject";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		jme.setDefaultCodeModelPrefix(defaultCodeModelPrefix);
		jme.setDefaultCodeModelName(defaultCodeModelName);
		jme.process("PhysicalObject class", javaContent, false);
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
		JavaModelExtractorJP jme = new JavaModelExtractorJP(acm, null);
		jme.setCodeModelFolder(getExtractionProjectModelFolder());
		String defaultCodeModelPrefix = "Isentrop";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		jme.setDefaultCodeModelPrefix(defaultCodeModelPrefix);
		jme.setDefaultCodeModelName(defaultCodeModelName);
		jme.process("Isentrop.java", javaContent, false);
//		String content = smg.generateSadlModel(jme, "http://sadl.org/Temperature.sadl");
//		System.out.println("SADL Model Output:\n" + content);
	}
	
	@Test
	public void test_05() throws IOException, ConfigurationException, OwlImportException {
		System.out.println(new File(".").getAbsoluteFile().getAbsolutePath());
		File sourceFile = new File(new File(".").getAbsolutePath() + "/resources/Mach.java");
//		ClassLoader classLoader = getClass().getClassLoader();
//		File sourceFile = new File(classLoader.getResource("/Isentrop.java").getFile());		
//		String javaContent = readFile(sourceFile);
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null);
		acm.getExtractionProcessor().getCodeExtractor().setCodeModelFolder(getExtractionProjectModelFolder());
		
		DialogAnswerProviderConsoleForTest dapcft = new DialogAnswerProviderConsoleForTest();
		cm.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, dapcft);
		
		boolean includeSerialization = true;
		
		String defaultCodeModelPrefix = includeSerialization ? "MachSz" : "Mach";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		acm.getExtractionProcessor().getCodeExtractor().setDefaultCodeModelPrefix(defaultCodeModelPrefix);
		acm.getExtractionProcessor().getCodeExtractor().setDefaultCodeModelName(defaultCodeModelName);
		
		String genFolder = new File(acm.getExtractionProcessor().getCodeExtractor().getCodeModelFolder()).getParent() + "/GeneratedModels";
		new File(genFolder).mkdirs();
		String owlFileName = genFolder + "/" + defaultCodeModelPrefix + ".owl";

		acm.getExtractionProcessor().getCodeExtractor().addCodeFile(sourceFile);
		acm.processImports(defaultCodeModelPrefix + ".owl", SaveAsSadl.SaveAsSadl.SaveAsSadl);
		
//		jme.process("Mach.java", javaContent, includeSerialization);
//		String content = smg.generateSadlModel(jme, "http://sadl.org/Temperature.sadl");
//		System.out.println("SADL Model Output:\n" + content);
//		cm.saveOwlFile(acm.getExtractionProcessor().getCodeModel(), 
//				acm.getExtractionProcessor().getCodeModelName(), owlFileName);
		OwlToSadl ots = new OwlToSadl(acm.getExtractionProcessor().getCodeModel());
		String sadlFN = owlFileName + ".sadl";
		File sf = new File(sadlFN);
		if (sf.exists()) {
			sf.delete();
		}
		ots.saveSadlModel(owlFileName + ".sadl");
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

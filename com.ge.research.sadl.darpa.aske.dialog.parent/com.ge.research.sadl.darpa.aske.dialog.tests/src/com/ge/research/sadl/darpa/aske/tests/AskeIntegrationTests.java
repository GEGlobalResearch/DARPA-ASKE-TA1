package com.ge.research.sadl.darpa.aske.tests;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;

import org.apache.jena.ontology.OntModel;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Ignore;
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
import com.ge.research.sadl.darpa.aske.processing.SaveContent;
import com.ge.research.sadl.darpa.aske.processing.imports.AnswerExtractionException;
import com.ge.research.sadl.reasoner.AmbiguousNameException;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.TranslationException;

public class AskeIntegrationTests {
	private static final Logger LOGGER = LoggerFactory.getLogger(AskeIntegrationTests.class);

	private String extractionProjectModelFolder;
	private String domainProjectModelFolder;
	private String extractionKbRoot;
	
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
		setExtractionKbRoot(projectRoot.getCanonicalPath());
		File codeExtractionPrjFolder = new File(getExtractionKbRoot());
		assertTrue(codeExtractionPrjFolder.exists());
		setExtractionProjectModelFolder(getExtractionKbRoot() + "/OwlModels");
		
		setDomainProjectModelFolder(getExtractionProjectModelFolder());
	}

	@Ignore("this test requires text extraction and java to python services to be running at default URLs")
	@Test
	public void test_01() throws ConfigurationException, IOException, QueryParseException, QueryCancelledException, ReasonerNotFoundException, InvalidNameException, AnswerExtractionException, InvalidInputException, AmbiguousNameException, TranslationException {
		// remove OWL file
		File owlF = new File(getExtractionKbRoot() + "/ExtractedModels/Mach.java.owl");	
		if (owlF.exists()) {
			owlF.delete();
			assertFalse(owlF.exists());
		}
		File codeFile = new File(getExtractionKbRoot() + "/ExtractedModels/Sources/Mach.java");
		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
		acm.setOwlModelsFolder(getExtractionProjectModelFolder());
		
		IDialogAnswerProvider dapcft = new DialogAnswerProviderConsoleForTest();
		cm.addPrivateKeyMapValueByResource(DialogConstants.DIALOG_ANSWER_PROVIDER, acm.getResourceUri(), dapcft);
		
		boolean includeSerialization = true; //false; //true;	
		String defaultCodeModelPrefix = includeSerialization ? "MachSz" : "Mach";
		String defaultCodeModelName = "http://com.ge.research.darpa.aske.ta1.explore/" + defaultCodeModelPrefix;
		acm.getExtractionProcessor().getCodeExtractor().setCodeModelPrefix(defaultCodeModelPrefix);
		acm.getExtractionProcessor().getCodeExtractor().setCodeModelName(defaultCodeModelName);
		
		String genFolder = new File(acm.getOwlModelsFolder()).getParent() + 
				"/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT;
		new File(genFolder).mkdirs();
		acm.getExtractionProcessor().getCodeExtractor().addCodeFile(codeFile);
		acm.getExtractionProcessor().getCodeExtractor().setIncludeSerialization(includeSerialization);
		acm.processImports(SaveAsSadl.DoNotSaveAsSadl);
		assertTrue(owlF.exists());
		
		// Identify interesting computation methods and generate OWL file containing these methods using SadlImplicitModel metamodel--this is the interestingMethodOntModel
// TODO must get a valid OWL model containing method translated to OWL		
		OntModel interestingMethodOntModel = acm.getExtractionProcessor().getCodeModel();
		// now build CAL_SOS model in K-CHAIN
		String modelUri = "CAL_SOS";
		// add to KG: 
		Resource resource = null;
		String modelName = interestingMethodOntModel.getNsPrefixMap().get("");
		SaveContent sc = new SaveContent(null, Agent.USER);
		String equationToBuildUri = defaultCodeModelName + "#Mach.CAL_SOS";
		sc.setSourceEquationUri(equationToBuildUri);
		acm.setDomainModel(interestingMethodOntModel);
		acm.setDomainModelName(modelName);
		String result = acm.processSaveRequest(resource, sc );
	}

	private String getExtractionProjectModelFolder() {
		return extractionProjectModelFolder;
	}

	private void setExtractionProjectModelFolder(String codeExtractionProjectModelFolder) {
		this.extractionProjectModelFolder = codeExtractionProjectModelFolder;
	}

	private String getDomainProjectModelFolder() {
		return domainProjectModelFolder;
	}

	private void setDomainProjectModelFolder(String domainProjectModelFolder) {
		this.domainProjectModelFolder = domainProjectModelFolder;
	}

	private String getExtractionKbRoot() {
		return extractionKbRoot;
	}

	private void setExtractionKbRoot(String extractionKbRoot) {
		this.extractionKbRoot = extractionKbRoot;
	}

}

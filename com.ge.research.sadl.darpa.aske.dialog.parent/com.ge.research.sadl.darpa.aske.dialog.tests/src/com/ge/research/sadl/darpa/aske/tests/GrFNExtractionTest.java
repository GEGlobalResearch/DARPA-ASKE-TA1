/**
 * 
 */
package com.ge.research.sadl.darpa.aske.tests;

import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

//import org.apache.jena.ontology.OntModel;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

//import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
//import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
//import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
//import com.ge.research.sadl.darpa.aske.processing.imports.IModelFromCodeExtractor;
//import com.ge.research.sadl.darpa.aske.processing.imports.AnswerExtractionProcessor.CodeLanguage;
import com.ge.research.sadl.darpa.aske.processing.imports.GrFNModelExtractor;
import com.ge.research.sadl.reasoner.ConfigurationException;

/**
 * @author alfredo
 *
 */
public class GrFNExtractionTest {
	private static final Logger LOGGER = LoggerFactory.getLogger(GrFNExtractionTest.class);

	private String grFNExtractionProjectModelFolder;
	private String domainProjectModelFolder;
	private String grFNExtractionKbRoot;
	
//	private String outputOwlFileName;
	
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
		File projectRoot = new File("resources/GraSEN");
		setGrFNExtractionKbRoot(projectRoot.getCanonicalPath());
		File grFNExtractionPrjFolder = new File(getGrFNExtractionKbRoot());
		assertTrue(grFNExtractionPrjFolder.exists());
		setExtractionProjectModelFolder(getGrFNExtractionKbRoot() + "/OwlModels");
		setDomainProjectModelFolder(getExtractionProjectModelFolder());
	}

	@Test
	public void test_01() throws ConfigurationException, IOException {
		String grFNJsonContent = 
				"{\n"
				+ "  \"uid\": \"ebe21368-98c7-5205-1e01-a934402d0baf\",\n"
				+ "  \"entry_point\": \"@container::GE_simple_PI_controller::GE_simple_PI_controller::GE_simple_PI_controller.main\",\n"
				+ "  \"timestamp\": \"2021-05-14\",\n"
				+ "  \"hyper_edges\": [\n"
				+ "    {\n"
				+ "      \"inputs\": [\n"
				+ "        \"f728b4fa-4248-5e3a-0a5d-2f346baa9455\",\n"
				+ "        \"eb1167b3-67a9-c378-7c65-c1e582e2e662\"\n"
				+ "      ],\n"
				+ "      \"function\": \"ba26d851-35e8-579a-7aaf-0e891fb797fa\",\n"
				+ "      \"outputs\": [\n"
				+ "        \"1846d424-c17c-6279-23c6-612f48268673\"\n"
				+ "      ]\n"
				+ "    }\n"
				+ "  ],\n"
				+ "  \"variables\": [\n"
				+ "    {\n"
				+ "      \"uid\": \"f728b4fa-4248-5e3a-0a5d-2f346baa9455\",\n"
				+ "      \"identifier\": \"GE_simple_PI_controller::GE_simple_PI_controller.PI_calc::Input_dmd::-1\",\n"
				+ "      \"object_ref\": null,\n"
				+ "      \"metadata\": [\n"
				+ "        {\n"
				+ "          \"type\": \"domain\",\n"
				+ "          \"provenance\": {\n"
				+ "            \"method\": \"program_analysis_pipeline\",\n"
				+ "            \"timestamp\": \"2021-05-14 16:28:20.565330\"\n"
				+ "          },\n"
				+ "          \"data_type\": \"integer\",\n"
				+ "          \"measurement_scale\": \"discrete\",\n"
				+ "          \"elements\": []\n"
				+ "        },\n"
				+ "        {\n"
				+ "          \"type\": \"code_span_reference\",\n"
				+ "          \"provenance\": {\n"
				+ "            \"method\": \"program_analysis_pipeline\",\n"
				+ "            \"timestamp\": \"2021-05-14 16:28:20.563284\"\n"
				+ "          },\n"
				+ "          \"code_type\": \"identifier\",\n"
				+ "          \"code_file_reference_uid\": \"\",\n"
				+ "          \"code_span\": {\n"
				+ "            \"line_begin\": 8,\n"
				+ "            \"line_end\": null,\n"
				+ "            \"col_begin\": 23,\n"
				+ "            \"col_end\": null\n"
				+ "          }\n"
				+ "        }\n"
				+ "      ]\n"
				+ "    }\n"
				+ "  ],\n"
				+ "  \"functions\": [\n"
				+ "    {\n"
				+ "      \"uid\": \"ba26d851-35e8-579a-7aaf-0e891fb797fa\",\n"
				+ "      \"type\": \"ASSIGN\",\n"
				+ "      \"lambda\": \"lambda Input_dmd,Input_sensed: (Input_dmd - Input_sensed)\",\n"
				+ "      \"metadata\": [\n"
				+ "        {\n"
				+ "          \"type\": \"code_span_reference\",\n"
				+ "          \"provenance\": {\n"
				+ "            \"method\": \"program_analysis_pipeline\",\n"
				+ "            \"timestamp\": \"2021-05-14 16:28:20.564182\"\n"
				+ "          },\n"
				+ "          \"code_type\": \"block\",\n"
				+ "          \"code_file_reference_uid\": \"\",\n"
				+ "          \"code_span\": {\n"
				+ "            \"line_begin\": 10,\n"
				+ "            \"line_end\": null,\n"
				+ "            \"col_begin\": 12,\n"
				+ "            \"col_end\": null\n"
				+ "          }\n"
				+ "        }\n"
				+ "      ]\n"
				+ "    }\n"
				+ "  ],\n"
				+ "  \"subgraphs\": [\n"
				+ "    {\n"
				+ "      \"uid\": \"31d0b664-0589-f877-9b02-52440950fd13\",\n"
				+ "      \"namespace\": \"GE_simple_PI_controller\",\n"
				+ "      \"scope\": \"GE_simple_PI_controller\",\n"
				+ "      \"basename\": \"GE_simple_PI_controller.PI_calc\",\n"
				+ "      \"occurrence_num\": 1,\n"
				+ "      \"parent\": \"176ea1b1-6426-4cd5-1ea4-5cd69371a71f\",\n"
				+ "      \"type\": \"FuncContainer\",\n"
				+ "      \"border_color\": \"forestgreen\",\n"
				+ "      \"nodes\": [\n"
//				+ "        \"b7d6467b-2f5a-522a-f87f-43fdf6062541\",\n"
				+ "        \"f728b4fa-4248-5e3a-0a5d-2f346baa9455\",\n" 
//				+ "        \"eb1167b3-67a9-c378-7c65-c1e582e2e662\",\n"
//				+ "        \"f7c1bd87-4da5-e709-d471-3d60c8a70639\",\n"
//				+ "        \"e443df78-9558-867f-5ba9-1faf7a024204\",\n"
//				+ "        \"23a7711a-8133-2876-37eb-dcd9e87a1613\",\n"
//				+ "        \"fcbd04c3-4021-2ef7-cca5-a5a19e4d6e3c\",\n"
//				+ "        \"1846d424-c17c-6279-23c6-612f48268673\",\n"
				+ "        \"ba26d851-35e8-579a-7aaf-0e891fb797fa\"\n" 
//				+ "        \"b4862b21-fb97-d435-8856-1712e8e5216a\",\n"
//				+ "        \"ade9b2b4-efdd-35f8-0fa3-4266ccfdba9b\",\n"
//				+ "        \"259f4329-e6f4-590b-9a16-4106cf6a659e\",\n"
//				+ "        \"9edfa3da-6cf5-5b15-8b53-031d05d51433\",\n"
//				+ "        \"11ebcd49-428a-1c22-d5fd-b76a19fbeb1d\"\n"
				+ "      ],\n"
				+ "      \"metadata\": [\n"
				+ "        {\n"
				+ "          \"type\": \"code_span_reference\",\n"
				+ "          \"provenance\": {\n"
				+ "            \"method\": \"program_analysis_pipeline\",\n"
				+ "            \"timestamp\": \"2021-05-14 16:28:20.564255\"\n"
				+ "          },\n"
				+ "          \"code_type\": \"block\",\n"
				+ "          \"code_file_reference_uid\": \"\",\n"
				+ "          \"code_span\": {\n"
				+ "            \"line_begin\": 9,\n"
				+ "            \"line_end\": 15,\n"
				+ "            \"col_begin\": null,\n"
				+ "            \"col_end\": null\n"
				+ "          }\n"
				+ "        }\n"
				+ "      ]\n"
				+ "    }\n"
				+ "  ]\n"
				+ "}";
//		IConfigurationManagerForIDE cm = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getDomainProjectModelFolder(), null);
//		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), cm, null, null);
//		AnswerCurationManager acm = new AnswerCurationManager(getDomainProjectModelFolder(), null, null, null);
//		acm.getCodeExtractor(CodeLanguage.GrFN);
//		acm.setOwlModelsFolder(getExtractionProjectModelFolder());
		String defaultCodeModelPrefix = "test_grfn_json";
		String defaultCodeModelName = "http://aske.ge.com/" + defaultCodeModelPrefix;
		String outputOwlFileName = "test_grfn_json.owl";
//		acm.getCodeExtractor().setCodeModelPrefix(defaultCodeModelPrefix);
//		acm.getCodeExtractor().setCodeModelName(defaultCodeModelName);

		GrFNModelExtractor grfnExtractor = new GrFNModelExtractor(null,null);
		grfnExtractor.setOwlModelsFolder(getExtractionProjectModelFolder());
		grfnExtractor.setCodeModelPrefix(defaultCodeModelPrefix);
		grfnExtractor.setCodeModelName(defaultCodeModelName);
		
		
//		assertTrue(acm.getCodeExtractor().process("some grfn file identifier", grFNJsonContent, defaultCodeModelName, defaultCodeModelPrefix));
		assertTrue(grfnExtractor.process("some grfn file identifier", grFNJsonContent, defaultCodeModelName, defaultCodeModelPrefix));
		
//		OntModel codeModel = acm.getExtractionProcessor().getCodeModel();
//		OntModel codeModel = acm.getCodeExtractor().getCurrentCodeModel(); //use this line
//		codeModel = acm.getCodeExtractor().getCurrentCodeModel(); //alternative to above line
//		codeModel.write(System.out);
//		File of = acm.saveCodeOwlFile(outputOwlFileName);
		File of = grfnExtractor.saveGrFNOwlFile(outputOwlFileName);

		Map<File, Integer> outputOwlFilesBySourceType = new HashMap<File, Integer>();
		outputOwlFilesBySourceType.put(of, 2);
//		List<String> sadlFilenames = acm.saveAsSadlFile(outputOwlFilesBySourceType, "yes");
		List<String> sadlFilenames = grfnExtractor.saveAsSadlFile(outputOwlFilesBySourceType, "yes");
		assertTrue(sadlFilenames.size() > 0);
	}
	

	
	
	
	private String getDomainProjectModelFolder() {
		return domainProjectModelFolder;
	}

	private void setDomainProjectModelFolder(String outputProjectModelFolder) {
		this.domainProjectModelFolder = outputProjectModelFolder;
	}
	String getGrFNExtractionKbRoot() {
		return grFNExtractionKbRoot;
	}

	void setGrFNExtractionKbRoot(String grFNExtractionKbRoot) {
		this.grFNExtractionKbRoot = grFNExtractionKbRoot;
	}
	private String getExtractionProjectModelFolder() {
		return grFNExtractionProjectModelFolder;
	}

	private void setExtractionProjectModelFolder(String extractionProjectModelFolder) {
		this.grFNExtractionProjectModelFolder = extractionProjectModelFolder;
	}
}

package com.ge.research.sadl.darpa.aske.curation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.HttpURLConnection;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.IDialogAnswerProvider;
import com.ge.research.sadl.darpa.aske.processing.imports.AnswerExtractionProcessor;
import com.ge.research.sadl.owl2sadl.OwlImportException;
import com.ge.research.sadl.owl2sadl.OwlToSadl;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.IConfigurationManagerForEditing.Scope;
import com.ge.research.sadl.reasoner.IReasoner;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class AnswerCurationManager {
	private String domainModelowlModelsFolder;
	
	private IConfigurationManagerForIDE domainModelConfigurationManager;
	private Map<String, String> preferences = null;
	private AnswerExtractionProcessor extractionProcessor = null;

	private IDialogAnswerProvider dialogAnswerProvider = null;	// The instance of an implementer of a DialogAnswerProvider
	public enum SaveAsSadl{SaveAsSadl, DoNotSaveAsSadl, AskUserSaveAsSadl}
	
	public AnswerCurationManager (String modelFolder, IConfigurationManagerForIDE configMgr, Map<String,String> prefs) {
		setDomainModelOwlModelsFolder(modelFolder);
		setDomainModelConfigurationManager(configMgr);
		setPreferences(prefs);
	}

	public String getDomainModelOwlModelsFolder() {
		return domainModelowlModelsFolder;
	}

	private void setDomainModelOwlModelsFolder(String owlModelsFolder) {
		this.domainModelowlModelsFolder = owlModelsFolder;
	}

	public IConfigurationManagerForIDE getDomainModelConfigurationManager() {
		return domainModelConfigurationManager;
	}

	private void setDomainModelConfigurationManager(IConfigurationManagerForIDE projectConfigurationManager) {
		this.domainModelConfigurationManager = projectConfigurationManager;
	}

	private Map<String, String> getPreferences() {
		return preferences;
	}

	private void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}

	public Object getTextProcessor() {
		// TODO Auto-generated method stub
		return null;
	}

	public AnswerExtractionProcessor getExtractionProcessor() {
		if (extractionProcessor == null) {
			extractionProcessor = new AnswerExtractionProcessor(this, getPreferences());
		}
		return extractionProcessor;
	}

	public void setExtractionProcessor(AnswerExtractionProcessor extractionProcessor) {
		this.extractionProcessor = extractionProcessor;
	}

	/**
	 * Method to process a set of imports of text and/or code
	 * @param outputFilename 
	 * @throws IOException
	 * @throws ConfigurationException 
	 */
	public void processImports(String outputFilename, SaveAsSadl saveAsSadl) throws IOException, ConfigurationException {
		if (outputFilename.endsWith(".sadl")) {
			outputFilename = outputFilename.substring(0, outputFilename.length() - 5) + ".owl";
		}
		String defPrefix;
		if (outputFilename.endsWith(".owl")) {
			defPrefix = outputFilename.substring(0, outputFilename.length() - 4);
		}
		else {
			defPrefix = outputFilename;
		}
		String defName = "http://com.ge.research.sadl.darpa.aske.answer/" + defPrefix;
		if (getExtractionProcessor().getCodeExtractor().getDefaultCodeModelName() == null) {
			getExtractionProcessor().getCodeExtractor().setDefaultCodeModelName(defName);
		}
		if (getExtractionProcessor().getCodeExtractor().getDefaultCodeModelPrefix() == null) {
			getExtractionProcessor().getCodeExtractor().setDefaultCodeModelPrefix(defPrefix);
		}

		List<File> textFiles = getExtractionProcessor().getTextProcessor().getTextFiles();
		if (textFiles != null) {
			for (File f : textFiles) {
				String content = readFileToString(f);
				getExtractionProcessor().getTextProcessor().process(f.getCanonicalPath(), content, null);
			}
		}
		
		List<File> codeFiles = getExtractionProcessor().getCodeExtractor().getCodeFiles();
		String outputOwlFileName = null;
		if (codeFiles != null) {
			for (File f : codeFiles) {
				String content = readFileToString(f);
				String fileIdentifier = ConfigurationManagerForIdeFactory.formatPathRemoveBackslashes(f.getCanonicalPath());
				getExtractionProcessor().getCodeExtractor().process(fileIdentifier, content, true);				
			}
			File of = new File(new File(getExtractionProcessor().getCodeExtractor().getCodeModelFolder()).getParent() + 
					"/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT + "/" + outputFilename);
			of.getParentFile().mkdirs();
			getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr().saveOwlFile(getExtractionProcessor().getCodeModel(), getExtractionProcessor().getCodeModelName(), of.getCanonicalPath());
			outputOwlFileName = of.getCanonicalPath();			

			// Don't do this here; do it if and only if the OWL file isn't saved as SADL file.
			String altUrl;
			try {
				altUrl = (new SadlUtils()).fileNameToFileUrl(outputOwlFileName);
				getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr().addMapping(altUrl, getExtractionProcessor().getCodeModelName(), getExtractionProcessor().getCodeModelPrefix(), false, "AnswerCurationManager");
				getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr().addJenaMapping(getExtractionProcessor().getCodeModelName(), altUrl);
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		// run inference on the model, interact with user to refine results
		IReasoner reasoner = getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr().getReasoner();
		String codeModelFolder = getExtractionProcessor().getCodeExtractor().getCodeModelFolder();
		if (reasoner == null) {
			// use domain model folder because that's the project we're working in
			notifyUser(codeModelFolder, "Unable to instantiate reasoner to analyze extracted code model.");
		}
		else {
			if (!reasoner.isInitialized()) {
				IConfigurationManagerForIDE codeModelConfigMgr = getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr();
				reasoner.setConfigurationManager(codeModelConfigMgr);
				try {
					reasoner.initializeReasoner(codeModelFolder, getExtractionProcessor().getCodeModelName(), null);
					String queryString = "select ?m where {?m <rdf:type> <Method> . ?m <cmArguments> ?args}";
					queryString = reasoner.prepareQuery(queryString);
					ResultSet results =  reasoner.ask(queryString);
					if (results != null && results.getRowCount() > 0) {
						results.setShowNamespaces(false);
						notifyUser(codeModelFolder, "Interesting methods found in extraction:\n" + results.toStringWithIndent(0, false));
					}
					else {
						notifyUser(codeModelFolder, "No interesting models were found in this extraction from code.");
					}
				} catch (ReasonerNotFoundException e) {
					notifyUser(codeModelFolder, e.getMessage());
					e.printStackTrace();
				} catch (InvalidNameException e) {
					notifyUser(codeModelFolder, e.getMessage());
					e.printStackTrace();
				} catch (QueryParseException e) {
					notifyUser(codeModelFolder, e.getMessage());
					e.printStackTrace();
				} catch (QueryCancelledException e) {
					notifyUser(codeModelFolder, e.getMessage());
					e.printStackTrace();
				}
			}
		}
		
		if (saveAsSadl != null) {
			if (saveAsSadl.equals(SaveAsSadl.AskUserSaveAsSadl)) {
				// ask user if they want a SADL file saved
				IDialogAnswerProvider dap = getDialogAnswerProvider();
				if (dap == null) {
					dap = new DialogAnswerProviderConsoleForTest();
				}
				List<Object> args = new ArrayList<Object>();
				args.add(outputOwlFileName);
// TODO add argument for model folder so we know which project to use in any followup inquiries.				
				dap.addCurationManagerInitiatedContent(this, "saveAsSadlFile", args, "Would you like to save the extracted model in SADL format?");
			}
			if (saveAsSadl != null && saveAsSadl.equals(SaveAsSadl.SaveAsSadl)) {
				saveAsSadlFile(outputOwlFileName, "yes");
			}
		}
	}
	
	public String processBuildRequest(String modelToBuildUri) throws ConfigurationException, IOException {
		OntModel codeModel = getExtractionProcessor().getCodeModel();
		if (codeModel == null) {
			codeModel = getDomainModelConfigurationManager().getOntModel(modelToBuildUri.substring(0, modelToBuildUri.indexOf("#")), Scope.INCLUDEIMPORTS);
			if (codeModel != null) {
				getExtractionProcessor().setCodeModel(codeModel);
			}
			else {
				return("Failed: Unable to find semantic mmodel");
			}
		}
		Individual extractedModelInstance = codeModel.getIndividual(modelToBuildUri);
		if (extractedModelInstance == null) {
			// try getting a text extraction model?
		}
		if (extractedModelInstance != null) {
			OntModel model = getExtractionProcessor().getCodeModel();
			RDFNode codeNode = extractedModelInstance.getPropertyValue(model.getProperty(DialogConstants.CODE_EXTRACTION_MODEL_SERIALIZATION_PROPERTY_URI));
			if (codeNode.isLiteral()) {
				String javaCode = codeNode.asLiteral().getValue().toString();
				// translate code to Python
				String baseJ2PServiceUri = getPreferences().get(DialogPreferences.ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI.getId());
				String pythonCode = javaMethodToPython(baseJ2PServiceUri, javaCode);
				// extract method
				String pythonMethodCode = removeClassWrapper(pythonCode);
				// create ExternalEquation in domain model
				
				String baseServiceUri = getPreferences().get(DialogPreferences.ANSWER_CG_SERVICE_BASE_URI.getId());
				String modelUri = extractedModelInstance.getURI();
				String equationModel = pythonMethodCode;
				List<String[]> inputs = null;
				List<String[]> outputs = null;
				// call kchain build service to instantiate in CG
//				buildCGModel(baseServiceUri, modelUri, equationModel, null, inputs, outputs);
				return "Successfully build K-CHAIN physics model with URI '" + modelToBuildUri + "'.";
			}
			else if (codeNode == null) {
				return("Failed: No code found for model '" + modelToBuildUri + "'");
			}
			else {
				return("Failed: Code of unexpected type: " + codeNode.getClass().getCanonicalName());
			}
		}
		else {
			return("Failed: Unable to find model '" + modelToBuildUri + "' in code model.");
		}
	}

	private String removeClassWrapper(String pythonCode) {
		StringBuilder methodCode = new StringBuilder();
		boolean saveLine = false;		
        try (BufferedReader reader = new BufferedReader(new StringReader(pythonCode))) {
            String line = reader.readLine();
            while (line != null) {
    			if (saveLine || line.trim().startsWith("def ")) {
    				methodCode.append(line);
    				methodCode.append("\n");
    				saveLine = true;
    			}
                line = reader.readLine();
            }
        } catch (IOException exc) {
            // quit
        }
		return methodCode.toString();
	}

	private String javaMethodToPython(String serviceUri, String javaCode) throws IOException {
		return getExtractionProcessor().translateMethodJavaToPython("DummyClass", javaCode);
	}

	/**
	 * Method to save the OWL model created from code extraction as a SADL file
	 * @param outputOwlFileName -- name of the OWL file created
	 * @return -- the sadl fully qualified file name
	 * @throws IOException
	 */
	public String saveAsSadlFile(String outputOwlFileName, String response) throws IOException {
		if (isYes(response)) {
			OwlToSadl ots = new OwlToSadl(getExtractionProcessor().getCodeModel());
			String sadlFN = outputOwlFileName + ".sadl";
			File sf = new File(sadlFN);
			if (sf.exists()) {
				sf.delete();
			}
			try {
				boolean status = ots.saveSadlModel(sadlFN);
				if (status) {
					return sadlFN;
				}
			} catch (OwlImportException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	public static boolean isYes(Object arg1) {
		return arg1.toString().equalsIgnoreCase("yes");
	}

	public void notifyUser(String modelFolder, String msg) throws ConfigurationException {
		if (getDialogAnswerProvider() != null) {
			// talk to the user via the Dialog editor
			Method acmic = null;
			try {
				acmic = getDialogAnswerProvider().getClass().getMethod("addCurationManagerInitiatedContent", String.class);
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (acmic == null) {
				Method[] dapMethods = getDialogAnswerProvider().getClass().getDeclaredMethods();
				if (dapMethods != null) {
					for (Method m : dapMethods) {
						if (m.getName().equals("addCurationManagerInitiatedContent")) {
							acmic = m;
							break;
						}
					}
				}
			}
			if (acmic != null) {
				acmic.setAccessible(true);
				try {
					acmic.invoke(getDialogAnswerProvider(), msg);
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvocationTargetException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
	}

	/**
	 * Method to read file content into a string
	 * @param file
	 * @return
	 * @throws IOException
	 */
	public String readFileToString(File file) throws IOException {
		StringBuilder fileData = new StringBuilder(1000);
		BufferedReader reader = new BufferedReader(new FileReader(file));
 
		char[] buf = new char[10];
		int numRead = 0;
		while ((numRead = reader.read(buf)) != -1) {
//			System.out.println(numRead);
			String readData = String.valueOf(buf, 0, numRead);
			fileData.append(readData);
			buf = new char[1024];
		}
 
		reader.close();
 
		return  fileData.toString();	
	}

	protected IDialogAnswerProvider getDialogAnswerProvider() {
		if (dialogAnswerProvider == null) {
			setDialogAnswerProvider((IDialogAnswerProvider) getDomainModelConfigurationManager().getPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER));
			if (dialogAnswerProvider == null) {
				dialogAnswerProvider = new DialogAnswerProviderConsoleForTest();
			}
		}
		return dialogAnswerProvider;
	}

	protected void setDialogAnswerProvider(IDialogAnswerProvider dialogAnswerProvider) {
		this.dialogAnswerProvider = dialogAnswerProvider;
	}
	
	public Object[] buildCGModel(String baseServiceUri, String modelUri, String equationModel, String dataLocation, List<String[]> inputs, List<String[]> outputs) throws IOException {
//		String host = "3.39.122.224";
//		String host = "3.1.176.139";
//		int port = 12345;
		String kchainServiceURL = baseServiceUri + "/darpa/aske/kchain/";
		
		JsonObject json = new JsonObject();
		json.addProperty("modelName", modelUri);
		if (equationModel != null) {
			json.addProperty("equationModel", equationModel);
		}
		if (dataLocation != null) {
			json.addProperty("dataLocation", dataLocation);
		}
		JsonArray jarrin = new JsonArray();
		json.add("inputVariables", jarrin);
		for (String[] input : inputs) {
			JsonObject inputj = new JsonObject();
			inputj.addProperty("name", input[0]);
			inputj.addProperty("type", input[1]);
			if (input.length > 2) {
				inputj.addProperty("value", input[2]);
			}
			jarrin.add(inputj);
		}
		JsonArray jarrout = new JsonArray();
		json.add("outputVariables", jarrout);
		for (String[] output : outputs) {
			JsonObject outputj = new JsonObject();
			outputj.addProperty("name", output[0]);
			outputj.addProperty("type", output[1]);
			jarrout.add(outputj);
		}
		
		String buildServiceURL = kchainServiceURL + "build";
		URL serviceUrl = new URL(buildServiceURL);			

		String jsonResponse = makeConnectionAndGetResponse(serviceUrl, json);
		
		System.out.println(jsonResponse);
		
		JsonElement je = new JsonParser().parse(jsonResponse);
		Object[] returnValues = new Object[3];
		if (je.isJsonObject()) {
			JsonObject jobj = je.getAsJsonObject();
			String modelType = jobj.get("modelType").getAsString();
			returnValues[0] = modelType;
			String metagraphLocation = jobj.get("metagraphLocation").getAsString();
			returnValues[1] = metagraphLocation;
			boolean trained = jobj.get("trainedState").getAsBoolean();	
			returnValues[2] = trained;
		}
		else {
			throw new IOException("Unexpected response: " + je.toString());
		}
		return returnValues;
	}

	private String makeConnectionAndGetResponse(URL url, JsonObject jsonObject) {
		String response = "";
		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();                     
			connection.setDoOutput(true);
			connection.setRequestMethod("POST"); 
			connection.setRequestProperty("Content-Type", "application/json");

			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(jsonObject.toString().getBytes());
			outputStream.flush();

			try {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));                                     
				String output = "";
				while((output = br.readLine()) != null) 
					response = response + output;                 
				outputStream.close();
				br.close();
			}
			catch (Exception e) {
				System.out.println("Error reading response: " + e.getMessage());
			}
			connection.disconnect();
		} catch (Exception e) {
			System.out.println(jsonObject.toString());
			e.printStackTrace();
		}
		return response;
	}


}

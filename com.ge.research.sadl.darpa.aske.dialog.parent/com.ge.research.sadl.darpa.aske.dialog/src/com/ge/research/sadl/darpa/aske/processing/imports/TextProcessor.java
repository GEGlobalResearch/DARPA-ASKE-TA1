package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;

import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.jena.UtilsForJena;
import com.ge.research.sadl.jena.inference.SadlJenaModelGetterPutter;
import com.ge.research.sadl.processing.SadlConstants;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.IConfigurationManagerForEditing.Scope;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TextProcessor {
	private static final Logger logger = Logger.getLogger (TextProcessor.class) ;

	private Map<String, String> preferences;
	
	private List<File> textFiles;
	
	private AnswerCurationManager answerCurationManager;

	private IConfigurationManagerForIDE textModelConfigMgr;

	private OntModel textModel;

	private String textmodelName;

	private String textmodelPrefix;

	private Map<String, OntModel> textModels;

	public TextProcessor(AnswerCurationManager answerCurationManager, Map<String, String> preferences) {
		setCurationManager(answerCurationManager);
		this.setPreferences(preferences);
	}

	public String process(String inputIdentifier, String text, String locality) throws ConfigurationException, IOException {
		initializeTextModel();
		try {
			String msg = "Importing text file '" + inputIdentifier + "'.";
			getCurationManager().notifyUser(getTextModelConfigMgr().getModelFolder(), msg);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
		String baseServiceUrl = "http://vesuvius-dev.crd.ge.com:4200/darpa/aske/";		// dev environment for stable development of other components
//		String baseServiceUrl = "http://vesuvius-063.crd.ge.com:4200/darpa/aske/";		// test environment for service development
		
		String servicePreference = getPreference(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getId());
		if (servicePreference != null) {
			baseServiceUrl = servicePreference + "/darpa/aske/";
		}
		String textToTripleServiceURL = baseServiceUrl + "text2triples";
		URL serviceUrl = new URL(textToTripleServiceURL);			
		if (text == null) {
			text = "Hello world";
		}
		if (locality == null) {
			locality = "NYI";
		}
		JsonObject json = new JsonObject();
		json.addProperty("locality", locality);
		json.addProperty("text", text);
//		System.out.println(text);
		String response = makeConnectionAndGetResponse(serviceUrl, json);
//		System.out.println(response);
		if (response != null && response.length() > 0) {
			OntModel theModel = getCurationManager().getExtractionProcessor().getTextModel();
			JsonArray sentences = new JsonParser().parse(response).getAsJsonArray();
			if (sentences != null) {
				for (JsonElement element : sentences) {
					if (element != null) {
						JsonObject sentence = element.getAsJsonObject();
						String originalText = sentence.get("text").getAsString();
//						System.out.println("Extracted from text:" + originalText);
						JsonArray concepts = sentence.get("concepts").getAsJsonArray();
						for (JsonElement concept : concepts) {
							String matchingText = concept.getAsJsonObject().get("string").getAsString();
							int startInOrigText = concept.getAsJsonObject().get("start").getAsInt();
							int endInOrigText = concept.getAsJsonObject().get("end").getAsInt();
							double extractionConfidence = concept.getAsJsonObject().get("extractionConfScore").getAsDouble();
//							System.out.println("  Match in substring '" + matchingText + "(" + startInOrigText + "," + endInOrigText + "):");
							JsonArray triples = concept.getAsJsonObject().get("triples").getAsJsonArray();
							String eqName = null;
							String eqExpr1 = null;
							String eqExpr2 = null;
							String eqScript1 = null;
							String eqScript2 = null;
							String lang1 = null;
							String lang2 = null;
							for (JsonElement triple : triples) {
								String subject = triple.getAsJsonObject().get("subject").getAsString();
								String predicate = triple.getAsJsonObject().get("predicate").getAsString();
								String object = triple.getAsJsonObject().get("object").getAsString();
								double tripleConfidenceScore = triple.getAsJsonObject().get("tripleConfScore").getAsDouble();
//								System.out.println("     <" + subject + ", " + predicate + ", " + object + "> (" + tripleConfidenceScore + ")");
								if (object.equals("<http://sadl.org/sadlimplicitmodel#ExternalEquation>")) {
									// equation found
									eqName = subject;
									// predicate assumed to be rdf:type
								}
								if (eqName != null && subject.equals(eqName)) {
									if (predicate.equals("<http://sadl.org/sadlimplicitmodel#expression>")) {
										if (eqExpr1 == null) {
											eqExpr1 = object;
										}
										else {
											eqExpr2 = object;
										}
									}
								}
								if (eqExpr1 != null && subject.equals(eqExpr1)) {
									if (predicate.equals("<" + SadlConstants.SADL_IMPLICIT_MODEL_SCRIPT_PROPERTY_URI + ">")) {
										eqScript1 = UtilsForJena.stripQuotes(object);
									}
									else if (predicate.equals("<" + SadlConstants.SADL_IMPLICIT_MODEL_LANGUAGE_PROPERTY_URI + ">")) {
										lang1 = object;
									}
								}
								if (eqExpr2 != null && subject.equals(eqExpr2)) {
									if (predicate.equals("<" + SadlConstants.SADL_IMPLICIT_MODEL_SCRIPT_PROPERTY_URI + ">")) {
										eqScript2 = UtilsForJena.stripQuotes(object);
									}
									else if (predicate.equals("<" + SadlConstants.SADL_IMPLICIT_MODEL_LANGUAGE_PROPERTY_URI + ">")) {
										lang2 = object;
									}
								}
							}
							if (eqName != null) {
								// add to model
								Individual eqInst = theModel.createIndividual(getCurationManager().getExtractionProcessor().getTextModelName() + "#" + eqName,
										theModel.getOntClass(SadlConstants.SADL_IMPLICIT_MODEL_EXTERNAL_EQUATION_CLASS_URI));
								if (eqName.startsWith("_:")) {
									eqName = eqName.substring(2);
								}
								if (eqScript1 != null) {
									Individual script1 = theModel.createIndividual(theModel.getOntClass(SadlConstants.SADL_IMPLICIT_MODEL_SCRIPT_CLASS_URI));
									script1.addProperty(theModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_SCRIPT_PROPERTY_URI), theModel.createTypedLiteral(eqScript1));
									script1.addProperty(theModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_LANGUAGE_PROPERTY_URI), theModel.getIndividual(lang1.substring(1, lang1.length() - 1)));
									eqInst.addProperty(theModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_EXPRESSTION_PROPERTY_URI), script1);
								}
								if (eqScript2 != null) {
									Individual script2 = theModel.createIndividual(theModel.getOntClass(SadlConstants.SADL_IMPLICIT_MODEL_SCRIPT_CLASS_URI));
									script2.addProperty(theModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_SCRIPT_PROPERTY_URI), theModel.createTypedLiteral(eqScript2));
									script2.addProperty(theModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_LANGUAGE_PROPERTY_URI), theModel.getIndividual(lang2.substring(1, lang2.length() - 1)));
									eqInst.addProperty(theModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_EXPRESSTION_PROPERTY_URI), script2);									
								}
							}
						}
					}
				}
			}
//			theModel.write(System.out, "N3");
		}
		else {
			System.err.println("No response received from service " + textToTripleServiceURL);
		}
		return sb.toString();
	}
	
	public void addTextModel(String key, OntModel textModel) {
		if (textModels == null) {
			textModels = new HashMap<String, OntModel>();
		}
		textModels.put(key, textModel);
	}
	
	public OntModel getTextModel(String key) {
		if (textModels != null) {
			return textModels.get(key);
		}
		return null;
	}

	private void initializeTextModel() throws ConfigurationException, IOException {
		if (getCurationManager().getExtractionProcessor().getTextModel() == null) {
			// create new text model	
			setTextModelConfigMgr(getCurationManager().getDomainModelConfigurationManager());
			OntDocumentManager owlDocMgr = getTextModelConfigMgr().getJenaDocumentMgr();
			OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
			if (owlDocMgr != null) {
				spec.setDocumentManager(owlDocMgr);
				owlDocMgr.setProcessImports(true);
			}
			getCurationManager().getExtractionProcessor().setTextModel(ModelFactory.createOntologyModel(spec));	
			setTextModel(getCurationManager().getExtractionProcessor().getTextModel());
			getTextModel().setNsPrefix(getTextModelPrefix(), getTextModelNamespace());
			Ontology modelOntology = getTextModel().createOntology(getTextModelName());
			logger.debug("Ontology '" + getTextModelName() + "' created");
			modelOntology.addComment("This ontology was created by extraction from text by the ANSWER TextProcessor.", "en");
			OntModel importedOntModel = getTextModelConfigMgr().getOntModel(SadlConstants.SADL_IMPLICIT_MODEL_URI, Scope.INCLUDEIMPORTS);
			addImportToJenaModel(getTextModelName(), SadlConstants.SADL_IMPLICIT_MODEL_URI, SadlConstants.SADL_IMPLICIT_MODEL_PREFIX, importedOntModel);
		}
		else {
			setTextModel(getCurationManager().getExtractionProcessor().getTextModel());
		}
	}
	
	private void addImportToJenaModel(String modelName, String importUri, String importPrefix, Model importedOntModel) {
		getTextModel().getDocumentManager().addModel(importUri, importedOntModel, true);
		Ontology modelOntology = getTextModel().createOntology(modelName);
		if (importPrefix != null) {
			getTextModel().setNsPrefix(importPrefix, importUri);
		}
		com.hp.hpl.jena.rdf.model.Resource importedOntology = getTextModel().createResource(importUri);
		modelOntology.addImport(importedOntology);
		getTextModel().addSubModel(importedOntModel);
		getTextModel().addLoadedImport(importUri);
	}

	private String getTextModelName() {
		return this.getTextmodelName();
	}

	private String getTextModelNamespace() {
		return getTextModelName() + "#";
	}

	private String getTextModelPrefix() {
		return this.getTextmodelPrefix();
	}

	private void setTextModel(OntModel textModel) {
		this.textModel = textModel;	
	}
	
	private OntModel getTextModel() {
		return textModel;
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

			BufferedReader br = new BufferedReader(
					new InputStreamReader(connection.getInputStream()));                                     
			String output = "";
			while((output = br.readLine()) != null) 
				response = response + output;                 
			outputStream.close();
			br.close();
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
	}

	public Map<String, String> getPreferences() {
		return preferences;
	}

	public String getPreference(String key) {
		if (preferences != null) {
			return preferences.get(key);
		}
		return null;
	}

	private void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}

	public void addFiles(List<File> txtFiles) {
		if (textFiles != null) {
			for (File f : txtFiles) {
				if (!textFiles.contains(f)) {
					textFiles.add(f);
				}
			}
		}
		else {
			setTextFiles(txtFiles);
		}
	}

	public void addFile(File txtFile) {
		if (textFiles == null) {
			textFiles = new ArrayList<File>();
		}
		if (!textFiles.contains(txtFile)) {
			textFiles.add(txtFile);
		}
	}

	public List<File> getTextFiles() {
		return textFiles;
	}

	public void setTextFiles(List<File> textFiles) {
		this.textFiles = textFiles;
	}

	public AnswerCurationManager getCurationManager() {
		return answerCurationManager;
	}

	public void setCurationManager(AnswerCurationManager answerCurationManager) {
		this.answerCurationManager = answerCurationManager;
	}

	public IConfigurationManagerForIDE getTextModelConfigMgr() {
		return textModelConfigMgr;
	}

	private void setTextModelConfigMgr(IConfigurationManagerForIDE textMetaModelConfigMgr) {
		this.textModelConfigMgr = textMetaModelConfigMgr;
	}

	public String getTextmodelName() {
		return textmodelName;
	}

	public void setTextmodelName(String textmodelName) {
		this.textmodelName = textmodelName;
	}

	public String getTextmodelPrefix() {
		return textmodelPrefix;
	}

	public void setTextmodelPrefix(String textmodelPrefix) {
		this.textmodelPrefix = textmodelPrefix;
	}

}

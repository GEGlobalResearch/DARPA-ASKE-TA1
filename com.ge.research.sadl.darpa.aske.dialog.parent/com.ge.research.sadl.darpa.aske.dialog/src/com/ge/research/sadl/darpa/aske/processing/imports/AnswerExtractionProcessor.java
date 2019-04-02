package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import org.eclipse.emf.ecore.resource.Resource;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.jena.JenaProcessorException;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.processing.SadlConstants;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.hp.hpl.jena.ontology.OntModel;

public class AnswerExtractionProcessor {
	private String documentContent;
	private CodeLanguage language;
	private String serializedCode;
	private OntModel contextModel;	// the knowledge graph to use during extraction
	private OntModel textModel;		// the knowledge graph extension to the contextModel extracted from text
	private String textModelName;
	private String textModelPrefix;
	private OntModel codeModel;		// the knowledge graph extension to the context model extracted from code
	private String codeModelName;
	private String codeModelPrefix;
	private IModelFromCodeExtractor codeExtractor;
	private TextProcessor textProcessor;
	private Map<String, String> preferences = null;
	private AnswerCurationManager curationManager = null;
	private String sadlContent;
	
	public enum CodeLanguage {
		JAVA
		}


	public AnswerExtractionProcessor(AnswerCurationManager curationMgr, String documentContent, String serializedCode, CodeLanguage language, OntModel context, Map<String, String> preferences) {
		setCurationManager(curationMgr);
		setContextModel(context);
		setDocumentContent(documentContent);
		setLanguage(language);
		setSerializedCode(serializedCode);
		setPreferences(preferences);
	}
	
	public AnswerExtractionProcessor(AnswerCurationManager curationMgr, Map<String,String> preferences) {
		setCurationManager(curationMgr);
		setPreferences(preferences);
	}
	
	public OntModel extractModelFromText() {
		// call REST service to do extraction from text
		String serviceURI = "";
		int servicePort = 1234;
		
		return null;
	}

	public OntModel extractModelFromCode() {
		OntModel extractedModel = null;
		if (getTextModel() == null && getDocumentContent() != null) {
			setTextModel(extractModelFromText());
		}
		
		return extractedModel;
	}

	public CodeLanguage getLanguage() {
		return language;
	}

	private void setLanguage(CodeLanguage language) {
		this.language = language;
	}

	public String getSerializedCode() {
		return serializedCode;
	}

	private void setSerializedCode(String serializedCode) {
		this.serializedCode = serializedCode;
	}

	public OntModel getContextModel() {
		return contextModel;
	}

	private void setContextModel(OntModel contextModel) {
		this.contextModel = contextModel;
	}

	public OntModel getTextModel() {
		return textModel;
	}

	public void setTextModel(OntModel textModel) {
		this.textModel = textModel;
	}

	private String getDocumentContent() {
		return documentContent;
	}

	private void setDocumentContent(String documentContent) {
		this.documentContent = documentContent;
	}

	public OntModel getCodeModel() {
		return codeModel;
	}

	public void setCodeModel(OntModel codeModel) {
		this.codeModel = codeModel;
	}

	public IModelFromCodeExtractor getCodeExtractor(CodeLanguage language) {
		if (codeExtractor == null) {
			if (language.equals(CodeLanguage.JAVA)) {
				codeExtractor = new JavaModelExtractorJP(getCurationManager(), getPreferences());
			}
		}
		return codeExtractor;
	}

	public IModelFromCodeExtractor getCodeExtractor() {
		if (codeExtractor == null) {
			codeExtractor = new JavaModelExtractorJP(getCurationManager(), getPreferences());
		}
		return codeExtractor;
	}

	public void setCodeExtractor(IModelFromCodeExtractor codeExtractor) {
		this.codeExtractor = codeExtractor;
	}

	public TextProcessor getTextProcessor() {
		if (textProcessor == null) {
			textProcessor = new TextProcessor(getCurationManager(), getPreferences());
		}
		return textProcessor;
	}

	public void setTextProcessor(TextProcessor textExtractor) {
		this.textProcessor = textExtractor;
	}

	public Map<String, String> getPreferences() {
		return preferences;
	}

	public void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}

	private AnswerCurationManager getCurationManager() {
		return this.curationManager ;
	}

	public void setCurationManager(AnswerCurationManager curationManager) {
		this.curationManager = curationManager;
	}

	public void addNewSadlContent(String newContent) {
		if (getGeneratedSadlContent() != null) {
			setSadlContent(getGeneratedSadlContent() + newContent);
		}
		else {
			setSadlContent(newContent);
		}
	}

	public String getGeneratedSadlContent() {
		return sadlContent;
	}

	public void setSadlContent(String sadlContent) {
		this.sadlContent = sadlContent;
	}

	public String getTextModelName() {
		return textModelName;
	}

	public void setTextModelName(String textModelName) {
		getTextProcessor().setTextmodelName(textModelName);
		this.textModelName = textModelName;
	}

	public String getCodeModelName() {
		return codeModelName;
	}

	public void setCodeModelName(String codeModelName) {
		this.codeModelName = codeModelName;
	}

	public String getCodeModelPrefix() {
		return codeModelPrefix;
	}

	public void setCodeModelPrefix(String codeModelPrefix) {
		this.codeModelPrefix = codeModelPrefix;
	}

	public String translateMethodJavaToPython(String className, String methodCode) throws IOException {
		StringBuilder sb = new StringBuilder();
//		String baseServiceUrl = "http://vesuvius-dev.crd.ge.com:19092/darpa/aske/";
		String baseServiceUrl = "http://vesuvius063.crd.ge.com:19092/darpa/aske/";
		
		String translateMethodServiceURL = baseServiceUrl + "translate/method/";
		URL serviceUrl = new URL(translateMethodServiceURL);			

		JsonObject json = new JsonObject();
		json.addProperty("className", className);
		json.addProperty("methodCode", methodCode);
		
//		System.out.println(json.toString());
	
		String response = getCurationManager().makeConnectionAndGetResponse(serviceUrl, json);
//		System.out.println(response);
		if (response != null && response.length() > 0) {
			JsonElement je = new JsonParser().parse(response);
			if (je.isJsonObject()) {
				JsonObject jobj = je.getAsJsonObject();
				JsonElement status = jobj.get("status");
				System.out.println("Status: " + status.getAsString());
				if (!status.getAsString().equalsIgnoreCase("SUCCESS")) {
					throw new IOException("Method translation failed: " + status.getAsString());
				}
				String pythonCode = jobj.get("code").getAsString();
//				System.out.println(pythonCode);
				sb.append(pythonCode);

			}
			else if (je instanceof JsonPrimitive) {
				String status = ((JsonPrimitive)je).getAsString();
				System.err.println(status);
			}
		}
		else {
			throw new IOException("No response received from service " + translateMethodServiceURL);
		}
		return sb.toString();
	}

	public void reset() {
		setCodeModel(null);
		getCodeExtractor().setCodeModelName(null);
		getCodeExtractor().setDefaultCodeModelName(null);
		setCodeModelPrefix(null);
		getCodeExtractor().setDefaultCodeModelPrefix(null);
		setTextModel(null);
		setTextModelName(null);
		setTextModelPrefix(null);
	}

	public String getTextModelPrefix() {
		return textModelPrefix;
	}

	public void setTextModelPrefix(String textModelPrefix) {
		getTextProcessor().setTextmodelPrefix(textModelPrefix);
		this.textModelPrefix = textModelPrefix;
	}

}

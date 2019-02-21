package com.ge.research.sadl.darpa.aske.processing.imports;

import java.util.Map;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.hp.hpl.jena.ontology.OntModel;

public class AnswerExtractionProcessor {
	private String documentContent;
	private CodeLanguage language;
	private String serializedCode;
	private OntModel contextModel;	// the knowledge graph to use during extraction
	private OntModel textModel;		// the knowledge graph extension to the contextModel extracted from text
	private String textModelName;
	private OntModel codeModel;		// the knowledge graph extension to the context model extracted from code
	private String codeModelName;
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
				codeExtractor = new JavaModelExtractorJP(getCurationManager(), new SadlModelGenerator(), getPreferences());
			}
		}
		return codeExtractor;
	}

	public IModelFromCodeExtractor getCodeExtractor() {
		return codeExtractor;
	}

	public void setCodeExtractor(IModelFromCodeExtractor codeExtractor) {
		this.codeExtractor = codeExtractor;
	}

	public TextProcessor getTextProcessor() {
		if (textProcessor == null) {
			textProcessor = new TextProcessor(getPreferences());
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
		this.textModelName = textModelName;
	}

	public String getCodeModelName() {
		return codeModelName;
	}

	public void setCodeModelName(String codeModelName) {
		this.codeModelName = codeModelName;
	}

}

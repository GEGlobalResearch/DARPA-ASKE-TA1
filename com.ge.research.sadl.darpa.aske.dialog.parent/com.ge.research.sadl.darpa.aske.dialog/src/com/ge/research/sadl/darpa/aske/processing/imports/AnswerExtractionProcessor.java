package com.ge.research.sadl.darpa.aske.processing.imports;

import com.hp.hpl.jena.ontology.OntModel;

enum CodeLanguage {
	JAVA
	}

public class AnswerExtractionProcessor {
	private String documentContent;
	private CodeLanguage language;
	private String serializedCode;
	private OntModel contextModel;
	private OntModel textModel;
	
	public AnswerExtractionProcessor(String documentContent, String serializedCode, CodeLanguage language, OntModel context) {
		setContextModel(context);
		setDocumentContent(documentContent);
		setLanguage(language);
		setSerializedCode(serializedCode);
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
}

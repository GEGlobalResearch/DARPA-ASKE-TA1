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
package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.IOException;
import java.net.URL;
import java.util.Map;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
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

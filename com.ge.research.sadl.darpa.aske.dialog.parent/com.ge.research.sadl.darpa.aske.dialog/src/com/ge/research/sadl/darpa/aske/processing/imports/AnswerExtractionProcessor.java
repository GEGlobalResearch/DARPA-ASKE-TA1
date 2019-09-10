/********************************************************************** 
 * Note: This license has also been called the "New BSD License" or 
 * "Modified BSD License". See also the 2-clause BSD License.
 *
 * Copyright © 2018-2019 - General Electric Company, All Rights Reserved
 * 
 * Projects: ANSWER and KApEESH, developed with the support of the Defense 
 * Advanced Research Projects Agency (DARPA) under Agreement  No.  
 * HR00111990006 and Agreement No. HR00111990007, respectively. 
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
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.reasoner.ResultSet;
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
		String baseServiceUrl = "http://vesuvius-dev.crd.ge.com:19092/darpa/aske/";
//		String baseServiceUrl = "http://vesuvius063.crd.ge.com:19092/darpa/aske/";
		
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

	/**
	 * Method to save a Python script as an equation in the target computational graph
	 * @param modelUri--the URI of the equation to be saved
	 * @param outputName--the name of the output of the calculation
	 * @param rs--a ResultSet containing input names and types
	 * @param rs2--a ResultSet containing output names (may be null) and types
	 * @param modifiedPythonScript--the Python script of the equation for physics-based models
	 * @param dataLocation--the location of the data for data-driven models
	 * @return--the URI of the successfully created model in the computational graph or null if not successful
	 * @throws IOException
	 */
	public String saveToComputationalGraph(String modelUri, String outputName, ResultSet rsInputs, ResultSet rsOutputs, String modifiedPythonScript, String dataLocation) throws IOException {
		// construct String inputs to the service
		/*
		 * Construct after this manner:
		 * 	String[] input1 = new String[3];
			input1[0] = "T";
			input1[1] = "double";
			input1[2] = "508.788";
			inputs.add(input1);
			String[] input2 = new String[3];
			input2[0] = "G";
			input2[1] = "double";
			input2[2] = "1.4";
			inputs.add(input2);
			String[] input3 = new String[3];
			input3[0] = "R";
			input3[1] = "double";
			input3[2] = "53.3";
			inputs.add(input3);
			String[] input4 = new String[3];
			input4[0] = "Q";
			input4[1] = "double";
			input4[2] = "5500";
			inputs.add(input4);

			String[] output1 = new String[2];
			output1[0] = "CAL_SOS";
			output1[1] = "double";
			outputs.add(output1);

		 */
		rsInputs.setShowNamespaces(false);
		rsOutputs.setShowNamespaces(false);
		
		List<String[]> inputs = new ArrayList<String[]>();
		List<String[]> outputs = new ArrayList<String[]>();
		String[] colNames = rsInputs.getColumnNames();    		// check to make sure columns are as expected for robustness
		if (!colNames[0].equals("argName")) {
			throw new IOException("The ResultSet for inputs to the equation does not have 'argName' in the first column");
		}
		if (!colNames[1].equals("argType")) {
			throw new IOException("The ResultSet for inputs to the equation does not have 'argType' in the second column");
		}
		String[] colNamesRet = rsOutputs.getColumnNames();
		if (!colNamesRet[0].equals("retName")) {
			throw new IOException("The ResultSet for returns from the equation does not have 'retName' in the first column");
		}
		if (!colNamesRet[1].equals("retType")) {
			throw new IOException("The ResultSet for returns from the equation does not have 'retType' in the second column");
		}
		int rsColCount = rsInputs.getColumnCount();
		int rsRowCount = rsInputs.getRowCount();
		for (int r = 0; r < rsRowCount; r++) {
			String[] input = new String[rsColCount];
			for (int c = 0; c < rsColCount; c++) {
				input[c] = rsInputs.getResultAt(r, c).toString();
			}
			inputs.add(input);
		}
		
		int rs2ColCount = rsOutputs.getColumnCount();
		int rs2RowCount = rsOutputs.getRowCount();
		if (rs2RowCount != 1) {
			throw new IOException("Only one output is currently handled by  saveToComputationalGraph");
		}
		for (int r = 0; r < rs2RowCount; r++) {
			String[] output = new String[rs2ColCount];
			for (int c = 0; c < rs2ColCount; c++) {
				output[0] = outputName;
				output[1] = rsOutputs.getResultAt(r, 1).toString();
			}
			outputs.add(output);
		}
		
		return saveToComputationalGraph(modelUri, modifiedPythonScript, dataLocation, inputs, outputs);
	}

	/**
	 * Method to save a Python script as an equation in the target computational graph
	 * @param modelUri--the URI of the equation to be saved
	 * @param modifiedPythonScript
	 * @param dataLocation
	 * @param inputs
	 * @param outputs
	 * @return
	 * @throws IOException
	 */
	public String saveToComputationalGraph(String modelUri, String modifiedPythonScript, String dataLocation,
			List<String[]> inputs, List<String[]> outputs) throws IOException {
		KChainServiceInterface kcService = new KChainServiceInterface();
		if (kcService.buildCGModel(modelUri, modifiedPythonScript, dataLocation, inputs, outputs)) {
			
			return modelUri;
		}
		return null;
	}
}

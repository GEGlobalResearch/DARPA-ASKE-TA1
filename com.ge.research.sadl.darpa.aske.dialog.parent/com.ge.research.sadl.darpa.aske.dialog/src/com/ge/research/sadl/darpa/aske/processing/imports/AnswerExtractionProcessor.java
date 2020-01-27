/********************************************************************** 
 * Note: This license has also been called the "New BSD License" or 
 * "Modified BSD License". See also the 2-clause BSD License.
 *
 * Copyright � 2018-2019 - General Electric Company, All Rights Reserved
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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.reasoner.ResultSet;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntModel;

public class AnswerExtractionProcessor {
	protected static final Logger logger = LoggerFactory.getLogger(AnswerExtractionProcessor.class);
	private String documentContent;
	private CodeLanguage language;
	private String serializedCode;
	private OntModel contextModel;	// the knowledge graph to use during extraction
	private IModelFromCodeExtractor codeExtractor;
	private TextProcessor textProcessor;
	private Map<String, String> preferences = null;
	private AnswerCurationManager curationManager = null;
	private String sadlContent;
	
	public enum CodeLanguage {
		JAVA, PYTHON, PYTHON_TF, TEXT, OTHERLANGUAGE
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
		return getTextProcessor().getCurrentTextModel();
	}

	public void setTextModel(OntModel textModel) {
		getTextProcessor().setTextModel(textModel);
	}

	private String getDocumentContent() {
		return documentContent;
	}

	private void setDocumentContent(String documentContent) {
		this.documentContent = documentContent;
	}

	public OntModel getCodeModel() {
		return getCodeExtractor().getCurrentCodeModel();
	}

//	public void setCodeModel(OntModel codeModel) {
//		this.codeModel = codeModel;
//	}

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
			String gc = getGeneratedSadlContent();
			if (!gc.endsWith("\n")) {
				gc = gc + "\n";
			}
			setSadlContent(gc + newContent);
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
		return getTextProcessor().getTextModelName();
	}

	public String getCodeModelName() {
		return getCodeExtractor().getCodeModelName();
	}

	public String getCodeModelPrefix() {
		return getCodeExtractor().getCodeModelPrefix();
	}

	/**
	 * method to get the namespace from the model name by adding a "#" to the end
	 * @param modelName
	 * @return
	 */
	public String getNamespaceFromModelName(String modelName) {
		return modelName + "#";
	}

	/**
	 * Method to translate a Java method into Python. The Python code will be wrapped in a class.
	 * 
	 * @param className -- the name of the class to use to wrap the code
	 * @param methodCode -- the Java method method code 
	 * @return -- the Python code
	 * @throws IOException
	 */
	public String translateMethodJavaToPython(String className, String methodCode) throws IOException {
		String serviceBaseUrl = getCurationManager().getPreference(DialogPreferences.ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI.getId());
		JavaToPythonServiceInterface jtpsi = new JavaToPythonServiceInterface(serviceBaseUrl);
		String pyCodeWrapped = jtpsi.translateMethodJavaToPython(className, methodCode);
		String pyCodeUnwrapped = unwrapPythonMethodInClass(pyCodeWrapped);
		return pyCodeUnwrapped;
	}

	private String unwrapPythonMethodInClass(String pyCodeWrapped) {
        StringBuilder sb = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new StringReader(pyCodeWrapped))) {
        	boolean foundClass = false;
        	boolean foundMethod = false;
        	int lineCntr = 0;
            String line = reader.readLine();
            while (line != null) {
                if (line.startsWith("class ")) {
                	foundClass = true;
                }
                if (foundClass && line.startsWith("    def ")) {
                	foundMethod = true;
                }
            	if (foundMethod) {
            		if (lineCntr++ > 0) {
            			sb.append("\n");
            		}
            		if (line.length() >= 4) {
            			sb.append(line.substring(4));
            		}
            	}
                line = reader.readLine();
            }
        } catch (IOException exc) {
            // quit
        }
        return sb.toString();
	}

	/**
	 * Method to translate a Java expression into Python.
	 * 
	 * @param className -- the name of the class to use to wrap the code
	 * @param exprCode -- the Java method method code 
	 * @return -- the Python code
	 * @throws IOException
	 */
	public String translateExpressionJavaToPython(String className, String methodName, String exprCode) throws IOException {
		String serviceBaseUrl = getCurationManager().getPreference(DialogPreferences.ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI.getId());
		JavaToPythonServiceInterface jtpsi = new JavaToPythonServiceInterface(serviceBaseUrl);
		return jtpsi.translateExpressionJavaToPython(className, methodName, exprCode);
	}

	public void reset() {
		getCodeExtractor().setCurrentCodeModel(null);
		getCodeExtractor().setCodeModelName(null);
		getCodeExtractor().setCodeModelPrefix(null);
		getTextProcessor().setTextModel(null);
		getTextProcessor().setTextModelName(null);
		getTextProcessor().setTextModelPrefix(null);
	}

	public String getTextModelPrefix() {
		return getTextProcessor().getTextModelPrefix();
	}

	/**
	 * Method to save a Python script as an equation in the target computational graph
	 * @param modelToEvaluate--the equation to be saved
	 * @param outputName--the name of the output of the calculation
	 * @param rs--a ResultSet containing input names and types
	 * @param rs2--a ResultSet containing output names (may be null) and types
	 * @param modifiedPythonScript--the Python script of the equation for physics-based models
	 * @param dataLocation--the location of the data for data-driven models
	 * @return--the URI of the successfully created model in the computational graph or null if not successful
	 * @throws IOException
	 */
	public String saveToComputationalGraph(Individual modelToEvaluate, String outputName, ResultSet rsInputs, ResultSet rsOutputs, String modifiedPythonScript, String dataLocation) throws IOException {
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
				String alias = modelToEvaluate.getLabel(null);
				output[0] = alias != null ? alias : (outputName != null) ? outputName : modelToEvaluate.getLocalName();
				output[1] = rsOutputs.getResultAt(r, 1).toString();
			}
			outputs.add(output);
		}
		
		return saveToComputationalGraph(getCurationManager().pythonify(modelToEvaluate.getLocalName()), modifiedPythonScript, dataLocation, inputs, outputs);
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
		String serviceBaseUri = getCurationManager().getPreference(DialogPreferences.ANSWER_KCHAIN_CG_SERVICE_BASE_URI.getId());
		KChainServiceInterface kcService = new KChainServiceInterface(serviceBaseUri);
		try {
			Object[] results = kcService.buildCGModel(modelUri, modifiedPythonScript, dataLocation, inputs, outputs);
			if (results != null && results.length == 3) {
				return "Successfully built model '" + modelUri + "', type=" + results[0] + ", location=" + results[2];
			}
		}
		catch (Exception e) {
			return JsonServiceInterface.aggregateExceptionMessage(e);
		}
		return null;
	}
}

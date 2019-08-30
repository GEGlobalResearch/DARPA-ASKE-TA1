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
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.darpa.aske.processing.ConversationElement;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.DialogContent;
import com.ge.research.sadl.darpa.aske.processing.IDialogAnswerProvider;
import com.ge.research.sadl.darpa.aske.processing.imports.AnswerExtractionProcessor;
import com.ge.research.sadl.darpa.aske.processing.imports.CodeExtractionException;
import com.ge.research.sadl.darpa.aske.processing.imports.IModelFromCodeExtractor;
import com.ge.research.sadl.darpa.aske.processing.imports.TextProcessor;
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

	private IReasoner codeModelReasoner;
	public enum SaveAsSadl{SaveAsSadl, DoNotSaveAsSadl, AskUserSaveAsSadl}

	public enum Agent {USER, CM}
	private DialogContent conversation = null; 
	private DialogContent lastConversation = null;

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

	public TextProcessor getTextProcessor() {
		return getExtractionProcessor().getTextProcessor();
	}
	
	public IModelFromCodeExtractor getCodeExtractor() {
		return getExtractionProcessor().getCodeExtractor();
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
	public void processImports(SaveAsSadl saveAsSadl) throws IOException, ConfigurationException {
		Map<File, Boolean> outputOwlFiles = new HashMap<File, Boolean>();
//		List<File> outputOwlFiles = new ArrayList<File>();
		List<File> textFiles = getExtractionProcessor().getTextProcessor().getTextFiles();
		if (textFiles != null) {
			for (File f : textFiles) {
				getExtractionProcessor().reset();
				String outputFilename = f.getName();
				String outputOwlFileName = outputFilename + ".owl";
				if (outputFilename.endsWith(".sadl")) {
					outputFilename = outputFilename.substring(0, outputFilename.length() - 5) + ".owl";
				}
				if (getExtractionProcessor().getTextProcessor().getTextmodelPrefix() == null) {
					String defPrefix;
					if (outputFilename.endsWith(".owl")) {
						defPrefix = outputFilename.substring(0, outputFilename.length() - 4);
					}
					else {
						defPrefix = outputFilename.replaceAll("\\.", "_");
					}
					getExtractionProcessor().getTextProcessor().setTextmodelPrefix(defPrefix);
					getExtractionProcessor().setTextModelPrefix(defPrefix);
				}
				if (getExtractionProcessor().getTextProcessor().getTextmodelName() == null) {
					String defName = "http://com.ge.research.sadl.darpa.aske.answer/" + getExtractionProcessor().getTextProcessor().getDefaultTextModelPrefix();
					getExtractionProcessor().getTextProcessor().setTextmodelName(defName);
					getExtractionProcessor().setTextModelName(defName);
				}
				String content = readFileToString(f);
				String fileIdentifier = ConfigurationManagerForIdeFactory.formatPathRemoveBackslashes(f.getCanonicalPath());
				getTextProcessor().process(fileIdentifier, content, null);
				File of = saveTextOwlFile(outputOwlFileName);
				outputOwlFiles.put(of, false);
				outputOwlFileName = of.getCanonicalPath();
				// run inference on the model, interact with user to refine results
				runInferenceDisplayInterestingTextModelResults(outputOwlFileName, saveAsSadl);
			}
		}
		
		List<File> codeFiles = getExtractionProcessor().getCodeExtractor().getCodeFiles();
		if (codeFiles != null) {
			for (File f : codeFiles) {
				// reset code extractor and text processor from any previous file
				getExtractionProcessor().reset();
				String outputFilename = f.getName();
				String outputOwlFileName = outputFilename + ".owl";
				if (outputFilename.endsWith(".sadl")) {
					outputFilename = outputFilename.substring(0, outputFilename.length() - 5) + ".owl";
				}
				String defPrefix;
				if (outputFilename.endsWith(".owl")) {
					defPrefix = outputFilename.substring(0, outputFilename.length() - 4);
				}
				else {
					defPrefix = outputFilename.replaceAll("\\.", "_");
				}
				String defName = "http://com.ge.research.sadl.darpa.aske.answer/" + defPrefix;
				if (getExtractionProcessor().getCodeExtractor().getDefaultCodeModelName() == null) {
					getExtractionProcessor().getCodeExtractor().setDefaultCodeModelName(defName);
				}
				if (getExtractionProcessor().getCodeExtractor().getDefaultCodeModelPrefix() == null) {
					getExtractionProcessor().getCodeExtractor().setDefaultCodeModelPrefix(defPrefix);
				}

				String content = readFileToString(f);
				String fileIdentifier = ConfigurationManagerForIdeFactory.formatPathRemoveBackslashes(f.getCanonicalPath());
				getCodeExtractor().process(fileIdentifier, content);				
				File of = saveCodeOwlFile(outputOwlFileName);
				outputOwlFiles.put(of, true);
				outputOwlFileName = of.getCanonicalPath();
				// run inference on the model, interact with user to refine results
				runInferenceDisplayInterestingCodeModelResults(outputOwlFileName, saveAsSadl, content);
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
				args.add(outputOwlFiles);
				// the strings must be unique to the question or they will get used as answers to subsequent questions with the same string
				if (outputOwlFiles.size() > 0) {
					StringBuilder sb = new StringBuilder();
					Iterator<File> ofitr = outputOwlFiles.keySet().iterator();
					int cntr = 0;
					while (ofitr.hasNext()) {
						File of = ofitr.next();
						if (cntr++ > 0) {
							sb.append(", ");
						}
						sb.append(of.getName());
					}
					if (cntr == 1) {
						dap.addCurationManagerInitiatedContent(this, "saveAsSadlFile", args, "Would you like to save the extracted model (" + sb.toString() + ") in SADL format?");						
					}
					else {
						dap.addCurationManagerInitiatedContent(this, "saveAsSadlFile", args, "Would you like to save the extracted models (" + sb.toString() + ") in SADL format?");
					}
				}
			}
			if (saveAsSadl != null && saveAsSadl.equals(SaveAsSadl.SaveAsSadl)) {
				saveAsSadlFile(outputOwlFiles, "yes");
			}
		}
	}
	
	private File saveTextOwlFile(String outputFilename) throws ConfigurationException, IOException {
		File of = new File(new File(getExtractionProcessor().getTextProcessor().getTextModelFolder()).getParent() + 
				"/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT + "/" + outputFilename);
		of.getParentFile().mkdirs();
		getExtractionProcessor().getTextProcessor().getTextModelConfigMgr().saveOwlFile(getExtractionProcessor().getTextModel(), getExtractionProcessor().getTextModelName(), of.getCanonicalPath());
		String outputOwlFileName = of.getCanonicalPath();			
		getExtractionProcessor().getTextProcessor().addTextModel(outputOwlFileName, getExtractionProcessor().getTextModel());

		String altUrl;
		try {
			altUrl = (new SadlUtils()).fileNameToFileUrl(outputOwlFileName);
			getExtractionProcessor().getTextProcessor().getTextModelConfigMgr().addMapping(altUrl, getExtractionProcessor().getTextModelName(), getExtractionProcessor().getTextModelPrefix(), false, "AnswerCurationManager");
			getExtractionProcessor().getTextProcessor().getTextModelConfigMgr().addJenaMapping(getExtractionProcessor().getTextModelName(), altUrl);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return of;
	}

	private File saveCodeOwlFile(String outputFilename) throws ConfigurationException, IOException {
		File of = new File(new File(getExtractionProcessor().getCodeExtractor().getCodeModelFolder()).getParent() + 
				"/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT + "/" + outputFilename);
		of.getParentFile().mkdirs();
		getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr().saveOwlFile(getExtractionProcessor().getCodeModel(), getExtractionProcessor().getCodeModelName(), of.getCanonicalPath());
		String outputOwlFileName = of.getCanonicalPath();			
		getExtractionProcessor().getCodeExtractor().addCodeModel(outputOwlFileName, getExtractionProcessor().getCodeModel());

		String altUrl;
		try {
			altUrl = (new SadlUtils()).fileNameToFileUrl(outputOwlFileName);
			getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr().addMapping(altUrl, getExtractionProcessor().getCodeModelName(), getExtractionProcessor().getCodeModelPrefix(), false, "AnswerCurationManager");
			getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr().addJenaMapping(getExtractionProcessor().getCodeModelName(), altUrl);
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return of;
	}

	private void runInferenceDisplayInterestingTextModelResults(String outputOwlFileName, SaveAsSadl saveAsSadl) throws ConfigurationException {
		// clear reasoner from any previous model
		IConfigurationManagerForIDE textModelConfigMgr = getExtractionProcessor().getTextProcessor().getTextModelConfigMgr();
		textModelConfigMgr.clearReasoner();
		IReasoner reasoner = textModelConfigMgr.getReasoner();
		String textModelFolder = getExtractionProcessor().getCodeExtractor().getCodeModelFolder();		// same as code model folder, at least for now
		if (reasoner == null) {
			// use domain model folder because that's the project we're working in
			notifyUser(textModelFolder, "Unable to instantiate reasoner to analyze extracted code model.");
		}
		else {
			if (!reasoner.isInitialized()) {
				reasoner.setConfigurationManager(textModelConfigMgr);
				try {
					reasoner.initializeReasoner(textModelFolder, getExtractionProcessor().getTextModelName(), null);
					String queryString = "select ?lang ?expr where {?eq <rdf:type> <ExternalEquation> . ?eq <expression> ?script . ?script <script> ?expr . ?script <language> ?lang}";
					queryString = reasoner.prepareQuery(queryString);
					ResultSet results =  reasoner.ask(queryString);
					if (results != null && results.getRowCount() > 0) {
						results.setShowNamespaces(false);
						notifyUser(textModelFolder, "Equations found in extraction:\n" + results.toStringWithIndent(0, false));
					}
					else {
						notifyUser(textModelFolder, "No equations were found in this extraction from text.");
					}
					String importinfo = "To import this model for exploration in this window, add an import at the top of the window (after the 'uri' statement) for URI:\n   " + 
							getExtractionProcessor().getTextModelName() + "\n.";
					notifyUser(textModelFolder, importinfo);
				} catch (ReasonerNotFoundException e) {
					notifyUser(textModelFolder, e.getMessage());
					e.printStackTrace();
				} catch (InvalidNameException e) {
					notifyUser(textModelFolder, e.getMessage());
					e.printStackTrace();
				} catch (QueryParseException e) {
					notifyUser(textModelFolder, e.getMessage());
					e.printStackTrace();
				} catch (QueryCancelledException e) {
					notifyUser(textModelFolder, e.getMessage());
					e.printStackTrace();
				}
			}
		}		
	}

	private void runInferenceDisplayInterestingCodeModelResults(String outputOwlFileName, SaveAsSadl saveAsSadl, String fileContent)
			throws ConfigurationException, IOException {
		// clear reasoner from any previous model
		clearCodeModelReasoner();
		String codeModelFolder = getExtractionProcessor().getCodeExtractor().getCodeModelFolder();
		try {
			if (getInitializedCodeModelReasoner() == null) {
				// use domain model folder because that's the project we're working in
				notifyUser(codeModelFolder, "Unable to instantiate reasoner to analyze extracted code model.");
			}
			else {
				String queryString = SparqlQueries.INTERESTING_METHODS_DOING_COMPUTATION;	//?m ?b ?e ?s
				queryString = getInitializedCodeModelReasoner().prepareQuery(queryString);
				ResultSet results =  getInitializedCodeModelReasoner().ask(queryString);
				if (results != null && results.getRowCount() > 0) {
					results.setShowNamespaces(false);
					String[] cns = ((ResultSet) results).getColumnNames();
					if (cns[0].equals("m") && cns[1].equals("b") && cns[2].equals("e") && cns[3].equals("s")) {
						notifyUser(codeModelFolder, "The following methods doing computation were found in the extraction:");
						for (int r = 0; r < results.getRowCount(); r++) {
							String methodName = results.getResultAt(r, 0).toString();
							String javaCode = results.getResultAt(r, 3).toString();
							if (javaCode == null || !(javaCode.length() > 0)) {
								// get code from fileContent
								try {
									String startStr = results.getResultAt(r, 1).toString();
									int b = Integer.parseInt(startStr);
									String endStr = results.getResultAt(r, 2).toString();
									int e = Integer.parseInt(endStr);
									BufferedReader brdr = new BufferedReader(new StringReader(fileContent));
									String line = "";
									StringBuilder sb = new StringBuilder();
									for (int ln = 0; ln <= e - 1; ln++) {
										line = brdr.readLine();
										if (ln >= b - 1) {
											sb.append(line);
											if (ln < e - 1) {
												sb.append("\n");
											}
										}
									}
									javaCode = sb.toString();
								}
								catch (Exception e) {
									e.printStackTrace();
								}
							}
							if (methodName != null && javaCode != null) {
								AnswerExtractionProcessor ep = getExtractionProcessor();
								String className;
								if (methodName.indexOf('.') > 0) {
									int endOfClassName = methodName.indexOf('.');
									className = methodName.substring(0, endOfClassName);
								}
								else {
									className = "UnidentifiedClass";
								}
								String pythoncode = null;
								try {
									pythoncode = ep.translateMethodJavaToPython(className, javaCode);
									List<String> sadlDeclaration = convertExtractedMethodToExternalEquationInSadlSyntax(methodName, javaCode, pythoncode);
									for (String sd : sadlDeclaration) {
	//									System.out.println(sadlDeclaration);
	//									System.out.println("SADL equation:");
	//									System.out.println(sadlDeclaration);
										notifyUser(codeModelFolder, sd);
									}
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (CodeExtractionException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
					else {
						notifyUser(codeModelFolder, "An internal error has occurred querying the model. Please report.");
					}
				}
				else {
					notifyUser(codeModelFolder, "No interesting models were found in this extraction from code.");
				}
//				String importinfo = "To import this model for exploration in this window, add an import at the top of this window (after the 'uri' statement) for URI:\n   " + 
//						getExtractionProcessor().getCodeModelName() + "\n.";
//				notifyUser(codeModelFolder, importinfo);
			}
		} catch (ReasonerNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (QueryCancelledException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}		
	}

	private void clearCodeModelReasoner() {
		getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr().clearReasoner();
		setCodeModelReasoner(null);
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
			if (codeNode == null) {
				return("Failed: No code found for model '" + modelToBuildUri + "'");
			}
			else if (codeNode.isLiteral()) {
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

	
	public boolean importCodeSnippetToComputationalGraph(Object rs, String userInput) throws InvalidNameException, ConfigurationException, ReasonerNotFoundException, QueryParseException, QueryCancelledException, CodeExtractionException {
		boolean success = false;
		if (rs instanceof ResultSet) {
			boolean sns = ((ResultSet)rs).getShowNamespaces();
			((ResultSet)rs).setShowNamespaces(false);
			String[] cns = ((ResultSet) rs).getColumnNames();
			if (cns[0].equals("m")) {
				for (int r = 0; r < ((ResultSet)rs).getRowCount(); r++) {
					Object rsa = ((ResultSet) rs).getResultAt(r, 0);
					if (rsa.toString().equals(userInput)) {
						System.out.println("Ready to import method " + rsa.toString());
						
						if (cns[3].equals("s")) {
							String methScript = ((ResultSet)rs).getResultAt(r, 3).toString();
							System.out.println("Ready to build CG with method '" + rsa.toString() + "':");
							System.out.println(methScript);
							AnswerExtractionProcessor ep = getExtractionProcessor();
							String className;
							if (userInput.indexOf('.') > 0) {
								int endOfClassName = userInput.indexOf('.');
								className = userInput.substring(0, endOfClassName);
							}
							else {
								className = "UnidentifiedClass";
							}
							String pythoncode = null;
							try {
								pythoncode = ep.translateMethodJavaToPython(className, methScript);
								List<String> sadlDeclaration = convertExtractedMethodToExternalEquationInSadlSyntax(rsa.toString(), methScript, pythoncode);
								for (String sd : sadlDeclaration) {
									System.out.println(sadlDeclaration);
									System.out.println("SADL equation:");
									System.out.println(sadlDeclaration);
								}
								success = true;
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						else {
							throw new CodeExtractionException("No method script found");
						}
						break;
					}
				}
			}
			if (sns) {
				((ResultSet)rs).setShowNamespaces(sns);
			}
		}
		return success;
	}

	/**
	 * Method to convert extracted code to an instance of SADL External
	 * @param methodName -- should be the name of the method in the extracted code model
	 * @param pythoncode 
	 * @param methScript 
	 * @return -- the serialization of the new instance of External in SADL syntax
	 * @throws InvalidNameException
	 * @throws ConfigurationException
	 * @throws ReasonerNotFoundException
	 * @throws QueryParseException
	 * @throws QueryCancelledException
	 * @throws CodeExtractionException 
	 */
	private List<String> convertExtractedMethodToExternalEquationInSadlSyntax(String methodName, String javaCode, String pythonCode) throws InvalidNameException, ConfigurationException,
			ReasonerNotFoundException, QueryParseException, QueryCancelledException, CodeExtractionException {
		List<String> returnSadlStatements = new ArrayList<String>();
		StringBuilder sb = new StringBuilder("External ");
		sb.append(methodName);
		sb.append("(");
		clearCodeModelReasoner();
		// get inputs and outputs and identify semantic meaning thereof
		String inputQuery = "select ?arg ?argName ?argtyp where {<";
		inputQuery += methodName.toString().trim();
		inputQuery += "> <cmArguments>/<sadllistmodel:rest>*/<sadllistmodel:first> ?arg . ?arg <varName> ?argName . OPTIONAL{?arg <varType> ?argtyp}}";
		inputQuery = getInitializedCodeModelReasoner().prepareQuery(inputQuery);
		ResultSet inputResults =  getInitializedCodeModelReasoner().ask(inputQuery);
		System.out.println(inputResults != null ? inputResults.toStringWithIndent(5) : "no results");
		if (inputResults != null) {
			for (int r = 0; r < inputResults.getRowCount(); r++) {
				String argType = inputResults.getResultAt(r, 2).toString();
				String argName = inputResults.getResultAt(r, 1).toString();
				if (r > 0) {
					sb.append(", ");
				}
				sb.append(argType);
				sb.append(" ");
				sb.append(argName);
			}
		}
		sb.append(") returns ");
		
		String outputTypeQuery = "select ?rettyp where {<";
		outputTypeQuery += methodName.toString().trim();
		outputTypeQuery += "> <cmReturnTypes>/<sadllistmodel:rest>*/<sadllistmodel:first> ?rettyp }";
		outputTypeQuery = getInitializedCodeModelReasoner().prepareQuery(outputTypeQuery);
		ResultSet outputResults =  getInitializedCodeModelReasoner().ask(outputTypeQuery);
		System.out.println(outputResults != null ? outputResults.toStringWithIndent(5) : "no results");
		if (outputResults != null) {
			int numReturnValues = outputResults.getRowCount();
			if (numReturnValues > 1) {
				sb.append("[");
			}
			for (int r = 0; r < numReturnValues; r++) {
				String retType = outputResults.getResultAt(r, 0).toString();
				if (r > 0) {
					sb.append(", ");
				}
				sb.append(retType);
				sb.append(":");
			}
			if (numReturnValues > 1) {
				sb.append("]");
			}	
		}
		else {
			// SADL doesn't currently support an equation that doesn't return anything
			throw new CodeExtractionException("Equations that do not return a value are not supported.");
		}
		String eqUri = getExtractionProcessor().getCodeModelName() + "#" + methodName.toString().trim();
		sb.append(" \"");
		sb.append(eqUri);
		sb.append("\".\n");
		returnSadlStatements.add(sb.toString());
		
		// now add the scripts in Java and Python
		StringBuilder sb2 = new StringBuilder(methodName);
		sb2.append(" has expression (a Script with language Java, with script \n\"");
		sb2.append(escapeDoubleQuotes(javaCode));
		sb2.append("\"\n), has expression (a Script with language Python, with script \n\"");
		sb2.append(escapeDoubleQuotes(pythonCode));
		sb2.append("\").");
		returnSadlStatements.add(sb2.toString());
		return returnSadlStatements;
	}

	/**
	 * Method to replace each double quote (") with an escaped double quote (\")
	 * @param strIn -- input string
	 * @return -- output string
	 */
	private String escapeDoubleQuotes(String strIn) {
		return strIn.replace("\"", "\\\"");
	}
	
	/**
	 * Method to save the OWL model created from code extraction as a SADL file
	 * @param outputOwlFiles -- List of names of the OWL file created
	 * @return -- the sadl fully qualified file name
	 * @throws IOException
	 */
	public List<String> saveAsSadlFile(Map<File, Boolean> outputOwlFiles, String response) throws IOException {
		if (isYes(response)) {
			List<String> sadlFileNames = new ArrayList<String>();
			for (File outputOwlFile : outputOwlFiles.keySet()) {
				boolean isCodeExtract = outputOwlFiles.get(outputOwlFile);
				String key = outputOwlFile.getCanonicalPath();
				OntModel mdl = null;
				IConfigurationManagerForIDE cfgmgr = null;
				String mdlName = null;
				if (isCodeExtract) {
					mdl = getExtractionProcessor().getCodeExtractor().getCodeModel(key);
					cfgmgr = getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr();
					mdlName = getExtractionProcessor().getCodeModelName();
				}
				else {
					mdl = getExtractionProcessor().getTextProcessor().getTextModel(key);
					cfgmgr = getExtractionProcessor().getTextProcessor().getTextModelConfigMgr();
					mdlName = getExtractionProcessor().getTextModelName();
				}
				if (mdl != null) {
					OwlToSadl ots = new OwlToSadl(mdl);
					String sadlFN = outputOwlFile.getCanonicalPath() + ".sadl";
					File sf = new File(sadlFN);
					if (sf.exists()) {
						sf.delete();
					}
					try {
						boolean status = ots.saveSadlModel(sadlFN);
						if (status) {
							sadlFileNames.add(sadlFN);
							/* We want to move the OWL file to the OwlModels folder so that it will exist,
							 * and change the mappings. If we aren't in the SADL IDE (e.g., JUnit tests),
							 * the .sadl file will not get rebuilt. If we are, it should be replaced(?).
							 */
							String newOwlFileName = cfgmgr.getModelFolder() + "/" + SadlUtils.replaceFileExtension(sf.getName(), "sadl", "owl");
							File newOwlFile = new File(newOwlFileName);
							if (newOwlFile.exists()) {
								newOwlFile.delete();
							}
							if (outputOwlFile.renameTo(newOwlFile)) {
								String altUrl;
								try {
									String prefix = cfgmgr.getGlobalPrefix(mdlName);
									altUrl = (new SadlUtils()).fileNameToFileUrl(newOwlFile.getCanonicalPath());
									cfgmgr.deleteMapping(altUrl,mdlName);
									cfgmgr.addMapping(altUrl, mdlName, prefix, true, "AnswerCurationManager");
									cfgmgr.addJenaMapping(mdlName, altUrl);		// this will replace old mapping?
								} catch (URISyntaxException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (ConfigurationException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					} catch (OwlImportException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidNameException e1) {
						// TODO Auto-generated catch block
						e1.printStackTrace();
					}
				}
				else {
					try {
						notifyUser(getDomainModelConfigurationManager().getModelFolder(), "Failed to save " + outputOwlFile.getName() + " as SADL file.");
					} catch (ConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			return sadlFileNames;
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

	public String makeConnectionAndGetResponse(URL url, JsonObject jsonObject) throws IOException {
		String response = "";
		HttpURLConnection connection = null;
		try {
			connection = (HttpURLConnection) url.openConnection();                     
			connection.setDoOutput(true);
			connection.setRequestMethod("POST"); 
			connection.setRequestProperty("Content-Type", "application/json");

			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(jsonObject.toString().getBytes());
			outputStream.flush();
			BufferedReader br = null;
			try {
				br = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));                                     
				String output = "";
				while((output = br.readLine()) != null) 
					response = response + output;                 
				outputStream.close();
			}
			catch (Exception e) {
				System.err.println("Error reading response: " + e.getMessage());
				e.printStackTrace();
				throw new IOException(e.getMessage(), e);
			}
			finally {
				if (br != null) {
					br.close();
				}
			}
		} catch (Exception e) {
			System.err.println(e.getMessage() + "\nJsonObject:");
			System.err.println(jsonObject.toString());
			e.printStackTrace();
			throw new IOException(e.getMessage(), e);
		}
		finally {
			if (connection != null) {
				connection.disconnect();
			}
		}
		return response;
	}

	private IReasoner getInitializedCodeModelReasoner() throws ConfigurationException, ReasonerNotFoundException {
		if (codeModelReasoner == null) {
			codeModelReasoner = getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr().getReasoner();
			String codeModelFolder = getExtractionProcessor().getCodeExtractor().getCodeModelFolder();
			if (!codeModelReasoner.isInitialized()) {
				IConfigurationManagerForIDE codeModelConfigMgr = getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr();
				codeModelReasoner.setConfigurationManager(codeModelConfigMgr);
				codeModelReasoner.initializeReasoner(codeModelFolder, getExtractionProcessor().getCodeModelName(), null);
			}
		}
		return codeModelReasoner;
	}

	private void setCodeModelReasoner(IReasoner codeModelReasoner) {
		this.codeModelReasoner = codeModelReasoner;
	}

	/**
	 * Method to add a statement to the conversation
	 * @param statement
	 * @param agent
	 * @return
	 */
	public boolean addToConversation(ConversationElement statement, Agent agent) {
		if (agent.equals(Agent.USER)) {
			return addToConversation(statement);
		}
		else if (agent.equals(Agent.CM)) {
			return addToConversation(statement);
		}
		return false;
	}
	
	public DialogContent getConversation() {
		if (conversation == null) {
			conversation = new DialogContent(getDialogAnswerProvider().getResource(), this);
		}
		return conversation;
	}

	public List<ConversationElement> getConversationElements() {
		return getConversation().getStatements();
	}

	public boolean addToConversation(ConversationElement statement) {
		return getConversation().addStatement(statement);
	}

	public boolean addToConversation(ConversationElement statement, int location) {
		if (getConversation().getStatements().size() <= location) {
			return false;
		}
		getConversation().getStatements().add(location, statement);
		return true;
	}
	
	public boolean resetConversation() {
		lastConversation = conversation;
		conversation = null;
		return true;
	}
	
	public DialogContent getLastConversation() {
		return lastConversation;
	}
}

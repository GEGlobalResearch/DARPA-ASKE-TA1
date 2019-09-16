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
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.dialog.AnswerCMStatement;
import com.ge.research.sadl.darpa.aske.inference.JenaBasedDialogInferenceProcessor;
import com.ge.research.sadl.darpa.aske.processing.AnswerContent;
import com.ge.research.sadl.darpa.aske.processing.AnswerPendingContent;
import com.ge.research.sadl.darpa.aske.processing.ConversationElement;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.DialogContent;
import com.ge.research.sadl.darpa.aske.processing.EvalContent;
import com.ge.research.sadl.darpa.aske.processing.ExpectsAnswerContent;
import com.ge.research.sadl.darpa.aske.processing.HowManyValuesContent;
import com.ge.research.sadl.darpa.aske.processing.IDialogAnswerProvider;
import com.ge.research.sadl.darpa.aske.processing.ModifiedAskContent;
import com.ge.research.sadl.darpa.aske.processing.SaveContent;
import com.ge.research.sadl.darpa.aske.processing.StatementContent;
import com.ge.research.sadl.darpa.aske.processing.WhatIsContent;
import com.ge.research.sadl.darpa.aske.processing.WhatValuesContent;
import com.ge.research.sadl.darpa.aske.processing.imports.AnswerExtractionProcessor;
import com.ge.research.sadl.darpa.aske.processing.imports.AnswerExtractionProcessor.CodeLanguage;
import com.ge.research.sadl.darpa.aske.processing.imports.CodeExtractionException;
import com.ge.research.sadl.darpa.aske.processing.imports.IModelFromCodeExtractor;
import com.ge.research.sadl.darpa.aske.processing.imports.TextProcessor;
import com.ge.research.sadl.jena.reasoner.JenaReasonerPlugin;
import com.ge.research.sadl.model.gp.GraphPatternElement;
import com.ge.research.sadl.model.gp.Junction;
import com.ge.research.sadl.model.gp.Junction.JunctionType;
import com.ge.research.sadl.model.gp.NamedNode;
import com.ge.research.sadl.model.gp.NamedNode.NodeType;
import com.ge.research.sadl.model.gp.Node;
import com.ge.research.sadl.model.gp.ProxyNode;
import com.ge.research.sadl.model.gp.Query;
import com.ge.research.sadl.model.gp.TripleElement;
import com.ge.research.sadl.model.gp.VariableNode;
import com.ge.research.sadl.model.visualizer.GraphVizVisualizer;
import com.ge.research.sadl.model.visualizer.IGraphVisualizer;
import com.ge.research.sadl.owl2sadl.OwlImportException;
import com.ge.research.sadl.owl2sadl.OwlToSadl;
import com.ge.research.sadl.processing.ISadlInferenceProcessor;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.processing.SadlConstants;
import com.ge.research.sadl.processing.SadlInferenceException;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.IConfigurationManager;
import com.ge.research.sadl.reasoner.IReasoner;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.SadlCommandResult;
import com.ge.research.sadl.reasoner.TranslationException;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.ge.research.sadl.utils.ResourceManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.Restriction;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.rdf.model.Statement;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.shared.NotFoundException;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class AnswerCurationManager {
	
	protected static final Logger logger = LoggerFactory.getLogger(AnswerCurationManager.class);
	private String domainModelowlModelsFolder;
	
	private Map<String, String> questionsAndAnswers = new HashMap<String, String>();
	
	private IConfigurationManagerForIDE domainModelConfigurationManager;
	private Map<String, String> preferences = null;
	private AnswerExtractionProcessor extractionProcessor = null;

	private IDialogAnswerProvider dialogAnswerProvider = null;	// The instance of an implementer of a DialogAnswerProvider

	private IReasoner codeModelReasoner;
	public enum SaveAsSadl{SaveAsSadl, DoNotSaveAsSadl, AskUserSaveAsSadl}

	public enum Agent {USER, CM}
	private String userName;
	private DialogContent conversation = null; 
	private DialogContent lastConversation = null;

	private Map<String, String[]> targetModelMap = null;
	private OwlToSadl owl2sadl = null;

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
						dap.addCurationManagerInitiatedContent(this, "saveAsSadlFile", args, doubleQuoteContent("Would you like to save the extracted model (" + sb.toString() + ") in SADL format?"));						
					}
					else {
						dap.addCurationManagerInitiatedContent(this, "saveAsSadlFile", args, doubleQuoteContent("Would you like to save the extracted models (" + sb.toString() + ") in SADL format?"));
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
			notifyUser(textModelFolder, "Unable to instantiate reasoner to analyze extracted code model.", true);
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
						notifyUser(textModelFolder, "Equations found in extraction:\n" + results.toStringWithIndent(0, false), true);
					}
					else {
						notifyUser(textModelFolder, "No equations were found in this extraction from text.", true);
					}
					String importinfo = "To import this model for exploration in this window, add an import at the top of the window (after the 'uri' statement) for URI:\n   " + 
							getExtractionProcessor().getTextModelName() + "\n.";
					notifyUser(textModelFolder, importinfo, true);
				} catch (ReasonerNotFoundException e) {
					notifyUser(textModelFolder, e.getMessage(), true);
					e.printStackTrace();
				} catch (InvalidNameException e) {
					notifyUser(textModelFolder, e.getMessage(), true);
					e.printStackTrace();
				} catch (QueryParseException e) {
					notifyUser(textModelFolder, e.getMessage(), true);
					e.printStackTrace();
				} catch (QueryCancelledException e) {
					notifyUser(textModelFolder, e.getMessage(), true);
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
				notifyUser(codeModelFolder, "Unable to instantiate reasoner to analyze extracted code model.", true);
			}
			else {
				String queryString = SparqlQueries.INTERESTING_METHODS_DOING_COMPUTATION;	//?m ?b ?e ?s
				queryString = getInitializedCodeModelReasoner().prepareQuery(queryString);
				ResultSet results =  getInitializedCodeModelReasoner().ask(queryString);
				if (results != null && results.getRowCount() > 0) {
					results.setShowNamespaces(false);
					String[] cns = ((ResultSet) results).getColumnNames();
					if (cns[0].equals("m") && cns[1].equals("b") && cns[2].equals("e") && cns[3].equals("s")) {
						notifyUser(codeModelFolder, "The following methods doing computation were found in the extraction:", true);
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
										notifyUser(codeModelFolder, sd, false);
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
						notifyUser(codeModelFolder, "An internal error has occurred querying the model. Please report.", true);
					}
				}
				else {
					notifyUser(codeModelFolder, "No interesting models were found in this extraction from code.", true);
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
	
	/**
	 * Method to save a specified Equation to a given semantic model.
	 * @param resource -- Xtext resource of Dialog window hosting save command
	 * @param ontModel -- the ontModel of the Dialog window content hosting save command
	 * @param modelName -- the model name of the Dialog window model nosting save command
	 * @param sc -- a SaveContent instance containing the URI of the equation to be saved
	 * @return
	 * @throws ConfigurationException
	 * @throws IOException
	 * @throws QueryParseException
	 * @throws QueryCancelledException
	 * @throws ReasonerNotFoundException
	 */
	public String processSaveRequest(org.eclipse.emf.ecore.resource.Resource resource, OntModel ontModel, String modelName, SaveContent sc) throws ConfigurationException, IOException, QueryParseException, QueryCancelledException, ReasonerNotFoundException {
		String returnValue = null;
		URI resourceURI = resource.getURI();
		IReasoner reasoner = null;
		if (ResourceManager.isSyntheticUri(null, resourceURI)) {
			reasoner = getInitializedReasonerForConfiguration(getDomainModelConfigurationManager(), ontModel, modelName);
		}
		else {
			reasoner = getInitializedReasonerForConfiguration(domainModelConfigurationManager, modelName);
		}
		String equationToBuildUri = sc.getSourceEquationUri();
		Individual extractedModelInstance = ontModel.getIndividual(equationToBuildUri);
		if (extractedModelInstance == null) {
			// try getting a text extraction model?
			ontModel.write(System.out, "N3");
			StmtIterator stmtItr = ontModel.listStatements(null, RDF.type, ontModel.getOntClass("http://sadl.org/sadlimplicitmodel#ExternalEquation"));
			while (stmtItr.hasNext()) {
				System.out.println(stmtItr.nextStatement().getSubject().toString());
			}
			throw new NotFoundException("Equation with URI " + equationToBuildUri + " not found.");
		}
		Statement argStmt = extractedModelInstance.getProperty(ontModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_ARGUMENTS_PROPERTY_URI));
		if (argStmt != null) {
			com.hp.hpl.jena.rdf.model.Resource ddList = argStmt.getObject().asResource();
			if (ddList instanceof Resource) {
				String argQuery = "select ?argName ?argType where {<" + extractedModelInstance.getURI() + "> <" + 
						SadlConstants.SADL_IMPLICIT_MODEL_ARGUMENTS_PROPERTY_URI + 
						"> ?ddList . ?ddList <http://jena.hpl.hp.com/ARQ/list#member> ?member . ?member <" +
						SadlConstants.SADL_IMPLICIT_MODEL_DESCRIPTOR_NAME_PROPERTY_URI + "> ?argName . ?member <" +
						SadlConstants.SADL_IMPLICIT_MODEL_DATATYPE_PROPERTY_URI + "> ?argType}";							// query to get the argument names and types
				ResultSet rs = reasoner.ask(argQuery);
				System.out.println(rs != null ? rs.toString() : "no argument results");
				String retQuery = "select ?retName ?retType where {<" + extractedModelInstance.getURI() + "> <" + 
						SadlConstants.SADL_IMPLICIT_MODEL_RETURN_TYPES_PROPERTY_URI + 
						"> ?ddList . ?ddList <http://jena.hpl.hp.com/ARQ/list#member> ?member . OPTIONAL{?member <" +
						SadlConstants.SADL_IMPLICIT_MODEL_DESCRIPTOR_NAME_PROPERTY_URI + "> ?retName} . ?member <" +
						SadlConstants.SADL_IMPLICIT_MODEL_DATATYPE_PROPERTY_URI + "> ?retType}";							// query to get the return types
				ResultSet rs2 = reasoner.ask(retQuery);
				System.out.println(rs2 != null ? rs2.toString() : "no return type results");
				String pythonScriptQuery = "select ?pyScript where {<" + extractedModelInstance.getURI() + "> <" + 
						SadlConstants.SADL_IMPLICIT_MODEL_EXPRESSTION_PROPERTY_URI +
						"> ?sc . ?sc <" + SadlConstants.SADL_IMPLICIT_MODEL_LANGUAGE_PROPERTY_URI + "> <" + 
						SadlConstants.SADL_IMPLICIT_MODEL_PYTHON_LANGUAGE_INST_URI +
						"> . ?sc <" + SadlConstants.SADL_IMPLICIT_MODEL_SCRIPT_PROPERTY_URI + "> ?pyScript}";				// query to get the Python script for the equation
				ResultSet rs3 = reasoner.ask(pythonScriptQuery);
				System.out.println(rs3 != null ? rs3.toString() : "no Python script results");
				if (rs3 != null && rs3.getRowCount() != 1) {
					return "There appears to be more than one Python script associated with the equation. Unable to save.";
				}
				if (rs3 != null) {
					String pythonScript = rs3.getResultAt(0, 0).toString();
					CodeLanguage language = CodeLanguage.JAVA;																	// only code we extract from currently (what about from text?)
					if (language.equals(CodeLanguage.JAVA)) {
						String[] returns = getExtractionProcessor().getCodeExtractor(CodeLanguage.JAVA).extractPythonEquationFromCodeExtractionModel(pythonScript);
						if (returns.length != 2) {
							throw new IOException("Invalid return from extractPythonEquationFromCodeExtractionModel; expected String[] of size 2");
						}
						String methName = returns[0];
						String modifiedPythonScript = returns[1];
						System.out.println(modifiedPythonScript);		
						// this seems klugey also
	//					returnValue = getExtractionProcessor().saveToComputationalGraph(equationToBuildUri, methName, rs, rs2, modifiedPythonScript, null);
						returnValue = getExtractionProcessor().saveToComputationalGraph(methName, methName, rs, rs2, modifiedPythonScript, null);
						System.out.println("saveToComputationalGraph returned '" + returnValue + "'");
					}
				}
				else {
					returnValue = "Failed to find a Python script for equation '" + equationToBuildUri + "'.";
				}
			}
		}
		if (returnValue == null) {
			return returnValue = "Failed: Unable to find model '" + equationToBuildUri + "' in code model.";
		}
		return returnValue;
	}

	private String stripNamespaceDelimiter(String ns) {
		if (ns.endsWith("#")) {
			ns = ns.substring(0, ns.length() - 1);
		}
		return ns;
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
						notifyUser(getDomainModelConfigurationManager().getModelFolder(), "Failed to save " + outputOwlFile.getName() + " as SADL file.", true);
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
	
	public void notifyUser(String modelFolder, String msg, boolean quote) throws ConfigurationException {
		if (quote) {
			msg = doubleQuoteContent(msg);
		}
		if (getDialogAnswerProvider() != null) {
			// talk to the user via the Dialog editor
			Method acmic = null;
			try {
				acmic = getDialogAnswerProvider().getClass().getMethod("addCurationManagerInitiatedContent", AnswerCurationManager.class, String.class);
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
					acmic.invoke(getDialogAnswerProvider(), this, msg);
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
	
	public String answerUser(String modelFolder, String msg, boolean quote, EObject ctx) throws ConfigurationException {
		if (quote) {
			msg = doubleQuoteContent(msg);
		}
		if (getDialogAnswerProvider() != null) {
			// talk to the user via the Dialog editor
			Method acmic = null;
			try {
				acmic = getDialogAnswerProvider().getClass().getMethod("addCurationManagerAnswerContent", AnswerCurationManager.class, String.class, Object.class);
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
						if (m.getName().equals("addCurationManagerAnswerContent")) {
							acmic = m;
							break;
						}
					}
				}
			}
			if (acmic != null) {
				acmic.setAccessible(true);
				try {
					Object retval = acmic.invoke(getDialogAnswerProvider(), this, msg, ctx);
					if (retval != null) {
						return retval.toString();
					}
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
		return null;
	}


	private String doubleQuoteContent(String msg) {
		String modContent = null;
		if (!msg.startsWith("\"") && !msg.endsWith("\"")) {
			if (msg.endsWith(".") || msg.endsWith("?")) {
				String lastChar = msg.substring(msg.length() - 1);
				modContent = "\"" + msg.substring(0, msg.length() - 1).replaceAll("\"", "'") + "\"" + lastChar;
			}
			else {
				modContent = "\"" + msg.replaceAll("\"", "'") + "\"" + ".";
			}
			msg = modContent;
		}
		return msg;
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

	/**
	 * Method to answer whether the DialogAnswerProvider has been initialized
	 * @return
	 */
	public boolean dialogAnserProviderInitialized() {
		if (dialogAnswerProvider == null) {
			setDialogAnswerProvider((IDialogAnswerProvider) getDomainModelConfigurationManager().getPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER));
		}
		return (dialogAnswerProvider != null);
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
	
	private IReasoner getInitializedReasonerForConfiguration(IConfigurationManager cm, String modelName) throws ConfigurationException, ReasonerNotFoundException, IOException {
		IReasoner reasoner = cm.getReasoner();
		if (!reasoner.isInitialized()) {
			reasoner.initializeReasoner(cm.getModelFolder(), modelName, null);
		}
		return reasoner;
	}

	private IReasoner getInitializedReasonerForConfiguration(
			IConfigurationManagerForIDE cm, OntModel ontModel, String modelName) throws ConfigurationException, ReasonerNotFoundException {
		IReasoner reasoner = cm.getReasoner();
		if (!reasoner.isInitialized()) {
			reasoner.initializeReasoner(ontModel, modelName, null, null);
		}
		return reasoner;
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
	
	/**
	 * Method to set the DialogContent, saving any existing conversation as the last conversation
	 * @param dc -- the new DialogContent
	 * @return -- return true if an existing conversation is saved as the last conversation
	 */
	public boolean setConversation(DialogContent dc) {
		boolean retval = false;
		if (conversation != null) {
			lastConversation = conversation;
			retval = true;
		}
		conversation = dc;
		return retval;
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
	
	public DialogContent getLastConversation() {
		return lastConversation;
	}

	public AnswerCMStatement getLastACMQuestion() {
		// TODO Auto-generated method stub
		return null;
	}

	public void addTargetModel(String uri, String altUrl, String prefix) {
		String[] targetUris = new String[2];
		targetUris[0] = uri;
		targetUris[1] = altUrl;
		getTargetModelMap().put(prefix, targetUris);
		
	}

	public Map<String, String[]> getTargetModelMap() {
		return targetModelMap;
	}

	public void addTargetModelToMap(String alias, String[] targetUris) {
		if (targetModelMap == null) {
			targetModelMap = new HashMap<String, String[]>();
		}
		targetModelMap.put(alias, targetUris);
	}
	
	public String processUserRequest(org.eclipse.emf.ecore.resource.Resource resource, OntModel theModel, String modelName, ExpectsAnswerContent sc) throws ConfigurationException, ExecutionException, IOException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException, SadlInferenceException {
		String retVal;
		if (sc instanceof WhatIsContent) {
    		retVal = processWhatIsContent(resource, theModel, modelName, (WhatIsContent) sc);
    	}
		else if (sc instanceof ModifiedAskContent) {
			retVal = processModifiedAsk(resource, theModel, modelName, (ModifiedAskContent) sc);
		}
		else if (sc instanceof WhatValuesContent) {
			retVal = processWhatValuesContent(resource, theModel, modelName, (WhatValuesContent)sc);
		}
		else if (sc instanceof HowManyValuesContent) {
			retVal = processHowManyValue(resource, theModel, modelName, (HowManyValuesContent)sc);
		}
		else if (sc instanceof SaveContent) {
			retVal = processSaveRequest(resource, theModel, modelName, (SaveContent)sc);
		}
		else if (sc instanceof EvalContent) {
			retVal = processEvalRequest(resource, theModel, modelName, (EvalContent)sc);
			retVal = "Eval not yet implemented";
		}
		else {
			System.out.println("Need to add '" + sc.getClass().getCanonicalName() + "' to processUserRequest");
			retVal = "Not yet implemented";
		}
		return retVal;
	}
	
	private String processEvalRequest(org.eclipse.emf.ecore.resource.Resource resource, OntModel theModel,
			String modelName, EvalContent sc) {
		// TODO Auto-generated method stub
		return null;
	}

	private String processModifiedAsk(org.eclipse.emf.ecore.resource.Resource resource, OntModel theModel, String modelName, ModifiedAskContent sc) throws ConfigurationException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException {
		Query q = ((ModifiedAskContent)sc).getQuery();
		String answer = null;
		boolean quote = true;
		try {
			ResultSet rs = runQuery(resource, q);
			if (rs != null) {
				rs.setShowNamespaces(false);
				if (rs.getColumnCount() == 1) {
					if (rs.getRowCount() == 1) {
						answer = rs.getResultAt(0, 0).toString();
					}
					else {
						answer = "";
						for (int i = 0; i < rs.getRowCount(); i++) {
							if (i > 0) answer += ",";
							answer += rs.getResultAt(i, 0);
						}
					}
				}
				else {
					answer = rs.toString();
					answer.replace("\"", "'");
				}
				quote = true;
			}
			else {
				answer = "No results found";
				quote = true;
			}
		}
		catch (Exception e) {
			answer = e.getMessage();
			quote = true;
		}
		if (answer != null) {
			answerUser(getDomainModelOwlModelsFolder(), answer, quote, sc.getHostEObject());
			return answer;
		}
		return null;
	}

	private String processWhatValuesContent(org.eclipse.emf.ecore.resource.Resource resource, OntModel theModel,
			String modelName, WhatValuesContent sc) throws ConfigurationException {
		StringBuilder sb = new StringBuilder();
		Node cls = sc.getCls();
		String article = sc.getArticle();
		Node prop = sc.getProp();
		
		OntClass theClass = theModel.getOntClass(cls.getURI());
		OntProperty theProp = theModel.getOntProperty(prop.toFullyQualifiedString());
		ExtendedIterator<OntClass> scitr = theClass.listSuperClasses();
		boolean restrictionFound = false;
		while (scitr.hasNext()) {
			OntClass scnxt = scitr.next();
			if (scnxt.isRestriction()) {
				Restriction restrict = scnxt.asRestriction();
				OntProperty rprop = restrict.getOnProperty();
				if (rprop.equals(theProp)) {
					restrictionFound = true;
				}
			}
		}
		if (!restrictionFound) {
			StmtIterator stmtitr = theModel.listStatements(theProp, RDFS.range, (RDFNode)null);
			while (stmtitr.hasNext()) {
				RDFNode obj = stmtitr.nextStatement().getObject();
				if (obj.isURIResource()) {
					if (sb.length() > 0) {
						sb.append(" or");
					}
					sb.append(obj.asResource().getLocalName());
				}
			}
		}
		String answer = sb.toString();
		answerUser(getDomainModelOwlModelsFolder(), answer, false, sc.getHostEObject());
		return answer;
	}

	private String processHowManyValue(org.eclipse.emf.ecore.resource.Resource resource, OntModel theModel,
			String modelName, HowManyValuesContent sc) throws ConfigurationException {
		StringBuilder sb = new StringBuilder();
		Node cls = sc.getCls();
		String article = sc.getArticle();
		Node prop = sc.getProp();
		
		OntClass theClass = theModel.getOntClass(cls.getURI());
		OntProperty theProp = theModel.getOntProperty(prop.toFullyQualifiedString());
		ExtendedIterator<OntClass> scitr = theClass.listSuperClasses();
		boolean restrictionFound = false;
		while (scitr.hasNext()) {
			OntClass scnxt = scitr.next();
			if (scnxt.isRestriction()) {
				Restriction restrict = scnxt.asRestriction();
				OntProperty rprop = restrict.getOnProperty();
				if (rprop.equals(theProp)) {
					restrictionFound = true;
				}
			}
		}
		if (!restrictionFound) {
			StmtIterator stmtitr = theModel.listStatements(theProp, RDFS.range, (RDFNode)null);
			while (stmtitr.hasNext()) {
				RDFNode obj = stmtitr.nextStatement().getObject();
				if (obj.isURIResource()) {
					if (sb.length() > 0) {
						sb.append(" or");
					}
					sb.append(obj.asResource().getLocalName());
				}
			}
		}
		sb.insert(0, "unlimited number of values of type ");
		String answer = sb.toString();
		answerUser(getDomainModelOwlModelsFolder(), answer, true, sc.getHostEObject());
		return answer;
	}

	private String processWhatIsContent(org.eclipse.emf.ecore.resource.Resource resource, OntModel theModel,
			String modelName, WhatIsContent sc) throws ExecutionException, SadlInferenceException,
			TranslationException, ConfigurationException, IOException {
		String retVal = null;
		Object trgt = ((WhatIsContent)sc).getTarget();
		Object whn = ((WhatIsContent)sc).getWhen();
		if (trgt instanceof NamedNode && whn == null) {
			String answer;
			try {
				answer = whatIsNamedNode(resource, theModel, getDomainModelOwlModelsFolder(), (NamedNode)trgt);
			}
			catch (Exception e) {
				answer = e.getMessage();
			}
			if (answer != null) {
				retVal = answer;
			}
		}
		else if (trgt instanceof Object[] && whn == null) {
			if (allTripleElements((Object[])trgt)) {
				Object ctx = null;
				TripleElement[] triples = flattenTriples((Object[])trgt);
				ctx = triples[0].getContext();
				StringBuilder answer = new StringBuilder();
				Object[] rss = insertTriplesAndQuery(resource, triples);
				String resultStr = null;
				if (rss != null) {
					StringBuilder sb = new StringBuilder();
					if (triples[0].getSubject() instanceof VariableNode && 
							((VariableNode)triples[0].getSubject()).getType() instanceof NamedNode) {
						sb.append("the ");
						sb.append(((VariableNode)(triples[0].getSubject())).getType().getName());
						sb.append(" has ");
						sb.append(triples[0].getPredicate().getName());
						sb.append(" ");
						sb.append(((ResultSet) rss[0]).getResultAt(0, 0).toString());
						resultStr = sb.toString();
					}
					else {
		    			for (Object rs : rss) {
		    				if (rs instanceof ResultSet) {
		    					((ResultSet) rs).setShowNamespaces(true);
		    					sb.append(rs.toString());
		    				}
		    				else {
		    					throw new TranslationException("Expected ResultSet, got " + rs.getClass().getCanonicalName());
		    				}
		    			}
						resultStr = resultSetToQuotableString(sb.toString());
					}
				}
				String insertionText = (resultStr != null ? resultStr : "\"Failed to find results\"");
				answerUser(getDomainModelOwlModelsFolder(), insertionText, false, sc.getHostEObject());
				retVal = insertionText;
			}
		}
		else {
			// there is a when clause, and this is in a WhatIsConstruct
			List<TripleElement> tripleLst = new ArrayList<TripleElement>();
			if (trgt instanceof TripleElement) {
				tripleLst.add((TripleElement)trgt);
			}
			else if (trgt instanceof Junction) {
				tripleLst = addTriplesFromJunction((Junction) trgt, tripleLst);
			}
			if (whn instanceof TripleElement) {
				// we have a when statement
				tripleLst.add((TripleElement)whn);
			}
			else if (whn instanceof Junction) {
				tripleLst = addTriplesFromJunction((Junction) whn, tripleLst);
			}
			TripleElement[] triples = new TripleElement[tripleLst.size()];
			triples = tripleLst.toArray(triples);
			
			StringBuilder answer = new StringBuilder();
			Object[] rss = insertTriplesAndQuery(resource, triples);
			if (rss != null) {
				int cntr = 0;
				for (Object rs : rss) {
					if (rs instanceof ResultSet) {
		    			if (cntr == 0) {
		    				// this is the first ResultSet, construct a graph if possible
		    				if (((ResultSet) rs).getColumnCount() != 3) {
		    					System.err.println("Can't construct graph; not 3 columns. Unexpected result.");
		    				}
//            				this.graphVisualizerHandler.resultSetToGraph(path, resultSet, description, baseFileName, orientation, properties);
		    				
		    				IGraphVisualizer visualizer = new GraphVizVisualizer();
		    				if (visualizer != null) {
		    					String graphsDirectory = new File(getDomainModelOwlModelsFolder()).getParent() + "/Graphs";
		    					new File(graphsDirectory).mkdir();
		    					String baseFileName = "QueryMetadata";
		    					visualizer.initialize(
		    		                    graphsDirectory,
		    		                    baseFileName,
		    		                    baseFileName,
		    		                    null,
		    		                    IGraphVisualizer.Orientation.TD,
		    		                    "Assembled Model");
		    					((ResultSet) rs).setShowNamespaces(false);
		    		            visualizer.graphResultSetData((ResultSet) rs);	
		    		        }
							String fileToOpen = visualizer.getGraphFileToOpen();
							if (fileToOpen != null) {
								File fto = new File(fileToOpen);
								if (fto.isFile()) {
// TODO graphing refactoring
//    								IFileStore fileStore = EFS.getLocalFileSystem().getStore(fto.toURI());
//    								IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
//    								try {
//    									IDE.openEditorOnFileStore(page, fileStore);
//    								}
//    								catch (Throwable t) {
//    									System.err.println("Error trying to display graph file '" + fileToOpen + "': " + t.getMessage());
//    								}
								}
								else if (fileToOpen != null) {
									System.err.println("Failed to open graph file '" + fileToOpen + "'. Try opening it manually.");
								}
							}
		    				else {
		    					System.err.println("Unable to find an instance of IGraphVisualizer to render graph for query.\n");
		    				}

		    			}
		    			if (cntr > 0) 
		    				answer.append(resultSetToQuotableString((ResultSet) rs));
		    			if(cntr++ > 1)
		    				answer.append(",\n");
					}
					else {
						throw new TranslationException("Expected ResultSet but got " + rs.getClass().getCanonicalName());
					}
					
				}
				answer.append(".\n");
			}
			if (rss != null) {
				retVal = answer.toString();
			}
			else {
				retVal = "Failed to evaluate answer";
			}
			answerUser(getDomainModelOwlModelsFolder(), retVal , true, sc.getHostEObject());
		}
		return retVal;
	}

	private String whatIsNamedNode(org.eclipse.emf.ecore.resource.Resource resource, OntModel theModel, String modelFolder, NamedNode lastcmd) throws ConfigurationException, ExecutionException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException {
		// what is NamedNode?
		NamedNode nn = (NamedNode) lastcmd;
		NodeType typ = nn.getNodeType();
		boolean isFirstProperty = true;
		StringBuilder answer = new StringBuilder();
		if (typ.equals(NodeType.ClassNode)) {
//			answer.append(checkForKeyword(nn.getName()));
//			int len = answer.length();
//			answer = getClassHierarchy(resource, nn, answer);
//			if (answer.length() == len) {
//				answer.append(" is a class");
//			}
			answer.append(getOwlToSadl(theModel).classToSadl(nn.getURI()));
//			isFirstProperty = addDomainAndRange(resource, nn, isFirstProperty, answer);
//			addQualifiedCardinalityRestriction(resource, nn, isFirstProperty, answer);		
			Object ctx = ((NamedNode)lastcmd).getContext();
			answerUser(modelFolder, answer.toString(), false, (EObject) ctx);
			return answer.toString();
		}
		else if (typ.equals(NodeType.ObjectProperty) || typ.equals(NodeType.DataTypeProperty) || typ.equals(NodeType.PropertyNode)) {
			addPropertyWithDomainAndRange(resource, nn, answer);
			Object ctx = ((NamedNode)lastcmd).getContext();
			answerUser(modelFolder, answer.toString(), false, (EObject) ctx);
			return answer.toString();
		}
		else if (typ.equals(NodeType.AnnotationProperty)) {
			addAnnotationPropertyDeclaration(resource, nn, answer);
			Object ctx = ((NamedNode)lastcmd).getContext();
			answerUser(modelFolder, answer.toString(), false, (EObject) ctx);
			return answer.toString();
		}
		else if (typ.equals(NodeType.InstanceNode)) {
			addInstanceDeclaration(resource, nn, answer);
			Object ctx = ((NamedNode)lastcmd).getContext();
			answerUser(modelFolder, answer.toString(), false, (EObject) ctx);
			return answer.toString();
		}
		else if (typ.equals(NodeType.FunctionNode)) {
			addInstanceDeclaration(resource, nn, answer);
			Object ctx = ((NamedNode)lastcmd).getContext();
			answerUser(modelFolder, answer.toString(), false, (EObject) ctx);
			return answer.toString();
		}
		else {
			answer.append("Type " + typ.getClass().getCanonicalName() + " not handled yet.");
			logger.debug(answer.toString());
			return answer.toString();
		}
	}

	private OwlToSadl getOwlToSadl(OntModel model) {
		if (owl2sadl  == null) {
			owl2sadl = new OwlToSadl(model);
		}
		return owl2sadl;
	}

	private void addInstanceDeclaration(org.eclipse.emf.ecore.resource.Resource resource, NamedNode nn, StringBuilder answer) throws ExecutionException, ConfigurationException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException {
		answer.append(checkForKeyword(nn.getName()));
//		String query = "select ?type where {<" + nn.getURI() + ">  <" + ReasonerVocabulary.directRDFType + "> ?type}";
		String query = "select ?type where {<" + nn.getURI() + ">  <rdf:type> ?type}";
		Query q = new Query();
		q.setSparqlQueryString(query);
		ResultSet rs = runQuery(resource, q);
		if (rs != null) {
			answer.append(" is a ");
			rs.setShowNamespaces(false);
			int rowcnt = rs.getRowCount();
			if (rowcnt > 1) {
				answer.append("{");
			}
				for (int r = 0; r < rowcnt; r++) {
					Object typ = rs.getResultAt(r, 0);
					if (r > 1) {
						answer.append(" and ");
					}
					answer.append(checkForKeyword(typ.toString()));
				}
			if (rowcnt > 1) {
				answer.append("}");
			}
		}
		query = "select ?p ?v where {<" + nn.getURI() + "> ?p ?v }";
		q = new Query();
		q.setSparqlQueryString(query);
		rs = runQuery(resource, q);
		if (rs != null) {
			rs.setShowNamespaces(true);
			int rowcnt = rs.getRowCount();
			for (int r = 0; r < rowcnt; r++) {
				Object pobjURI = rs.getResultAt(r, 0);
				if (pobjURI.toString().equals(RDFS.comment.getURI()) || pobjURI.toString().equals(RDFS.label.getURI())) {
					if (pobjURI.toString().equals(RDFS.comment.getURI())) {
						answer.append(" (note \"");
					}
					else {
						answer.append(" (alias \"");
					}
					answer.append(rs.getResultAt(r, 1).toString().replaceAll("\"", "'"));
					answer.append("\")");
				}
			}
			int outputcnt = 0;
			for (int r = 0; r < rowcnt; r++) {
				Object pobjURI = rs.getResultAt(r, 0);
				if (!pobjURI.toString().startsWith(OWL.getURI()) && !pobjURI.toString().startsWith(RDFS.getURI()) &&
						!pobjURI.toString().startsWith(RDF.getURI())) {
					Object vobjURI = rs.getResultAt(r, 1);
					if (isBlankNode(vobjURI.toString())) {
						answer = addBlankNodeObject(resource, answer, vobjURI.toString());
					}
					else {
						rs.setShowNamespaces(false);
						Object pobj = rs.getResultAt(r, 0);
						Object vobj = rs.getResultAt(r, 1);
						if (outputcnt++ > 0) {
							answer.append(", with ");
						}
						else {
							answer.append(" with ");
						}
						answer.append(checkForKeyword(pobj.toString()));
						answer.append(" ");
						if (vobjURI.toString().startsWith(XSD.getURI())) {
							answer.append(vobj.toString());
						}
						else {
							answer.append(checkForKeyword(vobj.toString()));			                						
						}
					}
				}
				
			}
		}
	}

	private ResultSet runQuery(org.eclipse.emf.ecore.resource.Resource resource, Query q) throws ConfigurationException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException {
		SadlCommandResult scr = getInferenceProcessor().processAdhocQuery(resource, q);
		if (scr != null) {
			Object rs = scr.getResults();
			if (rs instanceof ResultSet) {
				return (ResultSet)rs;
			}
			else if (rs == null) {
				throw new TranslationException("Query returned no results.");
			}
			else {
				throw new TranslationException("Unexpected query result type: " + rs.getClass().getCanonicalName());
			}
		}
		return null;
	}

	private ISadlInferenceProcessor getInferenceProcessor() {
		if (inferenceProcessor == null) {
			inferenceProcessor = new JenaBasedDialogInferenceProcessor();
		}
		return inferenceProcessor;
	}

	private ISadlInferenceProcessor getInferenceProcessor(OntModel theModel) {
		if (inferenceProcessor == null) {
			inferenceProcessor = new JenaBasedDialogInferenceProcessor(theModel);
		}
		return inferenceProcessor;
	}

	private StringBuilder addBlankNodeObject(org.eclipse.emf.ecore.resource.Resource resource, StringBuilder answer, String bNodeUri) throws ExecutionException, ConfigurationException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException {
		String query = "select ?t ?p ?v where {<" + bNodeUri + "> ?p ?v }";
		Query q = new Query();
		q.setSparqlQueryString(query);
		ResultSet rs = runQuery(resource, q);
		if (rs != null) {
			rs.setShowNamespaces(false);
			answer.append(rs.toStringWithIndent(5));
		}
		else {
			answer.append(bNodeUri);
		}
		return answer;
	}

	private boolean isBlankNode(String uri) {
		if (uri.startsWith("-")) {
			return true;
		}
		return false;
	}

	private void addAnnotationPropertyDeclaration(org.eclipse.emf.ecore.resource.Resource resource, NamedNode nn, StringBuilder answer) {
		answer.append(checkForKeyword(nn.getName()));
		answer.append(" is a type of annotation");
	}

	private void addPropertyWithDomainAndRange(org.eclipse.emf.ecore.resource.Resource resource, NamedNode nn, StringBuilder answer)
			throws ExecutionException, ConfigurationException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException {
		answer.append(checkForKeyword(nn.getName()));
		String query = "select ?d where {<" + nn.getURI() + "> <rdfs:domain> ?d}";
		Query q = new Query();
		q.setSparqlQueryString(query);
		ResultSet rs = runQuery(resource, q);
		boolean domainGiven = false;
		if (rs != null) {
			rs.setShowNamespaces(false);
			int rowcnt = rs.getRowCount();
			if (rowcnt > 0) {
				answer.append(" describes ");
				if (rowcnt > 1) answer.append("{");
				for (int r = 0; r < rowcnt; r++) {
					answer.append(checkForKeyword(rs.getResultAt(r, 0).toString()));
					domainGiven = true;
					if (rowcnt > 1 && r < rowcnt) answer.append(" and ");
				}
				if (rowcnt > 1) answer.append("}");
			}
		}

		if (!domainGiven) {
			answer.append(" is a property");
		}
		query = "select ?r where {<" + nn.getURI() + "> <rdfs:range> ?r}";
		q = new Query();
		q.setSparqlQueryString(query);
		rs = runQuery(resource, q);
		if (rs != null) {
			rs.setShowNamespaces(false);
			int rowcnt = rs.getRowCount();
			if (rowcnt > 0) {
				answer.append(" with values of type ");
				if (rowcnt > 1) answer.append("{");
				for (int r = 0; r < rowcnt; r++) {
					String val = rs.getResultAt(r, 0).toString();
					rs.setShowNamespaces(true);
					String pval = rs.getResultAt(r, 0).toString();
					rs.setShowNamespaces(false);
					if (pval.startsWith(XSD.getURI())) {
						answer.append(val);

					}
					else {
						answer.append(checkForKeyword(val));			                						
					}
					if (rowcnt > 1 && r < rowcnt) answer.append(" and ");
				}
				if (rowcnt > 1) answer.append("}");
			}
		}
	}

	private boolean addDomainAndRange(org.eclipse.emf.ecore.resource.Resource resource, NamedNode nn, boolean isFirstProperty,
			StringBuilder answer) throws ExecutionException {
		OntModel m = OntModelProvider.find(resource);
		OntClass cls = m.getOntClass(nn.getURI());
		return getDomainAndRangeOfClass(m, cls, answer);
	}

	private boolean getDomainAndRangeOfClass(OntModel m, OntClass cls, StringBuilder answer) {
		boolean isFirstProperty = true;
		StmtIterator sitr = m.listStatements(null, RDFS.domain, cls);
		while (sitr.hasNext()) {
			com.hp.hpl.jena.rdf.model.Resource p = sitr.nextStatement().getSubject();
			answer.append("\n      ");
			answer.append("described by ");
			if (p != null) {
				answer.append(checkForKeyword(p.isURIResource() ? p.getLocalName() : p.toString()));
				StmtIterator ritr = m.listStatements(p, RDFS.range, (RDFNode)null);
				while (ritr.hasNext()) {
					RDFNode r = ritr.nextStatement().getObject();
					if (r != null) {
						answer.append(" with values of type ");
						if (r.isURIResource()) {
							if (r.asResource().getNameSpace().equals(XSD.getURI())) {
								answer.append(r.asResource().getLocalName());										
							}
							else {
								answer.append(checkForKeyword(r.asResource().getLocalName()));
							}
						}
						else {
							answer.append(r.asLiteral().getValue().toString());
						}
					}
				}
			}
			if (sitr.hasNext()) {
				isFirstProperty = false;
			}
		}
		ExtendedIterator<OntClass> spritr = cls.listSuperClasses(true);
		while (spritr.hasNext()) {
			OntClass spcls = spritr.next();
			getDomainAndRangeOfClass(m, spcls, answer);
		}
//		answer.append("\n");
		return isFirstProperty;
	}

	private StringBuilder getClassHierarchy(org.eclipse.emf.ecore.resource.Resource resource, NamedNode nn, StringBuilder answer) throws ExecutionException, ConfigurationException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException {
		String query;
		Query q;
		ResultSet rs;
		query = "select ?typ where {<" + nn.getURI() + "> <rdfs:subClassOf> ?typ}";
		q = new Query();
		q.setSparqlQueryString(query);
		rs = runQuery(resource, q);
		if (rs != null) {
			rs.setShowNamespaces(false);
			answer.append(" is a type of ");
			if (rs.getRowCount() > 1) {
				answer.append("{");
			}
			for (int r = 0; r < rs.getRowCount(); r++) {
				Object rat = rs.getResultAt(r, 0);
				if (rat != null) {
					if (r > 0) {
						answer.append(" or ");
					}
					answer.append(rat.toString());
				}
			}
			if (rs.getRowCount() > 1) {
				answer.append("}");
			}
		}
		return answer;
	}

	private void addQualifiedCardinalityRestriction(org.eclipse.emf.ecore.resource.Resource resource, NamedNode nn, boolean isFirstProperty,
			StringBuilder answer) throws ExecutionException, ConfigurationException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException {
		String query;
		Query q;
		ResultSet rs;
		query = "select ?p ?cn ?rc where {<" + nn.getURI() + "> <rdfs:subClassOf> ?r . ?r <rdf:type> <owl:Restriction> . ?r <owl:onProperty> ?p . ";
		query += "?r <owl:qualifiedCardinality> ?cn . ?r <owl:onClass> ?rc}";
		q = new Query();
		q.setSparqlQueryString(query);
		rs = runQuery(resource, q);
		if (rs != null) {
			rs.setShowNamespaces(false);
			int rowcnt = rs.getRowCount();
			if (rowcnt > 0) {
				if (!isFirstProperty) {
					answer.append(",\n");
				}
				for (int r = 0; r < rowcnt; r++) {
					answer.append("      ");
					answer.append("described by ");
					answer.append(checkForKeyword(rs.getResultAt(r, 0).toString()));
					answer.append(" with exactly ");
					answer.append(rs.getResultAt(r, 1).toString());
					answer.append(" values of type ");
					String val = rs.getResultAt(r, 2).toString();
					rs.setShowNamespaces(true);
					String pval = rs.getResultAt(r, 1).toString();
					rs.setShowNamespaces(false);
					if (pval.startsWith(XSD.getURI())) {
						answer.append(val);
					}
					else {
						answer.append(checkForKeyword(val));			                						
					}
				}
			}
		}
	}
    
    private String checkForKeyword(String word) {
    	List<String> kwrds = getSadlKeywords();
    	if (kwrds != null && kwrds.contains(word)) {
    		return "^" + word;
    	}
    	return word;
    }
    
	private ISadlInferenceProcessor inferenceProcessor = null;
    
	public static List<String> getSadlKeywords() {
		return OwlToSadl.getSadlKeywords();
	}
	
	/**
	 * Are all elements of the array of type TripleElement?
	 * @param lastcmd
	 * @return
	 */
	private boolean allTripleElements(Object[] lastcmd) {
		for (int i = 0; i < lastcmd.length; i++) {
			Object el = lastcmd[i];
			if (el instanceof Junction) {
				if (!allTripleElements(((ProxyNode)((Junction)el).getLhs()).getProxyFor())) {
					return false;
				}
				if (!allTripleElements(((ProxyNode)((Junction)el).getRhs()).getProxyFor())) {
					return false;
				}
			}
			else if (!(el instanceof TripleElement)) {
				return false;
			}
		}
		return true;
	}

	private boolean allTripleElements(GraphPatternElement gpe) {
		if (gpe instanceof TripleElement) {
			return true;
		}
		return false;
	}

	private TripleElement[] flattenTriples(Object[] lastcmd) {
		List<TripleElement> triples = new ArrayList<TripleElement>();
		for (int i = 0; i < lastcmd.length; i++) {
			Object cmd = lastcmd[i];
			if (cmd instanceof TripleElement) {
				triples.add((TripleElement) cmd);
			}
			else if (cmd instanceof Junction) {
				triples.addAll(flattenJunction((Junction)cmd));
			}
		}
		return triples.toArray(new TripleElement[triples.size()]);
	}

	private Collection<? extends TripleElement> flattenJunction(Junction cmd) {
		List<TripleElement> triples = new ArrayList<TripleElement>();
		if (cmd.getJunctionType().equals(JunctionType.Conj)) {
			GraphPatternElement lhs = ((ProxyNode) cmd.getLhs()).getProxyFor();
			if (lhs instanceof TripleElement) {
				triples.add((TripleElement) lhs);
			}
			else if (lhs instanceof Junction) {
				triples.addAll(flattenJunction((Junction) lhs));
			}
			else {
				System.err.println("Encountered unsupported type flattening Junction: " + lhs.getClass().getCanonicalName());
			}
			GraphPatternElement rhs = ((ProxyNode) cmd.getRhs()).getProxyFor();
			if (rhs instanceof TripleElement) {
				triples.add((TripleElement) rhs);
			}
			else if (rhs instanceof Junction) {
				triples.addAll(flattenJunction((Junction) rhs));
			}
			else {
				System.err.println("Encountered unsupported type flattening Junction: " + rhs.getClass().getCanonicalName());
			}
		}
		else {
			System.err.println("Encountered disjunctive type flattening Junction");
		}
		return triples;
	}

	private String resultSetToQuotableString(ResultSet rs) {
		String resultStr;
		rs.setShowNamespaces(true);
		resultStr = rs.toString();
		return resultSetToQuotableString(resultStr);
	}

	private String resultSetToQuotableString(String resultStr) {
		resultStr = resultStr.replace('"', '\'');
		resultStr = resultStr.trim();
		resultStr = "\"" + resultStr + "\"";
		return resultStr;
	}

	private Object[] insertTriplesAndQuery(org.eclipse.emf.ecore.resource.Resource resource, TripleElement[] triples) throws ExecutionException, SadlInferenceException {
		return getInferenceProcessor().insertTriplesAndQuery(resource, triples);
	}

	private List<TripleElement> addTriplesFromJunction(Junction jct, List<TripleElement> tripleLst) {
		Object lhs = jct.getLhs();
		if (lhs instanceof ProxyNode) {
			if (((ProxyNode)lhs).getProxyFor() instanceof TripleElement) {
				tripleLst.add((TripleElement) ((ProxyNode)lhs).getProxyFor());
			}
			else if (((ProxyNode)lhs).getProxyFor() instanceof Junction) {
				tripleLst = addTriplesFromJunction((Junction) ((ProxyNode)lhs).getProxyFor(), tripleLst);
			}
		}
		Object rhs = jct.getRhs();
		if (rhs instanceof ProxyNode) {
			if (((ProxyNode)rhs).getProxyFor() instanceof TripleElement) {
				tripleLst.add((TripleElement) ((ProxyNode)rhs).getProxyFor());
			}
			else if (((ProxyNode)rhs).getProxyFor() instanceof Junction) {
				tripleLst = addTriplesFromJunction((Junction) ((ProxyNode)rhs).getProxyFor(), tripleLst);
			}
		}
		return tripleLst;
	}

	private void addTripleQuestion(org.eclipse.emf.ecore.resource.Resource resource, TripleElement tr, StringBuilder answer) throws ExecutionException, ConfigurationException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException {
		List<String> vars = new ArrayList<String>();
		StringBuilder sbwhere = new StringBuilder("where {");
		if (tr.getSubject() == null) {
			vars.add("?s");
			sbwhere.append("?s ");
		}
		else {
			sbwhere.append("<");
			sbwhere.append(tr.getSubject().getURI());
			sbwhere.append("> ");
		}
		if (tr.getPredicate() == null) {
			vars.add("?p");
			sbwhere.append("?p ");
		}
		else {
			sbwhere.append("<");
			sbwhere.append(tr.getPredicate().getURI());
			sbwhere.append("> ");
		}
		if (tr.getObject() == null) {
			vars.add("?o");
			sbwhere.append("?o");
		}
		else {
			sbwhere.append("<");
			Node obj = tr.getObject();
			if (obj instanceof NamedNode) {
				sbwhere.append(obj.getURI());
			}
			else {
				sbwhere.append(obj.toString());
			}
			sbwhere.append("> ");
		}
		if (vars.size() > 0) {
			for (int i = vars.size() - 1; i >= 0; i--) {
				sbwhere.insert(0, " ");
				sbwhere.insert(0, vars.get(i));
			}
			sbwhere.insert(0,  "select ");
			sbwhere.append("}");
		}
		Query q = new Query();
		q.setSparqlQueryString(sbwhere.toString());
		ResultSet rs = runQuery(resource, q);
		if (rs != null) {
			if (rs.getRowCount() == 1) {
				if (tr.getSubject() != null && tr.getPredicate() != null) {
					answer.append(tr.getSubject().getName());
					answer.append(" has ");
					answer.append(tr.getPredicate().getName());
					answer.append(" ");
					answer.append(rs.getResultAt(0, 0));
				}
				else {
					answer.append("\"");
					answer.append(resultSetToQuotableString(rs));
					answer.append("\"");
				}
			}
			else {
				answer.append("\"");
				answer.append(resultSetToQuotableString(rs));
				answer.append("\"");
			}
		}
	}

	public void setUserName(String answer) {
		// TODO Auto-generated method stub
		
	}

	public String getUserName() {
		return userName;
	}

	/**
	 * Method to process a conversation contained in a DialogContent and answer any unanswered question, etc.
	 * @param resource 
	 * @param ontModel 
	 * @param modelName 
	 */
	public void processConversation(org.eclipse.emf.ecore.resource.Resource resource, OntModel ontModel, String modelName) {
		// ConversationElements are processed in order (must process a save before an evaluate, for example),
		//	but the actual insertion of new responses into the Dialog occurs in reverse order so that the locations
		//	are less complicated to determine
		DialogContent dc = getConversation();
		List<ConversationElement> dialogStmts = dc.getStatements();
		Map<ConversationElement, ConversationElement> additionMap = new HashMap<ConversationElement, ConversationElement>();  // new CE, CE before
		List<ConversationElement> additions = new ArrayList<ConversationElement>();
		for (ConversationElement ce : dialogStmts) {
			int idx = dialogStmts.indexOf(ce);
			StatementContent statementAfter = idx < dialogStmts.size() ? dialogStmts.get(idx + 1).getStatement() : null;
			StatementContent sc = ce.getStatement();
			if (sc instanceof ExpectsAnswerContent) {
				String question = removeLeadingComments(sc.getText().trim());
				if (getQuestionsAndAnswers().containsKey(question)) {
					String ans = getQuestionsAndAnswers().get(question);
//					System.out.println(ans + " ? " + ((AnswerContent)statementAfter).getAnswer().toString().trim());
					if (statementAfter != null && statementAfter instanceof AnswerContent && 
							((AnswerContent)statementAfter).getAnswer().toString().trim().equals(ans)) {
						// this statement has already been answered		
						((ExpectsAnswerContent) sc).setAnswer((AnswerContent) statementAfter);
						continue;
					}
				}

				// this statement needs an answer
				if (sc instanceof ExpectsAnswerContent) {
					try {
						String answer = processUserRequest(resource, ontModel, modelName, (ExpectsAnswerContent)sc);
						if (answer != null) {
							addQuestionAndAnswer(sc.getText().trim(), answer.trim());
							AnswerPendingContent pending = new AnswerPendingContent(null, Agent.CM, (ExpectsAnswerContent) sc);
							ConversationElement cep = new ConversationElement(dc, pending, Agent.CM);
							additions.add(cep);
							additionMap.put(cep, ce);
							((ExpectsAnswerContent)sc).setAnswer(pending);
						}
					} catch (ConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ExecutionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TranslationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvalidNameException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ReasonerNotFoundException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (QueryParseException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (QueryCancelledException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (SadlInferenceException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}
		if (!additions.isEmpty()) {
			for (int i = additions.size() - 1; i >= 0; i--) {
				ConversationElement newCE = additions.get(i);
				ConversationElement preceeding = additionMap.get(newCE);
				
			}
		}
	}

	private Map<String, String> getQuestionsAndAnswers() {
		return questionsAndAnswers;
	}

	private boolean addQuestionAndAnswer(String question, String answer) {
		String modQuestion = removeLeadingComments(question);
		if (questionsAndAnswers.containsKey(modQuestion)) {
			String oldAnswer = questionsAndAnswers.get(modQuestion);
			if (oldAnswer.equals(answer)) {
				return false;
			}
		}
		questionsAndAnswers.put(modQuestion, answer);
		return true;
	}

	private String removeLeadingComments(String question) {
		String lines[] = question.split("\\r?\\n");
		String lastLine = null;
		for (String line : lines) {
			if (!line.trim().startsWith("//") && line.trim().length() > 0) {
				lastLine = line;
				break;
			}
		}
		if (lastLine != null) {
			int loc = question.indexOf(lastLine);
			question = question.substring(loc).trim();
		}
		return question;
	}

	public void clearQuestionsAndAnsers() {
		questionsAndAnswers.clear();
	}

}

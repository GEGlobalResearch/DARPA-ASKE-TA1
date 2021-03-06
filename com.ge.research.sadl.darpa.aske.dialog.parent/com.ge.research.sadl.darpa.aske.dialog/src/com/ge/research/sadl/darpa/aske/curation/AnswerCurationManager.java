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
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.ConnectException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.internal.localstore.IsSynchronizedVisitor;
import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.jdt.core.compiler.InvalidInputException;
import org.eclipse.xtext.resource.XtextResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.dialog.AnswerCMStatement;
import com.ge.research.sadl.darpa.aske.inference.JenaBasedDialogInferenceProcessor;
import com.ge.research.sadl.darpa.aske.inference.NoModelFoundForTargetException;
import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.darpa.aske.processing.AddAugmentedTypeInfoContent;
import com.ge.research.sadl.darpa.aske.processing.AddEquationContent;
import com.ge.research.sadl.darpa.aske.processing.AnswerContent;
import com.ge.research.sadl.darpa.aske.processing.AnswerPendingContent;
import com.ge.research.sadl.darpa.aske.processing.CompareContent;
import com.ge.research.sadl.darpa.aske.processing.ConversationElement;
import com.ge.research.sadl.darpa.aske.processing.ConversationException;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.DialogContent;
import com.ge.research.sadl.darpa.aske.processing.EquationStatementContent;
import com.ge.research.sadl.darpa.aske.processing.EvalContent;
import com.ge.research.sadl.darpa.aske.processing.EvalContent.UnittedParameter;
import com.ge.research.sadl.darpa.aske.processing.ExpectsAnswerContent;
import com.ge.research.sadl.darpa.aske.processing.ExtractContent;
import com.ge.research.sadl.darpa.aske.processing.HowManyValuesContent;
import com.ge.research.sadl.darpa.aske.processing.IDialogAnswerProvider;
import com.ge.research.sadl.darpa.aske.processing.InformationContent;
import com.ge.research.sadl.darpa.aske.processing.LongTaskContent;
import com.ge.research.sadl.darpa.aske.processing.ModifiedAskContent;
import com.ge.research.sadl.darpa.aske.processing.NoModelFoundStatementContent;
import com.ge.research.sadl.darpa.aske.processing.QuestionContent;
import com.ge.research.sadl.darpa.aske.processing.QuestionWithCallbackContent;
import com.ge.research.sadl.darpa.aske.processing.RequestArgumentAugmentedTypeContent;
import com.ge.research.sadl.darpa.aske.processing.RequestReturnAugmentedTypeContent;
import com.ge.research.sadl.darpa.aske.processing.SadlStatementContent;
import com.ge.research.sadl.darpa.aske.processing.SaveContent;
import com.ge.research.sadl.darpa.aske.processing.StatementContent;
import com.ge.research.sadl.darpa.aske.processing.UndefinedConceptStatementContent;
import com.ge.research.sadl.darpa.aske.processing.WhatIsContent;
import com.ge.research.sadl.darpa.aske.processing.WhatValuesContent;
import com.ge.research.sadl.darpa.aske.processing.imports.AnswerExtractionException;
import com.ge.research.sadl.darpa.aske.processing.imports.AnswerExtractionProcessor;
import com.ge.research.sadl.darpa.aske.processing.imports.IModelFromCodeExtractor;
import com.ge.research.sadl.darpa.aske.processing.imports.JavaModelExtractorJP;
import com.ge.research.sadl.darpa.aske.processing.imports.KChainServiceInterface;
import com.ge.research.sadl.darpa.aske.processing.imports.TextProcessingServiceInterface.EquationVariableContextResponse;
import com.ge.research.sadl.darpa.aske.processing.imports.TextProcessor;
import com.ge.research.sadl.external.NetworkProxyConfigurator;
import com.ge.research.sadl.external.XMLHelper;
import com.ge.research.sadl.jena.JenaBasedSadlModelProcessor;
import com.ge.research.sadl.jena.JenaProcessorException;
import com.ge.research.sadl.jena.inference.SadlJenaModelGetterPutter;
import com.ge.research.sadl.model.gp.GraphPatternElement;
import com.ge.research.sadl.model.gp.Junction;
import com.ge.research.sadl.model.gp.Junction.JunctionType;
import com.ge.research.sadl.model.gp.NamedNode;
import com.ge.research.sadl.model.gp.NamedNode.NodeType;
import com.ge.research.sadl.model.gp.Node;
import com.ge.research.sadl.model.gp.ProxyNode;
import com.ge.research.sadl.model.gp.Query;
import com.ge.research.sadl.model.gp.RDFTypeNode;
import com.ge.research.sadl.model.gp.Rule;
import com.ge.research.sadl.model.gp.TripleElement;
import com.ge.research.sadl.model.gp.VariableNode;
import com.ge.research.sadl.model.visualizer.GraphVizVisualizer;
import com.ge.research.sadl.model.visualizer.IGraphVisualizer;
import com.ge.research.sadl.owl2sadl.OwlImportException;
import com.ge.research.sadl.owl2sadl.OwlToSadl;
import com.ge.research.sadl.processing.IModelProcessor;
import com.ge.research.sadl.processing.ISadlInferenceProcessor;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.processing.SadlConstants;
import com.ge.research.sadl.processing.SadlInferenceException;
import com.ge.research.sadl.reasoner.AmbiguousNameException;
import com.ge.research.sadl.reasoner.CircularDependencyException;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.ConfigurationItem;
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
import com.ge.research.sadl.sADL.SadlModel;
import com.ge.research.sadl.utils.ResourceManager;
import com.google.common.base.Optional;
import com.google.common.io.Files;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.ontology.Restriction;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.NodeIterator;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.OWL;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

//import net.htmlparser.jericho.Renderer;
//import net.htmlparser.jericho.Source;

//import net.htmlparser.jericho.Source;

public class AnswerCurationManager {
	
	private static final String SUSPENDED_EXTRACTION_PENDING_DOMAIN_MODEL_ADDITIONS = "suspended extraction pending domain model additions";
	private static final String MATCHINGCLASS_PROPERTY_URI = SadlConstants.SADL_IMPLICIT_MODEL_URI + "#matchingClass";
	private static final String MATCHINGPROPERTY_PROPERTY_URI = SadlConstants.SADL_IMPLICIT_MODEL_URI + "#matchingProperty";;
	protected static final Logger logger = LoggerFactory.getLogger(AnswerCurationManager.class);
	private XtextResource resource = null;
	private URI resourceUri = null;
	private String owlModelsFolder;
	
	private Map<String, String> questionsAndAnswers = new HashMap<String, String>();	// question text is the key, answer text is the value
	private Map<String, QuestionWithCallbackContent> unansweredQuestions = new HashMap<String, QuestionWithCallbackContent>();
	private Map<String, String> augmentedTypeQuestionsAsked = new HashMap<String, String>();
	
	private IConfigurationManagerForIDE configurationManager;
	private Map<String, String> preferences = null;
	private AnswerExtractionProcessor extractionProcessor = null;

	private IDialogAnswerProvider dialogAnswerProvider = null;	// The instance of an implementer of a DialogAnswerProvider

	private IReasoner codeModelReasoner;
	private IReasoner textModelReasoner;

	public enum SaveAsSadl{SaveAsSadl, DoNotSaveAsSadl, AskUserSaveAsSadl}

	public enum Agent {USER, CM}
	private String userName;
	private DialogContent conversation = null; 
	private DialogContent lastConversation = null;
	private Map<String, String> equationCompleteness = null;	// holds text of complete equation on previous call,
																// in order to detect changes to complete equations
	private Map<String, String[]> targetModelMap = null;
	private OwlToSadl owl2sadl = null;
	
	private List<String> delayedImportAdditions = null;
	private Boolean useKCHAIN = null;

	private ISadlInferenceProcessor inferenceProcessor = null;
//	private Map<String, OntModel> extractionModelMap = new HashMap<String, OntModel>();
//	private Map<String, String> fileLocalityMap = new HashMap<String, String>();
	
	private OntModel domainModel = null;
	private String domainModelName = null;
	private OntModel simplifiedDomainModelForTextService = null;
	private String localityURI = null;
	private String localityPrefix = null;
	
	private boolean lookForSubclassInArticledClasses = true;	// should a domain of a property be replaced with a subclass
																// that has appeared in other arguments augmented types
	private Map<String, List<String>> unmatchedUrisAndLabels = null;	// Map of concept URIs and labels that were not matched in the domain ontology
														
	private List<String> addedTypeDeclarations = new ArrayList<String>();
	private Map<String,List<String>> equationInformation = null;
	private Map<String, String> cachedEquationVariableContext = new HashMap<String, String>();
	private Map<Individual, String> variableDeclarations = new HashMap<Individual, String>();
	
	private StatementContent extractionContext = null;
	private List<String> classesDeclared = new ArrayList<String>();
	private List<StatementContent> failureCorrectingActions = new ArrayList<StatementContent>();
	private StatementContent lastStatement = null;
	
	// Concepts from the latest extraction from text:
	//	0: Dialog window statements from extraction,
	//	1: labels, 
	//	2: seeAlsos, 
	//	3: matchingClasses, 
	//	4: matchingProperties
	private List<Object> currentTextExtractionExtracts = null;
	private boolean suspendConversationProcessing = false;
	private String suspendedExtractionContent = null;
	private OntModel suspendedCodeModel = null;
	private HashMap<String, List<String>> cachedAugmentedTypesOfCodeVariable = null;
    
	public AnswerCurationManager (String modelFolder, IConfigurationManagerForIDE configMgr, XtextResource resource, Map<String,String> prefs) {
		setOwlModelsFolder(modelFolder);
		setConfigurationManager(configMgr);
		setPreferences(prefs);
		setResource(resource);
		setResourceUri(resource != null ? resource.getURI() : null);
		loadQuestionsAndAnswersFromFile();
	}

	public String getOwlModelsFolder() {
		return owlModelsFolder;
	}

	public void setOwlModelsFolder(String owlModelsFolder) {
		this.owlModelsFolder = owlModelsFolder;
	}

	public IConfigurationManagerForIDE getConfigurationManager() {
		return configurationManager;
	}

	private void setConfigurationManager(IConfigurationManagerForIDE projectConfigurationManager) {
		this.configurationManager = projectConfigurationManager;
	}

	private Map<String, String> getPreferences() {
		return preferences;
	}

	private void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}
	
	/**
	 * Method to get the preference identified by key
	 * @param key
	 * @return
	 */
	public String getPreference(String key) {
		if (preferences != null) {
			return preferences.get(key);
		}
		return null;
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
	 * @return number of files successfully extracted
	 * @throws IOException
	 * @throws ConfigurationException 
	 * @throws QueryCancelledException 
	 * @throws QueryParseException 
	 * @throws ReasonerNotFoundException 
	 * @throws InvalidNameException 
	 * @throws AnswerExtractionException 
	 * @throws InvalidInputException 
	 * @throws AmbiguousNameException 
	 */
	public int processImports(SaveAsSadl saveAsSadl) throws IOException, ConfigurationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException, AnswerExtractionException, InvalidInputException, AmbiguousNameException {
		int numSuccessfullyProcessed = 0;
		addedTypeDeclarations.clear();
		Map<File, Boolean> outputOwlFilesBySourceType = new HashMap<File, Boolean>();	// Map of imports, value is true if code, false if text extraction
//		List<File> outputOwlFiles = new ArrayList<File>();
		List<File> textFiles = getExtractionProcessor().getTextProcessor().getTextFiles();
		if (textFiles != null) {
			for (File f : textFiles) {
				getExtractionProcessor().reset();
				String outputOwlFileName =  getOutputFilenameFromIputFile(f);
				String outputModelName = getModelNameFromInputFile(f);
				String prefix = getModelPrefixFromInputFile(f);

				String content = readFileToString(f);
				String fPath = f.getCanonicalPath();
				String fileIdentifier = ConfigurationManagerForIdeFactory.formatPathRemoveBackslashes(fPath);
				
				File of = extractFromTextAndSave(content, fileIdentifier, outputModelName, outputOwlFileName);
				if (of != null) {
					numSuccessfullyProcessed++;
					outputOwlFilesBySourceType.put(of, false);
					outputOwlFileName = of.getCanonicalPath();
					processExtractedText(outputModelName, outputOwlFileName, saveAsSadl);
				}
			}
		}
		
		List<File> codeFiles = getExtractionProcessor().getCodeExtractor().getCodeFiles();
		if (codeFiles != null) {
			boolean useAllCodeExtractedMethods = true;
			for (File f : codeFiles) {
				// reset code extractor and text processor from any previous file
				getExtractionProcessor().reset();
				String outputOwlFileName =  getOutputFilenameFromIputFile(f);
				String outputModelName = getModelNameFromInputFile(f);
				String prefix = getModelPrefixFromInputFile(f);
				
				String content = readFileToString(f);
				String fileIdentifier = ConfigurationManagerForIdeFactory.formatPathRemoveBackslashes(f.getCanonicalPath());
				
				File of = extractFromCodeAndSave(content, fileIdentifier, outputModelName, prefix, outputOwlFileName);
				if (of != null) {
					numSuccessfullyProcessed++;
					outputOwlFilesBySourceType.put(of, true);
					outputOwlFileName = of.getCanonicalPath();
					// run inference on the model, interact with user to refine results
					String queryString = useAllCodeExtractedMethods ? SparqlQueries.ALL_CODE_EXTRACTED_METHODS : SparqlQueries.INTERESTING_METHODS_DOING_COMPUTATION;	//?m ?b ?e ?s
					ResultSet results = runInferenceFindInterestingCodeModelResults(queryString, saveAsSadl, content);
					if (results == null || results.getRowCount() == 0) {
						notifyUser(getOwlModelsFolder(), "No equations were found in this extraction from code.", true);
					}
					else {
						equationsFromCodeResultSetToSadlContent(results, getOwlModelsFolder(), content);
					}
				}
			}			
		}
		if (saveAsSadl != null) {
			if (saveAsSadl.equals(SaveAsSadl.AskUserSaveAsSadl)) {
				// ask user if they want a SADL file saved
				IDialogAnswerProvider dap = getDialogAnswerProvider(getResourceUri());
//				if (dap == null) {
//					dap = new DialogAnswerProviderConsoleForTest();
//				}
				List<Object> args = new ArrayList<Object>();
				args.add(outputOwlFilesBySourceType);
				// the strings must be unique to the question or they will get used as answers to subsequent questions with the same string
				if (outputOwlFilesBySourceType.size() > 0) {
					StringBuilder sb = new StringBuilder();
					Iterator<File> ofitr = outputOwlFilesBySourceType.keySet().iterator();
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
				saveAsSadlFile(outputOwlFilesBySourceType, "yes");
			}
		}
		return numSuccessfullyProcessed;
	}

	private String processExtractedText(String outputModelName, String outputOwlFileName, SaveAsSadl saveAsSadl)
			throws ConfigurationException, IOException, ReasonerNotFoundException, InvalidNameException,
			QueryParseException, QueryCancelledException, AmbiguousNameException {
		// run inference on the model, interact with user to refine results
		String textModelFolder = getOwlModelsFolder();		// same as code model folder, at least for now
		String queryString = SparqlQueries.All_TEXT_EXTRACTED_METHODS;	 // SparqlQueries.ALL_EXTERNAL_EQUATIONS;
		ResultSet results = runInferenceFindInterestingTextModelResults(outputOwlFileName, queryString, saveAsSadl, outputModelName);
		if (results == null || results.getRowCount() == 0) {
			String msg = "No equations were found in this extraction from text.";
			notifyUser(textModelFolder, msg, true);
			return msg;
		}
		else {
			return equationsFromTextResultSetToSadlContent(results, textModelFolder, outputModelName);
		}
	}

	private File extractFromCodeAndSave(String content, String fileIdentifier, String outputModelName, String prefix,
			String outputOwlFileName) throws ConfigurationException, IOException {
		if (getCodeExtractor().process(fileIdentifier, content, outputModelName, prefix)) {			
			File of = saveCodeOwlFile(outputOwlFileName);
			return of;
		}
		return null;
	}

	private File extractFromTextAndSave(String content, String inputIdentifier, String extractedTxtModelName, String outputOwlFileName)
			throws IOException, ConfigurationException, AnswerExtractionException {
		// clear any existing localityURI graph before processing text.
// TODO this should eventually be a user choice?				
		String clearMsg = getExtractionProcessor().getTextProcessor().clearGraph(getLocalityURI());
		
		if (getDomainModelName() == null || getDomainModel() == null) {
			Object dap = getConfigurationManager().getPrivateKeyMapValueByResource(DialogConstants.DIALOG_ANSWER_PROVIDER, getResourceUri());
			if (dap instanceof IDialogAnswerProvider) {
				String domainModelName = OntModelProvider.getModelName(((IDialogAnswerProvider)dap).getResource());
				setDomainModelName(domainModelName);
				OntModel domainModel = OntModelProvider.find(((IDialogAnswerProvider)dap).getResource());
				setDomainModel(domainModel);
				if (verboseExtraction()) {
					System.out.println("Domain model:");
					domainModel.write(System.out, "N3");
				}
			}
		}
		
		OntModel txtModel = getOntModelFromText(inputIdentifier, content, getLocalityURI(), extractedTxtModelName, getLocalityPrefix(), false, false);
		File of = saveTextOwlFile(extractedTxtModelName, outputOwlFileName);
		if (txtModel != null) {
			List<Object> extracts = retrieveExtractedConcepts(txtModel, true, false);
			StringBuilder sb = new StringBuilder();
			if (extracts != null) {
				if (extracts.size() == 5) {
					Object statements = extracts.get(0);
					if (statements instanceof List<?>) {
						for (Object ex : (List<?>)statements) {
							notifyUser(getOwlModelsFolder(), ex.toString(), false);
							if (sb.length() > 0) {
								sb.append(System.lineSeparator());
							}
							sb.append(ex);							
						}
					}
				}
				setCurrentTextExtractionExtracts(extracts);
			}
		}
		return of;
	}

	/**
	 * Method to convert the domain ontology to a string
	 * @return
	 */
	public String getTextExtractionDomainModel() {
		OntModel domainModel = getDomainModel();
		return ontModelToString(domainModel);
	}

	/**
	 * Method to serialize the input OntModel as a string
	 * @param domainModel
	 * @return
	 */
	public String ontModelToString(OntModel domainModel) {
		try {
			Writer writer = new StringWriter();
			domainModel.write(writer, "RDF/XML");
//			Optional<String> xmlbaseuri = new XMLHelper().tryReadBaseUri(writer.toString());
//			if (xmlbaseuri.isPresent()) {
//				uri = xmlbaseuri.get();
//			}
			return writer.toString();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Method to generate a unique model name for an import from the input File to the import
	 * @param outputFilename
	 * @return
	 */
	public String getModelNameFromInputFile(File inputFile) {
		String name = "http://com.ge.research.sadl.darpa.aske.answer/" + getModelPrefixFromInputFile(inputFile);
		return name;
	}
	
	/**
	 * Method to generate the output OWL filename for an import from the input File to the import
	 * @param outputFilename
	 * @return
	 */
	public static String getOutputFilenameFromIputFile(File inputFile) {
		String inputFilename = inputFile.getName();
		String outputOwlFileName = inputFilename + ".owl";
//		if (inputFilename.endsWith(".sadl")) {
//			inputFilename = inputFilename.substring(0, inputFilename.length() - 5) + ".owl";
//		}
		return outputOwlFileName;
	}
	
	/**
	 * Method to generate a unique model prefix from the input File to the import
	 * @param inputFile
	 * @return
	 */
	public String getModelPrefixFromInputFile(File inputFile) {
		String prefix;
		String inputFilename = inputFile.getName();
		if (inputFilename.endsWith(".owl")) {
			prefix = inputFilename.substring(0, inputFilename.length() - 4);
		}
		else {
			prefix = inputFilename.replaceAll("\\.", "_");
		}
		return prefix;
	}

	private File saveTextOwlFile(String extractedTxtModelName, String outputFilename) throws ConfigurationException, IOException {
		File of = new File(new File(getOwlModelsFolder()).getParent() + 
				"/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT + "/" + outputFilename);
		of.getParentFile().mkdirs();
		getExtractionProcessor().getTextProcessor().getTextModelConfigMgr().saveOwlFile(getExtractionProcessor().getTextModel(), extractedTxtModelName, of.getCanonicalPath());
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
		File of = new File(new File(getOwlModelsFolder()).getParent() + 
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

	private ResultSet runInferenceFindInterestingTextModelResults(String outputOwlFileName, String queryString, SaveAsSadl saveAsSadl, String locality) throws ConfigurationException, IOException, ReasonerNotFoundException, InvalidNameException, QueryParseException, QueryCancelledException, AmbiguousNameException {
		ResultSet results = null;
		// clear reasoner from any previous model
		clearTextModelReasoner();
		String textModelFolder = getOwlModelsFolder();		// same as code model folder, at least for now
		if (getInitializedTextModelReasoner() == null) {
			// use domain model folder because that's the project we're working in
			notifyUser(textModelFolder, "Unable to instantiate reasoner to analyze extracted code model.", true);
		}
		
		else {
			// initialize the unmatched URIs and labels memory for the set of equations
			//	(another import could duplicate an unmatched URI, so there could be duplicate statements. It should not cause errors.
			//	 To avoid, one could check the remembered conversation to see if such a statement already exists.)
			if (getUnmatchedUrisAndLabels() != null) {
				getUnmatchedUrisAndLabels().clear();
			}		
			queryString = getInitializedTextModelReasoner().prepareQuery(queryString);
			results =  getInitializedTextModelReasoner().ask(queryString);
		}
		return results;
	}
	
	private void equationsFromCodeResultSetToSadlContent(ResultSet results, String codeModelFolder, String fileContent) throws ConfigurationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException, InvalidInputException, IOException {
//		results.setShowNamespaces(false);
//		System.out.println(results.toString());
		if (results != null && results.getRowCount() > 0) {
			List<String> initializerKeywords = getInitializerKeywords();
			results.setShowNamespaces(false);
			String[] cns = ((ResultSet) results).getColumnNames();
			if (cns[0].equals("m") && cns[1].equals("b") && cns[2].equals("e") && cns[3].equals("s")) {
				String msg = "The following methods were found in the extraction:";
				notifyUser(codeModelFolder, msg, true);
				for (int r = 0; r < results.getRowCount(); r++) {
					String methodName = results.getResultAt(r, 0).toString();
					results.setShowNamespaces(true);
					String methodUri = results.getResultAt(r, 0).toString();
					results.setShowNamespaces(false);
					Object script = results.getResultAt(r, 3);
					String javaCode = script != null ? script.toString() : null;
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
										sb.append(System.lineSeparator());
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
						try {
							List<String> sadlDeclaration = convertCodeExtractedMethodToExternalEquationInSadlSyntax(initializerKeywords, methodUri, methodName, "Java", javaCode);
							for (String sd : sadlDeclaration) {
//									logger.debug(sadlDeclaration);
//									logger.debug("SADL equation:");
//									logger.debug(sadlDeclaration);
								EObject host = getExtractionContext() != null ? getExtractionContext().getHostEObject() : null;
								SadlStatementContent ssc = new SadlStatementContent(host, Agent.CM, sd);
								notifyUser(codeModelFolder, ssc, false);
								getExtractionProcessor().addNewSadlContent(sd);
							}
						} catch (AnswerExtractionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (AmbiguousNameException e) {
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
//		String importinfo = "To import this model for exploration in this window, add an import at the top of this window (after the 'uri' statement) for URI:\n   " + 
//				getExtractionProcessor().getCodeModelName() + "\n.";
//		notifyUser(codeModelFolder, importinfo);
	}

	private List<String> getInitializerKeywords() {
		List<String> keywords = null;
		OntClass im = getExtractionProcessor().getCodeModel().getOntClass(DialogConstants.SADL_IMPLICIT_MODEL_INITIALZERMETHOD_CLASS_URI);
		if (im != null) {
			NodeIterator nitr = im.listPropertyValues(getExtractionProcessor().getCodeModel().getProperty(DialogConstants.SADL_IMPLICIT_MODEL_INITIALIZERKEYWORD_PROPERTY_URI));
			if (nitr.hasNext()) {
				keywords = new ArrayList<String>();
				while (nitr.hasNext()) {
					RDFNode val = nitr.next();
					keywords.add(val.isLiteral() ? ((Literal)val).getValue().toString() : val.asResource().toString());
				}
			}
		}
		return keywords;
	}

	private String equationsFromTextResultSetToSadlContent(ResultSet results, String textModelFolder, String locality)
			throws ConfigurationException, InvalidNameException, ReasonerNotFoundException, QueryParseException,
			QueryCancelledException, IOException {
		StringBuilder sbResults = new StringBuilder("Equations: ");
		results.setShowNamespaces(false);
		String[] cns = ((ResultSet) results).getColumnNames();
		if (cns[0].equals("m") && cns[2].equals("ts") && cns[3].equals("ps") && cns[4].equals("pstf") && cns[5].equals("psnp")) {
			notifyUser(textModelFolder, "The following equations were found in the text:", true);
			List<String> unmatchedStatementsAlreadyProcessed = null;
			for (int r = 0; r < results.getRowCount(); r++) {
				String methodName = results.getResultAt(r, 0).toString();
				String derivedFromMethodName = results.getResultAt(r, 1) != null ? results.getResultAt(r, 1).toString() : null;
				String txtscript = results.getResultAt(r, 2) != null ? results.getResultAt(r, 2).toString() : null;
				String pyscript = results.getResultAt(r, 3) != null ? results.getResultAt(r, 3).toString() : null;
				String pytfscript = results.getResultAt(r, 4) != null ? results.getResultAt(r, 4).toString() : null;
				String pynpscript = results.getResultAt(r, 5) != null ? results.getResultAt(r, 5).toString() : null;
				if (methodName != null) {	//&& txtscript != null && pyscriptToUse != null
					try {
						Map<String,String> scriptMap = new HashMap<String,String>();
						if (txtscript != null) {
							scriptMap.put(SadlConstants.SADL_IMPLICIT_MODEL_TEXT_LANGUAGE_INST_URI, txtscript);
						}
						if (pyscript != null) {
							scriptMap.put(DialogConstants.PYTHON_LANGUAGE, pyscript);
						}
						if (pytfscript != null) {
							scriptMap.put(DialogConstants.TF_PYTHON_LANGUAGE, pytfscript);
						}
						if (pynpscript != null) {
							scriptMap.put(DialogConstants.NUMPY_PYTHON_LANGUAGE, pynpscript);
						}
						List<String> sadlDeclaration = convertTextExtractedMethodToExternalEquationInSadlSyntax(methodName, derivedFromMethodName, scriptMap, locality);
						List<String> unmatchedConceptStatements = getUnmatchedConceptDeclarationStatements();
						if (unmatchedConceptStatements != null) {
							if (unmatchedStatementsAlreadyProcessed == null) unmatchedStatementsAlreadyProcessed = new ArrayList<String>();
							for (String sd : unmatchedConceptStatements) {
								if (!unmatchedStatementsAlreadyProcessed.contains(sd)) {
									SadlStatementContent ssc = new SadlStatementContent(getExtractionContext().getHostEObject(), Agent.CM, sd);
									notifyUser(textModelFolder, ssc, false);
									getExtractionProcessor().addNewSadlContent(sd);
									unmatchedStatementsAlreadyProcessed.add(sd);
								}
							}
						}
						for (String sd : sadlDeclaration) {
							EObject host = getExtractionContext() != null ? getExtractionContext().getHostEObject() : null;
							SadlStatementContent ssc = new SadlStatementContent(host, Agent.CM, sd);
							notifyUser(textModelFolder, ssc, false);
							getExtractionProcessor().addNewSadlContent(sd);
							if (sbResults.length() > 11) {
								sbResults.append(", ");
							}
							sbResults.append(methodName);
						}
					} catch (AnswerExtractionException e) {
						String msg = "Error converting method '" + methodName + "': " + e.getMessage();
						notifyUser(textModelFolder, msg, true);
					} catch (InvalidInputException e) {
						String msg = "Error converting method '" + methodName + "': " + e.getMessage();
						notifyUser(textModelFolder, msg, true);
					} catch (AmbiguousNameException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}								
				}
			}
		}
		else {
			String msg = "Results set columns not as expected, please report. Did the query change?";
			notifyUser(textModelFolder, msg, true);
			sbResults.append(msg);
		}
		return sbResults.toString();
	}

	private boolean useKCHAIN() {
		if (useKCHAIN  != null) {
			return useKCHAIN;
		}
		String prefstr = getPreference(DialogPreferences.USE_ANSWER_KCHAIN_CG_SERVICE.getId());
		if (prefstr != null) {
			useKCHAIN = Boolean.parseBoolean(prefstr);
		}
		else {
			useKCHAIN = true;
		}
		return useKCHAIN;
	}

	private boolean savePythonScript() {
		boolean pythonScript = true;
		String pyscstr = getPreference(DialogPreferences.PYTHON_LANGUAGE.getId());
		if (pyscstr != null) {
			pythonScript = Boolean.parseBoolean(pyscstr);
		}
		return pythonScript;
	}

	private boolean saveOtherPythonScripts() {
		boolean pythonTFScript = true;
		String tfpyscstr = getPreference(DialogPreferences.OTHER_PYTHON_LANGUAGE.getId());
		if (tfpyscstr != null) {
			pythonTFScript = Boolean.parseBoolean(tfpyscstr);
		}
		return pythonTFScript;
	}

	private boolean saveOriginalScript() {
		boolean originalScript = true;
		String str = getPreference(DialogPreferences.ORIGINAL_LANGUAGE.getId());
		if (str != null) {
			originalScript = Boolean.parseBoolean(str);
		}
		return originalScript;
	}
	
	private boolean verboseExtraction() {
		boolean verboseExtraction = false;
		String str = getPreference(DialogPreferences.VERBOSE_EXTRACTION.getId());
		if (str != null) {
			verboseExtraction = Boolean.parseBoolean(str);
		}
		return verboseExtraction;
	}

	private IReasoner getInitializedTextModelReasoner() throws ConfigurationException, ReasonerNotFoundException {
		if (getTextModelReasoner() == null) {
			IConfigurationManagerForIDE textModelConfigMgr = getExtractionProcessor().getTextProcessor().getTextModelConfigMgr();
			setTextModelReasoner(textModelConfigMgr.getReasoner());
			if (!getTextModelReasoner().isInitialized()) {
				getTextModelReasoner().setConfigurationManager(textModelConfigMgr);
				List<ConfigurationItem> configItems = null;  // TODO map preferences to configItems
				//				getTextModelReasoner().initializeReasoner(codeModelFolder, getExtractionProcessor().getCodeModelName(), null);
				getTextModelReasoner().initializeReasoner(getExtractionProcessor().getTextModel(), 
						getExtractionProcessor().getCodeModelName(), null, configItems);
			}
		}
		return textModelReasoner;
	}

	private void clearTextModelReasoner() {
		getConfigurationManager().clearReasoner();
	}

	private ResultSet runInferenceFindInterestingCodeModelResults(String queryString, SaveAsSadl saveAsSadl, String fileContent)
			throws ConfigurationException, IOException, ReasonerNotFoundException, InvalidNameException, QueryParseException, QueryCancelledException, AmbiguousNameException {
		ResultSet results = null;
		// clear reasoner from any previous model
		clearCodeModelReasoner();
		String codeModelFolder = getOwlModelsFolder();
		if (getInitializedCodeModelReasoner() == null) {
			// use domain model folder because that's the project we're working in
			notifyUser(codeModelFolder, "Unable to instantiate reasoner to analyze extracted code model.", true);
		}
		else {
			queryString = getInitializedCodeModelReasoner().prepareQuery(queryString);
			results =  getInitializedCodeModelReasoner().ask(queryString);
		}
		return results;
	}
	

	private void clearCodeModelReasoner() {
		getConfigurationManager().clearReasoner();
		codeModelReasoner = null;
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
	 * @throws InvalidNameException 
	 * @throws AnswerExtractionException 
	 * @throws AmbiguousNameException 
	 */
	public String processSaveRequest(org.eclipse.emf.ecore.resource.Resource resource, SaveContent sc) throws ConfigurationException, IOException, QueryParseException, QueryCancelledException, ReasonerNotFoundException, InvalidNameException, AnswerExtractionException, AmbiguousNameException {
		String returnValue = null;
		if (resource == null) {
			throw new IOException("Argument resource in processSaveRequest cannot be null");
		}
		URI resourceURI = resource.getURI();
		IReasoner reasoner = null;
		if (ResourceManager.isSyntheticUri(null, resourceURI)) {
			reasoner = getInitializedReasonerForConfiguration(getConfigurationManager(), getDomainModel(), getDomainModelName());
		}
		else {
			reasoner = getInitializedReasonerForConfiguration(configurationManager, getDomainModelName());
		}
		if (sc.isSaveAll()) {
			StmtIterator stmtItr = getDomainModel().listStatements(null, RDF.type, getDomainModel().getOntClass(SadlConstants.SADL_IMPLICIT_MODEL_EXTERNAL_EQUATION_CLASS_URI));
			if (stmtItr.hasNext()) {
				StringBuilder sb = new StringBuilder();
				while (stmtItr.hasNext()) {
					Resource subj = stmtItr.nextStatement().getSubject();
					if (subj.isURIResource()) {
						if (stripNamespaceDelimiter(subj.getNameSpace()).equals(getDomainModelName())) {
							if (subj.canAs(Individual.class)) {
								sb.append(saveEquationInstanceToComputationalGraph(getDomainModel(), reasoner, subj.as(Individual.class)));
								sb.append(System.lineSeparator());
							}
							else {
								sb.append("Unexpected error saving all. An equation cannot be seen as an instance.\n");
							}
						}
					}
					else {
						sb.append("Equation instance is a blank node. Cannot save.\n");
					}
				}
				returnValue = sb.toString();
			}
			else {
				returnValue = "No equations found to save.";
			}
		}
		else {
			String equationToBuildUri = sc.getSourceEquationUri();
			Individual extractedModelInstance = getDomainModel().getIndividual(equationToBuildUri);
			saveEquationInstanceToTargetSemanticModel(getDomainModel(), reasoner, extractedModelInstance, sc.getTargetModelAlias());
			returnValue =  saveEquationInstanceToComputationalGraph(getDomainModel(), reasoner, extractedModelInstance);
		}
		answerUser(getOwlModelsFolder(), returnValue, true, sc.getHostEObject());
		return returnValue;
	}
	
	private boolean saveEquationInstanceToTargetSemanticModel(OntModel ontModel, IReasoner reasoner,
			Individual extractedModelInstance, String targetModelAlias) throws AnswerExtractionException {
		String[] target = null;
		if (getTargetModelMap() == null) {
			throw new AnswerExtractionException("No target found to which to save the equation model.");
		}
		else {
			if (targetModelAlias != null) {
				if (getTargetModelMap().containsKey(targetModelAlias)) {
					target = getTargetModelMap().get(targetModelAlias);
				}
				else {
					// there is an alias specified but it is not in the target map
					throw new AnswerExtractionException("Specified target '" + targetModelAlias + "' not found, unable to save the equation model.");
				}
			}
			else if (getTargetModelMap().size() == 1){
				// there's only one choice so use it
				target = getTargetModelMap().values().iterator().next();
			}
		}
		// if the target model is open in an editor insert at end of editor buffer?
		
		if (target[1] == null) {
			try {
				target[1] = getConfigurationManager().getAltUrlFromPublicUri(target[0]);
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		// Otherwise append to the end of the file and refresh Resource
		if (target[1] != null) {
			try {
				SadlUtils su = new SadlUtils();
				String trgtFilename = su.fileUrlToFileName(target[1]);
				String sadlFileName = ResourceManager.sadlFileNameOfOwlAltUrl(target[1], true);
				File owlfile = new File(trgtFilename);
				String sadlfileWithPath = ResourceManager.findSadlFilesInDir(owlfile.getParentFile().getParent(), sadlFileName);
				if (sadlfileWithPath != null) {
					String content = getEquationContent(extractedModelInstance.getURI());
					if (content != null) {
						su.appendStringToFile(new File(sadlfileWithPath), content, false);
						return true;
					}
					else {
						throw new AnswerExtractionException("No content found for equation '" + target[0] + "'");
					}
				}
				else {
					throw new AnswerExtractionException("Saving of equations to an OWL file with no SADL file not yet supported.");
				}
			} catch (MalformedURLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return false;
	}

	private String getEquationContent(String eqUri) {
		if (equationInformation != null && equationInformation.get(eqUri) != null) {
			List<String> eobjstrs = equationInformation.get(eqUri);
			StringBuilder sb = new StringBuilder();
			for (String eobjstr : eobjstrs) {
				sb.append(eobjstr);
			}
			return sb.toString();
		}
		return null;
	}

	private String saveEquationInstanceToComputationalGraph(OntModel ontModel, IReasoner reasoner, Individual extractedModelInstance) throws IOException, QueryParseException, QueryCancelledException, InvalidNameException, ConfigurationException, AmbiguousNameException {
		String returnValue = "";
		Resource type = extractedModelInstance.getRDFType(true);
		if (type != null && type.isURIResource() && 
				(type.getURI().equals(SadlConstants.SADL_IMPLICIT_MODEL_EQUATION_CLASS_URI) ||
						type.getURI().equals(SadlConstants.SADL_IMPLICIT_MODEL_EXTERNAL_EQUATION_CLASS_URI)) ) {
			String retName =  null;
			Statement argStmt = extractedModelInstance.getProperty(ontModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_ARGUMENTS_PROPERTY_URI));
			if (argStmt != null) {
				org.apache.jena.rdf.model.Resource ddList = argStmt.getObject().asResource();
				if (ddList instanceof Resource) {
					// there are method arguments
					// TODO do what??
				}
			}
			ResultSet rs = getMethodArguments(ontModel, reasoner, extractedModelInstance);
			logger.debug(rs != null ? rs.toString() : "no argument results");
			ResultSet rs2 = getMethodReturns(ontModel, reasoner, extractedModelInstance);
			try {
				String[] scriptInfo = getMethodScript(ontModel, reasoner, extractedModelInstance);
				retName = (scriptInfo != null && scriptInfo[0] != null) ? scriptInfo[0] : retName;
				String script = scriptInfo != null ? scriptInfo[1] : null;
				logger.debug(script != null ? script : "no Python script results");
				if (script != null) {
					returnValue = getExtractionProcessor().saveToComputationalGraph(extractedModelInstance, retName, rs, rs2, script, null);
					logger.debug("saveToComputationalGraph returned '" + returnValue + "'");
				}
				else {
					returnValue = "Failed to find a Python script for equation '" + extractedModelInstance.getURI() + "'.";
				}
			} catch (TranslationException e) {
				returnValue = e.getMessage();
			}
		}
		if (returnValue != null) {
			returnValue += " ";
		}
		returnValue += "Saving '" + extractedModelInstance.getLocalName();
		return returnValue;
	}

	private String[] getMethodScript(OntModel ontModel, IReasoner reasoner, Individual extractedModelInstance)
			throws QueryParseException, QueryCancelledException, IOException, TranslationException {
		String[] scriptInfo = null;
//		String pythonScriptQuery = "select ?pyScript where {<" + extractedModelInstance.getURI() + "> <" + 
//				SadlConstants.SADL_IMPLICIT_MODEL_EXPRESSTION_PROPERTY_URI +
//				"> ?sc . ?sc <" + SadlConstants.SADL_IMPLICIT_MODEL_LANGUAGE_PROPERTY_URI + "> <" + 
//				(useKCHAIN() ? SadlConstants.SADL_IMPLICIT_MODEL_PYTHON_TF_LANGUAGE_INST_URI : SadlConstants.SADL_IMPLICIT_MODEL_PYTHON_LANGUAGE_INST_URI) +
//				"> . ?sc <" + SadlConstants.SADL_IMPLICIT_MODEL_SCRIPT_PROPERTY_URI + "> ?pyScript}";				// query to get the Python script for the equation
//		ResultSet rs3 = reasoner.ask(pythonScriptQuery);
//		if (rs3 != null) {
//			if (rs3.getRowCount() != 1) {
//				throw new TranslationException("There appears to be more than one Python script associated with the equation. Unable to save.");
//			}
//			String pythonScript = rs3.getResultAt(0, 0).toString();
//			CodeLanguage language = CodeLanguage.JAVA;																	// only code we extract from currently (what about from text?)
//			if (language.equals(CodeLanguage.JAVA)) {
//				String[] returns;
//				if (useKCHAIN()) {
//					returns = getExtractionProcessor().getCodeExtractor(CodeLanguage.JAVA).extractPythonTFEquationFromCodeExtractionModel(pythonScript, extractedModelInstance.getLocalName());
//				}
//				else {
//					returns = getExtractionProcessor().getCodeExtractor(CodeLanguage.JAVA).extractPythonEquationFromCodeExtractionModel(pythonScript, extractedModelInstance.getLocalName());
//				}
//				if (returns.length != 2) {
//					throw new IOException("Invalid return from extractPythonEquationFromCodeExtractionModel; expected String[] of size 2");
//				}
//				scriptInfo = returns;
//			}
//		}
//		else {
//			String pythonScript = null;
//			String pythonTFScript = null;
//			NodeIterator expritr = extractedModelInstance.listPropertyValues(ontModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_EXPRESSTION_PROPERTY_URI));
//			while (expritr.hasNext()) {
//				RDFNode exp = expritr.next();
//				if (exp.isResource()) {
//					RDFNode lang = exp.asResource().getProperty(ontModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_LANGUAGE_PROPERTY_URI)).getObject();
//					String langStr = null;
//					if (lang instanceof Resource) {
//						langStr = lang.asResource().toString();
//					}
//					else if (lang instanceof Literal) {
//						langStr = ((Literal)lang).getValue().toString();
//					}
//					boolean thisOne = false;
//					if (langStr != null &&langStr.equals(SadlConstants.SADL_IMPLICIT_MODEL_PYTHON_TF_LANGUAGE_INST_URI)) {
//						Statement scprtstmt = exp.asResource().getProperty(ontModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_SCRIPT_PROPERTY_URI));
//						if (scprtstmt != null) {
//							pythonTFScript = scprtstmt.getObject().asLiteral().getValue().toString();
//						}
//						if (useKCHAIN()) {
//							scriptInfo = new String[2];
//							scriptInfo[0] = null;
//							scriptInfo[1] = pythonTFScript;
//							thisOne = true;
//						}
//					}
//					else if (langStr != null && langStr.equals(SadlConstants.SADL_IMPLICIT_MODEL_PYTHON_LANGUAGE_INST_URI)) {
//						Statement scprtstmt = exp.asResource().getProperty(ontModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_SCRIPT_PROPERTY_URI));
//						if (scprtstmt != null) {
//							pythonScript = scprtstmt.getObject().asLiteral().getValue().toString();
//						}
//					}
//					if (thisOne) {
//						break;
//					}
//				}
//			}
//			if (scriptInfo == null) {
//				if (useKCHAIN() && pythonScript != null) {
//					String[] returns = getExtractionProcessor().getCodeExtractor(CodeLanguage.JAVA).extractPythonTFEquationFromCodeExtractionModel(pythonScript, extractedModelInstance.getLocalName());
//					scriptInfo = returns;
//				}
//			}
//		}
//		
		return scriptInfo;
	}

	private ResultSet getMethodReturns(OntModel ontModel, IReasoner reasoner, Individual extractedModelInstance)
			throws InvalidNameException, ConfigurationException, QueryParseException, QueryCancelledException, AmbiguousNameException {
		//				String retQuery = "select ?retName ?retType where {<" + extractedModelInstance.getURI() + "> <" + 
		//						SadlConstants.SADL_IMPLICIT_MODEL_RETURN_TYPES_PROPERTY_URI + 
		//						">/<sadllistmodel:rest>*/<sadllistmodel:first> ?member . OPTIONAL{?member <" +
		//						SadlConstants.SADL_IMPLICIT_MODEL_DESCRIPTOR_NAME_PROPERTY_URI + "> ?retName} . ?member <" +
		//						SadlConstants.SADL_IMPLICIT_MODEL_DATATYPE_PROPERTY_URI + "> ?retType}";							// query to get the return types
		String retQuery = "select ?retName ?retType where {<" + extractedModelInstance.getURI() + "> <" + 
				SadlConstants.SADL_IMPLICIT_MODEL_RETURN_TYPES_PROPERTY_URI + 
				"> ?retlst . ?retlst <sadllistmodel:rest>*/<sadllistmodel:first> ?member . OPTIONAL{?member <" +
				SadlConstants.SADL_IMPLICIT_MODEL_DESCRIPTOR_NAME_PROPERTY_URI + "> ?retName} . ?member <" +
				SadlConstants.SADL_IMPLICIT_MODEL_DATATYPE_PROPERTY_URI + "> ?retType}";							// query to get the return types
		retQuery = reasoner.prepareQuery(retQuery);
		ResultSet rs2 = reasoner.ask(retQuery);
		logger.debug(rs2 != null ? rs2.toString() : "no return type results");
		if (rs2 == null) {
			StmtIterator stmtitr = ontModel.listStatements(extractedModelInstance, ontModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_RETURN_TYPES_PROPERTY_URI), (RDFNode)null);
			int r = 0;
			while (stmtitr.hasNext()) {
				String[] properties = new String[2];
				properties[0] = SadlConstants.SADL_IMPLICIT_MODEL_DESCRIPTOR_NAME_PROPERTY_URI;
				properties[1] = SadlConstants.SADL_IMPLICIT_MODEL_DATATYPE_PROPERTY_URI;
				List<RDFNode[]> propertyValues = getListMemberProperties(ontModel, stmtitr.nextStatement().getObject(), properties);
				int numArgs = propertyValues.size();
				String[] colNames = {"retName", "retType"};
				Object[][] data = new Object[numArgs][properties.length];
				for (RDFNode[] nd : propertyValues) {
					String ldn = nd[0] != null ? nd[0].toString() : null;
					data[r][0] = ldn;
					String dt = nd[1].toString();
					dt = getLocalNameFromUri(dt);
					data[r][1] = dt;
					r++;
				}
				rs2 = new ResultSet(colNames, data);
			}
		}
		return rs2;
	}

	private ResultSet getMethodArguments(OntModel ontModel, IReasoner reasoner, Individual extractedModelInstance)
			throws InvalidNameException, ConfigurationException, QueryParseException, QueryCancelledException, AmbiguousNameException {
		//				String argQuery = "select ?argName ?argType where {<" + extractedModelInstance.getURI() + "> <" + 
		//						SadlConstants.SADL_IMPLICIT_MODEL_ARGUMENTS_PROPERTY_URI + 
		//						">/<sadllistmodel:rest>*/<sadllistmodel:first> ?member . ?member <" +
		//						SadlConstants.SADL_IMPLICIT_MODEL_DESCRIPTOR_NAME_PROPERTY_URI + "> ?argName . ?member <" +
		//						SadlConstants.SADL_IMPLICIT_MODEL_DATATYPE_PROPERTY_URI + "> ?argType}";							// query to get the argument names and types
		String argQuery = "select ?argName ?argType where {<" + extractedModelInstance.getURI() + "> <" + 
				SadlConstants.SADL_IMPLICIT_MODEL_ARGUMENTS_PROPERTY_URI + 
				"> ?arglst . ?arglst <sadllistmodel:rest>*/<sadllistmodel:first> ?member . ?member <" +
				SadlConstants.SADL_IMPLICIT_MODEL_DESCRIPTOR_NAME_PROPERTY_URI + "> ?argName . ?member <" +
				SadlConstants.SADL_IMPLICIT_MODEL_DATATYPE_PROPERTY_URI + "> ?argType}";							// query to get the argument names and types
		argQuery = reasoner.prepareQuery(argQuery);
		ResultSet rs = reasoner.ask(argQuery);
		if (rs == null) {
			StmtIterator stmtitr = ontModel.listStatements(extractedModelInstance, ontModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_ARGUMENTS_PROPERTY_URI), (RDFNode)null);
			int r = 0;
			while (stmtitr.hasNext()) {
				String[] properties = new String[2];
				properties[0] = SadlConstants.SADL_IMPLICIT_MODEL_DESCRIPTOR_NAME_PROPERTY_URI;
				properties[1] = SadlConstants.SADL_IMPLICIT_MODEL_DATATYPE_PROPERTY_URI;
				List<RDFNode[]> propertyValues = getListMemberProperties(ontModel, stmtitr.nextStatement().getObject(), properties);
				int numArgs = propertyValues.size();
				String[] colNames = {"argName", "argType"};
				Object[][] data = new Object[numArgs][properties.length];
				for (RDFNode[] nd : propertyValues) {
					String ldn = nd[0].toString();
					data[r][0] = ldn;
					String dt = nd[1].toString();
					dt = getLocalNameFromUri(dt);
					data[r][1] = dt;
					r++;
				}
				rs = new ResultSet(colNames, data);
			}
		}
		return rs;
	}

	private List<RDFNode[]> getListMemberProperties(OntModel model, RDFNode node, String[] properties) {
		if (node.isResource()) {
			Statement stmt = node.asResource().getProperty(model.getProperty(SadlConstants.SADL_LIST_MODEL_FIRST_URI));
			if (stmt != null) {
				RDFNode obj = stmt.getObject();
				RDFNode[] values = null;
				if (obj.isResource()) {
					values = new RDFNode[properties.length];
					for (int pidx = 0; pidx < properties.length; pidx++) {
						Statement ldnstmt = obj.asResource().getProperty(model.getProperty(properties[pidx]));
						if (ldnstmt != null) {
							values[pidx] = ldnstmt.getObject();
						}
					}
				}
				Statement stmt2 = node.asResource().getProperty(model.getProperty(SadlConstants.SADL_LIST_MODEL_REST_URI));
				if (stmt2 != null) {
					List<RDFNode[]> more = getListMemberProperties(model, stmt2.getObject(), properties);
					more.add(0, values);
					return more;
				}
				else {
					List<RDFNode[]> lst = new ArrayList<RDFNode[]>();
					lst.add(values);
					return lst;
				}
			}
		}
		return null;
	}

	private String processEvalRequest(org.eclipse.emf.ecore.resource.Resource resource, EvalContent sc) throws ConfigurationException, ReasonerNotFoundException, IOException, EquationNotFoundException, QueryParseException, QueryCancelledException, InvalidNameException, AmbiguousNameException {
		String returnValue = "Evaluation failed";
		URI resourceURI = resource.getURI();
		IReasoner reasoner = null;
		if (ResourceManager.isSyntheticUri(null, resourceURI)) {
			reasoner = getInitializedReasonerForConfiguration(getConfigurationManager(), getDomainModel(), getDomainModelName());
		}
		else {
			reasoner = getInitializedReasonerForConfiguration(configurationManager, getDomainModelName());
		}
		Node equationToEvaluate = sc.getEquationName();
		List<UnittedParameter> params = sc.getParameters();
		Individual modelInstance = getDomainModel().getIndividual(equationToEvaluate.getURI());
		if (modelInstance == null) {
			throw new EquationNotFoundException("Equation '" + equationToEvaluate.getURI() + "' not found.");
		}
		Statement argStmt = modelInstance.getProperty(getDomainModel().getProperty(SadlConstants.SADL_IMPLICIT_MODEL_ARGUMENTS_PROPERTY_URI));
		if (argStmt != null) {
			org.apache.jena.rdf.model.Resource ddList = argStmt.getObject().asResource();
			if (ddList instanceof Resource) {
				ResultSet rs = getMethodArguments(getDomainModel(), reasoner, modelInstance);
				ResultSet rs2 = getMethodReturns(getDomainModel(), reasoner, modelInstance);
				logger.debug(rs2 != null ? rs2.toString() : "no return type results");
				try {
					returnValue = evaluateInComputationalGraph(modelInstance, rs, rs2, params);
				}
				catch (IOException e) {
					returnValue = e.getMessage();
				}
				answerUser(getOwlModelsFolder(), returnValue, true, sc.getHostEObject());
			}
		}
		return returnValue;
	}

	/**
	 * Method to evaluate an equation in the target computational graph
	 * @param modelUri--the URI of the equation to be evaluated
	 * @param outputName--the name of the output of the calculation
	 * @param rs--a ResultSet containing input names and types
	 * @param rs2--a ResultSet containing output names (may be null) and types
	 * @param modifiedPythonScript--the Python script of the equation for physics-based models
	 * @param dataLocation--the location of the data for data-driven models
	 * @return--the URI of the successfully created model in the computational graph or null if not successful
	 * @throws IOException
	 */
//	public String evaluateInComputationalGraph(String modelUri, String outputName, ResultSet rsInputs, ResultSet rsOutputs, String modifiedPythonScript, String dataLocation) throws IOException {
	public String evaluateInComputationalGraph(Individual modelToEvaluate, ResultSet rsInputs, ResultSet rsOutputs, List<UnittedParameter> params) throws IOException {
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
		String msg = "";
		if (rsInputs == null) {
			msg += "Input result set is empty. ";
		}
		if (rsOutputs == null) {
			msg += "Input result set is empty.";
		}
		if (msg.length() > 0) {
			logger.debug(msg);
			return msg;
		}
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
		int numParams = params.size();
		if (numParams != rsRowCount) {
			throw new IOException("Number of parameters does not equal the number of equation arguments.");
		}
		for (int r = 0; r < rsRowCount; r++) {
			String[] input = new String[rsColCount + 1];
			for (int c = 0; c < rsColCount; c++) {
				input[c] = rsInputs.getResultAt(r, c).toString();
			}
			input[rsColCount] = params.get(r).toString();
			inputs.add(input);
		}
		
		int rs2ColCount = rsOutputs.getColumnCount();
		int rs2RowCount = rsOutputs.getRowCount();
		if (rs2RowCount != 1) {
			throw new IOException("Only one output is currently handled by  evaluateInComputationalGraph");
		}
		for (int r = 0; r < rs2RowCount; r++) {
			String[] output = new String[rs2ColCount];
			for (int c = 0; c < rs2ColCount; c++) {
				String alias = modelToEvaluate.getLabel(null);
				output[0] = alias != null ? alias : modelToEvaluate.getLocalName();
				output[1] = rsOutputs.getResultAt(r, 1).toString();
			}
			outputs.add(output);
		}
		
		List<List<String[]>> results = evaluateInComputationalGraph(pythonify(modelToEvaluate.getLocalName()), inputs, outputs);
		String retVal = results.get(0).get(0)[2];
		return retVal;
	}

	/**
	 * Method to convert a code extracted name into a valid Python name
	 * @param localName
	 * @return
	 */
	public String pythonify(String localName) {
		return localName.replace('.', '_');
	}

	/**
	 * Method to save a Python script as an equation in the target computational graph
	 * @param modelUri--the URI of the equation to be saved
	 * @param modifiedPythonScript
	 * @param dataLocation
	 * @param inputs
	 * @param outputs
	 * @return -- a List of Lists of String array. Each String array contains the [0] name, [1] type, adn [2] the value of the output.
	 * @throws IOException
	 */
	public List<List<String[]>> evaluateInComputationalGraph(String modelUri, List<String[]> inputs, List<String[]> outputs) throws IOException {
		String serviceBaseUri = getPreference(DialogPreferences.ANSWER_KCHAIN_CG_SERVICE_BASE_URI.getId());
		KChainServiceInterface kcService = new KChainServiceInterface(serviceBaseUri);
		List<List<String[]>> results = kcService.evalCGModel(modelUri, inputs, outputs);
						
		return results;
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
    				methodCode.append(System.lineSeparator());
    				saveLine = true;
    			}
                line = reader.readLine();
            }
        } catch (IOException exc) {
            // quit
        }
		return methodCode.toString();
	}

	public boolean importCodeSnippetToComputationalGraph(Object rs, String userInput) throws InvalidNameException, ConfigurationException, ReasonerNotFoundException, QueryParseException, QueryCancelledException, AnswerExtractionException, InvalidInputException, IOException, AmbiguousNameException {
		boolean success = false;
		if (rs instanceof ResultSet) {
			List<String> initializerKeywords = getInitializerKeywords();
			boolean sns = ((ResultSet)rs).getShowNamespaces();
			((ResultSet)rs).setShowNamespaces(false);
			String[] cns = ((ResultSet) rs).getColumnNames();
			if (cns[0].equals("m")) {
				for (int r = 0; r < ((ResultSet)rs).getRowCount(); r++) {
					Object rsa = ((ResultSet) rs).getResultAt(r, 0);
					if (rsa.toString().equals(userInput)) {
						logger.debug("Ready to import method " + rsa.toString());
						
						if (cns[3].equals("s")) {
							String methScript = ((ResultSet)rs).getResultAt(r, 3).toString();
							logger.debug("Ready to build CG with method '" + rsa.toString() + "':");
							logger.debug(methScript);
							String methodName = null;
							List<String> sadlDeclaration = convertCodeExtractedMethodToExternalEquationInSadlSyntax(initializerKeywords, methodName , rsa.toString(), "Java", methScript);
							for (String sd : sadlDeclaration) {
								logger.debug("SADL equation:");
								logger.debug(sd);
							}
							success = true;
						}
						else {
							throw new AnswerExtractionException("No method script found");
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
	 * Method to convert a method extracted from text to an instance of SADL External. Note
	 * that methods extracted from text will use the SadlImplicitModel as the meta-model for representation.
	 * Note that we aren't using OwlToSadl for this conversion because the triples in the extracted model don't
	 * appear to be quite the same as the OWL created from a SADL model.
	 * @param methodName -- should be the name of the method in the extracted code model
	 * @param locality 
	 * @param locality2 
	 * @param pytfscript 
	 * @param pythoncode 
	 * @param methScript 
	 * @return -- the serialization of the new instance of External in SADL syntax
	 * @throws InvalidNameException
	 * @throws ConfigurationException
	 * @throws ReasonerNotFoundException
	 * @throws QueryParseException
	 * @throws QueryCancelledException
	 * @throws AnswerExtractionException 
	 * @throws IOException 
	 * @throws InvalidInputException 
	 * @throws AmbiguousNameException 
	 */
	private List<String> convertTextExtractedMethodToExternalEquationInSadlSyntax(String methodName, String derivedFromMethodUri, Map<String,String> scriptMap, String locality) throws InvalidNameException, ConfigurationException,
			ReasonerNotFoundException, QueryParseException, QueryCancelledException, AnswerExtractionException, InvalidInputException, IOException, AmbiguousNameException {
		List<String> returnSadlStatements = new ArrayList<String>();
		StringBuilder sb = new StringBuilder("External ");
		sb.append(methodName);
		
		IReasoner reasoner = getConfigurationManager().getReasoner();
		if (!reasoner.isInitialized()) {
			reasoner.setConfigurationManager(getConfigurationManager());
			reasoner.initializeReasoner(getOwlModelsFolder(), getExtractionProcessor().getTextModelName(), null);
		}

		String outputTypeQuery = "select distinct ?retname ?rettyp ?alias ?st where {<";
		outputTypeQuery += getDomainModelName() + "#";
//		outputTypeQuery += getLocalityURI() + "#";
		outputTypeQuery += methodName.toString().trim();
		outputTypeQuery += "> <returnTypes>/<rdf:rest>*/<rdf:first> ?rt . OPTIONAL{?rt <localDescriptorName> ?retname} . ";
		outputTypeQuery += "?rt <dataType> ?rettyp . ";
		outputTypeQuery += "OPTIONAL{?rt <localDescriptorName> ?alias} . ";
		outputTypeQuery += "OPTIONAL{?rt <augmentedType> ?augt . ?augt <semType> ?st}}";
		outputTypeQuery = getInitializedTextModelReasoner().prepareQuery(outputTypeQuery);
		ResultSet outputResults =  getInitializedTextModelReasoner().ask(outputTypeQuery);
		String retName = outputResults != null ? outputResults.getResultAt(0, 0).toString() : null;	// this assumes a single return value
		if (retName != null) {
			sb.append(" (alias \"");
			sb.append(retName);
			sb.append("\")");
		}
		int returnNoteLocation = sb.length();
		if (derivedFromMethodUri != null) {
			sb.append(" (derivedFrom ");
			sb.append(derivedFromMethodUri);
			sb.append(")");
		}

		// get inputs and outputs and identify semantic meaning thereof
		String inputQuery = "select distinct ?arg ?argName ?argtyp ?st where {<";
		inputQuery += getDomainModelName() + "#";
//		inputQuery += getLocalityURI() + "#";
		inputQuery += methodName.toString().trim();
		inputQuery += "> <arguments>/<rdf:rest>*/<rdf:first> ?arg . ?arg <localDescriptorName> ?argName . ";
		inputQuery += "OPTIONAL{?arg <dataType> ?argtyp} . ";
		inputQuery += "OPTIONAL{?arg <augmentedType> ?augt . ?augt <semType> ?st}}";
		inputQuery = getInitializedTextModelReasoner().prepareQuery(inputQuery);
		ResultSet inputResults =  getInitializedTextModelReasoner().ask(inputQuery);

		sb.append("(");
//		clearCodeModelReasoner();

		String origScript = scriptMap.get(SadlConstants.SADL_IMPLICIT_MODEL_TEXT_LANGUAGE_INST_URI);
		List<OntResource> articledClasses = new ArrayList<OntResource>();
		logger.debug(inputResults != null ? inputResults.toStringWithIndent(5) : "no results");
		if (inputResults != null) {
			inputResults.setShowNamespaces(false);
			for (int r = 0; r < inputResults.getRowCount(); r++) {
				String argType = inputResults.getResultAt(r, 2).toString();
				String argName = inputResults.getResultAt(r, 1).toString();
				Object stobj = inputResults.getResultAt(r, 3);
				if (r > 0) {
					sb.append(", ");
				}
				sb.append(argType);
				sb.append(" ");
				sb.append(checkForKeyword(argName));
				inputResults.setShowNamespaces(true);
				Object rnobj =inputResults.getResultAt(r, 3); 
				inputResults.setShowNamespaces(false);
				String rn = rnobj != null ? rnobj.toString() : null;
				if (rn != null) {
					String augtype = null;
					Object augtypeStuff = getAugmentedTypeFromLocalitySearch(methodName, argName, rn, false, origScript, locality, articledClasses);
					if (augtypeStuff == null) {
						augtype = stobj != null ? processTextToTriplesAugType(rn) : null;
						if (augtype == null && stobj != null) {
							// we have a non-domain concept suggested by textToTriples as semantic type; make note
							sb.append(" (note \"text suggests possible type '");
							sb.append(checkForKeyword(stobj.toString()));
							sb.append("'\")");
						}
					}
					else if (augtypeStuff instanceof String[]) {
						augtype = ((String[])augtypeStuff)[0];
					}
					else if (augtypeStuff instanceof String) {
						augtype = augtypeStuff.toString();
					}
					if (augtype != null) {
						sb.append("(");
						sb.append(augtype);
						sb.append(")");
					}
					else if (augtypeStuff != null && augtypeStuff instanceof String[] && ((String[])augtypeStuff)[1] != null) {
						sb.append(" (note \"");
						sb.append(((String[])augtypeStuff)[1]);
						sb.append("\")");
					}
				}
			}
		}
		sb.append(")");
		logger.debug(outputResults != null ? outputResults.toStringWithIndent(5) : "no results");
		if (outputResults != null) {
			sb.append(" returns ");
			outputResults.setShowNamespaces(false);
			int numReturnValues = outputResults.getRowCount();
//			if (numReturnValues > 1) {
//				sb.append("[");
//			}
//			for (int r = 0; r < numReturnValues; r++) {
			int r = 0;	// only handle 1 return type
				Object rt = outputResults.getResultAt(r, 1);
				if (rt != null) {
					String retType = rt.toString();
					if (r > 0) {
						sb.append(", ");
					}
					sb.append(retType);
					Object atobj = outputResults.getResultAt(r, 3);
					outputResults.setShowNamespaces(true);
					Object rnobj = outputResults.getResultAt(r, 3);
					outputResults.setShowNamespaces(false);
					String rn = rnobj != null ? rnobj.toString() : null;
					if (rn != null) {
						String augtype = null;
						Object augtypeStuff = getAugmentedTypeFromLocalitySearch(methodName, retName, rn, true, origScript, locality, articledClasses);							
						if (augtypeStuff == null) {
							augtype = atobj != null ? processTextToTriplesAugType(rn) : null;
							if (augtype == null && atobj != null) {
								// we have a non-domain concept suggested by textToTriples as semantic type; make note
								sb.append(" (note \"text suggests possible type '");
								sb.append(checkForKeyword(atobj.toString()));
								sb.append("'\")");
							}
						}
						else if (augtypeStuff instanceof String[]){
							augtype = ((String[])augtypeStuff)[0];
						}
						else if (augtypeStuff instanceof String) {
							augtype = augtypeStuff.toString();
						}
						if (augtype != null) {
							sb.append("(");
							sb.append(augtype);
							sb.append(")");
						}
						else if (augtypeStuff != null && augtypeStuff instanceof String[] && ((String[])augtypeStuff)[1] != null) {
							String note = " (note \" returns " + ((String[])augtypeStuff)[1] + "\")";
							sb.insert(returnNoteLocation, note);
						}
					}
				}
//			}
//			if (numReturnValues > 1) {
//				sb.append("]");
//			}	
		}
		else {
			int argStart = sb.indexOf("(");
			sb.insert(argStart, " (note \"This equation does not return anything\") ");
		}
		sb.append(":");
//
//		else {
//			// ANSWER doesn't currently support an equation that doesn't return anything
////			getTextProcessor().getCurrentTextModel().write(System.err);
//			throw new AnswerExtractionException("Equations that do not return a value are not supported.");
//		}
		String eqUri = getExtractionProcessor().getTextModelName() + "#" + methodName.toString().trim();
		sb.append(" \"");
		sb.append(eqUri);
		sb.append("\".");
		returnSadlStatements.add(sb.toString());
		
		// now add the scripts for lang1 and lang2
		StringBuilder sb2 = new StringBuilder(methodName);
		boolean hasPriorTriple = false;
		if (saveOriginalScript() && scriptMap.containsKey(SadlConstants.SADL_IMPLICIT_MODEL_TEXT_LANGUAGE_INST_URI)) {
			sb2.append(" has expression (a Script with language ");
			sb2.append(getLocalNameFromUri(SadlConstants.SADL_IMPLICIT_MODEL_TEXT_LANGUAGE_INST_URI));
			sb2.append(", with script \n\"");
			sb2.append(escapeDoubleQuotes(origScript));
			sb2.append("\")");
			hasPriorTriple = true; 
		}
		if (savePythonScript() && scriptMap.containsKey(SadlConstants.SADL_IMPLICIT_MODEL_PYTHON_LANGUAGE_INST_URI)) {
			if (hasPriorTriple) {
				sb2.append(",\n");
			}
			sb2.append(" has expression (a Script with language ");
			sb2.append(getLocalNameFromUri(SadlConstants.SADL_IMPLICIT_MODEL_PYTHON_LANGUAGE_INST_URI));
			sb2.append(", with script \n\"");
			sb2.append(escapeDoubleQuotes(scriptMap.get(SadlConstants.SADL_IMPLICIT_MODEL_PYTHON_LANGUAGE_INST_URI)));
			sb2.append("\")");
			hasPriorTriple = true; 
		}
		if (saveOtherPythonScripts() && scriptMap.containsKey(DialogConstants.NUMPY_PYTHON_LANGUAGE)) {
			if (hasPriorTriple) {
				sb2.append(",\n");
			}
			sb2.append(" has expression (a Script with language ");
			sb2.append(getLocalNameFromUri(DialogConstants.NUMPY_PYTHON_LANGUAGE));
			sb2.append(", with script \n\"");
			sb2.append(escapeDoubleQuotes(scriptMap.get(DialogConstants.NUMPY_PYTHON_LANGUAGE)));
			sb2.append("\")");
		}
		sb2.append(".\n");
		returnSadlStatements.add(sb2.toString());
		return returnSadlStatements;
	}

	private String processTextToTriplesAugType(String textToTriplesAugType) {
		if (textToTriplesAugType.contains("#")) {
			OntClass cls = getDomainModel().getOntClass(textToTriplesAugType);
			if (cls != null) {
				return checkForKeyword(cls.getLocalName());
			}
			Property prop = getDomainModel().getOntProperty(textToTriplesAugType);
			if (prop != null) {
				return checkForKeyword(prop.getLocalName());
			}
		}
		else if (1 == 1) {	// ignore the rest for now
			return null;
		}
		else {
			String conceptUri = null;
			String conceptName = null;
			int split = textToTriplesAugType.lastIndexOf("#");
			if (split > 0) {
				conceptName = textToTriplesAugType.substring(split + 1);
				conceptUri = getDomainModelName() + "#" + conceptName;
			}
			if (conceptUri == null) {
				split = textToTriplesAugType.lastIndexOf('/');
				if (split > 0) {
					conceptName = textToTriplesAugType.substring(split + 1);
					conceptUri = getDomainModelName() + "#" + conceptName;
				}
			}
			if (conceptName == null) {	// must be a localname already
				conceptName = textToTriplesAugType;
				conceptUri = getDomainModelName() + "#" + conceptName;
			}
			// find name in domain model else insert line in dialog
			Resource dmnrsrc = getDomainModel().getOntResource(conceptUri);
			if (dmnrsrc == null) {
				try {
					if (!isConceptAdded(conceptName)) {
						notifyUser(getOwlModelsFolder(), checkForKeyword(conceptName) + " is a class.", false);
						conceptAdded(conceptName);
					}
				} catch (ConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			return conceptName;
		}
		return null;
	}

	private void conceptAdded(String conceptName) {
		if(!classesDeclared .contains(conceptName)) {
			classesDeclared.add(conceptName);
		}
	}

	private boolean isConceptAdded(String conceptName) {
		return classesDeclared.contains(conceptName);
	}
	
	private void clearConceptsAdded() {
		classesDeclared.clear();
	}

	/**
	 * Method to get the AugmentedType of a name from locality search and return a valid SADL AugmentedType expression
	 * @param methodUri -- the URI of the method being processed
	 * @param argName -- name of the argument for which information is desired
	 * @param semType -- the semantic type returned by the text service
	 * @param isMethodReturn -- true if looking for augmented type for method return else false for an argument
	 * @param script -- the script (text) that is being processed 
	 * @param locality -- the locality in which the search is to occur
	 * @param articledClasses 
	 * @return -- augmented type information in SADL format. This could be an augmented type as string or a String[2] with a note as element 1.
	 * @throws IOException 
	 * @throws InvalidInputException 
	 */
	private Object getAugmentedTypeFromLocalitySearch(String methodUri, String argName, String semType, boolean isMethodReturn, String script, String locality, List<OntResource> articledClasses) throws InvalidInputException, IOException {
		// check cache
		if (argName != null && equationVariableContextHasBeenCached(argName, locality)) {
			return getEquationVariableContextFromCache(argName, locality);
		}
		List<Object> extractions = getCurrentTextExtractionExtracts();
		// first try classes
		Object clsmap = extractions != null ? extractions.get(3) : null;
		if (clsmap instanceof Map<?,?>) {
			Object clses = ((Map<?,?>)clsmap).get(semType);
			if (clses instanceof List<?>) {
				Object cls = ((List<?>)clses).get(0);
				if (cls != null) {
					if (cls instanceof OntClass) {
						return ((OntClass)cls).getLocalName();
					}
					else {
						return cls.toString();
					}
				}
			}
		}
		// next try properties
		Object propmap = extractions != null ? extractions.get(3) : null;
		if (propmap instanceof Map<?,?>) {
			Object props = ((Map<?,?>)propmap).get(semType);
			if (props instanceof List<?>) {
				Object prop = ((List<?>)props).get(0);
				if (prop != null) {
					OntProperty op = getDomainModel().getOntProperty(prop.toString());
					if (op != null) {
						String rng = getRange(op);
						if (!rng.contains(" ")) {
							return checkForKeyword(getLocalName(rng));
						}
					}
					return checkForKeyword(getLocalName(prop.toString()));
				}
			}
		}
		if (getExtractionProcessor().getCodeExtractor() != null) {
			String argname = getLocalName(argName);
			IModelFromCodeExtractor codeExtractor = getExtractionProcessor().getCodeExtractor();
			if (codeExtractor instanceof JavaModelExtractorJP) {
				if (isMethodReturn) {
					String retCmt = ((JavaModelExtractorJP)codeExtractor).getReturnJavadoc(methodUri);
					if (retCmt == null) {
						retCmt = ((JavaModelExtractorJP)codeExtractor).getReturnLineComment(methodUri);
					}
					if (retCmt != null) {
						String retType;
						try {
							retType = getAugmentedTypeFromComment(localityURI, retCmt);
							String[] returns = new String[2];
							returns[0] = retType;
							returns[1] = retCmt;
//							if (retType != null) {
								return returns;
//							}
						} catch (ConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (AnswerExtractionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				else {
					String argCmt = ((JavaModelExtractorJP)codeExtractor).getParameterJavadoc(methodUri, argname);
					if (argCmt != null) {
						String argType;
						try {
							argType = getAugmentedTypeFromComment(localityURI, argCmt);
							String[] returns = new String[2];
							returns[0] = argType;
							returns[1] = argCmt;
//							if (argType != null) {
								return returns;
//							}
						} catch (ConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (IOException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (AnswerExtractionException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
			}
		}
		if (1 == 1) {		// don't use code below for now
			return null;
		}
		// 	search the locality for the argName
		EquationVariableContextResponse evc = getTextProcessor().equationVariableContext(argName, locality);
		List<String[]> evcr = null;
		if (evc != null) {
			evcr = evc.getResults();
			if (script != null) {
				if (evcr != null) {
					for (String[] r : evcr) {
						// r[0] is the script using this name, only consider results for this script
						if (r[0].trim().equals(script.trim())) {
							String conceptName = r[2];	// the label from the locality search
							if (conceptName != null) {
								if (getDomainModel() != null) {
									// look for something in the domain ontology that has a label matching the locality label
									Literal lit = getDomainModel().createLiteral( conceptName, "en" );  	// this is how the literals are created in JenaBasedSadlModelProcessor; 
																							// literals from elesewhere might not match
									StmtIterator stmtItr = getDomainModel().listStatements(null, RDFS.label, lit);
									if (stmtItr.hasNext()) {
										while (stmtItr.hasNext()) {
											Resource subj = stmtItr.nextStatement().getSubject();
											if (subj.isURIResource()) {
												if (subj.canAs(OntClass.class)) {
													stmtItr.close();
													cacheEquationVariableContext(argName, locality, subj.getLocalName());
													return " (" + checkForKeyword(subj.getLocalName()) + ")";
												}
											}
										}
									}
								}
							}
						}
					}
				}
				for (String[] r : evcr) {
					// r[0] is the script using this name, only consider results for this script
					if (r[0].trim().equals(script.trim())) {
						String conceptName = r[2];	// the label from the locality search
						if (conceptName != null) {
							String matchingRsrcQN = findOntModelResourceWithMatchingLocalname(conceptName, argName, articledClasses);
							if (matchingRsrcQN != null) {
								cacheEquationVariableContext(argName, locality, matchingRsrcQN);
								return " (" + checkForKeyword(matchingRsrcQN) + ")";
							}
						}
					}
				}
			}
		}
		if (getDomainModel() != null) {
			// look for something in the domain ontology that has a label matching the name
			Literal lit = getDomainModel().createLiteral( argName, "en" );  	// this is how the literals are created in JenaBasedSadlModelProcessor; 
																	// literals from elesewhere might not match
			StmtIterator stmtItr = getDomainModel().listStatements(null, RDFS.label, lit);
			if (stmtItr.hasNext()) {
				while (stmtItr.hasNext()) {
					Resource subj = stmtItr.nextStatement().getSubject();
					if (subj.isURIResource() && subj.canAs(OntResource.class)) {
						stmtItr.close();
						String concept = resourceToAugmentedTypeContent(getDomainModel(), argName, subj.as(OntResource.class), articledClasses);
						cacheEquationVariableContext(argName, locality, concept);
						return " (" + concept + ")";
					}
				}
			}
//			else {
//				getDomainModel().write(System.err);
//			}
			String matchingRsrcQN = findOntModelResourceWithMatchingLocalname(argName, argName, articledClasses);
			if (matchingRsrcQN != null) {
				cacheEquationVariableContext(argName, locality, matchingRsrcQN);
				return " (" + checkForKeyword(matchingRsrcQN) + ")";
			}
		}
		if (evcr != null) {
			List<String> thisNamesUris = null;
			for (String[] r : evcr) {
				if (r[3] != null) {
					// we have a URI
					if (thisNamesUris == null) thisNamesUris = new ArrayList<String>();
					if (!thisNamesUris.contains(r[3])) {
						thisNamesUris.add(r[3]);
					}
					if (getUnmatchedUrisAndLabels() == null) {
						setUnmatchedUrisAndLabels(new HashMap<String, List<String>>());
					}
					
					if (r[2] == null) {	// no label
						getUnmatchedUrisAndLabels().put(r[3],  null);
					}
					else {
						// we have a label; 
						if (getUnmatchedUrisAndLabels().containsKey(r[3])) {
							if (!getUnmatchedUrisAndLabels().get(r[3]).contains(r[2])) {
								getUnmatchedUrisAndLabels().get(r[3]).add(r[2]);
							}
						}
						else {	
							List<String> labels = new ArrayList<String>();
							labels.add(r[2]);
							getUnmatchedUrisAndLabels().put(r[3], labels);
						}
					}
				}
			}
//			System.out.println("Unmapped concepts found for parameter '" + name + "':");
			if (getUnmatchedUrisAndLabels() == null) {
//				System.out.println("   none");
			}
			else if (thisNamesUris != null) {
				StringBuilder sb = null;
				int cntr = 0;
				for (String thisUri : thisNamesUris) {
					if (getUnmatchedUrisAndLabels().containsKey(thisUri)) {
						if (sb == null) sb = new StringBuilder(" (");
						if (cntr > 0) sb.append(" or ");
						sb.append(checkForKeyword(getLocalNameFromUri(thisUri)));
					}
				}
				if (sb != null) sb.append(") ");
				String concept = sb.toString();
				cacheEquationVariableContext(argName, locality, concept);
				return concept;
			}
//				Iterator<String> itr = getUnmatchedUrisAndLabels().keySet().iterator();
//				if (itr.hasNext()) {
//					StringBuilder sb = new StringBuilder(" (");
//					int cntr = 0;
//					while (itr.hasNext()) {
//						String uri = itr.next();
//						System.out.println("   Uri: " + uri);
//						if (cntr > 0) sb.append(" or ");
//						sb.append(getLocalNameFromUri(uri));
//						if (getUnmatchedUrisAndLabels() != null && getUnmatchedUrisAndLabels().containsKey(uri)) {
//							System.out.println("      Labels:");
//							if (getUnmatchedUrisAndLabels().get(uri) != null) {
//								for (String label : getUnmatchedUrisAndLabels().get(uri)) {
//									System.out.println("        " + label);
//								}
//							}
//						}
//						cntr++;
//					}
//					sb.append(") ");
//					return sb.toString();
//				}
//			}
		}
		cacheEquationVariableContext(argName, locality, null);
		return null;
	}
	
	private String getAugmentedTypeFromComment(String locality, String comment) throws ConfigurationException, IOException, AnswerExtractionException {
		String prefix = null;
		OntModel cmtModel = getOntModelFromText(getLocalityURI(), comment, locality, getDomainModelName(), prefix , false, false);
		if (cmtModel != null) {
			StmtIterator mcpitr = cmtModel.listStatements(null, cmtModel.getProperty(MATCHINGCLASS_PROPERTY_URI), (RDFNode)null);
			if (mcpitr.hasNext()) {
				// we have matching classes from domain ontology
				StringBuilder sb = new StringBuilder();
				int cntr = 0;
				List<String> added = new ArrayList<String>();
				while (mcpitr.hasNext()) {
					Statement mcpstmt = mcpitr.nextStatement();
					String cls = checkForKeyword(mcpstmt.getObject().asResource().getLocalName());
					if (!added.contains(cls)) {
						if (cntr++ > 0) {
							sb.append(" or ");
						}
						sb.append(cls);
						added.add(cls);
					}
				}
				if (cntr > 1) {
					sb.insert(0,  "(");
					sb.append(")");
				}
				return sb.toString();
			}
			StmtIterator mppitr = cmtModel.listStatements(null, cmtModel.getProperty(MATCHINGCLASS_PROPERTY_URI), (RDFNode)null);
			if (mppitr.hasNext()) {
				// we have matching classes from domain ontology
				StringBuilder sb = new StringBuilder();
				int cntr = 0;
				List<String> added = new ArrayList<String>();
				while (mppitr.hasNext()) {
					Statement mcpstmt = mppitr.nextStatement();
					String prop = checkForKeyword(mcpstmt.getObject().asResource().getLocalName());
					if (!added.contains(prop)) {
						if (cntr++ > 0) {
							sb.append(" or ");
						}
						sb.append(prop);
						added.add(prop);
					}
				}
				
				if (cntr > 1) {
					sb.insert(0,  "(");
					sb.append(")");
				}
				return sb.toString();
			}
		}
		return null;
	}
	
	/**
	 * Method to get an OntModel of equations and concepts from a specific piece of text using the domain ontology as context
	 * @param identifier
	 * @param content
	 * @param locality
	 * @param domainModelName
	 * @param prefix
	 * @param addToTextModel
	 * @param notifyUser
	 * @return
	 * @throws IOException
	 * @throws ConfigurationException
	 * @throws AnswerExtractionException
	 */
	private OntModel getOntModelFromText(String identifier, String content, String locality, 
			String domainModelName, String prefix, boolean addToTextModel, boolean notifyUser) 
					throws IOException, ConfigurationException, AnswerExtractionException {
		if (!isDomainModelForTextSericeUploaded()) {
			String response = getTextProcessor().uploadDomainModel(getLocalityURI(), getDomainModelName(), getSimplifiedDomainModelForTextService());
			if (!response.equalsIgnoreCase("ontology successfully uploaded")) {
				System.err.println("Failed to upload domain model to text service");
//				notifyUser(getOwlModelsFolder(), "Domain " + response + " to text extraction service", true);
//				FileOutputStream fos = new FileOutputStream(new File("c:/TMP/domainmodel.n3"));
//				getSimplifiedDomainModelForTextService().write(fos, "N3");
//				fos.flush();
//				fos.close();
			}
			else if (notifyUser) {
				notifyUser(getOwlModelsFolder(), response, true);
			}
		}
		
		TextProcessor tp = getExtractionProcessor().getTextProcessor();
		OntModel txtModel = tp.getOntModelFromText(identifier, content, locality, domainModelName, prefix , addToTextModel, notifyUser);
		String cgResponse = tp.clearGraph(locality);
		return txtModel;
	}
	
	/**
	 * Method to extract a domain ontology from text
	 * @param identifier
	 * @param content
	 * @param locality
	 * @param domainModelName
	 * @param prefix
	 * @param AddToTextModel
	 * @param notifyUser
	 * @return
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	private OntModel getDomainOntologyFromText(String identifier, String textContent, String locality,
			String domainModelName, String prefix, boolean addToTextModel, boolean notifyUser) throws IOException, ConfigurationException {
		// Note: this service does not currently use the existing domain ontology so there is no need to upload it
		TextProcessor tp = getExtractionProcessor().getTextProcessor();
		OntModel theModel = tp.getDomainModelFromText(domainModelName, "n3", textContent);
		return theModel;
	}

	private boolean equationVariableContextHasBeenCached(String name, String locality) {
		String key = locality + "#" + name;
		return cachedEquationVariableContext.containsKey(key);
	}
	
	private String getEquationVariableContextFromCache(String name, String locality) {
		String key = locality + "#" + name;
		return cachedEquationVariableContext.get(key);
	}
	
	private void cacheEquationVariableContext(String name, String locality, String nameToCache) {
		String key = locality + "#" + name;
		cachedEquationVariableContext.put(key, nameToCache);
	}

	/**
	 * Method to get the declaration of concepts not matched in the domain ontology for inclusion in the dialog window content
	 * @return -- list of statements in SadlSyntax
	 */
	private List<String> getUnmatchedConceptDeclarationStatements() {
		Map<String, List<String>> unmatched = getUnmatchedUrisAndLabels();
		if (unmatched != null) {
			List<String> stmts = new ArrayList<String>();
			Iterator<String> itr = unmatched.keySet().iterator();
			while (itr.hasNext()) {
				String uri = itr.next();
				StringBuilder sb = new StringBuilder();
				String localName = getLocalNameFromUri(uri);
				sb.append(localName);
				List<String> labels = unmatched.get(uri);
				if (labels != null) {
					sb.append(" (alias ");
					boolean first = true;
					for (String label : labels) {
						if (!first) sb.append(", ");
						sb.append("\"");
						sb.append(label);
						sb.append("\"");
						first = false;
					}
					sb.append(") is a class.");
				}
				stmts.add(sb.toString());
				StringBuilder sb2 = new StringBuilder();
				sb2.append(localName);
				sb2.append(" definedBy \"");
				sb2.append(getNamespaceFromUri(uri));
				sb2.append("\".");
				stmts.add(sb2.toString());
			}
			return stmts;
		}
		return null;
	}

	/** Method to extract a localname from an ontology resource URI
	 * @param uri
	 * @return local name
	 */
	private String getLocalNameFromUri(String uri) {
		int lbl = uri.indexOf('#');
		if ( lbl > 0) {
			return uri.substring(lbl + 1);
		}
		int lastSlash = uri.lastIndexOf('/');
		if (lastSlash > 0) {
			return uri.substring(lastSlash + 1);
		}
		return uri;
	}

	/** Method to extract a namespace from an ontology resource URI
	 * @param uri
	 * @return namespace
	 */
	private String getNamespaceFromUri(String uri) {
		int lbl = uri.indexOf('#');
		if ( lbl > 0) {
			return uri.substring(0, lbl);
		}
		int lastSlash = uri.lastIndexOf('/');
		if (lastSlash > 0) {
			return uri.substring(0, lastSlash);
		}
		return uri;
	}

	/**
	 * Method to find an OntResource in the domain model that matches the resource name
	 * @param resourceName
	 * @param avoidanceName -- name that should be avoided (use prefix if match)
	 * @param articledClasses
	 * @return
	 */
	private String findOntModelResourceWithMatchingLocalname(String resourceName, String avoidanceName, List<OntResource> articledClasses) {
		OntModel dm = getDomainModel();
		if (dm != null) {
			OntResource resourceFound = null;
			if (getDialogAnswerProvider(getResourceUri()) instanceof DialogAnswerProviderConsoleForTest) {
				// only check the domain model if it is not generated from an Eclipse UI DialogAnswerProvider
				ExtendedIterator<Ontology> oitr = dm.listOntologies();
				while (oitr.hasNext()) {
					String ouri = oitr.next().getURI();
					String uri = ouri.endsWith("#") ? ouri : (ouri + "#") + resourceName;
					resourceFound = dm.getOntResource(uri);
					if (resourceFound != null) {
						oitr.close();
						break;
					}
				}
			}
			if (resourceFound == null) {
				Iterator<String> impitr = dm.listImportedOntologyURIs(true).iterator();
				while (impitr.hasNext()) {
					String impns = impitr.next();
					String uri = impns.endsWith("#") ? impns : (impns + "#") + resourceName;
					resourceFound = dm.getOntResource(uri);
					if (resourceFound != null) {
						break;
					}
				}
			}
			if (resourceFound != null) {
				return resourceToAugmentedTypeContent(dm, avoidanceName, resourceFound, articledClasses);
			}

		}
		return null;
	}

	private String resourceToAugmentedTypeContent(OntModel dm, String name, OntResource resourceFound, List<OntResource> articledClasses) {
		StringBuilder sb = new StringBuilder();
		String prefix = getConfigurationManager().getGlobalPrefix(resourceFound.getNameSpace());
		if (resourceFound.getLocalName().equals(name)) {
			sb.append(prefix + ":" + name);
		}
		else {
			sb.append(resourceFound.getLocalName());
		}
		if (resourceFound.isProperty()) {
			StmtIterator dmitr = dm.listStatements(resourceFound, RDFS.domain, (RDFNode)null);
			while (dmitr.hasNext()) {
				RDFNode domain = dmitr.nextStatement().getObject();
				if (domain.isURIResource()) {
					if (lookForSubclassInArticledClasses && domain.asResource().canAs(OntResource.class)) {
						OntResource subclass = findSubclassInArticledClasses(articledClasses, domain.asResource().as(OntResource.class));
						if (subclass != null) {
							domain = subclass;
						}
					}
					String ln = domain.asResource().getLocalName();
					if (!articledClasses.contains(domain)) {
						if (TextProcessor.isVowel(ln.charAt(0))) {
							sb.append(" of an ");
						}
						else {
							sb.append(" of a ");
						}
						articledClasses.add(domain.asResource().as(OntResource.class));
					}
					else {
						sb.append(" of the ");
					}
					sb.append(ln);
					dmitr.close();
					break;
				}
			}
		}
		return sb.toString();
	}

	private OntResource findSubclassInArticledClasses(List<OntResource> articledClasses, OntResource cls) {
		for (OntResource or : articledClasses) {
			try {
				if (SadlUtils.classIsSubclassOf(or.as(OntClass.class), cls, true, null)) {
					return or;
				}
			} catch (CircularDependencyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Method to convert a method extracted from code to an instance of SADL External. Note
	 * that methods extracted from code use the CodeExtractionModel as the meta-model for representation.
	 * @param initializerKeywords 
	 * @param methodName -- should be the name of the method in the extracted code model
	 * @param pythoncode 
	 * @param methScript 
	 * @return -- the serialization of the new instance of External in SADL syntax
	 * @throws InvalidNameException
	 * @throws ConfigurationException
	 * @throws ReasonerNotFoundException
	 * @throws QueryParseException
	 * @throws QueryCancelledException
	 * @throws AnswerExtractionException 
	 * @throws IOException 
	 * @throws InvalidInputException 
	 * @throws AmbiguousNameException 
	 */
	private List<String> convertCodeExtractedMethodToExternalEquationInSadlSyntax(List<String> initializerKeywords, String methodUri, String methodName, String originalLanguage, String originalScript) throws InvalidNameException, ConfigurationException,
			ReasonerNotFoundException, QueryParseException, QueryCancelledException, AnswerExtractionException, InvalidInputException, IOException, AmbiguousNameException {
		String pythoncode = null;
		String tfPythonCode = null;
		String npPythonCode = null;
		String locality = getLocalityURI();
		try {
			String className;
			if (methodName.indexOf('.') > 0) {
				int endOfClassName = methodName.indexOf('.');
				className = methodName.substring(0, endOfClassName);
			}
			else {
				className = "UnidentifiedClass";
			}
			if (savePythonScript() || saveOtherPythonScripts()) {
				AnswerExtractionProcessor ep = getExtractionProcessor();
				pythoncode = ep.translateMethodJavaToPython(className, originalScript);
				if (saveOtherPythonScripts()) {
					tfPythonCode = pythonToTensorFlowPython(pythoncode);
					npPythonCode = pythonToNumPyPython(pythoncode);
				}
			}
		} catch (IOException e) {
			Throwable cause = e.getCause();
			if (cause != null) {
				StringBuilder sb = new StringBuilder(e.getMessage());
				sb.append(" to translate Java method '" + methodName + "' to Python. ");
				sb.append(cause.getMessage());
				sb.append(".");
				System.err.println(sb.toString());
			}
			else {
				StringBuilder sb = new StringBuilder(e.getMessage());
				sb.append(" to translate Java method '" + methodName + "' to Python. ");
				System.err.println(sb.toString());
//				e.printStackTrace();
			}
		}
		List<String> returnSadlStatements = new ArrayList<String>();
		StringBuilder sb = new StringBuilder("External ");
		sb.append(methodName);
		int returnNoteLocation = sb.length();
		sb.append("(");
//		clearCodeModelReasoner();
		List<OntResource> articledClasses = new ArrayList<OntResource>();
		int typeDeclarationIndex = 0;
		// get inputs and outputs and identify semantic meaning thereof
		String inputQuery = "select ?arg ?argName ?argtyp where {<";
		inputQuery += methodName.toString().trim();
		inputQuery += "> <cmArguments>/<sadllistmodel:rest>*/<sadllistmodel:first> ?arg . ?arg <varName> ?argName . OPTIONAL{?arg <varType> ?argtyp}}";
		inputQuery = getInitializedCodeModelReasoner().prepareQuery(inputQuery);
		ResultSet inputResults =  getInitializedCodeModelReasoner().ask(inputQuery);
		logger.debug(inputResults != null ? inputResults.toStringWithIndent(5) : "no results");
		if (inputResults != null) {
			for (int r = 0; r < inputResults.getRowCount(); r++) {
				String argType = inputResults.getResultAt(r, 2).toString();
				if (!typeFoundInSomeModel(argType)) {
					if (!addedTypeDeclarations.contains(argType)) {
						returnSadlStatements.add(typeDeclarationIndex++, (argType + " is a class."));
						addedTypeDeclarations.add(argType);
					}
				}
				String argName = inputResults.getResultAt(r, 1).toString();
				Object stobj = null; // inputResults.getResultAt(r, 3);
				if (r > 0) {
					sb.append(", ");
				}
				sb.append(argType);
				sb.append(" ");
				sb.append(checkForKeyword(argName));
				inputResults.setShowNamespaces(true);
				Object argUri =inputResults.getResultAt(r, 0); 
				inputResults.setShowNamespaces(false);
				String augtype = null;
				Object augtypeStuff = getAugmentedTypeFromLocalitySearch(methodUri, argName, null, false, null, locality, articledClasses);
				if (augtypeStuff == null && argUri != null) {
					augtype = stobj != null ? processTextToTriplesAugType(argUri.toString()) : null;
				}
				else if (augtypeStuff instanceof String[]){
					augtype = ((String[])augtypeStuff)[0];
				}
				if (augtype != null) {
					sb.append("(");
					sb.append(augtype);
					sb.append(")");
				}
				else if (augtypeStuff != null && augtypeStuff instanceof String[] && ((String[])augtypeStuff)[1] != null) {
					sb.append(" (note \"");
					sb.append(((String[])augtypeStuff)[1]);
					sb.append("\")");
				}
			}
		}
		sb.append(")"); 
		
		String outputTypeQuery = "select ?rettyp where {<";
		outputTypeQuery += methodName.toString().trim();
		outputTypeQuery += "> <cmReturnTypes>/<sadllistmodel:rest>*/<sadllistmodel:first> ?rettyp }";
		outputTypeQuery = getInitializedCodeModelReasoner().prepareQuery(outputTypeQuery);
		ResultSet outputResults =  getInitializedCodeModelReasoner().ask(outputTypeQuery);
		logger.debug(outputResults != null ? outputResults.toStringWithIndent(5) : "no results");
		if (outputResults != null) {
			sb.append(" returns ");
			int numReturnValues = outputResults.getRowCount();
//			if (numReturnValues > 1) {
//				sb.append("[");
//			}
			for (int r = 0; r < numReturnValues; r++) {
				String retType = outputResults.getResultAt(r, 0).toString();
				if (!typeFoundInSomeModel(retType)) {
					if (!addedTypeDeclarations.contains(retType)) {
						returnSadlStatements.add(typeDeclarationIndex++, (retType + " is a class."));
						addedTypeDeclarations.add(retType);
					}
				}
				if (r > 0) {
					sb.append(", ");
				}
				sb.append(retType);
				String augtype = null;
				Object augtypeStuff = getAugmentedTypeFromLocalitySearch(methodUri, null, null, true, null, locality, articledClasses);							
				if (augtypeStuff == null) {
//					if (rn != null) {
//						augtype = atobj != null ? processTextToTriplesAugType(rn) : null;
//					}
				}
				else if (augtypeStuff instanceof String[]){
					augtype = ((String[])augtypeStuff)[0];
				}
				if (augtype != null) {
					sb.append("(");
					sb.append(augtype);
					sb.append(")");
				}
				else if (augtypeStuff != null && augtypeStuff instanceof String[] && ((String[])augtypeStuff)[1] != null) {
					String note = " (note \"returns " + ((String[])augtypeStuff)[1] + "\")";
					sb.insert(returnNoteLocation, note);
				}
			}
			sb.append(":");
//			if (numReturnValues > 1) {
//				sb.append("]");
//			}	
		}
		else {
			sb.append(":");
		}
		String eqUri = getExtractionProcessor().getCodeModelName() + "#" + methodName.toString().trim();
		sb.append(" \"");
		sb.append(eqUri);
		sb.append("\".");
		returnSadlStatements.add(sb.toString());
				
		// now add the scripts for lang1 and lang2
		StringBuilder sb2 = new StringBuilder(methodName);
		boolean hasPriorTriple = false;
		// is this an InitializerMethod?
		String initializedClassName = getInitializedClassName(initializerKeywords, methodName);
		if (initializedClassName != null) {
			sb2.append(" is an IntializerMethod, initializes ");
			sb2.append(initializedClassName);
			hasPriorTriple = true;
		}
		
		// put in any dependencies
//		SADL_IMPLICIT_MODEL_DEPENDS_ON_PROPERTY_URI
		String dependencyQuery = "select distinct ?dm where {<";
		dependencyQuery += methodName.toString().trim();
		dependencyQuery += "> <calls> ?mc . ?mc <codeBlock> ?dm}";
		dependencyQuery = getInitializedCodeModelReasoner().prepareQuery(dependencyQuery);
		ResultSet dependencyRs =  getInitializedCodeModelReasoner().ask(dependencyQuery);
		if (dependencyRs != null && dependencyRs.hasNext()) {
			dependencyRs.setShowNamespaces(false);
			for (int r = 0; r < dependencyRs.getRowCount(); r++) {
				if (hasPriorTriple) {
					sb2.append(",\n");
				}
				sb2.append(" has dependsOn ");
				sb2.append(dependencyRs.getResultAt(r, 0));
				hasPriorTriple = true;
			}
		}
		else {
			String q = "select ?m ?dm where {?m <" + DialogConstants.SADL_IMPLICIT_MODEL_DEPENDS_ON_PROPERTY_URI + "> ?dm}";
			ResultSet rsq = getInitializedCodeModelReasoner().ask(q);
			if (rsq != null) {
				int i = 0;
			}
		}
		ResultSet impIn = getImplicitInputs(methodName);
		if (impIn != null) {
			for (int r = 0; r < impIn.getRowCount(); r++) {
				if (hasPriorTriple) {
					sb2.append(",\n");
				}
				sb2.append(" has implicitInput (an ImplicitDataDescriptor with localDescriptorName \"");
				sb2.append(impIn.getResultAt(r, 0));
				sb2.append("\", with dataType ");
				String rat = impIn.getResultAt(r, 1).toString();
				String typ = nameToUri(rat);
				String resolved = nameToUri(typ);
				sb2.append("\"");
				if (resolved != null) {
					sb2.append(resolved);
				}
				else {
					sb2.append(typ);
				}
				sb2.append("\", ");
				if (impIn.getResultAt(r, 2) != null) {
					String cvuri = impIn.getResultAt(r, 2).toString();
					Individual cv = getExtractionProcessor().getCodeModel().getIndividual(cvuri);
					if (cv != null) {
						List<String> augmentedTypes = getAugmentedTypesOfVariable(cv);
						if (augmentedTypes != null) {
							sb2.append("with augmentedType (a SemanticType");
							for (int i = 0 ;i < augmentedTypes.size(); i++) {
								if (i > 0) {
									sb2.append(", ");
								}
								sb2.append(" with semType ");
								sb2.append(augmentedTypes.get(i));
							}
							sb2.append(")");
						}
						sb2.append(getCodeVariableDeclaration(methodName, cv));
					}
				}
				sb2.append(")");
				hasPriorTriple = true;
			}
			sb2.append(System.lineSeparator());
		}
		ResultSet impOut = getImplicitOutputs(methodName);
		if (impOut != null) {
			for (int r = 0; r < impOut.getRowCount(); r++) {
				if (hasPriorTriple) {
					sb2.append(",\n");
				}
				sb2.append(" has implicitOutput (an ImplicitDataDescriptor with localDescriptorName \"");
				sb2.append(impOut.getResultAt(r, 0));
				sb2.append("\", with dataType ");
				String typ = impOut.getResultAt(r, 1).toString();
				String resolved = nameToUri(typ);
				sb2.append("\"");
				if (resolved != null) {
					sb2.append(resolved);
				}
				else {
					sb2.append(typ);
				}
				if (impOut.getResultAt(r, 2) != null) {
					sb2.append("\", ");
					String cvuri = impOut.getResultAt(r, 2).toString();
					Individual cv = getExtractionProcessor().getCodeModel().getIndividual(cvuri);
					if (cv != null) {
						List<String> augmentedTypes = getAugmentedTypesOfVariable(cv);
						if (augmentedTypes != null) {
							sb2.append("with augmentedType (a SemanticType");
							for (int i = 0 ;i < augmentedTypes.size(); i++) {
								if (i > 0) {
									sb2.append(", ");
								}
								sb2.append(" with semType ");
								sb2.append(augmentedTypes.get(i));
							}
							sb2.append(")");
						}
						sb2.append(getCodeVariableDeclaration(methodName, cv));
					}
				}
				else {
					sb2.append("\"");
				}
				sb2.append(")");
				hasPriorTriple = true;
			}
		}
		
		if (saveOriginalScript() && originalLanguage != null && originalScript != null) {
			if (hasPriorTriple) {
				sb2.append(",\n");
			}
			sb2.append(" has expression (a Script with language ");
			sb2.append(originalLanguage);
			sb2.append(", with script \n\"");
			sb2.append(escapeDoubleQuotes(originalScript));
			sb2.append("\")");
			hasPriorTriple = true;
		}
		if (savePythonScript() && pythoncode != null) {
			if (hasPriorTriple) {
				sb2.append(",\n");
			}
			sb2.append(" has expression (a Script with language ");
			sb2.append(DialogConstants.PYTHON_LANGUAGE);
			sb2.append(", with script \n\"");
			sb2.append(escapeDoubleQuotes(pythoncode));
			sb2.append("\")");
			hasPriorTriple = true;
		}
		if (saveOtherPythonScripts()) {
			if (tfPythonCode != null) {
				if (hasPriorTriple) {
					sb2.append(",\n");
				}
				sb2.append(" has expression (a Script with language ");
				sb2.append(DialogConstants.TF_PYTHON_LANGUAGE);
				sb2.append(", with script \n\"");
				sb2.append(escapeDoubleQuotes(tfPythonCode));
				sb2.append("\")");
				hasPriorTriple = true;
			}
			if (npPythonCode != null) {
				if (hasPriorTriple) {
					sb2.append(",\n");
				}
				sb2.append(" has expression (a Script with language ");
				sb2.append(DialogConstants.NUMPY_PYTHON_LANGUAGE);
				sb2.append(", with script \n\"");
				sb2.append(escapeDoubleQuotes(npPythonCode));
				sb2.append("\")");
				hasPriorTriple = true;
			}
		}
		sb2.append(".\n");
		returnSadlStatements.add(sb2.toString());
		return returnSadlStatements;
	}

	// Method to get the augmented type, if possible, for an implicit variable
	private List<String> getAugmentedTypesOfVariable(Individual cv) throws InvalidNameException, ConfigurationException, ReasonerNotFoundException, QueryParseException, QueryCancelledException, AmbiguousNameException {
		if (augmentedTypesOfCodeVariableIsCached(cv)) {
			return getCachedAugmentedTypesOfCodeVariable(cv);
		}

		String queryUri = DialogConstants.CODE_EXTRACTION_MODEL_URI + "#VarComment";
		OntModel codeModel = getCodeExtractor().getCurrentCodeModel();
		Resource qrsrc = codeModel.getResource(queryUri);
		if (qrsrc != null) {
			StmtIterator qitr = codeModel.listStatements(qrsrc, RDFS.isDefinedBy, (RDFNode) null);
			if (qitr.hasNext()) {
				RDFNode obj = qitr.nextStatement().getObject();
				if (obj.isLiteral()) {
					String query =obj.asLiteral().getValue().toString();
					List<Object> pvals = new ArrayList<Object>();
					pvals.add(new NamedNode(cv.getURI()));
					String commentQuery = getInitializedCodeModelReasoner().parameterizeQuery(query, pvals);
					ResultSet commentRs =  getInitializedCodeModelReasoner().ask(commentQuery);
					if (commentRs != null) {
						// comment, line#, usage, ordered by line#
						for (int r = 0; r < commentRs.getRowCount(); r++) {
							String usage = commentRs.getResultAt(r, 2).toString();
							if (usage != null && !usage.equals("http://sadl.org/CodeExtractionModel.sadl#Used")) {
								String comment = commentRs.getResultAt(r, 0).toString();
//								System.out.println("Comment to be processed for augmented type for variable " + cv.getLocalName() + ": " + comment);
								try {
									List<Object> extractions = processExtractionFromText(comment, false);
									if (extractions != null) {
										OntClass uqcls = getDomainModel().getOntClass(SadlConstants.SADL_IMPLICIT_MODEL_UNITTEDQUANTITY_URI);
										if (extractions.size() == 5) {
											Object clses = extractions.get(3);
											if (clses instanceof Map<?,?>) {
												Iterator<?> clsitr = ((Map<?,?>)clses).keySet().iterator();
												if (clsitr != null && clsitr.hasNext()) {
													List<String> semTypes = new ArrayList<String>();
													while (clsitr.hasNext()) {
														Object key = clsitr.next();
														Object clslst = ((Map<?, ?>) clses).get(key);
														if (clslst instanceof List<?>) {
															int size = ((List<?>)clslst).size();
															for (int i = 0; i < size; i++) {
																String uri = ((List<?>)clslst).get(i).toString().trim();
																OntResource cls = getDomainModel().getOntClass(uri);
																if (cls != null && cls.canAs(OntClass.class)) {
																	try {
																		if (SadlUtils.classIsSubclassOf(cls.as(OntClass.class), uqcls, true, null)) {
																			String ln = checkForKeyword(getLocalName(uri));
																			if (!semTypes.contains(ln)) {
																				semTypes.add(ln);
																			}
																		}
																	} catch (CircularDependencyException e) {
																		// TODO Auto-generated catch block
																		e.printStackTrace();
																	}
																}
															}
														}
													}
													cacheAugmentedTypesOfCodeVariable(cv, semTypes);
													return semTypes;
												}
											}
											Object props = extractions.get(4);
											if (props instanceof Map<?,?>) {
												Iterator<?> propitr = ((Map<?,?>)props).keySet().iterator();
												if (propitr != null && propitr.hasNext()) {
													List<String> semTypes = new ArrayList<String>();
													while (propitr.hasNext()) {
														Object key = propitr.next();
														Object proplst = ((Map<?, ?>) props).get(key);
														if (proplst instanceof List<?>) {
															int size = ((List<?>)proplst).size();
															for (int i = 0; i < size; i++) {
																String uri = ((List<?>)proplst).get(i).toString().trim();
																String ln = checkForKeyword(getLocalName(uri));
																if (!semTypes.contains(ln)) {
																	semTypes.add(ln);
																}
															}
															cacheAugmentedTypesOfCodeVariable(cv, semTypes);
															return semTypes;
														}
													}
												}
											}
//											Object stmts = extractions.get(0);
//											String retVal = null;
//											if (stmts instanceof List<?>) {
//												int size = ((List<?>)stmts).size();
//												List<String> semTypes = new ArrayList<String>();
//												for (int i = 0; i < size; i++) {
//													String stmt = ((List<?>)stmts).get(i).toString();
//													if (stmt.startsWith("\"")) {
//														stmt = stmt.trim();
//													}
//													semTypes.add(stmt);
//												}
//												return semTypes;
//											}
										}
									}
								} catch (AnswerExtractionException e) {
									// OK to not find anything
								} catch (IOException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								} catch (ConfigurationException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
					}
				}
			}
		}
		cacheAugmentedTypesOfCodeVariable(cv, null);
		return null;
	}

	/**
	 * Method to get cached augmented types of a code variable extracted from the code comments
	 * @param cv
	 * @return
	 */
	private List<String> getCachedAugmentedTypesOfCodeVariable(Individual cv) {
		return cachedAugmentedTypesOfCodeVariable != null ? cachedAugmentedTypesOfCodeVariable.get(cv.getURI()) : null;
	}
	
	/**
	 * Method to determine if the augmented types of a code variable extracted from code comments has been cached
	 * @param cv
	 * @return -- true if cached (null could be cached) else false
	 */
	private boolean augmentedTypesOfCodeVariableIsCached(Individual cv) {
		if (cachedAugmentedTypesOfCodeVariable != null && cachedAugmentedTypesOfCodeVariable.containsKey(cv.getURI())) {
			return true;
		}
		return false;
	}

	/**
	 * Method to cache the augmented types of a code variable extracted from the code comments
	 * @param cv
	 * @param semTypes
	 */
	private void cacheAugmentedTypesOfCodeVariable(Individual cv, List<String> semTypes) {
		if (this.cachedAugmentedTypesOfCodeVariable == null) {
			this.cachedAugmentedTypesOfCodeVariable = new HashMap<String, List<String>>();
		}
		this.cachedAugmentedTypesOfCodeVariable.put(cv.getURI(), semTypes);
	}

	/**
	 * Method to get the Java declaration of a code variable, translate it to Python, 
	 * 	and add both as Script values of the declaration property to the code variable instance.
	 * @param methodName
	 * @param cv
	 * @return
	 */
	private String getCodeVariableDeclaration(String methodName, Individual cv) {
		StringBuilder sb = new StringBuilder(); 
		String jlbl = cv.getLabel("Java");
		if (jlbl != null) {
			sb.append("\n   with declaration (a Script with script \"");
			sb.append(jlbl);
			sb.append("\", with language Java)");
			// translate to Python and add script
			try {
				String pycode = getPyCode(cv);
				if (pycode == null) {
					pycode = getExtractionProcessor().translateExpressionJavaToPython("DummyClass", "DummyMethod", jlbl);
					storePyCode(cv, pycode);
				}
				if (pycode != null) {
					String commentBefore = "\"\"\" generated source for method DummyMethod \"\"\"";
					int idx = pycode.indexOf(commentBefore);
					if (idx > 0) {
						pycode = pycode.substring(idx + commentBefore.length()).trim();
					}
					sb.append("\n   with declaration (a Script with script \"");
					sb.append(pycode);
					sb.append("\", with language Python)");

					if (saveOtherPythonScripts() ) {
						String tfPythonCode = pythonToTensorFlowPython(pycode);
						String npPythonCode = pythonToNumPyPython(pycode);
						sb.append(",\n   with declaration (a Script with script \"");
						sb.append(tfPythonCode);
						sb.append("\", with language ");
						sb.append(getLocalNameFromUri(DialogConstants.TF_PYTHON_LANGUAGE));
						sb.append(")");
						sb.append(",\n   with declaration (a Script with script \"");
						sb.append(npPythonCode);
						sb.append("\", with language ");
						sb.append(getLocalNameFromUri(DialogConstants.NUMPY_PYTHON_LANGUAGE));
						sb.append(")");
					}
				}
			} catch (IOException cause) {
				if (cause instanceof ConnectException || cause instanceof UnknownHostException) {
					StringBuilder sbe = new StringBuilder(cause.getMessage());
					sbe.append(" to translate Java method '" + methodName + "' to Python. ");
					sbe.append(cause.getMessage());
					sbe.append(".");
					System.err.println(sbe.toString());
				}
				else {
					StringBuilder sbe = new StringBuilder(cause.getMessage());
					sbe.append(" to translate Java method '" + methodName + "' to Python. ");
					System.err.println(sbe.toString());
					cause.printStackTrace();
				}
			}
		}
		return sb.toString();
	}

	private void storePyCode(Individual cv, String pycode) {
		if (!variableDeclarations.containsKey(cv)) {
			variableDeclarations.put(cv, pycode);
		}
	}

	private String getPyCode(Individual cv) {
		if (variableDeclarations.containsKey(cv)) {
			return variableDeclarations.get(cv);
		}
		return null;
	}

	private String getInitializedClassName(List<String> initializerKeywords, String methodName) {
		if (initializerKeywords != null) {
			for (String ikw : initializerKeywords) {
				String lastSegment;
				int lastDot = methodName.lastIndexOf('.');
				if (lastDot > 0) {
					lastSegment = methodName.substring(lastDot+ 1); 
				}
				else {
					lastSegment = methodName;
				}
				if (lastSegment.startsWith(ikw)) {
					String rest = lastSegment.substring(ikw.length());
					// TODO if rest is the name of a domain class
					return rest;
				}
// This is commented out per request to not have constructors as initializer methods awc 4/22/2020	
//				else {
//					int firstDot = methodName.indexOf('.');
//					if (firstDot == lastDot) {	// there are only two dots
//						String firstSegment = methodName.substring(0, firstDot);
//						if (firstSegment.equals(lastSegment)) {
//							// this is a constructor, so it is an initializer
//							return firstSegment;
//						}
//					}
//				}
			}
		}
		return null;
	}

	private String nameToUri(String name) {
		// try for primitive data type
		try {
			if (name.endsWith("[]")) {
				name = name.substring(0, name.length() - 2);
			}
			Resource pdrsrc = JenaBasedSadlModelProcessor.primitiveDataTypeLocalnameToJenaResource(name);
			return pdrsrc.getURI();
		} catch (JenaProcessorException e) {
			// OK, not everything is primitive type
		}
//		OntModel om = getExtractionProcessor().getCodeModel();
		return getExtractionProcessor().getCodeExtractor().getClassUriFromSimpleName(name);
//			return JenaTranslatorPlugin.findNameNs(om, name);
	}

	private ResultSet getImplicitInputs(String methodName) throws InvalidNameException, ConfigurationException,
			ReasonerNotFoundException, QueryParseException, QueryCancelledException, AmbiguousNameException {
		String q = "select ?ivn ?ivt ?iv where {?ref <codeBlock> <";
		q += methodName.toString().trim();
		q += "> . ?ref <isImplicit> true . ?ref <cem:input> true . ";
		q += "?iv <reference> ?ref . ?iv <varName> ?ivn . ?iv <varType> ?ivt}";
		q = getInitializedCodeModelReasoner().prepareQuery(q);
		ResultSet qrs =  getInitializedCodeModelReasoner().ask(q);
		return qrs;
	}

	private ResultSet getImplicitOutputs(String methodName) throws InvalidNameException, ConfigurationException,
	ReasonerNotFoundException, QueryParseException, QueryCancelledException, AmbiguousNameException {
		String q = "select ?ivn ?ivt ?iv where {?ref <codeBlock> <";
		q += methodName.toString().trim();
		q += "> . ?ref <isImplicit> true . ?ref <output> true . ";
		q += "?iv <reference> ?ref . ?iv <varName> ?ivn . ?iv <varType> ?ivt}";
		q = getInitializedCodeModelReasoner().prepareQuery(q);
		ResultSet qrs =  getInitializedCodeModelReasoner().ask(q);
		return qrs;
	}
	
	/**
	 * Method to find typeLocalName as a class in the current or some imported ontology
	 * @param typeLocalName
	 * @return
	 */
	private boolean typeFoundInSomeModel(String typeLocalName) {
		List<String> primitiveTypes = new ArrayList<String>(Arrays.asList("string", "boolean", "decimal", "int", "long", "float", "double",
				"duration", "dateTime", "time", "date", "gYearMonth", "gYear", "gMonthDay", "gDay", "gMonth", "hexBinary",
				"base64Binary", "anyURI", "integer", "negativeInteger", "nonNegativeInteger", "positiveInteger",
				"nonPositiveInteger", "byte", "unsignedByte", "unsignedInt", "anySimpleType", "data", "class"));
		if (primitiveTypes.contains(typeLocalName)) {
			return true;
		}
		if (findOntModelResourceWithMatchingLocalname(typeLocalName, null, null) != null) {
			return true;
		}
		return false;
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
					OwlToSadl ots = new OwlToSadl(mdl, mdlName);
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
						notifyUser(getConfigurationManager().getModelFolder(), "Failed to save " + outputOwlFile.getName() + " as SADL file.", true);
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
		return arg1.toString().equalsIgnoreCase("yes") || arg1.toString().equalsIgnoreCase("yes.");
	}
	
	public void notifyUser(String modelFolder, String msg, boolean quote) throws ConfigurationException {
		if (getExtractionContext() != null) {
			answerUser(modelFolder, msg, quote, getExtractionContext().getHostEObject());
		}
		else {
			if (quote) {
				msg = doubleQuoteContent(msg);
			}
			InformationContent ic = new InformationContent(null, Agent.CM, msg);
			notifyUser(modelFolder, ic, quote);
		}
	}
	
	/**
	 * Method to call to put Statements into the Dialog window.
	 * @param codeModelFolder
	 * @param sc
	 * @param quote
	 */
	public void notifyUser(String codeModelFolder, StatementContent sc, boolean quote) {
		if (sc.getHostEObject() == null) {
			if (getExtractionContext() != null) {
				sc.setHostEObject(getExtractionContext().getHostEObject());
			}
		}
		else {
			sc.setHostEObject(getConversationHostObject(sc.getHostEObject()));
		}
		if (getDialogAnswerProvider(getResourceUri()) != null) {
			// talk to the user via the Dialog editor
			Method acmic = null;
			try {
				acmic = getDialogAnswerProvider(getResourceUri()).getClass().getMethod("addCurationManagerInitiatedContent", AnswerCurationManager.class, StatementContent.class);
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (acmic != null) {
				acmic.setAccessible(true);
				try {
					acmic.invoke(getDialogAnswerProvider(getResourceUri()), this, sc);
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
	 * Method to call the DialogAnserProvider and replace the first text with the second text in the context of the EObject
	 * @param equationEObject
	 * @param eqTxt
	 * @param replacementTxt
	 */
	private void replaceDialogText(EObject eObject, String originalTxt, String replacementTxt) {
		if (getDialogAnswerProvider(getResourceUri()) != null) {
			// talk to the user via the Dialog editor
			Method acmic = null;
			try {
				acmic = getDialogAnswerProvider(getResourceUri()).getClass().getMethod("replaceDialogText", AnswerCurationManager.class, EObject.class, String.class, String.class);
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (acmic != null) {
				acmic.setAccessible(true);
				try {
					acmic.invoke(getDialogAnswerProvider(getResourceUri()), this, getConversationHostObject(eObject), originalTxt, replacementTxt);
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
	 * Method to ask the user a question. (Not clear yet if it is different from ansewrUser.)
	 * @param modelFolder
	 * @param question
	 * @param quote
	 * @param ctx
	 * @return
	 * @throws ConfigurationException 
	 */
	public String askUser(String modelFolder, String question, boolean quote, EObject ctx) throws ConfigurationException {
		return answerUser(modelFolder, question, quote, ctx);
	}

	/**
	 * Method to answer a user question by displaying a statement, which may be quoted. Requires the EObject with which it is associated
	 * @param modelFolder
	 * @param msg
	 * @param quote
	 * @param ctx
	 * @return
	 * @throws ConfigurationException
	 */
	public String answerUser(String modelFolder, String msg, boolean quote, EObject ctx) throws ConfigurationException {
		if (quote) {
			msg = doubleQuoteContent(msg);
		}
		if (getDialogAnswerProvider(getResourceUri()) != null) {
			// talk to the user via the Dialog editor
			Method acmic = null;
			try {
				acmic = getDialogAnswerProvider(getResourceUri()).getClass().getMethod("addCurationManagerAnswerContent", AnswerCurationManager.class, String.class, Object.class);
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (acmic == null) {
				Method[] dapMethods = getDialogAnswerProvider(getResourceUri()).getClass().getDeclaredMethods();
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
					Object retval = acmic.invoke(getDialogAnswerProvider(getResourceUri()), this, msg, getConversationHostObject(ctx));
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
//			logger.debug(numRead);
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
	public boolean dialogAnserProviderInitialized(org.eclipse.emf.ecore.resource.Resource resource) {
		if (dialogAnswerProvider == null) {
			setDialogAnswerProvider((IDialogAnswerProvider) getConfigurationManager().getPrivateKeyMapValueByResource(DialogConstants.DIALOG_ANSWER_PROVIDER, resource.getURI()));
		}
		return (dialogAnswerProvider != null);
	}

	protected IDialogAnswerProvider getDialogAnswerProvider(URI resourceUri) {
		if (dialogAnswerProvider == null) {
			IDialogAnswerProvider dapFound = (IDialogAnswerProvider) getConfigurationManager().getPrivateKeyMapValueByResource(DialogConstants.DIALOG_ANSWER_PROVIDER, resourceUri);
			if (dapFound != null) {
//				org.eclipse.emf.ecore.resource.Resource dapRsrc = dapFound.getResource();
				if (dapFound.getUri().equals(resourceUri)) {
//					setResource((XtextResource) dapRsrc);
					setDialogAnswerProvider(dapFound);
					if (dialogAnswerProvider == null) {
						setDialogAnswerProvider(new DialogAnswerProviderConsoleForTest());
					}
				}
			}
			else if (resourceUri == null || ResourceManager.isSyntheticUri(null, resourceUri)) {
				// this looks like a backend JUnit test so provide a console
				setDialogAnswerProvider(new DialogAnswerProviderConsoleForTest());
			}
		} else if (dialogAnswerProvider instanceof DialogAnswerProviderConsoleForTest) {
			if (resourceUri != null && !ResourceManager.isSyntheticUri(null, resourceUri)) {
				// this doesn't look like a backend JUnit test so get a real DAP
				IDialogAnswerProvider provider = (IDialogAnswerProvider) getConfigurationManager().getPrivateKeyMapValueByResource(DialogConstants.DIALOG_ANSWER_PROVIDER, resource.getURI());
				if (provider != null && !(provider instanceof DialogAnswerProviderConsoleForTest)) {
					dialogAnswerProvider.dispose(); // Dispose the current, console-based answer provider.
					setDialogAnswerProvider(provider); // Updated with the`document`-aware dialog provider.
				}
			}
		}
		return dialogAnswerProvider;
	}

	protected void setDialogAnswerProvider(IDialogAnswerProvider dialogAnswerProvider) {
		this.dialogAnswerProvider = dialogAnswerProvider;
	}
	
	public Object[] buildCGModel(String baseServiceUri, String modelUri, String equationModel, String dataLocation, List<String[]> inputs, List<String[]> outputs) throws IOException {
		String serviceURL = getPreference(DialogPreferences.ANSWER_KCHAIN_CG_SERVICE_BASE_URI.getId());
		KChainServiceInterface kcsi = new KChainServiceInterface(serviceURL);
		return kcsi.buildCGModel(modelUri, equationModel, dataLocation, inputs, outputs);
	}

	private IReasoner getInitializedCodeModelReasoner() throws ConfigurationException, ReasonerNotFoundException {
		if (codeModelReasoner == null) {
			codeModelReasoner = getExtractionProcessor().getCodeExtractor().getCodeModelConfigMgr().getReasoner();
			String codeModelFolder = getOwlModelsFolder();
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
			conversation = new DialogContent(getDialogAnswerProvider(getResourceUri()).getResource(), this);
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
	
	public String processUserRequest(org.eclipse.emf.ecore.resource.Resource resource, ExpectsAnswerContent sc) 
			throws ConfigurationException, ExecutionException, IOException, TranslationException, InvalidNameException, 
				ReasonerNotFoundException, QueryParseException, QueryCancelledException, SadlInferenceException, EquationNotFoundException, AnswerExtractionException, AmbiguousNameException {
		String retVal;
		if (sc instanceof WhatIsContent) {
    		retVal = processWhatIsContent(resource, (WhatIsContent) sc);
    	}
		else if (sc instanceof ModifiedAskContent) {
			retVal = processModifiedAsk(resource, (ModifiedAskContent) sc);
		}
		else if (sc instanceof WhatValuesContent) {
			retVal = processWhatValuesContent(resource,(WhatValuesContent)sc);
		}
		else if (sc instanceof HowManyValuesContent) {
			retVal = processHowManyValue(resource, (HowManyValuesContent)sc);
		}
		else if (sc instanceof SaveContent) {
			retVal = processSaveRequest(resource, (SaveContent)sc);
		}
		else if (sc instanceof EvalContent) {
			retVal = processEvalRequest(resource, (EvalContent)sc);
		}
		else if (sc instanceof ExtractContent) {
			retVal = processExtractRequest(resource, (ExtractContent)sc);
		}
		else if (sc instanceof CompareContent) {
			retVal = processCompareRequest(resource,(CompareContent)sc);
		}
		else if (sc instanceof LongTaskContent) {
			// first action: this will last a long time so do something to notify user that it is in progress
			int t = ((LongTaskContent)sc).getTime();
			int telapsed = 0;
			while (telapsed < t) {
				try {
					System.out.println("sleeping for 500 ms");
					Thread.sleep(500);
				}
				catch (InterruptedException e) {
					System.out.println(e);
				}
				telapsed += 500;
			}
			retVal = "Task took " + telapsed + " ms";
			answerUser(getOwlModelsFolder(), retVal, true, sc.getHostEObject());	
			// last action: task is complete so remove user notification (cancel effect of first action)
		}
		else if (sc instanceof AddEquationContent) {
			String lhs = ((AddEquationContent)sc).getLhs();
			String eqStr = ((AddEquationContent)sc).getSadlEqStr();
			String eqName = ((AddEquationContent)sc).getEquationName();
			answerUser(getOwlModelsFolder(), eqStr, ((StatementContent)sc).isQuoteResult(), sc.getHostEObject());
			if (((AddEquationContent)sc).getEquationBody() != null) {
				// translate to Python using texttotriples service
				String toTranslate = lhs + " = " + ((AddEquationContent)sc).getEquationBody();
				try {
					getTextProcessor().clearGraph(getLocalityURI());
					OntModel m = getOntModelFromText("user-added-equation", toTranslate, getLocalityURI(), getDomainModelName(), getLocalityPrefix(), false, false);
					if (m != null) {
						List<String> scriptStatements = getScriptStatementsFromTextService(getLocalityURI(), m, eqName);
						if (scriptStatements != null) {
							for (String scptstmt : scriptStatements) {
								answerUser(getOwlModelsFolder(), scptstmt, false, sc.getHostEObject());
							}
						}
					}
					else {
						throw new AnswerExtractionException("Text processor failed to return an OWL model.");
					}
				}
				catch (Throwable t) {
					retVal = t.getMessage();
				}
			}
			retVal = eqStr;
		}
		else if (sc instanceof AddAugmentedTypeInfoContent) {
			retVal = addAugmentedTypeToEquation((AddAugmentedTypeInfoContent)sc);
		}
		else {
			logger.debug("Need to add '" + sc.getClass().getCanonicalName() + "' to processUserRequest");
			retVal = "Not yet implemented";
		}
		return retVal;
	}
	
	private List<String> getScriptStatementsFromTextService(String localityUri, OntModel m, String eqName) throws IOException {
		if (m != null) {
			List<String> results = new ArrayList<String>();
			StmtIterator eqnItr = m.listStatements(null, RDF.type, m.getOntClass(SadlConstants.SADL_IMPLICIT_MODEL_EXTERNAL_EQUATION_CLASS_URI));
			StringBuilder sb = new StringBuilder();
			sb.append(eqName);
			int cntr = 0;
			while (eqnItr.hasNext()) {
				Resource eqInst = eqnItr.nextStatement().getSubject();
				StmtIterator exprItr = m.listStatements(eqInst, m.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_EXPRESSTION_PROPERTY_URI), (RDFNode)null);
				while (exprItr.hasNext()) {
					RDFNode exprInst = exprItr.nextStatement().getObject();
					if (exprInst.isResource()) {
						Statement lstmt = m.getProperty(exprInst.asResource(), m.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_LANGUAGE_PROPERTY_URI));
						if (lstmt != null) {
							String language = lstmt.getObject().asResource().getLocalName();
							Statement scrptstmt = m.getProperty(exprInst.asResource(), m.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_SCRIPT_PROPERTY_URI));
							if (scrptstmt != null) {
								String scrpt = scrptstmt.getObject().asLiteral().getString();
								if (scrpt != null) {
									if (cntr++ > 0) {
										sb.append(", ");
									}
									sb.append(System.lineSeparator());
									sb.append(" has expression (a Script with language ");
									sb.append(language);
									sb.append(", with script \"");
									sb.append(scrpt);
									sb.append("\"");
									sb.append(")");
								}
							}
						}
					}
				}
			}
			sb.append(".");
			results.add(sb.toString());
			return results;
		}
		return null;
	}

	private String processCompareRequest(org.eclipse.emf.ecore.resource.Resource resource2, 
			CompareContent sc) throws AnswerExtractionException, ExecutionException, SadlInferenceException, TranslationException, ConfigurationException {
		if (ResourceManager.isSyntheticUri(null, resource2.getURI())) {
			notifyUser(null, "Can't process comparison request with a synthetic resource", true);
			return null;
		}
		else if (sc != null && sc.getComparisonRules() != null) {
			List<Rule> comparisonRules = sc.getComparisonRules();
			Object[] rss = null;
			try {
				rss = insertRulesAndQuery(resource2, comparisonRules);
			} catch (SadlInferenceException e) {
				if (e.getCause() instanceof NoModelFoundForTargetException) {
					return handleNoModelFoundForTargetException(sc, (NoModelFoundForTargetException) e.getCause());
				}
				else {
					throw e;
				}
			}
			return processResultsOfInsertRulesAndQuery(sc, rss, comparisonRules);
//			return "Processing of query disabled";
		}
		throw new AnswerExtractionException("Invalid comparison request inputs");
	}

	private String processExtractRequest(org.eclipse.emf.ecore.resource.Resource resource2, 
			ExtractContent sc) throws MalformedURLException, IOException, ConfigurationException, AnswerExtractionException {
		String returnStatus = "";
		getExtractionProcessor().reset();
		while (sc != null) {
			String scheme = sc.getScheme();
			String source = sc.getScheme();
			if ((scheme != null && scheme.equals("text")) || (source != null && source.equals("text"))) {
				// actual text from which to extract has been provided
				returnStatus += processExtractionFromActualTextRequest(sc);
				sc = sc.getNextExtractContent();
				continue;
			}
	
			String content = null;
			String outputModelName;
			String prefix;
			if (scheme != null && scheme.equals("file")) {
				SadlUtils su =  new SadlUtils();
				File f = new File(su.fileUrlToFileName(sc.getUrl()));
				content =su.fileToString(f);
				outputModelName = getModelNameFromInputFile(f);
				prefix = getModelPrefixFromInputFile(f);
				
				String destPath = (new File(getOwlModelsFolder()).getParent() + "/" + DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT + "/Sources/" + f.getName());
				File dest = new File(destPath);
				if (!f.equals(dest)) {
					if (dest.exists()) {
						dest.delete();
					}
					if (!dest.exists()) {
						dest.getParentFile().mkdirs();
						Files.copy(f, dest);
		//				file.createLink(location, IResource.NONE, null);
					}
				}
			}
			else {
				if (resource2 instanceof XtextResource) {
					NetworkProxyConfigurator proxyConfigurator = ((XtextResource) resource2).getResourceServiceProvider().get(NetworkProxyConfigurator.class);
					if (proxyConfigurator != null) {
						proxyConfigurator.configureProxies();
					}
				}
				content = downloadURL(sc.getUrl());
				outputModelName = getModelNameFromInputUrl(sc.getUrl());
				prefix = getModelPrefixFromInputUrl(sc.getUrl());
			}
	//		System.out.println(content);	
			setExtractionContext(sc);
	
			if (sc.getUrl().endsWith(".java")) {
				// code extraction
				String outputOwlFileName = prefix + ".owl";
				try {
					if (getSuspendedExtractionContent() == null) {
						File of = extractFromCodeAndSave(content, sc.getUrl(), outputModelName, prefix, outputOwlFileName);
						if (of != null) {
							String aggregatedComments = getExtractionProcessor().getCodeExtractor().getAggregatedComments();
							String inputIdentifier = "from comments";
							String modelPrefix = "cmts";
							OntModel commentModel;
							
							boolean useDomainOntologyService = true;
							if (useDomainOntologyService ) {
								commentModel = getDomainOntologyFromText(inputIdentifier, aggregatedComments, getLocalityURI(), getDomainModelName(), modelPrefix, false, false);
							}
							else {
								commentModel = getOntModelFromText(inputIdentifier, aggregatedComments, getLocalityURI(), getDomainModelName(), modelPrefix, false, false);
							}
							if (commentModel != null) {
								// find concepts that need to be added to the ontology. 
								// Insert them before the extraction and abort the extraction, leaving it to be handled with the new domain ontology
								List<String> newSadlStatements = getNewOntologyConceptsAsSadlStatements(commentModel, useDomainOntologyService);
								if (newSadlStatements != null) {
									for (int i = 0; i < newSadlStatements.size(); i++) {
										String sd = newSadlStatements.get(i);
										EObject host = getExtractionContext() != null ? getExtractionContext().getHostEObject() : null;
										SadlStatementContent ssc = new SadlStatementContent(host, Agent.CM, sd);
										notifyUser(getOwlModelsFolder(), ssc, false);
										getExtractionProcessor().addNewSadlContent(sd);
									}
									clearDomainModelForTextService();			// we'll have new model content so clear
									saveSuspendedExtractionContent(content);	// save the extraction state for resuming with new domain model content
									saveSuspendedCodeModel(getExtractionProcessor().getCodeModel());
									setSuspendConversationProcessing(true);
									return SUSPENDED_EXTRACTION_PENDING_DOMAIN_MODEL_ADDITIONS;
								}
							}
						}
					}
					else {
						getExtractionProcessor().setCodeModel(getSuspendedCodeModel());
					}
					returnStatus += continueExtraction(content);
					saveSuspendedExtractionContent(null);
					saveSuspendedCodeModel(null);
				}
				catch (Throwable t) {
					t.printStackTrace();
				}
			}
			else {
				if (sc.getUrl().endsWith(".html")) {
	//				Source src = new Source(content);
	//				Renderer rndrr = src.getRenderer();
	//				rndrr.setNewLine(System.lineSeparator());
	//				rndrr.setIncludeHyperlinkURLs(false);
	//				rndrr.setConvertNonBreakingSpaces(true);
	//				content = rndrr.toString();
	//				content = new Source(content).getRenderer().toString();
	//				System.out.println(content);
					throw new AnswerExtractionException("HTML files not currently supported");
				}
				//text extraction
				String outputOwlFileName = prefix + ".owl";
				try {
					File of = extractFromTextAndSave(content, sc.getUrl(), outputModelName, outputOwlFileName);
					String owlFileForDisplay = "file:///" + of.getCanonicalPath().replace("\\", "/");
					returnStatus += "Saved extracted model to OWL '" + owlFileForDisplay + "'";
					answerUser(getOwlModelsFolder(), returnStatus, true, sc.getHostEObject());	
					processExtractedText(outputModelName, outputOwlFileName, SaveAsSadl.DoNotSaveAsSadl);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
					returnStatus += e.getMessage();
				} 
	
			}
			clearExtractionContext();
			sc = sc.getNextExtractContent();
		}
		return returnStatus;
	}

	private String continueExtraction(String content)
			throws IOException, ConfigurationException, ReasonerNotFoundException, InvalidNameException,
			QueryParseException, QueryCancelledException, InvalidInputException, AmbiguousNameException {
		String outputOwlFileName;
		boolean useAllCodeExtractedMethods = true;
		SaveAsSadl saveAsSadl = SaveAsSadl.DoNotSaveAsSadl;
		
		// run inference on the model, interact with user to refine results
		String queryString = useAllCodeExtractedMethods ? SparqlQueries.ALL_CODE_EXTRACTED_METHODS : SparqlQueries.INTERESTING_METHODS_DOING_COMPUTATION;	//?m ?b ?e ?s
		ResultSet results = runInferenceFindInterestingCodeModelResults(queryString, saveAsSadl, content);
		if (results == null || results.getRowCount() == 0) {
			notifyUser(getOwlModelsFolder(), "No equations were found in this extraction from code.", true);
		}
		else {
			equationsFromCodeResultSetToSadlContent(results, getOwlModelsFolder(), content);
		}
		return "Suspended extraction completed";
	}
	
	private List<String> getNewOntologyConceptsAsSadlStatements(OntModel ttoModel, boolean fromDomainOntologyService) {
		if (verboseExtraction()) {
			if (fromDomainOntologyService) {
				System.out.println("Model domain model additions extracted from textToOntology service");
			}
			else {
				System.out.println("Model domain model additions extracted from text2Triples service");
			}
			ttoModel.write(System.err, "N3");
		}
		List<String> results = null;
		if (ttoModel != null) {
			OntModel dm = getDomainModel();
			if (fromDomainOntologyService) {
				// from textToOntology service
				ExtendedIterator<OntClass> clsitr = ttoModel.listClasses();
				while (clsitr.hasNext()) {
					boolean alreadyDomainConcept = false;
					String superClsStr = null;
					String unitstr = null;
					
					String pstr = null;
					String rngstr = null;
					StringBuilder sb = new StringBuilder();
					OntClass cls = clsitr.next();
					String clsstr = checkForKeyword(cls.getLocalName());
					if (dm.getOntResource(getDomainModelName() + "#" + cls.getLocalName()) != null) {
						if (verboseExtraction()) System.out.println(clsstr + " is already a domain concept");
						alreadyDomainConcept = true;
					}
					else {
						ExtendedIterator<Ontology> ontitr = dm.listOntologies();
						while (ontitr.hasNext()) {
							Ontology ont = ontitr.next();
							if (dm.getOntResource(ont.getURI() + "#" + cls.getLocalName()) != null) {
								if (verboseExtraction()) System.out.println(clsstr + " is already a domain concept");
								alreadyDomainConcept = true;
								break;
							}
						}
					}
					StmtIterator superitr = ttoModel.listStatements(cls, RDFS.subClassOf, (RDFNode)null);
					if (superitr.hasNext()) {
						while (superitr.hasNext()) {
							RDFNode supercls = superitr.nextStatement().getObject();
							if (supercls.isResource() && supercls.asResource().isURIResource()) {
								superClsStr = checkForKeyword(supercls.asResource().getLocalName());
								if (verboseExtraction()) System.out.println(clsstr + " is a type of " + superClsStr);
								if (supercls.asResource().getURI().equals(SadlConstants.SADL_IMPLICIT_MODEL_UNITTEDQUANTITY_URI)) {
									StmtIterator unitItr = ttoModel.listStatements(cls, ttoModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_UNIT_URI), (RDFNode)null);
									if (unitItr.hasNext()) {
										while (unitItr.hasNext()) {
											RDFNode unit = unitItr.nextStatement().getObject();
											if (unit.isLiteral()) {
												unitstr = unit.asLiteral().getValue().toString();
												if (verboseExtraction()) System.out.println("unit of " + clsstr + " always has value \"" + unitstr + "\"");
											}
										}
									}
								}
							}
						}
					}
					else {
						if (verboseExtraction()) System.out.println(clsstr + " is a class");
					}
					StmtIterator propItr = ttoModel.listStatements(null, RDFS.domain, cls);
					while (propItr.hasNext()) {
						Resource p = propItr.nextStatement().getSubject();
						pstr = checkForKeyword(p.getLocalName());
						StmtIterator rngItr = ttoModel.listStatements(p, RDFS.range, (RDFNode)null);
						while (rngItr.hasNext()) {
							RDFNode rng = rngItr.next().getObject();
							if (rng.isURIResource()) {
								rngstr = checkForKeyword(rng.asResource().getLocalName());
								if (verboseExtraction()) System.out.println(clsstr + " is described by " + pstr + " with values of type " + rngstr);
							}
						}
					}
					if (!alreadyDomainConcept) {
// TODO this could be more sophisticated by looking to see if this model has more information about the class than exists in the domain ontology.
						sb.append(clsstr);
						if (superClsStr == null) {
							sb.append(" is a class");
						}
						else {
							sb.append(" is a type of ");
							sb.append(superClsStr);
						}
						if (pstr != null) {
							sb.append(", described by ");
							sb.append(pstr);
							if (rngstr != null) {
								sb.append(" with values of type ");
								sb.append(rngstr);
							}
						}
						sb.append(".");
						if (unitstr != null) {
							sb.append(System.lineSeparator());
							sb.append("unit of ");
							sb.append(clsstr);
							sb.append(" always has value \"");
							sb.append(unitstr);
							sb.append("\".");
						}
						if (results == null) {
							results = new ArrayList<String>();
							results.add("\"The following domain ontology contribution was extracted from code comments:\"");
						}
						results.add(sb.toString());
					}
				}
					
			}
			else {
				// from text2triples service
				// find all of the triples with seeAlso as property
				StmtIterator saitr = ttoModel.listStatements(null, RDFS.seeAlso, (RDFNode)null);
				while (saitr.hasNext()) {
					Statement sastmt = saitr.nextStatement();
					Resource subj = sastmt.getSubject();
					if (!subj.isURIResource()) {
						// only propose named Concepts for the domain ontoloyg
						break;
					}
					StmtIterator mcpitr = ttoModel.listStatements(subj, ttoModel.getProperty(MATCHINGCLASS_PROPERTY_URI), (RDFNode)null);
					if (mcpitr.hasNext()) {
						// there's a matchingClass so this is already in the domain ontology
						break;
					}
					StmtIterator mppitr = ttoModel.listStatements(subj, ttoModel.getProperty(MATCHINGPROPERTY_PROPERTY_URI), (RDFNode)null);
					if (mppitr.hasNext()) {
						// there's a metchingProperty so this is already in the domain ontology
						break;
					}
					if (results == null) {
						results = new ArrayList<String>();
						results.add("\"The following classes are suggested for the domain model:\"");
					}
					String subjStr = subj.getLocalName();
					subjStr = subjStr.substring(0,1).toUpperCase() + subjStr.substring(1);
					StringBuilder sb = new StringBuilder();
					sb.append(checkForKeyword(subjStr));
					sb.append(" (see \"");
					sb.append(checkForKeyword(sastmt.getObject().asResource().getURI()));
					sb.append("\")");
					sb.append(" is a type of ScientificConcept.");
					results.add(sb.toString());
				}
			}
		}
		return results;
	}

	/**
	 * Method to extract from provided text and convert to semantic type or statement.
	 * @param txt
	 * @param bAsCompleteStatement
	 * @return
	 * @throws AnswerExtractionException
	 * @throws IOException
	 * @throws ConfigurationException
	 */
	private List<Object> processExtractionFromText(String txt, boolean bAsCompleteStatement) throws AnswerExtractionException, IOException, ConfigurationException {
		String prefix = "temp";
		OntModel m = getOntModelFromText("from text", txt, getLocalityURI(), getDomainModelName(), prefix, false, false);
		if (m != null) {
//			m.write(System.out, "N3");
			List<Object> extracts = retrieveExtractedConcepts(m, bAsCompleteStatement, false);
			return extracts;
		}
		return null;
	}

	private String processExtractionFromActualTextRequest(ExtractContent sc) throws AnswerExtractionException, IOException, ConfigurationException {
		String retVal = "";
		setExtractionContext(sc);
		String toTranslate = sc.getUrl();
		try {
			OntModel m = getOntModelFromText("in-line text", toTranslate, getLocalityURI(), getDomainModelName(), getLocalityPrefix(), false, false);
			if (m != null) {
				List<Object> extracts = null;
				// get the concept(s), if they aren't in the domain model?
				extracts = retrieveExtractedConcepts(m, true, false);
				StringBuilder sb = new StringBuilder();
				if (extracts != null) {
					if (extracts.size() == 5) {
						Object statements = extracts.get(0);
						if (statements instanceof List<?>) {
							for (Object ex : (List<?>)statements) {
								notifyUser(getOwlModelsFolder(), ex.toString(), false);
								if (sb.length() > 0) {
									sb.append(System.lineSeparator());
								}
								sb.append(ex);							
							}
						}
					}
					setCurrentTextExtractionExtracts(extracts);
				}
				retVal += sb.toString();
			}
			// get the equation(s).
			String rememberDomainModelName = getDomainModelName();
			try {
				retVal += getEquationNamesFromTextServiceResults(m, getDomainModelName());
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				retVal += e.getMessage();
			}
			finally {
				setDomainModelName(rememberDomainModelName);
			}
		} catch (Throwable t) {
			notifyUser(getOwlModelsFolder(), t.getMessage(), true);
			return t.getMessage();
		}
		return retVal;
	}

	private void setCurrentTextExtractionExtracts(List<Object> extracts) {
		this.currentTextExtractionExtracts = extracts;
	}
	
	/**
	 * Method to get the results of the latest text extraction as a List with elements:
		0: Dialog window statements from extraction,
		1: labels, 
		2: seeAlsos, 
		3: matchingClasses, 
		4: matchingProperties

	 * @return
	 */
	private List<Object> getCurrentTextExtractionExtracts() {
		return this.currentTextExtractionExtracts;
	}

	/**
	 * Method to pull concepts from text extraction model and map them according to the subject
	 * @param m
	 * @param bAsCompleteStatements
	 * @param bInferPropOfSubj
	 * @return -- list of 0) Dialog window statement, 1) labels, 2) seeAlsos, 3) matchingClasses, 4) matchingProperties
	 * @throws ConfigurationException
	 */
	private List<Object> retrieveExtractedConcepts(OntModel m, boolean bAsCompleteStatements, boolean bInferPropOfSubj) throws ConfigurationException {
		StringBuilder sb = new StringBuilder();
		// get everything with a label
		int cntr = 0;
		Property mathcingClassProperty = m.getProperty(MATCHINGCLASS_PROPERTY_URI);
		Property matchingPropertyProperty = m.getProperty(MATCHINGPROPERTY_PROPERTY_URI);
		List<String> returnStatements = new ArrayList<String>();
		if (mathcingClassProperty == null || matchingPropertyProperty == null) {
			String msg = "\"Unable to find matching properties in SADL Implicit Model\"";
			returnStatements.add(msg);
		}
		Map<String, List<Object>> labels = new HashMap<String, List<Object>>();
		Map<String, List<Object>> seeAlsos = new HashMap<String, List<Object>>();
		Map<String, List<Object>> matchingClasses = new HashMap<String, List<Object>>();
		Map<String, List<Object>> matchingProperties = new HashMap<String, List<Object>>();
		StmtIterator lblitr = m.listStatements(null, RDFS.label, (RDFNode)null);
		while (lblitr.hasNext()) {
			Statement lblstmt = lblitr.nextStatement();
			Resource subj = lblstmt.getSubject();
			String subjUri = subj.isURIResource() ? subj.getURI() : null;
			if (subjUri != null) {
				String subjLocalName = subj.isURIResource() ? subj.getLocalName() : null;
				String lbl = lblstmt.getObject().asLiteral().getValue().toString();
				addExtractToMap(labels, subjUri, lbl);
				StmtIterator seeAlsoItr = m.listStatements(subj, RDFS.seeAlso, (RDFNode)null);
				boolean needAnotherComma = false;
				while (seeAlsoItr.hasNext()) {
					RDFNode seeAlso = seeAlsoItr.nextStatement().getObject();
					if (seeAlso.isURIResource()) {
						Resource seeAlsoUri = seeAlso.asResource();
						addExtractToMap(seeAlsos, subjUri, seeAlsoUri);
					}
				}
				StmtIterator matchClassItr = m.listStatements(subj, mathcingClassProperty, (RDFNode)null);
				if (matchClassItr.hasNext()) {
					List<String> classesToCreate = null;
					while (matchClassItr.hasNext()) {
						RDFNode mc = matchClassItr.nextStatement().getObject();
						if (mc.isURIResource()) {
							if (mc.canAs(OntClass.class)) {
								addExtractToMap(matchingClasses, subjUri, mc.as(OntClass.class));
							}
							else {
								if (classesToCreate == null) {
									classesToCreate = new ArrayList<String>();
								}
								classesToCreate.add(mc.asResource().getURI());
							}
						}
					}
					if (classesToCreate != null) {
						for (String ccuri : classesToCreate) {
							addExtractToMap(matchingClasses, subjUri, m.createClass(ccuri));
						}
					}
				}
				StmtIterator matchPropItr = m.listStatements(subj, matchingPropertyProperty, (RDFNode)null);
				if (matchPropItr.hasNext()) {
					List<String> propsToCreate = null;
					while (matchPropItr.hasNext()) {
						RDFNode mp = matchPropItr.nextStatement().getObject();
						if (mp.isURIResource()) {
							if (mp.canAs(Property.class)) {
								addExtractToMap(matchingProperties, subjUri, mp.as(Property.class));
							}
							else {
								if (propsToCreate == null) {
									propsToCreate = new ArrayList<String>();
								}
								propsToCreate.add(mp.asResource().getURI());
							}
						}
					}
					if (propsToCreate != null) {
						for (String pcuri : propsToCreate) {
							addExtractToMap(matchingProperties, subjUri, m.createProperty(pcuri));
						}
					}					
				}
			}
		}

		List<Object> matchingClassList = null;
		boolean multipleClassMatches = false;
		List<Object> matchingPropList = null;
		boolean multiplePropertyMatches = false;
		if (matchingClasses.size() > 0) {
			Iterator<String> keyItr = matchingClasses.keySet().iterator();
			while (keyItr.hasNext()) {
				String key = keyItr.next();
				matchingClassList = matchingClasses.get(key);
				if (keyItr.hasNext() || matchingClassList.size() > 1) {
					multipleClassMatches = true;
					break;
				}
			}
		}

		if (matchingProperties.size() > 0) {
			Iterator<String> keyItr = matchingProperties.keySet().iterator();
			while (keyItr.hasNext()) {
				String key = keyItr.next();
				matchingPropList = matchingProperties.get(key);
				if (keyItr.hasNext() || matchingPropList.size() > 1) {
					multiplePropertyMatches = true;
					break;
				}
			}
		}

		Resource theClass = null;
		Property theProperty = null;
		if (matchingPropList != null && !multiplePropertyMatches) {
			// we have a single property match
			Object mp = matchingPropList.get(0);
			if (mp instanceof Resource && ((Resource)mp).canAs(Property.class)) {
				theProperty = ((Resource)mp).as(Property.class);
			}
		}
		if (matchingClassList != null && !multipleClassMatches) {
			// we have a single class match
			Object cls = matchingClassList.get(0);
//			if (cls instanceof Resource && ((Resource)cls).canAs(OntClass.class) && ((Resource) cls).asResource().isURIResource()) {
			if (cls instanceof Resource && ((Resource) cls).asResource().isURIResource()) {
				theClass = (Resource)cls;
			}
		}
		if (theProperty != null) {
			StmtIterator rngItr = getDomainModel().listStatements(theProperty, RDFS.range, (RDFNode)null);
			if (rngItr.hasNext()) {
				Resource rng = rngItr.nextStatement().getObject().asResource();
				if (bInferPropOfSubj && (theClass == null || (rng != null && theClass.equals(rng)))) {
					// we have a property and we don't have a class  or
					// we have a class and the class is the range of the property
					// get the domain and do <property> of <domain> as the semantic type
					StmtIterator dmnItr = getDomainModel().listStatements(theProperty, RDFS.domain, (RDFNode)null);
					if (dmnItr.hasNext()) {
						Resource dmn = dmnItr.nextStatement().getObject().asResource();
						if (dmn.isURIResource()) {
							if (bAsCompleteStatements) {
								sb.append(getExtractionPreamble("expression", theProperty));
							}
							sb.append(" ");
							sb.append(checkForKeyword(theProperty.getLocalName()));
							sb.append(" of an ");
							sb.append(checkForKeyword(dmn.getLocalName()));
						}
					}
				}
				else {
					if (rng.isURIResource()) {
						sb.append(" ");
						sb.append(checkForKeyword(rng.getLocalName()));
					}
				}
			}
		}
		if (sb.length() == 0) {
			if (theClass != null) {
				if (bAsCompleteStatements) {
					sb.append(getExtractionPreamble("class", theClass));
					sb.append(" ");
				}
				sb.append(checkForKeyword(theClass.getLocalName()));
			}
		}
		if (sb.length() == 0) {
			Iterator<String> clslstitr = matchingClasses.keySet().iterator();
			Iterator<String> proplstitr = matchingProperties.keySet().iterator();
			if (clslstitr.hasNext() || proplstitr.hasNext()) {
				returnStatements.add("\"These domain ontology concepts were matched in the extraction:\"");
			}
			while (clslstitr.hasNext()) {
				String key = clslstitr.next();
				List<Object> clslst = matchingClasses.get(key);
				for (Object o : clslst) {
					if (o instanceof OntClass) {
						returnStatements.add(checkForKeyword(((OntClass)o).getLocalName()) + " is a class.");
					}
					else if (o instanceof String) {
						returnStatements.add(getLocalName((String)o) + " is a class.");
					}
				}
			}
			while (proplstitr.hasNext()) {
				String key = proplstitr.next();
				List<Object> proplst = matchingProperties.get(key);
				for (Object o : proplst) {
					if (o instanceof Property) {
						returnStatements.add(checkForKeyword(((Property)o).getLocalName()) + " is a property.");
					}
					if (o instanceof String) {
						returnStatements.add(getLocalName((String)o) + " is a property.");
					}
				}
			}
			if (seeAlsos != null && seeAlsos.size() > 0) {
				Iterator<String> saitr = seeAlsos.keySet().iterator();
				if (saitr.hasNext()) {
					returnStatements.add("\"Other concepts found with external references:\"");
					while (saitr.hasNext()) {
						String sar = saitr.next();
						String sarln = getLocalName(sar);
						List<Object> refs = seeAlsos.get(sar);
						for (Object ref : refs) {
							returnStatements.add("\" " + sarln + ", '" + ref.toString() + "'\"");
						}
					}
				}
			}
		}
		else {	
			if (bAsCompleteStatements) {
				sb.append(".");
			}
			returnStatements.add(sb.toString());
		}
		List<Object> returns = new ArrayList<Object>();
		returns.add(returnStatements);
		returns.add(labels);
		returns.add(seeAlsos);
		returns.add(matchingClasses);
		returns.add(matchingProperties);
		return returns;
	}

	private String getLocalName(String uri) {
		if (uri != null && uri.indexOf("#") > 0) {
			return uri.substring(uri.indexOf("#") + 1);
		}
		return uri;
	}

	private Object getExtractionPreamble(String type, Object theContent) {
		StringBuilder sb = new StringBuilder();
		StatementContent lastsc = getLastStatement();
		if (lastsc instanceof WhatIsContent && ((WhatIsContent) lastsc).getTarget() instanceof VariableNode) {
			sb.append("Add");
		}
		else {
			if (type.equals("expression")) {
				sb.append("Inferred match ");
			}
			else {
				sb.append("Found matching ");
				sb.append(type);
			}
		}
		return sb.toString();
	}

	/**
	 * Method to put a value into a Map with key and list of values
	 * 
	 */
	private void addExtractToMap(Map<String, List<Object>> labels, String subjUri, Object value) {
		if (labels.containsKey(subjUri)) {
			List<Object> lbllst = labels.get(subjUri);
			if (!lbllst.contains(value)) {
				lbllst.add(value);
			}
		}
		else {
			List<Object> lbllst = new ArrayList<Object>();
			lbllst.add(value);
			labels.put(subjUri, lbllst);
		}
	}

	private OntModel getDomainModelExtractForTextService(boolean useSimplifiedDomainOntology) throws ConfigurationException {
		OntModel dm = getDomainModel();
		if (!useSimplifiedDomainOntology) {
//			try {
//				dm = getConfigurationManager().getOntModel("http://aske.ge.com/hypersonicsV2", Scope.INCLUDEIMPORTS);
//				dm.write(System.err, "N3");
//				return dm;
//			} catch (ConfigurationException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			return dm;
		}
		OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
		OntDocumentManager owlDocMgr = getConfigurationManager().getJenaDocumentMgr();
		if (getOwlModelsFolder() != null && !getOwlModelsFolder().startsWith(IModelProcessor.SYNTHETIC_FROM_TEST)) {
			File mff = new File(getOwlModelsFolder());
			mff.mkdirs();
			spec.setImportModelGetter(new SadlJenaModelGetterPutter(spec, getOwlModelsFolder()));
		}
		if (owlDocMgr != null) {
			spec.setDocumentManager(owlDocMgr);
			owlDocMgr.setProcessImports(true);
		}
		OntModel extractModel = ModelFactory.createOntologyModel(spec);
		ExtendedIterator<OntClass> clssitr = dm.listClasses();
		while (clssitr.hasNext()) {
			OntClass cls = clssitr.next();
			if (cls.isURIResource() && !isAnIgnoredNamespace(cls.getNameSpace())) {
				boolean stmtFound = false;
				StmtIterator stmtitr = dm.listStatements(cls, RDFS.subClassOf, (RDFNode)null);
				while (stmtitr.hasNext()) {
					extractModel.add(stmtitr.nextStatement());
					stmtFound = true;
				}
				StmtIterator lstmtitr = dm.listStatements(cls, RDFS.label, (RDFNode)null);
				while (lstmtitr.hasNext()) {
					extractModel.add(lstmtitr.nextStatement());
					stmtFound = true;
				}
				extractModel.createClass(cls.getURI());
			}
		}
		ExtendedIterator<OntProperty> opropitr = dm.listAllOntProperties();
		while (opropitr.hasNext()) {
			OntProperty op = opropitr.next();
			if (op.isURIResource() && !isAnIgnoredNamespace(op.getNameSpace())) {
				boolean stmtFound = false;
				StmtIterator stmtitr = dm.listStatements(op, RDF.type, (RDFNode)null);
				while (stmtitr.hasNext()) {
					extractModel.add(stmtitr.nextStatement());
					stmtFound = true;
				}
				StmtIterator lstmtitr = dm.listStatements(op, RDFS.label, (RDFNode)null);
				while (lstmtitr.hasNext()) {
					extractModel.add(lstmtitr.nextStatement());
					stmtFound = true;
				}
				extractModel.createOntProperty(op.getURI());
			}
		}
		ExtendedIterator<Individual> institr = dm.listIndividuals();
		while (institr.hasNext()) {
			Individual inst = institr.next();
			if (inst.isURIResource() && !isAnIgnoredNamespace(inst.getNameSpace())) {
				boolean stmtFound = false;
				StmtIterator stmtitr = dm.listStatements(inst, RDF.type, (RDFNode)null);
				while (stmtitr.hasNext()) {
					extractModel.add(stmtitr.nextStatement());
					stmtFound = true;
				}
				StmtIterator lstmtitr = dm.listStatements(inst, RDFS.label, (RDFNode)null);
				while (lstmtitr.hasNext()) {
					extractModel.add(lstmtitr.nextStatement());
					stmtFound = true;
				}
			}
		}
//		extractModel.write(System.err, "N3");	// for debug only, remove when not needed
		return extractModel;
	}

	private boolean isAnIgnoredNamespace(String nameSpace) {
		nameSpace = nameSpace.endsWith("#") ? nameSpace.substring(0, nameSpace.length() - 1) : nameSpace;
		if (nameSpace.equals("http://aske.ge.com/metamodel") ||
				nameSpace.equals(SadlConstants.SADL_BASE_MODEL_URI) ||
				nameSpace.equals(SadlConstants.SADL_LIST_MODEL_URI) ||
				nameSpace.equals(SadlConstants.SADL_IMPLICIT_MODEL_URI) ||
				nameSpace.equals("http://aske.ge.com/compgraphmodel") ||
				nameSpace.equals(IReasoner.SADL_BUILTIN_FUNCTIONS_URI)) {
			return true;
		}
		return false;
	}

	private String getEquationNamesFromTextServiceResults(OntModel m, String dialogModelName) throws ConfigurationException, IOException, ReasonerNotFoundException, InvalidNameException, QueryParseException, QueryCancelledException, AmbiguousNameException {
		OntModel theModel = getExtractionProcessor().getTextModel();
		if (theModel != null && m != null) {
			theModel.add(m);
		}
		else if (m != null) {
			getExtractionProcessor().setTextModel(m);
		}
//		logger.debug("The existing model:");
//		theModel.write(System.err, "N-TRIPLES");
		return processExtractedText(dialogModelName, null, SaveAsSadl.DoNotSaveAsSadl);
	}

	private String getModelPrefixFromInputUrl(String url) {
		int lastSlash = url.lastIndexOf('/');
		if (lastSlash > 0) {
			String prefix = url.substring(lastSlash + 1);
			prefix = prefix.replace(".", "_");
			return prefix;
		}
		return url;
	}

	private String getModelNameFromInputUrl(String url) {
		return url;
	}

	public String downloadURL(String downloadUrl) throws IOException {
        URL website = new URL(downloadUrl);
        URLConnection connection = website.openConnection();
        BufferedReader in = new BufferedReader(
                                new InputStreamReader(
                                    connection.getInputStream()));

        StringBuilder response = new StringBuilder();
        String inputLine;

        while ((inputLine = in.readLine()) != null) 
            response.append(inputLine);

        in.close();

        return response.toString();
	}
	
	private String processModifiedAsk(org.eclipse.emf.ecore.resource.Resource resource, ModifiedAskContent sc) throws ConfigurationException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException {
		Query q = ((ModifiedAskContent)sc).getQuery();
		String answer = null;
		boolean quote = true;

		try {

			if (isDocToModelQuery(q)) {
				answer = processDocToModelQuery(resource, q, sc);
	    		//insertionText = checkForEOS(insertionText);
//	    		Object ctx = q.getContext();
	    		//addCurationManagerContentToDialog(document, reg, insertionText, ctx, true);
	    		//notifyUser()
			} else {
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
						answer = answer.replace("\"", "'");
					}
					quote = true;
				}
				else {
					answer = "No results found";
					quote = true;
				}
			}
		}
		catch (Exception e) {
			answer = e.getMessage();
			quote = true;
		}
		if (answer != null) {
			answerUser(getOwlModelsFolder(), answer, quote, sc.getHostEObject());
			return answer;
		}
		return null;
	}

	private boolean isDocToModelQuery(Query q) {
		List<GraphPatternElement> patterns = q.getPatterns();
		if (patterns != null) {
			for (GraphPatternElement gpe : patterns) {
				if (gpe instanceof TripleElement) {
					if (((TripleElement)gpe).getPredicate().getURI().equals(DialogConstants.SADL_IMPLICIT_MODEL_DIALOG_MODEL_PROPERTY_URI)) {
						return true;
					}
				}
			}
		}
		return false;
	}
	
	private String processDocToModelQuery(org.eclipse.emf.ecore.resource.Resource resource, Query q, ModifiedAskContent sc)  {
		List<GraphPatternElement> patterns = q.getPatterns();
		String retVal = null;
		try {
			for (GraphPatternElement gpe : patterns) {
				if (gpe instanceof TripleElement) {
					if (((TripleElement)gpe).getPredicate().getURI().equals(DialogConstants.SADL_IMPLICIT_MODEL_DIALOG_MODEL_PROPERTY_URI)) {
						Node doc = ((TripleElement)gpe).getSubject();
						// get the name and semantic type of each column
						// TODO check for aliases for column names and use alias if exists
						String tableColSemTypeQuery = "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#> prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#> select distinct ?colname ?typ where {<" + doc.getURI() +
								"> <columnDescriptors> ?cdlist . ?cdlist rdf:rest*/rdf:first ?member . " + 
								"?member <localDescriptorName> ?colname . ?member <augmentedType> ?augtype . ?augtype <semType> ?typC. ?typ rdfs:range ?typC.}";
                		q.setSparqlQueryString(tableColSemTypeQuery);
						ResultSet rrs = runQuery(resource, q);
						if (rrs != null && rrs.getRowCount() > 0) {
							// TODO 
							// convert to triples, one set for each column
							// <doc, columnname, colname>
							// <colname, semtypeprop, semtype>
							rrs.setShowNamespaces(true);
							Map<String,String> colNamesAndTypes = new HashMap<String,String>();
							for (int i = 0; i <= rrs.getColumnCount(); i++) {
								String colname = rrs.getResultAt(i, 0).toString();
								String semtyp = rrs.getResultAt(i, 1).toString();
								colNamesAndTypes.put(colname, semtyp);
							}
							Iterator<String> keyitr = colNamesAndTypes.keySet().iterator();
							NamedNode docNN = new NamedNode(doc.getURI());
							List<TripleElement> triplesList = new ArrayList<TripleElement>();
							while (keyitr.hasNext()) {
								String colname = keyitr.next();
								NamedNode colNameNN = new NamedNode(colname);
								String semtyp = colNamesAndTypes.get(colname);
								NamedNode semtypNN = new NamedNode(semtyp);
								TripleElement tr = new TripleElement(docNN, colNameNN, semtypNN);
								triplesList.add(tr);
							}
							TripleElement[] triples = triplesList.toArray(new TripleElement[triplesList.size()]);
							List<TripleElement[]> listOfTriples = new ArrayList<TripleElement[]>();
							listOfTriples.add(triples);
			                OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND, listOfTriples);
	                		Object[] rss = insertTriplesAndQuery(resource, listOfTriples);
	                		
	            			String answer = getAnswerAndVisualize(sc, null, rss);

	            			if (rss != null) {
	            				retVal = stringToQuotedeString(answer);
	            			}
	            			else {
	            				retVal = "Failed to evaluate answer";
	            			}
	            			if (!retVal.equals(stringToQuotedeString(""))) {
	            				answerUser(getOwlModelsFolder(), retVal, true, sc.getHostEObject());
	            			}
						}
					}
				}
			}
		}
		catch (Exception e)
		{
            logger.debug("AutoEdit error (of type " + e.getClass().getCanonicalName() + "): " + e.getMessage());   
            return null;
        }
		return retVal;
	}
	
	

	private String processWhatValuesContent(org.eclipse.emf.ecore.resource.Resource resource, WhatValuesContent sc) throws ConfigurationException {
		StringBuilder sb = new StringBuilder();
		Node cls = sc.getCls();
		String article = sc.getArticle();
		Node prop = sc.getProp();
		
		OntClass theClass = getDomainModel().getOntClass(cls.getURI());
		OntProperty theProp = getDomainModel().getOntProperty(prop.toFullyQualifiedString());
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
			String rng = getRange(theProp);
			if (rng != null) {
				sb.append(rng);
			}
		}
		String answer = sb.toString();
		answerUser(getOwlModelsFolder(), answer, false, sc.getHostEObject());
		return answer;
	}

	private String getRange(OntProperty theProp) {
		StmtIterator stmtitr = getDomainModel().listStatements(theProp, RDFS.range, (RDFNode)null);
		if (stmtitr.hasNext()) {
			StringBuilder sb = new StringBuilder();
			while (stmtitr.hasNext()) {
				RDFNode obj = stmtitr.nextStatement().getObject();
				if (obj.isURIResource()) {
					if (sb.length() > 0) {
						sb.append(" or");
					}
					sb.append(obj.asResource().getLocalName());
				}
			}
			return sb.toString();
		}
		return null;
	}

	private String processHowManyValue(org.eclipse.emf.ecore.resource.Resource resource, HowManyValuesContent sc) throws ConfigurationException {
		StringBuilder sb = new StringBuilder();
		Node cls = sc.getCls();
		String article = sc.getArticle();
		Node typ = sc.getTyp();
		Node prop = sc.getProp();
		
		OntClass theClass = cls != null ? getDomainModel().getOntClass(cls.getURI()) : null;
		OntProperty theProp = getDomainModel().getOntProperty(prop.toFullyQualifiedString());
		boolean restrictionFound = false;
		if (theClass != null) {
			ExtendedIterator<OntClass> scitr = theClass.listSuperClasses();
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
		}
		if (!restrictionFound) {
			String rng = getRange(theProp);
			if (rng != null) {
				sb.append(rng);
			}
		}
		sb.insert(0, "unlimited number of values of type ");
		String answer = sb.toString();
		answerUser(getOwlModelsFolder(), answer, true, sc.getHostEObject());
		return answer;
	}

	private String processWhatIsContent(org.eclipse.emf.ecore.resource.Resource resource, 
			WhatIsContent sc) throws ExecutionException, SadlInferenceException, 
			TranslationException, ConfigurationException, IOException, AnswerExtractionException {
		if (sc != null) {
			if (sc.getComputationalGraphRules() != null) {
				List<Rule> comparisonRules = ((WhatIsContent)sc).getComputationalGraphRules();
				Object[] rss = null;
				try {
					rss = insertRulesAndQuery(resource, comparisonRules);
				} catch (SadlInferenceException e) {
					if (e.getCause() instanceof NoModelFoundForTargetException) {
						return handleNoModelFoundForTargetException(sc, (NoModelFoundForTargetException) e.getCause());
					}
					else {
						throw e;
					}
				}
				return processResultsOfInsertRulesAndQuery(sc, rss, comparisonRules);
			}
			else {
				// this is a normal query
				String retVal = null;
				Object trgt = ((WhatIsContent)sc).getTarget();
				Object whn = ((WhatIsContent)sc).getWhen();
				if (trgt instanceof NamedNode && whn == null) {
					String answer;
					try {
						answer = whatIsNamedNode(resource, getOwlModelsFolder(), (NamedNode)trgt);
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
						//Object ctx = null;
						TripleElement[] triples = flattenTriples((Object[])trgt);
						//ctx = triples[0].getContext();
						//StringBuilder answer = new StringBuilder();
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
								resultStr = stringToQuotedeString(sb.toString());
							}
						}
						String insertionText = (resultStr != null ? resultStr : "\"Failed to find results\"");
						answerUser(getOwlModelsFolder(), insertionText, false, sc.getHostEObject());
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
					
					Object[] rss = insertTriplesAndQuery(resource, triples);
					if (rss != null) {
						String answer = getAnswerAndVisualize(sc, null, rss);
						retVal = stringToQuotedeString(answer);
					}
					else {
						retVal = "Failed to evaluate answer";
					}
					if (!retVal.equals(stringToQuotedeString(""))) {
						answerUser(getOwlModelsFolder(), retVal, true, sc.getHostEObject());
					}
				}
				return retVal;
			}
	//		return "Processing of query disabled";
		}
		throw new AnswerExtractionException("Invalid what is request inputs");
	}

	private String processResultsOfInsertRulesAndQuery(StatementContent sc, Object[] rss, List<Rule> comparisonRules) throws TranslationException, ConfigurationException {
		String retVal = "";
		String resultStr = null;
		if (rss != null) {
			boolean cgr = true;
			if (!cgr) {
				StringBuilder sb = new StringBuilder();
				Rule firstRule = comparisonRules.get(0);
				TripleElement firstTriple = firstRule.getGivens() != null && firstRule.getGivens().get(0) instanceof TripleElement ? (TripleElement) firstRule.getGivens().get(0) :
					firstRule.getIfs() != null && firstRule.getIfs().get(0) instanceof TripleElement ? (TripleElement)firstRule.getIfs().get(0) : null;
				if (firstTriple.getSubject() instanceof VariableNode && 
						((VariableNode)firstTriple.getSubject()).getType() instanceof NamedNode) {
					sb.append("the ");
					sb.append(((VariableNode)(firstTriple.getSubject())).getType().getName());
					sb.append(" has ");
					sb.append(firstTriple.getPredicate().getName());
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
					resultStr = stringToQuotedeString(sb.toString());
				}
			} else {
				resultStr = getAnswerAndVisualize(sc, comparisonRules, rss);
				
			}
		}
		String insertionText = (resultStr != null && resultStr.length() > 0 ? resultStr : "\"Failed to find results\"");
		answerUser(getOwlModelsFolder(), insertionText, false, sc.getHostEObject());
		retVal = retVal + (retVal.length() > 0 ? System.lineSeparator() : "") + insertionText;
		return retVal;
	}

	/**
	 * Method to generate 
	 * @param sc
	 * @param comparisonRules
	 * @param rss
	 * @return
	 * @throws ConfigurationException
	 */
	private String getAnswerAndVisualize(StatementContent sc, List<Rule> comparisonRules, Object[] rss) throws ConfigurationException  {
		StringBuilder answer = new StringBuilder();
		String graphsDirectory = new File(getOwlModelsFolder()).getParent().replace('\\', '/') + "/Graphs";
		String baseFileName = "";
		List<List<String>> diagrams = new ArrayList<List<String>>();
			// diagrams is a List of Lists
			// The elements of the outer list are the rows in the table--there should be one for each key in table (below).
			// Each inner list is list of links, each one of which will be put in a row
		
		List<HashMap<String, List<HashMap<String, String>>>> table = new ArrayList<>();
			// table is a List of Maps of lists of maps. Each member of the List is the data for a row in the table
			// The key to the outer map is the comparator, e.g., CF6. For tabular output, 
			//	this is the value in the first column, headed "Options".
			// The inner list is an ordered set of maps contains the additional columns to be displayed. For each map, 
			//	the key is the column header, the type of the thing in that column, 
			//	and the value is the value to be displayed in that column.
		
		boolean isTable = false;
		String insights = "";
		
		if (rss != null) {
			int cntr = 0;
			if (rss.length > 3){
				isTable = true;
			}
			for (Object rs : rss) {
				if (rs instanceof ResultSet) {
		 			String[] colnames = ((ResultSet) rs).getColumnNames();
					String cols = String.join(" ",colnames);
					if ( cols.contains("_style") || cols.contains("_shape") ) {
						ResultSet rstemp = ((ResultSet)rs).deleteResultSetColumn("Model");

						IGraphVisualizer visualizer = new GraphVizVisualizer();
						if (visualizer != null) {
							//String graphsDirectory = new File(getOwlModelsFolder()).getParent() + "/Graphs";
							new File(graphsDirectory).mkdir();
//							if(cntr == 0) {
//								baseFileName = "EquationDependencyGraph";
//
//								visualizer.initialize(
//										graphsDirectory,
//										baseFileName,
//										baseFileName,
//										null,
//										IGraphVisualizer.Orientation.TD,
//										"Equation Dependency Graph"
//										);
//								cntr++;
//							}
//							else {
								baseFileName = "QueryMetadata_" + ((ResultSet)rs).getResultAt(0, 0).toString();

								visualizer.initialize(
										graphsDirectory,
										baseFileName,
										baseFileName,
										null,
										IGraphVisualizer.Orientation.TD,
										"Composed Model " + ((ResultSet)rs).getResultAt(0, 0).toString()
										);
//							}
							((ResultSet) rstemp).setShowNamespaces(false);
							try {
								visualizer.graphResultSetData(rstemp);	
							}
							catch (Exception e) {
								e.printStackTrace();
							}
				        }
						//Don't pop up model diagram. User can choose to click on a link to see.
//						String errorMsg = null; //displayGraph(visualizer); 
//						if (errorMsg != null) {
//							notifyUser(getOwlModelsFolder(), errorMsg, true);
//						}
					}
					else if(cols.contains("Trend")) {//the "insights" section
						insights += generateInsightsSADL((ResultSet)rs, isTable);
						
					}
					else {
		    			if(cntr > 1 && answer.length() > 0) {
		    				answer.append(",");
		    				answer.append(System.lineSeparator());
		    			}
						
//						String sadlAnswer = addResultsToDialog((ResultSet) rs, isTable);
						if(cntr <= 1) {
//							String graphicURL = "file://" + graphsDirectory + "/" + "EquationDependencyGraph.svg"; //file url
//							sadlAnswer += "(See \"Equation dependency diagram: \'" + graphicURL + "\'\".)\n";
						}
						
						ResultSet rset = (ResultSet) rs;
						rset.setShowNamespaces(false);
						
						if(rset.getRowCount() > 0) {
						
							String className = rset.getResultAt(0, 1).toString();	// Used to name the link to the model graph 
							if (isTable) {					
								HashMap<String,List<HashMap<String,String>>> tableRow = new HashMap<String, List<HashMap<String,String>>>();
								table.add(tableRow);
								// are there different conditions in the comparisonRules?
								// Note: the assumption is made there that the sets of results in rss are in the same
								//	order as the rules in comparisonRules, allowing us to get the conditions on which
								//	the answer depends from the rule conditions.
								
								for(int i=0; i< rset.getRowCount(); i++) {
									List<HashMap<String, String>> varVal = new ArrayList<HashMap<String,String>>();
									String comparand = rset.getResultAt(i, 1).toString();
									tableRow.put(comparand,  varVal); 
									// see if there are any conditions to be added
									// Note: if there are multiple models there will not be conditionals in every row, hence the test
									if (cntr < comparisonRules.size()) {
										List<HashMap<String, String>> conditionVals = getTableRowConditions(comparisonRules.get(cntr));
										if (conditionVals != null) {
											varVal.addAll(conditionVals);
										}
									}
									
									// now add the output of the computation
									String value = SadlUtils.formatNumberList(rset.getResultAt(i, 3).toString(), 5);
									Object unitObj = rset.getResultAt(i, 5);
									if (unitObj != null) {
										String unit = quoteUnitIfNecessary(unitObj.toString());
										value = value + " " + unit;
									}
									HashMap<String, String> valmap = new HashMap<String, String>();
									valmap.put(rset.getResultAt(i, 2).toString(), value);
									varVal.add(valmap);
								}
							}
							else {
								answer.append(addResultsToDialog((ResultSet) rs));
	
							}
							String graphicUrl;
							String sglink = getPreference(DialogPreferences.SHORT_GRAPH_LINK.getId());
							String graphFileName = baseFileName + ".svg";
							String sourceName = graphsDirectory + "/" + graphFileName;
							if (sglink != null && sglink.length() > 0) {
								String targetName = sglink + "/" + className + baseFileName.substring(13);
								File srcFile = new File(sourceName);
								if (srcFile.exists()) {
									File trgtFile = new File(targetName);
									try {
										Files.copy(srcFile, trgtFile);
									} catch (IOException e) {
										// TODO Auto-generated catch block
										e.printStackTrace();
										graphicUrl = "file://" + sourceName; //file url
									}
								}
								graphicUrl = "file://" + targetName;
							}
							else {
								graphicUrl = "file://" + sourceName; //file url
							}
							//sadlAnswer += 
							String seeStmt = "\"" + graphicUrl + "\"";
							List<String> rowUrls = new ArrayList<String>();
							rowUrls.add(seeStmt);
							if (rset.getResultAt(0, 6) != null) {
								String sensitivityUrl = rset.getResultAt(0, 6).toString();
								rowUrls.add("\"" + sensitivityUrl + "\"");
							}
							diagrams.add(rowUrls);
	//						answer.append(sadlAnswer);
							cntr++;
						}
						else {
							//TODO: answer that "No model was found"
						}
					}
				}
				else if (rs != null){
					answerUser(getOwlModelsFolder(), stringToQuotedeString(rs.toString()), true, sc.getHostEObject());
				}
				
			}
			
			if (cntr > 0) {
				if(isTable) {
					answer.append(generateSadlTable(table, diagrams));
				}
				else {
					StringBuilder sadlAnswer = new StringBuilder();
					sadlAnswer.append("  (See \'model diagram: " + diagrams.get(0).get(0) +  "\'");
					if (diagrams.get(0).size()> 1) {
						sadlAnswer.append(", \'sensitivity plot: " + diagrams.get(0).get(1));
						sadlAnswer.append("\'");
					}
					sadlAnswer.append(".)");
					answer.append(System.lineSeparator());
					answer.append(sadlAnswer);
				}
				answer.append(insights);
			}
		}
		
		return answer.toString();
	}

	/**
	 * Method to extract conditions from the rule
	 * @param rule
	 * @return
	 */
	private List<HashMap<String, String>> getTableRowConditions(Rule rule) {
		List<HashMap<String, String>> conditions = new ArrayList<HashMap<String, String>>();
		List<GraphPatternElement> ifs = rule.getIfs();
		for (int i = 0; i < ifs.size(); i++) {
			GraphPatternElement gpe = ifs.get(i);
			if (gpe instanceof TripleElement) {
				if (((TripleElement)gpe).getSubject() instanceof VariableNode && ((TripleElement)gpe).getPredicate() instanceof RDFTypeNode) {
					// this might be a condition variable
					if (i < ifs.size()-2 && ifs.get(i+1) instanceof TripleElement && ifs.get(i+2) instanceof TripleElement && 
							((TripleElement)ifs.get(i+1)).getSubject().equals(((TripleElement)gpe).getSubject()) && 
							((TripleElement)ifs.get(i+1)).getPredicate().getURI().equals(SadlConstants.SADL_IMPLICIT_MODEL_VALUE_URI) //&&
							//((TripleElement)ifs.get(i+2)).getSubject().equals(((TripleElement)gpe).getSubject()) && 
							//((TripleElement)ifs.get(i+2)).getPredicate().getURI().equals(SadlConstants.SADL_IMPLICIT_MODEL_UNIT_URI)
							) {
						HashMap<String, String> condition = new HashMap<String, String>();
						String colHeader = ((TripleElement)gpe).getObject().getName();
						String condValue = ((TripleElement)ifs.get(i+1)).getObject().toString();
						if (((TripleElement)ifs.get(i+2)).getSubject().equals(((TripleElement)gpe).getSubject()) && 
						    ((TripleElement)ifs.get(i+2)).getPredicate().getURI().equals(SadlConstants.SADL_IMPLICIT_MODEL_UNIT_URI)) {
							condValue += " " + ((TripleElement)ifs.get(i+2)).getObject().toString();
						}
						condition.put(colHeader, condValue);
						conditions.add(condition);
						i = i + 2;
					}
				}
			}
		}
		return conditions;
	}

	/**
	 * Generate sadl insight statements. For example: increasing Altitude increases Thrust 
	 * @param rs
	 * @param isTable 
	 * @return
	 */
	private String generateInsightsSADL(ResultSet rs, boolean isTable) {
		StringBuilder sb = new StringBuilder();
		Map<String,String> limitViolation = new HashMap<String,String>(); //Input->incr/decr
		if (rs != null && rs.getRowCount() > 0) {
			sb.append(System.lineSeparator());
			int classIdx = rs.getColumnPosition("Class");
			int inputIdx = rs.getColumnPosition("Input");
			int trendIdx = rs.getColumnPosition("Trend");
			int locIdx = rs.getColumnPosition("Location");
			int ouputIdx = rs.getColumnPosition("Output");
			/**
			 *  Only results can be:
			 *  1) decreasingDecreases => increasingIncreases
			 *  2) decreasingIncreases => increasingDecreases
			 *  3) localmin at lower/higher
			 *  4) localmax at lower/higher
			 *  5) localmin/localmax at query point
			 */
			
//			boolean compare=false;
//			Object cl = rs.getResultAt(0, classIdx);
//			for(int i=1; i<rs.getRowCount(); i++) {
//				if (cl != rs.getResultAt(0, classIdx)) {
//					compare=true;
//					continue;
//				}
//			}
			
			for (int row = 0; row < rs.getRowCount(); row++) {
				if(rs.getResultAt(row, trendIdx).equals("increasingIncreases")) {
					if (rs.getResultAt(row, ouputIdx).toString().equals("TemperatureLimitViolationFlag")) {
						sb.append("Increasing ");
						sb.append(rs.getResultAt(row, inputIdx).toString());
						sb.append(" takes the ");
						sb.append(rs.getResultAt(row, classIdx).toString());
						sb.append(" outside operating conditions.");
						sb.append(System.lineSeparator());
					}
					else {
						if(! limitViolation.containsKey(rs.getResultAt(row, inputIdx).toString()) ||
						   ! limitViolation.get(rs.getResultAt(row, inputIdx).toString()).equals("Increasing") ) {
							sb.append("Increasing ");
							sb.append(rs.getResultAt(row, inputIdx).toString());
							sb.append(" increases ");
							sb.append(rs.getResultAt(row, ouputIdx).toString());
							if(isTable) {
								sb.append(" of the ");
								sb.append(rs.getResultAt(row, classIdx).toString());
							}
							sb.append(".");
							sb.append(System.lineSeparator());
						}
					}
				} 
				else if(rs.getResultAt(row, trendIdx).equals("increasingDecreases")) {
					if (rs.getResultAt(row, ouputIdx).toString().equals("TemperatureLimitViolationFlag")) {
						sb.append("Increasing ");
						sb.append(rs.getResultAt(row, inputIdx).toString());
						sb.append(" takes the ");
						sb.append(rs.getResultAt(row, classIdx).toString());
						sb.append(" inside operating conditions.");
						sb.append(System.lineSeparator());
						limitViolation.put(rs.getResultAt(row, inputIdx).toString(), "Decreasing");
					}
					else {
						if(! limitViolation.containsKey(rs.getResultAt(row, inputIdx).toString()) ||
						   ! limitViolation.get(rs.getResultAt(row, inputIdx).toString()).equals("Increasing") ) {
							sb.append("Increasing ");
							sb.append(rs.getResultAt(row, inputIdx).toString());
							sb.append(" decreases ");
							sb.append(rs.getResultAt(row, ouputIdx).toString());
							if(isTable) {
								sb.append(" of the ");
								sb.append(rs.getResultAt(row, classIdx).toString());
							}
							sb.append(".");
							sb.append(System.lineSeparator());
						}
					}
				} 
				else if(rs.getResultAt(row, trendIdx).equals("decreasingIncreases")) {
					if (rs.getResultAt(row, ouputIdx).toString().equals("TemperatureLimitViolationFlag")) {
						sb.append("Decreasing ");
						sb.append(rs.getResultAt(row, inputIdx).toString());
						sb.append(" takes the ");
						sb.append(rs.getResultAt(row, classIdx).toString());
						sb.append(" outside operating conditions.");
						sb.append(System.lineSeparator());
					}
					else {
						if(! limitViolation.containsKey(rs.getResultAt(row, inputIdx).toString()) ||
								   ! limitViolation.get(rs.getResultAt(row, inputIdx).toString()).equals("Decreasing") ) {
							sb.append("Decreasing ");
							sb.append(rs.getResultAt(row, inputIdx).toString());
							sb.append(" increases ");
							sb.append(rs.getResultAt(row, ouputIdx).toString());
							if(isTable) {
								sb.append(" of the ");
								sb.append(rs.getResultAt(row, classIdx).toString());
							}
							sb.append(".");
							sb.append(System.lineSeparator());
						}
					}
				} 
				else if(rs.getResultAt(row, trendIdx).equals("decreasingDecreases")) {
					if (rs.getResultAt(row, ouputIdx).toString().equals("TemperatureLimitViolationFlag")) {
						sb.append("Decreasing ");
						sb.append(rs.getResultAt(row, inputIdx).toString());
						sb.append(" takes the ");
						sb.append(rs.getResultAt(row, classIdx).toString());
						sb.append(" inside operating conditions.");
						sb.append(System.lineSeparator());
						limitViolation.put(rs.getResultAt(row, inputIdx).toString(), "Increasing");
					}
					else {
						if(! limitViolation.containsKey(rs.getResultAt(row, inputIdx).toString()) ||
						   ! limitViolation.get(rs.getResultAt(row, inputIdx).toString()).equals("Decreasing") ) {
							sb.append("Decreasing ");
							sb.append(rs.getResultAt(row, inputIdx).toString());
							sb.append(" decreases ");
							sb.append(rs.getResultAt(row, ouputIdx).toString());
							if(isTable) {
								sb.append(" of the ");
								sb.append(rs.getResultAt(row, classIdx).toString());
							}
							sb.append(".");
							sb.append(System.lineSeparator());
						}
					}
				} 
				else if(rs.getResultAt(row, trendIdx).equals("local_minimum") || rs.getResultAt(row, trendIdx).equals("local_maximum")) {
					if(rs.getResultAt(row, locIdx) != null && (rs.getResultAt(row, locIdx).equals("lower_values") || rs.getResultAt(row, locIdx).equals("higher_values"))) {
						sb.append(rs.getResultAt(row, ouputIdx).toString());
						if(isTable) {
							sb.append(" of the ");
							sb.append(rs.getResultAt(row, classIdx).toString());
						}
						sb.append(" has a ");
						sb.append(rs.getResultAt(row, trendIdx));
						sb.append(" at " );
//						if(rs.getResultAt(row, locIdx).equals("lower_value")) {
//							sb.append("lower value");
//						}
//						else {
//							sb.append("higher value");
//						}
						sb.append(rs.getResultAt(row, locIdx));
						sb.append(" of " );
						sb.append(rs.getResultAt(row, inputIdx).toString());
						sb.append(".");
						sb.append(System.lineSeparator());
					}
				}
				else if(rs.getResultAt(row, trendIdx).equals("independent") && !rs.getResultAt(row, ouputIdx).toString().equals("TemperatureLimitViolationFlag")) {
					sb.append(rs.getResultAt(row, inputIdx).toString());
					sb.append(" does not affect ");
					sb.append(rs.getResultAt(row, ouputIdx).toString());
					if(isTable) {
						sb.append(" of the ");
						sb.append(rs.getResultAt(row, classIdx).toString());
					}
					sb.append(".");
					sb.append(System.lineSeparator());
				}
				else if(rs.getResultAt(row, trendIdx).equals("pos_sensitive")) {
					sb.append(rs.getResultAt(row, ouputIdx).toString());
					if(isTable) {
						sb.append(" of the ");
						sb.append(rs.getResultAt(row, classIdx).toString());
					}
					sb.append(" is very sensitive to ");
					sb.append(rs.getResultAt(row, inputIdx).toString());
					sb.append(" with positive slope.");
					sb.append(System.lineSeparator());
				}
				else if(rs.getResultAt(row, trendIdx).equals("neg_sensitive")) {
					sb.append(rs.getResultAt(row, ouputIdx).toString());
					if(isTable) {
						sb.append(" of the ");
						sb.append(rs.getResultAt(row, classIdx).toString());
					}
					sb.append(" is very sensitive to ");
					sb.append(rs.getResultAt(row, inputIdx).toString());
					sb.append(" with negative slope.");
					sb.append(System.lineSeparator());
				}
			}
		}
		return sb.toString();
	}

	/**
	 * Generate a sadl table from results table
	 * @param table
	 * @param diagrams-- list of list of URL. Outer list ranges over rows, inner list is link columns in row.
	 * @return sadl string
	 */
	private Object generateSadlTable(List<HashMap<String, List<HashMap<String, String>>>> table, List<List<String>> diagrams) {
		String firstLinkColHdr = "\'Model diagram\'";
		String secondLinkColHdr = "\'Sensitivity plot\'";

		List<String> colHeaders = new ArrayList<String>();
		// first column for comparanda
		String firstColHeader = "'Options'";
		colHeaders.add(firstColHeader);
		List<Integer> colWidths = new ArrayList<Integer>();
		colWidths.add(firstColHeader.length());
		
		boolean multiModelTable = false;

		// next columns for properties to be compared
		int rowIdx = 0;
		for (HashMap<String, List<HashMap<String, String>>> tableRow : table) {
			int colIdx = 1;
			for(String c : tableRow.keySet()) {
				if (c.length() > colWidths.get(0)) {
					colWidths.set(0, c.length());
				}
				List<HashMap<String, String>> colList = tableRow.get(c);
				if (rowIdx > 0 && colList.size() < colHeaders.size()) {
					multiModelTable = true;
				}
				for (int i = 0; i < colList.size(); i++) {
					HashMap<String, String> m = colList.get(i);
					int colWidth = 0;
					int colIndex = -1;
					for (String k : m.keySet()) {
						if (rowIdx == 0) {
							colHeaders.add(k);
						}
						else {
							colIndex = colHeaders.indexOf(k);
						}
						colWidth = Math.max(k.length(), m.get(k).toString().length());
					}
					int effectiveI;
					if (!multiModelTable) {
						effectiveI = i;
					}
					else {
						effectiveI = colIndex;
					}
					if (effectiveI+1 >= colWidths.size()) {
						colWidths.add(colWidth);	
					}
					else if (colWidth > colWidths.get(effectiveI+1)) {
						colWidths.set(effectiveI+1, colWidth);
					}
					colIdx++;
				}
				rowIdx++;
			}
			if (multiModelTable) {
				colIdx = colHeaders.size();
			}
			
			if (rowIdx == 1) {	// only need to do this part once for rowIdx == 1
				// column for first links
				for(List<String> rowLinks : diagrams) {		// ranges over rows
					for (int linkColIdx = 0; linkColIdx < rowLinks.size(); linkColIdx++) {
						int maxLinkLen = 0;
						if (linkColIdx == 0) {
							maxLinkLen = firstLinkColHdr.length();
						}
						else if (linkColIdx == 1) {
							maxLinkLen = secondLinkColHdr.length();
						}
						else {
							System.err.println("Not expecting more than 2 link columns in table");
						}
			
						String rowLink = rowLinks.get(linkColIdx);
						int linkLen = rowLink.length();
						if (linkLen > maxLinkLen) {
							maxLinkLen = linkLen;
						}
						if (colIdx + linkColIdx >= colWidths.size()) {
							colWidths.add(maxLinkLen);
						}
						else {
							if (maxLinkLen > colWidths.get(colIdx + linkColIdx)) {
								colWidths.set(colIdx + linkColIdx, maxLinkLen);
							}
						}
					}
				}
			}
		}
		colHeaders.add(firstLinkColHdr);
		colHeaders.add(secondLinkColHdr);
	
		StringBuilder sb = new StringBuilder("{");

		// now output the header row
		HashMap<String, List<HashMap<String, String>>> tableRow0 = table.get(0);
		sb.append("[");
		sb.append(centerString(colWidths.get(0), firstColHeader));
		
		String someC = (String) tableRow0.keySet().toArray()[0];
		int idx = 1;
		List<HashMap<String, String>> headerRowList = tableRow0.get(someC);
		for (HashMap<String, String> map : headerRowList) {
			sb.append(", ");
			sb.append(centerString(colWidths.get(idx++), map.keySet().toArray()[0].toString()));
		}
		sb.append(", ");
		sb.append(centerString(colWidths.get(idx++), firstLinkColHdr));
		sb.append(", ");
		sb.append(centerString(colWidths.get(idx++), secondLinkColHdr));
		sb.append("],");
		sb.append(System.lineSeparator());

		// now output the table rows
		int rowNum = 0;
		for (HashMap<String, List<HashMap<String, String>>> tableRow : table) {			
			for(String c : tableRow.keySet()) {
				idx = 0;
				sb.append(" [");
				sb.append(centerString(colWidths.get(idx++), c));				
				List<HashMap<String, String>> tblelement = tableRow.get(c);
				for (Map<String, String> m : tblelement) {
					String v = m.keySet().toArray()[0].toString();
					if (multiModelTable) {
						int colLoc = colHeaders.indexOf(v);
						if (colLoc != idx) {
							while (idx < colLoc) {
								sb.append(", ");
								int cw = colWidths.get(idx++);
								String ditto = centerString(cw, "''");
								sb.append(ditto);
							}
						}
					}
					sb.append(", ");
					sb.append(centerString(colWidths.get(idx++), m.get(v)));
				}
				sb.append(", ");
				sb.append(centerString(colWidths.get(idx++), diagrams.get(rowNum).get(0)));
				sb.append(", ");
				int thisCW = colWidths.get(idx++);
				if (diagrams.get(rowNum).size() > 1) {
					sb.append(centerString(thisCW, diagrams.get(rowNum).get(1)));
				}
				else {
					sb.append(centerString(thisCW, "''"));
				}
				sb.append("],");
				sb.append(System.lineSeparator());
				rowNum++;
			}
		}
		sb.deleteCharAt(sb.length()-(System.lineSeparator().length() + 1)); //delete last comma
		sb.append("}.");
		sb.append(System.lineSeparator());
		
		return sb.toString();
	}

	private static String centerString (int width, String s) {
	    return String.format("%-" + width  + "s", String.format("%" + (s.length() + (width - s.length()) / 2) + "s", s));
	}
	
	private String addResultsToDialog(ResultSet rs) {
		StringBuilder sb = new StringBuilder();
		if (rs != null && rs.getRowCount() > 0) {
			//			sb.append("The CGExecution with compGraph ");
			//			sb.append(rs.getResultAt(0, 0).toString());
			for (int row = 0; row < rs.getRowCount(); row++) {
				//				sb.append("    has output (a ");
				sb.append(" a ");
				sb.append(rs.getResultAt(row, 2).toString());
				sb.append(" with ^value ");
				//				sb.append(rs.getResultAt(row, 2));
				String value = SadlUtils.formatNumberList(rs.getResultAt(row, 3).toString(), 5);
				Object unitObj = rs.getResultAt(row, 5);
				if (unitObj != null) {
					String unit = quoteUnitIfNecessary(unitObj.toString());
					value = value + " " + unit;
				}
				sb.append(value);
				if (rs.getResultAt(row, 4) != null) {
					sb.append(", with stddev ");
					sb.append(rs.getResultAt(row, 4));
				}
				//				sb.append(")\n");
			}
			sb.append(" .");
//			sb.append(System.lineSeparator());
		}
		return sb.toString();
	}
	
	
	/**
	 * Method to quote the unit string if needed
	 * @param string
	 * @return
	 */
	private String quoteUnitIfNecessary(String string) {
		boolean quote = false;
		char[] charArray = string.toCharArray();
	    for(char c:charArray)
	    {
	        if (!Character.isLetterOrDigit(c)) {
	            quote = true;
	            break;
	        }
	    }
	    if (quote) {
			return "\"" + string + "\"";
		}
		return string;
	}

/**
 * 	invoke DialogAnswerProvider method displayGraph
 * @param visualizer
 * @return
 */
	private String displayGraph(IGraphVisualizer visualizer) {
		if (getDialogAnswerProvider(getResourceUri()) != null) {
			// talk to the user via the Dialog editor
			Method acmic = null;
			try {
				acmic = getDialogAnswerProvider(getResourceUri()).getClass().getMethod("displayGraph", IGraphVisualizer.class);
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			if (acmic == null) {
				Method[] dapMethods = getDialogAnswerProvider(getResourceUri()).getClass().getDeclaredMethods();
				if (dapMethods != null) {
					for (Method m : dapMethods) {
						if (m.getName().equals("displayGraph")) {
							acmic = m;
							break;
						}
					}
				}
			}
			if (acmic != null) {
				acmic.setAccessible(true);
				try {
					Object retVal = acmic.invoke(getDialogAnswerProvider(getResourceUri()), visualizer);
					return retVal != null ? retVal.toString() : null;
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
		return "Unable to find method to display graph";
	}

	private String whatIsNamedNode(org.eclipse.emf.ecore.resource.Resource resource, String modelFolder,  NamedNode lastcmd) throws ConfigurationException, ExecutionException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException, OwlImportException, AmbiguousNameException {
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
			answer.append(getOwlToSadl(getDomainModel(), getDomainModelName()).classToSadl(nn.getURI()));
//			isFirstProperty = addDomainAndRange(resource, nn, isFirstProperty, answer);
//			addQualifiedCardinalityRestriction(resource, nn, isFirstProperty, answer);		
			Object ctx = (EObject) ((NamedNode)lastcmd).getContext();
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
//			addInstanceDeclaration(resource, nn, answer);
			answer.append(getOwlToSadl(getDomainModel(), getDomainModelName()).individualToSadl(nn.getURI(), false));
			Object ctx = ((NamedNode)lastcmd).getContext();
			answerUser(modelFolder, answer.toString(), false, (EObject) ctx);
			return answer.toString();
		}
		else if (typ.equals(NodeType.FunctionNode)) {
//			addInstanceDeclaration(resource, nn, answer);
			String[] sds = getOwlToSadl(getDomainModel(), getDomainModelName()).equationToSadl(nn.getURI(), false, getConfigurationManager());
			for (int i = sds.length - 1; i >= 0; i--) { // do in reverse order as they will be inserted at the same location
				String sd = sds[i];
				if (i < sds.length - 1) {
					answer.append(System.lineSeparator());
				}
				answer.append(sd);
				Object ctx = ((NamedNode)lastcmd).getContext();
				answerUser(modelFolder, sd, false, (EObject) ctx);
			}
			return answer.toString();
		}
		else if (typ.equals(NodeType.VariableNode)) {
			String response = "Concept " + nn.getName() + " is not defined; please define or do extraction.";
			answerUser(modelFolder, response, false, (EObject)((VariableNode) nn).getHostObject());
			return response;
		}
		else {
			answer.append("Type " + typ.getClass().getCanonicalName() + " not handled yet.");
			logger.debug(answer.toString());
			return answer.toString();
		}
	}

	private String handleNoModelFoundForTargetException(ExpectsAnswerContent sc, NoModelFoundForTargetException cause) throws ConfigurationException {
		Node target = cause.getTarget();
		String msg;
		boolean quote = false;
		if (target instanceof NamedNode) {
			msg = "No model found to compute " + ((NamedNode)target).getName() + "; please add a model or do extraction";
		}
		else {
			msg = "No model found to compute " + target.toString() + "; please add a model or do extraction";
			quote = true;
		}
		answerUser(getOwlModelsFolder(), msg, quote, sc.getHostEObject());
		return msg;
	}

	/**
	 * Method to find the EObject which will be associated with the conversation element in the Dialog window.
	 * @param eobj	-- the starting EObject
	 * @return -- the conversation-level EObject
	 */
	private EObject getConversationHostObject(EObject eobj) {
		if (eobj.eContainer() != null) {
			if (eobj.eContainer() instanceof SadlModel) {
				return eobj;
			}
			return getConversationHostObject(eobj.eContainer());
		}
		return eobj;
	}

	private OwlToSadl getOwlToSadl(OntModel theModel, String modelName) {
		if (owl2sadl == null) {
			owl2sadl = new OwlToSadl(theModel, modelName);
			owl2sadl.setNeverUsePrefixes(true);
		}
		return owl2sadl;
	}

	private void addInstanceDeclaration(org.eclipse.emf.ecore.resource.Resource resource, NamedNode nn, StringBuilder answer) throws ExecutionException, ConfigurationException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException, AmbiguousNameException {
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
						answer = addBlankNodeObject(resource, answer, nn, pobjURI.toString());
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

	private ResultSet runQuery(org.eclipse.emf.ecore.resource.Resource resource, Query q) throws ConfigurationException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException, AmbiguousNameException {
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
			inferenceProcessor.setPreferences(getPreferences());
		}
		return inferenceProcessor;
	}

	private ISadlInferenceProcessor getInferenceProcessor(OntModel theModel) {
		if (inferenceProcessor == null) {
			inferenceProcessor.setPreferences(getPreferences());
			inferenceProcessor.setTheJenaModel(theModel);
		}
		return inferenceProcessor;
	}

	private StringBuilder addBlankNodeObject(org.eclipse.emf.ecore.resource.Resource resource, StringBuilder answer, String bNodeUri) throws ExecutionException, ConfigurationException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException, AmbiguousNameException {
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

	private StringBuilder addBlankNodeObject(org.eclipse.emf.ecore.resource.Resource resource2, StringBuilder answer,
			NamedNode subjNN, String pobjURI) throws ConfigurationException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException, AmbiguousNameException {
		String query = "select ?t ?p ?v where {<" + subjNN.getURI() + "> <" + pobjURI + "> ?bn . ?bn ?p ?v }";
		Query q = new Query();
		q.setSparqlQueryString(query);
		ResultSet rs = runQuery(resource2, q);
		if (rs != null) {
//			rs.setShowNamespaces(false);
			for (int r = 0; r < rs.getRowCount(); r++) {
				if (rs.getResultAt(r, 1).toString().equals(RDF.type.getURI())) {
					rs.setShowNamespaces(false);
					String typ = rs.getResultAt(r, 2).toString();
					answer.append(", has ");
					int predLNIdx = pobjURI.indexOf("#");
					if (predLNIdx > 0) {
						String predLN = pobjURI.substring(predLNIdx + 1);
						answer.append(predLN);
						answer.append(" (a ");
						answer.append(typ);
						answer.append(")");
						break;
					}
				}
			}
		}
		return answer;
	}

	private boolean isBlankNode(String uri) {
		if (uri.startsWith("-") || uri.contains("blank node")) {
			return true;
		}
		return false;
	}

	private void addAnnotationPropertyDeclaration(org.eclipse.emf.ecore.resource.Resource resource, NamedNode nn, StringBuilder answer) {
		answer.append(checkForKeyword(nn.getName()));
		answer.append(" is a type of annotation");
	}

	private void addPropertyWithDomainAndRange(org.eclipse.emf.ecore.resource.Resource resource, NamedNode nn, StringBuilder answer)
			throws ExecutionException, ConfigurationException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException, AmbiguousNameException {
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
			org.apache.jena.rdf.model.Resource p = sitr.nextStatement().getSubject();
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
//		answer.append(System.lineSeparator());
		return isFirstProperty;
	}

	private StringBuilder getClassHierarchy(org.eclipse.emf.ecore.resource.Resource resource, NamedNode nn, StringBuilder answer) throws ExecutionException, ConfigurationException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException, AmbiguousNameException {
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
			StringBuilder answer) throws ExecutionException, ConfigurationException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException, AmbiguousNameException {
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
    
    public String checkForKeyword(String word) {
    	List<String> kwrds = getSadlKeywords();
    	if (kwrds != null && kwrds.contains(word)) {
    		return "^" + word;
    	}
    	return word;
    }
    
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
				logger.debug("Encountered unsupported type flattening Junction: " + lhs.getClass().getCanonicalName());
			}
			GraphPatternElement rhs = ((ProxyNode) cmd.getRhs()).getProxyFor();
			if (rhs instanceof TripleElement) {
				triples.add((TripleElement) rhs);
			}
			else if (rhs instanceof Junction) {
				triples.addAll(flattenJunction((Junction) rhs));
			}
			else {
				logger.debug("Encountered unsupported type flattening Junction: " + rhs.getClass().getCanonicalName());
			}
		}
		else {
			logger.debug("Encountered disjunctive type flattening Junction");
		}
		return triples;
	}

	private String resultSetToQuotedString(ResultSet rs) {
		String resultStr;
		rs.setShowNamespaces(true);
		resultStr = rs.toString();
		return stringToQuotedeString(resultStr);
	}

	private String stringToQuotedeString(String resultStr) {
		resultStr = resultStr.replace('"', '\'');
		resultStr = resultStr.trim();
		resultStr = "\"" + resultStr + "\"";
		return resultStr;
	}

	private Object[] insertRulesAndQuery(org.eclipse.emf.ecore.resource.Resource resource, List<Rule> rules) throws ExecutionException, SadlInferenceException {
		getConfigurationManager().addPrivateKeyMapValueByResource(DialogConstants.ANSWER_CURATION_MANAGER, resource.getURI(), this);
		return getInferenceProcessor().insertRulesAndQuery(resource, rules);
	}

	private Object[] insertTriplesAndQuery(org.eclipse.emf.ecore.resource.Resource resource, List<TripleElement[]> triples) throws ExecutionException, SadlInferenceException {
		getConfigurationManager().addPrivateKeyMapValueByResource(DialogConstants.ANSWER_CURATION_MANAGER, resource.getURI(), this);
		return getInferenceProcessor().insertTriplesAndQuery(resource, triples);
	}
	
	private Object[] insertTriplesAndQuery(org.eclipse.emf.ecore.resource.Resource resource2, TripleElement[] triples) throws SadlInferenceException {
		getConfigurationManager().addPrivateKeyMapValueByResource(DialogConstants.ANSWER_CURATION_MANAGER, resource.getURI(), this);
		return getInferenceProcessor().insertTriplesAndQuery(resource2, triples);
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

	private void addTripleQuestion(org.eclipse.emf.ecore.resource.Resource resource, TripleElement tr, StringBuilder answer) throws ExecutionException, ConfigurationException, TranslationException, InvalidNameException, ReasonerNotFoundException, QueryParseException, QueryCancelledException, AmbiguousNameException {
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
					answer.append(resultSetToQuotedString(rs));
					answer.append("\"");
				}
			}
			else {
				answer.append("\"");
				answer.append(resultSetToQuotedString(rs));
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
	 * @param domainModel 
	 * @param domainModelName 
	 */
	public void processConversation(org.eclipse.emf.ecore.resource.Resource resource, OntModel domainModel, String domainModelName) {
		URI thisResourceUri = resource.getURI();
		if (getDialogAnswerProvider(thisResourceUri) == null) {
//			System.err.println("No DialogAnswerProvider registered for '" + resource.getURI().lastSegment() + "'.");
			return;
		}
		if (thisResourceUri != null && !thisResourceUri.equals(getResourceUri())) {
			// The DialogAnswerProvider has a Resource and this isn't the Resource for which this AnswerCurationManager is intended
			return;
		}
		// ConversationElements are processed in order (must process a save before an evaluate, for example),
		//	but the actual insertion of new responses into the Dialog occurs in reverse order so that the locations
		//	are less complicated to determine
		resetForConversationProcessing(domainModel, domainModelName);
		DialogContent dc = getConversation();
		List<ConversationElement> dialogStmts = dc.getStatements();
		Map<ConversationElement, ConversationElement> additionMap = new HashMap<ConversationElement, ConversationElement>();  // new CE, CE before
		List<ConversationElement> additions = new ArrayList<ConversationElement>();
		List<String> currentQuestions = new ArrayList<String>();
		StatementContent lastStatement = null;
		for (ConversationElement ce : dialogStmts) {
			if (!isSuspendConversationProcessing()) {
				int idx = dialogStmts.indexOf(ce);
				StatementContent statementAfter = idx < dialogStmts.size() - 1 ? dialogStmts.get(idx + 1).getStatement() : null;
				StatementContent sc = ce.getStatement();
				if (sc instanceof SadlStatementContent) {
					if (sc instanceof EquationStatementContent) {
						// look for missing AugmentedType questions and if there are, and **they haven't already been displayed**,
						//	display the questions.
						processMissingEquationContent((EquationStatementContent) sc, dialogStmts);
					}
					// otherwise this might be the answer to a question from CM provided by the user
					if (lastStatement instanceof WhatIsContent && lastStatement.getAgent().equals(Agent.CM)) {
						Object trgt = ((WhatIsContent)lastStatement).getTarget();
						if (trgt instanceof NamedNode) {
							processSadlStatementContentInContextOfPreceedingWhatIs((SadlStatementContent)sc, (WhatIsContent)lastStatement, trgt, dialogStmts, idx);
						}
					}
					else {
						// this is a SADL statement that could change the domain model
						clearDomainModelForTextService();
					}
				}
				else if (sc instanceof AnswerContent) {
					if (sc instanceof AddEquationContent) {
						// Insert new equation **unless it's already been inserted**. Response is an insertion into the 
						//	Dialog window which, when parsed, will create an EquationStatementContent
						processAddEquationContent(resource, dc, additionMap, additions, currentQuestions, ce, statementAfter, (AddEquationContent)sc);
					}
					else if (sc instanceof AddAugmentedTypeInfoContent) {
						// Modify equation with new AugmentedType info **unless it has already been modified**. Response is to
						//	modify the existing equation in the Dialog window which, when parsed, will create a modified EquationStatementContent
						processAddAugmentedTypeInfoContent((AddAugmentedTypeInfoContent)sc);
					}
				}
				else if (sc instanceof ExpectsAnswerContent) {
					// Subclasses CompareContent, EvalContent, ExtractContent, HowManyValueContent, ModifiedAskContent, WhatIsContent, WhatValuesContent can 
					//	come only from Agent.USER and will result in an insertion into the DialogWindow, **unless it has already been inserted**. If already
					//	answered, there will be the resulting InformationContent already later in the conversation.
					
					// Subclasses RequestArgumentAugmentedTypeContent, RequestReturnAugmentedTypeContent come from Agent.CM and may or may not be
					//	followed by an AddAugmentedTypeInfoContent.
	
					if (sc.getAgent().equals(Agent.USER)) {
						processExpectsAnswerContent(resource, dc, additionMap, additions, currentQuestions, ce, statementAfter, (ExpectsAnswerContent) sc);
					}
					else if (sc instanceof QuestionContent){
						// this is a question that was created for the CM to ask during JBDMP processing
						//	if not already asked, ask it
						processCMExpectsAnswerContent((QuestionContent)sc);
					}
				}
				lastStatement = sc;
				setLastStatement(lastStatement);
			}
		}
		if (!additions.isEmpty()) {
			for (int i = additions.size() - 1; i >= 0; i--) {
				ConversationElement newCE = additions.get(i);
				ConversationElement preceeding = additionMap.get(newCE);
				logger.debug("Conversation element '" + preceeding.toString() + "' has new addition following it:");
				logger.debug("   " + newCE.toString());
				logger.debug("   Answer is: " + getQuestionsAndAnswers().get(preceeding.getText().trim()));
			}
		}
		else {
			// additions is empty so there haven't been any other things added in this pass so now add any imports
			if (getDelayedImportAdditions() != null && getDelayedImportAdditions().size() > 0) {
				Object dap = getConfigurationManager().getPrivateKeyMapValueByResource(DialogConstants.DIALOG_ANSWER_PROVIDER, resource.getURI());
				if (dap instanceof IDialogAnswerProvider) {
					((IDialogAnswerProvider)dap).addImports(getDelayedImportAdditions());
				}
				getDelayedImportAdditions().clear();
			}
		}
		Map<String, String> qna = getQuestionsAndAnswers();
		if (!currentQuestions.isEmpty()) {
			if (!qna.isEmpty()) {
				List<String> toBeRemoved = new ArrayList<String>();
				Iterator<String> itr = qna.keySet().iterator();
				while (itr.hasNext()) {
					String key = itr.next();
					if (!currentQuestions.contains(key)) {
						toBeRemoved.add(key);
					}
				}
				if (!toBeRemoved.isEmpty()) {
					for (String tbr : toBeRemoved) {
						qna.remove(tbr);
					}
				}
			}
		}
		else {
			// there are no questions, clear qna
			qna.clear();
		}
	}

	private void setLastStatement(StatementContent lastStatement) {
		this.lastStatement = lastStatement;
	}
	
	private StatementContent getLastStatement() {
		return this.lastStatement;
	}

	private void processSadlStatementContentInContextOfPreceedingWhatIs(SadlStatementContent sc,
			WhatIsContent lastStatement, Object trgt, List<ConversationElement> dialogStmts, int conversationIdx) {
		// need the equation to modify
		String question = lastStatement.getText();
		String semTypeUri = sc.getConceptUri();

		addAugmentedTypeToEquation(dialogStmts, conversationIdx, question, semTypeUri);
	}

	private String addAugmentedTypeToEquation(AddAugmentedTypeInfoContent sc) {
		EquationStatementContent eqsc = sc.getEquationContent();
		NamedNode targetNode = ((AddAugmentedTypeInfoContent)sc).getTargetNode();
		Node semType = ((AddAugmentedTypeInfoContent)sc).getAddedType();
		if (semType instanceof NamedNode && ((NamedNode)semType).getNodeType().equals(NodeType.ClassNode)) {
			OntClass augTypeClass = getDomainModel().getOntClass(((NamedNode)semType).getURI());
			if (augTypeClass != null) {
				EObject equationEObject = eqsc.getHostEObject();
				addAugmentedTypeToEquation(eqsc, equationEObject , targetNode instanceof VariableNode ? targetNode.getName() : null, augTypeClass.getLocalName());
				return "Added augmented type '" + augTypeClass.getURI() + "' to " + eqsc.getEquationName();
			}
		}
		else if (semType instanceof ProxyNode) {
			EObject equationEObject = eqsc.getHostEObject();
			String augTypeTxt = sc.getUnParsedText();
			addAugmentedTypeToEquation(eqsc, equationEObject , targetNode instanceof VariableNode ? targetNode.getName() : null, augTypeTxt);
			return "Added augmented type '" + augTypeTxt + "' to " + eqsc.getEquationName();
		}
		return "Failed to add augmented type";
	}

	private String addAugmentedTypeToEquation(List<ConversationElement> dialogStmts, int conversationIdx, String question,
			String semTypeUri) {
		for (int i = conversationIdx - 1; i >= 0; i--) {
			ConversationElement priorCe = dialogStmts.get(i);
			if (priorCe.getStatement() instanceof EquationStatementContent) {
				EquationStatementContent eqsc = (EquationStatementContent)priorCe.getStatement();
				List<StatementContent> questions = eqsc.getQuestionsForUser();
				if (questions != null) {
					for (StatementContent qsc : questions) {
						String qscQuestion = null;
						EObject equationEObject = null;
						String argNameToUpdate = null;
						if (qsc instanceof RequestArgumentAugmentedTypeContent) {
							qscQuestion = stripEOS(((RequestArgumentAugmentedTypeContent)qsc).getQuestion());
							equationEObject = ((RequestArgumentAugmentedTypeContent)qsc).getHostEObject();
							argNameToUpdate = ((RequestArgumentAugmentedTypeContent)qsc).getArgumentName();
						}
						else if (qsc instanceof RequestReturnAugmentedTypeContent) {
							qscQuestion = stripEOS(((RequestReturnAugmentedTypeContent)qsc).getQuestion());
							equationEObject = ((RequestReturnAugmentedTypeContent)qsc).getHostEObject();
						}
						if (qscQuestion != null && qscQuestion.equals(question)) {
							// we have the equation and the question
							OntClass cls = getDomainModel().getOntClass(semTypeUri);
							if (cls != null) {
								addAugmentedTypeToEquation(eqsc, equationEObject, argNameToUpdate, cls.getLocalName());
								return "Added augmented type '" + cls.getURI() + "' to " + eqsc.getEquationName();
							}
							else {
								// error
							}
						}
					}
				}
			}
		}
		return "failure";
	}

	private void addAugmentedTypeToEquation(EquationStatementContent eqsc, EObject equationEObject,
			String argNameToUpdate, String augTypeTxt) {
		if (argNameToUpdate != null) {
			// update argument with semantic type
			String eqTxt = eqsc.getText();
			int sigStart = eqTxt.indexOf("(");
			int argStart = eqTxt.indexOf(argNameToUpdate, sigStart);
			int insertLoc = argStart + argNameToUpdate.length();
			String follows = eqTxt.substring(insertLoc + 1).trim();
			if (follows.startsWith("(")) {
				String fPlus1 = follows.substring(1).trim();
				if (fPlus1.startsWith("note") || fPlus1.startsWith("alias") || fPlus1.startsWith("see")) {
					char c;
					do {
						c = eqTxt.charAt(insertLoc);
						insertLoc++;
					} while (!(c == '"') && !(c == '\''));		// this gets us past the keyword to the opening quote
					char qc = c;
					do {
						c = eqTxt.charAt(insertLoc);
						insertLoc++;
					} while (!(c == qc));
					do {
						c = eqTxt.charAt(insertLoc);
						insertLoc++;
					} while (!(c == ')'));
				}
			}
			StringBuilder newEqTxt = new StringBuilder(eqTxt.subSequence(0, insertLoc));
			newEqTxt.append(" (");
			newEqTxt.append(correctArticlesInAugmentedType(eqTxt, insertLoc, augTypeTxt));
			newEqTxt.append(")");
			newEqTxt.append(eqTxt.substring(insertLoc));
			String replacementTxt = newEqTxt.toString();
//			System.out.println(replacementTxt);
			replaceDialogText(equationEObject, eqTxt, replacementTxt);
			return;
		}
		else {
			// update return statement with semantic type
			String eqTxt = eqsc.getText();
			int sigEnd = eqTxt.indexOf("returns");
			int insertLoc = eqTxt.indexOf(":", sigEnd);
			StringBuilder newEqTxt = new StringBuilder(eqTxt.subSequence(0, insertLoc));
			newEqTxt.append(" (");
			newEqTxt.append(correctArticlesInAugmentedType(eqTxt, insertLoc, augTypeTxt));
			newEqTxt.append(")");
			newEqTxt.append(eqTxt.substring(insertLoc));
			String replacementTxt = newEqTxt.toString();
//			System.out.println(replacementTxt);
			replaceDialogText(equationEObject, eqTxt, replacementTxt);
			return;
		}
	}

	private Object correctArticlesInAugmentedType(String eqTxt, int insertLoc, String augTypeTxt) {
		// if the concept is already referenced in the equation before the insertion point, the article should be definite.
		//	(if an independent additional reference is intended the human will need to collaboratively alter)
		String eqBefore = eqTxt.substring(0, insertLoc);
		eqBefore = eqBefore.replace('(',  ' ');
		eqBefore = eqBefore.replace(')', ' ');
		List<String> eqPieces = Arrays.asList(eqBefore.split("\\s+"));
		String[] augPieces = augTypeTxt.split("\\s+");
		if (augPieces != null && augPieces.length > 1) {
			for (int i = 0; i < augPieces.length - 1; i++) {
				String piece = augPieces[i];
				if (piece.equals("an") || piece.equals("the")) {
					String noun = augPieces[i+1];
					if (eqPieces.contains(noun)) {
						// make sure this noun has a definite article
						augPieces[i] = "the";
					}
					else {
						char ch = noun.toLowerCase().charAt(0);
						if (ch == 'a' || ch == 'e' || ch == 'i' || ch == 'o' || ch == 'u') {
							augPieces[i] = "an";
						}
						else {
							augPieces[i] = "a";
						}
					}
				}
			}
			StringBuilder sb = new StringBuilder();
			for (int i = 0; i < augPieces.length; i++) {
				if (i > 0) {
					sb.append(" ");
				}
				sb.append(augPieces[i]);
			}
			return sb.toString();
		}
		return augTypeTxt;
	}

	private void processAddAugmentedTypeInfoContent(AddAugmentedTypeInfoContent sc) {
		// TODO Auto-generated method stub
		int i = 0;
	}

	private void processAddEquationContent(org.eclipse.emf.ecore.resource.Resource resource2, DialogContent dc, Map<ConversationElement, ConversationElement> additionMap, List<ConversationElement> additions, List<String> currentQuestions, 
			ConversationElement ce, StatementContent statementAfter, AddEquationContent sc) {
		processExpectsAnswerContent(resource, dc, additionMap, additions, currentQuestions, ce, statementAfter, sc);
	}

	private void processExpectsAnswerContent(org.eclipse.emf.ecore.resource.Resource resource,
			DialogContent dc, Map<ConversationElement, ConversationElement> additionMap,
			List<ConversationElement> additions, List<String> currentQuestions, ConversationElement ce,
			StatementContent statementAfter, ExpectsAnswerContent sc) {
		StatementContent lastStatement;
		String question = sc.getText().trim();
		currentQuestions.add(question);
		if (sc instanceof AddAugmentedTypeInfoContent) {
			// for these we must look back at the equation ands see if there's still a question
			NamedNode targetNode = ((AddAugmentedTypeInfoContent)sc).getTargetNode();
			String atquestion;
			if (targetNode instanceof VariableNode) {
				atquestion = "What type is " + targetNode.getName();
			}
			else {
				atquestion = "What type does " + targetNode.getName() + " return";
			}
			EquationStatementContent eqcnt = ((AddAugmentedTypeInfoContent)sc).getEquationContent();
			List<StatementContent> questions = eqcnt.getQuestionsForUser();
			boolean questionFound = false;
			if (questions != null) {
				for (StatementContent anotherSc : questions) {
					String anotherTxt = null;
					if (anotherSc instanceof RequestArgumentAugmentedTypeContent) {
						anotherTxt = ((RequestArgumentAugmentedTypeContent) anotherSc).getQuestion();
					}
					else if (anotherSc instanceof RequestReturnAugmentedTypeContent) {
						anotherTxt = ((RequestReturnAugmentedTypeContent)anotherSc).getQuestion();
					}
					else {
						anotherSc.getText();
					}
					if (stripEOS(anotherTxt).equals(stripEOS(atquestion))) {
						questionFound = true;
						break;
					}
				}
			}
			if (questionFound) {
				processExpectsAnswerContent(resource, dc, additionMap, additions, ce, sc);	
			}
		}
		else {
			// for these we can look forward to see if the question has been answered
			if (getQuestionsAndAnswers().containsKey(question)) {
				String ans = getQuestionsAndAnswers().get(question);
	//					logger.debug(ans + " ? " + ((AnswerContent)statementAfter).getAnswer().toString().trim());
				if (ans.equals(SUSPENDED_EXTRACTION_PENDING_DOMAIN_MODEL_ADDITIONS)) {
					// We need to set the extractionContext to after the additions
					List<ConversationElement> celements = getConversationElements();
					int idx = findStatementContentIndexInConversation(celements, sc);
					if (idx >= 0) {
						idx++;
						StatementContent lastSc = null;
						while (idx < celements.size()) {
							ConversationElement ace = celements.get(idx++);
							if (ace.getAgent().equals(Agent.CM) || ace.getStatement() instanceof SadlStatementContent) {
								lastSc = ace.getStatement();
							}
						}
						if (lastSc != null) {
							setExtractionContext(lastSc);
						}
					}
					getExtractionProcessor().setCodeModel(getSuspendedCodeModel());
					try {
						String status = continueExtraction(getSuspendedExtractionContent());
						getQuestionsAndAnswers().put(question, status);
						saveSuspendedExtractionContent(null);
						saveSuspendedCodeModel(null);
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (ConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
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
					} catch (InvalidInputException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (AmbiguousNameException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				if (statementAfter != null && statementAfter != null) {
					String val = statementAfter.toString().trim();
					val = stripEOS(SadlUtils.stripQuotes(val));
					ans = stripEOS(SadlUtils.stripQuotes(ans));
					if (val.equals(ans)) {
						// this statement has already been answered		
						if (tryToAnswerAgain(sc, question, ans)) {							
							// try again
							processExpectsAnswerContent(resource, dc, additionMap, additions, ce, sc);
						}
						else {
							((ExpectsAnswerContent) sc).setAnswer(statementAfter);
						}
					}
				}
				else {
					try {
						answerUser(getOwlModelsFolder(), ans, sc.isQuoteResult(), sc.getHostEObject());
						addQuestionAndAnswer(question, ans);
					} catch (ConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}	
				}
			}
			else if (sc instanceof QuestionWithCallbackContent) {
				String key = getUnansweredQuestionKey((QuestionWithCallbackContent)sc);
				if (key != null) {
					replaceUnansweredQuestionStatementContent(key, (QuestionWithCallbackContent)sc);
					lastStatement = sc;
				}
			}
	
			// this statement needs an answer
			else {
				processExpectsAnswerContent(resource, dc, additionMap, additions, ce, sc);
			}
		}
	}

	private boolean tryToAnswerAgain(ExpectsAnswerContent sc, String question, String ans) {
		if (ans.startsWith("No model found to compute")) {
			// Has a new model been added by the user or 
			//  has extraction occurred between this question and the next since the last time we considered this
			//	or has an equation been edited so that it is now complete but was not before?
			List<ConversationElement> celements = getConversationElements();
			int idx = findStatementContentIndexInConversation(celements, sc);
			if (idx >= 0) {
				for (int i = idx + 1; i < celements.size(); i++) {
					StatementContent someSc = celements.get(i).getStatement();
					if (someSc instanceof WhatIsContent) {
						// this is the next question from the user 
						if (((WhatIsContent)someSc).getAgent().equals(Agent.USER)) {
							return false;
						}
					}
					if (someSc instanceof EquationStatementContent) {
						if (isNewlyCompletedEquation((EquationStatementContent)someSc)) {
							return true;
						}
					}
				}
			}
		}
		else if (ans.startsWith("Failed")) {
			// not sure yet what's needed here...
		}
		return false;
	}

	private void addFailureCorrectingAction(StatementContent someSc) {
		failureCorrectingActions.add(someSc);
	}

	private boolean isFailureCorrectingAction(StatementContent someSc) {
		for (StatementContent sc : failureCorrectingActions) {
			String scstr = sc.toString();
			if (scstr != null && someSc != null && scstr.equals(someSc.toString())) {
				return true;
			}
		}
		return false;
	}

	private String stripEOS(String stmt) {
		if (stmt.endsWith(".") || stmt.endsWith("?")) {
			stmt = stmt.substring(0, stmt.length() - 1);
		}
		return stmt;
	}

	private void processCMExpectsAnswerContent(QuestionContent sc) {
		if (!questionHasBeenAsked(getConversationElements(), sc)) {
			if (sc instanceof WhatIsContent && ((WhatIsContent)sc).getExplicitQuestion() != null) {
				sc.setUnParsedText(((WhatIsContent)sc).getExplicitQuestion());
			}
			notifyUser(getOwlModelsFolder(), sc, false);
		}
	}

	private boolean questionHasBeenAsked(List<ConversationElement> conversationElements, QuestionContent sc) {
		if (conversationElements != null) {
			int scLoc = findStatementContentIndexInConversation(conversationElements, sc);
			for (int idx = scLoc; idx < conversationElements.size(); idx++) {
				ConversationElement ce = conversationElements.get(idx);
				if (ce.getStatement() instanceof UndefinedConceptStatementContent) {
					if (((UndefinedConceptStatementContent)ce.getStatement()).getText().equals(sc.getExplicitQuestion())) {
						return true;
					}
				}
				else if (ce.getStatement() instanceof NoModelFoundStatementContent) {
					if (((NoModelFoundStatementContent)ce.getStatement()).getText().equals(sc.getExplicitQuestion())) {
						return true;
					}
				}
				else if (ce.getStatement() instanceof WhatIsContent) {
					if (((WhatIsContent)ce.getStatement()).getText().equals(sc.getUnParsedText())) {
						return true;
					}
				}
			}
		}
		return false;
	}

	/** Method to find a StatementContent in the conversation elements
	 * 
	 * @param conversationElements
	 * @param sc
	 * @return
	 */
	private int findStatementContentIndexInConversation(List<ConversationElement> conversationElements, StatementContent sc) {
		if (conversationElements != null) {
			for (int idx = 0; idx < conversationElements.size(); idx++) {
				ConversationElement ce = conversationElements.get(idx);
				if (ce.getStatement().equals(sc)) {
						return idx;
				}
			}
		}
		return -1;
	}

	private void processExpectsAnswerContent(org.eclipse.emf.ecore.resource.Resource resource, DialogContent dc, 
			Map<ConversationElement, ConversationElement> additionMap,
			List<ConversationElement> additions, ConversationElement ce, ExpectsAnswerContent sc) {
		try {
			String answer = processUserRequest(resource, sc);
//						String answer = getDialogAnswerProvider().processUserQueryNewThreadWithBusyIndicator(resource, ontModel, modelName, (ExpectsAnswerContent) sc);
			if (answer != null) {
				addQuestionAndAnswer(sc.getText().trim(), answer.trim());
				AnswerPendingContent pending = new AnswerPendingContent(null, Agent.CM, (StatementContent) sc);
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
		} catch (EquationNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AnswerExtractionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (AmbiguousNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private void processQuestionUserContent(StatementContent sc) {
		String question = null;
		if (sc instanceof UndefinedConceptStatementContent) {
			question = ((UndefinedConceptStatementContent)sc).getText();
		}
		else if (sc instanceof NoModelFoundStatementContent) {
			question = ((NoModelFoundStatementContent)sc).getText();
		}
		if (question != null) {
			try {
				askUser(getOwlModelsFolder(), question, false, sc.getHostEObject());
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
		
	private void processMissingEquationContent(EquationStatementContent eqsc, List<ConversationElement> dialogStmts) {
		String eqUri = getDomainModelName() + "#" + eqsc.getEquationName();
		List<StatementContent> questions = eqsc.getQuestionsForUser();
		if (questions != null) {
			for (StatementContent sc : questions) {
				if (!questionHasBeenAsked(dialogStmts, eqsc, sc)) {
					String questionKey = null;
					String question = null;
					if (sc instanceof RequestArgumentAugmentedTypeContent) {
						question = ((RequestArgumentAugmentedTypeContent)sc).getQuestion();
						questionKey = ((RequestArgumentAugmentedTypeContent)sc).getEquationName() + ((RequestArgumentAugmentedTypeContent)sc).getArgumentName();
					}
					else if (sc instanceof RequestReturnAugmentedTypeContent) {
						question = ((RequestReturnAugmentedTypeContent)sc).getQuestion();
						questionKey = ((RequestReturnAugmentedTypeContent)sc).getEquationName() + "_returns";
					}
					if (question != null) {
						try {
							if (getAugmentedTypeQuestionAsked(questionKey) == null) {
								askUser(getOwlModelsFolder(), question, false, sc.getHostEObject());
								addAugmentedTypeQuestionAsked(questionKey, question);
							}
						} catch (ConfigurationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}
				recordEquationCompleteness(eqUri, null);	// replace as it might have been complete before
			}
		}
		else {
			// this equation is complete
			recordEquationCompleteness(eqUri, eqsc.getText());
		}
	}

	private boolean questionHasBeenAsked(List<ConversationElement> dialogStmts, EquationStatementContent equationSc,
			StatementContent questionSc) {
		String eqName = equationSc.getEquationName();
		int cntr = 0;
		int eqIdx = -1;
		for (ConversationElement ce :dialogStmts) {
			StatementContent cesc = ce.getStatement();
			if (eqIdx < 0 && cesc.equals(equationSc)) {
				// found the equation
				eqIdx = cntr;
			}
			if (eqIdx >= 0 && cntr > eqIdx) {
				// look for this question until we see another EquationStatementContent
				if (cesc instanceof EquationStatementContent) {
					return false;
				}
				
				boolean sameArgName = false;
				boolean sameQuestion = false;
				if (cesc instanceof WhatIsContent) {
					Object trgtObj = ((WhatIsContent)cesc).getTarget();
					String cescTarget = trgtObj instanceof NamedNode ? ((NamedNode)trgtObj).getName() : trgtObj.toString();
					String cescQuestion = stripEOS(cesc.getText());
					if (questionSc instanceof RequestArgumentAugmentedTypeContent) {
						if (((RequestArgumentAugmentedTypeContent)questionSc).getArgumentName().equals(cescTarget)) {
							sameArgName = true;
						}
						String qscQuestion = stripEOS(((RequestArgumentAugmentedTypeContent)questionSc).getQuestion());
						if (qscQuestion.equals(cescQuestion)) {
							sameQuestion = true;
						}
					}
					else if (questionSc instanceof RequestReturnAugmentedTypeContent) {
						if (((RequestReturnAugmentedTypeContent)questionSc).getEquationName().equals(cescTarget)) {
							sameArgName = true;
						}
						String qscQuestion = stripEOS(((RequestReturnAugmentedTypeContent)questionSc).getQuestion());
						if (qscQuestion.equals(cescQuestion)) {
							sameQuestion = true;
						}
					}
					if (sameArgName && sameQuestion) {
						return true;
					}
				}
			}
			cntr++;
		}
		return false;
	}

	/**
	 * In this method do any cleanup necessary before starting to process the conversation anew
	 * @param domainModelName2 
	 */
	private void resetForConversationProcessing(OntModel domainModel, String domainModelName) {
		this.owl2sadl = null;
		clearConceptsAdded();
		getConfigurationManager().clearReasoner();
//		clearInferenceProcessor();
		setDomainModel(domainModel);
		setDomainModelName(domainModelName);
		setLocalityURI(domainModelName + "_txt#");
		setLocalityPrefix("txt");
		setSuspendConversationProcessing(false);
	}

	private void setSimplifiedDomainModelForTextService(OntModel sdmfts) {
		simplifiedDomainModelForTextService = sdmfts;
	}
	
	private OntModel getSimplifiedDomainModelForTextService() throws ConfigurationException {
		if (simplifiedDomainModelForTextService == null) {
			setSimplifiedDomainModelForTextService(getDomainModelExtractForTextService(true));
		}
		return simplifiedDomainModelForTextService;
	}
	
	/** 
	 * Method to clear the domain model uploaded to the textToTriples service so
	 * that a new one wil be uploaded before the next use of the service
	 * @return
	 */
	private boolean clearDomainModelForTextService() {
		if (simplifiedDomainModelForTextService != null) {
			simplifiedDomainModelForTextService = null;
			return true;
		}
		return false;
	}
	
	/**
	 * Method to determine if the domain model for the dialog window has been uploaded since last change
	 * @return
	 */
	private boolean isDomainModelForTextSericeUploaded() {
		return (simplifiedDomainModelForTextService != null);
	}

	private boolean applyAnswerToUnansweredQuestion(StatementContent question, StatementContent sc) {
		if (question instanceof QuestionWithCallbackContent && sc instanceof AnswerContent) {
			List<Object> args = ((QuestionWithCallbackContent)question).getArguments();
			if (args != null) {
				if (args.size() > 0) {
					if (!args.get(args.size() - 1).equals(((AnswerContent)sc).getAnswer())) {
						((QuestionWithCallbackContent)question).getArguments().add(((AnswerContent)sc).getAnswer());					
					}
				}
				else {
					((QuestionWithCallbackContent)question).getArguments().add(((AnswerContent)sc).getAnswer());					
				}	
			}
			provideResponse((QuestionWithCallbackContent) question);
			String key = getUnansweredQuestionKey((QuestionWithCallbackContent) question);
			getUnansweredQuestions().remove(key);
			addQuestionAndAnswer(key, ((AnswerContent)sc).getAnswer().toString());
			((QuestionWithCallbackContent)question).setAnswer((AnswerContent) sc);
			return true;
		}
		return false;
	}

	public void provideResponse(QuestionWithCallbackContent question) {
		String methodToCall = question.getMethodToCall();
		Method[] methods = this.getClass().getMethods();
		for (Method m : methods) {
			if (m.getName().equals(methodToCall)) {
				// call the method
				List<Object> args = question.getArguments();
				try {
					Object results = null;
					if (args.size() == 0) {
						results = m.invoke(this, null);
					}
					else {
						Object arg0 = args.get(0);
						if (args.size() == 1) {
							results = m.invoke(this, arg0);
						}
						else {
							Object arg1 = args.get(1);
							if (args.size() == 2) {
								results = m.invoke(this, arg0, arg1);
							}
							if (methodToCall.equals("saveAsSadlFile")) {
								Set owlFiles = ((Map)arg0).keySet();
								Iterator owlFilesItr = owlFiles.iterator();
								if (AnswerCurationManager.isYes(arg1)) {
									while (owlFilesItr.hasNext()) {
										File owlfile = (File)owlFilesItr.next();
										// delete OWL file so it won't be indexed
										if (owlfile.exists()) {
											owlfile.delete();
											try {
												String outputOwlFileName = owlfile.getCanonicalPath();
												String altUrl = new SadlUtils().fileNameToFileUrl(outputOwlFileName);

												String publicUri = getConfigurationManager().getPublicUriFromActualUrl(altUrl);
												if (publicUri != null) {
													getConfigurationManager().deleteModel(publicUri);
													getConfigurationManager().deleteMapping(altUrl, publicUri);
												}
											} catch (ConfigurationException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											} catch (URISyntaxException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											} catch (IOException e) {
												// TODO Auto-generated catch block
												e.printStackTrace();
											}
										}
									}
									String projectName = null;
									List<File> sadlFiles = new ArrayList<File>();
									for (int i = 0; i < ((List<?>) results).size(); i++) {
										Object result = ((List<?>) results).get(i);
										File sf = new File(result.toString());
										if (sf.exists()) {
											sadlFiles.add(sf);
											projectName = sf.getParentFile().getParentFile().getName();
										}
									}
									getDialogAnswerProvider(getResourceUri()).updateProjectAndDisplaySadlFiles(projectName, getOwlModelsFolder(), sadlFiles);
								}
								else {
									while (owlFilesItr.hasNext()) {
										File owlFile = (File) owlFilesItr.next();	
										// add import of OWL file from policy file since there won't be a SADL file to build an OWL and create mappings.
										try {
											String importActualUrl = new SadlUtils().fileNameToFileUrl(owlFile.getCanonicalPath());
											String altUrl = new SadlUtils().fileNameToFileUrl(importActualUrl);
											String importPublicUri = getConfigurationManager().getPublicUriFromActualUrl(altUrl);
											String prefix = getConfigurationManager().getGlobalPrefix(importPublicUri);
											getConfigurationManager().addMapping(importActualUrl, importPublicUri, prefix, false, "AnswerCurationManager");
											String prjname = owlFile.getParentFile().getParentFile().getName();
										} catch (IOException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (URISyntaxException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										} catch (ConfigurationException e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									}
								}
							}
						}
					}
					//						logger.debug(result.toString());
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
				break;
			}
		}
		//		}	
	}

	private void replaceUnansweredQuestionStatementContent(String key, QuestionWithCallbackContent sc) {
		if (key != null) {
			QuestionWithCallbackContent uq = getUnansweredQuestions().get(key);
			sc.setMethodToCall(uq.getMethodToCall());
			sc.setArguments(uq.getArguments());
			getUnansweredQuestions().put(key, sc);
		}
	}

	private String getUnansweredQuestionKey(QuestionWithCallbackContent sc) {
		String txt2 = sc.getTheQuestion();
		for (String uaq : getUnansweredQuestions().keySet()) {
			String trimmed = stripEOS(uaq);
			trimmed = stripEOS(SadlUtils.stripQuotes(trimmed.trim()));
			if (trimmed.equals(txt2)) {
				return uaq;
			}
		}
		return null;
	}

	private Map<String, String> getQuestionsAndAnswers() {
		return questionsAndAnswers;
	}
	
	/**
	 * Method to find an answer using the question text as key.
	 * @param question
	 * @return
	 */
	public String getAnswerToQuestion(String question) {
		if (questionsAndAnswers != null) {
			if (questionsAndAnswers.containsKey(question)) {
				return questionsAndAnswers.get(question);
			}
		}
		return null;
	}

	private boolean addQuestionAndAnswer(String question, String answer) {
		if (questionsAndAnswers.containsKey(question)) {
			String oldAnswer = questionsAndAnswers.get(question);
			if (oldAnswer.equals(answer)) {
				return false;
			}
		}
		questionsAndAnswers.put(question, answer);
		return true;
	}

	public void clearQuestionsAndAnsers() {
		questionsAndAnswers.clear();
		setDialogAnswerProvider(null);
	}
	
	public boolean saveQuestionsAndAnswersToFile() throws ConfigurationException, IOException {
		if (getResourceUri() != null) {
			String rsrcFn = getResourceUri().lastSegment();
			String qnaFn = getOwlModelsFolder() + "/" + rsrcFn + ".qna.owl";
			Map<String, String> qna = getQuestionsAndAnswers();
			if (qna != null && !qna.isEmpty()) {
				// write out Q and Q mappings
				String modelName = "http://com.ge.research.ask/Dialog/QNA/" + rsrcFn;
				OntDocumentManager owlDocMgr = getConfigurationManager().getJenaDocumentMgr();
				OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
				if (getOwlModelsFolder() != null && !getOwlModelsFolder().startsWith(IModelProcessor.SYNTHETIC_FROM_TEST)) {
					File mff = new File(getOwlModelsFolder());
					mff.mkdirs();
					spec.setImportModelGetter(new SadlJenaModelGetterPutter(spec, getOwlModelsFolder()));
				}
				if (owlDocMgr != null) {
					spec.setDocumentManager(owlDocMgr);
					owlDocMgr.setProcessImports(true);
				}
				OntModel qnaModel = ModelFactory.createOntologyModel(spec);
	//			qnaModel.addSubModel(getDomainModelConfigurationManager().getOntModel(SadlConstants.SADL_IMPLICIT_MODEL_URI, Scope.LOCALONLY));
				OntClass qclass = qnaModel.createClass(DialogConstants.SADL_IMPLICIT_MODEL_QUESTION_ELEMENT_URI);
				OntClass aclass = qnaModel.createClass(DialogConstants.SADL_IMPLICIT_MODEL_ANSWER_ELEMENT_URI);
				OntProperty txtprop = qnaModel.createOntProperty(DialogConstants.SADL_IMPLICIT_MODEL_TEXT_PROPERY_URI);
				OntProperty haprop = qnaModel.createOntProperty(DialogConstants.SADL_IMPLICIT_MODEL_HAS_ANSWER_PROPERY_URI);
				if (qclass == null || aclass == null || txtprop == null || haprop == null) {
					logger.debug("Unable to save mappings between questions and answers, no meta-model found. Do you need to update the SadlImplicitModel?");
				}
				else {
					Iterator<String> itr = qna.keySet().iterator();
					while (itr.hasNext()) {
						String qtxt = itr.next();
						String atxt = qna.get(qtxt);
						Individual qInst = qnaModel.createIndividual(qclass);
						qInst.addProperty(txtprop, qnaModel.createTypedLiteral(qtxt));
						Individual aInst = qnaModel.createIndividual(aclass);
						aInst.addProperty(txtprop,  qnaModel.createTypedLiteral(atxt));
						qInst.addProperty(haprop, aInst);
					}
					getConfigurationManager().saveOwlFile(qnaModel, modelName, qnaFn);
					return true;
				}
			}
			else {
				// delete file if there's no content for it
				File owlFile = new File(qnaFn);
				if (owlFile.exists()) {
					return owlFile.delete();
				}
			}	
		}
		return false;
	}

	public boolean loadQuestionsAndAnswersFromFile() {
		if (getResourceUri() != null) {
			String rsrcFn = getResourceUri().lastSegment();
			String qnaFn = getOwlModelsFolder() + "/" + rsrcFn + ".qna.owl";
			File qnaFile = new File(qnaFn);
			if (qnaFile.exists()) {
				// read in Q and A mappings
				OntModel qnaModel = getConfigurationManager().loadOntModel(qnaFn);
				OntProperty txtprop = qnaModel.createOntProperty(DialogConstants.SADL_IMPLICIT_MODEL_TEXT_PROPERY_URI);
				OntProperty haprop = qnaModel.createOntProperty(DialogConstants.SADL_IMPLICIT_MODEL_HAS_ANSWER_PROPERY_URI);
				StmtIterator sitr = qnaModel.listStatements(null, haprop, (RDFNode)null);
				while (sitr.hasNext()) {
					Statement stmt = sitr.nextStatement();
					Resource subj = stmt.getSubject();
					RDFNode obj = stmt.getObject();
					Statement qtxtpropstmt = subj.getProperty(txtprop);
					if (qtxtpropstmt != null) {
						String qtxt = qtxtpropstmt.getObject().asLiteral().getValue().toString();
						if (obj.isResource()) {
							Statement atxtpropstmt = obj.asResource().getProperty(txtprop);
							if (atxtpropstmt != null) {
								String atxt = atxtpropstmt.getObject().asLiteral().getValue().toString();
								addQuestionAndAnswer(qtxt, atxt);
							}
						}
					}
				}
				return true;
			}
		}
		return false;
	}

	public XtextResource getResource() {
		return resource;
	}

	private void setResource(XtextResource resource) {
		this.resource = resource;
	}

	public Map<String, QuestionWithCallbackContent> getUnansweredQuestions() {
		return unansweredQuestions;
	}

	public void addUnansweredQuestion(String questionString, QuestionWithCallbackContent unansweredQuestion) {
		unansweredQuestions.put(questionString, unansweredQuestion);
	}

	public static String getImportOutputFilename(String outputFilename) {
		String outputOwlFileName = outputFilename + ".owl";
		if (outputFilename.endsWith(".sadl")) {
			outputFilename = outputFilename.substring(0, outputFilename.length() - 5) + ".owl";
		}
		return outputOwlFileName;
	}

	private IReasoner getTextModelReasoner() {
		return textModelReasoner;
	}

	private void setTextModelReasoner(IReasoner textModelReasoner) {
		this.textModelReasoner = textModelReasoner;
	}

	public List<String> getDelayedImportAdditions() {
		return delayedImportAdditions;
	}

	public void addDelayedImportAddition(String delayedImportAddition) {
		if (delayedImportAdditions == null) {
			delayedImportAdditions = new ArrayList<String>();
		}
		delayedImportAdditions.add(delayedImportAddition + System.lineSeparator());
		getDialogAnswerProvider(getResourceUri()).addImports(delayedImportAdditions);
		delayedImportAdditions.clear();
	}

	public OntModel getDomainModel() {
		if (domainModel == null && resource != null) {
			domainModel = OntModelProvider.find(resource);
		}
		return domainModel;
	}

	public void setDomainModel(OntModel domainModel) {
		this.domainModel = domainModel;
	}

	public void setDomainModelName(String domainModelName) {
		this.domainModelName = domainModelName;
	}
	
	public String getDomainModelName() {
		return this.domainModelName;
	}

	public Map<String, List<String>> getUnmatchedUrisAndLabels() {
		return unmatchedUrisAndLabels;
	}

	private void setUnmatchedUrisAndLabels(Map<String, List<String>> unmatchedUrisAndLabels) {
		this.unmatchedUrisAndLabels = unmatchedUrisAndLabels;
	}

	public void addEquationInformation(String eqUri, String elementStr) {
		if (equationInformation == null) {
			equationInformation = new HashMap<String, List<String>>();
		}
		if (!equationInformation.containsKey(eqUri)) {
			equationInformation.put(eqUri, new ArrayList<String>());
		}
		if (!equationInformation.get(eqUri).contains(elementStr) ) {
			equationInformation.get(eqUri).add(elementStr);
		}
	}
	
	public List<String> getEquationInformation(String eqUri) {
		if (equationInformation != null) {
			return equationInformation.get(eqUri);
		}
		return null;
	}

	/**
	 * Method to convert regular Python to Tensor-Flow-compatible Python
	 * @param pythonScript
	 * @return
	 */
	public String pythonToTensorFlowPython(String pythonScript) {
		String modifiedScript;		
		if (pythonScript.contains(" math.")) {
			modifiedScript = pythonScript.replaceAll("math.", "tf.math.");
		}
		else {
			modifiedScript = pythonScript.replaceAll("Math.", "tf.math.");
		}
		modifiedScript = modifiedScript.replaceAll("tf.math.PI", "tf.pi");
		modifiedScript = modifiedScript.replaceAll("tf.math.E", "tf.e");
		modifiedScript = modifiedScript.replaceAll("tf.math.max", "tf.maximum");
		modifiedScript = modifiedScript.replaceAll("tf.math.", "tf.");
		modifiedScript = modifiedScript.replaceAll("Double.isNaN", "tf.is_nan");
		return modifiedScript;
	}

	/**
	 * Method to convert regular Python to NumPy-compatible Python
	 * @param pythonScript
	 * @return
	 */
	public String pythonToNumPyPython(String pythonScript) {
		String modifiedScript;		
		if (pythonScript.contains("math.")) {
			modifiedScript = pythonScript.replaceAll("math.", "np.math.");
		}
		else {
			modifiedScript = pythonScript.replaceAll("Math.", "np.math.");
		}
		modifiedScript = modifiedScript.replaceAll("np.math.PI", "np.pi");
		modifiedScript = modifiedScript.replaceAll("np.math.E", "np.e");
		modifiedScript = modifiedScript.replaceAll("np.math.acos", "np.arccos");
		modifiedScript = modifiedScript.replaceAll("np.math.atan", "np.arctan");
		modifiedScript = modifiedScript.replaceAll("np.math.max", "np.maximum");
		modifiedScript = modifiedScript.replaceAll("np.math.", "np.");
		modifiedScript = modifiedScript.replaceAll("np.pow", "np.power");
		modifiedScript = modifiedScript.replaceAll("Double.isNaN", "np.isnan");
		return modifiedScript;
	}

	private void clearExtractionContext() {
		extractionContext = null;
	}

	private void setExtractionContext(StatementContent extractionContext) {
		this.extractionContext = extractionContext;
	}
	
	private StatementContent getExtractionContext() {
		return extractionContext;
	}

	private String getAugmentedTypeQuestionAsked(String questionKey) {
		return augmentedTypeQuestionsAsked.get(questionKey);
	}

	private void addAugmentedTypeQuestionAsked(String questionKey, String questionText) {
		augmentedTypeQuestionsAsked.put(questionKey, questionText);
	}
	
	private void clearAugmentedTypeQuestionsAsked() {
		augmentedTypeQuestionsAsked.clear();
	}

	public String getLocalityURI() {
		return localityURI;
	}

	private void setLocalityURI(String localityURI) {
		this.localityURI = localityURI;
	}
	
	private void recordEquationCompleteness(String eqUri, String completeEqText) {
		if (equationCompleteness == null) {
			equationCompleteness = new HashMap<String, String>();
		}
		equationCompleteness.put(eqUri, completeEqText);
	}
	
	private String getEquationCompleteness(String eqUri) {
		if (equationCompleteness == null || !equationCompleteness.containsKey(eqUri)) {
			return null;
		}
		return equationCompleteness.get(eqUri);
	}

	private boolean isNewlyCompletedEquation(EquationStatementContent eqsc) {
		if (eqsc.getQuestionsForUser() == null) {
			// the equation is currently complete--no questions for user
			String eqUri = getDomainModelName() + "#" + eqsc.getEquationName();
			String lastCompleteText = getEquationCompleteness(eqUri);
			if (lastCompleteText == null) {
				// it wasn't complete on last call so it's newly complete
				return true;
			}
			if (eqsc.getText() != null && !eqsc.getText().equals(lastCompleteText)) {
				// the equation has changed
				return true;
			}
		}
		return false;
	}

	private String getLocalityPrefix() {
		return localityPrefix;
	}

	private void setLocalityPrefix(String localityPrefix) {
		this.localityPrefix = localityPrefix;
	}

	private void setSuspendConversationProcessing(boolean b) {
		suspendConversationProcessing = b;
	}

	private boolean isSuspendConversationProcessing() {
		return suspendConversationProcessing;
	}

	private String getSuspendedExtractionContent() {
		return this.suspendedExtractionContent;
	}
	
	private void saveSuspendedExtractionContent(String content) {
		this.suspendedExtractionContent = content;
	}

	private void saveSuspendedCodeModel(OntModel codeModel) {
		this.suspendedCodeModel  = codeModel;
		
	}

	private OntModel getSuspendedCodeModel() {
		return suspendedCodeModel;
	}

	public URI getResourceUri() {
		return resourceUri;
	}

	private void setResourceUri(URI resourceUri) {
		this.resourceUri = resourceUri;
	}

}

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
package com.ge.research.sadl.darpa.aske.processing;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.RDFWriter;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.emf.common.EMFPlugin;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.preferences.IPreferenceValues;
import org.eclipse.xtext.preferences.IPreferenceValuesProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.resource.XtextSyntaxDiagnostic;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.CheckType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.darpa.aske.dialog.AnswerCMStatement;
import com.ge.research.sadl.darpa.aske.dialog.CompareStatement;
import com.ge.research.sadl.darpa.aske.dialog.CompareWhen;
import com.ge.research.sadl.darpa.aske.dialog.ComparisonTableStatement;
import com.ge.research.sadl.darpa.aske.dialog.ExtractStatement;
import com.ge.research.sadl.darpa.aske.dialog.HowManyValuesStatement;
import com.ge.research.sadl.darpa.aske.dialog.InsightStatement;
import com.ge.research.sadl.darpa.aske.dialog.MatchFoundStatement;
import com.ge.research.sadl.darpa.aske.dialog.ModifiedAskStatement;
import com.ge.research.sadl.darpa.aske.dialog.MyNameIsStatement;
import com.ge.research.sadl.darpa.aske.dialog.NewExpressionStatement;
import com.ge.research.sadl.darpa.aske.dialog.NoModelFoundStatement;
import com.ge.research.sadl.darpa.aske.dialog.PLink;
import com.ge.research.sadl.darpa.aske.dialog.ParameterizedExpressionWithUnit;
import com.ge.research.sadl.darpa.aske.dialog.SadlEquationInvocation;
import com.ge.research.sadl.darpa.aske.dialog.SaveStatement;
import com.ge.research.sadl.darpa.aske.dialog.SeeParentheticalLink;
import com.ge.research.sadl.darpa.aske.dialog.SuitabilityStatement;
import com.ge.research.sadl.darpa.aske.dialog.TargetModelName;
import com.ge.research.sadl.darpa.aske.dialog.TestLongrunningTask;
import com.ge.research.sadl.darpa.aske.dialog.UndefinedConceptStatement;
import com.ge.research.sadl.darpa.aske.dialog.WhatIsStatement;
import com.ge.research.sadl.darpa.aske.dialog.WhatStatement;
import com.ge.research.sadl.darpa.aske.dialog.WhatTypeStatement;
import com.ge.research.sadl.darpa.aske.dialog.WhatValuesStatement;
import com.ge.research.sadl.darpa.aske.dialog.YesNoAnswerStatement;
import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.darpa.aske.processing.EvalContent.UnittedParameter;
import com.ge.research.sadl.errorgenerator.generator.SadlErrorMessages;
import com.ge.research.sadl.jena.IntermediateFormTranslator;
import com.ge.research.sadl.jena.JenaBasedSadlModelProcessor;
import com.ge.research.sadl.jena.JenaBasedSadlModelValidator.TypeCheckInfo;
import com.ge.research.sadl.jena.JenaProcessorException;
import com.ge.research.sadl.jena.MetricsProcessor;
import com.ge.research.sadl.jena.UtilsForJena;
import com.ge.research.sadl.model.CircularDefinitionException;
import com.ge.research.sadl.model.ModelError;
import com.ge.research.sadl.model.OntConceptType;
import com.ge.research.sadl.model.PrefixNotFoundException;
import com.ge.research.sadl.model.gp.BuiltinElement;
import com.ge.research.sadl.model.gp.BuiltinElement.BuiltinType;
import com.ge.research.sadl.model.gp.Equation;
import com.ge.research.sadl.model.gp.GraphPatternElement;
import com.ge.research.sadl.model.gp.Junction;
import com.ge.research.sadl.model.gp.JunctionNode;
import com.ge.research.sadl.model.gp.Literal;
import com.ge.research.sadl.model.gp.NamedNode;
import com.ge.research.sadl.model.gp.NamedNode.NodeType;
import com.ge.research.sadl.model.gp.Node;
import com.ge.research.sadl.model.gp.ProxyNode;
import com.ge.research.sadl.model.gp.Query;
import com.ge.research.sadl.model.gp.RDFTypeNode;
import com.ge.research.sadl.model.gp.Rule;
import com.ge.research.sadl.model.gp.TripleElement;
import com.ge.research.sadl.model.gp.VariableNode;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.processing.SadlConstants;
import com.ge.research.sadl.processing.ValidationAcceptor;
import com.ge.research.sadl.reasoner.CircularDependencyException;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.InvalidTypeException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.TranslationException;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.ge.research.sadl.sADL.BinaryOperation;
import com.ge.research.sadl.sADL.Declaration;
import com.ge.research.sadl.sADL.EquationStatement;
import com.ge.research.sadl.sADL.Expression;
import com.ge.research.sadl.sADL.ExternalEquationStatement;
import com.ge.research.sadl.sADL.Name;
import com.ge.research.sadl.sADL.NamedStructureAnnotation;
import com.ge.research.sadl.sADL.QueryStatement;
import com.ge.research.sadl.sADL.SadlAnnotation;
import com.ge.research.sadl.sADL.SadlImport;
import com.ge.research.sadl.sADL.SadlInstance;
import com.ge.research.sadl.sADL.SadlModel;
import com.ge.research.sadl.sADL.SadlModelElement;
import com.ge.research.sadl.sADL.SadlParameterDeclaration;
import com.ge.research.sadl.sADL.SadlProperty;
import com.ge.research.sadl.sADL.SadlPropertyRestriction;
import com.ge.research.sadl.sADL.SadlResource;
import com.ge.research.sadl.sADL.SadlReturnDeclaration;
import com.ge.research.sadl.sADL.SadlSimpleTypeReference;
import com.ge.research.sadl.sADL.SadlStatement;
import com.ge.research.sadl.sADL.SadlTypeAssociation;
import com.ge.research.sadl.sADL.SadlTypeReference;
import com.ge.research.sadl.sADL.ValueTable;
import com.ge.research.sadl.utils.ResourceManager;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;

public class JenaBasedDialogModelProcessor extends JenaBasedSadlModelProcessor {
	private static final Logger logger = LoggerFactory.getLogger(JenaBasedDialogModelProcessor.class);
	private boolean modelChanged;
	
	List<ModelElementInfo> modelElements = null;
	
	private String textServiceUrl = null;
	private String dbnCgServiceUrl = null;
	private String dbninputjsongenerationserviceurl = null;
	private String invizinserviceurl = null;
	private boolean useDbn = true;
	private String kchainCgServiceUrl = null;
	private boolean useKchain = false;
	private boolean savePythonTF = true;
	private boolean savePython = true;
	private boolean saveOriginal = true;
	private String shortGraphLink = null;

	private AnswerCurationManager answerCurationManager = null;
	
	private StatementContent lastStatementContent = null;

	@Inject IPreferenceValuesProvider preferenceProvider;
	private boolean savePhthonTF;
	private boolean savePhthon;
	@Override
	public void onValidate(Resource resource, ValidationAcceptor issueAcceptor, CheckMode mode, ProcessorContext context) {
		if (!isSupported(resource)) {
			return;
		}
		resetProcessor();
		logger.debug("JenaBasedDialogModelProcessor.onValidate called for Resource '" + resource.getURI() + "'"); 
		CancelIndicator cancelIndicator = context.getCancelIndicator();
//		if (resource.getContents().size() < 1) {
//			return;
//		}
	
		logger.debug("onValidate called for Resource '" + resource.getURI() + "'");
		if (mode.shouldCheck(CheckType.EXPENSIVE)) {
			// do expensive validation, i.e. those that should only be done when 'validate'
			// action was invoked.
		}
		setIssueAcceptor(issueAcceptor);
		setProcessorContext(context);
		setCancelIndicator(cancelIndicator);
		setCurrentResource(resource);
		SadlModel model;
		if (resource.getContents() != null && !resource.getContents().isEmpty()) {
			model = (SadlModel) resource.getContents().get(0);
		}
		else {
			return; // don't have any model yet
		}
		String modelActualUrl = resource.getURI().lastSegment();
		validateResourcePathAndName(resource, model, modelActualUrl);
		String modelName = model.getBaseUri();
		setModelName(modelName);
		setModelNamespace(assureNamespaceEndsWithHash(modelName));
		setModelAlias(model.getAlias());
		if (getModelAlias() == null) {
			setModelAlias("");
		}

		try {
			theJenaModel = prepareEmptyOntModel(resource, context);
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
			addError(SadlErrorMessages.CONFIGURATION_ERROR.get(e1.getMessage()), model);
			addError(e1.getMessage(), model);
			return; // this is a fatal error
		}
		getTheJenaModel().setNsPrefix(getModelAlias(), getModelNamespace());
		Ontology modelOntology = getTheJenaModel().createOntology(modelName);
		logger.debug("Ontology '" + modelName + "' created");
		modelOntology.addComment("This ontology was created from a DIALOG file '" + modelActualUrl
				+ "' and should not be directly edited.", "en");

		String modelVersion = model.getVersion();
		if (modelVersion != null) {
			modelOntology.addVersionInfo(modelVersion);
		}

		EList<SadlAnnotation> anns = model.getAnnotations();
		addAnnotationsToResource(modelOntology, anns);

		OntModelProvider.registerResource(resource);
		// clear any pre-existing content
		List<Object> oc = OntModelProvider.getOtherContent(resource);
		if (oc != null) {
			Iterator<Object> itr = oc.iterator();
			List<Equation> eqs = new ArrayList<Equation>();
			while (itr.hasNext()) {
				Object nxt = itr.next();
				if (nxt instanceof Equation) {
					eqs.add((Equation)nxt);
				}
			}
			if (eqs.size() > 0) {
				oc.removeAll(eqs);
			}
		}
		try {
			initializeDialogContent();
		} catch (ConversationException e1) {
			e1.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

		try {
			// Add SadlBaseModel to everything except the SadlImplicitModel
			if (!resource.getURI().lastSegment().equals(SadlConstants.SADL_IMPLICIT_MODEL_FILENAME)) {
				addSadlBaseModelImportToJenaModel(resource);
			}
			// Add the SadlImplicitModel to everything except itself and the
			// SadlBuilinFunctions
			if (!resource.getURI().lastSegment().equals(SadlConstants.SADL_IMPLICIT_MODEL_FILENAME)
					&& !resource.getURI().lastSegment().equals(SadlConstants.SADL_BUILTIN_FUNCTIONS_FILENAME)) {
				addImplicitSadlModelImportToJenaModel(resource, context);
				addImplicitBuiltinFunctionModelImportToJenaModel(resource, context);

			}
			if (modelActualUrl.equals(ResourceManager.ServicesConf_SFN)) {
				try {
					importSadlServicesConfigConceptsModel(resource);
				} catch (JenaProcessorException e) {
					e.printStackTrace();
				} catch (ConfigurationException e) {
					e.printStackTrace();
				}
			}
			checkCodeExtractionSadlModelExistence(resource, context);
			importSadlListModel(resource);		// an import could happen at any time and require a list model
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		} catch (JenaProcessorException e1) {
			e1.printStackTrace();
		}
		
		if (model.eContents().size() < 1) {
			// there are no imports
			MixedInitiativeTextualResponse mir = new MixedInitiativeTextualResponse("Please replace this reminder with at least one import of a domain namespace");
			int endOffset = NodeModelUtils.findActualNodeFor(model).getEndOffset();
			mir.setInsertionPoint(endOffset);
			OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND, mir);
		}

		if(!processModelImports(modelOntology, resource.getURI(), model)) {
			addError("Failed to load model imports. Try cleaning and building the project.", model);
			return;
		}
		EList<SadlImport> implist = model.getImports();
		Iterator<SadlImport> impitr = implist.iterator();
		while (impitr.hasNext()) {
			storeOriginalElementInfo(impitr.next());
		}

		boolean enableMetricsCollection = true; // no longer a preference
		try {
			if (enableMetricsCollection) {
				if (!isSyntheticUri(null, resource)) {
					setMetricsProcessor(new MetricsProcessor(modelName, resource,
							getConfigMgr(resource, getOwlModelFormat(context)), this));
				}
			}
		} catch (JenaProcessorException e1) {
			e1.printStackTrace();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}

		initializePreferences(context);

		// Check for a syntactically valid AST; if it isn't then don't process so that conversations will only be valid ones
	    boolean validAST = isAstSyntaxValid(model);	
//	    if (!validAST) {
//	    	return;
//	    }

		// create validator for expressions
		initializeModelValidator();
		initializeAllImpliedPropertyClasses();
		initializeAllExpandedPropertyClasses();
		
		// process rest of parse tree
		List<SadlModelElement> elements = model.getElements();
		if (elements != null) {
			Iterator<SadlModelElement> elitr = elements.iterator();
			while (elitr.hasNext()) {
				// check for cancelation from time to time
				if (cancelIndicator.isCanceled()) {
					throw new OperationCanceledException();
				}
				SadlModelElement element = elitr.next();
				storeOriginalElementInfo(element);
				boolean stmtComplete = statementIsComplete(element);
				String eos = getEos(element);
        		if (eos != null && (!(eos.endsWith(".") || eos.endsWith("?")))) {	// I don't think this is needed anymore awc 9/4/19
               		continue;
         		}
				logger.debug("   Model element of type '" + element.getClass().getCanonicalName() + "' being processed.");
				// reset state for a new model element
				try {
					resetProcessorState(element);
				} catch (InvalidTypeException e) {
					logger.error("Error:", e);
				}
				try {
					StatementContent sc = processDialogModelElement(element, resource);
					if (sc != null) {
						ConversationElement ce = new ConversationElement(getAnswerCurationManager(resource).getConversation(), sc, sc.getAgent());
						getAnswerCurationManager(resource).addToConversation(ce);
					}
					setLastStatementContent(sc);
				} catch (Exception e1) {
					// shouldn't happen; all exceptions should be caught before here
					e1.printStackTrace();
				} 
			}
			
			File saveFile = getModelFile(resource);
			try {
				if (saveFile != null && (isModelChanged() || !saveFile.exists() || 
						getConfigMgr().getAltUrlFromPublicUri(getModelName()) == null ||
						getConfigMgr().getAltUrlFromPublicUri(getModelName()) == getModelName())) {
					autoSaveModel(resource, getConfigMgr().getModelFolder(), saveFile, context);
					// refresh resource ?
				}
			} catch (IOException e) {
				e.printStackTrace();
			} catch (ConfigurationException e) {
				e.printStackTrace();
			} catch (URISyntaxException e) {
				e.printStackTrace();
			}
			if (getSadlCommands() != null && getSadlCommands().size() > 0) {
				OntModelProvider.attach(model.eResource(), getTheJenaModel(), getModelName(), getModelAlias(),
						getSadlCommands());
			} else {
				OntModelProvider.attach(model.eResource(), getTheJenaModel(), getModelName(), getModelAlias());
			}
			
			if (validAST && !refactoringHelper.isInProgress()) {
				// Do this **after** setting the resource information in the OntModelProvider
				try {
					getAnswerCurationManager(resource).processConversation(getCurrentResource(), getTheJenaModel(), getModelName());
				} catch (Exception e2) {
					e2.printStackTrace();
				}
			}
			
			logger.debug("At end of model processing, conversation is:");
			try {
				logger.debug(getAnswerCurationManager(resource).getConversation().toString());
			} catch (IOException e1) {
				e1.printStackTrace();
			}
			

		}
	}
	
	private void storeOriginalElementInfo(EObject element) {
		int start = getEObjectOffset(element);
		int length = getEObjectLength(element);
		int end = getEObjectEndOffset(element);
		if (!(start + length == end)) {
			System.err.println("Inconsistent ModelElementInfo");
		}
		EObject root = getCurrentResource().getContents().get(0);
		ICompositeNode node = NodeModelUtils.getNode(root);
		String dsl = node.getText();
		int end2 = start + length;
		String txt = dsl.substring(start, end2);
		if (!txt.endsWith(".") && !txt.endsWith("?")) {
			if (end2 + 1 < dsl.length()) {
				String after = dsl.substring(end2,end2+1);
				if (after.equals(".") || after.equals("?")) {
					txt = txt + after;
					length++;
					end++;
				}
			}
		}
		modelElements.add(new ModelElementInfo(element, txt, start, length, end, false));
	}

	private StatementContent processDialogModelElement(EObject element, Resource resource) throws JenaProcessorException, InvalidNameException, InvalidTypeException, TranslationException, IOException, ConfigurationException, QueryParseException, QueryCancelledException, ReasonerNotFoundException, PrefixNotFoundException {
		clearCruleVariables();
		if (element instanceof AnswerCMStatement) {
			return processAnswerCMStatement((AnswerCMStatement)element);
		}
		else if (element instanceof ExtractStatement) {
			return processStatement((ExtractStatement)element);
		}
		else if (element instanceof CompareStatement) {
			return processStatement((CompareStatement)element);
		}
		else if (element instanceof ComparisonTableStatement) {
			return processStatement((ComparisonTableStatement)element);
		}
		else if (element instanceof SuitabilityStatement) {
			return processStatement((SuitabilityStatement)element);
		}
		else if (element instanceof ModifiedAskStatement) {
			return processStatement((ModifiedAskStatement)element);
		}
		else if (element instanceof WhatStatement) {
			return processStatement((WhatStatement)element);
		}
		else if (element instanceof HowManyValuesStatement) {
			return processStatement((HowManyValuesStatement)element);
		}
		else if (element instanceof SaveStatement) {
			return processStatement((SaveStatement)element);
		}
		else if (element instanceof TestLongrunningTask) {
			int t = ((TestLongrunningTask)element).getTime();
			return new LongTaskContent(element, Agent.USER, t);
		}
		else if (element instanceof SadlEquationInvocation) {
			return processStatement((SadlEquationInvocation)element);
		}
		else if (element instanceof YesNoAnswerStatement) {
			return processStatement((YesNoAnswerStatement)element);
		}
		else if (element instanceof MyNameIsStatement) {
			return processStatement((MyNameIsStatement)element);
		}
		else if (element instanceof TargetModelName) {
			return processStatement((TargetModelName)element);
		}
		else if (element instanceof NewExpressionStatement) {
			return processStatement((NewExpressionStatement)element);
		}
		else if (element instanceof MatchFoundStatement) {
			return processStatement((MatchFoundStatement)element);
		}
		else if (element instanceof ExternalEquationStatement) {
			EquationStatementContent ssc = new EquationStatementContent(element, Agent.USER);
			super.processStatement((ExternalEquationStatement)element);
			List<Object> oc = OntModelProvider.getOtherContent(getCurrentResource());
			String eqUri  = null;
			if (oc != null && oc.size() > 0) {
				for (Object obj : oc) {
					if (obj instanceof Equation) {
						Equation eq = (Equation)obj;
						if (eq.getExternalUri().equals(((ExternalEquationStatement)element).getUri())) {
							List<StatementContent> questionsForUser = validateEquationAugmentedTypes((ExternalEquationStatement) element, eq);
							ssc.setEquationName(eq.getName());
							ssc.setQuestionsForUser(questionsForUser);
							eqUri = eq.getUri();
							break;
						}
					}
				}
			}
			if (eqUri != null) {
				String eqText2 = getEObjectText(element);
				getAnswerCurationManager(resource).addEquationInformation(eqUri, eqText2);
			}
			else {
				addError("Unable to find URI of External equation", element);
			}
			return ssc;
		}
		else if (element instanceof EquationStatement) {
			EquationStatementContent ssc = new EquationStatementContent(element, Agent.USER);
			super.processStatement((EquationStatement)element);
			List<Object> oc = OntModelProvider.getOtherContent(getCurrentResource());
			String eqUri  = null;
			if (oc != null && oc.size() > 0) {
				Object obj = oc.get(oc.size() - 1);
				if (obj instanceof Equation) {
					Equation eq = (Equation)obj;
					List<StatementContent> questionsForUser = validateEquationAugmentedTypes((EquationStatement) element, eq);
					ssc.setEquationName(eq.getName());
					ssc.setQuestionsForUser(questionsForUser);
					eqUri = eq.getUri();
				}
			}
			if (eqUri != null) {
				getAnswerCurationManager(resource).addEquationInformation(eqUri, getEObjectText(element));
			}
			else {
				addError("Unable to find URI of External equation", element);
			}
			return ssc;
		}
		else if (element instanceof SadlStatement) {
			SadlStatementContent ssc = new SadlStatementContent(element, Agent.USER);
			super.processModelElement((SadlStatement)element);
			if (element instanceof SadlInstance) {
				String srUri = getDeclarationExtensions().getConceptUri(sadlResourceFromSadlInstance((SadlInstance)element));
				if (getAnswerCurationManager(resource).getEquationInformation(srUri) != null) {
					getAnswerCurationManager(resource).addEquationInformation(srUri, getEObjectText(element));
				}
				ssc.setConceptUri(srUri);
			}
			return ssc;
		}
		else if (element instanceof SeeParentheticalLink) {
			EList<PLink> links = ((SeeParentheticalLink)element).getLinkUris();
			for (PLink plink : links) {
				String link = plink.getLinkUri();
				if (validURI(link)) {
					// currently there is nothing in the model to which we can add this to
					//	for statements that make it into the model as a triple we would need to do reification to add
					// 	for other statements we would need to represent the statement in the model.
	//				modelOntology.addSeeAlso(getTheJenaModel().getResource(link));
				}
				else {
					addWarning("Link is not a valid URL.", plink);
				}
			}
			return null;
		}
		else if (element instanceof UndefinedConceptStatement) {
			SadlResource ucsr = ((UndefinedConceptStatement)element).getConcept();
			return new UndefinedConceptStatementContent(element, Agent.CM, ucsr);
		}
		else if (element instanceof NoModelFoundStatement) {
			Expression trgt = ((NoModelFoundStatement)element).getTarget();
			if (trgt instanceof EObject) {
				Object trgtObj = super.processExpression((EObject)trgt);
				if (trgtObj instanceof Node) {
					return new NoModelFoundStatementContent(element, Agent.CM,  null, (Node) trgtObj);
				}
				else {
					throw new TranslationException("NoModelFoundStatement target translates to unsupported class: '" + trgtObj.getClass().getCanonicalName() + "'");
				}
			}
			else {
				throw new TranslationException("NoModelFoundStatement has unsupported target class: '" + trgt.getClass().getCanonicalName() + "'");
			}
		}
		else if (element instanceof InsightStatement) {
			// nothing needs to be done? Display only?
			return null;
		}
		else {
			throw new TranslationException("Model element of type '" + element.getClass().getCanonicalName() + "' not handled.");
		}	
	}

	private String getEObjectText(EObject element) {
		EObject root = getCurrentResource().getContents().get(0);
		ICompositeNode node = NodeModelUtils.getNode(root);
		String dsl = node.getText();
		int offset = NodeModelUtils.getNode(element).getOffset();
		int len = NodeModelUtils.getNode(element).getLength();
		String txt = dsl.substring(offset, offset + len);
		return txt;
	}
	
	private int getEObjectOffset(EObject element) {
		return NodeModelUtils.getNode(element).getOffset();
	}
	
	private int getEObjectLength(EObject element) {
		return NodeModelUtils.getNode(element).getLength();
	}
	
	private int getEObjectEndOffset(EObject element) {
		return NodeModelUtils.getNode(element).getEndOffset();
	}
	
	@Override
	public boolean validURI(String s) {	
		String uri = s;
		// is there a quoted string inside this string?
		int innerUriIdx = s.trim().indexOf('\'');
		if (innerUriIdx > 0) {
			if (s.trim().endsWith("'")) {
				uri = s.trim().substring(innerUriIdx + 1, s.trim().length() - 1);
			}
		}
		else  {
			innerUriIdx = s.trim().indexOf('"');
			if (innerUriIdx > 0) {
				if (s.trim().endsWith("\"")) {
					uri = s.trim().substring(innerUriIdx + 1, s.trim().length() - 1);
				}
			}
		}
		return super.validURI(uri);
	}
	
	private List<StatementContent>  validateEquationAugmentedTypes(ExternalEquationStatement element, Equation eq) throws IOException {
		List<StatementContent> missingInformation = null;
		Iterator<SadlParameterDeclaration> spitr = element.getParameter().iterator();
		while (spitr.hasNext()) {
			SadlParameterDeclaration spd = spitr.next();
			boolean isValid = true;
			if (spd.getAugtype() == null) {
				isValid = false;
				addWarning("Missing augmented type information", spd.getName());
			}
			else if (!isAugmentedTypeValid(spd.getAugtype())) {
				isValid = false;
				addWarning("Augmented type information is not valid", spd.getName());
			}
			if (!isValid) {
				String argName = getDeclarationExtensions().getConcreteName(spd.getName());
				String prompt = "What type is " + getAnswerCurationManager(getCurrentResource()).checkForKeyword(argName) + "?";
				if (missingInformation == null) {
					missingInformation = new ArrayList<StatementContent>();
				}
				missingInformation.add(new RequestArgumentAugmentedTypeContent(element, Agent.CM, null, argName, eq.getName(), prompt));
			}
		}
		Iterator<SadlReturnDeclaration> srtitr = element.getReturnType().iterator();
		while (srtitr.hasNext()) {
			SadlReturnDeclaration srd = srtitr.next();
			boolean isValid = true;
			if (srd.getAugtype() == null) {
				isValid = false;
				addWarning("Missing augmented return type information", srd.getType());
			}
			else if (!isAugmentedTypeValid(srd.getAugtype())) {
				isValid = false;
				addWarning("Augmented return type information is not valid", srd);
			}
			if (!isValid) {
				String prompt = "What type does " + eq.getName() + " return?";
				if (missingInformation == null) {
					missingInformation = new ArrayList<StatementContent>();
				}
				missingInformation.add(new RequestReturnAugmentedTypeContent(element, Agent.CM, null, eq.getName(), prompt));
			}
		}
		return missingInformation;
	}

	private boolean isAugmentedTypeValid(Expression augtype) {
		try {
			Object atobj = processExpression(augtype);
			if (atobj instanceof NamedNode && ((NamedNode)atobj).getNodeType().equals(NodeType.VariableNode)) {
				return false;
			}
			// TODO what other conditions are invalid?
		} catch (Exception e) {
			addError(e.getMessage(), augtype);
			return false;
		} 
		return true;
	}

	private List<StatementContent> validateEquationAugmentedTypes(EquationStatement element, Equation eq) {
		List<StatementContent> missingInformation = null;
		Iterator<SadlParameterDeclaration> spitr = element.getParameter().iterator();
		while (spitr.hasNext()) {
			SadlParameterDeclaration spd = spitr.next();
			if (spd.getAugtype() == null) {
				addWarning("Missing augmented type information", spd.getName());
				String argName = getDeclarationExtensions().getConcreteName(spd.getName());
				String prompt = "What type is " + argName + "?";
				if (missingInformation == null) {
					missingInformation = new ArrayList<StatementContent>();
				}
				missingInformation.add(new RequestArgumentAugmentedTypeContent(element, Agent.CM, null, argName, eq.getName(), prompt));
			}
		}
		Iterator<SadlReturnDeclaration> srtitr = element.getReturnType().iterator();
		while (srtitr.hasNext()) {
			SadlReturnDeclaration srd = srtitr.next();
			if (srd.getAugtype() == null) {
				addWarning("Missing augmented return type information", srd.getType());
				String prompt = "What type does " + eq.getName() + " return?";
				if (missingInformation == null) {
					missingInformation = new ArrayList<StatementContent>();
				}
				missingInformation.add(new RequestReturnAugmentedTypeContent(element, Agent.CM, null, eq.getName(), prompt));
			}
		}
		return missingInformation;
	}

	private StatementContent processStatement(ExtractStatement element) {
		EList<String> srcUris = element.getSources();
		if (srcUris.size() > 1) {
			addWarning("Extract statement currently only processes first source. Please use multiple statements.", element);
		}
		String str = projectHelper.toString();
		ExtractContent theFirstContent = null;
		ExtractContent theLastContent = null;
		for (String srcUri : srcUris) {
			ExtractContent newContent = null;
			try {
				String scheme = getUriScheme(srcUri);
				String source;
				if (scheme != null && scheme.equals("file")) {
					SadlUtils su = new SadlUtils();
					File srcFile = new File(su.fileUrlToFileName(srcUri));
					if (!srcFile.exists()) {
						addError("File '" + srcFile.getCanonicalPath() + "' does not exist.", element);
					}
					else if (!srcFile.isFile()) {
						addError("'" + srcFile.getCanonicalPath() + "' is not a file.", element);
					}
					source = srcFile.getCanonicalPath();
				}
				else {
					source = srcUri;
				}
				if (scheme != null) {
					newContent = new ExtractContent(element, Agent.USER, scheme, source, srcUri);
					if (theFirstContent == null) {
						theFirstContent = newContent;
						theLastContent = theFirstContent;
					}
					else {
						theLastContent.setNextExtractContent(newContent);
						theLastContent = newContent;
					}
					continue;
				}
			} catch (Exception e) {
				if (srcUri.startsWith("file:/") || srcUri.startsWith("http:/")) {
					addError(e.getMessage(), element);
					return null;
				}
			}
			addInfo("'" + srcUri + "' does not appear to be a URL so doing extraction from the text", element);
			newContent = new ExtractContent(element, Agent.USER, "text", srcUri, srcUri);
			if (theFirstContent == null) {
				theFirstContent = newContent;
				theLastContent = theFirstContent;
			}
			else {
				theLastContent.setNextExtractContent(newContent);
				theLastContent = newContent;
			}
		}
		return theFirstContent;
	}

	private StatementContent processStatement(CompareStatement element) throws InvalidNameException, InvalidTypeException, TranslationException, IOException, PrefixNotFoundException, ConfigurationException {
		Expression thenExpr = element.getToCompare();
		EList<CompareWhen> whenLst = element.getCompareWhens();
		List<Rule> comparisonRules = null;
		try {
			comparisonRules = whenListAndThenToCookedRules(whenLst, thenExpr);
		} catch (UndefinedConceptException e) {
			return e.getWhatIsContent();
		} catch (InvalidNameException e) {
			e.printStackTrace();
		} catch (InvalidTypeException e) {
			e.printStackTrace();
		} catch (TranslationException e) {
			e.printStackTrace();
		}
		
		return new CompareContent(element, Agent.USER, comparisonRules);
	}
	
	private StatementContent processStatement(SuitabilityStatement element) throws InvalidNameException, InvalidTypeException, TranslationException {
		EList<CompareWhen> whenExprs = element.getSuitableWhens();
		Expression thenExpr = element.getWhat();
		
		List<Rule> comparisonRules = null;
		try {
			comparisonRules = whenListAndThenToCookedRules(whenExprs, thenExpr);
		} catch (UndefinedConceptException e) {
			return e.getWhatIsContent();
		} catch (InvalidNameException e) {
			e.printStackTrace();
		} catch (InvalidTypeException e) {
			e.printStackTrace();
		} catch (TranslationException e) {
			e.printStackTrace();
		}
		return new CompareContent(element, Agent.USER, comparisonRules);
	}

	private List<Rule> whenAndThenToCookedRules(EObject when, EObject whatIsTarget) throws InvalidNameException, InvalidTypeException, TranslationException, UndefinedConceptException {
		List<EObject> whenLst = new ArrayList<EObject>();
		whenLst.add(when);
		return whenAndThenToCookedRules(whenLst, whatIsTarget);
	}

	private List<Rule> whenListAndThenToCookedRules(EList<CompareWhen> whenLst, EObject thenExpr) throws InvalidNameException, InvalidTypeException, TranslationException, UndefinedConceptException {
		List<EObject> whenObjLst = new ArrayList<EObject>();
		for (CompareWhen cw : whenLst) {
			whenObjLst.add(cw.getWhenExpr());
		}
		return whenAndThenToCookedRules(whenObjLst, thenExpr);
	}

	private List<Rule> whenAndThenToCookedRules(List<EObject> whenLst, EObject thenExpr)
			throws InvalidNameException, InvalidTypeException, TranslationException, UndefinedConceptException {
		if (whenLst == null && (thenExpr instanceof Declaration || thenExpr instanceof Name)) {
			// this is a simple query of the ontology, not a 
		}

		DialogIntermediateFormTranslator dift = new DialogIntermediateFormTranslator(this, getTheJenaModel());

		addVariableAllowedInContainerType(WhatStatement.class);
		Object thenObj = processExpression(thenExpr);	// do this first for article checking conformance

		List<Node> whenObjects = new ArrayList<Node>();
		for (EObject whenExpr : whenLst) {
			Object wh = processExpression(whenExpr);
			List<EObject> undefinedObjects = getUndefinedEObjects();
			if (undefinedObjects != null && undefinedObjects.size() > 0) {
				WhatIsContent wic = new WhatIsContent(undefinedObjects.get(0), Agent.CM, null, null);
				// this is Agent.CM because it houses a question/statement for the user from the CM
				String msg;
				try {
					String name = getEObjectName(undefinedObjects.get(0)).trim();
					msg = "Concept " + getAnswerCurationManager(getCurrentResource()).checkForKeyword(name) + " is not defined; please define or do extraction";
					wic.setExplicitQuestion(msg);
				} catch (IOException e) {
					msg = e.getMessage();
					e.printStackTrace();
				}
				clearUndefinedEObjects();
				throw new UndefinedConceptException(msg, wic);
			}
			dift.setStartingVariableNumber(getVariableNumber());
			if (wh instanceof Object[]) {
				if (((Object[])wh).length == 2 && ((Object[])wh)[1] instanceof GraphPatternElement) {
					// expected
					wh = ((Object[])wh)[1];
				}
			}
			if (wh instanceof GraphPatternElement) {
				wh = dift.addImpliedAndExpandedProperties((GraphPatternElement)wh);
				setVariableNumber(dift.getVariableNumber());
				List<GraphPatternElement> gpes = new ArrayList<GraphPatternElement>();
				gpes = unitSpecialConsiderations(gpes, wh, thenExpr);
				wh = dift.listToAnd(gpes);
				if (wh instanceof List<?> && ((List<?>)wh).size() == 1) {
					wh = ((List<?>)wh).get(0);
				}
				else {
					throw new TranslationException("Unexpected array of size > 1");
				}
			}
			Node whenObj = nodeCheck(wh);
			whenObjects.add(whenObj);
		}
		
		List<Object> originalThenObjects = new ArrayList<Object>();
		List<Node> thenObjects = new ArrayList<Node>();
		NamedNode specifiedPropertyNN = null;
		if (thenObj instanceof Junction) {
			List<Node> compareList = IntermediateFormTranslator.conjunctionToList((Junction)thenObj);
			if (compareList != null && compareList.size() > 1) {
				if (compareList.get(0) instanceof ProxyNode && 
						((ProxyNode)compareList.get(0)).getProxyFor() instanceof TripleElement &&
						((TripleElement)((ProxyNode)compareList.get(0)).getProxyFor()).getSubject() instanceof VariableNode) {
					TripleElement firstTriple = (TripleElement)((ProxyNode)compareList.get(0)).getProxyFor();
					for (int i = 1; i < compareList.size(); i++) {
						Node nextNode = compareList.get(i);
						if (nextNode instanceof VariableNode) {
							Node replacement = nodeCheck(new TripleElement(nextNode, firstTriple.getPredicate(), firstTriple.getObject()));
							compareList.set(i, replacement);
						}
					}
					
				}
			}
			for (Node n : compareList) {
				if (n instanceof VariableNode) {
					originalThenObjects.add(((VariableNode)n).getType());
				}
				else if (n instanceof ProxyNode) {
					originalThenObjects.add(((ProxyNode)n).getProxyFor());
				}
				else {
					originalThenObjects.add(n);
				}
			}
		}
		else if (thenObj instanceof JunctionNode) {
			List<Node> compareList = IntermediateFormTranslator.conjunctionToList((JunctionNode)thenObj);
//			if (compareList != null && compareList.size() > 1) {
//				for (int i = 0; i < compareList.size(); i++) {
//					if (compareList.get(i) instanceof VariableNode) {
//						Node replacement = nodeCheck(new TripleElement(compareList.get(i), new RDFTypeNode(), ((VariableNode)compareList.get(i)).getType()));
//						compareList.set(i, replacement);
//					}
//				}
//			}
			for (Node n : compareList) {
				if (n instanceof VariableNode) {
					originalThenObjects.add(((VariableNode)n).getType());
				}
				else if (n instanceof ProxyNode) {
					originalThenObjects.add(((ProxyNode)n).getProxyFor());
				}
				else {
					originalThenObjects.add(n);
				}
			}
		}
		else {
			originalThenObjects.add(thenObj);

		}
		for (Object origThenObj : originalThenObjects) {
			if (origThenObj instanceof NamedNode) {
				thenObjects.add((Node) origThenObj);
			}
			else if (origThenObj instanceof TripleElement) {
				if (((TripleElement)origThenObj).getPredicate() instanceof VariableNode) {
					// predicate cannot be a variable--means the question has an undefined concept
					VariableNode pvar = (VariableNode) ((TripleElement)origThenObj).getPredicate();
					addWarning(pvar.getName() + " is not defined.", thenExpr);
					WhatIsContent wic = new WhatIsContent(thenExpr, Agent.CM, pvar, null);	
					// this is Agent.CM because it houses a question/statement for the user from the CM
					String msg;
					try {
						msg = "Concept " + getAnswerCurationManager(getCurrentResource()).checkForKeyword(pvar.getName()) + " is not defined; please define or do extraction";
						wic.setExplicitQuestion(msg);
					} catch (IOException e) {
						msg = e.getMessage();
						e.printStackTrace();
					}
					throw new UndefinedConceptException(msg, wic);
				}
				if (((TripleElement)origThenObj).getSubject() instanceof VariableNode) {
					((TripleElement)origThenObj).setSubject(((VariableNode)((TripleElement)origThenObj).getSubject()).getType());
				}
				thenObjects.add(nodeCheck(origThenObj));
			}
			else if (origThenObj instanceof BuiltinElement) {
				addError("BuiltinElements not yet handled.", thenExpr);
			}
		}
		
		Map<Node, List<Node>> augmentedComparisonObjects = new HashMap<Node, List<Node>>();
		int whenObjIdx = 0;
		for (Node whenObj : whenObjects) {
			List<Node> comparisonObjects = new ArrayList<Node>();
			augmentedComparisonObjects.put(whenObj, comparisonObjects);
			for (int idx = 0; idx < thenObjects.size(); idx++) {
				Node cn = thenObjects.get(idx);
				if (cn instanceof NamedNode) {
					NamedNode nn = (NamedNode)cn;
					if (isProperty(nn.getNodeType())) {
						Property prop = getTheJenaModel().getProperty(nn.getURI());
						if (prop == null) {
							addError("Unexpected error finding property '" + nn.getURI() + "'", thenExpr);
							getTheJenaModel().write(System.err);
							ExtendedIterator<OntModel> smitr = getTheJenaModel().listSubModels();
							while (smitr.hasNext()) {
								smitr.next().write(System.err);
							}
							return null;
						}
						else {
							specifiedPropertyNN = nn;
						}
						StmtIterator stmtitr = getTheJenaModel().listStatements(prop, RDFS.domain, (RDFNode)null);
						while (stmtitr.hasNext()) {
							org.apache.jena.rdf.model.Resource dmn = stmtitr.nextStatement().getObject().asResource();
							if (dmn.isURIResource()) {
								nn = new NamedNode(dmn.getURI());
								nn.setNodeType(NodeType.ClassNode);
							}
							else {
								addError("Blank node domain not yet handled", thenExpr);
								return null;
							}
							if (stmtitr.hasNext()) {
								addError("Multiple domain classes not yet handled", thenExpr);
								return null;
							}
						}
					}
					if (nn instanceof VariableNode) {
						// this will happen when there is an article in front of a class name (a Declaration)
						Node tn = ((VariableNode)nn).getType();
						if (tn instanceof NamedNode) {
							nn = (NamedNode) tn;
						}
					}
					if (nn.getNodeType().equals(NodeType.ClassNode)) {
						NodeType ntype = null;
						boolean comparisonsFound = false;
						OntClass theClass = getTheJenaModel().getOntClass(nn.getURI());
						List<org.apache.jena.rdf.model.Resource> instances = new ArrayList<org.apache.jena.rdf.model.Resource>();
						// look for at least two instances of the class 
						instances = getInstancesOfClass(theClass, instances);
						if (instances.size() > 1) {
							comparisonsFound = true;
							ntype = NodeType.InstanceNode;
						}
						if (!comparisonsFound) {
							// if not resolved, look for leaf subclasses of the class, and create a variable of each leaf subclass type
							instances = getLeafSubclasses(theClass, instances);
							if (instances.size() > 1) {
								ntype = NodeType.ClassNode;
								comparisonsFound = true;
							}
						}
						if (!comparisonsFound) {
							instances.add(getTheJenaModel().getOntClass(nn.getURI()));
							ntype = NodeType.ClassNode;
						}
						if (instances.size() > 0) {
							for (int i = 0; i < instances.size(); i++) {
								Node compNode;
								if (specifiedPropertyNN != null) {
									Node subj;
									if (ntype.equals(NodeType.ClassNode)) {
										NamedNode varType = new NamedNode(instances.get(i).getURI());
										varType.setNodeType(ntype);
										subj = varType;	// don't create variable here--will be done in missing pattern processing
									}
									else {
										NamedNode instNN = new NamedNode(instances.get(i).getURI());
										instNN.setNodeType(ntype);
										subj = instNN;
									}
									compNode = nodeCheck(new TripleElement(subj, specifiedPropertyNN, null));
									comparisonObjects.add(whenObjIdx++ == 0 ? compNode : dift.newCopyOfProxyNode(compNode));
								}
								else {
									NamedNode instNN = new NamedNode(instances.get(i).getURI());
									instNN.setNodeType(ntype);
									instNN.setLocalizedType(nn);
									// we don't have any property specified so we need to generate a list of relevant properties
									List<NamedNode> relevantProperties;
									if (instNN.getNodeType().equals(NodeType.InstanceNode)) {
										relevantProperties = getRelevantPropertiesOfClass(nn);									
									}
									else {
										relevantProperties = getRelevantPropertiesOfClass(instNN);
									}
									if (relevantProperties != null) {
										for (NamedNode prop : relevantProperties) {
											if (!whenContainsProperty(whenObj, prop)) {
												compNode = nodeCheck(new TripleElement(instNN, prop, null));
												comparisonObjects.add(whenObjIdx++ == 0 ? compNode : dift.newCopyOfProxyNode(compNode));
											}
										}
									}
									else {
										addError("No properties found for target", thenExpr);
										return null;
									}
								}
							}
						}
						else {
							// need to find the important properties for the given subject class and create multiple triples, one for each property
							comparisonObjects.add(new ProxyNode(new TripleElement(nn, specifiedPropertyNN, null)));
							
						}
					}
					else {
						addError("Failed to establish an anchor node", thenExpr);
					}
				}
				else {
					comparisonObjects.add(whenObjIdx++ == 0 ? cn : dift.newCopyOfProxyNode(cn));
				}
			}
		}
		
		dift.setStartingVariableNumber(getVariableNumber());
		List<Rule> comparisonRules = new ArrayList<Rule>();
		int ridx = 0;
		for (Node whenObj : whenObjects) {
			List<Node> comparisonObjects = augmentedComparisonObjects.get(whenObj);
			for (int i = 0; i < comparisonObjects.size(); i++) {
				Node cobj = comparisonObjects.get(i);
				Node newWhen = dift.newCopyOfProxyNode(whenObj);
				Rule pseudoRule = new Rule("ComparePseudoRule" + ridx++, null, nodeToGPEList(newWhen), nodeToGPEList(cobj));
				populateRuleVariables(pseudoRule);
				dift.setTarget(pseudoRule);
				Rule modifiedRule = dift.cook(pseudoRule);
				comparisonRules.add(modifiedRule);
				logger.debug(modifiedRule.toDescriptiveString());
	//			System.out.println(modifiedRule.toFullyQualifiedString());
	//			addInfo(modifiedRule.toFullyQualifiedString(), thenExpr.eContainer());
			}
		}
		if (comparisonRules != null && comparisonRules.size() > 0) {
			StringBuilder sb = new StringBuilder();
			for (Rule r : comparisonRules) {
				sb.append(r.toString());
				sb.append("\n");
			}
			addInfo(sb.toString(), thenExpr.eContainer());
		}

		return comparisonRules;
	}

	private boolean whenContainsProperty(Node whenObj, NamedNode prop) {
		if (whenObj instanceof ProxyNode) {
			GraphPatternElement gpe = ((ProxyNode)whenObj).getProxyFor();
			return whenGpeContainsProperty(gpe, prop);
		}
		return false;
	}

	private boolean whenGpeContainsProperty(GraphPatternElement gpe, NamedNode prop) {
		if (prop != null) {
			if (gpe instanceof Junction) {
				if (whenContainsProperty((Node) ((Junction)gpe).getLhs(), prop)) {
					return true;
				}
				if (whenContainsProperty((Node) ((Junction)gpe).getRhs(), prop)) {
					return true;
				}
			}
			else if (gpe instanceof BuiltinElement) {
				for (Node arg : ((BuiltinElement)gpe).getArguments()) {
					if (arg instanceof NamedNode && ((NamedNode)arg).getURI() != null && ((NamedNode)arg).getURI().equals(prop.getURI())) {
						return true;
					}
					else if (arg instanceof ProxyNode) {
						if (whenContainsProperty((ProxyNode)arg, prop)) {
							return true;
						}
					}
				}
			}
			else if (gpe instanceof TripleElement) {
				if (((TripleElement)gpe).getPredicate().equals(prop)) {
					return true;
				}
			}
		}
		return false;
	}

	private List<NamedNode> getRelevantPropertiesOfClass(NamedNode clsNode) {
		if (clsNode.getNodeType().equals(NodeType.ClassNode)) {
			OntClass cls = getTheJenaModel().getOntClass(clsNode.getURI());
			return getReleventPropertiesOfClass(cls);
		}
		return null;
	}
	
	private List<NamedNode> getReleventPropertiesOfClass(OntClass cls) {
		List<NamedNode> relevantProperties = new ArrayList<NamedNode>();
		StmtIterator dmnitr = getTheJenaModel().listStatements(null, RDFS.domain, cls);
		while (dmnitr.hasNext()) {
			org.apache.jena.rdf.model.Resource propnode = dmnitr.nextStatement().getSubject();
			if (propnode.isURIResource()) {
				NamedNode objNN = new NamedNode(propnode.asResource().getURI());
				if (objNN != null  ) {
					if (propnode.canAs(Property.class)) {
						objNN.setNodeType(NodeType.PropertyNode);
						relevantProperties.add(objNN);
					}
				}
			}
		}
		StmtIterator scitr = getTheJenaModel().listStatements(cls, RDFS.subClassOf, (RDFNode)null);
		while (scitr.hasNext()) {
			RDFNode sc = scitr.nextStatement().getObject();
			if (sc.isResource() && sc.asResource().canAs(OntClass.class)) {
				List<NamedNode> moreProps = getReleventPropertiesOfClass(sc.asResource().as(OntClass.class));
				if (moreProps != null) {
					relevantProperties.addAll(moreProps);
				}
			}
		}
		return relevantProperties;
	}

	private void populateRuleVariables(Rule rule) {
		if (rule.getGivens() != null) {
			for (GraphPatternElement gpe : rule.getGivens()) {
				populateRuleVariables(rule, gpe);
			}
		}
		if (rule.getIfs() != null) {
			for (GraphPatternElement gpe : rule.getIfs()) {
				populateRuleVariables(rule, gpe);
			}
		}
		if (rule.getThens() != null) {
			for (GraphPatternElement gpe : rule.getThens()) {
				populateRuleVariables(rule, gpe);
			}
		}
		
	}

	private void populateRuleVariables(Rule rule, GraphPatternElement gpe) {
		if (gpe instanceof TripleElement) {
			if (((TripleElement)gpe).getSubject() instanceof VariableNode) {
				rule.addRuleVariable((VariableNode) ((TripleElement)gpe).getSubject());
			}
			if (((TripleElement)gpe).getPredicate() instanceof VariableNode) {
				rule.addRuleVariable((VariableNode) ((TripleElement)gpe).getPredicate());
			}
			if (((TripleElement)gpe).getObject() instanceof VariableNode) {
				rule.addRuleVariable((VariableNode) ((TripleElement)gpe).getObject());
			}
		}
		else if (gpe instanceof BuiltinElement) {
			if (((BuiltinElement)gpe).getArguments() != null) {
				for (Node arg : ((BuiltinElement)gpe).getArguments()) {
					if (arg instanceof VariableNode) {
						rule.addRuleVariable((VariableNode)arg);
					}
					else if (arg instanceof ProxyNode) {
						populateRuleVariables(rule, ((ProxyNode)arg).getProxyFor());
					}
				}
			}
		}
		else if (gpe instanceof Junction) {
			populateRuleVariables(rule, ((ProxyNode)((Junction)gpe).getLhs()).getProxyFor());
			populateRuleVariables(rule, ((ProxyNode)((Junction)gpe).getRhs()).getProxyFor());
		}
	}

	private List<GraphPatternElement> nodeToGPEList(Node node) {
		List<GraphPatternElement> gpelist = new ArrayList<GraphPatternElement>();
		if (node instanceof ProxyNode) {
			GraphPatternElement gpe = ((ProxyNode)node).getProxyFor();
			if (gpe instanceof Junction) {
				List<Node> nodes = DialogIntermediateFormTranslator.conjunctionToList((Junction) gpe);
				for (Node n : nodes) {
					if (n instanceof ProxyNode) {
						gpelist.add(((ProxyNode)n).getProxyFor());
					}
				}
			}
			else {
				gpelist.add(gpe);
			}
		}
		return gpelist;
	}

	private VariableNode createComparisonTypedVariable(NamedNode varType, EObject context) throws IOException, PrefixNotFoundException, InvalidNameException, InvalidTypeException, TranslationException, ConfigurationException {
		String nvar = getNewVar(context);
		VariableNode var = createVariable(getModelNamespace() +nvar, context);	
		var.setType(varType);
		return var;
	}

	/**
	 * Method to find all of the instances of a given class
	 * @param theClass
	 * @param instances
	 * @return
	 */
	private List<org.apache.jena.rdf.model.Resource> getInstancesOfClass(OntClass theClass, List<org.apache.jena.rdf.model.Resource> instances) {
		StmtIterator stmtitr = getTheJenaModel().listStatements(null, RDF.type, theClass);
		if (stmtitr.hasNext()) {
			while (stmtitr.hasNext()) {
				org.apache.jena.rdf.model.Resource inst = stmtitr.nextStatement().getSubject();
				if (inst.isURIResource()) {
					// don't include unnamed instances, at least for the time being (awc 1/29/2020)
					instances.add(inst);					
				}
			}
		}
		ExtendedIterator<OntClass> scitr = theClass.listSubClasses();
		while (scitr.hasNext()) {
			instances = getInstancesOfClass(scitr.next(), instances);
		}
		return instances;
	}

	/**
	 * Method to get all of the leaf subclasses of a given class
	 * @param theClass
	 * @param instances
	 * @return
	 */
	private List<org.apache.jena.rdf.model.Resource> getLeafSubclasses(OntClass theClass,
			List<org.apache.jena.rdf.model.Resource> instances) {
		ExtendedIterator<OntClass> scitr = theClass.listSubClasses();
		while (scitr.hasNext()) {
			OntClass sc = scitr.next();
			int cnt = instances.size();
			instances = getLeafSubclasses(sc, instances);
			if (instances.size() == cnt) {
				instances.add(sc);
			}
		}
		return instances;
	}

	private StatementContent processStatement(MyNameIsStatement element) throws IOException {
		getAnswerCurationManager(getCurrentResource()).setUserName(element.getAnswer());
		AnswerContent ac = new AnswerContent(element, Agent.USER);
		ac.setAnswer(element.getAnswer());
		return ac;
	}

	private StatementContent processStatement(YesNoAnswerStatement element) throws IOException {
		AnswerContent ac = new AnswerContent(element, Agent.USER);
		ac.setAnswer(element.getAnswer());
		return ac;
	}

	private String getEos(SadlModelElement element) {
		if (element instanceof EObject) {
			ICompositeNode nd = NodeModelUtils.findActualNodeFor((EObject) element);
			INode nsnd = nd.getNextSibling();
			String elementText = nd.getText();
			String nsndTxt = nsnd != null ? nsnd.getText() : null;
			String trimmed = elementText.trim();
			if (trimmed.endsWith(".")) {
				return ".";
			}
			else if (trimmed.endsWith("?")) {	// I don't think this is needed anymore awc 9/4/19
				return "?";
			}
		}
		return null;
	}
	
	private StatementContent processStatement(TargetModelName element) throws ConfigurationException, IOException {
		boolean returnVal = true;
		SadlModel targetResource = element.getTargetResource();
		String aliasToUse = element.getAlias();
		if (targetResource != null) {
			// URI importingResourceUri = resource.getURI();
			String targetUri = targetResource.getBaseUri();
			String targetPrefix = targetResource.getAlias();
			Resource eResource = targetResource.eResource();
			if (eResource instanceof XtextResource) {
				XtextResource xtrsrc = (XtextResource) eResource;
				URI targetResourceUri = xtrsrc.getURI();
				OntModel targetOntModel = OntModelProvider.find(xtrsrc);
				if (targetOntModel == null) {
					addError("Model not found", element);
					returnVal = false;
				}
				else if (aliasToUse == null) {
					if (targetPrefix != null) {
						aliasToUse = targetPrefix;
					}
					else {
						String gprefix = getConfigMgr().getGlobalPrefix(targetUri);
						if (gprefix == null) {
							addError("No global prefix found for model so a local alias is required", element);
							returnVal = false;
						}
						else {
							aliasToUse = gprefix;
						}
					}
				}
				if (returnVal) {
					String[] uris = new String[2];
					uris[0] = targetUri;
					uris[1] = null;
					getAnswerCurationManager(getCurrentResource()).addTargetModelToMap(aliasToUse, uris);
				}
			}
		}
		return null;
	}
	
	private StatementContent processStatement(MatchFoundStatement element) {
		String type = element.getType();
		Expression concept = element.getConcept();
		if (concept instanceof SadlResource) {
			String srName = getDeclarationExtensions().getConcreteName((SadlResource) concept);
			return new MatchFoundStatementContent(element, type, Agent.CM, srName);
		}
		else {
			try {
				Object exprObj = processExpression(concept);
				return new MatchFoundStatementContent(element, type, Agent.CM, exprObj.toString());
			} catch (Exception e) {
				addError(e.getMessage(), concept);
				e.printStackTrace();
				return new MatchFoundStatementContent(element, type, Agent.CM, e.getMessage());
			} 
		}
	}

	private StatementContent processStatement(NewExpressionStatement element) {
		Expression expr = element.getNewExpr();
		String uptxt = getSourceText(element);
		if (expr instanceof BinaryOperation && isEqualOperator(((BinaryOperation)expr).getOp())) {
			// this is of the form: a LHS, an equal operator, and a RHS
			Expression lexpr = ((BinaryOperation)expr).getLeft();
			Expression rexpr = ((BinaryOperation)expr).getRight();
			try {
				addVariableAllowedInContainerType(NewExpressionStatement.class);
				addVariableAllowedInContainerType(UndefinedConceptStatement.class);
				addVariableAllowedInContainerType(WhatTypeStatement.class);
				Object lobj = processExpression(lexpr);
//				String lexprtext = getEObjectText(lexpr);
				Object robj = processExpression(rexpr);
				boolean needsCooking = doesNewExpressionContainAugmentedTypeInfo(lobj, robj);
				if (!needsCooking) {
					if (lobj instanceof VariableNode) {			// LHS is a VariableNode
						if (robj instanceof NamedNode) {		// RHS is a NamedNode
							if (((NamedNode)robj).getNodeType().equals(NodeType.ClassNode)) {		// RHS NamedNode is a class name
								// this is the addition of a named argument variable type
								//  find prior Equation statement, find argument variable, add the type to the argument
								List<ConversationElement> celements = getAnswerCurationManager(getCurrentResource()).getConversationElements();
								if (celements != null) {
									ConversationElement lastElement = celements.get(celements.size() - 1);
									if (lastElement != null && lastElement.getStatement() instanceof WhatIsContent &&
											lastElement.getStatement().getAgent().equals(Agent.CM) &&
											((WhatIsContent)lastElement.getStatement()).getTarget() instanceof VariableNode) {
										VariableNode argVar = (VariableNode) ((WhatIsContent)lastElement.getStatement()).getTarget();
										// 2. find prior Equation statement to be able to add the type to the argument
										for (int i = celements.size() - 1; i >= 0; i--) {
											ConversationElement anElement = celements.get(i);
											if (anElement.getStatement() instanceof EquationStatementContent) {
												 EquationStatementContent eqContent = (EquationStatementContent)anElement.getStatement();
												AddAugmentedTypeInfoContent mec = new AddAugmentedTypeInfoContent(element, Agent.USER, uptxt, eqContent, argVar, (Node)robj);
												return mec;
											}
										}
										addError("Addition of information missing preceding equation to be augmented", expr);
									}
									else {
										addError("Addition of information missing preceding question to be answered", expr);
									}
								}
								else {
									addError("Addition of information missing preceding context", expr);
								}
							}
							else if (robj instanceof VariableNode) {		// RHS NamedNode is a variable
								// need to ask what this is
								WhatIsContent wic = new WhatIsContent(element, Agent.USER, robj, null);
								return wic;
							}
							else {
								addError("Unexpected right-hand side of Add expression", rexpr);
							}
						}
						String lhs = ((VariableNode)lobj).getName();
	//					String eqName = "calc_" + lhs;
						String eqName = "func_" + lhs;
						if (robj instanceof BuiltinElement) { // && isNumericOperator(((BuiltinElement)robj).getFuncName())) {
							if (allArgumentsVariables((BuiltinElement)robj)) {
								StringBuilder sb = new StringBuilder("Equation ");
								sb.append(eqName);
								sb.append(" (alias \"");
								sb.append(lhs);
								sb.append("\")");
								sb.append(" (");
								sb.append(generateArguments((BuiltinElement)robj));
								sb.append(") returns double: ");
								String sadlEqText;
								try {
									DialogIntermediateFormTranslator dift = new DialogIntermediateFormTranslator(this, getTheJenaModel());
									sadlEqText = dift.intermediateFormToSadlExpression((BuiltinElement) robj);
								}
								catch (TranslationException e) {
									addError(e.getMessage(), expr);
									sadlEqText = robj.toString();
								}
								sb.append(sadlEqText);
								sb.append(".");
								AddEquationContent nec = new AddEquationContent(element, Agent.USER, uptxt, eqName, sb.toString());
								nec.setLhs(lhs);
								nec.setEquationBody(sadlEqText);
								return nec;
							}
							else {
								addError("Expected all arguments to be variables", rexpr);
							}
						}
					}
				}
				else if (lobj instanceof NamedNode && isProperty(((NamedNode)lobj).getNodeType())) {		// LHS is a property name
					lobj = new TripleElement(null, (Node) lobj, null);
					if (lexpr instanceof Name && ((Name)lexpr).getName().eContainer() instanceof SadlProperty) {
						EList<SadlPropertyRestriction> rstrs = ((SadlProperty)((Name)lexpr).getName().eContainer()).getRestrictions();
						for (SadlPropertyRestriction rstr : rstrs) {
							if (rstr instanceof SadlTypeAssociation) {
								SadlTypeReference dmn = ((SadlTypeAssociation)rstr).getDomain();
								try {
									TypeCheckInfo dmntci = getModelValidator().getType(dmn);
									if (dmntci != null && dmntci.getTypeCheckType() instanceof NamedNode) {
										VariableNode svar = createComparisonTypedVariable((NamedNode) dmntci.getTypeCheckType(), lexpr);
										((TripleElement)lobj).setSubject(svar);
										break;
									}
								} catch (Exception e) {
									e.printStackTrace();
								} 
							}
						}
					}
				}
				else if (lobj instanceof TripleElement) {
					// I think this is OK
				}
				else if (!(lobj instanceof VariableNode)){
					addError("LHS is of a type not currently handled", lexpr);
				}
				if (!needsCooking) {
					if (lobj instanceof VariableNode) {
						addWarning(((VariableNode)lobj).getName() + " is not defined.", lexpr);
						WhatIsContent wic = new WhatIsContent(element, Agent.CM, lobj, null);
						try {
							wic.setExplicitQuestion("Concept " + getAnswerCurationManager(getCurrentResource()).checkForKeyword(((VariableNode)lobj).getName()) + " is not defined; please define or do extraction");
						} catch (IOException e) {
							e.printStackTrace();
						}
						return wic;					
					}
					else if (!(lobj instanceof GraphPatternElement)) {
						addError("LHS is not a Graph Pattern", lexpr);
					}
					else if (!(robj instanceof GraphPatternElement)) {
						addError("RHS is not a Graph Pattern", lexpr);
					}
				}
				else {
					String lhs = getNewEquationName(lobj);
//					String rn = "calc_" + lhs;
					String rn = "func_" + lhs;
					Rule pseudoRule = new Rule(rn);
					pseudoRule.addIf((GraphPatternElement) robj);
					if (lobj instanceof GraphPatternElement) {
						pseudoRule.addThen((GraphPatternElement) lobj);
					}
					DialogIntermediateFormTranslator dift = new DialogIntermediateFormTranslator(this, getTheJenaModel());
					dift.setStartingVariableNumber(getVariableNumber());
					populateRuleVariables(pseudoRule);
					dift.setTarget(pseudoRule);
					Rule modifiedRule = dift.cook(pseudoRule);
					List<GraphPatternElement> ifs = modifiedRule.getIfs();
					BuiltinElement trgtExpr = getTheTargetExpression(ifs, robj);
					StringBuilder sb = new StringBuilder("Equation ");
					sb.append(rn);
					sb.append(" (alias \"");
					sb.append(lhs);
					sb.append("\")");
					sb.append(" (");
					sb.append(generateArguments((BuiltinElement)robj, pseudoRule));
					sb.append(") returns double");
					String augt = getAugmentedTypeOfReturn(modifiedRule);
					if (augt != null) {
						sb.append(" (");
						sb.append(augt);
						sb.append(")");
					}
					sb.append(": ");
					String sadlEqText;
					try {
						sadlEqText = dift.intermediateFormToSadlExpression(trgtExpr);
					}
					catch (TranslationException e) {
						addError(e.getMessage(), expr);
						sadlEqText = trgtExpr.toString();
					}
					sb.append(sadlEqText);
					sb.append(".");
					AddEquationContent nec = new AddEquationContent(element, Agent.USER, uptxt, rn, sb.toString());
					nec.setLhs(lhs);
					nec.setEquationBody(sadlEqText);
					return nec;
				}
			} catch (InvalidNameException e) {
				e.printStackTrace();
			} catch (InvalidTypeException e) {
				e.printStackTrace();
			} catch (TranslationException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			// this isn't of type equation (LHS = RHS) so see if we can make sense of it for augmented type addition
			try {
				boolean originalUseArticlesInValidation = isUseArticlesInValidation();
				setUseArticlesInValidation(false);
				Object nmObj = processExpression(expr);
				setUseArticlesInValidation(originalUseArticlesInValidation);
				if (nmObj instanceof GraphPatternElement) {
					nmObj = nodeCheck(nmObj);
				}
				if (nmObj instanceof Node) {
					if (nmObj instanceof NamedNode) {
						NodeType ntype = ((NamedNode)nmObj).getNodeType();
						if (ntype.equals(NodeType.ClassNode)) {
							// this makes sense in the context of a prior question about type of a variable						
						}
						else if (isProperty(ntype)) {
							// return range
							org.apache.jena.rdf.model.Resource rng = getUniquePropertyRange(((NamedNode)nmObj).getURI());
							if (rng != null) {
								nmObj = new NamedNode((String)rng.getURI(), NodeType.ClassNode);
							}
						}
					}
					else if (nmObj instanceof ProxyNode) {
						uptxt = getSourceText(expr).trim();
						if (uptxt.endsWith(".")) {
							uptxt = uptxt.substring(0, uptxt.length() - 1);
						}
					}
					// 1. find prior statement asking about variable to get variable name
					List<ConversationElement> celements = getAnswerCurationManager(getCurrentResource()).getConversationElements();
					if (celements != null) {
						for (int idx = celements.size() - 1; idx >= 0; idx--) {
							ConversationElement ceprior = celements.get(idx);
							if (ceprior.getStatement() instanceof WhatIsContent &&
									ceprior.getStatement().getAgent().equals(Agent.CM) &&
								((WhatIsContent)ceprior.getStatement()).getTarget() instanceof NamedNode) {
								NamedNode targetNode = (NamedNode) ((WhatIsContent)ceprior.getStatement()).getTarget();
								// 2. find prior Equation statement to be able to add the type to the argument
								for (int i = idx - 1; i >= 0; i--) {
									ConversationElement anElement = celements.get(i);
									if (anElement.getStatement() instanceof EquationStatementContent) {
										 EquationStatementContent eqContent = (EquationStatementContent)anElement.getStatement();
										AddAugmentedTypeInfoContent mec = new AddAugmentedTypeInfoContent(element, Agent.USER, uptxt, eqContent, targetNode, (Node)nmObj);
										return mec;
									}
								}
							}
							else if (ceprior.getStatement() instanceof EquationStatementContent) {
								addError("Addition of information missing preceding question, which follows an equation, to be answered", expr);
								return null;
							}
						}
						addError("Addition of information missing preceding equation to be augmented", expr);
					}
					else {
						addError("Addition of information missing preceding context", expr);
					}
				}
			} catch (TranslationException e) {
				e.printStackTrace();
			} catch (InvalidNameException e) {
				e.printStackTrace();
			} catch (InvalidTypeException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	/**
	 * Method to determine if the new expression needs to be cooked and provides augmented type info
	 * @param lobj
	 * @param robj
	 * @return
	 */
	private boolean doesNewExpressionContainAugmentedTypeInfo(Object lobj, Object robj) {
		boolean leftAugmentedType = false;
		boolean rightAugmentedType = doesExpressionContainAugmentedTypeInfo(robj);
		if (lobj instanceof VariableNode && ((VariableNode)lobj).getType() != null) {
			leftAugmentedType = true;
		}
		else if (!(lobj instanceof VariableNode)) {
			leftAugmentedType = true;
		}
		if (leftAugmentedType || rightAugmentedType) {
			return true;
		}
		return false;
	}

	private boolean doesExpressionContainAugmentedTypeInfo(Object exprObj) {
		if (exprObj instanceof BuiltinElement) {
			for (Node arg : ((BuiltinElement)exprObj).getArguments()) {
				if (doesExpressionContainAugmentedTypeInfo(arg)) {
					return true;
				}
			}
		}
		else if (exprObj instanceof TripleElement) {
			return true;
		}
		else if (exprObj instanceof ProxyNode) {
			return doesExpressionContainAugmentedTypeInfo(((ProxyNode)exprObj).getProxyFor());
		}
		else if (exprObj instanceof NamedNode) {
			return true;
		}
		return false;
	}

	private String generateArguments(BuiltinElement be) throws TranslationException {
		List<NamedNode>args = getAllArguments(be, new ArrayList<NamedNode>());
		StringBuilder sb = new StringBuilder();
		int argCnt = 0;
		for (Node arg : args) {
			if (argCnt++ > 0) {
				sb.append(", ");
			}
			sb.append("double ");
			sb.append(arg.getName());
		}
		return sb.toString();
	}
	
	private List<NamedNode> getAllArguments(BuiltinElement be, List<NamedNode> nnArgs) throws TranslationException {
		List<Node> args = be.getArguments();
		for (Node arg : args) {
			if (arg instanceof NamedNode) {
				if (!nnArgs.contains((NamedNode)arg)) {
					nnArgs.add((NamedNode) arg);
				}
			}
			else if (arg instanceof ProxyNode) {
				if (((ProxyNode)arg).getProxyFor() instanceof BuiltinElement) {
					List<NamedNode> moreArgs = getAllArguments((BuiltinElement) ((ProxyNode)arg).getProxyFor(), nnArgs);
					if (moreArgs != null) {
						for (NamedNode nn : moreArgs) {
							if (!nnArgs.contains(nn)) {
								nnArgs.add(nn);
							}
						}
					}
				}
				else if (((ProxyNode)arg).getProxyFor() instanceof TripleElement) {
					if (((TripleElement)((ProxyNode)arg).getProxyFor()).getObject() instanceof NamedNode) {
						nnArgs.add((NamedNode) ((TripleElement)((ProxyNode)arg).getProxyFor()).getObject());
					}
					else if (((TripleElement)((ProxyNode)arg).getProxyFor()).getObject() == null) {
						try {
							Object cookedArg = getIfTranslator().cook(arg);
							Node obj = ((TripleElement)((ProxyNode)arg).getProxyFor()).getObject();
							if (obj instanceof VariableNode) {
								nnArgs.add((NamedNode) obj);
							}
							else {
								throw new TranslationException("Unable to find an argument from TripleElement " + arg.toString());
							}
						} catch (InvalidNameException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						} catch (InvalidTypeException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else if (((TripleElement)((ProxyNode)arg).getProxyFor()).getSubject() instanceof ProxyNode) {
						try {
							Object cookedArg = getIfTranslator().cook(arg);
							Node obj = ((TripleElement)((ProxyNode)arg).getProxyFor()).getObject();
							if (obj instanceof VariableNode) {
								nnArgs.add((NamedNode) obj);
							}
							else {
								throw new TranslationException("Unable to find an argument from TripleElement " + arg.toString());
							}
						} catch (InvalidNameException e) {
							e.printStackTrace();
						} catch (InvalidTypeException e) {
							e.printStackTrace();
						}
						
					}
					else {
						throw new TranslationException("Add equation argument of type TripleElement does not have a NamedNode as subject (subject is of type " + 
								((TripleElement)((ProxyNode)arg).getProxyFor()).getSubject().getClass().getCanonicalName() + ")");
					}
				}
				else {
					throw new TranslationException("Added equation argument type '" + arg.getClass().getCanonicalName() + "' not yet supported.");
				}
			}
		}
		return nnArgs;
	}

	private String generateArguments(BuiltinElement robj, Rule modifiedRule) {
		StringBuilder sb = new StringBuilder();
		int argCnt = 0;
		for (Node arg : robj.getArguments()) {
			if (argCnt > 0) {
				sb.append(", ");
			}
			sb.append("double ");
			sb.append(arg.getName());
			String augt = getAugmentedTypeOfArg(arg, modifiedRule, argCnt);
			if (augt != null) {
				sb.append(" (");
				sb.append(augt);
				sb.append(")");
			}
			argCnt++;
		}
		return sb.toString();
	}
	
	private String getAugmentedTypeOfArg(Node arg, Rule rule, int argCnt) {
		TripleElement tr = findTripleWithObject(rule.getIfs(), arg);
		if (tr != null) {
			StringBuilder sb = new StringBuilder();
			TripleElement lastTr = tr;
			do {
				TripleElement tr2 = findTripleWithObject(rule.getIfs(), tr.getSubject());
				sb.append(tr.getPredicate().getName());
				sb.append(" of ");
				tr = tr2;
				if (tr != null) {
					lastTr = tr;
				}
			} while (tr != null);
			if (argCnt == 0) {
				sb.append("an ");
			}
			else {
				sb.append("the ");
			}
			if (lastTr.getSubject()instanceof VariableNode) {
				Node subjType = ((VariableNode)lastTr.getSubject()).getType();
				if (subjType instanceof NamedNode) {
					sb.append(((NamedNode)subjType).getName());
				}
			}
			else if (lastTr.getSubject()instanceof NamedNode) {
				sb.append(((NamedNode)lastTr.getSubject()).getName());
			}
			return sb.toString();
		}
		return null;
	}

	private TripleElement findTripleWithObject(List<GraphPatternElement> gpes, Node arg) {
		for (GraphPatternElement gpe : gpes) {
			if (gpe instanceof TripleElement && ((TripleElement)gpe).getObject().equals(arg)) {
				return (TripleElement) gpe;
			}
		}
		return null;
	}

	private String getAugmentedTypeOfReturn(Rule rule) {
		List<GraphPatternElement> thens = rule.getThens();
		if (thens != null && thens.size() == 1 && thens.get(0) instanceof TripleElement) {
			StringBuilder sb = new StringBuilder();
			TripleElement tr = (TripleElement) thens.get(0);
			sb.append(tr.getPredicate().getName());
			sb.append(" of ");
			sb.append("the ");
			if (tr.getSubject()instanceof VariableNode) {
				Node subjType = ((VariableNode)tr.getSubject()).getType();
				if (subjType instanceof NamedNode) {
					sb.append(((NamedNode)subjType).getName());
				}
			}
			else if (tr.getSubject()instanceof NamedNode) {
				sb.append(((NamedNode)tr.getSubject()).getName());
			}
			return sb.toString();
		}
		return null;
	}

	private BuiltinElement getTheTargetExpression(List<GraphPatternElement> thens, Object lobj) {
		if (lobj instanceof BuiltinElement) {
			for (GraphPatternElement gpe : thens) {
				if (gpe.equals(lobj)) {
					return (BuiltinElement) gpe;
				}
			}
		}
		return null;
	}

	private boolean allArgumentsVariables(BuiltinElement robj) {
		for (Node arg : robj.getArguments()) {
			if (arg instanceof ProxyNode && ((ProxyNode)arg).getProxyFor() instanceof BuiltinElement) {
				if (!allArgumentsVariables((BuiltinElement)((ProxyNode)arg).getProxyFor())) {
					return false;
				}
			}
			else if (arg instanceof ProxyNode && ((ProxyNode)arg).getProxyFor() instanceof TripleElement) {
				Node objNode = ((TripleElement)((ProxyNode)arg).getProxyFor()).getObject();
				// ??
			}
			else if (!(arg instanceof VariableNode) && !(arg instanceof Literal)) {
				return false;
			}
		}
		return true;
	}

	private String getNewEquationName(Object lhs) {
		if (lhs instanceof TripleElement) {
			Node subj = ((TripleElement)lhs).getSubject();
			if (subj instanceof NamedNode && !(subj instanceof VariableNode)) {
				return ((NamedNode)subj).getName();
			}
			Node pred = ((TripleElement)lhs).getPredicate();
			return pred.getName();
		}
		else if (lhs instanceof VariableNode) {
			return ((VariableNode)lhs).getName();
		}
		return "tbd";
	}

	private StatementContent processStatement(SadlEquationInvocation element) throws TranslationException, InvalidNameException, InvalidTypeException, IOException {
		SadlResource name = element.getName();
		Node srobj = processExpression(name);
		EvalContent ec = new EvalContent(element, Agent.USER);
		ec.setEquationName(srobj);
		
		EList<ParameterizedExpressionWithUnit> params = element.getParameters();
		if (! params.isEmpty()) {
			for (ParameterizedExpressionWithUnit param : params) {
				Object paramObj = processExpression(param.getExpression());
				if (!(paramObj instanceof Node)) {
					if (paramObj instanceof GraphPatternElement) {
						paramObj = nodeCheck(paramObj);
					}
					else if (paramObj instanceof List<?>) {
						paramObj = nodeCheck(getIfTranslator().listToAnd((List<GraphPatternElement>) paramObj));
					}
					else {
						throw new TranslationException("Parameter expression did not process to expected result");
					}
				}
				String unit = param.getUnit();
				UnittedParameter up = ec.new UnittedParameter((Node)paramObj, unit);
				ec.addParameter(up);
			}
		}
		return ec;
	}

	private boolean statementIsComplete(SadlModelElement element) {
	    Iterable<XtextSyntaxDiagnostic> syntaxErrors = Iterables.<XtextSyntaxDiagnostic>filter(element.eResource().getErrors(), XtextSyntaxDiagnostic.class);
		if (syntaxErrors.iterator().hasNext()) {
			ICompositeNode node = NodeModelUtils.findActualNodeFor(element);
			if (node.getSyntaxErrorMessage() == null) {
				return true;
			}
			return false;
		}
		return true;
	}

	private void initializeDialogContent() throws ConversationException, IOException {
		Resource resource = getCurrentResource();
		AnswerCurationManager cm = getAnswerCurationManager(resource);
		DialogContent dc = new DialogContent(resource, cm);
		cm.setConversation(dc);
		if (modelElements == null && resource != null) {
			modelElements = new ArrayList<ModelElementInfo>();
			getConfigMgr().addPrivateKeyMapValueByResource(DialogConstants.DIALOG_ELEMENT_INFOS, resource.getURI(), modelElements);
		}
		else {
			modelElements.clear();
		}
	}

	public AnswerCurationManager getAnswerCurationManager(Resource resource) throws IOException {
		if (answerCurationManager == null) {
			if (resource != null) {
				Object cm = getConfigMgr().getPrivateKeyMapValueByResource(DialogConstants.ANSWER_CURATION_MANAGER, resource.getURI());
				if (cm != null) {
					if (cm instanceof AnswerCurationManager) {
						answerCurationManager  = (AnswerCurationManager) cm;
					}
				}
				else {
					Map<String, String> pmap = null;
	//				Resource resource = Preconditions.checkNotNull(getCurrentResource(), "resource");
					pmap = getPreferences(resource);
					answerCurationManager = new AnswerCurationManager(getConfigMgr().getModelFolder(), getConfigMgr(),
							(XtextResource) resource, pmap);
					answerCurationManager.setDomainModelName(getModelName());
					answerCurationManager.setDomainModel(getTheJenaModel());
					getConfigMgr().addPrivateKeyMapValueByResource(DialogConstants.ANSWER_CURATION_MANAGER, resource.getURI(), answerCurationManager);
				}
			}
		}
		return answerCurationManager;
	}
	
	@Override
	public Map<String, String> getPreferences(Resource resource) {
		if (modelProcessorPreferenceMap != null) {
			return modelProcessorPreferenceMap;
		}
		// TODO this is a kludge to get SADL preferences, should be fixed
		EList<Resource> rsrcs = resource.getResourceSet().getResources();
		for (Resource rsr : rsrcs) {
			if (rsr.getURI().lastSegment().endsWith(".sadl")) {
				modelProcessorPreferenceMap = super.getPreferences(rsr);
				break;
			}
		}
		IPreferenceValuesProvider pvp = ((XtextResource)resource).getResourceServiceProvider().get(IPreferenceValuesProvider.class);
		IPreferenceValues preferenceValues = pvp.getPreferenceValues(resource);
		if (preferenceValues != null) {
			if (modelProcessorPreferenceMap == null) {
				modelProcessorPreferenceMap = new HashMap<String, String>();
			}
			String saveOriginal = preferenceValues.getPreference(DialogPreferences.ORIGINAL_LANGUAGE);
			if (saveOriginal != null) {
				modelProcessorPreferenceMap.put(DialogPreferences.ORIGINAL_LANGUAGE.getId(), saveOriginal);
			}
			String savePython = preferenceValues.getPreference(DialogPreferences.PYTHON_LANGUAGE);
			if (savePython != null) {
				modelProcessorPreferenceMap.put(DialogPreferences.PYTHON_LANGUAGE.getId(), savePython);
			}
			String savePythonTF = preferenceValues.getPreference(DialogPreferences.OTHER_PYTHON_LANGUAGE);
			if (savePythonTF != null) {
				modelProcessorPreferenceMap.put(DialogPreferences.OTHER_PYTHON_LANGUAGE.getId(), savePythonTF);
			}
			String tsburl = preferenceValues.getPreference(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI);
			if (tsburl != null) {
				modelProcessorPreferenceMap.put(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getId(), tsburl);
			}
			String j2psburl = preferenceValues.getPreference(DialogPreferences.ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI);
			if (j2psburl != null) {
				modelProcessorPreferenceMap.put(DialogPreferences.ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI.getId(), j2psburl);
			}
			String usekchain = preferenceValues.getPreference(DialogPreferences.USE_ANSWER_KCHAIN_CG_SERVICE);
			if (usekchain != null) {
				modelProcessorPreferenceMap.put(DialogPreferences.USE_ANSWER_KCHAIN_CG_SERVICE.getId(), usekchain);
			}
			String kchaincgsburl = preferenceValues.getPreference(DialogPreferences.ANSWER_KCHAIN_CG_SERVICE_BASE_URI);
			if (kchaincgsburl != null) {
				modelProcessorPreferenceMap.put(DialogPreferences.ANSWER_KCHAIN_CG_SERVICE_BASE_URI.getId(), kchaincgsburl);
			}
			String usedbn = preferenceValues.getPreference(DialogPreferences.USE_DBN_CG_SERVICE);
			if (usedbn != null) {
				modelProcessorPreferenceMap.put(DialogPreferences.USE_DBN_CG_SERVICE.getId(), usedbn);
			}
			String dbncgsburl = preferenceValues.getPreference(DialogPreferences.ANSWER_DBN_CG_SERVICE_BASE_URI);
			if (dbncgsburl != null) {
				modelProcessorPreferenceMap.put(DialogPreferences.ANSWER_DBN_CG_SERVICE_BASE_URI.getId(), dbncgsburl);
			}
			String dbnjsongensburl = preferenceValues.getPreference(DialogPreferences.DBN_INPUT_JSON_GENERATION_SERVICE_BASE_URI);
			if (dbnjsongensburl != null) {
				modelProcessorPreferenceMap.put(DialogPreferences.DBN_INPUT_JSON_GENERATION_SERVICE_BASE_URI.getId(), dbnjsongensburl);
			}
			String invizinserviceurl = preferenceValues.getPreference(DialogPreferences.ANSWER_INVIZIN_SERVICE_BASE_URI);
			if (invizinserviceurl != null) {
				modelProcessorPreferenceMap.put(DialogPreferences.ANSWER_INVIZIN_SERVICE_BASE_URI.getId(), invizinserviceurl);
			}
			String codeextractionkbaseroot = preferenceValues.getPreference(DialogPreferences.ANSWER_CODE_EXTRACTION_KBASE_ROOT);
			if (codeextractionkbaseroot != null) {
				modelProcessorPreferenceMap.put(DialogPreferences.ANSWER_CODE_EXTRACTION_KBASE_ROOT.getId(), codeextractionkbaseroot);
			}
			String shortgraphlink = preferenceValues.getPreference(DialogPreferences.SHORT_GRAPH_LINK);
			if (shortgraphlink != null) {
				modelProcessorPreferenceMap.put(DialogPreferences.SHORT_GRAPH_LINK.getId(), shortgraphlink);
			}
			String verboseExtraction = preferenceValues.getPreference(DialogPreferences.VERBOSE_EXTRACTION);
			if (verboseExtraction != null) {
				modelProcessorPreferenceMap.put(DialogPreferences.VERBOSE_EXTRACTION.getId(), verboseExtraction);
			}
			return modelProcessorPreferenceMap;
		}
		return null;
	}

	private void autoSaveModel(Resource resource, String modelFolder, File saveFile, ProcessorContext context) throws IOException, URISyntaxException {
		String format = getOwlModelFormat(context);
		RDFWriter w = getTheJenaModel().getWriter(format);
		w.setProperty("xmlbase", getModelName());
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		w.write(getTheJenaModel().getBaseModel(), out, getModelName());
		Charset charset = Charset.forName("UTF-8");
		CharSequence seq = new String(out.toByteArray(), charset);
		(new SadlUtils()).stringToFile(saveFile, String.valueOf(seq), false);

	
		List<String[]> newMappings = new ArrayList<String[]>();
		String[] mapping = new String[3];
		SadlUtils su = new SadlUtils();
		mapping[0] = su.fileNameToFileUrl(saveFile.getCanonicalPath());
		mapping[1] = getModelName();
		mapping[2] = getModelAlias();

		newMappings.add(mapping);

		// Output the Rules and any other knowledge structures via the specified
		// translator
		List<Object> otherContent = OntModelProvider.getOtherContent(resource);
		if (otherContent != null) {
			for (int i = 0; i < otherContent.size(); i++) {
				Object oc = otherContent.get(i);
				if (oc instanceof List<?>) {
					if (((List<?>) oc).get(0) instanceof Rule) {
						setRules((List<Rule>) oc);
					}
				}
			}
		}
		List<ModelError> results = translateAndSaveModel(resource, saveFile.getName(), format, newMappings, "Dialog");
		if (results != null) {
			generationInProgress = false; // we need these errors to show up
			modelErrorsToOutput(resource, results, false);
		}
}

	private File getModelFile(Resource resource) {
		if (getMetricsProcessor() instanceof MetricsProcessor) {
			String mfp = ((MetricsProcessor) getMetricsProcessor()).getModelFolderPath(resource);
			String name = resource.getURI().lastSegment();
			String saveFN = mfp + "/" + name + ".owl";
			File saveFile = new File(saveFN);
			return saveFile;
		}
		return null;
	}

	
	private void autoSaveModel(Resource rsrc) {
		URI uri = rsrc.getURI();
		Path trgtpath;
		if (uri.isFile()) {
			trgtpath = new File(rsrc.getURI().toFileString()).toPath();
		}
		else {
			IFile trgtfile = getFile(rsrc);
			trgtpath = trgtfile.getLocation().toFile().toPath();
		}
		try {
			URI absuri = URI.createURI(trgtpath.toUri().toString());
			String modelFolderUri = ResourceManager.findModelFolderPath(absuri);
			getTheJenaModel();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		finally {

		}
	}

	public static IFile getFile(Resource resource) {
		ResourceSet rset = resource.getResourceSet();
// TODO how to get the IFile from the Resource??
		IFile retFile = null;
//		return getFile(resource.getURI(), (rset != null) ? rset.getURIConverter() : null, false);
		return retFile;
	}

	@Override
	public void onGenerate(Resource resource, IFileSystemAccess2 fsa, ProcessorContext context) {
		if (!resource.getURI().toString().endsWith(".dialog")) {
			return;
		}
		super.onGenerate(resource, fsa, context);
		try {
			getAnswerCurationManager(resource).saveQuestionsAndAnswersToFile();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
	}

	protected String getOwlFilename(URI lastSeg, String format) throws TranslationException {
		String owlFN = lastSeg.appendFileExtension(ResourceManager.getOwlFileExtension(format))
				.lastSegment().toString();
		return owlFN;
	}

	private void resetProcessor() {
		// TODO Auto-generated method stub
		
	}
	
	private StatementContent  processAnswerCMStatement( AnswerCMStatement element) throws IOException, TranslationException, InvalidNameException, InvalidTypeException, ConfigurationException, JenaProcessorException, QueryParseException, QueryCancelledException, ReasonerNotFoundException, PrefixNotFoundException {
		EObject stmt = element.getSstmt();
		if (stmt != null) {
			StatementContent sc = processDialogModelElement(stmt, getCurrentResource());
//			AnswerContent ac = new AnswerContent(element, Agent.CM);
//			ac.setAnswer(sc);
//			return ac;
			return sc;
		}
		else {
			String str = element.getStr();
			if (str != null) {
				String eos = getEos(element);
				if (eos.equals(".")) {
					AnswerContent ac = new AnswerContent(element, Agent.CM);
					ac.setAnswer(str);
					return ac;
				}
				else if (eos.equals("?")) {
					QuestionWithCallbackContent qwcc = new QuestionWithCallbackContent(element, Agent.CM, null, null, str);
					return qwcc;
				}
				else {
					throw new IOException("Statement has unexpected ending character.");
				}
			}
		}
		return null;
	}
	
	private StatementContent processStatement(ComparisonTableStatement element) throws InvalidNameException, InvalidTypeException, TranslationException {
		ValueTable ct = element.getComparisonTable();
		if (ct != null) {
			AnswerContent ac = new AnswerContent(element, Agent.CM);
			Object ctObj = processExpression(ct);
			ac.setOtherResults(ctObj);
			ac.setAnswer(ctObj.toString());
			return ac;
		}
		return null;
	}

	private StatementContent processStatement(SaveStatement element) throws IOException, ConfigurationException, QueryParseException, QueryCancelledException, ReasonerNotFoundException {
		SadlResource equationSR = ((SaveStatement)element).getTarget();
		String targetModelUri = null;
		String targetModelUrl = null;
		String targetModelAlias = ((SaveStatement)element).getSaveTarget();
		Map<String, String[]> targetModelMap = getAnswerCurationManager(getCurrentResource()).getTargetModelMap();
		if (targetModelMap == null || targetModelMap.size() < 1) {
			addError("No target models have been identified. Cannot identify a model into which to save.", element);
		}
		else {
			if (targetModelAlias != null) {
				String[] uris = targetModelMap.get(targetModelAlias);
				if (uris == null) {
					addError("Model with alias '" + targetModelAlias + "' not found in target models.", element);
				}
				else {
					targetModelUri = uris[0];
					targetModelUrl = uris[1];
				}
			}
			else {
				if (targetModelMap.size() > 1){
					addError("There are multiple target models identified; please specify which one to save to.", element);
				}
				else {
					String[] uris = targetModelMap.get(targetModelMap.keySet().iterator().next());
					targetModelUri = uris[0];
					targetModelUrl = uris[1];
				}
			}
			if (targetModelUri != null) {
				SaveContent sc = new SaveContent(element, Agent.USER);
				sc.setTargetModelAlias(targetModelAlias);
				if (element.getAll() != null && element.getAll().equals("all")) {
					sc.setSaveAll(true);
				}
				else {
					String equationUri = getDeclarationExtensions().getConceptUri(equationSR);
					Individual extractedModelInstance = getTheJenaModel().getIndividual(equationUri);
					if (extractedModelInstance == null) {
						addError("No equation with URI '" + equationUri + "' is found in current model.", equationSR);
						return null;
					}
					else if (extractedModelInstance.getNameSpace().equals(targetModelAlias)) {
						getAnswerCurationManager(getCurrentResource()).notifyUser(getConfigMgr().getModelFolder(), "The equation with URI '" + equationUri + "' is already in the target model '" + targetModelAlias + "'", true);
					}
					sc.setSourceEquationUri(extractedModelInstance.getURI());
				}
				return sc;
			}
		}
		return null;
	}

	private ModifiedAskContent processStatement(ModifiedAskStatement stmt) {
		ConversationElement ce = null;
		try {
			SadlResource elementName = null; // element.getName();
			EList<NamedStructureAnnotation> annotations = null; // element.getAnnotations();
			boolean isGraph = stmt.getStart().equals("Graph");
			Query query = processQueryExpression(stmt, stmt.getExpr(), elementName, annotations, isGraph);
			if (stmt.getParameterizedValues() != null) {
				EList<Expression> rowvals = stmt.getParameterizedValues().getExplicitValues();
				List<Object> rowObjects = new ArrayList<Object>();
				for (Expression val : rowvals) {
					Object valObj = processExpression(val);
					rowObjects.add(valObj);
				}
				query.setParameterizedValues(rowObjects);
				query.setContext(stmt);
			}
			if (query.getKeyword() == null && (stmt.getStart().equalsIgnoreCase("ask") || stmt.getStart().equalsIgnoreCase("find"))) {
				query.setKeyword("select");
			}
//			System.out.println("ModifiedAskStatement: " + query.toDescriptiveString());
			ModifiedAskContent mac = new ModifiedAskContent(stmt, Agent.USER);
			mac.setQuery(query);
			return mac;
		} catch (CircularDefinitionException e) {
			e.printStackTrace();
		} catch (InvalidNameException e) {
			e.printStackTrace();
		} catch (InvalidTypeException e) {
			e.printStackTrace();
		} catch (TranslationException e) {
			e.printStackTrace();
		} catch (JenaProcessorException e) {
			e.printStackTrace();
//		} catch (IOException e) {
//			e.printStackTrace();
		}
		return null;
	}

	private StatementContent processStatement(WhatStatement stmt) {
		try {
			EObject substmt = stmt.getStmt();
			if (substmt instanceof WhatIsStatement) {
				return processStatement((WhatIsStatement)substmt);
			}
			else if (substmt instanceof WhatValuesStatement) {
				return processStatement((WhatValuesStatement)substmt);
			}
			else if (substmt instanceof WhatTypeStatement) {
				return processStatement((WhatTypeStatement)substmt);
			}
		} catch (UndefinedConceptException e) {
			return e.getWhatIsContent();
		}
		return null;
	}
	
	private StatementContent processStatement(WhatTypeStatement stmt) {
		try {
			if (stmt.getTarget() != null) {
				addVariableAllowedInContainerType(stmt.getClass());
				Object trgtObj = processExpression(stmt.getTarget());
				WhatIsContent wic = new WhatIsContent(stmt.eContainer(), Agent.CM, trgtObj, stmt);
				wic.setUnParsedText(getSourceText(stmt.eContainer()).trim());
				if (!(trgtObj instanceof VariableNode)) {
					addWarning("The argument name is also a concept in the ontology, which may be confusing.", stmt.getTarget());
				}
				return wic;
			}
			else if (stmt.getEquation() != null) {
				Object eqObj = processExpression(stmt.getEquation());
				if (eqObj instanceof NamedNode) {
					WhatIsContent wic = new WhatIsContent(stmt.eContainer(), Agent.CM, eqObj, stmt);
					wic.setUnParsedText(getSourceText(stmt.eContainer()).trim());
					return wic;
				}
			}
		} catch (InvalidNameException e) {
			e.printStackTrace();
		} catch (InvalidTypeException e) {
			e.printStackTrace();
		} catch (TranslationException e) {
			e.printStackTrace();
		}
		return null;
	}

	private StatementContent processStatement(WhatIsStatement stmt) throws UndefinedConceptException {
		EObject whatIsTarget = stmt.getTarget();
		EObject when = stmt.getWhen();
		if (whatIsTarget == null) {
			// this is a request for user name
			
		}
		boolean generateComparisonRulesForCG = true;
		
		if (whatIsTarget instanceof Declaration) {
			whatIsTarget = ((Declaration)whatIsTarget).getType();
			if (whatIsTarget instanceof SadlSimpleTypeReference) {
				whatIsTarget = ((SadlSimpleTypeReference)whatIsTarget).getType();
				if (when == null) {
					generateComparisonRulesForCG = false;
				}
			}
		}
		else if (whatIsTarget instanceof Name) {
			if (when == null) {
				generateComparisonRulesForCG = false;
			}
		}
		Object trgtObj;
		if (!generateComparisonRulesForCG) {
			try {
				trgtObj = processExpression(whatIsTarget);
//				System.out.println("WhatIsStatement target: " + trgtObj.toString());
				if (trgtObj instanceof NamedNode) {
					((NamedNode)trgtObj).setContext(stmt);
				}
				else if (trgtObj instanceof Junction) {
					if (stmt.eContainer() instanceof WhatIsStatement) {
						setGraphPatternContext((WhatStatement) stmt.eContainer(), whatIsTarget, trgtObj);
					}
				}
				else if (trgtObj instanceof TripleElement) {
					((TripleElement)trgtObj).setContext(stmt);
				}
				else if (trgtObj instanceof Object[]) {
					for (int i = 0; i < ((Object[])trgtObj).length; i++) {
						Object obj = ((Object[])trgtObj)[i];
						if (stmt.eContainer() instanceof WhatIsStatement) {
							setGraphPatternContext((WhatStatement) stmt, whatIsTarget, obj);
						}
					}
				}
				else {
					// TODO
					if (trgtObj != null) {
						addInfo(trgtObj.getClass().getCanonicalName() + " not yet handled by dialog processor", whatIsTarget);
					}
					else {
						addError("Target object not resolved", trgtObj != null ? whatIsTarget : stmt);
					}
				}
				Object whenObj = when != null ? processExpression(when) : null;
			
				// apply implied/expanded properties
				DialogIntermediateFormTranslator dift = new DialogIntermediateFormTranslator(this, getTheJenaModel());
				if (trgtObj instanceof GraphPatternElement) {
					trgtObj = dift.addImpliedAndExpandedProperties((GraphPatternElement)trgtObj);
				}
				else if (trgtObj instanceof List<?>) {
					dift.addImpliedAndExpandedProperties((List<GraphPatternElement>) trgtObj);
				}
				if (whenObj instanceof GraphPatternElement) {
					whenObj = dift.addImpliedAndExpandedProperties((GraphPatternElement)whenObj);
					List<GraphPatternElement> gpes = new ArrayList<GraphPatternElement>();
					gpes = unitSpecialConsiderations(gpes, whenObj, whatIsTarget);
					Object temp = dift.cook(gpes, false);
					if (temp instanceof List<?>) {
						if (((List<?>)temp).size() == 1) {
							whenObj = ((List<?>)temp).get(0);
						}
						else {
							// ?
						}
					}
				}
				else if (whenObj instanceof List<?>) {
					dift.addImpliedAndExpandedProperties((List<GraphPatternElement>) whenObj);
					// does this ever happen? More to do...
				}
				WhatIsContent wic = new WhatIsContent(stmt.eContainer(), Agent.USER, trgtObj, whenObj);
				return wic;
			}
			catch (TranslationException e) {
				
			} catch (InvalidNameException e) {
				e.printStackTrace();
			} catch (InvalidTypeException e) {
				e.printStackTrace();
			}
		}
		else {
			// compute answer using computational graph
			List<Rule> comparisonRules = null;
			try {
				clearUndefinedEObjects();
				try {
					comparisonRules = whenAndThenToCookedRules(when, whatIsTarget);
				} catch (UndefinedConceptException e1) {
					return e1.getWhatIsContent();
				}
				if (comparisonRules != null && comparisonRules.size() > 0) {
					WhatIsContent wic = new WhatIsContent(stmt.eContainer(), Agent.USER, comparisonRules);
					return wic;
				}
				else {
					List<EObject> udeobjs = getUndefinedEObjects();
					if (udeobjs.size() > 0) {
						WhatIsContent wic = new WhatIsContent(udeobjs.get(0), Agent.CM, null, null);
						try {
							String name = getEObjectName(udeobjs.get(0));
							wic.setExplicitQuestion("Concept " + getAnswerCurationManager(getCurrentResource()).checkForKeyword(name) + " is not defined; please define or do extraction");
						} catch (IOException e) {
							e.printStackTrace();
						}
						return wic;					
					}
				}
			} catch (InvalidNameException | InvalidTypeException | TranslationException e) {
				e.printStackTrace();
			}
	//		} catch (TranslationException e) {
	//			e.printStackTrace();
	//		} catch (InvalidNameException e) {
	//			e.printStackTrace();
	//		} catch (InvalidTypeException e) {
	//			e.printStackTrace();
	//		}
		}
		return null;
	}

	private String getEObjectName(EObject eobj) {
		String name = null;
		if (eobj instanceof SadlResource) {
			name = getDeclarationExtensions().getConcreteName((SadlResource) eobj);
		}
		else if (eobj instanceof SadlSimpleTypeReference) {
			
		}
		else if (eobj instanceof Declaration) {
			ICompositeNode icn = NodeModelUtils.findActualNodeFor(eobj);
			if (icn != null) {
				String ntxt = (icn.getText());
				if (ntxt != null) {
					if (ntxt.trim().startsWith("the ")) {
						name = ntxt.trim().substring(4);
					}
				}
			}
		}
		if (name == null) {
			ICompositeNode icn = NodeModelUtils.findActualNodeFor(eobj);
			if (icn != null) {
				String ntxt = (icn.getText());
				if (ntxt != null) {
						name = ntxt;
				}
			}
		}
		return name;
	}

	private List<GraphPatternElement> unitSpecialConsiderations(List<GraphPatternElement> gpes, Object whenObj, EObject whatIsTarget)
			throws TranslationException, InvalidNameException, UndefinedConceptException {
		if (!isIgnoreUnittedQuantities()) {
			if (whenObj instanceof Junction) {
				Object lhs = ((Junction)whenObj).getLhs();
				if (lhs instanceof ProxyNode && ((ProxyNode)lhs).getProxyFor() instanceof GraphPatternElement) {
					gpes = unitSpecialConsiderations(gpes, ((ProxyNode) lhs).getProxyFor(), whatIsTarget);
				}
				Object rhs = ((Junction)whenObj).getRhs();
				if (rhs instanceof ProxyNode && ((ProxyNode)rhs).getProxyFor() instanceof GraphPatternElement) {
					gpes = unitSpecialConsiderations(gpes, ((ProxyNode) rhs).getProxyFor(), whatIsTarget);
				}
			}
			else if (whenObj instanceof BuiltinElement) {
				List<Node> args = ((BuiltinElement)whenObj).getArguments();
				OntClass expectedUnittedQuantityClass = null;
				for (Node arg : args) {
					try {
						if (arg instanceof Literal && 
								((((Literal)arg).getUnits() != null || expectedUnittedQuantityClass != null))) {
							OntClass unittedQuantitySubclass = getTheJenaModel().getOntClass(SadlConstants.SADL_IMPLICIT_MODEL_UNITTEDQUANTITY_URI);
							if (expectedUnittedQuantityClass != null) {
								if (SadlUtils.classIsSubclassOf(expectedUnittedQuantityClass, unittedQuantitySubclass, true, null)) {
									unittedQuantitySubclass = expectedUnittedQuantityClass;
								}
							}
							VariableNode var = new VariableNode(getNewVar(whatIsTarget));
							NamedNode type = new NamedNode(unittedQuantitySubclass.getURI());
							type.setNodeType(NodeType.ClassNode);
							var.setType(validateNode(type));
							Literal valueLiteral = (Literal) arg;
							int idx = args.indexOf(arg);
							args.set(idx, var);
							String units = valueLiteral.getUnits();
							TripleElement varTypeTriple = new TripleElement(var, new RDFTypeNode(), type);
							NamedNode valueProp = new NamedNode(SadlConstants.SADL_IMPLICIT_MODEL_VALUE_URI);
							valueProp.setNodeType(NodeType.DataTypeProperty);
							TripleElement valueTriple = new TripleElement(var, valueProp, valueLiteral);
							gpes.add(varTypeTriple);
							gpes.add(valueTriple);
							if (units != null) {
								Literal unitsLiteral = new Literal();
								unitsLiteral.setValue(units);
								valueLiteral.setUnits(null);
								NamedNode unitProp = new NamedNode(SadlConstants.SADL_IMPLICIT_MODEL_UNIT_URI);
								unitProp.setNodeType(NodeType.DataTypeProperty);
								TripleElement unitTriple = new TripleElement(var, unitProp, unitsLiteral);
								gpes.add(unitTriple);
							}	
							if (args.get(0) instanceof NamedNode && ((NamedNode)args.get(0)).getImpliedPropertyNode() != null) {
								NamedNode ipn = ((NamedNode)args.get(0)).getImpliedPropertyNode();
								if (ipn.getURI().equals(SadlConstants.SADL_IMPLICIT_MODEL_VALUE_URI)) {
									((NamedNode)args.get(0)).setImpliedPropertyNode(null);
								}
							}
						}
						else if ((arg instanceof ProxyNode &&									// this operand to the || is for when no unit is given but there is an implied property and 
								// value of the UnittedQuantity is given
								((ProxyNode)arg).getProxyFor() instanceof TripleElement &&
								((TripleElement)((ProxyNode)arg).getProxyFor()).getPredicate() instanceof NamedNode &&
								((NamedNode)((TripleElement)((ProxyNode)arg).getProxyFor()).getPredicate()).getURI().equals(SadlConstants.SADL_IMPLICIT_MODEL_VALUE_URI))) {
							
						}
						else if (arg instanceof NamedNode && isProperty(((NamedNode)arg).getNodeType())) {
							if (((BuiltinElement)whenObj).getFuncType().equals(BuiltinType.Equal)) {
								// find the range--that's the expected type of the next argument
								expectedUnittedQuantityClass = getUnittedQuantityOrSubclassPropertyRange(((NamedNode)arg).getURI());
							}
						}
					} catch (CircularDependencyException e) {
						e.printStackTrace();
					} catch (TranslationException e) {
						e.printStackTrace();
					} catch (InvalidNameException e) {
						e.printStackTrace();
					}
				}
				gpes.add((GraphPatternElement) whenObj);
			}
			else if (whenObj instanceof TripleElement && 
				((((TripleElement)whenObj).getObject() instanceof Literal &&
				((Literal)(((TripleElement)whenObj).getObject())).getUnits() != null) ||			// this operand to the || is for when the unit of a UnittedQuanity is given
				(((TripleElement)whenObj).getObject() instanceof ProxyNode &&		// this operand to the || is for when no unit is given but there is an implied property and 
																					// value of the UnittedQuantity is given
						((ProxyNode)((TripleElement)whenObj).getObject()).getProxyFor() instanceof TripleElement &&
						((TripleElement)((ProxyNode)((TripleElement)whenObj).getObject()).getProxyFor()).getPredicate() instanceof NamedNode &&
				((NamedNode)((TripleElement)((ProxyNode)((TripleElement)whenObj).getObject()).getProxyFor()).getPredicate()).getURI().equals(SadlConstants.SADL_IMPLICIT_MODEL_VALUE_URI)))) {
				String propUri = ((TripleElement)whenObj).getPredicate().getURI();
				// create a typed variable for the UnittedQuantity blank node, actual type range of prop
				OntClass unittedQuantitySubclass = getUnittedQuantityOrSubclassPropertyRange(propUri);
				if (unittedQuantitySubclass == null) {
					if (((TripleElement)whenObj).getPredicate() instanceof VariableNode) {
						// predicate cannot be a variable--means the question has an undefined concept
						VariableNode pvar = (VariableNode) ((TripleElement)whenObj).getPredicate();
						addWarning(pvar.getName() + " is not defined.", (EObject) ((TripleElement)whenObj).getContext());
						WhatIsContent wic = new WhatIsContent((EObject) ((TripleElement)whenObj).getContext(), Agent.CM, pvar, null);	
						// this is Agent.CM because it houses a question/statement for the user from the CM
						String msg;
						try {
							msg = "Concept " + getAnswerCurationManager(getCurrentResource()).checkForKeyword(pvar.getName()) + " is not defined; please define or do extraction";
							wic.setExplicitQuestion(msg);
						} catch (IOException e) {
							msg = e.getMessage();
							e.printStackTrace();
						}
						throw new UndefinedConceptException(msg, wic);
					}
				}
				else {
					VariableNode var = new VariableNode(getNewVar(whatIsTarget));
					NamedNode type = new NamedNode(unittedQuantitySubclass.getURI());
					type.setNodeType(NodeType.ClassNode);
					var.setType(validateNode(type));
					Literal valueLiteral;
					if (((TripleElement)whenObj).getObject() instanceof Literal) {
						valueLiteral = (Literal) ((TripleElement)whenObj).getObject();
					}
					else {
						valueLiteral = (Literal) ((TripleElement)((ProxyNode)((TripleElement)whenObj).getObject()).getProxyFor()).getObject();
					}
					((TripleElement)whenObj).setObject(var);
					String units = valueLiteral.getUnits();
					TripleElement varTypeTriple = new TripleElement(var, new RDFTypeNode(), type);
					NamedNode predNode = new NamedNode(SadlConstants.SADL_IMPLICIT_MODEL_VALUE_URI);
					predNode.setNodeType(NodeType.DataTypeProperty);
					TripleElement valueTriple = new TripleElement(var, predNode, valueLiteral);
					gpes.add(varTypeTriple);
					gpes.add((TripleElement)whenObj);
					gpes.add(valueTriple);
					if (units != null) {
						Literal unitsLiteral = new Literal();
						unitsLiteral.setValue(units);
						valueLiteral.setUnits(null);
						NamedNode unitPred = new NamedNode(SadlConstants.SADL_IMPLICIT_MODEL_UNIT_URI);
						unitPred.setNodeType(NodeType.DataTypeProperty);
						TripleElement unitTriple = new TripleElement(var, unitPred, unitsLiteral);
						gpes.add(unitTriple);
					}
				}
			}
			//				else if (!ignoreUnittedQuantities && whenObj instanceof BuiltinElement &&
			//						((BuiltinElement)whenObj).getFuncType().equals(BuiltinType.Equal) &&
			//						((BuiltinElement)whenObj).getArguments().get(1) instanceof Literal &&
			//						((Literal)((BuiltinElement)whenObj).getArguments().get(1)).getUnits() != null) {
			//					Literal val = (Literal)((BuiltinElement)whenObj).getArguments().get(1);
			//					String units = val.getUnits();
			//					val.setUnits(null);
			//					Literal unitsLiteral = new Literal();
			//					unitsLiteral.setValue(units);
			//					TripleElement valueTriple = new TripleElement(((BuiltinElement)whenObj).getArguments().get(0), 
			//							new NamedNode(SadlConstants.SADL_IMPLICIT_MODEL_VALUE_URI), val);
			//					TripleElement unitTriple = new TripleElement(((BuiltinElement)whenObj).getArguments().get(0), 
			//							new NamedNode(SadlConstants.SADL_IMPLICIT_MODEL_UNIT_URI), unitsLiteral);
			//					gpes.add(valueTriple);
			//					gpes.add(unitTriple);
			//				}
			else {
				gpes.add((GraphPatternElement) whenObj);
			}
		}
		else {
			gpes.add((GraphPatternElement) whenObj);
		}
		return gpes;
	}

	/**
	 * Method to get the range of a property whose subclass should be UnittedQuantity or a subclass thereof
	 * @param propUri
	 * @return
	 */
	private OntClass getUnittedQuantityOrSubclassPropertyRange(String propUri) {
		OntClass unittedQuantitySubclass = null;
		Property prop = getTheJenaModel().getProperty(propUri);
		if (prop != null) {
			StmtIterator rngItr = getTheJenaModel().listStatements(prop.asResource(), RDFS.range, (RDFNode)null);
			if (rngItr.hasNext()) {
				RDFNode rng = rngItr.nextStatement().getObject();
				if (!rngItr.hasNext()) {
					if (rng.isURIResource() && rng.canAs(OntClass.class)) {
						unittedQuantitySubclass = rng.as(OntClass.class);
					}
				}
				if (unittedQuantitySubclass == null) {
					// apparently has more than 1 range, use UnittedQuantity
					unittedQuantitySubclass = getTheJenaModel().getOntClass(SadlConstants.SADL_IMPLICIT_MODEL_UNITTEDQUANTITY_URI);
				}
			}
		}
		return unittedQuantitySubclass;
	}
	
	/**
	 * Method to get the range of a property by URI
	 * @param propUri
	 * @return 
	 * @return
	 */
	private org.apache.jena.rdf.model.Resource getUniquePropertyRange(String propUri) {
		Property prop = getTheJenaModel().getProperty(propUri);
		if (prop != null) {
			StmtIterator rngItr = getTheJenaModel().listStatements(prop.asResource(), RDFS.range, (RDFNode)null);
			if (rngItr.hasNext()) {
				RDFNode rng = rngItr.nextStatement().getObject();
				if (rng.isURIResource()) {
					org.apache.jena.rdf.model.Resource rngrsrc = rng.asResource();
					return rngrsrc;
				}
			}
		}
		return null;
	}
	
	private StatementContent processStatement(WhatValuesStatement stmt) {
		String article = stmt.getArticle();
		SadlTypeReference cls = stmt.getCls();
		SadlResource prop = stmt.getProp();
		String typ = stmt.getTyp(); 		// "can" or "must"
		try {
			Object clsObj = processExpression(cls);
			Object propObj = processExpression(prop);
//				System.out.println("WhatValuesStatement(" + typ + "): cls=" + (article!= null ? article : "") + 
//						" '" + clsObj.toString() + "'; prop='" + propObj.toString() + "'");
			WhatValuesContent wvc = new WhatValuesContent(stmt.eContainer(), Agent.USER);
			wvc.setArticle(article);
			wvc.setCls(nodeCheck(clsObj));
			wvc.setProp(nodeCheck(propObj));
			wvc.setTypeof(typ);
			return wvc;
		} catch (TranslationException e) {
			e.printStackTrace();
		} catch (InvalidNameException e) {
			e.printStackTrace();
		} catch (InvalidTypeException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Method to set the contextual EObject of a GraphPatternElement
	 * @param stmt
	 * @param whatIsTarget
	 * @param obj
	 */
	private void setGraphPatternContext(WhatStatement stmt, EObject whatIsTarget, Object obj) {
		if (obj instanceof TripleElement) {
			((TripleElement)obj).setContext(stmt.getStmt());
		}
		else if (obj instanceof Junction) {
			setGraphPatternContext(stmt, whatIsTarget, ((ProxyNode)((Junction)obj).getLhs()).getProxyFor());
			setGraphPatternContext(stmt, whatIsTarget, ((ProxyNode)((Junction)obj).getRhs()).getProxyFor());;
		}
		else {
			addInfo(obj.getClass().getCanonicalName() + " in array not yet handled by dialog processor", whatIsTarget);
		}
	}

	private StatementContent processStatement(HowManyValuesStatement stmt) throws IOException {
		String article = stmt.getArticle();
		SadlTypeReference cls = stmt.getCls();
		SadlResource prop = stmt.getProp();
		SadlTypeReference typ = stmt.getTyp(); 
		try {
			Object clsObj = cls != null ? processExpression(cls) : null;
			Object propObj = prop != null ? processExpression(prop) : null;
			Object typObj = typ != null ? processExpression(typ) : null;
//			System.out.println("HowManyValuesStatement: cls=" + (article!= null ? article : "") + " '" + 
//					clsObj.toString() + "'; prop='" + propObj.toString() + 
//					"'" + (typObj != null ? ("; type='" + typObj.toString() + "'") : ""));
			HowManyValuesContent hmvc = new HowManyValuesContent(stmt, Agent.USER);
			if (propObj != null) {
				hmvc.setProp(nodeCheck(propObj));
			}
			if (typObj != null) {
				hmvc.setTyp(nodeCheck(typObj));
			}
			if (article != null) {
				hmvc.setArticle(article);
			}
			if (clsObj != null) {
				hmvc.setCls(nodeCheck(clsObj));
			}
			return hmvc;
		} catch (TranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	protected boolean isContainedByQuery(Expression expr) {
		if (EcoreUtil2.getContainerOfType(expr, QueryStatement.class) != null) {
			return true;
		}
		else if (EcoreUtil2.getContainerOfType(expr, ModifiedAskStatement.class)!= null) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isSupported(String fileExtension) {
		return "dialog".equals(fileExtension);
	}

	private boolean isModelChanged() {
		return modelChanged;
	}

	private void setModelChanged(boolean modelChanged) {
		this.modelChanged = modelChanged;
	}

	@Override
	public void initializePreferences(ProcessorContext context) {
		super.initializePreferences(context);
		
		String saveOriginal = context.getPreferenceValues().getPreference(DialogPreferences.ORIGINAL_LANGUAGE);
		if (saveOriginal != null) {
			setSaveOriginal(Boolean.parseBoolean(saveOriginal.trim()));
		}
		String savePython = context.getPreferenceValues().getPreference(DialogPreferences.PYTHON_LANGUAGE);
		if (savePython != null) {
			setSavePython(Boolean.parseBoolean(savePython.trim()));
		}
		String savePythonTF = context.getPreferenceValues().getPreference(DialogPreferences.OTHER_PYTHON_LANGUAGE);
		if (savePythonTF != null) {
			setSavePythonTF(Boolean.parseBoolean(savePythonTF.trim()));
		}

		String textServiceUrl = context.getPreferenceValues().getPreference(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI);
		if (textServiceUrl != null) {
			setTextServiceUrl(textServiceUrl);
		}
		String useDbn = context.getPreferenceValues().getPreference(DialogPreferences.USE_DBN_CG_SERVICE);
		if (useDbn != null) {
			setUseDbn(Boolean.parseBoolean(useDbn.trim()));
		}
		String dbncgserviceurl = context.getPreferenceValues().getPreference(DialogPreferences.ANSWER_DBN_CG_SERVICE_BASE_URI);
		if (dbncgserviceurl != null) {
			setDbnCgServiceUrl(dbncgserviceurl);
		}
		String dbninputjsongenerationserviceurl = context.getPreferenceValues().getPreference(DialogPreferences.DBN_INPUT_JSON_GENERATION_SERVICE_BASE_URI);
		if (dbninputjsongenerationserviceurl != null) {
			setDbnInputJsonGenerationServiceUrl(dbninputjsongenerationserviceurl);
		}
		String invizinserviceurl = context.getPreferenceValues().getPreference(DialogPreferences.ANSWER_INVIZIN_SERVICE_BASE_URI);
		if (invizinserviceurl != null) {
			setInvizinserviceurl(invizinserviceurl);
		}
		String kchaincgserviceurl = context.getPreferenceValues().getPreference(DialogPreferences.ANSWER_KCHAIN_CG_SERVICE_BASE_URI);
		if (kchaincgserviceurl != null) {
			setKchainCgServiceUrl(kchaincgserviceurl);
		}
		String useKchain = context.getPreferenceValues().getPreference(DialogPreferences.USE_ANSWER_KCHAIN_CG_SERVICE);
		if (useKchain != null) {
			setUseKchain(Boolean.parseBoolean(useKchain.trim()));
		}
		String shortgraphlink = context.getPreferenceValues().getPreference(DialogPreferences.SHORT_GRAPH_LINK);
		if (shortgraphlink != null) {
//			Path dir;
//			try {
//				dir = Paths.get(shortgraphlink);
//				File sglFile = new File(shortgraphlink);
//				if (!sglFile.exists()) {
//					File modelFolder = getConfigMgr().getModelFolderPath();
//					Path trgt = Paths.get(modelFolder.getParentFile().getCanonicalPath() + "/Graphs");
//					Path link = Files.createLink(dir, trgt);
//				}
//				else {
//					if (Files.isSymbolicLink(dir)) {
//						Path existingLink = Files.readSymbolicLink(dir);
//						if (existingLink.compareTo(dir) != 0) {
//							System.err.println("Short graph path '" + shortgraphlink + "' already exists as a link with a different target. Please use a project-specific preference.");
//						}
//					}
//					else {
//						System.err.println("Short graph path '" + shortgraphlink + "' already exists and is not a link.");
//					}
//				}
//			} catch (IOException e1) {
//				e1.printStackTrace();
//			}
			File sgl = new File(shortgraphlink.trim());
			if (!sgl.exists()) {
				sgl.mkdirs();
			}
			else if (!sgl.isDirectory()) {
				System.err.println("Short graph path '" + shortgraphlink + "' already exists and is not a folder.");
			}
			setShortGraphLink(shortgraphlink.trim());
		}
//		System.out.println(textserviceurl);
//		System.out.println(cgserviceurl);
	}

	public String getTextServiceUrl() {
		return textServiceUrl;
	}

	private void setTextServiceUrl(String textServiceUrl) {
		this.textServiceUrl = textServiceUrl;
	}

	public String getDbnCgServiceUrl() {
		return dbnCgServiceUrl;
	}

	private void setDbnCgServiceUrl(String cgServiceUrl) {
		this.dbnCgServiceUrl = cgServiceUrl;
	}

	private void setDbnInputJsonGenerationServiceUrl(String dbninputjsongenerationserviceurl) {
		this.dbninputjsongenerationserviceurl = dbninputjsongenerationserviceurl;
	}
	
	public String getDbnInputJsonGenerationServiceUrl() {
		return dbninputjsongenerationserviceurl;
	}

	public String getKchainCgServiceUrl() {
		return kchainCgServiceUrl;
	}

	private void setKchainCgServiceUrl(String cgServiceUrl) {
		this.kchainCgServiceUrl = cgServiceUrl;
	}

	private java.nio.file.Path checkCodeExtractionSadlModelExistence(Resource resource, ProcessorContext context)
			throws IOException, ConfigurationException, URISyntaxException, JenaProcessorException {
		UtilsForJena ufj = new UtilsForJena();
		String policyFileUrl = ufj.getPolicyFilename(resource);
		String policyFilename = policyFileUrl != null ? ufj.fileUrlToFileName(policyFileUrl) : null;
		if (policyFilename != null) {
			File projectFolder = new File(policyFilename).getParentFile().getParentFile();
			String relPath = DialogConstants.EXTRACTED_MODELS_FOLDER_PATH_FRAGMENT + "/" + DialogConstants.CODE_EXTRACTION_MODEL_FILENAME;
			String platformPath = projectFolder.getName() + "/" + relPath;
			String codeExtractionSadlModelFN = projectFolder + "/" + relPath;
			File codeExtractionSadlModelFile = new File(codeExtractionSadlModelFN);
			if (!codeExtractionSadlModelFile.exists()) {
				createCodeExtractionSadlModel(codeExtractionSadlModelFile);
				try {
					Resource newRsrc = resource.getResourceSet()
							.getResource(URI.createPlatformResourceURI(platformPath, false), true);
					if (EMFPlugin.IS_ECLIPSE_RUNNING) {
						Job.create("Refreshing " + newRsrc.getURI().lastSegment(), (ICoreRunnable) monitor -> {
							IPath path = org.eclipse.core.runtime.Path.fromPortableString(newRsrc.getURI().toPlatformString(true));
							IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
							if (file.isAccessible()) {
								file.getProject().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
							} else {
								System.err.println("File " + file + " is not accessible.");
							}
							
						}).schedule();
					}
				} catch (Throwable t) {
				}
			}
			return codeExtractionSadlModelFile.getAbsoluteFile().toPath();
		}
		return null;
	}

	private void createCodeExtractionSadlModel(File cemf) throws IOException {
		String content = getCodeExtractionModel();
		if (!cemf.exists()) {
			cemf.getParentFile().mkdirs();
			new SadlUtils().stringToFile(cemf, content, false);
		}
	}

	private String getCodeExtractionModel() {
		String content = "uri \"http://sadl.org/CodeExtractionModel.sadl\" alias cem.\r\n" + 
				" \r\n" + 
				"// This is the code extraction meta-model\r\n" + 
				"CodeElement is a class described by beginsAt with a single value of type int,\r\n" + 
				"	described by endsAt with a single value of type int.\r\n" + 
				"\r\n" + 
				"CodeBlock is a type of CodeElement,\r\n" + 
				"	described by serialization with a single value of type string,\r\n" + 
				"	described by comment with values of type Comment,\r\n" + 
				"	described by containedIn with values of type CodeBlock.\r\n" + 
				"\r\n" + 
				"{Class, Method, ConditionalBlock, LoopBlock} are types of CodeBlock.\r\n" + 
				"\r\n" + 
				"cmArguments describes Method with a single value of type CodeVariable List.\r\n" + 
				"cmReturnTypes describes Method with a single value of type string List.\r\n" + 
				"cmSemanticReturnTypes describes Method with a single value of type string List.\r\n" + 
				"doesComputation describes Method with a single value of type boolean.\r\n" + 
				"incompleteInformation describes Method with a single value of type boolean.\r\n" + 
				"calls describes Method with values of type MethodCall.\r\n" + 
				"deadCode describes Method with values of type boolean.\r\n" + 
				"isCalled describes Method with values of type boolean.\r\n" + 
				"ExternalMethod is a type of Method.\r\n" + 
				"Constructor is a type of Method.\r\n" + 
				"Rule CisM: if x is a Constructor then x is a Method.\r\n" +
				"\r\n" + 
				"// The reference to a CodeVariable can be its definition (Defined),\r\n" + 
				"//	an assignment or reassignment (Reassigned), or just a reference\r\n" + 
				"//	in the right-hand side of an assignment or a conditional (Used)\r\n" + 
				"Usage is a class, must be one of {Defined, Used, Reassigned}.\r\n" + 
				"\r\n" + 
				"Reference  is a type of CodeElement\r\n" + 
				"	described by firstRef (note \"first reference in this CodeBlock\") \r\n" + 
				"		with a single value of type boolean\r\n" + 
				"	described by codeBlock with a single value of type CodeBlock\r\n" + 
				"	described by usage with values of type Usage\r\n" + 
				" 	described by cem:input (note \"CodeVariable is an input to codeBlock CodeBlock\") \r\n" + 
				" 		with a single value of type boolean\r\n" + 
				" 	described by output (note \"CodeVariable is an output of codeBlock CodeBlock\") \r\n" + 
				" 		with a single value of type boolean\r\n" + 
				" 	described by isImplicit (note \"the input or output of this reference is implicit (inferred), not explicit\")\r\n" + 
				" 		with a single value of type boolean\r\n" + 
				" 	described by setterArgument (note \"is this variable input to a setter?\") with a single value of type boolean\r\n" + 
				" 	described by comment with values of type Comment.\r\n" + 
				" 	\r\n" + 
				"MethodCall is a type of CodeElement\r\n" + 
				"	described by codeBlock with a single value of type CodeBlock\r\n" + 
				"	described by inputMapping with values of type InputMapping,\r\n" + 
				"	described by returnedMapping with values of type OutputMapping.\r\n" + 
				"MethodCallMapping is a class,\r\n" + 
				"	described by callingVariable with a single value of type CodeVariable,\r\n" + 
				"	described by calledVariable with a single value of type CodeVariable.\r\n" + 
				"{InputMapping, OutputMapping} are types of MethodCallMapping.		\r\n" + 
				"	\r\n" + 
				"Comment (note \"CodeBlock and Reference can have a Comment\") is a type of CodeElement\r\n" + 
				" 	described by commentContent with a single value of type string.	\r\n" + 
				"\r\n" + 
				"// what about Constant also? Note something maybe an input and then gets reassigned\r\n" + 
				"// Constant could be defined in terms of being set by equations that only involve Constants\r\n" + 
				"// Constants could also relate variables used in different equations as being same\r\n" + 
				"CodeVariable  is a type of CodeElement, \r\n" + 
				"	described by varName with a single value of type string,\r\n" + 
				"	described by varType with a single value of type string,\r\n" + 
				"	described by semanticVarType with a single value of type string,\r\n" + 
				"	described by quantityKind (note \"this should be qudt:QuantityKind\") with a single value of type ScientificConcept,\r\n" + 
				"	described by reference with values of type Reference.   \r\n" + 
				"\r\n" + 
				"{ClassField, MethodArgument, MethodVariable, ConstantVariable} are types of CodeVariable. 	\r\n" + 
				"\r\n" +
				"constantValue describes ConstantVariable with values of type UnittedQuantity.\r\n" + 
				"//External findFirstLocation (CodeVariable cv) returns int: \"http://ToBeImplemented\".\r\n" + 
				"\r\n" + 
				"Rule Transitive  \r\n" + 
				"if inst is a cls and \r\n" + 
				"   cls is a type of CodeVariable\r\n" + 
				"then inst is a CodeVariable. \r\n" + 
				"\r\n" + 
				"Rule Transitive2  \r\n" + 
				"if inst is a cls and \r\n" + 
				"   cls is a type of CodeBlock\r\n" + 
				"then inst is a CodeBlock. \r\n" + 
				"\r\n" + 
				"Rule FindFirstRef\r\n" + 
				"if c is a CodeVariable and\r\n" + 
				"   ref is reference of c and\r\n" + 
				"   ref has codeBlock cb and\r\n" + 
				"   l is beginsAt of ref and\r\n" + 
				"   minLoc = min(c, reference, r, r, codeBlock, cb, r, beginsAt) and\r\n" + 
				"   l = minLoc\r\n" + 
				"then firstRef of ref is true\r\n" + 
				"//	and print(c, \" at \", minLoc, \" is first reference.\")\r\n" + 
				".\r\n" + 
				"\r\n" + 
				"Rule ImplicitInput\r\n" + 
				"if cb is a CodeBlock and\r\n" + 
				"   ref has codeBlock cb and\r\n" + 
				"   ref has firstRef true and\r\n" + 
				"   ref has usage Used\r\n" + 
				"   and cv has reference ref\r\n" + 
				"//   and ref has beginsAt loc\r\n" + 
				"then input of ref is true and isImplicit of ref is true\r\n" + 
				"//	and print(cb, cv, loc, \" implicit input\")\r\n" + 
				".\r\n" + 
				"\r\n" + 
				"Rule ImplicitOutput\r\n" + 
				"if cb is a CodeBlock and\r\n" + 
				"   ref has codeBlock cb and\r\n" + 
				"   ref has firstRef true and\r\n" + 
				"   ref has usage Reassigned\r\n" + 
				"   and cv has reference ref\r\n" + 
				"   and noValue(cv, reference, ref2, ref2, codeBlock, cb, ref2, usage, Defined)\r\n" + 
				"//   and ref has beginsAt loc\r\n" + 
				"then output of ref is true and isImplicit of ref is true\r\n" + 
				"//	and print(cb, cv, loc, \" implicit output\")\r\n" + 
				"." + 
				"\r\n" + 
				"Rule IsCalled\r\n" + 
				"if	m is a Method and\r\n" + 
				"	mc is a MethodCall and\r\n" + 
				"	mc codeBlock m\r\n" + 
				"then m isCalled true.\r\n" + 
				"\r\n" + 
				"Rule DeadCode\r\n" + 
				"if  m1 is a Method and\r\n" + 
				"	m2 is a Method and\r\n" + 
				"	m1 != m2 and\r\n" + 
				"	m1 calls mc and\r\n" + 
				"	mc codeBlock m2 and \r\n" + 
				"	mc returnedMapping rm and\r\n" + 
				"	rm callingVariable cv and\r\n" + 
				"	noValue(cv, reference, ref, ref, usage, Used)\r\n" + 
				"then deadCode of m2 is true.	\r\n" + 
				"\r\n" +
				"ClassesToIgnore is a type of Class.\r\n" + 
				"{Canvas, CardLayout, Graphics, Insets, Panel, Image, cem:Event, Choice, Button,\r\n" + 
				"	Viewer, GridLayout, Math, Double, Float, String\r\n" + 
				"} are types of ClassesToIgnore.\r\n" + 
				"\r\n" + 
				"Ask ImplicitMethodInputs: \"select distinct ?m ?cv ?vt ?vn where {?r <isImplicit> true . ?r <http://sadl.org/CodeExtractionModel.sadl#input> true . \r\n" + 
				"	?r <codeBlock> ?m . ?cv <reference> ?r . ?cv <varType> ?vt . ?cv <varName> ?vn} order by ?m ?vn\".\r\n" + 
				"Ask ImplicitMethodOutputs: \"select distinct ?m ?cv ?vt ?vn where {?r <isImplicit> true . ?r <http://sadl.org/CodeExtractionModel.sadl#output> true . \r\n" + 
				"	?r <codeBlock> ?m . ?cv <reference> ?r . ?cv <varType> ?vt. ?cv <varName> ?vn} order by ?m ?vn\".\r\n" + 
				"Ask MethodsDoingComputation: \"select ?m where {?m <doesComputation> true}\".\r\n" + 
				"Ask MethodCalls: \"select ?m ?mcalled where {?m <calls> ?mc . ?mc <codeBlock> ?mcalled} order by ?m ?mcalled\"." + 
				"Ask VarComment: \"select ?cmntcontent ?eln ?usage where { ? <http://sadl.org/CodeExtractionModel.sadl#reference> ?ref . \r\n" + 
				"	?ref <http://sadl.org/CodeExtractionModel.sadl#endsAt> ?eln . \r\n" + 
				"	?ref <http://sadl.org/CodeExtractionModel.sadl#usage> ?usage .\r\n" + 
				"	?cmnt <rdf:type> <http://sadl.org/CodeExtractionModel.sadl#Comment> .\r\n" + 
				"	?cmnt <http://sadl.org/CodeExtractionModel.sadl#endsAt> ?eln . \r\n" + 
				"	?cmnt <http://sadl.org/CodeExtractionModel.sadl#commentContent> ?cmntcontent} order by ?eln\".\r\n"
				;
		return content;
	}

	private boolean isUseDbn() {
		return useDbn;
	}

	private void setUseDbn(boolean useDbn) {
		this.useDbn = useDbn;
	}
	
	private boolean isUseKchain() {
		return useKchain;
	}

	private void setUseKchain(boolean useKchain) {
		this.useKchain = useKchain;
	}

	private void setSavePythonTF(boolean savePythonTF) {
		this.savePythonTF = savePythonTF;
	}

	private boolean isSavePhthonTF() {
		return savePhthonTF;
	}

	private void setSavePython(boolean savePython) {
		this.savePython = savePython;
	}

	private boolean isSavePhthon() {
		return savePhthon;
	}

	private void setSaveOriginal(boolean saveOriginal) {
		this.saveOriginal = saveOriginal;
		
	}

	private boolean isSaveOriginal() {
		return saveOriginal;
	}

	private String getShortGraphLink() {
		return shortGraphLink;
	}

	private void setShortGraphLink(String shortGraphLink) {
		this.shortGraphLink = shortGraphLink;
	}

	private String getInvizinserviceurl() {
		return invizinserviceurl;
	}

	private void setInvizinserviceurl(String invizinserviceurl) {
		this.invizinserviceurl = invizinserviceurl;
	}

	private StatementContent getLastStatementContent() {
		return lastStatementContent;
	}

	private void setLastStatementContent(StatementContent lastStatementContent) {
		this.lastStatementContent = lastStatementContent;
	}

	@Override
	protected void redeclarationHandler(SadlResource sr, SadlResource decl) {
		//	if contained in a CM statement then redeclaration is OK
		if (EcoreUtil2.getContainerOfType(sr, AnswerCMStatement.class) != null) {
			return;
		}

		try {
			if (getDeclarationExtensions().getOntConceptType(decl).equals(OntConceptType.STRUCTURE_NAME)) {
				addError("This is already a Named Structure", sr);
			}
			if (!getDeclarationExtensions().getConceptNamespace(sr).equals(getModelNamespace())) {
				addError("Declaration of concepts in another namespace not supported", sr);
			}
		} catch (CircularDefinitionException e) {
			e.printStackTrace();
		}
	}
}

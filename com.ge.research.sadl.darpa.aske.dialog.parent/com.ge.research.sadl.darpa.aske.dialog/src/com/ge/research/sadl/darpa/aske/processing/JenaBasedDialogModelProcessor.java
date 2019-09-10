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
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

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
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.preferences.IPreferenceValuesProvider;
import org.eclipse.xtext.resource.XtextSyntaxDiagnostic;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.CheckType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.darpa.aske.dialog.AnswerCMStatement;
import com.ge.research.sadl.darpa.aske.dialog.HowManyValuesStatement;
import com.ge.research.sadl.darpa.aske.dialog.ModifiedAskStatement;
import com.ge.research.sadl.darpa.aske.dialog.MyNameIsStatement;
import com.ge.research.sadl.darpa.aske.dialog.SadlEquationInvocation;
import com.ge.research.sadl.darpa.aske.dialog.SaveStatement;
import com.ge.research.sadl.darpa.aske.dialog.TargetModelName;
import com.ge.research.sadl.darpa.aske.dialog.WhatIsStatement;
import com.ge.research.sadl.darpa.aske.dialog.WhatStatement;
import com.ge.research.sadl.darpa.aske.dialog.WhatValuesStatement;
import com.ge.research.sadl.darpa.aske.dialog.YesNoAnswerStatement;
import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.errorgenerator.generator.SadlErrorMessages;
import com.ge.research.sadl.jena.JenaBasedSadlModelProcessor;
import com.ge.research.sadl.jena.JenaProcessorException;
import com.ge.research.sadl.jena.MetricsProcessor;
import com.ge.research.sadl.jena.UtilsForJena;
import com.ge.research.sadl.model.CircularDefinitionException;
import com.ge.research.sadl.model.ModelError;
import com.ge.research.sadl.model.gp.Equation;
import com.ge.research.sadl.model.gp.GraphPatternElement;
import com.ge.research.sadl.model.gp.Junction;
import com.ge.research.sadl.model.gp.NamedNode;
import com.ge.research.sadl.model.gp.Node;
import com.ge.research.sadl.model.gp.ProxyNode;
import com.ge.research.sadl.model.gp.Query;
import com.ge.research.sadl.model.gp.Rule;
import com.ge.research.sadl.model.gp.TripleElement;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.processing.SadlConstants;
import com.ge.research.sadl.processing.SadlInferenceException;
import com.ge.research.sadl.processing.ValidationAcceptor;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.InvalidTypeException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.TranslationException;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.ge.research.sadl.sADL.Declaration;
import com.ge.research.sadl.sADL.Expression;
import com.ge.research.sadl.sADL.NamedStructureAnnotation;
import com.ge.research.sadl.sADL.QueryStatement;
import com.ge.research.sadl.sADL.SadlAnnotation;
import com.ge.research.sadl.sADL.SadlInstance;
import com.ge.research.sadl.sADL.SadlModel;
import com.ge.research.sadl.sADL.SadlModelElement;
import com.ge.research.sadl.sADL.SadlResource;
import com.ge.research.sadl.sADL.SadlSimpleTypeReference;
import com.ge.research.sadl.sADL.SadlStatement;
import com.ge.research.sadl.sADL.SadlTypeReference;
import com.ge.research.sadl.utils.ResourceManager;
import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.RDFWriter;

public class JenaBasedDialogModelProcessor extends JenaBasedSadlModelProcessor {
	private static final Logger logger = LoggerFactory.getLogger(JenaBasedDialogModelProcessor.class);
	private boolean modelChanged;
	
	private String textServiceUrl = null;
	private String cgServiceUrl = null;
	private AnswerCurationManager answerCurationManager = null;

	@Inject IPreferenceValuesProvider preferenceProvider;

	@Override
	public void onValidate(Resource resource, ValidationAcceptor issueAcceptor, CheckMode mode, ProcessorContext context) {
		if (!isSupported(resource)) {
			return;
		}
		resetProcessor();
		logger.debug("JenaBasedDialogModelProcessor.onValidate called for Resource '" + resource.getURI() + "'"); 
		CancelIndicator cancelIndicator = context.getCancelIndicator();
		if (resource.getContents().size() < 1) {
			return;
		}
	
		String textserviceurl = context.getPreferenceValues().getPreference(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI);
		String cgserviceurl = context.getPreferenceValues().getPreference(DialogPreferences.ANSWER_CG_SERVICE_BASE_URI);
//		System.out.println(textserviceurl);
//		System.out.println(cgserviceurl);

		logger.debug("onValidate called for Resource '" + resource.getURI() + "'");
		if (mode.shouldCheck(CheckType.EXPENSIVE)) {
			// do expensive validation, i.e. those that should only be done when 'validate'
			// action was invoked.
		}
		setIssueAcceptor(issueAcceptor);
		setProcessorContext(context);
		setCancelIndicator(cancelIndicator);
		setCurrentResource(resource);
		SadlModel model = (SadlModel) resource.getContents().get(0);
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
			theJenaModel = prepareEmptyOntModel(resource);
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
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			checkCodeExtractionSadlModelExistence(resource, context);
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
			return;
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
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		initializePreferences(context);

		// Check for a syntactically valid AST; if it isn't then don't process so that conversations will only be valid ones
	    boolean validAST = isAstSyntaxValid(model);	
	    if (!validAST) {
	    	return;
	    }

		// create validator for expressions
		initializeModelValidator();
		initializeAllImpliedPropertyClasses();
		initializeAllExpandedPropertyClasses();
		try {
			initializeDialogContent();
		} catch (ConversationException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		// process rest of parse tree
		List<SadlModelElement> elements = model.getElements();
		if (elements != null) {
			try {
				if (!getAnswerCurationManager().dialogAnserProviderInitialized()) {
					System.out.println("DialogAnswerProvider not yet initialized.");
					return;
				}
			} catch (IOException e2) {
				// TODO Auto-generated catch block
				e2.printStackTrace();
			}

			Iterator<SadlModelElement> elitr = elements.iterator();
			Object lastElement = null;
			AnswerCMStatement lastACMQuestion = null;
//			if (!elitr.hasNext() && !model.getImports().isEmpty()) {
//				// 
//				MixedInitiativeTextualResponse mir = new MixedInitiativeTextualResponse("What is your name?", false);
//				int endOffset = NodeModelUtils.findActualNodeFor(model).getEndOffset();
//				mir.setInsertionPoint(endOffset);
//				OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND, mir);
//				
//			}
			while (elitr.hasNext()) {
				// check for cancelation from time to time
				if (cancelIndicator.isCanceled()) {
					throw new OperationCanceledException();
				}
				SadlModelElement element = elitr.next();
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
					// TODO Auto-generated catch block
					logger.error("Error:", e);
				}
				try {
					if (!(element instanceof AnswerCMStatement)) {
						// this is user input
						lastElement = processDialogModelElement(resource, element);
					lastACMQuestion = null;
					}
					else if (eos != null && eos.equals("?")) {
						// this is a question from the backend
						lastACMQuestion = (AnswerCMStatement) element;
					}
					else {
						// this is a response from CM
						//	clear last command
						OntModelProvider.clearPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND);
						lastElement = null;
						processAnswerCMStatement(resource, (AnswerCMStatement)element);
					}
				} catch (IOException | TranslationException | InvalidNameException | InvalidTypeException | ConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (lastElement != null) {
				// this is the one to which the CM should respond; keep it for the DialogAnswerProvider
//				processUserInputElement(lastElement);
			}
			
			logger.debug("At end of model processing, conversation is:");
			try {
				logger.debug(getAnswerCurationManager().getConversation().toString());
			} catch (IOException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			
			File saveFile = getModelFile(resource);
			try {
				if (isModelChanged() || !saveFile.exists() || 
						getConfigMgr().getAltUrlFromPublicUri(getModelName()) == null ||
						getConfigMgr().getAltUrlFromPublicUri(getModelName()) == getModelName()) {
					autoSaveModel(resource, getConfigMgr().getModelFolder(), saveFile, context);
					// refresh resource ?
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (URISyntaxException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			if (getSadlCommands() != null && getSadlCommands().size() > 0) {
				OntModelProvider.attach(model.eResource(), getTheJenaModel(), getModelName(), getModelAlias(),
						getSadlCommands());
			} else {
				OntModelProvider.attach(model.eResource(), getTheJenaModel(), getModelName(), getModelAlias());
			}
		}
	}

	private String getEos(SadlModelElement element) {
		if (element instanceof EObject) {
			String elementText = NodeModelUtils.findActualNodeFor((EObject) element).getText().trim();
			if (elementText.endsWith(".")) {
				return ".";
			}
			else if (elementText.endsWith("?")) {	// I don't think this is needed anymore awc 9/4/19
				return "?";
			}
		}
		return null;
	}

	private Object processDialogModelElement(Resource resource, EObject stmt) throws IOException, TranslationException, InvalidNameException, InvalidTypeException, ConfigurationException {
		ConversationElement ce = null;
		Object toBeReturned = null;
		if (stmt instanceof MyNameIsStatement) {
			System.out.println("User name is " + ((MyNameIsStatement)stmt).getAnswer());
		}
		else if (stmt instanceof ModifiedAskStatement ||
				stmt instanceof WhatStatement ||
				stmt instanceof HowManyValuesStatement ||
				stmt instanceof SaveStatement) {
			ce = processUserInputElement(stmt);
		}
		else {
			boolean treatAsAnswerToBackend = false;
			AnswerCMStatement lastACMQuestion = getAnswerCurationManager().getLastACMQuestion();
			if (lastACMQuestion  != null) {
				// this could be the answer to a preceding question
				if (stmt instanceof SadlStatement || stmt instanceof YesNoAnswerStatement) {
					try {
						IDialogAnswerProvider dap = getDialogAnswerProvider(resource);
						String question = lastACMQuestion.getStr();
						if (question != null) {
							MixedInitiativeElement mie = dap.getMixedInitiativeElement(question);
//										dap.removeMixedInitiativeElement(question);
							if (mie != null) {
					            // construct response
								String answer = getResponseFromSadlStatement(stmt);
								mie.addArgument(answer);
								dap.provideResponse(mie);
//								            MixedInitiativeElement response = new MixedInitiativeElement(answer, null);
//								            response.setContent(new MixedInitiativeTextualResponse(answer));
//								            // make call identified in element
//								            mie.getRespondTo().accept(response);
								dap.removeMixedInitiativeElement(question);	// question has been answered
								treatAsAnswerToBackend = true;
								ce = new ConversationElement(getAnswerCurationManager().getConversation(), mie, Agent.USER);
								toBeReturned = mie;
							}
							else {
								treatAsAnswerToBackend = true;
								String answer = getResponseFromSadlStatement(stmt);
								ce = new ConversationElement(getAnswerCurationManager().getConversation(), answer, Agent.USER);
								toBeReturned = answer;
							}
						}
					} catch (ConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
			if (!treatAsAnswerToBackend && !(stmt instanceof YesNoAnswerStatement)) {
				if (stmt instanceof TargetModelName) {
					processModelElement((TargetModelName)stmt);
				}
				else if (stmt instanceof SadlEquationInvocation) {
					processModelElement((SadlEquationInvocation)stmt);
				}
				// This is some kind of SADL statement to add to the model
				else if (stmt instanceof SadlModelElement) {
					processModelElement((SadlModelElement) stmt);
					ce = new ConversationElement(getAnswerCurationManager().getConversation(), stmt, Agent.USER);
					toBeReturned = stmt;
				}
				else {
					try {
						throw new JenaProcessorException("statement wasn't of expected type");
					} catch (JenaProcessorException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				setModelChanged(true);
			}
		}
		if (ce != null) {
    		if (stmt instanceof EObject) {
    			ICompositeNode node = NodeModelUtils.findActualNodeFor((EObject) stmt);
     			ce.setText(node.getText());
    			ce.setStartingLocation(node.getTotalOffset());
    			ce.setLength(node.getTotalLength());
    		}
			getAnswerCurationManager().addToConversation(ce);
		}
		return toBeReturned;
	}

//	@Inject
//	private SADLGrammarAccess grammarAccess;
	
	private boolean processModelElement(TargetModelName element) throws ConfigurationException, IOException {
		boolean returnVal = true;
		String uri = element.getUri();
		String prefix = element.getPrefix();
		String altUrl = getConfigMgr().getAltUrlFromPublicUri(uri);
		if (altUrl == null || altUrl.equals(uri)) {
			addError("Model not found", element);
			returnVal = false;
		}
		else if (prefix == null) {
			String gprefix = getConfigMgr().getGlobalPrefix(uri);
			if (gprefix == null) {
				addError("No global prefix found for model so a local alias is required", element);
				returnVal = false;
			}
		}
		if (returnVal) {
			String[] uris = new String[2];
			uris[0] = uri;
			uris[1] = altUrl;
			getAnswerCurationManager().addTargetModelToMap(prefix, uris);
		}
		return returnVal;
	}
	
	private boolean processModelElement(SadlEquationInvocation element) throws TranslationException, InvalidNameException, InvalidTypeException {
		SadlResource name = element.getName();
		Node srobj = processExpression(name);
		EList<Expression> params = element.getParameter();
		for (Expression param : params) {
			Object paramObj = processExpression(param);
			System.out.println(paramObj.toString());
		}
		EList<String> units = element.getUnits();
		for (String unit : units) {
			System.out.println(unit);
		}
		return false;
	}
//
	private boolean statementIsComplete(SadlModelElement element) {
	    Iterable<XtextSyntaxDiagnostic> syntaxErrors = Iterables.<XtextSyntaxDiagnostic>filter(element.eResource().getErrors(), XtextSyntaxDiagnostic.class);
		if (syntaxErrors.iterator().hasNext()) {
			ICompositeNode node = NodeModelUtils.findActualNodeFor(element);
			if (node.getSyntaxErrorMessage() == null) {
				return true;
			}
			return false;
		}
//	    final ICompositeNode node = NodeModelUtils.findActualNodeFor(element);
//        if ((node != null)) {
//          final INode lastChild = node.getLastChild();
//          if ((lastChild != null)) {
//            final EObject grammarElement = lastChild.getGrammarElement();
//            if ((grammarElement instanceof RuleCall)) {
//              AbstractRule _rule = ((RuleCall)grammarElement).getRule();
//              final ParserRule EOS = this.grammarAccess.getEOSRule();
//              boolean _tripleEquals = (_rule == EOS);  //_rule.getName().equals(EOS.getName());
//              String _text = node.getText();
//              if (_tripleEquals) {
//                String _plus = ("SADL statement is complete: " + _text.trim());
//                InputOutput.<String>println(_plus);
//                return true;
//              }
//              else {
//                  String _plus = ("SADL statement is NOT complete: " + _text.trim());
//                  InputOutput.<String>println(_plus);
//              }
//            }
//          }
//        }
		return true;
	}

	private void initializeDialogContent() throws ConversationException, IOException {
		Resource resource = getCurrentResource();
		AnswerCurationManager cm = getAnswerCurationManager();
		DialogContent dc = new DialogContent(resource, cm);
		cm.setConversation(dc);
	}

	private AnswerCurationManager getAnswerCurationManager() throws IOException {
		if (answerCurationManager == null) {
			Object cm = getConfigMgr().getPrivateKeyValuePair(DialogConstants.ANSWER_CURATION_MANAGER);
			if (cm != null) {
				if (cm instanceof AnswerCurationManager) {
					answerCurationManager  = (AnswerCurationManager) cm;
				}
			}
			else {
				// need to get a new one
// TODO get preferences				
				answerCurationManager = new AnswerCurationManager(getConfigMgr().getModelFolder(), getConfigMgr(), null);
				getConfigMgr().addPrivateKeyValuePair(DialogConstants.ANSWER_CURATION_MANAGER, answerCurationManager);
			}
		}
		return answerCurationManager;
	}
	
	private String getResponseFromSadlStatement(EObject stmt) {
		// TODO Auto-generated method stub
		String ans = "no";
		if (stmt instanceof SadlInstance) {
			SadlResource sr = ((SadlInstance)stmt).getInstance();
			ans = NodeModelUtils.getTokenText(NodeModelUtils.getNode(stmt));
			int i = 0;
		}
		else if (stmt instanceof YesNoAnswerStatement) {
			ans = ((YesNoAnswerStatement)stmt).getAnswer();
		}
		if (ans.substring(0, 1).equalsIgnoreCase("y")) {
			return "yes";
		}
		else {
			return "no";
		}
	}

	private IDialogAnswerProvider getDialogAnswerProvider(Resource resource) throws ConfigurationException {
		Object dap = getConfigMgr(resource, null).getPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER);
		if (dap instanceof IDialogAnswerProvider) {
			return (IDialogAnswerProvider)dap;
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
			modelErrorsToOutput(resource, results);
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
	}

	private void resetProcessor() {
		// TODO Auto-generated method stub
		
	}
	
	private ConversationElement processUserInputElement(EObject element) {
		ConversationElement ce = null;
		try {
			if (element instanceof ModifiedAskStatement) {
				ce = processStatement((ModifiedAskStatement)element);
			}
			else if (element instanceof WhatStatement) {
				ce = processStatement((WhatStatement)element);
			}
			else if (element instanceof HowManyValuesStatement) {
				ce = processStatement((HowManyValuesStatement)element);
			}
			else if (element instanceof SaveStatement) {
				ce = processStatement((SaveStatement)element);
			}
			else {
				throw new JenaProcessorException("onValidate for element of type '"
						+ element.getClass().getCanonicalName() + "' not implemented");
			}
		} catch (JenaProcessorException e) {
			addError(e.getMessage(), element);
		} catch (Exception e) {
			e.printStackTrace();
		}
		return ce;
	}	
	
	private void  processAnswerCMStatement(Resource resource, AnswerCMStatement element) throws IOException, TranslationException, InvalidNameException, InvalidTypeException, ConfigurationException {
		EObject stmt = element.getSstmt();
		Object retval = processDialogModelElement(resource, stmt);
		SadlModelConstruct smc = new SadlModelConstruct(retval);
		getAnswerCurationManager().addToConversation(new ConversationElement(getAnswerCurationManager().getConversation(), smc, Agent.CM));
	}

	private ConversationElement processStatement(SaveStatement element) throws IOException, ConfigurationException, QueryParseException, QueryCancelledException, ReasonerNotFoundException {
		ConversationElement ce = null;
		SadlResource equationSR = ((SaveStatement)element).getTarget();
		String targetModelUri = null;
		String targetModelUrl = null;
		String targetModelAlias = ((SaveStatement)element).getSaveTarget();
		Map<String, String[]> targetModelMap = getAnswerCurationManager().getTargetModelMap();
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
			else if (targetModelMap.size() < 1) {
				addError("No target models have been identified. Cannot identify a model into which to save.", element);
			}
			else {
				String[] uris = targetModelMap.get(targetModelMap.keySet().iterator().next());
				targetModelUri = uris[0];
				targetModelUrl = uris[1];
			}
		}
		if (targetModelUri != null) {
			String equationUri = getDeclarationExtensions().getConceptUri(equationSR);
			Individual extractedModelInstance = getTheJenaModel().getIndividual(equationUri);
			if (extractedModelInstance == null) {
				getAnswerCurationManager().notifyUser(getConfigMgr().getModelFolder(), "No equation with URI '" + equationUri + "' is found in current model.", true);
			}
			else if (extractedModelInstance.getNameSpace().equals(targetModelAlias)) {
				getAnswerCurationManager().notifyUser(getConfigMgr().getModelFolder(), "The equation with URI '" + equationUri + "' is already in the target model '" + targetModelAlias + "'", true);
			}
			System.out.println("Ready to build model '" + equationUri + "'");
			String result = getAnswerCurationManager().processSaveRequest(equationUri, getTheJenaModel());
			BuildConstruct bc = new BuildConstruct(getTheJenaModel(), targetModelUri, targetModelUrl, extractedModelInstance);
			bc.setContext(element);
			ce = new ConversationElement(getAnswerCurationManager().getConversation(), bc, Agent.USER);
			OntModelProvider.addPrivateKeyValuePair(element.eResource(), DialogConstants.LAST_DIALOG_COMMAND, bc);
		}
		return ce;
	}

	private ConversationElement processStatement(ModifiedAskStatement stmt) {
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
			ce = new ConversationElement(getAnswerCurationManager().getConversation(), query, Agent.USER);
			OntModelProvider.addPrivateKeyValuePair(stmt.eResource(), DialogConstants.LAST_DIALOG_COMMAND, query);
		} catch (CircularDefinitionException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidNameException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (InvalidTypeException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (JenaProcessorException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ce;
	}

	private ConversationElement processStatement(WhatStatement stmt) {
		ConversationElement ce = null;
		if (stmt.getStmt() instanceof WhatIsStatement) {
			EObject whatIsTarget = ((WhatIsStatement)stmt.getStmt()).getTarget();
			if (whatIsTarget == null) {
				// this is a request for user name
				
			}
			if (whatIsTarget instanceof Declaration) {
				whatIsTarget = ((Declaration)whatIsTarget).getType();
				if (whatIsTarget instanceof SadlSimpleTypeReference) {
					whatIsTarget = ((SadlSimpleTypeReference)whatIsTarget).getType();
				}
			}
			Object trgtObj;
			try {
				trgtObj = processExpression(whatIsTarget);
//				System.out.println("WhatIsStatement target: " + trgtObj.toString());
				if (trgtObj instanceof NamedNode) {
					((NamedNode)trgtObj).setContext(stmt);
				}
				else if (trgtObj instanceof Junction) {
					setGraphPatternContext(stmt, whatIsTarget, trgtObj);
				}
				else if (trgtObj instanceof TripleElement) {
					((TripleElement)trgtObj).setContext(stmt.getStmt());
				}
				else if (trgtObj instanceof Object[]) {
					for (int i = 0; i < ((Object[])trgtObj).length; i++) {
						Object obj = ((Object[])trgtObj)[i];
						setGraphPatternContext(stmt, whatIsTarget, obj);
					}
				}
				else {
					// TODO
					addInfo(trgtObj.getClass().getCanonicalName() + " not yet handled by dialog processor", whatIsTarget);
				}
				Expression when = ((WhatIsStatement)stmt.getStmt()).getWhen();
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
					gpes.add((GraphPatternElement) whenObj);
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
				
				WhatIsConstruct wic = new WhatIsConstruct(trgtObj, whenObj);
				ce = new ConversationElement(getAnswerCurationManager().getConversation(), wic, Agent.USER);
//				OntModelProvider.addPrivateKeyValuePair(stmt.eResource(), DialogConstants.LAST_DIALOG_COMMAND, wic);
				getAnswerCurationManager().processUserRequest(getCurrentResource(), getTheJenaModel(), wic);
			} catch (TranslationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidNameException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (InvalidTypeException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ConfigurationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (ExecutionException e) {
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
		else if (stmt.getStmt() instanceof WhatValuesStatement) {
			WhatValuesStatement wvstmt = (WhatValuesStatement)stmt.getStmt();
			String article = wvstmt.getArticle();
			SadlTypeReference cls = wvstmt.getCls();
			SadlResource prop = wvstmt.getProp();
			String typ = wvstmt.getTyp(); 		// "can" or "must"
			try {
				Object clsObj = processExpression(cls);
				Object propObj = processExpression(prop);
//				System.out.println("WhatValuesStatement(" + typ + "): cls=" + (article!= null ? article : "") + 
//						" '" + clsObj.toString() + "'; prop='" + propObj.toString() + "'");
				Object[] temp = new Object[3];
				temp[0] = article;
				temp[1] = clsObj;
				temp[2] = propObj;
				OntModelProvider.addPrivateKeyValuePair(stmt.eResource(), DialogConstants.LAST_DIALOG_COMMAND, temp);
			} catch (TranslationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return ce;
	}

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

	private ConversationElement processStatement(HowManyValuesStatement stmt) throws IOException {
		ConversationElement ce = null;
		String article = stmt.getArticle();
		SadlTypeReference cls = stmt.getCls();
		SadlResource prop = stmt.getProp();
		SadlTypeReference typ = stmt.getTyp(); 
		try {
			Object clsObj = processExpression(cls);
			Object propObj = processExpression(prop);
			Object typObj = null;
			if (typ != null) {
				typObj = processExpression(typ);
			}
//			System.out.println("HowManyValuesStatement: cls=" + (article!= null ? article : "") + " '" + 
//					clsObj.toString() + "'; prop='" + propObj.toString() + 
//					"'" + (typObj != null ? ("; type='" + typObj.toString() + "'") : ""));
			Object[] temp = new Object[4];
			temp[0] = article;
			temp[1] = clsObj;
			temp[2] = propObj;
			temp[3] = typObj;
			HowManyValuesConstruct hmvc = new HowManyValuesConstruct(temp);
			ce = new ConversationElement(getAnswerCurationManager().getConversation(), hmvc, Agent.USER);
			OntModelProvider.addPrivateKeyValuePair(stmt.eResource(), DialogConstants.LAST_DIALOG_COMMAND, hmvc);
		} catch (TranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return ce;
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
		setTypeCheckingWarningsOnly(true);
		setUseArticlesInValidation(true);
		setIncludeImpliedPropertiesInTranslation(true);
		String textServiceUrl = context.getPreferenceValues().getPreference(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI);
		if (textServiceUrl != null) {
			setTextServiceUrl(textServiceUrl);
		}
		String cgServiceUrl = context.getPreferenceValues().getPreference(DialogPreferences.ANSWER_CG_SERVICE_BASE_URI);
		if (cgServiceUrl != null) {
			setCgServiceUrl(cgServiceUrl);
		}
	}

	public String getTextServiceUrl() {
		return textServiceUrl;
	}

	private void setTextServiceUrl(String textServiceUrl) {
		this.textServiceUrl = textServiceUrl;
	}

	public String getCgServiceUrl() {
		return cgServiceUrl;
	}

	private void setCgServiceUrl(String cgServiceUrl) {
		this.cgServiceUrl = cgServiceUrl;
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
				"calls describes Method with values of type MethodCall.\r\n" +
				"ExternalMethod is a type of Method.\r\n" +
				"\r\n" + 
				"// The reference to a CodeVariable can be its definition (Defined),\r\n" + 
				"//	an assignment or reassignment (Reassigned), or just a reference\r\n" + 
				"//	in the right-hand side of an assignment or a conditional (Used)\r\n" + 
				"Usage is a class, must be one of {Defined, Used, Reassigned}.\r\n" + 
				"\r\n" + 
				"Reference  is a type of CodeElement\r\n" + 
				"	described by firstRef with a single value of type boolean\r\n" + 
				"	described by codeBlock with a single value of type CodeBlock\r\n" + 
				"	described by usage with values of type Usage\r\n" + 
				" 	described by input (note \"CodeVariable is an input to codeBlock CodeBlock\") \r\n" + 
				" 		with a single value of type boolean\r\n" + 
				" 	described by output (note \"CodeVariable is an output of codeBlock CodeBlock\") \r\n" + 
				" 		with a single value of type boolean\r\n" + 
				" 	described by isImplicit (note \"the input or output of this reference is implicit (inferred), not explicit\")\r\n" + 
				" 		with a single value of type boolean\r\n" + 
				" 	described by setterArgument (note \"is this variable input to a setter?\") with a single value of type boolean\r\n" + 
				" 	described by comment with values of type Comment.\r\n" + 
				"	\r\n" + 
				"MethodCall is a type of CodeElement\r\n" + 
				"	described by codeBlock with a single value of type CodeBlock\r\n" + 
				"	described by inputMapping with values of type InputMapping,\r\n" + 
				"	described by returnedMapping with values of type OutputMapping.\r\n" + 
				"MethodCallMapping is a class,\r\n" + 
				"	described by callingVariable with a single value of type CodeVariable,\r\n" + 
				"	described by calledVariable with a single value of type CodeVariable.\r\n" + 
				"{InputMapping, OutputMapping} are types of MethodCallMapping.\r\n" + 
				"\r\n" +
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
				"{ClassField, MethodArgument, MethodVariable} are types of CodeVariable. 	\r\n" + 
				"\r\n" + 
				"//External findFirstLocation (CodeVariable cv) returns int: \"http://ToBeImplemented\".\r\n" + 
				"\r\n" + 
				"Rule Transitive  \r\n" + 
				"if inst is a cls and \r\n" + 
				"   cls is a type of CodeVariable\r\n" + 
				"then inst is a CodeVariable. \r\n" + 
				"\r\n" + 
				"Rule SetNotFirstRef1\r\n" + 
				"if c is a CodeVariable and\r\n" + 
				"   ref is reference of c and\r\n" + 
				"   oneOf(usage of ref, Used, Reassigned) and  \r\n" + 
				"   ref2 is reference of c and\r\n" + 
				"   ref != ref2 and\r\n" + 
				"   cb is codeBlock of ref and   \r\n" + 
				"   cb2 is codeBlock of ref2 and\r\n" + 
				"   cb = cb2 and\r\n" + 
				"   l1 is beginsAt of ref and\r\n" + 
				"   l2 is beginsAt of ref2 and\r\n" + 
				"   l2 > l1   // so ref2 is at an earlier location that ref\r\n" + 
				"then firstRef of ref2 is false.   \r\n" + 
				"\r\n" + 
				"// first reference is of type \"Used\" or all earlier refs are of type \"Used\"	\r\n" + 
				"// this does not cover when no ref2 with l2 < l1 exists\r\n" + 
				"Rule SetAsInput1\r\n" + 
				"if c is CodeVariable and\r\n" + 
				"   ref is reference of c and\r\n" + 
				"   input of ref is not known and \r\n" + 
				"   usage of ref is Used and\r\n" + 
				"   ref2 is reference of c and\r\n" + 
				"   ref != ref2 and\r\n" + 
				"   cb is codeBlock of ref and   \r\n" + 
				"   cb2 is codeBlock of ref2 and\r\n" + 
				"   cb = cb2 and   \r\n" + 
				"   l1 is beginsAt of ref and\r\n" + 
				"   l2 is beginsAt of ref2 and\r\n" + 
				"   l2 < l1 and  // so ref2 is at an earlier location that ref\r\n" + 
				"   noValue(ref2, usage, Reassigned) // no earlier reassignment of c exists\r\n" + 
				"then input of ref is true and isImplicit of ref is true. \r\n" + 
				"\r\n" + 
				"// if there is no l2 as specified in the previous rules, then the following covers that case\r\n" + 
				"// do I need to consider codeBlock?????\r\n" + 
				"Rule SetAsInput2\r\n" + 
				"if c is a CodeVariable and\r\n" + 
				"   ref is reference of c and\r\n" + 
				"   input of ref is not known and\r\n" + 
				"   usage of ref is Used and \r\n" + 
				"   noValue(ref, firstRef)\r\n" + 
				"then input of ref is true and isImplicit of ref is true. \r\n" + 
				"\r\n" + 
				"// \"it is an output if it is computed and is argument to a setter\"\r\n" + 
				"// or I could try to use the notion of a constant\r\n" + 
				"Rule SetAsOutput\r\n" + 
				"if c is a CodeVariable and\r\n" + 
				"   setterArgument of c is true and\r\n" + 
				"   ref is a reference of c and\r\n" + 
				"   output of ref is not known and\r\n" + 
				"   usage of ref is Defined //check this?\r\n" + 
				"then\r\n" + 
				"	output of ref is true and isImplicit of ref is true.      	 \r\n";
		return content;
	}

}

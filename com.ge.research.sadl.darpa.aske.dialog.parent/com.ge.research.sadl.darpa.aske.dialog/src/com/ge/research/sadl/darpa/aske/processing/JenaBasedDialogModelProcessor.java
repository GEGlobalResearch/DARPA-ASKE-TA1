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

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.resource.ResourceSet;
import org.eclipse.xtext.EcoreUtil2;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.CheckType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.dialog.AnswerCMStatement;
import com.ge.research.sadl.darpa.aske.dialog.BuildStatement;
import com.ge.research.sadl.darpa.aske.dialog.HowManyValuesStatement;
import com.ge.research.sadl.darpa.aske.dialog.ModifiedAskStatement;
import com.ge.research.sadl.darpa.aske.dialog.WhatIsStatement;
import com.ge.research.sadl.darpa.aske.dialog.WhatStatement;
import com.ge.research.sadl.darpa.aske.dialog.WhatValuesStatement;
import com.ge.research.sadl.darpa.aske.dialog.YesNoAnswerStatement;
import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.errorgenerator.generator.SadlErrorMessages;
import com.ge.research.sadl.external.ExternalEmfResource;
import com.ge.research.sadl.jena.JenaBasedSadlModelProcessor;
import com.ge.research.sadl.jena.JenaProcessorException;
import com.ge.research.sadl.jena.MetricsProcessor;
import com.ge.research.sadl.jena.UtilsForJena;
import com.ge.research.sadl.model.CircularDefinitionException;
import com.ge.research.sadl.model.ModelError;
import com.ge.research.sadl.model.gp.Equation;
import com.ge.research.sadl.model.gp.Junction;
import com.ge.research.sadl.model.gp.NamedNode;
import com.ge.research.sadl.model.gp.ProxyNode;
import com.ge.research.sadl.model.gp.Query;
import com.ge.research.sadl.model.gp.Rule;
import com.ge.research.sadl.model.gp.TripleElement;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.processing.SadlConstants;
import com.ge.research.sadl.processing.ValidationAcceptor;
import com.ge.research.sadl.processing.IModelProcessor.ProcessorContext;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.InvalidTypeException;
import com.ge.research.sadl.reasoner.SadlJenaModelGetter;
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
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.RDFWriter;

public class JenaBasedDialogModelProcessor extends JenaBasedSadlModelProcessor {
	private static final Logger logger = LoggerFactory.getLogger(JenaBasedDialogModelProcessor.class);
	private boolean modelChanged;
	
	private String textServiceUrl = null;
	private String cgServiceUrl = null;

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
			MixedInitiativeTextualResponse mir = new MixedInitiativeTextualResponse("Please add at least one import of a domain namespace");
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

		// create validator for expressions
		initializeModelValidator();
		initializeAllImpliedPropertyClasses();
		initializeAllExpandedPropertyClasses();

		// process rest of parse tree
		List<SadlModelElement> elements = model.getElements();
		if (elements != null) {
			Iterator<SadlModelElement> elitr = elements.iterator();
			SadlModelElement lastElement = null;
			AnswerCMStatement lastACMQuestion = null;
			while (elitr.hasNext()) {
				// check for cancelation from time to time
				if (cancelIndicator.isCanceled()) {
					throw new OperationCanceledException();
				}
				SadlModelElement element = elitr.next();
        		if (element instanceof EObject) {
        			String txt = NodeModelUtils.findActualNodeFor((EObject) element).getText();
        			if (!(txt.endsWith(".") || txt.endsWith("?"))) {
//                		System.out.println("It's NOT the real deal!");
                		continue;
        			}
        		}
				logger.debug("   Model element of type '" + element.getClass().getCanonicalName() + "' being processed.");
				// reset state for a new model element
				try {
					resetProcessorState(element);
				} catch (InvalidTypeException e) {
					// TODO Auto-generated catch block
					logger.error("Error:", e);
				}
				if (!(element instanceof AnswerCMStatement)) {
					// this is user input
					if (element instanceof ModifiedAskStatement ||
							element instanceof WhatStatement ||
							element instanceof HowManyValuesStatement ||
							element instanceof BuildStatement) {
						lastElement = element;
					}
					else {
						boolean treatAsAnswerToBackend = false;
						if (lastACMQuestion != null) {
							// this could be the answer to a preceding question
							if (element instanceof SadlStatement || element instanceof YesNoAnswerStatement) {
								try {
									IDialogAnswerProvider dap = getDialogAnswerProvider(resource);
									String question = lastACMQuestion.getStr();
									if (question != null) {
										MixedInitiativeElement mie = dap.getMixedInitiativeElement(question);
//										dap.removeMixedInitiativeElement(question);
										if (mie != null) {
								            // construct response
											String answer = getResponseFromSadlStatement(element);
											mie.addArgument(answer);
											dap.provideResponse(mie);
//								            MixedInitiativeElement response = new MixedInitiativeElement(answer, null);
//								            response.setContent(new MixedInitiativeTextualResponse(answer));
//								            // make call identified in element
//								            mie.getRespondTo().accept(response);
											treatAsAnswerToBackend = true;
										}
										else {
//											treatAsAnswerToBackend = true;
										}
									}
								} catch (ConfigurationException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
							}
						}
						if (!treatAsAnswerToBackend) {
							// This is some kind of statement to add to the model
							processModelElement(element);
							setModelChanged(true);
						}
					}
					lastACMQuestion = null;
				}
				else if (((AnswerCMStatement)element).getEos().equals("?")) {
					// this is a question from the backend
					lastACMQuestion = (AnswerCMStatement) element;
				}
				else {
					// this is a response from CM
					//	clear last command
					OntModelProvider.clearPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND);
					lastElement = null;
				}
			}
			if (lastElement != null) {
				// this is the one to which the CM should respond; keep it for the DialogAnswerProvider
				processUserInputElement(lastElement);
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

	private String getResponseFromSadlStatement(SadlModelElement element) {
		// TODO Auto-generated method stub
		String ans = "no";
		if (element instanceof SadlInstance) {
			SadlResource sr = ((SadlInstance)element).getInstance();
			ans = NodeModelUtils.getTokenText(NodeModelUtils.getNode(element));
			int i = 0;
		}
		else if (element instanceof YesNoAnswerStatement) {
			ans = ((YesNoAnswerStatement)element).getAnswer();
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
	
	private void processUserInputElement(SadlModelElement element) {
		try {
			if (element instanceof ModifiedAskStatement) {
				processStatement((ModifiedAskStatement)element);
			}
			else if (element instanceof WhatStatement) {
				processStatement((WhatStatement)element);
			}
			else if (element instanceof HowManyValuesStatement) {
				processStatement((HowManyValuesStatement)element);
			}
			else if (element instanceof BuildStatement) {
				processStatement((BuildStatement)element);
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
	}	
	
	private void processStatement(BuildStatement element) {
		SadlResource modelSr = ((BuildStatement)element).getTarget();
		String modelUri = getDeclarationExtensions().getConceptUri(modelSr);
		System.out.println("Ready to build model '" + modelUri + "'");
		BuildConstruct bc = new BuildConstruct(modelUri);
		OntModelProvider.addPrivateKeyValuePair(element.eResource(), DialogConstants.LAST_DIALOG_COMMAND, bc);
		
	}

	private void processStatement(ModifiedAskStatement stmt) {
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
		}
	}

	private void processStatement(WhatStatement stmt) {
		if (stmt.getStmt() instanceof WhatIsStatement) {
			EObject whatIsTarget = ((WhatIsStatement)stmt.getStmt()).getTarget();
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
				WhatIsConstruct wic = new WhatIsConstruct(trgtObj, whenObj);
				OntModelProvider.addPrivateKeyValuePair(stmt.eResource(), DialogConstants.LAST_DIALOG_COMMAND, wic);
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

	private void processStatement(HowManyValuesStatement stmt) {
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
			OntModelProvider.addPrivateKeyValuePair(stmt.eResource(), DialogConstants.LAST_DIALOG_COMMAND, temp);
		} catch (TranslationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
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
		setTypeCheckingWarningsOnly(true);
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
							.createResource(URI.createPlatformResourceURI(platformPath, false)); 
					newRsrc.load(resource.getResourceSet().getLoadOptions());
					refreshResource(newRsrc);
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
			new SadlUtils().stringToFile(cemf, content, true);
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
				" 	described by setterArgument (note \"is this variable input to a setter?\") with a single value of type boolean\r\n" + 
				" 	described by comment with values of type Comment.\r\n" + 
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
				"then input of ref is true. \r\n" + 
				"\r\n" + 
				"// if there is no l2 as specified in the previous rules, then the following covers that case\r\n" + 
				"// do I need to consider codeBlock?????\r\n" + 
				"Rule SetAsInput2\r\n" + 
				"if c is a CodeVariable and\r\n" + 
				"   ref is reference of c and\r\n" + 
				"   input of ref is not known and\r\n" + 
				"   usage of ref is Used and \r\n" + 
				"   noValue(ref, firstRef)\r\n" + 
				"then input of ref is true. \r\n" + 
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
				"	output of ref is true.      	 \r\n";
		return content;
	}

}

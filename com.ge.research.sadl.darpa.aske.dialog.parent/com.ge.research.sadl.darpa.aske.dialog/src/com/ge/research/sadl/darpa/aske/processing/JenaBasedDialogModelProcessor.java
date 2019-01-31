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
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.CheckType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.darpa.aske.dialog.HowManyValuesStatement;
import com.ge.research.sadl.darpa.aske.dialog.ModifiedAskStatement;
import com.ge.research.sadl.darpa.aske.dialog.ResponseStatement;
import com.ge.research.sadl.darpa.aske.dialog.WhatIsStatement;
import com.ge.research.sadl.darpa.aske.dialog.WhatStatement;
import com.ge.research.sadl.darpa.aske.dialog.WhatValuesStatement;
import com.ge.research.sadl.errorgenerator.generator.SadlErrorMessages;
import com.ge.research.sadl.jena.JenaBasedSadlModelProcessor;
import com.ge.research.sadl.jena.JenaProcessorException;
import com.ge.research.sadl.jena.MetricsProcessor;
import com.ge.research.sadl.model.CircularDefinitionException;
import com.ge.research.sadl.model.ModelError;
import com.ge.research.sadl.model.gp.Equation;
import com.ge.research.sadl.model.gp.Junction;
import com.ge.research.sadl.model.gp.NamedNode;
import com.ge.research.sadl.model.gp.Query;
import com.ge.research.sadl.model.gp.Rule;
import com.ge.research.sadl.model.gp.TripleElement;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.processing.SadlConstants;
import com.ge.research.sadl.processing.ValidationAcceptor;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.InvalidTypeException;
import com.ge.research.sadl.reasoner.TranslationException;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.ge.research.sadl.sADL.Declaration;
import com.ge.research.sadl.sADL.Expression;
import com.ge.research.sadl.sADL.NamedStructureAnnotation;
import com.ge.research.sadl.sADL.QueryStatement;
import com.ge.research.sadl.sADL.SadlAnnotation;
import com.ge.research.sadl.sADL.SadlModel;
import com.ge.research.sadl.sADL.SadlModelElement;
import com.ge.research.sadl.sADL.SadlResource;
import com.ge.research.sadl.sADL.SadlSimpleTypeReference;
import com.ge.research.sadl.sADL.SadlTypeReference;
import com.ge.research.sadl.utils.ResourceManager;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.RDFWriter;

public class JenaBasedDialogModelProcessor extends JenaBasedSadlModelProcessor {
	public static final String LAST_DIALOG_COMMAND = "LastDialogCommand";
	private static final Logger logger = LoggerFactory.getLogger(JenaBasedDialogModelProcessor.class);
	private boolean modelChanged;

	@Override
	public void onValidate(Resource resource, ValidationAcceptor issueAcceptor, CheckMode mode, ProcessorContext context) {
		if (!isSupported(resource)) {
			return;
		}
		resetProcessor();
		logger.debug("onValidate called for Resource '" + resource.getURI() + "'"); 
		CancelIndicator cancelIndicator = context.getCancelIndicator();
		if (resource.getContents().size() < 1) {
			return;
		}
	
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
		} catch (IOException e1) {
			e1.printStackTrace();
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		} catch (URISyntaxException e1) {
			e1.printStackTrace();
		} catch (JenaProcessorException e1) {
			e1.printStackTrace();
		}

		if(!processModelImports(modelOntology, resource.getURI(), model)) {
			return;
		}
		
//		setModelChanged(true);  // for now always save; old strategy isn't working
		
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
			while (elitr.hasNext()) {
				// check for cancelation from time to time
				if (cancelIndicator.isCanceled()) {
					throw new OperationCanceledException();
				}
				SadlModelElement element = elitr.next();
				// reset state for a new model element
				try {
					resetProcessorState(element);
				} catch (InvalidTypeException e) {
					// TODO Auto-generated catch block
					logger.error("Error:", e);
				}
				if (!(element instanceof ResponseStatement)) {
					// this is user input
					if (element instanceof ModifiedAskStatement ||
							element instanceof WhatStatement ||
							element instanceof HowManyValuesStatement) {
						lastElement = element;
					}
					else {
						// This is some kind of statement to add to the model
						processModelElement(element);
						setModelChanged(true);
					}
				}
				else {
					// this is a response from CM
					//	clear last command
					OntModelProvider.clearPrivateKeyValuePair(resource, LAST_DIALOG_COMMAND);
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
	
	private void processStatement(ModifiedAskStatement stmt) {
		try {
			SadlResource elementName = null; // element.getName();
			EList<NamedStructureAnnotation> annotations = null; // element.getAnnotations();
			boolean isGraph = stmt.getStart().equals("Graph");
			Query query = processQueryExpression(stmt, stmt.getExpr(), elementName, annotations, isGraph);
			System.out.println("ModifiedAskStatement: " + query.toDescriptiveString());
			OntModelProvider.addPrivateKeyValuePair(stmt.eResource(), LAST_DIALOG_COMMAND, query);
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
				System.out.println("WhatIsStatement target: " + trgtObj.toString());
				if (trgtObj instanceof NamedNode) {
					((NamedNode)trgtObj).setContext(stmt);
				}
				else if (trgtObj instanceof TripleElement) {
					// TODO
					addInfo("TripleElement not yet handled by dialog processor", whatIsTarget);
				}
				else if (trgtObj instanceof Junction) {
					// TODO
					addInfo("Junction not yet handled by dialog processor", whatIsTarget);
				}
				else {
					// TODO
					addInfo(trgtObj.getClass().getCanonicalName() + " not yet handled by dialog processor", whatIsTarget);
				}
				OntModelProvider.addPrivateKeyValuePair(stmt.eResource(), LAST_DIALOG_COMMAND, trgtObj);
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
				System.out.println("WhatValuesStatement(" + typ + "): cls=" + (article!= null ? article : "") + 
						" '" + clsObj.toString() + "'; prop='" + propObj.toString() + "'");
				Object[] temp = new Object[3];
				temp[0] = article;
				temp[1] = clsObj;
				temp[2] = propObj;
				OntModelProvider.addPrivateKeyValuePair(stmt.eResource(), LAST_DIALOG_COMMAND, temp);
			} catch (TranslationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
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
			System.out.println("HowManyValuesStatement: cls=" + (article!= null ? article : "") + " '" + 
					clsObj.toString() + "'; prop='" + propObj.toString() + 
					"'" + (typObj != null ? ("; type='" + typObj.toString() + "'") : ""));
			Object[] temp = new Object[4];
			temp[0] = article;
			temp[1] = clsObj;
			temp[2] = propObj;
			temp[3] = typObj;
			OntModelProvider.addPrivateKeyValuePair(stmt.eResource(), LAST_DIALOG_COMMAND, temp);
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
}

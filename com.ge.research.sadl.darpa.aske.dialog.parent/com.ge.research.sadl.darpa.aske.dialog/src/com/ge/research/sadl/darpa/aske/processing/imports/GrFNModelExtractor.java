/**
 * 
 */
package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;
import org.apache.log4j.Logger;

import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.imports.GrFN_Metadata.CodeSpan;
import com.ge.research.sadl.darpa.aske.processing.imports.GrFN_Type.Field;
import com.ge.research.sadl.darpa.aske.processing.imports.JavaModelExtractorJP.MethodCallMapping;
import com.ge.research.sadl.jena.JenaProcessorException;
import com.ge.research.sadl.jena.inference.SadlJenaModelGetterPutter;
import com.ge.research.sadl.model.gp.RDFTypeNode;
import com.ge.research.sadl.processing.SadlConstants;
import com.ge.research.sadl.reasoner.AmbiguousNameException;
import com.ge.research.sadl.reasoner.CircularDependencyException;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.TranslationException;
import com.ge.research.sadl.reasoner.IConfigurationManagerForEditing.Scope;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.github.javaparser.JavaParser;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.google.gson.Gson;
import com.google.gson.InstanceCreator;

/**
 * @author 212438865
 *
 */
public class GrFNModelExtractor implements IModelFromCodeExtractor {
    private static final Logger logger = Logger.getLogger (GrFNModelExtractor.class) ;
	private String packageName = "";
	private AnswerCurationManager curationMgr = null;
	private Map<String, String> preferences;

	// Assumption: the GrFN meta model and the GrFN model extracted are in the same folder (same kbase)
	private IConfigurationManagerForIDE codeModelConfigMgr;	// the ConfigurationManager used to access the code extraction model
	private String codeMetaModelUri;	// the name of the grFN extraction metamodel

	private String codeMetaModelPrefix;	// the prefix of the grFN extraction metamodel

	private OntModel codeModel;
	private Map<String,OntModel> codeModels = null;
	private String codeModelName;	// the name of the model  being created by extraction
	private String codeModelPrefix; // the prefix of the model being created by extraction

	private Individual rootContainingInstance = null;

//	private Map<Range, MethodCallMapping> postProcessingList = new HashMap<Range, MethodCallMapping>(); // key is the MethodCallExpr
	Map<String, String> classNameMap = new HashMap<String, String>();

	
	public GrFNModelExtractor(AnswerCurationManager acm, Map<String, String> preferences) {
		setCurationMgr(acm);
		setPreferences(preferences);
	}
	
	private void setCurationMgr(AnswerCurationManager curationMgr) {
		this.curationMgr = curationMgr;
	}
	private AnswerCurationManager getCurationMgr() {
		return curationMgr;
	}
	private void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}
	
	private void initializeContent(String modelName, String modelPrefix) {
		packageName = "";
//		postProcessingList.clear();
		if (getCodeModelName() == null) {	// don't override a preset model name
			setCodeModelName(modelName);
		}
		if (getCodeModelPrefix() == null) {
			setCodeModelPrefix(modelPrefix);	// don't override a preset model name		
		}
		codeModel = null;
		rootContainingInstance = null;
//		postProcessingList.clear();
		classNameMap.clear();
//		clearAggregatedComments();

	}
	
	/***
	 * inputIdentifier: identifier of file with grFN to be parsed
	 * content: string with the full content of the file
	 * modelName: url of the output model name
	 * modelPrefix
	 */
	@Override
	public boolean process(String inputIdentifier, String content, String modelName, String modelPrefix)
			throws ConfigurationException, IOException {
	    initializeContent(modelName, modelPrefix);
		String defName = getCodeModelName() + "_comments";
		getCurationMgr().getTextProcessor().setTextModelName(defName);
		String defPrefix = getCodeModelPrefix() + "_cmnt";
		getCurationMgr().getTextProcessor().setTextModelPrefix(defPrefix);

		parse(inputIdentifier, getCurationMgr().getOwlModelsFolder(), content);

		return true;
	}

//	private class GrFN_ExpressionNodeInstanceCreator implements InstanceCreator<GrFN_ExpressionNode> {
//		public GrFN_ExpressionNode createInstance(Type type) {
//			return new GrFN_ExpressionNode(uid);
//		}
//	}

	
	private void parse(String inputIdentifier, String modelFolder, String jsonASTContent) throws IOException, ConfigurationException {
		try {
			String source = null;
			if (inputIdentifier.lastIndexOf('/') > 0) {
				source = inputIdentifier.substring(inputIdentifier.lastIndexOf('/') + 1);
			}
			else if (inputIdentifier.lastIndexOf('\\') > 0) {
				source = inputIdentifier.substring(inputIdentifier.lastIndexOf('\\') + 1);
			}
			else {
				source = inputIdentifier;
			}
			String msg = "Parsing GrFN Json file '" + source + "'.";
			getCurationMgr().notifyUser(modelFolder, msg, true);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("***************** code to process ******************");
			logger.debug(jsonASTContent);
			logger.debug("****************************************************");
		}
				
        initializeGrFNModel(getCurationMgr().getOwlModelsFolder());
		
		Gson gson = new Gson();
		
		GrFN_Graph gr; //= new GrFN_Graph();
		GrFN_ExpressionTree[] expr;

		try {
			gr = gson.fromJson(jsonASTContent, GrFN_Graph.class);
			if (gr.getUid() != null) {
				processGrFN(gr);     	
			} 
			else { 
				if (logger.isDebugEnabled()) {
					logger.debug(" json is not a GrFN Graph ");
				}	
			}
		}catch (Exception e) {
//			e.printStackTrace();
		}
			
		try {
			expr = gson.fromJson(jsonASTContent, GrFN_ExpressionTree[].class);
			for (GrFN_ExpressionTree e : expr) {
				processGrFN_ExpressionTree(e);
			}
		} catch (Exception e) {
//			e.printStackTrace();
		}
		
		
		/*****
		// ACM may already save the owl model as sadl. See ACM line 435
		// If not, then we may need to invoke saveAsSadlFile from here

		String response = "Yes";
		Map<File, Boolean> outputOwlFiles = Map.of(owlfilename, true);
		String sadlFileName = saveAsSadlFile(Map<File, Boolean> outputOwlFiles, response)
		
		*****/
		
//        // Set up a minimal type solver that only looks at the classes used to run this sample.
//        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
//        combinedTypeSolver.add(new ReflectionTypeSolver());
//
//        // Configure JavaParser to use type resolution
//        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
//        JavaParser.getStaticConfiguration().setSymbolResolver(symbolSolver);
//
//        // Parse some code
//        CompilationUnit cu = JavaParser.parse(javaCodeContent);
//        Optional<PackageDeclaration> pkg = cu.getPackageDeclaration();
//        if (pkg.isPresent()) {
//        	setPackageName(pkg.get().getNameAsString());
//        	String rootClassName = getRootClassName(cu);
//        	setCodeModelName(getPackageName() + "/" + rootClassName);
//        	setCodeModelPrefix(derivePrefixFromPackage(getCodeModelName()));
//        }
//        initializeCodeModel(getCurationMgr().getOwlModelsFolder());
//        findAllClassesToIgnore(cu);
//        processBlock(cu, null);
//        postProcess();
	}

//	private void processGrFN_ExpressionArray(GrFN_ExpressionArray expr) {
//		for(GrFN_ExpressionTree e : expr.getExpressions()) {
//			processGrFN_ExpressionTree(e);
//		}
//	}

	private void processGrFN_ExpressionTree(GrFN_ExpressionTree e) {
		Individual eInst = getOrCreateExpressionTree(e);
		for(GrFN_ExpressionNode nd : e.getNodes()) {
			processExpressionNode(eInst, nd);
		}
	}

	private void processExpressionNode(Individual eInst, GrFN_ExpressionNode nd) {
		String p = nd.getType();
		Individual ndInst = null;

		//If it is a variable, the instance may already exist
		if (p.equals("VARIABLE") ) {
			String uid = nd.getUid();
			ndInst = getOrCreateVariable(uid);
			p = nd.getIdentifier();
			if (p != null) {
				//Note: If ndInst already exists, it may already have an identifier
				addDataProperty(ndInst, getVarnameProperty(), p);
			}
			p = nd.getGrfn_uid();
			if (p != null && !p.equals("")) {
				Individual vInst = getOrCreateVariable(p);
				ndInst.addProperty(getGrFNUidProperty(), vInst);
			}
		}
		else { //if not a variable, it is an OPERATOR or VALUE
			String uid = nd.getUid();
			ndInst = getOrCreateExpNode(uid);
			p = nd.getOperator();
			if (p != null) {
				addDataProperty(ndInst, getOperatorProperty(), p);
			}
			p = nd.getValue();
			if (p != null) {
				addDataProperty(ndInst, getNodeValueProperty(), p);	
			}
		}
		
		eInst.addProperty(getNodesProperty(), ndInst);
		p = nd.getType();
		addDataProperty(ndInst, getNodeTypeProperty(), p);
		
		// Only operators have children
		List<String> children = nd.getChildren();
		List<Individual> childInstances = new ArrayList<Individual>();
		if(children != null) {
			for (String c : children) { // children are expression tree node uids
				//child may be a variable or an operator. In either case, it is an Node
				Individual cInst = getOrCreateExpNode(c);
				childInstances.add(cInst);
			}
			if (childInstances.size() > 0) {
				try {
					Individual typedList = addMembersToList(getCurrentCodeModel(), null, getExpNodeListClass(), getExpNodeClass(), childInstances.iterator());
					ndInst.addProperty(getChildrenProperty(), typedList);
				} catch (JenaProcessorException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TranslationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}
		
//		processMetadata(nd, ndInst);
		 
	}

	private void processMetadata(GrFN_Node nd, Individual ndInst) {
		List<GrFN_Metadata> md = nd.getMetadata();
		Individual mdInst = createNewMetadata();
		ndInst.addProperty(getMetadataProperty(), mdInst);
		int bl,el;
		if (md != null) {
			for (GrFN_Metadata mdEntry : md) {
				if (mdEntry.getType().equals("code_span_reference") && mdEntry.getCode_span() != null) {
					bl = mdEntry.getCode_span().getLine_begin();
					if (bl > 0) {
						mdInst.addLiteral(getLineBeginsProperty(), mdEntry.getCode_span().getLine_begin());						
					}
					el = mdEntry.getCode_span().getLine_end();
					if (el > 0) {
						mdInst.addLiteral(getLineEndsProperty(), mdEntry.getCode_span().getLine_end());						
					}
				}
			}
		}
	}

	private void processGrFN(GrFN_Graph gr) {

		Individual grfnInst = getOrCreateGraph(gr);

		String p = gr.getDescription();
		if (p != null) {
			addDataProperty(grfnInst, getDescriptionProperty(), p);
		}

		p = gr.getIdentifier();
		if (p != null) {
			addDataProperty(grfnInst, getIdentifierProperty(), p);
		}

		p = gr.getDate_created();
		if (p != null) {
			addDataProperty(grfnInst, getDateCreatedProperty(), p);
		}

		List<GrFN_Function> fns = gr.getFunctions();
		for (GrFN_Function fn : fns) {
			processFunction(grfnInst, fn);
		}

		List<GrFN_Variable> vars = gr.getVariables();
		for (GrFN_Variable var : vars) {
			processVariable(grfnInst, var);
		}

		List<GrFN_Hyperedge> edges = gr.getHyperedges();
		for (GrFN_Hyperedge edge : edges) {
			processEdge(grfnInst, edge);
		}

		List<GrFN_Subgraph> subgraphs = gr.getSubgraphs();
		for (GrFN_Subgraph sg : subgraphs) {
			processSubgraph(grfnInst, sg);
		}
		
//		List<GrFN_Object> objects = gr.getObjects();
//		for (GrFN_Object obj : objects) {
//			processObjects(grfnInst, obj);
//		}

		List<GrFN_Type> types = gr.getTypes();
		for (GrFN_Type ty : types) {
			processTypes(grfnInst, ty);
		}

	}

	private void processTypes(Individual grfnInst, GrFN_Type type) {
		Individual typeInst = getOrCreateType(type);
//		grfnInst.addProperty(getObjectsProperty(), typeInst);
		String p = type.getUid();
		if (p != null) {
			addDataProperty(typeInst, getUidProperty(), p);
		}
		p = type.getName();
		if (p != null) {
			addDataProperty(typeInst, getNameProperty(), p);
		}
		List<Field> fields = type.getFields();
		for (Field f : fields) {
			Individual fieldInst = createNewField();
			typeInst.addProperty(getFieldsProperty(), fieldInst);
			p = f.getName();
			if (p != null) {
				addDataProperty(fieldInst, getNameProperty(), p);
			}
			p = f.getType();
			if (p != null) {
				addDataProperty(fieldInst, getDataTypeProperty(), p);
			}
		}
		
		processMetadata((GrFN_Node)type, typeInst);

	}


//	private void processObjects(Individual grfnInst, GrFN_Object obj) {
//		Individual objInst = getOrCreateObject(obj);
//		grfnInst.addProperty(getObjectsProperty(), objInst);
//		String p = obj.getUid();
//		if (p != null) {
//			addDataProperty(objInst, getUidProperty(), p);
//		}
//		String type = obj.getType();
//		Individual typeInst = getOrCreateType(type);
//		objInst.addProperty(getObjTypeProperty(), typeInst);
//		
//		
////		processMetadata((GrFN_Node)obj, objInst);
//
//	}



	private void processSubgraph(Individual grfnInst, GrFN_Subgraph sg) {
		Individual sgInst = getOrCreateSubgraph(sg); 
		grfnInst.addProperty(getSubgraphsProperty(), sgInst);
		String p = sg.getUid();
		if (p != null) {
			addDataProperty(sgInst, getUidProperty(), p);
		}
		p = sg.getName();
		if (p != null) {
			addDataProperty(sgInst, getNameProperty(), p);
		}
		p = sg.getType();
		if (p != null) {
			addDataProperty(sgInst, getSGTypeProperty(), p);
		}
		p = sg.getScope();
		if (p != null) {
			addDataProperty(sgInst, getScopeProperty(), p);
		}
		p = sg.getNamespace();
		if (p != null) {
			addDataProperty(sgInst, getNamespaceProperty(), p);
		}
		List<String> nodes = sg.getNodes();
		for (String n : nodes) {
			Individual nInst = getNodeInst(n);
			sgInst.addProperty(getSubgraphNodesProperty(), nInst);
		}
		p = sg.getParent();
		if (p != null) { // a subgraph may not have a parent
			Individual parInst = getOrCreateSubgraph(p);
			if (parInst != null) {
				sgInst.addProperty(getParentProperty(), parInst);
			}
		}
		
		processMetadata((GrFN_Node)sg, sgInst);

	}

	private void processEdge(Individual grfnInst, GrFN_Hyperedge edge) {
		//TODO: am I missing 
		Individual edgeInst = createNewEdge();
		grfnInst.addProperty(getEdgesProperty(), edgeInst);
		String fuid = edge.getFunction();
		List<String> inputs = edge.getInputs();
		List<String> outputs = edge.getOutputs();
		Individual fnInst = getOrCreateFunction(fuid);
		edgeInst.addProperty(getEdgeFunctionProperty(), fnInst);
		for (String inp : inputs) {
			Individual inpInst = getOrCreateVariable(inp);
			edgeInst.addProperty(getEdgeInputProperty(), inpInst);
		}
		for (String outp : outputs) {
			Individual outpInst = getOrCreateVariable(outp);
			edgeInst.addProperty(getEdgeOutputProperty(), outpInst);
		}
	}


	private void processVariable(Individual grfnInst, GrFN_Variable var) {
		Individual varInst = getOrCreateVariable(var);
		String p = var.getIdentifier();
		if (p != null) {
			addDataProperty(varInst, getIdentifierProperty(), p);
		}
		p = var.getUid();
		if (p != null) {
			addDataProperty(varInst, getUidProperty(), p);
		}
		p = var.getData_type();
		if (p != null) {
			addDataProperty(varInst, getDataTypeProperty(), p);
		}
		
		processMetadata((GrFN_Node)var, varInst);

	}




	private void processFunction(Individual grfnInst, GrFN_Function fn) {
		Individual fnInst = getOrCreateFunction(fn);
//		addObjectProperty(grfnInst,getFunctionProperty(), fnInst);
		String p = fn.getLambda();
		if (p != null) {
			addDataProperty(fnInst, getLambdaProperty(), p);
		}
		p = fn.getType();
		if (p != null) {
			addDataProperty(fnInst, getFunctionTypeProperty(), p);
		}
		
		processMetadata((GrFN_Node)fn, fnInst);

	}


//	private void addObjectProperty(Individual subject, Property property, Individual object) {
//		subject.addProperty(property, object);		
//	}

	private void addDataProperty(Individual subject, Property property, String d) {
		subject.addProperty(property, getCurrentCodeModel().createTypedLiteral(d));
	}

	private Individual getOrCreateGraph(GrFN_Graph gr) {
		Individual graphInst = null;
		String uid = gr.getUid();
		graphInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + uid);
    	if (graphInst == null) {
    		graphInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + uid, getGrFNClass());
    	}
    	else {
    		System.out.println("Something's wrong. This GrFN already exists!");
    	}
		return graphInst;
	}
	
	private Individual getOrCreateFunction(GrFN_Function fn) {
		Individual fnInst = null;
		String uid = fn.getUid();
		fnInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + uid);
    	if (fnInst == null) {
    		fnInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + uid, getFunctionClass());
    	}
    	else {
//    		System.out.println("Something's wrong. This Function already exists!");
    	}
		return fnInst;
	}


	private Individual getOrCreateFunction(String uid) {
		Individual fnInst = null;
		if (uid.length() > 0 && Character.isDigit(uid.toCharArray()[0]) ) {
			uid = "_" + uid;
		}
		fnInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + uid);
    	if (fnInst == null) {
    		fnInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + uid, getFunctionClass());
    	}
    	else {
//    		System.out.println("Something's wrong. This Function already exists!");
    	}
		return fnInst;
	}

	
	
	private Individual getOrCreateVariable(GrFN_Variable var) {
		Individual varInst = null;
		String uid = var.getUid(); 
		varInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + uid);
		if (varInst == null) {
			varInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + uid, getVariableClass());
		}
    	else {
//    		System.out.println("Something's wrong. Variable " + uid + " already exists!");
    	}
		return varInst;
	}
	
	
	private Individual getOrCreateVariable(String uid) {
		Individual varInst = null;
		if (uid.length() > 0 && Character.isDigit(uid.toCharArray()[0]) ) {
			uid = "_" + uid;
		}
		varInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + uid);
		if (varInst == null) {
			varInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + uid, getVariableClass());
		}
    	else {
//    		System.out.println("Something's wrong. Variable " + inp + " already exists!");
    	}
		return varInst;
	}

	private Individual getOrCreateExpressionTree(GrFN_ExpressionTree e) {
		Individual eInst = null;
		String uid = e.getUid();
//		String uid = e.getFunc_node_uid();
		eInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + uid);
    	if (uid == null || eInst == null) {
    		eInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + uid, getExpTreeClass());
    	}
    	else {
    		System.out.println("Something's wrong. This GrFN already exists!");
    	}
		return eInst;
	}


	
	private Individual createNewEdge() {
		Individual edgeInst = getCurrentCodeModel().createIndividual(getEdgeClass());
		return edgeInst;
	}


	private Individual createNewMetadata() {
		Individual mdInst = getCurrentCodeModel().createIndividual(getMetadataClass());
		return mdInst;
	}

	private Individual createNewField() {
		Individual fieldInst = getCurrentCodeModel().createIndividual(getFieldClass());
		return fieldInst;
	}
	
	private Individual getOrCreateSubgraph(GrFN_Subgraph sg) {
		Individual sgInst = null;
		String uid = sg.getUid();
		sgInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + uid);
		if (sgInst == null) {
			sgInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + uid, getSubgraphClass());
		}
		return sgInst;
	}
	private Individual getOrCreateSubgraph(String uid) {
		Individual sgInst = null;
		if (uid.length() > 0 && Character.isDigit(uid.toCharArray()[0]) ) {
			uid = "_" + uid;
		}
		sgInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + uid);
		if (sgInst == null) {
			sgInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + uid, getSubgraphClass());
		}
		return sgInst;
	}
	
	private Individual getOrCreateObject(GrFN_Object obj) {
		Individual objInst = null;
		String uid = obj.getUid();
		objInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + uid);
		if (objInst == null) {
			objInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + uid, getObjectClass());
		}
		return objInst;

	}
	
	private Individual getOrCreateType(GrFN_Type type) {
		Individual typeInst = null;
		String tuid = type.getName();  //type.getUid(); UofA removed uids for types
		typeInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + tuid);
		if (typeInst == null) {
			typeInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + tuid, getTypeClass());
		}
		return typeInst;
	}

	
	private Individual getOrCreateType(String uid) {
		Individual typeInst = null;
		typeInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + uid);
		if (typeInst == null) {
			typeInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + uid, getTypeClass());
		}
		return typeInst;
	}
	
//	private Individual createNewSubgraph() {
//		Individual sgInst = getCurrentCodeModel().createIndividual(getSubgraphClass());
//		return sgInst;
//	}
	
	private Individual getOrCreateNode(GrFN_Node node) {
		Individual ndInst = null;
		String uid = node.getUid();
		ndInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + uid);
		if (ndInst == null) {
			ndInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + uid, getNodeClass());
		}
		return ndInst;
	}

	private Individual getOrCreateNode(String uid) {
		Individual ndInst = null;
		if (uid.length() > 0 && Character.isDigit(uid.toCharArray()[0]) ) {
			uid = "_" + uid;
		}
		ndInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + uid);
		if (ndInst == null) {
			ndInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + uid, getNodeClass());
		}
		return ndInst;
	}

	private Individual getNodeInst(String uid) {
		Individual ndInst = null;
		if (uid.length() > 0 && Character.isDigit(uid.toCharArray()[0]) ) {
			uid = "_" + uid;
		}
		ndInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + uid);
		if (ndInst == null) {
			System.out.println("Something's wrong. Subgraph node with " + uid + " does not exist!");
		}
		return ndInst;
	}


	private Individual getOrCreateExpNode(GrFN_ExpressionNode nd) {
		Individual ndInst = null;
		String uid = nd.getUid();
		ndInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + uid);
		if (ndInst == null) {
			ndInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + uid, getExpNodeClass());
		}
		return ndInst;
	}
	private Individual getOrCreateExpNode(String uid) {
		Individual ndInst = null;
		if (uid.length() > 0 && Character.isDigit(uid.toCharArray()[0]) ) {
			uid = "_" + uid;
		}
		ndInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + uid);
		if (ndInst == null) {
			ndInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + uid, getExpNodeClass());
		}
		return ndInst;
	}

	private Individual getOrCreateNodeAndVar(String uid) {
		Individual ndInst = null;
		if (uid.length() > 0 && Character.isDigit(uid.toCharArray()[0]) ) {
			uid = "_" + uid;
		}
		ndInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + uid);
		if (ndInst == null) {
			ndInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + uid, getNodeAndVarClass());
		}
		return ndInst;
	}


	private void initializeGrFNModel(String extractionMetaModelModelFolder) throws ConfigurationException, IOException {
		if (getCurationMgr().getExtractionProcessor().getGrFNModel() == null) {
			// create new code model
			
			setCodeModelConfigMgr(ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(extractionMetaModelModelFolder, null)); //getCurationMgr().getProjectConfigurationManager());
			OntDocumentManager owlDocMgr = getCodeModelConfigMgr().getJenaDocumentMgr();
			OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
			if (extractionMetaModelModelFolder != null) { // && !modelFolderPathname.startsWith(SYNTHETIC_FROM_TEST)) {
				File mff = new File(extractionMetaModelModelFolder);
				mff.mkdirs();
				spec.setImportModelGetter(new SadlJenaModelGetterPutter(spec, extractionMetaModelModelFolder));
			}
			if (owlDocMgr != null) {
				spec.setDocumentManager(owlDocMgr);
				owlDocMgr.setProcessImports(true);
			}
			setCurrentCodeModel(ModelFactory.createOntologyModel(spec));	
			getCurrentCodeModel().setNsPrefix(getCodeModelPrefix(), getCodeModelNamespace());
			Ontology modelOntology = getCurrentCodeModel().createOntology(getCodeModelName());
			logger.debug("Ontology '" + getCodeModelName() + "' created");
			modelOntology.addComment("This ontology was created by extraction from GrFN json by the GraSEN GrFNModelExtractor.", "en");
			setCodeMetaModelUri(DialogConstants.GRFN_EXTRACTION_MODEL_URI);
			setCodeMetaModelPrefix(DialogConstants.GRFN_EXTRACTION_MODEL_PREFIX);
			OntModel importedOntModel = getCodeModelConfigMgr().getOntModel(getCodeMetaModelUri(), Scope.INCLUDEIMPORTS);
			addImportToJenaModel(getCodeModelName(), getCodeMetaModelUri(), getCodeMetaModelPrefix(), importedOntModel);
			OntModel sadlImplicitModel = getCodeModelConfigMgr().getOntModel(getSadlImplicitModelUri(), Scope.INCLUDEIMPORTS);
			addImportToJenaModel(getCodeModelName(), getSadlImplicitModelUri(), 
					getCodeModelConfigMgr().getGlobalPrefix(getSadlImplicitModelUri()), sadlImplicitModel);
			String listmodelurl = getCodeModelConfigMgr().getAltUrlFromPublicUri(getSadlListModelUri());
			if (listmodelurl != null && !listmodelurl.equals(getSadlListModelUri())) {
				if (new File((new SadlUtils()).fileUrlToFileName(listmodelurl)).exists()) {
					OntModel sadlListModel = getCodeModelConfigMgr().getOntModel(getSadlListModelUri(), Scope.INCLUDEIMPORTS);
					addImportToJenaModel(getCodeModelName(), getSadlListModelUri(), 
							getCodeModelConfigMgr().getGlobalPrefix(getSadlListModelUri()), sadlListModel);
				}
				else {
					System.err.println("Project is missing SadlListModel. This should not happen.");
				}
			}
			//For Java: get the subclasses of the class ClassesToIgnore and makes a list.
			//These subclasses just define the type of Java classes to ignore, e.g. Graphics, Button, etc.
			//For GrFN we probably don't need to ignore any type of node.
//			OntClass ctic = getClassesToIgnoreClass();
//			if (ctic != null) {
//				ExtendedIterator<OntClass> extitr = ctic.listSubClasses();
//				if (extitr != null && extitr.hasNext()) {
//					while (extitr.hasNext()) {
//						Resource cti = extitr.next();
//						classesToIgnore.add(cti.getLocalName());
//					}
//					extitr.close();
//				}
//			}
		}
		else {
			setCurrentCodeModel(getCurationMgr().getExtractionProcessor().getCodeModel());
		}
		
	}

	
	/**
	 * Method to add an import to the model identified by modelName
	 * @param modelName
	 * @param importUri
	 * @param importPrefix
	 * @param importedOntModel
	 */
	private void addImportToJenaModel(String modelName, String importUri, String importPrefix, Model importedOntModel) {
		getCurrentCodeModel().getDocumentManager().addModel(importUri, importedOntModel, true);
		Ontology modelOntology = getCurrentCodeModel().createOntology(modelName);
		if (importPrefix != null) {
			getCurrentCodeModel().setNsPrefix(importPrefix, ensureHashOnUri(importUri));
		}
		org.apache.jena.rdf.model.Resource importedOntology = getCurrentCodeModel().createResource(importUri);
		modelOntology.addImport(importedOntology);
		getCurrentCodeModel().addSubModel(importedOntModel);
		getCurrentCodeModel().addLoadedImport(importUri);
	}
	private String ensureHashOnUri(String uri) {
		if (!uri.endsWith("#")) {
			uri+= "#";
		}
		return uri;
	}

	private String getSadlImplicitModelUri() {
		return "http://sadl.org/sadlimplicitmodel";
	}

	private String getSadlListModelUri() {
		return "http://sadl.org/sadllistmodel";
	}

	
	public String getCodeMetaModelUri() {
		return codeMetaModelUri;
	}

	public void setCodeMetaModelUri(String codeMetaModelUri) {
		this.codeMetaModelUri = codeMetaModelUri;
	}
	
	public String getCodeMetaModelPrefix() {
		return codeMetaModelPrefix;
	}

	public void setCodeMetaModelPrefix(String codeMetaModelPrefix) {
		this.codeMetaModelPrefix = codeMetaModelPrefix;
	}
	

	private Resource getGrFNClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#GrFN");
	}
	private Resource getNodeClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#Node");
	}
	private Resource getFunctionClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#Function");
	}
	private Resource getVariableClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#Variable");
	}
	private Resource getEdgeClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#HyperEdge");
	}
	private Resource getSubgraphClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#SubGraph");
	}
	private Resource getObjectClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#Object");
	}
	private Resource getTypeClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#Type");
	}
	private Resource getFieldClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#Field");
	}
	private Resource getExpTreeClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#ExpressionTree");
	}
	private Resource getExpNodeClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#ExpNode");
	}
	private Resource getNodeAndVarClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#NodeAndVariable");
	}
	private Resource getMetadataClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#Metadata");
	}
	private Property getDescriptionProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#description");
	}
	private Property getUidProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#uid");
	}
	private Property getIdentifierProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#identifier");
	}
	private Property getDateCreatedProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#date_created");
	}
	private Property getFunctionProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#functions");
	}
	private Property getLambdaProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#lambda");
	}
	private Property getFunctionTypeProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#ftype");
	}
	private Property getNameProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#name");
	}
	private Property getDataTypeProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#data_type");
	}
	private Property getEdgeInputProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#inputs");
	}
	private Property getEdgeOutputProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#outputs");
	}
	private Property getEdgesProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#hyper_edges");
	}
	private Property getEdgeFunctionProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#function");
	}
	private Property getSubgraphsProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#subgraphs");
	}
	private Property getSGTypeProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#sg_type");
	}
	private Property getScopeProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#scope");
	}
	private Property getNamespaceProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#namespace");
	}
//	private Property getObjectsProperty() {
//		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#objects");
//	}
//	private Property getObjTypeProperty() {
//		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#obj_type");
//	}
	private Property getSubgraphNodesProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#nodes");
	}
	private Property getParentProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#parent");
	}
	private Property getFieldsProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#fields");
	}
	private Property getNodeTypeProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#exp_node_type");
	}
	private Property getVarnameProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#var_name");
	}
	private Property getOperatorProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#operator");
	}
	private Property getChildrenProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#children");
	}
	private Property getNodesProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#nodes");
	}
	private Property getNodeValueProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#node_value");
	}
	private Property getGrFNUidProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#grfn_uid");
	}
	private Property getMetadataProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#metadata");
	}
	private Property getLineBeginsProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#line_begin");
	}
	private Property getLineEndsProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#line_end");
	}


	private OntClass getExpNodeListClass() {
		Property childrenProp = getChildrenProperty();
		StmtIterator stmtItr = getCurrentCodeModel().listStatements(childrenProp, RDFS.range, (RDFNode)null);
		if (stmtItr.hasNext()) {
			RDFNode rng = stmtItr.nextStatement().getObject();
			if (rng.asResource().canAs(OntClass.class)) {
				return rng.asResource().as(OntClass.class);
			}
		}
		return null;
	}

//	private OntClass getLinesListClass() {
//		Property linesProp = getLinesProperty();
//		StmtIterator stmtItr = getCurrentCodeModel().listStatements(linesProp, RDFS.range, (RDFNode)null);
//		if (stmtItr.hasNext()) {
//			RDFNode rng = stmtItr.nextStatement().getObject();
//			if (rng.asResource().canAs(OntClass.class)) {
//				return rng.asResource().as(OntClass.class);
//			}
//		}
//		return null;
//	}

	

	/**
	 * Method to convert an Iterator over a List of values to a SADL Typed List in the provided model
	 * @param lastInst -- the list to which to add members
	 * @param cls -- the class of the SADL list
	 * @param type --  the type of the members of the list
	 * @param memberIterator -- Iterator over the values to add
	 * @return -- the list instance
	 * @throws JenaProcessorException
	 * @throws TranslationException
	 */
	protected Individual addMembersToList(OntModel model, Individual lastInst, OntClass cls,
			org.apache.jena.rdf.model.Resource type, Iterator<?> memberIterator) throws JenaProcessorException, TranslationException {
		if (lastInst == null) {
			lastInst = model.createIndividual(cls);
		}
		Object val = memberIterator.next();
		if (val instanceof Individual) {
			Individual listInst = (Individual) val;
			if (type.canAs(OntClass.class)) {
				ExtendedIterator<org.apache.jena.rdf.model.Resource> itr = listInst.listRDFTypes(false);
				boolean match = false;
				while (itr.hasNext()) {
					org.apache.jena.rdf.model.Resource typ = itr.next();
					if (typ.equals(type)) {
						match = true;
					} else {
						try {
							if (typ.canAs(OntClass.class) && SadlUtils.classIsSubclassOf(typ.as(OntClass.class), type.as(OntClass.class), true, null)) {
								match = true;
							}
						} catch (CircularDependencyException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					if (match) {
						break;
					}
				}
				if (!match) {
					throw new JenaProcessorException("The Instance '" + listInst.toString() + "' doesn't match the List type.");
				}
				model.add(lastInst, model.getProperty(SadlConstants.SADL_LIST_MODEL_FIRST_URI),
						listInst);
			} else {
				throw new JenaProcessorException("The type of the list could not be converted to a class.");
			}
		} else {
			Literal lval;
			if (val instanceof Literal) {
				lval = (Literal) val;
			}
			else {
				lval = SadlUtils.getLiteralMatchingDataPropertyRange(model,type.getURI(), val);
			}
			model.add(lastInst, model.getProperty(SadlConstants.SADL_LIST_MODEL_FIRST_URI), lval);
		}
		if (memberIterator.hasNext()) {
			Individual rest = addMembersToList(model, null, cls, type, memberIterator);
			model.add(lastInst, model.getProperty(SadlConstants.SADL_LIST_MODEL_REST_URI), rest);
		}
		return lastInst;
	}

	
	
	@Override
	public String getPackageName() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void addCodeFile(File javaFile) {
		// TODO Auto-generated method stub

	}

	@Override
	public void addCodeFiles(List<File> javaFiles) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<File> getCodeFiles() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setCodeFiles(List<File> codeFiles) {
		// TODO Auto-generated method stub

	}

	@Override
	public IConfigurationManagerForIDE getCodeModelConfigMgr() {
		return codeModelConfigMgr;
	}

	private void setCodeModelConfigMgr(IConfigurationManagerForIDE codeMetaModelConfigMgr) {
		this.codeModelConfigMgr = codeMetaModelConfigMgr;
	}

	
	@Override
	public String getCodeModelName() {
		return codeModelName;
	}

	@Override
	public void setCodeModelName(String codeModelName) {
		if (codeModelName != null && !codeModelName.startsWith("http://")) {
			codeModelName = "http://" + codeModelName;
		}
		this.codeModelName = codeModelName;
	}

	@Override
	public String getCodeModelPrefix() {
		return codeModelPrefix;
	}

	@Override
	public void setCodeModelPrefix(String prefix) {
		this.codeModelPrefix = prefix;
	}

	@Override
	public String getCodeModelNamespace() {
		return codeModelName + "#";
	}

	@Override
	public void addCodeModel(String key, OntModel codeModel) {
		if (codeModels == null) {
			codeModels = new HashMap<String, OntModel>();
		}
		codeModels.put(key, codeModel);
	}

	@Override
	public Map<String, OntModel> getCodeModels() {
		return codeModels;
	}

	@Override
	public OntModel getCodeModel(String key) {
		if (codeModels != null) {
			return codeModels.get(key);
		}
		return null;
	}

	@Override
	public void setCurrentCodeModel(OntModel m) {
		this.codeModel = m;

	}

	@Override
	public OntModel getCurrentCodeModel() {
		return codeModel;
	}

	@Override
	public boolean isIncludeSerialization() {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public void setIncludeSerialization(boolean includeSerialization) {
		// TODO Auto-generated method stub

	}

	@Override
	public ResultSet executeSparqlQuery(String query) throws ConfigurationException, ReasonerNotFoundException,
			IOException, InvalidNameException, QueryParseException, QueryCancelledException, AmbiguousNameException {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] extractPythonEquationFromCodeExtractionModel(String pythonScript) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String[] extractPythonEquationFromCodeExtractionModel(String pythonScript, String defaultMethodName) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getClassUriFromSimpleName(String name) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getAggregatedComments() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void clearAggregatedComments() {
		// TODO Auto-generated method stub

	}

}

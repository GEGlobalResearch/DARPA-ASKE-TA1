package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.emf.ecore.resource.Resource;

import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.imports.SadlModelGenerator.SadlMethod;
import com.ge.research.sadl.jena.inference.SadlJenaModelGetterPutter;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.ConfigurationManager;
import com.ge.research.sadl.reasoner.IConfigurationManagerForEditing.Scope;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.ReceiverParameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.ConditionalExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.MethodReferenceExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;

public class JavaModelExtractorJP implements IModelFromCodeExtractor {
    private static final Logger logger = Logger.getLogger (JavaModelExtractorJP.class) ;
	private SadlModelGenerator smg = null;
	private String packageName = "";
	private String type = null;
	private String typeComment = null;
	private List<SadlMethod> methods = new ArrayList<SadlMethod>();
	private Set<String> names = new HashSet<String>();
	private Map<String, String> classDeclarations = new HashMap<String, String>();
	private List<Comment> comments = new ArrayList<Comment>();
	private AnswerCurationManager curationMgr = null;
	
	public enum CONTEXT {PackageDecl, MainClassDecl, InnerClassDecl, ConstructorDecl, MethodDecl, Expression,
		Block, MethodBody}
	public enum USAGE {Defined, Used, Reassigned}
	private CONTEXT currentContext = null;
	private Map<String, String> preferences;
	Map<String, Tag> tagMap = null;;
	private List<File> codeFiles;
	private OntModel codeModel;
	private String codeModelName;
	private String codeModelPrefix;
	private String codeMetaModelUri;
	private IConfigurationManagerForIDE codeMetaModelConfigMgr;
	
	private static class MethodNameCollector extends VoidVisitorAdapter<List<MethodDeclaration>> {	
		@Override
		public void visit(MethodDeclaration md, List<MethodDeclaration> collector) {
			super.visit(md,  collector);
			collector.add(md);
		}
	}
	
	public JavaModelExtractorJP(AnswerCurationManager acm, SadlModelGenerator gen, Map<String, String> preferences) {
		setCurationMgr(acm);
		smg = gen;
		this.setPreferences(preferences);
	    logger.setLevel(Level.ALL);
	}
	
	private void initializeContent() {
		packageName = "";
		type = null;
		
		methods.clear();
		names.clear();
		getComments().clear();
	}

	public boolean process(String content) throws ConfigurationException, IOException {
		setCodeModelName("http://com.ge.research/darpa/aske/answer/testcodeextraction");
		setCodeModelPrefix("testcodeextraction");
		if (getCurationMgr().getExtractionProcessor().getCodeModel() == null) {
			// create new code model
			String astKbRoot = "C:/Users/200005201/sadl3-master6/git/DARPA-ASKE-TA1/Ontology/M5";
			
			String modelFolderPathname = astKbRoot + "/OwlModels";
			setCodeMetaModelConfigMgr(ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(modelFolderPathname, null)); //getCurationMgr().getProjectConfigurationManager());
			OntDocumentManager owlDocMgr = getCodeMetaModelConfigMgr().getJenaDocumentMgr();
			OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
			if (modelFolderPathname != null) { // && !modelFolderPathname.startsWith(SYNTHETIC_FROM_TEST)) {
				File mff = new File(modelFolderPathname);
				mff.mkdirs();
				spec.setImportModelGetter(new SadlJenaModelGetterPutter(spec, modelFolderPathname));
			}
			if (owlDocMgr != null) {
				spec.setDocumentManager(owlDocMgr);
				owlDocMgr.setProcessImports(true);
			}
			getCurationMgr().getExtractionProcessor().setCodeModel(ModelFactory.createOntologyModel(spec));	
			setCodeModel(getCurationMgr().getExtractionProcessor().getCodeModel());
			getCodeModel().setNsPrefix(getCodeModelPrefix(), getCodeModelNamespace());
			Ontology modelOntology = getCodeModel().createOntology(getCodeModelName());
			logger.debug("Ontology '" + getCodeModelName() + "' created");
			modelOntology.addComment("This ontology was created by extraction from code (JavaModelExtractorJP).", "en");
			setCodeMetaModelUri("http://sadl.org/CodeModel.sadl");
			OntModel importedOntModel = getCodeMetaModelConfigMgr().getOntModel(getCodeMetaModelUri(), Scope.INCLUDEIMPORTS);
			addImportToJenaModel(getCodeModelName(), getCodeMetaModelUri(), "codemdl", importedOntModel);
		}
		else {
			setCodeModel(getCurationMgr().getExtractionProcessor().getCodeModel());
		}
		
		parse(getCurationMgr().getOwlModelsFolder(), content);
		// Create a Reasoner and reason over the model, getting resulting InfModel
		// get deductions from InfModel, add to code model.
		getCodeModel().write(System.out, "RDF/XML-ABBREV");
		return true;
	}
	
	//use ASTParse to parse string
	private void parse(String modelFolder, String str) {
		Resource resource = null;
		try {
			notifyUser(modelFolder, str);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	    initializeContent();
		
		logger.debug("***************** code to process ******************");
//		logger.debug(str);
		logger.debug("****************************************************");
		
		TextProcessor txtpr = new TextProcessor(getPreferences());
//		try {
//			String result = txtpr.process(null, null);
//			logger.debug("test of text processor service:\n" + result);
//		} catch (MalformedURLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		} catch (UnsupportedEncodingException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
        // Set up a minimal type solver that only looks at the classes used to run this sample.
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        // Configure JavaParser to use type resolution
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        JavaParser.getStaticConfiguration().setSymbolResolver(symbolSolver);

        // Parse some code
        CompilationUnit cu = JavaParser.parse(str);
        Optional<PackageDeclaration> pkg = cu.getPackageDeclaration();
        if (pkg.isPresent()) {
        	setPackageName(pkg.get().getNameAsString());
        }
        
        processBlock(cu, null);
        
//         cu.getChildNodes();
//                
//        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
//        	Individual clsInst = getOrCreateClass(cls);
//        	indentifyAndLinkContainingBlock(cls, clsInst);
//
//      		Comment clscmnt = getComment(cls);
//        	System.out.println("Class or Interface Declaration: " + cls.getNameAsString() + (clscmnt != null ? "(Comment: " + clscmnt.toString().trim() + ")" : ""));
//            cls.findAll(FieldDeclaration.class).forEach(fd -> {
//            	if (fd.getParentNode().get().equals(cls)) {
//		           	 NodeList<VariableDeclarator> vars = fd.getVariables();
//		           	 vars.forEach(var -> {
//		           		 Comment cmnt = getComment(var);
//		           	     System.out.println("   Field Declaration: " + var.getTypeAsString() + " " + var.getNameAsString() + (cmnt != null ? "(Comment: " + cmnt.toString().trim() + ")" : ""));    
//		           	 });
//            	}
//            });
//         });
 
//        cu.findAll(FieldDeclaration.class).forEach(fdecl -> {
//        	System.out.println("Field Declaration of " + fdecl.toString() + " (at " + fdecl.getRange().get().begin.line + ")");
//        	NodeList<VariableDeclarator> vars = fdecl.getVariables();
//        	vars.forEach(var -> {
//            	try {
//					Individual fdInst = getOrCreateCodeVariable(var);
//				} catch (CodeExtractionException e) {
//					// TODO Auto-generated catch block
//					e.printStackTrace();
//				}
//        	});
//        });

//        cu.findAll(VariableDeclarationExpr.class).forEach(vdecl -> {
//        	System.out.println("Variable Declaration of " + vdecl.toString() + " (at " + vdecl.getRange().get().begin.line + ")");
//        	try {
//				Individual vdInst = getOrCreateCodeVariable(vdecl);
//			} catch (CodeExtractionException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
//        });

//        cu.findAll(MethodReferenceExpr.class).forEach(mr -> {
//        	System.out.println("Method Reference: " + mr.toString() + " (at " + mr.getRange().get().begin.line + ")");
//        });
//
//        cu.findAll(MethodCallExpr.class).forEach(mc -> {
//        	System.out.println("Method Call: " + mc.toString() + " (at " + mc.getRange().get().begin.line + ")");
//        	NodeList<Expression> args = mc.getArguments();
//        	Iterator<Expression> nlitr = args.iterator();
//        	while (nlitr.hasNext()) {
//        		Expression expr = nlitr.next();
//        		System.out.println("Argument Expression: " + expr.toString() + " (type " + expr.getClass().getCanonicalName() + ")");
//        		if (expr instanceof NameExpr) {
//        			try {
//						setSetterArgument(mc, (NameExpr)expr);
//					} catch (CodeExtractionException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//        		}
//        	}
//        	
//        });

//        cu.findAll(ConditionalExpr.class).forEach(cond -> {
//        	System.out.println("Conditional expression: " + cond.toString() + " (at " + cond.getRange().get().begin.line + ")");
//        });
//
//        cu.findAll(Parameter.class).forEach(pm -> {
//        	System.out.println("Parameter: " + pm.toString() + " (at " + pm.getRange().get().begin.line + ")");
//        });
//
//        cu.findAll(ReceiverParameter.class).forEach(pm -> {
//        	System.out.println("ReceiverParameter: " + pm.toString() + " (at " + pm.getRange().get().begin.line + ")");
//        });
//
//        cu.findAll(BinaryExpr.class).forEach(bin -> {
//        	System.out.println("BinaryExpr: " + bin.toString() + " (at " + bin.getRange().get().begin.line + ")");
//        });

//        List<MethodDeclaration> methods = new ArrayList<>();
//        VoidVisitor<List<MethodDeclaration>> methodNameCollector = new MethodNameCollector();
//        methodNameCollector.visit(cu, methods);
//        methods.forEach(m -> {
//      		Comment cmnt = getComment(m);
//      		Individual methInst = getOrCreateMethod(m);
//        	indentifyAndLinkContainingBlock(m, methInst);
//        	System.out.println("Method Collected: " + m.getNameAsString() + (cmnt != null ? "(Comment: " + cmnt.toString().trim() + ")" : ""));
//        	NodeList<Parameter> args = m.getParameters();
//        	args.forEach(arg -> {
//          		Comment argcmnt = getComment(arg);
//        		System.out.println("  Argument: " + arg.getTypeAsString() + " " + arg.getNameAsString() + (argcmnt != null ? "(Comment: " + argcmnt.toString().trim() + ")" : ""));
//        	});
//        	String rt = m.getTypeAsString();
//        	System.out.println("  Returns " + ((rt != null && rt.length() > 0) ? rt : "void"));
//        	
//            // Find all the assignments:
//            m.findAll(AssignExpr.class).forEach(be -> {
//           		Expression target = be.getTarget();
//            	if (target instanceof NameExpr) {
//                  	try {
//                      	Individual nnmInst = getOrCreateCodeVariable((NameExpr) target);
//                      	createReference((NameExpr)target, nnmInst, USAGE.Defined);
//                      	nnmInst.addProperty(getVarNameProperty(), getCodeModel().createTypedLiteral(((NameExpr)target).getNameAsString()));
//					} catch (CodeExtractionException e) {
//						// TODO Auto-generated catch block
//						e.printStackTrace();
//					}
//            		int assgnLine = be.getRange().get().begin.line;
//                	Comment asscmnt = getComment(be.getParentNode().get());
//                	int cmntLine = (asscmnt != null) ? asscmnt.getRange().get().begin.line : -1;
//                	boolean asscmntoutput = false;
//                	if (asscmnt != null && cmntLine > 0 && cmntLine < assgnLine) {
//                   		System.out.println("          Assignment comment: " + asscmnt.toString().trim() + " (line " + cmntLine + ")");
//                   		asscmntoutput = true;
//                	}
//            		Expression val = be.getValue();
//	           		Comment valcmnt = getComment(val);
//            		if (val instanceof BinaryExpr) {
//            			// target is computed (output). All names in be are inputs to this BinaryExpr.
//            			List<NameExpr> rhsNames = findNamesIn((BinaryExpr) val, null);
//            			System.out.println("    Computed " + target.toString() + (rhsNames != null ? " from " + rhsNames.toString() : "") + (valcmnt != null ? "(Comment: " + valcmnt.toString().trim() + ")" : ""));
//            		}
//            		else if (val instanceof ObjectCreationExpr) {
//               			System.out.println("    Set " + target.toString() + " to new instance of " + ((ObjectCreationExpr)val).getTypeAsString() + (valcmnt != null ? "(Comment: " + valcmnt.toString().trim() + ")" : ""));
//            		}
//            		else if (val instanceof MethodCallExpr) {
//            			NodeList<Expression> callArgs = ((MethodCallExpr)val).getArguments();
//            			List<NameExpr> argNames = findNamesIn(callArgs);
//            			System.out.println("    Computed " + target.toString() + (argNames != null ? " by calling method " + ((MethodCallExpr)val).getNameAsString() + " with arguments depending on " + argNames.toString().trim() : "")
//            					+ (valcmnt != null ? "(Comment: " + valcmnt.toString() + ")" : ""));
//            		}
//            		else {
//            			System.out.println("    Set " + target.toString() + " to " + val.toString() + (valcmnt != null ? "(Comment: " + valcmnt.toString().trim() + ")" : ""));
//            		}
//                	if (asscmnt != null && !asscmntoutput) {
//                		System.out.println("          Assignment comment: " + asscmnt.toString().trim() + " (line " + cmntLine + ")");
//                	}
//            	}
////                // Find out what type it has:
////                ResolvedType resolvedType = be.calculateResolvedType();
////                System.out.println(be.toString() + " is a: " + resolvedType);
//            });
//
//        });
	}

	private void processBlock(Node blkNode, Individual containingInst) {
		List<Node> childNodes = blkNode.getChildNodes();
		for (int i = 0; i < childNodes.size(); i++) {
			Node childNode = childNodes.get(i);
			processBlockChild(childNode, containingInst, null);
		}
	}

	private void processBlockChild(Node childNode, Individual containingInst, USAGE knownUsage) {
		if (childNode instanceof ClassOrInterfaceDeclaration) {
			ClassOrInterfaceDeclaration cls = (ClassOrInterfaceDeclaration) childNode;
			Individual blkInst = getOrCreateClass(cls);
			if (containingInst != null) {
				getCodeModel().add(blkInst, getContainedInProperty(), containingInst);
			}
			processBlock(cls, blkInst);
			Comment clscmnt = getComment(cls);
			System.out.println("Class or Interface Declaration: " + cls.getNameAsString() + (clscmnt != null ? "(Comment: " + clscmnt.toString().trim() + ")" : ""));
		}
		else if (childNode instanceof FieldDeclaration) {
			FieldDeclaration fd = (FieldDeclaration) childNode;
			NodeList<VariableDeclarator> vars = fd.getVariables();
			vars.forEach(var -> {
				try {
					Individual fdInst = getOrCreateCodeVariable(var, containingInst);
				} catch (CodeExtractionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				Comment cmnt = getComment(var);
				System.out.println("   Field Declaration: " + var.getTypeAsString() + " " + var.getNameAsString() + (cmnt != null ? "(Comment: " + cmnt.toString().trim() + ")" : ""));    
			});
		}
		else if (childNode instanceof MethodDeclaration) {
			MethodDeclaration m = (MethodDeclaration) childNode;
			Comment cmnt = getComment(m);
			Individual methInst = getOrCreateMethod(m, containingInst);
			if (containingInst != null) {
				getCodeModel().add(methInst, getContainedInProperty(), containingInst);
			}
			processBlock(m, methInst);
			System.out.println("Method Collected: " + m.getNameAsString() + (cmnt != null ? "(Comment: " + cmnt.toString().trim() + ")" : ""));
			NodeList<Parameter> args = m.getParameters();
			for (int j = 0; j < args.size(); j++) {
				Parameter param = args.get(j);
				String nm = param.getNameAsString();
				findCodeVariableAndAddReference(childNode, nm, containingInst, USAGE.Defined);
			}
			String rt = m.getTypeAsString();
			System.out.println(methInst.getURI() + " returns " + ((rt != null && rt.length() > 0) ? rt : "void"));
		}
		else if (childNode instanceof MethodCallExpr) {
			MethodCallExpr mc = (MethodCallExpr)childNode;
        	NodeList<Expression> args = mc.getArguments();
        	Iterator<Expression> nlitr = args.iterator();
        	while (nlitr.hasNext()) {
        		Expression expr = nlitr.next();
        		System.out.println("Argument Expression: " + expr.toString() + " (type " + expr.getClass().getCanonicalName() + ")");
        		if (expr instanceof NameExpr) {
        			try {
						setSetterArgument(mc, (NameExpr)expr, containingInst);
					} catch (CodeExtractionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
       			processBlockChild(expr, containingInst, USAGE.Defined);
        	}
		}
		else if (childNode instanceof BlockStmt) {
			processBlock(childNode, containingInst);
		}
		else if (childNode instanceof ExpressionStmt) {
			ExpressionStmt es = (ExpressionStmt)childNode;
			Expression expr = es.getExpression();
			if (expr instanceof AssignExpr) {
				processBlockChild(expr, containingInst, knownUsage);
			}
			else {
				processBlock(expr, containingInst);
			}
		}
		else if (childNode instanceof VariableDeclarationExpr) {
			VariableDeclarationExpr vdecl = (VariableDeclarationExpr)childNode; 
			try {
				Individual vdInst = getOrCreateCodeVariable(vdecl, containingInst);
			} catch (CodeExtractionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else if (childNode instanceof AssignExpr) {
			AssignExpr ass = (AssignExpr)childNode;
			List<Node> assChildren = ass.getChildNodes();
			for (int j = 01; j < assChildren.size(); j++) {
				processBlockChild(assChildren.get(j), containingInst, (j == 0 ? USAGE.Reassigned : USAGE.Used));					
			}
		}
		else if (childNode instanceof BinaryExpr) {
			processBlock(childNode, containingInst);
		}
		else if (childNode instanceof NameExpr) {
			String nm = ((NameExpr)childNode).getNameAsString();
			findCodeVariableAndAddReference(childNode, nm, containingInst, knownUsage);
		}
		else {
			System.err.println("Block child unhandled Node '" + childNode.toString().trim() + "' of type " + childNode.getClass().getCanonicalName());
		}
	}

	private void findCodeVariableAndAddReference(Node childNode, String nm, Individual containingInst, USAGE usage) {
		String nnm = containingInst.getLocalName() + "." + nm;
		Individual varInst = findDefinedVariable(nm, containingInst); //
		if (varInst != null) {
			try {
				createReference(childNode, varInst, containingInst, usage != null ? usage : USAGE.Used);
			} catch (CodeExtractionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			System.err.println("NameExpr (" + nnm + ") not found; it should already exist!");
		}
	}

	private Individual findDefinedVariable(String nm, Individual containingInst) {
		Individual inst = containingInst;
		String nnm = getCodeModelNamespace() + inst.getLocalName() + "." + nm;
		Individual varInst = getCodeModel().getIndividual(nnm);
		if (varInst == null) {
			RDFNode obj = containingInst.getPropertyValue(getContainedInProperty());
			if (obj != null && obj.isURIResource() && obj.canAs(Individual.class)) {
				return findDefinedVariable(nm, obj.as(Individual.class));
			}
		}
		return varInst;
	}

	private void setSetterArgument(MethodCallExpr mc, NameExpr arg, Individual containingInst) throws CodeExtractionException {
		boolean isArgToSetter = false;
		if (mc.getNameAsString().startsWith("set") || mc.getNameAsString().contains(".set")) {
			isArgToSetter = true;
		}
		else {
			Optional<Node> parent = mc.getParentNode();
			while (parent.isPresent()) {
				if (parent.get() instanceof MethodCallExpr) {
					if (((MethodCallExpr)parent.get()).getNameAsString().startsWith("set") ||
							((MethodCallExpr)parent.get()).getNameAsString().startsWith(".set")) {
						isArgToSetter = true;
						break;
					}
				}
				parent = parent.get().getParentNode();
			}	
		}
		if (isArgToSetter) {
			Individual cvInst = getOrCreateCodeVariable(arg, containingInst);
			cvInst.setPropertyValue(getSetterArgumentProperty(), getCodeModel().createTypedLiteral(true));
		}
	}

	private void createReference(Node varNode, Individual codeVarInst, Individual containingInst, USAGE usage) throws CodeExtractionException {
		int line = varNode.getRange().get().begin.line;
//		NodeWithSimpleName<?> codeBlockNode = getContainingCodeBlockNode(varNode);
//		Individual blkInst = null;
//		if (codeBlockNode != null) {
//			blkInst = getCodeModel().getIndividual(getCodeModelNamespace() + codeBlockNode.getNameAsString());
//		}
		Individual ref = createNewReference(containingInst, line, usage);
		codeVarInst.addProperty(getReferenceProperty(), ref);
	}

	private Individual createNewReference(Individual blkInst, int line, USAGE usage) throws CodeExtractionException {
		Individual refInst = getCodeModel().createIndividual(getReferenceClass());
		if (blkInst != null) {
			refInst.addProperty(getCodeBlockProperty(), blkInst);
		}
		if (line >= 0) {
			refInst.addProperty(getLocationProperty(), getCodeModel().createTypedLiteral(line));
		}
		refInst.addProperty(getUsageProperty(), getUsageInstance(usage));
		return refInst;
	}

	private NodeWithSimpleName<?> getContainingCodeBlockNode(Node node) {
		NodeWithSimpleName<?> containingNode = null;
		Optional<Node> parent = node.getParentNode();
		while (parent.isPresent()) {
			Node n = parent.get();
			if (n instanceof ClassOrInterfaceDeclaration) {
				containingNode = (ClassOrInterfaceDeclaration)n;
				break;
			}
			else if (n instanceof MethodDeclaration) {
				containingNode = (MethodDeclaration)n;
				break;
			}
			parent = parent.get().getParentNode();
		}
		return containingNode;
	}

	private void indentifyAndLinkContainingBlock(BodyDeclaration<?> blkNode, Individual blkInst) {
		Optional<Node> pn = blkNode.getParentNode();
		if (pn.isPresent()) {
			Node n = pn.get();
			Individual containingInst = getOrCreateBlockInstance(n);
			if (containingInst != null) {
				getCodeModel().add(blkInst, getContainedInProperty(), containingInst);
			}
		}
	}

	private Individual getOrCreateMethod(MethodDeclaration m, Individual containingInst) {
		Individual methInst = null;
    	String nm = m.getNameAsString();
    	String nnm = containingInst.getLocalName() + "." + nm;
    	methInst = getCodeModel().getIndividual(getCodeModelNamespace() + nnm);
    	if (methInst == null) {
    		methInst = getCodeModel().createIndividual(getCodeModelNamespace() + nnm, getCodeBlockMethodClass());
    	}
		return methInst;
	}

	private Individual getOrCreateCodeVariable(Node varNode, Individual containingInst) throws CodeExtractionException {
		Individual cvInst = null;
		OntClass codeVarClass = null;
		String origName = null;
		String nnm = null;
		if (varNode instanceof VariableDeclarator) {
			origName = ((VariableDeclarator)varNode).getNameAsString();
			if (containingInst != null) {
				nnm = containingInst.getLocalName() + "." + origName;
			}
			else {
				nnm = origName;
			}
//			nnm = constructCodeVariableUri(varNode, origName);
			codeVarClass = getClassFieldClass();
		}
		else if (varNode instanceof NameExpr) {
			origName = ((NameExpr)varNode).getNameAsString();
			if (containingInst != null) {
				nnm = containingInst.getLocalName() + "." + origName;
			}
			else {
				nnm = origName;
			}
//			nnm = constructCodeVariableUri(varNode, origName);
			codeVarClass = getMethodArgumentClass();
		}
		else if (varNode instanceof VariableDeclarationExpr) {
			NodeList<VariableDeclarator> vars = ((VariableDeclarationExpr)varNode).getVariables();
			for (int i = 0; i < vars.size(); i++) {
				if (i > 0) {
					System.err.println("Multiple vars (" + varNode.toString() + ") in VariableDeclarationExpr not current handled");
				}
				origName = vars.get(i).getNameAsString();
				if (containingInst != null) {
					nnm = containingInst.getLocalName() + "." + origName;
				}
				else {
					nnm = origName;
				}
//				nnm = constructCodeVariableUri(vars.get(i), origName);
				codeVarClass = getMethodVariableClass();
				cvInst = getOrCreateCodeVariable(varNode, origName, nnm, containingInst, codeVarClass);
			}
		}
		else {
			throw new CodeExtractionException("Unexpected CodeVariable varNode type: " + varNode.getClass().getCanonicalName());
		}
		cvInst = getOrCreateCodeVariable(varNode, origName, nnm, containingInst, codeVarClass);
		return cvInst;
	}

	private Individual getOrCreateCodeVariable(Node varNode, String origName, String nnm, Individual containingInst,
			OntClass codeVarClass) throws CodeExtractionException {
		Individual cvInst;
		cvInst = getCodeModel().getIndividual(getCodeModelNamespace() + nnm);
		if (cvInst == null && codeVarClass != null) {
			cvInst = getCodeModel().createIndividual(getCodeModelNamespace() + nnm, codeVarClass);
          	createReference(varNode, cvInst, containingInst, USAGE.Defined);
          	cvInst.addProperty(getVarNameProperty(), getCodeModel().createTypedLiteral(origName));
          	// TODO add varType
		}
		return cvInst;
	}

	private String constructCodeVariableUri(Node varNode, String nm) {
		StringBuilder sb = new StringBuilder();
		//return ((VariableDeclarator)varNode).getNameAsString();
		Optional<Node> parent = varNode.getParentNode();
		while (parent.isPresent()) {
			if (parent.get() instanceof MethodDeclaration) {
				sb.insert(0,((MethodDeclaration)parent.get()).getNameAsString() + ".");
			}
//			else if (parent.get() instanceof FieldDeclaration) {
//				
//			}
			else if (parent.get() instanceof ClassOrInterfaceDeclaration) {
				sb.insert(0, ((ClassOrInterfaceDeclaration)parent.get()).getNameAsString() + ".");
			}
			else {
				System.out.println("Variable " + nm + " parent type: " + parent.get().getClass().getCanonicalName());
			}
			parent = parent.get().getParentNode();
			if (parent.get() instanceof CompilationUnit) {
				break;
			}
		}
		sb.append(nm);
		return sb.toString();
	}

	private Individual getOrCreateClass(ClassOrInterfaceDeclaration cls) {
		Individual clsInst = null;
    	String nm = cls.getNameAsString();
    	clsInst = getCodeModel().getIndividual(getCodeModelNamespace() + nm);
    	if (clsInst == null) {
    		clsInst = getCodeModel().createIndividual(getCodeModelNamespace() + nm, getCodeBlockClassClass());
    	}
    	return clsInst;
	}

	private Individual getOrCreateBlockInstance(Node n) {
		Individual containingInst = null;
		if (n instanceof ClassOrInterfaceDeclaration) {
			containingInst = getCodeModel().getIndividual(getCodeModelNamespace() + ((ClassOrInterfaceDeclaration)n).getNameAsString());
			if (containingInst == null) {
				containingInst = getCodeModel().createIndividual(getCodeModelNamespace() + ((ClassOrInterfaceDeclaration)n).getNameAsString(), getCodeBlockClassClass());	
			}
		}
		else if (n instanceof MethodDeclaration) {
			containingInst = getCodeModel().getIndividual(getCodeModelNamespace() + ((MethodDeclaration)n).getNameAsString());
			if (containingInst == null) {
				containingInst = getCodeModel().createIndividual(getCodeModelNamespace() + ((MethodDeclaration)n).getNameAsString(), getCodeBlockClassClass());	
			}
		}
		return containingInst;
	}

	private Individual getUsageInstance(USAGE usage) throws CodeExtractionException {
		if (usage.equals(USAGE.Defined)) {
			return getCodeModel().getIndividual(getCodeMetaModelUri() + "#Defined");
		}
		else if (usage.equals(USAGE.Used)) {
			return getCodeModel().getIndividual(getCodeMetaModelUri() + "#Used");
		}
		else if (usage.equals(USAGE.Reassigned)) {
			return getCodeModel().getIndividual(getCodeMetaModelUri() + "#Reassigned");
		}
		throw new CodeExtractionException("Unexpected USAGE: " + usage.toString());
	}

	private Property getSetterArgumentProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#setterArgument");
	}

	private Property getUsageProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#usage");
	}

	private Property getLocationProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#location");
	}

	private Property getVarNameProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#varName");
	}

	private Property getVarTypeProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#varType");
	}

	private Property getCodeBlockProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#codeBlock");
	}

	private Property getReferenceProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#reference");
	}

	private Property getContainedInProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#containedIn");
	}

	private com.hp.hpl.jena.rdf.model.Resource getReferenceClass() {
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#Reference");
	}

//	private com.hp.hpl.jena.rdf.model.Resource getCodeVariableClass() {
//		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#CodeVariable");
//	}

	private com.hp.hpl.jena.rdf.model.Resource getCodeBlockMethodClass() {
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#Method");
	}

	private com.hp.hpl.jena.rdf.model.Resource getCodeBlockClassClass() {
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#Class");
	}

	private OntClass getMethodArgumentClass() {
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#MethodArgument");
	}

	private OntClass getClassFieldClass() {
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#ClassField");
	}

	private OntClass getMethodVariableClass() {
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#MethodVariable");
	}

	private void notifyUser(String modelFolder, String str) throws ConfigurationException {
		final String format = ConfigurationManager.RDF_XML_ABBREV_FORMAT;
		IConfigurationManagerForIDE configMgr = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(modelFolder, format);
		Object dap = configMgr.getPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER);
		if (dap != null) {
			// talk to the user via the Dialog editor
			Method acmic = null;
			try {
				acmic = dap.getClass().getMethod("addCurationManagerInitiatedContent", String.class);
			} catch (NoSuchMethodException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			} catch (SecurityException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			Method[] dapMethods = dap.getClass().getDeclaredMethods();
			if (dapMethods != null) {
				for (Method m : dapMethods) {
					if (m.getName().equals("addCurationManagerInitiatedContent")) {
						acmic = m;
						break;
					}
				}
			}
			if (acmic != null) {
				acmic.setAccessible(true);
				try {
					acmic.invoke(dap, str);
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

	private Comment getComment(Node node) {
		if (node != null) {
			Optional<Comment> cmnt = node.getComment();
			if (cmnt.isPresent()) {
				return node.getComment().get();
			}
		}
		return null;
	}

	private List<NameExpr> findNamesIn(NodeList<Expression> callArgs) {
		List<NameExpr> names = new ArrayList<NameExpr>();
		callArgs.forEach(arg -> {
			if (arg instanceof NameExpr) {
				names.add((NameExpr) arg);
			}
			else if (arg instanceof BinaryExpr) {
				findNamesIn((BinaryExpr)arg, names);
			}
		});
		return names.size() > 0 ? names : null;
	}

	private List<NameExpr> findNamesIn(BinaryExpr be, List<NameExpr> names) {
		if (be.getLeft() instanceof NameExpr) {
			if (names == null) names = new ArrayList<NameExpr>();
			names.add((NameExpr) be.getLeft());
		}
		else if (be.getLeft() instanceof BinaryExpr) {
			names = findNamesIn((BinaryExpr)be.getLeft(), names);
		}
		if (be.getRight() instanceof NameExpr) {
			if (names == null) names = new ArrayList<NameExpr>();
			names.add((NameExpr) be.getRight());
		}
		else if (be.getRight() instanceof BinaryExpr) {
			names = findNamesIn((BinaryExpr)be.getRight(), names);
		}
		return names;
	}

	public String getPackageName() {
		return packageName;
	}

	private void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Set<String> getNames() {
		return names;
	}

	void addName(String name) {
		if (!this.names.contains(name)) {
			this.names.add(name);
		}
	}
	
	public Map<String, String> getClassDeclarations() {
		return classDeclarations;
	}

	public void setClassDeclarations(Map<String, String> classDeclarations) {
		this.classDeclarations = classDeclarations;
	}
	
	public List<SadlMethod> getMethods() {
		return methods;
	}

	private void addMethod(SadlMethod mdecl) {
		this.methods.add(mdecl);
	}

	public List<Comment> getComments() {
		return comments;
	}

	private void addComment(Comment comment) {
		this.comments.add(comment);
	}

	@Override
	public String getType() {
		return type;
	}

	private void setType(String type) {
		this.type = type;
	}

	@Override
	public String getTypeComment() {
		return typeComment;
	}

	private void setTypeComment(String typeComment) {
		this.typeComment = typeComment;
	}

	@Override
	public Map<String, Tag> getTagMap() {
		return tagMap;
	}

	private void addTagToMap(String name, Tag tag) {
		if (tagMap == null) {
			tagMap = new HashMap<String, Tag>();
		}
		this.tagMap.put(name,  tag);
	}

	private CONTEXT getCurrentContext() {
		return currentContext;
	}

	private CONTEXT setCurrentContext(CONTEXT currentContext) {
		CONTEXT prev = this.currentContext;
		this.currentContext = currentContext;
		return prev;
	}

	public Map<String, String> getPreferences() {
		return preferences;
	}
	
	public String getPreference(String key) {
		if (preferences != null) {
			return preferences.get(key);
		}
		return null;
	}

	private void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}

	private AnswerCurationManager getCurationMgr() {
		return curationMgr;
	}

	private void setCurationMgr(AnswerCurationManager curationMgr) {
		this.curationMgr = curationMgr;
	}

	@Override
	public void addCodeFiles(List<File> javaFiles) {
		if (codeFiles != null) {
			codeFiles.addAll(javaFiles);
		}
		else {
			setCodeFiles(javaFiles);
		}
	}

	public List<File> getCodeFiles() {
		return codeFiles;
	}

	public void setCodeFiles(List<File> codeFiles) {
		this.codeFiles = codeFiles;
	}

	private OntModel getCodeModel() {
		return codeModel;
	}

	private void setCodeModel(OntModel codeModel) {
		this.codeModel = codeModel;
	}

	private String getCodeModelName() {
		return codeModelName;
	}

	private void setCodeModelName(String codeModelName) {
		this.codeModelName = codeModelName;
		getCurationMgr().getExtractionProcessor().setCodeModelName(codeModelName);
	}
	
	private String getCodeModelNamespace() {
		return codeModelName + "#";
	}

	private String getCodeModelPrefix() {
		return codeModelPrefix;
	}

	private void setCodeModelPrefix(String codeModelPrefix) {
		this.codeModelPrefix = codeModelPrefix;
	}

	private void addImportToJenaModel(String modelName, String importUri, String importPrefix, Model importedOntModel) {
		getCodeModel().getDocumentManager().addModel(importUri, importedOntModel, true);
		Ontology modelOntology = getCodeModel().createOntology(modelName);
		if (importPrefix != null) {
			getCodeModel().setNsPrefix(importPrefix, importUri);
		}
		com.hp.hpl.jena.rdf.model.Resource importedOntology = getCodeModel().createResource(importUri);
		modelOntology.addImport(importedOntology);
		getCodeModel().addSubModel(importedOntModel);
		getCodeModel().addLoadedImport(importUri);
	}

	private String getCodeMetaModelUri() {
		return codeMetaModelUri;
	}

	private void setCodeMetaModelUri(String codeMetaModelUri) {
		this.codeMetaModelUri = codeMetaModelUri;
	}

	private IConfigurationManagerForIDE getCodeMetaModelConfigMgr() {
		return codeMetaModelConfigMgr;
	}

	private void setCodeMetaModelConfigMgr(IConfigurationManagerForIDE codeMetaModelConfigMgr) {
		this.codeMetaModelConfigMgr = codeMetaModelConfigMgr;
	}

}

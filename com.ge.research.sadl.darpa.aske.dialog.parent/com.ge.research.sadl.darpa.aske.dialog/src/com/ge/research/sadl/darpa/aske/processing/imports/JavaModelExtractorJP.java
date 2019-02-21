package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
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
import com.github.javaparser.ast.body.BodyDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.nodeTypes.NodeWithSimpleName;
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;

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
		logger.debug(str);
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
        
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
        	String nm = cls.getNameAsString();
        	Individual clsInst = getCodeModel().createIndividual(getCodeModelNamespace() + nm, getCodeBlockClassClass());
        	
        	indentifyAndLinkContainingBlock(cls, clsInst);

      		Comment clscmnt = getComment(cls);
        	System.out.println("Class or Interface Declaration: " + nm + (clscmnt != null ? "(Comment: " + clscmnt.toString().trim() + ")" : ""));
            cls.findAll(FieldDeclaration.class).forEach(fd -> {
            	if (fd.getParentNode().get().equals(cls)) {
		           	 NodeList<VariableDeclarator> vars = fd.getVariables();
		           	 vars.forEach(var -> {
		           		 Comment cmnt = getComment(var);
		           	     System.out.println("   Field Declaration: " + var.getTypeAsString() + " " + var.getNameAsString() + (cmnt != null ? "(Comment: " + cmnt.toString().trim() + ")" : ""));       		 
		           	 });
            	}
            });
         });
        
        List<MethodDeclaration> methods = new ArrayList<>();
        VoidVisitor<List<MethodDeclaration>> methodNameCollector = new MethodNameCollector();
        methodNameCollector.visit(cu, methods);
        methods.forEach(m -> {
      		Comment cmnt = getComment(m);
        	String nm = m.getNameAsString();
        	Individual methInst = getCodeModel().createIndividual(getCodeModelNamespace() + nm, getCodeBlockMethodClass());
        	indentifyAndLinkContainingBlock(m, methInst);
        	System.out.println("Method Collected: " + nm + (cmnt != null ? "(Comment: " + cmnt.toString().trim() + ")" : ""));
        	NodeList<Parameter> args = m.getParameters();
        	args.forEach(arg -> {
          		Comment argcmnt = getComment(arg);
        		System.out.println("  Argument: " + arg.getTypeAsString() + " " + arg.getNameAsString() + (argcmnt != null ? "(Comment: " + argcmnt.toString().trim() + ")" : ""));
        	});
        	String rt = m.getTypeAsString();
        	System.out.println("  Returns " + ((rt != null && rt.length() > 0) ? rt : "void"));
        	
            // Find all the assignments:
            m.findAll(AssignExpr.class).forEach(be -> {
           		Expression target = be.getTarget();
            	if (target instanceof NameExpr) {
            		String nnm = ((NameExpr)target).getNameAsString();
                	Individual nnmInst = getCodeModel().createIndividual(getCodeModelNamespace() + nnm, getCodeVariableClass());
                	try {
						createReference((NameExpr)target, nnmInst, USAGE.Defined);
					} catch (CodeExtractionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
            		int assgnLine = be.getRange().get().begin.line;
                	Comment asscmnt = getComment(be.getParentNode().get());
                	int cmntLine = (asscmnt != null) ? asscmnt.getRange().get().begin.line : -1;
                	boolean asscmntoutput = false;
                	if (asscmnt != null && cmntLine > 0 && cmntLine < assgnLine) {
                   		System.out.println("          Assignment comment: " + asscmnt.toString().trim() + " (line " + cmntLine + ")");
                   		asscmntoutput = true;
                	}
            		Expression val = be.getValue();
	           		Comment valcmnt = getComment(val);
            		if (val instanceof BinaryExpr) {
            			// target is computed (output). All names in be are inputs to this BinaryExpr.
            			List<NameExpr> rhsNames = findNamesIn((BinaryExpr) val, null);
            			System.out.println("    Computed " + target.toString() + (rhsNames != null ? " from " + rhsNames.toString() : "") + (valcmnt != null ? "(Comment: " + valcmnt.toString().trim() + ")" : ""));
            		}
            		else if (val instanceof ObjectCreationExpr) {
               			System.out.println("    Set " + target.toString() + " to new instance of " + ((ObjectCreationExpr)val).getTypeAsString() + (valcmnt != null ? "(Comment: " + valcmnt.toString().trim() + ")" : ""));
            		}
            		else if (val instanceof MethodCallExpr) {
            			NodeList<Expression> callArgs = ((MethodCallExpr)val).getArguments();
            			List<NameExpr> argNames = findNamesIn(callArgs);
            			System.out.println("    Computed " + target.toString() + (argNames != null ? " by calling method " + ((MethodCallExpr)val).getNameAsString() + " with arguments depending on " + argNames.toString().trim() : "")
            					+ (valcmnt != null ? "(Comment: " + valcmnt.toString() + ")" : ""));
            		}
            		else {
            			System.out.println("    Set " + target.toString() + " to " + val.toString() + (valcmnt != null ? "(Comment: " + valcmnt.toString().trim() + ")" : ""));
            		}
                	if (asscmnt != null && !asscmntoutput) {
                		System.out.println("          Assignment comment: " + asscmnt.toString().trim() + " (line " + cmntLine + ")");
                	}
            	}
//                // Find out what type it has:
//                ResolvedType resolvedType = be.calculateResolvedType();
//                System.out.println(be.toString() + " is a: " + resolvedType);
            });

        });
	}

	private void createReference(NameExpr codeVar, Individual codeVarInst, USAGE usage) throws CodeExtractionException {
		int line = codeVar.getRange().get().begin.line;
		NodeWithSimpleName<?> codeBlockNode = getContainingCodeBlockNode(codeVar);
		Individual blkInst = null;
		if (codeBlockNode != null) {
			blkInst = getCodeModel().getIndividual(getCodeModelNamespace() + codeBlockNode.getNameAsString());
		}
		Individual ref = createNewReference(blkInst, line, usage);
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
		if (parent.isPresent()) {
			Node n = parent.get();
			if (n instanceof ClassOrInterfaceDeclaration) {
				containingNode = (ClassOrInterfaceDeclaration)n;
			}
			else if (n instanceof MethodDeclaration) {
				containingNode = (MethodDeclaration)n;
			}
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

	private Property getUsageProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#usage");
	}

	private Property getLocationProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#location");
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

	private com.hp.hpl.jena.rdf.model.Resource getCodeVariableClass() {
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#CodeVariable");
	}

	private com.hp.hpl.jena.rdf.model.Resource getCodeBlockMethodClass() {
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#Method");
	}

	private com.hp.hpl.jena.rdf.model.Resource getCodeBlockClassClass() {
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#Class");
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

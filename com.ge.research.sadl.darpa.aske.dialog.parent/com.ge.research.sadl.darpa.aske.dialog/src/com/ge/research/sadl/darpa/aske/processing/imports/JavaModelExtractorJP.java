package com.ge.research.sadl.darpa.aske.processing.imports;

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
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.imports.SadlModelGenerator.SadlMethod;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.ConfigurationManager;
import com.ge.research.sadl.utils.ResourceManager;
import com.github.javaparser.JavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
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
import com.github.javaparser.ast.visitor.VoidVisitor;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;
import com.github.javaparser.resolution.types.ResolvedType;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;

public class JavaModelExtractorJP {
    private static final Logger logger = Logger.getLogger (JavaModelExtractorJP.class) ;
	private SadlModelGenerator smg = null;
	private String packageName = "";
	private String type = null;
	private String typeComment = null;
	private List<SadlMethod> methods = new ArrayList<SadlMethod>();
	private Set<String> names = new HashSet<String>();
	private Map<String, String> classDeclarations = new HashMap<String, String>();
	private List<Comment> comments = new ArrayList<Comment>();
	private Map<String, Tag> tagMap = new HashMap<String, Tag>();;
	
	public enum CONTEXT {PackageDecl, MainClassDecl, InnerClassDecl, ConstructorDecl, MethodDecl, Expression,
		Block, MethodBody}
	private CONTEXT currentContext = null;
	
	public class Tag {
		private String name;
		private String text;
		
		public Tag(String n, String t) {
			setName(n); 
			setText(t);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getText() {
			return text;
		}

		public void setText(String text) {
			this.text = text;
		}
	}
	
	private static class MethodNameCollector extends VoidVisitorAdapter<List<MethodDeclaration>> {	
		@Override
		public void visit(MethodDeclaration md, List<MethodDeclaration> collector) {
			super.visit(md,  collector);
			collector.add(md);
		}
	}
	
	
	public JavaModelExtractorJP(SadlModelGenerator gen) {
		smg = gen;
	    logger.setLevel(Level.ALL);
	}
	
	private void initializeContent() {
		packageName = "";
		type = null;
		
		methods.clear();
		names.clear();
		getComments().clear();
	}

	//use ASTParse to parse string
	public void parse(String modelFolder, String str) {
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
		
        // Set up a minimal type solver that only looks at the classes used to run this sample.
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        // Configure JavaParser to use type resolution
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        JavaParser.getStaticConfiguration().setSymbolResolver(symbolSolver);

        // Parse some code
        CompilationUnit cu = JavaParser.parse(str);
        
        cu.findAll(ClassOrInterfaceDeclaration.class).forEach(cls -> {
      		Comment clscmnt = getComment(cls);
        	System.out.println("Class or Interface Declaration: " + cls.getNameAsString() + (clscmnt != null ? "(Comment: " + clscmnt.toString().trim() + ")" : ""));
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
        	System.out.println("Method Collected: " + m.getNameAsString() + (cmnt != null ? "(Comment: " + cmnt.toString().trim() + ")" : ""));
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

	public String getType() {
		return type;
	}

	private void setType(String type) {
		this.type = type;
	}

	public String getTypeComment() {
		return typeComment;
	}

	private void setTypeComment(String typeComment) {
		this.typeComment = typeComment;
	}

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
}

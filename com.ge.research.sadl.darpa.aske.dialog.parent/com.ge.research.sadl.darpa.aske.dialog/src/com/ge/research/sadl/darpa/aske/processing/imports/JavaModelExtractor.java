package com.ge.research.sadl.darpa.aske.processing.imports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.compiler.IProblem;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Statement;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.ge.research.sadl.darpa.aske.processing.imports.SadlModelGenerator.SadlMethod;
import com.ge.research.sadl.darpa.aske.processing.imports.SadlModelGenerator.SadlMethodArgument;

public class JavaModelExtractor {
    private static final Logger logger = Logger.getLogger (JavaModelExtractor.class) ;
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
	
	public JavaModelExtractor(SadlModelGenerator gen) {
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
	public void parse(IJavaProject jprj, String str) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(str.toCharArray());
		parser.setResolveBindings(true);
		parser.setBindingsRecovery(true);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
	    Hashtable<String, String> options = JavaCore.getDefaultOptions();
	    options.put(JavaCore.COMPILER_SOURCE, JavaCore.VERSION_1_7);
	    parser.setCompilerOptions(options);
//	    parser.setEnvironment(null, null, new String[] { "UTF-8", "UTF-8" }, true);
//		parser.setEnvironment(null, null, null, true);
//	    IWorkspaceRoot workspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
//	    IProject[] projects = workspaceRoot.getProjects();
//	    for (IProject project : projects) {
//	    	IJavaProject proj = JavaCore.create(project);
	    if (jprj != null) {
	    	parser.setProject(jprj);
	    }
//	    }

	    initializeContent();
		
		logger.debug("***************** code to process ******************");
		logger.debug(str);
		logger.debug("****************************************************");
		
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
	    IProblem[] problems = cu.getProblems();
	    if (problems != null && problems.length > 0) {
	        logger.debug("Got {} problems compiling the source file: ");
	        for (IProblem problem : problems) {
	        	logger.debug(problem.toString());
	        }
	    } 
		if (!cu.getAST().hasResolvedBindings()) {
			logger.debug("Bindings will not be resolved.");
		}
 
		cu.accept(new ASTVisitor() {
			
			public boolean visit(LineComment lcomment) {
				logger.debug("Line Comment: " + lcomment.toString());
				return true;
			}
			
			public boolean visit(BlockComment bcomment) {
				logger.debug("Block Comment: " + bcomment.toString());
				return true;
			}

			public boolean visit(PackageDeclaration pkgdecl) {
				CONTEXT savedContext = setCurrentContext(CONTEXT.PackageDecl);
				logger.debug("Package: " + pkgdecl.getName().toString() + " (at " + pkgdecl.getStartPosition() + ")");
				setPackageName(pkgdecl.getName().toString());
				considerJavaDoc(pkgdecl, pkgdecl.getName().toString(), pkgdecl.getJavadoc());
				setCurrentContext(savedContext);
				return true;
			}
 
			public boolean visit(TypeDeclaration typdecl) {
				CONTEXT savedContext;
//				logger.debug("Type: " + typdecl.toString() + " (at " + typdecl.getStartPosition() + ")");
				logger.debug("Type: " + typdecl.getName().toString() + " (at " + typdecl.getStartPosition() + ")");
				if (typdecl.getParent() instanceof CompilationUnit) {
					savedContext = setCurrentContext(CONTEXT.MainClassDecl);
					setType(typdecl.getName().toString());
				}
				else {
					savedContext = setCurrentContext(CONTEXT.InnerClassDecl);
				}
				Type supertype = typdecl.getSuperclassType();
				addClassDeclaration(typdecl);
				String tcomment = considerJavaDoc(typdecl, typdecl.getName().toString(), typdecl.getJavadoc());
				if (tcomment != null) {
					setTypeComment(tcomment);
				}
				setCurrentContext(savedContext);
				return true;
			}
			
			public boolean visit(MethodDeclaration methdecl) {
				CONTEXT savedContext;
				if (methdecl.isConstructor()) {
					logger.debug("Method constructor: TBD");
					savedContext = setCurrentContext(CONTEXT.ConstructorDecl);
				}
				else {
					savedContext = setCurrentContext(CONTEXT.MethodDecl);
					Type rettype = methdecl.getReturnType2();
					String typeqn = getQualifiedName(rettype);
					SadlMethod mdecl = smg.new SadlMethod(methdecl.getName().toString(), typeqn);
					List<SingleVariableDeclaration> params = methdecl.parameters();
					StringBuilder paramsb = new StringBuilder("(");
					for (SingleVariableDeclaration svd : params) {
						if (paramsb.length() > 1) paramsb.append(",");
						paramsb.append(svd.toString());
						SadlMethodArgument marg = smg.new SadlMethodArgument(svd.getName().toString(), getQualifiedName(svd));
						mdecl.addArgument(marg);
					}
					addMethod(mdecl);
					paramsb.append(")");
					Block bdy = methdecl.getBody();
					if (bdy != null) {
						addBody(mdecl, bdy);
					}
	//				logger.debug("Method: " + methdecl.toString() + " (at " + methdecl.getStartPosition() + ")");
					logger.debug("Method: " + (rettype != null ? rettype.toString() : "null") + " " + methdecl.getName().toString() + paramsb.toString() + " (at " + methdecl.getStartPosition() + ")");
					String comment = considerJavaDoc(methdecl, methdecl.getName().toString(), methdecl.getJavadoc());
					if (comment != null) {
						mdecl.getNotes().add(0, comment);
					}
				}
				setCurrentContext(savedContext);
				return true;
			}
			
			public boolean visit(ExpressionStatement exprstmt) {
				CONTEXT savedContext = setCurrentContext(CONTEXT.Expression);
				logger.debug("Expression: " + exprstmt.toString() + " (at " + exprstmt.getStartPosition() + ")");
				setCurrentContext(savedContext);
				return true;
			}
			
//			public boolean visit(ReturnStatement retstmt) {
//				logger.debug("Return: " + retstmt.toString() + " (at " + retstmt.getStartPosition() + ")");
//				return true;
//			}
//			
//			public boolean visit(SingleVariableDeclaration svdecl) {
//				logger.debug("SingleVariableDeclaration: " + svdecl.toString() + " (at " + svdecl.getStartPosition() + ")");
//				return true;
//			}
			
			public boolean visit(VariableDeclarationFragment node) {
				SimpleName name = node.getName();
				addName(name.getIdentifier());
				logger.debug("Declaration of '" + name + "' at line"
						+ cu.getLineNumber(name.getStartPosition()));
				return true; // false; // do not continue 
			}
			
			public boolean visit(SimpleName node) {
				if (getNames().contains(node.getIdentifier())) {
					logger.debug("Usage of '" + node + "' at line "
							+ cu.getLineNumber(node.getStartPosition()));
				}
				else {
					logger.debug("New name '" + node + "' at line " 
							+ cu.getLineNumber(node.getStartPosition()));
				}
				return true;
			}
			
			public boolean visit(ASTNode node) {
//				super.visit(node);
				logger.debug("generic visit: " + node.toString());
				return true;
			}
		});
 
		List<Comment> comments = cu.getCommentList();
		logger.debug("\nAdditional comments:");
		for (Comment c : comments) {
			if (!getComments().contains(c)) {
				int sp = c.getStartPosition();
				int ep = sp + c.getLength();
				if (c.isLineComment()) {
					logger.debug("  Line Comment: " + c.toString() + " (from " + sp + " to " + ep + ")");
				}
				else if (c.isBlockComment()) {
					String bcmmt = str.substring(c.getStartPosition(), c.getStartPosition() + c.getLength());
					logger.debug("  Block Comment: " + bcmmt + " (from " + sp + " to " + ep + ")");
				}
				else if (c.isDocComment()) {
					logger.debug("  JavaDoc Comment: " + c.toString() + " (from " + sp + " to " + ep + ")");
				}
			}
		}
	}

	private String getQualifiedName(SingleVariableDeclaration svd) {
		Type typ = svd.getType();
		return getQualifiedName(typ);
	}

	private String getQualifiedName(Type type) {
		ITypeBinding restype = type.resolveBinding();
//		IPackageBinding pkg = restype.getPackage();
//		String pkgstr = pkg.toString();
		return restype != null ? restype.getQualifiedName() : type.toString();
	}

	protected void addBody(SadlMethod mdecl, Block bdy) {
		CONTEXT savedContext = setCurrentContext(CONTEXT.MethodBody);	
		List<Object> stmts = bdy.statements();
		for (Object stmt : stmts) {
			if (stmt instanceof Statement) {
				((Statement)stmt).accept(new ASTVisitor() {
					public boolean visit (org.eclipse.jdt.core.dom.AssertStatement stmt) {
						logger.debug("AssertStatement: " + stmt.toString());
						return true;
					}
					
					public boolean visit (Block stmt) {
						logger.debug("Block: " + stmt.toString());
						return true;
					}
					
					public boolean visit (BreakStatement stmt) {
						logger.debug("BreakStatement: " + stmt.toString());
						return true;
					}
					
					public boolean visit (ConstructorInvocation stmt) {
						logger.debug("ConstructorInvocation: " + stmt.toString());
						return true;
					}
					
					public boolean visit (ContinueStatement stmt) {
						logger.debug("ContinueStatement: " + stmt.toString());
						return true;
					}
					
					public boolean visit (DoStatement stmt) {
						logger.debug("DoStatement: " + stmt.toString());
						return true;
					}
					
					public boolean visit (EmptyStatement stmt) {
						logger.debug("EmptyStatement: " + stmt.toString());
						return true;
					}
					
					public boolean visit (EnhancedForStatement stmt) {
						logger.debug("EnhancedForStatement: " + stmt.toString());
						return true;
					}
					
					public boolean visit (ExpressionStatement stmt) {
						logger.debug("ExpressionStatement: " + stmt.toString());
						return true;
					}
					
					public boolean visit (ForStatement stmt) {
						logger.debug("ForStatement: " + stmt.toString());
						return true;
					}
					
					public boolean visit (IfStatement stmt) {
						logger.debug("IfStatement: " + stmt.toString());
						return true;
					}
					
					public boolean visit (LabeledStatement stmt) {
						logger.debug("LabeledStatement: " + stmt.toString());
						return true;
					}
					
					public boolean visit(ReturnStatement stmt) {
						logger.debug("ReturnStatement: " + stmt.toString());
						stmt.getExpression();
						return true;
					}
					
					public boolean visit (SuperConstructorInvocation stmt) {
						logger.debug("SuperConstructorInvocation: " + stmt.toString());
						return true;
					}
					
					public boolean visit (SwitchStatement stmt) {
						logger.debug("SwitchStatement: " + stmt.toString());
						return true;
					}
					
					public boolean visit (SwitchCase stmt) {
						logger.debug("SwitchCase: " + stmt.toString());
						return true;
					}
					
					public boolean visit (Statement stmt) {
						logger.debug("Statement: " + stmt.toString());
						return true;
					}
					
					public boolean visit (PrefixExpression expr) {
						logger.debug("Prefix expression: " + expr);
						return true;
					}
					
					public boolean visit (PostfixExpression expr) {
						logger.debug("Postfix expression: " + expr);
						return true;
					}
					
				});
			}
		}
		setCurrentContext(savedContext);
	}

	private String considerJavaDoc(ASTNode node, String name, Javadoc javadoc) {
		String comment = null;
		if (javadoc != null) {
			List<StructuralPropertyDescriptor> descriptorList = node.structuralPropertiesForType();
		    for (StructuralPropertyDescriptor descriptor : descriptorList) {
		    	try {
			        Object child = node.getStructuralProperty(descriptor);
//			        if (child instanceof List && !((List)child).isEmpty()) {
//			            logger.debug("   List: " + ((List)child).toString());
//			        } else if (child instanceof ASTNode) {
//			            logger.debug("   ASTNode: " + ((ASTNode)child).toString());
//			        } else if (child != null) {
//			            logger.debug("   Other Child: " + child.toString());
//			        }
			        if (child instanceof Javadoc) {
//			        	logger.debug("   Javadoc: " + child.toString());
			        	addComment((Comment) child);
			        	List<TagElement> tags = ((Javadoc)child).tags();
			        	for (TagElement t : tags) {
			        		if (t.getTagName() == null || name == null) {
				        		logger.debug("    Javadoc  Comment: " + t.toString().trim());
				        		comment = t.toString().trim();
			        		}
			        		else {
			        			logger.debug("      Tag " + t.getTagName() + ": " + t.toString().trim());
			        			if (t.getTagName().equals("@return")) {
			        				List<SadlMethod> methlist = getMethods();
			        				for (SadlMethod m : methlist) {
			        					if (m.getName().equals(name)) {
			        						m.addNote("returns " + t.fragments().get(0).toString());
			        					}
			        				}
			        			}
			        			else if (t.fragments().size() > 0) {
			        				boolean done = false;
			        				if (t.fragments().size() > 1) {
			        					String frag0 = t.fragments().get(0).toString();
			        					String frag1 = t.fragments().get(1).toString();
			        					if (frag0 != null && frag1 != null) {
			        						addTagToMap(frag0,  new Tag(t.getTagName(), frag1));
			        						done = true;
			        					}
			        				}
			        				if (!done) {
			        					t.fragments().get(0);
			        					addTagToMap(name, new Tag(t.getTagName(), t.fragments().get(0).toString().trim()));
			        				}
			        			}
			        		}
			        	}
			        }
		    	}
		    	catch (Exception e) {
		    		e.printStackTrace();
		    		logger.debug("Descriptor '" + descriptor.toString() + "': " + e.getMessage());
		    	}
		    }
		}
		return comment;
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
	
	void addClassDeclaration(TypeDeclaration typdecl) {
		String key = typdecl.getName().toString();
		String supertype = (typdecl.getSuperclassType() != null) ? typdecl.getSuperclassType().toString() : null;
		if (this.getClassDeclarations().containsKey(key)) {
			String prevST = this.getClassDeclarations().get(key);
			if (prevST != null && supertype != null && !prevST.equals(supertype)) {
				logger.debug("Multiple supertypes not currently handled.");
			}
			this.getClassDeclarations().put(key, supertype);
		}
		else {
			this.getClassDeclarations().put(key, supertype);
		}
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

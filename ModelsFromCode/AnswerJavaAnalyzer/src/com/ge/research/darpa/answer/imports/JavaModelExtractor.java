package com.ge.research.darpa.answer.imports;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.Comment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;

import com.ge.research.darpa.answer.imports.SadlModelGenerator.SadlMethodArgument;
import com.ge.research.darpa.answer.imports.SadlModelGenerator.SadlMethodDeclaration;

public class JavaModelExtractor {
	private SadlModelGenerator smg = null;
	private String packageName = "";
	private String type = null;
	private String typeComment = null;
	private List<SadlMethodDeclaration> methods = new ArrayList<SadlMethodDeclaration>();
	private Set<String> names = new HashSet<String>();
	private List<Comment> comments = new ArrayList<Comment>();
	private Map<String, Tag> tagMap = new HashMap<String, Tag>();;
	
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
	}
	
	private void initializeContent() {
		packageName = "";
		type = null;
		
		methods.clear();
		names.clear();
		getComments().clear();
	}

	//use ASTParse to parse string
	public void parse(String str) {
		ASTParser parser = ASTParser.newParser(AST.JLS3);
		parser.setSource(str.toCharArray());
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
 
		initializeContent();
		
		System.out.println("***************** code to process ******************");
		System.out.println(str);
		System.out.println("****************************************************");
		
		final CompilationUnit cu = (CompilationUnit) parser.createAST(null);
 
		cu.accept(new ASTVisitor() {
			
			public boolean visit(LineComment lcomment) {
				System.out.println("Line Comment: " + lcomment.toString());
				return true;
			}
			
			public boolean visit(BlockComment bcomment) {
				System.out.println("Block Comment: " + bcomment.toString());
				return true;
			}

			public boolean visit(PackageDeclaration pkgdecl) {
				System.out.println("Package: " + pkgdecl.getName().toString() + " (at " + pkgdecl.getStartPosition() + ")");
				setPackageName(pkgdecl.getName().toString());
				considerJavaDoc(pkgdecl, pkgdecl.getName().toString(), pkgdecl.getJavadoc());
				return true;
			}
 
			public boolean visit(TypeDeclaration typdecl) {
//				System.out.println("Type: " + typdecl.toString() + " (at " + typdecl.getStartPosition() + ")");
				System.out.println("Type: " + typdecl.getName().toString() + " (at " + typdecl.getStartPosition() + ")");
				setType(typdecl.getName().toString());
				String tcomment = considerJavaDoc(typdecl, typdecl.getName().toString(), typdecl.getJavadoc());
				if (tcomment != null) {
					setTypeComment(tcomment);
				}
				return true;
			}
			
			public boolean visit(MethodDeclaration methdecl) {
				if (methdecl.isConstructor()) {
					System.err.println("Method constructor: TBD");
				}
				else {
					Type rettype = methdecl.getReturnType2();
					SadlMethodDeclaration mdecl = smg.new SadlMethodDeclaration(methdecl.getName().toString(), (rettype != null ? rettype.toString() : null));
					List<SingleVariableDeclaration> params = methdecl.parameters();
					StringBuilder paramsb = new StringBuilder("(");
					for (SingleVariableDeclaration svd : params) {
						if (paramsb.length() > 1) paramsb.append(",");
						paramsb.append(svd.toString());
						SadlMethodArgument marg = smg.new SadlMethodArgument(svd.getName().toString(), svd.getType().toString());
						mdecl.addArgument(marg);
					}
					addMethod(mdecl);
					paramsb.append(")");
	//				System.out.println("Method: " + methdecl.toString() + " (at " + methdecl.getStartPosition() + ")");
					System.out.println("Method: " + (rettype != null ? rettype.toString() : "null") + " " + methdecl.getName().toString() + paramsb.toString() + " (at " + methdecl.getStartPosition() + ")");
					String comment = considerJavaDoc(methdecl, methdecl.getName().toString(), methdecl.getJavadoc());
					if (comment != null) {
						mdecl.getNotes().add(0, comment);
					}
				}
				return true;
			}
			
			public boolean visit(ExpressionStatement exprstmt) {
				System.out.println("Expression: " + exprstmt.toString() + " (at " + exprstmt.getStartPosition() + ")");
				return true;
			}
			
//			public boolean visit(ReturnStatement retstmt) {
//				System.out.println("Return: " + retstmt.toString() + " (at " + retstmt.getStartPosition() + ")");
//				return true;
//			}
//			
//			public boolean visit(SingleVariableDeclaration svdecl) {
//				System.out.println("SingleVariableDeclaration: " + svdecl.toString() + " (at " + svdecl.getStartPosition() + ")");
//				return true;
//			}
			
			public boolean visit(VariableDeclarationFragment node) {
				SimpleName name = node.getName();
				addName(name.getIdentifier());
				System.out.println("Declaration of '" + name + "' at line"
						+ cu.getLineNumber(name.getStartPosition()));
				return true; // false; // do not continue 
			}
			
			public boolean visit(SimpleName node) {
				if (getNames().contains(node.getIdentifier())) {
					System.out.println("Usage of '" + node + "' at line "
							+ cu.getLineNumber(node.getStartPosition()));
				}
				return true;
			}
		});
 
		List<Comment> comments = cu.getCommentList();
		System.out.println("\nAdditional comments:");
		for (Comment c : comments) {
			if (!getComments().contains(c)) {
				int sp = c.getStartPosition();
				int ep = sp + c.getLength();
				if (c.isLineComment()) {
					System.out.println("  Line Comment: " + c.toString() + " (from " + sp + " to " + ep + ")");
				}
				else if (c.isBlockComment()) {
					String bcmmt = str.substring(c.getStartPosition(), c.getStartPosition() + c.getLength());
					System.out.println("  Block Comment: " + bcmmt + " (from " + sp + " to " + ep + ")");
				}
				else if (c.isDocComment()) {
					System.out.println("  JavaDoc Comment: " + c.toString() + " (from " + sp + " to " + ep + ")");
				}
			}
		}
	}

	private String considerJavaDoc(ASTNode node, String name, Javadoc javadoc) {
		String comment = null;
		if (javadoc != null) {
			List<StructuralPropertyDescriptor> descriptorList = node.structuralPropertiesForType();
		    for (StructuralPropertyDescriptor descriptor : descriptorList) {
		    	try {
			        Object child = node.getStructuralProperty(descriptor);
//			        if (child instanceof List && !((List)child).isEmpty()) {
//			            System.out.println("   List: " + ((List)child).toString());
//			        } else if (child instanceof ASTNode) {
//			            System.out.println("   ASTNode: " + ((ASTNode)child).toString());
//			        } else if (child != null) {
//			            System.out.println("   Other Child: " + child.toString());
//			        }
			        if (child instanceof Javadoc) {
//			        	System.out.println("   Javadoc: " + child.toString());
			        	addComment((Comment) child);
			        	List<TagElement> tags = ((Javadoc)child).tags();
			        	for (TagElement t : tags) {
			        		if (t.getTagName() == null || name == null) {
				        		System.out.println("    Javadoc  Comment: " + t.toString().trim());
				        		comment = t.toString().trim();
			        		}
			        		else {
			        			System.out.println("      Tag " + t.getTagName() + ": " + t.toString().trim());
			        			if (t.getTagName().equals("@return")) {
			        				List<SadlMethodDeclaration> methlist = getMethods();
			        				for (SadlMethodDeclaration m : methlist) {
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
		    		System.err.println("Descriptor '" + descriptor.toString() + "': " + e.getMessage());
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

	public List<SadlMethodDeclaration> getMethods() {
		return methods;
	}

	private void addMethod(SadlMethodDeclaration mdecl) {
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
}

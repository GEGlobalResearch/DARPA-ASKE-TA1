package com.ge.research.sadl.darpa.aske.ui.answer.imports;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.apache.log4j.Logger;

import com.ge.research.sadl.model.gp.NamedNode;

public class SadlModelGenerator {
    private static final Logger logger = Logger.getLogger (SadlModelGenerator.class) ;
	
    public class SadlOOClass {
    	private String name = null;
    	private String superClass = null;
    	private List<String> fields = null;
    	private List<SadlMethod> methods = null;
    	private List<SadlVariable> inputs = null;
    	private List<SadlVariable> outputs = null;
    	private List<SadlExpression> expressions = null;
    }
    
    public class SadlExpression {
    	private List<SadlVariable> inputs = null;
    	private List<SadlVariable> outputs = null;
    	private List<SadlExpression> priors = null;
    	private List<SadlExpression> nexts = null;
		public List<SadlVariable> getInputs() {
			return inputs;
		}
		public void setInputs(List<SadlVariable> inputs) {
			this.inputs = inputs;
		}
		public List<SadlVariable> getOutputs() {
			return outputs;
		}
		public void setOutputs(List<SadlVariable> outputs) {
			this.outputs = outputs;
		}
		public List<SadlExpression> getPriors() {
			return priors;
		}
		public void setPriors(List<SadlExpression> priors) {
			this.priors = priors;
		}
		public List<SadlExpression> getNexts() {
			return nexts;
		}
		public void setNexts(List<SadlExpression> nexts) {
			this.nexts = nexts;
		}
    }
    
    public class SadlVariable {
		private String name = null;
		private String type = null;
		private List<String> notes = null;
		
		public SadlVariable(String nm, String typ) {
			name = nm;
			type = typ;
		}
		
		public String getName() {
			return name;
		}
		public void setName(String name) {
			this.name = name;
		}
		public String getType() {
			return type;
		}
		public void setType(String type) {
			this.type = type;
		}

		public List<String> getNotes() {
			return notes;
		}

		public void addNote(String note) {
			this.notes.add(note);
		}
    }
    
	public class SadlMethodArgument extends SadlVariable {
		public SadlMethodArgument(String nm, String typ) {
			super(nm, typ);
		}
	}
	
	public class SadlMethod {
		private String name = null;
		private String returnType = null;
		private List<SadlMethodArgument> arguments = null;
		private List<String> notes = null;
		
		public SadlMethod(String nm, String rtype) {
			setName(nm);
			setReturnType(rtype);
		}
		
		public String getReturnType() {
			return returnType;
		}
		public void setReturnType(String returnType) {
			this.returnType = returnType;
		}
		public List<SadlMethodArgument> getArguments() {
			return arguments;
		}
		public void addArgument(SadlMethodArgument argument) {
			if (arguments == null) {
				arguments = new ArrayList<SadlMethodArgument>();
			}
			this.arguments.add(argument);
		}

		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public List<String> getNotes() {
			return notes;
		}

		public void addNote(String note) {
			if (notes == null) {
				notes = new ArrayList<String>();
			}
			this.notes.add(note);
		}
	}

	public String generateSadlModel(JavaModelExtractor jme, String ontologyRootUri) {
		StringBuilder sb = new StringBuilder();
		// generate SADL model URI
		String thisNS = "uri \"http://";
		thisNS += jme.getPackageName();
		if (jme.getPackageName().length() < 1) {
			// that was insufficient to provide reasonable unique URI
			// use Java filename
			thisNS += "nopackage";
		}
		thisNS += "/";
		thisNS += jme.getType();
		sb.append(thisNS);
		sb.append("\"");
		if (jme.getTypeComment() != null) {
			sb.append("\n (note \"");
			sb.append(jme.getTypeComment());
			sb.append("\")");
		}
		sb.append(".\n\n");
		
		// generate SADL model imports
		Iterator<String> namesitr = jme.getNames().iterator();
		while (namesitr.hasNext()) {
			String name = namesitr.next();
			NamedNode fqn = getNameFromOntology(name);
			if (fqn != null && fqn.getNamespace() != null && !thisNS.equals(fqn.getNamespace().replace("#", ""))) {
				sb.append("import \"");
				sb.append(fqn.getNamespace());
				sb.append("\".\n\n");
			}
		}
		Iterator<String> cditr = jme.getClassDeclarations().keySet().iterator();
		while (cditr.hasNext()) {
			String type = cditr.next();
			String supertype = jme.getClassDeclarations().get(type);
			if (supertype != null) {
				NamedNode fqn = getNameFromOntology(supertype);
				if (fqn != null && fqn.getNamespace() != null && !thisNS.equals(fqn.getNamespace().replace("#", ""))) {
					sb.append("import \"");
					sb.append(fqn.getNamespace());
					sb.append("\".\n\n");
				}
				else {
					logger.debug("Found supertype '" + supertype + "' but could not find import to define.");
				}
			}
		}
		
		// generate External equations from methods
		List<SadlMethod> methods = jme.getMethods();
		for (SadlMethod smd : methods) {
			sb.append("External ");
			sb.append(smd.getName());
			if (smd.getNotes() != null) {
				for (String note : smd.getNotes()) {
					sb.append(" (note \"");
					sb.append(note);
					sb.append("\")\n  ");
				}
			}
			sb.append("(");
			List<SadlMethodArgument> args = smd.getArguments();
			int argCntr = 0;
			if (args != null) {
				for (SadlMethodArgument arg : args) {
					if (argCntr > 0 && argCntr < args.size()) sb.append(" ");
					sb.append(arg.getType());
					sb.append(" ");
					sb.append(arg.getName());
					if (jme.getTagMap().containsKey(arg.getName())) {
						sb.append(" (note \"");
						sb.append(jme.getTagMap().get(arg.getName()).getText());
						sb.append("\")");
						if (argCntr++ < args.size() - 1) sb.append(",");
						sb.append("\n  ");
					}
					else {
						if (argCntr++ < args.size() - 1) sb.append(",");
					}
				}
			}
			sb.append(") returns ");
			String rettype = smd.getReturnType();
			sb.append((rettype == null || rettype.equals("void")) ? "None" : rettype);
			sb.append(": \"");
			sb.append(jme.getPackageName());
			sb.append("/");
			sb.append(jme.getType());
			sb.append("/");
			sb.append(smd.getName());
			sb.append("\".\n");
		}
		
		return sb.toString();
	}

	/**
	 * This is a connection to the semantic models of the project (including projects upon which this depends)
	 * @param name
	 * @return
	 */
	private NamedNode getNameFromOntology(String name) {
		// TODO Auto-generated method stub
		return null;
	}


}

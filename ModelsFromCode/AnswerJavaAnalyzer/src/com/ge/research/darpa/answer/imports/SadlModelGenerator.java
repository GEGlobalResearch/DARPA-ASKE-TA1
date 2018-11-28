package com.ge.research.darpa.answer.imports;

import java.util.ArrayList;
import java.util.List;

public class SadlModelGenerator {
	
	public class SadlMethodArgument {
		private String name = null;
		private String type = null;
		private List<String> notes = null;
		
		public SadlMethodArgument(String nm, String typ) {
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
	
	public class SadlMethodDeclaration {
		private String name = null;
		private String returnType = null;
		private List<SadlMethodArgument> arguments = null;
		private List<String> notes = null;
		
		public SadlMethodDeclaration(String nm, String rtype) {
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
		sb.append("uri \"http://");
		sb.append(jme.getPackageName());
		sb.append("/");
		sb.append(jme.getType());
		sb.append("\"");
		if (jme.getTypeComment() != null) {
			sb.append("\n (note \"");
			sb.append(jme.getTypeComment());
			sb.append("\")");
		}
		sb.append(".\n\n");
		
		// generate SADL model import
		sb.append("import \"");
		sb.append(ontologyRootUri);
		sb.append("\".\n\n");
		
		// generate External equations from methods
		List<SadlMethodDeclaration> methods = jme.getMethods();
		for (SadlMethodDeclaration smd : methods) {
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


}

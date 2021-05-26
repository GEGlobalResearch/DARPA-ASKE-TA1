/**
 * 
 */
package com.ge.research.sadl.darpa.aske.processing.imports;

import java.util.List;

/**
 * @author alfredo
 *
 */
public class GrFN_Function extends GrFN_Node {

	/**
	 * 
	 */
	private String type;
	private String lambda;
//	private Expression expression;
//	private GrFN_Expression expression;
	
//	public class Expression {
//		private String uid;
//		private String type;
//		private String identifier;
//		private List<String> edges;
//		public String getUid() {
//			return uid;
//		}
//		public void setUid(String uid) {
//			this.uid = uid;
//		}
//		public String getType() {
//			return type;
//		}
//		public void setType(String type) {
//			this.type = type;
//		}
//		public String getIdentifier() {
//			return identifier;
//		}
//		public void setIdentifier(String identifier) {
//			this.identifier = identifier;
//		}
//		public List<String> getEdges() {
//			return edges;
//		}
//		public void setEdges(List<String> edges) {
//			this.edges = edges;
//		}
//	}
//	
//	public Expression getExpression() {
//		return expression;
//	}
//
//	public void setExpression(Expression expression) {
//		this.expression = expression;
//	}

	public GrFN_Function() {
		// TODO Auto-generated constructor stub
	}

	public String getType() {
		return type;
	}


	public void setType(String ftype) {
		this.type = ftype;
	}


	public String getLambda() {
		return lambda;
	}

	public void setLambda(String lambda) {
		this.lambda = lambda;
	}

//	public GrFN_Expression getExpression() {
//		return expression;
//	}
//
//	public void setExpression(GrFN_Expression expression) {
//		this.expression = expression;
//	}

}

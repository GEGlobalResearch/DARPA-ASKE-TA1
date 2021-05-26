/**
 * 
 */
package com.ge.research.sadl.darpa.aske.processing.imports;

import java.util.List;

/**
 * @author alfredo
 *
 */
public class GrFN_ExpressionNode extends GrFN_Node{

	/**
	 * 
	 */
	private String type;
	private String operator;
//	private double value;
	private String value;
	private List<String> children;
	private String grfn_uid;


	public String getType() {
		return type;
	}
	public void setType(String type) {
		this.type = type;
	}
	public String getOperator() {
		return operator;
	}
	public void setOperator(String operator) {
		this.operator = operator;
	}
	public List<String> getChildren() {
		return children;
	}
	public void setChildren(List<String> children) {
		this.children = children;
	}
	public String getGrfn_uid() {
		return grfn_uid;
	}
	public void setGrfn_uid(String grfn_uid) {
		this.grfn_uid = grfn_uid;
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
	}
	public GrFN_ExpressionNode() {
		// TODO Auto-generated constructor stub
	}

}

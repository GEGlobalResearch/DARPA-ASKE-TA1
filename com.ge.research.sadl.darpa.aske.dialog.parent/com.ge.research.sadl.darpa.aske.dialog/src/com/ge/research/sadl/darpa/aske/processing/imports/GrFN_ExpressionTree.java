/**
 * 
 */
package com.ge.research.sadl.darpa.aske.processing.imports;

import java.util.List;

//import com.google.gson.annotations.SerializedName;

/**
 * @author alfredo
 *
 */
public class GrFN_ExpressionTree extends GrFN_Node {

	/**
	 * 
	 */
//	@SerializedName("func_node_uid") private String func_uid;
	
//	public String getFunc_uid() {
//		return func_uid;
//	}
//	public void setFunc_uid(String func_uid) {
//		this.func_uid = func_uid;
//	}

	//	public String getFunc_node_uid() {
//		if (func_node_uid != null && func_node_uid.length() > 0 && Character.isDigit(func_node_uid.toCharArray()[0]) ) {
//			func_node_uid = "_" + func_node_uid;
//		}
//		return func_node_uid;
//	}
//	public void setFunc_node_uid(String func_node_uid) {
//		if (func_node_uid.length() > 0 && Character.isDigit(func_node_uid.toCharArray()[0]) ) {
//			func_node_uid = "_" + func_node_uid;
//		}
//		this.func_node_uid = func_node_uid;
//	}
	private List<GrFN_ExpressionNode> nodes;
	
//	public void setUid(String uid) {
//		if (uid.length() > 0 && Character.isDigit(uid.toCharArray()[0]) ) {
//			uid = "_" + uid;
//		}
//		this.uid = uid;
//	}
//	public String getUid() {
//		if (uid != null && uid.length() > 0 && Character.isDigit(uid.toCharArray()[0]) ) {
//			uid = "_" + uid;
//		}
//		return uid;
//	}
	
	public List<GrFN_ExpressionNode> getNodes() {
		return nodes;
	}
	public void setNodes(List<GrFN_ExpressionNode> nodes) {
		this.nodes = nodes;
	}

	public GrFN_ExpressionTree() {
		// TODO Auto-generated constructor stub
	}

}

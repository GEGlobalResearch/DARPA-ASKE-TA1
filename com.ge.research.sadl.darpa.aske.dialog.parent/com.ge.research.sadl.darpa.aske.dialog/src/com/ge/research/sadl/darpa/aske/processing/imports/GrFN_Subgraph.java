/**
 * 
 */
package com.ge.research.sadl.darpa.aske.processing.imports;

import java.util.ArrayList;
import java.util.List;

/**
 * @author alfredo
 *
 */
public class GrFN_Subgraph extends GrFN_Node {

	/**
	 * 
	 */
	
	private String name;
	private String namespace;
	private String scope;
	private String parent;
	private String type;
	private List<String> nodes = new ArrayList<String>();
	
	public GrFN_Subgraph() {
		// TODO Auto-generated constructor stub
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getNamespace() {
		return namespace;
	}

	public void setNamespace(String namespace) {
		this.namespace = namespace;
	}

	public String getScope() {
		return scope;
	}

	public void setScope(String scope) {
		this.scope = scope;
	}

	public List<String> getNodes() {
		return nodes;
	}

	public void setNodes(List<String> nodes) {
		this.nodes = nodes;
	}

	public String getParent() {
		return parent;
	}

	public void setParent(String parent) {
		this.parent = parent;
	}
	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}
}

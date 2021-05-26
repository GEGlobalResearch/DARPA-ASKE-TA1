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
public class GrFN_Graph extends GrFN_Node {

	/**
	 * 
	 */
	
	private String timestamp;
	private List<GrFN_Function> functions = new ArrayList<GrFN_Function>();
	private List<GrFN_Hyperedge> hyper_edges = new ArrayList<GrFN_Hyperedge>();
	private List<GrFN_Variable> variables = new ArrayList<GrFN_Variable>();
	private List<GrFN_Subgraph> subgraphs = new ArrayList<GrFN_Subgraph>();
//	private List<GrFN_Object> objects = new ArrayList<GrFN_Object>();
	private List<GrFN_Type> types = new ArrayList<GrFN_Type>();


	public GrFN_Graph() {
		// TODO Auto-generated constructor stub
		
	}

	public void setDate_created(String date_created) {
		this.timestamp = date_created;
	}
	public String getDate_created() {
		return timestamp;
	}
	public List<GrFN_Function> getFunctions() {
		return functions;
	}

	public void setFunctions(List<GrFN_Function> functions) {
		this.functions = functions;
	}
	public List<GrFN_Hyperedge> getHyperedges() {
		return hyper_edges;
	}

	public void setHyperedges(List<GrFN_Hyperedge> hyperedges) {
		this.hyper_edges = hyperedges;
	}

	public List<GrFN_Variable> getVariables() {
		return variables;
	}

	public void setVariables(List<GrFN_Variable> variables) {
		this.variables = variables;
	}

	public List<GrFN_Subgraph> getSubgraphs() {
		return subgraphs;
	}

	public void setSubgraphs(List<GrFN_Subgraph> subgraphs) {
		this.subgraphs = subgraphs;
	}

//	public List<GrFN_Object> getObjects() {
//		return objects;
//	}
//
//	public void setObjects(List<GrFN_Object> objects) {
//		this.objects = objects;
//	}

	public List<GrFN_Type> getTypes() {
		return types;
	}

	public void setTypes(List<GrFN_Type> types) {
		this.types = types;
	}

}

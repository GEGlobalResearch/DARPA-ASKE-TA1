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
public class GrFN_Hyperedge extends GrFN_Node {
	/**
	 * 
	 */
	private String function;
	private List<String> inputs = new ArrayList<String>(); 
	private List<String> outputs = new ArrayList<String>(); 
	
	public GrFN_Hyperedge() {
		// TODO Auto-generated constructor stub
	}

	public String getFunction() {
		return function;
	}

	public void setFunction(String function) {
		this.function = function;
	}

	public List<String> getInputs() {
		return inputs;
	}

	public void setInputs(List<String> inputs) {
		this.inputs = inputs;
	}

	public List<String> getOutputs() {
		return outputs;
	}

	public void setOutputs(List<String> outputs) {
		this.outputs = outputs;
	}


}

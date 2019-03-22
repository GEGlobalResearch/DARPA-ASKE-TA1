package com.ge.research.sadl.darpa.aske.processing;

public class BuildConstruct extends MixedInitiativeContent {
	private Object target;

	public BuildConstruct(String modelUri) {
		setTarget(modelUri);
	}

	public Object getTarget() {
		return target;
	}

	public void setTarget(Object target) {
		this.target = target;
	}
	
	public String toString() {
		return "Build " + target.toString();
	}
}

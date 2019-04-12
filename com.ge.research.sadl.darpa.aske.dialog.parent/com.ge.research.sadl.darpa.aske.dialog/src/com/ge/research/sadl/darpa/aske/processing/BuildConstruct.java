package com.ge.research.sadl.darpa.aske.processing;

public class BuildConstruct extends MixedInitiativeContent {
	private String target;

	public BuildConstruct(String modelUri) {
		setTarget(modelUri);
	}

	public String getTarget() {
		return target;
	}

	public void setTarget(String target) {
		this.target = target;
	}
	
	public String toString() {
		return "Build " + target.toString();
	}
}

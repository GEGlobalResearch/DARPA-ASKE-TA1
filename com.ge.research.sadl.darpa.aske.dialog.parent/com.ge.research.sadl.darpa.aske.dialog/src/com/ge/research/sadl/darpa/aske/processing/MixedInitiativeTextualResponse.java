package com.ge.research.sadl.darpa.aske.processing;

public class MixedInitiativeTextualResponse extends MixedInitiativeContent {
	private String response = null;
	private int insertionPoint = -1;
	
	public MixedInitiativeTextualResponse(String content) {
		setResponse(content);
	}

	public String getResponse() {
		return response;
	}

	public void setResponse(String response) {
		this.response = response;
	}
	
	public String toString() {
		return response;
	}

	public int getInsertionPoint() {
		return insertionPoint;
	}

	public void setInsertionPoint(int insertionPoint) {
		this.insertionPoint = insertionPoint;
	}
}

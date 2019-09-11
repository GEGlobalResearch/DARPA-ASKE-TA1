package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class SaveContent extends ExpectsAnswerContent {
	private String targetModelUri;
	private String targetModelActualUrl;
	private String sourceEquationUri; 

	public SaveContent(EObject host) {
		super(host);
		// TODO Auto-generated constructor stub
	}

	public SaveContent(EObject host, Agent agnt) {
		super(host, agnt);
		// TODO Auto-generated constructor stub
	}

	public String getTargetModelUri() {
		return targetModelUri;
	}

	public void setTargetModelUri(String targetModelUri) {
		this.targetModelUri = targetModelUri;
	}

	public String getTargetModelActualUrl() {
		return targetModelActualUrl;
	}

	public void setTargetModelActualUrl(String targetModelActualUrl) {
		this.targetModelActualUrl = targetModelActualUrl;
	}

	public String getSourceEquationUri() {
		return sourceEquationUri;
	}

	public void setSourceEquationUri(String sourceEquationUri) {
		this.sourceEquationUri = sourceEquationUri;
	}

}

package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class SadlStatementContent extends StatementContent {
	private String conceptUri = null;
	
	public SadlStatementContent(EObject host) {
		super(host);
	}
	
	public SadlStatementContent(EObject host, Agent agnt) {
		super(host, agnt);
	}

	public SadlStatementContent(EObject host, Agent agnt, String upTxt) {
		super(host, agnt, upTxt);
	}

	public String getConceptUri() {
		return conceptUri;
	}

	public void setConceptUri(String conceptUri) {
		this.conceptUri = conceptUri;
	}
}

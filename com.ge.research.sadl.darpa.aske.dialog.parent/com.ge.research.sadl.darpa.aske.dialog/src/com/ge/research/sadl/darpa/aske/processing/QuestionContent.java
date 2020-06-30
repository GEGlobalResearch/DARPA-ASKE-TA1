package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class QuestionContent extends ExpectsAnswerContent {
	private String explicitQuestion = null;
	public QuestionContent(EObject host) {
		super(host);
	}
	
	public QuestionContent(EObject host, Agent agnt) {
		super(host, agnt);
	}

	public QuestionContent(EObject host, Agent agnt, String uptxt) {
		super(host, agnt, uptxt);
	}

	public String getExplicitQuestion() {
		return explicitQuestion;
	}

	public void setExplicitQuestion(String explicitQuestion) {
		this.explicitQuestion = explicitQuestion;
	}
	

}

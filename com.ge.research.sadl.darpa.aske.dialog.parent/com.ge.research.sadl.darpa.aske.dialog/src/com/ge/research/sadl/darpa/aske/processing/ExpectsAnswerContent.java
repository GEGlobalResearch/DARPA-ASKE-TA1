package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public abstract class ExpectsAnswerContent extends StatementContent {

	private AnswerContent answer;

	public ExpectsAnswerContent(EObject host) {
		super(host);
	}
	
	public ExpectsAnswerContent(EObject host, Agent agnt) {
		super(host, agnt);
	}

	public ExpectsAnswerContent(EObject host, Agent agnt, String uptxt) {
		super(host, agnt, uptxt);
	}

	public AnswerContent getAnswer() {
		return answer;
	}

	public void setAnswer(AnswerContent answer) {
		this.answer = answer;
	}

}

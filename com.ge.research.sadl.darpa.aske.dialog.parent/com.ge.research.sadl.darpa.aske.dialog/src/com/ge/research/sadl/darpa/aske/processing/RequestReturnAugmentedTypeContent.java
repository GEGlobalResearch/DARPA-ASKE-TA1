package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class RequestReturnAugmentedTypeContent extends QuestionUserContent {
	private String equationName;
	private String question;
	
	public RequestReturnAugmentedTypeContent(EObject host, Agent agnt) {
		super(host, agnt);
	}

	public RequestReturnAugmentedTypeContent(EObject host, Agent agnt, String text) {
		super(host, agnt, text);
	}

	public RequestReturnAugmentedTypeContent(EObject host, Agent agnt, String text, String eqn, String q) {
		super(host, agnt, text);
		setEquationName(eqn);
		setQuestion(q);
	}

	public String getEquationName() {
		return equationName;
	}

	public void setEquationName(String equationName) {
		this.equationName = equationName;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

}

package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class RequestArgumentAugmentedTypeContent extends QuestionUserContent {
	private String argumentName;
	private String question;
	
	public RequestArgumentAugmentedTypeContent(EObject host, Agent agnt) {
		super(host, agnt);
		// TODO Auto-generated constructor stub
	}

	public RequestArgumentAugmentedTypeContent(EObject host, Agent agnt, String text) {
		super(host, agnt, text);
	}

	public RequestArgumentAugmentedTypeContent(EObject host, Agent agnt, String text, String arg, String q) {
		super(host, agnt, text);
		setArgumentName(arg);
		setQuestion(q);
	}
	
	public String getArgumentName() {
		return argumentName;
	}

	public void setArgumentName(String argumentName) {
		this.argumentName = argumentName;
	}

	public String getQuestion() {
		return question;
	}

	public void setQuestion(String question) {
		this.question = question;
	}

}

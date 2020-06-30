package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class RequestArgumentAugmentedTypeContent extends QuestionContent {
	private String equationName;
	private String argumentName;
	private String question;
	
	public RequestArgumentAugmentedTypeContent(EObject host, Agent agnt) {
		super(host, agnt);
	}

	public RequestArgumentAugmentedTypeContent(EObject host, Agent agnt, String uptxt) {
		super(host, agnt, uptxt);
	}

	public RequestArgumentAugmentedTypeContent(EObject host, Agent agnt, String text, String arg, String eqName, String q) {
		super(host, agnt, text);
		setArgumentName(arg);
		setQuestion(q);
		setEquationName(eqName);
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

	public String getEquationName() {
		return equationName;
	}

	public void setEquationName(String eqName) {
		this.equationName = eqName;
	}

}

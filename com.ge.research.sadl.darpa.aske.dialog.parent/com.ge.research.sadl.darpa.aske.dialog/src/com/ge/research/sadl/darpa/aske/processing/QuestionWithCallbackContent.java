package com.ge.research.sadl.darpa.aske.processing;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class QuestionWithCallbackContent extends QuestionContent {
	private String methodToCall = null;
	private List<Object> arguments = null;
	private String theQuestion = null;

	public QuestionWithCallbackContent(EObject host) {
		super(host);
	}
	
	public QuestionWithCallbackContent(EObject host, Agent agnt) {
		super(host, agnt);
		// TODO Auto-generated constructor stub
	}
	
	public QuestionWithCallbackContent(EObject host, Agent agent, String method, List<Object> args) {
		super(host, agent);
		setMethodToCall(method);
		setArguments(args);
	}

	public QuestionWithCallbackContent(EObject host, Agent agent, String method, List<Object> args, String question) {
		super(host, agent);
		setMethodToCall(method);
		setArguments(args);
		setTheQuestion(question);
	}

	public String getMethodToCall() {
		return methodToCall;
	}

	public void setMethodToCall(String methodToCall) {
		this.methodToCall = methodToCall;
	}

	public List<Object> getArguments() {
		return arguments;
	}

	public void setArguments(List<Object> arguments) {
		this.arguments = arguments;
	}

	public String getTheQuestion() {
		return theQuestion;
	}

	public void setTheQuestion(String theQuestion) {
		this.theQuestion = theQuestion;
	}

	
}

package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class QuestionContent extends StatementContent {
	private AnswerContent answer;
	
	public QuestionContent(EObject host) {
		super(host);
		// TODO Auto-generated constructor stub
	}
	
	public QuestionContent(EObject host, Agent agnt) {
		super(host, agnt);
		// TODO Auto-generated constructor stub
	}

	public AnswerContent getAnswer() {
		return answer;
	}

	public void setAnswer(AnswerContent answer) {
		this.answer = answer;
	}
	

}

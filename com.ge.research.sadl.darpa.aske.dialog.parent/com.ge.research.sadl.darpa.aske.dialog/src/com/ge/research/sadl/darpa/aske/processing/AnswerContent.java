package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class AnswerContent extends StatementContent {
	private Object answer;
	private QuestionContent answerTo;

	public AnswerContent(EObject host) {
		super(host);
		// TODO Auto-generated constructor stub
	}

	public AnswerContent(EObject host, Agent agnt) {
		super(host, agnt);
		// TODO Auto-generated constructor stub
	}

	public QuestionContent getAnswerTo() {
		return answerTo;
	}

	public void setAnswerTo(QuestionContent answerTo) {
		this.answerTo = answerTo;
	}

	public Object getAnswer() {
		return answer;
	}

	public void setAnswer(Object answer) {
		this.answer = answer;
	}
}

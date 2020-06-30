package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class AnswerContent extends StatementContent {
	private Object answer;
	private StatementContent answerTo;
	private Object otherResults;

	public AnswerContent(EObject host) {
		super(host);
	}

	public AnswerContent(EObject host, Agent agnt) {
		super(host, agnt);
	}

	public AnswerContent(EObject host, Agent agnt, String uptxt) {
		super(host, agnt, uptxt);
	}

	public StatementContent getAnswerTo() {
		return answerTo;
	}

	public void setAnswerTo(StatementContent answerTo) {
		this.answerTo = answerTo;
	}

	public Object getAnswer() {
		return answer;
	}

	public void setAnswer(Object answer) {
		this.answer = answer;
	}

	public Object getOtherResults() {
		return otherResults;
	}

	public void setOtherResults(Object otherResults) {
		this.otherResults = otherResults;
	}
}

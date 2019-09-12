package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class AnswerPendingContent extends AnswerContent {

	public AnswerPendingContent(EObject host) {
		super(host);
		// TODO Auto-generated constructor stub
	}

	public AnswerPendingContent(EObject host, Agent agnt) {
		super(host, agnt);
		// TODO Auto-generated constructor stub
	}

	public AnswerPendingContent(EObject host, Agent agnt, ExpectsAnswerContent inAnswerTo) {
		super(host, agnt);
		setAnswerTo(inAnswerTo);
	}
	
	public String toString() {
		return "Answer pending...";
	}
}

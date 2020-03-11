package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.sADL.SadlResource;

public class UndefinedConceptStatementContent extends QuestionUserContent {
	private SadlResource undefinedSadlResource;

	public UndefinedConceptStatementContent(EObject host, Agent agnt, SadlResource ucsr) {
		super(host, agnt);
		setUndefinedSadlResource(ucsr);
	}

	public SadlResource getUndefinedSadlResource() {
		return undefinedSadlResource;
	}

	public void setUndefinedSadlResource(SadlResource undefinedSadlResource) {
		this.undefinedSadlResource = undefinedSadlResource;
	}

}

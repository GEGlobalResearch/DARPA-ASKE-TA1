package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class AddToModelContent extends ExpectsAnswerContent {

	public AddToModelContent(EObject host) {
		super(host);
	}

	public AddToModelContent(EObject host, Agent agnt) {
		super(host, agnt);
	}

	public AddToModelContent(EObject host, Agent agnt, String uptxt) {
		super(host, agnt, uptxt);
	}

}

package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class MatchFoundStatementContent extends InformationContent {

	public MatchFoundStatementContent(EObject host, Agent agnt, String msg) {
		super(host, agnt, msg);
	}

}

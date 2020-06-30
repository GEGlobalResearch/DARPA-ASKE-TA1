package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.model.gp.Query;

public class ModifiedAskContent extends QuestionContent {
	private Query query;
	
	public ModifiedAskContent(EObject host) {
		super(host);
		// TODO Auto-generated constructor stub
	}

	public ModifiedAskContent(EObject host, Agent agnt) {
		super(host, agnt);
		// TODO Auto-generated constructor stub
	}

	public Query getQuery() {
		return query;
	}

	public void setQuery(Query query) {
		this.query = query;
	}
}

package com.ge.research.sadl.darpa.aske.processing;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.model.gp.Rule;

public class CompareContent extends ExpectsAnswerContent {
	private List<Rule> comparisonRules;

	public CompareContent(EObject host) {
		super(host);
	}
	
	public CompareContent(EObject host, Agent agnt) {
		super(host, agnt);
	}

	public CompareContent(EObject host, Agent agnt, List<Rule> toCompare) {
		super(host, agnt);
		setComparisonRules(toCompare);
	}

	public List<Rule> getComparisonRules() {
		return comparisonRules;
	}

	public void setComparisonRules(List<Rule> comparisonRules) {
		this.comparisonRules = comparisonRules;
	}

}

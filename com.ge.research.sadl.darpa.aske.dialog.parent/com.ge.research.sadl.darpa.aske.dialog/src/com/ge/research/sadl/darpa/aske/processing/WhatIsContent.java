package com.ge.research.sadl.darpa.aske.processing;

import java.util.List;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.ICompositeNode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.model.gp.Rule;

public class WhatIsContent extends QuestionContent {
//	private Object target;
//	private Object when;
	private List<Rule> comparisonRules;

	public WhatIsContent(EObject host) {
		super(host);
		// TODO Auto-generated constructor stub
	}

	public WhatIsContent(EObject host, Agent agnt) {
		super(host, agnt);
		// TODO Auto-generated constructor stub
	}
	
	public WhatIsContent(EObject host, Agent agnt, List<Rule> comparisonRules) {
		super(host, agnt);
		setComparisonRules(comparisonRules);
//		setTarget(trgt);
//		setWhen(whn);
	}

//	public Object getTarget() {
//		return target;
//	}
//	
//	private void setTarget(Object target) {
//		this.target = target;
//	}
//	
//	public Object getWhen() {
//		return when;
//	}
//	
//	private void setWhen(Object when) {
//		this.when = when;
//	}

	public String toString() {
		ICompositeNode nd = NodeModelUtils.findActualNodeFor(getHostEObject().eContainer());
		return nd.getText().trim();
	}

	public List<Rule> getComparisonRules() {
		return comparisonRules;
	}

	public void setComparisonRules(List<Rule> comparisonRules) {
		this.comparisonRules = comparisonRules;
	}
}


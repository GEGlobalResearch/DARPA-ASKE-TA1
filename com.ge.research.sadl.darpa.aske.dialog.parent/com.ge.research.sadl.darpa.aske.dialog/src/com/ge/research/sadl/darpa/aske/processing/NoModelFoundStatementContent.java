package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.model.gp.Node;

public class NoModelFoundStatementContent extends InformationContent {
	private Node target;

	public NoModelFoundStatementContent(EObject host, Agent agnt, String msg, Node target) {
		super(host, agnt, msg);
		setTarget(target);
	}

	public Node getTarget() {
		return target;
	}

	public void setTarget(Node target) {
		this.target = target;
	}

	
}

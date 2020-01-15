package com.ge.research.sadl.darpa.aske.processing;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.model.gp.Node;

public class CompareContent extends ExpectsAnswerContent {
	private List<Node> comparators;
	private Node when;

	public CompareContent(EObject host) {
		super(host);
	}
	
	public CompareContent(EObject host, Agent agnt) {
		super(host, agnt);
		
	}

	public CompareContent(EObject host, Agent agnt,
			List<Node> toCompare, Node condition) {
		super(host, agnt);
		setComparators(toCompare);
		setWhen(condition);
	}

	public List<Node> getComparators() {
		return comparators;
	}

	public void setComparators(List<Node> comparators) {
		this.comparators = comparators;
	}

	public Node getWhen() {
		return when;
	}

	public void setWhen(Node when) {
		this.when = when;
	}
	
	
}

package com.ge.research.sadl.darpa.aske.processing;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.model.gp.Node;

public class EvalContent extends ExpectsAnswerContent {
	private Node equationName;
	private List<Node> parameters;
	private List<String> units;
	
	public EvalContent(EObject host) {
		super(host);
		// TODO Auto-generated constructor stub
	}

	public EvalContent(EObject host, Agent agnt) {
		super(host, agnt);
		// TODO Auto-generated constructor stub
	}

	public Node getEquationName() {
		return equationName;
	}

	public void setEquationName(Node equationName) {
		this.equationName = equationName;
	}

	public List<Node> getParameters() {
		return parameters;
	}

	public void setParameters(List<Node> parameters) {
		this.parameters = parameters;
	}

	public List<String> getUnits() {
		return units;
	}

	public void setUnits(List<String> units) {
		this.units = units;
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("evaluate ");
		sb.append(getEquationName().toFullyQualifiedString());
		sb.append("(");
		int cntr = 0;
		if (getParameters() != null) {
			List<String> unts = getUnits();
			for (Node p : getParameters()) {
				if (cntr > 0) sb.append(", ");
				sb.append(p.toFullyQualifiedString());
				if (unts != null && unts.size() > cntr) {
					sb.append(" ");
					sb.append(unts.get(cntr));
				}
				cntr++;
			}
		}
		sb.append(")");
		return sb.toString();
	}
}

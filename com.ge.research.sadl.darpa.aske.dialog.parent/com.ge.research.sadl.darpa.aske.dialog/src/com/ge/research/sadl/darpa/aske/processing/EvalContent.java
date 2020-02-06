package com.ge.research.sadl.darpa.aske.processing;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.model.gp.NamedNode;
import com.ge.research.sadl.model.gp.Node;

public class EvalContent extends ExpectsAnswerContent {
	private Node equationName;
	private List<UnittedParameter> parameters;
	
	public class UnittedParameter {
		private Node parameter;
		private String unit;
		
		public UnittedParameter(Node param, String unit) {
			setParameter(param);
			setUnit(unit);
		}
		
		public Node getParameter() {
			return parameter;
		}
		
		public void setParameter(Node parameter) {
			this.parameter = parameter;
		}
		
		public String getUnit() {
			return unit;
		}
		
		public void setUnit(String unit) {
			this.unit = unit;
		}
		
		public String toString() {
			if (parameter instanceof NamedNode) {
				return ((NamedNode)parameter).getName();
			}
			return parameter.toString();
		}
	}
	
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

	public List<UnittedParameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<UnittedParameter> parameters) {
		this.parameters = parameters;
	}
	
	public void addParameter(UnittedParameter parameter) {
		if (parameters == null) {
			parameters = new ArrayList<UnittedParameter>();
		}
		parameters.add(parameter);
	}

	public String toString() {
		StringBuilder sb = new StringBuilder("evaluate ");
		sb.append(getEquationName().toFullyQualifiedString());
		sb.append("(");
		int cntr = 0;
		if (getParameters() != null) {
			List<UnittedParameter> params = getParameters();
			for (UnittedParameter param : params) {
				String unt = param.getUnit();
				Node p = param.getParameter();
				if (cntr > 0) sb.append(", ");
				sb.append(p.toFullyQualifiedString());
				if (unt != null) {
					sb.append(" ");
					sb.append(unt);
				}
				cntr++;
			}
		}
		sb.append(")");
		return sb.toString();
	}
}

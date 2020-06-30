package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.darpa.aske.dialog.NewExpressionStatement;

public class AddEquationContent extends AddToModelContent {
	private String lhs;
	private String equationName;
	private String sadlEqStr;		// AKA, RHS
	private String equationBody;

	public AddEquationContent(EObject host) {
		super(host);
	}

	public AddEquationContent(EObject host, Agent agnt) {
		super(host, agnt);
	}

	public AddEquationContent(EObject host, Agent agnt, String uptxt) {
		super(host, agnt, uptxt);
	}

	public AddEquationContent(NewExpressionStatement host, Agent agnt, String uptxt, String eqName, String eqString) {
		super(host, agnt, uptxt);
		setEquationName(eqName);
		setSadlEqStr(eqString);
	}

	public String getSadlEqStr() {
		return sadlEqStr;
	}
	
	public void setSadlEqStr(String sadlEqStr) {
		this.sadlEqStr = sadlEqStr;
	}

	public String getEquationName() {
		return equationName;
	}

	public void setEquationName(String equationName) {
		this.equationName = equationName;
	}

	public String getEquationBody() {
		return equationBody;
	}

	public void setEquationBody(String equationBody) {
		this.equationBody = equationBody;
	}

	public String getLhs() {
		return lhs;
	}

	public void setLhs(String lhs) {
		this.lhs = lhs;
	}
}

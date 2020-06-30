package com.ge.research.sadl.darpa.aske.processing;

import java.util.List;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class EquationStatementContent extends SadlStatementContent {
	private String equationName = null;
	
	private List<StatementContent> questionsForUser = null;

	public EquationStatementContent(EObject host, Agent agnt) {
		super(host, agnt);
	}

	public EquationStatementContent(EObject host, Agent agnt, String upTxt) {
		super(host, agnt, upTxt);
	}

	public EquationStatementContent(EObject host, Agent agnt, String upTxt, String eqName) {
		super(host, agnt, upTxt);
		setEquationName(eqName);
	}

	public List<StatementContent> getQuestionsForUser() {
		return questionsForUser;
	}

	public void setQuestionsForUser(List<StatementContent> questionsForUser) {
		this.questionsForUser = questionsForUser;
	}

	public String getEquationName() {
		return equationName;
	}

	public void setEquationName(String equationName) {
		this.equationName = equationName;
	}

}

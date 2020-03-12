package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.darpa.aske.dialog.NewExpressionStatement;

public class AddAugmentedTypeInfoContent extends AddToModelContent {
	private String equationName;
	private String argName;
	private String addedTypeUri;

	public AddAugmentedTypeInfoContent(EObject host) {
		super(host);
	}

	public AddAugmentedTypeInfoContent(EObject host, Agent agnt) {
		super(host, agnt);
	}

	public AddAugmentedTypeInfoContent(EObject host, Agent agnt, String uptxt) {
		super(host, agnt, uptxt);
	}

	public AddAugmentedTypeInfoContent(NewExpressionStatement host, Agent agnt, String uptxt, String modEq, boolean quote) {
		super(host, agnt, uptxt);
		setEquationName(modEq);
		setQuoteResult(quote);
	}

	public String getEquationName() {
		return equationName;
	}

	public void setEquationName(String equationName) {
		this.equationName = equationName;
	}

	public String getArgName() {
		return argName;
	}

	public void setArgName(String argName) {
		this.argName = argName;
	}

	public String getAddedTypeUri() {
		return addedTypeUri;
	}

	public void setAddedTypeUri(String addedTypeUri) {
		this.addedTypeUri = addedTypeUri;
	}
}

package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class NewOrModifiedEquationContent extends ExpectsAnswerContent {
	private String sadlEqStr;
	private boolean quoteResult = false;
	
	public NewOrModifiedEquationContent(EObject host, Agent agnt, String unparsed, String eqStr) {
		super(host, agnt, unparsed);
		setSadlEqStr(eqStr);
	}
	
	public NewOrModifiedEquationContent(EObject host, Agent agnt, String unparsed, String eqStr, boolean quote) {
		super(host, agnt, unparsed);
		setSadlEqStr(eqStr);
		setQuoteResult(quote);
	}
	
	public String getSadlEqStr() {
		return sadlEqStr;
	}
	
	public void setSadlEqStr(String sadlEqStr) {
		this.sadlEqStr = sadlEqStr;
	}

	public boolean isQuoteResult() {
		return quoteResult;
	}

	public void setQuoteResult(boolean quoteResult) {
		this.quoteResult = quoteResult;
	}

}

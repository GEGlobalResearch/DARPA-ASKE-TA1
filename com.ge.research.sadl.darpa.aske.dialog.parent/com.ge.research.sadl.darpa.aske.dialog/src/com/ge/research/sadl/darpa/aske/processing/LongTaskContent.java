package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

/**
 * Class to encapsulate all necessary information to save a single equation model or all equation models
 * @author 200005201
 *
 */
public class LongTaskContent extends ExpectsAnswerContent {
	private int time = 10000;

	public LongTaskContent(EObject host) {
		super(host);
	}

	public LongTaskContent(EObject host, Agent agnt) {
		super(host, agnt);
	}

	public LongTaskContent(EObject host, Agent agnt, int t) {
		super(host, agnt);
		setTime(t);
	}
	
	public int getTime() {
		return time;
	}

	public void setTime(int time) {
		this.time = time;
	}

}

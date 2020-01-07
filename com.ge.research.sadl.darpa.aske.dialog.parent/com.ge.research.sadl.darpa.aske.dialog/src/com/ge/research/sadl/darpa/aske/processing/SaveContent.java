package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

/**
 * Class to encapsulate all necessary information to save a single equation model or all equation models
 * @author 200005201
 *
 */
public class SaveContent extends ExpectsAnswerContent {
	private boolean saveAll = false;
	private String targetModelAlias;
	private String sourceEquationUri; 

	public SaveContent(EObject host) {
		super(host);
	}

	public SaveContent(EObject host, Agent agnt) {
		super(host, agnt);
	}

	public String getSourceEquationUri() {
		return sourceEquationUri;
	}

	public void setSourceEquationUri(String sourceEquationUri) {
		this.sourceEquationUri = sourceEquationUri;
	}

	public boolean isSaveAll() {
		return saveAll;
	}

	public void setSaveAll(boolean saveAll) {
		this.saveAll = saveAll;
	}

	public String getTargetModelAlias() {
		return targetModelAlias;
	}

	public void setTargetModelAlias(String targetModelAlias) {
		this.targetModelAlias = targetModelAlias;
	}

}

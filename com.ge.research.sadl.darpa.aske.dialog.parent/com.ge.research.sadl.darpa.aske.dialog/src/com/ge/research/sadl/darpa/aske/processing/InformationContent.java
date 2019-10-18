package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class InformationContent extends StatementContent {
	private String infoMessage;

	public InformationContent(EObject host) {
		super(host);
	}

	public InformationContent(EObject host, Agent agnt) {
		super(host, agnt);
	}

	public InformationContent(EObject host, Agent agnt, String msg) {
		super(host, agnt);
		setInfoMessage(msg);
	}

	public String getText() {
		if (getInfoMessage() != null) {
			return getInfoMessage();
		}
		else if (getHostEObject() != null) {
			return removeLeadingComments(NodeModelUtils.findActualNodeFor(getHostEObject()).getText());
		}
		return infoMessage;
	}
	
	public String getInfoMessage() {
		return infoMessage;
	}

	public void setInfoMessage(String infoMessage) {
		this.infoMessage = infoMessage;
	}
}

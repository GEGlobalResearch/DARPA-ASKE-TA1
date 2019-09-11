package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

/**
 * Root class of classes holding Dialog conversation content, for inclusion in ConversationElements in DialogContent
 * @author 200005201
 *
 */
public abstract class StatementContent {
	private EObject hostEObject;
	private Agent agent;
	
	public StatementContent(EObject host) {
		setHostEObject(host);
	}
	
	public StatementContent(EObject host, Agent agnt) {
		setHostEObject(host);
		setAgent(agnt);
	}

	public EObject getHostEObject() {
		return hostEObject;
	}

	private void setHostEObject(EObject hostEObject) {
		this.hostEObject = hostEObject;
	}
	
	public String getText() {
		return NodeModelUtils.findActualNodeFor(getHostEObject()).getText();
	}
	
	public int getOffset() {
		return NodeModelUtils.findActualNodeFor(getHostEObject()).getTotalOffset();
	}

	public int getLength() {
		return NodeModelUtils.findActualNodeFor(getHostEObject()).getLength();
	}
	
	public Agent getAgent() {
		return agent;
	}

	public void setAgent(Agent agent) {
		this.agent = agent;
	}
	
	public String toString() {
		return getText().trim();
	}
}

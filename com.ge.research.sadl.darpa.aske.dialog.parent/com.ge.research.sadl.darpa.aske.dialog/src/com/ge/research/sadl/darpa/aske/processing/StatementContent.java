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
	private String unParsedText = null;
	
	public StatementContent(EObject host) {
		setHostEObject(host);
	}
	
	public StatementContent(EObject host, Agent agnt) {
		setHostEObject(host);
		setAgent(agnt);
	}

	public StatementContent(EObject host, Agent agnt, String text) {
		setHostEObject(host);
		setAgent(agnt);
		setUnParsedText(text);
	}

	public EObject getHostEObject() {
		return hostEObject;
	}

	private void setHostEObject(EObject hostEObject) {
		this.hostEObject = hostEObject;
	}
	
	public String getText() {
		if (getHostEObject() != null) {
			return removeLeadingComments(NodeModelUtils.findActualNodeFor(getHostEObject()).getText());
		}
		return getUnParsedText();
	}
	
	public int getOffset() {
		if (getHostEObject() != null) {
			return NodeModelUtils.findActualNodeFor(getHostEObject()).getTotalOffset();
		}
		return -1;
	}

	public int getLength() {
		if (getHostEObject() != null) {
			return NodeModelUtils.findActualNodeFor(getHostEObject()).getLength();
		}
		return -1;
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
	
	public String removeLeadingComments(String text) {
		String lines[] = text.split("\\r?\\n");
		String lastLine = null;
		for (String line : lines) {
			if (!line.trim().startsWith("//") && line.trim().length() > 0) {
				lastLine = line;
				break;
			}
		}
		if (lastLine != null) {
			int loc = text.indexOf(lastLine);
			text = text.substring(loc).trim();
		}
		return text;
	}

	private String getUnParsedText() {
		return unParsedText;
	}

	private void setUnParsedText(String unParsedText) {
		this.unParsedText = unParsedText;
	}


}

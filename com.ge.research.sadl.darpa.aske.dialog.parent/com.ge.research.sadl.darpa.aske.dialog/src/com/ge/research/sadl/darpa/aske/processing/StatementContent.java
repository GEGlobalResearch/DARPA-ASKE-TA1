package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;
import org.eclipse.xtext.nodemodel.ICompositeNode;
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
	private boolean quoteResult = false;
	
	public StatementContent(EObject host) {
		setHostEObject(host);
	}
	
	public StatementContent(EObject host, Agent agnt) {
		setHostEObject(host);
		setAgent(agnt);
	}

	public StatementContent(EObject host, Agent agnt, String uptxt) {
		setHostEObject(host);
		setAgent(agnt);
		setUnParsedText(uptxt);
	}

	public EObject getHostEObject() {
		return hostEObject;
	}

	public void setHostEObject(EObject hostEObject) {
		this.hostEObject = hostEObject;
	}
	
	public String getText() {
		if (getUnParsedText() == null && getHostEObject() != null) {
			ICompositeNode icn = NodeModelUtils.findActualNodeFor(getHostEObject());
			if (icn != null) {
				return removeLeadingComments(icn.getText());
			}
		}
		return getUnParsedText();
	}
	
	public int getOffset() {
		if (getHostEObject() != null) {
			ICompositeNode icn = NodeModelUtils.findActualNodeFor(getHostEObject());
			if (icn != null) {
				return icn.getTotalOffset();
			}
		}
		return -1;
	}

	public int getLength() {
		if (getHostEObject() != null) {
			ICompositeNode icn = NodeModelUtils.findActualNodeFor(getHostEObject());
			if (icn != null) {
				return icn.getLength();
			}
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
		String txt = getText();
		if (txt != null) {
			txt = txt.trim();
		}
		return txt;
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

	public String getUnParsedText() {
		return unParsedText;
	}

	public void setUnParsedText(String unParsedText) {
		this.unParsedText = unParsedText;
	}

	public boolean isQuoteResult() {
		return quoteResult;
	}

	public void setQuoteResult(boolean quoteResult) {
		this.quoteResult = quoteResult;
	}


}

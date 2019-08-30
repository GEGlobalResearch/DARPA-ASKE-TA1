package com.ge.research.sadl.darpa.aske.processing;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class ConversationElement {
	private DialogContent hostConversation;
	private Agent agent;
	private Object statement;

	private String text;
	private int startingLocation;
	private int length;
	
	public ConversationElement(DialogContent dc) {
		setHostConversation(dc);
	}
	
	public ConversationElement(DialogContent dc, Object statement, Agent agent) {
		setHostConversation(dc);
		setStatement(statement);
		setAgent(agent);
	}
	
	public String getText() {
		return text;
	}
	public void setText(String text) {
		this.text = text;
	}
	public int getStartingLocation() {
		return startingLocation;
	}
	public void setStartingLocation(int startingLocation) {
		this.startingLocation = startingLocation;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public Agent getAgent() {
		return agent;
	}
	public void setAgent(Agent agent) {
		this.agent = agent;
	}
	
	public DialogContent getHostConversation() {
		return hostConversation;
	}

	public void setHostConversation(DialogContent hostConversation) {
		this.hostConversation = hostConversation;
	}

	public Object getStatement() {
		return statement;
	}

	public void setStatement(Object statement) {
		this.statement = statement;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder(getAgent().toString());
		sb.append(": ");
		if (getText() != null) {
			sb.append(getText());
		}
		else if (getStatement() != null) {
			sb.append(getStatement().toString());
		}
		return sb.toString();
	}
}

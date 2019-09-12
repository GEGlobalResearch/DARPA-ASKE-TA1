package com.ge.research.sadl.darpa.aske.processing;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;

public class ConversationElement {
	private DialogContent hostConversation;
	private Agent agent;
	private StatementContent statement;

	private String text;
	private int startingLocation;
	private int length;
	
	public ConversationElement(DialogContent dc) {
		setHostConversation(dc);
	}
	
	public ConversationElement(DialogContent dc, StatementContent statement, Agent agent) {
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

	public StatementContent getStatement() {
		return statement;
	}

	public void setStatement(StatementContent statement) {
		this.statement = statement;
		setText(statement.getText());
		setStartingLocation(statement.getOffset());
		setLength(statement.getLength());
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder();
		if (getAgent().equals(Agent.USER)) {
			String userName = getHostConversation().getCurationManager().getUserName();
			if (userName != null) {
				sb.append(userName);
			}
			else {
				sb.append(Agent.USER.toString());
			}
		}
		else {
			sb.append(getAgent().toString());
		}
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

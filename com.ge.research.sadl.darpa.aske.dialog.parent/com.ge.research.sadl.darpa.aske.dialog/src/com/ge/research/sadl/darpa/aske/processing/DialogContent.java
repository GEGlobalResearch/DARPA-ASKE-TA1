package com.ge.research.sadl.darpa.aske.processing;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.emf.ecore.resource.Resource;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;

public class DialogContent {
	private Resource resource;
	private AnswerCurationManager curationManager;
	private List<ConversationElement> statements;
	
	public DialogContent(Resource rsrc, AnswerCurationManager cm) {
		setResource(rsrc);
		setCurationManager(cm);
	}

	public Resource getResource() {
		return resource;
	}

	private void setResource(Resource resource) {
		this.resource = resource;
	}

	public AnswerCurationManager getCurationManager() {
		return curationManager;
	}

	private void setCurationManager(AnswerCurationManager curationManager) {
		this.curationManager = curationManager;
	}

	public List<ConversationElement> getStatements() {
		if (statements == null) {
			statements = new ArrayList<ConversationElement>();
		}
		return statements;
	}

	public boolean addStatement(ConversationElement statement) {
		return getStatements().add(statement);
	}

	public boolean addStatement(ConversationElement statement, int location) {
		if (getStatements().size() <= location) {
			return false;
		}
		getStatements().add(location, statement);
		return true;
	}
	
	public String toString() {
		StringBuilder sb = new StringBuilder("Dialog content:\n");
		Iterator<ConversationElement> itr = getCurationManager().getConversation().getStatements().iterator();
		while (itr.hasNext()) {
			sb.append(itr.next().toString());
			sb.append("\n");
		}
		return sb.toString();
	}
}

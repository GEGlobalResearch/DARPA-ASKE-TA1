package com.ge.research.sadl.darpa.aske.processing;

import java.util.List;
import java.util.function.Consumer;

import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;

public class MixedInitiativeElement {
	private String key = null;
	private Object content = null;
	private MixedInitiativeElement next = null;
	private MixedInitiativeElement previous = null;
	private Consumer<MixedInitiativeElement> respondTo;
	private AnswerCurationManager curationManager = null;
	private String methodToCall = null;
	private List<Object> arguments = null;
	
	public MixedInitiativeElement(Object content, Consumer<MixedInitiativeElement> respondTo) {
		setRespondTo(respondTo);
		setContent(content);
		setKey("Key" + System.currentTimeMillis());
	}

	public MixedInitiativeElement(Object content, Consumer<MixedInitiativeElement> respondTo, AnswerCurationManager acm, String meth, List<Object> args) {
		this(content, respondTo);
		setCurationManager(acm);
		setMethodToCall(meth);
		setArguments(args);
	}

	public String getKey() {
		return key;
	}
	
	public void setKey(String key) {
		this.key = key;
	}
	
	public Object getContent() {
		return content;
	}
	
	public void setContent(Object content) {
		this.content = content;
	}
	
	public MixedInitiativeElement getNext() {
		return next;
	}
	
	public void setNext(MixedInitiativeElement next) {
		this.next = next;
	}
	
	public MixedInitiativeElement getPrevious() {
		return previous;
	}
	
	public void setPrevious(MixedInitiativeElement previous) {
		this.previous = previous;
	}

	public Consumer<MixedInitiativeElement> getRespondTo() {
		return respondTo;
	}

	public void setRespondTo(Consumer<MixedInitiativeElement> respondTo2) {
		this.respondTo = respondTo2;
	}
	
	public String toString() {
		return getContent() != null ? getContent().toString() : "<null> content";
	}

	public AnswerCurationManager getCurationManager() {
		return curationManager;
	}

	public void setCurationManager(AnswerCurationManager curationManager) {
		this.curationManager = curationManager;
	}

	public String getMethodToCall() {
		return methodToCall;
	}

	public void setMethodToCall(String methodToCall) {
		this.methodToCall = methodToCall;
	}

	public List<Object> getArguments() {
		return arguments;
	}

	public void setArguments(List<Object> arguments) {
		this.arguments = arguments;
	}
}

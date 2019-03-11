package com.ge.research.sadl.darpa.aske.processing;

import java.util.function.Consumer;

public class MixedInitiativeElement {
	private String key = null;
	private Object content = null;
	private MixedInitiativeElement next = null;
	private MixedInitiativeElement previous = null;
	private Consumer<MixedInitiativeElement> respondTo;
	
	public MixedInitiativeElement(Object content, Consumer<MixedInitiativeElement> respondTo) {
		setRespondTo(respondTo);
		setContent(content);
		setKey("Key" + System.currentTimeMillis());
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
}

package com.ge.research.sadl.darpa.aske.processing;

@SuppressWarnings("serial")
public class ConversationException extends Exception {
	public ConversationException(String msg) {
		super(msg);
	}
	public ConversationException(String msg, Exception cause) {
		super(msg, cause);
	}
}

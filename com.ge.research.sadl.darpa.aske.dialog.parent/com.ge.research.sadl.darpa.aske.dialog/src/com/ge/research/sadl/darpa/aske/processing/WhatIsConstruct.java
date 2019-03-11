package com.ge.research.sadl.darpa.aske.processing;

public class WhatIsConstruct extends MixedInitiativeContent {
	private Object target;
	private Object when;
	
	public WhatIsConstruct(Object trgt, Object whn) {
		setTarget(trgt);
		setWhen(whn);
	}
	
	public Object getTarget() {
		return target;
	}
	
	private void setTarget(Object target) {
		this.target = target;
	}
	
	public Object getWhen() {
		return when;
	}
	
	private void setWhen(Object when) {
		this.when = when;
	}
}

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
	
	public String toString() {
		StringBuilder sb = new StringBuilder("WhatIsConstruct(");
		if (getTarget() != null) {
			sb.append(getTarget().toString());
		}
		else {
			sb.append("<none>");
		}
		sb.append(",");
		if (getWhen() != null) {
			sb.append(getWhen().toString());
		}
		else {
			sb.append("<none>");
		}
		sb.append(")");
		return sb.toString();
	}
}

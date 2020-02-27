package com.ge.research.sadl.darpa.aske.processing;

import org.eclipse.emf.ecore.EObject;

/**
 * Class to contain information about Dialog window model elements useful for determining how to add to Dialog window
 * from the backend.
 * @author 200005201
 *
 */
public class ModelElementInfo {
	private EObject object;
	private String txt;
	private int start;
	private int length;
	private int end;
	private boolean inserted;	// it's true for inserted, false for original
	
	public ModelElementInfo(EObject obj, String t, int s, int l, int e, boolean i) {
		setObject(obj);
		setTxt(t);
		setStart(s);
		setLength(l);
		setEnd(e);
		setInserted(i);
	}
	
	public String getTxt() {
		return txt;
	}
	public void setTxt(String txt) {
		this.txt = txt;
	}
	public int getStart() {
		return start;
	}
	public void setStart(int start) {
		this.start = start;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	public int getEnd() {
		return end;
	}
	public void setEnd(int end) {
		this.end = end;
	}

	public boolean isInserted() {
		return inserted;
	}

	public void setInserted(boolean inserted) {
		this.inserted = inserted;
	}

	public EObject getObject() {
		return object;
	}

	public void setObject(EObject object) {
		this.object = object;
	}
	
}

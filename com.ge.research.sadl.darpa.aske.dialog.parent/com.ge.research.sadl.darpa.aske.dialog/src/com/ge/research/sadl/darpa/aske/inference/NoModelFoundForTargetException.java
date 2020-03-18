package com.ge.research.sadl.darpa.aske.inference;

import com.ge.research.sadl.model.gp.Node;

/**
 * Class to report an error inferring an answer due to failure to find a model for the specified target.
 * @author 200005201
 *
 */
public class NoModelFoundForTargetException extends DialogInferenceException {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private Node target = null;

	public NoModelFoundForTargetException(String msg, Node target) {
		super(msg);
		setTarget(target);
	}
	
	public NoModelFoundForTargetException(String msg, Throwable cause, Node target) {
		super(msg, cause);
		setTarget(target);
	}
	
	public Node getTarget() {
		return target;
	}
	
	public void setTarget(Node target) {
		this.target = target;
	}

}

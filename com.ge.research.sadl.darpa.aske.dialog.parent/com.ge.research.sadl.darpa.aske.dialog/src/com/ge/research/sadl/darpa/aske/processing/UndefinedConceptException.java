package com.ge.research.sadl.darpa.aske.processing;

/**
 * Class to report that a concept is undefined and pass that error up
 * to a level that can deal with it correctly
 * @author 200005201
 *
 */
public class UndefinedConceptException extends Exception {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private WhatIsContent whatIsContent;

	public UndefinedConceptException(String msg, WhatIsContent wic) {
		super(msg);
		setWhatIsContent(wic);
	}
	
	public WhatIsContent getWhatIsContent() {
		return whatIsContent;
	}

	public void setWhatIsContent(WhatIsContent whatIsContent) {
		this.whatIsContent = whatIsContent;
	}
	
	
}

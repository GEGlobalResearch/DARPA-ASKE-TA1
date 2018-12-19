/*
 * © 2014-2017 General Electric Company – All Rights Reserved
 * 
 * This software and any accompanying data and documentation are CONFIDENTIAL 
 * INFORMATION of the General Electric Company (“GE�?) and may contain trade secrets 
 * and other proprietary information.  It is intended for use solely by GE and authorized 
 * personnel.
 */
package com.ge.research.sadl.darpa.aske

import com.ge.research.sadl.processing.ISadlImplicitModelFragmentProvider

/**
 * Implicit model fragment provider for SRL and SADL. This service is used for 
 * appending additional content to the implicit SADL model. 
 * 
 * <p>
 * This class is registered as an Eclipse-based extension point and as an SPI to support both
 * the headless and the Eclipse platform service discovery.
 */
class DialogImplicitModelFragmentProvider implements ISadlImplicitModelFragmentProvider {

	public static val DIALOG_IMPLICIT_MODEL_FRAGMENT = '''
	''';

	@Override
	override getFragmentToAppend() {
		return DIALOG_IMPLICIT_MODEL_FRAGMENT;
	}

}

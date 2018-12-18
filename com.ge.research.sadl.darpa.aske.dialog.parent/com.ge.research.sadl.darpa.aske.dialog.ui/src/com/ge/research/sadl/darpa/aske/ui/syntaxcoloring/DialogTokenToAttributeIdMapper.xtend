package com.ge.research.sadl.darpa.aske.ui.syntaxcoloring
/*
 * © 2014-2016 General Electric Company – All Rights Reserved
 *
 * This software and any accompanying data and documentation are CONFIDENTIAL 
 * INFORMATION of the General Electric Company (“GE�?) and may contain trade secrets 
 * and other proprietary information.  It is intended for use solely by GE and authorized 
 * personnel.
 */

import org.eclipse.xtext.ide.editor.syntaxcoloring.DefaultAntlrTokenToAttributeIdMapper
import com.ge.research.sadl.darpa.aske.parser.antlr.internal.InternalDialogParser

class DialogTokenToAttributeIdMapper extends DefaultAntlrTokenToAttributeIdMapper {
	// Sadl.xtext defines only two new token terminals: NUMBER and EOS.
	// EOS is just punctuation, so we don't need another style for it.
	override protected String calculateId(String tokenName, int tokenType) {
		// We could hardcode the comparison to use "RULE_NUMBER" but
		// we want to ensure a compilation error if someone renames 
		// the NUMBER terminal without updating this place.
		var String ruleNumber = {
			val _rdIndx_tmpNode = InternalDialogParser.RULE_NUMBER //UNSIGNED_NUMBER
			InternalDialogParser.tokenNames.get(_rdIndx_tmpNode)
		}
		if (ruleNumber.equals(tokenName)) {
			return DialogHighlightingConfiguration.NUMBER_ID
		}
		return super.calculateId(tokenName, tokenType)
	}
}

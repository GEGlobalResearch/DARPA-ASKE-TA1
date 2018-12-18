package com.ge.research.sadl.darpa.aske.ui.syntaxcoloring;

import com.ge.research.sadl.darpa.aske.parser.antlr.internal.InternalDialogParser;
import com.ge.research.sadl.darpa.aske.ui.syntaxcoloring.DialogHighlightingConfiguration;
import org.eclipse.xtext.ide.editor.syntaxcoloring.DefaultAntlrTokenToAttributeIdMapper;

@SuppressWarnings("all")
public class DialogTokenToAttributeIdMapper extends DefaultAntlrTokenToAttributeIdMapper {
  @Override
  protected String calculateId(final String tokenName, final int tokenType) {
    String _xblockexpression = null;
    {
      final int _rdIndx_tmpNode = InternalDialogParser.RULE_NUMBER;
      _xblockexpression = InternalDialogParser.tokenNames[_rdIndx_tmpNode];
    }
    String ruleNumber = _xblockexpression;
    boolean _equals = ruleNumber.equals(tokenName);
    if (_equals) {
      return DialogHighlightingConfiguration.NUMBER_ID;
    }
    return super.calculateId(tokenName, tokenType);
  }
}

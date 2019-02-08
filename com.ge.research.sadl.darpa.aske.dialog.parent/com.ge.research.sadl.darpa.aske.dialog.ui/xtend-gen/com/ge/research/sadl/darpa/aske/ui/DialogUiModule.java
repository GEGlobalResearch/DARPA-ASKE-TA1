/**
 * generated by Xtext 2.14.0.RC1
 */
package com.ge.research.sadl.darpa.aske.ui;

import com.ge.research.sadl.darpa.aske.ui.AbstractDialogUiModule;
import com.ge.research.sadl.darpa.aske.ui.answer.DialogAnswerProvider;
import com.ge.research.sadl.darpa.aske.ui.syntaxcoloring.DialogHighlightingConfiguration;
import com.ge.research.sadl.darpa.aske.ui.syntaxcoloring.DialogSemanticHighlightingCalculator;
import com.ge.research.sadl.darpa.aske.ui.syntaxcoloring.DialogTokenToAttributeIdMapper;
import com.ge.research.sadl.ui.utils.EclipseSadlProjectHelper;
import com.ge.research.sadl.utils.SadlProjectHelper;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor;
import org.eclipse.xtext.ide.editor.syntaxcoloring.AbstractAntlrTokenToAttributeIdMapper;
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator;
import org.eclipse.xtext.ui.editor.autoedit.DefaultAutoEditStrategyProvider;
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration;

/**
 * Use this class to register components to be used within the Eclipse IDE.
 */
@FinalFieldsConstructor
@SuppressWarnings("all")
public class DialogUiModule extends AbstractDialogUiModule {
  public Class<? extends IHighlightingConfiguration> bindILexicalHighlightingConfiguration() {
    return DialogHighlightingConfiguration.class;
  }
  
  public Class<? extends ISemanticHighlightingCalculator> bindISemanticHighlightingCalculator() {
    return DialogSemanticHighlightingCalculator.class;
  }
  
  public Class<? extends AbstractAntlrTokenToAttributeIdMapper> bindTokenToAttributeIdMapper() {
    return DialogTokenToAttributeIdMapper.class;
  }
  
  public Class<? extends DefaultAutoEditStrategyProvider> bindDefaultAutoEditStrategyProvider() {
    return DialogAnswerProvider.class;
  }
  
  public Class<? extends SadlProjectHelper> bindSadlProjectHelper() {
    return EclipseSadlProjectHelper.class;
  }
  
  public DialogUiModule(final AbstractUIPlugin plugin) {
    super(plugin);
  }
}

/********************************************************************** 
 * Note: This license has also been called the "New BSD License" or 
 * "Modified BSD License". See also the 2-clause BSD License.
 *
 * Copyright © 2018-2019 - General Electric Company, All Rights Reserved
 * 
 * Projects: ANSWER and KApEESH, developed with the support of the Defense 
 * Advanced Research Projects Agency (DARPA) under Agreement  No.  
 * HR00111990006 and Agreement No. HR00111990007, respectively. 
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 ***********************************************************************/
package com.ge.research.sadl.darpa.aske.ui

import com.ge.research.sadl.darpa.aske.ui.answer.DialogAnswerProvider
import com.ge.research.sadl.darpa.aske.ui.contentassist.DialogOntologyContextProvider
import com.ge.research.sadl.darpa.aske.ui.preferences.DialogPreferencesInitializer
import com.ge.research.sadl.darpa.aske.ui.preferences.DialogRootPreferencePage
import com.ge.research.sadl.darpa.aske.ui.syntaxcoloring.DialogHighlightingConfiguration
import com.ge.research.sadl.darpa.aske.ui.syntaxcoloring.DialogSemanticHighlightingCalculator
import com.ge.research.sadl.darpa.aske.ui.syntaxcoloring.DialogTokenToAttributeIdMapper
import com.ge.research.sadl.ide.editor.contentassist.IOntologyContextProvider
import com.ge.research.sadl.ui.contentassist.SadlReferenceProposalCreator
import com.google.inject.Binder
import com.google.inject.name.Names
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.ide.editor.syntaxcoloring.AbstractAntlrTokenToAttributeIdMapper
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator
import org.eclipse.xtext.ui.editor.autoedit.DefaultAutoEditStrategyProvider
import org.eclipse.xtext.ui.editor.contentassist.AbstractJavaBasedContentProposalProvider.ReferenceProposalCreator
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreInitializer
import org.eclipse.xtext.ui.editor.preferences.LanguageRootPreferencePage
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration

/**
 * Use this class to register components to be used within the Eclipse IDE.
 */
@FinalFieldsConstructor
class DialogUiModule extends AbstractDialogUiModule {
		// Registers our own syntax coloring styles.
	def Class<? extends IHighlightingConfiguration> bindILexicalHighlightingConfiguration() {
		return DialogHighlightingConfiguration
	}

	// Maps our Ecore nodes to our syntax coloring styles.
	def Class<? extends ISemanticHighlightingCalculator> bindISemanticHighlightingCalculator() {
		return DialogSemanticHighlightingCalculator
	}

	// Maps our token names to our syntax coloring styles.
	def Class<? extends AbstractAntlrTokenToAttributeIdMapper> bindTokenToAttributeIdMapper() {
		return DialogTokenToAttributeIdMapper
	}
	
	def Class<? extends DefaultAutoEditStrategyProvider> bindDefaultAutoEditStrategyProvider() {
		return DialogAnswerProvider
	}

	def void configurePreferenceInitializer(Binder binder) {
		binder.bind(IPreferenceStoreInitializer).annotatedWith(Names.named("dialogPreferenceInitializer")).to(
			DialogPreferencesInitializer)
	}

	// registers our own root preference page.
	def Class<? extends LanguageRootPreferencePage> bindLanguageRootPreferencePage() {
		return DialogRootPreferencePage
	}

	def Class<? extends ReferenceProposalCreator> bindReferenceProposalCreator() {
		return SadlReferenceProposalCreator;
	}

	def Class<? extends IOntologyContextProvider> bindIOntologyContextProvider() {
		return DialogOntologyContextProvider;
	}
	
}

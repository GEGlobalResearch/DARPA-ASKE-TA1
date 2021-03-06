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

import com.ge.research.sadl.darpa.aske.ide.editor.contentassist.DialogIdeContentProposalProvider
import com.ge.research.sadl.darpa.aske.ide.editor.contentassist.DialogIdeCrossrefProposalProvider
import com.ge.research.sadl.darpa.aske.ui.answer.DialogAnswerProviders
import com.ge.research.sadl.darpa.aske.ui.answer.IDialogAnswerProviders
import com.ge.research.sadl.darpa.aske.ui.contentassist.DialogOntologyContextProvider
import com.ge.research.sadl.darpa.aske.ui.editor.folding.DialogFoldingFoldingRegionProvider
import com.ge.research.sadl.darpa.aske.ui.editor.folding.DialogFoldingStructureProvider
import com.ge.research.sadl.darpa.aske.ui.preferences.DialogPreferencesInitializer
import com.ge.research.sadl.darpa.aske.ui.preferences.DialogRootPreferencePage
import com.ge.research.sadl.darpa.aske.ui.syntaxcoloring.DialogHighlightingConfiguration
import com.ge.research.sadl.darpa.aske.ui.syntaxcoloring.DialogSemanticHighlightingCalculator
import com.ge.research.sadl.darpa.aske.ui.syntaxcoloring.DialogTokenToAttributeIdMapper
import com.ge.research.sadl.ide.editor.contentassist.IOntologyContextProvider
import com.ge.research.sadl.refactoring.RefactoringHelper
import com.ge.research.sadl.ui.contentassist.SadlReferenceProposalCreator
import com.ge.research.sadl.ui.outline.NoopOutlineRefreshJob
import com.ge.research.sadl.ui.refactoring.EclipseRefactoringHelper
import com.ge.research.sadl.ui.refactoring.SadlReferenceUpdater
import com.ge.research.sadl.ui.refactoring.SadlRenameContextFactory
import com.ge.research.sadl.ui.refactoring.SadlRenameRefactoringController
import com.ge.research.sadl.ui.refactoring.SadlRenameRefactoringExecuter
import com.ge.research.sadl.ui.refactoring.SadlResourceRenameStrategy
import com.google.inject.Binder
import com.google.inject.Provider
import com.google.inject.name.Names
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.ide.editor.contentassist.IdeContentProposalProvider
import org.eclipse.xtext.ide.editor.contentassist.IdeCrossrefProposalProvider
import org.eclipse.xtext.ide.editor.syntaxcoloring.AbstractAntlrTokenToAttributeIdMapper
import org.eclipse.xtext.ide.editor.syntaxcoloring.ISemanticHighlightingCalculator
import org.eclipse.xtext.service.SingletonBinding
import org.eclipse.xtext.ui.editor.contentassist.AbstractJavaBasedContentProposalProvider.ReferenceProposalCreator
import org.eclipse.xtext.ui.editor.folding.DefaultFoldingRegionProvider
import org.eclipse.xtext.ui.editor.folding.DefaultFoldingStructureProvider
import org.eclipse.xtext.ui.editor.folding.IFoldingRegionProvider
import org.eclipse.xtext.ui.editor.folding.IFoldingStructureProvider
import org.eclipse.xtext.ui.editor.outline.impl.OutlineRefreshJob
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreInitializer
import org.eclipse.xtext.ui.editor.preferences.LanguageRootPreferencePage
import org.eclipse.xtext.ui.editor.syntaxcoloring.IHighlightingConfiguration
import org.eclipse.xtext.ui.refactoring.IRenameStrategy
import org.eclipse.xtext.ui.refactoring.ui.IRenameContextFactory
import org.eclipse.xtext.ui.refactoring.ui.RenameRefactoringController
import org.eclipse.xtext.ui.refactoring.ui.RenameRefactoringExecuter

/**
 * Use this class to register components to be used within the Eclipse IDE.
 */
@FinalFieldsConstructor
class DialogUiModule extends AbstractDialogUiModule {

	def Class<? extends IHighlightingConfiguration> bindILexicalHighlightingConfiguration() {
		return DialogHighlightingConfiguration
	}

	def Class<? extends ISemanticHighlightingCalculator> bindISemanticHighlightingCalculator() {
		return DialogSemanticHighlightingCalculator
	}

	def Class<? extends AbstractAntlrTokenToAttributeIdMapper> bindTokenToAttributeIdMapper() {
		return DialogTokenToAttributeIdMapper
	}

	def void configurePreferenceInitializer(Binder binder) {
		binder.bind(IPreferenceStoreInitializer).annotatedWith(Names.named("dialogPreferenceInitializer")).to(
			DialogPreferencesInitializer)
	}

	def Class<? extends LanguageRootPreferencePage> bindLanguageRootPreferencePage() {
		return DialogRootPreferencePage
	}

	def Class<? extends ReferenceProposalCreator> bindReferenceProposalCreator() {
		return SadlReferenceProposalCreator
	}

	def Class<? extends IOntologyContextProvider> bindIOntologyContextProvider() {
		return DialogOntologyContextProvider
	}

	def Class<? extends IdeContentProposalProvider> bindIdeContentProposalProvider() {
		return DialogIdeContentProposalProvider
	}

	def Class<? extends IdeCrossrefProposalProvider> bindIdeCrossrefProposalProvider() {
		return DialogIdeCrossrefProposalProvider
	}

	@SingletonBinding(eager=true)
	def Class<? extends IDialogAnswerProviders> bindIDialogEditorStateManager() {
		return DialogAnswerProviders
	}

	def Class<? extends IFoldingStructureProvider> bindIFoldingStructureProvider() {
		return DialogFoldingStructureProvider;
	}

	def Class<? extends DefaultFoldingStructureProvider> bindDefaultFoldingStructureProvider() {
		return DialogFoldingStructureProvider;
	}

	def Class<? extends IFoldingRegionProvider> bindIFoldingRegionProvider() {
		return DialogFoldingFoldingRegionProvider;
	}

	def Class<? extends DefaultFoldingRegionProvider> bindDefaultFoldingRegionProvider() {
		return DialogFoldingFoldingRegionProvider;
	}

	override Class<? extends IRenameStrategy> bindIRenameStrategy() {
		return SadlResourceRenameStrategy;
	}

	override bindIReferenceUpdater() {
		return SadlReferenceUpdater;
	}

	def Class<? extends IRenameContextFactory> bindIRenameContextFactory() {
		return SadlRenameContextFactory;
	}

	def Provider<? extends RefactoringHelper> provideRefactoringHelper() {
		return [
			EclipseRefactoringHelper.INSTANCE
		];
	}

	def Provider<? extends EclipseRefactoringHelper> provideEclipseRefactoringHelper() {
		return [
			EclipseRefactoringHelper.INSTANCE
		];
	}

	def Class<? extends RenameRefactoringController> bindRenameRefactoringController() {
		return SadlRenameRefactoringController;
	}

	def Class<? extends OutlineRefreshJob> bindOutlineRefreshJob() {
		return NoopOutlineRefreshJob;
	}

	def Class<? extends RenameRefactoringExecuter> bindRenameRefactoringExecuter() {
		return SadlRenameRefactoringExecuter;
	}

}

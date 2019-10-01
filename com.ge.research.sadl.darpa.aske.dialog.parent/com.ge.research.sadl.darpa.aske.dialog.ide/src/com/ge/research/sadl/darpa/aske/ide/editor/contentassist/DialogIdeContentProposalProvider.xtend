package com.ge.research.sadl.darpa.aske.ide.editor.contentassist

import com.ge.research.sadl.darpa.aske.services.DialogGrammarAccess
import com.ge.research.sadl.ide.editor.contentassist.SadlIdeContentProposalProvider
import com.google.inject.Inject
import org.eclipse.xtext.Assignment
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.ide.editor.contentassist.IIdeContentProposalAcceptor

class DialogIdeContentProposalProvider extends SadlIdeContentProposalProvider {

	@Inject
	DialogGrammarAccess grammarAccess;

	override protected _createProposals(Assignment assignment, ContentAssistContext ctx,
		IIdeContentProposalAcceptor acceptor) {

		if(excludedNamespaces !== null) excludedNamespaces.clear
		if(typeRestrictions !== null) typeRestrictions.clear
		switch (assignment) {
			case grammarAccess.targetModelNameAccess.targetResourceAssignment_2: {
				ctx.completeImports(acceptor);
			}
			case grammarAccess.targetModelNameAccess.aliasAssignment_3_1: {
				ctx.completeAlias(acceptor);
			}
			default: {
				super._createProposals(assignment, ctx, acceptor);
			}
		}
	}

}

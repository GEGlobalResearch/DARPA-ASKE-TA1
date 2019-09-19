package com.ge.research.sadl.darpa.aske.ide.editor.contentassist

import com.ge.research.sadl.ide.editor.contentassist.SadlIdeCrossrefProposalProvider
import org.eclipse.xtext.CrossReference
import org.eclipse.xtext.ide.editor.contentassist.ContentAssistContext
import org.eclipse.xtext.resource.IEObjectDescription

import static com.ge.research.sadl.darpa.aske.dialog.DialogPackage.Literals.*
import static com.ge.research.sadl.sADL.SADLPackage.Literals.*

class DialogIdeCrossrefProposalProvider extends SadlIdeCrossrefProposalProvider {

	override protected createProposal(IEObjectDescription candidate, CrossReference crossRef,
		ContentAssistContext context) {

		// Need to escape the import with double quotes.
		if (SADL_MODEL == candidate.EClass && SADL_MODEL == crossRef?.type.classifier &&
			TARGET_MODEL_NAME == context?.currentModel.eClass) {

			val proposal = '''"«qualifiedNameConverter.toString(candidate.name)»"''';
			return proposalCreator.createProposal(proposal, context) [
				source = candidate
				description = candidate.getEClass?.name
			];

		} else {
			super.createProposal(candidate, crossRef, context)
		}
	}
}

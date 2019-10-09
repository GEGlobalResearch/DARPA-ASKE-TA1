package com.ge.research.sadl.darpa.aske.ui.editor.folding

import com.ge.research.sadl.darpa.aske.dialog.AnswerCMStatement
import com.ge.research.sadl.sADL.ExternalEquationStatement
import org.eclipse.emf.ecore.EObject
import org.eclipse.xtext.nodemodel.util.NodeModelUtils
import org.eclipse.xtext.ui.editor.folding.DefaultFoldingRegionProvider
import org.eclipse.xtext.ui.editor.folding.IFoldingRegionAcceptor
import org.eclipse.xtext.util.ITextRegion

/**
 * Customized folding region provider for {@code .dialog} resources.
 * To fold only the really long-winded "answer" from code or text extraction.
 * 
 * @see https://github.com/GEGlobalResearch/DARPA-ASKE-TA1/issues/35#issuecomment-535686568
 */
class DialogFoldingFoldingRegionProvider extends DefaultFoldingRegionProvider {

	dispatch def void computeObjectFolding(EObject it, IFoldingRegionAcceptor<ITextRegion> acceptor,
		boolean initiallyFolded) {

		super.computeObjectFolding(it, acceptor, initiallyFolded)
	}

	dispatch def void computeObjectFolding(Void it, IFoldingRegionAcceptor<ITextRegion> acceptor,
		boolean initiallyFolded) {
		// null-guard
	}

	dispatch def void computeObjectFolding(AnswerCMStatement it, IFoldingRegionAcceptor<ITextRegion> acceptor,
		boolean initiallyFolded) {

		super.computeObjectFolding(it, acceptor, initiallyFolded || shouldFoldAnswer)
	}

	private def boolean shouldFoldAnswer(AnswerCMStatement it) {
		if (sstmt instanceof ExternalEquationStatement) {
			return false
		}

		val node = NodeModelUtils.findActualNodeFor(it)
		if (node === null) {
			return false
		}

		// This magic 180 value should be tuned. It is just an example for `Mach.java`.
		// The range of the last statement should not be collapsed
		// as the node length does not exceeds 180 characters.
		return node.length > 180
	}

}

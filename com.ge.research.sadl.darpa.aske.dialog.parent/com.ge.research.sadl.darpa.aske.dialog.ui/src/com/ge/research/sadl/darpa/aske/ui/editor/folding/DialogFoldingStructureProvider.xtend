package com.ge.research.sadl.darpa.aske.ui.editor.folding

import com.ge.research.sadl.ui.editor.folding.PatchedDefaultFoldingStructureProvider
import org.eclipse.jface.text.source.projection.ProjectionAnnotationModel
import java.util.Collection
import org.eclipse.xtext.ui.editor.folding.FoldedPosition
import org.eclipse.jface.text.source.Annotation

/**
 * Folding structure provider which always collapses regions on update.
 */
class DialogFoldingStructureProvider extends PatchedDefaultFoldingStructureProvider {

	override protected updateFoldingRegions(boolean allowCollapse, ProjectionAnnotationModel model,
		Collection<FoldedPosition> foldedPositions, Annotation[] deletions) {

		super.updateFoldingRegions(true, model, foldedPositions, deletions)
	}

}

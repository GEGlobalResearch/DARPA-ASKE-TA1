package com.ge.research.sadl.darpa.aske.ui.editor

import org.eclipse.emf.common.util.URI
import org.eclipse.swt.widgets.Display
import org.eclipse.ui.IFileEditorInput
import org.eclipse.ui.PlatformUI
import org.eclipse.xtend.lib.annotations.Accessors
import org.eclipse.xtend.lib.annotations.FinalFieldsConstructor
import org.eclipse.xtext.ui.editor.XtextEditor

import static com.ge.research.sadl.darpa.aske.ui.answer.DialogAnswerProviders.DIALOG_EDITOR_ID

class DialogEditors {

	static def asyncExec(Options options, Procedure procedure) {
		val uri = options.uri
		Display.^default.asyncExec([
			val page = PlatformUI.workbench.activeWorkbenchWindow.activePage
			for (editorRef : page.editorReferences.filter[id == DIALOG_EDITOR_ID]) {
				val input = editorRef.editorInput
				if (input instanceof IFileEditorInput) {
					val path = input.file.fullPath.toPortableString
					val editorUri = URI.createPlatformResourceURI(path, true)
					if (editorUri == uri) {
						val part = editorRef.getPart(false)
						if (part instanceof XtextEditor) {
							if (options.activate) {
								page.activate(part)
								part.setFocus
							}
							procedure.apply(part)
							return
						}
					}
				}
			}
		])
	}

	static interface Procedure {
		def void apply(XtextEditor editor)
	}

	@Accessors
	@FinalFieldsConstructor
	static class Options {
		val URI uri
		var boolean activate
	}

}

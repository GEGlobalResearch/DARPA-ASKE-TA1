package com.ge.research.sadl.darpa.aske.ui.answer

import com.ge.research.sadl.darpa.aske.processing.IDialogAnswerProvider
import com.google.common.collect.ImmutableList
import com.google.inject.Inject
import com.google.inject.Provider
import java.util.Map
import org.eclipse.emf.common.util.URI
import org.eclipse.swt.widgets.Display
import org.eclipse.ui.IEditorReference
import org.eclipse.ui.IFileEditorInput
import org.eclipse.ui.IPartListener2
import org.eclipse.ui.IWorkbenchPartReference
import org.eclipse.ui.PlatformUI
import org.eclipse.xtext.ui.editor.XtextEditor
import org.eclipse.xtext.ui.editor.model.IXtextDocument

class DialogAnswerProviders implements IPartListener2, IDialogAnswerProviders {

	public static val DIALOG_EDITOR_ID = 'com.ge.research.sadl.darpa.aske.Dialog'

	@Inject
	Provider<DialogAnswerProvider> factory
	val Map<URI, IDialogAnswerProvider> providers

	new() {
		providers = newHashMap
		Display.^default.asyncExec [
			// Before we register this as a part listener, we iterate through all opened editors
			// and register the answer providers for all opened `Dialog` editors.
			val workbench = PlatformUI.workbench
			val window = workbench.activeWorkbenchWindow
			val dialogDocuments = window.activePage.editorReferences.map[dialogDocument].filterNull
			for (document : dialogDocuments) {
				register(document)
			}
			PlatformUI.workbench.activeWorkbenchWindow.partService.addPartListener(this)
		]
	}

	override getAllProviders() {
		return ImmutableList.copyOf(providers.values)
	}

	override getProvider(URI uri) {
		return providers.get(uri)
	}

	def void register(IXtextDocument document) {
		val uri = document.uri
		if (!providers.containsKey(uri)) {
			println('''Registering new answer provider for editor: «uri»''')
			val provider = factory.get
			try {
				provider.configure(document)
			} catch (Exception e) {
				println('''ERROR: failed to configure answer provider «uri»''')
				e.printStackTrace
			}
			providers.put(uri, provider)
		} else {
			println('''WARN: answer provider was already registered for «uri»''')
		}
	}

	def void unregister(URI uri) {
		val provider = providers.remove(uri)
		if (provider === null) {
			println('''WARN: answer provider was not registered for «uri»''')
		} else {
			println('''Unregistering and disposing answer provider for editor: «uri»''')
			provider.dispose
		}
	}

	override partClosed(IWorkbenchPartReference partRef) {
		val document = partRef.dialogDocument
		if (document !== null) {
			document.uri.unregister
		}
	}

	override partOpened(IWorkbenchPartReference partRef) {
		val document = partRef.dialogDocument
		if (document !== null) {
			val xtduri = getUri(document)
			document.register
			val provider = getProvider(xtduri)
			val rsrc = provider.resource
			val i = 0
		}
	}

	override partDeactivated(IWorkbenchPartReference partRef) {
		// NOOP
	}

	override partHidden(IWorkbenchPartReference partRef) {
		// NOOP
	}

	override partInputChanged(IWorkbenchPartReference partRef) {
		// NOOP
	}

	override partActivated(IWorkbenchPartReference partRef) {
		// NOOP
	}

	override partBroughtToTop(IWorkbenchPartReference partRef) {
		// NOOP
	}

	override partVisible(IWorkbenchPartReference partRef) {
		// NOOP
	}

	/**
	 * Returns with the URI of the underlying dialog model, if the part reference is an
	 * Xtext editor opened for a dialog resource. Otherwise, returns {@code null}.
	 */
	def private IXtextDocument getDialogDocument(IWorkbenchPartReference it) {
		if (id != DIALOG_EDITOR_ID) {
			return null
		}
		if (it instanceof IEditorReference) {
			val input = editorInput
			if (input instanceof IFileEditorInput) {
				val path = input.file.fullPath
				val uri = URI.createPlatformResourceURI(path.toPortableString, true)
				if (uri.fileExtension == 'dialog') {
					val editor = getPart(false)
					if (editor instanceof XtextEditor) {
						return editor.document
					}
				}
			}
		}
		return null
	}

	def private URI getUri(IXtextDocument document) {
		return document.readOnly(GetResourceUri.INSTANCE)
	}

}

interface IDialogAnswerProviders {

	def Iterable<IDialogAnswerProvider> getAllProviders()

	def IDialogAnswerProvider getProvider(URI uri)

}

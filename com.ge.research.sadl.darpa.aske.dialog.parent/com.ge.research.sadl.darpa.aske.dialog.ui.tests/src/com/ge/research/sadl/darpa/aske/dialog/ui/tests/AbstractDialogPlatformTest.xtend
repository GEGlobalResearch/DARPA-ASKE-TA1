package com.ge.research.sadl.darpa.aske.dialog.ui.tests

import com.ge.research.sadl.darpa.aske.ui.tests.DialogUiInjectorProvider
import com.ge.research.sadl.jena.IJenaBasedModelProcessor
import com.ge.research.sadl.model.gp.Rule
import com.ge.research.sadl.model.gp.SadlCommand
import com.ge.research.sadl.tests.SadlTestAssertions
import com.ge.research.sadl.ui.tests.AbstractSadlPlatformTest
import org.apache.jena.ontology.OntModel
import java.util.List
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.ui.editor.preferences.IPreferenceStoreAccess
import org.eclipse.xtext.validation.Issue
import org.junit.runner.RunWith

import static com.google.common.base.Preconditions.*

@RunWith(XtextRunner)
@InjectWith(DialogUiInjectorProvider)
abstract class AbstractDialogPlatformTest extends AbstractSadlPlatformTest {

	protected def Resource assertValidatesDialogTo(Resource resource,
		(OntModel, List<Rule>, List<SadlCommand>, List<Issue>, IJenaBasedModelProcessor)=>void assertions) {

		return SadlTestAssertions.assertValidatesTo(resource as XtextResource, assertions)
	}

	override protected getPreferenceStore(String preferenceKey) {
		val store = super.getPreferenceStore(preferenceKey)
		if (store.contains(preferenceKey)) {
			return store
		}
		// We assume, it is a SADL preference as it was not in the `Dialog`-specific preference store.
		// We cannot use injection here, as we are in a `Dialog` test so we have to assume, the SADL built-ins are in the project.
		// We load the of the built-in implicit SADL model and acquire the SADL preference store from the `XtextResource`'s service provider.
		val resource = ('ImplicitModel/SadlImplicitModel.sadl'.file.resource as XtextResource)
		val storeAccess = resource.resourceServiceProvider.get(IPreferenceStoreAccess)
		val sadlStore = checkNotNull(storeAccess.writablePreferenceStore, 'Could not get preference store for SADL.')
		checkState(
			sadlStore.contains(preferenceKey),
			'''Could not find the '«preferenceKey»' preference key in the SADL store.'''
		)
		return sadlStore
	}

}

package com.ge.research.sadl.darpa.aske.tests

import com.ge.research.sadl.sADL.SadlModel
import com.google.inject.Inject
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.testing.util.ParseHelper
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(XtextRunner)
@InjectWith(DialogInjectorProvider)
class DialogParsingTest {

	@Inject
	extension ParseHelper<SadlModel> parseHelper

	@Test
	def void evalStatement_0_params() {
		'''
			uri "http://test".
			Evaluate CAL_SOS().
		'''.assertNoErrors
	}

	@Test
	def void evalStatement_1_params() {
		'''
			uri "http://test".
			Evaluate CAL_SOS(1).
		'''.assertNoErrors
	}

	@Test
	def void evalStatement_2_params() {
		'''
			uri "http://test".
			Evaluate CAL_SOS(1, 2).
		'''.assertNoErrors
	}

	private def assertNoErrors(CharSequence s) {
		val model = s.parse
		Assert.assertNotNull(model)
		val errors = model.eResource.errors
		Assert.assertTrue('''Unexpected errors: «errors.join(", ")»''', errors.isEmpty)
	}

}
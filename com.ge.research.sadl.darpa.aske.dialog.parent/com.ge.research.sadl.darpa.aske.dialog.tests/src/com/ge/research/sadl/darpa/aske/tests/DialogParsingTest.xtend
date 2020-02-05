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

	@Test
	def void evalStatement_doubleNoUnits() {
		'''
			uri "http://test".
			Evaluate CAL_SOS(245.6, 1.4, 8.314, 3056).
		'''.assertNoErrors
	}

	@Test
	def void evalStatement_doubleWithUnits() {
		'''
			uri "http://test".
			Evaluate CAL_SOS(245.6 {cm}, 1.4 {cm}, 8.314 {cm}, 3056 {cm}).
		'''.assertNoErrors
	}

	@Test
	def void evalStatement_with() {
		'''
			uri "http://test".
			MyRect is a Rectangle with ^length 5, with width 2.
			Evaluate CAL_SOS(2, width of MyRect, ^length of MyRect, 3.14).
		'''.assertNoErrors
	}

	private def assertNoErrors(CharSequence s) {
		val model = s.parse
		Assert.assertNotNull(model)
		val errors = model.eResource.errors
		Assert.assertTrue('''Unexpected errors: «errors.join(", ")»''', errors.isEmpty)
	}

}

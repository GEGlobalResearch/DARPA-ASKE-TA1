package com.ge.research.sadl.darpa.aske.tests

import com.ge.research.sadl.jena.IJenaBasedModelProcessor
import com.ge.research.sadl.model.gp.Rule
import com.ge.research.sadl.model.gp.SadlCommand
import com.ge.research.sadl.tests.AbstractSadlTest
import com.ge.research.sadl.tests.SadlTestAssertions
import com.hp.hpl.jena.ontology.OntModel
import java.util.List
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.testing.InjectWith
import org.eclipse.xtext.testing.XtextRunner
import org.eclipse.xtext.validation.Issue
import org.junit.runner.RunWith
import org.junit.Before
import com.ge.research.sadl.SADLStandaloneSetup

@RunWith(XtextRunner)
@InjectWith(DialogInjectorProvider)
abstract class AbstractDialogTest extends AbstractSadlTest {
	
	@Before
	override initialize() {
		SADLStandaloneSetup.doSetup
		super.initialize()
	}

	protected def XtextResource dialog(CharSequence seq) {
		return resource(seq, 'dialog') as XtextResource;
	}

	protected def Resource assertValidatesTo(CharSequence code,
		(OntModel, List<Issue>, IJenaBasedModelProcessor)=>void assertions) {

		return SadlTestAssertions.assertValidatesTo(code.dialog, [ ontModel, rules, commands, issues, processor |
			assertions.apply(ontModel, issues, processor)
		]);
	}

	override Resource assertValidatesTo(CharSequence code,
		(OntModel, List<Rule>, List<SadlCommand>, List<Issue>, IJenaBasedModelProcessor)=>void assertions) {

		return SadlTestAssertions.assertValidatesTo(code.dialog, assertions);
	}

	protected def Resource assertValidatesSadlTo(CharSequence code,
		(OntModel, List<Rule>, List<SadlCommand>, List<Issue>, IJenaBasedModelProcessor)=>void assertions) {

		return SadlTestAssertions.assertValidatesTo(code.sadl, assertions);
	}

}

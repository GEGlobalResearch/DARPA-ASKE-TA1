package com.ge.research.sadl.darpa.aske.dialog.ui.tests

import com.hp.hpl.jena.ontology.OntModel
import java.util.List
import org.eclipse.emf.ecore.resource.Resource
import org.eclipse.xtext.resource.XtextResource
import org.eclipse.xtext.validation.Issue
import com.ge.research.sadl.ui.tests.AbstractSadlPlatformTest
import com.ge.research.sadl.model.gp.SadlCommand
import com.ge.research.sadl.model.gp.Rule
import com.ge.research.sadl.jena.IJenaBasedModelProcessor
import com.ge.research.sadl.tests.SadlTestAssertions
import com.ge.research.sadl.darpa.aske.tests.AbstractDialogTest

abstract class AbstractDialogPlatformTest extends AbstractSadlPlatformTest {
	
	
	protected def Resource assertValidatesDialogTo(Resource resource,
		(OntModel, List<Rule>, List<SadlCommand>, List<Issue>, IJenaBasedModelProcessor)=>void assertions) {

		return SadlTestAssertions.assertValidatesTo(resource as XtextResource, assertions);
	}
	
	protected def Resource assertValidatesDialogTo(Resource resource, boolean rawResults, boolean treatAsConclusion,
		(OntModel, List<Object>, List<SadlCommand>, List<Issue>, IJenaBasedModelProcessor)=>void assertions) {
			
//			return AbstractDialogTest.assertValidatesTo(resource as XtextResource, assertions); 
		}

}

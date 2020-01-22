package com.ge.research.sadl.darpa.aske.tests;

import org.junit.Test

import static org.junit.Assert.*

class BasicMissingPatternsInCompareTest extends AbstractDialogTest {

	@Test
	def void test() {
		val sadlModel = '''
				uri "http://sadl.org/Model.sadl" alias mdl.
							
				AircraftEngine is a class,
					described by sfc (alias "specific fuel consumption") with values of type float,
					described by thrust with values of type float.
				
				{CF6, F100, RamJet} are types of AircraftEngine.
				
				Rule R1: if sfc is .35 then thrust is 2000.
			'''.assertValidatesSadlTo[ontModel, rules, cmds, issues, processor |
				assertNotNull(ontModel)
				assertNotNull(rules)
				assertTrue(rules.size == 1)
				val rtxt = rules.get(0).toDescriptiveString
				println(rtxt)
			]
		val dialogModel = '''
				 uri "http://sadl.org/MP1.dialog" alias MP1D.
				 
				 import "http://sadl.org/Model.sadl".
				 
				 Compare thrust when sfc is .35.		
		'''.assertValidatesTo[ontModel, issues, processor |
			for (issue : issues) {
				println(issue.toString)
			}	
		]
	}

}
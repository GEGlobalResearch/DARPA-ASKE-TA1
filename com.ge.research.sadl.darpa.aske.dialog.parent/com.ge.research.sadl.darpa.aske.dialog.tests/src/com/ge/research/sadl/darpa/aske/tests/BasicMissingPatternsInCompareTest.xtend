package com.ge.research.sadl.darpa.aske.tests;

import org.junit.Test

import static org.junit.Assert.*

class BasicMissingPatternsInCompareTest extends AbstractDialogTest {

	@Test
	def void test_01() {
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
			val expected ="INFO:Rule ComparePseudoRule0:  if rdf(v0, rdf:type, UnittedQuantity) and rdf(v0, value, 0.35) and rdf(v2, rdf:type, RamJet) and rdf(v2, mdl:sfc, v0) then rdf(v2, mdl:thrust, v1).
Rule ComparePseudoRule1:  if rdf(v0, rdf:type, UnittedQuantity) and rdf(v0, value, 0.35) and rdf(v4, rdf:type, F100) and rdf(v4, mdl:sfc, v0) then rdf(v4, mdl:thrust, v3).
Rule ComparePseudoRule2:  if rdf(v0, rdf:type, UnittedQuantity) and rdf(v0, value, 0.35) and rdf(v6, rdf:type, CF6) and rdf(v6, mdl:sfc, v0) then rdf(v6, mdl:thrust, v5).
 (synthetic://test/Resource3.dialog line : 5 column : 1)".replaceAll("\r\n", "\n");
			for (issue : issues) {
				println(issue.toString.trim)
			}	
			println(expected.trim);
			assertEquals(1, issues.size);
			assertEquals(expected.trim, issues.get(0).toString.trim);
		]
	}

	@Test
	def void test_02() {
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
				 
				 Compare thrust when sfc is .35 and when sfc is .45.		
		'''.assertValidatesTo[ontModel, issues, processor |
			val expected = "INFO:Rule ComparePseudoRule0:  if rdf(v0, rdf:type, UnittedQuantity) and rdf(v0, value, 0.35) and rdf(v3, rdf:type, RamJet) and rdf(v3, mdl:sfc, v0) then rdf(v3, mdl:thrust, v2).
Rule ComparePseudoRule1:  if rdf(v0, rdf:type, UnittedQuantity) and rdf(v0, value, 0.35) and rdf(v5, rdf:type, F100) and rdf(v5, mdl:sfc, v0) then rdf(v5, mdl:thrust, v4).
Rule ComparePseudoRule2:  if rdf(v0, rdf:type, UnittedQuantity) and rdf(v0, value, 0.35) and rdf(v7, rdf:type, CF6) and rdf(v7, mdl:sfc, v0) then rdf(v7, mdl:thrust, v6).
Rule ComparePseudoRule3:  if rdf(v1, rdf:type, UnittedQuantity) and rdf(v1, value, 0.45) and rdf(v9, rdf:type, RamJet) and rdf(v9, mdl:sfc, v1) then rdf(v9, mdl:thrust, v8).
Rule ComparePseudoRule4:  if rdf(v1, rdf:type, UnittedQuantity) and rdf(v1, value, 0.45) and rdf(v11, rdf:type, F100) and rdf(v11, mdl:sfc, v1) then rdf(v11, mdl:thrust, v10).
Rule ComparePseudoRule5:  if rdf(v1, rdf:type, UnittedQuantity) and rdf(v1, value, 0.45) and rdf(v13, rdf:type, CF6) and rdf(v13, mdl:sfc, v1) then rdf(v13, mdl:thrust, v12).
 (synthetic://test/Resource3.dialog line : 5 column : 1)".replaceAll("\r\n", "\n");
			for (issue : issues) {
				println(issue.toString.trim)
			}	
			println(expected.trim);
			assertEquals(1, issues.size);
			assertEquals(expected.trim, issues.get(0).toString.trim);
		]
	}
	
	@Test
	def void test_03() {
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
				 
				 Compare thrust of a CF6 when sfc is .35 and when sfc is .45.		
		'''.assertValidatesTo[ontModel, issues, processor |
			val expected = "INFO:Rule ComparePseudoRule0:  if rdf(v1, rdf:type, UnittedQuantity) and rdf(v1, value, 0.35) and rdf(v4, rdf:type, mdl:CF6) and rdf(v4, mdl:sfc, v1) then rdf(v4, mdl:thrust, v3).
Rule ComparePseudoRule1:  if rdf(v2, rdf:type, UnittedQuantity) and rdf(v2, value, 0.45) and rdf(v6, rdf:type, mdl:CF6) and rdf(v6, mdl:sfc, v2) then rdf(v6, mdl:thrust, v5).
 (synthetic://test/Resource3.dialog line : 5 column : 1)".replaceAll("\r\n", "\n");
			for (issue : issues) {
				println(issue.toString.trim)
			}	
			println(expected.trim);
			assertEquals(1, issues.size);
			assertEquals(expected.trim, issues.get(0).toString.trim);
		]
	}
	
}

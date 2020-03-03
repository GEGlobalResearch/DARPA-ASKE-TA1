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
			val expected = "INFO:Rule ComparePseudoRule0:  if rdf(v1, rdf:type, RamJet) and rdf(v1, mdl:sfc, 0.35) then rdf(v1, mdl:thrust, v0).\n" +
"Rule ComparePseudoRule1:  if rdf(v3, rdf:type, F100) and rdf(v3, mdl:sfc, 0.35) then rdf(v3, mdl:thrust, v2).\n" +
"Rule ComparePseudoRule2:  if rdf(v5, rdf:type, CF6) and rdf(v5, mdl:sfc, 0.35) then rdf(v5, mdl:thrust, v4).\n" +
" (synthetic://test/Resource3.dialog line : 5 column : 1)";
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
			val expected = "INFO:Rule ComparePseudoRule0:  if rdf(v1, rdf:type, RamJet) and rdf(v1, mdl:sfc, 0.35) then rdf(v1, mdl:thrust, v0).
Rule ComparePseudoRule1:  if rdf(v3, rdf:type, F100) and rdf(v3, mdl:sfc, 0.35) then rdf(v3, mdl:thrust, v2).
Rule ComparePseudoRule2:  if rdf(v5, rdf:type, CF6) and rdf(v5, mdl:sfc, 0.35) then rdf(v5, mdl:thrust, v4).
Rule ComparePseudoRule3:  if rdf(v7, rdf:type, RamJet) and rdf(v7, mdl:sfc, 0.45) then rdf(v7, mdl:thrust, v6).
Rule ComparePseudoRule4:  if rdf(v9, rdf:type, F100) and rdf(v9, mdl:sfc, 0.45) then rdf(v9, mdl:thrust, v8).
Rule ComparePseudoRule5:  if rdf(v11, rdf:type, CF6) and rdf(v11, mdl:sfc, 0.45) then rdf(v11, mdl:thrust, v10).
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
			val expected = "INFO:Rule ComparePseudoRule0:  if rdf(v2, rdf:type, mdl:CF6) and rdf(v2, mdl:sfc, 0.35) then rdf(v2, mdl:thrust, v1).
Rule ComparePseudoRule1:  if rdf(v4, rdf:type, mdl:CF6) and rdf(v4, mdl:sfc, 0.45) then rdf(v4, mdl:thrust, v3).
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

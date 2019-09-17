package com.ge.research.sadl.darpa.aske.tests

import com.ge.research.sadl.reasoner.ConfigurationManager
import org.eclipse.xtext.diagnostics.Severity
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertEquals

class DialogTest extends AbstractDialogTest {

	@Test
	def void dummy_test() {
		'''
			uri "http://sadl.org/Model.sadl" alias mdl.
			
			Mass is a type of UnittedQuantity, 
				described by force with values of type Force,
				described by speed with values of type Speed,
				described by acceleration with values of type Acceleration.
			
			Force is a type of UnittedQuantity.
			Speed is a type of UnittedQuantity.
			Acceleration is a type of UnittedQuantity.
			
			External secondLaw(double m, double acc) returns double: "http://sadl.org/secondlaw".
		'''.sadl

		'''
			uri "http://sadl.org/SaveTarget.sadl" alias svtgt.
		'''.sadl

		'''
			uri "http://sadl.org/CodeExtractionModel.sadl" alias cem.
			 
			// This is the code extraction meta-model
			CodeElement is a class described by beginsAt with a single value of type int,
				described by endsAt with a single value of type int.
			
			CodeBlock is a type of CodeElement,
				described by serialization with a single value of type string,
				described by comment with values of type Comment,
				described by containedIn with values of type CodeBlock.
			
			{Class, Method, ConditionalBlock, LoopBlock} are types of CodeBlock.
			
			cmArguments describes Method with a single value of type CodeVariable List.
			cmReturnTypes describes Method with a single value of type string List.
			cmSemanticReturnTypes describes Method with a single value of type string List.
			doesComputation describes Method with a single value of type boolean.
			calls describes Method with values of type MethodCall.
			ExternalMethod is a type of Method.
			
			// The reference to a CodeVariable can be its definition (Defined),
			//	an assignment or reassignment (Reassigned), or just a reference
			//	in the right-hand side of an assignment or a conditional (Used)
			Usage is a class, must be one of {Defined, Used, Reassigned}.
			
			Reference  is a type of CodeElement
				described by firstRef with a single value of type boolean
				described by codeBlock with a single value of type CodeBlock
				described by usage with values of type Usage
					described by input (note "CodeVariable is an input to codeBlock CodeBlock")
						with a single value of type boolean
					described by output (note "CodeVariable is an output of codeBlock CodeBlock")
						with a single value of type boolean
					described by isImplicit (note "the input or output of this reference is implicit (inferred), not explicit")
						with a single value of type boolean
					described by setterArgument (note "is this variable input to a setter?") with a single value of type boolean
					described by comment with values of type Comment.
				
			MethodCall is a type of CodeElement
				described by codeBlock with a single value of type CodeBlock
				described by inputMapping with values of type InputMapping,
				described by returnedMapping with values of type OutputMapping.
			MethodCallMapping is a class,
				described by callingVariable with a single value of type CodeVariable,
				described by calledVariable with a single value of type CodeVariable.
			{InputMapping, OutputMapping} are types of MethodCallMapping.
			
			Comment (note "CodeBlock and Reference can have a Comment") is a type of CodeElement
			 	described by commentContent with a single value of type string.	
			
			// what about Constant also? Note something maybe an input and then gets reassigned
			// Constant could be defined in terms of being set by equations that only involve Constants
			// Constants could also relate variables used in different equations as being same
			CodeVariable  is a type of CodeElement, 
				described by varName with a single value of type string,
				described by varType with a single value of type string,
				described by semanticVarType with a single value of type string,
				described by quantityKind (note "this should be qudt:QuantityKind") with a single value of type ScientificConcept,
				described by reference with values of type Reference.
			
			{ClassField, MethodArgument, MethodVariable} are types of CodeVariable.
			
			//External findFirstLocation (CodeVariable cv) returns int: "http://ToBeImplemented".
			
			Rule Transitive
			if inst is a cls and
			   cls is a type of CodeVariable
			then inst is a CodeVariable. 
			
			Rule SetNotFirstRef1
			if c is a CodeVariable and
			   ref is reference of c and
			   oneOf(usage of ref, Used, Reassigned) and
			   ref2 is reference of c and
			   ref != ref2 and
			   cb is codeBlock of ref and
			   cb2 is codeBlock of ref2 and
			   cb = cb2 and
			   l1 is beginsAt of ref and
			   l2 is beginsAt of ref2 and
			   l2 > l1   // so ref2 is at an earlier location that ref
			then firstRef of ref2 is false.
			
			// first reference is of type "Used" or all earlier refs are of type "Used"
			// this does not cover when no ref2 with l2 < l1 exists
			Rule SetAsInput1
			if c is CodeVariable and
			   ref is reference of c and
			   input of ref is not known and
			   usage of ref is Used and
			   ref2 is reference of c and
			   ref != ref2 and
			   cb is codeBlock of ref and
			   cb2 is codeBlock of ref2 and
			   cb = cb2 and   
			   l1 is beginsAt of ref and
			   l2 is beginsAt of ref2 and
			   l2 < l1 and  // so ref2 is at an earlier location that ref
			   noValue(ref2, usage, Reassigned) // no earlier reassignment of c exists
			then input of ref is true and isImplicit of ref is true. 
			
			// if there is no l2 as specified in the previous rules, then the following covers that case
			// do I need to consider codeBlock?????
			Rule SetAsInput2
			if c is a CodeVariable and
			   ref is reference of c and
			   input of ref is not known and
			   usage of ref is Used and 
			   noValue(ref, firstRef)
			then input of ref is true and isImplicit of ref is true.
			
			// "it is an output if it is computed and is argument to a setter"
			// or I could try to use the notion of a constant
			Rule SetAsOutput
			if c is a CodeVariable and
			   setterArgument of c is true and
			   ref is a reference of c and
			   output of ref is not known and
			   usage of ref is Defined //check this?
			then
				output of ref is true and isImplicit of ref is true.
		'''.sadl

		'''
			uri "http://darpa/aske/ge/ta1/testdlg" alias tdlg.
			
			import "http://sadl.org/Model.sadl".
			
			target model "http://sadl.org/SaveTarget.sadl" alias tgt.
			
			MyHulk is a Mass with ^value 280, with unit "lbs".
			
			Ask unit of MyHulk?
			
			What is acceleration?
			
«««			What is the force of some Mass when the acceleration of the Mass is 25?
			
			What type of values can acceleration of Mass have?
			
			How many values of acceleration of type Acceleration can a Mass have?
			
			Save secondLaw to sos.
			
			evaluate secondLaw(10, 25).
			
			yes.
			
			My name is Andy.
			
			External Mach.CAL_GAM(double T, double G, double Q) returns double: "http://com.ge.research.sadl.darpa.aske.answer/Mach_java#Mach.CAL_GAM".
			
			Equation plusOne(int i) returns int: i+1.
			
			Evaluate plusOne(10).
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
			val errors = issues.filter[severity === Severity.ERROR]
			assertEquals(1, errors.size)
			assertEquals("Model with alias 'sos' not found in target models.", errors.head.message)
		]
	}

	@Test
	def void saveThroughEval_01_test() {
		'''
			uri "http://sadl.org/Model.sadl" alias mdl.
			
			Mass is a type of UnittedQuantity, 
				described by force with values of type Force,
				described by speed with values of type Speed,
				described by acceleration with values of type Acceleration.
			
			Force is a type of UnittedQuantity.
			Speed is a type of UnittedQuantity.
			Acceleration is a type of UnittedQuantity.
		'''.sadl

		'''
			uri "http://sadl.org/SaveTarget.sadl" alias svtgt.
		'''.sadl

		'''
			uri "http://sadl.org/CodeExtractionModel.sadl" alias cem.
			 
			// This is the code extraction meta-model
			CodeElement is a class described by beginsAt with a single value of type int,
				described by endsAt with a single value of type int.
			
			CodeBlock is a type of CodeElement,
				described by serialization with a single value of type string,
				described by comment with values of type Comment,
				described by containedIn with values of type CodeBlock.
			
			{Class, Method, ConditionalBlock, LoopBlock} are types of CodeBlock.
			
			cmArguments describes Method with a single value of type CodeVariable List.
			cmReturnTypes describes Method with a single value of type string List.
			cmSemanticReturnTypes describes Method with a single value of type string List.
			doesComputation describes Method with a single value of type boolean.
			calls describes Method with values of type MethodCall.
			ExternalMethod is a type of Method.
			
			// The reference to a CodeVariable can be its definition (Defined),
			//	an assignment or reassignment (Reassigned), or just a reference
			//	in the right-hand side of an assignment or a conditional (Used)
			Usage is a class, must be one of {Defined, Used, Reassigned}.
			
			Reference  is a type of CodeElement
				described by firstRef with a single value of type boolean
				described by codeBlock with a single value of type CodeBlock
				described by usage with values of type Usage
					described by input (note "CodeVariable is an input to codeBlock CodeBlock")
						with a single value of type boolean
					described by output (note "CodeVariable is an output of codeBlock CodeBlock")
						with a single value of type boolean
					described by isImplicit (note "the input or output of this reference is implicit (inferred), not explicit")
						with a single value of type boolean
					described by setterArgument (note "is this variable input to a setter?") with a single value of type boolean
					described by comment with values of type Comment.
				
			MethodCall is a type of CodeElement
				described by codeBlock with a single value of type CodeBlock
				described by inputMapping with values of type InputMapping,
				described by returnedMapping with values of type OutputMapping.
			MethodCallMapping is a class,
				described by callingVariable with a single value of type CodeVariable,
				described by calledVariable with a single value of type CodeVariable.
			{InputMapping, OutputMapping} are types of MethodCallMapping.
			
			Comment (note "CodeBlock and Reference can have a Comment") is a type of CodeElement
			 	described by commentContent with a single value of type string.	
			
			// what about Constant also? Note something maybe an input and then gets reassigned
			// Constant could be defined in terms of being set by equations that only involve Constants
			// Constants could also relate variables used in different equations as being same
			CodeVariable  is a type of CodeElement, 
				described by varName with a single value of type string,
				described by varType with a single value of type string,
				described by semanticVarType with a single value of type string,
				described by quantityKind (note "this should be qudt:QuantityKind") with a single value of type ScientificConcept,
				described by reference with values of type Reference.
			
			{ClassField, MethodArgument, MethodVariable} are types of CodeVariable.
			
			//External findFirstLocation (CodeVariable cv) returns int: "http://ToBeImplemented".
			
			Rule Transitive
			if inst is a cls and
			   cls is a type of CodeVariable
			then inst is a CodeVariable. 
			
			Rule SetNotFirstRef1
			if c is a CodeVariable and
			   ref is reference of c and
			   oneOf(usage of ref, Used, Reassigned) and
			   ref2 is reference of c and
			   ref != ref2 and
			   cb is codeBlock of ref and
			   cb2 is codeBlock of ref2 and
			   cb = cb2 and
			   l1 is beginsAt of ref and
			   l2 is beginsAt of ref2 and
			   l2 > l1   // so ref2 is at an earlier location that ref
			then firstRef of ref2 is false.
			
			// first reference is of type "Used" or all earlier refs are of type "Used"
			// this does not cover when no ref2 with l2 < l1 exists
			Rule SetAsInput1
			if c is CodeVariable and
			   ref is reference of c and
			   input of ref is not known and
			   usage of ref is Used and
			   ref2 is reference of c and
			   ref != ref2 and
			   cb is codeBlock of ref and
			   cb2 is codeBlock of ref2 and
			   cb = cb2 and   
			   l1 is beginsAt of ref and
			   l2 is beginsAt of ref2 and
			   l2 < l1 and  // so ref2 is at an earlier location that ref
			   noValue(ref2, usage, Reassigned) // no earlier reassignment of c exists
			then input of ref is true and isImplicit of ref is true. 
			
			// if there is no l2 as specified in the previous rules, then the following covers that case
			// do I need to consider codeBlock?????
			Rule SetAsInput2
			if c is a CodeVariable and
			   ref is reference of c and
			   input of ref is not known and
			   usage of ref is Used and 
			   noValue(ref, firstRef)
			then input of ref is true and isImplicit of ref is true.
			
			// "it is an output if it is computed and is argument to a setter"
			// or I could try to use the notion of a constant
			Rule SetAsOutput
			if c is a CodeVariable and
			   setterArgument of c is true and
			   ref is a reference of c and
			   output of ref is not known and
			   usage of ref is Defined //check this?
			then
				output of ref is true and isImplicit of ref is true.
		'''.sadl

		'''
			uri "http://darpa/aske/ge/ta1/testdlg" alias tdlg.
			
			import "http://sadl.org/Model.sadl".
			
			target model "http://sadl.org/SaveTarget.sadl" alias tgt.
			
			CM: External Mach.CAL_SOS(double T, double G, double R, double Q) returns double: "http://com.ge.research.sadl.darpa.aske.answer/Mach_java#Mach.CAL_SOS".
			
			CM: Mach.CAL_SOS has expression (a Script with language Java, with script 
			"public double CAL_SOS(double T, double G, double R, double Q) {
			    double WOW = 1 + (G - 1) / (1 + (G - 1) * Math.pow((Q / T), 2) * Math.exp(Q / T) / Math.pow((Math.exp(Q / T) - 1), 2));
			    return (Math.sqrt(32.174 * T * R * WOW));
			}"
			), has expression (a Script with language Python, with script 
			"#!/usr/bin/env python
			\"\"\" generated source for module inputfile \"\"\"
			class Mach(object):
			    \"\"\" generated source for class Mach \"\"\"
			    def CAL_SOS(self, T, G, R, Q):
			        \"\"\" generated source for method CAL_SOS \"\"\"
			        WOW = 1 + (G - 1) / (1 + (G - 1) * Math.pow((Q / T), 2) * Math.exp(Q / T) / Math.pow((Math.exp(Q / T) - 1), 2))
			        return (Math.sqrt(32.174 * T * R * WOW))
			
			").
			save Mach.CAL_SOS to tgt.
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
			val errors = issues.filter[severity === Severity.ERROR]
			assertEquals(0, errors.size)
		]
	}
//	@Ignore
	@Test
	def void saveThroughEval_02_test() {
		'''
			uri "http://sadl.org/Model.sadl" alias mdl.
			
			Mass is a type of UnittedQuantity, 
				described by force with values of type Force,
				described by speed with values of type Speed,
				described by acceleration with values of type Acceleration.
			
			Force is a type of UnittedQuantity.
			Speed is a type of UnittedQuantity.
			Acceleration is a type of UnittedQuantity.
			
		'''.sadl

		'''
			uri "http://sadl.org/SaveTarget.sadl" alias svtgt.
		'''.sadl

		'''
			uri "http://sadl.org/CodeExtractionModel.sadl" alias cem.
			 
			// This is the code extraction meta-model
			CodeElement is a class described by beginsAt with a single value of type int,
				described by endsAt with a single value of type int.
			
			CodeBlock is a type of CodeElement,
				described by serialization with a single value of type string,
				described by comment with values of type Comment,
				described by containedIn with values of type CodeBlock.
			
			{Class, Method, ConditionalBlock, LoopBlock} are types of CodeBlock.
			
			cmArguments describes Method with a single value of type CodeVariable List.
			cmReturnTypes describes Method with a single value of type string List.
			cmSemanticReturnTypes describes Method with a single value of type string List.
			doesComputation describes Method with a single value of type boolean.
			calls describes Method with values of type MethodCall.
			ExternalMethod is a type of Method.
			
			// The reference to a CodeVariable can be its definition (Defined),
			//	an assignment or reassignment (Reassigned), or just a reference
			//	in the right-hand side of an assignment or a conditional (Used)
			Usage is a class, must be one of {Defined, Used, Reassigned}.
			
			Reference  is a type of CodeElement
				described by firstRef with a single value of type boolean
				described by codeBlock with a single value of type CodeBlock
				described by usage with values of type Usage
					described by input (note "CodeVariable is an input to codeBlock CodeBlock")
						with a single value of type boolean
					described by output (note "CodeVariable is an output of codeBlock CodeBlock")
						with a single value of type boolean
					described by isImplicit (note "the input or output of this reference is implicit (inferred), not explicit")
						with a single value of type boolean
					described by setterArgument (note "is this variable input to a setter?") with a single value of type boolean
					described by comment with values of type Comment.
				
			MethodCall is a type of CodeElement
				described by codeBlock with a single value of type CodeBlock
				described by inputMapping with values of type InputMapping,
				described by returnedMapping with values of type OutputMapping.
			MethodCallMapping is a class,
				described by callingVariable with a single value of type CodeVariable,
				described by calledVariable with a single value of type CodeVariable.
			{InputMapping, OutputMapping} are types of MethodCallMapping.
			
			Comment (note "CodeBlock and Reference can have a Comment") is a type of CodeElement
			 	described by commentContent with a single value of type string.	
			
			// what about Constant also? Note something maybe an input and then gets reassigned
			// Constant could be defined in terms of being set by equations that only involve Constants
			// Constants could also relate variables used in different equations as being same
			CodeVariable  is a type of CodeElement, 
				described by varName with a single value of type string,
				described by varType with a single value of type string,
				described by semanticVarType with a single value of type string,
				described by quantityKind (note "this should be qudt:QuantityKind") with a single value of type ScientificConcept,
				described by reference with values of type Reference.
			
			{ClassField, MethodArgument, MethodVariable} are types of CodeVariable.
			
			//External findFirstLocation (CodeVariable cv) returns int: "http://ToBeImplemented".
			
			Rule Transitive
			if inst is a cls and
			   cls is a type of CodeVariable
			then inst is a CodeVariable. 
			
			Rule SetNotFirstRef1
			if c is a CodeVariable and
			   ref is reference of c and
			   oneOf(usage of ref, Used, Reassigned) and
			   ref2 is reference of c and
			   ref != ref2 and
			   cb is codeBlock of ref and
			   cb2 is codeBlock of ref2 and
			   cb = cb2 and
			   l1 is beginsAt of ref and
			   l2 is beginsAt of ref2 and
			   l2 > l1   // so ref2 is at an earlier location that ref
			then firstRef of ref2 is false.
			
			// first reference is of type "Used" or all earlier refs are of type "Used"
			// this does not cover when no ref2 with l2 < l1 exists
			Rule SetAsInput1
			if c is CodeVariable and
			   ref is reference of c and
			   input of ref is not known and
			   usage of ref is Used and
			   ref2 is reference of c and
			   ref != ref2 and
			   cb is codeBlock of ref and
			   cb2 is codeBlock of ref2 and
			   cb = cb2 and   
			   l1 is beginsAt of ref and
			   l2 is beginsAt of ref2 and
			   l2 < l1 and  // so ref2 is at an earlier location that ref
			   noValue(ref2, usage, Reassigned) // no earlier reassignment of c exists
			then input of ref is true and isImplicit of ref is true. 
			
			// if there is no l2 as specified in the previous rules, then the following covers that case
			// do I need to consider codeBlock?????
			Rule SetAsInput2
			if c is a CodeVariable and
			   ref is reference of c and
			   input of ref is not known and
			   usage of ref is Used and 
			   noValue(ref, firstRef)
			then input of ref is true and isImplicit of ref is true.
			
			// "it is an output if it is computed and is argument to a setter"
			// or I could try to use the notion of a constant
			Rule SetAsOutput
			if c is a CodeVariable and
			   setterArgument of c is true and
			   ref is a reference of c and
			   output of ref is not known and
			   usage of ref is Defined //check this?
			then
				output of ref is true and isImplicit of ref is true.
		'''.sadl

		'''
			uri "http://darpa/aske/ge/ta1/testdlg" alias tdlg.
			
			import "http://sadl.org/Model.sadl".
			
			target model "http://sadl.org/SaveTarget.sadl" alias tgt.
						
			CM: External secondLaw(double m, double acc) returns double: "http://sadl.org/secondlaw".
			CM: secondLaw has expression (a Script with language Python, with script 
			"#!/usr/bin/env python
			\"\"\" generated source for module inputfile \"\"\"
			class SL(object):
			    \"\"\" generated source for class Mach \"\"\"
			    def secondLaw(self, m, acc):
			        \"\"\" generated source for method secondLaw \"\"\"
			        return (m * acc)
			
			").
			Save secondLaw to tgt.
			
			evaluate secondLaw(10, 25).
			
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
			assertTrue(issues.filter[severity === Severity.ERROR].empty)
		]
	}

	@Test
	def void testGetTranslatorInstance() {
		val cm = new ConfigurationManager
		val tr = cm.translator
		assertNotNull(tr)
	}
	
}

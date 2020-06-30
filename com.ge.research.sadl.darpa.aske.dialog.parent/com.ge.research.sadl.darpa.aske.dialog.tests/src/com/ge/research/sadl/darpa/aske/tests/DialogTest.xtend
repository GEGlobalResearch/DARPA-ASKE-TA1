package com.ge.research.sadl.darpa.aske.tests

import com.ge.research.sadl.reasoner.ConfigurationManager
import org.eclipse.xtext.diagnostics.Severity
import org.junit.Ignore
import org.junit.Test

import static org.junit.Assert.assertNotNull
import static org.junit.Assert.assertTrue
import static org.junit.Assert.assertEquals
import com.ge.research.sadl.darpa.aske.processing.JenaBasedDialogModelProcessor
import com.ge.research.sadl.darpa.aske.processing.CompareContent
import com.ge.research.sadl.darpa.aske.processing.WhatIsContent
import java.io.File
import com.ge.research.sadl.builder.ConfigurationManagerForIDE
import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory
import org.junit.Before
import java.io.IOException
import com.ge.research.sadl.reasoner.utils.SadlUtils
import com.ge.research.sadl.darpa.aske.processing.ModifiedAskContent
import com.ge.research.sadl.darpa.aske.processing.WhatValuesContent

class DialogTest extends AbstractDialogTest {

	private String codeExtractionProjectModelFolder;
	private String domainProjectModelFolder;
	private String codeExtractionKbRoot;

	private String speedOfSoundPath;
	private String scientificConcepts2Path;

	@Before
	def void setUp() throws Exception {
		val projectRoot = new File("resources/M5Snapshot");
		setCodeExtractionKbRoot(projectRoot.getCanonicalPath());
		val codeExtractionPrjFolder = new File(getCodeExtractionKbRoot());
		assertTrue(codeExtractionPrjFolder.exists());
		setExtractionProjectModelFolder(getCodeExtractionKbRoot() + "/OwlModels");
		setScientificConcepts2(getCodeExtractionKbRoot() + "/ScientificConcepts2.sadl");
		setSpeedOfSoundPath(getCodeExtractionKbRoot() + "/SpeedOfSound.sadl");
		setDomainProjectModelFolder(getExtractionProjectModelFolder());
	}

	def String getExtractionProjectModelFolder() {
		return codeExtractionProjectModelFolder;
	}

	def void setExtractionProjectModelFolder(String extractionProjectModelFolder) {
		this.codeExtractionProjectModelFolder = extractionProjectModelFolder;
	}

	def String getDomainProjectModelFolder() {
		return domainProjectModelFolder;
	}

	def void setDomainProjectModelFolder(String outputProjectModelFolder) {
		this.domainProjectModelFolder = outputProjectModelFolder;
	}

	def void setSpeedOfSoundPath(String path) {
		this.speedOfSoundPath = path;	
	}

	def String getSpeedOfSoundPath() {
		return speedOfSoundPath;
	}

	def void setScientificConcepts2(String path) {
		this.setScientificConcepts2Path(path);
	}

	def String getScientificConcepts2Path() {
		return scientificConcepts2Path;
	}

	def void setScientificConcepts2Path(String scientificConcepts2Path) {
		this.scientificConcepts2Path = scientificConcepts2Path;
	}
	
	def String getCodeExtractionKbRoot() {
		return codeExtractionKbRoot;
	}

	def void setCodeExtractionKbRoot(String codeExtractionKbRoot) {
		this.codeExtractionKbRoot = codeExtractionKbRoot;
	}

	def CharSequence getContent(String path) throws IOException {
		val f = new File(path);
		assertTrue(f.exists());
		val su = new SadlUtils();
		return su.fileToString(f);
	}

	@Ignore
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

	@Ignore
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

	@Ignore
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
			val jbdp = (processor as JenaBasedDialogModelProcessor)
			val resource = jbdp.currentResource
			val secondLaw250 = jbdp.getAnswerCurationManager(resource).getAnswerToQuestion("evaluate secondLaw(10, 25).")
			assertTrue(secondLaw250 !== null && secondLaw250.equals("[250.]"))
		]
	}

	@Test
	def void testWhatIsStatement_01() {
			val grd = newArrayList(
"Rule ComparePseudoRule0:  if rdf(v1, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v1, http://sadl.org/sadlimplicitmodel#value, 25000) and rdf(v1, http://sadl.org/sadlimplicitmodel#unit, \"ft\") and rdf(http://sadl.org/Suitability.dialog#v4, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/Suitability.sadl#CF6) and rdf(http://sadl.org/Suitability.dialog#v4, http://sadl.org/Suitability.sadl#altitude, v1) and rdf(v2, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v2, http://sadl.org/sadlimplicitmodel#value, 600) and rdf(v2, http://sadl.org/sadlimplicitmodel#unit, \"mph\") and rdf(http://sadl.org/Suitability.dialog#v4, http://sadl.org/Suitability.sadl#speed, v2) then rdf(http://sadl.org/Suitability.dialog#v4, http://sadl.org/Suitability.sadl#thrust, v3).")
		'''
			 uri "http://sadl.org/Suitability.sadl" alias stblt.
			 
			 AircraftEngine is a class.
			 altitude describes AircraftEngine with values of type UnittedQuantity.
			 thrust describes AircraftEngine with values of type UnittedQuantity.
			 weight describes AircraftEngine with values of type UnittedQuantity.
			 speed describes AircraftEngine with values of type UnittedQuantity.
			 sfc describes AircraftEngine with values of type float.
			  
			 F100 is a type of AircraftEngine.
			 CF6 is a type of AircraftEngine.
		'''.sadl

		'''
			 uri "http://sadl.org/Suitability.dialog" alias stbltdlg.
			 
			 import "http://sadl.org/Suitability.sadl".
			 
			 What is the thrust of a CF6 when altitude is 25000 ft and speed is 600 mph?
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
//			val stmtitr = ontModel.listStatements()
//			while (stmtitr.hasNext) {
//				println(stmtitr.nextStatement)
//			}
			val errors = issues.filter[severity === Severity.ERROR]
			assertEquals(0, errors.size)
			if (processor instanceof JenaBasedDialogModelProcessor) {
				val jbdp = (processor as JenaBasedDialogModelProcessor)
				val resource = jbdp.currentResource
				val conversation = jbdp.getAnswerCurationManager(resource).conversation
				assertNotNull(conversation)
				assertNotNull(conversation.statements);
				assertEquals(1, conversation.statements.size)
				assertTrue(conversation.statements.get(0).statement instanceof WhatIsContent)
				val cc = conversation.statements.get(0).statement as WhatIsContent
				val rules = cc.getComputationalGraphRules
				assertEquals(1, rules.size)
				assertEquals(grd.get(0), rules.get(0).toFullyQualifiedString)
			}
		]
	}
	
	@Test
	def void testWhatIsStatement_02() {
			val grd = newArrayList(
"Rule ComparePseudoRule0:  if rdf(v1, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://aske.ge.com/hypersonicsV2#Speed) and rdf(v1, http://sadl.org/sadlimplicitmodel#value, 300) and rdf(v1, http://sadl.org/sadlimplicitmodel#unit, \"mph\") and rdf(http://sadl.org/test4.dialog#v3, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://aske.ge.com/hypersonicsV2#CF6) and rdf(http://sadl.org/test4.dialog#v3, http://aske.ge.com/hypersonicsV2#speed, v1) then rdf(http://sadl.org/test4.dialog#v3, http://aske.ge.com/hypersonicsV2#machSpeed, v2)."
)
		'''
			 uri "http://aske.ge.com/hypersonicsV2" alias hv2.
			 
			 PhysicalThing is a class,
			 	described by mass with values of type Mass
			 	described by volume with values of type Volume
			 	described by density with values of type Density
			 	described by temperature with values of type Temperature
			 	described by altitude with values of type Altitude
			 	described by speed with values of type Speed.
			 	
			 Speed is a type of UnittedQuantity.
			 Mass is a type of UnittedQuantity.
			 Volume is a type of UnittedQuantity.
			 Density is a type of UnittedQuantity.
			 Length is a type of UnittedQuantity.
			 Time is a type of UnittedQuantity.
			 Temperature is a type of UnittedQuantity.
			 Altitude is a type of UnittedQuantity.
			 MachSpeed is a type of Speed.
			 
			 Gas is a type of PhysicalThing
			 	described by pressure with values of type Pressure.
			 Air is a type of Gas.
			 machSpeed describes Air with values of type MachSpeed.
			 
			 SpecificNetThrust is a type of UnittedQuantity.
			 SpecificFuelConsumption is a type of UnittedQuantity.
			 
			 AircraftEngine is a type of PhysicalThing
			 	described by mach with values of type MachSpeed
			 	described by sfc (alias "specific fuel consumption") with values of type SpecificFuelConsumption
			 	described by thrust with values of type SpecificNetThrust.
			                     
			 {CF6, F100, J85, RamJet} are types of AircraftEngine.
		'''.sadl

		'''
			 uri "http://sadl.org/test4.dialog" alias test4dialog.
			 
			 import "http://aske.ge.com/hypersonicsV2".
			 
			 what is the machSpeed of a CF6 when the speed is 300 mph?
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
//			val stmtitr = ontModel.listStatements()
//			while (stmtitr.hasNext) {
//				println(stmtitr.nextStatement)
//			}
			val errors = issues.filter[severity === Severity.ERROR]
			for (err : errors) {
				println(err)
			}
			assertEquals(0, errors.size)
			if (processor instanceof JenaBasedDialogModelProcessor) {
				val jbdp = (processor as JenaBasedDialogModelProcessor)
				val resource = jbdp.currentResource
				val conversation = jbdp.getAnswerCurationManager(resource).conversation
				assertNotNull(conversation)
				assertNotNull(conversation.statements);
				assertEquals(1, conversation.statements.size)
				assertTrue(conversation.statements.get(0).statement instanceof WhatIsContent)
				val cc = conversation.statements.get(0).statement as WhatIsContent
				val rules = cc.getComputationalGraphRules
				assertEquals(1, rules.size)
				assertEquals(grd.get(0), rules.get(0).toFullyQualifiedString)
			}
		]
	}
	
	@Test
	def void testWhatIsStatement_03() {
		val actualAnswer = "AircraftEngine is a class,
    described by altitude with values of type UnittedQuantity,
    described by sfc with values of type float,
    described by speed with values of type UnittedQuantity,
    described by thrust with values of type UnittedQuantity,
    described by weight with values of type UnittedQuantity."
		'''
			 uri "http://sadl.org/Suitability.sadl" alias stblt.
			 
			 AircraftEngine is a class.
			 altitude describes AircraftEngine with values of type UnittedQuantity.
			 thrust describes AircraftEngine with values of type UnittedQuantity.
			 weight describes AircraftEngine with values of type UnittedQuantity.
			 speed describes AircraftEngine with values of type UnittedQuantity.
			 sfc describes AircraftEngine with values of type float.
			  
			 F100 is a type of AircraftEngine.
			 CF6 is a type of AircraftEngine.
		'''.sadl

		'''
			 uri "http://sadl.org/Suitability.dialog" alias stbltdlg.
			 
			 import "http://sadl.org/Suitability.sadl".
			 
			 What is an AircraftEngine?
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
//			val stmtitr = ontModel.listStatements()
//			while (stmtitr.hasNext) {
//				println(stmtitr.nextStatement)
//			}
			val errors = issues.filter[severity === Severity.ERROR]
			assertEquals(0, errors.size)
			if (processor instanceof JenaBasedDialogModelProcessor) {
				val jbdp = (processor as JenaBasedDialogModelProcessor)
				val resource = jbdp.currentResource
				val acm = jbdp.getAnswerCurationManager(resource)
				val conversation = acm.conversation
				assertNotNull(conversation)
				assertNotNull(conversation.statements);
				assertEquals(1, conversation.statements.size)
				assertTrue(conversation.statements.get(0).statement instanceof WhatIsContent)
				val cc = conversation.statements.get(0).statement as WhatIsContent
				val rules = cc.getComputationalGraphRules
				assertTrue(rules === null)	
				val question = cc.getText().trim();
				val answer = acm.getAnswerToQuestion(question)
				assertTrue(processor.compareTranslations(actualAnswer, answer))	
			}
		]
	}
	
	@Test
	def void testWhatIsStatement_04() {
		val actualAnswer = "Failed to evaluate answer"
	    this.sadl(getContent(getScientificConcepts2Path())); 
	    this.sadl(getContent(getSpeedOfSoundPath()));

		'''
			 uri "http://sadl.org/Suitability.dialog" alias stbltdlg.
			 
			 import "http://sadl.org/SpeedOfSound.sadl".
			 
			 What is an AircraftEngine?
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
//			val stmtitr = ontModel.listStatements()
//			while (stmtitr.hasNext) {
//				println(stmtitr.nextStatement)
//			}
			val errors = issues.filter[severity === Severity.ERROR]
			if (errors.size > 0) {
				for (err : errors) {
					println(err.toString)
				}
			}
			assertEquals(2, errors.size)
			if (processor instanceof JenaBasedDialogModelProcessor) {
				val jbdp = (processor as JenaBasedDialogModelProcessor)
				val resource = jbdp.currentResource
				val acm = jbdp.getAnswerCurationManager(resource)
				val conversation = acm.conversation
				assertNotNull(conversation)
				assertNotNull(conversation.statements);
				assertEquals(1, conversation.statements.size)
				assertTrue(conversation.statements.get(0).statement instanceof WhatIsContent)
				val cc = conversation.statements.get(0).statement as WhatIsContent
				val rules = cc.getComputationalGraphRules
				assertTrue(rules === null)	
				val question = cc.getText().trim();
				val answer = acm.getAnswerToQuestion(question)
				println(answer)
				assertTrue(processor.compareTranslations(actualAnswer, answer))	
			}
		]
	}
	
	@Test
	def void testWhatIsStatement_05() {
		val actualAnswer = "CAL_SOS has expression (a Script with language Text, with script \"xsd:double CAL_SOS(double T,double G,double R,double Q,string us): uri(\\\"http://com.ge.research.darpa.aske.kchain.CAL_SOS\\\")\").
External CAL_SOS(double T, double G, double R, double Q, string us) returns double: \"http://com.ge.research.darpa.aske.kchain.CAL_SOS\"."
	    this.sadl(getContent(getScientificConcepts2Path())); 
	    this.sadl(getContent(getSpeedOfSoundPath()));
		'''
			 uri "http://sadl.org/Suitability.sadl" alias stblt.
			 
			 AircraftEngine is a class.
			 altitude describes AircraftEngine with values of type UnittedQuantity.
			 thrust describes AircraftEngine with values of type UnittedQuantity.
			 weight describes AircraftEngine with values of type UnittedQuantity.
			 speed describes AircraftEngine with values of type UnittedQuantity.
			 sfc describes AircraftEngine with values of type float.
			  
			 F100 is a type of AircraftEngine.
			 CF6 is a type of AircraftEngine.
		'''.sadl

		'''
			 uri "http://sadl.org/Suitability.dialog" alias stbltdlg.
			 import "http://sadl.org/SpeedOfSound.sadl".
			 import "http://sadl.org/Suitability.sadl".
			 
			 What is CAL_SOS?
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
//			val stmtitr = ontModel.listStatements()
//			while (stmtitr.hasNext) {
//				println(stmtitr.nextStatement)
//			}
			val errors = issues.filter[severity === Severity.ERROR]
			assertEquals(0, errors.size)
			if (processor instanceof JenaBasedDialogModelProcessor) {
				val jbdp = (processor as JenaBasedDialogModelProcessor)
				val resource = jbdp.currentResource
				val acm = jbdp.getAnswerCurationManager(resource)
				val conversation = acm.conversation
				assertNotNull(conversation)
				assertNotNull(conversation.statements);
				assertEquals(1, conversation.statements.size)
				assertTrue(conversation.statements.get(0).statement instanceof WhatIsContent)
				val cc = conversation.statements.get(0).statement as WhatIsContent
				val rules = cc.getComputationalGraphRules
				assertTrue(rules === null)	
				val question = cc.getText().trim();
				val answer = acm.getAnswerToQuestion(question)
				println(answer)
				assertTrue(processor.compareTranslations(actualAnswer, answer))	
			}
		]
	}
	
	@Test
	def void testWhatIsStatement_06() {
		val actualAnswer = "lbs"
	    this.sadl(getContent(getScientificConcepts2Path())); 
	    this.sadl(getContent(getSpeedOfSoundPath()));

		'''
			 uri "http://sadl.org/Suitability.dialog" alias stbltdlg.
			 
			 import "http://sadl.org/SpeedOfSound.sadl".
			 
			 MyHulk is a Mass with ^value 280, with unit "lbs".
			 
			 Ask unit of MyHulk?
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
//			val stmtitr = ontModel.listStatements()
//			while (stmtitr.hasNext) {
//				println(stmtitr.nextStatement)
//			}
			val errors = issues.filter[severity === Severity.ERROR]
			assertEquals(0, errors.size)
			if (processor instanceof JenaBasedDialogModelProcessor) {
				val jbdp = (processor as JenaBasedDialogModelProcessor)
				val resource = jbdp.currentResource
				val acm = jbdp.getAnswerCurationManager(resource)
				val conversation = acm.conversation
				assertNotNull(conversation)
				assertNotNull(conversation.statements);
				assertEquals(2, conversation.statements.size)
				assertTrue(conversation.statements.get(1).statement instanceof ModifiedAskContent)
				val cc = conversation.statements.get(1).statement as ModifiedAskContent
				val question = cc.getText().trim();
				val answer = acm.getAnswerToQuestion(question)
				assertTrue(processor.compareTranslations(actualAnswer, answer))	
			}
		]
	}
	
	@Test
	def void testWhatIsStatement_07() {
		val actualAnswer = "Acceleration"
	    this.sadl(getContent(getScientificConcepts2Path())); 
	    this.sadl(getContent(getSpeedOfSoundPath()));

		'''
			 uri "http://sadl.org/Suitability.dialog" alias stbltdlg.
			 
			 import "http://sadl.org/SpeedOfSound.sadl".
			 
			 What type of values can acceleration of Mass have?
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
//			val stmtitr = ontModel.listStatements()
//			while (stmtitr.hasNext) {
//				println(stmtitr.nextStatement)
//			}
			val errors = issues.filter[severity === Severity.ERROR]
			assertEquals(0, errors.size)
			if (processor instanceof JenaBasedDialogModelProcessor) {
				val jbdp = (processor as JenaBasedDialogModelProcessor)
				val resource = jbdp.currentResource
				val acm = jbdp.getAnswerCurationManager(resource)
				val conversation = acm.conversation
				assertNotNull(conversation)
				assertNotNull(conversation.statements);
				assertEquals(1, conversation.statements.size)
				assertTrue(conversation.statements.get(0).statement instanceof WhatValuesContent)
				val cc = conversation.statements.get(0).statement as WhatValuesContent
				val question = cc.getText().trim();
				val answer = acm.getAnswerToQuestion(question)
				assertTrue(processor.compareTranslations(actualAnswer, answer))	
			}
		]
	}
	
	@Test
	def void testCompareStatement_00() {
		val grd = newArrayList(
"Rule ComparePseudoRule0:  if rdf(v2, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://aske.ge.com/hypersonicsV2#Speed) and rdf(v2, http://sadl.org/sadlimplicitmodel#value, 300) and rdf(v2, http://sadl.org/sadlimplicitmodel#unit, \"mph\") and rdf(http://sadl.org/test4.dialog#v4, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://aske.ge.com/hypersonicsV2#CF6) and rdf(http://sadl.org/test4.dialog#v4, http://aske.ge.com/hypersonicsV2#speed, v2) then rdf(http://sadl.org/test4.dialog#v4, http://aske.ge.com/hypersonicsV2#machSpeed, v3).",
"Rule ComparePseudoRule1:  if rdf(v2, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://aske.ge.com/hypersonicsV2#Speed) and rdf(v2, http://sadl.org/sadlimplicitmodel#value, 300) and rdf(v2, http://sadl.org/sadlimplicitmodel#unit, \"mph\") and rdf(http://sadl.org/test4.dialog#v6, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://aske.ge.com/hypersonicsV2#F100) and rdf(http://sadl.org/test4.dialog#v6, http://aske.ge.com/hypersonicsV2#speed, v2) then rdf(http://sadl.org/test4.dialog#v6, http://aske.ge.com/hypersonicsV2#machSpeed, v5)."
)
		'''
			 uri "http://aske.ge.com/hypersonicsV2" alias hv2.
			 
			 PhysicalThing is a class,
			 	described by mass with values of type Mass
			 	described by volume with values of type Volume
			 	described by density with values of type Density
			 	described by temperature with values of type Temperature
			 	described by altitude with values of type Altitude
			 	described by speed with values of type Speed.
			 	
			 Speed is a type of UnittedQuantity.
			 Mass is a type of UnittedQuantity.
			 Volume is a type of UnittedQuantity.
			 Density is a type of UnittedQuantity.
			 Length is a type of UnittedQuantity.
			 Time is a type of UnittedQuantity.
			 Temperature is a type of UnittedQuantity.
			 Altitude is a type of UnittedQuantity.
			 MachSpeed is a type of Speed.
			 
			 Gas is a type of PhysicalThing
			 	described by pressure with values of type Pressure.
			 Air is a type of Gas.
			 machSpeed describes Air with values of type MachSpeed.
			 
			 SpecificNetThrust is a type of UnittedQuantity.
			 SpecificFuelConsumption is a type of UnittedQuantity.
			 
			 AircraftEngine is a type of PhysicalThing
			 	described by mach with values of type MachSpeed
			 	described by sfc (alias "specific fuel consumption") with values of type SpecificFuelConsumption
			 	described by thrust with values of type SpecificNetThrust.
			                     
			 {CF6, F100, J85, RamJet} are types of AircraftEngine.
		'''.sadl

		'''
			 uri "http://sadl.org/test4.dialog" alias test4dialog.
			 
			 import "http://aske.ge.com/hypersonicsV2".
			 
			 compare machSpeed of a CF6 and machSpeed of a F100 when speed is 300 mph.
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
//			val stmtitr = ontModel.listStatements()
//			while (stmtitr.hasNext) {
//				println(stmtitr.nextStatement)
//			}
			val errors = issues.filter[severity === Severity.ERROR]
			for (err : errors) {
				println(err)
			}
			assertEquals(0, errors.size)
			if (processor instanceof JenaBasedDialogModelProcessor) {
				val jbdp = (processor as JenaBasedDialogModelProcessor)
				val resource = jbdp.currentResource
				val conversation = jbdp.getAnswerCurationManager(resource).conversation
				assertNotNull(conversation)
				assertNotNull(conversation.statements);
				assertEquals(1, conversation.statements.size)
				assertTrue(conversation.statements.get(0).statement instanceof CompareContent)
				val cc = conversation.statements.get(0).statement as CompareContent
				val rules = cc.comparisonRules
				for (r : rules) {
					println(r.toFullyQualifiedString)
				}
				assertEquals(2, rules.size)
				for (var idx = 0; idx < rules.size; idx++) {
					assertEquals(grd.get(idx), rules.get(idx).toFullyQualifiedString)					
				}
			}
		]
	}
	@Test
	def void testCompareStatement_01() {
			val grd = newArrayList(
"Rule ComparePseudoRule0:  if rdf(v2, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v2, http://sadl.org/sadlimplicitmodel#value, 0.35) and rdf(http://aske.ge.com/testdiag#v4, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/Model.sadl#CF6) and rdf(http://aske.ge.com/testdiag#v4, http://sadl.org/Model.sadl#sfc, v2) then rdf(http://aske.ge.com/testdiag#v4, http://sadl.org/Model.sadl#thrust, v3).",
"Rule ComparePseudoRule1:  if rdf(v2, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v2, http://sadl.org/sadlimplicitmodel#value, 0.35) and rdf(http://aske.ge.com/testdiag#v6, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/Model.sadl#F100) and rdf(http://aske.ge.com/testdiag#v6, http://sadl.org/Model.sadl#sfc, v2) then rdf(http://aske.ge.com/testdiag#v6, http://sadl.org/Model.sadl#thrust, v5)."
)
		'''
			uri "http://sadl.org/Model.sadl" alias mdl.
			
			AircraftEngine is a class,
				described by sfc (alias "specific fuel consumption") with values of type float,
				described by thrust with values of type float.
			
			{CF6, F100, RamJet} are types of AircraftEngine.
		'''.sadl

		'''
			uri "http://aske.ge.com/testdiag" alias testdiag.
			
			import "http://sadl.org/Model.sadl".
			
			Compare thrust of a CF6 and thrust of a F100 when sfc is .35.
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
//			val stmtitr = ontModel.listStatements()
//			while (stmtitr.hasNext) {
//				println(stmtitr.nextStatement)
//			}
			val errors = issues.filter[severity === Severity.ERROR]
			assertEquals(0, errors.size)
			if (processor instanceof JenaBasedDialogModelProcessor) {
				val jbdp = (processor as JenaBasedDialogModelProcessor)
				val resource = jbdp.currentResource
				val acm = jbdp.getAnswerCurationManager(resource)
				val conversation = acm.conversation
				assertNotNull(conversation)
				assertNotNull(conversation.statements);
				assertEquals(1, conversation.statements.size)
				assertTrue(conversation.statements.get(0).statement instanceof CompareContent)
				val cc = conversation.statements.get(0).statement as CompareContent
				val rules = cc.comparisonRules
				assertEquals(2, rules.size)
				assertEquals(grd.get(0), rules.get(0).toFullyQualifiedString)
				assertEquals(grd.get(1), rules.get(1).toFullyQualifiedString)
			}
		]
	}

	@Test
	def void testCompareStatement_02() {
			val grd = newArrayList(
"Rule ComparePseudoRule0:  if rdf(v0, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v0, http://sadl.org/sadlimplicitmodel#value, 0.35) and rdf(http://aske.ge.com/testdiag#v2, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/Model.sadl#RamJet) and rdf(http://aske.ge.com/testdiag#v2, http://sadl.org/Model.sadl#sfc, v0) then rdf(http://aske.ge.com/testdiag#v2, http://sadl.org/Model.sadl#thrust, v1).",
"Rule ComparePseudoRule1:  if rdf(v0, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v0, http://sadl.org/sadlimplicitmodel#value, 0.35) and rdf(http://aske.ge.com/testdiag#v4, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/Model.sadl#F100) and rdf(http://aske.ge.com/testdiag#v4, http://sadl.org/Model.sadl#sfc, v0) then rdf(http://aske.ge.com/testdiag#v4, http://sadl.org/Model.sadl#thrust, v3).",
"Rule ComparePseudoRule2:  if rdf(v0, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v0, http://sadl.org/sadlimplicitmodel#value, 0.35) and rdf(http://aske.ge.com/testdiag#v6, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/Model.sadl#CF6) and rdf(http://aske.ge.com/testdiag#v6, http://sadl.org/Model.sadl#sfc, v0) then rdf(http://aske.ge.com/testdiag#v6, http://sadl.org/Model.sadl#thrust, v5)."
)
		'''
			uri "http://sadl.org/Model.sadl" alias mdl.
						
						AircraftEngine is a class,
							described by sfc (alias "specific fuel consumption") with values of type float,
							described by thrust with values of type float.
						
						{CF6, F100, RamJet} are types of AircraftEngine.
		'''.sadl

		'''
			uri "http://aske.ge.com/testdiag" alias testdiag.
			
			import "http://sadl.org/Model.sadl".
			
			Compare thrust when sfc is .35.
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
//			val stmtitr = ontModel.listStatements()
//			while (stmtitr.hasNext) {
//				println(stmtitr.nextStatement)
//			}
			val errors = issues.filter[severity === Severity.ERROR]
			assertEquals(0, errors.size)
			if (processor instanceof JenaBasedDialogModelProcessor) {
				val jbdp = (processor as JenaBasedDialogModelProcessor)
				val resource = jbdp.currentResource
				val acm = jbdp.getAnswerCurationManager(resource)
				val conversation = acm.conversation
				assertNotNull(conversation)
				assertNotNull(conversation.statements);
				assertEquals(1, conversation.statements.size)
				assertTrue(conversation.statements.get(0).statement instanceof CompareContent)
				val cc = conversation.statements.get(0).statement as CompareContent
				val rules = cc.comparisonRules
				assertEquals(3, rules.size)
				assertEquals(grd.get(0), rules.get(0).toFullyQualifiedString)
				assertEquals(grd.get(1), rules.get(1).toFullyQualifiedString)
				assertEquals(grd.get(2), rules.get(2).toFullyQualifiedString)
			}
		]
	}

	@Test
	def void testCompareStatement_03() {
			val grd = newArrayList(
"Rule ComparePseudoRule0:  if rdf(v0, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v0, http://sadl.org/sadlimplicitmodel#value, 0.35) and rdf(http://aske.ge.com/testdiag#v2, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/Model.sadl#RamJet) and rdf(http://aske.ge.com/testdiag#v2, http://sadl.org/Model.sadl#sfc, v0) then rdf(http://aske.ge.com/testdiag#v2, http://sadl.org/Model.sadl#thrust, v1).",
"Rule ComparePseudoRule1:  if rdf(v0, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v0, http://sadl.org/sadlimplicitmodel#value, 0.35) and rdf(http://aske.ge.com/testdiag#v4, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/Model.sadl#F100) and rdf(http://aske.ge.com/testdiag#v4, http://sadl.org/Model.sadl#sfc, v0) then rdf(http://aske.ge.com/testdiag#v4, http://sadl.org/Model.sadl#thrust, v3).",
"Rule ComparePseudoRule2:  if rdf(v0, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v0, http://sadl.org/sadlimplicitmodel#value, 0.35) and rdf(http://aske.ge.com/testdiag#v6, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/Model.sadl#CF6) and rdf(http://aske.ge.com/testdiag#v6, http://sadl.org/Model.sadl#sfc, v0) then rdf(http://aske.ge.com/testdiag#v6, http://sadl.org/Model.sadl#thrust, v5)."
)
		'''
			uri "http://sadl.org/Model.sadl" alias mdl.
			
			AircraftEngine is a class,
				described by sfc (alias "specific fuel consumption") with values of type float,
				described by thrust with values of type float.
			
			{CF6, F100, RamJet} are types of AircraftEngine.
		'''.sadl

		'''
			uri "http://aske.ge.com/testdiag" alias testdiag.
			
			import "http://sadl.org/Model.sadl".
			
			Compare AircraftEngine when sfc is .35.
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
			val errors = issues.filter[severity === Severity.ERROR]
			assertEquals(0, errors.size)
			if (processor instanceof JenaBasedDialogModelProcessor) {
				val jbdp = (processor as JenaBasedDialogModelProcessor)
				val resource = jbdp.currentResource
				val acm = jbdp.getAnswerCurationManager(resource)
				val conversation = acm.conversation
				assertNotNull(conversation)
				assertNotNull(conversation.statements);
				assertEquals(1, conversation.statements.size)
				assertTrue(conversation.statements.get(0).statement instanceof CompareContent)
				val cc = conversation.statements.get(0).statement as CompareContent
				val rules = cc.comparisonRules
				assertEquals(3, rules.size)
				assertEquals(grd.get(0), rules.get(0).toFullyQualifiedString)
				assertEquals(grd.get(1), rules.get(1).toFullyQualifiedString)
				assertEquals(grd.get(2), rules.get(2).toFullyQualifiedString)
			}
		]
	}

	@Test
	def void testCompareStatement_04() {
			val grd = newArrayList(
"Rule ComparePseudoRule0:  if rdf(v0, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v0, http://sadl.org/sadlimplicitmodel#value, 0.35) and rdf(http://aske.ge.com/testdiag#YourF100, http://sadl.org/Model.sadl#sfc, v0) then rdf(http://aske.ge.com/testdiag#YourF100, http://sadl.org/Model.sadl#thrust, v1).",
"Rule ComparePseudoRule1:  if rdf(v0, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v0, http://sadl.org/sadlimplicitmodel#value, 0.35) and rdf(http://aske.ge.com/testdiag#MyCF6, http://sadl.org/Model.sadl#sfc, v0) then rdf(http://aske.ge.com/testdiag#MyCF6, http://sadl.org/Model.sadl#thrust, v2)."
)
		'''
			uri "http://sadl.org/Model.sadl" alias mdl.
			
			AircraftEngine is a class,
				described by sfc (alias "specific fuel consumption") with values of type float,
				described by thrust with values of type float.
			
			{CF6, F100, RamJet} are types of AircraftEngine.
		'''.sadl

		'''
			uri "http://aske.ge.com/testdiag" alias testdiag.
			
			import "http://sadl.org/Model.sadl".
			
			MyCF6 is a CF6.
			YourF100 is an F100.
			
			Compare AircraftEngine when sfc is .35.
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
			val errors = issues.filter[severity === Severity.ERROR]
			assertEquals(0, errors.size)
			if (processor instanceof JenaBasedDialogModelProcessor) {
				val jbdp = (processor as JenaBasedDialogModelProcessor)
				val resource = jbdp.currentResource
				val acm = jbdp.getAnswerCurationManager(resource)
				val conversation = acm.conversation
				assertNotNull(conversation)
				assertNotNull(conversation.statements);
				assertEquals(3, conversation.statements.size)
				assertTrue(conversation.statements.get(2).statement instanceof CompareContent)
				val cc = conversation.statements.get(2).statement as CompareContent
				val rules = cc.comparisonRules
				assertEquals(2, rules.size)
				assertEquals(grd.get(0), rules.get(0).toFullyQualifiedString)
				assertEquals(grd.get(1), rules.get(1).toFullyQualifiedString)
			}
		]
	}

	@Test
	def void testCompareStatement_05() {
			val grd = newArrayList(
"Rule ComparePseudoRule0:  if rdf(v0, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v0, http://sadl.org/sadlimplicitmodel#value, 0.35) and rdf(http://aske.ge.com/testdiag#CF6b, http://sadl.org/Model.sadl#sfc, v0) then rdf(http://aske.ge.com/testdiag#CF6b, http://sadl.org/Model.sadl#thrust, v1).",
"Rule ComparePseudoRule1:  if rdf(v0, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v0, http://sadl.org/sadlimplicitmodel#value, 0.35) and rdf(http://aske.ge.com/testdiag#CF6a, http://sadl.org/Model.sadl#sfc, v0) then rdf(http://aske.ge.com/testdiag#CF6a, http://sadl.org/Model.sadl#thrust, v2)."
)
		'''
			uri "http://sadl.org/Model.sadl" alias mdl.
			
			AircraftEngine is a class,
				described by sfc (alias "specific fuel consumption") with values of type float,
				described by thrust with values of type float.
			
			{CF6, F100, RamJet} are types of AircraftEngine.
		'''.sadl

		'''
			uri "http://aske.ge.com/testdiag" alias testdiag.
			
			import "http://sadl.org/Model.sadl".
			
			{CF6a, CF6b} are instances of CF6.
			
			Compare CF6 when sfc is .35.
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
//			val stmtitr = ontModel.listStatements()
//			while (stmtitr.hasNext) {
//				println(stmtitr.nextStatement)
//			}
			val errors = issues.filter[severity === Severity.ERROR]
			assertEquals(0, errors.size)
			if (processor instanceof JenaBasedDialogModelProcessor) {
				val jbdp = (processor as JenaBasedDialogModelProcessor)
				val resource = jbdp.currentResource
				val acm = jbdp.getAnswerCurationManager(resource)
				val conversation = acm.conversation
				assertNotNull(conversation)
				assertNotNull(conversation.statements);
				assertEquals(2, conversation.statements.size)
				assertTrue(conversation.statements.get(1).statement instanceof CompareContent)
				val cc = conversation.statements.get(1).statement as CompareContent
				val rules = cc.comparisonRules
				for (r : rules) {
					println(r.toFullyQualifiedString)
				}
				assertEquals(2, rules.size)
				assertEquals(grd.get(0), rules.get(0).toFullyQualifiedString)
				assertEquals(grd.get(1), rules.get(1).toFullyQualifiedString)
			}
		]
	}

	@Test
	def void testCompareStatement_06() {
			val grd = newArrayList(
"Rule ComparePseudoRule0:  if rdf(v3, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v3, http://sadl.org/sadlimplicitmodel#value, 0.36) and rdf(http://aske.ge.com/testdiag#v5, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/Model.sadl#CF6) and rdf(http://aske.ge.com/testdiag#v5, http://sadl.org/Model.sadl#sfc, v3) then rdf(http://aske.ge.com/testdiag#v5, http://sadl.org/Model.sadl#thrust, v4).",
"Rule ComparePseudoRule1:  if rdf(v3, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v3, http://sadl.org/sadlimplicitmodel#value, 0.36) and rdf(http://aske.ge.com/testdiag#v7, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/Model.sadl#F100) and rdf(http://aske.ge.com/testdiag#v7, http://sadl.org/Model.sadl#sfc, v3) then rdf(http://aske.ge.com/testdiag#v7, http://sadl.org/Model.sadl#thrust, v6).",
"Rule ComparePseudoRule2:  if rdf(v3, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v3, http://sadl.org/sadlimplicitmodel#value, 0.36) and rdf(http://aske.ge.com/testdiag#v9, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/Model.sadl#RamJet) and rdf(http://aske.ge.com/testdiag#v9, http://sadl.org/Model.sadl#sfc, v3) then rdf(http://aske.ge.com/testdiag#v9, http://sadl.org/Model.sadl#thrust, v8)."
)
		'''
			uri "http://sadl.org/Model.sadl" alias mdl.
			
			AircraftEngine is a class,
				described by sfc (alias "specific fuel consumption") with values of type float,
				described by thrust with values of type float.
			
			{CF6, F100, RamJet} are types of AircraftEngine.
		'''.sadl

		'''
			uri "http://aske.ge.com/testdiag" alias testdiag.
			
			import "http://sadl.org/Model.sadl".
			
			Compare a CF6 and an F100 and a RamJet when sfc is .36.
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
//			val stmtitr = ontModel.listStatements()
//			while (stmtitr.hasNext) {
//				println(stmtitr.nextStatement)
//			}
			val errors = issues.filter[severity === Severity.ERROR]
			assertEquals(0, errors.size)
			if (processor instanceof JenaBasedDialogModelProcessor) {
				val jbdp = (processor as JenaBasedDialogModelProcessor)
				val resource = jbdp.currentResource
				val acm = jbdp.getAnswerCurationManager(resource)
				val conversation = acm.conversation
				assertNotNull(conversation)
				assertNotNull(conversation.statements);
				assertEquals(1, conversation.statements.size)
				assertTrue(conversation.statements.get(0).statement instanceof CompareContent)
				val cc = conversation.statements.get(0).statement as CompareContent
				val rules = cc.comparisonRules
				for (r : rules) {
					println(r.toFullyQualifiedString)
				}
				assertEquals(3, rules.size)
				assertEquals(grd.get(0), rules.get(0).toFullyQualifiedString)
				assertEquals(grd.get(1), rules.get(1).toFullyQualifiedString)
				assertEquals(grd.get(2), rules.get(2).toFullyQualifiedString)
			}
		]
	}
	
	@Test
	def void testThinThread1() {
			val grd = newArrayList(
"Rule ComparePseudoRule0:  if rdf(v0, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v0, http://sadl.org/sadlimplicitmodel#value, 25000) and rdf(v0, http://sadl.org/sadlimplicitmodel#unit, \"ft\") and rdf(http://aske.ge.com/testdiag#v3, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/Model.sadl#RamJet) and rdf(http://aske.ge.com/testdiag#v3, http://sadl.org/Model.sadl#altitude, v0) and rdf(v1, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v1, http://sadl.org/sadlimplicitmodel#value, 800) and rdf(v1, http://sadl.org/sadlimplicitmodel#unit, \"mph\") and rdf(http://aske.ge.com/testdiag#v3, http://sadl.org/Model.sadl#speed, v1) then rdf(http://aske.ge.com/testdiag#v3, http://sadl.org/Model.sadl#mach, v2).",
"Rule ComparePseudoRule1:  if rdf(v0, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v0, http://sadl.org/sadlimplicitmodel#value, 25000) and rdf(v0, http://sadl.org/sadlimplicitmodel#unit, \"ft\") and rdf(http://aske.ge.com/testdiag#v5, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/Model.sadl#J85) and rdf(http://aske.ge.com/testdiag#v5, http://sadl.org/Model.sadl#altitude, v0) and rdf(v1, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v1, http://sadl.org/sadlimplicitmodel#value, 800) and rdf(v1, http://sadl.org/sadlimplicitmodel#unit, \"mph\") and rdf(http://aske.ge.com/testdiag#v5, http://sadl.org/Model.sadl#speed, v1) then rdf(http://aske.ge.com/testdiag#v5, http://sadl.org/Model.sadl#mach, v4).",
"Rule ComparePseudoRule2:  if rdf(v0, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v0, http://sadl.org/sadlimplicitmodel#value, 25000) and rdf(v0, http://sadl.org/sadlimplicitmodel#unit, \"ft\") and rdf(http://aske.ge.com/testdiag#v7, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/Model.sadl#F100) and rdf(http://aske.ge.com/testdiag#v7, http://sadl.org/Model.sadl#altitude, v0) and rdf(v1, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v1, http://sadl.org/sadlimplicitmodel#value, 800) and rdf(v1, http://sadl.org/sadlimplicitmodel#unit, \"mph\") and rdf(http://aske.ge.com/testdiag#v7, http://sadl.org/Model.sadl#speed, v1) then rdf(http://aske.ge.com/testdiag#v7, http://sadl.org/Model.sadl#mach, v6).",
"Rule ComparePseudoRule3:  if rdf(v0, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v0, http://sadl.org/sadlimplicitmodel#value, 25000) and rdf(v0, http://sadl.org/sadlimplicitmodel#unit, \"ft\") and rdf(http://aske.ge.com/testdiag#v9, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/Model.sadl#CF6) and rdf(http://aske.ge.com/testdiag#v9, http://sadl.org/Model.sadl#altitude, v0) and rdf(v1, http://www.w3.org/1999/02/22-rdf-syntax-ns#type, http://sadl.org/sadlimplicitmodel#UnittedQuantity) and rdf(v1, http://sadl.org/sadlimplicitmodel#value, 800) and rdf(v1, http://sadl.org/sadlimplicitmodel#unit, \"mph\") and rdf(http://aske.ge.com/testdiag#v9, http://sadl.org/Model.sadl#speed, v1) then rdf(http://aske.ge.com/testdiag#v9, http://sadl.org/Model.sadl#mach, v8)."
)
		'''
			uri "http://sadl.org/Model.sadl" alias mdl.
			
			AircraftEngine is a class,
				described by mach with values of type float,
				described by speed with values of type UnittedQuantity,
				described by altitude with values of type UnittedQuantity,
				described by sfc (alias "specific fuel consumption") with values of type float,
				described by thrust with values of type float.
			
			{CF6, F100, J85, RamJet} are types of AircraftEngine.
		'''.sadl

		'''
			uri "http://aske.ge.com/testdiag" alias testdiag.
			
			import "http://sadl.org/Model.sadl".
			
			What is mach when altitude is 25000 ft and speed is 800 mph?
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
//			val stmtitr = ontModel.listStatements()
//			while (stmtitr.hasNext) {
//				println(stmtitr.nextStatement)
//			}
			val errors = issues.filter[severity === Severity.ERROR]
			assertEquals(0, errors.size)
			if (processor instanceof JenaBasedDialogModelProcessor) {
				val jbdp = (processor as JenaBasedDialogModelProcessor)
				val resource = jbdp.currentResource
				val acm = jbdp.getAnswerCurationManager(resource)
				val conversation = acm.conversation
				assertNotNull(conversation)
				assertNotNull(conversation.statements);
				assertEquals(1, conversation.statements.size)
				assertTrue(conversation.statements.get(0).statement instanceof WhatIsContent)
				val cc = conversation.statements.get(0).statement as WhatIsContent
				val rules = cc.getComputationalGraphRules
				for (r : rules) {
					println(r.toFullyQualifiedString)
				}
				assertEquals(4, rules.size)
				for (var i = 0; i < 4; i++) {
					assertEquals(grd.get(i), rules.get(i).toFullyQualifiedString)
				}
			}
		]
	}

	@Ignore	// this test will only pass if the service is running at the specified URL
	@Test
	def void testUnittedQuantityTriples() {
		'''
			uri "http://aske.ge.com/sciknow" alias sciknow .
			
			UnittedQuantity has impliedProperty ^value.
			
			Temperature is a type of UnittedQuantity.
			Pressure is a type of UnittedQuantity.
			
			Speed is a type of UnittedQuantity.
			Mass is a type of UnittedQuantity.
			Volume is a type of UnittedQuantity.
			Density is a type of UnittedQuantity.
			Length is a type of UnittedQuantity.
			Time is a type of UnittedQuantity.
			
				
			PhysicalThing is a class,
				described by mass with values of type Mass
				described by volume with values of type Volume
				described by density with values of type Density
				described by temperature with values of type Temperature
			   .
			
			Gas is a type of PhysicalThing
				described by pressure with values of type Pressure.
			Air is a type of Gas.
		'''.sadl
		
		'''
			uri "http://aske.ge.com/hypersonicsV2" alias hypersonicsV2 .
			
			//import "http://aske.ge.com/compgraph". 
			import "http://aske.ge.com/sciknow".
			
			Altitude is a type of UnittedQuantity.
			AtmosphericTemperature is a type of Temperature.
			AtmosphericPressure is a type of Pressure.
			StaticTemperature is a type of AtmosphericTemperature.
			StaticPressure is a type of AtmosphericPressure.
			TotalTemperature is a type of AtmosphericTemperature.
			TotalPressure is a type of AtmosphericPressure.
			SpeedOfSound is a type of UnittedQuantity.
			AirSpeed is a type of Speed.
			MachSpeed is a type of Speed.
			
			StaticTemperatureEquation is a type of ^Equation.
			StaticPressureEquation is a type of ^Equation.
			SpeedOfSoundEquation is a type of ^Equation.
			MachSpeedEquation is a type of ^Equation.
			TotalTemperatureEquation is a type of ^Equation.
			TotalPressureEquation is a type of ^Equation.
			
			altitude describes Air with values of type Altitude.
			staticTemperature is a type of temperature.
			staticTemperature describes Air with values of type StaticTemperature.
			totalTemperature is a type of temperature.
			totalTemperature describes Air with values of type TotalTemperature.
			staticPressure is a type of pressure.
			staticPressure describes Air with values of type StaticPressure.
			totalPressure is a type of pressure.
			totalPressure describes Air with values of type TotalPressure.
			
			speed describes PhysicalThing with values of type Speed.
			speedOfSound describes Air with values of type SpeedOfSound.
			airSpeed describes Air with values of type AirSpeed.
			machSpeed describes Air with values of type MachSpeed.
			
			gamma describes Air with values of type double.
			rgas describes Air with values of type double.
			
			
			//tropo_c1 describes Air with values of type double.
			//tropo_c2 describes Air with values of type double.
			
			////Troposphere Constants
			//Equation tropo_C1_Eq() returns double (tropo_c1 of some Air {F}) : return 59.0 .
			//Equation tropo_C2_Eq() returns double (tropo_c2 of some Air {"1/ft"}) : return .00356 .
			
			Equation gamma_const() returns double (gamma of some Air): return 1.4 . //unitless
			gamma_const has expression (a Script script "1.4" language Python).
			
			Equation rgas_const() returns double (rgas of some Air): return 1718 . //ft*lb/slug/Rankine
			//ft*lb/slug/Rankine = ft^2/sec^2/Rankine
			rgas_const has expression (a Script script "1718" language Python).
			
			//Equation farenheitToRankine(double f (temperature of some PhysicalThing {F}))
			//		returns double (temperature of the PhysicalThing {Rankine}) : f + 459.67. 
			
			Equation mphToftpersec(double s (speed of a PhysicalThing {mph}))
					returns double (speed of the PhysicalThing {"ft/sec"}):
					return s*5280/3600.
			// Code expression if using Pint for unit conversion.
			mphToftpersec has expression (a Script script "s * 5280/3600" language Python).
			
			//air is an Air with altitude alt.
			//alt is an Altitude.
			
			
			//Equation st_temp_eq(Altitude alt (altitude of some Air 
			//	                            and unit of alt is "ft" 
			//	                            and ^value of alt < 36152 ) )
			//	returns StaticTemperature (staticTemperature of the Air {F}) : 
			//	return (a StaticTemperature with ^value (59 - 0.00356 * ^value of alt) unit "F") .
			
			//Equation foo(double alt (Altitude)) //altitude of some Air))
			//	returns double (Altitude) :
			//	return alt.
			
			st_temp_eq_tropo is a StaticTemperatureEquation.
			
			Equation st_temp_eq_tropo(double alt (altitude of some Air and alt < 36152 {ft}) )
				returns double (staticTemperature of the Air {F}) : 
				return 59 - 0.00356 * alt .
			
			st_temp_eq_tropo has expression (a Script with script "59 - 0.00356 * alt" language Python).
			
			st_temp_eq_lowerStrato is a StaticTemperatureEquation.
			Equation st_temp_eq_lowerStrato(double alt (altitude of some Air and alt < 82345 and alt >= 36152 {m}))
				returns double (staticTemperature of the Air {F}) : 
				return -70 .
			
			st_temp_eq_lowerStrato has expression (a Script with script "-70" language Python).
			
			st_temp_eq_upperStrato is a StaticTemperatureEquation.
			Equation st_temp_eq_upperStrato(double alt (altitude of some Air and alt > 82345 {ft}) )
				returns double (staticTemperature of the Air {F}) : 
				return -205.05 + .00164 * alt .
			
			st_temp_eq_upperStrato has expression (a Script with script "-205.05 + .00164 * alt" language Python).
			
			
			st_pressure_eq_tropo is a StaticPressureEquation.
			Equation st_pressure_eq_tropo(double alt (altitude of some Air and alt < 36152 {ft}))
				returns double (staticPressure of the Air {"force_pound/ft**2"}) : 
				return -2116.0 * ((59 - 0.00356 * alt + 459.7) / 518.6)^5.256 .
			
			st_pressure_eq_tropo has expression (a Script with script "-2116.0 * ((59 - 0.00356 * alt + 459.7) / 518.6)**5.256" language Python).
			
			
			
			st_pressure_eq_lowerStrato is a StaticPressureEquation.
			Equation st_pressure_eq_lowerStrato(double alt (altitude of some Air and alt < 82345 
				                            and alt > 36152 {ft}) )
				returns double (staticPressure of the Air {"force_pound/ft**2"}) : 
				//return 473.1 * exp(1.73 - .000048 * alt) .
				return 473.1 * e^(1.73 - .000048 * alt) .
			
			st_pressure_eq_lowerStrato has expression (a Script with script "473.1 * math.exp(1.73 - .000048 * alt)" language Python).
			
			st_pressure_eq_upperStrato is a StaticPressureEquation.
			Equation st_pressure_eq_upperStrato(double alt (altitude of some Air and alt > 82345 {ft}) )
				returns double (staticPressure of the Air {"force_pound/ft**2"}) : 
				return 51.97 * (((-205.05 + .00164 * alt) + 459.7)/ 389.98)^-11.388 .
				
			st_pressure_eq_upperStrato has expression (a Script with script "51.97 * (((-205.05 + .00164 * alt) + 459.7)/ 389.98)**-11.388" language Python).
				
			sos_eq is a SpeedOfSoundEquation.
			Equation sos_eq(double ts0 (staticTemperature of some Air {F}),
							double g (gamma of the Air),
							double R (rgas of the Air)
							)
				returns double (speedOfSound of the Air {"ft/sec"}) :
				return sqrt(g * R * ts0) .
			
			sos_eq has expression (a Script with script "g * R * ts0" language Python).
							
			mach_speed_eq is a MachSpeedEquation.
			//Equation mach_speed_eq(double u0 (airSpeed of some Air {"mph"}),
			//					double a0 (speedOfSound of the Air {"ft/sec"}) )
			//			returns double (machSpeed of the Air) :
			//			//return u0 * 5280.0 / 3600.0 / a0 .
			//			// 1 mile = 5280 ft
			//			// 
			//			return u0*5280.0 * 3600.0 * a0 .
			
			Equation mach_speed_eq(double u0 (airSpeed of some Air {"ft/sec"}),
								double a0 (speedOfSound of the Air {"ft/sec"}) )
						returns double (machSpeed of the Air) :
						return u0 / a0 .
			
			mach_speed_eq has expression (a Script with script "u0 / a0" language Python).
						
			total_temp_eq is a TotalTemperatureEquation.
			Equation total_temp_eq(double ts0 (staticTemperature of some Air {F}),
								   double mach (machSpeed of the Air) )
						returns double (totalTemperature of the Air {F}) :
						return ts0*(1.0 + 0.5*(1.4-1.0)*mach*mach).
			
			total_temp_eq has expression (a Script with script "ts0*(1.0 + 0.5*(1.4-1.0)*mach*mach)" language Python).
			
			
			total_pressure_eq is a TotalTemperatureEquation.
			Equation total_pressure_eq(double ps0 (staticPressure of some Air {"force_pound/ft**2"}),
								   double mach (machSpeed of the Air) )
						returns double (totalPressure of the Air {"force_pound/ft**2"}) :
						//return ps0*(1.0 + 0.5*(1.4-1.0)*mach*mach)^(1.4/(1.4-1.0)).
						return ps0*(1.0 + 0.5*(1.4-1.0)*mach*mach)^(1.4*(1.4-1.0)).
			
			total_pressure_eq has expression (a Script with script "ps0*(1.0 + 0.5*(1.4-1.0)*mach*mach)**(1.4*(1.4-1.0))" language Python).
		'''.sadl
		
		'''
			uri "http://aske.ge.com/compgraphmodel" alias compgraphmodel .
			
			Thing is a class
				described by name with values of type string
				described by description with values of type string
				.
			
			ComputationalGraph is a class.
			
			KChain is a type of ComputationalGraph.
			
			DBN is a type of ComputationalGraph
				//described by input with values of type UnittedQuantity
				described by node with a single value of type DataDescriptor //was Argument
				// 'script' is a string containing python code for the computation
				//described by script with values of type string
				described by hasEquation with a single value of type ^Equation
				described by hasModel with a single value of type ^ExternalEquation
				// DBN info
				described by ^type with values of type NodeType
				described by distribution with a single value of type Distribution	
				described by range with values of type Range 
				.
			
			
			TaskType is a class, must be one of {prognosis,counterfactual,sensitivityAnalysis,riskAssessment,optimization}.
			
			
			NodeType is a class, must be one of {equation,discrete,continuous,constant,deterministic,stochastic_transient,stochastic} .
			
			Distribution is a class, must be one of {uniform,discrete_custom} .
			
			PTable is a type of Thing.
			       //described by 
			
			Range is a class
				described by lower with a single value of type float
				described by upper with a single value of type float
				.
		'''.sadl
		
		'''
			uri "http://aske.ge.com/metamodel" alias mm.
			 
			import "http://aske.ge.com/compgraphmodel".
			
			//Composite CG
			CCG is a type of ComputationalGraph
			    described by subgraph with values of type SubGraph //Multiple subgraphs
			    //described by modelError with values of type decimal
			 	.
			 
			SubGraph is a class
			 	described by cgraph with a single value of type ComputationalGraph
			 	described by output with values of type UnittedQuantity
			 	.
			
			QueryType is a class must be one of {prognostic,calibration,explanation,sensitivity}.
			 
			CGQuery is a class
				described by queryType with a single value of type QueryType
			 	described by input with values of type UnittedQuantity
			 	described by machineGenerated with a single value of type boolean
			 	described by execution with values of type CGExecution //A query can result in multiple models and executions
			 	.
			 	
			CGExecution is a class
			 	described by startTime with a single value of type dateTime
			 	described by endTime with a single value of type dateTime
			 	described by compGraph with a single value of type CCG //A DBN in our case.
			 	described by output with values of type UnittedQuantity
			 	described by accuracy with a single value of type decimal
				.
			
			Dataset is a type of Thing
				described by creator with a single value of type string //Person
			//	described by location with a single value of type string
				.
			
			CSVDataset is a type of Dataset
				described by column with values of type CSVColumn 
				.
				
			CSVColumn is a type of Thing
				described by header with values of type string
				described by colType with values of type string
				described by variable with values of type UnittedQuantity //Variable
				described by percentMissingValues with values of type float
				. 
			
			parent describes ^Equation with values of type ^Equation.
		'''.sadl
		
		'''
			uri "http://aske.ge.com/dbnnodes" alias dbnnodes .
			
			import "http://aske.ge.com/compgraphmodel".
			import "http://aske.ge.com/hypersonicsV2". 
			
			// Hypersonics domain DBNs. The general DBN definitions are in CompGraphModel.sadl
			
			
			//Note: need implicitModel to have semType with value of type UnittedQuantity.
					
			AltitudeDBN is a type of DBN.
			node of AltitudeDBN always has value (a DataDescriptor localDescriptorName "altitude" augmentedType (a SemanticType semType Altitude) ).
			distribution of AltitudeDBN always has value uniform.
			range of AltitudeDBN always has value (a Range lower 0 upper 60000).
			
			AirSpeedDBN is a type of DBN.
			node of AirSpeedDBN always has value (a DataDescriptor localDescriptorName "speed" augmentedType (a SemanticType semType AirSpeed) ).
			distribution of AirSpeedDBN always has value uniform.
			range of AirSpeedDBN always has value (a Range lower 0 upper 5000).
			
			
			StaticTempDBN is a type of DBN.
			hasEquation of StaticTempDBN only has values of type StaticTemperatureEquation. //StaticTempEq. //or StaticTempEq2) .
			//hasEquation of StaticTempDBN has at most one value of type StaticTempEq2. // {StaticTempEq or StaticTempEq2} .
			range of StaticTempDBN always has value (a Range with lower 359 with upper 700).
			distribution of StaticTempDBN always has value uniform. //should be default value
			
			
			StaticPressureDBN is a type of DBN.
			hasEquation of  StaticPressureDBN only has values of type StaticPressureEquation.
			range of StaticPressureDBN always has value (a Range with lower 0 with upper 30000).
			distribution of StaticPressureDBN always has value uniform.
			
			SpeedOfSoundDBN is a type of DBN.
			hasEquation of  SpeedOfSoundDBN only has values of type SpeedOfSoundEquation.
			range of SpeedOfSoundDBN always has value (a Range with lower 270 with upper 350).
			distribution of SpeedOfSoundDBN always has value uniform.
			
			MachSpeedDBN is a type of DBN.
			hasEquation of  MachSpeedDBN only has values of type MachSpeedEquation.
			range of MachSpeedDBN always has value (a Range with lower 0 with upper 20).
			distribution of MachSpeedDBN always has value uniform.
			
			
			TotalTemperatureDBN is a type of DBN.
			hasEquation of  TotalTemperatureDBN only has values of type TotalTemperatureEquation.
			range of TotalTemperatureDBN always has value (a Range with lower 359 with upper 2000).
			distribution of TotalTemperatureDBN always has value uniform.
			
			TotalPressureDBN is a type of DBN.
			hasEquation of  TotalPressureDBN only has values of type TotalPressureEquation.
			range of TotalPressureDBN always has value (a Range with lower 0 with upper 50000).
			distribution of TotalPressureDBN always has value uniform.
		'''.sadl
		
		'''
			uri "http://aske.ge.com/testdiag" alias testdiag.
			
			import "http://aske.ge.com/metamodel".
			import "http://aske.ge.com/hypersonicsV2".
			import "http://aske.ge.com/dbnnodes".
			
			what is the staticTemperature of some Air when the altitude of the Air is 35000 ft?
			what is the staticTemperature of some Air when the altitude of the Air is 35000?
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
			val stmtitr = ontModel.listStatements()
			while (stmtitr.hasNext) {
				println(stmtitr.nextStatement)
			}
			val errors = issues.filter[severity === Severity.ERROR]
			assertEquals(0, errors.size)
		]
	}

//	@Ignore
	@Test
	def void testSuitabilityStatement_01() {
		'''
			 uri "http://sadl.org/Suitability.sadl" alias stblt.
			 
			 Resource is a class.
			 Equipment is a type of Resource.
			 part describes Equipment with values of type Equipment.
			 {Aircraft, AircraftEngine} are types of Equipment.
			 part of Aircraft has at least 1 value of type AircraftEngine.
			 altitude describes Aircraft with values of type UnittedQuantity.
			 
			 thrust describes AircraftEngine with values of type UnittedQuantity.
			 weight describes Equipment with values of type UnittedQuantity.
			 speed describes Aircraft with values of type UnittedQuantity.
			 sfc describes AircraftEngine with values of type float.
			  
			 Mission is a class, described by requires with values of type Resource.
			 suitable describes Resource with values of type Mission.
			 
			// MissionX is a Mission, requires (an Aircraft with speed 1.0 mach, with altitude between 25000 ft and 260000 ft, with part
			// 	(an AircraftEngine with thrust at least 25000 lb, with weight 3500 lb, with at most sfc 1.5 )
			// ).
			 
			 MissionX is a Mission, requires (an Aircraft with speed 1.0 mach, with altitude 25000 ft, with part
			 	(an AircraftEngine with thrust 25000 lb, with weight 3500 lb, with sfc 1.5 )
			 ).
			 
			 F100 is a type of AircraftEngine.
			 CF6 is a type of AircraftEngine.
		'''.sadl

		'''
			 uri "http://sadl.org/Suitability.dialog" alias stbltdlg.
			 
			 import "http://sadl.org/Suitability.sadl".
			 
«««			 Which AircraftEngine is suitable for MissionX?
«««			 
«««			 Is F100 suitable for MissionX? 
«««			 Is a CF6 suitable when altitude is 25000 ft?
			 
			 Is an F100 suitable for (a Mission, requires (an Aircraft with speed 1.0 mach, with altitude 25000 ft, with part
			 	(an AircraftEngine with thrust 25000 lb, with weight 3500 lb, with sfc 1.5 )))?
			 
«««			 Is a CF6 suitable when speed is 1.0 mach and 
«««			 	altitude is 25000 ft and 
«««			 	thrust is 55000 lb and 
«««			 	weight is 3500 lb and 
«««			 	sfc is 1.5 ?
		'''.assertValidatesTo [ ontModel, issues, processor |
			assertNotNull(ontModel)
			assertTrue(issues.filter[severity === Severity.ERROR].empty)
			if (processor instanceof JenaBasedDialogModelProcessor) {
				val jbdp = (processor as JenaBasedDialogModelProcessor)
				val resource = jbdp.currentResource
				val acm = jbdp.getAnswerCurationManager(resource)
				val conversation = acm.conversation
				assertNotNull(conversation)
				assertNotNull(conversation.statements);
//				assertEquals(1, conversation.statements.size)
//				assertTrue(conversation.statements.get(0).statement instanceof WhatICompareContentsContent)
				val cc = conversation.statements.get(0).statement as CompareContent
				val rules = cc.comparisonRules
				for (r : rules) {
					println(r.toFullyQualifiedString)
				}
//				assertEquals(1, rules.size)
//				assertEquals(grd.get(0), rules.get(0).toFullyQualifiedString)
			}
		]
	}
	
	
	@Test
	def void testGetTranslatorInstance() {
		val cm = new ConfigurationManager
		val tr = cm.translator
		assertNotNull(tr)
	}
	
	def createCodeExtractionFile() {
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

	}
}

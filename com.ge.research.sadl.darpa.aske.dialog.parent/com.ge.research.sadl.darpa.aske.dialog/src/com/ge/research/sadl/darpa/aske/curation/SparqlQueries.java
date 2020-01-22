package com.ge.research.sadl.darpa.aske.curation;

public class SparqlQueries {
	// identity and order of returned variables matters!!! Changes must also be changed in AnswerCurationManager where query is used.
	public static final String INTERESTING_METHODS_DOING_COMPUTATION = 
			"select ?m ?b ?e ?s where {?m <rdf:type> <Method> . ?m <doesComputation> true . OPTIONAL {?m <beginsAt> ?b . ?m <endsAt> ?e . OPTIONAL{?m <serialization> ?s}} .\r\n" + 
			"		MINUS {\r\n" + 
			"			{?ref <codeBlock> ?m . ?ref <isImplicit> true}\r\n" + 
			"			UNION {?m <rdf:type> <ExternalMethod>} } }";

//	public static final String ALL_CODE_EXTRACTED_METHODS =
//			"select distinct ?m ?b ?e ?s where {?m <rdf:type> <Method> . OPTIONAL {?m <beginsAt> ?b . ?m <endsAt> ?e . OPTIONAL{?m <serialization> ?s}} .\r\n" + 
//			"		MINUS {\r\n" + 
//			"			{?ref <codeBlock> ?m . ?ref <isImplicit> true}\r\n" + 
//			"			UNION {?m <rdf:type> <ExternalMethod>} } }";

	public static final String ALL_CODE_EXTRACTED_METHODS =
			"select distinct ?m ?b ?e ?s where {?m <rdf:type> <Method> . OPTIONAL {?m <beginsAt> ?b . ?m <endsAt> ?e . OPTIONAL{?m <serialization> ?s}} .\r\n" + 
			"		MINUS {?m <rdf:type> <ExternalMethod>} }";

	public static final String All_TEXT_EXTRACTED_METHODS = 
			"select distinct ?m ?pm ?ts ?ps ?ptfs where {?m <rdf:type> <ExternalEquation> . "
			+ "OPTIONAL {?m <http://sadl.org/sadlimplicitmodel#derivedFrom> ?pm} . "
			+ "OPTIONAL { ?m <expression> ?exp . ?exp <language> <Text> . ?exp <script> ?ts} . "
			+ "OPTIONAL {?m <expression> ?exp2 . ?exp2 <language> <Python> . ?exp2 <script> ?ps} . "
			+ "OPTIONAL {?m <expression> ?exp3 . ?exp3 <language> <Python-TF> . ?exp3 <script> ?ptfs}}";
	
	public static final String ALL_EXTERNAL_EQUATIONS = 
			"select ?eq ?lang ?expr where {?eq <rdf:type> <ExternalEquation> . ?eq <expression> ?script . ?script <script> ?expr . ?script <language> ?lang}";

	public static final String METHOD_IMPLICIT_INPUTS =
			"";
	
	public static final String METHOD_IMPLICIT_OUTPUTS =
			"";
}

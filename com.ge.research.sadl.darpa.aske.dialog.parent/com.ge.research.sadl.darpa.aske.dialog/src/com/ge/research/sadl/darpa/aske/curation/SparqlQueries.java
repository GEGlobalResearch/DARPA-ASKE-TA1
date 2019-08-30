package com.ge.research.sadl.darpa.aske.curation;

public class SparqlQueries {
	// identity and order of returned variables matters!!! Changes must also be changed in AnswerCurationManager where query is used.
	public static final String INTERESTING_METHODS_DOING_COMPUTATION = 
			"select ?m ?b ?e ?s where {?m <rdf:type> <Method> . ?m <doesComputation> true . OPTIONAL {?m <beginsAt> ?b . ?m <endsAt> ?e . OPTIONAL{?m <serialization> ?s}} .\r\n" + 
			"		MINUS {\r\n" + 
			"			{?ref <codeBlock> ?m . ?ref <isImplicit> true}\r\n" + 
			"			UNION {?m <rdf:type> <ExternalMethod>} } }";

}

package com.research.ge.darpa.aske.text.extraction;

import java.util.List;

import org.junit.Test;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.research.ge.darpa.aske.util.ConceptTagging;

import junit.framework.TestCase;

public class ConceptTaggingTest extends TestCase {
	
	@Test
	public void testRunTagging() throws Exception {
		String ontologyFile = "..\\models-from-text-sadl-model\\OwlModels\\PhysicalQuantities.owl";
		String text = "For an object with a constant mass m, the second law states that the force F is the product of an object's mass and its acceleration a";
		ConceptTagging tagging = new ConceptTagging(ontologyFile);
		List<String> taggedConcepts = tagging.runConceptTagging(text);
		
		for(String concept : taggedConcepts) {
			System.out.println(concept);
		}
	}

}

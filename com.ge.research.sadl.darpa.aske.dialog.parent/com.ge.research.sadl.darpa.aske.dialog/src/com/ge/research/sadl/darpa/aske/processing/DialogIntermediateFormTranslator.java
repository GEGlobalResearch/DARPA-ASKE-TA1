package com.ge.research.sadl.darpa.aske.processing;

import java.util.List;

import com.ge.research.sadl.jena.IntermediateFormTranslator;
import com.ge.research.sadl.jena.JenaBasedSadlModelProcessor;
import com.ge.research.sadl.model.gp.GraphPatternElement;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.InvalidTypeException;
import com.ge.research.sadl.reasoner.TranslationException;
import com.hp.hpl.jena.ontology.OntModel;

public class DialogIntermediateFormTranslator extends IntermediateFormTranslator {

	public DialogIntermediateFormTranslator(JenaBasedSadlModelProcessor processor, OntModel ontModel) {
		super(processor, ontModel);
	}
	
	public void addImpliedAndExpandedProperties(List<GraphPatternElement> fgpes) throws InvalidNameException, InvalidTypeException, TranslationException {
		super.addImpliedAndExpandedProperties(fgpes);
	}

	public GraphPatternElement addImpliedAndExpandedProperties(GraphPatternElement gpe) throws InvalidNameException, InvalidTypeException, TranslationException {
		return super.addImpliedAndExpandedProperties(gpe);
	}
}

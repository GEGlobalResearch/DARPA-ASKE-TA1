package com.ge.research.sadl.darpa.aske.processing;

import java.util.List;

import com.ge.research.sadl.jena.IntermediateFormTranslator;
import com.ge.research.sadl.jena.JenaBasedSadlModelProcessor;
import com.ge.research.sadl.model.gp.BuiltinElement;
import com.ge.research.sadl.model.gp.GraphPatternElement;
import com.ge.research.sadl.model.gp.Junction;
import com.ge.research.sadl.model.gp.Node;
import com.ge.research.sadl.model.gp.ProxyNode;
import com.ge.research.sadl.model.gp.TripleElement;
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

	/**
	 * Method to create a new, independent copy of the Node if it is a ProxyNode
	 * @param node
	 * @return
	 * @throws InvalidTypeException 
	 */
	public Node newCopyOfProxyNode(Node node) throws InvalidTypeException {
		if (node instanceof ProxyNode) {
			try {
				Object clone = ((ProxyNode)node).clone();
				if (clone instanceof Node) {
					return (Node)clone;
				}
			} catch (CloneNotSupportedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		return node;
	}
}

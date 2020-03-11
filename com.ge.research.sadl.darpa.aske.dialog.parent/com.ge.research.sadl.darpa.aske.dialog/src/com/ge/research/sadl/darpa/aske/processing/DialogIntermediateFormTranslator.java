package com.ge.research.sadl.darpa.aske.processing;

import java.util.Arrays;
import java.util.List;

import com.ge.research.sadl.jena.IntermediateFormTranslator;
import com.ge.research.sadl.jena.JenaBasedSadlModelProcessor;
import com.ge.research.sadl.model.gp.BuiltinElement;
import com.ge.research.sadl.model.gp.GraphPatternElement;
import com.ge.research.sadl.model.gp.Junction;
import com.ge.research.sadl.model.gp.NamedNode;
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

	public String intermediateFormToSadlExpression(BuiltinElement be) throws TranslationException {
		List<String> supportedOps = Arrays.asList("*", "/", "+", "-", "==", "!=", "<", "<=", ">", ">=");
		String op = be.getFuncName();
		if (supportedOps.contains(op)) {
			StringBuilder sb = new StringBuilder();
			if (be.getArguments().size() < 2) {
				throw new TranslationException("BuiltinElement argument can't be translated to SADL syntax as it doesn't have the expected number of arguments");
			}
			Node arg1 = be.getArguments().get(0);
			Node arg2 = be.getArguments().get(1);
			if (!(arg1 instanceof NamedNode) || !(arg2 instanceof NamedNode)) {
				throw new TranslationException("Unexpected BuiltinElement argument can't be translated to SADL syntax");
			}
			if (arg1 instanceof NamedNode) {
				sb.append(arg1.getName());
			}
			sb.append(op);
			sb.append(arg2.getName());
			return sb.toString();
		}
		throw new TranslationException("Unable to translate BuiltinElement to SADL syntax. Unsupported operator '" + op + "'");
	}
}

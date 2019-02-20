package com.ge.research.sadl.darpa.aske.inference;

import org.eclipse.emf.ecore.resource.Resource;

import com.ge.research.sadl.jena.JenaBasedSadlInferenceProcessor;
import com.ge.research.sadl.model.gp.Node;
import com.ge.research.sadl.model.gp.TripleElement;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.processing.SadlInferenceException;

public class JenaBasedDialogInferenceProcessor extends JenaBasedSadlInferenceProcessor {

	@Override
	public Object[] insertTriplesAndQuery(Resource resource, TripleElement[] triples) throws SadlInferenceException {
		setCurrentResource(resource);
		setModelFolderPath(getModelFolderPath(resource));
		setModelName(OntModelProvider.getModelName(resource));
		setTheJenaModel(OntModelProvider.find(resource));
		
		if (commonSubject(triples)) {
			return super.insertTriplesAndQuery(resource, triples);
		}
		// TODO Alfredo
		
		return null;
	}

	private boolean commonSubject(TripleElement[] triples) {
		Node lastSubject = null;
		for (TripleElement tr : triples) {
			Node thisSubject = tr.getSubject();
			if (lastSubject != null && !lastSubject.equals(thisSubject)) {
				return false;
			}
			lastSubject = thisSubject;
		}
		return true;
	}
}

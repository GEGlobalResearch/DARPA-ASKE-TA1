package com.ge.research.sadl.darpa.aske.inference;

import java.util.Iterator;
import java.util.ServiceLoader;

import org.eclipse.emf.ecore.resource.Resource;

import com.ge.research.sadl.jena.JenaBasedSadlInferenceProcessor;
import com.ge.research.sadl.model.gp.Node;
import com.ge.research.sadl.model.gp.TripleElement;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.processing.SadlInferenceException;
import com.hp.hpl.jena.reasoner.rulesys.Builtin;

public class JenaBasedDialogInferenceProcessor extends JenaBasedSadlInferenceProcessor {

	@Override
	public Object[] insertTriplesAndQuery(Resource resource, TripleElement[] triples) throws SadlInferenceException {
		setCurrentResource(resource);
		setModelFolderPath(getModelFolderPath(resource));
		setModelName(OntModelProvider.getModelName(resource));
		setTheJenaModel(OntModelProvider.find(resource));
		
		System.out.println(" >> Builtin classes discovered by the service loader:");
		Iterator<Builtin> itr = ServiceLoader.load(Builtin.class).iterator();
		while (itr.hasNext()) {
			Builtin service = itr.next();
			System.out.println(service.getClass().getCanonicalName());
		}
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

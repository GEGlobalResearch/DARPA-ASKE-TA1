package com.ge.research.sadl.darpa.aske.processing;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.IFileSystemAccess2;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.validation.CheckMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.jena.JenaBasedSadlModelProcessor;
import com.ge.research.sadl.processing.ValidationAcceptor;
import com.ge.research.sadl.reasoner.InvalidTypeException;
import com.ge.research.sadl.sADL.SadlModel;
import com.ge.research.sadl.sADL.SadlModelElement;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class JenaBasedDialogModelProcessor extends JenaBasedSadlModelProcessor {
	private static final Logger logger = LoggerFactory.getLogger(JenaBasedDialogModelProcessor.class);

	@Override
	public void onValidate(Resource resource, ValidationAcceptor issueAcceptor, CheckMode mode, ProcessorContext context) {
		SadlModel model = null; 
		if (!isSupported(resource)) {
			return;
		}
		resetProcessor();
		logger.debug("onValidate called for Resource '" + resource.getURI() + "'"); 
		CancelIndicator cancelIndicator = context.getCancelIndicator();
		setIssueAcceptor(issueAcceptor);
		setCancelIndicator(cancelIndicator);
		setProcessorContext(context);
		if (resource.getContents().size() < 1) {
			return;
		}
		model = (SadlModel) resource.getContents().get(0);
		theJenaModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		setCurrentResource(resource);

	
		// process rest of parse tree
		List<SadlModelElement> elements = model.getElements();
		if (elements != null) {
			Iterator<SadlModelElement> elitr = elements.iterator();
			while (elitr.hasNext()) {
				// check for cancelation from time to time
				if (cancelIndicator.isCanceled()) {
					throw new OperationCanceledException();
				}
				SadlModelElement element = elitr.next();
				// reset state for a new model element
				try {
					resetProcessorState(element);
				} catch (InvalidTypeException e) {
					// TODO Auto-generated catch block
					logger.error("Error:", e);
				}
				processModelElement(element);
			}
		}
}

	@Override
	public void onGenerate(Resource resource, IFileSystemAccess2 fsa, ProcessorContext context) {
		if (!resource.getURI().toString().endsWith(".dialog")) {
			return;
		}
	}

	private void resetProcessor() {
		// TODO Auto-generated method stub
		
	}
}

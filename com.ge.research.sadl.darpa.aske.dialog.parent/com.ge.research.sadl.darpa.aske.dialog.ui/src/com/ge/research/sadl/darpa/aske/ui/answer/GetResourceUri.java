package com.ge.research.sadl.darpa.aske.ui.answer;

import org.eclipse.emf.common.util.URI;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;

/**
 * Singleton for getting the URI from the Xtext document in a unit of work.
 */
public enum GetResourceUri implements IUnitOfWork<URI, XtextResource> {

	INSTANCE;

	@Override
	public URI exec(XtextResource resource) throws Exception {
		return resource.getURI();
	}

}

package com.ge.research.sadl.darpa.aske.ui.handler;

import java.io.File;
import java.nio.file.Path;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.util.ResourceUtil;

import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.model.gp.Query;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.ui.handlers.RunQuery;
import com.ge.research.sadl.utils.ResourceManager;
import com.google.common.base.Supplier;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class RunDialogQuery extends RunQuery {

	@Inject
	protected Provider<DialogRunInferenceHandler> infHandlerProvider;
	
	public Object execute(Provider<DialogRunInferenceHandler> backupHandlerProvider, Resource rsrc, Query lastcmd) throws ExecutionException {
		URI uri = rsrc.getURI();
		Path trgtpath;
		if (uri.isFile()) {
			trgtpath = new File(rsrc.getURI().toFileString()).toPath();
		}
		else {
			IFile trgtfile = ResourceUtil.getFile(rsrc);
			trgtpath = trgtfile.getLocation().toFile().toPath();
		}
		try {
			URI absuri = URI.createURI(trgtpath.toUri().toString());
			String modelFolderUri = ResourceManager.findModelFolderPath(absuri);
//			final String format = ConfigurationManager.RDF_XML_ABBREV_FORMAT;
//			IConfigurationManagerForIDE configMgr = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(modelFolderUri, format);
			final XtextResource resource = (XtextResource) rsrc;
			Supplier<XtextResource> resourceSupplier = () -> resource;
//			final IProject currentProject = null; //project;
//			final IFile targetFile = null; //trgtFile;
			if (infHandlerProvider == null) {
				infHandlerProvider = backupHandlerProvider;
			}
//			SadlRunQueryHandler delegate = handlerProvider.get();
			DialogRunInferenceHandler infDelegate = infHandlerProvider.get();
//			delegate.setGraphVisualizerHandler(new SadlEclipseGraphVisualizerHandler(RunDialogQuery.this, currentProject, targetFile));
			String owlPathStr = modelFolderUri + "/" + uri.lastSegment() + ".owl";
			Path owlPath = new File(owlPathStr).toPath();
//			delegate.run(owlPath, resourceSupplier, lastcmd.getSparqlQueryString());
			infDelegate.run(owlPath, resourceSupplier);
			Object retval = OntModelProvider.getPrivateKeyValuePair(resource, "CMResult");
			OntModelProvider.clearPrivateKeyValuePair(resource, "CMResult");
			return retval;
		}
		catch (Exception e) {
			console.error(e.getMessage() + "\n");
		}
		finally {

		}

		return null;
	}

	@Override
	public String getQuery(IConfigurationManagerForIDE configMgr) throws ConfigurationException {
		return super.getQuery(configMgr);
	}
}

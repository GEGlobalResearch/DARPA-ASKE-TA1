/********************************************************************** 
 * Note: This license has also been called the "New BSD License" or 
 * "Modified BSD License". See also the 2-clause BSD License.
 *
 * Copyright © 2018-2019 - General Electric Company, All Rights Reserved
 * 
 * Projects: ANSWER and KApEESH, developed with the support of the Defense 
 * Advanced Research Projects Agency (DARPA) under Agreement  No.  
 * HR00111990006 and Agreement No. HR00111990007, respectively. 
 *
 * Redistribution and use in source and binary forms, with or without 
 * modification, are permitted provided that the following conditions are met:
 * 1. Redistributions of source code must retain the above copyright notice, 
 *    this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, 
 *    this list of conditions and the following disclaimer in the documentation 
 *    and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its 
 *    contributors may be used to endorse or promote products derived 
 *    from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" 
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE 
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE 
 * ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE 
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR 
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF 
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS 
 * INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN 
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) 
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF 
 * THE POSSIBILITY OF SUCH DAMAGE.
 *
 ***********************************************************************/
package com.ge.research.sadl.darpa.aske.ui.handler;

import java.io.File;
import java.nio.file.Path;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;

import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.ui.handlers.RunQuery;
import com.google.common.base.Supplier;
import com.google.inject.Inject;
import com.google.inject.Provider;

public class RunDialogQuery extends RunQuery {

	@Inject
	protected Provider<DialogRunInferenceHandler> infHandlerProvider;
	
	public Object execute(Provider<DialogRunInferenceHandler> backupHandlerProvider, Resource rsrc, Object lastcmd) throws ExecutionException {
		URI uri = rsrc.getURI();
		try {
			String modelFolderUri = getModelFolderFromResource(rsrc);
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

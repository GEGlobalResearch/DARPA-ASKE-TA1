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

import java.io.InvalidClassException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.validation.Issue;

import com.ge.research.sadl.builder.MessageManager.SadlMessage;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.ide.handlers.SadlRunInferenceHandler;
import com.ge.research.sadl.model.gp.Query;
import com.ge.research.sadl.model.gp.TripleElement;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.reasoner.SadlCommandResult;
import com.google.common.base.Supplier;
import com.google.common.base.Throwables;

/**
 * IDE agnostic handler for running the inferencer.
 *
 * <b>Note:</b> I have copied the logic from the {@code RunInference} class and tried to keep it as it was.
 *
 * @author akos.kitta
 */
public class DialogRunInferenceHandler extends SadlRunInferenceHandler {

	@Override
	public void run(Path path, Supplier<XtextResource> resourceSupplier, Map<String, String> properties) {
		try {
			// *** NOTE: Copied from com.ge.research.sadl.ui.handlers.RunInference.execute(ExecutionEvent) *** 
			if (path.getFileName().toString().endsWith(".dialog.owl")) {
				// run inference on this model
				console.info("Inference of '" + path.toAbsolutePath().toString() + "' requested.\n");
				final List<Issue> issues = new ArrayList<Issue>();
				final List<SadlMessage> results = null;
				String modelFolderPath = getOwlModelsFolderPath(path).toString();
				String owlModelPath = modelFolderPath + "/" + path.getFileName().toString().replaceFirst("[.][^.]+$", ".owl");
				Resource res = prepareResource(resourceSupplier.get());
				Object lastCmd = OntModelProvider.getPrivateKeyValuePair(res, DialogConstants.LAST_DIALOG_COMMAND);
				if (lastCmd instanceof Query) {
					SadlCommandResult retval = inferenceProcessor.processAdhocQuery(res, (Query)lastCmd);
					Object[] retvals = new Object[1];
					retvals[0] = retval;
					displayInferenceResults(retvals, path, owlModelPath, modelFolderPath, properties);
					OntModelProvider.addPrivateKeyValuePair(res, "CMResult", retval);
				}
				else if (lastCmd instanceof TripleElement[]) {
					Object[] rss = inferenceProcessor.insertTriplesAndQuery(res, (TripleElement[]) lastCmd);
//
//            		String desc = "Computational Graph";
//            		String baseFileName = OntModelProvider.getModelName(res); //path.getFileName().toString() + System.currentTimeMillis();
//            		//String path = resource.getURI().toFileString();
//            		baseFileName = path.getFileName().toString() + System.currentTimeMillis() ;
//            		baseFileName = baseFileName.replace(".", "_");
//            		resultSetToGraph(path, (ResultSet)rss[0], desc, baseFileName, null, properties);
//            		
					OntModelProvider.addPrivateKeyValuePair(res, "CMResult", rss);
				}
				else if (lastCmd != null) {
					throw new InvalidClassException("Unhandled last command type: " + lastCmd.getClass().getCanonicalName());
				}
			}
		} catch (Exception e) {
			console.error(Throwables.getStackTraceAsString(e));
		}
	}

}

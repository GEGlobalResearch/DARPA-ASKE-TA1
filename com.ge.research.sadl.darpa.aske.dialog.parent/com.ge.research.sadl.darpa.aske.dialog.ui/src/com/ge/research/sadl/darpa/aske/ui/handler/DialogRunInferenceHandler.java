/************************************************************************
 * Copyright 2007-2018 General Electric Company, All Rights Reserved
 * 
 * Project: SADL
 * 
 * Description: The Semantic Application Design Language (SADL) is a
 * language for building semantic models and expressing rules that
 * capture additional domain knowledge. The SADL-IDE (integrated
 * development environment) is a set of Eclipse plug-ins that
 * support the editing and testing of semantic models using the
 * SADL language.
 * 
 * This software is distributed "AS-IS" without ANY WARRANTIES
 * and licensed under the Eclipse Public License - v 1.0
 * which is available at http://www.eclipse.org/org/documents/epl-v10.php
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
import com.ge.research.sadl.darpa.aske.processing.JenaBasedDialogModelProcessor;
import com.ge.research.sadl.ide.handlers.SadlRunInferenceHandler;
import com.ge.research.sadl.model.gp.Query;
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
				Object lastCmd = OntModelProvider.getPrivateKeyValuePair(res, JenaBasedDialogModelProcessor.LAST_DIALOG_COMMAND);
				if (lastCmd instanceof Query) {
					SadlCommandResult retval = inferenceProcessor.processAdhocQuery(res, (Query)lastCmd);
					Object[] retvals = new Object[1];
					retvals[0] = retval;
					displayInferenceResults(retvals, path, owlModelPath, modelFolderPath, properties);
					OntModelProvider.addPrivateKeyValuePair(res, "CMResult", retval);
				}
				else {
					throw new InvalidClassException("Unhandled last command type: " + lastCmd.getClass().getCanonicalName());
				}
			}
		} catch (Exception e) {
			console.error(Throwables.getStackTraceAsString(e));
		}
	}

}

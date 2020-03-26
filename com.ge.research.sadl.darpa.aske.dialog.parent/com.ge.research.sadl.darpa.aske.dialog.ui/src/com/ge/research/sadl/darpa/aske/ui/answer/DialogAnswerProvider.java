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
package com.ge.research.sadl.darpa.aske.ui.answer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Cursor;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.preferences.IPreferenceValues;
import org.eclipse.xtext.preferences.IPreferenceValuesProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.IXtextModelListener;
import org.eclipse.xtext.ui.editor.model.XtextDocument;
import org.eclipse.xtext.util.CancelIndicator;
import org.eclipse.xtext.util.Exceptions;
import org.eclipse.xtext.validation.CheckMode;
import org.eclipse.xtext.validation.IResourceValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.builder.ConfigurationManagerForIDE;
import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.curation.BaseDialogAnswerProvider;
import com.ge.research.sadl.darpa.aske.curation.EquationNotFoundException;
import com.ge.research.sadl.darpa.aske.dialog.ExtractStatement;
import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.ExpectsAnswerContent;
import com.ge.research.sadl.darpa.aske.processing.JenaBasedDialogModelProcessor;
import com.ge.research.sadl.darpa.aske.processing.ModelElementInfo;
import com.ge.research.sadl.darpa.aske.processing.QuestionWithCallbackContent;
import com.ge.research.sadl.darpa.aske.processing.StatementContent;
import com.ge.research.sadl.darpa.aske.processing.imports.AnswerExtractionException;
import com.ge.research.sadl.darpa.aske.ui.editor.DialogEditors;
import com.ge.research.sadl.darpa.aske.ui.editor.DialogEditors.Options;
import com.ge.research.sadl.model.visualizer.IGraphVisualizer;
import com.ge.research.sadl.processing.SadlInferenceException;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.TranslationException;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.ge.research.sadl.sADL.SadlImport;
import com.ge.research.sadl.sADL.SadlModel;
import com.ge.research.sadl.ui.handlers.SadlActionHandler;
import com.google.common.base.Preconditions;
import com.hp.hpl.jena.ontology.OntModel;

public class DialogAnswerProvider extends BaseDialogAnswerProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(DialogAnswerProvider.class);

	private IXtextDocument document;
	private IConfigurationManagerForIDE configManager;
	private IXtextModelListener modelListener;
	private URI uri;
	
//	private int cumulativeOffset = 0;

	public void configure(IXtextDocument document) {
		Preconditions.checkState(this.document == null, "Already initialized.");
		this.document = document;
		this.document.readOnly(resource -> {
			// The `OwlModels` folder might not exist this point.
			// Instead of creating the expected folder structure, we delay the configuration
			// manager initialization and the dialog answer provider registration.
			// See: https://github.com/GEGlobalResearch/DARPA-ASKE-TA1/issues/37
			if (SadlActionHandler.getModelFolderFromResource(resource) != null) {
				doConfigure(resource);
			} else {
				modelListener = r -> {
					if (SadlActionHandler.getModelFolderFromResource(r) != null) {
						doConfigure(r);
						DialogAnswerProvider.this.document.removeModelListener(modelListener);
						modelListener = null;
					}
				};
				this.document.addModelListener(modelListener);
			}
			return null; // Void
		});
	}

	/**
	 * Does the initialization of the configuration and the answer curation manager.
	 * Expects an existing `OwlModels` folder. Otherwise, throws a runtime
	 * exception.
	 */
	protected void doConfigure(XtextResource resource) {
		uri = resource.getURI();
		try {
			LOGGER.debug("[DialogAnswerProvider] >>> Registering... [" + uri + "]");
			this.configManager = initializeConfigManager(resource);
			// If we validate here, we trigger the dialog model processor to register the answer curation manager.
			IResourceValidator validator = resource.getResourceServiceProvider().getResourceValidator();
			validator.validate(resource, CheckMode.ALL, CancelIndicator.NullImpl);
			LOGGER.debug("[DialogAnswerProvider] <<< Registered. [" + uri + "]");
		} catch (Exception e) {
			System.err.println("[DialogAnswerProvider] <<< Failed to register answer provider. [" + uri + "]");
			e.printStackTrace();
			throw new RuntimeException("Error occurred during the configuration for " + uri, e);
		}
	}

	public void dispose() {
		URI uri = document.readOnly(GetResourceUri.INSTANCE);
		LOGGER.debug("[DialogAnswerProvider] >>> Disposing... [" + uri + "]");
		super.dispose();
		if (modelListener != null) {
			this.document.removeModelListener(modelListener);
		}
//		if (answerConfigurationManager != null) {
//			answerConfigurationManager.clearQuestionsAndAnsers();
//		}
		if (configManager != null) {
			configManager.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, null);
			configManager.addPrivateKeyValuePair(DialogConstants.ANSWER_CURATION_MANAGER, null);
		}
		LOGGER.debug("[DialogAnswerProvider] >>> Disposed. [" + uri + "]");
	}

	/**
	 * Method to display graph
	 * 
	 * @param visualizer -- IGraphVisualizer instance that contains graphing info
	 * @return -- null if successful else an error message
	 */
	// XXX: called via reflection from the
	// com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.displayGraph(IGraphVisualizer)
	public String displayGraph(IGraphVisualizer visualizer) {
		String[] errorMsg = { null };
		String fileToOpen = visualizer.getGraphFileToOpen();
		if (fileToOpen != null) {
			File fto = new File(fileToOpen);
			if (fto.isFile()) {
				IFileStore fileStore = EFS.getLocalFileSystem().getStore(fto.toURI());
				new Thread(new Runnable() {
					public void run() {
						try {
							Thread.sleep(1000);
						} catch (Exception e) {
						}
						Display.getDefault().asyncExec(new Runnable() {
							public void run() {
								try {
									IWorkbench wb = PlatformUI.getWorkbench();
									IWorkbenchWindow awbw = wb.getActiveWorkbenchWindow();
									IWorkbenchPage page = null;
									if (awbw == null) {
										if (wb.getWorkbenchWindowCount() == 1) {
											page = wb.getWorkbenchWindows()[0].getActivePage();
										}
									} else {
										page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
									}
									if (page != null) {
										IDE.openEditorOnFileStore(page, fileStore);
									} else {
										errorMsg[0] = "Error trying to get active window";
										System.err.println(errorMsg);
									}
								} catch (Throwable t) {
									errorMsg[0] = "Error trying to display graph file '" + fileToOpen + "': "
											+ t.getMessage();
									System.err.println(errorMsg);
								}
							}
						});
					}
				}).start();
			} else if (fileToOpen != null) {
				errorMsg[0] = "Failed to open graph file '" + fileToOpen + "'. Try opening it manually.";
				System.err.println(errorMsg);
			}
		} else {
			errorMsg[0] = "Unable to find an instance of IGraphVisualizer to render graph for query.";
			System.err.println(errorMsg + "\n");
		}
		return errorMsg[0];
	}

	private IConfigurationManagerForIDE initializeConfigManager(Resource resource) throws ConfigurationException {
		String modelFolder = SadlActionHandler.getModelFolderFromResource(resource);
		ConfigurationManagerForIDE configManager = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(modelFolder, null);
		configManager.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, this);
		return configManager;
	}

	private String generateDoubleQuotedContentForDialog(String content) {
		boolean prependCM = true;
		if (content.startsWith("CM:")) {
			prependCM = false;
		}
		String modContent = null;
		if (!content.startsWith("\"") && !content.endsWith("\"")) {
			if (content.endsWith(".") || content.endsWith("?")) {
				String lastChar = content.substring(content.length() - 1);
				modContent = "\"" + content.substring(0, content.length() - 1).replaceAll("\"", "'") + "\"" + lastChar;
			} else {
				modContent = "\"" + content.replaceAll("\"", "'") + "\"" + ".";
			}
		} else {
			modContent = content;
		}
		if (prependCM) {
			modContent = "CM: " + modContent;
		}
		return modContent;
	}

	private IXtextDocument getTheDocument() {
		return document;
	}

	private void setTheDocument(XtextDocument theDocument) {
		this.document = theDocument;
	}

	private synchronized boolean addCurationManagerContentToDialog(IXtextDocument document, IRegion reg, String content,
			Object ctx, boolean quote) throws BadLocationException {
		return addCurationManagerContentToDialog(document, reg, content, ctx, quote, true, true, true);
	}
	
	private synchronized boolean addCurationManagerContentToDialog(IXtextDocument document, IRegion reg, String content,
			Object ctx, boolean quote, boolean prependAgent, boolean repositionCursor, boolean addLeadingSpaces) throws BadLocationException {
		LOGGER.debug(content);
//		System.err.println("addCMContent: " + content);
		Display.getDefault().asyncExec(() -> {
//		Display.getDefault().syncExec(() -> {
			try {
				String modContent = generateModifiedContent(document, ctx, quote, prependAgent, content);
				Object[] insertionInfo = generateInsertionLocation(document, ctx, modContent);
				modContent = (String) insertionInfo[0];
				int loc = (int) insertionInfo[1];
				if (loc >= 0) {
					document.replace(loc, 0, modContent);
					if (repositionCursor && document instanceof XtextDocument && ctx instanceof EObject) {
						final int caretOffset = loc + modContent.length();
						setCaretOffsetInEditor(uri, caretOffset);
					}
				} else {
					loc = document.getLength();
					document.set(document.get() + System.lineSeparator() + modContent);
					if (repositionCursor) {
						setCaretOffsetInEditor(uri, document.get().length() - 1);
					}
				}
			} catch (BadLocationException e) {
				// This happens sometimes but doesn't usually have dire consequences....
//				Exceptions.throwUncheckedException(e);
			}
		});
		if (ctx instanceof ExtractStatement) {
			try {
				String mf = SadlActionHandler.getModelFolderFromResource(((ExtractStatement) ctx).eResource());
				File mff = new File(mf);
				String prjname = mff.getParentFile().getName();
				IProject prj = ResourcesPlugin.getWorkspace().getRoot().getProject(prjname);
				prj.refreshLocal(IResource.DEPTH_INFINITE, null);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return true;
	}

	private synchronized boolean replaceDialogText(IXtextDocument theDocument, EObject ctx, String originalTxt, String replacementTxt) throws BadLocationException {
		LOGGER.debug("replacing '" + originalTxt + "' with '" + replacementTxt + "'");
		Display.getDefault().asyncExec(() -> {
//		Display.getDefault().syncExec(() -> {
			Object elementInfos = getConfigMgr().getPrivateKeyValuePair("ElementInfo");
			String docText = document.get();
			int docLength = document.getLength();
			int idx = 0;
			if (elementInfos instanceof List<?>) {
				int cumulativeOffset = 0;
				for (Object einfo : ((List<?>)elementInfos)) {
					if (einfo instanceof ModelElementInfo) {
						ModelElementInfo mei = (ModelElementInfo) einfo;
						if (mei.isInserted()) {
							cumulativeOffset += mei.getLength();
						}
						else if (mei.getObject().equals(ctx)) {
							String origTxt = mei.getTxt();
							if(origTxt.endsWith(".")) {
								origTxt = origTxt.substring(0,origTxt.length()-1);
							}
							if (!origTxt.trim().substring(4).equals(originalTxt)) {
								// error
								System.err.println("equation text doesn't match");
							}
							int len = mei.getLength();									// length of original element
							int currentStart = docText.indexOf(origTxt);				// start of element text in current document
							String currentTxt;
							try {
								currentTxt = document.get(currentStart, len);
								if(currentTxt.endsWith(".")) {
									currentTxt = currentTxt.substring(0,currentTxt.length()-1);
								}
								if (!currentTxt.trim().substring(4).equals(originalTxt)) {
									// error
									System.err.println("document text doesn't match");
								}
								int loc = currentStart + 4 + System.lineSeparator().length(); 	// this is for "/r/nCM: "
								document.replace(loc, originalTxt.length(), replacementTxt);
//								final int caretOffset = loc + modContent.length();
//								setCaretOffsetInEditor(uri, caretOffset);
								break;
							} catch (BadLocationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
				}
			}
		});
		return true;
	}

	private String checkForAdditionalNewlines(IXtextDocument document, String modContent, int loc) {
		String lineSep = System.lineSeparator();
		int lineSepLen = lineSep.length();
		int docLen = document.getLength();
		if (!modContent.startsWith(lineSep) && loc > lineSepLen) {
			try {
				String before = document.get(loc - lineSepLen, lineSepLen);
				if (!before.equals(lineSep)) {
					modContent = lineSep + modContent;
				}
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		if (!modContent.endsWith(lineSep)) {
			if (loc < docLen - lineSepLen) {
				try {
					String after = document.get(loc, lineSepLen);
					if (!after.equals(lineSep)) {
						modContent += lineSep;
					}
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				modContent += lineSep;
			}
		}
		return modContent;
	}

	private Object[] generateInsertionLocation(IXtextDocument document, Object ctx, String modContent) {
		int loc = 0;
		String lineSep = System.lineSeparator();
		int lineSepLen = lineSep.length();
		Object elementInfos = getConfigMgr().getPrivateKeyValuePair("ElementInfo");
		String docText = document.get();
		int docLength = document.getLength();
		int idx = 0;
		if (elementInfos instanceof List<?>) {
			int cumulativeOffset = 0;
			for (Object einfo : ((List<?>)elementInfos)) {
				if (einfo instanceof ModelElementInfo) {
					ModelElementInfo mei = (ModelElementInfo) einfo;
					if (mei.isInserted()) {
						cumulativeOffset += mei.getLength();
					}
					else if (mei.getObject().equals(ctx)) {
						try {
							String origTxt = mei.getTxt();
							int len = mei.getLength();									// length of original element
							int currentStart = docText.indexOf(origTxt);				// start of element text in current document
							if (currentStart >= 0) {
								String currentTxt = document.get(currentStart, len);
								if (!currentTxt.equals(origTxt)) {
									System.err.println("Error in Dialog text");
									System.err.println("  currentTxt: " + currentTxt);
									System.err.println("  origTxt: " + origTxt);
								}
							}
							else {
								currentStart = 0;
							}
							int currentEndLoc = currentStart+ origTxt.length();			// end of element text in current document
							int expectedEndLoc = mei.getEnd() + cumulativeOffset;		// expected end of element text in current document
							if (currentEndLoc != expectedEndLoc) {
								System.err.println("currentLoc=" + currentEndLoc + ", expectedLoc=" + expectedEndLoc);
							}
							else {
								// this is the original object after which the insertion is to occur
								// but there could be other insertions before this, so roll forward through
								// any follow-on insertions to get the actual point of insertion
								int priorInsertionsOffset = 0;
								for (int insertionIdx = idx + 1; insertionIdx < ((List<?>)elementInfos).size(); insertionIdx++) {
									ModelElementInfo nextMei = ((List<ModelElementInfo>)elementInfos).get(insertionIdx);
									if (nextMei.isInserted()) {
										priorInsertionsOffset += nextMei.getLength();
									}
									else {
										break;
									}
								}
								loc = expectedEndLoc + priorInsertionsOffset;
								if (docLength - loc >= lineSepLen) {
									String rightAfter = document.get(loc,lineSepLen);
									if (rightAfter.equals(lineSep)) {
										loc += lineSep.length();
									}
								}
							}							
						} catch (BadLocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						break;
					}
				}
				idx++;
			}
		}
		if (loc == 0) {
			if (getResource() != null) {
				System.err.println("Context EObject not found in list of ModelElementInfos for document '" + getResource().getURI() + "'!");
			}
			else {
				System.err.println("Context EObject not found in list of ModelElementInfos!");
			}
			loc = docLength;
		}
		// should there be a newline at the beginning or a newline at the end of modContent?
		modContent = checkForAdditionalNewlines(document, modContent, loc);

		ModelElementInfo newMei = new ModelElementInfo(null, modContent, loc, modContent.length(), loc+modContent.length(), true);
		if (idx + 1 < ((List<ModelElementInfo>)elementInfos).size()) {
			((List<ModelElementInfo>)elementInfos).add(idx + 1, newMei);
		}
		else {
			((List<ModelElementInfo>)elementInfos).add(newMei);
		}
		Object[] returnvals = new Object[2];
		returnvals[0] = modContent;
		returnvals[1] = loc;
		return returnvals;
	}

	private String generateModifiedContent(IXtextDocument document, Object ctx, boolean quote, boolean prependAgent, String content) {
		String response;
		if (quote) {
			response = generateDoubleQuotedContentForDialog(content);
		}
		else {
			response = content;
		}
		int numSpacesBeforeEachLine = 0;
		if (prependAgent) {
			if (!content.startsWith("CM:")) {
				String lines[] = response.split("\\r\\n\\.|\\n\\.|\\r\\.");
				if (lines.length > 1) {
					StringBuilder sb = new StringBuilder();
					for (String s : lines) {
						sb.append("CM: ");
						sb.append(s);
						sb.append(System.lineSeparator());
					}
					response = sb.toString();
				}
				else {
					response = "CM: " + response;
				}
				numSpacesBeforeEachLine += 4;
			}
		}
		if (!response.trim().endsWith(".") && !response.trim().endsWith("?") && !response.trim().endsWith(")")) {
			response += ".";
		}
		if (numSpacesBeforeEachLine > 0) {
			String lines[] = response.split("\\r\\n|\\n|\\r");
			StringBuilder sb = new StringBuilder(lines[0]);
			sb.append(System.lineSeparator());
			for (int i = 1; i < lines.length; i++) {
				for (int j = 0; j < numSpacesBeforeEachLine; j++) {
					sb.append(" ");
				}
				sb.append(lines[i]);
				if (i < lines.length - 1) {
					sb.append(System.lineSeparator());
				}
			}
			response = sb.toString();
		}
		return response;
	}

	@Override
	public boolean addImports(List<String> importStatements) {
		if (importStatements == null || importStatements.size() == 0) {
			return false;
		}

		Resource rsrc = getResource();
		EObject precedingObj = null;
		if (rsrc instanceof XtextResource) {
			SadlModel model = (SadlModel) rsrc.getContents().get(0);
			if (!model.getImports().isEmpty()) {
				SadlImport lastImport = null;
				EList<SadlImport> importlst = model.getImports();
				for (SadlImport imprt : importlst) {
					if (importStatements.contains(imprt.toString())) {	
						importStatements.remove(importStatements.indexOf(imprt.toString()));
					}
					lastImport = imprt;
				}
				// add after last import
				precedingObj = lastImport;
//				showEObjectTextInfo(precedingObj);
			}
			else {
				// add before elements
				precedingObj = model;
			}
			if (precedingObj != null) {
				StringBuilder sb = new StringBuilder();
				for (int i = 0; i < importStatements.size(); i++) {
					String importStatement = importStatements.get(i);
					LOGGER.debug(importStatement);
					if (i > 0) {
						sb.append(System.lineSeparator());
					}
					sb.append(importStatement);
				}
//				String precedingObjText = NodeModelUtils.getTokenText(NodeModelUtils.getNode(precedingObj));
				try {
					return addCurationManagerContentToDialog(document, null, sb.toString(), precedingObj, false, false, false, false);
				} catch (BadLocationException e) {
					// This happens sometimes but doesn't usually have dire consequences....
//					e.printStackTrace();
				}
			}
			return true;
		}
		return false;
	}

	private void setCaretOffsetInEditor(URI uri, int caretOffset) {
		Options options = new DialogEditors.Options(uri);
		options.setActivate(true);
		DialogEditors.asyncExec(options, editor -> {
			if (editor instanceof IAdaptable) {
				Control control = (Control) editor.getAdapter(Control.class);
				if (control instanceof StyledText) {
					((StyledText) control).setCaretOffset(caretOffset); // Sets the cursor.
					((StyledText) control).setSelection(caretOffset); // Scrolls in the editor, if required.
					control.redraw();
				}
			}
		});
	}

	@Override
	public String addCurationManagerInitiatedContent(AnswerCurationManager answerCurationManager, StatementContent sc) {
		try {
			addCurationManagerContentToDialog(getTheDocument(), null, sc.getText(), sc.getHostEObject(), false);
		} catch (BadLocationException e) {
			// This happens sometimes but doesn't usually have dire consequences....
//			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean addCurationManagerAnswerContent(AnswerCurationManager acm, String content, Object ctx) {
		answerConfigurationManager = acm;
		try {
			return addCurationManagerContentToDialog(document, null, content, ctx, false);
		} catch (BadLocationException e) {
			// This happens sometimes but doesn't usually have dire consequences....
//			e.printStackTrace();
		}
		return false;
	}

	@Override
	public String initiateMixedInitiativeInteraction(QuestionWithCallbackContent element) {
		String content = element.getTheQuestion();
		if (getTheDocument() != null) {
			try {
				boolean quote = isContentQuoted(content);
				addCurationManagerContentToDialog(document, null, content, null, quote);
			} catch (BadLocationException e) {
				// This happens sometimes but doesn't usually have dire consequences....
//				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public String replaceDialogText(AnswerCurationManager answerCurationManager, EObject eObject, String originalTxt, String replacementTxt) {
		try {
			replaceDialogText(getTheDocument(), eObject, originalTxt, replacementTxt);
		} catch (BadLocationException e) {
			// This happens sometimes but doesn't usually have dire consequences....
//			e.printStackTrace();
		}
		return null;
	}
	private boolean isContentQuoted(String content) {
		content = content.trim();
		if (content.startsWith("\"") && content.endsWith("\"")) {
			return true;
		}
		return false;
	}

	@Override
	public void provideResponse(QuestionWithCallbackContent question) {
//		if (response.getCurationManager() != null) {
		AnswerCurationManager acm = getAnswerConfigurationManager();
		String methodToCall = question.getMethodToCall();
		Method[] methods = acm.getClass().getMethods();
		for (Method m : methods) {
			if (m.getName().equals(methodToCall)) {
				// call the method
				List<Object> args = question.getArguments();
				try {
					Object results = null;
					if (args.size() == 0) {
						results = m.invoke(acm);
					} else {
						Object arg0 = args.get(0);
						if (args.size() == 1) {
							results = m.invoke(acm, arg0);
						} else {
							Object arg1 = args.get(1);
							if (args.size() == 2) {
								results = m.invoke(acm, arg0, arg1);
							}
							if (methodToCall.equals("saveAsSadlFile")) {
								if (arg0 instanceof List<?> && results instanceof List<?>) {
									String projectName = null;
									List<File> sadlFiles = new ArrayList<File>();
									for (int i = 0; i < ((List<?>) results).size(); i++) {
										Object result = ((List<?>) results).get(i);
										File owlfile = (File) ((List<?>) arg0).get(i);
										if (AnswerCurationManager.isYes(arg1)) {
											File sf = new File(result.toString());
											if (sf.exists()) {
												sadlFiles.add(sf);
												// delete OWL file so it won't be indexed
												if (owlfile.exists()) {
													owlfile.delete();
													try {
														String outputOwlFileName = owlfile.getCanonicalPath();
														String altUrl = new SadlUtils()
																.fileNameToFileUrl(outputOwlFileName);

														String publicUri = acm.getConfigurationManager()
																.getPublicUriFromActualUrl(altUrl);
														if (publicUri != null) {
															acm.getConfigurationManager()
																	.deleteModel(publicUri);
															acm.getConfigurationManager()
																	.deleteMapping(altUrl, publicUri);
														}
													} catch (Exception e) {
														e.printStackTrace();
													}
												}
												projectName = sf.getParentFile().getParentFile().getName();
											}
										} else {
											// add import of OWL file from policy file since there won't be a SADL
											// file to build an OWL and create mappings.
											try {
												String importActualUrl = new SadlUtils()
														.fileNameToFileUrl(arg0.toString());
												String altUrl = new SadlUtils().fileNameToFileUrl(importActualUrl);
												String importPublicUri = acm.getConfigurationManager()
														.getPublicUriFromActualUrl(altUrl);
												String prefix = acm.getConfigurationManager()
														.getGlobalPrefix(importPublicUri);
												acm.getConfigurationManager().addMapping(importActualUrl,
														importPublicUri, prefix, false, "AnswerCurationManager");
												String prjname = owlfile.getParentFile().getParentFile().getName();
												IProject prj = ResourcesPlugin.getWorkspace().getRoot()
														.getProject(prjname);
												prj.refreshLocal(IResource.DEPTH_INFINITE, null);
											} catch (Exception e) {
												e.printStackTrace();
												e.printStackTrace();
											}
										}
									}
									if (projectName != null) { // only not null if doing SADL conversion
										IProject project = ResourcesPlugin.getWorkspace().getRoot()
												.getProject(projectName);
										try {
											project.build(IncrementalProjectBuilder.AUTO_BUILD, null);
											// display new SADL file
											displayFiles(configManager.getModelFolder(), sadlFiles);
										} catch (Exception e) {
											e.printStackTrace();
										}
									}
								}
							}
						}
					}
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
//		}	
	}

	@Override
	public void updateProjectAndDisplaySadlFiles(String projectName, String modelsFolder, List<File> sadlFiles) {
		String prjname = projectName;
		IProject prj = ResourcesPlugin.getPlugin().getWorkspace().getRoot().getProject(prjname);
		try {
			prj.refreshLocal(IResource.DEPTH_INFINITE, null);
		} catch (CoreException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		if (projectName != null) { // only not null if doing SADL conversion
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			try {
				project.build(IncrementalProjectBuilder.AUTO_BUILD, null);
				// display new SADL file
				displayFiles(getConfigMgr().getModelFolder(), sadlFiles);
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	@Override
	protected IConfigurationManagerForIDE getConfigMgr() {
		return configManager;
	}

	@Override
	protected void displayFiles(String codeModelFolder, List<File> sadlFiles) {
		File cmf = new File(codeModelFolder);
		IProject codeModelProject = null;
		if (cmf.exists()) {
			File prjf = cmf.getParentFile();
			codeModelProject = ResourcesPlugin.getWorkspace().getRoot().getProject(prjf.getName());
		}
		if (codeModelProject != null) {
			try {
				// open files to be imported in the code model project
				IProject project = codeModelProject;
				if (!project.isOpen())
					project.open(null);
				try {
					List<IFile> iFiles = new ArrayList<IFile>();
					List<IPath> locations = new ArrayList<IPath>();
					for (File sf : sadlFiles) {
						String prjname = project.getFullPath().lastSegment();
						IPath location = new Path(sf.getCanonicalPath());
						locations.add(location);
						String[] segarray = location.segments();
						int segsAdded = 0;
						boolean prjFound = false;
						StringBuilder sb = new StringBuilder();
						for (String seg : segarray) {
							if (prjFound) {
								if (segsAdded++ > 0) {
									sb.append("/");
								}
								sb.append(seg);
							}
							if (!prjFound && seg.equals(prjname)) {
								prjFound = true;
								;
							}
						}
						IFile file = project.getFile(sb.toString());
						iFiles.add(file);
					}
					project.refreshLocal(IResource.DEPTH_INFINITE, null);
					for (int i = 0; i < iFiles.size(); i++) {
						IFile file = iFiles.get(i);
						IPath location = locations.get(i);
						if (!file.exists()) {
							file.createLink(location, IResource.NONE, null);
						}
					}
					IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
					if (window != null) {
						IWorkbenchPage page = window.getActivePage();
						if (page != null) {
							for (IPath location : locations) {
								try {
									IWorkbenchPart actpart = page.getActivePart();
									IFileStore fileStore = EFS.getLocalFileSystem().getStore(location);
									IDE.openEditorOnFileStore(page, fileStore);
									page.bringToTop(actpart);
								} catch (PartInitException e) {
									System.err.println("Unable to open '" + location.lastSegment() + "' in an editor.");
								}
							}
						}
					} else {
						PlatformUI.getWorkbench().getDisplay().asyncExec(new Runnable() {
							public void run() {
								IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
								if (window != null) {
									IWorkbenchPage page = window.getActivePage();
									if (page != null) {
										for (IPath location : locations) {
											try {
												IWorkbenchPart actpart = page.getActivePart();
												IFileStore fileStore = EFS.getLocalFileSystem().getStore(location);
												IDE.openEditorOnFileStore(page, fileStore);
												page.bringToTop(actpart);
											} catch (PartInitException e) {
												System.err.println("Unable to open '" + location.lastSegment()
														+ "' in an editor.");
											}
										}
									}
								}
							}
						});
					}
				} catch (IOException e2) {
					e2.printStackTrace();
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
	}

	public Resource getResource() {
		Preconditions.checkState(this.document != null, "Not initialized yet.");
		return this.document.readOnly(resource -> resource);
	}

	private String retvalue = null;
	
	@Override
	public String processUserQueryNewThreadWithBusyIndicator(Resource resource, OntModel theModel, String modelName, ExpectsAnswerContent sc) {
		Display display = PlatformUI.getWorkbench().getDisplay();
	 	BusyIndicator.showWhile(display, new Runnable() {				
	 		@Override
	 		public void run() {
	 			long time1 = System.currentTimeMillis();				
	 			try {		
	 				String retstr = getAnswerConfigurationManager().processUserRequest(resource, theModel, modelName, sc);
	 				setRetvalue(retstr);
	 			} catch (ConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ExecutionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (TranslationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (InvalidNameException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (ReasonerNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (QueryParseException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (QueryCancelledException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SadlInferenceException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (EquationNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (AnswerExtractionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
	 				long time2 = System.currentTimeMillis();
	 				long delay = time2 - time1;
	 				LOGGER.debug("Time to process user query: " + delay + " ms");
	 			}
	 		}
	 	});
		return getRetvalue();
	}

	public String getRetvalue() {
		return retvalue;
	}

	public void setRetvalue(String retvalue) {
		this.retvalue = retvalue;
	}
	
}

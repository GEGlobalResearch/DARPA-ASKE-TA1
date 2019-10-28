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
import java.util.concurrent.atomic.AtomicReference;

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
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
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
import com.ge.research.sadl.darpa.aske.dialog.ui.internal.DialogActivator;
import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.QuestionWithCallbackContent;
import com.ge.research.sadl.darpa.aske.processing.StatementContent;
import com.ge.research.sadl.darpa.aske.ui.editor.DialogEditors;
import com.ge.research.sadl.darpa.aske.ui.editor.DialogEditors.Options;
import com.ge.research.sadl.model.visualizer.IGraphVisualizer;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.ge.research.sadl.sADL.SadlImport;
import com.ge.research.sadl.sADL.SadlModel;
import com.ge.research.sadl.ui.handlers.SadlActionHandler;
import com.google.common.base.Preconditions;
import com.google.inject.Injector;

public class DialogAnswerProvider extends BaseDialogAnswerProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(DialogAnswerProvider.class);

	private IXtextDocument document;
	private IConfigurationManagerForIDE configManager;
	private IXtextModelListener modelListener;

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
		URI uri = resource.getURI();
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
		if (answerConfigurationManager != null) {
			answerConfigurationManager.clearQuestionsAndAnsers();
		}
		if (configManager != null) {
			configManager.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, null);
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

	private Object[] getSourceText(EObject object) {
		INode node = NodeModelUtils.findActualNodeFor(object);
		if (node != null) {
			String txt = NodeModelUtils.getTokenText(node);
			int start = NodeModelUtils.getNode(object).getTotalOffset();
			int length = NodeModelUtils.getNode(object).getTotalLength();
			Object[] ret = new Object[3];
			ret[0] = txt.trim();
			ret[1] = start;
			ret[2] = length;
			return ret;
		}
		return null;
	}

	private synchronized boolean addCurationManagerContentToDialog(IXtextDocument document, IRegion reg, String content,
			Object ctx, boolean quote) throws BadLocationException {
		return addCurationManagerContentToDialog(document, reg, content, ctx, quote, true, true, true);
	}
	
	private synchronized boolean addCurationManagerContentToDialog(IXtextDocument document, IRegion reg, String content,
			Object ctx, boolean quote, boolean prependAgent, boolean repositionCursor, boolean addLeadingSpaces) throws BadLocationException {
		LOGGER.debug(content);
//		System.err.println("addCMContent: " + content);
		URI uri = document.readOnly(GetResourceUri.INSTANCE);
		Display.getDefault().syncExec(() -> {
			try {
				String modContent;
				int loc;
				if (quote) {
					modContent = generateDoubleQuotedContentForDialog(content);
				} else {
					if (prependAgent) {
						modContent = content.startsWith("CM:") ? content : ("CM: " + content);
					}
					else {
						modContent = content;
					}
					if (!modContent.trim().endsWith(".") && !modContent.trim().endsWith("?")) {
						modContent += ".";
					}
				}
				if (ctx instanceof EObject && getSourceText((EObject) ctx) != null) {
					Object[] srcinfo = getSourceText((EObject) ctx);
					// String srctext = (String) srcinfo[0];
					int start = (int) srcinfo[1];
					int length = (int) srcinfo[2];
					// find location of this in document
					if (addLeadingSpaces && !srcinfo[0].toString().endsWith(" ")) {
						modContent = " " + modContent;
					}
					loc = start + length + 1;
					int docLen = document.getLength();
					int testLen = Math.min(5, docLen - loc);
					String test = document.get(loc, testLen);
					if (addLeadingSpaces && !test.startsWith(" ")) {
						modContent = " " + modContent;
					}
					if (!test.startsWith("\r\n")) {
						modContent += "\r\n";
					}
					document.replace(start + length + 1, 0, modContent);
					loc = start + length + 1;
					if (repositionCursor && document instanceof XtextDocument && ctx instanceof EObject) {
						final int caretOffset = loc + modContent.length();
						setCaretOffsetInEditor(uri, caretOffset);
					}
				} else {
					loc = document.getLength();
					document.set(document.get() + "\n" + modContent);
					if (repositionCursor) {
						setCaretOffsetInEditor(uri, document.get().length() - 1);
					}
				}
				LOGGER.debug("Adding to Dialog editor: " + modContent);
				textAtLocation(document, modContent, loc);
			} catch (BadLocationException e) {
				Exceptions.throwUncheckedException(e);
			}
		});
		return true;
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
						sb.append("\n");
					}
					sb.append(importStatement);
				}
//				String precedingObjText = NodeModelUtils.getTokenText(NodeModelUtils.getNode(precedingObj));
				try {
					return addCurationManagerContentToDialog(document, null, sb.toString(), precedingObj, false, false, false, false);
				} catch (BadLocationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
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

	/**
	 * Method to check to see if the content was added successfully
	 * 
	 * @param document
	 * @param content
	 * @param loc
	 * @return
	 */
	private boolean textAtLocation(IDocument document, String content, int loc) {
		int doclen = document.getLength();
		if (content != null && loc < doclen) {
			String test;
			try {
				test = document.get(loc, content.length());
				if (test.trim().equals(content.trim())) {
					return true;
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return false;
	}

	@Override
	public String addCurationManagerInitiatedContent(AnswerCurationManager answerCurationManager, StatementContent sc) {
		try {
			addCurationManagerContentToDialog(getTheDocument(), null, sc.getText(), null, false);
		} catch (BadLocationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public boolean addCurationManagerAnswerContent(AnswerCurationManager acm, String content, Object ctx) {
		answerConfigurationManager = acm;
		try {
			return addCurationManagerContentToDialog(document, null, content, ctx, false);
		} catch (BadLocationException e) {
			e.printStackTrace();
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
				e.printStackTrace();
			}
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

	protected Map<String, String> getPreferences(IFile file) {
		final URI uri = URI.createPlatformResourceURI(file.getFullPath().toString(), true);
		return getPreferences(uri);
	}

	@Override
	public Map<String, String> getPreferences(URI uri) {
		Injector reqInjector = safeGetInjector(DialogActivator.COM_GE_RESEARCH_SADL_DARPA_ASKE_DIALOG);
		IPreferenceValuesProvider pvp = reqInjector.getInstance(IPreferenceValuesProvider.class);
		IPreferenceValues preferenceValues = pvp.getPreferenceValues(new XtextResource(uri));
		if (preferenceValues != null) {
			Map<String, String> map = new HashMap<String, String>();
			String tsburl = preferenceValues.getPreference(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI);
			if (tsburl != null) {
				map.put(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getId(), tsburl);
			}
			String j2psburl = preferenceValues.getPreference(DialogPreferences.ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI);
			if (j2psburl != null) {
				map.put(DialogPreferences.ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI.getId(), j2psburl);
			}
			String usekchain = preferenceValues.getPreference(DialogPreferences.USE_ANSWER_KCHAIN_CG_SERVICE);
			if (usekchain != null) {
				map.put(DialogPreferences.USE_ANSWER_KCHAIN_CG_SERVICE.getId(), usekchain);
			}
			String kchaincgsburl = preferenceValues.getPreference(DialogPreferences.ANSWER_KCHAIN_CG_SERVICE_BASE_URI);
			if (kchaincgsburl != null) {
				map.put(DialogPreferences.ANSWER_KCHAIN_CG_SERVICE_BASE_URI.getId(), kchaincgsburl);
			}
			String dbncgsburl = preferenceValues.getPreference(DialogPreferences.ANSWER_DBN_CG_SERVICE_BASE_URI);
			if (dbncgsburl != null) {
				map.put(DialogPreferences.ANSWER_DBN_CG_SERVICE_BASE_URI.getId(), dbncgsburl);
			}
			String dbnjsongensburl = preferenceValues.getPreference(DialogPreferences.DBN_INPUT_JSON_GENERATION_SERVICE_BASE_URI);
			if (dbnjsongensburl != null) {
				map.put(DialogPreferences.DBN_INPUT_JSON_GENERATION_SERVICE_BASE_URI.getId(), dbnjsongensburl);
			}
			String usedbn = preferenceValues.getPreference(DialogPreferences.USE_DBN_CG_SERVICE);
			if (usedbn != null) {
				map.put(DialogPreferences.USE_DBN_CG_SERVICE.getId(), usedbn);
			}
			return map;
		}
		return null;
	}

	protected final Injector safeGetInjector(String name) {
		final AtomicReference<Injector> i = new AtomicReference<Injector>();
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run() {
				i.set(DialogActivator.getInstance().getInjector(name));
			}
		});

		return i.get();
	}

	public Resource getResource() {
		Preconditions.checkState(this.document != null, "Not initialized yet.");
		return this.document.readOnly(resource -> resource);
	}

}

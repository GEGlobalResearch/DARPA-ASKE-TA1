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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.ui.editor.model.IXtextDocument;
import org.eclipse.xtext.ui.editor.model.XtextDocument;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.curation.BaseDialogAnswerProvider;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.MixedInitiativeElement;
import com.ge.research.sadl.model.visualizer.IGraphVisualizer;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.ui.handlers.SadlActionHandler;
import com.google.common.base.Preconditions;

public class DialogAnswerProvider extends BaseDialogAnswerProvider {

	private static final Logger LOGGER = LoggerFactory.getLogger(DialogAnswerProvider.class);

	private IXtextDocument document;
	private IConfigurationManagerForIDE configManager;

	public void configure(IXtextDocument document) {
		Preconditions.checkState(this.document == null, "Already initialized.");
		this.document = document;
		this.document.readOnly(resource -> {
			initializedConfigManager(resource);
			Object lastCommand = OntModelProvider.getPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND);
			LOGGER.debug("DialogAnswerProvider: Last cmd: " + (lastCommand == null ? "null" : lastCommand.toString()));
			return null; // Void
		});
	}

	public void dispose() {
		super.dispose();
		if (answerConfigurationManager != null) {
			answerConfigurationManager.clearQuestionsAndAnsers();
		}
		configManager.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, null);
	}

	/**
	 * Method to display graph
	 * 
	 * @param visualizer -- IGraphVisualizer instance that contains graphing info
	 * @return -- null if successful else an error message
	 */
	// XXX: called via reflection from the com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.displayGraph(IGraphVisualizer)
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

	private String initializedConfigManager(Resource resource) throws ConfigurationException {
		String modelFolder = SadlActionHandler.getModelFolderFromResource(resource);
		configManager = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(modelFolder, null);
		configManager.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, this);
		return modelFolder;
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

	private synchronized boolean addCurationManagerContentToDialog(IDocument document, IRegion reg, String content,
			Object ctx, boolean quote) throws BadLocationException {
		String modContent;
		int loc;
		if (quote) {
			modContent = generateDoubleQuotedContentForDialog(content);
		} else {
			modContent = content.startsWith("CM:") ? content : ("CM: " + content);
			if (!modContent.trim().endsWith(".") && !modContent.trim().endsWith("?")) {
				modContent += ".";
			}
		}
		if (ctx instanceof EObject && getSourceText((EObject) ctx) != null) {
			Object[] srcinfo = getSourceText((EObject) ctx);
//			String srctext = (String) srcinfo[0];
			int start = (int) srcinfo[1];
			int length = (int) srcinfo[2];
			// find location of this in document
			loc = start + length + 1;
			int docLen = document.getLength();
			int testLen = Math.min(5, docLen - loc);
			String test = document.get(loc, testLen);
			if (!test.startsWith(" ")) {
				modContent = " " + modContent;
			}
			if (!test.startsWith("\r\n")) {
				modContent += "\r\n";
			}
			document.replace(start + length + 1, 0, modContent);
			System.out.println("AFTER UPDATE:\n" + document.get());
			System.out.println("ENDOF-DOC");
			loc = start + length + 1;
			if (document instanceof XtextDocument && ctx instanceof EObject) {
				final URI expectedUri = ((EObject) ctx).eResource().getURI();
				final int caretOffset = start + length + 1 + modContent.length();
				setCaretOffsetInEditor(expectedUri, caretOffset); // Async
			}
		} else {
			loc = document.getLength();
			document.set(document.get() + modContent + "\n");
		}
		LOGGER.debug("Adding to Dialog editor: " + modContent);
		return textAtLocation(document, modContent, loc);
	}

	private void setCaretOffsetInEditor(URI uri, int caretOffset) {
		Display.getDefault().asyncExec(() -> {
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			for (IEditorReference editorRef : page.getEditorReferences()) {
				if (DialogAnswerProviders.DIALOG_EDITOR_ID.equals(editorRef.getId())) {
					IEditorInput editorInput;
					try {
						editorInput = editorRef.getEditorInput();
						if (editorInput instanceof IFileEditorInput) {
							String path = ((IFileEditorInput) editorInput).getFile().getFullPath().toPortableString();
							URI editorUri = URI.createPlatformResourceURI(path, true);
							if (editorUri.equals(uri)) {
								IWorkbenchPart part = editorRef.getPart(false);
								if (part instanceof IAdaptable) {
									Control control = part.getAdapter(Control.class);
									if (control instanceof StyledText) {
										((StyledText) control).setCaretOffset(caretOffset);
										control.redraw();
									}
								}
								break;
							}
						}
					} catch (PartInitException e) {
						e.printStackTrace();
					}
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
	public String initiateMixedInitiativeInteraction(MixedInitiativeElement element) {
		String content = element.getContent().toString();
		if (document != null) {
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

	protected IConfigurationManagerForIDE getConfigManager() {
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

			} catch (CoreException e) {
				e.printStackTrace();
			}
		}
	}

	public Resource getResource() {
		Preconditions.checkState(this.document != null, "Not initialized yet.");
		return this.document.readOnly(resource -> resource);
	}

}

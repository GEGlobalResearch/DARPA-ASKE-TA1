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
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Consumer;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.emf.common.util.EList;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.impl.CompositeNodeWithSemanticElement;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.preferences.IPreferenceValues;
import org.eclipse.xtext.preferences.IPreferenceValuesProvider;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.autoedit.DefaultAutoEditStrategyProvider;
import org.eclipse.xtext.ui.editor.model.XtextDocument;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.eclipse.xtext.util.ITextRegionWithLineInformation;
import org.eclipse.xtext.util.concurrent.IUnitOfWork;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.builder.ConfigurationManagerForIDE;
import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.dialog.ui.internal.DialogActivator;
import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.darpa.aske.processing.BuildConstruct;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.HowManyValuesConstruct;
import com.ge.research.sadl.darpa.aske.processing.IDialogAnswerProvider;
import com.ge.research.sadl.darpa.aske.processing.MixedInitiativeElement;
import com.ge.research.sadl.darpa.aske.processing.MixedInitiativeTextualResponse;
import com.ge.research.sadl.darpa.aske.processing.WhatIsConstruct;
import com.ge.research.sadl.darpa.aske.ui.handler.DialogRunInferenceHandler;
import com.ge.research.sadl.darpa.aske.ui.handler.RunDialogQuery;
import com.ge.research.sadl.model.gp.GraphPatternElement;
import com.ge.research.sadl.model.gp.Junction;
import com.ge.research.sadl.model.gp.Junction.JunctionType;
import com.ge.research.sadl.model.gp.NamedNode;
import com.ge.research.sadl.model.gp.NamedNode.NodeType;
import com.ge.research.sadl.model.gp.Node;
import com.ge.research.sadl.model.gp.ProxyNode;
import com.ge.research.sadl.model.gp.Query;
import com.ge.research.sadl.model.gp.TripleElement;
import com.ge.research.sadl.model.gp.VariableNode;
import com.ge.research.sadl.model.visualizer.GraphVizVisualizer;
import com.ge.research.sadl.model.visualizer.IGraphVisualizer;
import com.ge.research.sadl.owl2sadl.OwlToSadl;
import com.ge.research.sadl.parser.antlr.SADLParser;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.SadlCommandResult;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.ge.research.sadl.sADL.SadlImport;
import com.ge.research.sadl.sADL.SadlModel;
import com.ge.research.sadl.sADL.SadlModelElement;
import com.ge.research.sadl.ui.handlers.SadlActionHandler;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class DialogAnswerProvider extends DefaultAutoEditStrategyProvider implements IDialogAnswerProvider {
	private static final Logger logger = LoggerFactory.getLogger(DialogAnswerProvider.class);
	private XtextDocument theDocument;
	private Resource resource;
	private IConfigurationManagerForIDE configMgr = null;

	@Inject
	protected IResourceSetProvider resourceSetProvider;

//	@Inject
//	protected Provider<SadlRunQueryHandler> handlerProvider;

	@Inject
	protected Provider<DialogRunInferenceHandler> handlerProvider;		// Provider will give new instance
	
	@Inject
	protected SADLParser sadlParser;
	
	@Inject
	protected Provider<RunDialogQuery> rdqProvider;
	private Map<String, MixedInitiativeElement> mixedInitiativeElements = new HashMap<String, MixedInitiativeElement>();

	@Override
	protected void configure(IEditStrategyAcceptor acceptor) {

	    IAutoEditStrategy strategy = new IAutoEditStrategy() 
	    {

	        private List<String> sadlkeywords = null;
			private IGraphVisualizer visualizer = null;
			private OntModel theModel;
			private OwlToSadl owl2sadl;

			@Override
	        public void customizeDocumentCommand(IDocument document, DocumentCommand command) 
	        {
				int cursorLocation = findCursorLocation(document, command);
//	            if ( command.text.length() == 0 || command.text.charAt(0) > ' ') return;

	            IRegion reg = ((XtextDocument) document).getLastDamage();

	            try {
//	            	ServiceLoader<Builtin> itr = ServiceLoader.load(Builtin.class);
//	            	Iterator<Builtin> itrr = itr.iterator();
//	            	while (itrr.hasNext()) {
//	            		System.out.println(itrr.next().getClass().getCanonicalName());
//	            	}
	                if (document instanceof XtextDocument) {
	                	setResource(getResourceFromDocument((XtextDocument)document));
	    				
	                	String insertionText = null;

                		setTheDocument((XtextDocument) document);
                		String modelFolder = setDialogAnswerProvider(resource);
		                Object lastcmd = OntModelProvider.getPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND);
		                logger.debug("DialogAnswerProvider: Last cmd: " + (lastcmd != null ? lastcmd.toString() : "null"));
	                	if (lastcmd instanceof Query) {
	                		if (isDocToModelQuery((Query)lastcmd)) {
	                			insertionText = (String) processDocToModelQuery(resource, (Query)lastcmd);
		                		insertionText = checkForEOS(insertionText);
		                		Object ctx = ((Query)lastcmd).getContext();
		                		addCurationManagerContentToDialog(document, reg, insertionText, ctx, true);
	                		}
	                		else if (isModelToDocQuery((Query)lastcmd)) {
	                			lastcmd = processModelToDocQuery(resource, (Query)lastcmd);
	                		}
	                		else {
		                		ResultSet rs = runQuery(resource, (Query)lastcmd);
		                		String resultStr = null;
		                		if (rs != null) {
		                			resultStr = resultSetToQuotableString(rs);
		                		}
		                		insertionText = (resultStr != null ? resultStr : "\"Failed to find results\"");
		                		insertionText = checkForEOS(insertionText);
		                		Object ctx = ((Query)lastcmd).getContext();
		                		addCurationManagerContentToDialog(document, reg, insertionText, ctx, true);
	                		}
	                	}
	                	else if (lastcmd instanceof NamedNode) {
	                		whatIsNamedNode(document, reg, resource, lastcmd);
	                	}
	                	else if (lastcmd instanceof WhatIsConstruct) {
	                		Object trgt = ((WhatIsConstruct)lastcmd).getTarget();
	                		Object whn = ((WhatIsConstruct)lastcmd).getWhen();
	                		if (trgt instanceof NamedNode && whn == null) {
	                			whatIsNamedNode(document, reg, resource, (NamedNode)trgt);
	                		}
	                		else if (trgt instanceof Object[] && whn == null) {
		                		if (allTripleElements((Object[])trgt)) {
			                		Object ctx = null;
		                			TripleElement[] triples = flattenTriples((Object[])trgt);
		                			ctx = triples[0].getContext();
					                OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND, triples);
			                		StringBuilder answer = new StringBuilder();
			                		ResultSet[] rss = insertTriplesAndQuery(resource, triples);
			                		String resultStr = null;
			                		if (rss != null) {
			                			StringBuilder sb = new StringBuilder();
			                			if (triples[0].getSubject() instanceof VariableNode && 
			                					((VariableNode)triples[0].getSubject()).getType() instanceof NamedNode) {
			                				sb.append("the ");
			                				sb.append(((VariableNode)(triples[0].getSubject())).getType().getName());
			                				sb.append(" has ");
			                				sb.append(triples[0].getPredicate().getName());
			                				sb.append(" ");
			                				sb.append(rss[0].getResultAt(0, 0).toString());
			                				resultStr = sb.toString();
			                			}
			                			else {
				                			for (ResultSet rs : rss) {
				                				rs.setShowNamespaces(true);
				                				sb.append(rs.toString());
				                			}
			                				resultStr = resultSetToQuotableString(sb.toString());
			                			}
			                		}
			                		insertionText = (resultStr != null ? resultStr : "\"Failed to find results\"");
			                		addCurationManagerContentToDialog(document, reg, insertionText, ctx, true);
		                		}
	                		}
	                		else {
	                			// there is a when clause, and this is in a WhatIsConstruct
	                			List<TripleElement> tripleLst = new ArrayList<TripleElement>();
                				if (trgt instanceof TripleElement) {
	                				tripleLst.add((TripleElement)trgt);
                				}
                				else if (trgt instanceof Junction) {
                					tripleLst = addTriplesFromJunction((Junction) trgt, tripleLst);
                				}
	                			if (whn instanceof TripleElement) {
	                				// we have a when statement
	                				tripleLst.add((TripleElement)whn);
	                			}
	                			else if (whn instanceof Junction) {
	                				tripleLst = addTriplesFromJunction((Junction) whn, tripleLst);
	                			}
                				TripleElement[] triples = new TripleElement[tripleLst.size()];
                				triples = tripleLst.toArray(triples);
	                			
				                OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND, triples);
		                		StringBuilder answer = new StringBuilder();
		                		ResultSet[] rss = insertTriplesAndQuery(resource, triples);
		                		if (rss != null) {
		                			int cntr = 0;
			                		for (ResultSet rs : rss) {
			                			if (cntr == 0) {
			                				// this is the first ResultSet, construct a graph if possible
			                				if (rs.getColumnCount() != 3) {
			                					System.err.println("Can't construct graph; not 3 columns. Unexpected result.");
			                				}
//			                				this.graphVisualizerHandler.resultSetToGraph(path, resultSet, description, baseFileName, orientation, properties);
			                				
			                				IGraphVisualizer visualizer = new GraphVizVisualizer();
			                				if (visualizer != null) {
			                					String graphsDirectory = new File(modelFolder).getParent() + "/Graphs";
			                					new File(graphsDirectory).mkdir();
			                					String baseFileName = "QueryMetadata";
			                					visualizer.initialize(
			                		                    graphsDirectory,
			                		                    baseFileName,
			                		                    baseFileName,
			                		                    null,
			                		                    IGraphVisualizer.Orientation.TD,
			                		                    "Assembled Model");
			                					rs.setShowNamespaces(false);
			                		            visualizer.graphResultSetData(rs);				                				}
			        						String fileToOpen = visualizer.getGraphFileToOpen();
			        						if (fileToOpen != null) {
			        							File fto = new File(fileToOpen);
			        							if (fto.isFile()) {
			        								IFileStore fileStore = EFS.getLocalFileSystem().getStore(fto.toURI());
			        								IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
			        								try {
			        									IDE.openEditorOnFileStore(page, fileStore);
			        								}
			        								catch (Throwable t) {
			        									System.err.println("Error trying to display graph file '" + fileToOpen + "': " + t.getMessage());
			        								}
			        							}
			        							else if (fileToOpen != null) {
			        								System.err.println("Failed to open graph file '" + fileToOpen + "'. Try opening it manually.");
			        							}
			        						}
			                				else {
			                					System.err.println("Unable to find an instance of IGraphVisualizer to render graph for query.\n");
			                				}

			                			}
			                			if (cntr > 0) 
			                				answer.append(resultSetToQuotableString(rs));
			                			if(cntr++ > 1)
			                				answer.append(",\n");
			                			
			                		}
			                		answer.append(".\n");
		                		}
		                		Object ctx = triples[0].getContext();
		                		addCurationManagerContentToDialog(document, reg, answer.toString(), ctx, true);
//	                			}
//	                			else {
////	                				rdqProvider.get().execute(null, null, null);
//	                				System.out.println("Target is: " + trgt.toString());
//	                				System.out.println("When is: " + whn.toString());
//	                			}
	                		}
	                	}
	                	else if (lastcmd instanceof HowManyValuesConstruct) {
	                		// this is a HowManyValues  ??
	                		Object article = ((Object[])lastcmd)[0];
	                		Object cls = ((Object[])lastcmd)[1];
	                		Object prop = ((Object[])lastcmd)[2];
	                		Object typ = ((Object[])lastcmd)[3];
	                		StringBuilder answer = new StringBuilder("CM: ");
	                		answer.append(prop.toString());
	                		answer.append(" describes ");
	                		answer.append(cls.toString());
	                		answer.append(" with exactly ");
	                		
	                	}
	                	else if (lastcmd instanceof Object[]) {
	                		System.err.println("Unhandled Object[] lastcmd");
	                	}
	                	else if (lastcmd instanceof TripleElement) {
	                		StringBuilder answer = new StringBuilder();
	                		addTripleQuestion(resource, (TripleElement)lastcmd, answer);
	                		Object ctx = ((TripleElement)lastcmd).getContext();
	                		addCurationManagerContentToDialog(document, reg, answer.toString(), ctx, true);
	                	}
	                	else if (lastcmd instanceof MixedInitiativeTextualResponse) {
	                		int ip = ((MixedInitiativeTextualResponse)lastcmd).getInsertionPoint();
	                		String content = ((MixedInitiativeTextualResponse)lastcmd).getResponse();
	                		Region nreg = new Region(ip, content.length());
	                		Object ctx = null; //((MixedInitiativeTextualResponse)lastcmd).getContext();
	                		addCurationManagerContentToDialog(document, nreg, content, ctx, true);
	                	}
	                	else if (lastcmd instanceof BuildConstruct) {
	                		processBuildRequest(resource, (BuildConstruct)lastcmd);
	                	}
	                	else if (lastcmd != null) {
                			logger.debug("    Lastcmd '" + lastcmd.getClass().getCanonicalName() + "' not handled yet!");
	                	}
//		                String possibleKWD = token.toLowerCase();
//		                if ( token.equals(possibleKWD.toUpperCase()) || !KWDS.contains(possibleKWD) ) return;
//		                document.replace(reg.getOffset(), reg.getLength(), possibleKWD.toUpperCase());
	                }
	            } 
	            catch (Exception e) 
	            {
	                logger.debug("AutoEdit error (of type " + e.getClass().getCanonicalName() + "): " + e.getMessage());   
	            }
	        }

			private int findCursorLocation(IDocument document, DocumentCommand command) {
				// determine the cursor location
				int len = command.length;
				int caroffset = command.caretOffset;
				int offset = command.offset;
				String txt = command.text;
				if (document instanceof XtextDocument) {
					XtextDocument xdoc = (XtextDocument)document;
					try {
						String doctxt = xdoc.get(offset, Math.max(txt.length(),len));
						if (doctxt.equals(txt)) {
							return offset;
						}
					} catch (BadLocationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
//				XtextEditor xtextEditor = EditorUtils.getActiveXtextEditor(event);
				return -1;
			}

			private void processBuildRequest(Resource resource, BuildConstruct lastcmd) throws BadLocationException {
				String buildTarget = ((BuildConstruct)lastcmd).getTarget();
				Object acmObj = getConfigMgr().getPrivateKeyValuePair(DialogConstants.ANSWER_CURATION_MANAGER);
				String result = null;
				if (acmObj == null) {
	    			Map<String, String> preferences = getPreferences(resource.getURI());
	    			try {
						acmObj = new AnswerCurationManager(getConfigMgr().getModelFolder(), getConfigMgr(), preferences);
		    			getConfigMgr().addPrivateKeyValuePair(DialogConstants.ANSWER_CURATION_MANAGER, acmObj);
		    			((AnswerCurationManager) acmObj).getExtractionProcessor().getCodeExtractor().setCodeModelFolder(getConfigMgr().getModelFolder());
					} catch (IOException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
						result = "Failed: " + e.getMessage();
					}
	    		}

				if (acmObj != null) {
					if (acmObj instanceof AnswerCurationManager) {
						try {
							result = ((AnswerCurationManager)acmObj).processBuildRequest(buildTarget);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
							result = "Failed: " + e.getMessage();
						}
					}
				}
				if (result != null) {
					addCurationManagerContentToDialog(getTheDocument(), null, result, lastcmd.getContext(), true);
				}
			}

			private String checkForEOS(String insertionText) {
				if (!insertionText.endsWith(".") && !insertionText.endsWith("?")) {
					insertionText += ".";
				}
				return insertionText;
			}

			private boolean isDocToModelQuery(Query lastcmd) {
				List<GraphPatternElement> patterns = lastcmd.getPatterns();
				if (patterns != null) {
					for (GraphPatternElement gpe : patterns) {
						if (gpe instanceof TripleElement) {
							if (((TripleElement)gpe).getPredicate().getURI().equals(DialogConstants.SADL_IMPLICIT_MODEL_DIALOG_MODEL_PROPERTY_URI)) {
								return true;
							}
						}
					}
				}
				return false;
			}

			private boolean isModelToDocQuery(Query lastcmd) {
				List<GraphPatternElement> patterns = lastcmd.getPatterns();
				if (patterns != null) {
					for (GraphPatternElement gpe : patterns) {
						if (gpe instanceof TripleElement) {
							if (((TripleElement)gpe).getPredicate().getURI().equals(DialogConstants.SADL_IMPLICIT_MODEL_DIALOG_DATA_PROPERTY_URI)) {
								return true;
							}
						}
					}
				}
				return false;
			}

			private Object processDocToModelQuery(Resource resource, Query lastcmd) throws ExecutionException {
				List<GraphPatternElement> patterns = lastcmd.getPatterns();
				for (GraphPatternElement gpe : patterns) {
					if (gpe instanceof TripleElement) {
						if (((TripleElement)gpe).getPredicate().getURI().equals(DialogConstants.SADL_IMPLICIT_MODEL_DIALOG_MODEL_PROPERTY_URI)) {
							Node doc = ((TripleElement)gpe).getSubject();
							// get the name and semantic type of each column
							// TODO check for aliases for column names and use alias if exists
							String tableColSemTypeQuery = "select distinct ?colname ?typ where {<" + doc.getURI() +
									"> <columnDescriptors> ?cdlist . ?cdlist <http://jena.hpl.hp.com/ARQ/list#member> ?member . " + 
									"?member <descriptorName> ?colname . ?member <augmentedType> ?augtype . ?augtype <semType> ?typ}";
	                		((Query)lastcmd).setSparqlQueryString(tableColSemTypeQuery);
							ResultSet rs = runQuery(resource, (Query)lastcmd);
							if (rs != null && rs.getRowCount() > 0) {
								// TODO 
								// convert to triples, one set for each column
								// <doc, columnname, colname>
								// <colname, semtypeprop, semtype>
								rs.setShowNamespaces(true);
								Map<String,String> colNamesAndTypes = new HashMap<String,String>();
								for (int i = 0; i <= rs.getColumnCount(); i++) {
									String colname = rs.getResultAt(i, 0).toString();
									String semtyp = rs.getResultAt(i, 1).toString();
									colNamesAndTypes.put(colname, semtyp);
								}
								Iterator<String> keyitr = colNamesAndTypes.keySet().iterator();
								NamedNode docNN = new NamedNode(doc.getURI());
//								NamedNode descriptorNameNN = new NamedNode(SadlConstants.SADL_IMPLICIT_MODEL_DESCRIPTOR_NAME_PROPERTY_URI);
//								NamedNode augmentedTypeNN = new NamedNode(SadlConstants.SADL_IMPLICIT_MODEL_AUGMENTED_TYPE_PROPERTY_URI);
								List<TripleElement> triplesList = new ArrayList<TripleElement>();
								while (keyitr.hasNext()) {
									String colname = keyitr.next();
									NamedNode colNameNN = new NamedNode(colname);
									String semtyp = colNamesAndTypes.get(colname);
									NamedNode semtypNN = new NamedNode(semtyp);
									TripleElement tr = new TripleElement(docNN, colNameNN, semtypNN);
									triplesList.add(tr);
								}
								TripleElement[] triples = triplesList.toArray(new TripleElement[triplesList.size()]);
				                OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND, triples);
		                		ResultSet[] rss = insertTriplesAndQuery(resource, triples);
								int numResultSets = rss.length;
								
								// 
								// TODO 
								// SIMILAR TO WhatIfConstruct with a when
//				                OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND, triples);
//		                		StringBuilder answer = new StringBuilder();
//		                		ResultSet[] rss = insertTriplesAndQuery(resource, triples);

//								Map<String,String> colNamesAndTypes = new HashMap<String,String>();
//								for (int i = 0; i <= rs.getColumnCount(); i++) {
//									String colname = rs.getResultAt(i, 0).toString();
//									String semtyp = rs.getResultAt(i, 1).toString();
//									colNamesAndTypes.put(semtyp, colname);
//								}
//								if (colNamesAndTypes.size() > 0) {
//									StringBuilder qsb = new StringBuilder("select distinct ?eq ?argname ?argsemtype where {" + 
//											"	{select ?eq ?argname ?argsemtype where {?eq <arguments> ?arglist . ?arglist <http://jena.hpl.hp.com/ARQ/list#member> ?member . " + 
//											"	?member <descriptorName> ?argname . ?member <augmentedType> ?augtype . ?augtype <semType> ?argsemtype" + 
//											"	}}" + 
//											"	UNION" + 
//											"	{select ?eq ?argname ?argsemtype where {?eq <returnTypes> ?retlist . ?retlist <http://jena.hpl.hp.com/ARQ/list#member> ?member . " + 
//											"	OPTIONAL{?member <descriptorName> ?argname} . ?member <augmentedType> ?augtype . ?augtype <semType> ?argsemtype}}" + 
//											"	. 	VALUES ?argsemtype {");
//									Set<String> keys = colNamesAndTypes.keySet();
//									for (String key : keys) {
//										qsb.append("<");
//										qsb.append(key);
//										qsb.append("> ");
//									}
//									qsb.append("}}");
////									System.out.println(qsb.toString());
//									((Query)lastcmd).setSparqlQueryString(qsb.toString());
//									ResultSet rs2 = runQuery(resource, (Query)lastcmd);
//									rs2.setShowNamespaces(true);
//									if (rs2 != null && rs2.getRowCount() > 0) {
//										StringBuilder retsb = new StringBuilder("Models found (table, colname, model, argname, matchingType)\n");
//										for (int i = 0; i < rs2.getRowCount(); i++) {
//											String eq = rs2.getResultAt(i, 0).toString();
//											Object argnameObj = rs2.getResultAt(i, 1);
//											String argname = argnameObj != null ? argnameObj.toString() : "return";
//											String argsemtype = rs2.getResultAt(i, 2).toString();
//											String colname = colNamesAndTypes.get(argsemtype);
//											retsb.append(doc.getName() + ", " + colname + ", " + eq + ", " + argname  + ", " + argsemtype);
//											retsb.append("\n");
//										}
//										return retsb.toString();
//									}
//								}
							}
							else {
								
							}
						}
					}
				}
				return lastcmd;
			}

			private Object processModelToDocQuery(Resource resource, Query lastcmd) {
				List<GraphPatternElement> patterns = lastcmd.getPatterns();
				for (GraphPatternElement gpe : patterns) {
					if (gpe instanceof TripleElement) {
						if (((TripleElement)gpe).getPredicate().getURI().equals(DialogConstants.SADL_IMPLICIT_MODEL_DIALOG_DATA_PROPERTY_URI)) {
							Node doc = ((TripleElement)gpe).getSubject();
							
						}
					}
				}
				return lastcmd;
			}

			private List<TripleElement> addTriplesFromJunction(Junction jct, List<TripleElement> tripleLst) {
				Object lhs = jct.getLhs();
				if (lhs instanceof ProxyNode) {
					if (((ProxyNode)lhs).getProxyFor() instanceof TripleElement) {
						tripleLst.add((TripleElement) ((ProxyNode)lhs).getProxyFor());
					}
					else if (((ProxyNode)lhs).getProxyFor() instanceof Junction) {
						tripleLst = addTriplesFromJunction((Junction) ((ProxyNode)lhs).getProxyFor(), tripleLst);
					}
				}
				Object rhs = jct.getRhs();
				if (rhs instanceof ProxyNode) {
					if (((ProxyNode)rhs).getProxyFor() instanceof TripleElement) {
						tripleLst.add((TripleElement) ((ProxyNode)rhs).getProxyFor());
					}
					else if (((ProxyNode)rhs).getProxyFor() instanceof Junction) {
						tripleLst = addTriplesFromJunction((Junction) ((ProxyNode)rhs).getProxyFor(), tripleLst);
					}
				}
				return tripleLst;
			}

			private void whatIsNamedNode(IDocument document, IRegion reg, Resource resource, Object lastcmd)
					throws ExecutionException, BadLocationException {
				// what is NamedNode?
				NamedNode nn = (NamedNode) lastcmd;
				NodeType typ = nn.getNodeType();
				boolean isFirstProperty = true;
				StringBuilder answer = new StringBuilder();
				if (typ.equals(NodeType.ClassNode)) {
//					answer.append(checkForKeyword(nn.getName()));
//					int len = answer.length();
//					answer = getClassHierarchy(resource, nn, answer);
//					if (answer.length() == len) {
//						answer.append(" is a class");
//					}
					answer.append(getOwlToSadl(getTheModel()).classToSadl(nn.getURI()));
					isFirstProperty = addDomainAndRange(resource, nn, isFirstProperty, answer);
					addQualifiedCardinalityRestriction(resource, nn, isFirstProperty, answer);		
					Object ctx = ((NamedNode)lastcmd).getContext();
					addCurationManagerContentToDialog(document, reg, answer.toString(), ctx, false);
				}
				else if (typ.equals(NodeType.ObjectProperty) || typ.equals(NodeType.DataTypeProperty) || typ.equals(NodeType.PropertyNode)) {
					addPropertyWithDomainAndRange(resource, nn, answer);
					Object ctx = ((NamedNode)lastcmd).getContext();
					addCurationManagerContentToDialog(document, reg, answer.toString(), ctx, true);
				}
				else if (typ.equals(NodeType.AnnotationProperty)) {
					addAnnotationPropertyDeclaration(resource, nn, answer);
					Object ctx = ((NamedNode)lastcmd).getContext();
					addCurationManagerContentToDialog(document, reg, answer.toString(), ctx, true);
				}
				else if (typ.equals(NodeType.InstanceNode)) {
					addInstanceDeclaration(resource, nn, answer);
					Object ctx = ((NamedNode)lastcmd).getContext();
				    addCurationManagerContentToDialog(document, reg, answer.toString(), ctx, true);
				}
				else if (typ.equals(NodeType.FunctionNode)) {
					addInstanceDeclaration(resource, nn, answer);
					Object ctx = ((NamedNode)lastcmd).getContext();
					addCurationManagerContentToDialog(document, reg, answer.toString(), ctx, true);
				}
				else {
					logger.debug("    Lastcmd '" + lastcmd.getClass().getCanonicalName() + "' not handled yet!");
					System.err.println("Type " + typ.getClass().getCanonicalName() + " not handled yet.");
				}
			}

			private OwlToSadl getOwlToSadl(OntModel theModel) {
				if (owl2sadl == null) {
					owl2sadl = new OwlToSadl(theModel);
				}
				return owl2sadl;
			}

			private TripleElement[] flattenTriples(Object[] lastcmd) {
				List<TripleElement> triples = new ArrayList<TripleElement>();
				for (int i = 0; i < lastcmd.length; i++) {
					Object cmd = lastcmd[i];
					if (cmd instanceof TripleElement) {
						triples.add((TripleElement) cmd);
					}
					else if (cmd instanceof Junction) {
						triples.addAll(flattenJunction((Junction)cmd));
					}
				}
				return triples.toArray(new TripleElement[triples.size()]);
			}

			private Collection<? extends TripleElement> flattenJunction(Junction cmd) {
				List<TripleElement> triples = new ArrayList<TripleElement>();
				if (cmd.getJunctionType().equals(JunctionType.Conj)) {
					GraphPatternElement lhs = ((ProxyNode) cmd.getLhs()).getProxyFor();
					if (lhs instanceof TripleElement) {
						triples.add((TripleElement) lhs);
					}
					else if (lhs instanceof Junction) {
						triples.addAll(flattenJunction((Junction) lhs));
					}
					else {
						System.err.println("Encountered unsupported type flattening Junction: " + lhs.getClass().getCanonicalName());
					}
					GraphPatternElement rhs = ((ProxyNode) cmd.getRhs()).getProxyFor();
					if (rhs instanceof TripleElement) {
						triples.add((TripleElement) rhs);
					}
					else if (rhs instanceof Junction) {
						triples.addAll(flattenJunction((Junction) rhs));
					}
					else {
						System.err.println("Encountered unsupported type flattening Junction: " + rhs.getClass().getCanonicalName());
					}
				}
				else {
					System.err.println("Encountered disjunctive type flattening Junction");
				}
				return triples;
			}

			/**
			 * Are all elements of the array of type TripleElement?
			 * @param lastcmd
			 * @return
			 */
			private boolean allTripleElements(Object[] lastcmd) {
				for (int i = 0; i < lastcmd.length; i++) {
					Object el = lastcmd[i];
					if (el instanceof Junction) {
						if (!allTripleElements(((ProxyNode)((Junction)el).getLhs()).getProxyFor())) {
							return false;
						}
						if (!allTripleElements(((ProxyNode)((Junction)el).getRhs()).getProxyFor())) {
							return false;
						}
					}
					else if (!(el instanceof TripleElement)) {
						return false;
					}
				}
				return true;
			}

			private boolean allTripleElements(GraphPatternElement gpe) {
				if (gpe instanceof TripleElement) {
					return true;
				}
				return false;
			}

			private String resultSetToQuotableString(ResultSet rs) {
				String resultStr;
				rs.setShowNamespaces(true);
				resultStr = rs.toString();
				return resultSetToQuotableString(resultStr);
			}

			private String resultSetToQuotableString(String resultStr) {
				resultStr = resultStr.replace('"', '\'');
				resultStr = resultStr.trim();
				resultStr = "\"" + resultStr + "\"";
				return resultStr;
			}

			private void addTripleQuestion(Resource resource, TripleElement tr, StringBuilder answer) throws ExecutionException {
				List<String> vars = new ArrayList<String>();
				StringBuilder sbwhere = new StringBuilder("where {");
				if (tr.getSubject() == null) {
					vars.add("?s");
					sbwhere.append("?s ");
				}
				else {
					sbwhere.append("<");
					sbwhere.append(tr.getSubject().getURI());
					sbwhere.append("> ");
				}
				if (tr.getPredicate() == null) {
					vars.add("?p");
					sbwhere.append("?p ");
				}
				else {
					sbwhere.append("<");
					sbwhere.append(tr.getPredicate().getURI());
					sbwhere.append("> ");
				}
				if (tr.getObject() == null) {
					vars.add("?o");
					sbwhere.append("?o");
				}
				else {
					sbwhere.append("<");
					Node obj = tr.getObject();
					if (obj instanceof NamedNode) {
						sbwhere.append(obj.getURI());
					}
					else {
						sbwhere.append(obj.toString());
					}
					sbwhere.append("> ");
				}
				if (vars.size() > 0) {
					for (int i = vars.size() - 1; i >= 0; i--) {
						sbwhere.insert(0, " ");
						sbwhere.insert(0, vars.get(i));
					}
					sbwhere.insert(0,  "select ");
					sbwhere.append("}");
				}
				Query q = new Query();
				q.setSparqlQueryString(sbwhere.toString());
				OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND, q);
				ResultSet rs = runQuery(resource, q);
				OntModelProvider.clearPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND);
				if (rs != null) {
					if (rs.getRowCount() == 1) {
						if (tr.getSubject() != null && tr.getPredicate() != null) {
							answer.append(tr.getSubject().getName());
							answer.append(" has ");
							answer.append(tr.getPredicate().getName());
							answer.append(" ");
							answer.append(rs.getResultAt(0, 0));
						}
						else {
							answer.append("\"");
							answer.append(resultSetToQuotableString(rs));
							answer.append("\"");
						}
					}
					else {
						answer.append("\"");
						answer.append(resultSetToQuotableString(rs));
						answer.append("\"");
					}
				}
			}

			private void addInstanceDeclaration(Resource resource, NamedNode nn, StringBuilder answer) throws ExecutionException {
				answer.append(checkForKeyword(nn.getName()));
//				String query = "select ?type where {<" + nn.getURI() + ">  <" + ReasonerVocabulary.directRDFType + "> ?type}";
				String query = "select ?type where {<" + nn.getURI() + ">  <rdf:type> ?type}";
				Query q = new Query();
				q.setSparqlQueryString(query);
				OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND, q);
				ResultSet rs = runQuery(resource, q);
				OntModelProvider.clearPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND);
				if (rs != null) {
					answer.append(" is a ");
					rs.setShowNamespaces(false);
					int rowcnt = rs.getRowCount();
					if (rowcnt > 1) {
						answer.append("{");
					}
						for (int r = 0; r < rowcnt; r++) {
							Object typ = rs.getResultAt(r, 0);
							if (r > 1) {
								answer.append(" and ");
							}
							answer.append(checkForKeyword(typ.toString()));
						}
					if (rowcnt > 1) {
						answer.append("}");
					}
				}
				query = "select ?p ?v where {<" + nn.getURI() + "> ?p ?v }";
				q = new Query();
				q.setSparqlQueryString(query);
				OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND, q);
				rs = runQuery(resource, q);
				OntModelProvider.clearPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND);
				if (rs != null) {
					rs.setShowNamespaces(true);
					int rowcnt = rs.getRowCount();
					for (int r = 0; r < rowcnt; r++) {
						Object pobjURI = rs.getResultAt(r, 0);
						if (pobjURI.toString().equals(RDFS.comment.getURI()) || pobjURI.toString().equals(RDFS.label.getURI())) {
							if (pobjURI.toString().equals(RDFS.comment.getURI())) {
								answer.append(" (note \"");
							}
							else {
								answer.append(" (alias \"");
							}
							answer.append(rs.getResultAt(r, 1).toString().replaceAll("\"", "'"));
							answer.append("\")");
						}
					}
					int outputcnt = 0;
					for (int r = 0; r < rowcnt; r++) {
						Object pobjURI = rs.getResultAt(r, 0);
						if (!pobjURI.toString().startsWith(OWL.getURI()) && !pobjURI.toString().startsWith(RDFS.getURI()) &&
								!pobjURI.toString().startsWith(RDF.getURI())) {
							Object vobjURI = rs.getResultAt(r, 1);
							if (isBlankNode(vobjURI.toString())) {
								answer = addBlankNodeObject(resource, answer, vobjURI.toString());
							}
							else {
								rs.setShowNamespaces(false);
								Object pobj = rs.getResultAt(r, 0);
								Object vobj = rs.getResultAt(r, 1);
								if (outputcnt++ > 0) {
									answer.append(", with ");
								}
								else {
									answer.append(" with ");
								}
								answer.append(checkForKeyword(pobj.toString()));
								answer.append(" ");
								if (vobjURI.toString().startsWith(XSD.getURI())) {
									answer.append(vobj.toString());
								}
								else {
									answer.append(checkForKeyword(vobj.toString()));			                						
								}
							}
						}
						
					}
				}
			}

			private StringBuilder addBlankNodeObject(Resource resource, StringBuilder answer, String bNodeUri) throws ExecutionException {
				String query = "select ?t ?p ?v where {<" + bNodeUri + "> ?p ?v }";
				Query q = new Query();
				q.setSparqlQueryString(query);
				OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND, q);
				ResultSet rs = runQuery(resource, q);
				OntModelProvider.clearPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND);
				if (rs != null) {
					rs.setShowNamespaces(false);
					answer.append(rs.toStringWithIndent(5));
				}
				else {
					answer.append(bNodeUri);
				}
				return answer;
			}

			private boolean isBlankNode(String uri) {
				if (uri.startsWith("-")) {
					return true;
				}
				return false;
			}

			private void addAnnotationPropertyDeclaration(Resource resource, NamedNode nn, StringBuilder answer) {
				answer.append(checkForKeyword(nn.getName()));
				answer.append(" is a type of annotation");
			}

			private void addPropertyWithDomainAndRange(Resource resource, NamedNode nn, StringBuilder answer)
					throws ExecutionException {
				answer.append(checkForKeyword(nn.getName()));
				String query = "select ?d where {<" + nn.getURI() + "> <rdfs:domain> ?d}";
				Query q = new Query();
				q.setSparqlQueryString(query);
				OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND, q);
				ResultSet rs = runQuery(resource, q);
				OntModelProvider.clearPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND);
				boolean domainGiven = false;
				if (rs != null) {
					rs.setShowNamespaces(false);
					int rowcnt = rs.getRowCount();
					if (rowcnt > 0) {
						answer.append(" describes ");
						if (rowcnt > 1) answer.append("{");
						for (int r = 0; r < rowcnt; r++) {
							answer.append(checkForKeyword(rs.getResultAt(r, 0).toString()));
							domainGiven = true;
							if (rowcnt > 1 && r < rowcnt) answer.append(" and ");
						}
						if (rowcnt > 1) answer.append("}");
					}
				}

				if (!domainGiven) {
					answer.append(" is a property");
				}
				query = "select ?r where {<" + nn.getURI() + "> <rdfs:range> ?r}";
				q = new Query();
				q.setSparqlQueryString(query);
				OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND, q);
				rs = runQuery(resource, q);
				OntModelProvider.clearPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND);
				if (rs != null) {
					rs.setShowNamespaces(false);
					int rowcnt = rs.getRowCount();
					if (rowcnt > 0) {
						answer.append(" with values of type ");
						if (rowcnt > 1) answer.append("{");
						for (int r = 0; r < rowcnt; r++) {
							String val = rs.getResultAt(r, 0).toString();
							rs.setShowNamespaces(true);
							String pval = rs.getResultAt(r, 0).toString();
							rs.setShowNamespaces(false);
							if (pval.startsWith(XSD.getURI())) {
								answer.append(val);

							}
							else {
								answer.append(checkForKeyword(val));			                						
							}
							if (rowcnt > 1 && r < rowcnt) answer.append(" and ");
						}
						if (rowcnt > 1) answer.append("}");
					}
				}
			}

			private boolean addDomainAndRange(Resource resource, NamedNode nn, boolean isFirstProperty,
					StringBuilder answer) throws ExecutionException {
				OntModel m = OntModelProvider.find(resource);
				OntClass cls = m.getOntClass(nn.getURI());
				return getDomainAndRangeOfClass(m, cls, answer);
			}

			private boolean getDomainAndRangeOfClass(OntModel m, OntClass cls, StringBuilder answer) {
				boolean isFirstProperty = true;
				StmtIterator sitr = m.listStatements(null, RDFS.domain, cls);
				while (sitr.hasNext()) {
					com.hp.hpl.jena.rdf.model.Resource p = sitr.nextStatement().getSubject();
					answer.append("\n      ");
					answer.append("described by ");
					if (p != null) {
						answer.append(checkForKeyword(p.isURIResource() ? p.getLocalName() : p.toString()));
						StmtIterator ritr = m.listStatements(p, RDFS.range, (RDFNode)null);
						while (ritr.hasNext()) {
							RDFNode r = ritr.nextStatement().getObject();
							if (r != null) {
								answer.append(" with values of type ");
								if (r.isURIResource()) {
									if (r.asResource().getNameSpace().equals(XSD.getURI())) {
										answer.append(r.asResource().getLocalName());										
									}
									else {
										answer.append(checkForKeyword(r.asResource().getLocalName()));
									}
								}
								else {
									answer.append(r.asLiteral().getValue().toString());
								}
							}
						}
					}
					if (sitr.hasNext()) {
						isFirstProperty = false;
					}
				}
				ExtendedIterator<OntClass> spritr = cls.listSuperClasses(true);
				while (spritr.hasNext()) {
					OntClass spcls = spritr.next();
					getDomainAndRangeOfClass(m, spcls, answer);
				}
//				answer.append("\n");
				return isFirstProperty;
			}

			private StringBuilder getClassHierarchy(Resource resource, NamedNode nn, StringBuilder answer) throws ExecutionException {
				String query;
				Query q;
				ResultSet rs;
				query = "select ?typ where {<" + nn.getURI() + "> <rdfs:subClassOf> ?typ}";
				q = new Query();
				q.setSparqlQueryString(query);
				OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND, q);
				rs = runQuery(resource, q);
				OntModelProvider.clearPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND);
				if (rs != null) {
					rs.setShowNamespaces(false);
					answer.append(" is a type of ");
					if (rs.getRowCount() > 1) {
						answer.append("{");
					}
					for (int r = 0; r < rs.getRowCount(); r++) {
						Object rat = rs.getResultAt(r, 0);
						if (rat != null) {
							if (r > 0) {
								answer.append(" or ");
							}
							answer.append(rat.toString());
						}
					}
					if (rs.getRowCount() > 1) {
						answer.append("}");
					}
				}
				return answer;
			}

			private void addQualifiedCardinalityRestriction(Resource resource, NamedNode nn, boolean isFirstProperty,
					StringBuilder answer) throws ExecutionException {
				String query;
				Query q;
				ResultSet rs;
				query = "select ?p ?cn ?rc where {<" + nn.getURI() + "> <rdfs:subClassOf> ?r . ?r <rdf:type> <owl:Restriction> . ?r <owl:onProperty> ?p . ";
				query += "?r <owl:qualifiedCardinality> ?cn . ?r <owl:onClass> ?rc}";
				q = new Query();
				q.setSparqlQueryString(query);
				OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND, q);
				rs = runQuery(resource, q);
				OntModelProvider.clearPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND);
				if (rs != null) {
					rs.setShowNamespaces(false);
					int rowcnt = rs.getRowCount();
					if (rowcnt > 0) {
						if (!isFirstProperty) {
							answer.append(",\n");
						}
						for (int r = 0; r < rowcnt; r++) {
							answer.append("      ");
							answer.append("described by ");
							answer.append(checkForKeyword(rs.getResultAt(r, 0).toString()));
							answer.append(" with exactly ");
							answer.append(rs.getResultAt(r, 1).toString());
							answer.append(" values of type ");
							String val = rs.getResultAt(r, 2).toString();
							rs.setShowNamespaces(true);
							String pval = rs.getResultAt(r, 1).toString();
							rs.setShowNamespaces(false);
							if (pval.startsWith(XSD.getURI())) {
								answer.append(val);
							}
							else {
								answer.append(checkForKeyword(val));			                						
							}
						}
					}
				}
			}
	        
	        private String checkForKeyword(String word) {
	        	List<String> kwrds = getSadlKeywords();
	        	if (kwrds != null && kwrds.contains(word)) {
	        		return "^" + word;
	        	}
	        	return word;
	        }
	        
	    	private List<String> getSadlKeywords() {
	    		if (sadlkeywords == null) {
		    		Set<String> keywords = GrammarUtil.getAllKeywords(sadlParser.getGrammarAccess().getGrammar());
		    		if (keywords != null) {
		    			sadlkeywords = new ArrayList<String>();
		    			Iterator<String> itr = keywords.iterator();
		    			while (itr.hasNext()) {
		    				String token = itr.next();
		    				sadlkeywords.add(token);
		    			}
		    		}
	    		}
	    		return sadlkeywords;
	    	}
	    	
	        private ResultSet runQuery(Resource resource, Query q) throws ExecutionException {
        		RunDialogQuery rdq = new RunDialogQuery();
        		Object result = rdq.execute(handlerProvider, resource, q);
        		if (result instanceof SadlCommandResult) {
        			Object subresults = ((SadlCommandResult)result).getResults();
        			if (subresults instanceof ResultSet) {
        				return (ResultSet)subresults;
        			}
        		}
        		return null;
	        }

			private ResultSet[] insertTriplesAndQuery(Resource resource, TripleElement[] triples) throws ExecutionException {
        		RunDialogQuery rdq = rdqProvider.get();  //new RunDialogQuery();
        		Object result = rdq.execute(handlerProvider, resource, triples);
        		if (result instanceof ResultSet[]) {
        			return (ResultSet[])result;
        		}
        		else {
        			System.err.println("Unexpected return type in insertTriplesAndQuery");
        		}
        		return null;
			}

			private Resource getResourceFromDocument(XtextDocument document) {
				Class<?> c = document.getClass();

			    Field resfield;
				try {
					resfield = c.getDeclaredField("resource");
					resfield.setAccessible(true);
					return (Resource) resfield.get(document);
				} catch (NoSuchFieldException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (SecurityException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalArgumentException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IllegalAccessException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				return null;
			}

			private OntModel getTheModel() {
				if (theModel == null) {
					setTheModel(OntModelProvider.find(getResource()));
				}
				return theModel;
			}

			private void setTheModel(OntModel theModel) {
				this.theModel = theModel;
			}

		};

	    
	    acceptor.accept(strategy, IDocument.DEFAULT_CONTENT_TYPE);

	    super.configure(acceptor);

	}

	private String setDialogAnswerProvider(Resource resource) throws ConfigurationException {
		String modelFolder = SadlActionHandler.getModelFolderFromResource(resource);
		ConfigurationManagerForIDE cfgmgr = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(modelFolder, null);
		setConfigMgr(cfgmgr);
		cfgmgr.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, this);
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
			}
			else {
				modContent = "\"" + content.replaceAll("\"", "'") + "\"" + ".";
			}
		}
		else {
			modContent = content;
		}
		if (prependCM) {
			modContent = "CM: " + modContent;
		}
		return modContent;
	}

	private XtextDocument getTheDocument() {
		return theDocument;
	}

	private void setTheDocument(XtextDocument theDocument) {
		this.theDocument = theDocument;
	}
	
	public Object[] getSourceText(EObject po) {
		INode node = getParserObjectNode(po);
		if (node != null) {
			String txt = NodeModelUtils.getTokenText(node);
			int start = NodeModelUtils.getNode(po).getTotalOffset();
			int len = NodeModelUtils.getNode(po).getTotalLength();
			Object[] ret = new Object[3];
			ret[0] = txt.trim();
			ret[1] = start;
			ret[2] = len;
			return ret;
		}
		return null;
	}

	public int[] getStartAndLength(EObject po) {
		INode node = getParserObjectNode(po);
		if (node != null) {
			ITextRegionWithLineInformation reg = NodeModelUtils.getNode(po).getTextRegionWithLineInformation();
			int start = NodeModelUtils.getNode(po).getTotalOffset();
			int len = NodeModelUtils.getNode(po).getTotalLength();
			int[] ret = new int[2];
			ret[0] = start;
			ret[1] = len;
			return ret;
		}
		return null;
	}

	protected INode getParserObjectNode(EObject po) {
		Object r = po.eResource();
		if (r instanceof XtextResource) {
			INode root = ((XtextResource) r).getParseResult().getRootNode();
	        for(INode node : root.getAsTreeIterable()) {   
	        	if (node instanceof CompositeNodeWithSemanticElement) {
	        		EObject semElt = ((CompositeNodeWithSemanticElement)node).getSemanticElement();
	        		if (semElt != null && semElt.equals(po)) {
	        			// this is the one!
       					return node;
	        		}
	        	}
	        }
		}
		org.eclipse.emf.common.util.TreeIterator<EObject> titr = po.eAllContents();
		while (titr.hasNext()) {
			EObject el = titr.next();
//TODO what's supposed to happen here?
			int i = 0;
		}
		return null;
	}
	
	public synchronized void addCurationManagerContentToDialog(IDocument document, IRegion reg, String content, Object ctx, boolean quote)
			throws BadLocationException {
		String modContent;
		if (quote) {
			modContent = generateDoubleQuotedContentForDialog(content);
		}
		else {
			modContent = content.startsWith("CM:") ? content : ("CM: " + content);
			if (!modContent.trim().endsWith(".") && !modContent.trim().endsWith("?")) {
				modContent += ".";
			}
		}
		if (ctx instanceof EObject && getSourceText((EObject)ctx) != null) {
//			String damageStr = document.get(reg.getOffset(), reg.getLength());
			Object[] srcinfo = getSourceText((EObject)ctx);
			String srctext = (String) srcinfo[0];
			int start = (int) srcinfo[1];
			int len = (int) srcinfo[2];
			//find location of this in document
		    document.replace(start + len + 1, 0, modContent + "\n");
		}
		else {
			int loc = document.getLength();
//			document.replace(loc, 0, modContent + "\n");
			document.set(document.get() + modContent + "\n");
		}
		logger.debug("Adding to Dialog editor: " + modContent);
	}
	
	public String addCurationManagerInitiatedContent(String content) {
        Consumer<MixedInitiativeElement> respond = a -> this.provideResponse(a);
        MixedInitiativeTextualResponse question = new MixedInitiativeTextualResponse(content);
        MixedInitiativeElement questionElement = new MixedInitiativeElement(question, respond);
		addMixedInitiativeElement(content, questionElement);
        initiateMixedInitiativeInteraction(questionElement);
		return "success";
	}

	@Override
	public String addCurationManagerInitiatedContent(AnswerCurationManager acm, String methodToCall,
			List<Object> args, String content) {
        Consumer<MixedInitiativeElement> respond = a -> this.provideResponse(a);
        MixedInitiativeTextualResponse question = new MixedInitiativeTextualResponse(content);
        MixedInitiativeElement questionElement = new MixedInitiativeElement(question, respond, acm, methodToCall, args);
		addMixedInitiativeElement(content, questionElement);
        initiateMixedInitiativeInteraction(questionElement);
		return "success";
	}
	private void addMixedInitiativeElement(String key, MixedInitiativeElement element) {
		if (key.endsWith(".") || key.endsWith("?")) {
			// drop EOS
			key = key.substring(0, key.length() - 1);
		}
		mixedInitiativeElements.put(key, element);
	}
	
	@Override
	public boolean removeMixedInitiativeElement(String key) {
		mixedInitiativeElements.remove(key);
		return true;
	}
	
	@Override
	public MixedInitiativeElement getMixedInitiativeElement(String key) {
		return mixedInitiativeElements.get(key);
	}

	@Override
	public String initiateMixedInitiativeInteraction(MixedInitiativeElement element) {
		String content = element.getContent().toString();
		if (getTheDocument() != null) {
			try {
				addCurationManagerContentToDialog(getTheDocument(), null, content, null, true);
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	@Override
	public void provideResponse(MixedInitiativeElement response) {
		if (response.getCurationManager() != null) {
			AnswerCurationManager acm = response.getCurationManager();
			String methodToCall = response.getMethodToCall();
			Method[] methods = acm.getClass().getMethods();
			for (Method m : methods) {
				if (m.getName().equals(methodToCall)) {
					// call the method
					List<Object> args = response.getArguments();
					try {
						Object results = null;
						if (args.size() == 0) {
							results = m.invoke(acm, null);
						}
						else {
							Object arg0 = args.get(0);
							if (args.size() == 1) {
								results = m.invoke(acm, arg0);
							}
							else {
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
															String altUrl = new SadlUtils().fileNameToFileUrl(outputOwlFileName);
															
															String publicUri = acm.getDomainModelConfigurationManager().getPublicUriFromActualUrl(altUrl);
															if (publicUri != null) {
																acm.getDomainModelConfigurationManager().deleteModel(publicUri);
																acm.getDomainModelConfigurationManager().deleteMapping(altUrl, publicUri);
															}
														} catch (ConfigurationException e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														} catch (URISyntaxException e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														} catch (IOException e) {
															// TODO Auto-generated catch block
															e.printStackTrace();
														}
													}
													projectName = sf.getParentFile().getParentFile().getName();
												}
											}				
											else {
												// add import of OWL file from policy file since there won't be a SADL file to build an OWL and create mappings.
												try {
													String importActualUrl = new SadlUtils().fileNameToFileUrl(arg0.toString());
													String altUrl = new SadlUtils().fileNameToFileUrl(importActualUrl);
													String importPublicUri = acm.getDomainModelConfigurationManager().getPublicUriFromActualUrl(altUrl);
													String prefix = acm.getDomainModelConfigurationManager().getGlobalPrefix(importPublicUri);
													acm.getDomainModelConfigurationManager().addMapping(importActualUrl, importPublicUri, prefix, false, "AnswerCurationManager");
													String prjname = owlfile.getParentFile().getParentFile().getName();
													IProject prj = ResourcesPlugin.getPlugin().getWorkspace().getRoot().getProject(prjname);
													prj.refreshLocal(IResource.DEPTH_INFINITE, null);
												} catch (IOException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												} catch (URISyntaxException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												} catch (ConfigurationException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												} catch (CoreException e) {
													// TODO Auto-generated catch block
													e.printStackTrace();
												}
											}
										}
										if (projectName != null) {	// only not null if doing SADL conversion
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
								}
							}
						}
//						System.out.println(result.toString());
					} catch (IllegalAccessException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (IllegalArgumentException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (InvocationTargetException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					break;
				}
			}
		}	
	}

	private void insertExtractedModelImport(XtextDocument doc, AnswerCurationManager acm) {
		String cmn = acm.getExtractionProcessor().getCodeModelName();
		String insert = "import \"" + cmn + "\".\n\r";
		if (cmn != null) {
			doc.readOnly(new IUnitOfWork.Void<XtextResource>() {

				@Override
				public void process(XtextResource rsrc) throws Exception {
					// TODO Auto-generated method stub
					EList<EObject> cntnts = rsrc.getContents();
					if (cntnts.get(0) instanceof SadlModel) {
						int loc = -1;
						SadlModel sm = (SadlModel) cntnts.get(0);
						EList<SadlImport> imprts = sm.getImports();
						if (imprts != null) {
							// insert import after last import
							int[] info = getStartAndLength(imprts.get(imprts.size() - 1));
							loc = info[0] + info[1];
						}
						else {
							// insert import after uri
							EList<SadlModelElement> elements = sm.getElements();
							if (elements != null) {
								int[] info = getStartAndLength(elements.get(elements.size() - 1));
								loc = info[0] + info[1];
							}
							else {
								loc = doc.getLength();
							}
						}
						if (loc > 0) {
							// insert at loc
							try {
								doc.replace(loc + 1, insert.length(), insert);
							} catch (BadLocationException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
					}
					
				}
			});
		}
	}

	private void displayFiles(String codeModelFolder, List<File> sadlFiles) {
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
								prjFound = true;;
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
			            		    IDE.openEditorOnFileStore( page, fileStore );
			            		    page.bringToTop(actpart);
				    		    } catch ( PartInitException e ) {
				    		        System.err.println("Unable to open '" + location.lastSegment() + "' in an editor.");
				    		    }
		    	    		}
		    	    	}
		    	    }
		    	    else {
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
			    		            		    IDE.openEditorOnFileStore( page, fileStore );
			    		            		    page.bringToTop(actpart);
			    			    		    } catch ( PartInitException e ) {
			    			    		        System.err.println("Unable to open '" + location.lastSegment() + "' in an editor.");
			    			    		    }
		    		    	    		}
		    		    	    	}
		    		    	    }
		    	    	    }
		    	    	});
		    	    }
				} catch (IOException e2) {
					// TODO Auto-generated catch block
					e2.printStackTrace();
				}
	 
	    	} catch (CoreException e) {
//	    		IStatus coreStatus = e.getStatus();
//	    		String newMessage = NLS.bind(DataTransferMessages.ImportOperation_coreImportError, sf, coreStatus.getMessage());
//	    		IStatus status = new Status(coreStatus.getSeverity(), coreStatus
//	    				.getPlugin(), coreStatus.getCode(), newMessage, null);
	    		e.printStackTrace();
	    	}
		}
	}

	private IConfigurationManagerForIDE getConfigMgr() {
		return configMgr;
	}

	private void setConfigMgr(IConfigurationManagerForIDE configMgr) {
		this.configMgr = configMgr;
	}

	protected Map<String, String> getPreferences(IFile file) {
		final URI uri = URI.createPlatformResourceURI(file.getFullPath().toString(), true);
		return getPreferences(uri);
	}
	
	protected Map<String, String> getPreferences(URI uri) {
		Injector reqInjector = safeGetInjector(DialogActivator.COM_GE_RESEARCH_SADL_DARPA_ASKE_DIALOG);
		IPreferenceValuesProvider pvp = reqInjector.getInstance(IPreferenceValuesProvider.class);
		IPreferenceValues preferenceValues = pvp.getPreferenceValues(new XtextResource(uri));
		if (preferenceValues != null) {
			Map<String, String> map = new HashMap<String, String>();
			String tsburl = preferenceValues.getPreference(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI);
			if (tsburl != null) {
				map.put(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getId(), tsburl);
			}
			String cgsburl = preferenceValues.getPreference(DialogPreferences.ANSWER_CG_SERVICE_BASE_URI);
			if (cgsburl != null) {
				map.put(DialogPreferences.ANSWER_CG_SERVICE_BASE_URI.getId(), cgsburl);
			}
			String j2psburl = preferenceValues.getPreference(DialogPreferences.ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI);
			if (j2psburl != null) {
				map.put(DialogPreferences.ANSWER_JAVA_TO_PYTHON_SERVICE_BASE_URI.getId(), j2psburl);
			}
//			preferenceValues.getPreference(DialogPreferences.)
			return map;
		}
		return null;
	}

	protected final Injector safeGetInjector(String name){
		final AtomicReference<Injector> i = new AtomicReference<Injector>();
		Display.getDefault().syncExec(new Runnable(){
			@Override
			public void run() {
				i.set(DialogActivator.getInstance().getInjector(name));
			}
		});
		
		return i.get();
	}

	private Resource getResource() {
		return resource;
	}

	private void setResource(Resource resource) {
		this.resource = resource;
	}

}

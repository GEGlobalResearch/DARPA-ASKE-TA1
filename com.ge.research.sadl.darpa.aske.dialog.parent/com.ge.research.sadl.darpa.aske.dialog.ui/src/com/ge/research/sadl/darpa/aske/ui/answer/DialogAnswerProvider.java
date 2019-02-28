package com.ge.research.sadl.darpa.aske.ui.answer;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ServiceLoader;
import java.util.Set;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.DocumentCommand;
import org.eclipse.jface.text.IAutoEditStrategy;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.xtext.GrammarUtil;
import org.eclipse.xtext.nodemodel.INode;
import org.eclipse.xtext.nodemodel.impl.CompositeNodeWithSemanticElement;
import org.eclipse.xtext.nodemodel.util.NodeModelUtils;
import org.eclipse.xtext.resource.XtextResource;
import org.eclipse.xtext.ui.editor.autoedit.DefaultAutoEditStrategyProvider;
import org.eclipse.xtext.ui.editor.model.XtextDocument;
import org.eclipse.xtext.ui.resource.IResourceSetProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.SADLStandaloneSetup;
import com.ge.research.sadl.builder.ConfigurationManagerForIDE;
import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.HowManyValuesConstruct;
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
import com.ge.research.sadl.parser.antlr.SADLParser;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.SadlCommandResult;
import com.ge.research.sadl.ui.handlers.SadlActionHandler;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Provider;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.StmtIterator;
import com.hp.hpl.jena.reasoner.rulesys.Builtin;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;
import com.hp.hpl.jena.vocabulary.OWL;
import com.hp.hpl.jena.vocabulary.RDF;
import com.hp.hpl.jena.vocabulary.RDFS;
import com.hp.hpl.jena.vocabulary.XSD;

public class DialogAnswerProvider extends DefaultAutoEditStrategyProvider {
	private static final Logger logger = LoggerFactory.getLogger(DialogAnswerProvider.class);
	private XtextDocument theDocument;

	@Inject
	protected IResourceSetProvider resourceSetProvider;

//	@Inject
//	protected Provider<SadlRunQueryHandler> handlerProvider;

	@Inject
	protected Provider<DialogRunInferenceHandler> handlerProvider;		// Provider will give new instance
	
	@Inject
	protected Provider<RunDialogQuery> rdqProvider;

	@Override
	protected void configure(IEditStrategyAcceptor acceptor) {

	    IAutoEditStrategy strategy = new IAutoEditStrategy() 
	    {

	        private List<String> sadlkeywords = null;

			@Override
	        public void customizeDocumentCommand(IDocument document, DocumentCommand command) 
	        {
//	            if ( command.text.length() == 0 || command.text.charAt(0) > ' ') return;

	            IRegion reg = ((XtextDocument) document).getLastDamage();

	            try {
	            	ServiceLoader<Builtin> itr = ServiceLoader.load(Builtin.class);
	            	Iterator<Builtin> itrr = itr.iterator();
	            	while (itrr.hasNext()) {
	            		System.out.println(itrr.next().getClass().getCanonicalName());
	            	}
	                if (document instanceof XtextDocument) {
	                	Resource resource = getResourceFromDocument((XtextDocument)document);
	                	String tempInsert = null;
	                	if (resource != null) {
	                		setTheDocument((XtextDocument) document);
	                		String modelFolder = SadlActionHandler.getModelFolderFromResource(resource);
	                		ConfigurationManagerForIDE cfgmgr = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(modelFolder, null);
	                		cfgmgr.addPrivateKeyValuePair(DialogConstants.DIALOG_ANSWER_PROVIDER, this);
//	                		OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.DIALOG_ANSWER_PROVIDER, this);
			                Object lastcmd = OntModelProvider.getPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND);
			                logger.debug("DialogAnswerProvider: Last cmd: " + (lastcmd != null ? lastcmd.toString() : "null"));
		                	if (lastcmd instanceof Query) {
		                		StringBuilder answer = new StringBuilder("CM: ");
		                		ResultSet rs = runQuery(resource, (Query)lastcmd);
		                		String resultStr = null;
		                		if (rs != null) {
		                			resultStr = resultSetToQuotableString(rs);
		                		}
		                		tempInsert = (resultStr != null ? resultStr : "\"Failed to find results\"");
		                		answer.append(tempInsert);
		                		answer.append(".");
		                		Object ctx = ((Query)lastcmd).getContext();
		                		addResponseToDialog(document, reg, answer, ctx);
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
				                		StringBuilder answer = new StringBuilder("CM: ");
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
				                		tempInsert = (resultStr != null ? resultStr : "\"Failed to find results\"");
				                		answer.append(tempInsert);
				                		answer.append(".");
				                		addResponseToDialog(document, reg, answer, ctx);
			                		}
		                		}
		                		else {
		                			int tripleCnt = 0;
		                			if (trgt instanceof TripleElement) {
		                				tripleCnt++;
		                			}
		                			if (whn instanceof TripleElement) {
		                				tripleCnt++;
		                			}
	                				TripleElement[] triples = new TripleElement[tripleCnt];
	                				if (trgt instanceof TripleElement) {
		                				triples[0] = (TripleElement)trgt;
	                				}
		                			if (whn instanceof TripleElement) {
		                				// we have a when statement
		                				triples[1] = (TripleElement)whn;
		                			}
					                OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND, triples);
			                		StringBuilder answer = new StringBuilder("CM: ");
			                		ResultSet[] rss = insertTriplesAndQuery(resource, triples);
			                		if (rss != null) {
			                			int cntr = 0;
				                		for (ResultSet rs : rss) {
				                			if (cntr++ > 0) {
				                				answer.append(",\n");
				                			}
				                			answer.append(resultSetToQuotableString(rs));
				                		}
				                		answer.append(".\n");
			                		}
			                		Object ctx = triples[0].getContext();
			                		addResponseToDialog(document, reg, answer, ctx);
//		                			}
//		                			else {
////		                				rdqProvider.get().execute(null, null, null);
//		                				System.out.println("Target is: " + trgt.toString());
//		                				System.out.println("When is: " + whn.toString());
//		                			}
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
		                		StringBuilder answer = new StringBuilder("CM: ");
		                		addTripleQuestion(resource, (TripleElement)lastcmd, answer);
		                		answer.append(".");
		                		Object ctx = ((TripleElement)lastcmd).getContext();
		                		addResponseToDialog(document, reg, answer, ctx);
		                	}
		                	else if (lastcmd != null) {
	                			logger.debug("    Lastcmd '" + lastcmd.getClass().getCanonicalName() + "' not handled yet!");
		                	}
	                	}
	                	else {
                			logger.debug("DialogAnswerProvider called with null resource!");
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

			private void whatIsNamedNode(IDocument document, IRegion reg, Resource resource, Object lastcmd)
					throws ExecutionException, BadLocationException {
				// what is NamedNode?
				NamedNode nn = (NamedNode) lastcmd;
				NodeType typ = nn.getNodeType();
				boolean isFirstProperty = true;
				StringBuilder answer = new StringBuilder("CM: ");
				if (typ.equals(NodeType.ClassNode)) {
					answer.append(checkForKeyword(nn.getName()));
					answer.append(" is a class");
					isFirstProperty = addDomainAndRange(resource, nn, isFirstProperty, answer);
					addQualifiedCardinalityRestriction(resource, nn, isFirstProperty, answer);		
					answer.append(".");
					Object ctx = ((NamedNode)lastcmd).getContext();
					addResponseToDialog(document, reg, answer, ctx);
				}
				else if (typ.equals(NodeType.ObjectProperty) || typ.equals(NodeType.DataTypeProperty) || typ.equals(NodeType.PropertyNode)) {
					addPropertyWithDomainAndRange(resource, nn, answer);
					answer.append(".");
					Object ctx = ((NamedNode)lastcmd).getContext();
					addResponseToDialog(document, reg, answer, ctx);
				}
				else if (typ.equals(NodeType.AnnotationProperty)) {
					addAnnotationPropertyDeclaration(resource, nn, answer);
					answer.append(".");
					Object ctx = ((NamedNode)lastcmd).getContext();
					addResponseToDialog(document, reg, answer, ctx);
				}
				else if (typ.equals(NodeType.InstanceNode)) {
					addInstanceDeclaration(resource, nn, answer);
					answer.append(".");
					Object ctx = ((NamedNode)lastcmd).getContext();
				    addResponseToDialog(document, reg, answer, ctx);
				}
				else {
					logger.debug("    Lastcmd '" + lastcmd.getClass().getCanonicalName() + "' not handled yet!");
				}
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
				answer.append(" is a ");
//				String query = "select ?type where {<" + nn.getURI() + ">  <" + ReasonerVocabulary.directRDFType + "> ?type}";
				String query = "select ?type where {<" + nn.getURI() + ">  <rdf:type> ?type}";
				Query q = new Query();
				q.setSparqlQueryString(query);
				OntModelProvider.addPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND, q);
				ResultSet rs = runQuery(resource, q);
				OntModelProvider.clearPrivateKeyValuePair(resource, DialogConstants.LAST_DIALOG_COMMAND);
				if (rs != null) {
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
					int rowcnt = rs.getRowCount();
					int outputcnt = 0;
					for (int r = 0; r < rowcnt; r++) {
						rs.setShowNamespaces(true);
						Object pobjURI = rs.getResultAt(r, 0);
						if (!pobjURI.toString().startsWith(OWL.getURI()) && !pobjURI.toString().startsWith(RDFS.getURI()) &&
								!pobjURI.toString().startsWith(RDF.getURI())) {
							Object vobjURI = rs.getResultAt(r, 1);
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

			private void addResponseToDialog(IDocument document, IRegion reg, StringBuilder answer, Object ctx)
					throws BadLocationException {
				if (ctx instanceof EObject) {
					String damageStr = document.get(reg.getOffset(), reg.getLength());
					Object[] srcinfo = getSourceText((EObject)ctx);
					String srctext = (String) srcinfo[0];
					int start = (int) srcinfo[1];
					int len = (int) srcinfo[2];
					//find location of this in document
				    document.replace(start + len + 1, 0, answer.toString() + "\n");
				}
				else {
					logger.debug(answer.toString());
				}
			}
			
			private void addCurationManagerInitiatedContent(String content) throws BadLocationException {
				XtextDocument document = getTheDocument();
				int len = document.getLength();
				document.replace(len, 0, "\n\n" + content);
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
		    		SADLParser sparser = null;
		    		Injector injector = new SADLStandaloneSetup().createInjectorAndDoEMFRegistration();
		    	    sparser = injector.getInstance(SADLParser.class);
		    		if (sparser != null) {
		    			Set<String> keywords = GrammarUtil.getAllKeywords(sparser.getGrammarAccess().getGrammar());
		    			if (keywords != null) {
		    				sadlkeywords = new ArrayList<String>();
		    				Iterator<String> itr = keywords.iterator();
		    				while (itr.hasNext()) {
		    					String token = itr.next();
		    					sadlkeywords.add(token);
		    				}
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

		};

	    
	    acceptor.accept(strategy, IDocument.DEFAULT_CONTENT_TYPE);

	    super.configure(acceptor);

	}

	private XtextDocument getTheDocument() {
		return theDocument;
	}

	private void setTheDocument(XtextDocument theDocument) {
		this.theDocument = theDocument;
	}
}

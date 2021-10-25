/********************************************************************** 
 * Note: This license has also been called the "New BSD License" or 
 * "Modified BSD License". See also the 2-clause BSD License.
 *
 * Copyright � 2018-2019 - General Electric Company, All Rights Reserved
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
package com.ge.research.sadl.darpa.aske.processing.imports;

import static com.github.javaparser.utils.PositionUtils.sortByBeginPosition;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;

import org.apache.log4j.Logger;

import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.jena.JenaProcessorException;
//import com.ge.research.sadl.jena.inference.SadlJenaModelGetterPutter;
import com.ge.research.sadl.processing.SadlConstants;
import com.ge.research.sadl.reasoner.AmbiguousNameException;
import com.ge.research.sadl.reasoner.CircularDependencyException;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.IConfigurationManagerForEditing.Scope;
import com.ge.research.sadl.reasoner.IReasoner;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.TranslationException;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.github.javaparser.JavaParser;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.ImportDeclaration;
import com.github.javaparser.ast.Modifier;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.ConstructorDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.comments.JavadocComment;
import com.github.javaparser.ast.comments.LineComment;
import com.github.javaparser.ast.expr.ArrayAccessExpr;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.BinaryExpr.Operator;
import com.github.javaparser.ast.expr.BooleanLiteralExpr;
import com.github.javaparser.ast.expr.DoubleLiteralExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.FieldAccessExpr;
import com.github.javaparser.ast.expr.IntegerLiteralExpr;
import com.github.javaparser.ast.expr.LiteralExpr;
import com.github.javaparser.ast.expr.LongLiteralExpr;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.ObjectCreationExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.ForStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.ast.stmt.WhileStmt;
import com.github.javaparser.ast.type.ClassOrInterfaceType;
import com.github.javaparser.ast.type.Type;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntDocumentManager;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.Ontology;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;
import org.apache.jena.util.iterator.ExtendedIterator;
import org.apache.jena.vocabulary.RDFS;
import org.apache.jena.vocabulary.XSD;

public class JavaModelExtractorJP implements IModelFromCodeExtractor {
    private static final Logger logger = Logger.getLogger (JavaModelExtractorJP.class) ;
	private String packageName = "";
	private AnswerCurationManager curationMgr = null;
	
	public enum USAGE {Defined, Used, Reassigned}
	public enum InputOutput {Input, Output}
	private Map<String, String> preferences;
	private List<File> codeFiles;
	
	// Assumption: the code meta model and the code model extracted are in the same codeModelFolder (same kbase)
	private IConfigurationManagerForIDE codeModelConfigMgr;	// the ConfigurationManager used to access the code extraction model
	private String codeMetaModelUri;	// the name of the code extraction metamodel
	private String codeMetaModelPrefix;	// the prefix of the code extraction metamodel
	private OntModel codeModel;
	private Map<String,OntModel> codeModels = null;
	private String codeModelName;	// the name of the model  being created by extraction
	private String codeModelPrefix; // the prefix of the model being created by extraction

	private Individual rootContainingInstance = null;
	private boolean includeSerialization = false;	// if this is false, the scripts will not be included 
													// in the extraction code model but will pulled from the code by location
	private Map<Range, MethodCallMapping> postProcessingList = new HashMap<Range, MethodCallMapping>(); // key is the MethodCallExpr
	Map<String, String> classNameMap = new HashMap<String, String>();
																						// value is the calling method instance
	private Map<Node,Individual> methodsFound = new HashMap<Node, Individual>();
	private Individual methodWithBodyInProcess = null;
	private Map<Individual, LiteralExpr> potentialConstants = new HashMap<Individual, LiteralExpr>();
	private List<Individual> discountedPotentialConstants = new ArrayList<Individual>();
	private List<String> classesToIgnore = new ArrayList<String>();
	private List<String> primitiveTypesList = null;
	private HashMap<String, String> methodJavadoc = new HashMap<String, String>();
	private StringBuilder aggregatedComments = new StringBuilder();
	private List<Node> nodesWithCommentInvestigated = new ArrayList<Node>();
	private HashMap<String, String> returnStatementComments = new HashMap<String, String>();
	
	public class MethodCallMapping {
		private Node methodCallNode;
		private Individual callingInstance;
		
		public MethodCallMapping(Node n, Individual i) {
			setMethodCallNode(n);
			setCallingInstance(i);
		}
		
		Node getMethodCallNode() {
			return methodCallNode;
		}
		
		void setMethodCallNode(Node methodCallNode) {
			this.methodCallNode = methodCallNode;
		}
		
		Individual getCallingInstance() {
			return callingInstance;
		}
		
		void setCallingInstance(Individual callingInstance) {
			this.callingInstance = callingInstance;
		}
		
		public String toString() {
			return getCallingInstance().getLocalName() + " calls " + getMethodCallNode().toString();
		}
		
	}
	
	public JavaModelExtractorJP(AnswerCurationManager acm, Map<String, String> preferences) {
		setCurationMgr(acm);
		this.setPreferences(preferences);
//	    logger.setLevel(Level.ALL);
	}
	
	private void initializeContent(String modelName, String modelPrefix) {
		packageName = "";
		postProcessingList.clear();
		if (getCodeModelName() == null) {	// don't override a preset model name
			setCodeModelName(modelName);
		}
		if (getCodeModelPrefix() == null) {
			setCodeModelPrefix(modelPrefix);	// don't override a preset model name		
		}
		codeModel = null;
		rootContainingInstance = null;
		postProcessingList.clear();
		classNameMap.clear();
		methodWithBodyInProcess = null;
		potentialConstants.clear();
		discountedPotentialConstants.clear();
		nodesWithCommentInvestigated.clear();
		methodJavadoc.clear();
		clearAggregatedComments();

	}

	public boolean process(String inputIdentifier, String content, String modelName, String modelPrefix) throws ConfigurationException, IOException, TranslationException {
	    initializeContent(modelName, modelPrefix);
		String defName = getCodeModelName() + "_comments";
		getCurationMgr().getTextProcessor().setTextModelName(defName);
		String defPrefix = getCodeModelPrefix() + "_cmnt";
		getCurationMgr().getTextProcessor().setTextModelPrefix(defPrefix);
		parse(inputIdentifier, getCurationMgr().getOwlModelsFolder(), content);
		try {
			addConstants();
		} catch (TranslationException e) {
			throw new IOException(e.getMessage(), e);
		}
		return true;
	}
	
	private void addConstants() throws TranslationException {
		if (potentialConstants.size() > 0) {
			if (discountedPotentialConstants.size() > 0) {
				for (Individual inst : discountedPotentialConstants) {
					potentialConstants.remove(inst);
				}
			}
			logger.debug("Constants:");
			if (potentialConstants.size() > 0) {
				for (Individual inst : potentialConstants.keySet()) {
					LiteralExpr value = potentialConstants.get(inst);
					logger.debug("   " + inst.getLocalName() + "=" + value.toString());
					Literal lval;
					if (value instanceof IntegerLiteralExpr) {
						lval = SadlUtils.getLiteralMatchingDataPropertyRange(getCurrentCodeModel(),XSD.xint.getURI(), value.toString());
					}
					else if (value instanceof DoubleLiteralExpr) {
						lval = SadlUtils.getLiteralMatchingDataPropertyRange(getCurrentCodeModel(),XSD.xdouble.getURI(), value.toString());
					}
					else if (value instanceof LongLiteralExpr) {
						lval = SadlUtils.getLiteralMatchingDataPropertyRange(getCurrentCodeModel(),XSD.xlong.getURI(), value.toString());
					}
					else if (value instanceof BooleanLiteralExpr) {
						lval = SadlUtils.getLiteralMatchingDataPropertyRange(getCurrentCodeModel(),XSD.xboolean.getURI(), value.toString());
					}
					else {
						lval = SadlUtils.getLiteralMatchingDataPropertyRange(getCurrentCodeModel(),XSD.xstring.getURI(), value.toString());
					}
					inst.addRDFType(getConstantVariableClass());
					Individual uq = createUnittedQuantity(lval, null);
					inst.addProperty(getConstantValueProperty(), uq);
				}
			}
		}
	}

	private Individual createUnittedQuantity(Literal lval, String unit) {
		OntClass uqcls = getUnittedQuantityClass();
		Individual uqinst = getCurrentCodeModel().createIndividual(uqcls);
		uqinst.addProperty(getUnittedQuantityValueProperty(), lval);
		if (unit != null) {
			uqinst.addProperty(getUnittedQuantityUnitProperty(), unit);
		}
		return uqinst;
	}

	//use ASTParse to parse string
	private void parse(String inputIdentifier, String modelFolder, String javaCodeContent) throws IOException, ConfigurationException, TranslationException {
		try {
			String source = null;
			if (inputIdentifier.lastIndexOf('/') > 0) {
				source = inputIdentifier.substring(inputIdentifier.lastIndexOf('/') + 1);
			}
			else if (inputIdentifier.lastIndexOf('\\') > 0) {
				source = inputIdentifier.substring(inputIdentifier.lastIndexOf('\\') + 1);
			}
			else {
				source = inputIdentifier;
			}
			String msg = "Parsing code file '" + source + "'.";
			getCurationMgr().notifyUser(modelFolder, msg, true);
		} catch (ConfigurationException e) {
			e.printStackTrace();
		}
		if (logger.isDebugEnabled()) {
			logger.debug("***************** code to process ******************");
			logger.debug(javaCodeContent);
			logger.debug("****************************************************");
		}
				
        // Set up a minimal type solver that only looks at the classes used to run this sample.
        CombinedTypeSolver combinedTypeSolver = new CombinedTypeSolver();
        combinedTypeSolver.add(new ReflectionTypeSolver());

        // Configure JavaParser to use type resolution
        JavaSymbolSolver symbolSolver = new JavaSymbolSolver(combinedTypeSolver);
        JavaParser.getStaticConfiguration().setSymbolResolver(symbolSolver);

        // Parse some code
        CompilationUnit cu = JavaParser.parse(javaCodeContent);
        Optional<PackageDeclaration> pkg = cu.getPackageDeclaration();
        if (pkg.isPresent()) {
        	setPackageName(pkg.get().getNameAsString());
        	String rootClassName = getRootClassName(cu);
        	setCodeModelName(getPackageName() + "/" + rootClassName);
        	setCodeModelPrefix(derivePrefixFromPackage(getCodeModelName()));
        }
        initializeCodeModel(getCurationMgr().getOwlModelsFolder());
        findAllClassesToIgnore(cu);
        processBlock(cu, null);
        postProcess();
	}

	private void postProcess() {
		Iterator<Range> nitr = postProcessingList.keySet().iterator();
//		Iterator<Node> nitr = postProcessingList.keySet().iterator();
		while (nitr.hasNext()) {
			Range rng = nitr.next();
			MethodCallMapping mcm = postProcessingList.get(rng);
			Node node = mcm.getMethodCallNode();
			Individual callingMethod = mcm.getCallingInstance();
			if (node instanceof MethodCallExpr && ignoreMethodCall((MethodCallExpr) node, callingMethod)) {
				continue;
			}
			Node containerNode = null;
			if (node instanceof MethodCallExpr) {
				// node is the MethodCallExpr
				Optional<Node> optContainer = ((MethodCallExpr)node).getParentNode();
				if (optContainer.isPresent()) {
					containerNode = optContainer.get();
				}
				NodeList<Expression> args = ((MethodCallExpr)node).getArguments();
				String methName = ((MethodCallExpr)node).getNameAsString();
				Individual methodCalled = findMethodCalled((MethodCallExpr)node);
				// create instance of MethodCall that will contain details of this call
				Individual methodCall = getCurrentCodeModel().createIndividual(getMethodCallClass());
//				methodCalled.addProperty(getCallsProperty(), methodCall);
//				methodCall.addProperty(getCallsProperty(), methodCalled);
//				methodCall.addProperty(getCodeBlockProperty(), cb);
				callingMethod.addProperty(getCallsProperty(), methodCall);
				methodCall.addProperty(getCodeBlockProperty(), methodCalled);
				
				addRange(methodCall, node);

				addInputMapping(callingMethod, methodCall, methodCalled, (MethodCallExpr)node);
				addReturnedMapping(callingMethod, methodCall, methodCalled, containerNode, (MethodCallExpr)node);
			}
			else {
				logger.debug("Unexprected node type in postProcessingList: " + node.getClass().getCanonicalName());
			}
		}
		Iterator<Node> mitr = methodsFound.keySet().iterator();
		while (mitr.hasNext()) {
			Node mnode = mitr.next();
			String jdComment = getOrphanJavadocCommentsBeforeThisChildNode(mnode);
			if (jdComment != null) {
				Individual minst = methodsFound.get(mnode);
				getMethodJavadoc().put(minst.getURI(), jdComment);
			}
		}
	}

	private boolean addRange(Individual blockInst, Node blockNode) {
		Optional<Range> rng = blockNode.getRange();
		if (rng.isPresent()) {
			blockInst.addProperty(getBeginsAtProperty(), getCurrentCodeModel().createTypedLiteral(rng.get().begin.line));
			blockInst.addProperty(getEndsAtProperty(), getCurrentCodeModel().createTypedLiteral(rng.get().end.line));
			return true;
		}
		return false;
	}

	private void addReturnedMapping(Individual methodCalling, Individual methodCall, Individual methodCalled, Node containerNode, MethodCallExpr node) {
		if (containerNode instanceof AssignExpr) {
			Expression assignedTo = ((AssignExpr)containerNode).getTarget();
			if (assignedTo instanceof NameExpr) {
				// Java can only return a single value
				StmtIterator stmtitr = getCurrentCodeModel().listStatements(null, getCodeBlockProperty(), methodCalled);
				while (stmtitr.hasNext()) {
					Resource ref = stmtitr.next().getSubject();
					Statement outputstmt = ref.getProperty(getOutputProperty());
					if (outputstmt != null) {
						RDFNode output = outputstmt.getObject();
						if (output != null && output.isLiteral() && output.asLiteral().getValue().equals(true)) {
							StmtIterator stmtitr2 = getCurrentCodeModel().listStatements(null, getReferenceProperty(), ref);
							while (stmtitr2.hasNext()) {
								Resource cv = stmtitr2.next().getSubject();
//								System.out.println("\nSetting output mapping for " + cv.toString());
								Individual mapping = getCurrentCodeModel().createIndividual(getOutputMappingClass());
								mapping.addProperty(getCallingVariableProperty(), findDefinedVariable(((NameExpr)assignedTo).getNameAsString(), methodCalling));
								mapping.addProperty(getCalledVariableProperty(), cv);
								methodCall.addProperty(getReturnedMappingProperty(), mapping);
							}
						}
					}
//					else {
//						StmtIterator stmtitr2 = getCurrentCodeModel().listStatements(null, getReferenceProperty(), ref);
//						while (stmtitr2.hasNext()) {
//							Resource cv = stmtitr2.next().getSubject();
//							System.out.println("\nCodeVariable: " + cv.toString());
//						}
//						StmtIterator stmtitr3 = ref.listProperties();
//						while (stmtitr3.hasNext()) {
//							System.out.println(stmtitr3.next().toString());
//						}
//					}
				}
			}
		}
	}

	private void addInputMapping(Individual methodCalling, Individual methodCall, Individual methodCalled, MethodCallExpr node) {
		Iterator<Expression> argsitr = node.getArguments().iterator();
		List<Individual> argVars = null;
		Statement argsstmt = methodCalled.getProperty(getArgumentsProperty());
		if (argsstmt != null) {
			RDFNode argsList = argsstmt.getObject();
			if (argsList.canAs(Individual.class)) {
				argVars = getMembersOfList(getCurrentCodeModel(), argsList.as(Individual.class));
			}
		}
		if (argVars != null) {
			Iterator<Individual> argvaritr = argVars.iterator();
			while (argsitr.hasNext() && argvaritr.hasNext()) {
				Individual argvar = argvaritr.next();
				Expression argexpr = argsitr.next();
				if (argexpr instanceof NameExpr) {
					Individual mapping = getCurrentCodeModel().createIndividual(getInputMappingClass());
					mapping.addProperty(getCallingVariableProperty(), findDefinedVariable(((NameExpr)argexpr).getNameAsString(), methodCalling));
					mapping.addProperty(getCalledVariableProperty(), argvar);
					methodCall.addProperty(getInputMappingProperty(), mapping);
				}
				else {
					
				}
			}
		}
	}

	private Individual findMethodCalled(MethodCallExpr node) {
		Iterator<Node> nitr = methodsFound.keySet().iterator();
		while (nitr.hasNext()) {
			Node meth = nitr.next();
			if (meth instanceof MethodDeclaration && ((MethodDeclaration) meth).getNameAsString().equals(node.getNameAsString())) {
				return methodsFound.get(meth);
			}
			else if (meth instanceof ConstructorDeclaration && ((ConstructorDeclaration)meth).getNameAsString().equals(node.getNameAsString())) {
				return methodsFound.get(meth);
			}
		}
		// external reference; create instance.
		String methName = node.getNameAsString();
		String extUri = "";
		Optional<Expression> scope = node.getScope();
		if (scope.isPresent()) {
			extUri += scope.get().toString();
			extUri += ".";
		}
		extUri += methName;
		extUri = getCodeModelNamespace() + extUri;
		return getCurrentCodeModel().createIndividual(extUri, getExternalModelClass());
	}

	@Override
	public void setIncludeSerialization(boolean includeSerialization) {
		this.includeSerialization = includeSerialization;
	}

	@Override
	public boolean isIncludeSerialization() {
		return includeSerialization;
	}

	private Resource getExternalModelClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#ExternalMethod");
	}
	
	private Resource getMethodCallClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#MethodCall");
	}	

	private String getRootClassName(Node nd) {
		if (nd instanceof ClassOrInterfaceDeclaration) {
			return ((ClassOrInterfaceDeclaration)nd).getNameAsString();
		}
		List<ClassOrInterfaceDeclaration> clses = nd.findAll(ClassOrInterfaceDeclaration.class);
		if (clses != null && clses.size() > 0) {
			return clses.get(0).getNameAsString();
		}
		return "NoNameFound";
	}

	private String derivePrefixFromPackage(String pkg) {
		StringBuilder sb = new StringBuilder();
		int lastStart = 0;
		String locpkg = pkg;
		while (locpkg.contains(".") || locpkg.contains("/") || locpkg.contains(":")) {
			int charLoc = Math.max(Math.max(locpkg.indexOf('.'), locpkg.indexOf('/')), locpkg.indexOf(':'));
			if (charLoc > 0) {
				sb.append(pkg.substring(lastStart, lastStart + 1));
				lastStart = lastStart + charLoc + 1;
			}
			else {
				lastStart++;
			}
			locpkg = pkg.substring(lastStart);
		}
		sb.append(locpkg);
		if (sb.length() < 1) {
			sb.append(pkg);
		}
		return sb.toString();
//		if (pkg.contains(".")) {
//			StringBuilder sb = new StringBuilder();
//			int lastStart = 0;
//			int dotLoc = pkg.indexOf('.');
//			while (dotLoc > 0) {
//				sb.append(pkg.substring(lastStart, lastStart + 1));
//				lastStart = dotLoc + 1;
//				dotLoc = pkg.indexOf('.', dotLoc + 1);
//			}
//			return sb.toString();
//		}
//		else if (pkg.contains("/")) {
//			StringBuilder sb = new StringBuilder();
//			int lastStart = 0;
//			int dotLoc = pkg.lastIndexOf('/');
//			while (dotLoc > 0) {
//				sb.append(pkg.substring(lastStart, lastStart + 1));
//				lastStart = dotLoc + 1;
//				dotLoc = pkg.indexOf('.', dotLoc + 1);
//			}
//			return sb.toString();
//		}
//		else {
//			return pkg;
//		}
	}

	private void initializeCodeModel(String extractionMetaModelModelFolder) throws ConfigurationException, IOException, TranslationException {
		if (getCurationMgr().getExtractionProcessor().getCodeModel() == null) {
			// create new code model
			
			setCodeModelConfigMgr(ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(extractionMetaModelModelFolder, null)); //getCurationMgr().getProjectConfigurationManager());
			OntDocumentManager owlDocMgr = getCodeModelConfigMgr().getJenaDocumentMgr();
			OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
			if (extractionMetaModelModelFolder != null) { // && !modelFolderPathname.startsWith(SYNTHETIC_FROM_TEST)) {
				File mff = new File(extractionMetaModelModelFolder);
				mff.mkdirs();
//				spec.setImportModelGetter(new SadlJenaModelGetterPutter(spec, extractionMetaModelModelFolder));
				spec.setImportModelGetter(getCodeModelConfigMgr().getSadlModelGetterPutter(getCodeModelConfigMgr().getRepoType()));
			}
			if (owlDocMgr != null) {
				spec.setDocumentManager(owlDocMgr);
				owlDocMgr.setProcessImports(true);
			}
			setCurrentCodeModel(ModelFactory.createOntologyModel(spec));	
			getCurrentCodeModel().setNsPrefix(getCodeModelPrefix(), getCodeModelNamespace());
			Ontology modelOntology = getCurrentCodeModel().createOntology(getCodeModelName());
			logger.debug("Ontology '" + getCodeModelName() + "' created");
			modelOntology.addComment("This ontology was created by extraction from code by the ANSWER JavaModelExtractorJP.", "en");
			setCodeMetaModelUri(DialogConstants.CODE_EXTRACTION_MODEL_URI);
			setCodeMetaModelPrefix(DialogConstants.CODE_EXTRACTION_MODEL_PREFIX);
			OntModel importedOntModel = getCodeModelConfigMgr().getOntModel(getCodeMetaModelUri(), Scope.INCLUDEIMPORTS);
			addImportToJenaModel(getCodeModelName(), getCodeMetaModelUri(), getCodeMetaModelPrefix(), importedOntModel);
			OntModel sadlImplicitModel = getCodeModelConfigMgr().getOntModel(getSadlImplicitModelUri(), Scope.INCLUDEIMPORTS);
			addImportToJenaModel(getCodeModelName(), getSadlImplicitModelUri(), 
					getCodeModelConfigMgr().getGlobalPrefix(getSadlImplicitModelUri()), sadlImplicitModel);
			String listmodelurl = getCodeModelConfigMgr().getAltUrlFromPublicUri(getSadlListModelUri());
			if (listmodelurl != null && !listmodelurl.equals(getSadlListModelUri())) {
				if (new File((new SadlUtils()).fileUrlToFileName(listmodelurl)).exists()) {
					OntModel sadlListModel = getCodeModelConfigMgr().getOntModel(getSadlListModelUri(), Scope.INCLUDEIMPORTS);
					addImportToJenaModel(getCodeModelName(), getSadlListModelUri(), 
							getCodeModelConfigMgr().getGlobalPrefix(getSadlListModelUri()), sadlListModel);
				}
				else {
					System.err.println("Project is missing SadlListModel. This should not happen.");
				}
			}
			OntClass ctic = getClassesToIgnoreClass();
			if (ctic != null) {
				ExtendedIterator<OntClass> extitr = ctic.listSubClasses();
				if (extitr != null && extitr.hasNext()) {
					while (extitr.hasNext()) {
						Resource cti = extitr.next();
						classesToIgnore.add(cti.getLocalName());
					}
					extitr.close();
				}
			}
		}
		else {
			setCurrentCodeModel(getCurationMgr().getExtractionProcessor().getCodeModel());
		}
		
	}

	private void findAllClassesToIgnore(Node node) {
		if (node instanceof ClassOrInterfaceDeclaration) {
			ClassOrInterfaceDeclaration cls = (ClassOrInterfaceDeclaration) node;
			Iterator<ClassOrInterfaceType> etitr = cls.getExtendedTypes().iterator();
			while (etitr.hasNext()) {
				ClassOrInterfaceType et = etitr.next();
				if (classesToIgnore.contains(et.getNameAsString())) {
					if (!classesToIgnore.contains(cls.getNameAsString())) {
						classesToIgnore.add(cls.getNameAsString());
						logger.debug("findAllClassesToIgnore added '" + cls.getNameAsString() + "' to classesToIgnore");
					}
				}
				else {
					findAllClassesToIgnore(et);
				}
			}
		}
		else if (node instanceof ImportDeclaration) {
			return;
		}
		else if (node instanceof Modifier) {
			return;
		}
		else if (node instanceof ClassOrInterfaceType) {
			return;
		}
		else if (node instanceof MethodDeclaration) {
			return;
		}
		else if (node instanceof ConstructorDeclaration) {
			return;
		}
		else if (node instanceof FieldDeclaration) {
			return;
		}
		else if (node instanceof FieldAccessExpr) {
			return;
		}
		else if (node instanceof VariableDeclarator) {
			return;
		}
		else if (node instanceof ObjectCreationExpr) {
			return;
		}
		else if (node instanceof MethodCallExpr) {
			return;
		}
		else if (node instanceof AssignExpr) {
			return;
		}
		List<Node> childNodes = node.getChildNodes();
		for (int i = 0; i < childNodes.size(); i++) {
			Node childNode = childNodes.get(i);
			findAllClassesToIgnore(childNode);
		}
	}

	private void processBlock(Node blkNode, Individual containingInst) {
		investigateComments(blkNode, containingInst);
		List<Node> childNodes = blkNode.getChildNodes();
		for (int i = 0; i < childNodes.size(); i++) {
			Node childNode = childNodes.get(i);
			processBlockChild(childNode, containingInst, null);
		}
	}

	private void processBlockChild(Node childNode, Individual containingInst, USAGE knownUsage) {
		if (childNode instanceof ClassOrInterfaceDeclaration) {
			ClassOrInterfaceDeclaration cls = (ClassOrInterfaceDeclaration) childNode;
			if (!ignoreClass(cls, containingInst)) {
				Individual blkInst = getOrCreateClass(cls);
				addClassUriToMap(cls.getName().asString(), blkInst.getURI());
				if (containingInst != null) {
					getCurrentCodeModel().add(blkInst, getContainedInProperty(), containingInst);
				}
				else {
					setRootContainingInstance(blkInst);
				}
				processBlock(cls, blkInst);
				addSerialization(blkInst, cls.toString());
			}
		}
		else if (childNode instanceof FieldDeclaration) {
			FieldDeclaration fd = (FieldDeclaration) childNode;
			NodeList<VariableDeclarator> vars = fd.getVariables();
			vars.forEach(var -> {
				processBlockChild(var, containingInst, USAGE.Defined);
			});
		}
		else if (childNode instanceof ConstructorDeclaration) {
			ConstructorDeclaration m = (ConstructorDeclaration)childNode;
			String rt = m.getNameAsString();
			if (rt == null || !ignoreClass(rt, containingInst, false)) {
				Comment cmnt = getComment(m);
				Individual methInst = getOrCreateConstructor(m, containingInst);
				methodsFound.put(m, methInst);
				if (containingInst != null) {
					getCurrentCodeModel().add(methInst, getContainedInProperty(), containingInst);
				}
				// Note that this local scope overrides any variable of the same name in a
				//	more global scope. Therefore do not look for an existing variable
				NodeList<Parameter> args = m.getParameters();
				List<Individual> argList = args.size() > 0 ? new ArrayList<Individual>() : null;
				for (int j = 0; j < args.size(); j++) {
					Parameter param = args.get(j);
					String nm = param.getNameAsString();
					try {
						Individual argCV = getOrCreateCodeVariable(param, methInst, getMethodVariableClass());
						argList.add(argCV);
					} catch (AnswerExtractionException e) {
						e.printStackTrace();
					}
				}
				if (argList != null) {
					try {
						Individual typedList = addMembersToList(getCurrentCodeModel(), null, getCodeVariableListClass(), getCodeVariableClass(), argList.iterator());
						methInst.addProperty(getArgumentsProperty(), typedList);
					} catch (JenaProcessorException e) {
						e.printStackTrace();
					} catch (TranslationException e) {
						e.printStackTrace();
					}
				}
				addRange(methInst, m);
				Individual prior = setMethodWithBodyInProcess(methInst);
				processBlock(m, methInst);	// order matters--do this after parameters and before return
				setMethodWithBodyInProcess(prior);
				logger.debug(methInst.getURI() + " is a constructor");
				addSerialization(methInst, ((ConstructorDeclaration) childNode).toString());
			}
		}
		else if (childNode instanceof MethodDeclaration) {
			MethodDeclaration m = (MethodDeclaration) childNode;
			String rt = m.getTypeAsString();
			if (rt == null || !ignoreClass(rt, containingInst, false)) {
				Comment cmnt = getComment(m);
				Individual methInst = getOrCreateMethod(m, containingInst);
				methodsFound.put(m, methInst);
				if (containingInst != null) {
					getCurrentCodeModel().add(methInst, getContainedInProperty(), containingInst);
				}
				// Note that this local scope overrides any variable of the same name in a
				//	more global scope. Therefore do not look for an existing variable
				NodeList<Parameter> args = m.getParameters();
				List<Individual> argList = args.size() > 0 ? new ArrayList<Individual>() : null;
				for (int j = 0; j < args.size(); j++) {
					Parameter param = args.get(j);
					String nm = param.getNameAsString();
					try {
						Individual argCV = getOrCreateCodeVariable(param, methInst, getMethodVariableClass());
						argList.add(argCV);
					} catch (AnswerExtractionException e) {
						e.printStackTrace();
					}
				}
				if (argList != null) {
					try {
						Individual typedList = addMembersToList(getCurrentCodeModel(), null, getCodeVariableListClass(), getCodeVariableClass(), argList.iterator());
						methInst.addProperty(getArgumentsProperty(), typedList);
					} catch (JenaProcessorException e) {
						e.printStackTrace();
					} catch (TranslationException e) {
						e.printStackTrace();
					}
				}
				addRange(methInst, m);
				Individual prior = setMethodWithBodyInProcess(methInst);
				processBlock(m, methInst);	// order matters--do this after parameters and before return
				setMethodWithBodyInProcess(prior);
				
				if (rt != null && !rt.equals("void")) {
					List<Literal> rtypes = new ArrayList<Literal>();
					rtypes.add(getCurrentCodeModel().createTypedLiteral(rt));
					Individual returnTypes;
					try {
						returnTypes = addMembersToList(getCurrentCodeModel(), null, getStringListClass(), XSD.xstring, rtypes.iterator());
						methInst.addProperty(getReturnTypeProperty(), returnTypes);
					} catch (JenaProcessorException e) {
						e.printStackTrace();
					} catch (TranslationException e) {
						e.printStackTrace();
					}
				}
				logger.debug(methInst.getURI() + " returns " + ((rt != null && rt.length() > 0) ? rt : "void"));
				addSerialization(methInst, ((MethodDeclaration) childNode).toString());
			}
		}
		else if (childNode instanceof MethodCallExpr) {
			MethodCallExpr mc = (MethodCallExpr)childNode;
			if (!ignoreMethodCall(mc, containingInst)) {
				addNodeToPostProcessingList(mc.getRange().get(), mc, containingInst);
			}
        	NodeList<Expression> args = mc.getArguments();
        	Iterator<Expression> nlitr = args.iterator();
        	while (nlitr.hasNext()) {
        		Expression expr = nlitr.next();
        		if (expr instanceof NameExpr) {
        			try {
						setSetterArgument(mc, (NameExpr)expr, containingInst);
						processBlockChild(expr, containingInst, USAGE.Used);
					} catch (AnswerExtractionException e) {
						e.printStackTrace();
					}
        		}
        		else {
//           			processBlockChild(expr, containingInst, USAGE.Defined);   
        			processBlockChild(expr, containingInst, USAGE.Used);	// how could a method argument be Defined? awc 2/7/2020
        		}
 			}
		}
		else if (childNode instanceof BlockStmt) {
			processBlock(childNode, containingInst);
		}
		else if (childNode instanceof ExpressionStmt) {
			ExpressionStmt es = (ExpressionStmt)childNode;
			Expression expr = es.getExpression();
			if (expr instanceof AssignExpr) {
				processBlockChild(expr, containingInst, knownUsage);
			}
			else if (expr instanceof VariableDeclarationExpr) {
				processBlockChild(expr, containingInst, USAGE.Defined);
			}
			else if (expr instanceof MethodCallExpr) {
				processBlockChild(expr, containingInst, USAGE.Used);
			}
			else {
				processBlock(expr, containingInst);
			}
		}
		else if (childNode instanceof EnclosedExpr) {
			processBlock(childNode, containingInst);
		}
		else if (childNode instanceof VariableDeclarationExpr) {
			VariableDeclarationExpr vdecl = (VariableDeclarationExpr)childNode; 
			try {
				OntClass codeVarClass = getMethodVariableClass();
				Individual vdInst = getOrCreateCodeVariable(vdecl, containingInst, codeVarClass);
			} catch (AnswerExtractionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else if (childNode instanceof VariableDeclarator) {
			try {
				OntClass codeVarClass = getClassFieldClass();	// TODO is this always field or only in a class?
				Individual fdInst = getOrCreateCodeVariable(childNode, containingInst, codeVarClass);
			} catch (AnswerExtractionException e) {
				e.printStackTrace();
			}
		}
		else if (childNode instanceof AssignExpr) {
			AssignExpr ass = (AssignExpr)childNode;
			List<Node> assChildren = ass.getChildNodes();
			if (assChildren.size() == 2) {
				Node n0 = assChildren.get(0);
				Node n1 = assChildren.get(1);
				if (n0 instanceof NameExpr) {
					String nm = ((NameExpr)n0).getNameAsString();
					Individual varInst = findDefinedVariable(nm, containingInst);
					if (n1 instanceof LiteralExpr) {
						if (varInst != null) {
							logger.debug(varInst.getLocalName() + ": " + ass.toString());
							addPotentialConstant(varInst, (LiteralExpr)n1);
						}
					}
					else {
						removePotentalConstant(varInst);
//						processBlockChild(n1, containingInst, knownUsage);
					}
				}
			}
			for (int j = 0; j < assChildren.size(); j++) {
				processBlockChild(assChildren.get(j), containingInst, (j == 0 ? USAGE.Reassigned : USAGE.Used));					
			}
		}
		else if (childNode instanceof BinaryExpr) {
			Operator op = ((BinaryExpr)childNode).getOperator();
			if (op.equals(Operator.DIVIDE) ||
					op.equals(Operator.MINUS) ||
					op.equals(Operator.MULTIPLY) ||
					op.equals(Operator.PLUS) ||
					op.equals(Operator.REMAINDER) ||
					op.equals(Operator.SIGNED_RIGHT_SHIFT) ||
					op.equals(Operator.UNSIGNED_RIGHT_SHIFT)
					) {
				containingInst.setPropertyValue(getDoesComputationProperty(), getCurrentCodeModel().createTypedLiteral(true));
				logger.debug("BinaryExpr: " + ((BinaryExpr)childNode).toString());
			}
			processBlock(childNode, containingInst);
		}
		else if (childNode instanceof NameExpr) {
			String nm = ((NameExpr)childNode).getNameAsString();
			findCodeVariableAndAddReference(childNode, nm, containingInst, knownUsage, true, null, false);
		}
		else if (childNode instanceof FieldAccessExpr) {
			FieldAccessExpr fae = (FieldAccessExpr)childNode;
			String nm = fae.getNameAsString();
			findCodeVariableAndAddReference(childNode, nm, containingInst, knownUsage, true, null, false);
		}
		else if (childNode instanceof ArrayAccessExpr) {
			Expression nmexpr = ((ArrayAccessExpr)childNode).getName();
			if (nmexpr instanceof NameExpr) {
				String nm = ((NameExpr)nmexpr).getNameAsString();
				findCodeVariableAndAddReference(childNode, nm, containingInst, knownUsage, true, null, false);
			}
		}
		else if (childNode instanceof IfStmt) {
			Expression cond = ((IfStmt)childNode).getCondition();
			processBlockChild(cond, containingInst, USAGE.Used);
			List<Node> condChildren = ((IfStmt)childNode).getChildNodes();
			for (int j = 1; j < condChildren.size(); j++) {
				processBlockChild(condChildren.get(j), containingInst, null);
			}
		}
		else if (childNode instanceof ForStmt) {
			List<Node> children = ((ForStmt)childNode).getChildNodes();
			for (int j = 0; j < children.size(); j++) {
				processBlockChild(children.get(j), containingInst, null);
			}
		}
		else if (childNode instanceof WhileStmt) {
			List<Node> children = ((WhileStmt)childNode).getChildNodes();
			for (int j = 0; j < children.size(); j++) {
				processBlockChild(children.get(j), containingInst, null);
			}
		}
		else if (childNode instanceof ReturnStmt) {
			List<Node> returnChildren = ((ReturnStmt)childNode).getChildNodes();
			if (!returnChildren.isEmpty()) {
				Node ret0 = returnChildren.get(0);
				if (ret0 instanceof EnclosedExpr) {
					ret0 = ((EnclosedExpr)ret0).getInner();
				}
				if (ret0 instanceof NameExpr) {
					// this is an output of this block. It should already exist.
					String nm = ((NameExpr)ret0).getNameAsString();
					findCodeVariableAndAddReference(ret0, nm, containingInst, USAGE.Used, true, InputOutput.Output, false);
				}
				else {
					for (int j = 0; j < returnChildren.size(); j++) {
						processBlockChild(returnChildren.get(j), containingInst, null);
					}
				}
			}
		}
		else if (childNode instanceof BlockComment) {
			String cntnt = ((BlockComment)childNode).getContent();
			if (cntnt != null) {
				investigateComment(childNode, containingInst, (BlockComment)childNode);
			}
			List<Comment> cmnts = ((BlockComment)childNode).getAllContainedComments();
			for (int i = 0; cmnts != null && i < cmnts.size(); i++) {
				Comment cmt = cmnts.get(i);
				investigateComment(childNode, containingInst, cmt);
			}
		}
//		else if (childNode instanceof ImportDeclaration) {
//			logger.debug("Ignoring '" + childNode.toString() + "'");
//		}
		else {
			logger.debug("Block child unhandled Node '" + childNode.toString().trim() + "' of type " + childNode.getClass().getCanonicalName());
		}
		investigateComments(childNode, containingInst);
	}

	private boolean ignoreMethodCall(MethodCallExpr mc, Individual containingInst) {
		if (mc.getScope().isPresent()) {
			Expression scope = mc.getScope().get();
			if (scope instanceof FieldAccessExpr) {
				if (ignoreMethod((FieldAccessExpr)scope, containingInst)) {
					return true;
				}
			}
			else if (scope instanceof NameExpr) {
				RDFNode tvtype = findDefinedVariableType(((NameExpr)scope).getNameAsString(), containingInst);
				if (tvtype != null && ignoreType(tvtype, containingInst)) {
					return true;
				}
				else if (ignoreClass(((NameExpr)scope).getNameAsString(), containingInst, false)){
					return true;
				}
			}
		}
		// consider the type returned and if ignored class, ignore
		if (mc.getParentNode().get() instanceof AssignExpr) {
			AssignExpr ae = (AssignExpr) mc.getParentNode().get();
			if (ae.getTarget() instanceof NameExpr) {
				NameExpr aene = (NameExpr)ae.getTarget();
				RDFNode tvtype = findDefinedVariableType(aene.getNameAsString(), containingInst);
				if (ignoreType(tvtype, containingInst)) {
					return true;
				}
			}
		}
		// consider the arguments and if ignored types ignore
		Iterator<Expression> argitr = mc.getArguments().iterator();
		while (argitr.hasNext()) {
			Expression arg = argitr.next();
			if (arg instanceof NameExpr) {
				RDFNode typ = findDefinedVariableType(((NameExpr)arg).getNameAsString(), containingInst);
				if (ignoreType(typ, containingInst)) {
					return true;
				}
			}
			else if (arg instanceof ObjectCreationExpr) {
				ClassOrInterfaceType typ = ((ObjectCreationExpr)arg).getType();
				if (ignoreClass(typ.getNameAsString(), containingInst, false)) {
					return true;
				}
			}
			else if (arg instanceof FieldAccessExpr) {
				if (ignoreMethod((FieldAccessExpr)arg, containingInst)) {
					return true;
				}
			}
		}
		return false;
	}

	private boolean ignoreMethod(FieldAccessExpr fae, Individual containingInst) {
		Expression scope = fae.getScope();
		if (scope instanceof NameExpr) {
			if (ignoreClass(((NameExpr)scope).getNameAsString(), containingInst, true)) {
				return true;
			}
		}
		else if (scope instanceof FieldAccessExpr) {
			if (ignoreMethod((FieldAccessExpr)scope, containingInst)) {
				return true;
			}
		}
		return false;
	}

	private boolean ignoreType(RDFNode tvtype, Individual containingInst) {
		if (tvtype == null) {
			// if the variable doesn't exist at this point it must have been an ignored type
			return true;					
		}
		if (tvtype.isLiteral()) {
			if (ignoreClass(tvtype.asLiteral().getValue().toString(), containingInst, true)) {
				return true;
			}
		}
		else if (tvtype.isURIResource()) {
			if (ignoreClass(tvtype.asResource().getLocalName(), containingInst, true)) {
				return true;
			}
		}
		return false;
	}
	
	private RDFNode findDefinedVariableType(String nm, Individual containingInst) {
		RDFNode nmType = null;
		Individual targetVar = findDefinedVariable(nm, containingInst);
		if (targetVar != null) {
			nmType = targetVar.getPropertyValue(getVarTypeProperty());
		}
		return nmType;
	}

	private boolean ignoreClass(ClassOrInterfaceDeclaration cls, Individual containingInst) {
		if (ignoreClass(cls.getName().asString(), containingInst, false)) {
			return true;
		}
		return false;
	}
	
	private boolean ignoreClass(String clsname, Individual containingInst, boolean nullIsIgnore) {
		if (classesToIgnore.contains(clsname)) {
			return true;
		}
		OntClass cls = getCurrentCodeModel().getOntClass(getCodeModelNamespace() + clsname);
		if (cls != null) {
			ExtendedIterator<OntClass> extitr = cls.listSuperClasses();
			while (extitr.hasNext()) {
				if (classesToIgnore.contains(extitr.next().getLocalName())) {
					extitr.close();
					return true;
				}
			}
		}
		else if (containingInst != null && !isBuiltinType(clsname)) {
			RDFNode tvtype = findDefinedVariableType(clsname, containingInst);
			if (tvtype == null) {
				String clsuri = getClassUriFromSimpleName(clsname);
				if (clsuri != null) {
					return false;
				}
			}
			if ((nullIsIgnore || tvtype != null) && ignoreType(tvtype, containingInst)) {
				return true;
			}
		}
		return false;
	}

	private boolean isBuiltinType(String clsname) {
		String[] builtinTypes = {"byte", "short", "int", "long", "float", "double", "char",
				"String", "boolean", 
				"Byte", "Short", "Integer", "Long", "Float", "Double", "Character",
				"Boolean"};
		if (primitiveTypesList == null) {
			primitiveTypesList = Arrays.asList(builtinTypes);
		}
		if (primitiveTypesList .contains(clsname)) {
			return true;
		}
		return false;
	}

	private void removePotentalConstant(Individual varInst) {
		if (!discountedPotentialConstants.contains(varInst)) {
			discountedPotentialConstants.add(varInst);
		}
	}

	private void addPotentialConstant(Individual varInst, LiteralExpr n1) {
		if (potentialConstants.containsKey(varInst)) {
			removePotentalConstant(varInst);
		}
		else {
			potentialConstants.put(varInst, n1);
		}
	}

	/**
	 * Method to add a method call to the post-processing list. 
	 * @param expr -- the MethodCallExpr
	 * @param codeBlock -- the code block (method) in which the call occurs
	 */
	private void addNodeToPostProcessingList(Range rng, Node expr, Individual codeBlock) {
		postProcessingList.put(rng, new MethodCallMapping(expr, codeBlock));
	}

	private void addSerialization(Individual blkInst, String code) {
		if (isIncludeSerialization()) {
			blkInst.addProperty(getSerializationProperty(), getCurrentCodeModel().createTypedLiteral(code));
		}
	}

	private boolean setRootContainingInstance(Individual thisInst) {
		if (rootContainingInstance == null) {
			rootContainingInstance = thisInst;
			return true;
		}
		return false;
	}

	private void investigateComments(Node childNode, Individual subject) {
		if (!areNodeCommentsInvestigated(childNode)) {
			Comment cmt = getComment(childNode);
			investigateComment(childNode, subject, cmt);
			List<Comment> ocmts = childNode.getOrphanComments();
			for (int i = 0; ocmts != null && i < ocmts.size(); i++) {
				cmt = ocmts.get(i);
				Optional<Range> rng = cmt.getRange();
				if (rng.isPresent()) {
					logger.debug("Found orphaned comment at line " + rng.get().getLineCount() + "(" + rng.get().begin.toString() + " to " + rng.get().end.toString() + ")");
				}
				else {
					logger.debug("Found orphaned comment but range not known");
				}
				logger.debug("   " + cmt.getContent());
				investigateComment(childNode, subject, cmt);
			}
			nodeCommentsAreInvestigated(childNode);
		}
	}
	
	private void nodeCommentsAreInvestigated(Node childNode) {
		if (!nodesWithCommentInvestigated.contains(childNode)) {
			nodesWithCommentInvestigated.add(childNode);
		}
	}

	private boolean areNodeCommentsInvestigated(Node childNode) {
		return nodesWithCommentInvestigated.contains(childNode);
	}

	/**
	 * Method to get orphaned Javadoc comments before this node.
	 * Fashioned after PrettyPrintVisitor.printOrphanCommentsBeforeThisChildNode
	 * @param node
	 * @return--Javadoc comment before else null
	 */
    private String getOrphanJavadocCommentsBeforeThisChildNode(Node node) {
        if (node instanceof Comment) return null;
        Node parent = node.getParentNode().orElse(null);
        if (parent == null) return null;
        List<Node> everything = new LinkedList<>(parent.getChildNodes());
        sortByBeginPosition(everything);
        int positionOfTheChild = -1;
        for (int i = 0; i < everything.size(); i++) {
            if (everything.get(i) == node) positionOfTheChild = i;
        }
        if (positionOfTheChild == -1) {
            throw new AssertionError("I am not a child of my parent.");
        }
        int positionOfPreviousChild = -1;
        for (int i = positionOfTheChild - 1; i >= 0 && positionOfPreviousChild == -1; i--) {
            if (!(everything.get(i) instanceof Comment)) positionOfPreviousChild = i;
        }
        if (positionOfPreviousChild >= 0) {
	        StringBuilder sb = new StringBuilder();
	        for (int i = positionOfPreviousChild + 1; i < positionOfTheChild; i++) {
	            Node nodeToPrint = everything.get(i);
	            if (!(nodeToPrint instanceof JavadocComment)) {
	                System.out.println(
	                        "Found comment: " + nodeToPrint.getClass() + ". Position of previous child: "
	                                + positionOfPreviousChild + ", position of child " + positionOfTheChild);
	            }
	            else {
	            	sb.append(nodeToPrint.toString());
	            }
	        }
	        if (sb.length() > 0) {
	        	return sb.toString();
	        }
        }
        if (node instanceof MethodDeclaration) {
        	Optional<JavadocComment> jdc = ((MethodDeclaration)node).getJavadocComment();
        	if (jdc.isPresent()) {
        		return jdc.get().toString().trim();
        	}
        }
        return null;
    }


	private void investigateComment(Node childNode, Individual subject, Comment cmt) {
		if (cmt != null) {
			logger.debug("   " + cmt.getContent());
			Individual cmtInst = getCurrentCodeModel().createIndividual(getCommentClass());
			if (subject == null) {
				subject = rootContainingInstance;
			}
			if (subject != null) {
				subject.addProperty(getCommentProperty(), cmtInst);
				cmtInst.addProperty(getCommentContentProperty(), getCurrentCodeModel().createTypedLiteral(cmt.getContent()));
				addRange(cmtInst, childNode);
			}
			else {
				logger.debug("Unable to add comment because there is no known subject");
			}
			Optional<Range> rng = childNode.getRange();
			if (rng.isPresent()) {
				logger.debug("Found comment at line " + rng.get().getLineCount() + "(" + rng.get().begin.toString() + " to " + rng.get().end.toString() + ")");
			}
			else {
				logger.debug("Found comment but range not known");
			}
			this.aggregatedComments.append(" ");		// make sure there are spaces between the comments.
			this.aggregatedComments.append(preProcessCommentForAggregation(cmt));
		}
	}
	
	private String preProcessCommentForAggregation(Comment cmt) {
		if (cmt instanceof JavadocComment) {
			String c = ((JavadocComment)cmt).getContent().trim();
			String[] lines = c.split("\\r?\\n");
			StringBuilder sb = new StringBuilder();
			int cntr = 0;
			for (String line : lines) {
				line = line.trim();
				if (line.startsWith("*")) {
					line = line.substring(1).trim();
				}
				if (line.startsWith("@author") || line.startsWith("@version") || 
						line.startsWith("@deprecated") || line.startsWith("@exception") ||
						line.startsWith("@since") || line.startsWith("@throws")) {				
					continue;
				}
				if (line.startsWith("@")) {
					if (line.startsWith("@param")) {
						line = line.substring(6).trim();
					}
					else if (line.startsWith("@return")) {
						line = line.substring(7).trim();
					}
					if (cntr++ > 0) {
						sb.append(System.lineSeparator());
					}
					sb.append(line);
				}
				else {
					if (cntr++ > 0) {
						sb.append(" ");
					}
					sb.append(minusRemovals(line));
				}
			}
			return sb.toString();
		}
		else if (cmt instanceof LineComment) {
			String c = ((LineComment)cmt).toString().trim();
			if (c.startsWith("//")) {
				c = c.substring(2);
				c = minusRemovals(c.trim());
			}
			Optional<Node> cmttedNode = ((LineComment)cmt).getCommentedNode();
			if (cmttedNode.isPresent()) {
				Node cmtnode = cmttedNode.get();
				if (cmtnode instanceof ReturnStmt) {
					addReturnstatementComment(getMethodNodeUri(cmtnode), c);
				}
				else if (cmtnode instanceof FieldDeclaration) {
					
				}
			}
			return c;
		}
		else if (cmt instanceof BlockComment) {
			String c = ((BlockComment)cmt).getContent();
			String[] lines = c.split("\\r?\\n");
			StringBuilder sb = new StringBuilder();
			int cntr = 0;
			for (String line : lines) {
				line = line.trim();
				if (line.startsWith("*")) {
					line = line.substring(1).trim();
				}
				if (line.length() > 0) {
					if (cntr++ > 0) {
						sb.append(" ");
					}
					sb.append(minusRemovals(line));
				}
			}
			return sb.toString();
		}
		return cmt.toString();
	}
	
	private void addReturnstatementComment(String methodNodeUri, String c) {
		returnStatementComments.put(methodNodeUri, c);
	}

	private String getMethodNodeUri(Node cmtnode) {
		Node parent = cmtnode;
		do {
			parent = parent.getParentNode().isPresent() ? parent.getParentNode().get() : null;
		} while (parent != null && !(parent instanceof MethodDeclaration));
		if (parent != null) {
			Individual meth = methodsFound.get(parent);
			if (meth != null) {
				return meth.getURI();
			}
		}
		return null;
	}
	
	public String getReturnLineComment(String methodUri) {
		return returnStatementComments.get(methodUri);
	}


	private String minusRemovals(String txt) {
		if (txt.contains("@code")) {
			int loc = txt.indexOf("@code");
			StringBuilder sb = new StringBuilder(txt.substring(0, loc - 1));
			for (int i = loc + 5; i< txt.length(); i++) {
				char c = txt.charAt(i);
				if (c == '}') {
					String rest = minusRemovals(txt.substring(i + 1));
					if (Character.isWhitespace(txt.charAt(loc - 2))) {
						while (Character.isWhitespace(rest.charAt(0))) {
							rest = rest.substring(1);
						}
					}
					sb.append(rest);
					break;
				}
			}
			return sb.toString();
		}
		return txt;
	}

	/**
	 * Method to return the aggregated comments from the code extracted from
	 * @return
	 */
	@Override
	public String getAggregatedComments() {
		return aggregatedComments.toString();
	}
	
	@Override
	public void clearAggregatedComments() {
		aggregatedComments = new StringBuilder();
	}

	private Property getInputProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#input");
	}

	private Property getOutputProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#output");
	}

	private Individual findCodeVariableAndAddReference(Node childNode, String nm, Individual containingInst, USAGE usage, boolean lookToLargerScope, InputOutput inputOutput, boolean isSetterArgument) {
		String nnm = constructNestedElementUri(childNode, nm);

		Individual varInst = lookToLargerScope ? findDefinedVariable(nm, containingInst) : null;
		if (varInst == null && !lookToLargerScope) {
			try {
				varInst = getOrCreateCodeVariable(childNode, nm, nnm, containingInst, getCodeVariableClass(childNode), inputOutput);
			} catch (AnswerExtractionException e) {
				e.printStackTrace();
			}
		}
		else if (varInst != null) {
			try {
				Individual ref = createReference(childNode, varInst, containingInst, usage != null ? usage : USAGE.Used);
	          	setInputOutputIfKnown(ref, inputOutput);
	          	if (isSetterArgument) {
	          		ref.setPropertyValue(getSetterArgumentProperty(), getCurrentCodeModel().createTypedLiteral(true));
	          	}
			} catch (AnswerExtractionException e) {
				e.printStackTrace();
			}
		}
		else {
			logger.debug("NameExpr (" + nnm + ") not found; it must have been ignored.");
		}
		return varInst;
	}

	private OntClass getCodeVariableClass(Node varNode) {
		if (varNode instanceof Parameter) {
			return getMethodArgumentClass();
		}
		else if (varNode instanceof FieldDeclaration) {
			return getClassFieldClass();
		}
		else {
			Optional<Node> parent = varNode.getParentNode();
			while (parent.isPresent()) {
				if (parent.isPresent()) {
					if (parent.get() instanceof MethodDeclaration) {
						return getMethodVariableClass();
					}
					else if (parent.get() instanceof ConstructorDeclaration) {
						return getMethodVariableClass();
					}
					else if (parent.get() instanceof ClassOrInterfaceDeclaration) {
						return getClassFieldClass();
					}
				}
				parent = parent.get().getParentNode();
			}
		}
		return null;
	}

	/**
	 * Method to find a CodeVariable given its name and containing instance
	 * @param nm
	 * @param containingInst
	 * @return
	 */
	private Individual findDefinedVariable(String nm, Individual containingInst) {
		Individual varInst = null;
		if (containingInst != null) {
			Individual inst = containingInst;
			String nnm = getCodeModelNamespace() + inst.getLocalName() + "." + nm;
			varInst = getCurrentCodeModel().getIndividual(nnm);
			if (varInst == null) {
				RDFNode obj = containingInst.getPropertyValue(getContainedInProperty());
				if (obj != null && obj.isURIResource() && obj.canAs(Individual.class)) {
					return findDefinedVariable(nm, obj.as(Individual.class));
				}
			}
		}
		return varInst;
	}

	private void setSetterArgument(MethodCallExpr mc, NameExpr arg, Individual containingInst) throws AnswerExtractionException {
		boolean isArgToSetter = false;
		if (mc.getNameAsString().startsWith("set") || mc.getNameAsString().contains(".set")) {
			isArgToSetter = true;
		}
		else {
			Optional<Node> parent = mc.getParentNode();
			while (parent.isPresent()) {
				if (parent.get() instanceof MethodCallExpr) {
					if (((MethodCallExpr)parent.get()).getNameAsString().startsWith("set") ||
							((MethodCallExpr)parent.get()).getNameAsString().startsWith(".set")) {
						isArgToSetter = true;
						break;
					}
				}
				parent = parent.get().getParentNode();
			}	
		}
		if (isArgToSetter) {
			Individual cvInst = findCodeVariableAndAddReference(mc, arg.getNameAsString(), containingInst, USAGE.Used, true, InputOutput.Output, true);
		}
	}

	private Individual createReference(Node varNode, Individual codeVarInst, Individual containingInst, USAGE usage) throws AnswerExtractionException {
		Individual ref = createNewReference(containingInst, varNode, usage);
		codeVarInst.addProperty(getReferenceProperty(), ref);
		return ref;
	}

//	private Individual createNewReference(Individual blkInst, int beginsAt, int endsAt, USAGE usage) throws CodeExtractionException {
	private Individual createNewReference(Individual blkInst, Node varNode, USAGE usage) throws AnswerExtractionException {
		Individual refInst = getCurrentCodeModel().createIndividual(getReferenceClass());
		if (blkInst != null) {
			refInst.addProperty(getCodeBlockProperty(), blkInst);
		}
		refInst.addProperty(getUsageProperty(), getUsageInstance(usage));
		addRange(refInst, varNode);
		return refInst;
	}

	private Individual getOrCreateConstructor(ConstructorDeclaration m, Individual containingInst) {
		Individual methInst = null;
    	String nm = m.getNameAsString();
		String nnm = constructNestedElementUri(m, nm);
    	methInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + nnm);
// TODO this needs to generate a unique name for this constructor as there may be other constructors with the same name but different signatures.    	
    	if (methInst == null) {
    		methInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + nnm, getCodeBlockConstructorClass());
    	}
    	else {
    		System.out.println("Is this a different constructor with a different signature?");
    	}
		return methInst;
	}

	private Individual getOrCreateMethod(MethodDeclaration m, Individual containingInst) {
		Individual methInst = null;
    	String nm = m.getNameAsString();
		String nnm = constructNestedElementUri(m, nm);
    	methInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + nnm);
    	// TODO this needs to generate a unique name for this constructor as there may be other constructors with the same name but different signatures.    	
    	if (methInst == null) {
    		methInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + nnm, getCodeBlockMethodClass());
    	}
    	else {
    		System.out.println("Is this a different method with a different signature?");
    	}
		return methInst;
	}

	private Individual getOrCreateCodeVariable(Node varNode, Individual containingInst, OntClass codeVarClass) throws AnswerExtractionException {
		Individual cvInst = null;
		String origName = null;
		String nnm = null;
		if (varNode instanceof VariableDeclarator) {
			origName = ((VariableDeclarator)varNode).getNameAsString();
			if (containingInst != null) {
				nnm = constructNestedElementUri(varNode, origName);
			}
			else {
				nnm = origName;
			}
			codeVarClass = getClassFieldClass();
		}
		else if (varNode instanceof NameExpr) {
			origName = ((NameExpr)varNode).getNameAsString();
			if (containingInst != null) {
				nnm = constructNestedElementUri(varNode, origName);
			}
			else {
				nnm = origName;
			}
		}
		else if (varNode instanceof VariableDeclarationExpr) {
			NodeList<VariableDeclarator> vars = ((VariableDeclarationExpr)varNode).getVariables();
			for (int i = 0; i < vars.size(); i++) {
				if (i > 0) {
					logger.debug("Multiple vars (" + varNode.toString() + ") in VariableDeclarationExpr not current handled");
				}
				VariableDeclarator var = vars.get(i);
				origName = var.getNameAsString();
				if (containingInst != null) {
					nnm = constructNestedElementUri(varNode, origName);
				}
				else {
					nnm = origName;
				}
				codeVarClass = getMethodVariableClass();
				cvInst = getOrCreateCodeVariable(var, origName, nnm, containingInst, codeVarClass, null);
				if (vars.get(i).getInitializer().isPresent()) {
		          	createReference(var, cvInst, containingInst, USAGE.Reassigned);
				}
			}
		}
		else if (varNode instanceof Parameter) {
			NameExpr nm = ((Parameter)varNode).getNameAsExpression();
			return findCodeVariableAndAddReference(varNode, nm.getNameAsString(), containingInst, USAGE.Defined, false, InputOutput.Input, false);
		}
		else {
			throw new AnswerExtractionException("Unexpected CodeVariable varNode type: " + varNode.getClass().getCanonicalName());
		}
		cvInst = getOrCreateCodeVariable(varNode, origName, nnm, containingInst, codeVarClass, null);
		return cvInst;
	}

	private Individual getOrCreateCodeVariable(Node varNode, String origName, String nnm, Individual containingInst,
			OntClass codeVarClass, InputOutput inputOutput) throws AnswerExtractionException {
      	String typeStr = null;
      	if (varNode instanceof VariableDeclarator) {
      		typeStr = ((VariableDeclarator)varNode).getTypeAsString();
      	}
      	else if (varNode instanceof Parameter) {
      		typeStr = ((Parameter)varNode).getTypeAsString();
      	}
      	else if (varNode instanceof VariableDeclarationExpr) {
      		typeStr = ((VariableDeclarationExpr)varNode).getVariable(0).getTypeAsString();
      	}
      	else {
      		int i = 0;
      	}
		Individual cvInst = null;
     	if (typeStr != null) {
      		if (!ignoreClass(typeStr, containingInst, false)) {
       			cvInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + nnm);
      			if (cvInst == null && codeVarClass != null) {
      				cvInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + nnm, codeVarClass);
      	          	Individual ref = createReference(varNode, cvInst, containingInst, USAGE.Defined);
      	          	addDeclarationScript(varNode, cvInst, containingInst);
      	          	setInputOutputIfKnown(ref, inputOutput);
      	          	cvInst.addProperty(getVarNameProperty(), getCurrentCodeModel().createTypedLiteral(origName));
      			}
      			cvInst.addProperty(getVarTypeProperty(), getCurrentCodeModel().createTypedLiteral(typeStr));
      		}
      	}
		return cvInst;
	}

	private void addDeclarationScript(Node varNode, Individual cvInst, Individual containingInst) {
		if (varNode instanceof VariableDeclarator) {
			Type type = ((VariableDeclarator)varNode).getType();
			Expression initializer = null;
			if (((VariableDeclarator)varNode).getInitializer().isPresent()) {
				initializer = ((VariableDeclarator)varNode).getInitializer().get();
			}
			if (type != null) {
				StringBuilder javaScript = new StringBuilder(type.asString());
				javaScript.append(" ");
				javaScript.append(((VariableDeclarator) varNode).getNameAsString());
				if (initializer != null) {
					javaScript.append(" = ");
					javaScript.append(initializer.toString());
				}
				javaScript.append(";");
				cvInst.addLabel(javaScript.toString(), "Java");
			}
		}
	}

	private void setInputOutputIfKnown(Individual ref, InputOutput inputOutput) {
		if (inputOutput != null) {
			if (inputOutput.equals(InputOutput.Input)) {
				ref.addProperty(getInputProperty(), getCurrentCodeModel().createTypedLiteral(true));
			}
			else if (inputOutput.equals(InputOutput.Output)) {
				ref.addProperty(getOutputProperty(), getCurrentCodeModel().createTypedLiteral(true));
			}
		}
	}

	private String constructNestedElementUri(Node node, String nm) {
		StringBuilder sb = new StringBuilder();
		Optional<Node> parent = node.getParentNode();
		while (parent.isPresent()) {
			if (parent.get() instanceof MethodDeclaration) {
				sb.insert(0,((MethodDeclaration)parent.get()).getNameAsString() + ".");
			}
			else if (parent.get() instanceof ConstructorDeclaration) {
				sb.insert(0,((ConstructorDeclaration)parent.get()).getNameAsString() + ".");
			}
			else if (parent.get() instanceof ClassOrInterfaceDeclaration) {
				sb.insert(0, ((ClassOrInterfaceDeclaration)parent.get()).getNameAsString() + ".");
			}
			parent = parent.get().getParentNode();
			if (!parent.isPresent()) {
				break;
			}
			if (parent.get() instanceof CompilationUnit) {
				break;
			}
		}
		sb.append(nm);
		return sb.toString();
	}

	private Individual getOrCreateClass(ClassOrInterfaceDeclaration cls) {
		Individual clsInst = null;
    	String nm = cls.getNameAsString();
    	String nmm = constructNestedElementUri(cls, nm);
    	clsInst = getCurrentCodeModel().getIndividual(getCodeModelNamespace() + nmm); //getCodeModelNamespace() + nm);
    	if (clsInst == null) {
    		clsInst = getCurrentCodeModel().createIndividual(getCodeModelNamespace() + nmm, getCodeBlockClassClass()); //getCodeModelNamespace() + nm, getCodeBlockClassClass());
    	}
    	return clsInst;
	}

	private Individual getUsageInstance(USAGE usage) throws AnswerExtractionException {
		if (usage.equals(USAGE.Defined)) {
			return getCurrentCodeModel().getIndividual(getCodeMetaModelUri() + "#Defined");
		}
		else if (usage.equals(USAGE.Used)) {
			return getCurrentCodeModel().getIndividual(getCodeMetaModelUri() + "#Used");
		}
		else if (usage.equals(USAGE.Reassigned)) {
			return getCurrentCodeModel().getIndividual(getCodeMetaModelUri() + "#Reassigned");
		}
		throw new AnswerExtractionException("Unexpected USAGE: " + usage.toString());
	}

	private Property getSetterArgumentProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#setterArgument");
	}

	private Property getUsageProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#usage");
	}

	private Property getCommentProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#comment");
	}

	private Property getCommentContentProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#commentContent");
	}

	private Property getSerializationProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#serialization");
	}
	
	private Property getArgumentsProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#cmArguments");
	}
	
	private Property getCallingVariableProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#callingVariable");
	}
	
	private Property getCalledVariableProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#calledVariable");
	}
	
	private OntClass getCodeVariableListClass() {
		Property argProp = getArgumentsProperty();
		StmtIterator stmtItr = getCurrentCodeModel().listStatements(argProp, RDFS.range, (RDFNode)null);
		if (stmtItr.hasNext()) {
			RDFNode rng = stmtItr.nextStatement().getObject();
			if (rng.asResource().canAs(OntClass.class)) {
				return rng.asResource().as(OntClass.class);
			}
		}
		return null;
	}
	
	private Property getCallsProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#calls");
	}

	private Property getBeginsAtProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#beginsAt");
	}

	private Property getEndsAtProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#endsAt");
	}

	private Property getVarNameProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#varName");
	}

	private Property getVarTypeProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#varType");
	}

	private Property getCodeBlockProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#codeBlock");
	}

	private Property getDoesComputationProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#doesComputation");
	}
	
	private Property getIncompleteInformationProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#incompleteInformation");
	}

	private Property getReferenceProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#reference");
	}

	private Property getContainedInProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#containedIn");
	}

	private Property getConstantValueProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#constantValue");
	}

	private Property getReturnTypeProperty() {
		return getCurrentCodeModel().getOntProperty(getCodeMetaModelUri() + "#cmReturnTypes");
	}

	private OntClass getStringListClass() {
		Property argProp = getReturnTypeProperty();
		StmtIterator stmtItr = getCurrentCodeModel().listStatements(argProp, RDFS.range, (RDFNode)null);
		if (stmtItr.hasNext()) {
			RDFNode rng = stmtItr.nextStatement().getObject();
			if (rng.asResource().canAs(OntClass.class)) {
				return rng.asResource().as(OntClass.class);
			}
		}
		return null;
	}
	
	private org.apache.jena.rdf.model.Resource getInputMappingClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#InputMapping");
	}

	private Property getInputMappingProperty() {
		return getCurrentCodeModel().getProperty(getCodeMetaModelUri() + "#inputMapping");
	}

	private org.apache.jena.rdf.model.Resource getOutputMappingClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#OutputMapping");
	}

	private Property getReturnedMappingProperty() {
		return getCurrentCodeModel().getProperty(getCodeMetaModelUri() + "#returnedMapping");
	}

	private org.apache.jena.rdf.model.Resource getReferenceClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#Reference");
	}

	private org.apache.jena.rdf.model.Resource getCodeBlockMethodClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#Method");
	}

	private org.apache.jena.rdf.model.Resource getCodeBlockConstructorClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#Constructor");
	}

	private OntClass getClassesToIgnoreClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#ClassesToIgnore");
	}
	
	private org.apache.jena.rdf.model.Resource getCodeBlockClassClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#Class");
	}

	private OntClass getMethodArgumentClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#MethodArgument");
	}

	private OntClass getCommentClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#Comment");
	}

	private OntClass getClassFieldClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#ClassField");
	}

	private OntClass getCodeVariableClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#CodeVariable");
	}
	
	private OntClass getUnittedQuantityClass() {
		return getCurrentCodeModel().getOntClass(getSadlImplicitModelUri() + "#UnittedQuantity");
	}

	private Property getUnittedQuantityUnitProperty() {
		return getCurrentCodeModel().getOntProperty(getSadlImplicitModelUri() + "#unit");
	}

	private Property getUnittedQuantityValueProperty() {
		return getCurrentCodeModel().getOntProperty(getSadlImplicitModelUri() + "#value");
	}

	private OntClass getMethodVariableClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#MethodVariable");
	}

	private Resource getConstantVariableClass() {
		return getCurrentCodeModel().getOntClass(getCodeMetaModelUri() + "#ConstantVariable");
	}

	private Comment getComment(Node node) {
		if (node != null) {
			Optional<Comment> cmnt = node.getComment();
			if (cmnt.isPresent()) {
				return node.getComment().get();
			}
		}
		return null;
	}

	public String getPackageName() {
		return packageName;
	}

	private void setPackageName(String packageName) {
		this.packageName = packageName;
	}

	public Map<String, String> getPreferences() {
		return preferences;
	}
	
	public String getPreference(String key) {
		if (preferences != null) {
			return preferences.get(key);
		}
		return null;
	}

	private void setPreferences(Map<String, String> preferences) {
		this.preferences = preferences;
	}

	private AnswerCurationManager getCurationMgr() {
		return curationMgr;
	}

	private void setCurationMgr(AnswerCurationManager curationMgr) {
		this.curationMgr = curationMgr;
	}

	@Override
	public void addCodeFile(File javaFile) {
		if (codeFiles == null) {
			codeFiles = new ArrayList<File>();
		}
		if (!codeFiles.contains(javaFile)) {
			codeFiles.add(javaFile);
		}
	}

	@Override
	public void addCodeFiles(List<File> javaFiles) {
		if (codeFiles != null) {
			for (File f : javaFiles) {
				if (!codeFiles.contains(f)) {
					codeFiles.add(f);
				}
			}
		}
		else {
			setCodeFiles(javaFiles);
		}
	}

	public List<File> getCodeFiles() {
		return codeFiles;
	}

	public void setCodeFiles(List<File> codeFiles) {
		this.codeFiles = codeFiles;
	}

	public OntModel getCurrentCodeModel() {
		return codeModel;
	}

	public void setCurrentCodeModel(OntModel codeModel) {
		this.codeModel = codeModel;
	}

	public String getCodeModelName() {
		return codeModelName;
	}

	public void setCodeModelName(String codeModelName) {
		if (codeModelName != null && !codeModelName.startsWith("http://")) {
			codeModelName = "http://" + codeModelName;
		}
		this.codeModelName = codeModelName;
	}
	
	@Override
	public String getCodeModelNamespace() {
		return codeModelName + "#";
	}

	@Override
	public String getCodeModelPrefix() {
		return codeModelPrefix;
	}

	public void setCodeModelPrefix(String codeModelPrefix) {
		this.codeModelPrefix = codeModelPrefix;
	}

	/**
	 * Method to add an import to the model identified by modelName
	 * @param modelName
	 * @param importUri
	 * @param importPrefix
	 * @param importedOntModel
	 */
	private void addImportToJenaModel(String modelName, String importUri, String importPrefix, Model importedOntModel) {
		getCurrentCodeModel().getDocumentManager().addModel(importUri, importedOntModel, true);
		Ontology modelOntology = getCurrentCodeModel().createOntology(modelName);
		if (importPrefix != null) {
			getCurrentCodeModel().setNsPrefix(importPrefix, ensureHashOnUri(importUri));
		}
		org.apache.jena.rdf.model.Resource importedOntology = getCurrentCodeModel().createResource(importUri);
		modelOntology.addImport(importedOntology);
		getCurrentCodeModel().addSubModel(importedOntModel);
		getCurrentCodeModel().addLoadedImport(importUri);
	}
	
	private String ensureHashOnUri(String uri) {
		if (!uri.endsWith("#")) {
			uri+= "#";
		}
		return uri;
	}

	private String getCodeMetaModelUri() {
		return codeMetaModelUri;
	}
	
	private String getSadlImplicitModelUri() {
		return "http://sadl.org/sadlimplicitmodel";
	}

	private String getSadlListModelUri() {
		return "http://sadl.org/sadllistmodel";
	}

	private void setCodeMetaModelUri(String codeMetaModelUri) {
		this.codeMetaModelUri = codeMetaModelUri;
	}

	public IConfigurationManagerForIDE getCodeModelConfigMgr() {
		return codeModelConfigMgr;
	}

	private void setCodeModelConfigMgr(IConfigurationManagerForIDE codeMetaModelConfigMgr) {
		this.codeModelConfigMgr = codeMetaModelConfigMgr;
	}

	private String getCodeMetaModelPrefix() {
		return codeMetaModelPrefix;
	}

	private void setCodeMetaModelPrefix(String codeMetaModelPrefix) {
		this.codeMetaModelPrefix = codeMetaModelPrefix;
	}

	@Override
	public Map<String,OntModel> getCodeModels() {
		return codeModels;
	}

	@Override
	public void addCodeModel(String key ,OntModel codeModel) {
		if (codeModels == null) {
			codeModels = new HashMap<String, OntModel>();
		}
		codeModels.put(key, codeModel);
	}

	@Override
	public OntModel getCodeModel(String key) {
		if (codeModels != null) {
			return codeModels.get(key);
		}
		return null;
	}

	private Individual getMethodWithBodyInProcess() {
		return methodWithBodyInProcess;
	}

	private Individual setMethodWithBodyInProcess(Individual methodWithBodyInProcess) {
		Individual prior = this.methodWithBodyInProcess;
		this.methodWithBodyInProcess = methodWithBodyInProcess;
		return prior;
	}
	
	@Override
	public ResultSet executeSparqlQuery(String query) throws ConfigurationException, ReasonerNotFoundException, IOException, InvalidNameException, QueryParseException, QueryCancelledException, AmbiguousNameException {
		query = SadlUtils.stripQuotes(query);
		IReasoner reasoner = getCodeModelConfigMgr().getReasoner();
		if (!reasoner.isInitialized()) {
			reasoner.setConfigurationManager(getCodeModelConfigMgr());
			reasoner.initializeReasoner(getCodeModelConfigMgr().getModelFolder(), getCodeModelName(), null);
		}
		query = reasoner.prepareQuery(query);
		ResultSet results =  reasoner.ask(query);
		return results;
	}
	

	/**
	 * Method to convert an Iterator over a List of values to a SADL Typed List in the provided model
	 * @param lastInst -- the list to which to add members
	 * @param cls -- the class of the SADL list
	 * @param type --  the type of the members of the list
	 * @param memberIterator -- Iterator over the values to add
	 * @return -- the list instance
	 * @throws JenaProcessorException
	 * @throws TranslationException
	 */
	protected Individual addMembersToList(OntModel model, Individual lastInst, OntClass cls,
			org.apache.jena.rdf.model.Resource type, Iterator<?> memberIterator) throws JenaProcessorException, TranslationException {
		if (lastInst == null) {
			lastInst = model.createIndividual(cls);
		}
		Object val = memberIterator.next();
		if (val instanceof Individual) {
			Individual listInst = (Individual) val;
			if (type.canAs(OntClass.class)) {
				ExtendedIterator<org.apache.jena.rdf.model.Resource> itr = listInst.listRDFTypes(false);
				boolean match = false;
				while (itr.hasNext()) {
					org.apache.jena.rdf.model.Resource typ = itr.next();
					if (typ.equals(type)) {
						match = true;
					} else {
						try {
							if (typ.canAs(OntClass.class) && SadlUtils.classIsSubclassOf(typ.as(OntClass.class), type.as(OntClass.class), true, null)) {
								match = true;
							}
						} catch (CircularDependencyException e) {
							e.printStackTrace();
						}
					}
					if (match) {
						break;
					}
				}
				if (!match) {
					throw new JenaProcessorException("The Instance '" + listInst.toString() + "' doesn't match the List type.");
				}
				model.add(lastInst, model.getProperty(SadlConstants.SADL_LIST_MODEL_FIRST_URI),
						listInst);
			} else {
				throw new JenaProcessorException("The type of the list could not be converted to a class.");
			}
		} else {
			Literal lval;
			if (val instanceof Literal) {
				lval = (Literal) val;
			}
			else {
				lval = SadlUtils.getLiteralMatchingDataPropertyRange(model,type.getURI(), val);
			}
			model.add(lastInst, model.getProperty(SadlConstants.SADL_LIST_MODEL_FIRST_URI), lval);
		}
		if (memberIterator.hasNext()) {
			Individual rest = addMembersToList(model, null, cls, type, memberIterator);
			model.add(lastInst, model.getProperty(SadlConstants.SADL_LIST_MODEL_REST_URI), rest);
		}
		return lastInst;
	}
	
	/** Method to return the members of a SADL typed list
	 * 
	 * @param model
	 * @param typedList
	 * @return
	 */
	protected List<Individual> getMembersOfList(OntModel model, Individual typedList) {
		List<Individual> members = new ArrayList<Individual>();
		Individual rest = typedList;
		while (rest != null) {
			RDFNode first = rest.getPropertyValue(model.getProperty(SadlConstants.SADL_LIST_MODEL_FIRST_URI));
			if (first != null && first.isResource() && first.asResource().canAs(Individual.class)) {
				members.add(first.asResource().as(Individual.class));
				RDFNode restRdfNode = rest.getPropertyValue(model.getProperty(SadlConstants.SADL_LIST_MODEL_REST_URI));
				if (restRdfNode != null && restRdfNode.canAs(Individual.class)) {
					rest = restRdfNode.as(Individual.class);
				}
				else {
					rest = null;
				}
			}
		}
		return members;
	}
	
//	@Override
//	public String[] extractPythonTFEquationFromCodeExtractionModel(String pythonScript) {
//		return extractPythonTFEquationFromCodeExtractionModel(pythonScript, null);
//	}
//
//	@Override
//	public String[] extractPythonTFEquationFromCodeExtractionModel(String pythonScript, String defaultMethodName) {
//		String modifiedScript = pythonToTensorFlowPython(pythonScript);		
//		return extractPythonEquationFromCodeExtractionModel(modifiedScript, defaultMethodName);
//	}
//	
	@Override
	public String[] extractPythonEquationFromCodeExtractionModel(String pythonScript) {
		return extractPythonEquationFromCodeExtractionModel(pythonScript, null);
	}

	@Override
	public String[] extractPythonEquationFromCodeExtractionModel(String pythonScript, String defaultMethodName) {
		/*
		 * A typical script coming from the Java to Python service looks like this (indentation preserved):

#!/usr/bin/env python
"""""" generated source for module inputfile """"""
class Mach(object):
    """""" generated source for class Mach """"""
    def CAL_SOS(self, T, G, R, Q):
        """""" generated source for method CAL_SOS """"""
        WOW = 1 + (G - 1) / (1 + (G - 1) * Math.pow((Q / T), 2) * Math.exp(Q / T) / Math.pow((Math.exp(Q / T) - 1), 2))
        return (Math.sqrt(32.174 * T * R * WOW))

		 * We need to find the line containing "generated source for method <methodName>" and extract the name of the method.
		 * Then we need to replace the "return " with "<methodName> =".
		 * If there are multiple rows (newlines) we need to place the number of spaces that the row ending in the newline is indented from the "def <methName>..." line.
		 * This becomes the name of the output variable in K-CHAIN.
		 */
		String methName = null;
		StringBuilder sb = new StringBuilder();
		int indent = -1;
		Scanner scanner = new Scanner(pythonScript);
		String lastLine = "";
		int lineCnt = 0;
		int firstLine = 0;
		while (scanner.hasNextLine()) {
			String line = scanner.nextLine();
			String trimmedLine = line.trim();
			if (methName != null) {
				if (lineCnt > firstLine) {
					for (int i = 0; i < indent; i++) {	// provide correct indent
						sb.append(" ");
					}
				}
				int returnIdx = trimmedLine.indexOf("return");
				if (returnIdx >= 0) {
					sb.append(methName);
					sb.append(" = ");
					sb.append(trimmedLine.substring(returnIdx + 6));
				}
				else {
					sb.append(trimmedLine);
				}
			}
			int idx = trimmedLine.indexOf("generated source for method");
			if (idx > 0) {
				methName = trimmedLine.substring(idx + 28);
				for (int i = 0; i < methName.length(); i++) {
					if (methName.charAt(i) == '\"' || Character.isWhitespace(methName.charAt(i))) {
						methName  = methName.substring(0, i);
						break;
					}
				}
				int lastLineIndent = getLineIndent(lastLine);
				int thisLineIndent = getLineIndent(line);
				indent = thisLineIndent - lastLineIndent;
				firstLine = lineCnt + 1;
			}
			lastLine = line;
			if (scanner.hasNextLine() && methName != null && sb.length() > 0) {
				sb.append("\n");
			}
			lineCnt++;
		}
		scanner.close();
	    String modifiedScript = sb.length() > 0 ? sb.toString() : pythonScript;
		String[] returns = new String[2];
		returns[0] = methName != null ? methName : defaultMethodName;
		returns[1] = modifiedScript;
		return returns;
	}
	
	private int getLineIndent(String line) {
		int indent = 0;
		for (int lidx = 0; lidx < line.length(); lidx++) {
			if (Character.isWhitespace(line.charAt(lidx))) {
				indent++;
			}
			else {
				break;
			}
		}
		return indent;
	}

	private boolean addClassUriToMap(String simpleName, String uri) {
		if (!classNameMap.containsKey(simpleName)) {
			classNameMap.put(simpleName, uri);
			return true;
		}
		return false;
	}
	
	@Override
	public String getClassUriFromSimpleName(String name) {
		if (classNameMap.containsKey(name)) {
			return classNameMap.get(name);
		}
		return null;
	}

	private HashMap<String, String> getMethodJavadoc() {
		return methodJavadoc;
	}

	/**
	 * Method to get the Javadoc comment, if any, for the method identified by URI
	 * @param methodUri
	 * @return -- Javadoc comment for method else null if none exists
	 */
	public String getMethodJavadoc(String methodUri) {
		return methodJavadoc.get(methodUri);
	}
	
	/**
	 * Method to get the Javadoc comment associated with a specfic parameter, if it exists
	 * @param methodUri
	 * @param argName
	 * @return -- Javadoc comment for the argument else null if none is found
	 */
	public String getParameterJavadoc(String methodUri, String argName) {
		if (argName.indexOf('.') > 0) {
			argName = argName.substring(argName.lastIndexOf('.') + 1);
		}
		String jd = getMethodJavadoc(methodUri);
		if (jd != null) {
			int paramIdx = jd.indexOf("@param");
			while (paramIdx > -1) {
				String rest = jd.substring(paramIdx + 6).trim();
				int idx2 = 0;
				char c;
				do {
					c = rest.charAt(idx2++);
				} while (!Character.isWhitespace(c));
				String an = rest.substring(0, idx2).trim();
				if (an.equals(argName)) {
					String cmt = rest.substring(idx2).trim();
					int endCmt = cmt.indexOf("@");
					if (endCmt > 0) {
						cmt = cmt.substring(0, endCmt);
					}
					int end = cmt.length() - 1;
					if (end > 0) {
						do {
							c = cmt.charAt(end--);
						} while (end >= 0 && Character.isWhitespace(c) || c == '*' || c == '/' || c == '.');
						if (end < cmt.length() - 1) {
							cmt = cmt.substring(0, end + 2);
						}
					}
					return cmt;
				}	
				paramIdx = jd.indexOf("@param", paramIdx + 6);
			}
		}
		return null;
	}
	
	/**
	 * Method to get the Javadoc comment associated with the method return
	 * @param methodUri
	 * @return -- Javadoc comment for the method return else null if none found
	 */
	public String getReturnJavadoc(String methodUri) {
		String jd = getMethodJavadoc(methodUri);
		if (jd != null) {
			int paramIdx = jd.indexOf("@return");
			if (paramIdx > -1) {
				String cmt = jd.substring(paramIdx + 7).trim();
				int endCmt = cmt.indexOf("@");
				if (endCmt > 0) {
					cmt = cmt.substring(0, endCmt);
				}
				int end = cmt.length() - 1;
				char c;
				do {
					c = cmt.charAt(end--);
				} while (Character.isWhitespace(c) || c == '*' || c == '/' || c == '.');
				if (end < cmt.length() - 2) {
					cmt = cmt.substring(0, end + 2);
				}
				return cmt;
			}
		}
		return null;
	}

}

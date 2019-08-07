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
package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.jena.inference.SadlJenaModelGetterPutter;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.IConfigurationManagerForEditing.Scope;
import com.ge.research.sadl.reasoner.IReasoner;
import com.ge.research.sadl.reasoner.ITranslator;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.github.javaparser.JavaParser;
import com.github.javaparser.Range;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.Node;
import com.github.javaparser.ast.NodeList;
import com.github.javaparser.ast.PackageDeclaration;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.comments.BlockComment;
import com.github.javaparser.ast.comments.Comment;
import com.github.javaparser.ast.expr.AssignExpr;
import com.github.javaparser.ast.expr.BinaryExpr;
import com.github.javaparser.ast.expr.EnclosedExpr;
import com.github.javaparser.ast.expr.Expression;
import com.github.javaparser.ast.expr.MethodCallExpr;
import com.github.javaparser.ast.expr.NameExpr;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.stmt.BlockStmt;
import com.github.javaparser.ast.stmt.ExpressionStmt;
import com.github.javaparser.ast.stmt.IfStmt;
import com.github.javaparser.ast.stmt.ReturnStmt;
import com.github.javaparser.symbolsolver.JavaSymbolSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.CombinedTypeSolver;
import com.github.javaparser.symbolsolver.resolution.typesolvers.ReflectionTypeSolver;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Literal;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFList;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.Resource;

public class JavaModelExtractorJP implements IModelFromCodeExtractor {
    private static final Logger logger = Logger.getLogger (JavaModelExtractorJP.class) ;
	private String packageName = "";
	private AnswerCurationManager curationMgr = null;
	
	public enum USAGE {Defined, Used, Reassigned}
	public enum InputOutput {Input, Output}
	private Map<String, String> preferences;
	private List<File> codeFiles;
	
	// Assumption: the code meta model and the code model extracted are in the same codeModelFolder (same kbase)
	private String codeModelFolder = null;
	private IConfigurationManagerForIDE codeModelConfigMgr;	// the ConfigurationManager used to access the code extraction model
	private String codeMetaModelUri;	// the name of the code extraction metamodel
	private String codeMetaModelPrefix;	// the prefix of the code extraction metamodel
	private OntModel codeModel;
	private Map<String,OntModel> codeModels = null;
	private String codeModelName;	// the name of the model  being created by extraction
	private String codeModelPrefix; // the prefix of the model being created by extraction

	private Individual rootContainingInstance = null;
	private boolean includeSerialization = true;
	private String defaultCodeModelName = null;
	private String defaultCodeModelPrefix = null;
	private Map<Node, Individual> postProcessingList = new HashMap<Node, Individual>();
	private Map<Node,Individual> methodsFound = new HashMap<Node, Individual>();
	private Individual methodWithBodyInProcess = null;
	private boolean sendCommentsToTextService = false;	// change to true to send comments to text-to-triples service
	
	public JavaModelExtractorJP(AnswerCurationManager acm, Map<String, String> preferences) {
		setCurationMgr(acm);
		this.setPreferences(preferences);
	    logger.setLevel(Level.ALL);
	}
	
	private void initializeContent() {
		packageName = "";
		postProcessingList.clear();
	}

	public boolean process(String inputIdentifier, String content) throws ConfigurationException, IOException {
	    initializeContent();
		String defName = getDefaultCodeModelName() + "_comments";
		getCurationMgr().getTextProcessor().setTextmodelName(defName);
		String defPrefix = getDefaultCodeModelPrefix() + "_cmnt";
		getCurationMgr().getTextProcessor().setTextmodelPrefix(defPrefix);
		parse(inputIdentifier, getCurationMgr().getDomainModelOwlModelsFolder(), content);
		return true;
	}
	
	//use ASTParse to parse string
	private void parse(String inputIdentifier, String modelFolder, String javaCodeContent) throws IOException, ConfigurationException {
		try {
			String msg = "Parsing code file '" + inputIdentifier + "'.";
			getCurationMgr().notifyUser(modelFolder, msg);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
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
        else {
    		setCodeModelName(getDefaultCodeModelName());
    		setCodeModelPrefix(getDefaultCodeModelPrefix());

        }
        initializeCodeModel(getCodeModelFolder());
        processBlock(cu, null);
        postProcess();
	}

	private void postProcess() {
		Iterator<Node> nitr = postProcessingList.keySet().iterator();
		while (nitr.hasNext()) {
			Node node = nitr.next();
			Node containerNode = null;
			if (node instanceof MethodCallExpr) {
				Optional<Node> optContainer = ((MethodCallExpr)node).getParentNode();
				if (optContainer.isPresent()) {
					containerNode = optContainer.get();
				}
				NodeList<Expression> args = ((MethodCallExpr)node).getArguments();
				String methName = ((MethodCallExpr)node).getNameAsString();
				Individual methodCalled = findMethodCalled((MethodCallExpr)node);
				// create instance of MethodCall, link to methodCalled
				Individual methodCall = getCodeModel().createIndividual(getMethodCallClass());
				methodCalled.addProperty(getCallsProperty(), methodCall);
				Individual cb = postProcessingList.get(node);
				methodCall.addProperty(getCodeBlockProperty(), cb);
				
				addRange(methodCall, node);

				addInputMapping(methodCalled, (MethodCallExpr)node);
				addOutputMapping(methodCalled, (MethodCallExpr)node);
			}
			else {
				System.err.println("Unexprected node type in postProcessingList: " + node.getClass().getCanonicalName());
			}
		}
	}

	private boolean addRange(Individual blockInst, Node blockNode) {
		Optional<Range> rng = blockNode.getRange();
		if (rng.isPresent()) {
			blockInst.addProperty(getBeginsAtProperty(), getCodeModel().createTypedLiteral(rng.get().begin.line));
			blockInst.addProperty(getEndsAtProperty(), getCodeModel().createTypedLiteral(rng.get().end.line));
			return true;
		}
		return false;
	}

	private void addOutputMapping(Individual methodCalled, MethodCallExpr node) {
		// TODO Auto-generated method stub
		int i = 0;
	}

	private void addInputMapping(Individual methodCalled, MethodCallExpr node) {
		// TODO Auto-generated method stub
		int i = 0;
	}

	private Individual findMethodCalled(MethodCallExpr node) {
		Iterator<Node> nitr = methodsFound.keySet().iterator();
		while (nitr.hasNext()) {
			Node meth = nitr.next();
			if (meth instanceof MethodDeclaration && ((MethodDeclaration) meth).getNameAsString().equals(node.getNameAsString())) {
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
		return getCodeModel().createIndividual(extUri, getExternalModelClass());
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
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#ExternalMethod");
	}
	
	private Resource getMethodCallClass() {
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#MethodCall");
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
		if (pkg.contains(".")) {
			StringBuilder sb = new StringBuilder();
			int lastStart = 0;
			int dotLoc = pkg.indexOf('.');
			while (dotLoc > 0) {
				sb.append(pkg.substring(lastStart, lastStart + 1));
				lastStart = dotLoc + 1;
				dotLoc = pkg.indexOf('.', dotLoc + 1);
			}
			return sb.toString();
		}
		else {
			return pkg;
		}
	}

	private void initializeCodeModel(String extractionMetaModelModelFolder) throws ConfigurationException, IOException {
		if (getCurationMgr().getExtractionProcessor().getCodeModel() == null) {
			// create new code model
			
			setCodeModelConfigMgr(ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(extractionMetaModelModelFolder, null)); //getCurationMgr().getProjectConfigurationManager());
			OntDocumentManager owlDocMgr = getCodeModelConfigMgr().getJenaDocumentMgr();
			OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
			if (extractionMetaModelModelFolder != null) { // && !modelFolderPathname.startsWith(SYNTHETIC_FROM_TEST)) {
				File mff = new File(extractionMetaModelModelFolder);
				mff.mkdirs();
				spec.setImportModelGetter(new SadlJenaModelGetterPutter(spec, extractionMetaModelModelFolder));
			}
			if (owlDocMgr != null) {
				spec.setDocumentManager(owlDocMgr);
				owlDocMgr.setProcessImports(true);
			}
			getCurationMgr().getExtractionProcessor().setCodeModel(ModelFactory.createOntologyModel(spec));	
			setCodeModel(getCurationMgr().getExtractionProcessor().getCodeModel());
			getCodeModel().setNsPrefix(getCodeModelPrefix(), getCodeModelNamespace());
			Ontology modelOntology = getCodeModel().createOntology(getCodeModelName());
			logger.debug("Ontology '" + getCodeModelName() + "' created");
			modelOntology.addComment("This ontology was created by extraction from code by the ANSWER JavaModelExtractorJP.", "en");
			setCodeMetaModelUri(DialogConstants.CODE_EXTRACTION_MODEL_URI);
			setCodeMetaModelPrefix(DialogConstants.CODE_EXTRACTION_MODEL_PREFIX);
			OntModel importedOntModel = getCodeModelConfigMgr().getOntModel(getCodeMetaModelUri(), Scope.INCLUDEIMPORTS);
			addImportToJenaModel(getCodeModelName(), getCodeMetaModelUri(), getCodeMetaModelPrefix(), importedOntModel);
		}
		else {
			setCodeModel(getCurationMgr().getExtractionProcessor().getCodeModel());
		}
		
	}

	private void processBlock(Node blkNode, Individual containingInst) {
		List<Node> childNodes = blkNode.getChildNodes();
		for (int i = 0; i < childNodes.size(); i++) {
			Node childNode = childNodes.get(i);
			processBlockChild(childNode, containingInst, null);
		}
		investigateComments(blkNode, containingInst);
	}

	private void processBlockChild(Node childNode, Individual containingInst, USAGE knownUsage) {
		if (childNode instanceof ClassOrInterfaceDeclaration) {
			ClassOrInterfaceDeclaration cls = (ClassOrInterfaceDeclaration) childNode;
			Individual blkInst = getOrCreateClass(cls);
			if (containingInst != null) {
				getCodeModel().add(blkInst, getContainedInProperty(), containingInst);
			}
			else {
				setRootContainingInstance(blkInst);
			}
			processBlock(cls, blkInst);
			addSerialization(blkInst, cls.toString());
		}
		else if (childNode instanceof FieldDeclaration) {
			FieldDeclaration fd = (FieldDeclaration) childNode;
			NodeList<VariableDeclarator> vars = fd.getVariables();
			vars.forEach(var -> {
				processBlockChild(var, containingInst, USAGE.Defined);
			});
		}
		else if (childNode instanceof MethodDeclaration) {
			MethodDeclaration m = (MethodDeclaration) childNode;
			Comment cmnt = getComment(m);
			Individual methInst = getOrCreateMethod(m, containingInst);
			methodsFound.put(m, methInst);
			if (containingInst != null) {
				getCodeModel().add(methInst, getContainedInProperty(), containingInst);
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
				} catch (CodeExtractionException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
			if (argList != null) {
				RDFList argInstList = getCodeModel().createList(argList.iterator());
				methInst.addProperty(getArgumentsProperty(), argInstList);
			}
			addRange(methInst, m);
			Individual prior = setMethodWithBodyInProcess(methInst);
			processBlock(m, methInst);	// order matters--do this after parameters and before return
			setMethodWithBodyInProcess(prior);
			
			String rt = m.getTypeAsString();
			if (rt != null) {
				List<Literal> rtypes = new ArrayList<Literal>();
				rtypes.add(getCodeModel().createTypedLiteral(rt));
				RDFList rtList = getCodeModel().createList(rtypes.iterator());
				methInst.addProperty(getReturnTypeProperty(), rtList);
			}
			System.out.println(methInst.getURI() + " returns " + ((rt != null && rt.length() > 0) ? rt : "void"));
			addSerialization(methInst, ((MethodDeclaration) childNode).toString());
		}
		else if (childNode instanceof MethodCallExpr) {
			MethodCallExpr mc = (MethodCallExpr)childNode;
			addNodeToPostProcessingList(mc, containingInst);
        	NodeList<Expression> args = mc.getArguments();
        	Iterator<Expression> nlitr = args.iterator();
        	while (nlitr.hasNext()) {
        		Expression expr = nlitr.next();
        		if (expr instanceof NameExpr) {
        			try {
						setSetterArgument(mc, (NameExpr)expr, containingInst);
					} catch (CodeExtractionException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
        		}
        		else {
           			processBlockChild(expr, containingInst, USAGE.Defined);        			
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
			} catch (CodeExtractionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
		}
		else if (childNode instanceof VariableDeclarator) {
			try {
				OntClass codeVarClass = getClassFieldClass();	// TODO is this always field or only in a class?
				Individual fdInst = getOrCreateCodeVariable(childNode, containingInst, codeVarClass);
			} catch (CodeExtractionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (childNode instanceof AssignExpr) {
			AssignExpr ass = (AssignExpr)childNode;
			List<Node> assChildren = ass.getChildNodes();
			for (int j = 0; j < assChildren.size(); j++) {
				processBlockChild(assChildren.get(j), containingInst, (j == 0 ? USAGE.Reassigned : USAGE.Used));					
			}
		}
		else if (childNode instanceof BinaryExpr) {
			containingInst.setPropertyValue(getDoesComputationProperty(), getCodeModel().createTypedLiteral(true));
			processBlock(childNode, containingInst);
		}
		else if (childNode instanceof NameExpr) {
			String nm = ((NameExpr)childNode).getNameAsString();
			findCodeVariableAndAddReference(childNode, nm, containingInst, knownUsage, true, null, false);
		}
		else if (childNode instanceof IfStmt) {
			Expression cond = ((IfStmt)childNode).getCondition();
			processBlockChild(cond, containingInst, USAGE.Used);
			List<Node> condChildren = ((IfStmt)childNode).getChildNodes();
			for (int j = 1; j < condChildren.size(); j++) {
				processBlockChild(condChildren.get(j), containingInst, null);
			}
		}
		else if (childNode instanceof ReturnStmt) {
			List<Node> returnChildren = ((ReturnStmt)childNode).getChildNodes();
			if (!returnChildren.isEmpty()) {
				Node ret0 = returnChildren.get(0);
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
//			System.out.println("Ignoring '" + childNode.toString() + "'");
//		}
		else {
			System.err.println("Block child unhandled Node '" + childNode.toString().trim() + "' of type " + childNode.getClass().getCanonicalName());
		}
		investigateComments(childNode, containingInst);
	}

	private void addNodeToPostProcessingList(Node expr, Individual codeBlock) {
		postProcessingList.put(expr, codeBlock);
	}

	private void addSerialization(Individual blkInst, String code) {
		if (isIncludeSerialization()) {
			blkInst.addProperty(getSerializationProperty(), getCodeModel().createTypedLiteral(code));
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
		Comment cmt = getComment(childNode);
		investigateComment(childNode, subject, cmt);
		List<Comment> ocmts = childNode.getOrphanComments();
		for (int i = 0; ocmts != null && i < ocmts.size(); i++) {
			cmt = ocmts.get(i);
			Optional<Range> rng = cmt.getRange();
			if (rng.isPresent()) {
				System.out.println("Found orphaned comment at line " + rng.get().getLineCount() + "(" + rng.get().begin.toString() + " to " + rng.get().end.toString() + ")");
			}
			else {
				System.out.println("Found orphaned comment but range not known");
			}
			System.out.println("   " + cmt.getContent());
		}
	}

	private void investigateComment(Node childNode, Individual subject, Comment cmt) {
		if (cmt != null) {
			System.out.println("   " + cmt.getContent());
			Individual cmtInst = getCodeModel().createIndividual(getCommentClass());
			if (subject == null) {
				subject = rootContainingInstance;
			}
			if (sendCommentsToTextService ) {
				String locality = null;
				String inputIdentifier = "CodeComments";
				String tpresult = null;
				try {
					tpresult = getCurationMgr().getTextProcessor().process(inputIdentifier, cmt.getContent(), locality);
				} catch (ConfigurationException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println("Text: " + cmt.getContent());
				System.out.println("   returned: " + tpresult);
			}

			if (subject != null) {
				subject.addProperty(getCommentProperty(), cmtInst);
				cmtInst.addProperty(getCommentContentProperty(), getCodeModel().createTypedLiteral(cmt.getContent()));
				addRange(cmtInst, childNode);
			}
			else {
				System.err.println("Unable to add comment because there is no known subject");
			}
			Optional<Range> rng = childNode.getRange();
			if (rng.isPresent()) {
				System.out.println("Found comment at line " + rng.get().getLineCount() + "(" + rng.get().begin.toString() + " to " + rng.get().end.toString() + ")");
			}
			else {
				System.out.println("Found comment but range not known");
			}
		}
	}

	private Property getInputProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#input");
	}

	private Property getOutputProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#output");
	}

	private Individual findCodeVariableAndAddReference(Node childNode, String nm, Individual containingInst, USAGE usage, boolean lookToLargerScope, InputOutput inputOutput, boolean isSetterArgument) {
		String nnm = constructNestedElementUri(childNode, nm);

		Individual varInst = lookToLargerScope ? findDefinedVariable(nm, containingInst) : null;
		if (varInst == null && !lookToLargerScope) {
			try {
				varInst = getOrCreateCodeVariable(childNode, nm, nnm, containingInst, getCodeVariableClass(childNode), inputOutput);
			} catch (CodeExtractionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else if (varInst != null) {
			try {
				Individual ref = createReference(childNode, varInst, containingInst, usage != null ? usage : USAGE.Used);
	          	setInputOutputIfKnown(ref, inputOutput);
	          	if (isSetterArgument) {
	          		ref.setPropertyValue(getSetterArgumentProperty(), getCodeModel().createTypedLiteral(true));
	          	}
			} catch (CodeExtractionException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		else {
			System.err.println("NameExpr (" + nnm + ") not found; it should already exist!");
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
					else if (parent.get() instanceof ClassOrInterfaceDeclaration) {
						return getClassFieldClass();
					}
				}
				parent = parent.get().getParentNode();
			}
		}
		return null;
	}

	private Individual findDefinedVariable(String nm, Individual containingInst) {
		Individual inst = containingInst;
		String nnm = getCodeModelNamespace() + inst.getLocalName() + "." + nm;
		Individual varInst = getCodeModel().getIndividual(nnm);
		if (varInst == null) {
			RDFNode obj = containingInst.getPropertyValue(getContainedInProperty());
			if (obj != null && obj.isURIResource() && obj.canAs(Individual.class)) {
				return findDefinedVariable(nm, obj.as(Individual.class));
			}
		}
		return varInst;
	}

	private void setSetterArgument(MethodCallExpr mc, NameExpr arg, Individual containingInst) throws CodeExtractionException {
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

	private Individual createReference(Node varNode, Individual codeVarInst, Individual containingInst, USAGE usage) throws CodeExtractionException {
		Individual ref = createNewReference(containingInst, varNode, usage);
		codeVarInst.addProperty(getReferenceProperty(), ref);
		return ref;
	}

//	private Individual createNewReference(Individual blkInst, int beginsAt, int endsAt, USAGE usage) throws CodeExtractionException {
	private Individual createNewReference(Individual blkInst, Node varNode, USAGE usage) throws CodeExtractionException {
		Individual refInst = getCodeModel().createIndividual(getReferenceClass());
		if (blkInst != null) {
			refInst.addProperty(getCodeBlockProperty(), blkInst);
		}
		refInst.addProperty(getUsageProperty(), getUsageInstance(usage));
		addRange(refInst, varNode);
		return refInst;
	}

	private Individual getOrCreateMethod(MethodDeclaration m, Individual containingInst) {
		Individual methInst = null;
    	String nm = m.getNameAsString();
		String nnm = constructNestedElementUri(m, nm);
    	methInst = getCodeModel().getIndividual(getCodeModelNamespace() + nnm);
    	if (methInst == null) {
    		methInst = getCodeModel().createIndividual(getCodeModelNamespace() + nnm, getCodeBlockMethodClass());
    	}
		return methInst;
	}

	private Individual getOrCreateCodeVariable(Node varNode, Individual containingInst, OntClass codeVarClass) throws CodeExtractionException {
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
					System.err.println("Multiple vars (" + varNode.toString() + ") in VariableDeclarationExpr not current handled");
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
			throw new CodeExtractionException("Unexpected CodeVariable varNode type: " + varNode.getClass().getCanonicalName());
		}
		cvInst = getOrCreateCodeVariable(varNode, origName, nnm, containingInst, codeVarClass, null);
		return cvInst;
	}

	private Individual getOrCreateCodeVariable(Node varNode, String origName, String nnm, Individual containingInst,
			OntClass codeVarClass, InputOutput inputOutput) throws CodeExtractionException {
		Individual cvInst;
		cvInst = getCodeModel().getIndividual(getCodeModelNamespace() + nnm);
		if (cvInst == null && codeVarClass != null) {
			cvInst = getCodeModel().createIndividual(getCodeModelNamespace() + nnm, codeVarClass);
          	Individual ref = createReference(varNode, cvInst, containingInst, USAGE.Defined);
          	setInputOutputIfKnown(ref, inputOutput);
          	cvInst.addProperty(getVarNameProperty(), getCodeModel().createTypedLiteral(origName));
          	// TODO add varType
		}
		return cvInst;
	}

	private void setInputOutputIfKnown(Individual ref, InputOutput inputOutput) {
		if (inputOutput != null) {
			if (inputOutput.equals(InputOutput.Input)) {
				ref.addProperty(getInputProperty(), getCodeModel().createTypedLiteral(true));
			}
			else if (inputOutput.equals(InputOutput.Output)) {
				ref.addProperty(getOutputProperty(), getCodeModel().createTypedLiteral(true));
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
    	clsInst = getCodeModel().getIndividual(getCodeModelNamespace() + nmm); //getCodeModelNamespace() + nm);
    	if (clsInst == null) {
    		clsInst = getCodeModel().createIndividual(getCodeModelNamespace() + nmm, getCodeBlockClassClass()); //getCodeModelNamespace() + nm, getCodeBlockClassClass());
    	}
    	return clsInst;
	}

	private Individual getUsageInstance(USAGE usage) throws CodeExtractionException {
		if (usage.equals(USAGE.Defined)) {
			return getCodeModel().getIndividual(getCodeMetaModelUri() + "#Defined");
		}
		else if (usage.equals(USAGE.Used)) {
			return getCodeModel().getIndividual(getCodeMetaModelUri() + "#Used");
		}
		else if (usage.equals(USAGE.Reassigned)) {
			return getCodeModel().getIndividual(getCodeMetaModelUri() + "#Reassigned");
		}
		throw new CodeExtractionException("Unexpected USAGE: " + usage.toString());
	}

	private Property getSetterArgumentProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#setterArgument");
	}

	private Property getUsageProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#usage");
	}

	private Property getCommentProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#comment");
	}

	private Property getCommentContentProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#commentContent");
	}

	private Property getSerializationProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#serialization");
	}
	
	private Property getArgumentsProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#cmArguments");
	}
	
	private Property getCallsProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#calls");
	}

	private Property getBeginsAtProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#beginsAt");
	}

	private Property getEndsAtProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#endsAt");
	}

	private Property getVarNameProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#varName");
	}

	private Property getVarTypeProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#varType");
	}

	private Property getCodeBlockProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#codeBlock");
	}

	private Property getDoesComputationProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#doesComputation");
	}
	
	private Property getReferenceProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#reference");
	}

	private Property getContainedInProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#containedIn");
	}

	private Property getReturnTypeProperty() {
		return getCodeModel().getOntProperty(getCodeMetaModelUri() + "#cmReturnTypes");
	}

	private com.hp.hpl.jena.rdf.model.Resource getReferenceClass() {
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#Reference");
	}

	private com.hp.hpl.jena.rdf.model.Resource getCodeBlockMethodClass() {
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#Method");
	}

	private com.hp.hpl.jena.rdf.model.Resource getCodeBlockClassClass() {
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#Class");
	}

	private OntClass getMethodArgumentClass() {
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#MethodArgument");
	}

	private OntClass getCommentClass() {
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#Comment");
	}

	private OntClass getClassFieldClass() {
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#ClassField");
	}

	private OntClass getMethodVariableClass() {
		return getCodeModel().getOntClass(getCodeMetaModelUri() + "#MethodVariable");
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

	private OntModel getCodeModel() {
		return codeModel;
	}

	private void setCodeModel(OntModel codeModel) {
		this.codeModel = codeModel;
	}

	public String getCodeModelName() {
		return codeModelName;
	}

	@Override
	public void setCodeModelName(String codeModelName) {
		this.codeModelName = codeModelName;
		getCurationMgr().getExtractionProcessor().setCodeModelName(codeModelName);
	}
	
	@Override
	public String getCodeModelNamespace() {
		return codeModelName + "#";
	}

	@Override
	public String getCodeModelPrefix() {
		return codeModelPrefix;
	}

	@Override
	public void setCodeModelPrefix(String codeModelPrefix) {
		getCurationMgr().getExtractionProcessor().setCodeModelPrefix(codeModelPrefix);
		this.codeModelPrefix = codeModelPrefix;
	}

	public String getDefaultCodeModelName() {
//		if (defaultCodeModelName == null) {
//			defaultCodeModelName = "http://com.ge.reasearch.sadl.darpa.aske.answer/DefaultModelName";
//		}
		return defaultCodeModelName;
	}

	public void setDefaultCodeModelName(String defCodeModelName) {
		this.defaultCodeModelName = defCodeModelName;
		getCurationMgr().getExtractionProcessor().setCodeModelName(codeModelName);
	}

	public String getDefaultCodeModelPrefix() {
//		if (defaultCodeModelPrefix == null) {
//			defaultCodeModelPrefix = "defmdlnm";
//		}
		return defaultCodeModelPrefix;
	}

	public void setDefaultCodeModelPrefix(String codeModelPrefix) {
		this.defaultCodeModelPrefix = codeModelPrefix;
	}

	private void addImportToJenaModel(String modelName, String importUri, String importPrefix, Model importedOntModel) {
		getCodeModel().getDocumentManager().addModel(importUri, importedOntModel, true);
		Ontology modelOntology = getCodeModel().createOntology(modelName);
		if (importPrefix != null) {
			getCodeModel().setNsPrefix(importPrefix, importUri);
		}
		com.hp.hpl.jena.rdf.model.Resource importedOntology = getCodeModel().createResource(importUri);
		modelOntology.addImport(importedOntology);
		getCodeModel().addSubModel(importedOntModel);
		getCodeModel().addLoadedImport(importUri);
	}

	private String getCodeMetaModelUri() {
		return codeMetaModelUri;
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

	public String getCodeModelFolder() {
		return codeModelFolder;
	}

	public void setCodeModelFolder(String codeMetaModelModelFolder) {
		this.codeModelFolder = codeMetaModelModelFolder;
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
	public ResultSet executeSparqlQuery(String query) throws ConfigurationException, ReasonerNotFoundException, IOException, InvalidNameException, QueryParseException, QueryCancelledException {
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
}

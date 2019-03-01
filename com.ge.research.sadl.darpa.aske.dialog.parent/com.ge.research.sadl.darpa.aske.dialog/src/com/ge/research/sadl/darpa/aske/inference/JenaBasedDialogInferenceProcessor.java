package com.ge.research.sadl.darpa.aske.inference;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.emf.ecore.resource.Resource;

import com.ge.research.sadl.builder.ConfigurationManagerForIDE;
import com.ge.research.sadl.jena.JenaBasedSadlInferenceProcessor;
import com.ge.research.sadl.jena.UtilsForJena;
import com.ge.research.sadl.model.gp.Literal;
import com.ge.research.sadl.model.gp.NamedNode;
import com.ge.research.sadl.model.gp.Node;
import com.ge.research.sadl.model.gp.TripleElement;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.processing.SadlInferenceException;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.ConfigurationManager;
import com.ge.research.sadl.reasoner.TranslationException;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.ge.research.sadl.utils.ResourceManager;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.vocabulary.RDF;

public class JenaBasedDialogInferenceProcessor extends JenaBasedSadlInferenceProcessor {

	public static final String METAMODEL_PREFIX = "http://aske.ge.com/metamodel#";
	public static final String METAMODEL_QUERY_CLASS = METAMODEL_PREFIX + "CGQuery";
	public static final String METAMODEL_INPUT_PROP = METAMODEL_PREFIX + "input";
	public static final String METAMODEL_OUTPUT_PROP = METAMODEL_PREFIX + "output";
	public static final String METAMODEL_CCG = METAMODEL_PREFIX + "CCG";
	public static final String METAMODEL_SUBGRAPH = METAMODEL_PREFIX + "SubGraph";
	public static final String METAMODEL_SUBG_PROP = METAMODEL_PREFIX + "subgraph";
	public static final String METAMODEL_CGRAPH_PROP = METAMODEL_PREFIX + "cgraph";
	public static final String METAMODEL_HASEQN_PROP = "http://aske.ge.com/sciknow#hasEquation";
	public static final String METAMODEL_QUERYTYPE_PROP = METAMODEL_PREFIX + "queryType";
	public static final String METAMODEL_PROGNOSTIC = METAMODEL_PREFIX + "prognostic";
	public static final String METAMODEL_CALIBRATION = METAMODEL_PREFIX + "calibration";
	public static final String METAMODEL_CGEXEC_CLASS = METAMODEL_PREFIX + "CGExecution";
	public static final String METAMODEL_STARTTIME_PROP = METAMODEL_PREFIX + "startTime";
	
	
	public static final String DEPENDENCY_GRAPH_INSERT = "prefix dbn:<http://aske.ge.com/dbn#>\n" + 
			"prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" +
			"insert {?EqCh dbn:parent ?EqPa}\n" + 
			"where {\n" + 
			" ?EqCh a imp:Equation.\n" + 
			" ?EqCh imp:input ?Arg.\n" + 
			" ?Arg imp:argType ?In.\n" + 
			" ?EqCh imp:output ?Oinst.\n" + 
			" ?Oinst a ?Out.\n" + 
			"\n" + 
			" ?EqPa a imp:Equation.\n" + 
			" ?EqPa imp:output ?POinst.\n" + 
			" ?POinst a ?In.\n" + 
			"}";
	public static final String RETRIEVE_COMP_GRAPH = "prefix hyper:<http://aske.ge.com/hypersonics#>\n" + 
			"prefix dbn:<http://aske.ge.com/dbn#>\n" + 
			"prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" + 
			"prefix owl:<http://www.w3.org/2002/07/owl#>\n" + 
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
			"prefix sci:<http://aske.ge.com/sciknow#>\n" +
			"select distinct ?Eq ?DBN ?Out where {\n" + 
			" {select ?Eq where {\n" + 
			"    ?EqOut a imp:Equation.\n" + 
			"     ?EqOut imp:output ?Oinst.\n" + 
			"     ?Oinst a ?Out.\n" + 
			"     filter (?Out in ( LISTOFOUTPUTS )).\n" + 
			"     ?EqOut dbn:parent* ?Eq.\n" + 
			"    ?Eq imp:input/imp:argType ?In.\n" + 
			"    filter (?In in (LISTOFINPUTS)).\n" + 
			"  }}\n" + 
			"   union {\n" + 
			"   select ?Eq where {\n" + 
			"     ?EqOut a imp:Equation.\n" + 
			"     ?EqOut imp:output ?Oinst.\n" + 
			"     ?Oinst a ?Out.\n" + 
			"     filter (?Out in ( LISTOFOUTPUTS )).\n" + 
			"     ?EqOut dbn:parent* ?Eq.\n" + 
			"     ?Eq imp:input/imp:argType ?In.\n" + 
			"      ?Eq2 a imp:Equation.\n" + 
			"      ?Eq2 imp:output ?Oi.\n" + 
			"      ?Oi a ?In. \n" + 
			"   }}\n "	+
			" ?Eq imp:output ?Oi.\n" + 
			" ?Oi a ?Out.\n" + 
			" ?DBN rdfs:subClassOf ?EQR.\n" + 
			" ?EQR owl:onProperty sci:hasEquation.\n" + 
			" ?EQR owl:someValuesFrom ?Eq.\n" +
			"}";
	public static final String RETRIEVE_MODELS = "prefix hyper:<http://aske.ge.com/hypersonics#>\n" + 
			"prefix dbn:<http://aske.ge.com/dbn#>\n" + 
			"prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" +
			"prefix sci:<http://aske.ge.com/sciknow#>" + 
			"prefix owl:<http://www.w3.org/2002/07/owl#>\n" + 
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
			"select distinct ?Model ?Input ?Output (str(?expr) as ?ModelForm)\n" +
			"where {\n" + 
			"  ?Model rdfs:subClassOf sci:DBN.\n" + 
			"  ?Model rdfs:subClassOf ?BN. \n" + 
			"  ?BN owl:onProperty sci:hasEquation. \n" + 
			"  ?BN owl:someValuesFrom ?Eq.\n" + 
			"  filter (?Eq in (EQNSLIST)) .   \n" + 
			"  ?Eq imp:input/imp:argType ?Input.\n" + 
			"  ?Eq imp:output ?Oinst.\n" + 
			"  ?Oinst a ?Output.\n" + 
			"  ?Eq imp:expression ?expr.\n" + 
			"} order by ?Model";
	public static final String RETRIEVE_NODES = "prefix hyper:<http://aske.ge.com/hypersonics#>\n" + 
			"prefix dbn:<http://aske.ge.com/dbn#>\n" + 
			"prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" + 
			"prefix sci:<http://aske.ge.com/sciknow#>" + 
			"prefix owl:<http://www.w3.org/2002/07/owl#>\n" + 
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
			"select ?Node ?Child ?Distribution\n" + 
			"where {\n" +
			" {select distinct ?Eq ?Node ?Child where {\n" + 
			"   ?Eq imp:input/imp:argType ?Node.\n" + 
			"   ?Eq imp:output ?Oinst. ?Oinst a ?Child.\n" + 
			"   filter (?Eq in (EQNSLIST))\n" + 
			" }}\n" + 
			" union\n" + 
			" { select distinct ?Eq ?Node where {\n" + 
			"   ?Eq imp:output ?Oi. ?Oi a ?Node.\n" + 
			"   filter (?Eq in (EQNSLIST))\n" + 
			"   filter not exists {\n" + 
			"     ?Eq1 imp:input/imp:argType ?Node.\n" + 
			"     filter (?Eq1 in (EQNSLIST))\n" + 
			"   }\n" + 
			" }}\n" + 
			"  ?DBN rdfs:subClassOf ?EB. ?EB owl:onProperty sci:hasEquation.  ?EB owl:someValuesFrom ?Eq.\n" + 
			"  ?DBN rdfs:subClassOf ?DB. ?DB owl:onProperty sci:distribution. ?DB owl:hasValue ?Distribution.\n" + 
			"} order by ?Node";
	
	@Override
	public Object[] insertTriplesAndQuery(Resource resource, TripleElement[] triples) throws SadlInferenceException {
		setCurrentResource(resource);
		setModelFolderPath(getModelFolderPath(resource));
		setModelName(OntModelProvider.getModelName(resource));
		setTheJenaModel(OntModelProvider.find(resource));

		if (commonSubject(triples)) {
			return super.insertTriplesAndQuery(resource, triples);
		}

		
		String queryHistoryKey = "MetaData";
		String qhModelName = "http://aske.ge.com/MetaData";
		String qhOwlFileName = "MetaData.owl";
		String qhOwlFileWithPath = getModelFolderPath(resource) + 
				 					File.separator + qhOwlFileName;
		Boolean newMetaDataFile = false;
		//Path qhpath = Paths.get(qhOwlFileWithPath);
		ConfigurationManagerForIDE cmgr = null;
		Object qhModelObj = OntModelProvider.getPrivateKeyValuePair(resource, queryHistoryKey);
		OntModel qhmodel;
		
		boolean calibrationQuery = false;
		
		
		if (qhModelObj == null) {
			File f = new File(qhOwlFileWithPath);
			if (f.exists() && !f.isDirectory()) {
			//if (Files.exists(qhpath)) {
				try {
					String p = getModelFolderPath(resource);
					cmgr = new ConfigurationManagerForIDE(getModelFolderPath(resource), ConfigurationManagerForIDE.getOWLFormat());
				} catch (ConfigurationException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				qhmodel = cmgr.loadOntModel(qhOwlFileWithPath);
			}
			else {
				qhmodel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
				newMetaDataFile = true;
			}
			OntModelProvider.addPrivateKeyValuePair(resource, queryHistoryKey, qhmodel);
		}
		else {
			qhmodel = (OntModel) qhModelObj;
		}
		
		// TODO Alfredo
		// ingest query object and store to CGQuery model file
		// get inputs and outputs
		// query for equations "buildCompSubgraph.sparql"
		// query for models & nodes
		// build json
		// request dbn execution
		// ingest dbn instance
		// ingest dbn results
		// output results on dialog window
		
		//return null;
		
		// This is from the body of insertTriplesAndQuery
		//NamedNode commonSubject = null;
		//Individual commonSubjectInst = null;
		//Node subjectType = null;
		List<TripleElement> insertions = new ArrayList<TripleElement>();
		List<TripleElement> queryPatterns = new ArrayList<TripleElement>();
		List<OntClass> inputsList = new ArrayList<OntClass>();
		List<OntClass> outputsList = new ArrayList<OntClass>();
		Map<OntClass, Individual> outputInstance = new HashMap<OntClass, Individual>();
		
		
		for (int i = 0; i < triples.length; i++) {
			TripleElement tr = triples[i];
			//Node snode = tr.getSubject();
			if (tr.getSubject() instanceof NamedNode) {
//				if (tr.getSubject() instanceof VariableNode) {
//					//subjectType = ((VariableNode)tr.getSubject()).getType();
//				}
				if (tr.getPredicate() != null && tr.getObject() != null) {
					// this is an input
					insertions.add(tr);
				}
				else {
					// this is an output
					queryPatterns.add(tr);
				}
			}
		}

		// Query instance
		//OntClass cgquery = getTheJenaModel().getOntClass(METAMODEL_QUERY_CLASS);
		//Individual cgq = getTheJenaModel().createIndividual(cgquery);
		Individual cgq = createIndividualOfClass(qhmodel,METAMODEL_QUERY_CLASS);
		// This is how to add properties to the individual
		//i.addProperty(RDFS.comment, "something");
	    //i.addRDFType(OWL2.NamedIndividual);
		
		//Create execution instance with time
//		OntClass cexec = getTheJenaModel().getOntClass(METAMODEL_CGEXEC_CLASS);
//		Individual ce = getTheJenaModel().createIndividual(cexec);
//		OntProperty stprop = getTheJenaModel().getOntProperty(METAMODEL_STARTTIME_PROP);
//		Object tim = getTime();
//		qhmodel.add(ce,stprop,(XSDDateTime)val).asCalendar().getTime())
		
		
		
		String suri;
		OntClass sclass;
		Individual sinst;
		
		if (insertions.size() > 0) {
			for (int i = 0; i < insertions.size(); i++) {
				TripleElement itr = insertions.get(i);
				//if (itr.getSubject().equals(commonSubject)) {
				//NamedNode subj = (NamedNode) getTheJenaModel().getOntClass(itr.getSubject().getURI());
				suri = itr.getSubject().getURI();
				sinst = createIndividualOfClass(qhmodel,suri);
				//getTheJenaModel().write(System.out);
				
				Property pred = getTheJenaModel().getProperty(itr.getPredicate().getURI());
				Node objNode = itr.getObject();
				RDFNode val = null;
				if (objNode instanceof Literal) {
					if (pred.canAs(OntProperty.class)) {
						OntResource rng = pred.as(OntProperty.class).getRange();
						try {
							val = SadlUtils.getLiteralMatchingDataPropertyRange(getTheJenaModel(), rng.getURI(), ((Literal)objNode).getValue());
						} catch (TranslationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					else {
						val = getTheJenaModel().createTypedLiteral(((Literal)objNode).getValue());
					}
				}
				else {
					val = getTheJenaModel().getResource(objNode.getURI());
				}
				sclass = getTheJenaModel().getOntClass(suri);
				inputsList.add(sclass);

				qhmodel.add(sinst, pred, val);
				getTheJenaModel().add(sinst,RDF.type,getTheJenaModel().getOntClass(suri));
				getTheJenaModel().add(sinst,pred,val);
				// create triple: cgq, mm:input, sinst
				OntProperty inputprop = getTheJenaModel().getOntProperty(METAMODEL_INPUT_PROP);
				qhmodel.add(cgq, inputprop, sinst);
			
			}
		}
		getTheJenaModel().write(System.out, "TTL" );

		if (queryPatterns.size() > 0) {
			// how many results there will be
			//ResultSet[] results = new ResultSet[queryPatterns.size()];
			for (int i = 0; i < queryPatterns.size(); i++) {
				TripleElement itr = queryPatterns.get(i);

				suri = itr.getSubject().getURI();
				sinst = createIndividualOfClass(qhmodel,suri);

//				Property pred = getTheJenaModel().getProperty(itr.getPredicate().getURI());
//				Node objNode = itr.getObject();
				
				sclass = getTheJenaModel().getOntClass(suri);
				outputsList.add(sclass);

				OntProperty outputprop = getTheJenaModel().getOntProperty(METAMODEL_OUTPUT_PROP);
				//getTheJenaModel().add(cgq, outputprop, sinst);
				qhmodel.add(cgq, outputprop, sinst);
				outputInstance.put(sclass,sinst);
			}
		}
		
		
		// Insert dependency graph
		UpdateAction.parseExecute(DEPENDENCY_GRAPH_INSERT , getTheJenaModel());
		
		//getTheJenaModel().write(System.out, "TTL" );


		// Inputs/Outputs filter parameters
		String listOfInputs = "";
		for ( OntClass o : inputsList ) {
			listOfInputs += "<" + o.toString() + ">,";
		}
		listOfInputs = listOfInputs.substring(0,listOfInputs.length()-1);
		String listOfOutputs = "";
		for ( OntClass o : outputsList ) {
			listOfOutputs += "<" + o.toString() + ">,";
		}
		listOfOutputs = listOfOutputs.substring(0,listOfOutputs.length()-1);
		
		// Retrieve computational graph
		String queryStr = RETRIEVE_COMP_GRAPH.replaceAll("LISTOFINPUTS", listOfInputs).replaceAll("LISTOFOUTPUTS", listOfOutputs);
		
		com.hp.hpl.jena.query.Query q = QueryFactory.create(queryStr);
		QueryExecution qexec = QueryExecutionFactory.create(q, getTheJenaModel()); 
		//com.hp.hpl.jena.query.ResultSet eqnsResults = qexec.execSelect() ;
		com.hp.hpl.jena.query.ResultSetRewindable eqnsResults = com.hp.hpl.jena.query.ResultSetFactory.makeRewindable(qexec.execSelect()) ;

		
		OntProperty qtypeprop = getTheJenaModel().getOntProperty(METAMODEL_QUERYTYPE_PROP);
		OntResource qtype;
		
		if ( eqnsResults.hasNext() ) {
			qtype = getTheJenaModel().getOntResource(METAMODEL_PREFIX + "prognostic");
			qhmodel.add(cgq, qtypeprop, qtype);			
		} else {
			queryStr = RETRIEVE_COMP_GRAPH.replaceAll("LISTOFOUTPUTS", listOfInputs).replaceAll("LISTOFINPUTS", listOfOutputs);
			com.hp.hpl.jena.query.Query qinv = QueryFactory.create(queryStr);
			qexec = QueryExecutionFactory.create(qinv, getTheJenaModel()); 
			eqnsResults = com.hp.hpl.jena.query.ResultSetFactory.makeRewindable(qexec.execSelect()) ;
			if (eqnsResults.hasNext()) {
					calibrationQuery = true;
					qtype = getTheJenaModel().getOntResource(METAMODEL_PREFIX + "calibration");
					qhmodel.add(cgq, qtypeprop, qtype);			
			}
		}
		
		
		
		String listOfEqns = "";
		//List<RDFNode> listOfEqnObjs = new ArrayList<RDFNode>();
		//List<RDFNode> listOfObjs = new ArrayList<RDFNode>();
		
		QuerySolution soln;
		for ( ; eqnsResults.hasNext() ; )
	    {
	      soln = eqnsResults.nextSolution() ;
	      RDFNode s = soln.get("?Eq") ; 
	      //RDFNode p = soln.get("?DBN") ;
	      //Resource r = soln.getResource("VarR") ;
	      //Literal l = soln.getLiteral("VarL") ; 
	      System.out.println(s.toString()); // + "  " + p.toString() );
	      listOfEqns += "<" + s.toString() +">,";
	      //listOfEqnObjs.add(s);
	    }
		listOfEqns = listOfEqns.substring(0,listOfEqns.length()-1);

		
		// Comp Graph instance
		Individual cgIns = createIndividualOfClass(qhmodel,METAMODEL_CCG);
		OntProperty subgraphprop = 	getTheJenaModel().getOntProperty(METAMODEL_SUBG_PROP);
		OntProperty cgraphprop = 	getTheJenaModel().getOntProperty(METAMODEL_CGRAPH_PROP);
		OntProperty hasEqnProp = 	getTheJenaModel().getOntProperty(METAMODEL_HASEQN_PROP);
		OntProperty outputprop = 	getTheJenaModel().getOntProperty(METAMODEL_OUTPUT_PROP);
		
		Individual sgIns, dbnIns, eqnIns, outpIns;
		
		eqnsResults.reset();
		//for (RDFNode eq : listOfEqnObjs) {
		while( eqnsResults.hasNext() ) {
	      	soln = eqnsResults.nextSolution() ;
			RDFNode s = soln.get("?Eq") ; 
			RDFNode p = soln.get("?DBN") ;
			RDFNode o = soln.get("?Out") ;
			sgIns = createIndividualOfClass(qhmodel,METAMODEL_SUBGRAPH);
			//sgIns = qhmodel.createIndividual(getTheJenaModel().getOntClass(METAMODEL_SUBGRAPH));
			//getTheJenaModel().add(cgIns, subgraphprop, sgIns); 
			qhmodel.add(cgIns, subgraphprop, sgIns);
			dbnIns = createIndividualOfClass(qhmodel,p.toString());
			//getTheJenaModel().add(sgIns, cgraphprop, dbnIns);
			qhmodel.add(sgIns, cgraphprop, dbnIns);
			//getTheJenaModel().add(dbnIns, hasEqnProp, s); //the equation is already an instance
			qhmodel.add(dbnIns, hasEqnProp, s); //the equation is already an instance
			if (outputInstance.containsKey(o)) {
				qhmodel.add(sgIns, outputprop, outputInstance.get(o));
			}
			else {
				outpIns = createIndividualOfClass(qhmodel,o.toString());
				//outpIns = getTheJenaModel().createIndividual("http://aske.ge.com/testdiag#dbn1",getTheJenaModel().getOntClass(o.toString()));
				//getTheJenaModel().add(sgIns, outputprop, outpIns);
				qhmodel.add(sgIns, outputprop, outpIns);
			}
		}
		
		//qhmodel.write(System.out,"RDF/XML-ABBREV");
		//qhmodel.write(System.out,"TTL");
		
		String qhGlobalPrefix = null;
		try {
			getConfigMgr(null).saveOwlFile(qhmodel, qhModelName, qhOwlFileWithPath);
			String fileUrl = (new UtilsForJena()).fileNameToFileUrl(qhOwlFileWithPath);
			if (newMetaDataFile)
				getConfigMgr(null).addMapping(fileUrl, qhModelName, qhGlobalPrefix, false, "DialogInference"); //Only if new file
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (URISyntaxException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

//		OntModelProvider.attach(resource, getTheJenaModel(), OntModelProvider.getModelName(resource),OntModelProvider.getModelPrefix(resource));
		
		//OntModelProvider.
		
		// Retrieve Models & Nodes
		queryStr = RETRIEVE_MODELS.replaceAll("EQNSLIST", listOfEqns);
		com.hp.hpl.jena.query.Query qm = QueryFactory.create(queryStr);
		queryStr = RETRIEVE_NODES.replaceAll("EQNSLIST", listOfEqns);
		com.hp.hpl.jena.query.Query qn = QueryFactory.create(queryStr);

		qexec = QueryExecutionFactory.create(qm, getTheJenaModel()); 
		com.hp.hpl.jena.query.ResultSetRewindable results = com.hp.hpl.jena.query.ResultSetFactory.makeRewindable(qexec.execSelect()) ;

		while( results.hasNext() ) {
	      	soln = results.nextSolution() ;
			RDFNode m = soln.get("?Model") ; 
			RDFNode i = soln.get("?Input") ;
			RDFNode o = soln.get("?Output") ;
			RDFNode f = soln.get("?ModelForm") ;
		}
		//TODO: create models csv (models are the DBNs)
		
		qexec = QueryExecutionFactory.create(qn, getTheJenaModel());
		results = com.hp.hpl.jena.query.ResultSetFactory.makeRewindable(qexec.execSelect());

		while( results.hasNext() ) {
	      	soln = results.nextSolution() ;
			RDFNode n = soln.get("?Node") ; 
			RDFNode c = soln.get("?Child") ;
			RDFNode d = soln.get("?Distribution") ;
		}
		
		//TODO: create nodes csv
		
		//TODO: request DBN json build
		
		//TODO: request DBN Execution
		
		//TODO: add triples with result to Meta Model
		

		
		
		
		
		return null;
	}


	private Individual createIndividualOfClass(OntModel model, String classUriStr) {
		return model.createIndividual(getTheJenaModel().getOntClass(classUriStr));
	}

	private boolean commonSubject(TripleElement[] triples) {
		Node lastSubject = null;
		for (TripleElement tr : triples) {
			Node thisSubject = tr.getSubject();
			if (lastSubject != null && !lastSubject.equals(thisSubject)) {
				return false;
			}
			lastSubject = thisSubject;
		}
		return true;
	}
}

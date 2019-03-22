package com.ge.research.sadl.darpa.aske.inference;

import java.io.ByteArrayOutputStream;
import java.util.Iterator;
import java.util.ServiceLoader;
import java.util.Set;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.http.HttpEntity;
import org.apache.http.HttpHost;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.conn.params.ConnRoutePNames;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.eclipse.emf.ecore.resource.Resource;
import com.ge.research.sadl.builder.ConfigurationManagerForIDE;
import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.jena.JenaBasedSadlInferenceProcessor;
import com.ge.research.sadl.jena.UtilsForJena;
import com.ge.research.sadl.model.gp.Literal;
import com.ge.research.sadl.model.gp.NamedNode;
import com.ge.research.sadl.model.gp.Node;
import com.ge.research.sadl.model.gp.TripleElement;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.processing.SadlInferenceException;
import com.hp.hpl.jena.reasoner.rulesys.Builtin;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.ConfigurationManager;
import com.ge.research.sadl.reasoner.IReasoner;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.TranslationException;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.ge.research.sadl.utils.ResourceManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hp.hpl.jena.datatypes.xsd.XSDDateTime;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.query.*;
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.vocabulary.RDF;

public class JenaBasedDialogInferenceProcessor extends JenaBasedSadlInferenceProcessor {

	private OntModel qhmodel;
	
	public static final String queryHistoryKey = "MetaData";
	public static final String qhModelName = "http://aske.ge.com/MetaData";
	public static final String qhOwlFileName = "MetaData.owl";


	public static final String METAMODEL_PREFIX = "http://aske.ge.com/metamodel#";
	public static final String METAMODEL_QUERY_CLASS = METAMODEL_PREFIX + "CGQuery";
	public static final String METAMODEL_INPUT_PROP = METAMODEL_PREFIX + "input";
	public static final String METAMODEL_OUTPUT_PROP = METAMODEL_PREFIX + "output";
	public static final String METAMODEL_CCG = METAMODEL_PREFIX + "CCG";
	public static final String METAMODEL_SUBGRAPH = METAMODEL_PREFIX + "SubGraph";
	public static final String METAMODEL_SUBG_PROP = METAMODEL_PREFIX + "subgraph";
	public static final String METAMODEL_CGRAPH_PROP = METAMODEL_PREFIX + "cgraph";
	public static final String METAMODEL_EXEC_PROP = METAMODEL_PREFIX + "execution";
	public static final String METAMODEL_COMPGRAPH_PROP = METAMODEL_PREFIX + "compGraph";
	public static final String METAMODEL_HASEQN_PROP = "http://aske.ge.com/sciknow#hasEquation";
	public static final String METAMODEL_QUERYTYPE_PROP = METAMODEL_PREFIX + "queryType";
	public static final String METAMODEL_PROGNOSTIC = METAMODEL_PREFIX + "prognostic";
	public static final String METAMODEL_CALIBRATION = METAMODEL_PREFIX + "calibration";
	public static final String METAMODEL_CGEXEC_CLASS = METAMODEL_PREFIX + "CGExecution";
	public static final String METAMODEL_STARTTIME_PROP = METAMODEL_PREFIX + "startTime";
	public static final String VALUE_PROP = "http://sadl.org/sadlimplicitmodel#value";
	public static final String STDDEV_PROP = "http://sadl.org/sadlimplicitmodel#stddev";
	
	public static final String DEPENDENCY_GRAPH_INSERT = "prefix dbn:<http://aske.ge.com/dbn#>\n" + 
			"prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" +
			"prefix sci:<http://aske.ge.com/sciknow#>\n" +
			"insert {?EqCh dbn:parent ?EqPa}\n" + 
			"where {\n" + 
			" ?EqCh a imp:Equation.\n" + 
			" ?EqCh sci:input ?Arg.\n" + 
			" ?Arg sci:argType ?In.\n" + 
			" ?EqCh sci:output ?Oinst.\n" + 
			" ?Oinst a ?Out.\n" + 
			"\n" + 
			" ?EqPa a imp:Equation.\n" + 
			" ?EqPa sci:output ?POinst.\n" + 
			" ?POinst a ?In.\n" + 
			"}";
	public static final String BUILD_COMP_GRAPH = "prefix hyper:<http://aske.ge.com/hypersonics#>\n" + 
			"prefix dbn:<http://aske.ge.com/dbn#>\n" + 
			"prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" + 
			"prefix owl:<http://www.w3.org/2002/07/owl#>\n" + 
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
			"prefix sci:<http://aske.ge.com/sciknow#>\n" +
			"select distinct ?Eq ?DBN ?Out where {\n" + 
			" {select ?Eq where {\n" + 
			"    ?EqOut a imp:Equation.\n" + 
			"     ?EqOut sci:output ?Oinst.\n" + 
			"     ?Oinst a ?Out.\n" + 
			"     filter (?Out in ( LISTOFOUTPUTS )).\n" + 
			"     ?EqOut dbn:parent* ?Eq.\n" + 
			"    ?Eq sci:input/sci:argType ?In.\n" + 
			"    filter (?In in (LISTOFINPUTS)).\n" + 
			"  }}\n" + 
			"   union {\n" + 
			"   select ?Eq where {\n" + 
			"     ?EqOut a imp:Equation.\n" + 
			"     ?EqOut sci:output ?Oinst.\n" + 
			"     ?Oinst a ?Out.\n" + 
			"     filter (?Out in ( LISTOFOUTPUTS )).\n" + 
			"     ?EqOut dbn:parent* ?Eq.\n" + 
			"     ?Eq sci:input/sci:argType ?In.\n" + 
			"      ?Eq2 a imp:Equation.\n" + 
			"      ?Eq2 sci:output ?Oi.\n" + 
			"      ?Oi a ?In. \n" + 
			"   }}\n "	+
			" ?Eq sci:output ?Oi.\n" + 
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
			"select distinct ?Model ?Input (str(?Label) as ?InputLabel) ?Output (str(?expr) as ?ModelForm)\n" +
			"where {\n" + 
			"  ?Model rdfs:subClassOf sci:DBN.\n" + 
			"  ?Model rdfs:subClassOf ?BN. \n" + 
			"  ?BN owl:onProperty sci:hasEquation. \n" + 
			"  ?BN owl:someValuesFrom ?Eq.\n" + 
			"  filter (?Eq in (EQNSLIST)) .   \n" + 
			"  ?Eq sci:input ?In.\n" + 
			"  ?In sci:argType ?Input.\n" + 
			"  ?In sci:argName ?Label.\n" + 
			"  ?Eq sci:output ?Oinst.\n" + 
			"  ?Oinst a ?Output.\n" + 
			"  ?Eq imp:expression ?Scr.\n" + 
			"  ?Scr imp:script ?expr.\n" + 
			"} order by ?Model";
	public static final String RETRIEVE_NODES = "prefix hyper:<http://aske.ge.com/hypersonics#>\n" + 
			"prefix dbn:<http://aske.ge.com/dbn#>\n" + 
			"prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" + 
			"prefix sci:<http://aske.ge.com/sciknow#>" +
			"prefix mm:<http://aske.ge.com/metamodel#>\n" + 
			"prefix owl:<http://www.w3.org/2002/07/owl#>\n" + 
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
			"select distinct ?Node ?Child ?Distribution ?Lower ?Upper ?Value\n" + 
			"where {\n" +
			" {select distinct ?Eq ?Node ?Child where {\n" + 
			"   ?Eq sci:input/sci:argType ?Node.\n" + 
			"   ?Eq sci:output ?Oinst. ?Oinst a ?Child.\n" + 
			"   filter (?Eq in (EQNSLIST))\n" + 
			" }}\n" + 
			" union\n" + 
			" { select distinct ?Eq ?Node where {\n" + 
			"   ?Eq sci:output ?Oi. ?Oi a ?Node.\n" + 
			"   filter (?Eq in (EQNSLIST))\n" + 
			"   filter not exists {\n" + 
			"     ?Eq1 sci:input/sci:argType ?Node.\n" + 
			"     filter (?Eq1 in (EQNSLIST))\n" + 
			"   }\n" + 
			" }}\n" + 
			"  ?DBN rdfs:subClassOf ?EB. ?EB owl:onProperty sci:hasEquation.  ?EB owl:someValuesFrom ?Eq.\n" + 
			"  ?DBN rdfs:subClassOf ?DB. ?DB owl:onProperty sci:distribution. ?DB owl:hasValue ?Distribution.\n" +
			"  ?DBN rdfs:subClassOf ?RB. ?RB owl:onProperty sci:range.        ?RB owl:hasValue ?Range.\n" + 
			"  ?Range sci:lower ?Lower.\n" + 
			"  ?Range sci:upper ?Upper.\n" + 
//			"  ?CG mm:subgraph/mm:cgraph ?DBNI.\n" +
			"  ?Q mm:execution/mm:compGraph ?CG.\n" +
			"  filter (?CG in (COMPGRAPH)). \n" + 
//			"  ?DBNI a ?DBN.\n" + 
			"     optional{\n" + 
//			"     ?Q mm:execution/mm:compGraph ?CG.\n" + 
			"     ?Q mm:input ?II.\n" + 
			"     ?II a ?Node.\n" + 
			"     ?II imp:value ?Value.}" + 
			"} order by ?Node";
	public static final String CGQUERY = "prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" +
			"select distinct ?Input ?EQ ?Output ?Input_style ?Input_color ?Output_tooltip\n" + //(?Expr as ?EQ_tooltip)
			"where {\n" + 
			" ?CCG a <http://aske.ge.com/metamodel#CCG>.\n" +
			" filter (?CCG in (COMPGRAPH)).\n" +
			" ?CCG <http://aske.ge.com/metamodel#subgraph> ?SG.\n" + 
			" ?SG <http://aske.ge.com/metamodel#cgraph> ?CG.\n" + 
			" ?CG <http://aske.ge.com/sciknow#hasEquation> ?EQ.\n" + 
			" ?EQ <http://sadl.org/sadlimplicitmodel#input> ?I.\n" + 
			" ?I <http://sadl.org/sadlimplicitmodel#argType> ?Input.\n" + 
			" ?EQ <http://sadl.org/sadlimplicitmodel#output> ?O.\n" + 
			" ?EQ imp:expression ?Expr." +
			" let(?Output_tooltip := str(?Expr))" +
			" ?O a ?Output.\n" +
			" let(?Input_style := 'filled')" +
			" let(?Input_color := 'yellow')\n" +
			"} order by ?EQ";
	
	public static final String CGQUERYWITHVALUES ="prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" + 
			"prefix sci:<http://aske.ge.com/sciknow#>\n" +
			"prefix mm:<http://aske.ge.com/metamodel#>\n" + 
			"\n" + 
			"select ?X ?Y ?Z ?X_style ?X_color ?Z_shape ?Z_tooltip\n" + //?X_style \n" + 
			//"from <http://kd.ge.com/md2>\n" + 
			//"from <http://kd.ge.com/aske3>\n" + 
			"where {\n" + 
			"{select (?Input as ?X) (?EQ as ?Y) (?Output as ?Z) ?X_style ?X_color ('box' as ?Z_shape) ?Z_tooltip\n" + 
			"where {\n" + 
			"    ?CCG a mm:CCG.\n" + 
			"    filter (?CCG in (COMPGRAPH)).\n" + 
			"    ?CCG mm:subgraph ?SG.\n" + 
			"    ?SG mm:cgraph ?CG.\n" + 
			"    ?CG sci:hasEquation ?EQ.\n" + 
			"    ?EQ sci:input ?I.\n" + 
			"    ?I sci:argType ?Input.\n" + 
			"    ?EQ sci:output ?O.\n" + 
			"    ?O a ?Output.\n" + 
			"    ?CCG mm:subgraph ?SG1.\n" + 
			"    ?SG1 mm:cgraph ?CG1.\n" + 
			"    ?CG1 sci:hasEquation ?EQ1.\n" + 
			"    ?EQ1 sci:output ?O1.\n" + 
			"    ?O1 a ?Input.\n" +
			"    ?EQ imp:expression ?Expr.\n" + 
			"	 bind(str(?Expr) as ?Z_tooltip)" +
			"    bind('solid' as ?X_style)\n" + 
			"    bind('black' as ?X_color)\n" + 
			"}}union\n" + 
			"{select (?Input as ?X) (?EQ as ?Y) (?Output as ?Z) ?X_style ?X_color ('box' as ?Z_shape) ?Z_tooltip\n" + 
			"where {\n" + 
			"    ?CCG a mm:CCG.\n" + 
			"    filter (?CCG in (COMPGRAPH)).\n" + 
			"    ?CCG mm:subgraph ?SG.\n" + 
			"    ?SG mm:cgraph ?CG.\n" + 
			"    ?CG sci:hasEquation ?EQ.\n" + 
			"    ?EQ sci:input ?I.\n" + 
			"    ?I sci:argType ?Input.\n" + 
			"    ?EQ sci:output ?O.\n" + 
			"    ?O a ?Output.\n" + 
			"    ?EQ imp:expression ?Expr.\n" + 
			"	 bind(str(?Expr) as ?Z_tooltip)" +
			"   filter not exists {\n" + 
			"    ?CCG mm:subgraph ?SG2.\n" + 
			"    ?SG2 mm:cgraph ?CG2.\n" + 
			"    ?CG2 sci:hasEquation ?EQ2.\n" + 
			"    ?EQ2 sci:output ?O2.\n" + 
			"    ?O2 a ?Input.}\n" + 
			"    bind('filled' as ?X_style)\n" + 
			"    bind('yellow' as ?X_color)\n" + 
			"}}union\n" + 
			" {select (?Output as ?X) ?Y (concat(concat(strbefore(?Value,'.'),'.'),substr(strafter(?Value,'.'),1,4)) as ?Z) ?X_style ?X_color ('oval' as ?Z_shape) ('output value' as ?Z_tooltip)\n" + 
			"  where {\n" + 
			"    ?CCG mm:subgraph ?SG.\n" + 
			"    filter (?CCG in (COMPGRAPH)).\n" + 
			"    ?SG mm:output ?Oinst.\n" + 
			"    ?Oinst a ?Output.\n" + 
			"    ?Oinst imp:value ?Value.\n" + 
			"    bind(\"value\" as ?Y).\n" +
			"    bind('solid' as ?X_style)\n" + 
			"    bind('black' as ?X_color)\n" +
			"  }}\n" + 
			//" bind('filled' as ?X_style)\n" + 
			//" bind(?color as ?X_color)\n" + 
			"}";

	public static final String RESULTSQUERY = "prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" + 
			"prefix mm:<http://aske.ge.com/metamodel#>\n" +
			"prefix sci:<http://aske.ge.com/sciknow#>\n" + 
			"select distinct (strafter(str(?Var),'#') as ?Variable) ?Mean ?StdDev\n" + 
			"from <http://kd.ge.com/md2>\n" + 
			"from <http://kd.ge.com/aske3>\n" + 
			"where {\n" + 
			"    ?CCG mm:subgraph ?SG.\n" + 
			"    filter (?CCG in (COMPGRAPH)).\n" + 
			"    ?SG mm:output ?Oinst.\n" + 
			"    ?Oinst a ?Var.\n" + 
			"    ?Oinst imp:value ?Mean.\n" + 
			"  ?Oinst imp:stddev ?StdDev.\n" +
			//"  ?SG mm:cgraph/sci:hasEquation/imp:expression ?EQ.\n" + 
			"}";
	
	
	
	@Override
	public Object[] insertTriplesAndQuery(Resource resource, TripleElement[] triples) throws SadlInferenceException {
		setCurrentResource(resource);
		setModelFolderPath(getModelFolderPath(resource));
		setModelName(OntModelProvider.getModelName(resource));
		setTheJenaModel(OntModelProvider.find(resource));
		
//		System.out.println(" >> Builtin classes discovered by the service loader:");
//		Iterator<Builtin> iter = ServiceLoader.load(Builtin.class).iterator();
//		while (iter.hasNext()) {
//			Builtin service = iter.next();
//			System.out.println(service.getClass().getCanonicalName());
//		}

		if (commonSubject(triples)) {
			return super.insertTriplesAndQuery(resource, triples);
		}

		
//		String queryHistoryKey = "MetaData";
//		String qhModelName = "http://aske.ge.com/MetaData";
//		String qhOwlFileName = "MetaData.owl";
		String qhOwlFileWithPath = getModelFolderPath(resource) + File.separator + qhOwlFileName;
		//Path qhpath = Paths.get(qhOwlFileWithPath);
		ConfigurationManagerForIDE cmgr = null;
		Object qhModelObj = OntModelProvider.getPrivateKeyValuePair(resource, queryHistoryKey);
//		OntModel qhmodel;
		
		if (qhModelObj == null) {
			File f = new File(qhOwlFileWithPath);
			if (f.exists() && !f.isDirectory()) {
			//if (Files.exists(qhpath)) {
				try {
					//String p = getModelFolderPath(resource);
					cmgr = new ConfigurationManagerForIDE(getModelFolderPath(resource), ConfigurationManagerForIDE.getOWLFormat());
				} catch (ConfigurationException e1) {
					//  Auto-generated catch block
					e1.printStackTrace();
				}
				qhmodel = cmgr.loadOntModel(qhOwlFileWithPath);
			}
			else {
				qhmodel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			}
			OntModelProvider.addPrivateKeyValuePair(resource, queryHistoryKey, qhmodel);
		}
		else {
			qhmodel = (OntModel) qhModelObj;
		}
		
		// To do: Alfredo
		// ingest query object and store to CGQuery model file
		// get inputs and outputs
		// query for equations "buildCompSubgraph.sparql"
		// query for models & nodes
		// build json
		// request dbn execution
		// ingest dbn instance
		// ingest dbn results
		// output results on dialog window
		
		

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
					//TODO: just for testing model search, add to queryPatterns
					//queryPatterns.add(tr);
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
		OntClass cexec = getTheJenaModel().getOntClass(METAMODEL_CGEXEC_CLASS);
		Individual ce = qhmodel.createIndividual(cexec);
		//OntProperty stprop = getTheJenaModel().getOntProperty(METAMODEL_STARTTIME_PROP);

//		String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
//		Calendar cal = Calendar.getInstance();
//		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
//		XSDDateTime tim = sdf.format(cal.getTime());;
////		qhmodel.add(ce,stprop,(XSDDateTime)val).asCalendar().getTime())
		
		OntProperty execprop = getTheJenaModel().getOntProperty(METAMODEL_EXEC_PROP);
		qhmodel.add(cgq,execprop,ce);
		

		String suri;
		OntClass sclass;
		Individual sinst;
		
		
		if (insertions != null && insertions.size() > 0) {
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
							//  Auto-generated catch block
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
		//getTheJenaModel().write(System.out, "TTL" );

		if (queryPatterns.size() > 0) {
			// how many results there will be
			//ResultSet[] results = new ResultSet[queryPatterns.size()];
			for (int i = 0; i < queryPatterns.size(); i++) {
				TripleElement itr = queryPatterns.get(i);

				suri = itr.getSubject().getURI();

//				Property pred = getTheJenaModel().getProperty(itr.getPredicate().getURI());
//				Node objNode = itr.getObject();
				
				sclass = getTheJenaModel().getOntClass(suri);
				outputsList.add(sclass);

//				OntProperty outputprop = getTheJenaModel().getOntProperty(METAMODEL_OUTPUT_PROP);
//				sinst = createIndividualOfClass(qhmodel,suri);
//				qhmodel.add(cgq, outputprop, sinst);
//				outputInstance.put(sclass,sinst);
			}
		}
		
		
		// Insert dependency graph
		UpdateAction.parseExecute(DEPENDENCY_GRAPH_INSERT , getTheJenaModel());
		
		//getTheJenaModel().write(System.out, "TTL" );


		// Inputs/Outputs filter parameters
		
		String listOfInputs = "";
		String listOfOutputs = "";

		
		com.hp.hpl.jena.query.ResultSetRewindable eqnsResults = null;
		String queryStr = "";
		QueryExecution qexec = null;
		OntResource qtype;
		OntProperty qtypeprop = getTheJenaModel().getOntProperty(METAMODEL_QUERYTYPE_PROP);
		OntProperty outputprop = getTheJenaModel().getOntProperty(METAMODEL_OUTPUT_PROP);
		String listOfEqns = "";
		com.hp.hpl.jena.query.ResultSetRewindable models, nodes;
		String modelsCSVString = "";
		String nodesCSVString = "";
		Individual cgIns = null;
		
		Map<String,String> class2lbl;
		Map<String,String[]> lbl2value;
		
		String cgJson, dbnJson, dbnResultsJson;
		
		ResultSet[] results = new ResultSet[2];  //[numNodes+1];

		
		try {
		
			if (inputsList.size() > 0 && outputsList.size() > 0) {
	
				for(int i=0; i<outputsList.size(); i++) {
					OntClass oclass = outputsList.get(i);
					Individual oinst = createIndividualOfClass(qhmodel, oclass.getURI());
					qhmodel.add(cgq, outputprop, oinst);
					outputInstance.put(oclass,oinst);
				}			
				
				listOfInputs = strUriList(inputsList);
				listOfOutputs = strUriList(outputsList);
	
			
				// Retrieve computational graph
				queryStr = BUILD_COMP_GRAPH.replaceAll("LISTOFINPUTS", listOfInputs).replaceAll("LISTOFOUTPUTS", listOfOutputs);
				
				com.hp.hpl.jena.query.Query q = QueryFactory.create(queryStr);
				qexec = QueryExecutionFactory.create(q, getTheJenaModel()); 
				//com.hp.hpl.jena.query.ResultSet eqnsResults = qexec.execSelect() ;
				eqnsResults = com.hp.hpl.jena.query.ResultSetFactory.makeRewindable(qexec.execSelect()) ;
	
				boolean calibrationQuery = false;
		
				if ( eqnsResults.hasNext() ) {
					qtype = getTheJenaModel().getOntResource(METAMODEL_PREFIX + "prognostic");
					qhmodel.add(cgq, qtypeprop, qtype);			
				} else {
					queryStr = BUILD_COMP_GRAPH.replaceAll("LISTOFOUTPUTS", listOfInputs).replaceAll("LISTOFINPUTS", listOfOutputs);
					com.hp.hpl.jena.query.Query qinv = QueryFactory.create(queryStr);
					qexec = QueryExecutionFactory.create(qinv, getTheJenaModel()); 
					eqnsResults = com.hp.hpl.jena.query.ResultSetFactory.makeRewindable(qexec.execSelect()) ;
					if (eqnsResults.hasNext()) {
							calibrationQuery = true;
							qtype = getTheJenaModel().getOntResource(METAMODEL_PREFIX + "calibration");
							qhmodel.add(cgq, qtypeprop, qtype);			
					}
				}
				
				listOfEqns = getEqnUrisFromResults(eqnsResults);
	            eqnsResults.reset();

				// Comp Graph instance
				//Individual cgIns = createIndividualOfClass(qhmodel,METAMODEL_CCG);
				cgIns = qhmodel.createIndividual(METAMODEL_PREFIX + "CG_" + System.currentTimeMillis(), getTheJenaModel().getOntClass(METAMODEL_CCG));
				qhmodel.add(ce, getTheJenaModel().getOntProperty(METAMODEL_COMPGRAPH_PROP), cgIns);
	
				// Retrieve Models & Nodes
				queryStr = RETRIEVE_MODELS.replaceAll("EQNSLIST", listOfEqns);
				models = queryKnowledgeGraph(queryStr, getTheJenaModel());
				modelsCSVString = convertResultSetToString(models);
				System.out.println(modelsCSVString);
	
				queryStr = RETRIEVE_NODES.replaceAll("EQNSLIST", listOfEqns);
				queryStr = queryStr.replaceAll("COMPGRAPH", "<" + cgIns.getURI() + ">");
				nodes = queryKnowledgeGraph(queryStr, getTheJenaModel().union(qhmodel));
				nodesCSVString = convertResultSetToString(nodes);
				System.out.println(nodesCSVString);
	
				if (nodes.size() <= 0) {
					// TODO: Output "no models found" message
					return null;
				}
	
				
				cgJson = kgResultsToJson(nodesCSVString, modelsCSVString);
				dbnJson = generateDBNjson(cgJson);
				class2lbl = getClassLabelMapping(dbnJson);
				
	            dbnResultsJson = executeDBN(dbnJson);
	            lbl2value = getLabelToMeanStdMapping(dbnResultsJson);

	            createCGsubgraphs(cgIns, eqnsResults, class2lbl, lbl2value, outputInstance);
				
				// create ResultSet
				results[0] = retrieveCompGraph(resource, cgIns);
				
				results[1] = retrieveValues(resource, cgIns);
				
				
			} else if (inputsList.size() <= 0 && outputsList.size() > 0) {
				
				
				
				qtype = getTheJenaModel().getOntResource(METAMODEL_PREFIX + "explanation");
				qhmodel.add(cgq, qtypeprop, qtype);	// do we add it even if we don't know if there's a model yet?
	
				inputsList.addAll(outputsList);
				for(int i=0; i < outputsList.size(); i++) {
					List<OntClass> outpl = new ArrayList<OntClass>();
					List<OntClass> inpl = new ArrayList<OntClass>();
					OntClass oclass = outputsList.get(i);
	
					outpl.add(oclass);
					inpl.addAll(outputsList);
					inpl.remove(i);
					
					Individual oinst = createIndividualOfClass(qhmodel, oclass.getURI());
					outputInstance.put(oclass,oinst);
					
					
					listOfInputs = strUriList(inpl);
					listOfOutputs = strUriList(outpl);
					System.out.println(listOfInputs);
					System.out.println(listOfOutputs);
					
					queryStr = BUILD_COMP_GRAPH.replaceAll("LISTOFINPUTS", listOfInputs).replaceAll("LISTOFOUTPUTS", listOfOutputs);
					com.hp.hpl.jena.query.Query q = QueryFactory.create(queryStr);
					qexec = QueryExecutionFactory.create(q, getTheJenaModel()); 
					//com.hp.hpl.jena.query.ResultSet eqnsResults = qexec.execSelect() ;
					eqnsResults = com.hp.hpl.jena.query.ResultSetFactory.makeRewindable(qexec.execSelect()) ;
					
					if (eqnsResults.hasNext()) {
					
						listOfEqns = getEqnUrisFromResults(eqnsResults);
			            eqnsResults.reset();

						cgIns = qhmodel.createIndividual(METAMODEL_PREFIX + "CG_" + System.currentTimeMillis(), getTheJenaModel().getOntClass(METAMODEL_CCG));

						// Each comp graph is attached to the execution instance
						qhmodel.add(ce, getTheJenaModel().getOntProperty(METAMODEL_COMPGRAPH_PROP), cgIns);
						
						// Retrieve Models & Nodes
						queryStr = RETRIEVE_MODELS.replaceAll("EQNSLIST", listOfEqns);
						models = queryKnowledgeGraph(queryStr, getTheJenaModel());
						
						queryStr = RETRIEVE_NODES.replaceAll("EQNSLIST", listOfEqns);
						queryStr = queryStr.replaceAll("COMPGRAPH", "<" + cgIns.getURI() + ">");
						nodes = queryKnowledgeGraph(queryStr, getTheJenaModel().union(qhmodel));
						
						modelsCSVString = convertResultSetToString(models);
						nodesCSVString = convertResultSetToString(nodes);
						
						cgJson = kgResultsToJson(nodesCSVString, modelsCSVString);
						dbnJson = generateDBNjson(cgJson);
						class2lbl = getClassLabelMapping(dbnJson);
	
						// Run the model
						dbnResultsJson = executeDBN(dbnJson);
			            lbl2value = getLabelToMeanStdMapping(dbnResultsJson);
						

			            createCGsubgraphs(cgIns, eqnsResults, class2lbl, lbl2value, outputInstance);
			            
			            //TODO: create ResultSet
			            
					}
					// else continue looking
				}
			}
			
		
		
		
		
		//TODO: need to get node count. 
		//Integer numNodes = 
		//Integer numNodes = 1;
		
    		
  		//qhmodel.write(System.out,"RDF/XML-ABBREV");
   		//qhmodel.write(System.out,"TTL");
    		

			
			
			// Save metadata owl file 
			saveMetaDataFile(resource);
		
//			// create ResultSet
//			results[0] = retrieveCompGraph(resource, cgIns);
//			
//			results[1] = retrieveValues(resource, cgIns);
			
			return results;
				
		} catch (Exception e) {
			// Auto-generated catch block
			//System.out.println(e.getMessage());
			//TODO: reset model
			e.printStackTrace();
		}

		
		
		
//		Object[] results = new Object[outputInstance.keySet().size()];
//		for(OntClass oclass : outputInstance.keySet()) {
//			results[0] = outputInstance.get(oclass);
//	
//		}
		

		
		
		
		return null; //results;
	}


	private ResultSet retrieveValues(Resource resource, Individual cgIns) throws Exception {
		String query = RESULTSQUERY.replaceAll("COMPGRAPH", "<" + cgIns.getURI() + ">");
		return runReasonerQuery(resource, query);
	}


	private ResultSet retrieveCompGraph(Resource resource, Individual cgIns) throws Exception {
		//String cgqtemp = CGQUERY.replaceAll("COMPGRAPH", "<" + cgIns.getURI() + ">");
		String query = CGQUERYWITHVALUES.replaceAll("COMPGRAPH", "<" + cgIns.getURI() + ">");
		//System.out.println(CGQUERYWITHVALUES.replaceAll("COMPGRAPH", "<" + cgIns.getURI() + ">"));
		//System.out.println(cgquery);
		
		return runReasonerQuery(resource, query);
	}

	
	
	private ResultSet runReasonerQuery(Resource resource, String query) throws Exception {
		String modelFolderUri = getModelFolderPath(resource); //getOwlModelsFolderPath(path).toString(); 
		final String format = ConfigurationManager.RDF_XML_ABBREV_FORMAT;
		IConfigurationManagerForIDE configMgr;

		
		configMgr = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(modelFolderUri, format);
		IReasoner reasoner = configMgr.getReasoner();
	
		
		if (!reasoner.isInitialized()) {
			reasoner.setConfigurationManager(configMgr);
			//String modelName = configMgr.getPublicUriFromActualUrl(new SadlUtils().fileNameToFileUrl(modelFolderUri + "/" + owlFileName));
			//model name something like http://aske.ge.com/metamodel
			reasoner.initializeReasoner(modelFolderUri, getModelName(), format); 
			//reasoner.loadInstanceData(qhmodel);
			//System.out.print("reasoner is not initialized");
			//return null;
		}
	
		reasoner.loadInstanceData(qhmodel);	//Need to load new metadata
		String pquery = reasoner.prepareQuery(query);

		ResultSet res = reasoner.ask(pquery);
		return res;
	}


	private void saveMetaDataFile(Resource resource) {
		// TODO Auto-generated method stub
		String qhOwlFileWithPath = getModelFolderPath(resource) + File.separator + qhOwlFileName;
		File f = new File(qhOwlFileWithPath);

		String qhGlobalPrefix = null;
		try {
			getConfigMgr(null).saveOwlFile(qhmodel, qhModelName, qhOwlFileWithPath);
			String fileUrl = (new UtilsForJena()).fileNameToFileUrl(qhOwlFileWithPath);
			if (!f.exists() && !f.isDirectory()) {
				getConfigMgr(null).addMapping(fileUrl, qhModelName, qhGlobalPrefix, false, "DialogInference"); //Only if new file
			}
		} catch (Exception e) {
			//  Auto-generated catch block
			e.printStackTrace();
		} 

	}


//	while( models.hasNext() ) {
//	soln = models.nextSolution() ;
//RDFNode m = soln.get("?Model") ; 
//RDFNode i = soln.get("?Input") ;
//String lbl = soln.get("?InputLabel").toString();
//RDFNode o = soln.get("?Output") ;
//RDFNode f = soln.get("?ModelForm") ;
//}


//	while( nodes.hasNext() ) {
//		soln = nodes.nextSolution() ;
//RDFNode n = soln.get("?Node") ; 
//RDFNode c = soln.get("?Child") ;
//RDFNode d = soln.get("?Distribution") ;
//}







	private void createCGsubgraphs(Individual cgIns, ResultSetRewindable eqnsResults, 
			Map<String, String> class2lbl,
			Map<String, String[]> lbl2value, 
			Map<OntClass, Individual> outputInstance) {
		
		// TODO Auto-generated method stub
		OntProperty subgraphprop = 	getTheJenaModel().getOntProperty(METAMODEL_SUBG_PROP);
		OntProperty cgraphprop = 	getTheJenaModel().getOntProperty(METAMODEL_CGRAPH_PROP);
		OntProperty hasEqnProp = 	getTheJenaModel().getOntProperty(METAMODEL_HASEQN_PROP);
		OntProperty outputprop = 	getTheJenaModel().getOntProperty(METAMODEL_OUTPUT_PROP);
		
		Individual sgIns, dbnIns, outpIns;

		//need to do before querying for Nodes
//		qhmodel.add(ce, getTheJenaModel().getOntProperty(METAMODEL_COMPGRAPH_PROP), cgIns);

		//String cgInsStr = cgIns.getURI();
		//System.out.println("***CG instance: " + cgInsStr);
		
		QuerySolution soln;

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
				outpIns = outputInstance.get(o);
				//qhmodel.add(sgIns, outputprop, outputInstance.get(o));
			}
			else {
				outpIns = createIndividualOfClass(qhmodel,o.toString());
//				getTheJenaModel().add(sgIns, outputprop, outpIns);
//				qhmodel.add(sgIns, outputprop, outpIns);
			}
			getTheJenaModel().add(sgIns, outputprop, outpIns);
			qhmodel.add(sgIns, outputprop, outpIns);
			
				String ocls = o.toString();
			String cls = class2lbl.get(ocls);
			String[] ms = lbl2value.get(cls);  //class2lbl.get(o.toString()));
			qhmodel.add(outpIns, getTheJenaModel().getProperty(VALUE_PROP), ms[0] );
			qhmodel.add(outpIns, getTheJenaModel().getProperty(STDDEV_PROP), ms[1] );
			//Add to JenaModel too?
		}
	}








	private Map<String, String[]> getLabelToMeanStdMapping(String dbnResultsJson) {
        Map<String,String[]> lbl2value = new HashMap<String, String[]>();
        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(dbnResultsJson);
        JsonObject jsonObject;
        JsonObject jres;
        Set<Entry<String, JsonElement>> keys;

        if (jsonTree.isJsonObject()) {
            jsonObject = jsonTree.getAsJsonObject();
            if (jsonObject.has("results")) {
                jres = (JsonObject) jsonObject.get("results");
                keys = jres.entrySet();
                
                for (Map.Entry<String, JsonElement> k: keys) {
                	System.out.println(k.getKey() + "   " + jres.get(k.getKey()).toString());
                	JsonObject vpair = jres.getAsJsonObject(k.getKey());
                	if (vpair.has("mean")) {
	                	String[] vpr = new String[2];
	                	vpr[0] = vpair.get("mean").toString();
	                	vpr[1] = vpair.get("std").toString();
	                	
	                	lbl2value.put(k.getKey(), vpr);
                	}
                }	
            }
        }
        return lbl2value;
	}


	private Map<String, String> getClassLabelMapping(String dbnJson) {
        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(dbnJson);
        Map<String,String> class2lbl = new HashMap<String, String>();
        JsonObject jsonObject;
        JsonObject jres;
        Set<Entry<String, JsonElement>> keys;
        
        if (jsonTree.isJsonObject()) {
            jsonObject = jsonTree.getAsJsonObject();
            //JsonArray jsonRes = (JsonArray) jsonObject.get("results");
            
            jres = (JsonObject) jsonObject.get("mapping");
            keys = jres.entrySet();
            
            for (Map.Entry<String, JsonElement> k: keys) {
            	System.out.println(k.getKey() + "   " + k.getValue());  //jres.get(k.getKey()).toString());
            	//String foo = k.getValue().getAsString();
            	class2lbl.put(k.getValue().getAsString(), k.getKey());
            }	
        }
        return class2lbl;
	}


	private ResultSetRewindable queryKnowledgeGraph(String queryStr, Model model) {
		System.out.println(queryStr);
		com.hp.hpl.jena.query.Query qm = QueryFactory.create(queryStr);
		QueryExecution qe = QueryExecutionFactory.create(qm, model); 
		//com.hp.hpl.jena.query.ResultSetRewindable 
		return com.hp.hpl.jena.query.ResultSetFactory.makeRewindable(qe.execSelect()) ;
	}


	private String getEqnUrisFromResults(ResultSetRewindable eqnsResults) {
		QuerySolution soln;
		String listOfEqns = "";
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
		if(listOfEqns.length() <= 0)
			return "";
		else {
			listOfEqns = listOfEqns.substring(0,listOfEqns.length()-1);
			return listOfEqns;
		}
	}


	@SuppressWarnings("deprecation")
	private String executeDBN(String jsonTxt) throws Exception {
		DefaultHttpClient httpclient = new DefaultHttpClient();
        //httppost = new HttpPost("http://3.1.46.19:5000/process");
		HttpPost httppost = new HttpPost("http://alpha.ubl.research.ge.com:5000/process");
        //httppost = new HttpPost("http://vesuvius001.crd.ge.com:5000/process");
        httppost.setHeader("Accept", "*/*");
        httppost.setHeader("Content-type", "application/json");
        
        //String testJson = "{\"outputs\":[],\"techniqueName\":\"dbn\",\"mapping\":{\"ts0\":\"http://aske.ge.com/hypersonics#StaticTemperature\",\"altitude\":\"http://aske.ge.com/hypersonics#Altitude\",\"tt75\":\"http://aske.ge.com/hypersonics#TotalTemperature\",\"mach\":\"http://aske.ge.com/hypersonics#MachSpeed\",\"u0\":\"http://aske.ge.com/hypersonics#AirSpeed\",\"a0\":\"http://aske.ge.com/hypersonics#SpeedOfSound\"},\"inputs\":[],\"headerMappings\":{},\"additionalFilesIds\":{},\"dataSourcesInfo\":{},\"modelName\":\"defaultModel\",\"analyticSettings\":{\"ExpertKnowledge\":{},\"observationDataSources\":{},\"riskRollup\":{},\"maintenanceLimits\":[],\"ObservationData\":{},\"DBNSetup\":{\"PlotFlag\":false,\"TaskName\":\"Calibration\",\"TrackingTimeSteps\":1,\"ParticleFilterOptions\":{\"BackendFname\":\"/tmp/Results\",\"Backend\":\"RAM\",\"ResampleThreshold\":0.4,\"BackendKeepVectors\":true,\"BackendKeepScalars\":true,\"Parallel\":true,\"NodeNamesNotRecorded\":[],\"ParallelProcesses\":\"5\"},\"WorkDir\":\"/tmp\",\"NumberOfSamples\":500},\"Nodes\":{\"ts0\":{\"Type\":\"Deterministic\",\"DistributionParameters\":{},\"ModelName\":\"ts0\",\"Distribution\":\"\",\"Parents\":[\"altitude\"],\"Tag\":[],\"Children\":[\"a0\",\"tt75\"]},\"altitude\":{\"IsDistributionFixed\":false,\"Type\":\"Stochastic_Transient\",\"DistributionParameters\":{\"lower\":-460.0,\"upper\":200.0},\"Parents\":[],\"Distribution\":\"Uniform\",\"Tag\":[],\"Children\":[\"ts0\"],\"InitialChildren\":[],\"Range\":[-460.0,200.0]},\"tt75\":{\"IsDistributionFixed\":true,\"Type\":\"Deterministic\",\"DistributionParameters\":{},\"ModelName\":\"tt75\",\"Distribution\":\"Uniform\",\"Parents\":[\"mach\",\"ts0\"],\"Tag\":[],\"Children\":[],\"ObservationData\":[800]},\"mach\":{\"Type\":\"Deterministic\",\"DistributionParameters\":{},\"ModelName\":\"mach\",\"Distribution\":\"\",\"Parents\":[\"u0\",\"a0\"],\"Tag\":[],\"Children\":[\"tt75\"]},\"u0\":{\"IsDistributionFixed\":true,\"Type\":\"Stochastic_Transient\",\"DistributionParameters\":{\"lower\":-200.0,\"upper\":200.0},\"Parents\":[],\"Distribution\":\"Uniform\",\"Tag\":[],\"Children\":[\"mach\"],\"ObservationData\":[],\"InitialChildren\":[],\"Range\":[-200.0,200.0]},\"a0\":{\"Type\":\"Deterministic\",\"DistributionParameters\":{},\"ModelName\":\"a0\",\"Distribution\":\"\",\"Parents\":[\"ts0\"],\"Tag\":[],\"Children\":[\"mach\"]}},\"PriorData\":{},\"UT_node_init_data\":{\"UT_node_names\":[]},\"fullNetwork\":{},\"Models\":{\"ts0\":{\"FunctionName\":\"ts0\",\"Input\":[\"altitude\"],\"Type\":\"Equation\",\"Output\":[\"a0\",\"tt75\"],\"ModelForm\":\"518.6-3.56 * altitude /1000.0\"},\"tt75\":{\"FunctionName\":\"tt75\",\"Input\":[\"mach\",\"ts0\"],\"Type\":\"Equation\",\"Output\":[],\"ModelForm\":\"ts0*(1.0 + 0.5*(1.4-1.0)*mach*mach)\"},\"mach\":{\"FunctionName\":\"mach\",\"Input\":[\"u0\",\"a0\"],\"Type\":\"Equation\",\"Output\":[\"tt75\"],\"ModelForm\":\"u0/5280.0 * 3600.0 / a0\"},\"a0\":{\"FunctionName\":\"a0\",\"Input\":[\"ts0\"],\"Type\":\"Equation\",\"Output\":[\"mach\"],\"ModelForm\":\"(1.4*1718.0*ts0)**0.5\"}},\"StateVariableDefinition\":{},\"InspectionSchedule\":{}},\"taskName\":\"build\",\"dataSourceIds\":{},\"workDir\":\"/tmp\",\"dataSources\":{}}";

        //String testJson = "{ \"taskName\": \"build\", \"techniqueName\": \"dbn\", \"modelName\": \"Model_20190115153656\", \"analyticSettings\": { \"ObservationData\": {}, \"ExpertKnowledge\": {}, \"PriorData\": {}, \"StateVariableDefinition\": {}, \"Models\": { \"ps0\": { \"Input\": [ \"ts0\", \"altitude\" ], \"Type\": \"Equation\", \"FunctionName\": \"ps0\", \"ModelForm\": \"2116.0*(ts0/518.6)**5.256\", \"Output\": [ \"pt0\" ] }, \"a0\": { \"Input\": [ \"gama\", \"Rgas\", \"ts0\" ], \"Type\": \"Equation\", \"FunctionName\": \"a0\", \"ModelForm\": \"(gama*Rgas*ts0)**0.5\", \"Output\": [ \"mach\" ] }, \"mach\": { \"Input\": [ \"a0\", \"speed\" ], \"Type\": \"Equation\", \"FunctionName\": \"mach\", \"ModelForm\": \"(speed/5280.0*3600.0)/a0\", \"Output\": [ \"tt0\" ] }, \"tt0\": { \"Input\": [ \"ts0\", \"gama\", \"mach\" ], \"Type\": \"Equation\", \"FunctionName\": \"tt0\", \"ModelForm\": \"ts0*(1.0 + 0.5*(gama-1.0)*mach*mach)\", \"Output\": [ \"pt0\" ] }, \"pt0\": { \"Input\": [ \"tt0\", \"ps0\", \"ts0\", \"gama\" ], \"Type\": \"Equation\", \"FunctionName\": \"pt0\", \"ModelForm\": \"ps0*(tt0/ts0)**(gama/(gama-1.0))\", \"Output\": [] }, \"ts0\": { \"Input\": [ \"altitude\" ], \"Type\": \"Equation\", \"FunctionName\": \"ts0\", \"ModelForm\": \"518.6-3.56*altitude/1000.0\", \"Output\": [ \"ps0\", \"a0\", \"tt0\", \"pt0\" ] } }, \"Nodes\": { \"altitude\": { \"Type\": \"Stochastic_Transient\", \"Tag\": [], \"Distribution\": \"Uniform\", \"DistributionParameters\": { \"lower\": \"0\", \"upper\": \"100000\" }, \"InitialChildren\": [], \"Children\": [ \"ps0\", \"ts0\" ], \"ObservationData\":[10000.0], \"Parents\": [], \"IsDistributionFixed\": true, \"Range\": [ \"0\", \"100000\" ] }, \"ps0\": { \"Type\": \"Deterministic\", \"Tag\": [], \"Distribution\": \"\", \"DistributionParameters\": {}, \"ModelName\": \"ps0\", \"Parents\": [ \"ts0\", \"altitude\" ], \"Children\": [ \"pt0\" ] }, \"gama\": { \"Type\": \"Constant\", \"Tag\": [], \"Distribution\": \"\", \"DistributionParameters\": {}, \"Value\": [ \"1.4\" ], \"Parents\": [], \"Children\": [ \"a0\", \"tt0\", \"pt0\" ] }, \"Rgas\": { \"Type\": \"Constant\", \"Tag\": [], \"Distribution\": \"\", \"DistributionParameters\": {}, \"Value\": [ \"1718.0\" ], \"Parents\": [], \"Children\": [ \"a0\" ] }, \"a0\": { \"Type\": \"Deterministic\", \"Tag\": [], \"Distribution\": \"\", \"DistributionParameters\": {}, \"ModelName\": \"a0\", \"Parents\": [ \"gama\", \"Rgas\", \"ts0\" ], \"Children\": [ \"mach\" ] }, \"speed\": { \"Type\": \"Stochastic_Transient\", \"Tag\": [], \"Distribution\": \"Uniform\", \"DistributionParameters\": { \"lower\": \"0\", \"upper\": \"1000\" }, \"InitialChildren\": [], \"Children\": [ \"mach\" ], \"ObservationData\":[], \"Parents\": [], \"IsDistributionFixed\": true, \"Range\": [ \"0\", \"1000\" ] }, \"mach\": { \"Type\": \"Deterministic\", \"Tag\": [], \"Distribution\": \"\", \"DistributionParameters\": {}, \"ModelName\": \"mach\", \"Parents\": [ \"a0\", \"speed\" ], \"Children\": [ \"tt0\" ] }, \"tt0\": { \"Type\": \"Deterministic\", \"Tag\": [], \"Distribution\": \"\", \"DistributionParameters\": {}, \"ModelName\": \"tt0\", \"Parents\": [ \"ts0\", \"gama\", \"mach\" ], \"Children\": [ \"pt0\" ] }, \"pt0\": { \"Type\": \"Deterministic\", \"Tag\": [], \"Distribution\": \"\", \"DistributionParameters\": {}, \"ModelName\": \"pt0\", \"Parents\": [ \"tt0\", \"ps0\", \"ts0\", \"gama\" ], \"Children\": [] }, \"ts0\": { \"Type\": \"Deterministic\", \"Tag\": [], \"Distribution\": \"\", \"DistributionParameters\": {}, \"ModelName\": \"ts0\", \"Parents\": [ \"altitude\" ], \"Children\": [ \"ps0\", \"a0\", \"tt0\", \"pt0\" ] } }, \"DBNSetup\": { \"WorkDir\": \"/hostfiles\", \"ParticleFilterOptions\": { \"NodeNamesNotRecorded\": [], \"BackendKeepVectors\": true, \"BackendFname\": \"/hostfiles/Results\", \"BackendKeepScalars\": true, \"ParallelProcesses\": \"5\", \"ResampleThreshold\": 0.4, \"Parallel\": true, \"Backend\": \"RAM\" }, \"NumberOfSamples\": 500, \"PlotFlag\": false, \"TrackingTimeSteps\": \"1\", \"TaskName\": \"Prognosis\" }, \"UT_node_init_data\": { \"UT_node_names\": [] }, \"InspectionSchedule\": {}, \"riskRollup\": {}, \"maintenanceLimits\": [], \"observationDataSources\": {}, \"fullNetwork\": { \"nodes\": [ { \"id\": \"altitude\", \"data\": [ { \"id\": 0, \"label\": \"Type\", \"value\": \"Stochastic\", \"varName\": \"type\", \"validation\": \"\" }, { \"id\": 1, \"label\": \"Distribution\", \"value\": \"Uniform\", \"varName\": \"distribution\", \"validation\": \"\" }, { \"id\": 2, \"label\": \"Is distribution fixed?\", \"value\": \"Yes\", \"varName\": \"isDistributionFixed\", \"validation\": \"\" }, { \"id\": 3, \"label\": \"Lower\", \"value\": \"0\", \"varName\": \"lower\", \"validation\": \"^[0-9]+$\" }, { \"id\": 4, \"label\": \"Upper\", \"value\": \"100000\", \"varName\": \"upper\", \"validation\": \"^[0-9]+$\" }, { \"id\": 5, \"label\": \"Observation Data\", \"value\": { \"items\": [], \"columns\": [], \"filter\": { \"template\": [], \"data\": [] }, \"dataSources\": [], \"columnsCount\": 0 }, \"varName\": \"observationData\", \"config\": { \"dataSource\": \"dataSource\", \"numeric\": true }, \"validation\": \"\" }, { \"id\": 6, \"label\": \"Range\", \"value\": [ \"0\", \"100000\" ], \"varName\": \"range\", \"config\": { \"fixedLength\": 2 }, \"validation\": \"^[0-9]+$\" }, { \"id\": 7, \"label\": \"Tags\", \"value\": [], \"varName\": \"tags\", \"validation\": \"\" } ], \"inputs\": [ { \"id\": \"altitudeInput0\", \"label\": \"lower\", \"fixedLabel\": true, \"node\": null, \"nodeId\": \"altitude\", \"outputConnected\": null, \"outputIdConnected\": null, \"dataEditable\": true, \"filledData\": false }, { \"id\": \"altitudeInput1\", \"label\": \"upper\", \"fixedLabel\": true, \"node\": null, \"nodeId\": \"altitude\", \"outputConnected\": null, \"outputIdConnected\": null, \"dataEditable\": true, \"filledData\": false } ], \"outputs\": [ { \"id\": \"altitudeOutput0\", \"label\": \"output\", \"fixedLabel\": true, \"node\": null, \"nodeId\": \"altitude\", \"inputsConnected\": [], \"inputsIdConnected\": [ \"ps0.ps0Input1\", \"ts0.ts0Input0\" ] } ], \"nodeType\": { \"id\": \"stochastic-uniform\", \"name\": \"Stochastic - Uniform\", \"inputCount\": 2, \"outputCount\": 1, \"inputLabels\": [ \"lower\", \"upper\" ], \"outputLabels\": [ \"output\" ], \"formTemplate\": [ { \"index\": 0, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": false, \"cols\": 12, \"value\": \"Stochastic\", \"label\": \"Type\", \"varName\": \"type\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [] }, { \"index\": 1, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": false, \"cols\": 12, \"value\": \"Uniform\", \"label\": \"Distribution\", \"varName\": \"distribution\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [] }, { \"index\": 2, \"description\": \"\", \"component\": \"radio\", \"required\": true, \"editable\": true, \"cols\": 12, \"value\": \"Yes\", \"label\": \"Is distribution fixed?\", \"varName\": \"isDistributionFixed\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [ \"Yes\", \"No\" ], \"finalValue\": { \"Yes\": true, \"No\": false } }, { \"index\": 3, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Lower\", \"varName\": \"lower\", \"validation\": \"^[0-9]+$\", \"placeholder\": \"\", \"options\": [] }, { \"index\": 4, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Upper\", \"varName\": \"upper\", \"validation\": \"^[0-9]+$\", \"placeholder\": \"\", \"options\": [] }, { \"index\": 5, \"description\": \"\", \"component\": \"tableInput\", \"required\": false, \"editable\": false, \"cols\": 12, \"value\": { \"items\": [], \"columns\": [] }, \"label\": \"Observation Data\", \"varName\": \"observationData\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"config\": { \"dataSource\": \"dataSource\", \"numeric\": true } }, { \"index\": 6, \"description\": \"\", \"component\": \"smallArrayInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": [ null, null ], \"label\": \"Range\", \"varName\": \"range\", \"config\": { \"fixedLength\": 2 }, \"validation\": \"^[0-9]+$\", \"placeholder\": \"\", \"options\": [] }, { \"index\": 7, \"description\": \"\", \"component\": \"dbnTagsInput\", \"required\": false, \"editable\": false, \"cols\": 12, \"value\": [], \"label\": \"Tags\", \"varName\": \"tags\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [] } ], \"outputTemplate\": { \"Type\": \"@type\", \"Tag\": \"@tags\", \"Distribution\": \"@distribution\", \"DistributionParameters\": { \"lower\": \"@lower\", \"upper\": \"@upper\" }, \"InitialChildren\": [], \"Children\": [], \"Parents\": [], \"ObservationData\": \"@?observationData.items\", \"FullObservationData\": \"@?observationData.fullData\", \"IsDistributionFixed\": \"@isDistributionFixed\", \"Range\": \"@?range\" }, \"upgradeValues\": [ { \"varName\": \"observationData\", \"oldValueType\": \"array\", \"newValueTemplate\": { \"items\": \"@oldValue\", \"columns\": [] } } ] }, \"inputCount\": 2, \"inputCountFixed\": true, \"outputCount\": 1, \"outputCountFixed\": true, \"top\": \"150px\", \"left\": \"457px\" }, { \"id\": \"ps0\", \"data\": [ { \"id\": 0, \"label\": \"Type\", \"value\": \"Deterministic\", \"varName\": \"type\", \"validation\": \"\" }, { \"id\": 1, \"label\": \"Model Name\", \"value\": \"ps0\", \"varName\": \"modelName\", \"validation\": \"\" }, { \"id\": 2, \"label\": \"Model Type\", \"value\": \"Equation\", \"varName\": \"modelType\", \"validation\": \"\" }, { \"id\": 3, \"label\": \"Function Name\", \"value\": \"ps0\", \"varName\": \"functionName\", \"validation\": \"\" }, { \"id\": 4, \"label\": \"Model Form\", \"value\": \"2116.0*(ts0/518.6)**5.256\", \"varName\": \"modelForm\", \"validation\": \"\" }, { \"id\": 5, \"label\": \"Code\", \"value\": \"\", \"varName\": \"code\", \"config\": { \"mode\": \"python\", \"require\": [ \"ace/ext/language_tools\" ], \"advanced\": { \"enableSnippets\": true, \"enableBasicAutocompletion\": true, \"enableLiveAutocompletion\": true } }, \"validation\": \"\" }, { \"id\": 6, \"label\": \"Tags\", \"value\": [], \"varName\": \"tags\", \"validation\": \"\" } ], \"inputs\": [ { \"id\": \"ps0Input0\", \"label\": \"ps0Input0\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"ps0\", \"outputConnected\": null, \"outputIdConnected\": \"ts0.ts0Output0\", \"dataEditable\": false, \"filledData\": false }, { \"id\": \"ps0Input1\", \"label\": \"ps0Input1\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"ps0\", \"outputConnected\": null, \"outputIdConnected\": \"altitude.altitudeOutput0\", \"dataEditable\": false, \"filledData\": false } ], \"outputs\": [ { \"id\": \"ps0Output0\", \"label\": \"ps0Output0\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"ps0\", \"inputsConnected\": [], \"inputsIdConnected\": [ \"pt0.pt0Input1\" ] } ], \"nodeType\": { \"id\": \"deterministic\", \"name\": \"Deterministic\", \"formTemplate\": [ { \"index\": 0, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": false, \"cols\": 12, \"value\": \"Deterministic\", \"label\": \"Type\", \"varName\": \"type\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 0 }, { \"index\": 1, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Name\", \"varName\": \"modelName\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 1 }, { \"index\": 2, \"description\": \"\", \"component\": \"select\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Type\", \"varName\": \"modelType\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [ \"Equation\", \"InitialState\", \"PythonFunction\" ], \"id\": 2 }, { \"index\": 3, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Function Name\", \"varName\": \"functionName\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 3 }, { \"index\": 4, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Form\", \"varName\": \"modelForm\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 4 }, { \"index\": 5, \"description\": \"\", \"component\": \"codeEditor\", \"required\": false, \"editable\": true, \"cols\": 12, \"value\": \"\", \"label\": \"Code\", \"varName\": \"code\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"config\": { \"mode\": \"python\", \"require\": [ \"ace/ext/language_tools\" ], \"advanced\": { \"enableSnippets\": true, \"enableBasicAutocompletion\": true, \"enableLiveAutocompletion\": true } }, \"id\": 5 }, { \"index\": 6, \"description\": \"\", \"component\": \"dbnTagsInput\", \"required\": false, \"editable\": false, \"cols\": 12, \"value\": [], \"label\": \"Tags\", \"varName\": \"tags\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 6 } ], \"outputTemplate\": { \"Type\": \"@type\", \"Tag\": \"@tags\", \"Distribution\": \"\", \"DistributionParameters\": {}, \"ModelName\": \"@modelName\", \"Parents\": [], \"Children\": [] } }, \"inputCount\": 2, \"outputCount\": 1, \"top\": \"325px\", \"left\": \"577px\" }, { \"id\": \"gama\", \"data\": [ { \"id\": 0, \"label\": \"Type\", \"value\": \"Constant\", \"varName\": \"type\", \"validation\": \"\" }, { \"id\": 1, \"label\": \"Value\", \"value\": [ \"1.4\" ], \"varName\": \"value\", \"config\": { \"minLength\": 1 }, \"validation\": \"\" }, { \"id\": 2, \"label\": \"Initial Children\", \"value\": [ null ], \"varName\": \"initialChildren\", \"config\": { \"minLength\": 1 }, \"validation\": \"^[0-9]+$\" }, { \"id\": 3, \"label\": \"Tags\", \"value\": [], \"varName\": \"tags\", \"validation\": \"\" } ], \"inputs\": [], \"outputs\": [ { \"id\": \"gamaOutput0\", \"label\": \"gamaOutput0\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"gama\", \"inputsConnected\": [], \"inputsIdConnected\": [ \"a0.a0Input0\", \"tt0.tt0Input1\", \"pt0.pt0Input3\" ] } ], \"nodeType\": { \"id\": \"constant\", \"name\": \"Constant\", \"class\": \"circle\", \"inputCount\": 0, \"outputCount\": 1, \"formTemplate\": [ { \"index\": 0, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": false, \"cols\": 12, \"value\": \"Constant\", \"label\": \"Type\", \"varName\": \"type\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [] }, { \"index\": 1, \"description\": \"\", \"component\": \"smallArrayInput\", \"required\": true, \"editable\": true, \"cols\": 12, \"value\": [ null ], \"label\": \"Value\", \"varName\": \"value\", \"config\": { \"minLength\": 1 }, \"validation\": \"\", \"placeholder\": \"\", \"options\": [] }, { \"index\": 2, \"description\": \"\", \"component\": \"smallArrayInput\", \"required\": false, \"editable\": true, \"cols\": 12, \"value\": [ null ], \"label\": \"Initial Children\", \"varName\": \"initialChildren\", \"config\": { \"minLength\": 1 }, \"validation\": \"^[0-9]+$\", \"placeholder\": \"\", \"options\": [] }, { \"index\": 3, \"description\": \"\", \"component\": \"dbnTagsInput\", \"required\": false, \"editable\": false, \"cols\": 12, \"value\": [], \"label\": \"Tags\", \"varName\": \"tags\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [] } ], \"outputTemplate\": { \"Type\": \"@type\", \"Tag\": \"@tags\", \"Distribution\": \"\", \"DistributionParameters\": {}, \"Value\": \"@value\", \"Parents\": [], \"Children\": [], \"InitialChildren\": \"@?initialChildren\" }, \"upgradeValues\": [ { \"varName\": \"value\", \"oldValueType\": \"primitive\", \"newValueTemplate\": [ \"@oldValue\" ] } ] }, \"inputCount\": 0, \"inputCountFixed\": true, \"outputCount\": 1, \"outputCountFixed\": true, \"top\": \"335px\", \"left\": \"474px\" }, { \"id\": \"Rgas\", \"data\": [ { \"id\": 0, \"label\": \"Type\", \"value\": \"Constant\", \"varName\": \"type\", \"validation\": \"\" }, { \"id\": 1, \"label\": \"Value\", \"value\": [ \"1718.0\" ], \"varName\": \"value\", \"config\": { \"minLength\": 1 }, \"validation\": \"\" }, { \"id\": 2, \"label\": \"Initial Children\", \"value\": [ null ], \"varName\": \"initialChildren\", \"config\": { \"minLength\": 1 }, \"validation\": \"^[0-9]+$\" }, { \"id\": 3, \"label\": \"Tags\", \"value\": [], \"varName\": \"tags\", \"validation\": \"\" } ], \"inputs\": [], \"outputs\": [ { \"id\": \"RgasOutput0\", \"label\": \"RgasOutput0\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"Rgas\", \"inputsConnected\": [], \"inputsIdConnected\": [ \"a0.a0Input1\" ] } ], \"nodeType\": { \"id\": \"constant\", \"name\": \"Constant\", \"class\": \"circle\", \"inputCount\": 0, \"outputCount\": 1, \"formTemplate\": [ { \"index\": 0, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": false, \"cols\": 12, \"value\": \"Constant\", \"label\": \"Type\", \"varName\": \"type\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [] }, { \"index\": 1, \"description\": \"\", \"component\": \"smallArrayInput\", \"required\": true, \"editable\": true, \"cols\": 12, \"value\": [ null ], \"label\": \"Value\", \"varName\": \"value\", \"config\": { \"minLength\": 1 }, \"validation\": \"\", \"placeholder\": \"\", \"options\": [] }, { \"index\": 2, \"description\": \"\", \"component\": \"smallArrayInput\", \"required\": false, \"editable\": true, \"cols\": 12, \"value\": [ null ], \"label\": \"Initial Children\", \"varName\": \"initialChildren\", \"config\": { \"minLength\": 1 }, \"validation\": \"^[0-9]+$\", \"placeholder\": \"\", \"options\": [] }, { \"index\": 3, \"description\": \"\", \"component\": \"dbnTagsInput\", \"required\": false, \"editable\": false, \"cols\": 12, \"value\": [], \"label\": \"Tags\", \"varName\": \"tags\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [] } ], \"outputTemplate\": { \"Type\": \"@type\", \"Tag\": \"@tags\", \"Distribution\": \"\", \"DistributionParameters\": {}, \"Value\": \"@value\", \"Parents\": [], \"Children\": [], \"InitialChildren\": \"@?initialChildren\" }, \"upgradeValues\": [ { \"varName\": \"value\", \"oldValueType\": \"primitive\", \"newValueTemplate\": [ \"@oldValue\" ] } ] }, \"inputCount\": 0, \"inputCountFixed\": true, \"outputCount\": 1, \"outputCountFixed\": true, \"top\": \"311px\", \"left\": \"161px\" }, { \"id\": \"a0\", \"data\": [ { \"id\": 0, \"label\": \"Type\", \"value\": \"Deterministic\", \"varName\": \"type\", \"validation\": \"\" }, { \"id\": 1, \"label\": \"Model Name\", \"value\": \"a0\", \"varName\": \"modelName\", \"validation\": \"\" }, { \"id\": 2, \"label\": \"Model Type\", \"value\": \"Equation\", \"varName\": \"modelType\", \"validation\": \"\" }, { \"id\": 3, \"label\": \"Function Name\", \"value\": \"a0\", \"varName\": \"functionName\", \"validation\": \"\" }, { \"id\": 4, \"label\": \"Model Form\", \"value\": \"(gama*Rgas*ts0)**0.5\", \"varName\": \"modelForm\", \"validation\": \"\" }, { \"id\": 5, \"label\": \"Code\", \"value\": \"\", \"varName\": \"code\", \"config\": { \"mode\": \"python\", \"require\": [ \"ace/ext/language_tools\" ], \"advanced\": { \"enableSnippets\": true, \"enableBasicAutocompletion\": true, \"enableLiveAutocompletion\": true } }, \"validation\": \"\" }, { \"id\": 6, \"label\": \"Tags\", \"value\": [], \"varName\": \"tags\", \"validation\": \"\" } ], \"inputs\": [ { \"id\": \"a0Input0\", \"label\": \"a0Input0\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"a0\", \"outputConnected\": null, \"outputIdConnected\": \"gama.gamaOutput0\", \"dataEditable\": false, \"filledData\": false }, { \"id\": \"a0Input1\", \"label\": \"a0Input1\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"a0\", \"outputConnected\": null, \"outputIdConnected\": \"Rgas.RgasOutput0\", \"dataEditable\": false, \"filledData\": false }, { \"id\": \"a0Input2\", \"label\": \"a0Input2\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"a0\", \"outputConnected\": null, \"outputIdConnected\": \"ts0.ts0Output0\", \"dataEditable\": false, \"filledData\": false } ], \"outputs\": [ { \"id\": \"a0Output0\", \"label\": \"a0Output0\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"a0\", \"inputsConnected\": [], \"inputsIdConnected\": [ \"mach.machInput0\" ] } ], \"nodeType\": { \"id\": \"deterministic\", \"name\": \"Deterministic\", \"formTemplate\": [ { \"index\": 0, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": false, \"cols\": 12, \"value\": \"Deterministic\", \"label\": \"Type\", \"varName\": \"type\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 0 }, { \"index\": 1, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Name\", \"varName\": \"modelName\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 1 }, { \"index\": 2, \"description\": \"\", \"component\": \"select\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Type\", \"varName\": \"modelType\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [ \"Equation\", \"InitialState\", \"PythonFunction\" ], \"id\": 2 }, { \"index\": 3, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Function Name\", \"varName\": \"functionName\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 3 }, { \"index\": 4, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Form\", \"varName\": \"modelForm\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 4 }, { \"index\": 5, \"description\": \"\", \"component\": \"codeEditor\", \"required\": false, \"editable\": true, \"cols\": 12, \"value\": \"\", \"label\": \"Code\", \"varName\": \"code\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"config\": { \"mode\": \"python\", \"require\": [ \"ace/ext/language_tools\" ], \"advanced\": { \"enableSnippets\": true, \"enableBasicAutocompletion\": true, \"enableLiveAutocompletion\": true } }, \"id\": 5 }, { \"index\": 6, \"description\": \"\", \"component\": \"dbnTagsInput\", \"required\": false, \"editable\": false, \"cols\": 12, \"value\": [], \"label\": \"Tags\", \"varName\": \"tags\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 6 } ], \"outputTemplate\": { \"Type\": \"@type\", \"Tag\": \"@tags\", \"Distribution\": \"\", \"DistributionParameters\": {}, \"ModelName\": \"@modelName\", \"Parents\": [], \"Children\": [] } }, \"inputCount\": 3, \"outputCount\": 1, \"top\": \"446px\", \"left\": \"305px\" }, { \"id\": \"speed\", \"data\": [ { \"id\": 0, \"label\": \"Type\", \"value\": \"Stochastic\", \"varName\": \"type\", \"validation\": \"\" }, { \"id\": 1, \"label\": \"Distribution\", \"value\": \"Uniform\", \"varName\": \"distribution\", \"validation\": \"\" }, { \"id\": 2, \"label\": \"Is distribution fixed?\", \"value\": \"Yes\", \"varName\": \"isDistributionFixed\", \"validation\": \"\" }, { \"id\": 3, \"label\": \"Lower\", \"value\": \"0\", \"varName\": \"lower\", \"validation\": \"^[0-9]+$\" }, { \"id\": 4, \"label\": \"Upper\", \"value\": \"1000\", \"varName\": \"upper\", \"validation\": \"^[0-9]+$\" }, { \"id\": 5, \"label\": \"Observation Data\", \"value\": { \"items\": [], \"columns\": [], \"filter\": { \"template\": [], \"data\": [] }, \"dataSources\": [], \"columnsCount\": 0 }, \"varName\": \"observationData\", \"config\": { \"dataSource\": \"dataSource\", \"numeric\": true }, \"validation\": \"\" }, { \"id\": 6, \"label\": \"Range\", \"value\": [ \"0\", \"1000\" ], \"varName\": \"range\", \"config\": { \"fixedLength\": 2 }, \"validation\": \"^[0-9]+$\" }, { \"id\": 7, \"label\": \"Tags\", \"value\": [], \"varName\": \"tags\", \"validation\": \"\" } ], \"inputs\": [ { \"id\": \"speedInput0\", \"label\": \"lower\", \"fixedLabel\": true, \"node\": null, \"nodeId\": \"speed\", \"outputConnected\": null, \"outputIdConnected\": null, \"dataEditable\": true, \"filledData\": false }, { \"id\": \"speedInput1\", \"label\": \"upper\", \"fixedLabel\": true, \"node\": null, \"nodeId\": \"speed\", \"outputConnected\": null, \"outputIdConnected\": null, \"dataEditable\": true, \"filledData\": false } ], \"outputs\": [ { \"id\": \"speedOutput0\", \"label\": \"output\", \"fixedLabel\": true, \"node\": null, \"nodeId\": \"speed\", \"inputsConnected\": [], \"inputsIdConnected\": [ \"mach.machInput1\" ] } ], \"nodeType\": { \"id\": \"stochastic-uniform\", \"name\": \"Stochastic - Uniform\", \"inputCount\": 2, \"outputCount\": 1, \"inputLabels\": [ \"lower\", \"upper\" ], \"outputLabels\": [ \"output\" ], \"formTemplate\": [ { \"index\": 0, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": false, \"cols\": 12, \"value\": \"Stochastic\", \"label\": \"Type\", \"varName\": \"type\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [] }, { \"index\": 1, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": false, \"cols\": 12, \"value\": \"Uniform\", \"label\": \"Distribution\", \"varName\": \"distribution\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [] }, { \"index\": 2, \"description\": \"\", \"component\": \"radio\", \"required\": true, \"editable\": true, \"cols\": 12, \"value\": \"Yes\", \"label\": \"Is distribution fixed?\", \"varName\": \"isDistributionFixed\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [ \"Yes\", \"No\" ], \"finalValue\": { \"Yes\": true, \"No\": false } }, { \"index\": 3, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Lower\", \"varName\": \"lower\", \"validation\": \"^[0-9]+$\", \"placeholder\": \"\", \"options\": [] }, { \"index\": 4, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Upper\", \"varName\": \"upper\", \"validation\": \"^[0-9]+$\", \"placeholder\": \"\", \"options\": [] }, { \"index\": 5, \"description\": \"\", \"component\": \"tableInput\", \"required\": false, \"editable\": false, \"cols\": 12, \"value\": { \"items\": [], \"columns\": [] }, \"label\": \"Observation Data\", \"varName\": \"observationData\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"config\": { \"dataSource\": \"dataSource\", \"numeric\": true } }, { \"index\": 6, \"description\": \"\", \"component\": \"smallArrayInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": [ null, null ], \"label\": \"Range\", \"varName\": \"range\", \"config\": { \"fixedLength\": 2 }, \"validation\": \"^[0-9]+$\", \"placeholder\": \"\", \"options\": [] }, { \"index\": 7, \"description\": \"\", \"component\": \"dbnTagsInput\", \"required\": false, \"editable\": false, \"cols\": 12, \"value\": [], \"label\": \"Tags\", \"varName\": \"tags\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [] } ], \"outputTemplate\": { \"Type\": \"@type\", \"Tag\": \"@tags\", \"Distribution\": \"@distribution\", \"DistributionParameters\": { \"lower\": \"@lower\", \"upper\": \"@upper\" }, \"InitialChildren\": [], \"Children\": [], \"Parents\": [], \"ObservationData\": \"@?observationData.items\", \"FullObservationData\": \"@?observationData.fullData\", \"IsDistributionFixed\": \"@isDistributionFixed\", \"Range\": \"@?range\" }, \"upgradeValues\": [ { \"varName\": \"observationData\", \"oldValueType\": \"array\", \"newValueTemplate\": { \"items\": \"@oldValue\", \"columns\": [] } } ] }, \"inputCount\": 2, \"inputCountFixed\": true, \"outputCount\": 1, \"outputCountFixed\": true, \"top\": \"166px\", \"left\": \"740px\" }, { \"id\": \"mach\", \"data\": [ { \"id\": 0, \"label\": \"Type\", \"value\": \"Deterministic\", \"varName\": \"type\", \"validation\": \"\" }, { \"id\": 1, \"label\": \"Model Name\", \"value\": \"mach\", \"varName\": \"modelName\", \"validation\": \"\" }, { \"id\": 2, \"label\": \"Model Type\", \"value\": \"Equation\", \"varName\": \"modelType\", \"validation\": \"\" }, { \"id\": 3, \"label\": \"Function Name\", \"value\": \"mach\", \"varName\": \"functionName\", \"validation\": \"\" }, { \"id\": 4, \"label\": \"Model Form\", \"value\": \"(speed/5280.0*3600.0)/a0\", \"varName\": \"modelForm\", \"validation\": \"\" }, { \"id\": 5, \"label\": \"Code\", \"value\": \"\", \"varName\": \"code\", \"config\": { \"mode\": \"python\", \"require\": [ \"ace/ext/language_tools\" ], \"advanced\": { \"enableSnippets\": true, \"enableBasicAutocompletion\": true, \"enableLiveAutocompletion\": true } }, \"validation\": \"\" }, { \"id\": 6, \"label\": \"Tags\", \"value\": [], \"varName\": \"tags\", \"validation\": \"\" } ], \"inputs\": [ { \"id\": \"machInput0\", \"label\": \"machInput0\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"mach\", \"outputConnected\": null, \"outputIdConnected\": \"a0.a0Output0\", \"dataEditable\": false, \"filledData\": false }, { \"id\": \"machInput1\", \"label\": \"machInput1\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"mach\", \"outputConnected\": null, \"outputIdConnected\": \"speed.speedOutput0\", \"dataEditable\": false, \"filledData\": false } ], \"outputs\": [ { \"id\": \"machOutput0\", \"label\": \"machOutput0\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"mach\", \"inputsConnected\": [], \"inputsIdConnected\": [ \"tt0.tt0Input2\" ] } ], \"nodeType\": { \"id\": \"deterministic\", \"name\": \"Deterministic\", \"formTemplate\": [ { \"index\": 0, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": false, \"cols\": 12, \"value\": \"Deterministic\", \"label\": \"Type\", \"varName\": \"type\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 0 }, { \"index\": 1, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Name\", \"varName\": \"modelName\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 1 }, { \"index\": 2, \"description\": \"\", \"component\": \"select\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Type\", \"varName\": \"modelType\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [ \"Equation\", \"InitialState\", \"PythonFunction\" ], \"id\": 2 }, { \"index\": 3, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Function Name\", \"varName\": \"functionName\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 3 }, { \"index\": 4, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Form\", \"varName\": \"modelForm\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 4 }, { \"index\": 5, \"description\": \"\", \"component\": \"codeEditor\", \"required\": false, \"editable\": true, \"cols\": 12, \"value\": \"\", \"label\": \"Code\", \"varName\": \"code\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"config\": { \"mode\": \"python\", \"require\": [ \"ace/ext/language_tools\" ], \"advanced\": { \"enableSnippets\": true, \"enableBasicAutocompletion\": true, \"enableLiveAutocompletion\": true } }, \"id\": 5 }, { \"index\": 6, \"description\": \"\", \"component\": \"dbnTagsInput\", \"required\": false, \"editable\": false, \"cols\": 12, \"value\": [], \"label\": \"Tags\", \"varName\": \"tags\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 6 } ], \"outputTemplate\": { \"Type\": \"@type\", \"Tag\": \"@tags\", \"Distribution\": \"\", \"DistributionParameters\": {}, \"ModelName\": \"@modelName\", \"Parents\": [], \"Children\": [] } }, \"inputCount\": 2, \"outputCount\": 1, \"top\": \"465px\", \"left\": \"801px\" }, { \"id\": \"tt0\", \"data\": [ { \"id\": 0, \"label\": \"Type\", \"value\": \"Deterministic\", \"varName\": \"type\", \"validation\": \"\" }, { \"id\": 1, \"label\": \"Model Name\", \"value\": \"tt0\", \"varName\": \"modelName\", \"validation\": \"\" }, { \"id\": 2, \"label\": \"Model Type\", \"value\": \"Equation\", \"varName\": \"modelType\", \"validation\": \"\" }, { \"id\": 3, \"label\": \"Function Name\", \"value\": \"tt0\", \"varName\": \"functionName\", \"validation\": \"\" }, { \"id\": 4, \"label\": \"Model Form\", \"value\": \"ts0*(1.0 + 0.5*(gama-1.0)*mach*mach)\", \"varName\": \"modelForm\", \"validation\": \"\" }, { \"id\": 5, \"label\": \"Code\", \"value\": \"\", \"varName\": \"code\", \"config\": { \"mode\": \"python\", \"require\": [ \"ace/ext/language_tools\" ], \"advanced\": { \"enableSnippets\": true, \"enableBasicAutocompletion\": true, \"enableLiveAutocompletion\": true } }, \"validation\": \"\" }, { \"id\": 6, \"label\": \"Tags\", \"value\": [], \"varName\": \"tags\", \"validation\": \"\" } ], \"inputs\": [ { \"id\": \"tt0Input0\", \"label\": \"tt0Input0\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"tt0\", \"outputConnected\": null, \"outputIdConnected\": \"ts0.ts0Output0\", \"dataEditable\": false, \"filledData\": false }, { \"id\": \"tt0Input1\", \"label\": \"tt0Input1\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"tt0\", \"outputConnected\": null, \"outputIdConnected\": \"gama.gamaOutput0\", \"dataEditable\": false, \"filledData\": false }, { \"id\": \"tt0Input2\", \"label\": \"tt0Input2\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"tt0\", \"outputConnected\": null, \"outputIdConnected\": \"mach.machOutput0\", \"dataEditable\": false, \"filledData\": false } ], \"outputs\": [ { \"id\": \"tt0Output0\", \"label\": \"tt0Output0\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"tt0\", \"inputsConnected\": [], \"inputsIdConnected\": [ \"pt0.pt0Input0\" ] } ], \"nodeType\": { \"id\": \"deterministic\", \"name\": \"Deterministic\", \"formTemplate\": [ { \"index\": 0, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": false, \"cols\": 12, \"value\": \"Deterministic\", \"label\": \"Type\", \"varName\": \"type\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 0 }, { \"index\": 1, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Name\", \"varName\": \"modelName\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 1 }, { \"index\": 2, \"description\": \"\", \"component\": \"select\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Type\", \"varName\": \"modelType\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [ \"Equation\", \"InitialState\", \"PythonFunction\" ], \"id\": 2 }, { \"index\": 3, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Function Name\", \"varName\": \"functionName\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 3 }, { \"index\": 4, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Form\", \"varName\": \"modelForm\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 4 }, { \"index\": 5, \"description\": \"\", \"component\": \"codeEditor\", \"required\": false, \"editable\": true, \"cols\": 12, \"value\": \"\", \"label\": \"Code\", \"varName\": \"code\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"config\": { \"mode\": \"python\", \"require\": [ \"ace/ext/language_tools\" ], \"advanced\": { \"enableSnippets\": true, \"enableBasicAutocompletion\": true, \"enableLiveAutocompletion\": true } }, \"id\": 5 }, { \"index\": 6, \"description\": \"\", \"component\": \"dbnTagsInput\", \"required\": false, \"editable\": false, \"cols\": 12, \"value\": [], \"label\": \"Tags\", \"varName\": \"tags\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 6 } ], \"outputTemplate\": { \"Type\": \"@type\", \"Tag\": \"@tags\", \"Distribution\": \"\", \"DistributionParameters\": {}, \"ModelName\": \"@modelName\", \"Parents\": [], \"Children\": [] } }, \"inputCount\": 3, \"outputCount\": 1, \"top\": \"600px\", \"left\": \"404px\" }, { \"id\": \"pt0\", \"data\": [ { \"id\": 0, \"label\": \"Type\", \"value\": \"Deterministic\", \"varName\": \"type\", \"validation\": \"\" }, { \"id\": 1, \"label\": \"Model Name\", \"value\": \"pt0\", \"varName\": \"modelName\", \"validation\": \"\" }, { \"id\": 2, \"label\": \"Model Type\", \"value\": \"Equation\", \"varName\": \"modelType\", \"validation\": \"\" }, { \"id\": 3, \"label\": \"Function Name\", \"value\": \"pt0\", \"varName\": \"functionName\", \"validation\": \"\" }, { \"id\": 4, \"label\": \"Model Form\", \"value\": \"ps0*(tt0/ts0)**(gama/(gama-1.0))\", \"varName\": \"modelForm\", \"validation\": \"\" }, { \"id\": 5, \"label\": \"Code\", \"value\": \"\", \"varName\": \"code\", \"config\": { \"mode\": \"python\", \"require\": [ \"ace/ext/language_tools\" ], \"advanced\": { \"enableSnippets\": true, \"enableBasicAutocompletion\": true, \"enableLiveAutocompletion\": true } }, \"validation\": \"\" }, { \"id\": 6, \"label\": \"Tags\", \"value\": [], \"varName\": \"tags\", \"validation\": \"\" } ], \"inputs\": [ { \"id\": \"pt0Input0\", \"label\": \"pt0Input0\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"pt0\", \"outputConnected\": null, \"outputIdConnected\": \"tt0.tt0Output0\", \"dataEditable\": false, \"filledData\": false }, { \"id\": \"pt0Input1\", \"label\": \"pt0Input1\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"pt0\", \"outputConnected\": null, \"outputIdConnected\": \"ps0.ps0Output0\", \"dataEditable\": false, \"filledData\": false }, { \"id\": \"pt0Input2\", \"label\": \"pt0Input2\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"pt0\", \"outputConnected\": null, \"outputIdConnected\": \"ts0.ts0Output0\", \"dataEditable\": false, \"filledData\": false }, { \"id\": \"pt0Input3\", \"label\": \"pt0Input3\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"pt0\", \"outputConnected\": null, \"outputIdConnected\": \"gama.gamaOutput0\", \"dataEditable\": false, \"filledData\": false } ], \"outputs\": [ { \"id\": \"pt0Output0\", \"label\": \"pt0Output0\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"pt0\", \"inputsConnected\": [], \"inputsIdConnected\": [] } ], \"nodeType\": { \"id\": \"deterministic\", \"name\": \"Deterministic\", \"formTemplate\": [ { \"index\": 0, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": false, \"cols\": 12, \"value\": \"Deterministic\", \"label\": \"Type\", \"varName\": \"type\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 0 }, { \"index\": 1, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Name\", \"varName\": \"modelName\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 1 }, { \"index\": 2, \"description\": \"\", \"component\": \"select\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Type\", \"varName\": \"modelType\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [ \"Equation\", \"InitialState\", \"PythonFunction\" ], \"id\": 2 }, { \"index\": 3, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Function Name\", \"varName\": \"functionName\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 3 }, { \"index\": 4, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Form\", \"varName\": \"modelForm\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 4 }, { \"index\": 5, \"description\": \"\", \"component\": \"codeEditor\", \"required\": false, \"editable\": true, \"cols\": 12, \"value\": \"\", \"label\": \"Code\", \"varName\": \"code\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"config\": { \"mode\": \"python\", \"require\": [ \"ace/ext/language_tools\" ], \"advanced\": { \"enableSnippets\": true, \"enableBasicAutocompletion\": true, \"enableLiveAutocompletion\": true } }, \"id\": 5 }, { \"index\": 6, \"description\": \"\", \"component\": \"dbnTagsInput\", \"required\": false, \"editable\": false, \"cols\": 12, \"value\": [], \"label\": \"Tags\", \"varName\": \"tags\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 6 } ], \"outputTemplate\": { \"Type\": \"@type\", \"Tag\": \"@tags\", \"Distribution\": \"\", \"DistributionParameters\": {}, \"ModelName\": \"@modelName\", \"Parents\": [], \"Children\": [] } }, \"inputCount\": 4, \"outputCount\": 1, \"top\": \"648px\", \"left\": \"630px\" }, { \"id\": \"ts0\", \"data\": [ { \"id\": 0, \"label\": \"Type\", \"value\": \"Deterministic\", \"varName\": \"type\", \"validation\": \"\" }, { \"id\": 1, \"label\": \"Model Name\", \"value\": \"ts0\", \"varName\": \"modelName\", \"validation\": \"\" }, { \"id\": 2, \"label\": \"Model Type\", \"value\": \"Equation\", \"varName\": \"modelType\", \"validation\": \"\" }, { \"id\": 3, \"label\": \"Function Name\", \"value\": \"ts0\", \"varName\": \"functionName\", \"validation\": \"\" }, { \"id\": 4, \"label\": \"Model Form\", \"value\": \"518.6-3.56*altitude/1000.0\", \"varName\": \"modelForm\", \"validation\": \"\" }, { \"id\": 5, \"label\": \"Code\", \"value\": \"\", \"varName\": \"code\", \"config\": { \"mode\": \"python\", \"require\": [ \"ace/ext/language_tools\" ], \"advanced\": { \"enableSnippets\": true, \"enableBasicAutocompletion\": true, \"enableLiveAutocompletion\": true } }, \"validation\": \"\" }, { \"id\": 6, \"label\": \"Tags\", \"value\": [], \"varName\": \"tags\", \"validation\": \"\" } ], \"inputs\": [ { \"id\": \"ts0Input0\", \"label\": \"ts0Input0\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"ts0\", \"outputConnected\": null, \"outputIdConnected\": \"altitude.altitudeOutput0\", \"dataEditable\": false, \"filledData\": false } ], \"outputs\": [ { \"id\": \"ts0Output0\", \"label\": \"ts0Output0\", \"fixedLabel\": false, \"node\": null, \"nodeId\": \"ts0\", \"inputsConnected\": [], \"inputsIdConnected\": [ \"ps0.ps0Input0\", \"a0.a0Input2\", \"tt0.tt0Input0\", \"pt0.pt0Input2\" ] } ], \"nodeType\": { \"id\": \"deterministic\", \"name\": \"Deterministic\", \"formTemplate\": [ { \"index\": 0, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": false, \"cols\": 12, \"value\": \"Deterministic\", \"label\": \"Type\", \"varName\": \"type\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 0 }, { \"index\": 1, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Name\", \"varName\": \"modelName\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 1 }, { \"index\": 2, \"description\": \"\", \"component\": \"select\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Type\", \"varName\": \"modelType\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [ \"Equation\", \"InitialState\", \"PythonFunction\" ], \"id\": 2 }, { \"index\": 3, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Function Name\", \"varName\": \"functionName\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 3 }, { \"index\": 4, \"description\": \"\", \"component\": \"textInput\", \"required\": true, \"editable\": true, \"cols\": 6, \"value\": \"\", \"label\": \"Model Form\", \"varName\": \"modelForm\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 4 }, { \"index\": 5, \"description\": \"\", \"component\": \"codeEditor\", \"required\": false, \"editable\": true, \"cols\": 12, \"value\": \"\", \"label\": \"Code\", \"varName\": \"code\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"config\": { \"mode\": \"python\", \"require\": [ \"ace/ext/language_tools\" ], \"advanced\": { \"enableSnippets\": true, \"enableBasicAutocompletion\": true, \"enableLiveAutocompletion\": true } }, \"id\": 5 }, { \"index\": 6, \"description\": \"\", \"component\": \"dbnTagsInput\", \"required\": false, \"editable\": false, \"cols\": 12, \"value\": [], \"label\": \"Tags\", \"varName\": \"tags\", \"validation\": \"\", \"placeholder\": \"\", \"options\": [], \"id\": 6 } ], \"outputTemplate\": { \"Type\": \"@type\", \"Tag\": \"@tags\", \"Distribution\": \"\", \"DistributionParameters\": {}, \"ModelName\": \"@modelName\", \"Parents\": [], \"Children\": [] } }, \"inputCount\": 1, \"outputCount\": 1, \"top\": \"242px\", \"left\": \"293px\" } ] } }, \"dataSources\": {}, \"dataSourcesIds\": {}, \"additionalFilesIds\": {}, \"dataSourcesInfo\": {}, \"inputs\": [], \"outputs\": [], \"headerMappings\": {}, \"workDir\": \"/tmp/\" }";
        //String testJson = "\"{ \\\"taskName\\\": \\\"build\\\", \\\"techniqueName\\\": \\\"dbn\\\", \\\"modelName\\\": \\\"Model_20190115153656\\\", \\\"analyticSettings\\\": { \\\"ObservationData\\\": {}, \\\"ExpertKnowledge\\\": {}, \\\"PriorData\\\": {}, \\\"StateVariableDefinition\\\": {}, \\\"Models\\\": { \\\"ps0\\\": { \\\"Input\\\": [ \\\"ts0\\\", \\\"altitude\\\" ], \\\"Type\\\": \\\"Equation\\\", \\\"FunctionName\\\": \\\"ps0\\\", \\\"ModelForm\\\": \\\"2116.0*(ts0/518.6)**5.256\\\", \\\"Output\\\": [ \\\"pt0\\\" ] }, \\\"a0\\\": { \\\"Input\\\": [ \\\"gama\\\", \\\"Rgas\\\", \\\"ts0\\\" ], \\\"Type\\\": \\\"Equation\\\", \\\"FunctionName\\\": \\\"a0\\\", \\\"ModelForm\\\": \\\"(gama*Rgas*ts0)**0.5\\\", \\\"Output\\\": [ \\\"mach\\\" ] }, \\\"mach\\\": { \\\"Input\\\": [ \\\"a0\\\", \\\"speed\\\" ], \\\"Type\\\": \\\"Equation\\\", \\\"FunctionName\\\": \\\"mach\\\", \\\"ModelForm\\\": \\\"(speed/5280.0*3600.0)/a0\\\", \\\"Output\\\": [ \\\"tt0\\\" ] }, \\\"tt0\\\": { \\\"Input\\\": [ \\\"ts0\\\", \\\"gama\\\", \\\"mach\\\" ], \\\"Type\\\": \\\"Equation\\\", \\\"FunctionName\\\": \\\"tt0\\\", \\\"ModelForm\\\": \\\"ts0*(1.0 + 0.5*(gama-1.0)*mach*mach)\\\", \\\"Output\\\": [ \\\"pt0\\\" ] }, \\\"pt0\\\": { \\\"Input\\\": [ \\\"tt0\\\", \\\"ps0\\\", \\\"ts0\\\", \\\"gama\\\" ], \\\"Type\\\": \\\"Equation\\\", \\\"FunctionName\\\": \\\"pt0\\\", \\\"ModelForm\\\": \\\"ps0*(tt0/ts0)**(gama/(gama-1.0))\\\", \\\"Output\\\": [] }, \\\"ts0\\\": { \\\"Input\\\": [ \\\"altitude\\\" ], \\\"Type\\\": \\\"Equation\\\", \\\"FunctionName\\\": \\\"ts0\\\", \\\"ModelForm\\\": \\\"518.6-3.56*altitude/1000.0\\\", \\\"Output\\\": [ \\\"ps0\\\", \\\"a0\\\", \\\"tt0\\\", \\\"pt0\\\" ] } }, \\\"Nodes\\\": { \\\"altitude\\\": { \\\"Type\\\": \\\"Stochastic_Transient\\\", \\\"Tag\\\": [], \\\"Distribution\\\": \\\"Uniform\\\", \\\"DistributionParameters\\\": { \\\"lower\\\": \\\"0\\\", \\\"upper\\\": \\\"100000\\\" }, \\\"InitialChildren\\\": [], \\\"Children\\\": [ \\\"ps0\\\", \\\"ts0\\\" ], \\\"ObservationData\\\":[10000.0], \\\"Parents\\\": [], \\\"IsDistributionFixed\\\": true, \\\"Range\\\": [ \\\"0\\\", \\\"100000\\\" ] }, \\\"ps0\\\": { \\\"Type\\\": \\\"Deterministic\\\", \\\"Tag\\\": [], \\\"Distribution\\\": \\\"\\\", \\\"DistributionParameters\\\": {}, \\\"ModelName\\\": \\\"ps0\\\", \\\"Parents\\\": [ \\\"ts0\\\", \\\"altitude\\\" ], \\\"Children\\\": [ \\\"pt0\\\" ] }, \\\"gama\\\": { \\\"Type\\\": \\\"Constant\\\", \\\"Tag\\\": [], \\\"Distribution\\\": \\\"\\\", \\\"DistributionParameters\\\": {}, \\\"Value\\\": [ \\\"1.4\\\" ], \\\"Parents\\\": [], \\\"Children\\\": [ \\\"a0\\\", \\\"tt0\\\", \\\"pt0\\\" ] }, \\\"Rgas\\\": { \\\"Type\\\": \\\"Constant\\\", \\\"Tag\\\": [], \\\"Distribution\\\": \\\"\\\", \\\"DistributionParameters\\\": {}, \\\"Value\\\": [ \\\"1718.0\\\" ], \\\"Parents\\\": [], \\\"Children\\\": [ \\\"a0\\\" ] }, \\\"a0\\\": { \\\"Type\\\": \\\"Deterministic\\\", \\\"Tag\\\": [], \\\"Distribution\\\": \\\"\\\", \\\"DistributionParameters\\\": {}, \\\"ModelName\\\": \\\"a0\\\", \\\"Parents\\\": [ \\\"gama\\\", \\\"Rgas\\\", \\\"ts0\\\" ], \\\"Children\\\": [ \\\"mach\\\" ] }, \\\"speed\\\": { \\\"Type\\\": \\\"Stochastic_Transient\\\", \\\"Tag\\\": [], \\\"Distribution\\\": \\\"Uniform\\\", \\\"DistributionParameters\\\": { \\\"lower\\\": \\\"0\\\", \\\"upper\\\": \\\"1000\\\" }, \\\"InitialChildren\\\": [], \\\"Children\\\": [ \\\"mach\\\" ], \\\"ObservationData\\\":[], \\\"Parents\\\": [], \\\"IsDistributionFixed\\\": true, \\\"Range\\\": [ \\\"0\\\", \\\"1000\\\" ] }, \\\"mach\\\": { \\\"Type\\\": \\\"Deterministic\\\", \\\"Tag\\\": [], \\\"Distribution\\\": \\\"\\\", \\\"DistributionParameters\\\": {}, \\\"ModelName\\\": \\\"mach\\\", \\\"Parents\\\": [ \\\"a0\\\", \\\"speed\\\" ], \\\"Children\\\": [ \\\"tt0\\\" ] }, \\\"tt0\\\": { \\\"Type\\\": \\\"Deterministic\\\", \\\"Tag\\\": [], \\\"Distribution\\\": \\\"\\\", \\\"DistributionParameters\\\": {}, \\\"ModelName\\\": \\\"tt0\\\", \\\"Parents\\\": [ \\\"ts0\\\", \\\"gama\\\", \\\"mach\\\" ], \\\"Children\\\": [ \\\"pt0\\\" ] }, \\\"pt0\\\": { \\\"Type\\\": \\\"Deterministic\\\", \\\"Tag\\\": [], \\\"Distribution\\\": \\\"\\\", \\\"DistributionParameters\\\": {}, \\\"ModelName\\\": \\\"pt0\\\", \\\"Parents\\\": [ \\\"tt0\\\", \\\"ps0\\\", \\\"ts0\\\", \\\"gama\\\" ], \\\"Children\\\": [] }, \\\"ts0\\\": { \\\"Type\\\": \\\"Deterministic\\\", \\\"Tag\\\": [], \\\"Distribution\\\": \\\"\\\", \\\"DistributionParameters\\\": {}, \\\"ModelName\\\": \\\"ts0\\\", \\\"Parents\\\": [ \\\"altitude\\\" ], \\\"Children\\\": [ \\\"ps0\\\", \\\"a0\\\", \\\"tt0\\\", \\\"pt0\\\" ] } }, \\\"DBNSetup\\\": { \\\"WorkDir\\\": \\\"C:/Users/200019210/Documents/2019/Projects/ASKE-TA2/Initial-DBN-try-freestream/\\\", \\\"ParticleFilterOptions\\\": { \\\"NodeNamesNotRecorded\\\": [], \\\"BackendKeepVectors\\\": true, \\\"BackendFname\\\": \\\"C:/Users/200019210/Documents/2019/Projects/ASKE-TA2/Initial-DBN-try-freestream/Results\\\", \\\"BackendKeepScalars\\\": true, \\\"ParallelProcesses\\\": \\\"5\\\", \\\"ResampleThreshold\\\": 0.4, \\\"Parallel\\\": true, \\\"Backend\\\": \\\"RAM\\\" }, \\\"NumberOfSamples\\\": 500, \\\"PlotFlag\\\": false, \\\"TrackingTimeSteps\\\": \\\"1\\\", \\\"TaskName\\\": \\\"Prognosis\\\" }, \\\"UT_node_init_data\\\": { \\\"UT_node_names\\\": [] }, \\\"InspectionSchedule\\\": {}, \\\"riskRollup\\\": {}, \\\"maintenanceLimits\\\": [], \\\"observationDataSources\\\": {}, \\\"fullNetwork\\\": { \\\"nodes\\\": [ { \\\"id\\\": \\\"altitude\\\", \\\"data\\\": [ { \\\"id\\\": 0, \\\"label\\\": \\\"Type\\\", \\\"value\\\": \\\"Stochastic\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 1, \\\"label\\\": \\\"Distribution\\\", \\\"value\\\": \\\"Uniform\\\", \\\"varName\\\": \\\"distribution\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 2, \\\"label\\\": \\\"Is distribution fixed?\\\", \\\"value\\\": \\\"Yes\\\", \\\"varName\\\": \\\"isDistributionFixed\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 3, \\\"label\\\": \\\"Lower\\\", \\\"value\\\": \\\"0\\\", \\\"varName\\\": \\\"lower\\\", \\\"validation\\\": \\\"^[0-9]+$\\\" }, { \\\"id\\\": 4, \\\"label\\\": \\\"Upper\\\", \\\"value\\\": \\\"100000\\\", \\\"varName\\\": \\\"upper\\\", \\\"validation\\\": \\\"^[0-9]+$\\\" }, { \\\"id\\\": 5, \\\"label\\\": \\\"Observation Data\\\", \\\"value\\\": { \\\"items\\\": [], \\\"columns\\\": [], \\\"filter\\\": { \\\"template\\\": [], \\\"data\\\": [] }, \\\"dataSources\\\": [], \\\"columnsCount\\\": 0 }, \\\"varName\\\": \\\"observationData\\\", \\\"config\\\": { \\\"dataSource\\\": \\\"dataSource\\\", \\\"numeric\\\": true }, \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 6, \\\"label\\\": \\\"Range\\\", \\\"value\\\": [ \\\"0\\\", \\\"100000\\\" ], \\\"varName\\\": \\\"range\\\", \\\"config\\\": { \\\"fixedLength\\\": 2 }, \\\"validation\\\": \\\"^[0-9]+$\\\" }, { \\\"id\\\": 7, \\\"label\\\": \\\"Tags\\\", \\\"value\\\": [], \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\" } ], \\\"inputs\\\": [ { \\\"id\\\": \\\"altitudeInput0\\\", \\\"label\\\": \\\"lower\\\", \\\"fixedLabel\\\": true, \\\"node\\\": null, \\\"nodeId\\\": \\\"altitude\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": null, \\\"dataEditable\\\": true, \\\"filledData\\\": false }, { \\\"id\\\": \\\"altitudeInput1\\\", \\\"label\\\": \\\"upper\\\", \\\"fixedLabel\\\": true, \\\"node\\\": null, \\\"nodeId\\\": \\\"altitude\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": null, \\\"dataEditable\\\": true, \\\"filledData\\\": false } ], \\\"outputs\\\": [ { \\\"id\\\": \\\"altitudeOutput0\\\", \\\"label\\\": \\\"output\\\", \\\"fixedLabel\\\": true, \\\"node\\\": null, \\\"nodeId\\\": \\\"altitude\\\", \\\"inputsConnected\\\": [], \\\"inputsIdConnected\\\": [ \\\"ps0.ps0Input1\\\", \\\"ts0.ts0Input0\\\" ] } ], \\\"nodeType\\\": { \\\"id\\\": \\\"stochastic-uniform\\\", \\\"name\\\": \\\"Stochastic - Uniform\\\", \\\"inputCount\\\": 2, \\\"outputCount\\\": 1, \\\"inputLabels\\\": [ \\\"lower\\\", \\\"upper\\\" ], \\\"outputLabels\\\": [ \\\"output\\\" ], \\\"formTemplate\\\": [ { \\\"index\\\": 0, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": \\\"Stochastic\\\", \\\"label\\\": \\\"Type\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] }, { \\\"index\\\": 1, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": \\\"Uniform\\\", \\\"label\\\": \\\"Distribution\\\", \\\"varName\\\": \\\"distribution\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] }, { \\\"index\\\": 2, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"radio\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 12, \\\"value\\\": \\\"Yes\\\", \\\"label\\\": \\\"Is distribution fixed?\\\", \\\"varName\\\": \\\"isDistributionFixed\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [ \\\"Yes\\\", \\\"No\\\" ], \\\"finalValue\\\": { \\\"Yes\\\": true, \\\"No\\\": false } }, { \\\"index\\\": 3, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Lower\\\", \\\"varName\\\": \\\"lower\\\", \\\"validation\\\": \\\"^[0-9]+$\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] }, { \\\"index\\\": 4, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Upper\\\", \\\"varName\\\": \\\"upper\\\", \\\"validation\\\": \\\"^[0-9]+$\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] }, { \\\"index\\\": 5, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"tableInput\\\", \\\"required\\\": false, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": { \\\"items\\\": [], \\\"columns\\\": [] }, \\\"label\\\": \\\"Observation Data\\\", \\\"varName\\\": \\\"observationData\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"config\\\": { \\\"dataSource\\\": \\\"dataSource\\\", \\\"numeric\\\": true } }, { \\\"index\\\": 6, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"smallArrayInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": [ null, null ], \\\"label\\\": \\\"Range\\\", \\\"varName\\\": \\\"range\\\", \\\"config\\\": { \\\"fixedLength\\\": 2 }, \\\"validation\\\": \\\"^[0-9]+$\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] }, { \\\"index\\\": 7, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"dbnTagsInput\\\", \\\"required\\\": false, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": [], \\\"label\\\": \\\"Tags\\\", \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] } ], \\\"outputTemplate\\\": { \\\"Type\\\": \\\"@type\\\", \\\"Tag\\\": \\\"@tags\\\", \\\"Distribution\\\": \\\"@distribution\\\", \\\"DistributionParameters\\\": { \\\"lower\\\": \\\"@lower\\\", \\\"upper\\\": \\\"@upper\\\" }, \\\"InitialChildren\\\": [], \\\"Children\\\": [], \\\"Parents\\\": [], \\\"ObservationData\\\": \\\"@?observationData.items\\\", \\\"FullObservationData\\\": \\\"@?observationData.fullData\\\", \\\"IsDistributionFixed\\\": \\\"@isDistributionFixed\\\", \\\"Range\\\": \\\"@?range\\\" }, \\\"upgradeValues\\\": [ { \\\"varName\\\": \\\"observationData\\\", \\\"oldValueType\\\": \\\"array\\\", \\\"newValueTemplate\\\": { \\\"items\\\": \\\"@oldValue\\\", \\\"columns\\\": [] } } ] }, \\\"inputCount\\\": 2, \\\"inputCountFixed\\\": true, \\\"outputCount\\\": 1, \\\"outputCountFixed\\\": true, \\\"top\\\": \\\"150px\\\", \\\"left\\\": \\\"457px\\\" }, { \\\"id\\\": \\\"ps0\\\", \\\"data\\\": [ { \\\"id\\\": 0, \\\"label\\\": \\\"Type\\\", \\\"value\\\": \\\"Deterministic\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 1, \\\"label\\\": \\\"Model Name\\\", \\\"value\\\": \\\"ps0\\\", \\\"varName\\\": \\\"modelName\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 2, \\\"label\\\": \\\"Model Type\\\", \\\"value\\\": \\\"Equation\\\", \\\"varName\\\": \\\"modelType\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 3, \\\"label\\\": \\\"Function Name\\\", \\\"value\\\": \\\"ps0\\\", \\\"varName\\\": \\\"functionName\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 4, \\\"label\\\": \\\"Model Form\\\", \\\"value\\\": \\\"2116.0*(ts0/518.6)**5.256\\\", \\\"varName\\\": \\\"modelForm\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 5, \\\"label\\\": \\\"Code\\\", \\\"value\\\": \\\"\\\", \\\"varName\\\": \\\"code\\\", \\\"config\\\": { \\\"mode\\\": \\\"python\\\", \\\"require\\\": [ \\\"ace/ext/language_tools\\\" ], \\\"advanced\\\": { \\\"enableSnippets\\\": true, \\\"enableBasicAutocompletion\\\": true, \\\"enableLiveAutocompletion\\\": true } }, \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 6, \\\"label\\\": \\\"Tags\\\", \\\"value\\\": [], \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\" } ], \\\"inputs\\\": [ { \\\"id\\\": \\\"ps0Input0\\\", \\\"label\\\": \\\"ps0Input0\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"ps0\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": \\\"ts0.ts0Output0\\\", \\\"dataEditable\\\": false, \\\"filledData\\\": false }, { \\\"id\\\": \\\"ps0Input1\\\", \\\"label\\\": \\\"ps0Input1\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"ps0\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": \\\"altitude.altitudeOutput0\\\", \\\"dataEditable\\\": false, \\\"filledData\\\": false } ], \\\"outputs\\\": [ { \\\"id\\\": \\\"ps0Output0\\\", \\\"label\\\": \\\"ps0Output0\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"ps0\\\", \\\"inputsConnected\\\": [], \\\"inputsIdConnected\\\": [ \\\"pt0.pt0Input1\\\" ] } ], \\\"nodeType\\\": { \\\"id\\\": \\\"deterministic\\\", \\\"name\\\": \\\"Deterministic\\\", \\\"formTemplate\\\": [ { \\\"index\\\": 0, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": \\\"Deterministic\\\", \\\"label\\\": \\\"Type\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 0 }, { \\\"index\\\": 1, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Name\\\", \\\"varName\\\": \\\"modelName\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 1 }, { \\\"index\\\": 2, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"select\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Type\\\", \\\"varName\\\": \\\"modelType\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [ \\\"Equation\\\", \\\"InitialState\\\", \\\"PythonFunction\\\" ], \\\"id\\\": 2 }, { \\\"index\\\": 3, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Function Name\\\", \\\"varName\\\": \\\"functionName\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 3 }, { \\\"index\\\": 4, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Form\\\", \\\"varName\\\": \\\"modelForm\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 4 }, { \\\"index\\\": 5, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"codeEditor\\\", \\\"required\\\": false, \\\"editable\\\": true, \\\"cols\\\": 12, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Code\\\", \\\"varName\\\": \\\"code\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"config\\\": { \\\"mode\\\": \\\"python\\\", \\\"require\\\": [ \\\"ace/ext/language_tools\\\" ], \\\"advanced\\\": { \\\"enableSnippets\\\": true, \\\"enableBasicAutocompletion\\\": true, \\\"enableLiveAutocompletion\\\": true } }, \\\"id\\\": 5 }, { \\\"index\\\": 6, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"dbnTagsInput\\\", \\\"required\\\": false, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": [], \\\"label\\\": \\\"Tags\\\", \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 6 } ], \\\"outputTemplate\\\": { \\\"Type\\\": \\\"@type\\\", \\\"Tag\\\": \\\"@tags\\\", \\\"Distribution\\\": \\\"\\\", \\\"DistributionParameters\\\": {}, \\\"ModelName\\\": \\\"@modelName\\\", \\\"Parents\\\": [], \\\"Children\\\": [] } }, \\\"inputCount\\\": 2, \\\"outputCount\\\": 1, \\\"top\\\": \\\"325px\\\", \\\"left\\\": \\\"577px\\\" }, { \\\"id\\\": \\\"gama\\\", \\\"data\\\": [ { \\\"id\\\": 0, \\\"label\\\": \\\"Type\\\", \\\"value\\\": \\\"Constant\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 1, \\\"label\\\": \\\"Value\\\", \\\"value\\\": [ \\\"1.4\\\" ], \\\"varName\\\": \\\"value\\\", \\\"config\\\": { \\\"minLength\\\": 1 }, \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 2, \\\"label\\\": \\\"Initial Children\\\", \\\"value\\\": [ null ], \\\"varName\\\": \\\"initialChildren\\\", \\\"config\\\": { \\\"minLength\\\": 1 }, \\\"validation\\\": \\\"^[0-9]+$\\\" }, { \\\"id\\\": 3, \\\"label\\\": \\\"Tags\\\", \\\"value\\\": [], \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\" } ], \\\"inputs\\\": [], \\\"outputs\\\": [ { \\\"id\\\": \\\"gamaOutput0\\\", \\\"label\\\": \\\"gamaOutput0\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"gama\\\", \\\"inputsConnected\\\": [], \\\"inputsIdConnected\\\": [ \\\"a0.a0Input0\\\", \\\"tt0.tt0Input1\\\", \\\"pt0.pt0Input3\\\" ] } ], \\\"nodeType\\\": { \\\"id\\\": \\\"constant\\\", \\\"name\\\": \\\"Constant\\\", \\\"class\\\": \\\"circle\\\", \\\"inputCount\\\": 0, \\\"outputCount\\\": 1, \\\"formTemplate\\\": [ { \\\"index\\\": 0, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": \\\"Constant\\\", \\\"label\\\": \\\"Type\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] }, { \\\"index\\\": 1, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"smallArrayInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 12, \\\"value\\\": [ null ], \\\"label\\\": \\\"Value\\\", \\\"varName\\\": \\\"value\\\", \\\"config\\\": { \\\"minLength\\\": 1 }, \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] }, { \\\"index\\\": 2, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"smallArrayInput\\\", \\\"required\\\": false, \\\"editable\\\": true, \\\"cols\\\": 12, \\\"value\\\": [ null ], \\\"label\\\": \\\"Initial Children\\\", \\\"varName\\\": \\\"initialChildren\\\", \\\"config\\\": { \\\"minLength\\\": 1 }, \\\"validation\\\": \\\"^[0-9]+$\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] }, { \\\"index\\\": 3, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"dbnTagsInput\\\", \\\"required\\\": false, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": [], \\\"label\\\": \\\"Tags\\\", \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] } ], \\\"outputTemplate\\\": { \\\"Type\\\": \\\"@type\\\", \\\"Tag\\\": \\\"@tags\\\", \\\"Distribution\\\": \\\"\\\", \\\"DistributionParameters\\\": {}, \\\"Value\\\": \\\"@value\\\", \\\"Parents\\\": [], \\\"Children\\\": [], \\\"InitialChildren\\\": \\\"@?initialChildren\\\" }, \\\"upgradeValues\\\": [ { \\\"varName\\\": \\\"value\\\", \\\"oldValueType\\\": \\\"primitive\\\", \\\"newValueTemplate\\\": [ \\\"@oldValue\\\" ] } ] }, \\\"inputCount\\\": 0, \\\"inputCountFixed\\\": true, \\\"outputCount\\\": 1, \\\"outputCountFixed\\\": true, \\\"top\\\": \\\"335px\\\", \\\"left\\\": \\\"474px\\\" }, { \\\"id\\\": \\\"Rgas\\\", \\\"data\\\": [ { \\\"id\\\": 0, \\\"label\\\": \\\"Type\\\", \\\"value\\\": \\\"Constant\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 1, \\\"label\\\": \\\"Value\\\", \\\"value\\\": [ \\\"1718.0\\\" ], \\\"varName\\\": \\\"value\\\", \\\"config\\\": { \\\"minLength\\\": 1 }, \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 2, \\\"label\\\": \\\"Initial Children\\\", \\\"value\\\": [ null ], \\\"varName\\\": \\\"initialChildren\\\", \\\"config\\\": { \\\"minLength\\\": 1 }, \\\"validation\\\": \\\"^[0-9]+$\\\" }, { \\\"id\\\": 3, \\\"label\\\": \\\"Tags\\\", \\\"value\\\": [], \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\" } ], \\\"inputs\\\": [], \\\"outputs\\\": [ { \\\"id\\\": \\\"RgasOutput0\\\", \\\"label\\\": \\\"RgasOutput0\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"Rgas\\\", \\\"inputsConnected\\\": [], \\\"inputsIdConnected\\\": [ \\\"a0.a0Input1\\\" ] } ], \\\"nodeType\\\": { \\\"id\\\": \\\"constant\\\", \\\"name\\\": \\\"Constant\\\", \\\"class\\\": \\\"circle\\\", \\\"inputCount\\\": 0, \\\"outputCount\\\": 1, \\\"formTemplate\\\": [ { \\\"index\\\": 0, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": \\\"Constant\\\", \\\"label\\\": \\\"Type\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] }, { \\\"index\\\": 1, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"smallArrayInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 12, \\\"value\\\": [ null ], \\\"label\\\": \\\"Value\\\", \\\"varName\\\": \\\"value\\\", \\\"config\\\": { \\\"minLength\\\": 1 }, \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] }, { \\\"index\\\": 2, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"smallArrayInput\\\", \\\"required\\\": false, \\\"editable\\\": true, \\\"cols\\\": 12, \\\"value\\\": [ null ], \\\"label\\\": \\\"Initial Children\\\", \\\"varName\\\": \\\"initialChildren\\\", \\\"config\\\": { \\\"minLength\\\": 1 }, \\\"validation\\\": \\\"^[0-9]+$\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] }, { \\\"index\\\": 3, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"dbnTagsInput\\\", \\\"required\\\": false, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": [], \\\"label\\\": \\\"Tags\\\", \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] } ], \\\"outputTemplate\\\": { \\\"Type\\\": \\\"@type\\\", \\\"Tag\\\": \\\"@tags\\\", \\\"Distribution\\\": \\\"\\\", \\\"DistributionParameters\\\": {}, \\\"Value\\\": \\\"@value\\\", \\\"Parents\\\": [], \\\"Children\\\": [], \\\"InitialChildren\\\": \\\"@?initialChildren\\\" }, \\\"upgradeValues\\\": [ { \\\"varName\\\": \\\"value\\\", \\\"oldValueType\\\": \\\"primitive\\\", \\\"newValueTemplate\\\": [ \\\"@oldValue\\\" ] } ] }, \\\"inputCount\\\": 0, \\\"inputCountFixed\\\": true, \\\"outputCount\\\": 1, \\\"outputCountFixed\\\": true, \\\"top\\\": \\\"311px\\\", \\\"left\\\": \\\"161px\\\" }, { \\\"id\\\": \\\"a0\\\", \\\"data\\\": [ { \\\"id\\\": 0, \\\"label\\\": \\\"Type\\\", \\\"value\\\": \\\"Deterministic\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 1, \\\"label\\\": \\\"Model Name\\\", \\\"value\\\": \\\"a0\\\", \\\"varName\\\": \\\"modelName\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 2, \\\"label\\\": \\\"Model Type\\\", \\\"value\\\": \\\"Equation\\\", \\\"varName\\\": \\\"modelType\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 3, \\\"label\\\": \\\"Function Name\\\", \\\"value\\\": \\\"a0\\\", \\\"varName\\\": \\\"functionName\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 4, \\\"label\\\": \\\"Model Form\\\", \\\"value\\\": \\\"(gama*Rgas*ts0)**0.5\\\", \\\"varName\\\": \\\"modelForm\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 5, \\\"label\\\": \\\"Code\\\", \\\"value\\\": \\\"\\\", \\\"varName\\\": \\\"code\\\", \\\"config\\\": { \\\"mode\\\": \\\"python\\\", \\\"require\\\": [ \\\"ace/ext/language_tools\\\" ], \\\"advanced\\\": { \\\"enableSnippets\\\": true, \\\"enableBasicAutocompletion\\\": true, \\\"enableLiveAutocompletion\\\": true } }, \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 6, \\\"label\\\": \\\"Tags\\\", \\\"value\\\": [], \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\" } ], \\\"inputs\\\": [ { \\\"id\\\": \\\"a0Input0\\\", \\\"label\\\": \\\"a0Input0\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"a0\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": \\\"gama.gamaOutput0\\\", \\\"dataEditable\\\": false, \\\"filledData\\\": false }, { \\\"id\\\": \\\"a0Input1\\\", \\\"label\\\": \\\"a0Input1\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"a0\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": \\\"Rgas.RgasOutput0\\\", \\\"dataEditable\\\": false, \\\"filledData\\\": false }, { \\\"id\\\": \\\"a0Input2\\\", \\\"label\\\": \\\"a0Input2\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"a0\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": \\\"ts0.ts0Output0\\\", \\\"dataEditable\\\": false, \\\"filledData\\\": false } ], \\\"outputs\\\": [ { \\\"id\\\": \\\"a0Output0\\\", \\\"label\\\": \\\"a0Output0\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"a0\\\", \\\"inputsConnected\\\": [], \\\"inputsIdConnected\\\": [ \\\"mach.machInput0\\\" ] } ], \\\"nodeType\\\": { \\\"id\\\": \\\"deterministic\\\", \\\"name\\\": \\\"Deterministic\\\", \\\"formTemplate\\\": [ { \\\"index\\\": 0, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": \\\"Deterministic\\\", \\\"label\\\": \\\"Type\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 0 }, { \\\"index\\\": 1, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Name\\\", \\\"varName\\\": \\\"modelName\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 1 }, { \\\"index\\\": 2, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"select\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Type\\\", \\\"varName\\\": \\\"modelType\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [ \\\"Equation\\\", \\\"InitialState\\\", \\\"PythonFunction\\\" ], \\\"id\\\": 2 }, { \\\"index\\\": 3, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Function Name\\\", \\\"varName\\\": \\\"functionName\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 3 }, { \\\"index\\\": 4, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Form\\\", \\\"varName\\\": \\\"modelForm\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 4 }, { \\\"index\\\": 5, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"codeEditor\\\", \\\"required\\\": false, \\\"editable\\\": true, \\\"cols\\\": 12, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Code\\\", \\\"varName\\\": \\\"code\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"config\\\": { \\\"mode\\\": \\\"python\\\", \\\"require\\\": [ \\\"ace/ext/language_tools\\\" ], \\\"advanced\\\": { \\\"enableSnippets\\\": true, \\\"enableBasicAutocompletion\\\": true, \\\"enableLiveAutocompletion\\\": true } }, \\\"id\\\": 5 }, { \\\"index\\\": 6, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"dbnTagsInput\\\", \\\"required\\\": false, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": [], \\\"label\\\": \\\"Tags\\\", \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 6 } ], \\\"outputTemplate\\\": { \\\"Type\\\": \\\"@type\\\", \\\"Tag\\\": \\\"@tags\\\", \\\"Distribution\\\": \\\"\\\", \\\"DistributionParameters\\\": {}, \\\"ModelName\\\": \\\"@modelName\\\", \\\"Parents\\\": [], \\\"Children\\\": [] } }, \\\"inputCount\\\": 3, \\\"outputCount\\\": 1, \\\"top\\\": \\\"446px\\\", \\\"left\\\": \\\"305px\\\" }, { \\\"id\\\": \\\"speed\\\", \\\"data\\\": [ { \\\"id\\\": 0, \\\"label\\\": \\\"Type\\\", \\\"value\\\": \\\"Stochastic\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 1, \\\"label\\\": \\\"Distribution\\\", \\\"value\\\": \\\"Uniform\\\", \\\"varName\\\": \\\"distribution\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 2, \\\"label\\\": \\\"Is distribution fixed?\\\", \\\"value\\\": \\\"Yes\\\", \\\"varName\\\": \\\"isDistributionFixed\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 3, \\\"label\\\": \\\"Lower\\\", \\\"value\\\": \\\"0\\\", \\\"varName\\\": \\\"lower\\\", \\\"validation\\\": \\\"^[0-9]+$\\\" }, { \\\"id\\\": 4, \\\"label\\\": \\\"Upper\\\", \\\"value\\\": \\\"1000\\\", \\\"varName\\\": \\\"upper\\\", \\\"validation\\\": \\\"^[0-9]+$\\\" }, { \\\"id\\\": 5, \\\"label\\\": \\\"Observation Data\\\", \\\"value\\\": { \\\"items\\\": [], \\\"columns\\\": [], \\\"filter\\\": { \\\"template\\\": [], \\\"data\\\": [] }, \\\"dataSources\\\": [], \\\"columnsCount\\\": 0 }, \\\"varName\\\": \\\"observationData\\\", \\\"config\\\": { \\\"dataSource\\\": \\\"dataSource\\\", \\\"numeric\\\": true }, \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 6, \\\"label\\\": \\\"Range\\\", \\\"value\\\": [ \\\"0\\\", \\\"1000\\\" ], \\\"varName\\\": \\\"range\\\", \\\"config\\\": { \\\"fixedLength\\\": 2 }, \\\"validation\\\": \\\"^[0-9]+$\\\" }, { \\\"id\\\": 7, \\\"label\\\": \\\"Tags\\\", \\\"value\\\": [], \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\" } ], \\\"inputs\\\": [ { \\\"id\\\": \\\"speedInput0\\\", \\\"label\\\": \\\"lower\\\", \\\"fixedLabel\\\": true, \\\"node\\\": null, \\\"nodeId\\\": \\\"speed\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": null, \\\"dataEditable\\\": true, \\\"filledData\\\": false }, { \\\"id\\\": \\\"speedInput1\\\", \\\"label\\\": \\\"upper\\\", \\\"fixedLabel\\\": true, \\\"node\\\": null, \\\"nodeId\\\": \\\"speed\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": null, \\\"dataEditable\\\": true, \\\"filledData\\\": false } ], \\\"outputs\\\": [ { \\\"id\\\": \\\"speedOutput0\\\", \\\"label\\\": \\\"output\\\", \\\"fixedLabel\\\": true, \\\"node\\\": null, \\\"nodeId\\\": \\\"speed\\\", \\\"inputsConnected\\\": [], \\\"inputsIdConnected\\\": [ \\\"mach.machInput1\\\" ] } ], \\\"nodeType\\\": { \\\"id\\\": \\\"stochastic-uniform\\\", \\\"name\\\": \\\"Stochastic - Uniform\\\", \\\"inputCount\\\": 2, \\\"outputCount\\\": 1, \\\"inputLabels\\\": [ \\\"lower\\\", \\\"upper\\\" ], \\\"outputLabels\\\": [ \\\"output\\\" ], \\\"formTemplate\\\": [ { \\\"index\\\": 0, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": \\\"Stochastic\\\", \\\"label\\\": \\\"Type\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] }, { \\\"index\\\": 1, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": \\\"Uniform\\\", \\\"label\\\": \\\"Distribution\\\", \\\"varName\\\": \\\"distribution\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] }, { \\\"index\\\": 2, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"radio\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 12, \\\"value\\\": \\\"Yes\\\", \\\"label\\\": \\\"Is distribution fixed?\\\", \\\"varName\\\": \\\"isDistributionFixed\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [ \\\"Yes\\\", \\\"No\\\" ], \\\"finalValue\\\": { \\\"Yes\\\": true, \\\"No\\\": false } }, { \\\"index\\\": 3, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Lower\\\", \\\"varName\\\": \\\"lower\\\", \\\"validation\\\": \\\"^[0-9]+$\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] }, { \\\"index\\\": 4, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Upper\\\", \\\"varName\\\": \\\"upper\\\", \\\"validation\\\": \\\"^[0-9]+$\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] }, { \\\"index\\\": 5, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"tableInput\\\", \\\"required\\\": false, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": { \\\"items\\\": [], \\\"columns\\\": [] }, \\\"label\\\": \\\"Observation Data\\\", \\\"varName\\\": \\\"observationData\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"config\\\": { \\\"dataSource\\\": \\\"dataSource\\\", \\\"numeric\\\": true } }, { \\\"index\\\": 6, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"smallArrayInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": [ null, null ], \\\"label\\\": \\\"Range\\\", \\\"varName\\\": \\\"range\\\", \\\"config\\\": { \\\"fixedLength\\\": 2 }, \\\"validation\\\": \\\"^[0-9]+$\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] }, { \\\"index\\\": 7, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"dbnTagsInput\\\", \\\"required\\\": false, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": [], \\\"label\\\": \\\"Tags\\\", \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [] } ], \\\"outputTemplate\\\": { \\\"Type\\\": \\\"@type\\\", \\\"Tag\\\": \\\"@tags\\\", \\\"Distribution\\\": \\\"@distribution\\\", \\\"DistributionParameters\\\": { \\\"lower\\\": \\\"@lower\\\", \\\"upper\\\": \\\"@upper\\\" }, \\\"InitialChildren\\\": [], \\\"Children\\\": [], \\\"Parents\\\": [], \\\"ObservationData\\\": \\\"@?observationData.items\\\", \\\"FullObservationData\\\": \\\"@?observationData.fullData\\\", \\\"IsDistributionFixed\\\": \\\"@isDistributionFixed\\\", \\\"Range\\\": \\\"@?range\\\" }, \\\"upgradeValues\\\": [ { \\\"varName\\\": \\\"observationData\\\", \\\"oldValueType\\\": \\\"array\\\", \\\"newValueTemplate\\\": { \\\"items\\\": \\\"@oldValue\\\", \\\"columns\\\": [] } } ] }, \\\"inputCount\\\": 2, \\\"inputCountFixed\\\": true, \\\"outputCount\\\": 1, \\\"outputCountFixed\\\": true, \\\"top\\\": \\\"166px\\\", \\\"left\\\": \\\"740px\\\" }, { \\\"id\\\": \\\"mach\\\", \\\"data\\\": [ { \\\"id\\\": 0, \\\"label\\\": \\\"Type\\\", \\\"value\\\": \\\"Deterministic\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 1, \\\"label\\\": \\\"Model Name\\\", \\\"value\\\": \\\"mach\\\", \\\"varName\\\": \\\"modelName\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 2, \\\"label\\\": \\\"Model Type\\\", \\\"value\\\": \\\"Equation\\\", \\\"varName\\\": \\\"modelType\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 3, \\\"label\\\": \\\"Function Name\\\", \\\"value\\\": \\\"mach\\\", \\\"varName\\\": \\\"functionName\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 4, \\\"label\\\": \\\"Model Form\\\", \\\"value\\\": \\\"(speed/5280.0*3600.0)/a0\\\", \\\"varName\\\": \\\"modelForm\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 5, \\\"label\\\": \\\"Code\\\", \\\"value\\\": \\\"\\\", \\\"varName\\\": \\\"code\\\", \\\"config\\\": { \\\"mode\\\": \\\"python\\\", \\\"require\\\": [ \\\"ace/ext/language_tools\\\" ], \\\"advanced\\\": { \\\"enableSnippets\\\": true, \\\"enableBasicAutocompletion\\\": true, \\\"enableLiveAutocompletion\\\": true } }, \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 6, \\\"label\\\": \\\"Tags\\\", \\\"value\\\": [], \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\" } ], \\\"inputs\\\": [ { \\\"id\\\": \\\"machInput0\\\", \\\"label\\\": \\\"machInput0\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"mach\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": \\\"a0.a0Output0\\\", \\\"dataEditable\\\": false, \\\"filledData\\\": false }, { \\\"id\\\": \\\"machInput1\\\", \\\"label\\\": \\\"machInput1\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"mach\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": \\\"speed.speedOutput0\\\", \\\"dataEditable\\\": false, \\\"filledData\\\": false } ], \\\"outputs\\\": [ { \\\"id\\\": \\\"machOutput0\\\", \\\"label\\\": \\\"machOutput0\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"mach\\\", \\\"inputsConnected\\\": [], \\\"inputsIdConnected\\\": [ \\\"tt0.tt0Input2\\\" ] } ], \\\"nodeType\\\": { \\\"id\\\": \\\"deterministic\\\", \\\"name\\\": \\\"Deterministic\\\", \\\"formTemplate\\\": [ { \\\"index\\\": 0, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": \\\"Deterministic\\\", \\\"label\\\": \\\"Type\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 0 }, { \\\"index\\\": 1, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Name\\\", \\\"varName\\\": \\\"modelName\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 1 }, { \\\"index\\\": 2, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"select\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Type\\\", \\\"varName\\\": \\\"modelType\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [ \\\"Equation\\\", \\\"InitialState\\\", \\\"PythonFunction\\\" ], \\\"id\\\": 2 }, { \\\"index\\\": 3, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Function Name\\\", \\\"varName\\\": \\\"functionName\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 3 }, { \\\"index\\\": 4, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Form\\\", \\\"varName\\\": \\\"modelForm\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 4 }, { \\\"index\\\": 5, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"codeEditor\\\", \\\"required\\\": false, \\\"editable\\\": true, \\\"cols\\\": 12, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Code\\\", \\\"varName\\\": \\\"code\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"config\\\": { \\\"mode\\\": \\\"python\\\", \\\"require\\\": [ \\\"ace/ext/language_tools\\\" ], \\\"advanced\\\": { \\\"enableSnippets\\\": true, \\\"enableBasicAutocompletion\\\": true, \\\"enableLiveAutocompletion\\\": true } }, \\\"id\\\": 5 }, { \\\"index\\\": 6, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"dbnTagsInput\\\", \\\"required\\\": false, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": [], \\\"label\\\": \\\"Tags\\\", \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 6 } ], \\\"outputTemplate\\\": { \\\"Type\\\": \\\"@type\\\", \\\"Tag\\\": \\\"@tags\\\", \\\"Distribution\\\": \\\"\\\", \\\"DistributionParameters\\\": {}, \\\"ModelName\\\": \\\"@modelName\\\", \\\"Parents\\\": [], \\\"Children\\\": [] } }, \\\"inputCount\\\": 2, \\\"outputCount\\\": 1, \\\"top\\\": \\\"465px\\\", \\\"left\\\": \\\"801px\\\" }, { \\\"id\\\": \\\"tt0\\\", \\\"data\\\": [ { \\\"id\\\": 0, \\\"label\\\": \\\"Type\\\", \\\"value\\\": \\\"Deterministic\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 1, \\\"label\\\": \\\"Model Name\\\", \\\"value\\\": \\\"tt0\\\", \\\"varName\\\": \\\"modelName\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 2, \\\"label\\\": \\\"Model Type\\\", \\\"value\\\": \\\"Equation\\\", \\\"varName\\\": \\\"modelType\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 3, \\\"label\\\": \\\"Function Name\\\", \\\"value\\\": \\\"tt0\\\", \\\"varName\\\": \\\"functionName\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 4, \\\"label\\\": \\\"Model Form\\\", \\\"value\\\": \\\"ts0*(1.0 + 0.5*(gama-1.0)*mach*mach)\\\", \\\"varName\\\": \\\"modelForm\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 5, \\\"label\\\": \\\"Code\\\", \\\"value\\\": \\\"\\\", \\\"varName\\\": \\\"code\\\", \\\"config\\\": { \\\"mode\\\": \\\"python\\\", \\\"require\\\": [ \\\"ace/ext/language_tools\\\" ], \\\"advanced\\\": { \\\"enableSnippets\\\": true, \\\"enableBasicAutocompletion\\\": true, \\\"enableLiveAutocompletion\\\": true } }, \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 6, \\\"label\\\": \\\"Tags\\\", \\\"value\\\": [], \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\" } ], \\\"inputs\\\": [ { \\\"id\\\": \\\"tt0Input0\\\", \\\"label\\\": \\\"tt0Input0\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"tt0\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": \\\"ts0.ts0Output0\\\", \\\"dataEditable\\\": false, \\\"filledData\\\": false }, { \\\"id\\\": \\\"tt0Input1\\\", \\\"label\\\": \\\"tt0Input1\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"tt0\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": \\\"gama.gamaOutput0\\\", \\\"dataEditable\\\": false, \\\"filledData\\\": false }, { \\\"id\\\": \\\"tt0Input2\\\", \\\"label\\\": \\\"tt0Input2\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"tt0\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": \\\"mach.machOutput0\\\", \\\"dataEditable\\\": false, \\\"filledData\\\": false } ], \\\"outputs\\\": [ { \\\"id\\\": \\\"tt0Output0\\\", \\\"label\\\": \\\"tt0Output0\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"tt0\\\", \\\"inputsConnected\\\": [], \\\"inputsIdConnected\\\": [ \\\"pt0.pt0Input0\\\" ] } ], \\\"nodeType\\\": { \\\"id\\\": \\\"deterministic\\\", \\\"name\\\": \\\"Deterministic\\\", \\\"formTemplate\\\": [ { \\\"index\\\": 0, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": \\\"Deterministic\\\", \\\"label\\\": \\\"Type\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 0 }, { \\\"index\\\": 1, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Name\\\", \\\"varName\\\": \\\"modelName\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 1 }, { \\\"index\\\": 2, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"select\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Type\\\", \\\"varName\\\": \\\"modelType\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [ \\\"Equation\\\", \\\"InitialState\\\", \\\"PythonFunction\\\" ], \\\"id\\\": 2 }, { \\\"index\\\": 3, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Function Name\\\", \\\"varName\\\": \\\"functionName\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 3 }, { \\\"index\\\": 4, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Form\\\", \\\"varName\\\": \\\"modelForm\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 4 }, { \\\"index\\\": 5, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"codeEditor\\\", \\\"required\\\": false, \\\"editable\\\": true, \\\"cols\\\": 12, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Code\\\", \\\"varName\\\": \\\"code\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"config\\\": { \\\"mode\\\": \\\"python\\\", \\\"require\\\": [ \\\"ace/ext/language_tools\\\" ], \\\"advanced\\\": { \\\"enableSnippets\\\": true, \\\"enableBasicAutocompletion\\\": true, \\\"enableLiveAutocompletion\\\": true } }, \\\"id\\\": 5 }, { \\\"index\\\": 6, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"dbnTagsInput\\\", \\\"required\\\": false, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": [], \\\"label\\\": \\\"Tags\\\", \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 6 } ], \\\"outputTemplate\\\": { \\\"Type\\\": \\\"@type\\\", \\\"Tag\\\": \\\"@tags\\\", \\\"Distribution\\\": \\\"\\\", \\\"DistributionParameters\\\": {}, \\\"ModelName\\\": \\\"@modelName\\\", \\\"Parents\\\": [], \\\"Children\\\": [] } }, \\\"inputCount\\\": 3, \\\"outputCount\\\": 1, \\\"top\\\": \\\"600px\\\", \\\"left\\\": \\\"404px\\\" }, { \\\"id\\\": \\\"pt0\\\", \\\"data\\\": [ { \\\"id\\\": 0, \\\"label\\\": \\\"Type\\\", \\\"value\\\": \\\"Deterministic\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 1, \\\"label\\\": \\\"Model Name\\\", \\\"value\\\": \\\"pt0\\\", \\\"varName\\\": \\\"modelName\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 2, \\\"label\\\": \\\"Model Type\\\", \\\"value\\\": \\\"Equation\\\", \\\"varName\\\": \\\"modelType\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 3, \\\"label\\\": \\\"Function Name\\\", \\\"value\\\": \\\"pt0\\\", \\\"varName\\\": \\\"functionName\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 4, \\\"label\\\": \\\"Model Form\\\", \\\"value\\\": \\\"ps0*(tt0/ts0)**(gama/(gama-1.0))\\\", \\\"varName\\\": \\\"modelForm\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 5, \\\"label\\\": \\\"Code\\\", \\\"value\\\": \\\"\\\", \\\"varName\\\": \\\"code\\\", \\\"config\\\": { \\\"mode\\\": \\\"python\\\", \\\"require\\\": [ \\\"ace/ext/language_tools\\\" ], \\\"advanced\\\": { \\\"enableSnippets\\\": true, \\\"enableBasicAutocompletion\\\": true, \\\"enableLiveAutocompletion\\\": true } }, \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 6, \\\"label\\\": \\\"Tags\\\", \\\"value\\\": [], \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\" } ], \\\"inputs\\\": [ { \\\"id\\\": \\\"pt0Input0\\\", \\\"label\\\": \\\"pt0Input0\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"pt0\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": \\\"tt0.tt0Output0\\\", \\\"dataEditable\\\": false, \\\"filledData\\\": false }, { \\\"id\\\": \\\"pt0Input1\\\", \\\"label\\\": \\\"pt0Input1\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"pt0\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": \\\"ps0.ps0Output0\\\", \\\"dataEditable\\\": false, \\\"filledData\\\": false }, { \\\"id\\\": \\\"pt0Input2\\\", \\\"label\\\": \\\"pt0Input2\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"pt0\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": \\\"ts0.ts0Output0\\\", \\\"dataEditable\\\": false, \\\"filledData\\\": false }, { \\\"id\\\": \\\"pt0Input3\\\", \\\"label\\\": \\\"pt0Input3\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"pt0\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": \\\"gama.gamaOutput0\\\", \\\"dataEditable\\\": false, \\\"filledData\\\": false } ], \\\"outputs\\\": [ { \\\"id\\\": \\\"pt0Output0\\\", \\\"label\\\": \\\"pt0Output0\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"pt0\\\", \\\"inputsConnected\\\": [], \\\"inputsIdConnected\\\": [] } ], \\\"nodeType\\\": { \\\"id\\\": \\\"deterministic\\\", \\\"name\\\": \\\"Deterministic\\\", \\\"formTemplate\\\": [ { \\\"index\\\": 0, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": \\\"Deterministic\\\", \\\"label\\\": \\\"Type\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 0 }, { \\\"index\\\": 1, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Name\\\", \\\"varName\\\": \\\"modelName\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 1 }, { \\\"index\\\": 2, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"select\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Type\\\", \\\"varName\\\": \\\"modelType\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [ \\\"Equation\\\", \\\"InitialState\\\", \\\"PythonFunction\\\" ], \\\"id\\\": 2 }, { \\\"index\\\": 3, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Function Name\\\", \\\"varName\\\": \\\"functionName\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 3 }, { \\\"index\\\": 4, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Form\\\", \\\"varName\\\": \\\"modelForm\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 4 }, { \\\"index\\\": 5, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"codeEditor\\\", \\\"required\\\": false, \\\"editable\\\": true, \\\"cols\\\": 12, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Code\\\", \\\"varName\\\": \\\"code\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"config\\\": { \\\"mode\\\": \\\"python\\\", \\\"require\\\": [ \\\"ace/ext/language_tools\\\" ], \\\"advanced\\\": { \\\"enableSnippets\\\": true, \\\"enableBasicAutocompletion\\\": true, \\\"enableLiveAutocompletion\\\": true } }, \\\"id\\\": 5 }, { \\\"index\\\": 6, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"dbnTagsInput\\\", \\\"required\\\": false, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": [], \\\"label\\\": \\\"Tags\\\", \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 6 } ], \\\"outputTemplate\\\": { \\\"Type\\\": \\\"@type\\\", \\\"Tag\\\": \\\"@tags\\\", \\\"Distribution\\\": \\\"\\\", \\\"DistributionParameters\\\": {}, \\\"ModelName\\\": \\\"@modelName\\\", \\\"Parents\\\": [], \\\"Children\\\": [] } }, \\\"inputCount\\\": 4, \\\"outputCount\\\": 1, \\\"top\\\": \\\"648px\\\", \\\"left\\\": \\\"630px\\\" }, { \\\"id\\\": \\\"ts0\\\", \\\"data\\\": [ { \\\"id\\\": 0, \\\"label\\\": \\\"Type\\\", \\\"value\\\": \\\"Deterministic\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 1, \\\"label\\\": \\\"Model Name\\\", \\\"value\\\": \\\"ts0\\\", \\\"varName\\\": \\\"modelName\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 2, \\\"label\\\": \\\"Model Type\\\", \\\"value\\\": \\\"Equation\\\", \\\"varName\\\": \\\"modelType\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 3, \\\"label\\\": \\\"Function Name\\\", \\\"value\\\": \\\"ts0\\\", \\\"varName\\\": \\\"functionName\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 4, \\\"label\\\": \\\"Model Form\\\", \\\"value\\\": \\\"518.6-3.56*altitude/1000.0\\\", \\\"varName\\\": \\\"modelForm\\\", \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 5, \\\"label\\\": \\\"Code\\\", \\\"value\\\": \\\"\\\", \\\"varName\\\": \\\"code\\\", \\\"config\\\": { \\\"mode\\\": \\\"python\\\", \\\"require\\\": [ \\\"ace/ext/language_tools\\\" ], \\\"advanced\\\": { \\\"enableSnippets\\\": true, \\\"enableBasicAutocompletion\\\": true, \\\"enableLiveAutocompletion\\\": true } }, \\\"validation\\\": \\\"\\\" }, { \\\"id\\\": 6, \\\"label\\\": \\\"Tags\\\", \\\"value\\\": [], \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\" } ], \\\"inputs\\\": [ { \\\"id\\\": \\\"ts0Input0\\\", \\\"label\\\": \\\"ts0Input0\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"ts0\\\", \\\"outputConnected\\\": null, \\\"outputIdConnected\\\": \\\"altitude.altitudeOutput0\\\", \\\"dataEditable\\\": false, \\\"filledData\\\": false } ], \\\"outputs\\\": [ { \\\"id\\\": \\\"ts0Output0\\\", \\\"label\\\": \\\"ts0Output0\\\", \\\"fixedLabel\\\": false, \\\"node\\\": null, \\\"nodeId\\\": \\\"ts0\\\", \\\"inputsConnected\\\": [], \\\"inputsIdConnected\\\": [ \\\"ps0.ps0Input0\\\", \\\"a0.a0Input2\\\", \\\"tt0.tt0Input0\\\", \\\"pt0.pt0Input2\\\" ] } ], \\\"nodeType\\\": { \\\"id\\\": \\\"deterministic\\\", \\\"name\\\": \\\"Deterministic\\\", \\\"formTemplate\\\": [ { \\\"index\\\": 0, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": \\\"Deterministic\\\", \\\"label\\\": \\\"Type\\\", \\\"varName\\\": \\\"type\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 0 }, { \\\"index\\\": 1, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Name\\\", \\\"varName\\\": \\\"modelName\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 1 }, { \\\"index\\\": 2, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"select\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Type\\\", \\\"varName\\\": \\\"modelType\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [ \\\"Equation\\\", \\\"InitialState\\\", \\\"PythonFunction\\\" ], \\\"id\\\": 2 }, { \\\"index\\\": 3, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Function Name\\\", \\\"varName\\\": \\\"functionName\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 3 }, { \\\"index\\\": 4, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"textInput\\\", \\\"required\\\": true, \\\"editable\\\": true, \\\"cols\\\": 6, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Model Form\\\", \\\"varName\\\": \\\"modelForm\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 4 }, { \\\"index\\\": 5, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"codeEditor\\\", \\\"required\\\": false, \\\"editable\\\": true, \\\"cols\\\": 12, \\\"value\\\": \\\"\\\", \\\"label\\\": \\\"Code\\\", \\\"varName\\\": \\\"code\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"config\\\": { \\\"mode\\\": \\\"python\\\", \\\"require\\\": [ \\\"ace/ext/language_tools\\\" ], \\\"advanced\\\": { \\\"enableSnippets\\\": true, \\\"enableBasicAutocompletion\\\": true, \\\"enableLiveAutocompletion\\\": true } }, \\\"id\\\": 5 }, { \\\"index\\\": 6, \\\"description\\\": \\\"\\\", \\\"component\\\": \\\"dbnTagsInput\\\", \\\"required\\\": false, \\\"editable\\\": false, \\\"cols\\\": 12, \\\"value\\\": [], \\\"label\\\": \\\"Tags\\\", \\\"varName\\\": \\\"tags\\\", \\\"validation\\\": \\\"\\\", \\\"placeholder\\\": \\\"\\\", \\\"options\\\": [], \\\"id\\\": 6 } ], \\\"outputTemplate\\\": { \\\"Type\\\": \\\"@type\\\", \\\"Tag\\\": \\\"@tags\\\", \\\"Distribution\\\": \\\"\\\", \\\"DistributionParameters\\\": {}, \\\"ModelName\\\": \\\"@modelName\\\", \\\"Parents\\\": [], \\\"Children\\\": [] } }, \\\"inputCount\\\": 1, \\\"outputCount\\\": 1, \\\"top\\\": \\\"242px\\\", \\\"left\\\": \\\"293px\\\" } ] } }, \\\"dataSources\\\": {}, \\\"dataSourcesIds\\\": {}, \\\"additionalFilesIds\\\": {}, \\\"dataSourcesInfo\\\": {}, \\\"inputs\\\": [], \\\"outputs\\\": [], \\\"headerMappings\\\": {}, \\\"workDir\\\": \\\"/tmp/\\\" }\"";
        //System.out.println("\"" + responseTxt.replace("\"", "\\\"") + "\"");
//        System.out.println("\"" + testJson.replace("\"", "\\\"") + "\"");
        httppost.setEntity(new StringEntity("\"" + jsonTxt.replace("\"", "\\\"") + "\""));
//        httppost.setEntity(new StringEntity("\"" + testJson.replace("\"", "\\\"") + "\""));
        CloseableHttpResponse response = httpclient.execute(httppost);
        HttpEntity respEntity = response.getEntity();
        String responseTxt = EntityUtils.toString(respEntity, "UTF-8");
        System.out.println(responseTxt);
        httpclient.close();
        return responseTxt;
	}


	@SuppressWarnings("deprecation")
	private String generateDBNjson(String jsonTxt) throws Exception {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://vesuvius063.crd.ge.com:46000/dbn/jsonGenerator");
        httppost.setHeader("Accept", "application/json");
        httppost.setHeader("Content-type", "application/json");
        
        httppost.setEntity(new StringEntity(jsonTxt));
        CloseableHttpResponse response = httpclient.execute(httppost);
        HttpEntity respEntity = response.getEntity();
        String responseTxt = EntityUtils.toString(respEntity, "UTF-8");
        System.out.println(responseTxt);
        httpclient.close();
        return responseTxt;
	}


	@SuppressWarnings("deprecation")
	private String kgResultsToJson(String nodesCSVString, String modelsCSVString) throws Exception {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://vesuvius063.crd.ge.com:46000/dbn/SADLResultSetToJson");
		httppost.setHeader("Accept", "application/json");
        httppost.setHeader("Content-type", "application/x-www-form-urlencoded");
        
        List<NameValuePair> arguments = new ArrayList<>(2);
        arguments.add(new BasicNameValuePair("nodes", nodesCSVString));
        arguments.add(new BasicNameValuePair("models", modelsCSVString));
        httppost.setEntity(new UrlEncodedFormEntity(arguments, "UTF-8"));
//         HttpResponse response = httpclient.execute(httppost);
//         HttpEntity respEntity = response.getEntity();
//         String responseTxt = EntityUtils.toString(respEntity, "UTF-8");
        CloseableHttpResponse response = httpclient.execute(httppost);
        HttpEntity respEntity = response.getEntity();
        String responseTxt = EntityUtils.toString(respEntity, "UTF-8");
        httpclient.close();
        return responseTxt;
	}


	private String strUriList(List<OntClass> classList) {
		String uriStr = "";
		for ( OntClass o : classList ) {
			uriStr += "<" + o.toString() + ">,";
		}
		uriStr = uriStr.substring(0,uriStr.length()-1);
		return uriStr;
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
	
	private String convertResultSetToString(com.hp.hpl.jena.query.ResultSetRewindable results) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		com.hp.hpl.jena.query.ResultSetFormatter.outputAsCSV(baos, results);
		return new String(baos.toByteArray(), Charset.defaultCharset()).replace(System.getProperty("line.separator"), "\n");		
	}
}

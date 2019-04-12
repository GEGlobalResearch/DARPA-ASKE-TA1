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
package com.ge.research.sadl.darpa.aske.inference;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpPost;
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
import com.hp.hpl.jena.tdb.store.Hash;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.ConfigurationManager;
import com.ge.research.sadl.reasoner.IReasoner;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.TranslationException;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.ontology.OntResource;
import com.hp.hpl.jena.query.QueryExecution;
import com.hp.hpl.jena.query.QueryExecutionFactory;
import com.hp.hpl.jena.query.QueryFactory;
import com.hp.hpl.jena.query.QuerySolution;
import com.hp.hpl.jena.query.ResultSetRewindable;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Property;
import com.hp.hpl.jena.rdf.model.RDFNode;
import com.hp.hpl.jena.rdf.model.RDFVisitor;
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
	public static final String VARERROR_PROP = "http://sadl.org/sadlimplicitmodel#varError";
	public static final String MODELERROR_PROP = METAMODEL_PREFIX + "modelError";

	
	public static final String DEPENDENCY_GRAPH_INSERT = "prefix dbn:<http://aske.ge.com/dbn#>\n" + 
			"prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" +
			"prefix sci:<http://aske.ge.com/sciknow#>" +
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
			"insert {?EqCh dbn:parent ?EqPa}\n" + 
			"where {\n" + 
			" ?EqCh a ?EqClass. \n" + 
			" ?EqClass rdfs:subClassOf imp:Equation.\n" + 
			" ?EqCh sci:input ?Arg.\n" + 
			" ?Arg sci:argType ?In.\n" + 
			" ?EqCh sci:output ?Oinst.\n" + 
			" ?Oinst a ?Out.\n" + 
			"\n" + 
			" ?EqPa a ?EqClass1. \n" + 
			" ?EqClass1 rdfs:subClassOf imp:Equation.\n" + 
			" ?EqPa sci:output ?POinst.\n" + 
			" ?POinst a ?In.\n" + 
			"}";
	public static final String BUILD_COMP_GRAPH = "prefix hyper:<http://aske.ge.com/hypersonics#>\n" + 
			"prefix dbn:<http://aske.ge.com/dbn#>\n" + 
			"prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" + 
			"prefix owl:<http://www.w3.org/2002/07/owl#>\n" + 
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
			"prefix sci:<http://aske.ge.com/sciknow#>\n" +
			"prefix afn:<http://jena.apache.org/ARQ/function#>\n" +
			"select distinct ?DBN ?Out ?Eq where {\n" + 
			" {select distinct ?Eq where {\n" + 
			"    ?EqOut a ?EqClass. \n" + 
			"    ?EqClass rdfs:subClassOf imp:Equation.\n" + 
			"     ?EqOut sci:output ?Oinst.\n" + 
			"     ?Oinst a ?Out.\n" + 
			"     filter (?Out in ( LISTOFOUTPUTS )).\n" + 
			"     ?EqOut dbn:parent* ?EqIn.\n" + 
			"    ?EqIn sci:input/sci:argType ?In.\n" + 
			"    filter (?In in (LISTOFINPUTS)).\n" + 
			"    ?EqOut dbn:parent* ?Eq.\n" + 
			"    ?Eq dbn:parent* ?EqIn.\n" + 
			"  }}\n" + 
//			"    union {\n" + 
//			"   select distinct ?Eq where {\n" + 
//			"    ?EqOut a ?EqClass. \n" + 
//			"    ?EqClass rdfs:subClassOf imp:Equation.\n" + 
//			"     ?EqOut sci:output ?Oinst.\n" + 
//			"     ?Oinst a ?Out.\n" + 
//			"     filter (?Out in ( LISTOFOUTPUTS )).\n" + 
//			"     ?EqOut dbn:parent* ?EqIn.\n" + 
//			"    ?EqIn sci:input/sci:argType ?In.\n" + 
//			"    filter (?In in (LISTOFINPUTS)).\n" + 
//			"\n" + 
//			"    ?EqOut dbn:parent* ?EqInPath.\n" + 
//			"    ?EqInPath dbn:parent* ?EqIn.\n" + 
//			"\n" + 
//			"    ?EqInPath dbn:parent ?Eq.\n" + 
//			"    filter not exists {\n" + 
//			"       ?Eq dbn:parent* ?EqIn.\n" + 
//			"    }\n" +
//			"    filter not exists{\n" + 
//			"      ?Eq sci:input/sci:argType ?In1.\n" + 
//			"      filter (?In1 in (LISTOFINPUTS)).\n" + 
//			"    }" +
//			"  }}\n" + 
			"   ?Eq sci:output ?Oi.\n" + 
			" ?Oi a ?Out.\n" + 
			" ?DBN rdfs:subClassOf ?EQR.\n" + 
			" ?EQR owl:onProperty sci:hasEquation.\n" + 
			" ?EQR owl:allValuesFrom ?EqClass.\n" + 
			" ?Eq a ?EqClass.\n" + 
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
			"  ?BN owl:allValuesFrom ?EqClass.\n" + 
			"  ?Eq a ?EqClass.\n" + 
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
			"{select distinct ?Node ?Child ?Distribution ?Lower ?Upper # ?Value \n" + 
			"where {\n" + 
			"   ?Eq sci:input/sci:argType ?Node.\n" + 
			"   ?Eq sci:output ?Oinst. ?Oinst a ?Child.\n" + 
			"   filter (?Eq in (EQNSLIST))\n" + 
			"\n" + 
			" ?DBN rdfs:subClassOf ?N1. ?N1 owl:onProperty sci:node. ?N1 owl:hasValue/sci:argType ?Node.\n" + 
			" ?DBN rdfs:subClassOf ?DB. ?DB owl:onProperty sci:distribution. ?DB owl:hasValue ?Distribution.\n" + 
			" ?DBN rdfs:subClassOf ?RB. ?RB owl:onProperty sci:range. ?RB owl:hasValue ?Range.\n" + 
			" ?Range sci:lower ?Lower.\n" + 
			" ?Range sci:upper ?Upper.\n" + 
			"}}\n" + 
			" union {\n" + 
			" select distinct ?Node ?Child ?Distribution ?Lower ?Upper # ?Value \n" + 
			"where {\n" + 
			"   ?Eq sci:input/sci:argType ?Node.\n" + 
			"   ?Eq sci:output ?Oinst. ?Oinst a ?Child.\n" + 
			"   filter (?Eq in (EQNSLIST))\n" + 
			"\n" + 
			" ?DBN rdfs:subClassOf ?EB. ?EB owl:onProperty sci:hasEquation.  ?EB owl:allValuesFrom ?EqC. ?Eq1 a ?EqC. ?Eq1 sci:output ?O1. ?O1 a ?Node.\n" + 
			" ?DBN rdfs:subClassOf ?DB. ?DB owl:onProperty sci:distribution. ?DB owl:hasValue ?Distribution.\n" + 
			" ?DBN rdfs:subClassOf ?RB. ?RB owl:onProperty sci:range. ?RB owl:hasValue ?Range.\n" + 
			" ?Range sci:lower ?Lower.\n" + 
			" ?Range sci:upper ?Upper.\n" + 
			"}}\n" +
			" union {\n" + 
			" select distinct ?Eq ?Node where {\n" + 
			"   ?Eq sci:output ?Oi. ?Oi a ?Node.\n" + 
			"   filter (?Eq in (EQNSLIST))\n" + 
			"   filter not exists {\n" + 
			"     ?Eq1 sci:input/sci:argType ?Node.\n" + 
			"     filter (?Eq1 in (EQNSLIST))\n" + 
			"   }\n" + 
			"  ?DBN rdfs:subClassOf ?EB. ?EB owl:onProperty sci:hasEquation.  ?EB owl:allValuesFrom ?EqClass.\n" +
			"  ?Eq a ?EqClass." + 
			"  ?DBN rdfs:subClassOf ?DB. ?DB owl:onProperty sci:distribution. ?DB owl:hasValue ?Distribution.\n" +
			"  ?DBN rdfs:subClassOf ?RB. ?RB owl:onProperty sci:range.        ?RB owl:hasValue ?Range.\n" + 
			"  ?Range sci:lower ?Lower.\n" + 
			"  ?Range sci:upper ?Upper.\n" + 
			" }}\n" +
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
			"select distinct ?X ?Y ?Z ?X_style ?X_color ?Z_shape ?Z_tooltip\n" + //?X_style \n" + 
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
			"    ?EQ imp:expression ?Script. \n" +
			"    ?Script imp:script ?Expr.\n" + 
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
			"    ?EQ imp:expression ?Script. \n" + 
			"    ?Script imp:script ?Expr.\n" + 
			"	 bind(str(?Expr) as ?Z_tooltip)\n" +
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
			"select distinct (strafter(str(?CCG),'#') as ?Model) (strafter(str(?Var),'#') as ?Variable) ?Mean ?StdDev\n" + 
			"where {\n" + 
			"   {?CCG mm:subgraph ?SG.\n" + 
			"    filter (?CCG in (COMPGRAPH)).\n" +
			"    ?SG mm:output ?Oinst.\n" + 
			"    ?Oinst a ?Var.\n" + 
			"    ?Oinst imp:value ?Mean.\n" + 
			"    ?Oinst imp:stddev ?StdDev.\n" +
			"  } union {\n" +
			"   ?CCG mm:subgraph ?SG. \n" + 
			"   filter (?CCG in (COMPGRAPH)).\n" +
			"   ?Q mm:execution ?CE.\n" + 
			"   ?CE mm:compGraph ?CCG.\n" + 
			"   ?Q mm:output ?Vinst.\n" + 
			"   ?Vinst a ?Var.\n" + 
			"   ?Vinst imp:value ?Mean.\n" +
			"   ?Vinst imp:stddev ?StdDev.\n" +
			"}\n" +
			"}";
	public static final String RESULTSQUERYHYPO = "prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" + 
			"prefix mm:<http://aske.ge.com/metamodel#>\n" +
			"prefix sci:<http://aske.ge.com/sciknow#>\n" + 
			"select distinct (strafter(str(?CCG),'#') as ?Model) ?ModelAccuracy (strafter(str(?Var),'#') as ?Variable) ?Mean ?StdDev ?VarError\n" + 
			"where {\n" + 
			"   {?CCG mm:subgraph ?SG.\n" + 
			"    filter (?CCG in (COMPGRAPH)).\n" +
			"    ?CCG mm:modelError ?ModelAccuracy." + 
			"    ?SG mm:output ?Oinst.\n" + 
			"    ?Oinst a ?Var.\n" + 
			"    ?Oinst imp:value ?Mean.\n" + 
			"    ?Oinst imp:stddev ?StdDev.\n" +
			"    optional{?Oinst imp:varError ?VarError.}\n" + 
			"  } union {\n" +
			"   ?CCG mm:subgraph ?SG. \n" + 
			"   filter (?CCG in (COMPGRAPH)).\n" +
			"   ?CCG mm:modelError ?ModelAccuracy." + 
			"   ?Q mm:execution ?CE.\n" + 
			"   ?CE mm:compGraph ?CCG.\n" + 
			"   ?Q mm:output ?Vinst.\n" + 
			"   ?Vinst a ?Var.\n" + 
			"   ?Vinst imp:value ?Mean.\n" +
			"   ?Vinst imp:stddev ?StdDev.\n" +
			"   optional{?Vinst imp:varError ?VarError.}\n" +
			"}\n" +
			"}";
	public static final String DOCLOCATIONQUERY = "prefix imp:<http://sadl.org/sadlimplicitmodel#> \n" + 
			"select distinct (str(?furl) as ?file)\n" + 
			"where { \n" + 
			"  ?doc a imp:DataTable.\n" + 
			"  filter (?doc in (<DOCINSTANCE>))\n" + 
			"  ?doc imp:dataLocation ?furl.\n" + 
			"}";
	@Override
	public boolean isSupported(String fileExtension) {
		return "dialog".equals(fileExtension);
	}

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

		if (commonSubject(triples) && allPredicatesAreProperties(triples)) {
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
		List<TripleElement> docPatterns = new ArrayList<TripleElement>();

		List<OntClass> inputsList = new ArrayList<OntClass>();
		List<OntClass> outputsList = new ArrayList<OntClass>();
		//Map<OntClass, Individual> outputInstance = new HashMap<OntClass, Individual>();
		
		
		for (int i = 0; i < triples.length; i++) {
			TripleElement tr = triples[i];
			//Node snode = tr.getSubject();
			if (tr.getSubject() instanceof NamedNode) {
				
				if (tr.getPredicate().getURI() != null) {
				
					if (tr.getPredicate() != null && tr.getObject() != null && tr.getPredicate().getURI() != null) {
						// this is an input
						insertions.add(tr);
					}
					else {
						// this is an output
						queryPatterns.add(tr);
					}
				} 
				else {
					docPatterns.add(tr);
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
		ingestKGTriple(cgq,execprop,ce);
		//getTheJenaModel().add(cgq,execprop,ce);
		//qhmodel.add(cgq,execprop,ce);
		

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

				ingestKGTriple(sinst, pred, val);
				//qhmodel.add(sinst, pred, val);
				//getTheJenaModel().add(sinst,pred,val);
				getTheJenaModel().add(sinst, RDF.type, getTheJenaModel().getOntClass(suri));
				// create triple: cgq, mm:input, sinst
				OntProperty inputprop = getTheJenaModel().getOntProperty(METAMODEL_INPUT_PROP);
				ingestKGTriple(cgq, inputprop, sinst);
				//qhmodel.add(cgq, inputprop, sinst);
			
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
		
		
		if (docPatterns.size() > 0) {
			// how many results there will be
			//ResultSet[] results = new ResultSet[queryPatterns.size()];
			for (int i = 0; i < docPatterns.size(); i++) {
				TripleElement itr = docPatterns.get(i);

				suri = itr.getObject().getURI();

				sclass = getTheJenaModel().getOntClass(suri);
				outputsList.add(sclass);
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
		//OntProperty outputprop = getTheJenaModel().getOntProperty(METAMODEL_OUTPUT_PROP);
		String listOfEqns = "";
		com.hp.hpl.jena.query.ResultSetRewindable models, nodes;
		String modelsCSVString = "";
		String nodesCSVString = "";
		Individual cgIns = null;
		
		Map<String,String> class2lbl;
		Map<String,String[]> lbl2value;
		
		String cgJson, dbnJson, dbnResultsJson;
		
		Map<RDFNode, String[]> dbnEqns = new HashMap<RDFNode,String[]>();
		Map<RDFNode, RDFNode> dbnOutput = new HashMap<RDFNode,RDFNode>();

		
//		ResultSet[] results = new ResultSet[2]; 
		ResultSet[] results = null;
		
		try {
		
			if (outputsList.size() > 0 && docPatterns.size() <= 0) {
	
				
				listOfInputs = strUriList(inputsList);
				listOfOutputs = strUriList(outputsList);
	
				// Retrieve computational graph
				queryStr = BUILD_COMP_GRAPH.replaceAll("LISTOFINPUTS", listOfInputs).replaceAll("LISTOFOUTPUTS", listOfOutputs);
				
				com.hp.hpl.jena.query.Query q = QueryFactory.create(queryStr);
				qexec = QueryExecutionFactory.create(q, getTheJenaModel()); 
				eqnsResults = com.hp.hpl.jena.query.ResultSetFactory.makeRewindable(qexec.execSelect()) ;
				String queryMode = "prognostic";
				
				//if(eqnsResults.getRowNumber() <=0)
				//	System.out.println("***No models***");
				
				//int sz = eqnsResults.size();
				//boolean nxt = eqnsResults.hasNext();

				if ( eqnsResults.size() > 0 ) {
					qtype = getTheJenaModel().getOntResource(METAMODEL_PREFIX + "prognostic");
					//qhmodel.add(cgq, qtypeprop, qtype);
					ingestKGTriple(cgq, qtypeprop, qtype);
				} else {
					queryStr = BUILD_COMP_GRAPH.replaceAll("LISTOFOUTPUTS", listOfInputs).replaceAll("LISTOFINPUTS", listOfOutputs);
					com.hp.hpl.jena.query.Query qinv = QueryFactory.create(queryStr);
					qexec = QueryExecutionFactory.create(qinv, getTheJenaModel()); 
					eqnsResults = com.hp.hpl.jena.query.ResultSetFactory.makeRewindable(qexec.execSelect()) ;
					if (eqnsResults.size() > 0) {
							queryMode = "calibration";
							qtype = getTheJenaModel().getOntResource(METAMODEL_PREFIX + "calibration");
							//qhmodel.add(cgq, qtypeprop, qtype);	
							ingestKGTriple(cgq, qtypeprop, qtype);
					}
				}
				
				dbnEqns = createDbnEqnMap(eqnsResults);
				dbnOutput = createDbnOutputMap(eqnsResults);

				
				int numOfModels = 1;
				if (dbnEqns.isEmpty())
					numOfModels = 0;
				for (RDFNode dbnk :  dbnEqns.keySet()) {
					numOfModels *= dbnEqns.get(dbnk).length;
				}

				
				String[] modelEqnList = buildEqnsLists(numOfModels, dbnEqns);

				
				results = new ResultSet[numOfModels*2]; 
				
				
				for(int i=0; i<numOfModels; i++) {
					listOfEqns = modelEqnList[i];
	
					// Comp Graph instance
					cgIns = qhmodel.createIndividual(METAMODEL_PREFIX + "CG_" + System.currentTimeMillis(), getTheJenaModel().getOntClass(METAMODEL_CCG));
					//qhmodel.add(ce, getTheJenaModel().getOntProperty(METAMODEL_COMPGRAPH_PROP), cgIns);
					ingestKGTriple(ce, getTheJenaModel().getOntProperty(METAMODEL_COMPGRAPH_PROP), cgIns);
					//getTheJenaModel().add(ce, RDF.type, getTheJenaModel().getOntClass(METAMODEL_CCG));


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
		
//					if (nodes.size() <= 0) {
//						return null;
//					}
		
					
					cgJson = kgResultsToJson(nodesCSVString, modelsCSVString, queryMode, "");
					dbnJson = generateDBNjson(cgJson);
					class2lbl = getClassLabelMapping(dbnJson);
					
		            dbnResultsJson = executeDBN(dbnJson);
		            
		            //check if DBN execution was successful
		            
		            String resmsg = getDBNoutcome(dbnResultsJson).trim();
		            //boolean suc = resmsg.equals("Success");
		            
		            if(resmsg.equals("Success")) {
			            
			            lbl2value = getLabelToMeanStdMapping(dbnResultsJson);
		
			            //createCGsubgraphs(cgIns, eqnsResults, class2lbl, lbl2value, outputInstance);
			            createCGsubgraphs(cgIns, dbnEqns, dbnOutput, listOfEqns, class2lbl, lbl2value); //, outputInstance);

			            //add outputs to CG if calibration
						if(queryMode.equals("calibration")) {
							for(OntClass oc : outputsList) {
								String ocls = oc.toString();
								String cls = class2lbl.get(ocls);
								String[] ms = lbl2value.get(cls);  
								Individual oinst = createIndividualOfClass(qhmodel,ocls);
								//getTheJenaModel().add(oinst,RDF.type, ocls);
								OntProperty outputprop = getTheJenaModel().getOntProperty(METAMODEL_OUTPUT_PROP);
								//qhmodel.add(cgq, outputprop, oinst);
								ingestKGTriple(cgq, outputprop, oinst);
								qhmodel.add(oinst, getTheJenaModel().getProperty(VALUE_PROP), ms[0] );
								qhmodel.add(oinst, getTheJenaModel().getProperty(STDDEV_PROP), ms[1] );
							}
						}

			            
						// create ResultSet
						results[i] = retrieveCompGraph(resource, cgIns);
						
						results[i+numOfModels] = retrieveValues(resource, cgIns);

		            
		            }
		            else {
		            	results = null;
		            	//System.err.println("DBN execution failed. Message: " + dbnResultsJson.toString());
		            }
				}
				if (numOfModels < 1) {
					results = null;
					//System.out.println("Unable to assemble a model with current knowledge");
				}
				
				
			} else if (outputsList.size() > 0 && docPatterns.size() > 0) {
				
				
				List<ResultSet> resultsList = new ArrayList<ResultSet>();
				
				qtype = getTheJenaModel().getOntResource(METAMODEL_PREFIX + "explanation");
				//qhmodel.add(cgq, qtypeprop, qtype);	// do we add it even if we don't know if there's a model yet?
				ingestKGTriple(cgq, qtypeprop, qtype);
				inputsList.addAll(outputsList);
				
				//results = new ResultSet[outputsList.size()*2]; 

				int numOfModels = 0;

				for(int i=0; i < outputsList.size(); i++) {
					List<OntClass> outpl = new ArrayList<OntClass>();
					List<OntClass> inpl = new ArrayList<OntClass>();
					OntClass oclass = outputsList.get(i);
	
					outpl.add(oclass);
					inpl.addAll(outputsList);
					inpl.remove(i);
					
					//Individual oinst = createIndividualOfClass(qhmodel, oclass.getURI());
					//outputInstance.put(oclass,oinst);
					
					
					listOfInputs = strUriList(inpl);
					listOfOutputs = strUriList(outpl);
					System.out.println(listOfInputs);
					System.out.println(listOfOutputs);

					queryStr = BUILD_COMP_GRAPH.replaceAll("LISTOFINPUTS", listOfInputs).replaceAll("LISTOFOUTPUTS", listOfOutputs);
					com.hp.hpl.jena.query.Query q = QueryFactory.create(queryStr);
					qexec = QueryExecutionFactory.create(q, getTheJenaModel()); 
					eqnsResults = com.hp.hpl.jena.query.ResultSetFactory.makeRewindable(qexec.execSelect()) ;
					
					//int ns = eqnsResults.size();
					//boolean nn = eqnsResults.hasNext();
					
					dbnEqns = createDbnEqnMap(eqnsResults);
					dbnOutput = createDbnOutputMap(eqnsResults);
					
//					int numOfModels = 1;
//					if (dbnEqns.isEmpty())
//						numOfModels = 0;
					
					int newModels = 0;
					if(!dbnEqns.isEmpty())
						newModels = 1;
					for (RDFNode dbnk :  dbnEqns.keySet()) {
						newModels *= dbnEqns.get(dbnk).length;
					}

					
					String[] modelEqnList = buildEqnsLists(newModels, dbnEqns);

					//results = new ResultSet[outputsList.size()*numOfModels*2]; 
					
					//qtype = getTheJenaModel().getOntResource(METAMODEL_PREFIX + "prognostic");
					//qhmodel.add(cgq, qtypeprop, qtype);			
					
					//if (eqnsResults.size() > 0 ) {
					for(int mod=0; mod<newModels; mod++) {
						
						listOfEqns = modelEqnList[mod]; //getEqnUrisFromResults(eqnsResults);
			            eqnsResults.reset();

						cgIns = qhmodel.createIndividual(METAMODEL_PREFIX + "CG_" + System.currentTimeMillis(), getTheJenaModel().getOntClass(METAMODEL_CCG));
						//getTheJenaModel().add(cgIns, RDF.type, getTheJenaModel().getOntClass(METAMODEL_CCG));
						// Each comp graph is attached to the execution instance
						//qhmodel.add(ce, getTheJenaModel().getOntProperty(METAMODEL_COMPGRAPH_PROP), cgIns);
						ingestKGTriple(ce, getTheJenaModel().getOntProperty(METAMODEL_COMPGRAPH_PROP), cgIns);
						
						// Retrieve Models & Nodes
						queryStr = RETRIEVE_MODELS.replaceAll("EQNSLIST", listOfEqns);
						models = queryKnowledgeGraph(queryStr, getTheJenaModel());
						
						queryStr = RETRIEVE_NODES.replaceAll("EQNSLIST", listOfEqns);
						queryStr = queryStr.replaceAll("COMPGRAPH", "<" + cgIns.getURI() + ">");
						nodes = queryKnowledgeGraph(queryStr, getTheJenaModel().union(qhmodel));
						
						modelsCSVString = convertResultSetToString(models);
						nodesCSVString = convertResultSetToString(nodes);

						String docUri = triples[0].getSubject().getURI();
						
						queryStr = DOCLOCATIONQUERY.replaceAll("DOCINSTANCE", docUri);
						
						ResultSetRewindable fileRes = queryKnowledgeGraph(queryStr, getTheJenaModel());
						if (fileRes.size() < 1) {
							results = null;
							continue;
						}
						QuerySolution fsol = fileRes.nextSolution();
						String fileName = fsol.get("?file").toString().split("//")[1] ;
						
						//String dataFile = new File(getModelFolderPath(resource)).getParent() + File.separator + "Data" + File.separator + "hypothesis.csv";
						String dataFile = new File(getModelFolderPath(resource)).getParent() + File.separator + fileName;
						cgJson = kgResultsToJson(nodesCSVString, modelsCSVString, "prognostic", getDataForHypothesisTesting(dataFile));
						dbnJson = generateDBNjson(cgJson);
						class2lbl = getClassLabelMapping(dbnJson);
	
						// Run the model
						dbnResultsJson = executeDBN(dbnJson);
			            String resmsg = getDBNoutcome(dbnResultsJson).trim();
			            //boolean suc = resmsg.equals("Success");
			            
			            if(resmsg.equals("Success")) {
			            	
			            	numOfModels ++;
							
			            	//Ingest model error
			            	String modelError = getModelError(dbnResultsJson);
			    			qhmodel.add(cgIns, getTheJenaModel().getProperty(MODELERROR_PROP), modelError);
			            	
			            	lbl2value = getLabelToMeanStdMapping(dbnResultsJson);
						
				            createCGsubgraphs(cgIns, dbnEqns, dbnOutput, listOfEqns, class2lbl, lbl2value); //, outputInstance);
							
				            resultsList.add(numOfModels-1, retrieveCompGraph(resource, cgIns));
				            resultsList.add(numOfModels*2-1, retrieveValuesHypothesis(resource, cgIns));
				            
							// create ResultSet
//							results[i] = retrieveCompGraph(resource, cgIns);
//							results[i+1] = retrieveValuesHypothesis(resource, cgIns);
			            }
					}
					// else continue looking
				}
				results = new ResultSet[numOfModels*2];
				results = resultsList.toArray(results);
			}
			
		
		    		
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
		

		
		
		
		return null;
	}




	private void ingestKGTriple(Individual s, Property pred, RDFNode o) {
		qhmodel.add(s,pred,o);
		//getTheJenaModel().add(s,pred,o);
	}



	private String getModelError(String dbnResultsJson) {
        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(dbnResultsJson);
        JsonObject jsonObject;
        String error;

        if (jsonTree.isJsonObject()) {
            jsonObject = jsonTree.getAsJsonObject();
            if (jsonObject.has("results")) {
            	jsonObject = (JsonObject) jsonObject.get("results");
                if (jsonObject.has("modelAccuracy")) {
                	jsonObject = (JsonObject) jsonObject.get("modelAccuracy");
                	error = jsonObject.get("meanError").toString();
                	return error;
                }
            }
        }
        return "";
	}



	private String getDBNoutcome(String dbnResultsJson) {
        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(dbnResultsJson);
        JsonObject jsonObject;
        String jres = null;

        if (jsonTree.isJsonObject()) {
            jsonObject = jsonTree.getAsJsonObject();
            if (jsonObject.has("modelStatus")) 
                jres = jsonObject.get("modelStatus").getAsString();
        }
        return jres;
	}


	private Map<RDFNode, RDFNode> createDbnOutputMap(ResultSetRewindable eqnsResults) {
		Map<RDFNode, RDFNode> map = new HashMap<RDFNode, RDFNode>();
//		int dbnNum = eqnsResults.getRowNumber();
//		List<String> eqArray = new ArrayList<String>();
		RDFNode dbn;
		RDFNode op;
		QuerySolution row;
		eqnsResults.reset();
		while(eqnsResults.hasNext()) {
			row = eqnsResults.nextSolution() ;
			dbn = row.get("?DBN") ;
			op = row.get("?Out");
			map.put(dbn, op);
		}
		return map;
	}


	private String[] buildEqnsLists(int numOfModels, Map<RDFNode, String[]> dbnEqns) {
		String[] modelEqnList = new String[numOfModels];
		RDFNode prevKey;
		Map<RDFNode, Integer> dbnEqnIdx = new HashMap<RDFNode,Integer>();

		for(RDFNode dbnk : dbnEqns.keySet()) 
			dbnEqnIdx.put(dbnk, 0);
		
		for(int m=0; m<numOfModels; m++) {
			modelEqnList[m] = "";
//			modelEqnList[m] = dbnEqn[ dbnEqnIdx[] ]
			prevKey = null;
			for(RDFNode dbnk : dbnEqns.keySet()) {
				modelEqnList[m] += "<" + dbnEqns.get(dbnk)[ dbnEqnIdx.get(dbnk) ] + ">,";
				
				if( prevKey == null || dbnEqnIdx.get(prevKey) == 0 ) {
				    if( dbnEqnIdx.get(dbnk) < dbnEqns.get(dbnk).length -1)
				    	dbnEqnIdx.put(dbnk, dbnEqnIdx.get(dbnk)+1 );
				    else
				    	dbnEqnIdx.put(dbnk, 0);
				    prevKey = dbnk;
				}
			}
			modelEqnList[m] = modelEqnList[m].substring(0,modelEqnList[m].length()-1);
		}
		return modelEqnList;
	}


	private Map<RDFNode, String[]> createDbnEqnMap(ResultSetRewindable eqnsResults) {
		Map<RDFNode, String[]> map = new HashMap<RDFNode, String[]>();
//		int dbnNum = eqnsResults.getRowNumber();
//		List<String> eqArray = new ArrayList<String>();
		eqnsResults.reset();
		RDFNode dbn;
		//String eqns[];
		QuerySolution row;
		//Set<String> eq_set = new HashSet<String>(); 
		Map<RDFNode, Set<String>> mapSet = new HashMap<RDFNode, Set<String>>();
		String eq;

		while(eqnsResults.hasNext()) {
			row = eqnsResults.nextSolution() ;
			dbn = row.get("?DBN");
			Set<String> eq_set = new HashSet<String>(); 
			mapSet.put(dbn, eq_set);
		}
		eqnsResults.reset();
		while(eqnsResults.hasNext()) {
			row = eqnsResults.nextSolution() ;
			dbn = row.get("?DBN");
			eq = row.get("?Eq").toString();
			mapSet.get(dbn).add(eq);
		}
		for(RDFNode n : mapSet.keySet()) {
			String[] eqns = new String[mapSet.get(n).size()];
			eqns = mapSet.get(n).toArray(eqns);
			map.put(n, eqns );
		}
		return map;
	}

	private boolean allPredicatesAreProperties(TripleElement[] triples) {
		for (TripleElement tr : triples) {
			if (tr.getPredicate().getNamespace() == null && tr.getPredicate().getPrefix() == null) {
				return false;
			}
		}
		return true;
	}


	private ResultSet retrieveValuesHypothesis(Resource resource, Individual cgIns) throws Exception {
		String query = RESULTSQUERYHYPO.replaceAll("COMPGRAPH", "<" + cgIns.getURI() + ">");
		ResultSet res = runReasonerQuery(resource, query);
		return res;
	}

	
	private ResultSet retrieveValues(Resource resource, Individual cgIns) throws Exception {
		String query = RESULTSQUERY.replaceAll("COMPGRAPH", "<" + cgIns.getURI() + ">");
		ResultSet res = runReasonerQuery(resource, query);
		return res;
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
		reasoner.loadInstanceData(getTheJenaModel());
		String pquery = reasoner.prepareQuery(query);

		ResultSet res = reasoner.ask(pquery);
		return res;
	}


	private void saveMetaDataFile(Resource resource) {
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







//	private void createCGsubgraphs(Individual cgIns, ResultSetRewindable eqnsResults, 
//			Map<String, String> class2lbl,
//			Map<String, String[]> lbl2value, 
//			Map<OntClass, Individual> outputInstance) {
		private void createCGsubgraphs(Individual cgIns, Map<RDFNode, String[]> dbnEqns, Map<RDFNode, RDFNode> dbnOutput,
				String listOfEqns, Map<String, String> class2lbl, Map<String, String[]> lbl2value) //, Map<OntClass, Individual> outputInstance)
		{

		OntProperty subgraphprop = 	getTheJenaModel().getOntProperty(METAMODEL_SUBG_PROP);
		OntProperty cgraphprop = 	getTheJenaModel().getOntProperty(METAMODEL_CGRAPH_PROP);
		OntProperty hasEqnProp = 	getTheJenaModel().getOntProperty(METAMODEL_HASEQN_PROP);
		OntProperty outputprop = 	getTheJenaModel().getOntProperty(METAMODEL_OUTPUT_PROP);
		
		Individual sgIns, dbnIns, outpIns;

		//need to do before querying for Nodes
//		qhmodel.add(ce, getTheJenaModel().getOntProperty(METAMODEL_COMPGRAPH_PROP), cgIns);

		//String cgInsStr = cgIns.getURI();
		//System.out.println("***CG instance: " + cgInsStr);
		
		//for (RDFNode eq : listOfEqnObjs) {
		for( RDFNode dbn : dbnEqns.keySet() ) {
//			RDFNode s = soln.get("?Eq") ; 
//			RDFNode p = soln.get("?DBN") ;
//			RDFNode o = soln.get("?Out") ;
			sgIns = createIndividualOfClass(qhmodel,METAMODEL_SUBGRAPH);
			   //getTheJenaModel().add(sgIns, RDF.type, getTheJenaModel().getOntClass(METAMODEL_SUBGRAPH));
			//qhmodel.add(cgIns, subgraphprop, sgIns);
			ingestKGTriple(cgIns, subgraphprop, sgIns);
			dbnIns = createIndividualOfClass(qhmodel,dbn.toString());
			   //getTheJenaModel().add(dbnIns, RDF.type, getTheJenaModel().getOntClass(dbn.toString()));

			//getTheJenaModel().add(sgIns, cgraphprop, dbnIns);
			//qhmodel.add(sgIns, cgraphprop, dbnIns);
			ingestKGTriple(sgIns, cgraphprop, dbnIns);
			//getTheJenaModel().add(dbnIns, hasEqnProp, s); //the equation is already an instance
			String[] modelEqns = listOfEqns.split(",");
			String[] deqs = dbnEqns.get(dbn);
			String eqn=null;
			for(int i=0; i<deqs.length; i++) {
				for(int j=0; j<modelEqns.length; j++) {
					String me = modelEqns[j];
					me = me.substring(1, me.length()-1);
					if( me.equals(deqs[i])) {
						eqn = me;
						break;
					}
				}
				if (eqn != null)
					break;
			}
			RDFNode e = qhmodel.getResource(eqn);  //getIndividual(eqn);
			//qhmodel.add(dbnIns, hasEqnProp, e);
			ingestKGTriple(dbnIns, hasEqnProp, e);
			//RDFNode o = dbnOutput.get(dbn);
			//if (outputInstance.containsKey(o)) {
			//	outpIns = outputInstance.get(o);
			//	//qhmodel.add(sgIns, outputprop, outputInstance.get(o));
			//}
			//else {
			String ostr = dbnOutput.get(dbn).toString();
			outpIns = createIndividualOfClass(qhmodel,ostr);
			   //getTheJenaModel().add(outpIns, RDF.type, ostr);
//				getTheJenaModel().add(sgIns, outputprop, outpIns);
//				qhmodel.add(sgIns, outputprop, outpIns);
			//}
			//getTheJenaModel().add(sgIns, outputprop, outpIns);
			//qhmodel.add(sgIns, outputprop, outpIns);
			ingestKGTriple(sgIns, outputprop, outpIns);
			
			String ocls = dbnOutput.get(dbn).toString();
			String cls = class2lbl.get(ocls);
			String[] ms = lbl2value.get(cls);  //class2lbl.get(o.toString()));
			qhmodel.add(outpIns, getTheJenaModel().getProperty(VALUE_PROP), ms[0] );
			qhmodel.add(outpIns, getTheJenaModel().getProperty(STDDEV_PROP), ms[1] );
			if(ms[2] != null)
				qhmodel.add(outpIns, getTheJenaModel().getProperty(VARERROR_PROP), ms[2] );
			
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
                	String[] vpr = new String[3];
                	if (vpair.has("mean")) {
	                	vpr[0] = vpair.get("mean").toString();
                	}
                	if (vpair.has("std")) {
	                	vpr[1] = vpair.get("std").toString();
                	}	
                	if (vpair.has("error")) {
	                	vpr[2] = vpair.get("error").toString();
                	}	
                	lbl2value.put(k.getKey(), vpr);
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
		eqnsResults.reset();
		for ( ; eqnsResults.hasNext() ; )
	    {
	      soln = eqnsResults.nextSolution() ;
	      RDFNode s = soln.get("?Eqns") ; 
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
	private String kgResultsToJson(String nodesCSVString, String modelsCSVString, String mode, String obsData) throws Exception {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		HttpPost httppost = new HttpPost("http://vesuvius063.crd.ge.com:46000/dbn/SADLResultSetToJson");
		httppost.setHeader("Accept", "application/json");
        httppost.setHeader("Content-type", "application/x-www-form-urlencoded");
        
        List<NameValuePair> arguments = new ArrayList<>(2);
        arguments.add(new BasicNameValuePair("nodes", nodesCSVString));
        arguments.add(new BasicNameValuePair("models", modelsCSVString));
        arguments.add(new BasicNameValuePair("mode", mode));
        arguments.add(new BasicNameValuePair("data", obsData));

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
	
	private String getDataForHypothesisTesting(String dataFile) {
		String dataContent = "";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(dataFile));
			String line;
			try {
				while ((line = br.readLine()) != null) {
					dataContent += line + "\n"; 
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
		
		return dataContent;
	}
}

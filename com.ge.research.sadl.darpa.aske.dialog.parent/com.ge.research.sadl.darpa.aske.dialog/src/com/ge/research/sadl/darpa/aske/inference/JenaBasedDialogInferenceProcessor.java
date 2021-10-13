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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FilenameFilter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
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
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.xtext.generator.parser.antlr.debug.simpleAntlr.Skip;

import com.ge.research.sadl.builder.ConfigurationManagerForIDE;
import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.SadlStatementContent;
import com.ge.research.sadl.darpa.aske.processing.imports.InvizinServiceInterface;
import com.ge.research.sadl.darpa.aske.processing.imports.KChainServiceInterface;
import com.ge.research.sadl.jena.JenaBasedSadlInferenceProcessor;
import com.ge.research.sadl.jena.JenaBasedSadlModelProcessor;
import com.ge.research.sadl.jena.UtilsForJena;
import com.ge.research.sadl.model.persistence.SadlPersistenceFormat;
import com.ge.research.sadl.model.gp.Literal;
import com.ge.research.sadl.model.gp.NamedNode;
import com.ge.research.sadl.model.gp.Node;
import com.ge.research.sadl.model.gp.Rule;
import com.ge.research.sadl.model.gp.TripleElement;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.processing.SadlConstants;
import com.ge.research.sadl.processing.SadlInferenceException;
import com.ge.research.sadl.reasoner.AmbiguousNameException;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.ConfigurationManager;
import com.ge.research.sadl.reasoner.IReasoner;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.TranslationException;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.ge.research.sadl.utils.ResourceManager;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.ontology.OntResource;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSetRewindable;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.update.UpdateAction;
import org.apache.jena.vocabulary.RDF;

import com.ge.research.semtk.resultSet.min.Table;
import com.ge.research.semtk.sparqlx.min.*;


/**
 * @author 212438865
 *
 */
public class JenaBasedDialogInferenceProcessor extends JenaBasedSadlInferenceProcessor {

	private OntModel queryModel;
//	private Map<String, String> preferenceMap;
	
//	public static final String queryHistoryKey = "MetaData";
//	public static final String qhModelName = "http://aske.ge.com/MetaData";
//	public static final String qhOwlFileName = "MetaData.owl";

	public static final boolean debugMode = true;
	public static final boolean performSensitivity = false;

	
    private static final String KCHAIN_SERVICE_URL_FRAGMENT = "/darpa/aske/kchain/";

	public static final String CGMODELS_FOLDER = "ComputationalGraphModels";
	
	
	public static final String METAMODEL_PREFIX = "http://aske.ge.com/metamodel#";
	public static final String METAMODEL_QUERY_CLASS = METAMODEL_PREFIX + "CGQuery";
	public static final String METAMODEL_INPUT_PROP = METAMODEL_PREFIX + "cgInput";
	public static final String METAMODEL_OUTPUT_PROP = METAMODEL_PREFIX + "output";
	public static final String METAMODEL_CCG = METAMODEL_PREFIX + "CCG";
	public static final String METAMODEL_SUBGRAPH = METAMODEL_PREFIX + "SubGraph";
	public static final String METAMODEL_SUBG_PROP = METAMODEL_PREFIX + "subgraph";
	public static final String METAMODEL_CGRAPH_PROP = METAMODEL_PREFIX + "cgraph";
	public static final String METAMODEL_EXEC_PROP = METAMODEL_PREFIX + "execution";
	public static final String METAMODEL_COMPGRAPH_PROP = METAMODEL_PREFIX + "compGraph";
	public static final String METAMODEL_HASEQN_PROP = "http://aske.ge.com/compgraphmodel#hasEquation";
	public static final String METAMODEL_HASMODEL_PROP = "http://aske.ge.com/compgraphmodel#hasModel";
	public static final String METAMODEL_QUERYTYPE_PROP = METAMODEL_PREFIX + "queryType";
	public static final String METAMODEL_PROGNOSTIC = METAMODEL_PREFIX + "prognostic";
	public static final String METAMODEL_CALIBRATION = METAMODEL_PREFIX + "calibration";
	public static final String METAMODEL_CGEXEC_CLASS = METAMODEL_PREFIX + "CGExecution";
	public static final String METAMODEL_STARTTIME_PROP = METAMODEL_PREFIX + "startTime";
	public static final String VALUE_PROP = "http://sadl.org/sadlimplicitmodel#value";
	public static final String UNIT_PROP = "http://sadl.org/sadlimplicitmodel#unit";
	public static final String STDDEV_PROP = "http://sadl.org/sadlimplicitmodel#stddev";
	public static final String VARERROR_PROP = "http://sadl.org/sadlimplicitmodel#varError";
	public static final String MODELERROR_PROP = METAMODEL_PREFIX + "accuracy";
	public static final String METAMODEL_SENS_PROP = METAMODEL_PREFIX + "sensitivity";
	public static final String METAMODEL_INPUTSENS_PROP = METAMODEL_PREFIX + "inputSensitivity";
	public static final String METAMODEL_SENSVALUE_PROP = METAMODEL_PREFIX + "sensitivityValue";
	public static final String METAMODEL_SENSITIVITY = METAMODEL_PREFIX + "Sensitivity";
	public static final String METAMODEL_INPUTSENSITIVITY = METAMODEL_PREFIX + "InputSensitivity";
	public static final String METAMODEL_SENS_TRENDEFFECT_PROP = METAMODEL_PREFIX + "trendEffect";
	public static final String METAMODEL_SENS_OUTPUTTREND = METAMODEL_PREFIX + "OutputTrend";
	public static final String METAMODEL_SENS_TREND_PROP = METAMODEL_PREFIX + "trend";
	private static final String METAMODEL_TREND_INCRINCR = METAMODEL_PREFIX + "increasingIncreases";
	private static final String METAMODEL_TREND_INCRDECR = METAMODEL_PREFIX + "increasingDecreases";
	private static final String METAMODEL_TREND_DECRINCR = METAMODEL_PREFIX + "decreasingIncreases";
	private static final String METAMODEL_TREND_DECRDECR = METAMODEL_PREFIX + "decreasingDecreases";
	private static final String METAMODEL_TREND_INDEPENDENT = METAMODEL_PREFIX + "independent";
	private static final String METAMODEL_POS_SENSITIVE = METAMODEL_PREFIX + "pos_sensitive";
	private static final String METAMODEL_NEG_SENSITIVE = METAMODEL_PREFIX + "neg_sensitive";
	private static final String METAMODEL_SENS_TREND_LOC_PROP = METAMODEL_PREFIX + "locationWRTquery";
	private static final String METAMODEL_TREND_AT_LOWER = METAMODEL_PREFIX + "lower_values";
	private static final String METAMODEL_TREND_AT_HIGHER = METAMODEL_PREFIX + "higher_values";
	private static final String METAMODEL_TREND_LOCALMAX = METAMODEL_PREFIX + "local_maximum";
	private static final String METAMODEL_TREND_LOCALMIN = METAMODEL_PREFIX + "local_minimum";
	public static final String METAMODEL_ASSUMPTIONSSATISFIED_PROP = METAMODEL_PREFIX + "assumptionsSatisfied";
	public static final String METAMODEL_ASSUMPTIONUNSATISFIED_PROP = METAMODEL_PREFIX + "unsatisfiedAssumption";
	public static final String CGMODEL_CG_CLASS = "http://aske.ge.com/compgraphmodel#ComputationalGraph";

	
	
	
	public static final String GENERICIOs = "prefix cg:<http://aske.ge.com/compgraphmodel#>\n" + 
			"prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" + 
			"prefix sci:<http://aske.ge.com/sciknow#>\n" + 
			"prefix list:<http://sadl.org/sadllistmodel#>\n" + 
			"prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
			"\n" + 
			"insert \n" + 
			"{ ?Eq imp:genericInput ?In. ?Eq imp:genericOutput ?Out. }\n" + 
			"where {\n" + 
			"  \n" + 
			" {?Eq imp:returnTypes ?AL1.\n" + 
			"  ?AL1 list:rest*/list:first ?AO1.\n" + 
			"  ?AO1 imp:augmentedType ?Type1.\n" + 
			"  ?Type1 imp:constraints ?CL1.\n" + 
			"  ?CL1 rdf:rest*/rdf:first ?C1.\n" + 
			"  ?C1 imp:gpPredicate ?P.\n" + 
			"  ?P rdfs:range ?Out. }\n" + 
			"\n" + 
			"  union {\n" + 
			"  ?Eq imp:returnTypes ?AL1.\n" + 
			"  ?AL1 list:rest*/list:first ?AO1.\n" + 
			"  ?AO1 imp:augmentedType ?Type1.\n" + 
			"  ?Type1 imp:semType ?Out. }\n" + 
			"\n" + 
			"  union {\n" + 
			"    ?Eq imp:implicitOutput/imp:augmentedType/imp:semType ?Out.\n" + 
			"    filter not exists{?Eq a imp:IntializerMethod} }\n" + 
			"\n" + 
			"  union{\n" + 
			"   ?Eq imp:arguments ?AL2.\n" + 
			"   ?AL2 list:rest*/list:first ?AO2.\n" + 
			"   ?AO2 imp:augmentedType ?Type2.\n" + 
			"   ?Type2 imp:constraints ?CL2.\n" + 
			"   ?CL2 rdf:rest*/rdf:first ?C2.\n" + 
			"   ?C2 imp:gpPredicate ?P.\n" + 
			"   ?P rdfs:range ?In." +
			"   ?In rdfs:subClassOf imp:UnittedQuantity.}\n" + 
			"  union{\n" + 
			"   ?Eq imp:arguments ?AL2.\n" + 
			"   ?AL2 list:rest*/list:first ?AO2.\n" + 
			"   ?AO2 imp:augmentedType ?Type2.\n" + 
			"   ?Type2 imp:semType ?In.}" +
//			"   ?In rdfs:subClassOf imp:UnittedQuantity.}\n" + 
			"\n" + 
			"  union { #Explicit inputs w/o AT\n" + 
			"    ?Eq imp:arguments ?AL2.\n" + 
			"    ?AL2 list:rest*/list:first ?AO2.\n" + 
			"    filter not exists {?AO2 imp:augmentedType []}\n" + 
			"    ?AO2 imp:localDescriptorName ?In.}\n" + 
			"\n" + 
			"  union {?Eq imp:implicitInput/imp:augmentedType/imp:semType ?In.\n" + 
			"   filter not exists{?Eq a imp:IntializerMethod} }\n" + 
			"\n" + 
			"  union {\n" + 
			"   ?Eq imp:implicitOutput/imp:localDescriptorName ?Out.\n" + 
			"   filter not exists{?Eq a imp:IntializerMethod} }\n" + 
			"  union {\n" + 
			"   ?Eq imp:implicitInput?IO.\n" + 
			"   filter not exists {?IO imp:augmentedType [] }\n" + 
			"   ?IO imp:localDescriptorName ?In.\n" + 
			"   filter not exists{?Eq a imp:IntializerMethod} } \n" + 
			"}";

	public static final String CHECK_GENERICIOs = "select distinct ?Eq ?In ?Out where { ?Eq <http://sadl.org/sadlimplicitmodel#genericInput> ?In. ?Eq <http://sadl.org/sadlimplicitmodel#genericOutput> ?Out.}order by ?Eq";
	
	public static final String DEPENDENCY_GRAPH_INSERT = "prefix cg:<http://aske.ge.com/compgraphmodel#>\n" +
		    "prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" +
			"insert {?EqCh cg:parent ?EqPa}\n" +
			"where {\n" + 
			"{\n" + 
			" ?EqCh imp:genericInput ?V.\n" + 
			" ?EqPa imp:genericOutput ?V.\n" + 
			"\n" + 
			"  filter( ?EqPa != ?EqCh)}\n" + 
			"  filter not exists {?EqCh imp:dependsOn ?EqPa}\n" + 
			"  filter not exists {?EqPa imp:dependsOn ?EqCh}\n" + 
			"  filter not exists {?EqCh imp:versionOf ?EqPa}\n" + 
			"  filter not exists {?EqPa imp:versionOf ?EqCh}\n" + 
			"\n" + 
			"  filter not exists {\n" + 
			"    ?EqCh imp:genericOutput ?V1.\n" + 
			"    ?EqPa imp:genericInput ?V1.\n" + 
			"    filter not exists {?EqPa imp:genericOutput ?V1.}\n" + 
			"  }\n" + 
			"}";
	
	public static final String CHECK_DEPENDENCY = "select distinct ?EqCh ?EqPa where { ?EqCh <http://aske.ge.com/compgraphmodel#parent> ?EqPa}";
	
	public static final String BUILD_COMP_GRAPH = "prefix hyper:<http://aske.ge.com/hypersonicsV2#>\n" + 
			"prefix imp:<http://sadl.org/sadlimplicitmodel#> \n" + 
			"prefix owl:<http://www.w3.org/2002/07/owl#> \n" + 
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>prefix sci:<http://aske.ge.com/sciknow#>\n" + 
			"prefix cg:<http://aske.ge.com/compgraphmodel#>\n" + 
			"prefix list:<http://sadl.org/sadllistmodel#>\n" +
			"\n" + 
			"select distinct ?Eq ?Out where {\n" + 
			"{select distinct ?EqOut ?EqIn ?Eq\n" + 
			"where {\n" + 
			"  ?EqOut imp:genericOutput ?Op.\n" + 
			"     filter (?Op in ( LISTOFOUTPUTS )).\n" + 
			"\n" + 
			"   ?EqIn imp:genericInput ?In.\n" + 
			"   filter (?In in ( LISTOFINPUTS )).\n" + 
			"\n" + 
			"    ?EqOut cg:parent* ?Eq. \n" + 
			"    ?Eq cg:parent* ?EqIn. \n" + 
			"}}\n" + 
			"  ?Eq imp:genericOutput ?Out. \n" + 
			"  filter not exists {?Eq imp:implicitOutput/imp:localDescriptorName ?Out} \n" + 
			"}";
	
	
	
	public static final String RETRIEVE_MODEL_EXPRESSIONS = "prefix hyper:<http://aske.ge.com/hypersonicsV2#>\n" + 
			"prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" + 
			"prefix sci:<http://aske.ge.com/sciknow#> \n" + 
			"prefix owl:<http://www.w3.org/2002/07/owl#> \n" + 
			"prefix cg:<http://aske.ge.com/compgraphmodel#>\n" + 
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"prefix list:<http://sadl.org/sadllistmodel#>\n" +
			"\n" + 
			"select distinct ?Model \n" + 
			"(str(?expr) as ?ModelForm) (str(?Fun) as ?Function) (str(?fname) as ?FunctionName)\n" + 
			"?Initializer ?Dependency\n" + 
			"where { \n" + 
			"\n" + 
			"  { ?Model a imp:Equation.\n" + 
			"         filter (?Model in ( EQNSLIST )) #EQNSLIST\n" + 
			"  }union \n" +
			"  {?Model a imp:ExternalEquation.\n" + 
			"         filter (?Model in ( EQNSLIST )) #EQNSLIST\n" + 
			"  } union \n" + 
			"  {select distinct ?Model ?Initializer ?Dependency\n" + 
			"      where {\n" + 
			"        {select ?Model ?Initializer where {\n" + 
			"           ?Model imp:initializes ?Class.\n" + 
			"           filter (?Class in ( CONTEXCLASSES )). #CONTEXCLASSES\n" + 
			"           bind(bound(?Model) as ?Initializer).\n" + 
			"        }}union\n" +
			"        {select ?Model ?Dependency where {\n" + 
			"           ?Eq imp:initializes ?Class.\n" + 
			"           filter (?Class in ( CONTEXCLASSES )). #CONTEXCLASSES\n" + 
			"           ?Eq imp:dependsOn ?Model.\n" + 
			"           bind(bound(?Model) as ?Dependency).\n" + 
			"        }}union\n" +
			"        {select ?Model ?Dependency  where {\n" + 
			"          ?Eq imp:dependsOn ?Model.\n" + 
			"          filter (?Eq in ( EQNSLIST )). #EQNSLIST\n" + 
			"          #bind(\"\" as ?Initializer)\n" + 
			"	  bind(bound(?Model) as ?Dependency)\n" + 
			"       }}union\n" + 
			"        {select distinct ?Model where {\n" + 
			"         ?EqCh cg:parent ?Model.\n" + 
			"         filter (?EqCh in (EQNSLIST)) #EQNSLIST\n" + 
			"         ?Model imp:genericOutput ?Out.\n" + 
			"         ?EqCh imp:genericInput ?Out.\n" + 
			"         filter not exists {?Out rdfs:subClassOf* imp:UnittedQuantity}\n" + 
			"      }}}\n" + 
			"  }\n" + 
			"  \n" + 
			"  optional{\n" + 
			"    ?Model imp:expression ?Scr.\n" + 
			"    ?Scr imp:script ?expr.\n" + 
			"    ?Scr imp:language ?lang.\n" + 
			"      filter ( ?lang = <SCRIPTLANGUAGE> )\n" + //http://sadl.org/sadlimplicitmodel#Python-NumPy
			"  }\n" + 
			"  optional {\n" + 
			"    ?Model imp:externalURI ?Fun.\n" + 
			"  }\n" + 
			"  optional{?Model rdfs:label ?fname.}\n" + 
			"}\n" + 
			"order by ?Model";
	public static final String RETRIEVE_MODELS_WEXP = "prefix hyper:<http://aske.ge.com/hypersonicsV2#>\n" + 
			"prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" + 
			"prefix sci:<http://aske.ge.com/sciknow#> \n" + 
			"prefix owl:<http://www.w3.org/2002/07/owl#> \n" + 
			"prefix cg:<http://aske.ge.com/compgraphmodel#>\n" + 
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"prefix list:<http://sadl.org/sadllistmodel#>\n" +
			"\n" + 
			"select distinct ?Model ?Input (str(?InLabel) as ?InputLabel) ?UniqueInputLabel \n" + 
			"?Output \n" + 
			"(str(?expr) as ?ModelForm) (str(?Fun) as ?Function) \n" + 
			"(str(?ImpIn) as ?ImpInput) ?ImpInputAugType (str(?InpD) as ?InpDeclaration)\n" + 
			"(str(?ImpOut) as ?ImpOutput) ?ImpOutputAugType (str(?OutpD) as?OutpDeclaration)\n" + 
			"?Initializer ?Dependency\n" + 
			"where { \n" + 
			"\n" + 
			"  { ?Model a imp:ExternalEquation.\n" + 
			"         filter (?Model in ( EQNSLIST )) #EQNSLIST\n" + 
			"  }union \n" + 
			"      {select distinct ?Model ?Initializer ?Dependency\n" + 
			"      where {\n" + 
			"        {select ?Model ?Initializer where {\n" + 
			"           ?Model imp:initializes ?Class.\n" + 
			"           filter (?Class in ( CONTEXCLASSES )). #CONTEXCLASSES\n" + 
			"           bind(bound(?Model) as ?Initializer).\n" + 
			"       }}union\n" + 
			"        {select ?Model ?Dependency where {\n" + 
			"           ?Eq imp:initializes ?Class.\n" + 
			"           filter (?Class in ( CONTEXCLASSES )). #CONTEXCLASSES\n" + 
			"           ?Eq imp:dependsOn ?Model.\n" + 
			"           bind(bound(?Model) as ?Dependency).\n" + 
			"        }}union\n" +
			"        {select ?Model ?Dependency where {\n" + 
			"          ?Eq imp:dependsOn ?Model.\n" + 
			"          filter (?Eq in ( EQNSLIST )). #EQNSLIST\n" + 
			"          #bind(\"\" as ?Initializer)\n" + 
			"          bind(bound(?Model) as ?Dependency)\n" + 
			"       }}union\n" + 
			"        {select distinct ?Model where {\n" + 
			"         ?EqCh cg:parent ?Model.\n" + 
			"         filter (?EqCh in (EQNSLIST)) #EQNSLIST\n" + 
			"         ?Model imp:genericOutput ?Out.\n" + 
			"         ?EqCh imp:genericInput ?Out.\n" + 
			"         filter not exists {?Out rdfs:subClassOf* imp:UnittedQuantity}\n" + 
			"      }}}\n" + 
			"   }\n" + 
			"  \n" + 
			"  optional{ #Explicit outputs. ?Output is a UQ or a label\n" + 
			"   ?Model imp:genericOutput ?Output.\n" + 
			"   filter not exists {?Model imp:implicitOutput/imp:localDescriptorName ?Output}\n" + 
			"   filter not exists {?Model imp:implicitOutput/imp:augmentedType/imp:semType ?Output.}}\n" + 
			"\n" + 
			"  optional{ #Explicit inputs. ?Input is a UQ or a label\n" + 
			"   ?Model imp:genericInput ?Input. #For explicit inputs w/o sem type, this is the label\n" + 
			"   filter not exists {?Model imp:implicitInput/imp:localDescriptorName ?Input.}\n" + 
			"   filter not exists {?Model imp:implicitInput/imp:augmentedType/imp:semType ?Input.}\n" + 
			"\n" + 
			"   optional {?Input imp:localDescriptorName ?InLabel.} #only applies to SemanticInputs\n" + 
			"   optional {?Input imp:descriptorVariable ?UniqueInputLabel.} \n" + 
			"   # Get the uniquelabel if available\n" + 
			"   #optional {?Model imp:arguments ?AL2.\n" + 
			"   #         ?AL2 list:rest*/list:first ?AO2.\n" + 
			"   #         ?AO2 imp:localDescriptorName ?Input.\n" + 
			"   #         ?AO2 imp:descriptorVariable  ?UniqueInputLabel.} #are we using these?\n" + 
			"   }\n" + 
			"  \n" + 
			"  optional{\n" + 
			"    ?Model imp:expression ?Scr.\n" + 
			"    ?Scr imp:script ?expr.\n" + 
			"    ?Scr imp:language ?lang.\n" + 
			"      filter ( ?lang = <http://sadl.org/sadlimplicitmodel#Python-NumPy> )\n" + 
			"  }\n" + 
			"  optional {\n" + 
			"    ?Model imp:externalURI ?Fun.\n" + 
			"  }\n" + 
			"\n" + 
			"  #Get implicit IOs that have augmentedTypes\n" + 
			"  optional{\n" + 
			"   ?Model imp:implicitInput ?II.\n" + 
			"   ?II imp:localDescriptorName ?ImpIn.\n" + 
			"   #optional{?II imp:descriptorVariable  ?UniqueImpInputLabel.} #These are not being added to the extracted models\n" + 
			"   optional{?II imp:augmentedType ?IT. ?IT imp:semType ?ImpInputAugType.}\n" + 
			"   optional{?II imp:declaration ?ID. ?ID imp:language imp:Python. ?ID imp:script ?InpD}}\n" + 
			"\n" + 
			"  optional{\n" + 
			"   ?Model imp:implicitOutput ?IO.\n" + 
			"   ?IO imp:localDescriptorName ?ImpOut.\n" + 
			"   optional{?IO imp:augmentedType ?OT. ?OT imp:semType ?ImpOutputAugType.}\n" + 
			"   optional{?IO imp:declaration ?OD. ?OD imp:language imp:Python. ?OD imp:script ?OutpD}}\n" + 
			"  \n" + 
			"}order by ?Model";
	
	public static final String RETRIEVE_MODELS = "prefix hyper:<http://aske.ge.com/hypersonicsV2#>\n" + 
			"prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" + 
			"prefix sci:<http://aske.ge.com/sciknow#> \n" + 
			"prefix owl:<http://www.w3.org/2002/07/owl#> \n" + 
			"prefix cg:<http://aske.ge.com/compgraphmodel#>\n" + 
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"prefix list:<http://sadl.org/sadllistmodel#>\n" +
			"\n" + 
			"select distinct ?Model ?Input (str(?InLabel) as ?InputLabel) ?UniqueInputLabel \n" + 
			"?Output ?OutputLabel\n" + 
//			"(str(?expr) as ?ModelForm) (str(?Fun) as ?Function) \n" + 
			"(str(?ImpIn) as ?ImpInput) ?ImpInputAugType (str(?InpD) as ?InpDeclaration)\n" + 
			"(str(?ImpOut) as ?ImpOutput) (strafter(str(?DT),'#') as ?DataType) ?ImpOutputAugType (str(?OutpD) as?OutpDeclaration)\n" + 
			"?Initializer ?Dependency\n" + 
			"where { \n" + 
			"\n" + 
			"  { ?Model a imp:Equation.\n" + 
			"         filter (?Model in ( EQNSLIST )) #EQNSLIST\n" + 
			"  }union \n" + 
			"  { ?Model a imp:ExternalEquation.\n" + 
			"         filter (?Model in ( EQNSLIST )) #EQNSLIST\n" + 
			"  }union \n" + 
			"      {select distinct ?Model ?Initializer ?Dependency\n" + 
			"      where {\n" + 
			"        {select ?Model ?Initializer where {\n" + 
			"           ?Model imp:initializes ?Class.\n" + 
			"           filter (?Class in ( CONTEXCLASSES )). #CONTEXCLASSES\n" + 
			"           bind(bound(?Model) as ?Initializer).\n" + 
			"       }}union\n" + 
			"        {select ?Model ?Dependency where {\n" + 
			"           ?Eq imp:initializes ?Class.\n" + 
			"           filter (?Class in ( CONTEXCLASSES )). #CONTEXCLASSES\n" + 
			"           ?Eq imp:dependsOn ?Model.\n" + 
			"           bind(bound(?Model) as ?Dependency).\n" + 
			"        }}union\n" +			"        {select ?Model ?Dependency where {\n" + 
			"          ?Eq imp:dependsOn ?Model.\n" + 
			"          filter (?Eq in ( EQNSLIST )). #EQNSLIST\n" + 
			"          #bind(\"\" as ?Initializer)\n" + 
			"          bind(bound(?Model) as ?Dependency)\n" + 
			"       }}union\n" + 
			"        {select distinct ?Model where {\n" + 
			"         ?EqCh cg:parent ?Model.\n" + 
			"         filter (?EqCh in (EQNSLIST)) #EQNSLIST\n" + 
			"         ?Model imp:genericOutput ?Out.\n" + 
			"         ?EqCh imp:genericInput ?Out.\n" + 
			"         filter not exists {?Out rdfs:subClassOf* imp:UnittedQuantity}\n" + 
			"      }}}\n" + 
			"   }\n" + 
			"  \n" + 
			"  optional{ #Explicit outputs. ?Output is a UQ or a label\n" + 
			"   ?Model imp:genericOutput ?Output.\n" + 
			"   optional{?Model rdfs:label ?OutputLabel}\n" + 
			"   filter not exists {?Model imp:implicitOutput/imp:localDescriptorName ?Output}\n" + 
			"   filter not exists {?Model imp:implicitOutput/imp:augmentedType/imp:semType ?Output.}}\n" + 
			"\n" + 
			"  optional{ #Explicit inputs. ?Input is a UQ or a label\n" + 
			"   ?Model imp:genericInput ?Input. #For explicit inputs w/o sem type, this is the label\n" + 
			"   filter not exists {?Model imp:implicitInput/imp:localDescriptorName ?Input.}\n" + 
			"   filter not exists {?Model imp:implicitInput/imp:augmentedType/imp:semType ?Input.}\n" + 
			"\n" + 
//			"   optional {?Input imp:localDescriptorName ?InLabel.} #only applies to SemanticInputs\n" + 
//			"   optional {?Input imp:descriptorVariable ?UniqueInputLabel.} \n" + 
			"   # Get variable label for augmented type inputs\n" + 
			"   optional {?Model imp:arguments/list:rest*/list:first ?AO2.\n" +
			"            ?AO2 imp:localDescriptorName ?InLabel.\n" + 
			"            ?AO2 imp:descriptorVariable  ?UniqueInputLabel.\n" + 
            "            ?AO2 imp:augmentedType/imp:constraints/rdf:rest*/rdf:first/imp:gpPredicate/rdfs:range ?Input.\n" +
			"            ?Input rdfs:subClassOf imp:UnittedQuantity.}\n" +
			"   optional {?Model imp:arguments/list:rest*/list:first ?AO2.\n" +
			"            ?AO2 imp:localDescriptorName ?InLabel.\n" + 
			"            ?AO2 imp:descriptorVariable  ?UniqueInputLabel.\n" + 
            "            ?AO2 imp:augmentedType/imp:semType ?Input.}\n" +
//			"            ?Input rdfs:subClassOf imp:UnittedQuantity.}\n" +
            "   # Get the uniquelabel if available\n" + 
			"   optional {?Model imp:arguments ?AL2.\n" + 
			"            ?AL2 list:rest*/list:first ?AO2.\n" + 
			"            ?AO2 imp:localDescriptorName ?Input.\n" + 
			"            ?AO2 imp:descriptorVariable  ?UniqueInputLabel.} #are we using these?\n" + 
			"   }\n" + 
			"  \n" + 
			"  #Get implicit IOs that have augmentedTypes\n" + 
			"  optional{\n" + 
			"   ?Model imp:implicitInput ?II.\n" + 
			"   ?II imp:localDescriptorName ?ImpIn.\n" + 
			"   #optional{?II imp:descriptorVariable  ?UniqueImpInputLabel.} #These are not being added to the extracted models\n" + 
			"   optional{?II imp:augmentedType ?IT. ?IT imp:semType ?ImpInputAugType.}\n" + 
			"   optional{?II imp:declaration ?ID. ?ID imp:language imp:Python-NumPy. ?ID imp:script ?InpD}}\n" + 
			"\n" + 
			"  optional{\n" + 
			"   ?Model imp:implicitOutput ?IO.\n" + 
			"   ?IO imp:localDescriptorName ?ImpOut.\n" + 
			"   optional{?IO imp:dataType ?DT.}\n" + 
			"   optional{?IO imp:augmentedType ?OT. ?OT imp:semType ?ImpOutputAugType.}\n" + 
			"   optional{?IO imp:declaration ?OD. ?OD imp:language imp:Python-NumPy. ?OD imp:script ?OutpD}}\n" + 
			"  \n" + 
			"}order by ?Model";
	
	public static final String RETRIEVE_NODES = "prefix hyper:<http://aske.ge.com/hypersonicsV2#>\n" + 
			"prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" + 
			"prefix sci:<http://aske.ge.com/sciknow#>\n" + 
			"prefix mm:<http://aske.ge.com/metamodel#>\n" + 
			"prefix cg:<http://aske.ge.com/compgraphmodel#>\n" + 
			"prefix owl:<http://www.w3.org/2002/07/owl#>\n" + 
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n"+
			"prefix list:<http://sadl.org/sadllistmodel#>\n" +
			"\n" + 
			"select distinct ?Node ?DataType (str(?NUnits) as ?NodeOutputUnits) ?Child (str(?CUnits) as ?ChildInputUnits) ?Eq ?InlineEq ?Value ?Lower ?Upper #?Distribution \n" + 
			"where {\n" + 
			"{select distinct ?Node ?Child ?CUnits ?Eq ?Lower ?Upper \n" + 
			"where { \n" + 
			"   {?Eq imp:genericInput ?Node.\n" + 
			"     filter not exists {?Model imp:implicitInput/imp:localDescriptorName ?Node.}\n" + 
			"     filter not exists {?Model imp:implicitInput/imp:augmentedType/imp:semType ?Node.}\n" + 
			"     optional{\n" + 
			"      ?Eq imp:arguments ?EI1.\n" + 
			"      ?EI1 rdf:rest*/rdf:first ?EI2.\n" + 
			"      ?EI2 imp:specifiedUnits/rdf:first ?CUnits.}\n" + 
			"   }union {\n" + 
			"    ?Eq imp:implicitInput ?II. \n" + 
			"    ?II imp:augmentedType/imp:semType ?Node.\n" + 
			"    optional {?II imp:specifiedUnits/list:first ?CUnits.}\n" +
			"    optional {?II imp:minValue ?Lower}\n" + 
			"    optional {?II imp:maxValue ?Upper}\n" +
			"   }\n" + 
			"     \n" + 
			"     # This is an input node, it's units come from the query\n" + 
			"     # Get NUnits from current model via the property (?Node)\n" + 
			"    #?Q mm:execution/mm:compGraph ?CG. \n" + 
			"    #filter (?CG in (COMPGRAPH)).\n" + 
			"    #optional{\n" + 
			"    #   ?Q mm:cgInput ?UQNode.\n" + 
			"    #   ?Var ?Node ?UQNode.\n" + 
			"    #   ?UQNode imp:unit ?InputUnitsQuery.\n" + 
			"    #}\n" + 
			"   \n" + 
			"   {?Eq imp:genericOutput ?Child.\n" + 
			"     filter not exists {?Eq imp:implicitOutput/imp:localDescriptorName ?Child.}\n" + 
			"     filter not exists {?Eq imp:implicitOutput/imp:augmentedType/imp:semType ?Child.}\n" + 
			"   }union {\n" + 
			"    ?Eq imp:implicitOutput ?IO. \n" + 
			"    ?IO imp:augmentedType/imp:semType ?Child.\n" + 
			"   }\n" + 
			"\n" + 
			" filter (?Eq in (EQNSLIST)) . #EQNSLIST\n" + 
			"\n" + 
			"  }} \n" + 
			"  union {\n" + 
			"  select distinct ?Node  ?Child ?CUnits ?Eq ?Lower ?Upper #?NUnits\n" + 
			"where { \n" + 
			"   {?Eq imp:genericInput ?Node.\n" + 
			"     filter not exists {?Model imp:implicitInput/imp:localDescriptorName ?Node.}\n" + 
			"     filter not exists {?Model imp:implicitInput/imp:augmentedType/imp:semType ?Node.}\n" + 
			"     optional{\n" + 
			"      ?Eq imp:arguments ?EI1.\n" + 
			"      ?EI1 rdf:rest*/rdf:first ?EI2.\n" + 
			"      ?EI2 imp:specifiedUnits/rdf:first ?CUnits.}\n" + 
			"   }union {\n" + 
			"    ?Eq imp:implicitInput ?II. \n" + 
			"    ?II imp:augmentedType/imp:semType ?Node.\n" + 
			"    optional {?II imp:specifiedUnits/list:first ?CUnits.}\n" + 
			"    optional {?II imp:minValue ?Lower}\n" + 
			"    optional {?II imp:maxValue ?Upper}\n" +			"   }\n" + 
			"     \n" + 
			"   \n" + 
			"   {?Eq imp:genericOutput ?Child.\n" + 
			"     filter not exists {?Model imp:implicitOutput/imp:localDescriptorName ?Child.}\n" + 
			"     filter not exists {?Model imp:implicitOutput/imp:augmentedType/imp:semType ?Child.}\n" + 
			"   }union {\n" + 
			"    ?Eq imp:implicitOutput ?IO. \n" + 
			"    ?IO imp:augmentedType/imp:semType ?Child.\n" + 
			"   }\n" + 
			"\n" + 
			"  #TODO: get the NUnits\n" + 
			"\n" + 
			"\n" + 
			" filter (?Eq in (EQNSLIST )) . #EQNSLIST\n" + 
			"   \n" + 
			" }} \n" + 
			"  union {\n" + 
			"  select distinct ?Eq ?Node (strafter(str(?DT),'#') as ?DataType) ?NUnits #?CUnits\n" + 
			"where { \n" + 
			"   \n" + 
			"   {?Eq imp:genericOutput ?Node.\n" + 
			"     filter not exists {?Eq imp:implicitOutput/imp:localDescriptorName ?Node.}\n" + 
			"     filter not exists {?Eq imp:implicitOutput/imp:augmentedType/imp:semType ?Node.}\n" + 
			"     optional{\n" + 
			"      ?Eq imp:arguments ?EI1.\n" + 
			"      ?EI1 rdf:rest*/rdf:first ?EI2.\n" + 
			"      ?EI2 imp:specifiedUnits/rdf:first ?NUnits.}\n" + 
			"   }union {\n" + 
			"    ?Eq imp:implicitOutput ?IO. \n" + 
			"    ?IO imp:augmentedType/imp:semType ?Node.\n" + 
			"    optional {?IO imp:specifiedUnits/list:first ?NUnits.}\n" +
			"    ?IO imp:dataType ?DT.\n" + 
			"   }\n" + 
			"\n" + 
			"  filter not exists {\n" + 
			"    {?Eq1 imp:genericInput ?Node.}\n" + 
			"    union {?Eq imp:implicitInput/imp:augmentedType/imp:semType ?Node.  }\n" + 
			"    filter (?Eq1 in (EQNSLIST )) #EQNSLIST\n" + 
			"  }\n" + 
			"\n" + 
			"  #TODO: get the CUnits\n" + 
			"\n" + 
			"\n" + 
			" filter (?Eq in (EQNSLIST )) . #EQNSLIST\n" + 
			" \n" + 
			"}}\n" +
			// This option is needed for inline equations
//			"   optional { ?Eq a imp:Equation. \n" +
//			"    ?Eq imp:expression ?Scr.\n" + 
//			"    ?Scr imp:script ?InlineEq.\n" + 
//			"    ?Scr imp:language ?lang.\n" + 
//			"      filter ( ?lang = <SCRIPTLANGUAGE> ) }\n" + //http://sadl.org/sadlimplicitmodel#Python-NumPy
			"   ?Q mm:execution/mm:compGraph ?CG.\n" + 
			"   filter (?CG in (COMPGRAPH)).\n" + 
			"  optional{\n" + 
			"    ?Q mm:cgInput ?Inp.\n" + 
			"    ?Inp a ?Node.\n" + 
			"    ?Inp imp:value ?Value.}  \n" + 
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
			"prefix cg:<http://aske.ge.com/compgraphmodel#>\n" + 
			"prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"prefix list:<http://sadl.org/sadllistmodel#>\n" +
			"\n" + 
			"select distinct (strafter(str(?CCG),'#') as ?Model) ?X ?Y ?Z ?X_style ?X_color ?Z_shape ?Z_tooltip\n" + 
			"where {\n" + 
			"{select ?CCG (?Input as ?X) (?EQ as ?Y) (?Output as ?Z) ?X_style ?X_color ('box' as ?Z_shape) ?Z_tooltip\n" + 
			"where {\n" + 
			"    ?CCG a mm:CCG.\n" + 
			"    filter (?CCG in (COMPGRAPH)).\n" + 
			"    ?CCG mm:subgraph ?SG.\n" + 
			"    ?SG mm:cgraph ?CG.\n" + 
			"    ?CG cg:hasEquation ?EQ.\n" + 
			"\n" + 
			"  optional{ \n" + 
			"   ?EQ imp:genericInput ?Input. \n" + 
			"   filter not exists {?EQ imp:implicitInput/imp:localDescriptorName ?Input.}\n" + 
			"   filter not exists {?EQ imp:implicitInput/imp:augmentedType/imp:semType ?Input.} }\n" + 
			"  optional{?EQ imp:implicitInput/imp:augmentedType/imp:semType ?Input.}\n" + 
			"\n" + 
			"  optional{ \n" + 
			"   ?EQ imp:genericOutput ?Output.\n" + 
			"   filter not exists {?EQ imp:implicitOutput/imp:localDescriptorName ?Output}\n" + 
			"   filter not exists {?EQ imp:implicitOutput/imp:augmentedType/imp:semType ?Output.}}\n" + 
			"\n" + 
			"   optional{?EQ imp:implicitOutput/imp:augmentedType/imp:semType ?Output.}\n" + 
			"\n" + 
			"\n" + 
			"# There's an eqn that outputs EQ's input (a parent Eqn)\n" + 
			"    ?CCG mm:subgraph ?SG1.\n" + 
			"    ?SG1 mm:cgraph ?CG1.\n" + 
			"    ?CG1 cg:hasEquation ?EQ1.\n" + 
			"    ?EQ1 imp:genericOutput ?Input.\n" + 
			"\n" + 
			"    optional{ \n" + 
			"      ?EQ a imp:Equation. #only get the script if not an ExternalEquation\n" + 
			"      ?EQ imp:expression ?Scr.\n" + 
			"      ?Scr imp:script ?Expr.\n" + 
			"      ?Scr imp:language ?lang.\n" + 
			"         filter ( ?lang = <http://sadl.org/sadlimplicitmodel#Python> )\n" + 
			"      bind(str(?Expr) as ?Z_tooltip)\n" + 
			"    }\n" + 
			"     bind('solid' as ?X_style)\n" + 
			"    bind('black' as ?X_color)\n" + 
			"}}union\n" + 
			"{select ?CCG (?Input as ?X) (?EQ as ?Y) (?Output as ?Z) ?X_style ?X_color ('box' as ?Z_shape) ?Z_tooltip\n" + 
			"where {\n" + 
			"    ?CCG a mm:CCG.\n" + 
			"    filter (?CCG in (COMPGRAPH)).\n" + 
			"    ?CCG mm:subgraph ?SG.\n" + 
			"    ?SG mm:cgraph ?CG.\n" + 
			"    ?CG cg:hasEquation ?EQ.\n" + 
			"\n" + 
			"    ?CCG a mm:CCG.\n" + 
			"    filter (?CCG in (COMPGRAPH)).\n" + 
			"    ?CCG mm:subgraph ?SG.\n" + 
			"    ?SG mm:cgraph ?CG.\n" + 
			"    ?CG cg:hasEquation ?EQ.\n" + 
			"\n" + 
			"  optional{ \n" + 
			"   ?EQ imp:genericInput ?Input. \n" + 
			"   filter not exists {?EQ imp:implicitInput/imp:localDescriptorName ?Input.}\n" + 
			"   filter not exists {?EQ imp:implicitInput/imp:augmentedType/imp:semType ?Input.} \n" + 
			"  }\n" + 
			"  optional{?EQ imp:implicitInput/imp:augmentedType/imp:semType ?Input.}\n" + 
			"\n" + 
			" optional{ \n" + 
			"   ?EQ imp:genericOutput ?Output.\n" + 
			"   filter not exists {?EQ imp:implicitOutput/imp:localDescriptorName ?Output}\n" + 
			"   filter not exists {?EQ imp:implicitOutput/imp:augmentedType/imp:semType ?Output.}}\n" + 
			"\n" + 
			"   optional{?EQ imp:implicitOutput/imp:augmentedType/imp:semType ?Output.}\n" + 
			"\n" + 
			"  filter not exists { # EQ does not have a parent EQn\n" + 
			"    ?CCG mm:subgraph ?SG1.\n" + 
			"    ?SG1 mm:cgraph ?CG1.\n" + 
			"    ?CG1 cg:hasEquation ?EQ1.\n" + 
			"    ?EQ1 imp:genericOutput ?Input.}\n" + 
			"\n" + 
			"  optional{\n" + 
			"    ?EQ a imp:Equation. \n" + 
			"    ?EQ imp:expression ?Scr.\n" + 
			"    ?Scr imp:script ?Expr.\n" + 
			"    ?Scr imp:language ?lang.\n" + 
			"       filter ( ?lang = <http://sadl.org/sadlimplicitmodel#Python> )\n" + 
			"     bind(str(?Expr) as ?Z_tooltip) }\n" + 
			"     \n" + 
			"    bind('filled' as ?X_style)\n" + 
			"    bind('yellow' as ?X_color)\n" + 
			"}}union\n" + 
			" {select ?CCG (?Output as ?X) ?Y (if(strbefore(strafter(?Value,'['),'.')='',strbefore(strafter(?Value,'['),']'), concat(concat(strbefore(strafter(?Value,'['),'.'),'.'),substr(strafter(?Value,'.'),1,3))) as ?Z) ?X_style ?X_color ('oval' as ?Z_shape) ('output value' as ?Z_tooltip)\n" + 
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
			"}";

	public static final String RESULTSQUERY = "prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" + 
			"prefix mm:<http://aske.ge.com/metamodel#>\n" +
			"prefix sci:<http://aske.ge.com/sciknow#>\n" +
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"select distinct (strafter(str(?CCG),'#') as ?Model) (strafter(str(?C),'#') as ?Class) (strafter(str(?Var),'#') as ?Variable) ?Mean ?StdDev ?Units\n" + 
			"where {\n" + 
//			"   {?CCG mm:subgraph ?SG.\n" + 
//			"    filter (?CCG in (COMPGRAPH)).\n" +
//			"    ?SG mm:output ?Oinst.\n" + 
//			"  } union {\n" +
			"   filter (?CCG in (COMPGRAPH)).\n" +
			"   ?CE mm:compGraph ?CCG.\n" + 
			"   ?CE mm:output ?Oinst.\n" + 
//			"  }\n" +
			"    ?Oinst a ?Var.\n" + 
			"    ?Oinst imp:value ?Mean.\n" + 
			"    optional{?Oinst imp:stddev ?StdDev.}\n" +
			"    optional{?Oinst imp:unit ?Units}\n" +
			"   ?CGQ mm:execution/mm:compGraph ?CCG.\n" + 
			"   ?CGQ mm:cgInput ?IVar.\n" + 
			"   ?Obj ?prop ?IVar.\n" + 
			"   filter (?prop not in (mm:cgInput))\n" + 
			"   ?Obj rdf:type ?C.\n" +
			"   ?C rdfs:subClassOf* ?CP.\n" + 
			"   ?p rdfs:domain ?CP.\n" + 
			"   ?p rdfs:range ?Var.\n" +
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

	private static final String SENSITIVITYQUERY = "prefix mm:<http://aske.ge.com/metamodel#>\n" + 
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"\n" + 
			"select distinct (strafter(str(?O),'#') as ?Output) (strafter(str(?I),'#') as ?Input) ?Sensitivity\n" + 
			"where { \n" + 
			" ?CG mm:sensitivity ?SS.\n" + 
			"     filter (?CG in ( COMPGRAPH )) \n" + 
			" ?SS mm:output ?O.\n" + 
			" ?SS mm:inputSensitivity ?Sy.\n" + 
			" ?Sy mm:cgInput ?I.\n" + 
			" ?Sy mm:sensitivityValue ?Sensitivity\n" + 
			" } ";

	private static final String DEPENDENCY_GRAPH = "prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" + 
			"\n" + 
			"select distinct ?EqPa ?V ?EqCh ('oval' as ?EqPa_shape) ('oval' as ?EqCh_shape) \n" + // 
			"where {\n" + 
//			"{select distinct ?EqPa  ?EqCh ?V where {\n" + 
//			" ?EqCh imp:genericInput ?V.\n" + 
//			" ?EqPa imp:genericOutput ?V.\n" + 
//			"\n" + 
//			"  filter( ?EqPa != ?EqCh)\n" + 
//			"  filter not exists {?EqCh imp:dependsOn ?EqPa}\n" + 
//			"  filter not exists {?EqPa imp:dependsOn ?EqCh}\n" + 
//			"  filter not exists {?EqCh imp:versionOf ?EqPa}\n" + 
//			"  filter not exists {?EqPa imp:versionOf ?EqCh}\n" + 
//			"\n" + 
//			"  filter not exists {\n" + 
//			"    ?EqCh imp:genericOutput ?V1.\n" + 
//			"    ?EqPa imp:genericInput ?V1.\n" + 
//			"    filter not exists {?EqPa imp:genericOutput ?V1.}\n" + 
//			"  }\n" + 
////			"} group by ?EqPa ?EqCh ?V \n" + 
//			"} having (count(distinct ?V)<5)\n" + 
//			"} union\n" + 
			"{select distinct ?EqPa ?EqCh  \n" + //(sample(?Var) as ?V) 
			"where {\n" + 
			" ?EqCh imp:genericInput ?Var.\n" + 
			" ?EqPa imp:genericOutput ?Var.\n" + 
			"\n" + 
			"  filter( ?EqPa != ?EqCh)\n" + 
//			"  filter not exists {?EqCh imp:dependsOn ?EqPa}\n" + 
//			"  filter not exists {?EqPa imp:dependsOn ?EqCh}\n" + 
			"  filter not exists {?EqCh imp:versionOf ?EqPa}\n" + 
			"  filter not exists {?EqPa imp:versionOf ?EqCh}\n" + 
			"\n" + 
			"  filter not exists {\n" + 
			"    ?EqCh imp:genericOutput ?V1.\n" + 
			"    ?EqPa imp:genericInput ?V1.\n" + 
			"    filter not exists {?EqPa imp:genericOutput ?V1.}\n" + 
			"  }\n" + 
			" }group by ?EqPa ?EqCh \n" +
			" }union \n" + 
			" { select ?EqCh ?EqPa where{\n" + 
			"  ?EqCh imp:dependsOn ?EqPa.\n" + 
			" }}\n " + 
			"}";
	
	
	private static final String TRENDSQUERY = "prefix mm:<http://aske.ge.com/metamodel#>\n" + 
			"prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"select distinct (strafter(str(?C),'#') as ?Class) (strafter(str(?SIn),'#') as ?Input) (strafter(str(?Trnd),'#') as ?Trend) (strafter(str(?SOut),'#') as ?Output) (strafter(str(?Loc),'#') as ?Location)\n" + 
			"where {\n" + 
			"   filter (?CCG in (COMPGRAPH)). #<http://aske.ge.com/Model_Q_1583352078126#CG_1583352078156>\n" +  
			"  ?CCG mm:sensitivity ?SS.\n" + 
			"  ?SS mm:trendEffect ?TE.\n" + 
			"  ?TE mm:cgInput ?SIn.\n" + 
			"  ?TE mm:trend ?Trnd.\n" +
			"  optional {?TE mm:locationWRTquery ?Loc.}\n" +
			"  ?SS mm:output ?SOut.\n\n" + 
			"  ?CGQ mm:execution/mm:compGraph ?CCG.\n" + 
			"  ?CGQ mm:cgInput ?IVar.\n" + 
			"  ?Obj ?prop ?IVar.\n" + 
			"  filter (?prop not in (mm:cgInput))\n" + 
			"  ?Obj rdf:type ?C.\n" + 
			"  ?C rdfs:subClassOf* ?CP.\n" + 
			"  ?prop rdfs:domain ?CP.\n" + 
			"  ?prop rdfs:range ?SIn.\n"+
			"} order by ?Output";


	
	boolean useDbn;
	boolean useKC; 

	@Override
	public boolean isSupported(String fileExtension) {
		return "dialog".equals(fileExtension);
	}

	@Override
	public Object[] insertRulesAndQuery(Resource resource, List<Rule> rules) throws SadlInferenceException {
		List<TripleElement[]> triples = new ArrayList<TripleElement[]>();
		for (Rule rule : rules) {
			int size = (rule.getGivens() != null ? rule.getGivens().size() : 0) +
					   (rule.getIfs() != null ? rule.getIfs().size() : 0) +
					   (rule.getThens() != null ? rule.getThens().size() : 0);
			TripleElement[] thisRulesTriples = new TripleElement[size];
			int idx = 0;
			for (int i = 0; rule.getGivens() != null && i < rule.getGivens().size(); i++) {
				if (rule.getGivens().get(i) instanceof TripleElement) {
					thisRulesTriples[idx++] = (TripleElement) rule.getGivens().get(i);
				}
				else {
					throw new SadlInferenceException("insertTriplesAndQuery only handles TripleElements currently");
				}
			}
			for (int i = 0; rule.getIfs() != null && i < rule.getIfs().size(); i++) {
				if (rule.getIfs().get(i) instanceof TripleElement) {
					thisRulesTriples[idx++] = (TripleElement) rule.getIfs().get(i);
				}
				else {
					throw new SadlInferenceException("insertTriplesAndQuery only handles TripleElements currently");
				}
			}
			for (int i = 0; rule.getThens() != null && i < rule.getThens().size(); i++) {
				if (rule.getThens().get(i) instanceof TripleElement) {
					thisRulesTriples[idx++] = (TripleElement) rule.getThens().get(i);
				}
				else {
					throw new SadlInferenceException("insertTriplesAndQuery only handles TripleElements currently");
				}
			}
			triples.add(thisRulesTriples);
		}
		try {
			return insertTriplesAndQuery(resource, rules, triples);
		}
		catch (Throwable t) {
			throw new SadlInferenceException(t.getMessage(), t);
		}
	}
	
	/**
	 * Returns a ResultSet containing one answer for each simple query. Each answer consists of three ResultSet subsets appearing in sequence:
	 * 1) data for the model diagram
	 * 2) the actual numeric answer + the sensitivity URL
	 * 3) triples corresponding to increasing/decreasing trend insights
	 * @throws Exception 
	 * @throws ConfigurationException 
	 * @throws URISyntaxException 
	 * @throws IOException 
	 * @throws TranslationException 
	 */
	
//	@Override
//	public Object[] insertTriplesAndQuery(Resource resource, List<TripleElement[]> triples) throws SadlInferenceException {
	public Object[] insertTriplesAndQuery(Resource resource, List<Rule> rules, List<TripleElement[]> triples) throws TranslationException, IOException, URISyntaxException, ConfigurationException, Exception {
 		List<Object[]> combinedResults = new ArrayList<Object[]>(triples.size());
 		Object[] results = null;
//	    JsonArray sensitivityJsonList = new JsonArray();
		Map<String, Map<String,List<String>>> insightsMap = new HashMap<String, Map<String,List<String>>>();

 		setCurrentResource(resource);
		setModelFolderPath(getModelFolderPath(resource));
		setModelName(OntModelProvider.getModelName(resource));
		setTheJenaModel(OntModelProvider.find(resource));
//		getTheJenaModel().write(System.err); 
		long startTime;
		
		if(debugMode) {
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = sdf.format(Calendar.getInstance().getTime());
			System.out.println("InsertTriplesAndQuery " + time);
			startTime = System.currentTimeMillis();
		}

		useDbn = useDbn();
		useKC = useKChain();
		if (!useDbn && !useKC) {
			System.out.println("Query answering disabled as no Computational Graph is selected in preferences.");
			return null;
		}
		
//		// TODO this is temporary for testing only
//		if (useDbn) {
//			TripleElement[] trl0 = triples.get(0);
//			int lastTr = trl0.length - 1;
//			TripleElement tr0 = trl0[lastTr];
//			NamedNode nn = (NamedNode) tr0.getPredicate();
//			throw new NoModelFoundForTargetException("No model found for " + nn.getName() + ".", nn);
//		}

		//		System.out.println(" >> Builtin classes discovered by the service loader:");
//		Iterator<Builtin> iter = ServiceLoader.load(Builtin.class).iterator();
//		while (iter.hasNext()) {
//			Builtin service = iter.next();
//			System.out.println(service.getClass().getCanonicalName());
//		}
		
		
//		try {
//			System.out.println(System.getProperty("user.dir"));
//			String q = getFileContents("genericIOs");
//		} catch (Exception e1) {
//			e1.printStackTrace();
//		}

		
		
//		boolean useDbn = useDbn();
//		boolean useKC = useKChain();
//		if (useKC) {
//			if (commonSubject(triples.get(0)) && allPredicatesAreProperties(triples.get(0))) {
//				Object[] jbsipResult = super.insertTriplesAndQuery(resource, triples);
//				results = jbsipResult;
//			}
//			else {
//				results = new Object[1];
//				results[0] = "KChain requires that all triples have the same subject and that all predicates are properties";
//			}
//		}

		//if (useDbn) {
			
//			//Create a model for this query [don't need it?]
//			OntModel queryModel;
//			String queryKey = null;
//			String queryGraphNamePrefix = "http://aske.ge.com/";
//			String queryOwlFileName = null;
//			String queryOwlFilePath = getModelFolderPath(resource) + File.separator;  //+ qhOwlFileName;
	//	
//			queryKey = cgq.getLocalName();
//			queryModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
//			OntModelProvider.addPrivateKeyValuePair(resource, queryKey, queryModel);


		String kgsDirectory = null;
		if (ResourceManager.isSyntheticUri(null,resource.getURI())) {
			File projectRoot = new File("resources/ASKE_P2");
			try {
				kgsDirectory = projectRoot.getCanonicalPath() + File.separator + CGMODELS_FOLDER;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		else {
			kgsDirectory = new File(getModelFolderPath(resource)).getParent() + File.separator  + CGMODELS_FOLDER;
		}
		if (kgsDirectory == null) {
			throw new SadlInferenceException("Unable to create folder for CG models");
		}
		new File(kgsDirectory).mkdir();
//		
//		 queryModelFileName = "Q_" + System.currentTimeMillis();
//		 queryModelURI = "http://aske.ge.com/" + "Model_" + queryModelFileName;
//		 queryModelPrefix = queryModelURI + "#";
//		//String queryInstanceName = METAMODEL_PREFIX + queryModelFileName;
//		 queryInstanceName = queryModelPrefix + queryModelFileName;
//		 queryOwlFileWithPath = kgsDirectory + File.separator + queryModelFileName + ".owl";
//
//		
//		
//		//ConfigurationManagerForIDE cmgr = null;
//		//Object qhModelObj = OntModelProvider.getPrivateKeyValuePair(resource, queryHistoryKey);
//		
//		File f = new File(queryOwlFileWithPath);
//		if (f.exists() && !f.isDirectory()) {
//			throw new SadlInferenceException("Query model file already exists");
//		}
//		else {
//			queryModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
//		}
//		
//		OntModelProvider.addPrivateKeyValuePair(resource, queryModelFileName, queryModel);
		
		try {
			inferDependencyGraph(resource);
		}catch (Exception e) {
			// TODO: handle exception
		}

		int numOfQueries = triples.size();
		
		for (int i=0 ; i<triples.size(); i++) {
//			results = processSingleWhatWhenQuery(resource, triples.get(i), useDbn, useKC, queryModelFileName, queryModelURI,queryModelPrefix, queryInstanceName, queryOwlFileWithPath);
			results = processSingleWhatWhenQuery(resource, triples.get(i), kgsDirectory, numOfQueries, i+1, insightsMap);
			combinedResults.add(results);
			
		}
		
		if (triples.size() > 1) {
			results = generateCompareResults(combinedResults);
			//run sensitivity
		}
		
		
		//} //if (useDBN)
		
		if(debugMode) {
			long endTime = System.currentTimeMillis();
			System.out.println("InsertTriplesAndQuery: " + (endTime - startTime)/1000.0 + " secs" );
			SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
			String time = sdf.format(Calendar.getInstance().getTime());
			System.out.println("InsertTriplesAndQuery returned " + time);
		}
		return results;
	}

	
	
	
	private Object[] generateCompareResults(List<Object[]> combinedResults) {
		List<Object> res = new ArrayList<Object>();
		List<Object> wres;
		Map<String, List<Object[]>> classResMap = new HashMap<String, List<Object[]>>();
		
		List<Object[]> list;
		String cl = null;
		
		for(int i=0; i<combinedResults.size(); i++) {
			if (combinedResults.get(i).length > 0) {
				cl = ((ResultSet)combinedResults.get(i)[1]).getResultAt(0, 1).toString();
				if(classResMap.containsKey(cl)) {
					list = classResMap.get(cl);
				}
				else {
					list = new ArrayList<Object[]>();
				}
				list.add(combinedResults.get(i));
				classResMap.put(cl, list );
			}
		}
		
		int numOfWhens = (classResMap.get(cl) != null ? classResMap.get(cl).size() : 0);
		int numOfClasses = combinedResults.size()/numOfWhens;
		
		List<Object[]> singleCaseResults = new ArrayList<Object[]>();
		
		for(int i=0; i<numOfWhens; i++) {
			singleCaseResults.clear();
			for(int j=i; j<numOfClasses+i; j++) {
				singleCaseResults.add( combinedResults.get(i+j));
			}
			wres = generateCompareResults1(singleCaseResults);
			for(int k=0; k<wres.size(); k++) {
				res.add(wres.get(k));
			}
		}
		
		return res.toArray();
	}

	/***
	 * For each "class" (e.g. CF6, F100), computes the unique insights.
	 * 
	 * @param combinedResults
	 * @return
	 */
	private List<Object> generateCompareResults1(List<Object[]> combinedResults) {
//		List<Object> cres = new ArrayList<Object>();
		List<Object> res = new ArrayList<Object>();
		
		int numOfClasses = combinedResults.size();

		Object insights1, insights2;
		
		for(int i=0; i<numOfClasses; i++) {
			if(combinedResults.get(i).length > 0) {
				insights1 = combinedResults.get(i)[2];			
				for(int j=0; j<numOfClasses; j++) {
					if(j!=i) {
						if(combinedResults.get(j).length > 0) {
							insights2 = combinedResults.get(j)[2];
							insights1 = getUniqueResults((ResultSet)insights1, (ResultSet)insights2);
						}
					}
				}
				res.add(combinedResults.get(i)[0]);
				res.add(combinedResults.get(i)[1]);
				res.add(insights1);
			}
			else {
				ResultSet e1 = new ResultSet(new String[0]);
				res.add(e1);
//				res.add(e1);
//				res.add(e1);
			}
		}
		
		
		return res;
	}

	
	
	private Object[] generateCompareResults0(List<Object[]> combinedResults) {
		List<Object> res = new ArrayList<Object>();
		Object[] res1 = new Object[3];
		Object[] res2 = new Object[3];
		
//		for(int i=0; i<combinedResults.size(); i++) {
//			Object[] cr = combinedResults.get(i);
//			for(int j=0; j<cr.length; j++) {
//				res.add(cr[j]);
//			}
//		}
		
		Object[] result1 = combinedResults.get(0);
		Object[] result2 = combinedResults.get(1);

		res1[0] = result1[0];
		res1[1] = result1[1];
		res2[0] = result2[0];
		res2[1] = result2[1];
		
		ResultSet insights1 = (ResultSet) result1[2];
		ResultSet insights2 = (ResultSet) result2[2];
		
		res1[2] = getUniqueResults(insights1, insights2);

		res2[2] = getUniqueResults(insights2, insights1);
		

		res.add(result1[0]);
		res.add(result1[1]);
		res.add(res1[2]);
		res.add(result2[0]);
		res.add(result2[1]);
		res.add(res2[2]);
		
		
		return res.toArray();
	}
	
	private Object getUniqueResults(ResultSet insights1, ResultSet insights2) {
		ResultSet jointInsights ;
		boolean jointProperty;
		String[] insHeaders = insights1.getColumnNames();
		Object[][] insData1 = insights1.getData();
		Object[][] insData2 = insights2.getData();

		List<Object[]> jointInsightsData = new ArrayList<Object[]>();
		
		int trendIdx = 2;
		int inputIdx = 1;
		int outputIdx = 3;
		
		for(int i=0; i<insData1.length; i++) {
			String input1 = insData1[i][inputIdx].toString();
			String output1 = insData1[i][outputIdx].toString();
			String ins1 = insData1[i][trendIdx].toString();
			jointProperty = false;
			for(int j=0; j<insData2.length; j++) {
				String input2 = insData2[j][inputIdx].toString();
				String output2 = insData2[j][outputIdx].toString();
				String ins2 = insData2[j][trendIdx].toString();
				if(input1.equals(input2) && output1.equals(output2) && ins1.equals(ins2)) {
					jointProperty = true;
					break;
				}
			}
			if(!jointProperty) {
				jointInsightsData.add(insData1[i]);
			}
		}
		
	
//		Object[][] jid = new Object[insHeaders.length][jointInsightsData.size()];
		Object[][] jid = new Object[jointInsightsData.size()][insHeaders.length];
		
		for (int i=0; i<jointInsightsData.size(); i++)
			for (int j=0; j<insHeaders.length; j++)
				jid[i][j] = jointInsightsData.get(i)[j];
		
		if(jointInsightsData.size() <= 0) {
			jointInsights = new ResultSet(insHeaders);
		}
		else {
			jointInsights = new ResultSet(insHeaders, (Object[][]) jid);
		}
		return jointInsights;
	}



	private Object[] processSingleWhatWhenQuery(Resource resource, TripleElement[] triples, String kgsDirectory, int numOfQueries, int queryNum, Map<String, Map<String, List<String>>> insightsMap) throws TranslationException, IOException, URISyntaxException, ConfigurationException, Exception {
		Object[] dbnResults = null;
		Object[] kcResults = null;
		Object[] results = null;

		List<TripleElement[]> inputPatterns = new ArrayList<TripleElement[]>();
		List<TripleElement> outputPatterns = new ArrayList<TripleElement>();
		List<TripleElement> docPatterns = new ArrayList<TripleElement>();
		List<TripleElement> contextPatterns = new ArrayList<TripleElement>();
		List<Node> inputNodes = new ArrayList<Node>();
		List<Node> contextNodes = new ArrayList<Node>();

		//getTheJenaModel().write(System.out, "TTL" );
  		//qhmodel.write(System.out,"RDF/XML-ABBREV");

		String queryModelFileName;
		String queryModelURI ;
		String queryModelPrefix ;
		String queryInstanceName ;
		String queryOwlFileWithPath ;

		


		queryModelFileName = "Q_" + System.currentTimeMillis();
		queryModelURI = "http://aske.ge.com/" + "Model_" + queryModelFileName;
		queryModelPrefix = queryModelURI + "#";
		//String queryInstanceName = METAMODEL_PREFIX + queryModelFileName;
		queryInstanceName = queryModelPrefix + queryModelFileName;
		queryOwlFileWithPath = kgsDirectory + File.separator + queryModelFileName + ".owl";

		
		
		//ConfigurationManagerForIDE cmgr = null;
		//Object qhModelObj = OntModelProvider.getPrivateKeyValuePair(resource, queryHistoryKey);
		
		registerQueryModel(resource, queryModelFileName, queryOwlFileWithPath);
		
		
		
		// Query instance
		Individual cgq = queryModel.createIndividual(queryInstanceName, getModelClass(getTheJenaModel(), METAMODEL_QUERY_CLASS));
		// This is how to add properties to the individual
		//i.addProperty(RDFS.comment, "something");
	    //i.addRDFType(OWL2.NamedIndividual);

		
		getInputPatterns(triples, inputPatterns, inputNodes);

		getContextPatterns(triples, inputNodes, contextPatterns, contextNodes);
		
//		getOutputDocContextPatterns(triples, inputNodes, outputPatterns, docPatterns, contextPatterns);
		getOutputPatterns(triples, inputNodes, contextNodes, outputPatterns);
		
		ingestInputValues(queryModelPrefix, inputPatterns, contextPatterns, cgq);
		

		List<RDFNode> inputsList = getRDFInputsList(inputPatterns);
		
		List<RDFNode> outputsList = getRDFOutputsList(outputPatterns, docPatterns);
		
		List<String> contextClassList = getContextClassList(contextPatterns);
		
	
		int successfulModels=0;
		
		if (useDbn) {

			if (outputsList.size() > 0 && docPatterns.size() <= 0) { 
				dbnResults = processWhatWhenQuery(resource, queryModelFileName, queryModelURI, queryModelPrefix,
					queryOwlFileWithPath, inputsList, outputsList, contextClassList, cgq, numOfQueries, queryNum, 
					successfulModels, insightsMap);



			} else if (outputsList.size() > 0 && docPatterns.size() > 0) { //models from dataset

				dbnResults = processModelsFromDataset(resource, triples, queryModelFileName, queryModelURI, queryModelPrefix,
						queryOwlFileWithPath, inputsList, outputsList, contextClassList, cgq);

			}
		}

		if (useKC) {
			kcResults = processWhatWhenQuery(resource, queryModelFileName, queryModelURI, queryModelPrefix,
			queryOwlFileWithPath, inputsList, outputsList, contextClassList, cgq, numOfQueries, queryNum, successfulModels, insightsMap);

		}

			
			// Save metadata owl file
			// Don't need to save at this point?
			//saveMetaDataFile(resource,queryModelURI, queryModelFileName);
		
		results = getCombinedResultSet(dbnResults, kcResults);
	
		return results;
	}




private ResultSet[] processWhatWhenQuery(Resource resource, String queryModelFileName, String queryModelURI,
		String queryModelPrefix, String queryOwlFileWithPath, List<RDFNode> inputsList, List<RDFNode> outputsList,
		List<String> contextClassList, Individual cgq, int numOfQueries, int queryNum, int successfulModels, Map<String, Map<String, List<String>>> insightsMap)
		throws TranslationException, Exception, IOException, URISyntaxException, ConfigurationException {
	
	org.apache.jena.query.ResultSetRewindable eqnsResults = null;
	ResultSet eqnsResults1 = null;
//	QueryExecution qexec = null;
	String listOfEqns = "";

	Individual cgIns = null;
	String resmsg = null;
	String sensitivityURL = null;
	String sensitivityResult = null;
//	OntClass cexec;
	Individual ce;
	
	Map<String,String> class2lbl = null;
	Map<String,String> lbl2class;
	Map<String,String[]> lbl2value = null;
	Map<String,String> class2units = null;
	
	String cgJson, dbnJson, dbnResultsJson = null;
	JsonObject kchainBuildJson = null;
	String kchainResultsJson = null;
	JsonObject kchainEvalJson;

	
	Map<RDFNode, String[]> dbnEqnMap = new HashMap<RDFNode,String[]>();
	Map<RDFNode, RDFNode> dbnOutput = new HashMap<RDFNode,RDFNode>();

	ResultSet[] dbnResults = null;
	
	boolean inverseQuery=false;
	
	ConfigurationManagerForIDE cmgr = getConfigMgrForIDE(resource);
	
	

	System.out.print("Retrieving composite model eqns: ");
	long startTime = System.currentTimeMillis();
//	eqnsResults = retrieveCG(resource, inputsList, outputsList);
	eqnsResults1 = retrieveCG1(resource, inputsList, outputsList, inverseQuery);
	if (eqnsResults1 == null) {
		RDFNode rdfn = outputsList.get(0);
		if (rdfn.isURIResource()) {
			NamedNode nn = new NamedNode(rdfn.asResource().getURI());
			throw new NoModelFoundForTargetException("No model found for " + nn.toString() + ".", nn);
		}
	}
	long endTime = System.currentTimeMillis();
	System.out.println((endTime - startTime)/1000.0 + " secs");
	
	//dbnEqns = createDbnEqnMap(eqnsResults);
//	dbnEqnMap = createOutputEqnMap(eqnsResults1);
	dbnEqnMap = createOutputEqnMap1(eqnsResults1);

	//dbnOutput = createDbnOutputMap(eqnsResults); //don't need map from dbns to ouputs anymore
	dbnOutput = null;
	
	int numOfModels = getNumberOfModels(dbnEqnMap);

	if (numOfModels > 0) {
		dbnResults = new ResultSet[numOfModels*3]; //+1 to accommodate dependency graph 
		//dbnResults = new ResultSet[numOfModels]; 
	} else {
//		System.out.println("Unable to assemble a model with current knowledge");
	}
	
	String[] modelEqnList = buildEqnsLists(numOfModels, dbnEqnMap);

	
	successfulModels = 0;
	List<Individual> modelCCGs = new ArrayList<Individual>();
	
	
	
	for(int i=0; i<numOfModels; i++) {
		listOfEqns = modelEqnList[i];

		ce = createExecInstance(cgq);

		cgIns = createCGinstance(queryModelPrefix, ce);
		
		modelCCGs.add(cgIns);
		
		//saveMetaDataFile(resource,queryModelURI,queryModelFileName); 
		
		String scriptLanguage = "http://sadl.org/sadlimplicitmodel#Python-NumPy";
//		String scriptLanguage = "http://sadl.org/sadlimplicitmodel#Python";
		
//		contextClassList.
		
		String nodesModelsJSONStr = retrieveModelsAndNodes(resource, listOfEqns, cgIns, contextClassList, scriptLanguage, numOfQueries*numOfModels, queryNum);
		
		

		class2lbl 	= getClassLabelMappingFromModelsJson(nodesModelsJSONStr);
		
		lbl2class = getInverseMap(class2lbl);
		
		class2units = getClassUnitsMappingFromModelsJson(nodesModelsJSONStr);
		
		
		System.out.print("Translating KG results into json: ");
		startTime = System.currentTimeMillis();
		cgJson = kgResultsToJson(nodesModelsJSONStr, "prognostic", "");
		endTime = System.currentTimeMillis();
		System.out.println((endTime - startTime)/1000.0 + " secs"); // + "  Payload size: " + cgJson.length());
		
		if (useDbn) {
		
			dbnJson 	= generateDBNjson(cgJson);
			
			//Save the label mapping
			class2lbl 	= getClassLabelMapping(dbnJson);

			// Execute DBN
	
//			ArrayList<String> methods = new ArrayList<String>();
//			me  = methods.get(methods.size()-1);
			
			dbnResultsJson = executeDBN(dbnJson);
			
				//get DBN execution outcome
			resmsg = getDBNoutcome(dbnResultsJson);
		}
		
// TODO should this be else if? code following does not handle multiple responses, would need to be refactored and called for each...		
		if (useKC) {
			
			System.out.print("Translating KG json into KC json: ");
			startTime = System.currentTimeMillis();
			cgJson = generateDBNjson(cgJson);
			endTime = System.currentTimeMillis();
			System.out.println((endTime - startTime)/1000.0 + " secs"); // + "  Payload size: " + cgJson.length());

			kchainBuildJson = generateKChainBuildJson(cgJson);
			
			if (kchainBuildJson != null) {
				System.out.print("Building KChain: ");
				startTime = System.currentTimeMillis();
				String buildResult = buildKChain(kchainBuildJson);
				endTime = System.currentTimeMillis();
				System.out.println((endTime - startTime)/1000.0 + " secs"); // + "  Payload size: " + kchainBuildJson.toString().length());

				if ( getBuildKChainOutcome(buildResult) ) {
			
					kchainEvalJson = generateKChainExecJson(cgJson);
					
					System.out.print("Executing KChain: ");
					startTime = System.currentTimeMillis();
					kchainResultsJson = executeKChain(kchainEvalJson);
					endTime = System.currentTimeMillis();
					System.out.println((endTime - startTime)/1000.0 + " secs");
				    
				    resmsg = getEvalKChainOutcome(kchainResultsJson);
				    
				    if(!inverseQuery && performSensitivity) {
					    JsonObject sensitivityJson = generateKChainSensitivityJson(cgJson);
				    
					    sensitivityJson = addKCserviceURL(sensitivityJson); //Add kchain eval service URL for invizin
					    
						System.out.print("Sensitivity analysis: ");
						startTime = System.currentTimeMillis();
						sensitivityResult = execKChainSensitivity(sensitivityJson);
						//System.out.println(sensitivityResult);
					    sensitivityURL = getVisualizationURL(sensitivityResult);
						endTime = System.currentTimeMillis();
						System.out.println((endTime - startTime)/1000.0 + " secs");
						
				    }
				    else {
				    	System.out.print("Model uses inverse submodels");
				    }
				}
			}		
		}
	    
	    //TODO: if exec was unsuccessful, ingest CG anyway and do something further
	    
	    if(resmsg != null && resmsg.equals("Success")) {
	    	successfulModels++;
	    	
	    	if (useDbn) {
	    		lbl2value = getLabelToMeanStdMapping(dbnResultsJson);
	    	}
	    	else if (useKC) {
	    		lbl2value = getLabelToValueMapping(kchainResultsJson);
	    	}
	        createCGsubgraphs(cgIns, dbnEqnMap, dbnOutput, listOfEqns, class2lbl, lbl2value, class2units, queryModelPrefix);
	        
			//Create output instances and link them to ce
			//There may be multiple outputs, need to loop through them
	        createCEoutputInstances(outputsList, ce, class2lbl, lbl2class, lbl2value, class2units);

	        if (performSensitivity) {
	        	analyzeSensitivityResults(sensitivityResult, cgIns, lbl2class, outputsList, queryModelPrefix, insightsMap);			
	        }
	        
	        saveMetaDataFile(resource,queryModelURI,queryModelFileName); //so we can query the the eqns in the CCG
			
			
			// Get the CG info for diagram
			
			dbnResults[i*3] = retrieveCompGraph(resource, cgIns); //+1 to accomodate dependency graph
			
			ResultSet rvalues = retrieveValues(resource, cgIns); //outputsList
			
			//ResultSet svalues = 
		
			dbnResults[i*3+1] = addSensitivityURLtoResults(rvalues, sensitivityURL);
//			dbnResults[i+1] = retrieveValues(resource, cgIns);
			
			ResultSet insights = retrieveInsights(resource, cgIns);
			
			dbnResults[i*3+2] = insights;

//Temporarily commented out assumption check
//			String assumpCheck = checkAssumptions(resource, queryModelURI, queryOwlFileWithPath, cgIns);

			//Are we going to do sensitivity from here any more?
			//If assumptions are satisfied, compute sensitivity
//			if ( assumpCheck.equals("satisfied") ) {
//				computeSensitivityAndAddToDialog(resource, cmgr, queryModelFileName, queryModelURI, queryModelPrefix,cgIns, nodesModelsJSONStr);
//			}
			
			//saveMetaDataFile(resource,queryModelURI,queryModelFileName); //so we can query the eqns in the CCG

	    }
	    else {
	    	dbnResults[i*3] = null;
	    	System.err.println("DBN execution failed. Message: " + dbnResultsJson.toString());
	    }
	}// end of models loop
	
	
	//If there were some results, create Eclipse resource, refresh, add mapping to ont-policy.rdf, add import stmt to dialog
	if(successfulModels>0) {
		//saveMetaDataFile(resource,queryModelURI,queryModelFileName); //to include sensitivity results
		addQueryModelAsResource(resource, queryModelFileName, queryModelURI, queryOwlFileWithPath, cmgr);
//		dbnResults[0] = runReasonerQuery(resource, DEPENDENCY_GRAPH);

	} else {
		saveMetaDataFile(resource,queryModelURI,queryModelFileName);
//		dbnResults = new ResultSet[1];
//		Object[][] data = new Object[1][1];
//		data[0][0] = outputsList.get(0).toString().split("#")[1];
//		String[] header = {"NoModelFound"};
//		dbnResults[0] = new ResultSet(header,data);
		dbnResults = null;
	}
	
	if (modelCCGs.size() > 0) {
		//checkAssumptionGaps(resource, queryModelURI, queryOwlFileWithPath, cgq);	
	}
	
	return dbnResults;
}






/**
 * 
 * @param resource
 * @param outputsList
 * @return
 */
private ResultSet retrieveQueryOutput(Resource resource, List<RDFNode> outputsList) {
	// TODO Auto-generated method stub
	return null;
}

/**
 * 
 * @param resource
 * @param cgIns
 * @return
 */
private ResultSet retrieveInsights(Resource resource, Individual cgIns) throws Exception {
	String query = TRENDSQUERY.replaceAll("COMPGRAPH", "<" + cgIns.getURI() + ">");
	ResultSet res = runReasonerQuery(resource, query);
	return res;
}

/**
 * 
 * @param map
 * @return
 */
private Map<String, String> getInverseMap(Map<String, String> map) {
	Map<String, String> imap = new HashMap<String, String>();
	for(Entry<String, String> entry : map.entrySet()) {
		imap.put(entry.getValue(), entry.getKey());
	}
	return imap;
}

/**
 * 
 * @param sensitivityResult
 * @param queryModelPrefix 
 * @param class2lbl 
 * @param cgIns 
 * @param outputsList 
 * @param insightsMap 
 * @throws IOException 
 */
private void analyzeSensitivityResults(String sensitivityResult, Individual cgIns, Map<String, String> lbl2class, List<RDFNode> outputsList, String queryModelPrefix, Map<String, Map<String, List<String>>> insightsMap) throws IOException {
//	sensitivityResult = "{\"sensitivityData\":[{\"OATMatrix\":{\"fsmach\":[0,0.09232463,0.1766437,0.2464482,0.29775146,0.32939076,0.34265238,0.3404852,0.3266189,0.30482608,0.2784374,0.25010738,0.22177133,0.194718,0.1697141,0.14713784,0.12709871,0.10953298,0.09427574,0.08111054],\"altd\":[0,0.15789473684210525,0.3157894736842105,0.47368421052631576,0.631578947368421,0.7894736842105263,0.9473684210526315,1.1052631578947367,1.263157894736842,1.4210526315789473,1.5789473684210527,1.7368421052631577,1.894736842105263,2.052631578947368,2.2105263157894735,2.3684210526315788,2.526315789473684,2.6842105263157894,2.8421052631578947,3]},\"OATRSMatrix\":{\"fsmach\":[0.33210394,0.3332521,0.33433527,0.33535418,0.33630925,0.33720127,0.33803058,0.3387981,0.3395045,0.34015036,0.3407363,0.34126318,0.34173167,0.3421426,0.34249645,0.3427943,0.34303662,0.34322447,0.34335843,0.34343934],\"altd\":[0.81,0.8194736842105264,0.8289473684210527,0.838421052631579,0.8478947368421054,0.8573684210526317,0.866842105263158,0.8763157894736843,0.8857894736842106,0.8952631578947369,0.9047368421052633,0.9142105263157896,0.9236842105263159,0.9331578947368422,0.9426315789473685,0.9521052631578948,0.9615789473684211,0.9710526315789475,0.9805263157894738,0.9900000000000001]},\"name\":\"altd\",\"type\":\"float\",\"value\":\"0.9\"},{\"OATMatrix\":{\"fsmach\":[0.30224335,0.30802384,0.3135674,0.3188915,0.32401192,0.32894263,0.33369592,0.33828318,0.34271488,0.34699997,0.35114703,0.355164,0.35905787,0.36283517,0.36650208,0.3700641,0.37352648,0.37689403,0.38017118,0.3833621],\"u0d\":[1.01,1.0621052631578947,1.1142105263157895,1.1663157894736842,1.2184210526315788,1.2705263157894737,1.3226315789473684,1.3747368421052633,1.426842105263158,1.4789473684210526,1.5310526315789474,1.583157894736842,1.635263157894737,1.6873684210526316,1.7394736842105263,1.791578947368421,1.8436842105263158,1.8957894736842107,1.9478947368421053,2]},\"OATRSMatrix\":{\"fsmach\":[0.32796124,0.32933322,0.33069092,0.33203492,0.3333654,0.33468255,0.33598652,0.33727765,0.33855626,0.3398223,0.34107617,0.34231815,0.34354833,0.34476686,0.34597406,0.34717005,0.34835514,0.34952927,0.3506928,0.35184592],\"u0d\":[1.26,1.2747368421052632,1.2894736842105263,1.3042105263157895,1.3189473684210526,1.3336842105263158,1.348421052631579,1.3631578947368421,1.3778947368421053,1.3926315789473684,1.4073684210526316,1.4221052631578948,1.436842105263158,1.451578947368421,1.4663157894736842,1.4810526315789474,1.4957894736842106,1.5105263157894737,1.5252631578947369,1.54]},\"name\":\"u0d\",\"type\":\"float\",\"value\":\"1.4\"}],\"url\":\"http://localhost:1177\"}";

	if(debugMode) {System.out.println(sensitivityResult);}
	
	JsonElement je = new JsonParser().parse(sensitivityResult);
	if (je.isJsonObject()) {
		JsonObject jobj = je.getAsJsonObject();
		if (jobj.has("OATSensitivityData")) { //sensitivityData
			JsonArray ja = jobj.getAsJsonArray("OATSensitivityData");
			JsonArray jacobians = null;
			if (jobj.has("normalizedSensitivityData")) {
				jacobians = jobj.getAsJsonArray("normalizedSensitivityData");
			}
			for(int i=0; i<ja.size(); i++) { //for each input variable
				extractVarInfluence(ja.get(i).getAsJsonObject(), i, jacobians, cgIns, lbl2class, outputsList, queryModelPrefix, insightsMap);
			}
		}
		else {
			throw new IOException("Sensitivity returned no data: " + je.toString());
		}
	}
	else {
		throw new IOException("Unexpected response from sensitivity: " + je.toString());
	}
}

/**
 * 
 * @param outputsList 
 * @param insightsMap 
 * @param asJsonObject
 */
private void extractVarInfluence(JsonObject sensitivityResult, int inputIdx, JsonArray jacobians, Individual cgIns, Map<String, String> lbl2class, List<RDFNode> outputsList, String queryModelPrefix, Map<String, Map<String, List<String>>> insightsMap) {
	String input = sensitivityResult.get("name").getAsString();
	String output = null;
	String vstr = sensitivityResult.get("value").getAsString();
	double value = new Double(vstr);
	JsonArray inputArray = null;
	JsonArray outputArray = null;
	int queryPointIdx;
	double queryOutput; 
	double prevOut, min, max, ip, op;
	boolean increasingIncreases;
	boolean increasingDecreases;
	boolean decreasingIncreases;
	boolean decreasingDecreases;
	boolean independent;
	boolean pos_sensitive;
	boolean neg_sensitive;
	Map<String, List<String>> outputInsights = new HashMap<String, List<String>>();
	List<String> insightList;
	
//	boolean localmin;
//	boolean localmax;
//	boolean incr;
//	boolean decr = incr = false;
	boolean lower;
	boolean higher;
	
	String matrixType = "OATRSMatrix";
	
	Iterator<Entry<String, JsonElement>> mtxitr = sensitivityResult.get(matrixType).getAsJsonObject().entrySet().iterator();  //input/output - data
	
	inputArray = sensitivityResult.get(matrixType).getAsJsonObject().get(input).getAsJsonArray();

	while(mtxitr.hasNext()) {
		Entry<String, JsonElement> mtxe = mtxitr.next();
		if (mtxe.getKey().equals(input))
			if (mtxitr.hasNext())
				mtxe = mtxitr.next();
			else
				continue;
		output = mtxe.getKey();
		outputArray = sensitivityResult.get(matrixType).getAsJsonObject().get(output).getAsJsonArray();

		independent = increasingIncreases = increasingDecreases = decreasingIncreases = decreasingDecreases = lower = higher = false;
	
		prevOut = min = max = outputArray.get(0).getAsDouble();
		
		insightList= new ArrayList<String>();
		
		/**
		 *  Only results can be:
		 *  1) decreasingDecreases (at lower)
		 *  2) decreasingIncreases (at lower)
		 *  3) decreasingIncreases & increasingIncreases (localmin) at lower
		 *  4) decreasingDecreases & increasingDecreases (localmax) at lower
		 *  5) increasingIncreases (at higher)
		 *  6) increasingDecreases (at higher)
		 *  7) increasingDecreases & increasingIncreases (localmin) at higher
		 */
			
		int size = outputArray.size();
		queryPointIdx = size/2+1;
		queryOutput = outputArray.get(queryPointIdx).getAsDouble();
	
		for(int i=1; i<size; i++) {
			ip = inputArray.get(i).getAsDouble();
			op = outputArray.get(i).getAsDouble();
			if (op < min)
				min = op;
			if (op > max)
				max = op;
			
			if(i<=queryPointIdx) {
				if(prevOut < op) {
					decreasingDecreases = true;
					insightList.add("decreasingDecreases");
				}
				if(prevOut > op) {
					decreasingIncreases = true;
					insightList.add("decreasingIncreases");
				}
			}
			if (queryPointIdx <= i) {
				if(prevOut < op) {
					increasingIncreases = true;
					insightList.add("increasingIncreases");
				}
				if(prevOut > op) {
					increasingDecreases = true;
					insightList.add("increasingDecreases");
				}
			}

//		if(prevOut < op && !decreasingIncreases)
//			decreasingDecreases = true;
//		if(prevOut > op && !decreasingDecreases)
//			decreasingIncreases = true;
//		if(decreasingIncreases && ! increasingIncreases && prevOut < op) {//found local min
//			increasingIncreases = true;
//			if (i < queryPointIdx && prevOut != queryOutput)
//				lower = true;
//			if (i > queryPointIdx && prevOut != queryOutput)
//				higher = true;
//		}
//		if(decreasingDecreases && !increasingDecreases && prevOut > op) {//found local max
//			increasingDecreases = true;
//			if (i < queryPointIdx && prevOut != queryOutput)
//				lower = true;
//			if (i > queryPointIdx && prevOut != queryOutput)
//				higher = true;
//		}

			
			prevOut = op;
		}
	
		
	//	
	//	prevOut = outputArray.get(0).getAsDouble();
	//	for(int i=1; i<size; i++) {
	//		op = outputArray.get(i).getAsDouble();
	//		if(incr && prevOut > op)
	//			localmax = true;
	//		if(decr && prevOut < op)
	//			localmin = true;
	//		if(prevOut < op) {
	//			incr = true;
	//			decr = false;
	//		}
	//		if( prevOut > op) {
	//			incr = false;
	//			decr = true;
	//		}
	//	}
		
		
		if(min == max) {
			independent = true;
			insightList.add("independent");
		}
		
		pos_sensitive = neg_sensitive = false;
		
		// Get sensitivity insights
		for (int j=0; j<jacobians.size(); j++) {
			JsonObject je = jacobians.get(j).getAsJsonObject();
			if (je.get("name").getAsString().equals(output)) {
				String sensArrayStr = je.get("value").getAsString();
				String[] splitStr = sensArrayStr.replace("[", "").replace("]", "").split(",");
				
				try {
					double sens = new Double(splitStr[inputIdx]);
					if( sens > 0.4 ) {
						insightList.add("pos_sensitive");
						pos_sensitive = true;
					}
					if( sens < -0.4 ) {
						insightList.add("neg_sensitive");
						neg_sensitive = true;
					}
					break;
				}
				catch (Exception e) {
					// just continue
				}
			}	
		}
		
		outputInsights.put(output,insightList);
		insightsMap.put(input, outputInsights);
		
		ingestTrend(input, output, increasingIncreases, increasingDecreases, decreasingIncreases, decreasingDecreases,
				    lower, higher, independent, pos_sensitive, neg_sensitive, cgIns, lbl2class, outputsList);
	}	
	
}

/**
 * 
 * @param input
 * @param output
 * @param increasing
 * @param decreasing
 * @param independent 
 * @param independent2 
 * @param decreasingDecreases 
 * @param independent2 
 * @param higher 
 * @param cgIns
 * @param outputsList 
 * @param class2lbl
 */
private void ingestTrend(String input, String output, boolean increasingIncreases, boolean increasingDecreases, boolean decreasingIncreases, boolean decreasingDecreases, boolean lower, boolean higher, boolean independent, boolean pos_sensitive, boolean neg_sensitive, Individual cgIns, Map<String, String> lbl2class, List<RDFNode> outputsList) {
	String inputType = lbl2class.get(input);
	String outputType;
	
	if (lbl2class.containsKey(output)) {
		outputType = lbl2class.get(output);
	}
	else {
		outputType = outputsList.get(0).toString();
	}
	//cgIns sensitivity sensIns
	Individual sensIns = createIndividualOfClass(queryModel, null, null, METAMODEL_SENSITIVITY);
	ingestKGTriple(cgIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_PROP), sensIns);
	
	//sensins output MachSpeed
	ingestKGTriple(sensIns, getModelProperty(getTheJenaModel(), METAMODEL_OUTPUT_PROP), getTheJenaModel().getResource(outputType)) ;
	
	//sensIns trendEffect outputTrendIns
	Individual outputTrendIns = createIndividualOfClass(queryModel, null, null, METAMODEL_SENS_OUTPUTTREND);
	ingestKGTriple(sensIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TRENDEFFECT_PROP), outputTrendIns);
	//outputTrendIns input Altitude
	ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_INPUT_PROP), getTheJenaModel().getResource(inputType));
	
	/**
	 *  Only results can be:
	 *  1) decreasingDecreases
	 *  2) decreasingIncreases
	 *  3) decreasingDecreases & increasingDecreases
	 *  4) decreasingIncreases & increasingIncreases
	 */

	//outputTrednIns trend :increasing

	if(decreasingDecreases && increasingIncreases) { //uptrend around query point
		if(decreasingIncreases) { //local min @ lower
			ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_LOC_PROP), getTheJenaModel().getResource(METAMODEL_TREND_AT_LOWER));
			ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_LOCALMIN));
		}
		else if (increasingDecreases) { //local max @ higher
			ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_LOC_PROP), getTheJenaModel().getResource(METAMODEL_TREND_AT_HIGHER));
			ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_LOCALMAX));
		}
		else { // only uptrend
			ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_INCRINCR));
		}
	}
	else if(decreasingIncreases &&  increasingDecreases) { //downtrend around query point
		if(decreasingDecreases) { //local max @ lower
			ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_LOC_PROP), getTheJenaModel().getResource(METAMODEL_TREND_AT_LOWER));
			ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_LOCALMAX));
		}
		else if (increasingIncreases) { //local min @ higher
				ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_LOC_PROP), getTheJenaModel().getResource(METAMODEL_TREND_AT_HIGHER));
				ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_LOCALMIN));
		}
		else {
			ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_INCRDECR));
		}
	}
	else if (decreasingIncreases && increasingIncreases) {//local min @ query point
		ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_DECRINCR));
		ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_INCRINCR));
	}
	else if (decreasingDecreases && increasingDecreases) { //local max @ query point
		ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_DECRDECR));
		ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_INCRDECR));
	}
	else { // otherwise just output as-is
		if (increasingIncreases) {
			ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_INCRINCR));
		}
		if (increasingDecreases) {
			ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_INCRDECR));
		}
		if (decreasingIncreases) {
			ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_DECRINCR));
		}
		if (decreasingDecreases) {
			ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_DECRDECR));
		}
		
		
	}
	if (independent) {
		ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_INDEPENDENT));
	}
	
	if (pos_sensitive) {
		ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_POS_SENSITIVE));
	}

	if (neg_sensitive) {
		ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_NEG_SENSITIVE));
	}

	
//	if(decreasingDecreases) {
//		if(increasingDecreases) {
//			if (lower) {
//				ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_LOC_PROP), getTheJenaModel().getResource(METAMODEL_TREND_AT_LOWER));
//				ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_LOCALMAX));
//			}
//			if (higher) {
//				ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_LOC_PROP), getTheJenaModel().getResource(METAMODEL_TREND_AT_HIGHER));
//				ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_LOCALMAX));
//			}
//			else {
//				ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_DECRDECR));
//				ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_INCRDECR));
//			}
//		}
//		else {
//			ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_INCRINCR));
//		}
//	} 
//	if(decreasingIncreases) {
//		if(increasingIncreases) {
//			if (lower) {
//				ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_LOC_PROP), getTheJenaModel().getResource(METAMODEL_TREND_AT_LOWER));
//				ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_LOCALMIN));
//			}
//			if (higher) {
//				ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_LOC_PROP), getTheJenaModel().getResource(METAMODEL_TREND_AT_HIGHER));
//				ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_LOCALMIN));
//			}
//			else {
//				ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_DECRINCR));
//				ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_INCRINCR));
//			}
//		}
//		else {
//			ingestKGTriple(outputTrendIns, getModelProperty(getTheJenaModel(), METAMODEL_SENS_TREND_PROP), getTheJenaModel().getResource(METAMODEL_TREND_INCRDECR));
//		}
//	}
	
}

private void registerQueryModel(Resource resource, String queryModelFileName, String queryOwlFileWithPath) throws SadlInferenceException {
	File f = new File(queryOwlFileWithPath);
	if (f.exists() && !f.isDirectory()) {
		throw new SadlInferenceException("Query model file already exists");
	}
	else {
		queryModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
	}
	OntModelProvider.addPrivateKeyValuePair(resource, queryModelFileName, queryModel);
}





/**
 * 
 * @param kchainEvalJson
 * @return
 */
private JsonObject addKCserviceURL(JsonObject kchainEvalJson) {
	String kcServiceURL = getPreference(DialogPreferences.ANSWER_KCHAIN_CG_SERVICE_BASE_URI.getId()) + KCHAIN_SERVICE_URL_FRAGMENT + "evaluate";
	kchainEvalJson.addProperty("url", kcServiceURL);
	
	return kchainEvalJson;
}

/**
 * 
 * @param rvalues
 * @param sensitivityURL
 * @return
 */
private ResultSet addSensitivityURLtoResults(ResultSet rvalues, String sensitivityURL) {
	String[] cols = new String[rvalues.getColumnCount()+1];
	for(int i=0; i<rvalues.getColumnCount(); i++)
		cols[i] = rvalues.getColumnNames()[i];
	cols[rvalues.getColumnCount()] = "SensitivityURL";

	Object[][] newData = new Object[rvalues.getRowCount()][rvalues.getColumnCount()+1];
	Object[][] data = rvalues.getData();

	for(int i=0; i<rvalues.getRowCount(); i++)
		for(int j=0; j<rvalues.getColumnCount(); j++)
			newData[i][j] = data[i][j];

	newData[0][rvalues.getColumnCount()] = sensitivityURL;
	
	ResultSet res = new ResultSet(cols, newData);
	
	return res;
}

private void computeSensitivityAndAddToDialog(Resource resource, ConfigurationManagerForIDE cmgr,
		String queryModelFileName, String queryModelURI, String queryModelPrefix, Individual cgIns,
		String nodesModelsJSONStr) throws Exception {
	Map<String, String> lbl2class;
	String cgJson;
	String dbnJson;
	if (useDbn()) {
		//Send sensitivity request to DBN
		cgJson = kgResultsToJson(nodesModelsJSONStr, "sensitivity", "");
		dbnJson = generateDBNjson(cgJson);
		String dbnResultsJsonSensitivity = executeDBN(dbnJson);
		String sensitivitiyOutcome = getDBNoutcome(dbnResultsJsonSensitivity);

		if(sensitivitiyOutcome.equals("Success")) {
			lbl2class = getLabelClassMapping(dbnJson);
			ingestSensitivityResults(cgIns, lbl2class, dbnResultsJsonSensitivity, queryModelPrefix);

		} else {
			System.err.println("Sensitivity computation failed");
		}

		saveMetaDataFile(resource,queryModelURI,queryModelFileName); //so we can query the the eqns in the CCG
		AnswerCurationManager acm = (AnswerCurationManager) cmgr.getPrivateKeyMapValueByResource(DialogConstants.ANSWER_CURATION_MANAGER, resource.getURI());
		addSensitivityResultsToDialog(resource, cgIns, acm);

	}//assumptions satisfied
}

private String retrieveModelsAndNodes(Resource resource, String listOfEqns, Individual cgIns, List<String> contextClassList, String scriptLanguage, int numOfQueries, int modelNum) throws SadlInferenceException, ConfigurationException, ReasonerNotFoundException, InvalidNameException, QueryParseException, QueryCancelledException, AmbiguousNameException {
	long startTime;
	long endTime;
	String modelsJSONString = "";
	String nodesJSONString = "";
	String expressionsJSONString = "";
	System.out.print("Retrieving model info from KG: ");
	startTime = System.currentTimeMillis();
//	modelsJSONString = retrieveCGforDBNSpec(listOfEqns, contextClassList, cgIns, RETRIEVE_MODELS_WEXP);
	modelsJSONString = retrieveCGforDBNSpec(resource, listOfEqns, contextClassList, cgIns, RETRIEVE_MODELS);
	nodesJSONString = retrieveCGforDBNSpec(resource, listOfEqns, null, cgIns, RETRIEVE_NODES.replaceAll("SCRIPTLANGUAGE", scriptLanguage));
	expressionsJSONString = retrieveCGforDBNSpec(resource, listOfEqns, contextClassList, cgIns, RETRIEVE_MODEL_EXPRESSIONS.replaceAll("SCRIPTLANGUAGE", scriptLanguage));
	endTime = System.currentTimeMillis();
	System.out.println((endTime - startTime)/1000.0 + " secs");

	StringBuilder ctxtBldr = new StringBuilder();
	for(String c : contextClassList) {
		ctxtBldr.append(c.split("#")[1]);
	}
	
    String context = ctxtBldr.toString();
    
	String nodesModelsJSONStr = "{ \"models\": " + modelsJSONString
							   + ", \"nodes\": "  + nodesJSONString 
							   + ", \"expressions\": " + expressionsJSONString
							   + ", \"context\": \"" + context
							   + "\", \"numOfModels\": \"" + numOfQueries
							   + "\", \"modelNum\": \"" + modelNum
							   + "\" }";
	if (debugMode) {System.out.println(nodesModelsJSONStr);}
	return nodesModelsJSONStr;
}

private Individual createCGinstance(String queryModelPrefix, Individual ce) {
	Individual cgIns;
	cgIns = queryModel.createIndividual(queryModelPrefix + "CG_" + System.currentTimeMillis(), getModelClass(getTheJenaModel(), METAMODEL_CCG));
	//getTheJenaModel().add(cgIns, RDF.type, getTheJenaModel().getOntClass(METAMODEL_CCG));
	ingestKGTriple(ce, getModelProperty(getTheJenaModel(), METAMODEL_COMPGRAPH_PROP), cgIns);
	return cgIns;
}

private Individual createExecInstance(Individual cgq) throws TranslationException {
	Individual ce;
	ce = createIndividualOfClass(queryModel, null, null, METAMODEL_CGEXEC_CLASS);
	addTimePropertyToCE(ce, getModelProperty(getTheJenaModel(), METAMODEL_STARTTIME_PROP));
	
	ingestKGTriple(cgq, getModelProperty(getTheJenaModel(), METAMODEL_EXEC_PROP), ce);
	return ce;
}



private Object[] getCombinedResultSet(Object[] dbnResults, Object[] kcResults) {
	Object[] results;
	int kcrlen = 0;
	int dbnrlen =0;
	if(kcResults != null) 
		kcrlen = kcResults.length;
	if(dbnResults != null)
		dbnrlen = dbnResults.length;
	results = new Object[kcrlen + dbnrlen];
	for(int i=0; i<kcrlen; i++) {
		results[i] = kcResults[i];
	}
	for(int i=0; i<dbnrlen; i++) {
		results[i] = dbnResults[i];
	}
	return results;
}




private List<String> getContextClassList(List<TripleElement> contextPatterns) {
	String so;
	TripleElement itr;
	List<String> contextClassList = new ArrayList<String>();
	
	if (contextPatterns != null) {
		for (int i = 0; i < contextPatterns.size(); i++) {
			itr = contextPatterns.get(i);
			so = itr.getObject().getURI(); // e.g. #CF6
			//ssp = getTheJenaModel().getProperty(sp);
			contextClassList.add(so);
		}
	}
	return contextClassList;
}

private List<RDFNode> getRDFOutputsList(List<TripleElement> outputPatterns, List<TripleElement> docPatterns) {
	String sp;
	Property ssp;
	TripleElement itr;
	List<RDFNode> outputsList = new ArrayList<RDFNode>();

	if (outputPatterns != null) {
		for (int i = 0; i < outputPatterns.size(); i++) {
			
			itr = outputPatterns.get(i); //e.g. itr = (v0 altitude v1)
			sp = itr.getPredicate().getURI();
			ssp = getTheJenaModel().getProperty(sp);
			if (ssp.canAs(OntProperty.class)) { 
				OntResource rng = ssp.as(OntProperty.class).getRange();
				// Add property to list of vars
				outputsList.add(rng);
			}
		}
	}
		
	// Create list of outputs from document patterns 
	if (docPatterns != null) {
		for (int i = 0; i < docPatterns.size(); i++) {
			itr = docPatterns.get(i);
			sp = itr.getObject().getURI(); // e.g. #altitude
			ssp = getTheJenaModel().getProperty(sp);
			outputsList.add(ssp);
		}
	}
	return outputsList;
}

private List<RDFNode> getRDFInputsList(List<TripleElement[]> inputPatterns) {
	String so;
	RDFNode sso;
	TripleElement itr;
	List<RDFNode> inputsList = new ArrayList<RDFNode>();
		
	if (inputPatterns != null) {
		for (int i = 0; i < inputPatterns.size(); i++) {
			
			//itr = inputPatterns.get(i)[0]; //e.g. itr = (v0 altitude v1)
			itr = inputPatterns.get(i)[3]; //e.g. itr = rdf(v1, rdf:type, hypersonicsV2:Speed)
			//sp = itr.getPredicate().getURI();
			//ssp = getTheJenaModel().getProperty(sp);
			if (itr != null) {
				so = itr.getObject().getURI();
				sso = getTheJenaModel().getResource(so);
				// Add property to list of vars
				inputsList.add(sso);
			}
		}
	}
	return inputsList;
}

/**
 * 
 * @param triples
 * @param inputNodes
 * @param contextPatterns
 * @param contextNodes 
 */
private void getContextPatterns(TripleElement[] triples, List<Node> inputNodes, List<TripleElement> contextPatterns, List<Node> contextNodes) {
	for (int i = 0; i < triples.length; i++) {
		TripleElement tr = triples[i];
		if (tr.getSubject() instanceof NamedNode) {
			if (tr.getPredicate().getURI() != null) {
				if ( ! inputNodes.contains(tr.getSubject()) && ! inputNodes.contains(tr.getObject()) ) {
					if (tr.getPredicate().toString().equals("rdf:type")){
						contextPatterns.add(tr); //rdf(v16, rdf:type, hypersonicsV2:CF6)
						contextNodes.add(tr.getSubject());
					}
				}
			}
		}
	}
}

private void getOutputPatterns(TripleElement[] triples, List<Node> inputNodes, List<Node> contextNodes, List<TripleElement> outputPatterns) {
	for (int i = 0; i < triples.length; i++) {
		TripleElement tr = triples[i];
		if (tr.getSubject() instanceof NamedNode) {
			if (tr.getPredicate().getURI() != null) {
				if ( ! inputNodes.contains(tr.getSubject()) && ! inputNodes.contains(tr.getObject()) ) {
					if ( !tr.getPredicate().toString().equals("rdf:type") && !contextNodes.contains( tr.getObject() ) ){
						outputPatterns.add(tr); //[rdf(v16, hypersonicsV2:machSpeed, v15)]
					}
				}
			}
		}
	}
}


private void getOutputDocContextPatterns(TripleElement[] triples, List<Node> inputNodes,
		List<TripleElement> outputPatterns, List<TripleElement> docPatterns, List<TripleElement> contextPatterns) {
	for (int i = 0; i < triples.length; i++) {
		TripleElement tr = triples[i];
		if (tr.getSubject() instanceof NamedNode) {
			if (tr.getPredicate().getURI() != null) {
				if ( ! inputNodes.contains(tr.getSubject()) && ! inputNodes.contains(tr.getObject()) ) {
					if (tr.getPredicate().toString().equals("rdf:type")){
						contextPatterns.add(tr); //rdf(v16, rdf:type, hypersonicsV2:CF6)
					}
					else { //if (! (tr.getObject() instanceof Literal)) {
						outputPatterns.add(tr); //[rdf(v16, hypersonicsV2:machSpeed, v15)]
					}
				}
			} else { //property is null  
				docPatterns.add(tr);
			}
		}
	}
}

private void inferDependencyGraph(Resource resource) throws Exception {
	System.out.print("Dependency graph inference: ");
//	long startTime = System.currentTimeMillis();
//	// Insert dependency graph
//	runInference(resource, GENERICIOs, CHECK_GENERICIOs);
	
//	//String tmp = DEPENDENCY_GRAPH_INSERT
//	runInference(resource, DEPENDENCY_GRAPH_INSERT,CHECK_DEPENDENCY);
//	long endTime = System.currentTimeMillis();
//	System.out.println((endTime - startTime)/1000.0 + " secs" );

	String retrieveGraphQry = "CONSTRUCT {?s ?p ?o} FROM <http://aske.ge.com/turbo> WHERE {?s ?p ?o}";

	SparqlEndpointInterface sei = SparqlEndpointInterface.getInstance(SparqlEndpointInterface.FUSEKI_SERVER, "http://leb1acdev.hpc.ge.com:3030/ML4M", "http://aske.ge.com/turbo");
	String res = sei.executeQueryToRdf(retrieveGraphQry);

	if (res != null) {
		System.out.print("Graph retrieved");
	}

}

/**
 * 
 * @param cgJson
 * @return
 * @throws DialogInferenceException 
 */
private JsonObject generateKChainSensitivityJson(String cgJson) throws DialogInferenceException {
	JsonObject jo;
	JsonObject keoJson = null;
	try {
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(cgJson);
		keoJson = je.getAsJsonObject();
		jo = keoJson.get("visualize").getAsJsonObject();
		jo.remove("plotType");
		jo.addProperty("plotType","2");


//		jo = jp.parse(mach).getAsJsonObject();
	}
	catch (Throwable t) {
		throw new DialogInferenceException(t.getMessage(), t);
	}

	
	return jo;
}

private JsonObject generateKChainExecJson(String cgJson) throws DialogInferenceException {
	String keo = "{\n" + 
			"    \"modelName\": \"getResponse\",\n" + 
			"  \"outputVariables\": [\n" + 
			"    {";
	keo +=  " \"name\": \"fsmach\", \"type\": \"float\"";
	
	keo += "   }\n" + 
			"  ],\n" + 
			"  \"inputVariables\": [\n" + 
			"    {";
	keo += "      \"name\": \"u0d\",\n" + 
			"      \"type\": \"float\",\n" + 
			"        \"value\": \"300.0\" }, "+
			"    {  \"name\": \"altd\",\n" + 
			"      \"type\": \"float\",\n" + 
			"        \"value\": \"20000.0\" ";
	keo += "}]}";
	
//	String mach = "{\"modelName\":\"getResponse\",\"outputVariables\":[{\"unit\":\"\",\"name\":\"fsmach\",\"alias\":\"MachSpeed\",\"type\":\"float\"}],\"inputVariables\":[{\"name\":\"altd\",\"alias\":\"Altitude\",\"type\":\"float\",\"value\":\"30000\",\"minValue\":\"0\",\"maxValue\":\"60000\"},{\"name\":\"u0d\",\"alias\":\"Speed\",\"type\":\"float\",\"value\":\"500\",\"minValue\":\"0\",\"maxValue\":\"1500\"}],\"plotType\":\"2\"}";
	
	JsonObject jo;
	JsonObject keoJson = null;
	try {
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(cgJson);
		keoJson = je.getAsJsonObject();
		jo = keoJson.get("eval").getAsJsonObject();
		jo.addProperty("plotType","2");

//		jo = jp.parse(mach).getAsJsonObject();
	}
	catch (Throwable t) {
		throw new DialogInferenceException(t.getMessage(), t);
	}

	
	return jo;
}


/**
 * Method to generate the JSON string needed to build in K-CHAIN
 * @param cgJson -- input ??
 * @return -- the requested JSON as a String 
 * @throws DialogInferenceException 
 */
private JsonObject generateKChainBuildJson(String cgJson) throws DialogInferenceException {
	JsonObject jo;
	JsonObject keoJson = null;
	try {
		JsonParser jp = new JsonParser();
		JsonElement je = jp.parse(cgJson);
		keoJson = je.getAsJsonObject();
		jo = keoJson.get("build").getAsJsonObject();
	}
	catch (Throwable t) {
		throw new DialogInferenceException(t.getMessage(), t);
	}
	return jo;
}

/**
 * Method to build a K-CHAIN model from the input JSON object
 * @param kchainJsonObj -- the JsonObject created for the build service call
 * @return -- the json response
 */
private String buildKChain(JsonObject kchainJsonObj) throws MalformedURLException, IOException, DialogInferenceException {
try {
		String serviceURL = getPreference(DialogPreferences.ANSWER_KCHAIN_CG_SERVICE_BASE_URI.getId());
		KChainServiceInterface kcsi = new KChainServiceInterface(serviceURL);
		return kcsi.buildCGModel(kchainJsonObj);
	}
	catch (Throwable t) {
		throw new DialogInferenceException(t.getMessage(), t);
	}
}

/**
 * Method to process the K-CHAIN build service call
 * @param kchainResultsJson -- the json response from a build call
 * @return -- true if successful else false
 */
private boolean getBuildKChainOutcome(String kchainResultsJson) {
	// TODO Auto-generated method stub
	return true;
}

/**
 * Method to execute an eval service call to the K-CHAIN computational graph
 * @param kchainJsonObj -- the JsonObject created for the eval service call
 * @return -- the json response from the service eval service call
 */
private String executeKChain(JsonObject kchainJsonObj) throws MalformedURLException, IOException, DialogInferenceException {
	try {
		String serviceURL = getPreference(DialogPreferences.ANSWER_KCHAIN_CG_SERVICE_BASE_URI.getId());
		KChainServiceInterface kcsi = new KChainServiceInterface(serviceURL);
		return kcsi.evalCGModel(kchainJsonObj);
	}
	catch (Throwable t) {
		throw new DialogInferenceException(t.getMessage(), t);
	}
}

private String execKChainSensitivity(JsonObject kchainJsonObj) throws MalformedURLException, IOException, DialogInferenceException {
	try {
		String serviceURL = getPreference(DialogPreferences.ANSWER_INVIZIN_SERVICE_BASE_URI.getId());
		InvizinServiceInterface insi = new InvizinServiceInterface(serviceURL);
		return insi.visualize(kchainJsonObj);
	}
	catch (Throwable t) {
		throw new DialogInferenceException(t.getMessage(), t);
	}
}


/**
 * Method to process the json response from the K-CHAIN eval service call
 * @param kchainResultsJson -- the json response from the eval service call
 * @return -- the processed response
 */
private String getEvalKChainOutcome(String kchainResultsJson) {
    JsonParser parser = new JsonParser();
    String jres = null;
    if (!kchainResultsJson.equals("")) {
        JsonElement jsonTree = parser.parse(kchainResultsJson);
        JsonObject jsonObject;
        //String jres = null;

        if (jsonTree.isJsonObject()) {
            jsonObject = jsonTree.getAsJsonObject();
            if (jsonObject.has("outputVariables")) 
                jres = "Success"; //jsonObject.get("outputVariables").getAsString().trim();
        }
    }
    return jres;
}

/**
 * 
 * @param sensitivityResult
 * @return
 * @throws IOException 
 */
private String getVisualizationURL(String sensitivityResult) throws IOException {
	JsonElement je = new JsonParser().parse(sensitivityResult);
	if (je.isJsonObject()) {
		JsonObject jobj = je.getAsJsonObject();
		String visualizationUrl = jobj.get("url").getAsString();
		return visualizationUrl;
	}
	else {
		throw new IOException("Unexpected response: " + je.toString());
	}
}



private ResultSet[] processModelsFromDataset(Resource resource, TripleElement[] triples, String queryModelFileName,
	String queryModelURI, String queryModelPrefix, String queryOwlFileWithPath, List<RDFNode> inputsList,
	List<RDFNode> outputsList, List<String> contextClassList, Individual cgq) throws TranslationException,
	Exception, FileNotFoundException, IOException, URISyntaxException, ConfigurationException {

	org.apache.jena.query.ResultSetRewindable eqnsResults = null;
	QueryExecution qexec = null;
	OntResource qtype;
	OntProperty qtypeprop = getTheJenaModel().getOntProperty(METAMODEL_QUERYTYPE_PROP);
	String listOfEqns = "";
	org.apache.jena.query.ResultSetRewindable models, nodes;
	String modelsJSONString = "";
	String nodesJSONString = "";
	Individual cgIns = null;

	OntClass cexec;
	Individual ce;
	OntProperty execprop;

	Map<String,String> class2lbl, lbl2class;
	Map<String,String[]> lbl2value;
	Map<String,String> class2units;

	String cgJson, dbnJson, dbnResultsJson;

	String outputType;
	Individual oinst; 

	Map<RDFNode, String[]> dbnEqnMap = new HashMap<RDFNode,String[]>();
	Map<RDFNode, RDFNode> dbnOutput = new HashMap<RDFNode,RDFNode>();

	ResultSet[] dbnResults = null;

	ConfigurationManagerForIDE cmgr = getConfigMgrForIDE(resource);

	List<ResultSet> resultsList = new ArrayList<ResultSet>();

	qtype = getTheJenaModel().getOntResource(METAMODEL_PREFIX + "explanation");
	//qhmodel.add(cgq, qtypeprop, qtype);	// do we add it even if we don't know if there's a model yet?
	ingestKGTriple(cgq, qtypeprop, qtype);
	inputsList.addAll(outputsList);

	//results = new ResultSet[outputsList.size()*2]; 

	int numOfModels = 0;
	List<RDFNode> outpl = new ArrayList<RDFNode>();
	List<RDFNode> inpl = new ArrayList<RDFNode>();
	RDFNode oputType;

	for(int i=0; i < outputsList.size(); i++) {

		oputType = outputsList.get(i);
		outpl.add(oputType);
		inpl.addAll(outputsList);
		inpl.remove(i);

		//Individual oinst = createIndividualOfClass(qhmodel, oclass.getURI());
		//outputInstance.put(oclass,oinst);

		eqnsResults = retrieveCG(resource, inpl, outpl);

		//int ns = eqnsResults.size();
		//boolean nn = eqnsResults.hasNext();

		dbnEqnMap = createDbnEqnMap(eqnsResults);
		dbnOutput = createDbnOutputMap(eqnsResults);

		int newModels = getNumberOfModels(dbnEqnMap);

		String[] modelEqnList = buildEqnsLists(newModels, dbnEqnMap);

		//results = new ResultSet[outputsList.size()*numOfModels*2]; 

		//if (eqnsResults.size() > 0 ) {
		for(int mod=0; mod<newModels; mod++) {

			listOfEqns = modelEqnList[mod]; //getEqnUrisFromResults(eqnsResults);
			eqnsResults.reset();

			ce = createExecInstance(cgq);

			cgIns = queryModel.createIndividual(queryModelPrefix + "CG_" + System.currentTimeMillis(), getTheJenaModel().getOntClass(METAMODEL_CCG));
			//getTheJenaModel().add(cgIns, RDF.type, getTheJenaModel().getOntClass(METAMODEL_CCG));
			//qhmodel.add(ce, getTheJenaModel().getOntProperty(METAMODEL_COMPGRAPH_PROP), cgIns);
			ingestKGTriple(ce, getModelProperty(getTheJenaModel(), METAMODEL_COMPGRAPH_PROP), cgIns);

			// Retrieve Models & Nodes
			modelsJSONString = retrieveCGforDBNSpec(resource, listOfEqns, contextClassList, cgIns, RETRIEVE_MODELS);
			nodesJSONString = retrieveCGforDBNSpec(resource, listOfEqns, null, cgIns, RETRIEVE_NODES);



			String docUri = triples[0].getSubject().getURI();


			String nodesModelsJSONStr = "{ \"models\": {" + modelsJSONString + "}, \"nodes\": {"  + nodesJSONString + "} }";


			cgJson = kgResultsToJson(nodesModelsJSONStr, "prognostic", getDataForHypothesisTesting(resource, docUri));
			dbnJson = generateDBNjson(cgJson);
			class2lbl = getClassLabelMapping(dbnJson);

			
			class2units = getClassUnitsMappingFromModelsJson(nodesModelsJSONStr);

			
			// Run the model
			dbnResultsJson = executeDBN(dbnJson);
			String resmsg = getDBNoutcome(dbnResultsJson);
			//boolean suc = resmsg.equals("Success");

			if(resmsg != null && resmsg.equals("Success")) {

				numOfModels ++;

				AnswerCurationManager acm = (AnswerCurationManager) cmgr.getPrivateKeyMapValueByResource(DialogConstants.ANSWER_CURATION_MANAGER, resource.getURI());

				lbl2value = getLabelToMeanStdMapping(dbnResultsJson);
				createCGsubgraphs(cgIns, dbnEqnMap, dbnOutput, listOfEqns, class2lbl, lbl2value, class2units, queryModelPrefix);

				//createCEoutputInstances(outputsList, ce, class2lbl, lbl2class, lbl2value, class2units);

				//							
				//							//TODO: create output instances and link them to ce
				//							//      There is only one output for datasets
				//							outputType = oputType.as(OntProperty.class).getRange().toString();
				//							oinst = createIndividualOfClass(queryModel,null, null, outputType);
				//							OntProperty outputprop = getTheJenaModel().getOntProperty(METAMODEL_OUTPUT_PROP);
				//							ingestKGTriple(ce,outputprop,oinst);


				//Ingest model error
				String modelError = getModelError(dbnResultsJson);
				queryModel.add(cgIns, getModelProperty(getTheJenaModel(), MODELERROR_PROP), modelError);
				//getTheJenaModel().add(cgIns, errorProp, modelError);

				saveMetaDataFile(resource,queryModelURI,queryModelFileName); //so we can query the the eqns in the CCG
				String assumpCheck = checkAssumptions(resource, queryModelURI, queryOwlFileWithPath, cgIns);

				//We don't do anything with the result of assumpCheck (it is already ingested), we don't check sensitivity for hypothesis 
				if ( assumpCheck.equals("satisfied") ) {
					//Do nothing
				}


				resultsList.add(numOfModels-1, retrieveCompGraph(resource, cgIns));

				addResultsToDialog(resource, cgIns, acm);

				//resultsList.add(numOfModels*2-1, retrieveValuesHypothesis(resource, cgIns));

				// create ResultSet
				//						results[i] = retrieveCompGraph(resource, cgIns);
				//						results[i+1] = retrieveValuesHypothesis(resource, cgIns);
			} else {
				System.err.println("DBN execution failed. Message: " + dbnResultsJson.toString());
			}
		}
	}

	if(numOfModels > 0) {
		addQueryModelAsResource(resource, queryModelFileName, queryModelURI, queryOwlFileWithPath, cmgr);
		dbnResults = new ResultSet[numOfModels];
		dbnResults = resultsList.toArray(dbnResults);
	} else {
		dbnResults = null;
	}

	return dbnResults;
}











private void getInputPatterns(TripleElement[] triples, List<TripleElement[]> inputPatterns, List<Node> inputNodes) {
	TripleElement[] quantity;
	for (int i = 0; i < triples.length; i++) {
		TripleElement tr = triples[i];
		if (tr.getSubject() instanceof NamedNode) {
			if (tr.getPredicate().getURI() != null && tr.getPredicate().getURI().equals(SadlConstants.SADL_IMPLICIT_MODEL_VALUE_URI)) { //input value triple (v1, sadlimplicitmodel:value, 35000)
				quantity = createUQtriplesArray(tr,triples);
				inputPatterns.add(quantity);
				inputNodes.add(tr.getSubject());
			}
		}
	}
}

private void runInference(Resource resource, String query, String testQuery) throws SadlInferenceException, ConfigurationException, ReasonerNotFoundException, InvalidNameException, QueryParseException, QueryCancelledException, AmbiguousNameException {

//	UpdateAction.parseExecute(query , getTheJenaModel()); // use runReasonerQuery instead

	runReasonerQuery(resource, query);
	
	if(debugMode) {
		ResultSet insertTest = runReasonerQuery(resource, testQuery);
		if (insertTest == null || !insertTest.hasNext()) {
			throw new SadlInferenceException("Inference execution failed for " + query);
		}
	}
}
	


	private ConfigurationManagerForIDE getConfigMgrForIDE(Resource resource) {
		ConfigurationManagerForIDE cmgr = null;
		try {
			//String p = getModelFolderPath(resource);
			cmgr = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getModelFolderPath(resource), ConfigurationManagerForIDE.getPersistenceFormatFromPreferences());
		} catch (ConfigurationException e1) {
			e1.printStackTrace();
		}
		return cmgr;
	}

	private void ingestInputValues(String queryModelPrefix, List<TripleElement[]> inputPatterns, List<TripleElement> contextPatterns, Individual cgq) {
		String ss;
		String sp;
		String so;
		String ns;
		org.apache.jena.rdf.model.Resource sss, ssc;
		Property ssp;
		RDFNode sso;
		Statement jtr;
		TripleElement itr;
		// ingest input values
		if (inputPatterns != null && inputPatterns.size() > 0) {
			for (int i = 0; i < inputPatterns.size(); i++) {
				
				itr = inputPatterns.get(i)[0]; //e.g. itr = (v0 altitude v1)
				//if (itr.getSubject().equals(commonSubject)) {
				//NamedNode subj = (NamedNode) getTheJenaModel().getOntClass(itr.getSubject().getURI());

				ss = itr.getSubject().getName();
				sp = itr.getPredicate().getURI();
				so = itr.getObject().getName();
				
				//ns = getModelName() + "#";
				ns = queryModelPrefix;
				
				sss = getTheJenaModel().getResource(ns+ss);
				ssp = getTheJenaModel().getProperty(sp);
				sso = getTheJenaModel().getResource(ns+so);

				ssc = getInputClass(ss, contextPatterns);
				
				//org.apache.jena.rdf.model.Resource foo = qhmodel.getResource(ns+so+cgq.ge);
				
				// Add property to list of inputs
//				inputsList.add(ssp);
				
				if(ssc == null) {
					continue;
				}

				
				ingestKGTriple(sss, ssp, sso); //(:v0 :altitude :v1)
				ingestKGTriple(sss, RDF.type, ssc); //v0 rdf:type :CF6)
				
				
				// create triple: cgq, mm:input, inputVar
				OntProperty inputprop = getModelProperty(getTheJenaModel(), METAMODEL_INPUT_PROP); // getTheJenaModel().getOntProperty(METAMODEL_INPUT_PROP);
				if (inputprop == null) {
					System.err.println("Can't find property '" + METAMODEL_INPUT_PROP + "'; is the SadlImplicitModel out of date?");
					return;
				}
				ingestKGTriple(cgq, inputprop, sso); //(bnode, mm:input, v1)

				
				//ingest value triple
				itr = inputPatterns.get(i)[1]; //e.g. itr = (v1 :value 30000)

				ss = itr.getSubject().getName();
				sss = getTheJenaModel().getResource(ns+ss);
				sp = itr.getPredicate().getURI();
				ssp = getTheJenaModel().getProperty(sp);
				RDFNode val = getObjectAsLiteralOrResource(itr.getPredicate(), itr.getObject());
				
				ingestKGTriple(sss, ssp, val);
				//ingestKGTriple(sss, getTheJenaModel().getProperty(VALUE_PROP), val); // (#v1, #value, 30000^^decimal )
				
				//ingest units triple
				itr = inputPatterns.get(i)[2]; //e.g. itr = (v1, units, "ft")
				if (itr != null) {
					sp = itr.getPredicate().getURI();
					ssp = getTheJenaModel().getProperty(sp);
					sso = getObjectAsLiteralOrResource(itr.getPredicate(), itr.getObject());
					ingestKGTriple(sss, ssp, sso); // (#v1, #unit, ft^^string )
					//ingestKGTriple(sss, getTheJenaModel().getProperty(UNIT_PROP), sso); // (#v1, #unit, ft^^string )
				}
				
				itr = inputPatterns.get(i)[3]; // (v1 rdf:type :Altitude)
				if (itr != null) {
					ss = itr.getSubject().getName();
					sss = getTheJenaModel().getResource(ns+ss);
					sp = itr.getPredicate().getURI();
					ssp = getTheJenaModel().getProperty(sp);
					so = itr.getObject().getNamespace() + itr.getObject().getName();
					sso = getTheJenaModel().getResource(so);
					ingestKGTriple(sss, ssp, sso);
				}
			}

		}
	}


	
	/**
	 * 
	 * @param ss
	 * @param contextPatterns
	 * @param queryModelPrefix 
	 * @return
	 */
	private org.apache.jena.rdf.model.Resource getInputClass(String ss, List<TripleElement> contextPatterns) {
		org.apache.jena.rdf.model.Resource c=null;
		Node n=null;
		for(int i=0; i<contextPatterns.size(); i++) {
			n = contextPatterns.get(i).getSubject();
			if(n.toString().equals(ss)) {
				c = getTheJenaModel().getResource(contextPatterns.get(i).getObject().getURI());
			}
		}
		return c;
	}

	private void addTimePropertyToCE(Individual execInstance, OntProperty timeProperty) throws TranslationException {
		String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
		Calendar cal = Calendar.getInstance();
		SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
		String time = sdf.format(cal.getTime());
		OntResource rng = timeProperty.as(OntProperty.class).getRange();
		org.apache.jena.rdf.model.Literal timeLiteral = SadlUtils.getLiteralMatchingDataPropertyRange(getTheJenaModel(), rng.getURI(), time);
		ingestKGTriple(execInstance, timeProperty, timeLiteral);
	}

	private void addQueryModelAsResource(Resource resource, String queryModelFileName, String queryModelURI,
			String queryOwlFileWithPath, ConfigurationManagerForIDE cmgr)
			throws IOException, URISyntaxException, ConfigurationException {
		String projectName = new File(getModelFolderPath(resource)).getParentFile().getName(); // ASKE_P2
		
		Resource newRsrc = resource.getResourceSet()
				.createResource(URI.createPlatformResourceURI(projectName + File.separator + CGMODELS_FOLDER + File.separator + queryModelFileName + ".owl" , false));
		newRsrc.load(resource.getResourceSet().getLoadOptions());
		JenaBasedSadlModelProcessor.refreshResource(newRsrc);
		String owlURL = new SadlUtils().fileNameToFileUrl(queryOwlFileWithPath);
		cmgr.addMapping(owlURL, queryModelURI, "", true, "JBDIP");
		cmgr.addJenaMapping(queryModelURI, owlURL);	
		
		AnswerCurationManager acm = (AnswerCurationManager) cmgr.getPrivateKeyMapValueByResource(DialogConstants.ANSWER_CURATION_MANAGER, resource.getURI());
		acm.addDelayedImportAddition("import \"" + queryModelURI + "\".");
	}

	private void addSensitivityResultsToDialog(Resource resource, Individual cgIns, AnswerCurationManager acm) throws Exception {
		ResultSet sensres = retrieveSensitivityResults(resource, cgIns);
		String outp = "", nxtoutp = "";
		//int opcount = 0;
		if (sensres != null && sensres.getRowCount() > 0) {
			StringBuilder sb = new StringBuilder("The CCG " + cgIns.getLocalName() );

			for (int row = 0; row < sensres.getRowCount(); row++) {
				nxtoutp = sensres.getResultAt(row, 0).toString();
				if (!nxtoutp.equals(outp)) {
					if (!outp.equals("")) {
						sb.append("\n  )");
					}
					sb.append("\n  has sensitivity (a Sensitivity output " + nxtoutp);
					outp = nxtoutp;
					//opcount++;
				}
				sb.append("\n      inputSensitivity (a InputSensitivity input ");
				sb.append(sensres.getResultAt(row, 1).toString()); //input
				sb.append(", sensitivityValue ");
				String v = sensres.getResultAt(row, 2).toString();
				if (v.equals("NaN")) {
					sb.append("0");
				}
				else {
					sb.append(v);
				}
				sb.append(")");
				
			}
			if (!nxtoutp.equals("")) {
				sb.append("\n  ).\n");
			}
			SadlStatementContent ssc = new SadlStatementContent(null, Agent.CM, sb.toString());
			if (debugMode) {System.out.println(ssc.toString());}
			//acm.notifyUser(getModelFolderPath(resource), ssc, false);
		}
	}

	private List<String> addResultsToDialog(Resource resource, Individual cgIns, AnswerCurationManager acm) throws Exception {
		ResultSet rs = retrieveValues(resource, cgIns);
		List<String> sadlDeclaration = new ArrayList<String>();
		if (rs.getRowCount() > 0) {
			StringBuilder sb = new StringBuilder("The CGExecution with compGraph ");
			sb.append(rs.getResultAt(0, 0).toString());
			sb.append(System.lineSeparator());
			for (int row = 0; row < rs.getRowCount(); row++) {
				//StringBuilder sb = new StringBuilder("The CGExecution ");
				sb.append("    has output (a ");
				sb.append(rs.getResultAt(row, 1).toString());
				sb.append(" with ^value ");
				sb.append(rs.getResultAt(row, 2));
				if (rs.getResultAt(row, 3) != null) {
					sb.append(", with stddev ");
					sb.append(rs.getResultAt(row, 3));
				}
				sb.append(")");
				sb.append(System.lineSeparator());
			}
			sb.append(".");
			sb.append(System.lineSeparator());
			sadlDeclaration.add(sb.toString());
		}							
		for (String sd : sadlDeclaration) {
			SadlStatementContent ssc = new SadlStatementContent(null, Agent.CM, sd);
//			AnswerCurationManager acm = (AnswerCurationManager) cmgr.getPrivateKeyValuePair(DialogConstants.ANSWER_CURATION_MANAGER);
			//System.out.println(ssc.toString());
			acm.notifyUser(getModelFolderPath(resource), ssc, false);
		}
		return sadlDeclaration;
	}

	private String checkAssumptions(Resource resource, String queryModelURI, String queryOwlFileWithPath, Individual cgIns) throws Exception {
		ResultSet assumpCheck = null;
		assumpCheck = checkModelAssumptions(resource, cgIns.toString(), queryModelURI, queryOwlFileWithPath);
		if (assumpCheck != null) {
			
			StringBuilder sb = new StringBuilder("The CCG " + cgIns.getLocalName() + " has assumptionsSatisfied ");
			//StringBuilder sb = new StringBuilder("the CCG " + cgIns.getLocalName() + " has assumptionsSatisfied ");
			if( assumpCheck.getResultAt(0, 0).equals("satisfied")) {
				sb.append("true");
				RDFNode obj = getTheJenaModel().createTypedLiteral(true);
				ingestKGTriple(cgIns, getTheJenaModel().getOntProperty(METAMODEL_ASSUMPTIONSSATISFIED_PROP), obj);
			} else {
				sb.append(" false unsatisfiedAssumption \"");
				sb.append(assumpCheck.getResultAt(0, 1).toString());
				sb.append("\"");
				String trace = assumpCheck.getResultAt(0, 1).toString();
				trace = trace.substring(1, trace.length()).replaceAll("'", " ");
				trace = "\"" + trace + "\"";
				
				RDFNode obj = getTheJenaModel().createTypedLiteral(trace);
				ingestKGTriple(cgIns, getTheJenaModel().getOntProperty(METAMODEL_ASSUMPTIONUNSATISFIED_PROP), obj);
			}
			sb.append(".");
			//sadlDeclaration.add(sb.toString());
			SadlStatementContent ssc = new SadlStatementContent(null, Agent.CM, sb.toString());
			if (debugMode) {System.out.println(ssc.toString());}
			//acm.notifyUser(getModelFolderPath(resource), ssc, false);
		}
		return assumpCheck.getResultAt(0, 0).toString();
	}

	private OntClass getModelClass(OntModel model, String uri) {
		OntClass res = model.getOntClass(uri);
		if (res == null) {
			System.err.println("Can't find property '" + uri + "'. Something is wrong.");
			return null;
		} else {
			return res;
		}
	}
	private OntProperty getModelProperty(OntModel model, String uri) {
		OntProperty res = model.getOntProperty(uri);
		if (res == null) {
			System.err.println("Can't find property '" + uri + "'. Something is wrong.");
			return null;
		} else {
			return res;
		}
	}

	private int getNumberOfModels(Map<RDFNode, String[]> dbnEqns) {
		int numOfModels = 1;
		if (dbnEqns.isEmpty())
			numOfModels = 0;
		for (RDFNode dbnk :  dbnEqns.keySet()) {
			numOfModels *= dbnEqns.get(dbnk).length;
		}
		return numOfModels;
	}

	private void createCEoutputInstances(List<RDFNode> outputsList, Individual ce, Map<String, String> class2lbl, Map<String,String> lbl2class, Map<String, String[]> lbl2value, Map<String, String> class2units) {
		
		OntProperty outputprop = getTheJenaModel().getOntProperty(METAMODEL_OUTPUT_PROP);
		String oType;
		
//		for(RDFNode outType : outputsList) {
//			Individual outpIns = createIndividualOfClass(queryModel, null, null, outType.toString());
//			ingestKGTriple(ce, outputprop, outpIns);
//
//			if( class2lbl.containsKey(outType.toString())) {
//				String cls = class2lbl.get(outType.toString());
//					
//			if(class2units.containsKey(outType.toString())) {
//				String unit = class2units.get(outType.toString());
//				queryModel.add(outpIns, getTheJenaModel().getProperty(UNIT_PROP), unit);
//			}
//			String[] ms = lbl2value.get(cls);  //class2lbl.get(o.toString()));
//			queryModel.add(outpIns, getTheJenaModel().getProperty(VALUE_PROP), ms[0] );
//			if(ms[1] != null)
//				queryModel.add(outpIns, getTheJenaModel().getProperty(STDDEV_PROP), ms[1] );
//			if(ms[2] != null)
//				queryModel.add(outpIns, getTheJenaModel().getProperty(VARERROR_PROP), ms[2] );
//			}
//		}
		
		for( String lbl : lbl2value.keySet()) {
			oType = null;
			if(lbl2class.containsKey(lbl) ) {
				String c = lbl2class.get(lbl);
				for(RDFNode o : outputsList) {
					if(o.toString().equals(c)) {
						oType = c;
					}
				}
				if(oType == null) {
					continue;
				}
			}
			else {
				oType = outputsList.get(0).toString(); //we don't know the class for the label, assume is the outputList class. Only works with single output.
			}
			Individual outpIns = createIndividualOfClass(queryModel, null, null, oType);
			ingestKGTriple(ce, outputprop, outpIns);
			
			if(class2units.containsKey(oType)) {
				String unit = class2units.get(oType);
				queryModel.add(outpIns, getTheJenaModel().getProperty(UNIT_PROP), unit);
			}
			String[] ms = lbl2value.get(lbl);  //class2lbl.get(o.toString()));
			queryModel.add(outpIns, getTheJenaModel().getProperty(VALUE_PROP), ms[0] );
			if(ms[1] != null) {
				queryModel.add(outpIns, getTheJenaModel().getProperty(STDDEV_PROP), ms[1] );
			}
			if(ms[2] != null) {
				queryModel.add(outpIns, getTheJenaModel().getProperty(VARERROR_PROP), ms[2] );
			}
		}
	}

	private String retrieveCGforDBNSpec(Resource resource, String listOfEqns, List<String> contextClassList, Individual cgIns, String queryTemplate) throws SadlInferenceException, ConfigurationException, ReasonerNotFoundException, InvalidNameException, QueryParseException, QueryCancelledException, AmbiguousNameException {
		String queryStr;
//		org.apache.jena.query.ResultSetRewindable rset;
//		ResultSet resultSet;
		//Object[] abridgedRes = new Object[rset.size()];
		String resStr;
		String contextClasses = "";
		queryStr = queryTemplate.replaceAll("EQNSLIST", listOfEqns);
		queryStr = queryStr.replaceAll("COMPGRAPH", "<" + cgIns.getURI() + ">");
		if (contextClassList != null) {
			for(int i=0; i<contextClassList.size(); i++) {
				contextClasses += "<" + contextClassList.get(i) + ">, ";
			}
			contextClasses=contextClasses.substring(0, contextClasses.length()-2);
		}
		queryStr = queryStr.replaceAll("CONTEXCLASSES", contextClasses);
//		rset = queryKnowledgeGraph(queryStr, getTheJenaModel().union(queryModel));
		
//		resultSet = runReasonerQuery(resource, queryStr);
		
//		QuerySolution res;
//		RDFNode model = null;
//		int i=0;
//		while (rset.hasNext()) {
//			res = rset.next();
//			if (model == null) {
//				model = res.get("model");
//			} else if (res.get("model") == model) {
//				
//			}
//			
//		}
		
//		resStr = convertResultSetToString(rset);
//		if (debugMode) {System.out.println(resStr);}
		
//		resStr = resultSet.toString();
		
		resStr = runReasonerQueryJson(resource, queryStr);
		
		return resStr;
	}





private ResultSet retrieveSensitivityResults(Resource resource, Individual cgIns) throws Exception {
	String query = SENSITIVITYQUERY.replaceAll("COMPGRAPH", "<" + cgIns.getURI() + ">");
	ResultSet res = runReasonerQuery(resource, query);
	return res;
	}

private ResultSet checkModelAssumptions(Resource resource, String model, String instanceDataURI, String queryOwlFileWithPath) throws Exception {
		//String query = "select Eq Var Oper Val VP where assumption(Eq,Var,Oper,Val,VP).";
		String query = "select Res Trace where model_satisfies_assumptions('" + model + "', Res, Trace)";
		ResultSet result = runPrologQuery(resource, query, instanceDataURI, queryOwlFileWithPath);
		return result;
	}

private TripleElement[] createUQtriplesArray(TripleElement valueTriple, TripleElement[] triples) {
		TripleElement[] quantity = new TripleElement[4];
		Node varNode = valueTriple.getSubject();
		quantity[1] = valueTriple;  // (v1 :value 30000)
		
		for (TripleElement tr : triples) {
			if(tr.getObject() != null && tr.getObject().equals(varNode)) {
				quantity[0] = tr; // (v0 :altitude v1)
			} else if ( tr.getSubject().equals(varNode) && tr.getPredicate().getName().contains("unit")) { // (v1 :unit "ft")
				quantity[2] = tr;
			} else if ( tr.getSubject().equals(varNode) && tr.getPredicate().getName().contains("type")) { // (vi rdf:type :Altitude)
				quantity[3] = tr;
			}
		}
		return quantity;
	}

private RDFNode getObjectAsLiteralOrResource(Node property, Node object) {
		Property prop = getTheJenaModel().getProperty(property.getURI());
		RDFNode obj=null;
		if (object instanceof Literal) {
			if (prop.canAs(OntProperty.class)) {
				OntResource rng = prop.as(OntProperty.class).getRange();
				try {
					obj = SadlUtils.getLiteralMatchingDataPropertyRange(getTheJenaModel(), rng.getURI(), ((Literal)object).getValue());
				} catch (TranslationException e) {
					//  Auto-generated catch block
					e.printStackTrace();
				}
			}
			else {
				obj = getTheJenaModel().createTypedLiteral(((Literal)object).getValue());
			}
		}
		else {
			obj = getTheJenaModel().getResource(object.getURI());
		}

		return obj;
	}

//private TripleElement getInputUnitsTriple(Node varNode, TripleElement[] triples) {
//		for(int i=0; i<triples.length; i++) {
//			if(triples[i].getSubject().equals(varNode) && triples[i].getPredicate().getName().contains("unit")) { // && !triples[i].getPredicate().getName().contains("value")) {
//				return triples[i];
//			}
//		}
//		return null;
//	}
//
//private TripleElement getInputTypeTriple(Node varNode, TripleElement[] triples) {
//		for(int i=0; i<triples.length; i++) {
//			if(triples[i].getObject() != null && triples[i].getObject().equals(varNode)) { // && !triples[i].getPredicate().getName().contains("value")) {
//				return triples[i];
//			}
//		}
//		return null;
//	}

	private org.apache.jena.query.ResultSetRewindable retrieveCG(Resource resource, List<RDFNode> inputsList, List<RDFNode> outputsList) throws SadlInferenceException, ConfigurationException, ReasonerNotFoundException, InvalidNameException, QueryParseException, QueryCancelledException {
		org.apache.jena.query.ResultSet eqns;
		org.apache.jena.query.ResultSet eqnsRes = null;
		String queryStr, inpStr, outpStr;
		org.apache.jena.query.Query qinv;
		QueryExecution qexec;
	
		//		//This is a roundabout way to initialize eqnsRes. There must be a better way?
		//		queryStr = BUILD_COMP_GRAPH.replaceAll("LISTOFOUTPUTS", "").replaceAll("LISTOFINPUTS", "");
		//		//qinv = QueryFactory.create(queryStr);
		//		//qexec = QueryExecutionFactory.create(qinv, getTheJenaModel());
		//		//eqnsRes = qexec.execSelect() ;
		//		eqnsRes = queryKnowledgeGraph(queryStr, getTheJenaModel());
	
	
		for(RDFNode ic : inputsList) {
			for(RDFNode oc : outputsList) {
				inpStr = "<" + ic.toString() + ">";
				outpStr = "<" + oc.toString() + ">";
				queryStr = BUILD_COMP_GRAPH.replaceAll("LISTOFOUTPUTS", outpStr).replaceAll("LISTOFINPUTS", inpStr);
	
				//qinv = QueryFactory.create(queryStr);
				//qexec = QueryExecutionFactory.create(qinv, getTheJenaModel()); 
				//eqns = qexec.execSelect() ;
	
				eqns = queryKnowledgeGraph(queryStr, getTheJenaModel());
	
				//boolean r = eqns.hasNext();
				//System.out.println(r);
				if (!eqns.hasNext()) {
					queryStr = BUILD_COMP_GRAPH.replaceAll("LISTOFOUTPUTS", inpStr).replaceAll("LISTOFINPUTS", outpStr);
					qinv = QueryFactory.create(queryStr);
					qexec = QueryExecutionFactory.create(qinv, getTheJenaModel()); 
					eqns = qexec.execSelect() ;
				}
				if (eqns.hasNext() ) {
					if(eqnsRes == null) {
						eqnsRes = eqns;
					}
					else {
						eqnsRes = org.apache.jena.sparql.util.ResultSetUtils.union(eqnsRes, eqns);
					}
				}
			}
		}
		return org.apache.jena.query.ResultSetFactory.makeRewindable(eqnsRes);
	}

	private ResultSet retrieveCG1(Resource resource, List<RDFNode> inputsList, List<RDFNode> outputsList, boolean inverseQuery) throws Exception, SadlInferenceException, ConfigurationException, ReasonerNotFoundException, InvalidNameException, QueryParseException, QueryCancelledException, AmbiguousNameException {
		String queryStr, inpStr, outpStr;
	
		ResultSet equations = null;
		ResultSet equationsRes = null;
		inverseQuery = false;
	
	
		for(RDFNode ic : inputsList) {
			for(RDFNode oc : outputsList) {
				inpStr = "<" + ic.toString() + ">";
				outpStr = "<" + oc.toString() + ">";
				queryStr = BUILD_COMP_GRAPH.replaceAll("LISTOFOUTPUTS", outpStr).replaceAll("LISTOFINPUTS", inpStr);
	
				//TODO
				queryStr = "prefix hyper:<http://aske.ge.com/hypersonicsV2#>\n"
						+ "prefix imp:<http://sadl.org/sadlimplicitmodel#> \n"
						+ "prefix owl:<http://www.w3.org/2002/07/owl#> \n"
						+ "prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n"
						+ "prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>prefix sci:<http://aske.ge.com/sciknow#>\n"
						+ "prefix cg:<http://aske.ge.com/compgraphmodel#>\n"
						+ "prefix list:<http://sadl.org/sadllistmodel#>\n"
						+ "\n"
						+ "select distinct *\n"
						+ "FROM <http://aske.ge.com/turbo>\n"
						+ "where {\n"
						+ " ?eq rdf:type imp:ExternalEquation.\n"
						+ "}";
				
				
//				SparqlEndpointInterface sei = SparqlEndpointInterface.getInstance(SparqlEndpointInterface.FUSEKI_SERVER, "http://leb1acdev.hpc.ge.com:3030/ML4M", "http://aske.ge.com/turbo");
//				Table tab = sei.executeQueryToTable(queryStr);
//				equations = new ResultSet(tab.getColumnNames(),tab.getDataArray());




//				equations = runReasonerQuery(resource, queryStr);
				
				if (equations == null || !equations.hasNext()) {
					queryStr = BUILD_COMP_GRAPH.replaceAll("LISTOFOUTPUTS", inpStr).replaceAll("LISTOFINPUTS", outpStr);
					equations = runReasonerQuery(resource, queryStr);
					if (equations != null) {
						inverseQuery = true;
					}
				}
	
				if (equations != null && equations.hasNext() ) {
					if(equationsRes == null) {
						equationsRes = equations;
					}
					else {
						String[] headers = equationsRes.getColumnNames();
						Object[][] data1 = equationsRes.getData();
						Object[][] data2 = equations.getData();
	
						int coln = headers.length;
						int len1 = equationsRes.getRowCount();
						int len2 = equations.getRowCount();
						Object[][] data = new Object[len1 + len2][coln]; 
	
						for(int i=0; i<len1 ; i++) {
							for(int j=0; j<coln; j++) {
								data[i][j] = data1[i][j];
							}
						}
						for(int i=0; i<len2 ; i++) {
							for(int j=0; j<coln; j++) {
								data[i+len1][j] = data2[i][j];
							}
						}
						equationsRes = new ResultSet(headers, data);
					}
				}
			}
		}
		return equationsRes;
	}

	
	
	
	private void ingestKGTriple(org.apache.jena.rdf.model.Resource s, Property pred, RDFNode o) {
		queryModel.add(s,pred,o);
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
        String jres = null;
        if (!dbnResultsJson.equals("")) {
	        JsonElement jsonTree = parser.parse(dbnResultsJson);
	        JsonObject jsonObject;
	        //String jres = null;
	
	        if (jsonTree.isJsonObject()) {
	            jsonObject = jsonTree.getAsJsonObject();
	            if (jsonObject.has("modelStatus")) 
	                jres = jsonObject.get("modelStatus").getAsString().trim();
	        }
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
		String[] modelEqnList = null;
		if (numOfModels > 0) {
			modelEqnList = new String[numOfModels];
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
		}
		return modelEqnList;
	}


	private Map<RDFNode, String[]> createOutputEqnMap(ResultSetRewindable eqnsResults) {
		Map<RDFNode, String[]> map = new HashMap<RDFNode, String[]>();
		eqnsResults.reset();
		RDFNode outp;
		QuerySolution row;
		Map<RDFNode, Set<String>> mapSet = new HashMap<RDFNode, Set<String>>();
		String eq;

		while(eqnsResults.hasNext()) {
			row = eqnsResults.nextSolution() ;
			outp = row.get("?Out");
			Set<String> eq_set = new HashSet<String>(); 
			mapSet.put(outp, eq_set);
		}
		eqnsResults.reset();
		while(eqnsResults.hasNext()) {
			row = eqnsResults.nextSolution() ;
			outp = row.get("?Out");
			eq = row.get("?Eq").toString();
			mapSet.get(outp).add(eq);
		}
		for(RDFNode n : mapSet.keySet()) {
			String[] eqns = new String[mapSet.get(n).size()];
			eqns = mapSet.get(n).toArray(eqns);
			map.put(n, eqns );
		}
		return map;
	}

	private Map<RDFNode, String[]> createOutputEqnMap1(ResultSet eqnsResults) {
		Map<RDFNode, String[]> map = new HashMap<RDFNode, String[]>();
		RDFNode outp;
		Map<RDFNode, Set<String>> mapSet = new HashMap<RDFNode, Set<String>>();
		String eq;

		if(eqnsResults != null) {
			int OutColIdx = eqnsResults.getColumnPosition("Out");
			int EqColIdx = eqnsResults.getColumnPosition("Eq");
			
			for(int i=0; i<eqnsResults.getRowCount(); i++) {
				String outpStr = eqnsResults.getResultAt(i, OutColIdx).toString();
				outp = (RDFNode) getTheJenaModel().getResource(outpStr); 
				Set<String> eq_set = new HashSet<String>(); 
				mapSet.put(outp, eq_set);
			}
	
			for(int i=0; i<eqnsResults.getRowCount(); i++) {
				String outpStr = eqnsResults.getResultAt(i, OutColIdx).toString();
				outp = (RDFNode) getTheJenaModel().getResource(outpStr); 
				eq = eqnsResults.getResultAt(i, EqColIdx).toString();
				mapSet.get(outp).add(eq);
			}
			for(RDFNode n : mapSet.keySet()) {
				String[] eqns = new String[mapSet.get(n).size()];
				eqns = mapSet.get(n).toArray(eqns);
				map.put(n, eqns );
			}
		}
		return map;
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

	private ResultSet runPrologQuery(Resource resource, String query, String instanceDataURI, String queryOwlFileWithPath) throws Exception {
		String modelFolder = getModelFolderPath(resource); //getOwlModelsFolderPath(path).toString(); 
		final String format = SadlPersistenceFormat.RDF_XML_ABBREV_FORMAT;
		IConfigurationManagerForIDE configMgr;

		
		configMgr = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(modelFolder, format);
		String prologReasonerClassName = "com.ge.research.sadl.swi_prolog.reasoner.SWIPrologReasonerPlugin";
		IReasoner reasoner = configMgr.getOtherReasoner(prologReasonerClassName);
		//String instanceDatafile = "http://aske.ge.com/MetaData.owl";

		//reasoner.initializeReasoner((new File(modelFolder).getParent()), queryOwlFileWithPath, null);
		reasoner.initializeReasoner((new File(modelFolder).getParent()), instanceDataURI, null);
		
//		assertTrue(pr.loadInstanceData(instanceDatafile));
		loadAllOwlFilesInProject(modelFolder, reasoner);
		
		//String queriesFolder = new File(getModelFolderPath(resource)).getParent() + File.separator + CGMODELS_FOLDER;
		//loadAllOwlFilesInProject(queriesFolder, reasoner);
		
		File queryModelFile = new File(queryOwlFileWithPath);
		reasoner.loadInstanceData(queryModelFile.getCanonicalPath());
	
		//String modelFile = getModelFolderPath(resource) + File.separator + qhOwlFileName;
		//reasoner.loadInstanceData(modelFile);
		
		//String pquery = reasoner.prepareQuery(query);

		ResultSet res = reasoner.ask(query);
		return res;
	}
	
	private boolean loadAllOwlFilesInProject(String owlModelsFolder, IReasoner reasoner) throws IOException, ConfigurationException {
		String[] excludeFiles = {"CodeExtractionModel.owl", "metrics.owl"};
		File omf = new File(owlModelsFolder);
		if (!omf.exists()) {
			throw new IOException("Model folder '" + owlModelsFolder + "' does not exist");
		}
		if (!omf.isDirectory()) {
			throw new IOException("'" + owlModelsFolder + "' is not a folder");
		}
		FilenameFilter filter = new FilenameFilter() {
			public boolean accept(java.io.File dir, String name) {
				if(name != null) {
					return name.endsWith(".owl");
				}else {
					return false;
				}
			}
		};
		File[] owlFiles = omf.listFiles(filter);
		for (File owlFile : owlFiles) {
			boolean exclude = false;
			for (String exf : excludeFiles) {
				if (owlFile.getName().endsWith(exf)) {
					exclude = true;
					break;
				}
			}
			if (exclude) {
				continue;
			}
			reasoner.loadInstanceData(owlFile.getCanonicalPath());
			File plFile = new File(owlFileToPlFile(owlFile));
			if (plFile.exists()) {
				reasoner.loadRules(plFile.getCanonicalPath());
			}
		}
		return true;
	}

	private String owlFileToPlFile(File owlFile) throws IOException {
		String altFN = owlFile.getCanonicalPath();
		String rulefn = altFN.substring(0, altFN.lastIndexOf(".")) + ".pl";
		return rulefn;
	}

	private ResultSet runReasonerQuery(Resource resource, String query) throws SadlInferenceException, ConfigurationException, ReasonerNotFoundException, InvalidNameException, QueryParseException, QueryCancelledException, AmbiguousNameException {
		String modelFolderUri = getModelFolderPath(resource); //getOwlModelsFolderPath(path).toString(); 
		final String format = SadlPersistenceFormat.RDF_XML_ABBREV_FORMAT;
		IConfigurationManagerForIDE configMgr;

		
		configMgr = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(modelFolderUri, format);
//		configMgr.clearReasoner();
		IReasoner reasoner = configMgr.getReasoner();
	
		
		if (!reasoner.isInitialized()) {
			reasoner.setConfigurationManager(configMgr);
			//String modelName = configMgr.getPublicUriFromActualUrl(new SadlUtils().fileNameToFileUrl(modelFolderUri + File.separator + owlFileName));
			//model name something like http://aske.ge.com/metamodel
			//String mname = getModelName();
//			reasoner.initializeReasoner(modelFolderUri, getModelName(), format); 
			reasoner.initializeReasoner(getTheJenaModel(), getModelName(), null, null); 
//			getTheJenaModel().write(System.err,"N3");
			//reasoner.loadInstanceData(qhmodel);
			//System.out.print("reasoner is not initialized");
			//return null;
		}

		
		reasoner.loadInstanceData(queryModel);	//Need to load new metadata
		
//		String family = reasoner.getReasonerFamily();
		
//		String modelFile = "http://aske.ge.com/MetaData.owl";		
//		reasoner.loadInstanceData(modelFile);
		
//		if (family.equals("Jena-Based")) {
//			reasoner.loadInstanceData(qhmodel);	//Need to load new metadata
//			reasoner.loadInstanceData(getTheJenaModel());
//		}
//		else if (family.equals("Prolog-Based")) {
//			String modelFile = getModelFolderPath(resource) + File.separator + qhOwlFileName;
//			String baseModel = getModelFolderPath(resource) + File.separator + getModelName();
//			reasoner.loadRules(baseModel);
//			reasoner.loadRules(modelFile);
//		}
			
//		((Model) reasoner.getInferredModel(false)).write(System.err);
		
		String pquery = reasoner.prepareQuery(query);

		ResultSet res = reasoner.ask(pquery);
		return res;
	}

	private String runReasonerQueryJson(Resource resource, String query) throws SadlInferenceException, ConfigurationException, ReasonerNotFoundException, InvalidNameException, QueryParseException, QueryCancelledException, AmbiguousNameException {
		String modelFolderUri = getModelFolderPath(resource); 
		final String format = SadlPersistenceFormat.RDF_XML_ABBREV_FORMAT;
		IConfigurationManagerForIDE configMgr;

		
		configMgr = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(modelFolderUri, format);
		IReasoner reasoner = configMgr.getReasoner();
	
		
		if (!reasoner.isInitialized()) {
			reasoner.setConfigurationManager(configMgr);
			reasoner.initializeReasoner(getTheJenaModel(), getModelName(), null, null); 
		}

		reasoner.loadInstanceData(queryModel);	//Need to load new metadata
		
		String pquery = reasoner.prepareQuery(query);

		String resJson = reasoner.askJson(pquery);
		return resJson;
	}

	private void saveMetaDataFile(Resource resource, String queryModelURI, String queryModelFileName) {
		String qhOwlFileWithPath = new File(getModelFolderPath(resource)).getParent() + File.separator + CGMODELS_FOLDER + File.separator + queryModelFileName + ".owl";
		

		String qhGlobalPrefix = null;
		try {
			getConfigMgr(null).saveOwlFile(queryModel, queryModelURI, qhOwlFileWithPath);
			String fileUrl = (new UtilsForJena()).fileNameToFileUrl(qhOwlFileWithPath);
			getConfigMgr(null).addMapping(fileUrl, queryModelURI, qhGlobalPrefix, false, "DialogInference"); //Only if new file
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




	private void ingestSensitivityResults(Individual cgIns, Map<String, String> lbl2class, String dbnResultsJsonSensitivity, String prefix) {
		OntProperty sensprop = 	getTheJenaModel().getOntProperty(METAMODEL_SENS_PROP);
		OntProperty outputprop = 	getTheJenaModel().getOntProperty(METAMODEL_OUTPUT_PROP);
		OntProperty inputsensprop = 	getTheJenaModel().getOntProperty(METAMODEL_INPUTSENS_PROP);
		OntProperty inputprop = 	getTheJenaModel().getOntProperty(METAMODEL_INPUT_PROP);
		OntProperty inputsensvalprop = 	getTheJenaModel().getOntProperty(METAMODEL_SENSVALUE_PROP);

        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(dbnResultsJsonSensitivity);
        JsonObject jsonObject, jvalues, jsens;
        JsonArray jinputs, sensvals;
        Set<Entry<String, JsonElement>> sensRes;
        
        String outputlbl, outputType, inputType, v;
        
        Individual sensIns, inputSens;

        if (jsonTree.isJsonObject()) {
            jsonObject = jsonTree.getAsJsonObject();
            jsens = jsonObject.getAsJsonObject("results").getAsJsonObject("sens");
            jinputs = jsens.getAsJsonArray("inputs");

            sensRes = jsens.entrySet();
            
            for ( Entry<String, JsonElement> node : sensRes ) {
            	if (node.getKey().equals("inputs")) {
            		continue;
            	}

            	//sensIns = createIndividualOfClass(queryModel, prefix, node.toString(), METAMODEL_SENSITIVITY);
                //ingestKGTriple(cgIns, sensprop, sensIns);
            	
            	outputlbl = node.getKey(); // "a0"
            	outputType = lbl2class.get(outputlbl); //e.g. "http...#staticTemperature"
            	//op = queryModel.getResource(outputType); //
            	RDFNode otypeProp = getTheJenaModel().getProperty(outputType);
        		String rng = otypeProp.as(OntProperty.class).getRange().toString();
        		OntClass outputClass = getTheJenaModel().getOntClass(rng);

            	sensIns = createIndividualOfClass(queryModel, prefix, outputClass.getLocalName(), METAMODEL_SENSITIVITY);
                ingestKGTriple(cgIns, sensprop, sensIns);
        		
        		ingestKGTriple(sensIns, outputprop, outputClass);
            	
            	jvalues = (JsonObject)node.getValue();
            	sensvals = jvalues.getAsJsonArray("totalEffect") ;
            	
            	for (int i = 0; i<jinputs.size() ; i++) {
            		inputType = lbl2class.get(jinputs.get(i).getAsString());
            		//ip = queryModel.getResource(inputType);
            		RDFNode itypeProp = getTheJenaModel().getProperty(inputType);
            		rng = itypeProp.as(OntProperty.class).getRange().toString();
            		OntClass inputClass = getTheJenaModel().getOntClass(rng);
            		inputSens = createIndividualOfClass(queryModel, null, null, METAMODEL_INPUTSENSITIVITY);
            		v = sensvals.get(0).getAsJsonArray().get(i).getAsString();

            		ingestKGTriple(sensIns, inputsensprop, inputSens);
            		//ingestKGTriple(inputSens, inputprop, ip);
            		ingestKGTriple(inputSens, inputprop, inputClass);
            		queryModel.add(inputSens, inputsensvalprop, v); //sensval should be encoded as proper literal
            	}
            }
        }
	}



		/**
		 * 
		 * 
		 * @param cgIns
		 * @param dbnEqnMap  mapping from DBNs (kc: outputs) to equations
		 * @param dbnOutput  mapping from DBN to its output (kc: null)
		 * @param listOfEqns equations in the model
		 * @param class2lbl  
		 * @param lbl2value
		 * @param class2units 
		 * @param prefix
		 */
		private void createCGsubgraphs(Individual cgIns, Map<RDFNode, String[]> dbnEqnMap, Map<RDFNode, RDFNode> dbnOutput,
				String listOfEqns, Map<String, String> class2lbl, Map<String, String[]> lbl2value, Map<String, String> class2units, String prefix) //, Map<OntClass, Individual> outputInstance)
		{

		OntProperty subgraphprop = 	getTheJenaModel().getOntProperty(METAMODEL_SUBG_PROP);
		OntProperty cgraphprop = 	getTheJenaModel().getOntProperty(METAMODEL_CGRAPH_PROP);
		OntProperty hasEqnProp = 	getTheJenaModel().getOntProperty(METAMODEL_HASEQN_PROP);
		//OntProperty hasModelProp = 	getTheJenaModel().getOntProperty(METAMODEL_HASMODEL_PROP);
		OntProperty outputprop = 	getTheJenaModel().getOntProperty(METAMODEL_OUTPUT_PROP);
		
		
		Individual sgIns, dbnIns, outpIns;
		String rng;
		RDFNode otype, otypeProp;
		
		for( RDFNode dbn : dbnEqnMap.keySet() ) {
			sgIns = createIndividualOfClass(queryModel,null, null, METAMODEL_SUBGRAPH);
			ingestKGTriple(cgIns, subgraphprop, sgIns);
			//This is being created as an instance of a UQ. Needs to be changed.
			//dbnIns = createIndividualOfClass(queryModel,null, null, dbn.toString());
			dbnIns = createIndividualOfClass(queryModel,null, null, CGMODEL_CG_CLASS);
			ingestKGTriple(sgIns, cgraphprop, dbnIns);

			// Find the equation for this dbn (kchain: for this output)
			String[] modelEqns = listOfEqns.split(",");
			String[] deqns = dbnEqnMap.get(dbn); //eqns for dbn (kc: output)
			String eqn=null;
			for(int i=0; i<deqns.length; i++) {
				for(int j=0; j<modelEqns.length; j++) {
					String me = modelEqns[j];
					me = me.substring(1, me.length()-1);
					if( me.equals(deqns[i])) {
						eqn = me;
						break;
					}
				}
				if (eqn != null)
					break;
			}
			RDFNode e = queryModel.getResource(eqn); 
			ingestKGTriple(dbnIns, hasEqnProp, e);
			
			if (dbnOutput != null)	{		
				otype = dbnOutput.get(dbn); //this is a property now 
		
				otypeProp = getTheJenaModel().getProperty(dbnOutput.get(dbn).toString()); //e.g. :altitude
			
				rng = otypeProp.as(OntProperty.class).getRange().toString(); //e.g. :Altitude
			} else {
				otype = dbn;
				rng = dbn.toString();
			}

			
			outpIns = createIndividualOfClass(queryModel, null, null, rng); //e.g. instance of :Altitude
			ingestKGTriple(sgIns, outputprop, outpIns);
			if(class2units.containsKey(otype.toString())) {
				String unit = class2units.get(otype.toString());
				queryModel.add(outpIns, getTheJenaModel().getProperty(UNIT_PROP), unit);
			}
			String varLabel = class2lbl.get(otype.toString());
			String[] ms = lbl2value.get(varLabel);  //class2lbl.get(o.toString()));
			if (ms != null) {
				if (ms[0] != null) // DBN returns values for intermediate nodes, but KChain does not.
					queryModel.add(outpIns, getTheJenaModel().getProperty(VALUE_PROP), ms[0] );
				if (ms[1] != null) 
					queryModel.add(outpIns, getTheJenaModel().getProperty(STDDEV_PROP), ms[1] );
				if(ms[2] != null)
					queryModel.add(outpIns, getTheJenaModel().getProperty(VARERROR_PROP), ms[2] );
			}
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
//                	System.out.println(k.getKey() + "   " + jres.get(k.getKey()).toString());
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

	/**
	 * Extract lable to value mapping from kchain results.
	 * 
	 */
	private Map<String, String[]> getLabelToValueMapping(String dbnResultsJson) {
        Map<String,String[]> lbl2value = new HashMap<String, String[]>();
        JsonParser parser = new JsonParser();
        JsonElement jsonTree = parser.parse(dbnResultsJson);
        JsonObject jsonObject;
        JsonArray jres;
//        Set<Entry<String, JsonElement>> keys;

        if (jsonTree.isJsonObject()) {
            jsonObject = jsonTree.getAsJsonObject();
            if (jsonObject.has("outputVariables")) {
                jres = (JsonArray) jsonObject.get("outputVariables");
                //keys = jres.entrySet();
                
                for(int i=0; i<jres.size(); i++) {
                	JsonObject o = (JsonObject) jres.get(i);
                	String[] vpr = new String[3];
                	vpr[0] = o.get("value").getAsString();
                	lbl2value.put(o.get("name").getAsString(), vpr);
                	
                	
                }
                
//                for (Map.Entry<String, JsonElement> k: keys) {
////                	System.out.println(k.getKey() + "   " + jres.get(k.getKey()).toString());
//                	JsonObject vpair = jres.getAsJsonObject(k.getKey());
//                	String[] vpr = new String[3];
//                	if (vpair.has("mean")) {
//	                	vpr[0] = vpair.get("mean").toString();
//                	}
//                	if (vpair.has("std")) {
//	                	vpr[1] = vpair.get("std").toString();
//                	}	
//                	if (vpair.has("error")) {
//	                	vpr[2] = vpair.get("error").toString();
//                	}	
//                	lbl2value.put(k.getKey(), vpr);
//                }	
            }
        }
        return lbl2value;
	}
	

private Map<String, String> getLabelClassMapping(String dbnJson) {
    JsonParser parser = new JsonParser();
    JsonElement jsonTree = parser.parse(dbnJson);
    Map<String,String> lbl2class = new HashMap<String, String>();
    JsonObject jsonObject;
    JsonObject jres;
    Set<Entry<String, JsonElement>> keys;
    
    if (jsonTree.isJsonObject()) {
        jsonObject = jsonTree.getAsJsonObject();
        //JsonArray jsonRes = (JsonArray) jsonObject.get("results");
        
        jres = (JsonObject) jsonObject.get("mapping");
        keys = jres.entrySet();
        
        for (Map.Entry<String, JsonElement> k: keys) {
        	//System.out.println(k.getKey() + "   " + k.getValue());  //jres.get(k.getKey()).toString());
        	//String foo = k.getValue().getAsString();
        	lbl2class.put(k.getKey(), k.getValue().getAsString());
        }	
    }
    return lbl2class;
}


private Map<String, String> getClassLabelMappingFromModelsJson(String json) {
    JsonParser parser = new JsonParser();
    JsonElement jsonTree = parser.parse(json);
    Map<String,String> class2lbl = new HashMap<String, String>();
    JsonObject jsonObject;
    JsonObject jres;
    Set<Entry<String, JsonElement>> keys;
    
    if (jsonTree.isJsonObject()) {
        jsonObject = jsonTree.getAsJsonObject();
        //JsonArray jsonRes = (JsonArray) jsonObject.get("results");
        
        JsonArray jbindings = (JsonArray) jsonObject.getAsJsonObject("models").getAsJsonObject("results").get("bindings");
        
        for(int i=0; i < jbindings.size(); i++) {
        	JsonObject jo = (JsonObject)jbindings.get(i);
        	if (jo.has("Input") && jo.has("InputLabel")) {
        		String c = jo.getAsJsonObject("Input").get("value").getAsString();
        		String l = jo.getAsJsonObject("InputLabel").get("value").getAsString();
        		class2lbl.put(jo.getAsJsonObject("Input").get("value").getAsString(), jo.getAsJsonObject("InputLabel").get("value").getAsString() );
        	}
        	if (jo.has("ImpInput") && jo.has("ImpInputAugType")) {
        		String c = jo.getAsJsonObject("ImpInputAugType").get("value").getAsString();
        		String l = jo.getAsJsonObject("ImpInput").get("value").getAsString();
        		class2lbl.put(jo.getAsJsonObject("ImpInputAugType").get("value").getAsString(), jo.getAsJsonObject("ImpInput").get("value").getAsString() );
        	}
        	if (jo.has("Output") && jo.has("OutputLabel")) {
        		String c = jo.getAsJsonObject("Output").get("value").getAsString();
        		String l = jo.getAsJsonObject("OutputLabel").get("value").getAsString();
        		class2lbl.put(jo.getAsJsonObject("Output").get("value").getAsString(), jo.getAsJsonObject("OutputLabel").get("value").getAsString() );
        	}
        	if (jo.has("ImpOutput") && jo.has("ImpOutputAugType")) {
        		String c = jo.getAsJsonObject("ImpOutputAugType").get("value").getAsString();
        		String l = jo.getAsJsonObject("ImpOutput").get("value").getAsString();
        		class2lbl.put(jo.getAsJsonObject("ImpOutputAugType").get("value").getAsString(), jo.getAsJsonObject("ImpOutput").get("value").getAsString() );
        	}
        	
        }
        
        //keys = jres.entrySet();
        
//        for (Map.Entry<String, JsonElement> k: keys) {
//        	//System.out.println(k.getKey() + "   " + k.getValue());  //jres.get(k.getKey()).toString());
//        	//String foo = k.getValue().getAsString();
//        	class2lbl.put(k.getValue().getAsString(), k.getKey());
//        }	
    }
    return class2lbl;	
	
}

private Map<String, String> getClassUnitsMappingFromModelsJson(String json) {
    JsonParser parser = new JsonParser();
    JsonElement jsonTree = parser.parse(json);
    Map<String,String> class2units = new HashMap<String, String>();
    JsonObject jsonObject;
    JsonObject jres;
    Set<Entry<String, JsonElement>> keys;
    
    if (jsonTree.isJsonObject()) {
        jsonObject = jsonTree.getAsJsonObject();
        
        JsonArray jbindings = (JsonArray) jsonObject.getAsJsonObject("nodes").getAsJsonObject("results").get("bindings");
        
        for(int i=0; i < jbindings.size(); i++) {
        	JsonObject jo = (JsonObject)jbindings.get(i);
        	if (jo.has("Node") && jo.has("NodeOutputUnits")) {
        		class2units.put(jo.getAsJsonObject("Node").get("value").getAsString(), jo.getAsJsonObject("NodeOutputUnits").get("value").getAsString() );
        	}
        }
    }
    return class2units;	
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
            	//System.out.println(k.getKey() + "   " + k.getValue());  //jres.get(k.getKey()).toString());
            	//String foo = k.getValue().getAsString();
            	class2lbl.put(k.getValue().getAsString(), k.getKey());
            }	
        }
        return class2lbl;
	}

	
	
	
	private ResultSetRewindable queryKnowledgeGraph(String queryStr, Model model) {
		//System.out.println(queryStr);
		org.apache.jena.query.Query qm = QueryFactory.create(queryStr);
		QueryExecution qe = QueryExecutionFactory.create(qm, model); 
		//org.apache.jena.query.ResultSetRewindable 
		return org.apache.jena.query.ResultSetFactory.makeRewindable(qe.execSelect()) ;
	}


//	private String getEqnUrisFromResults(ResultSetRewindable eqnsResults) {
//		QuerySolution soln;
//		String listOfEqns = "";
//		eqnsResults.reset();
//		for ( ; eqnsResults.hasNext() ; )
//	    {
//	      soln = eqnsResults.nextSolution() ;
//	      RDFNode s = soln.get("?Eqns") ; 
//	      //RDFNode p = soln.get("?DBN") ;
//	      //Resource r = soln.getResource("VarR") ;
//	      //Literal l = soln.getLiteral("VarL") ; 
////	      System.out.println(s.toString()); // + "  " + p.toString() );
//	      listOfEqns += "<" + s.toString() +">,";
//	      //listOfEqnObjs.add(s);
//	    }
//		if(listOfEqns.length() <= 0)
//			return "";
//		else {
//			listOfEqns = listOfEqns.substring(0,listOfEqns.length()-1);
//			return listOfEqns;
//		}
//	}

	//TODO
	@SuppressWarnings("deprecation")
	private String querySemTK(String Url, String query)  {
		DefaultHttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(Url);
        httppost.setHeader("Accept", "*/*");
        httppost.setHeader("Content-type", "application/txt");
        
        try {
			httppost.setEntity(new StringEntity("\"" + query.replace("\"", "\\\"") + "\""));
			CloseableHttpResponse response = httpclient.execute(httppost);
	        HttpEntity respEntity = response.getEntity();
	        String responseTxt = EntityUtils.toString(respEntity, "UTF-8");
	        if (debugMode) {System.out.println(responseTxt);}
	        httpclient.close();
	        return responseTxt;
		} catch (IOException e) {
			System.err.println("SemTK query request failed.");
	        httpclient.close();
			return "";
		}
	}

	
	
	@SuppressWarnings("deprecation")
	private String executeDBN(String jsonTxt)  {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		String dbnBaseUrl = getPreference(DialogPreferences.ANSWER_DBN_CG_SERVICE_BASE_URI.getId());
        String dbnUrl = dbnBaseUrl + "/process";
        HttpPost httppost = new HttpPost(dbnUrl);
        httppost.setHeader("Accept", "*/*");
        httppost.setHeader("Content-type", "application/json");
        
        try {
			httppost.setEntity(new StringEntity("\"" + jsonTxt.replace("\"", "\\\"") + "\""));
			CloseableHttpResponse response = httpclient.execute(httppost);
	        HttpEntity respEntity = response.getEntity();
	        String responseTxt = EntityUtils.toString(respEntity, "UTF-8");
	        if (debugMode) {System.out.println(responseTxt);}
	        httpclient.close();
	        return responseTxt;
		} catch (IOException e) {
			System.err.println("DBN execution service request failed.");
	        httpclient.close();
			return "";
		}
	}


	@SuppressWarnings("deprecation")
	private String generateDBNjson(String jsonTxt) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
		String dbnBaseUrl = getPreference(DialogPreferences.DBN_INPUT_JSON_GENERATION_SERVICE_BASE_URI.getId());
        String jsonGenUrl = dbnBaseUrl + "/dbn/jsonGenerator";
        HttpPost httppost = new HttpPost(jsonGenUrl);
        httppost.setHeader("Accept", "application/json");
        httppost.setHeader("Content-type", "application/json");
        
        try {
	        httppost.setEntity(new StringEntity(jsonTxt));
	        CloseableHttpResponse response = httpclient.execute(httppost);
	        HttpEntity respEntity = response.getEntity();
	        String responseTxt = EntityUtils.toString(respEntity, "UTF-8");
	        if (debugMode) {System.out.println(responseTxt);}
	        httpclient.close();
	        return responseTxt;
        } catch (IOException e) {
			e.printStackTrace();
			System.err.println("DBN json generation service request failed.");
	        httpclient.close();
			return "";
		}
	}


    private String kgResultsToJson(String nodesAndModelsJSONString, String mode, String obsData) throws IOException  {
    	CloseableHttpClient httpclient = HttpClientBuilder.create().build();
        String dbnBaseUrl = getPreference(DialogPreferences.DBN_INPUT_JSON_GENERATION_SERVICE_BASE_URI.getId());
        String jsonGenUrl = dbnBaseUrl + "/dbn/SADLResultSetJsonToTableJson";
        HttpPost httppost = new HttpPost(jsonGenUrl);
        httppost.setHeader("Accept", "application/json");
        httppost.setHeader("Content-type", "application/json");

        try {
	        httppost.setEntity(new StringEntity(nodesAndModelsJSONString));
	        CloseableHttpResponse response = httpclient.execute(httppost);
	        HttpEntity respEntity = response.getEntity();
	        String responseTxt = EntityUtils.toString(respEntity, "UTF-8");
	        //System.out.println(responseTxt);
	        httpclient.close();
	        return responseTxt;
        } catch (IOException e) {
             e.printStackTrace();
             System.err.println("KG results to json translate service request failed.");
             httpclient.close();
             return "";
        }
    }

	private String strUriList(List<RDFNode> nodeList) {
		String uriStr = "";
		for ( RDFNode o : nodeList ) {
			uriStr += "<" + o.toString() + ">,";
		}
		uriStr = uriStr.substring(0,uriStr.length()-1);
		return uriStr;
	}


	private Individual createIndividualOfClass(OntModel model, String prefix, String nameprefix, String classUriStr) {
		OntClass ontClass = getTheJenaModel().getOntClass(classUriStr);
		Individual ind;
		if (prefix == null) {
			ind = model.createIndividual(ontClass); //make it a blank node
		}
		else {
			ind = model.createIndividual(prefix + ontClass.getLocalName()+ "_" + nameprefix + System.currentTimeMillis(), ontClass);
		}
		return ind;
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
	
	private String convertResultSetToString(org.apache.jena.query.ResultSetRewindable results) {
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		//org.apache.jena.query.ResultSetFormatter.outputAsCSV(baos, results);
		org.apache.jena.query.ResultSetFormatter.outputAsJSON(baos, results);
		return new String(baos.toByteArray(), Charset.defaultCharset()).replace(System.getProperty("line.separator"), "\n");		
	}

	
	private String getDataForHypothesisTesting(Resource resource, String docUri) throws FileNotFoundException {//String dataFile, 
		String dataContent = "";
		BufferedReader br = null;
		
		String queryStr = DOCLOCATIONQUERY.replaceAll("DOCINSTANCE", docUri);
		
		ResultSetRewindable fileRes = queryKnowledgeGraph(queryStr, getTheJenaModel());
		if (fileRes.size() < 1) {
			//dbnResults = null;
			throw new FileNotFoundException("Failed to find location of file " + docUri);
			//continue;
			//return null;
		}
		QuerySolution fsol = fileRes.nextSolution();
		String fileName = fsol.get("?file").toString().split("//")[1] ;
		
		//String dataFile = new File(getModelFolderPath(resource)).getParent() + File.separator + "Data" + File.separator + "hypothesis.csv";
		String dataFile = new File(getModelFolderPath(resource)).getParent() + File.separator + fileName;
		
		try {
			br = new BufferedReader(new FileReader(dataFile));
			String line;
			try {
				while ((line = br.readLine()) != null) {
					dataContent += line + "\n"; 
				}
			} catch (IOException e) {
				e.printStackTrace();
			}
		} catch (FileNotFoundException e) {
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

	private boolean useKChain() {
		String useKCStr = getPreference(DialogPreferences.USE_ANSWER_KCHAIN_CG_SERVICE.getId());
		boolean useKC = useKCStr != null ? Boolean.parseBoolean(useKCStr) : false;
		return useKC;
	}

	private boolean useDbn() {
		String useDbnStr = getPreference(DialogPreferences.USE_DBN_CG_SERVICE.getId());
		boolean useDbn = useDbnStr != null ? Boolean.parseBoolean(useDbnStr) : false;
		return useDbn;
	}

	

	
	
	/** 
	 * Gets a file from resourceDir
	 * @param fileName
	 * @return
	 */
	public String getFileContents(String fileName) throws Exception {
	    String pathStr; // =  fileName;
	    pathStr = "resources/" + fileName;
 
//	    Systme.out.println()
	    System.out.println("Reading file: " + pathStr);
		File f = new File(pathStr);
		InputStream in = null;

		if ( f.exists() ) {
			in = new FileInputStream(f);
		}else{
			System.out.println("File does not exist, trying to load as resource from classpath ...");
			in = this.getClass().getResourceAsStream(pathStr);
		}

		BufferedReader reader = new BufferedReader(new InputStreamReader(in));
		
		String         line = null;
		StringBuilder  stringBuilder = new StringBuilder();
		String         ls = System.getProperty("line.separator");

		try {
		    while((line = reader.readLine()) != null) {
		        stringBuilder.append(line);
		        stringBuilder.append(ls);
		    }
		
		    return stringBuilder.toString();
		} finally {
		    reader.close();
		}
	}

	private static String getOSIdent() {
		return System.getProperty("os.name").toLowerCase();
		
	}
	
	public static boolean isWindows() {

		return (getOSIdent().indexOf("win") >= 0);

	}

	public static boolean isMac() {

		return (getOSIdent().indexOf("mac") >= 0);

	}

	public static boolean isUnix() {

		return (getOSIdent().indexOf("nix") >= 0 || getOSIdent().indexOf("nux") >= 0 || getOSIdent().indexOf("aix") > 0 );
		
	}
	
}

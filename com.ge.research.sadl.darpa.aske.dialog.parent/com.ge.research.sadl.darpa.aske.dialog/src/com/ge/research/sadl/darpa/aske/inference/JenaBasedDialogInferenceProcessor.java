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
import java.io.FilenameFilter;
import java.io.IOException;
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
import org.eclipse.emf.common.util.URI;
import org.eclipse.emf.ecore.resource.Resource;

import com.ge.research.sadl.builder.ConfigurationManagerForIDE;
import com.ge.research.sadl.builder.ConfigurationManagerForIdeFactory;
import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager.Agent;
import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.darpa.aske.processing.DialogConstants;
import com.ge.research.sadl.darpa.aske.processing.IDialogAnswerProvider;
import com.ge.research.sadl.darpa.aske.processing.SadlStatementContent;
import com.ge.research.sadl.jena.JenaBasedSadlInferenceProcessor;
import com.ge.research.sadl.jena.JenaBasedSadlModelProcessor;
import com.ge.research.sadl.jena.UtilsForJena;
import com.ge.research.sadl.model.gp.Literal;
import com.ge.research.sadl.model.gp.NamedNode;
import com.ge.research.sadl.model.gp.Node;
import com.ge.research.sadl.model.gp.TripleElement;
import com.ge.research.sadl.processing.OntModelProvider;
import com.ge.research.sadl.processing.SadlInferenceException;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.ConfigurationManager;
import com.ge.research.sadl.reasoner.IReasoner;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.TranslationException;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.google.gson.JsonArray;
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
import com.hp.hpl.jena.update.UpdateAction;
import com.hp.hpl.jena.vocabulary.RDF;

public class JenaBasedDialogInferenceProcessor extends JenaBasedSadlInferenceProcessor {

	private OntModel queryModel;
//	private Map<String, String> preferenceMap;
	
//	public static final String queryHistoryKey = "MetaData";
//	public static final String qhModelName = "http://aske.ge.com/MetaData";
//	public static final String qhOwlFileName = "MetaData.owl";

	
	public static final String CGMODELS_FOLDER = "ComputationalGraphModels";
	
	
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
	public static final String METAMODEL_HASEQN_PROP = "http://aske.ge.com/compgraphmodel#hasEquation";
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
	public static final String METAMODEL_ASSUMPTIONSSATISFIED_PROP = METAMODEL_PREFIX + "assumptionsSatisfied";
	public static final String METAMODEL_ASSUMPTIONUNSATISFIED_PROP = METAMODEL_PREFIX + "unsatisfiedAssumption";
	
	public static final String DEPENDENCY_GRAPH_INSERT = "prefix cg:<http://aske.ge.com/compgraphmodel#>\n" + 
			"prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" +
			"prefix sci:<http://aske.ge.com/sciknow#>\n" +
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" +
			"prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" +
			"prefix list:<http://sadl.org/sadllistmodel#>\n" +
			"insert {?EqCh cg:parent ?EqPa}\n" + 
			"where {\n" +
			//"  ?EqCh a imp:Equation.\n" + //to include External equations
			" ?EqCh imp:arguments ?AL2.\n" + 
			" ?AL2 list:rest*/list:first ?AO2.\n" + 
			" ?AO2 imp:augmentedType ?Type2.\n" + 
			" ?Type2 imp:constraints ?CL2.\n" + 
			" ?CL2 rdf:rest*/rdf:first ?C2.\n" + 
			" ?C2 imp:gpPredicate ?P.\n" + 
			" filter (?P != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>)\n" + 
			"\n" + 
			//" ?EqPa a imp:Equation.\n" + 
			" ?EqPa imp:returnTypes ?AL1.\n" + 
			" ?AL1 list:rest*/list:first ?AO1.\n" + 
			" ?AO1 imp:augmentedType ?Type1.\n" + 
			" ?Type1 imp:constraints ?CL1.\n" + 
			" ?CL1 rdf:rest*/rdf:first ?C1.\n" + 
			" ?C1 imp:gpPredicate ?P.\n" + 
			" \n" + 
			" filter( ?EqPa != ?EqCh) " + 
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
			"select distinct ?DBN ?Out ?Eq where { \n" + 
			"  {select distinct ?Eq where { \n" + 
			//"     ?EqOut a imp:Equation. \n" + // to include External equation
			"     ?EqOut imp:returnTypes ?EO1. \n" + 
			"     ?EO1 list:rest*/list:first ?EO2.\n" + 
			"     ?EO2 imp:augmentedType ?EO3. \n" + 
			"     ?EO3 imp:constraints ?EO4.\n" + 
			"     ?EO4 rdf:rest*/rdf:first ?EO5.\n" + 
			"     ?EO5 imp:gpPredicate ?Op.\n" + 
			"     filter (?Op in ( LISTOFOUTPUTS )).\n" + 
			"\n" + 
			"     ?EqOut cg:parent* ?EqIn. \n" + 
			"     ?EqIn imp:arguments ?EI1.\n" + 
			"     ?EI1 list:rest*/list:first ?EI2.\n" + 
			"     ?EI2 imp:augmentedType ?EI3. \n" + 
			"     ?EI3 imp:constraints ?EI4.\n" + 
			"     ?EI4 rdf:rest*/rdf:first ?EI5.\n" + 
			"     ?EI5 imp:gpPredicate ?Ip.\n" + 
			"     filter (?Ip in ( LISTOFINPUTS )). \n" + 
			"\n" + 
			"     ?EqOut cg:parent* ?Eq. \n" + 
			"     ?Eq cg:parent* ?EqIn. \n" + 
			"  }} \n" + 
			"\n" + 
			"  ?Eq imp:returnTypes ?O1. \n" + 
			"  ?O1 list:rest*/list:first ?O2.\n" + 
			"  ?O2 imp:augmentedType ?O3. \n" + 
			"  ?O3 imp:constraints ?O4.\n" + 
			"  ?O4 rdf:rest*/rdf:first ?O5.\n" + 
			"  ?O5 imp:gpPredicate ?Out.\n" + 
			"  filter (?Out != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>) \n" + 
			"\n" + 
			"\n" + 
			"  ?DBN rdfs:subClassOf ?EQR. \n" + 
			"  ?EQR owl:onProperty cg:hasEquation.\n" + 
			"  ?EQR owl:allValuesFrom ?EqClass.\n" + 
			"  ?Eq a ?EqClass. \n" + 
			"}";

	
	public static final String RETRIEVE_MODELS = "prefix hyper:<http://aske.ge.com/hypersonicsV2#>\n" + 
			"prefix imp:<http://sadl.org/sadlimplicitmodel#>\n" + 
			"prefix sci:<http://aske.ge.com/sciknow#> \n" + 
			"prefix owl:<http://www.w3.org/2002/07/owl#> \n" + 
			"prefix cg:<http://aske.ge.com/compgraphmodel#>\n" + 
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"prefix rdf:<http://www.w3.org/1999/02/22-rdf-syntax-ns#>\n" + 
			"prefix list:<http://sadl.org/sadllistmodel#>\n" +
			"\n" + 
			"select distinct ?Model ?Input (str(?EI2Label) as ?InputLabel) ?UniqueInputLabel ?Output (str(?expr) as ?ModelForm) (str(?Fun) as ?Function) where { \n" + 
			"	  ?Model rdfs:subClassOf cg:DBN.\n" + 
			"	  ?Model rdfs:subClassOf ?BN.\n" + 
			"	  ?BN owl:onProperty cg:hasEquation.\n" + 
			"	  ?BN owl:allValuesFrom ?EqClass. \n" + 
			"	  ?Eq a ?EqClass. \n" + 
			"	    filter (?Eq in ( EQNSLIST )) .\n" + 
			"\n" + 
			"	  ?Eq imp:arguments ?EI1.\n" + 
			"     ?EI1 list:rest*/list:first ?EI2.\n" + 
			"     ?EI2 imp:localDescriptorName ?EI2Label.\n" + 
			"     ?EI2 imp:descriptorVariable ?UniqueInputLabel.\n" + 
			"     ?EI2 imp:augmentedType ?EI3. \n" + 
			"     ?EI3 imp:constraints ?EI4.\n" + 
			"     ?EI4 rdf:rest*/rdf:first ?EI5.\n" + 
			"     ?EI5 imp:gpPredicate ?Input.\n" + 
			"       filter (?Input != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>)\n" + 
			"     ?EI5 imp:gpObject ?Label.\n" + 
			"\n" + 
			"	  ?Eq imp:returnTypes ?EO1. \n" + 
			"     ?EO1 list:rest*/list:first ?EO2.\n" + 
			"     ?EO2 imp:augmentedType ?EO3. \n" + 
			"     ?EO3 imp:constraints ?EO4.\n" + 
			"     ?EO4 rdf:rest*/rdf:first ?EO5.\n" + 
			"     ?EO5 imp:gpPredicate ?Output.\n" + 
			"       filter (?Output != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>)\n" + 
			"\n" + 
			"	  optional{\n" + 
			"  	    ?Eq imp:expression ?Scr.\n" + 
			"	    ?Scr imp:script ?expr.\n" + 
			"       ?Scr imp:language ?lang.\n" + 
			"          filter ( ?lang = <http://sadl.org/sadlimplicitmodel#Python> )\n" + 
			"	  }\n" + 
			"	  optional {\n" + 
			"	    ?Eq imp:externalURI ?Fun.\n" + 
			"	  }\n "+
			"}\n" + 
			"order by ?Model";
	
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
			"select distinct ?Node (str(?NUnits) as ?NodeOutputUnits) ?Child (str(?CUnits) as ?ChildInputUnits) ?Distribution ?Lower ?Upper ?Eq ?Value \n" +
			" where {\n" + 
			"  {select distinct ?Node ?NUnits ?Child ?CUnits ?Distribution ?Lower ?Upper ?Eq where { \n" + 
			"     ?Eq imp:arguments ?EI1.\n" + 
			"     ?EI1 list:rest*/list:first ?EI2.\n" + 
			"     ?EI2 imp:augmentedType ?EI3. \n" + 
			"     ?EI3 imp:constraints ?EI4.\n" + 
			"     ?EI4 rdf:rest*/rdf:first ?EI5.\n" + 
			"     ?EI5 imp:gpPredicate ?Node.\n" + 
			"       filter (?Node != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>)\n" +
			"     optional{?EI2 imp:specifiedUnits/list:first ?CUnits.}" + 
			"\n" + 
			"     optional{\n" + 
			"       ?Var ?Node ?UQNode.\n" + 
			"       ?UQNode imp:unit ?InputUnitsQuery.\n" + 
			"     }\n" + 
			"     ?Eq imp:returnTypes ?EO1.\n" + 
			"     ?EO1 list:rest*/list:first ?EO2.\n" + 
			"     ?EO2 imp:augmentedType ?EO3. \n" + 
			"     ?EO3 imp:constraints ?EO4.\n" + 
			"     ?EO4 rdf:rest*/rdf:first ?EO5.\n" + 
			"     ?EO5 imp:gpPredicate ?Child.\n" + 
			"       filter (?Child != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>) \n" + 
			"\n" + 
			"     filter (?Eq in ( EQNSLIST )) \n" + 
			"\n" + 
			"     ?DBN rdfs:subClassOf ?N1. \n" + 
			"     ?N1 owl:onProperty cg:node. \n" + 
			"     ?N1 owl:hasValue ?TypeIns.\n" + 
			"     ?TypeIns a ?Type. \n" + 
			"     ?Node rdfs:range ?Type." +
			"\n" + 
			"     ?DBN rdfs:subClassOf ?DB. \n" + 
			"     ?DB owl:onProperty cg:distribution. \n" + 
			"     ?DB owl:hasValue ?Distribution.\n" + 
			"\n" + 
			"     ?DBN rdfs:subClassOf ?RB. \n" + 
			"     ?RB owl:onProperty cg:range. \n" + 
			"     ?RB owl:hasValue ?Range.\n" + 
			"     ?Range cg:lower ?Lower.\n" + 
			"     ?Range cg:upper ?Upper.\n" +
			"     optional {?DBN rdfs:subClassOf/owl:hasValue/imp:unit ?DBNUnits.}\n" + 
			"     bind( if(bound(?InputUnitsQuery), ?InputUnitsQuery, ?DBNUnits) as ?NUnits ).\n " +   
			"  }} \n" + 
			"  union {\n" + 
			"  select distinct ?Node ?NUnits ?Child ?CUnits ?Distribution ?Lower ?Upper ?Eq where {\n" + 
			"     ?Eq imp:arguments ?EI1.\n" + 
			"     ?EI1 list:rest*/list:first ?EI2.\n" + 
			"     ?EI2 imp:augmentedType ?EI3. \n" + 
			"     ?EI3 imp:constraints ?EI4.\n" + 
			"     ?EI4 rdf:rest*/rdf:first ?EI5.\n" + 
			"     ?EI5 imp:gpPredicate ?Node.\n" + 
			"       filter (?Node != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>)\n" + 
			"     optional{?EI2 imp:specifiedUnits/list:first ?CUnits.}" + 
			"\n" + 
			"     ?Eq imp:returnTypes ?EO1.\n" + 
			"     ?EO1 list:rest*/list:first ?EO2.\n" + 
			"     ?EO2 imp:augmentedType ?EO3. \n" + 
			"     ?EO3 imp:constraints ?EO4.\n" + 
			"     ?EO4 rdf:rest*/rdf:first ?EO5.\n" + 
			"     ?EO5 imp:gpPredicate ?Child.\n" + 
			"       filter (?Child != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>) \n" + 
			"		\n" + 
			"     filter (?Eq in ( EQNSLIST )) \n" + 
			"\n" + 
			"     ?DBN rdfs:subClassOf ?EB. \n" + 
			"     ?EB owl:onProperty cg:hasEquation.  \n" + 
			"     ?EB owl:allValuesFrom ?EqC. \n" + 
			"     ?Eq1 a ?EqC. \n" + 
			"     ?Eq1 imp:returnTypes ?EqO1.\n" + 
			"     ?EqO1 list:rest*/list:first ?EqO2.\n" + 
			"     ?EqO2 imp:augmentedType ?EqO3. \n" + 
			"     ?EqO3 imp:constraints ?EqO4.\n" + 
			"     ?EqO4 rdf:rest*/rdf:first ?EqO5.\n" + 
			"     ?EqO5 imp:gpPredicate ?Node.\n" + 
			"     optional{?EqO2 imp:specifiedUnits/list:first ?NUnits.}" +
			"\n" + 
			"     ?DBN rdfs:subClassOf ?DB. \n" + 
			"     ?DB owl:onProperty cg:distribution. \n" + 
			"     ?DB owl:hasValue ?Distribution. \n" + 
			"			\n" + 
			"     ?DBN rdfs:subClassOf ?RB. \n" + 
			"     ?RB owl:onProperty cg:range. \n" + 
			"     ?RB owl:hasValue ?Range. \n" + 
			"     ?Range cg:lower ?Lower. \n" + 
			"     ?Range cg:upper ?Upper. \n" + 
			"  }}\n" + 
			"  union {\n" + 
			"  select distinct ?Eq ?Node ?NUnits ?Distribution ?Lower ?Upper where { \n" + 
			"     ?Eq imp:returnTypes ?EO1.\n" + 
			"     ?EO1 list:rest*/list:first ?EO2.\n" + 
			"     ?EO2 imp:augmentedType ?EO3. \n" + 
			"     ?EO3 imp:constraints ?EO4.\n" + 
			"     ?EO4 rdf:rest*/rdf:first ?EO5.\n" + 
			"     ?EO5 imp:gpPredicate ?Node. \n" + 
			"       filter (?Eq in ( EQNSLIST ))\n" + 
			"     optional{?EO2 imp:specifiedUnits/list:first ?NUnits.}\n" + 
			"     filter not exists { \n" + 
			"	?Eq1 imp:arguments ?EI1.\n" + 
			"        ?EI1 list:rest*/list:first ?EI2.\n" + 
			"        ?EI2 imp:augmentedType ?EI3. \n" + 
			"        ?EI3 imp:constraints ?EI4.\n" + 
			"        ?EI4 rdf:rest*/rdf:first ?EI5.\n" + 
			"        ?EI5 imp:gpPredicate ?Node.\n" + 
			"	  filter (?Eq1 in ( EQNSLIST ))\n" + 
			"      }\n" + 
			"     ?DBN rdfs:subClassOf ?EB. \n" + 
			"     ?EB owl:onProperty cg:hasEquation.  \n" + 
			"     ?EB owl:allValuesFrom ?EqClass.\n" + 
			"     ?Eq a ?EqClass.\n" + 
			"\n" + 
			"     ?DBN rdfs:subClassOf ?DB. \n" + 
			"     ?DB owl:onProperty cg:distribution. \n" + 
			"     ?DB owl:hasValue ?Distribution.\n" + 
			"     ?DBN rdfs:subClassOf ?RB. \n" + 
			"     ?RB owl:onProperty cg:range.        \n" + 
			"     ?RB owl:hasValue ?Range.\n" + 
			"     ?Range cg:lower ?Lower.\n" + 
			"     ?Range cg:upper ?Upper.\n" + 
			" }}\n" +
			"  ?Q mm:execution/mm:compGraph ?CG. \n" +
			"   filter (?CG in (COMPGRAPH)).\n" + 
			"  optional{\n" + 
			"    ?Q mm:input ?Inp.\n" +
			"    ?Inp a ?IType." +
			"    ?Node rdfs:range ?IType." +
			"    ?Inp imp:value ?Value.}\n " + 
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
			"     ?EQ imp:arguments ?EI1.\n" + 
			"     ?EI1 list:rest*/list:first ?EI2.\n" + 
			"     ?EI2 imp:augmentedType ?EI3. \n" + 
			"     ?EI3 imp:constraints ?EI4.\n" + 
			"     ?EI4 rdf:rest*/rdf:first ?EI5.\n" + 
			"     ?EI5 imp:gpPredicate ?InputProp.\n" + 
			"       filter (?InputProp != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>)\n" +
			"     ?InputProp rdfs:range ?Input." +
			"\n" + 
			"     ?EQ imp:returnTypes ?EO1.\n" + 
			"     ?EO1 list:rest*/list:first ?EO2.\n" + 
			"     ?EO2 imp:augmentedType ?EO3. \n" + 
			"     ?EO3 imp:constraints ?EO4.\n" + 
			"     ?EO4 rdf:rest*/rdf:first ?EO5.\n" + 
			"     ?EO5 imp:gpPredicate ?OutputProp.\n" + 
			"       filter (?OutputProp != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>) \n" + 
			"     ?OutputProp rdfs:range ?Output." +
			"\n" + 
			"    # There's an eqn that outputs EQ's input (a parent Eqn)\n" + 
			"    ?CCG mm:subgraph ?SG1.\n" + 
			"    ?SG1 mm:cgraph ?CG1.\n" + 
			"    ?CG1 cg:hasEquation ?EQ1.\n" + 
			"    ?EQ1 imp:returnTypes ?E11.\n" + 
			"    ?E11 list:rest*/list:first ?E12.\n" + 
			"    ?E12 imp:augmentedType ?E13. \n" + 
			"    ?E13 imp:constraints ?E14.\n" + 
			"    ?E14 rdf:rest*/rdf:first ?E15.\n" + 
			"    ?E15 imp:gpPredicate ?InputProp.\n" + 
			"\n" +
			"    optional{ \n" +
			"      ?EQ imp:expression ?Scr.\n" + 
			"      ?Scr imp:script ?Expr.\n" + 
			"      ?Scr imp:language ?lang.\n" + 
			"         filter ( ?lang = <http://sadl.org/sadlimplicitmodel#Python> )\n" + 
			"      bind(str(?Expr) as ?Z_tooltip)\n" +
			"    }\n " + 
			"    bind('solid' as ?X_style)\n" + 
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
			"     ?EQ imp:arguments ?EI1.\n" + 
			"     ?EI1 list:rest*/list:first ?EI2.\n" + 
			"     ?EI2 imp:augmentedType ?EI3. \n" + 
			"     ?EI3 imp:constraints ?EI4.\n" + 
			"     ?EI4 rdf:rest*/rdf:first ?EI5.\n" + 
			"     ?EI5 imp:gpPredicate ?InputProp.\n" + 
			"       filter (?InputProp != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>)\n" +
			"     ?InputProp rdfs:range ?Input." +
			"\n" + 
			"     ?EQ imp:returnTypes ?EO1.\n" + 
			"     ?EO1 list:rest*/list:first ?EO2.\n" + 
			"     ?EO2 imp:augmentedType ?EO3. \n" + 
			"     ?EO3 imp:constraints ?EO4.\n" + 
			"     ?EO4 rdf:rest*/rdf:first ?EO5.\n" + 
			"     ?EO5 imp:gpPredicate ?OutputProp.\n" + 
			"       filter (?OutputProp != <http://www.w3.org/1999/02/22-rdf-syntax-ns#type>) \n" + 
			"     ?OutputProp rdfs:range ?Output." +			"\n" + 
			"    ?EQ imp:expression ?Scr.\n" + 
			"    ?Scr imp:script ?Expr.\n" + 
			"    ?Scr imp:language ?lang.\n" + 
			"       filter ( ?lang = <http://sadl.org/sadlimplicitmodel#Python> )\n" + 
			"     bind(str(?Expr) as ?Z_tooltip)\n" + 
			"\n" + 
			"    # EQ does not have a parent EQn\n" + 
			"    filter not exists {\n" + 
			"      ?CCG mm:subgraph ?SG2.\n" + 
			"      ?SG2 mm:cgraph ?CG2.\n" + 
			"      ?CG2 cg:hasEquation ?EQ2.\n" + 
			"      ?EQ2 imp:returnTypes ?E21.\n" + 
			"      ?E21 list:rest*/rdf:first ?E22.\n" + 
			"      ?E22 imp:augmentedType ?E23. \n" + 
			"      ?E23 imp:constraints ?E24.\n" + 
			"      ?E24 rdf:rest*/rdf:first ?E25.\n" + 
			"      ?E25 imp:gpPredicate ?InputProp.\n" + 
			"    }\n" + 
			"      \n" + 
			"    bind('filled' as ?X_style)\n" + 
			"    bind('yellow' as ?X_color)\n" + 
			"}}union\n" + 
//			" {select (?Output as ?X) ?Y (?Value as ?Z) ?X_style ?X_color ('oval' as ?Z_shape) ('output value' as ?Z_tooltip)\n" + 
			" {select ?CCG (?Output as ?X) ?Y (concat(concat(strbefore(?Value,'.'),'.'),substr(strafter(?Value,'.'),1,4)) as ?Z) ?X_style ?X_color ('oval' as ?Z_shape) ('output value' as ?Z_tooltip)\n" + 
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
			"select distinct (strafter(str(?CCG),'#') as ?Model) (strafter(str(?Var),'#') as ?Variable) ?Mean ?StdDev\n" + 
			"where {\n" + 
			"   {?CCG mm:subgraph ?SG.\n" + 
			"    filter (?CCG in (COMPGRAPH)).\n" +
			"    ?SG mm:output ?Oinst.\n" + 
			"    ?Oinst a ?Var.\n" + 
			"    ?Oinst imp:value ?Mean.\n" + 
			"    ?Oinst imp:stddev ?StdDev.\n" +
			"  } union {\n" +
//			"   ?CCG mm:subgraph ?SG. \n" + 
			"   filter (?CCG in (COMPGRAPH)).\n" +
//			"   ?Q mm:execution ?CE.\n" + 
			"   ?CE mm:compGraph ?CCG.\n" + 
//			"   ?Q mm:output ?Vinst.\n" + 
			"   ?CE mm:output ?Vinst.\n" + 
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

	private static final String SENSITIVITYQUERY = "prefix mm:<http://aske.ge.com/metamodel#>\n" + 
			"prefix rdfs:<http://www.w3.org/2000/01/rdf-schema#>\n" + 
			"\n" + 
			"select distinct (strafter(str(?O),'#') as ?Output) (strafter(str(?I),'#') as ?Input) ?Sensitivity\n" + 
			"where { \n" + 
			" ?CG mm:sensitivity ?SS.\n" + 
			"     filter (?CG in ( COMPGRAPH )) \n" + 
			" ?SS mm:output ?O.\n" + 
			" ?SS mm:inputSensitivity ?Sy.\n" + 
			" ?Sy mm:input ?I.\n" + 
			" ?Sy mm:sensitivityValue ?Sensitivity\n" + 
			" } ";


	@Override
	public boolean isSupported(String fileExtension) {
		return "dialog".equals(fileExtension);
	}

	@Override
	public Object[] insertTriplesAndQuery(Resource resource, TripleElement[] triples) throws SadlInferenceException {
		Object[] results = null;
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

		String useDbnStr = getPreference(DialogPreferences.USE_DBN_CG_SERVICE.getId());
		boolean useDbn = useDbnStr != null ? Boolean.parseBoolean(useDbnStr) : false;
		String useKCStr = getPreference(DialogPreferences.USE_ANSWER_KCHAIN_CG_SERVICE.getId());
		boolean useKC = useKCStr != null ? Boolean.parseBoolean(useKCStr) : false;
		if (useKC) {
			if (commonSubject(triples) && allPredicatesAreProperties(triples)) {
				Object[] jbsipResult = super.insertTriplesAndQuery(resource, triples);
				results = jbsipResult;
			}
			else {
				results = new Object[1];
				results[0] = "KChain requires that all triples have the same subject and that all predicates are properties";
			}
		}

		if (useDbn) {
			
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
			
			String kgsDirectory = new File(getModelFolderPath(resource)).getParent() + File.separator  + CGMODELS_FOLDER;
			new File(kgsDirectory).mkdir();
			
			String queryModelFileName = "Q_" + System.currentTimeMillis();
			String queryModelURI = "http://aske.ge.com/" + "Model_" + queryModelFileName;
			String queryModelPrefix = queryModelURI + "#";
			//String queryInstanceName = METAMODEL_PREFIX + queryModelFileName;
			String queryInstanceName = queryModelPrefix + queryModelFileName;
			String queryOwlFileWithPath = kgsDirectory + File.separator + queryModelFileName + ".owl";

			
			
			//ConfigurationManagerForIDE cmgr = null;
			//Object qhModelObj = OntModelProvider.getPrivateKeyValuePair(resource, queryHistoryKey);
			
			File f = new File(queryOwlFileWithPath);
			if (f.exists() && !f.isDirectory()) {
				throw new SadlInferenceException("Query model file already exists");
			}
			else {
				queryModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
			}
			
			OntModelProvider.addPrivateKeyValuePair(resource, queryModelFileName, queryModel);
		

			ConfigurationManagerForIDE cmgr = null;
			try {
				//String p = getModelFolderPath(resource);
				cmgr = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(getModelFolderPath(resource), ConfigurationManagerForIDE.getOWLFormat());
			} catch (ConfigurationException e1) {
				//  Auto-generated catch block
				e1.printStackTrace();
			}
			
			
			// Triples representing a quantity
			// v0 altitude v1, v1 value 35000, v1 units "ft", v1 type Altitude
			TripleElement[] quantity; // = new TripleElement[4];

			List<TripleElement[]> insertions = new ArrayList<TripleElement[]>();
			List<TripleElement> queryPatterns = new ArrayList<TripleElement>();
			List<TripleElement> docPatterns = new ArrayList<TripleElement>();
			
			
			List<RDFNode> inputsList = new ArrayList<RDFNode>();
			List<RDFNode> outputsList = new ArrayList<RDFNode>();
			//Map<OntClass, Individual> outputInstance = new HashMap<OntClass, Individual>();
			
			
			for (int i = 0; i < triples.length; i++) {
				TripleElement tr = triples[i];
				if (tr.getSubject() instanceof NamedNode) {
					
					if (tr.getPredicate().getURI() != null) {
						if (tr.getPredicate().getName().contains("value")) { //input value triple (v1, sadlimplicitmodel:value, 35000)
							quantity = createUQtriplesArray(tr,triples);
							insertions.add(quantity);
						}
						else if (tr.getObject() == null) { //(v0, hypersonicsV2:staticTemperature, null)
							// this is an output
							queryPatterns.add(tr);
						}
					}
					else {
						docPatterns.add(tr);
					}
				}
			}
			
		


		


		
		
			String ss, sp, so, ns;
			com.hp.hpl.jena.rdf.model.Resource sss;
			Property ssp;
			RDFNode sso;
			String suri;
			OntClass sclass;
			//Individual sinst;
			TripleElement itr;
			
			//Create a new metaData file for every query, or make query variables unique.
			
			
			// Create list of inputs & ingest input values
			if (insertions != null && insertions.size() > 0) {
				for (int i = 0; i < insertions.size(); i++) {
					
					itr = insertions.get(i)[0]; //e.g. itr = (v0 altitude v1)
					sp = itr.getPredicate().getURI();
					ssp = getTheJenaModel().getProperty(sp);
					// Add property to list of inputs
					inputsList.add(ssp);
				}
			}
				
			
			// Query instance
			//OntClass cgquery = getTheJenaModel().getOntClass(METAMODEL_QUERY_CLASS);
			//Individual cgq = getTheJenaModel().createIndividual(cgquery);
			//Individual cgq = createIndividualOfClass(queryModel, METAMODEL_QUERY_CLASS);
			Individual cgq = queryModel.createIndividual(queryInstanceName, getTheJenaModel().getOntClass(METAMODEL_QUERY_CLASS));
			// This is how to add properties to the individual
			//i.addProperty(RDFS.comment, "something");
		    //i.addRDFType(OWL2.NamedIndividual);
	
			// ingest input values
			if (insertions != null && insertions.size() > 0) {
				for (int i = 0; i < insertions.size(); i++) {
					
					itr = insertions.get(i)[0]; //e.g. itr = (v0 altitude v1)
					//if (itr.getSubject().equals(commonSubject)) {
					//NamedNode subj = (NamedNode) getTheJenaModel().getOntClass(itr.getSubject().getURI());
	
					ss = itr.getSubject().getName();
					sp = itr.getPredicate().getURI();
					so = itr.getObject().getName();
					
					ns = getModelName() + "#";
					ns = queryModelPrefix;
					
					sss = getTheJenaModel().getResource(ns+ss);
					ssp = getTheJenaModel().getProperty(sp);
					sso = getTheJenaModel().getResource(ns+so);
	
					//com.hp.hpl.jena.rdf.model.Resource foo = qhmodel.getResource(ns+so+cgq.ge);
					
					// Add property to list of inputs
	//				inputsList.add(ssp);
					
					ingestKGTriple(sss, ssp, sso); //(:v0 :altitude :v1)
					
					// create triple: cgq, mm:input, inputVar
					OntProperty inputprop = getTheJenaModel().getOntProperty(METAMODEL_INPUT_PROP);
					if (inputprop == null) {
						// TODO need EObject to display error marker in Dialog window... 
						System.err.println("Unable to find property '" + METAMODEL_INPUT_PROP + "'; is the meta model imported?");
						return null;
					}
					//ingestKGTriple(cgq, inputprop, ssp); //(bnode, mm:input, :altitude)
	
					ingestKGTriple(cgq, inputprop, sso); //(bnode, mm:input, v1)
	
					
					
					//ingest value triple
					itr = insertions.get(i)[1]; //e.g. itr = (v1 :value 30000)
	
					ss = itr.getSubject().getName();
					sss = getTheJenaModel().getResource(ns+ss);
					sp = itr.getPredicate().getURI();
					ssp = getTheJenaModel().getProperty(sp);
					RDFNode val = getObjectAsLiteralOrResource(itr.getPredicate(), itr.getObject());
					
					ingestKGTriple(sss, ssp, val);
					//ingestKGTriple(sss, getTheJenaModel().getProperty(VALUE_PROP), val); // (#v1, #value, 30000^^decimal )
					
					//ingest units triple
					itr = insertions.get(i)[2]; //e.g. itr = (v1, units, "ft")
					if (itr != null) {
						sp = itr.getPredicate().getURI();
						ssp = getTheJenaModel().getProperty(sp);
						sso = getObjectAsLiteralOrResource(itr.getPredicate(), itr.getObject());
						ingestKGTriple(sss, ssp, sso); // (#v1, #unit, ft^^string )
						//ingestKGTriple(sss, getTheJenaModel().getProperty(UNIT_PROP), sso); // (#v1, #unit, ft^^string )
					}
					
					itr = insertions.get(i)[3]; // (v1 rdf:type :Altitude)
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
			
			//getTheJenaModel().write(System.out, "TTL" );
		
			// Will need object to create (obj prop uq) triple, where uq is a unitted quantity obj
			HashMap<String, String> mapPropertiesToOutputObj = new HashMap<String,String>(); 
				
				
				// Create list of outputs
		//		if (queryPatterns.size() > 0) {
		//			// how many results there will be
		//			//ResultSet[] results = new ResultSet[queryPatterns.size()];
			for (int i = 0; i < queryPatterns.size(); i++) {
				itr = queryPatterns.get(i);
				sp = itr.getPredicate().getURI(); // e.g. #altitude
				ssp = getTheJenaModel().getProperty(sp);
				//ssp = getTheJenaModel().getProperty(queryModelPrefix+sp);
				outputsList.add(ssp);
					
				mapPropertiesToOutputObj.put(itr.getSubject().toString(), sp.toString());
			}
				
			// Create list of outputs
		//	if (docPatterns.size() > 0) {
			for (int i = 0; i < docPatterns.size(); i++) {
				itr = docPatterns.get(i);
				sp = itr.getPredicate().getURI(); // e.g. #altitude
				ssp = getTheJenaModel().getProperty(sp);
				outputsList.add(ssp);
			}
				
				
			// Insert dependency graph
			//String tmp = DEPENDENCY_GRAPH_INSERT
			UpdateAction.parseExecute(DEPENDENCY_GRAPH_INSERT , getTheJenaModel());
			
			ResultSetRewindable depTest = queryKnowledgeGraph(CHECK_DEPENDENCY, getTheJenaModel());
			
			if (!depTest.hasNext()) {
				throw new SadlInferenceException("Dependency inference failed");
			}
				
			
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
	
			OntClass cexec;
			Individual ce;
			OntProperty execprop;
			
			Map<String,String> class2lbl, lbl2class;
			Map<String,String[]> lbl2value;
			
			String queryMode;
			String cgJson, dbnJson, dbnResultsJson;
			
			String outputType;
			Individual oinst; 
			
			Map<RDFNode, String[]> dbnEqns = new HashMap<RDFNode,String[]>();
			Map<RDFNode, RDFNode> dbnOutput = new HashMap<RDFNode,RDFNode>();
	
			
	//		ResultSet[] results = new ResultSet[2]; 
			ResultSet[] dbnresults = null;
			
			try {
			
				if (outputsList.size() > 0 && docPatterns.size() <= 0) { 
		
					
					listOfInputs = strUriList(inputsList);
					listOfOutputs = strUriList(outputsList);
		
					eqnsResults = retrieveCG(inputsList, outputsList);
					queryMode = "prognostic";
					
					//qtype = getTheJenaModel().getOntResource(METAMODEL_PREFIX + "prognostic"); //don't distinguish prognostic/calibration
					
					dbnEqns = createDbnEqnMap(eqnsResults);
					dbnOutput = createDbnOutputMap(eqnsResults);
	
					
					int numOfModels = 1;
					if (dbnEqns.isEmpty())
						numOfModels = 0;
					for (RDFNode dbnk :  dbnEqns.keySet()) {
						numOfModels *= dbnEqns.get(dbnk).length;
					}
	
					
					String[] modelEqnList = buildEqnsLists(numOfModels, dbnEqns);
	
					
					//dbnresults = new ResultSet[numOfModels*2]; 
					dbnresults = new ResultSet[numOfModels]; 
					
					String resmsg = null;
					
					for(int i=0; i<numOfModels; i++) {
						listOfEqns = modelEqnList[i];
		
						//Create execution instance with time
						cexec = getTheJenaModel().getOntClass(METAMODEL_CGEXEC_CLASS);
						//String cexecStr = cexec.getLocalName();
						//ce = queryModel.createIndividual(cexecStr + "_" + System.currentTimeMillis(), cexec);
						ce = createIndividualOfClass(queryModel, null, null, cexec.toString());
						//OntProperty stprop = getTheJenaModel().getOntProperty(METAMODEL_STARTTIME_PROP);
				
	//					String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	//					Calendar cal = Calendar.getInstance();
	//					SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	//					XSDDateTime tim = sdf.format(cal.getTime());;
	////					qhmodel.add(ce,stprop,(XSDDateTime)val).asCalendar().getTime())
						
						
						execprop = getTheJenaModel().getOntProperty(METAMODEL_EXEC_PROP);
						if (execprop == null) {
							System.err.println("Can't find property '" + METAMODEL_EXEC_PROP + "'. Something is wrong.");
							return null;
						}
						else {
							ingestKGTriple(cgq,execprop,ce);
						}

						//TODO: for each equation
						//  	retrieve context triples
						//		replace instances already in the graph (e.g. if (v0 type Air) in graph, and equation has (vv type Air), replace v0 for vv
						//		insert new triples into graph
						
						
	//					//TODO: create output instances and link them to ce
	//					//      There may be multiple outputs, need to loop through them
	//					// We may need to do this later after the results have been computed
	//					for (RDFNode op : outputsList) {
	//						outputType = op.as(OntProperty.class).getRange().toString();
	//						oinst = createIndividualOfClass(qhmodel,outputType);
	//						ingestKGTriple(ce,outputprop,oinst);
	//					}
	//					
						
						// Comp Graph instance
						cgIns = queryModel.createIndividual(queryModelPrefix + "CG_" + System.currentTimeMillis(), getTheJenaModel().getOntClass(METAMODEL_CCG));
						//cgIns = queryModel.createIndividual("CG_" + System.currentTimeMillis(), getTheJenaModel().getOntClass(METAMODEL_CCG));
						getTheJenaModel().add(cgIns, RDF.type, getTheJenaModel().getOntClass(METAMODEL_CCG));
						//qhmodel.add(ce, getTheJenaModel().getOntProperty(METAMODEL_COMPGRAPH_PROP), cgIns);
						ingestKGTriple(ce, getTheJenaModel().getOntProperty(METAMODEL_COMPGRAPH_PROP), cgIns);
						//getTheJenaModel().add(ce, RDF.type, getTheJenaModel().getOntClass(METAMODEL_CCG));
	
	
						// Retrieve Models & Nodes
						queryStr = RETRIEVE_MODELS.replaceAll("EQNSLIST", listOfEqns);
						models = queryKnowledgeGraph(queryStr, getTheJenaModel());
						modelsCSVString = convertResultSetToString(models);
//						System.out.println(modelsCSVString);
						
						queryStr = RETRIEVE_NODES.replaceAll("EQNSLIST", listOfEqns);
						queryStr = queryStr.replaceAll("COMPGRAPH", "<" + cgIns.getURI() + ">");
						nodes = queryKnowledgeGraph(queryStr, getTheJenaModel().union(queryModel));
						nodesCSVString = convertResultSetToString(nodes);
//						System.out.println(nodesCSVString);
			
						cgJson 		= kgResultsToJson(nodesCSVString, modelsCSVString, queryMode, "");
						dbnJson 	= generateDBNjson(cgJson);
						class2lbl 	= getClassLabelMapping(dbnJson);
						
						
						
			            dbnResultsJson = executeDBN(dbnJson);
			            
			            
			            
			            //check if DBN execution was successful
			            
			            resmsg = getDBNoutcome(dbnResultsJson);
			            //boolean suc = resmsg.equals("Success");
			            
			            if(resmsg != null && resmsg.equals("Success")) {
				            
				            lbl2value = getLabelToMeanStdMapping(dbnResultsJson);
			//TODO
				            createCGsubgraphs(cgIns, dbnEqns, dbnOutput, listOfEqns, class2lbl, lbl2value, queryModelPrefix);
				            
							//TODO: create output instances and link them to ce
							//      There may be multiple outputs, need to loop through them
							// We may need to do this later after the results have been computed
//							for (RDFNode op : outputsList) {
//								outputType = op.as(OntProperty.class).getRange().toString();
//								oinst = createIndividualOfClass(queryModel, null, outputType);
//								ingestKGTriple(ce,outputprop,oinst);
//							}
							
	
				            for(RDFNode outType : outputsList) {
				            	if( class2lbl.containsKey(outType.toString())) {
				            		
				        			String rng = outType.as(OntProperty.class).getRange().toString(); //e.g. :Altitude
				        			
				        			Individual outpIns = createIndividualOfClass(queryModel, null, null, rng); //e.g. instance of :Altitude
				        			ingestKGTriple(ce, outputprop, outpIns);
				        			
				        			String cls = class2lbl.get(outType.toString());
				        			String[] ms = lbl2value.get(cls);  //class2lbl.get(o.toString()));
				        			queryModel.add(outpIns, getTheJenaModel().getProperty(VALUE_PROP), ms[0] );
				        			queryModel.add(outpIns, getTheJenaModel().getProperty(STDDEV_PROP), ms[1] );
				        			if(ms[2] != null)
				        				queryModel.add(outpIns, getTheJenaModel().getProperty(VARERROR_PROP), ms[2] );
				            	}
				            }
				            
				            //add outputs to CG if calibration
							//if(queryMode.equals("prognostic")) {
	
	//			            //Comment out until switch from Class to Property for outputs
	//			            for(OntClass oc : outputsList) {
	//							if (!dbnOutput.values().contains(oc)) {//if no DBN has this output, ie it's a model input 
	//								String ocls = oc.toString();
	//								String cls = class2lbl.get(ocls);
	//								String[] ms = lbl2value.get(cls);  
	//								Individual oinst = createIndividualOfClass(qhmodel,ocls);
	//								//getTheJenaModel().add(oinst,RDF.type, ocls);
	//								OntProperty outputprop = getTheJenaModel().getOntProperty(METAMODEL_OUTPUT_PROP);
	//								//qhmodel.add(cgq, outputprop, oinst);
	//								//TODO link oinst to ce not cgq
	//								//ingestKGTriple(cgq, outputprop, oinst);
	//								ingestKGTriple(ce, outputprop, oinst);
	//								qhmodel.add(oinst, getTheJenaModel().getProperty(VALUE_PROP), ms[0] );
	//								qhmodel.add(oinst, getTheJenaModel().getProperty(STDDEV_PROP), ms[1] );
	//							}
	//						}
							//}
	
							
							
							AnswerCurationManager acm = (AnswerCurationManager) cmgr.getPrivateKeyValuePair(DialogConstants.ANSWER_CURATION_MANAGER);

							saveMetaDataFile(resource,queryModelURI,queryModelFileName); //so we can query the the eqns in the CCG
							
							ResultSet assumpCheck = null;
							assumpCheck = checkModelAssumptions(resource, cgIns.toString(), queryModelURI, queryOwlFileWithPath);
							if (assumpCheck != null) {
//								System.out.println(assumpCheck.toString());
								
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
//								System.out.println(ssc.toString());
								//acm.notifyUser(getModelFolderPath(resource), ssc, false);
							}
							
							
				            
							// create ResultSet
							dbnresults[i] = retrieveCompGraph(resource, cgIns);
							
//							dbnresults[i+numOfModels] = retrieveValues(resource, cgIns);

							// convert to SADL to insert into dialog window
							//ResultSet rs = dbnresults[i+numOfModels];
							ResultSet rs = retrieveValues(resource, cgIns);
							List<String> sadlDeclaration = new ArrayList<String>();
							if (rs.getRowCount() > 0) {
								StringBuilder sb = new StringBuilder("The CGExecution with compGraph ");
								sb.append(rs.getResultAt(0, 0).toString());
								sb.append("\n");
								for (int row = 0; row < rs.getRowCount(); row++) {
									//StringBuilder sb = new StringBuilder("The CGExecution ");
									sb.append("    has output (a ");
									sb.append(rs.getResultAt(row, 1).toString());
									sb.append(" with ^value ");
									sb.append(rs.getResultAt(row, 2));
									sb.append(", with stddev ");
									sb.append(rs.getResultAt(row, 3));
									sb.append(")\n");
								}
								sb.append(".\n");
								sadlDeclaration.add(sb.toString());
							}							
							for (String sd : sadlDeclaration) {
								SadlStatementContent ssc = new SadlStatementContent(null, Agent.CM, sd);
//								AnswerCurationManager acm = (AnswerCurationManager) cmgr.getPrivateKeyValuePair(DialogConstants.ANSWER_CURATION_MANAGER);
								//System.out.println(ssc.toString());
								acm.notifyUser(getModelFolderPath(resource), ssc, false);
							}
							
							//If assumptions are satisfied, compute sensitivity
							if ( assumpCheck != null && assumpCheck.getResultAt(0, 0).equals("satisfied") ) {
					            //Send sensitivity request to DBN
					            //queryMode = "sensitivity";
					            cgJson = kgResultsToJson(nodesCSVString, modelsCSVString, "sensitivity", "");
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
//									System.out.println(ssc.toString());
									//acm.notifyUser(getModelFolderPath(resource), ssc, false);
					            }
							}//assumptions satisfied
				            
							
							
			            }
			            else {
			            	dbnresults = null;
			            	//System.err.println("DBN execution failed. Message: " + dbnResultsJson.toString());
			            }
					}// end of models loop
					if (numOfModels < 1) {
						dbnresults = null;
						//System.out.println("Unable to assemble a model with current knowledge");
					}
					
					if(resmsg != null && resmsg.equals("Success")) {
						saveMetaDataFile(resource,queryModelURI,queryModelFileName); //to include sensitivity results

						String projectName = new File(getModelFolderPath(resource)).getParentFile().getName(); // ASKE_P2
						
						Resource newRsrc = resource.getResourceSet()
								.createResource(URI.createPlatformResourceURI(projectName + File.separator + CGMODELS_FOLDER + File.separator + queryModelFileName + ".owl" , false));
						newRsrc.load(resource.getResourceSet().getLoadOptions());
						JenaBasedSadlModelProcessor.refreshResource(newRsrc);
						//SadlUtils fileNameToFileUrl
						String owlURL = new SadlUtils().fileNameToFileUrl(queryOwlFileWithPath);
						cmgr.addMapping(owlURL, queryModelURI, "", true, "JBDIP");
						cmgr.addJenaMapping(queryModelURI, owlURL);	
						
						AnswerCurationManager acm = (AnswerCurationManager) cmgr.getPrivateKeyValuePair(DialogConstants.ANSWER_CURATION_MANAGER);
						acm.addDelayedImportAddition("import \"" + queryModelURI + "\".");
					}
					
				} else if (outputsList.size() > 0 && docPatterns.size() > 0) { //models from dataset
					
					
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
						
						
						listOfInputs = strUriList(inpl);
						listOfOutputs = strUriList(outpl);
//						System.out.println(listOfInputs);
//						System.out.println(listOfOutputs);
	
						queryStr = BUILD_COMP_GRAPH.replaceAll("LISTOFINPUTS", listOfInputs).replaceAll("LISTOFOUTPUTS", listOfOutputs);
						com.hp.hpl.jena.query.Query q = QueryFactory.create(queryStr);
						qexec = QueryExecutionFactory.create(q, getTheJenaModel()); 
						eqnsResults = com.hp.hpl.jena.query.ResultSetFactory.makeRewindable(qexec.execSelect()) ;
						
						//int ns = eqnsResults.size();
						//boolean nn = eqnsResults.hasNext();
						
						dbnEqns = createDbnEqnMap(eqnsResults);
						dbnOutput = createDbnOutputMap(eqnsResults);
						
						int newModels = 0;
						if(!dbnEqns.isEmpty())
							newModels = 1;
						for (RDFNode dbnk :  dbnEqns.keySet()) {
							newModels *= dbnEqns.get(dbnk).length;
						}
	
						String[] modelEqnList = buildEqnsLists(newModels, dbnEqns);
	
						//results = new ResultSet[outputsList.size()*numOfModels*2]; 
						
						//if (eqnsResults.size() > 0 ) {
						for(int mod=0; mod<newModels; mod++) {
							
							listOfEqns = modelEqnList[mod]; //getEqnUrisFromResults(eqnsResults);
				            eqnsResults.reset();
	
				            
				            
							//Create execution instance with time
							cexec = getTheJenaModel().getOntClass(METAMODEL_CGEXEC_CLASS);
							ce = queryModel.createIndividual(cexec);
							//OntProperty stprop = getTheJenaModel().getOntProperty(METAMODEL_STARTTIME_PROP);
					
	//						String DATE_FORMAT_NOW = "yyyy-MM-dd HH:mm:ss";
	//						Calendar cal = Calendar.getInstance();
	//						SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT_NOW);
	//						XSDDateTime tim = sdf.format(cal.getTime());;
	////						qhmodel.add(ce,stprop,(XSDDateTime)val).asCalendar().getTime())
							
							
							execprop = getTheJenaModel().getOntProperty(METAMODEL_EXEC_PROP);
							if (execprop == null) {
								System.err.println("Can't find property '" + METAMODEL_EXEC_PROP + "'. Something is wrong.");
								return null;
							}
							else {
								ingestKGTriple(cgq,execprop,ce);
							}
				            
							//TODO: create output instances and link them to ce
							//      There is only one output for datasets
							outputType = oputType.as(OntProperty.class).getRange().toString();
							oinst = createIndividualOfClass(queryModel,null, null, outputType);
							ingestKGTriple(ce,outputprop,oinst);
							
							cgIns = queryModel.createIndividual(queryModelPrefix + "CG_" + System.currentTimeMillis(), getTheJenaModel().getOntClass(METAMODEL_CCG));
							//getTheJenaModel().add(cgIns, RDF.type, getTheJenaModel().getOntClass(METAMODEL_CCG));
							// Each comp graph is attached to the execution instance
							//qhmodel.add(ce, getTheJenaModel().getOntProperty(METAMODEL_COMPGRAPH_PROP), cgIns);
							ingestKGTriple(ce, getTheJenaModel().getOntProperty(METAMODEL_COMPGRAPH_PROP), cgIns);
							
							// Retrieve Models & Nodes
							queryStr = RETRIEVE_MODELS.replaceAll("EQNSLIST", listOfEqns);
							models = queryKnowledgeGraph(queryStr, getTheJenaModel());
							
							queryStr = RETRIEVE_NODES.replaceAll("EQNSLIST", listOfEqns);
							queryStr = queryStr.replaceAll("COMPGRAPH", "<" + cgIns.getURI() + ">");
							nodes = queryKnowledgeGraph(queryStr, getTheJenaModel().union(queryModel));
							
							modelsCSVString = convertResultSetToString(models);
							nodesCSVString = convertResultSetToString(nodes);
	
							String docUri = triples[0].getSubject().getURI();
							
							queryStr = DOCLOCATIONQUERY.replaceAll("DOCINSTANCE", docUri);
							
							ResultSetRewindable fileRes = queryKnowledgeGraph(queryStr, getTheJenaModel());
							if (fileRes.size() < 1) {
								dbnresults = null;
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
				            String resmsg = getDBNoutcome(dbnResultsJson);
				            //boolean suc = resmsg.equals("Success");
				            
				            if(resmsg.equals("Success")) {
				            	
				            	numOfModels ++;
								
				            	//Ingest model error
				            	String modelError = getModelError(dbnResultsJson);
				            	Property errorProp = getTheJenaModel().getProperty(MODELERROR_PROP);
				            	queryModel.add(cgIns, getTheJenaModel().getProperty(MODELERROR_PROP), modelError);
				            	getTheJenaModel().add(cgIns, errorProp, modelError);
				            	
				            	lbl2value = getLabelToMeanStdMapping(dbnResultsJson);
							
					            createCGsubgraphs(cgIns, dbnEqns, dbnOutput, listOfEqns, class2lbl, lbl2value, queryModelPrefix); //, outputInstance);
								
					            resultsList.add(numOfModels-1, retrieveCompGraph(resource, cgIns));
					            resultsList.add(numOfModels*2-1, retrieveValuesHypothesis(resource, cgIns));
					            
								// create ResultSet
	//							results[i] = retrieveCompGraph(resource, cgIns);
	//							results[i+1] = retrieveValuesHypothesis(resource, cgIns);
				            }
						}
						// else continue looking
					}
					dbnresults = new ResultSet[numOfModels*2];
					dbnresults = resultsList.toArray(dbnresults);
				}
				
			
			    		
	  		//qhmodel.write(System.out,"RDF/XML-ABBREV");
	   		//qhmodel.write(System.out,"TTL");
	    		
	
				
				
				// Save metadata owl file 
				saveMetaDataFile(resource,queryModelURI, queryModelFileName);
			
	//			// create ResultSet
				
				if (results == null || results.length == 0) {
					results = dbnresults;
				}
				else {
					Object[] compositeResults = new Object[results.length + dbnresults.length];
					int idx = 0;
					for (Object result : results) {
						compositeResults[idx++] = result;
					}
					for (Object result : dbnresults) {
						compositeResults[idx++] = result;
					}
					results = compositeResults;
				}
				return results;
					
			} catch (Exception e) {
				// Auto-generated catch block
				//System.out.println(e.getMessage());
				//TODO: reset model
				e.printStackTrace();
			}

		}
		return null;
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

	private com.hp.hpl.jena.query.ResultSetRewindable retrieveCG(List<RDFNode> inputsList, List<RDFNode> outputsList) {
		com.hp.hpl.jena.query.ResultSet eqns;
		com.hp.hpl.jena.query.ResultSet eqnsRes;
		
		String queryStr, inpStr, outpStr;
		com.hp.hpl.jena.query.Query qinv;
		QueryExecution qexec;
		
		//This is a roundabout way to initialize eqnsRes. There must be a better way?
		queryStr = BUILD_COMP_GRAPH.replaceAll("LISTOFOUTPUTS", "").replaceAll("LISTOFINPUTS", "");
		//qinv = QueryFactory.create(queryStr);
		//qexec = QueryExecutionFactory.create(qinv, getTheJenaModel());
		//eqnsRes = qexec.execSelect() ;
		eqnsRes = queryKnowledgeGraph(queryStr, getTheJenaModel());
		
		for(RDFNode ic : inputsList) 
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
					eqnsRes = com.hp.hpl.jena.sparql.util.ResultSetUtils.union(eqnsRes, eqns);
				}		
		}			
		
		
		return com.hp.hpl.jena.query.ResultSetFactory.makeRewindable(eqnsRes);
	}

	private void ingestKGTriple(com.hp.hpl.jena.rdf.model.Resource s, Property pred, RDFNode o) {
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

	private ResultSet runPrologQuery(Resource resource, String query, String instanceDataURI, String queryOwlFileWithPath) throws Exception {
		String modelFolder = getModelFolderPath(resource); //getOwlModelsFolderPath(path).toString(); 
		final String format = ConfigurationManager.RDF_XML_ABBREV_FORMAT;
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

	private ResultSet runReasonerQuery(Resource resource, String query) throws Exception {
		String modelFolderUri = getModelFolderPath(resource); //getOwlModelsFolderPath(path).toString(); 
		final String format = ConfigurationManager.RDF_XML_ABBREV_FORMAT;
		IConfigurationManagerForIDE configMgr;

		
		configMgr = ConfigurationManagerForIdeFactory.getConfigurationManagerForIDE(modelFolderUri, format);
		IReasoner reasoner = configMgr.getReasoner();
	
		
		if (!reasoner.isInitialized()) {
			reasoner.setConfigurationManager(configMgr);
			//String modelName = configMgr.getPublicUriFromActualUrl(new SadlUtils().fileNameToFileUrl(modelFolderUri + File.separator + owlFileName));
			//model name something like http://aske.ge.com/metamodel
			//String mname = getModelName();
			reasoner.initializeReasoner(modelFolderUri, getModelName(), format); 
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
			
		
		String pquery = reasoner.prepareQuery(query);

		ResultSet res = reasoner.ask(pquery);
		return res;
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




		private void createCGsubgraphs(Individual cgIns, Map<RDFNode, String[]> dbnEqns, Map<RDFNode, RDFNode> dbnOutput,
				String listOfEqns, Map<String, String> class2lbl, Map<String, String[]> lbl2value, String prefix) //, Map<OntClass, Individual> outputInstance)
		{

		OntProperty subgraphprop = 	getTheJenaModel().getOntProperty(METAMODEL_SUBG_PROP);
		OntProperty cgraphprop = 	getTheJenaModel().getOntProperty(METAMODEL_CGRAPH_PROP);
		OntProperty hasEqnProp = 	getTheJenaModel().getOntProperty(METAMODEL_HASEQN_PROP);
		OntProperty outputprop = 	getTheJenaModel().getOntProperty(METAMODEL_OUTPUT_PROP);
		
		Individual sgIns, dbnIns, outpIns;
		String rng;
		
		for( RDFNode dbn : dbnEqns.keySet() ) {
			sgIns = createIndividualOfClass(queryModel,null, null, METAMODEL_SUBGRAPH);
			ingestKGTriple(cgIns, subgraphprop, sgIns);
			dbnIns = createIndividualOfClass(queryModel,null, null, dbn.toString());
			ingestKGTriple(sgIns, cgraphprop, dbnIns);

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
			RDFNode e = queryModel.getResource(eqn); 
			ingestKGTriple(dbnIns, hasEqnProp, e);
			
			RDFNode otype = dbnOutput.get(dbn); //this is a property now 

			RDFNode otypeProp = getTheJenaModel().getProperty(dbnOutput.get(dbn).toString()); //e.g. :altitude
			
			rng = otypeProp.as(OntProperty.class).getRange().toString(); //e.g. :Altitude
			
			outpIns = createIndividualOfClass(queryModel, null, null, rng); //e.g. instance of :Altitude
			ingestKGTriple(sgIns, outputprop, outpIns);
			
			String cls = class2lbl.get(otype.toString());
			String[] ms = lbl2value.get(cls);  //class2lbl.get(o.toString()));
			queryModel.add(outpIns, getTheJenaModel().getProperty(VALUE_PROP), ms[0] );
			queryModel.add(outpIns, getTheJenaModel().getProperty(STDDEV_PROP), ms[1] );
			if(ms[2] != null)
				queryModel.add(outpIns, getTheJenaModel().getProperty(VARERROR_PROP), ms[2] );
			
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
		com.hp.hpl.jena.query.Query qm = QueryFactory.create(queryStr);
		QueryExecution qe = QueryExecutionFactory.create(qm, model); 
		//com.hp.hpl.jena.query.ResultSetRewindable 
		return com.hp.hpl.jena.query.ResultSetFactory.makeRewindable(qe.execSelect()) ;
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


	@SuppressWarnings("deprecation")
	private String executeDBN(String jsonTxt)  {
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
        try {
			httppost.setEntity(new StringEntity("\"" + jsonTxt.replace("\"", "\\\"") + "\""));
//        httppost.setEntity(new StringEntity("\"" + testJson.replace("\"", "\\\"") + "\""));
			CloseableHttpResponse response = httpclient.execute(httppost);
	        HttpEntity respEntity = response.getEntity();
	        String responseTxt = EntityUtils.toString(respEntity, "UTF-8");
//	        System.out.println(responseTxt);
	        httpclient.close();
	        return responseTxt;
		} catch (IOException e) {
			//e.printStackTrace();
			System.err.println("DBN execution service request failed.");
	        httpclient.close();
			return "";
		}
	}


	@SuppressWarnings("deprecation")
	private String generateDBNjson(String jsonTxt) {
		DefaultHttpClient httpclient = new DefaultHttpClient();
//		HttpPost httppost = new HttpPost("http://vesuvius063.crd.ge.com:46000/dbn/jsonGenerator");
		HttpPost httppost = new HttpPost("http://mazama6.crd.ge.com:46000/dbn/jsonGenerator");
        httppost.setHeader("Accept", "application/json");
        httppost.setHeader("Content-type", "application/json");
        
        try {
	        httppost.setEntity(new StringEntity(jsonTxt));
	        CloseableHttpResponse response = httpclient.execute(httppost);
	        HttpEntity respEntity = response.getEntity();
	        String responseTxt = EntityUtils.toString(respEntity, "UTF-8");
//	        System.out.println(responseTxt);
	        httpclient.close();
	        return responseTxt;
        } catch (IOException e) {
			e.printStackTrace();
			System.err.println("DBN json generation service request failed.");
	        httpclient.close();
			return "";
		}
	}


	@SuppressWarnings("deprecation")
	private String kgResultsToJson(String nodesCSVString, String modelsCSVString, String mode, String obsData) throws Exception {
		DefaultHttpClient httpclient = new DefaultHttpClient();
//		HttpPost httppost = new HttpPost("http://vesuvius063.crd.ge.com:46000/dbn/SADLResultSetToJson");
		HttpPost httppost = new HttpPost("http://mazama6.crd.ge.com:46000/dbn/SADLResultSetToJson");
		httppost.setHeader("Accept", "application/json");
        httppost.setHeader("Content-type", "application/x-www-form-urlencoded");
        
        List<NameValuePair> arguments = new ArrayList<>(2);
        arguments.add(new BasicNameValuePair("nodes", nodesCSVString));
        arguments.add(new BasicNameValuePair("models", modelsCSVString));
        arguments.add(new BasicNameValuePair("mode", mode));
        arguments.add(new BasicNameValuePair("data", obsData));

        try {
	        httppost.setEntity(new UrlEncodedFormEntity(arguments, "UTF-8"));
	//         HttpResponse response = httpclient.execute(httppost);
	//         HttpEntity respEntity = response.getEntity();
	//         String responseTxt = EntityUtils.toString(respEntity, "UTF-8");
	        CloseableHttpResponse response = httpclient.execute(httppost);
	        HttpEntity respEntity = response.getEntity();
	        String responseTxt = EntityUtils.toString(respEntity, "UTF-8");
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
	
}

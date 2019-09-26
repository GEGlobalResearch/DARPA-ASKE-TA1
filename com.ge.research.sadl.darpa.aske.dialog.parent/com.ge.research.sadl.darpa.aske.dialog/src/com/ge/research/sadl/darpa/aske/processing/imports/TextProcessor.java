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

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.compiler.InvalidInputException;

import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.jena.UtilsForJena;
import com.ge.research.sadl.processing.SadlConstants;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.IReasoner;
import com.ge.research.sadl.reasoner.ITranslator;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.IConfigurationManagerForEditing.Scope;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.hp.hpl.jena.ontology.Individual;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class TextProcessor {
	private static final Logger logger = Logger.getLogger (TextProcessor.class) ;

	private Map<String, String> preferences;
	
	private List<File> textFiles;
	
	private AnswerCurationManager answerCurationManager;

	private String textModelFolder;
	private IConfigurationManagerForIDE textModelConfigMgr;

	private OntModel textModel;
	private Map<String, OntModel> textModels;
	private String textmodelName;	// the name of the model being created by extraction
	private String textmodelPrefix;	// the prefix of the model being created by extraction

	private String defaultTextModelName = null;
	private String defaultTextModelPrefix = null;

	public TextProcessor(AnswerCurationManager answerCurationManager, Map<String, String> preferences) {
		setCurationManager(answerCurationManager);
		this.setPreferences(preferences);
	}

	/**
	 * Method to find semantic content related to the name provided, e.g., if the locality were the text surrounding an equation, which contained the phrase
	 * "where T is the temperature of the air", then a call with name "T" might return the 
	 * @param name
	 * @param locality
	 * @return
	 * @throws InvalidInputException 
	 * @throws IOException 
	 */
	public List<String[]> processName(String name, String locality) throws InvalidInputException, IOException {
		if (name == null) {
			throw new InvalidInputException("Name cannot be null");
		}
		if (locality == null) {
			throw new InvalidInputException("Locality cannot be null");
		}
		try {
			String msg = "Searching for name '" + name + "' in locality '" + locality + "'.";
			getCurationManager().notifyUser(getTextModelConfigMgr().getModelFolder(), msg, true);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
//		String baseServiceUrl = "http://vesuvius-dev.crd.ge.com:4200/darpa/aske/";		// dev environment for stable development of other components
		String baseServiceUrl = "http://vesuvius063.crd.ge.com:4200/darpa/aske/";		// test environment for service development
		
		String servicePreference = getPreference(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getId());
		if (servicePreference != null) {
			baseServiceUrl = servicePreference + "/darpa/aske/";
		}
		String equationVariableContextServiceURL = baseServiceUrl + "equationVariableContext";
		URL serviceUrl = new URL(equationVariableContextServiceURL);			
		JsonObject json = new JsonObject();
		json.addProperty("localityURI", locality);
		json.addProperty("variableName", name);
//		System.out.println(text);
		String response = getCurationManager().makeConnectionAndGetResponse(serviceUrl, json);
//		System.out.println(response);
		if (response != null && response.length() > 0) {
			OntModel theModel = getCurationManager().getExtractionProcessor().getTextModel();
			JsonElement je = new JsonParser().parse(response);
			System.out.println(je.toString());
			if (je instanceof JsonArray) {
				JsonArray sentences = je.getAsJsonArray();
				if (sentences != null) {
					List<String[]> results = new ArrayList<String[]>();
					for (JsonElement element : sentences) {
						if (element != null) {
							JsonObject sentence = element.getAsJsonObject();
							String varName = sentence.get("variableName").getAsString();
							String eqStr = sentence.get("equationString").getAsString();
							String[] use = new String[2];
							use[0] = varName;
							use[1] = eqStr;
							results.add(use);
						}
					}
					return results;
				}
			}
		}
		return null;
	}
	
	/**
	 * Mehod to process a block of text via the textToTriples service to find equations and concepts
	 * @param inputIdentifier -- the identifier, normally a model URI, of the source text
	 * @param text --  the source text to be processed
	 * @param locality -- the URI of the model to be used as context for the extraction
	 * @return -- an array int[2], 0th element being the number of concepts found in the text, 1st element being the number of equations found in the text
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public int[] processText(String inputIdentifier, String text, String locality) throws ConfigurationException, IOException {
		initializeTextModel();
		try {
			String msg = "Importing text with identifier '" + inputIdentifier + "'.";
			getCurationManager().notifyUser(getTextModelConfigMgr().getModelFolder(), msg, true);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		StringBuilder sb = new StringBuilder();
//		String baseServiceUrl = "http://vesuvius-dev.crd.ge.com:4200/darpa/aske/";		// dev environment for stable development of other components
		String baseServiceUrl = "http://vesuvius063.crd.ge.com:4200/darpa/aske/";		// test environment for service development
		
		String servicePreference = getPreference(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getId());
		if (servicePreference != null) {
			baseServiceUrl = servicePreference + "/darpa/aske/";
		}
		String textToTripleServiceURL = baseServiceUrl + "text2triples";
		URL serviceUrl = new URL(textToTripleServiceURL);			
		if (text == null) {
			text = "Hello world";
		}
		if (locality == null) {
			locality = "NYI";
		}
		JsonObject json = new JsonObject();
		json.addProperty("localityURI", locality);
		json.addProperty("text", text);
//		System.out.println(text);
		String response = getCurationManager().makeConnectionAndGetResponse(serviceUrl, json);
//		System.out.println(response);
		if (response != null && response.length() > 0) {
			OntModel theModel = getCurationManager().getExtractionProcessor().getTextModel();
			JsonElement je = new JsonParser().parse(response);
			if (je.isJsonObject()) {
				int nc = je.getAsJsonObject().get("numConceptsExtracted").getAsInt();
				int neq = je.getAsJsonObject().get("numEquationsExtracted").getAsInt();
				System.out.println("nc=" + nc + ", neq=" + neq);
				int[] results = new int[2];
				results[0] = nc;
				results[1] = neq;
				return results;
			}
//			JsonArray sentences = new JsonParser().parse(response).getAsJsonArray();
//			if (sentences != null) {
//				for (JsonElement element : sentences) {
//					if (element != null) {
//						JsonObject sentence = element.getAsJsonObject();
//						String originalText = sentence.get("text").getAsString();
////						System.out.println("Extracted from text:" + originalText);
//						JsonArray concepts = sentence.get("concepts").getAsJsonArray();
//						for (JsonElement concept : concepts) {
//							String matchingText = concept.getAsJsonObject().get("string").getAsString();
//							int startInOrigText = concept.getAsJsonObject().get("start").getAsInt();
//							int endInOrigText = concept.getAsJsonObject().get("end").getAsInt();
//							double extractionConfidence = concept.getAsJsonObject().get("extractionConfScore").getAsDouble();
////							System.out.println("  Match in substring '" + matchingText + "(" + startInOrigText + "," + endInOrigText + "):");
//							JsonArray triples = concept.getAsJsonObject().get("triples").getAsJsonArray();
//							String eqName = null;
//							String eqExpr1 = null;
//							String eqExpr2 = null;
//							String eqScript1 = null;
//							String eqScript2 = null;
//							String lang1 = null;
//							String lang2 = null;
//							for (JsonElement triple : triples) {
//								String subject = triple.getAsJsonObject().get("subject").getAsString();
//								String predicate = triple.getAsJsonObject().get("predicate").getAsString();
//								String object = triple.getAsJsonObject().get("object").getAsString();
//								double tripleConfidenceScore = triple.getAsJsonObject().get("tripleConfScore").getAsDouble();
//								System.out.println("     <" + subject + ", " + predicate + ", " + object + "> (" + tripleConfidenceScore + ")");
//								if (object.equals("<http://sadl.org/sadlimplicitmodel#ExternalEquation>")) {
//									// equation found
//									eqName = subject;
//									// predicate assumed to be rdf:type
//								}
//								if (eqName != null && subject.equals(eqName)) {
//									if (predicate.equals("<http://sadl.org/sadlimplicitmodel#expression>")) {
//										if (eqExpr1 == null) {
//											eqExpr1 = object;
//										}
//										else {
//											eqExpr2 = object;
//										}
//									}
//								}
//								if (eqExpr1 != null && subject.equals(eqExpr1)) {
//									if (predicate.equals("<" + SadlConstants.SADL_IMPLICIT_MODEL_SCRIPT_PROPERTY_URI + ">")) {
//										eqScript1 = UtilsForJena.stripQuotes(object);
//									}
//									else if (predicate.equals("<" + SadlConstants.SADL_IMPLICIT_MODEL_LANGUAGE_PROPERTY_URI + ">")) {
//										lang1 = object;
//									}
//								}
//								if (eqExpr2 != null && subject.equals(eqExpr2)) {
//									if (predicate.equals("<" + SadlConstants.SADL_IMPLICIT_MODEL_SCRIPT_PROPERTY_URI + ">")) {
//										eqScript2 = UtilsForJena.stripQuotes(object);
//									}
//									else if (predicate.equals("<" + SadlConstants.SADL_IMPLICIT_MODEL_LANGUAGE_PROPERTY_URI + ">")) {
//										lang2 = object;
//									}
//								}
//							}
//							if (eqName != null) {
//								// add to model
//								Individual eqInst = theModel.createIndividual(getCurationManager().getExtractionProcessor().getTextModelName() + "#" + eqName,
//										theModel.getOntClass(SadlConstants.SADL_IMPLICIT_MODEL_EXTERNAL_EQUATION_CLASS_URI));
//								if (eqName.startsWith("_:")) {
//									eqName = eqName.substring(2);
//								}
//								if (eqScript1 != null) {
//									Individual script1 = theModel.createIndividual(theModel.getOntClass(SadlConstants.SADL_IMPLICIT_MODEL_SCRIPT_CLASS_URI));
//									script1.addProperty(theModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_SCRIPT_PROPERTY_URI), theModel.createTypedLiteral(eqScript1));
//									script1.addProperty(theModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_LANGUAGE_PROPERTY_URI), theModel.getIndividual(lang1.substring(1, lang1.length() - 1)));
//									eqInst.addProperty(theModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_EXPRESSTION_PROPERTY_URI), script1);
//								}
//								if (eqScript2 != null) {
//									Individual script2 = theModel.createIndividual(theModel.getOntClass(SadlConstants.SADL_IMPLICIT_MODEL_SCRIPT_CLASS_URI));
//									script2.addProperty(theModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_SCRIPT_PROPERTY_URI), theModel.createTypedLiteral(eqScript2));
//									script2.addProperty(theModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_LANGUAGE_PROPERTY_URI), theModel.getIndividual(lang2.substring(1, lang2.length() - 1)));
//									eqInst.addProperty(theModel.getProperty(SadlConstants.SADL_IMPLICIT_MODEL_EXPRESSTION_PROPERTY_URI), script2);									
//								}
//							}
//						}
//					}
//				}
//			}
//			System.out.println("The extracted model is:");
//			theModel.write(System.out, "N3");
//			addTextModel(inputIdentifier, theModel);
		}
		else {
			System.err.println("No response received from service " + textToTripleServiceURL);
		}
		return null;
	}
	
	public void addTextModel(String key, OntModel textModel) {
		if (textModels == null) {
			textModels = new HashMap<String, OntModel>();
		}
		textModels.put(key, textModel);
	}
	
	public OntModel getTextModel(String key) {
		if (textModels != null) {
			return textModels.get(key);
		}
		return null;
	}

	private void initializeTextModel() throws ConfigurationException, IOException {
		if (getCurationManager().getExtractionProcessor().getTextModel() == null) {
			// create new text model	
			setTextModelConfigMgr(getCurationManager().getDomainModelConfigurationManager());
			OntDocumentManager owlDocMgr = getTextModelConfigMgr().getJenaDocumentMgr();
			OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
			if (owlDocMgr != null) {
				spec.setDocumentManager(owlDocMgr);
				owlDocMgr.setProcessImports(true);
			}
			getCurationManager().getExtractionProcessor().setTextModel(ModelFactory.createOntologyModel(spec));	
			setTextModel(getCurationManager().getExtractionProcessor().getTextModel());
			getTextModel().setNsPrefix(getTextModelPrefix(), getTextModelNamespace());
			Ontology modelOntology = getTextModel().createOntology(getTextModelName());
			logger.debug("Ontology '" + getTextModelName() + "' created");
			modelOntology.addComment("This ontology was created by extraction from text by the ANSWER TextProcessor.", "en");
			OntModel importedOntModel = getTextModelConfigMgr().getOntModel(SadlConstants.SADL_IMPLICIT_MODEL_URI, Scope.INCLUDEIMPORTS);
			addImportToJenaModel(getTextModelName(), SadlConstants.SADL_IMPLICIT_MODEL_URI, SadlConstants.SADL_IMPLICIT_MODEL_PREFIX, importedOntModel);
		}
		else {
			setTextModel(getCurationManager().getExtractionProcessor().getTextModel());
		}
	}
	
	private void addImportToJenaModel(String modelName, String importUri, String importPrefix, Model importedOntModel) {
		getTextModel().getDocumentManager().addModel(importUri, importedOntModel, true);
		Ontology modelOntology = getTextModel().createOntology(modelName);
		if (importPrefix != null) {
			getTextModel().setNsPrefix(importPrefix, importUri);
		}
		com.hp.hpl.jena.rdf.model.Resource importedOntology = getTextModel().createResource(importUri);
		modelOntology.addImport(importedOntology);
		getTextModel().addSubModel(importedOntModel);
		getTextModel().addLoadedImport(importUri);
	}

	private String getTextModelName() {
		return this.getTextmodelName();
	}

	private String getTextModelNamespace() {
		return getTextModelName() + "#";
	}

	private String getTextModelPrefix() {
		return this.getTextmodelPrefix();
	}

	private void setTextModel(OntModel textModel) {
		this.textModel = textModel;	
	}
	
	private OntModel getTextModel() {
		return textModel;
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

	public void addFiles(List<File> txtFiles) {
		if (textFiles != null) {
			for (File f : txtFiles) {
				if (!textFiles.contains(f)) {
					textFiles.add(f);
				}
			}
		}
		else {
			setTextFiles(txtFiles);
		}
	}

	public void addFile(File txtFile) {
		if (textFiles == null) {
			textFiles = new ArrayList<File>();
		}
		if (!textFiles.contains(txtFile)) {
			textFiles.add(txtFile);
		}
	}

	public List<File> getTextFiles() {
		return textFiles;
	}

	public void setTextFiles(List<File> textFiles) {
		this.textFiles = textFiles;
	}

	public AnswerCurationManager getCurationManager() {
		return answerCurationManager;
	}

	public void setCurationManager(AnswerCurationManager answerCurationManager) {
		this.answerCurationManager = answerCurationManager;
	}

	public IConfigurationManagerForIDE getTextModelConfigMgr() {
		return textModelConfigMgr;
	}

	private void setTextModelConfigMgr(IConfigurationManagerForIDE textMetaModelConfigMgr) {
		this.textModelConfigMgr = textMetaModelConfigMgr;
	}

	public String getTextmodelName() {
		return textmodelName;
	}

	public void setTextmodelName(String textmodelName) {
		this.textmodelName = textmodelName;
	}

	public String getTextmodelPrefix() {
		return textmodelPrefix;
	}

	public void setTextmodelPrefix(String textmodelPrefix) {
		this.textmodelPrefix = textmodelPrefix;
	}

	public String getTextModelFolder() {
		return textModelFolder;
	}

	public void setTextModelFolder(String textModelFolder) {
		this.textModelFolder = textModelFolder;
	}

	public String getDefaultTextModelName() {
		return defaultTextModelName;
	}

	public void setDefaultTextModelName(String defaultTextModelName) {
		this.defaultTextModelName = defaultTextModelName;
	}

	public String getDefaultTextModelPrefix() {
		return defaultTextModelPrefix;
	}

	public void setDefaultTextModelPrefix(String defaultTextModelPrefix) {
		this.defaultTextModelPrefix = defaultTextModelPrefix;
	}

	public ResultSet executeSparqlQuery(String query) throws ConfigurationException, ReasonerNotFoundException, IOException, InvalidNameException, QueryParseException, QueryCancelledException {
//		ITranslator translator = getTextModelConfigMgr().getTranslator();
		query = SadlUtils.stripQuotes(query);
		IReasoner reasoner = getTextModelConfigMgr().getReasoner();
		if (!reasoner.isInitialized()) {
			reasoner.setConfigurationManager(getTextModelConfigMgr());
			reasoner.initializeReasoner(getTextModelConfigMgr().getModelFolder(), getTextModelName(), null);
		}
		query = reasoner.prepareQuery(query);
		ResultSet results =  reasoner.ask(query);
		return results;
	}
}

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
import java.io.Writer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.compiler.InvalidInputException;

import com.ge.research.sadl.builder.IConfigurationManagerForIDE;
import com.ge.research.sadl.darpa.aske.curation.AnswerCurationManager;
import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.darpa.aske.processing.imports.TextProcessingServiceInterface.EquationVariableContextResponse;
import com.ge.research.sadl.darpa.aske.processing.imports.TextProcessingServiceInterface.UnitExtractionResponse;
import com.ge.research.sadl.processing.SadlConstants;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.ge.research.sadl.reasoner.IConfigurationManagerForEditing.Scope;
import com.ge.research.sadl.reasoner.IReasoner;
import com.ge.research.sadl.reasoner.InvalidNameException;
import com.ge.research.sadl.reasoner.QueryCancelledException;
import com.ge.research.sadl.reasoner.QueryParseException;
import com.ge.research.sadl.reasoner.ReasonerNotFoundException;
import com.ge.research.sadl.reasoner.ResultSet;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.hp.hpl.jena.ontology.OntDocumentManager;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.Ontology;
import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.util.iterator.ExtendedIterator;

public class TextProcessor {
	private static final Logger logger = Logger.getLogger (TextProcessor.class) ;

	private Map<String, String> preferences;
	
	private List<File> textFiles;
	
	private AnswerCurationManager answerCurationManager;

	private String textModelFolder;
	private IConfigurationManagerForIDE textModelConfigMgr;

	private OntModel textModel;
	private Map<String, OntModel> textModels;
	private String textModelName;	// the name of the model being created by extraction
	private String textmodelPrefix;	// the prefix of the model being created by extraction

	private OntModel registeredDomainModel = null;	// domain model registered with text to triples service

//	private String defaultTextModelName = null;
//	private String defaultTextModelPrefix = null;

	public class MergedEquationVariableContext {
		private String conceptUri;
		private List<String> labels = new ArrayList<String>();
		private Map<String, String> equationsAndArguments = new HashMap<String, String>();
		
		public MergedEquationVariableContext(String uri) {
			setConceptUri(uri);
		}
		
		public MergedEquationVariableContext(List<List<String[]>> equationVariableContextResponses) throws AnswerExtractionException {
			String uri = null;
			for (List<String[]> response : equationVariableContextResponses) {
				for (String[] content : response) {
					if (uri == null) {
						uri = content[3];
					}
					else if (!content[3].equals(uri)) {
						throw new AnswerExtractionException("Cannot create MergedEquationVariableContext from contents with different URIs ('" +
								content[3] + "' != '" + uri + "').");
					}
					addEquationAndArgument(content[0], content[1]);
					addLabel(content[2]);
				}
			}
			setConceptUri(uri);
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder("External concept uri: ");
			sb.append(getConceptUri());
			sb.append("\n  Labels: ");
			boolean first = true;
			for (String label : getLabels()) {
				if (!first) sb.append(", ");
				sb.append(label);
			}
			
			return sb.toString();
		}

		public String getConceptUri() {
			return conceptUri;
		}
		
		private void setConceptUri(String conceptUri) {
			this.conceptUri = conceptUri;
		}

		public List<String> getLabels() {
			return labels;
		}
		
		private void addLabel(String label) {
			if (!labels.contains(label)) {
				labels.add(label);
			}
		}
		
		private Map<String, String> getEquationsAndArguments() {
			return equationsAndArguments;
		}

		private void addEquationAndArgument(String eq, String arg) {
			if (!equationsAndArguments.containsKey(eq)) {
				equationsAndArguments.put(eq, arg);
			}
		}
	}

	public TextProcessor(AnswerCurationManager answerCurationManager, Map<String, String> preferences) {
		setCurationManager(answerCurationManager);
		this.setPreferences(preferences);
	}

	/**
	 * Method to process a block of text via the textToTriples service to find equations and concepts
	 * @param inputIdentifier -- the identifier, normally a model URI, of the source text
	 * @param text --  the source text to be processed
	 * @param locality -- the URI of the model to be used as context for the extraction
	 * @param prefix 
	 * @return -- an array int[2], 0th element being the number of concepts found in the text, 1st element being the number of equations found in the text
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public int[] processText(String inputIdentifier, String text, String localityURI, String modelName, String modelPrefix) throws ConfigurationException, IOException {
		initializeTextModel(modelName, modelPrefix);
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
			String msg = "Extracting text from '" + source + "' into locality '" + localityURI + "'.";
			getCurationManager().notifyUser(getTextModelConfigMgr().getModelFolder(), msg, true);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String serviceBaseUri = getPreference(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getId());
		TextProcessingServiceInterface tpsi = new TextProcessingServiceInterface(serviceBaseUri);
		return tpsi.processText(inputIdentifier, text, localityURI);
	}
	
	public String[] retrieveGraph(String locality) throws IOException {
		logger.debug("Retrieving graph for locality '" + locality + "'");
		String serviceBaseUri = getPreference(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getId());
		TextProcessingServiceInterface tpsi = new TextProcessingServiceInterface(serviceBaseUri);
		return tpsi.retrieveGraph(locality);
	}
	
	public String clearGraph(String locality) throws IOException {
		logger.debug("Clearing graph for locality '" + locality + "'");
		String serviceBaseUri = getPreference(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getId());
		TextProcessingServiceInterface tpsi = new TextProcessingServiceInterface(serviceBaseUri);
		return tpsi.clearGraph(locality);
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
	public EquationVariableContextResponse equationVariableContext(String name, String locality) throws InvalidInputException, IOException {
		try {
			String msg = "Searching for name '" + name + "' in locality '" + locality + "'.";
			getCurationManager().notifyUser(getTextModelConfigMgr().getModelFolder(), msg, true);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String serviceUri = getPreference(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getId());
		TextProcessingServiceInterface tpsi = new TextProcessingServiceInterface(serviceUri);
		return tpsi.equationVariableContext(name, locality);
	}
	
	public List<UnitExtractionResponse> unitExtraction(String text, String locality) throws IOException, InvalidInputException {
		try {
			String msg = "Searching for units in text '" + text + "' in locality '" + locality + "'.";
			getCurationManager().notifyUser(getTextModelConfigMgr().getModelFolder(), msg, true);
		} catch (ConfigurationException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		String serviceUri = getPreference(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getId());
		TextProcessingServiceInterface tpsi = new TextProcessingServiceInterface(serviceUri);
		return tpsi.unitExtraction(text, locality);
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

	public Map<String, MergedEquationVariableContext> unifyEquationVariableContentResponses(List<EquationVariableContextResponse> responses) {
		Map<String, MergedEquationVariableContext> results = new HashMap<String, MergedEquationVariableContext>();
		for (EquationVariableContextResponse evcr : responses) {
			List<String[]> contentList = evcr.getResults();
			for (String[] content : contentList) {
				MergedEquationVariableContext mevc;
				String uri = content[3];
				if (!results.containsKey(uri)) {
					mevc = new MergedEquationVariableContext(uri);
					results.put(uri, mevc);
				}
				else {
					mevc = results.get(uri);
				}
				mevc.addLabel(content[2]);
				mevc.addEquationAndArgument(content[0], content[1]);
			}
		}
		return results;
	}

	private void initializeTextModel(String modelName, String modelPrefix) throws ConfigurationException, IOException {
		if (getCurationManager().getExtractionProcessor().getTextModel() == null) {
			// create new text model	
			setTextModelConfigMgr(getCurationManager().getConfigurationManager());
			OntDocumentManager owlDocMgr = getTextModelConfigMgr().getJenaDocumentMgr();
			OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
			if (owlDocMgr != null) {
				spec.setDocumentManager(owlDocMgr);
				owlDocMgr.setProcessImports(true);
			}
			setTextModel(ModelFactory.createOntologyModel(spec));	
			if (getTextModelName() == null) {
				setTextModelName(modelName);	// don't override a preset model name
			}
			if (getTextModelPrefix() == null) {
				if (modelPrefix == null) {
					throw new ConfigurationException("Model prefix is required");
				}
				setTextModelPrefix(modelPrefix);	// don't override a preset model name
			}
			addTextModel(modelName, getCurrentTextModel());
			getCurrentTextModel().setNsPrefix(getTextModelPrefix(), getCurationManager().getExtractionProcessor().getNamespaceFromModelName(getTextModelName()));
			Ontology modelOntology = getCurrentTextModel().createOntology(getTextModelName());
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
		getCurrentTextModel().getDocumentManager().addModel(importUri, importedOntModel, true);
		Ontology modelOntology = getCurrentTextModel().createOntology(modelName);
		if (importPrefix != null) {
			getCurrentTextModel().setNsPrefix(importPrefix, importUri);
		}
		com.hp.hpl.jena.rdf.model.Resource importedOntology = getCurrentTextModel().createResource(importUri);
		modelOntology.addImport(importedOntology);
		getCurrentTextModel().addSubModel(importedOntModel);
		getCurrentTextModel().addLoadedImport(importUri);
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
		if (textModelConfigMgr == null) {
			setTextModelConfigMgr(getCurationManager().getConfigurationManager());
		}
		return textModelConfigMgr;
	}

	private void setTextModelConfigMgr(IConfigurationManagerForIDE textMetaModelConfigMgr) {
		this.textModelConfigMgr = textMetaModelConfigMgr;
	}

	public void setTextModel(OntModel textModel) {
		this.textModel = textModel;	
	}
	
	public OntModel getCurrentTextModel() {
		return textModel;
	}

	public String getTextModelName() {
		return textModelName;
	}

	public void setTextModelName(String textmodelName) {
		this.textModelName = textmodelName;
	}

	public String getTextModelPrefix() {
		return textmodelPrefix;
	}

	public void setTextModelPrefix(String textmodelPrefix) {
		this.textmodelPrefix = textmodelPrefix;
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

	/** 
	 * Method to add a domainOntology to the text to triples service
	 * @param dialogModelName
	 * @param domainModel
	 * @throws IOException 
	 */
	public String addDomainOntology(String dialogModelName, OntModel domainModel) throws IOException {
//		if (getRegisteredDomainModel() == null) {
			OntModelSpec spec = new OntModelSpec(OntModelSpec.OWL_MEM);
			OntModel aggregateDomainModel = ModelFactory.createOntologyModel(spec);	
			aggregateDomainModel.add(domainModel.getBaseModel());
//			aggregateDomainModel.write(System.out);
			ExtendedIterator<OntModel> smitr = domainModel.listSubModels();
			for (OntModel sm : smitr.toList()) {
				ExtendedIterator<Ontology> ontitr = sm.listOntologies();
				String onturi = null;
				while (ontitr.hasNext()) {
					onturi = ontitr.next().getURI();
//					System.out.println(onturi);
					break;
				}
				if (onturi != null) {
					if (onturi.equals(SadlConstants.SADL_BASE_MODEL_URI) ||
							onturi.equals(SadlConstants.SADL_DEFAULTS_MODEL_URI) ||
							onturi.equals(SadlConstants.SADL_IMPLICIT_MODEL_URI) ||
							onturi.equals(SadlConstants.SADL_LIST_MODEL_URI) ||
							onturi.equals(IReasoner.SADL_BUILTIN_FUNCTIONS_URI)) {
						continue;
					}
				}
//				System.out.println("*****************************************************************");
//				sm.getBaseModel().write(System.out);
				aggregateDomainModel.add(sm.getBaseModel());
			}
			String ontologyAsString = getCurationManager().ontModelToString(aggregateDomainModel);
			(new SadlUtils()).stringToFile(new File("c:/tmp/isentrop_txt.owl"), ontologyAsString, false);	// temporary for debug purposes
			String serviceBaseUri = getPreference(DialogPreferences.ANSWER_TEXT_SERVICE_BASE_URI.getId());
			TextProcessingServiceInterface tpsi = new TextProcessingServiceInterface(serviceBaseUri);
			String response = tpsi.uploadDomainOntology(dialogModelName, dialogModelName, ontologyAsString);
			setRegisteredDomainModel(aggregateDomainModel);
			return response;
//		}
//		return "Domain ontology already uploaded";
	}
	
	/**
	 * Method to determine if a character is a vowel. 
	 * Reference: https://stackoverflow.com/questions/26557604/whats-the-best-way-to-check-if-a-character-is-a-vowel-in-java
	 * @param c
	 * @return
	 */
	public static boolean isVowel(char c) {
	    switch (c) {
	        case 65:
	        case 69:
	        case 73:
	        case 79:
	        case 85:
	        case 89:
	        case 97:
	        case 101:
	        case 105:
	        case 111:
	        case 117:
	        case 121:
	        case 192:
	        case 193:
	        case 194:
	        case 195:
	        case 196:
	        case 197:
	        case 198:
	        case 200:
	        case 201:
	        case 202:
	        case 203:
	        case 204:
	        case 205:
	        case 206:
	        case 207:
	        case 210:
	        case 211:
	        case 212:
	        case 213:
	        case 214:
	        case 216:
	        case 217:
	        case 218:
	        case 219:
	        case 220:
	        case 221:
	        case 224:
	        case 225:
	        case 226:
	        case 227:
	        case 228:
	        case 229:
	        case 230:
	        case 232:
	        case 233:
	        case 234:
	        case 235:
	        case 236:
	        case 237:
	        case 238:
	        case 239:
	        case 242:
	        case 243:
	        case 244:
	        case 245:
	        case 246:
	        case 248:
	        case 249:
	        case 250:
	        case 251:
	        case 252:
	        case 253:
	        case 255:
	        case 256:
	        case 257:
	        case 258:
	        case 259:
	        case 260:
	        case 261:
	        case 274:
	        case 275:
	        case 276:
	        case 277:
	        case 278:
	        case 279:
	        case 280:
	        case 281:
	        case 282:
	        case 283:
	        case 296:
	        case 297:
	        case 298:
	        case 299:
	        case 300:
	        case 301:
	        case 302:
	        case 303:
	        case 304:
	        case 305:
	        case 306:
	        case 307:
	        case 332:
	        case 333:
	        case 334:
	        case 335:
	        case 336:
	        case 337:
	        case 338:
	        case 339:
	        case 360:
	        case 361:
	        case 362:
	        case 363:
	        case 364:
	        case 365:
	        case 366:
	        case 367:
	        case 368:
	        case 369:
	        case 370:
	        case 371:
	        case 374:
	        case 375:
	        case 376:
	        case 506:
	        case 507:
	        case 508:
	        case 509:
	        case 510:
	        case 511:
	        case 512:
	        case 513:
	        case 514:
	        case 515:
	        case 516:
	        case 517:
	        case 518:
	        case 519:
	        case 520:
	        case 521:
	        case 522:
	        case 523:
	        case 524:
	        case 525:
	        case 526:
	        case 527:
	        case 532:
	        case 533:
	        case 534:
	        case 535:
	            return true;
	        default:
	            switch (c) {
	                case 7680:
	                case 7681:
	                case 7700:
	                case 7701:
	                case 7702:
	                case 7703:
	                case 7704:
	                case 7705:
	                case 7706:
	                case 7707:
	                case 7708:
	                case 7709:
	                case 7724:
	                case 7725:
	                case 7726:
	                case 7727:
	                case 7756:
	                case 7757:
	                case 7758:
	                case 7759:
	                case 7760:
	                case 7761:
	                case 7762:
	                case 7763:
	                case 7794:
	                case 7795:
	                case 7796:
	                case 7797:
	                case 7798:
	                case 7799:
	                case 7800:
	                case 7801:
	                case 7802:
	                case 7803:
	                case 7833:
	                case 7840:
	                case 7841:
	                case 7842:
	                case 7843:
	                case 7844:
	                case 7845:
	                case 7846:
	                case 7847:
	                case 7848:
	                case 7849:
	                case 7850:
	                case 7851:
	                case 7852:
	                case 7853:
	                case 7854:
	                case 7855:
	                case 7856:
	                case 7857:
	                case 7858:
	                case 7859:
	                case 7860:
	                case 7861:
	                case 7862:
	                case 7863:
	                case 7864:
	                case 7865:
	                case 7866:
	                case 7867:
	                case 7868:
	                case 7869:
	                case 7870:
	                case 7871:
	                case 7872:
	                case 7873:
	                case 7874:
	                case 7875:
	                case 7876:
	                case 7877:
	                case 7878:
	                case 7879:
	                case 7880:
	                case 7881:
	                case 7882:
	                case 7883:
	                case 7884:
	                case 7885:
	                case 7886:
	                case 7887:
	                case 7888:
	                case 7889:
	                case 7890:
	                case 7891:
	                case 7892:
	                case 7893:
	                case 7894:
	                case 7895:
	                case 7896:
	                case 7897:
	                case 7898:
	                case 7899:
	                case 7900:
	                case 7901:
	                case 7902:
	                case 7903:
	                case 7904:
	                case 7905:
	                case 7906:
	                case 7907:
	                case 7908:
	                case 7909:
	                case 7910:
	                case 7911:
	                case 7912:
	                case 7913:
	                case 7914:
	                case 7915:
	                case 7916:
	                case 7917:
	                case 7918:
	                case 7919:
	                case 7920:
	                case 7921:
	                case 7922:
	                case 7923:
	                case 7924:
	                case 7925:
	                case 7926:
	                case 7927:
	                case 7928:
	                case 7929:
	                    return true;
	            }
	    }
	    return false;
	}

	private OntModel getRegisteredDomainModel() {
		return registeredDomainModel;
	}

	private void setRegisteredDomainModel(OntModel registeredDomainModel) {
		this.registeredDomainModel = registeredDomainModel;
	}

}

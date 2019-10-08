package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.compiler.InvalidInputException;

import com.ge.research.sadl.darpa.aske.preferences.DialogPreferences;
import com.ge.research.sadl.reasoner.ConfigurationException;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;
import com.hp.hpl.jena.ontology.OntModel;

/**
 * 
 * Class to provide interface to the text extraction service, sending inputs and receiving outputs as serialized JSON
 * @author 200005201
 *
 */
public class TextProcessingServiceInterface extends JsonServiceInterface {
    private static final String DARPA_ASKE_TEXT_SERVICE_URL_FRAGMENT = "/darpa/aske/";

	private static final Logger logger = Logger.getLogger (TextProcessingServiceInterface.class) ;
    
	private String textServiceURL = null;

	public TextProcessingServiceInterface(String serviceBaseUri) {
		setTextServiceURL(serviceBaseUri);
	}

	private String getTextServiceURL() {
		return textServiceURL;
	}

	private void setTextServiceURL(String baseUrl) {
		String host = "vesuvius063";	// default
		int port = 4200;			// default
		if (baseUrl != null) {
			textServiceURL = baseUrl + DARPA_ASKE_TEXT_SERVICE_URL_FRAGMENT;
		}
		else {
			textServiceURL = "http://" + host + ":" + port + DARPA_ASKE_TEXT_SERVICE_URL_FRAGMENT;
		}
	}

	/**
	 * Method to retrieve a specified graph from the text service.
	 * @param locality -- identifies the graph to be retrieved
	 * @return -- String[2], where first is the serialization of the triples returned and the second is the format in which they are serialized, e.g., "n3"
	 * @throws IOException
	 */
	public String[] retrieveGraph(String locality) throws IOException {
		logger.debug("Retrieving graph for locality '" + locality + "'");
		String retrieveGraphServiceURL = getTextServiceURL() + "saveGraph";
		URL serviceUrl = new URL(retrieveGraphServiceURL);			
		JsonObject json = new JsonObject();
		json.addProperty("localityURI", locality);
		String response = makeConnectionAndGetResponse(serviceUrl, json);
//		logger.debug(response);
		if (response != null && response.length() > 0) {
//			OntModel theModel = getCurationManager().getExtractionProcessor().getTextModel();
			JsonElement je = new JsonParser().parse(response);
			if (je.isJsonObject()) {
				String format = je.getAsJsonObject().get("serializationFormat").getAsString();
				String triples = je.getAsJsonObject().get("triples").getAsString();
				logger.debug(triples);
				String[] results = new String[2];
				results[0] = format.toUpperCase();
				triples = triples.trim();
				triples = triples.startsWith("b") ? triples.substring(1) : triples;
				if (triples.startsWith("'") && triples.endsWith("'")) {
					triples = triples.substring(1, triples.length() - 1);
				}
				triples = triples.replace("\\n", "\n");
				triples = triples.replace("\\\\", "\\");
				results[1] = triples;
				return results;
			}
		}
		return null;
	}
	
	/**
	 * Method to clear a specified graph in the text service. For example, if a text has been processed and one
	 * wished to process it again, discarding the previous results.
	 * @param locality -- identifies the grpah to be cleared
	 * @return -- ?
	 * @throws IOException
	 */
	public String clearGraph(String locality) throws IOException {
		logger.debug("Retrieving graph for locality '" + locality + "'");
		String retrieveGraphServiceURL = getTextServiceURL() + "clearGraph";
		URL serviceUrl = new URL(retrieveGraphServiceURL);			
		JsonObject json = new JsonObject();
		json.addProperty("localityURI", locality);
		String response = makeConnectionAndGetResponse(serviceUrl, json);
//		logger.debug(response);
		if (response != null && response.length() > 0) {
//			OntModel theModel = getCurationManager().getExtractionProcessor().getTextModel();
			JsonElement je = new JsonParser().parse(response);
			if (je.isJsonObject()) {
				String msg = je.getAsJsonObject().get("message").getAsString();
				return msg;
			}
		}
		return null;
	}

	/**
	 * Method to process a block of text via the textToTriples service to find equations and concepts
	 * @param inputIdentifier -- the identifier, normally a model URI, of the source text
	 * @param text --  the source text to be processed
	 * @param locality -- the URI of the model to be used as context for the extraction
	 * @return -- an array int[2], 0th element being the number of concepts found in the text, 1st element being the number of equations found in the text
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public int[] processText(String inputIdentifier, String text, String locality) throws ConfigurationException, IOException {
		String textToTripleServiceURL = getTextServiceURL() + "text2triples";
		URL serviceUrl = new URL(textToTripleServiceURL);			
		JsonObject json = new JsonObject();
		json.addProperty("localityURI", locality);
		json.addProperty("text", text);
		logger.debug(json.toString());
		String response = makeConnectionAndGetResponse(serviceUrl, json);
		logger.debug(response);
		if (response != null && response.length() > 0) {
			JsonElement je = new JsonParser().parse(response);
			if (je.isJsonObject()) {
				int nc = je.getAsJsonObject().get("numConceptsExtracted").getAsInt();
				int neq = je.getAsJsonObject().get("numEquationsExtracted").getAsInt();
				logger.debug("nc=" + nc + ", neq=" + neq);
				int[] results = new int[2];
				results[0] = nc;
				results[1] = neq;
				return results;
			}
		}
		else {
			throw new IOException("No response received from service " + textToTripleServiceURL);
		}
		return null;
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
	public List<String[]> equationVariableContext(String name, String locality) throws InvalidInputException, IOException {
		if (name == null) {
			throw new InvalidInputException("Name cannot be null");
		}
		if (locality == null) {
			throw new InvalidInputException("Locality cannot be null");
		}
		String equationVariableContextServiceURL = getTextServiceURL() + "equationVariableContext";
		URL serviceUrl = new URL(equationVariableContextServiceURL);			
		JsonObject json = new JsonObject();
		json.addProperty("localityURI", locality);
		json.addProperty("variableName", name);
//		logger.debug(text);
		String response = makeConnectionAndGetResponse(serviceUrl, json);
//		logger.debug(response);
		if (response != null && response.length() > 0) {
			JsonElement je = new JsonParser().parse(response);
			logger.debug(je.toString());
			JsonElement msg = je.getAsJsonObject().get("message");
			JsonElement rslts = je.getAsJsonObject().get("results");
			JsonElement vn = null;
			JsonElement eqstr = null;
			List<String[]> results = new ArrayList<String[]>();
			if (rslts.isJsonArray()) {
				for (JsonElement rslt : rslts.getAsJsonArray()) {
					eqstr = rslt.getAsJsonObject().get("equationString");
					vn = rslt.getAsJsonObject().get("variableName");
					String[] use = new String[2];
					if (vn != null) {
						use[0] = vn.getAsString();
						if (eqstr != null) {
							use[1] = eqstr.getAsString();
						}
					}
					results.add(use);
				}
			}
			if (results.isEmpty()) {
				// failed to get any results--get the message
				if (msg instanceof JsonPrimitive) {
					String[] failureMsg = new String[1];
					if (msg.getAsJsonPrimitive().isString()) {
						failureMsg[0] = msg.getAsJsonPrimitive().getAsString();
					}
					else {
						failureMsg[0] = msg.toString();
					}
					results.add(failureMsg);
				}
			}
			return results;
		}
		return null;
	}

}

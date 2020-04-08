package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;
import org.eclipse.jdt.core.compiler.InvalidInputException;

import com.ge.research.sadl.reasoner.ConfigurationException;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

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
	
	public class EquationVariableContextResponse {
		private String message;
		private List<String[]> results;
		
		public EquationVariableContextResponse(String msg, List<String[]> results) {
			setMessage(msg);
			setResults(results);
		}
		
		public String getMessage() {
			return message;
		}
		
		private void setMessage(String message) {
			this.message = message;
		}
		
		public List<String[]> getResults() {
			return results;
		}
		
		private void setResults(List<String[]> results) {
			this.results = results;
		}
		
		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append(message);
			sb.append("\n");
			for (int i = 0; results != null && i < results.size(); i++) {
				String[] result = results.get(i);
				if (result.length >= 2) {
					sb.append(result[1]);
					sb.append(" appears in equation: \n");
					sb.append("    ");
					sb.append(result[0]);
					sb.append("\n");
					if (result.length == 4) {
						if (result[2] != null) {
							sb.append("concept: ");
							sb.append(result[2]);
							sb.append("\n");
						}
						if (result[3] != null) {
							sb.append("uri: ");
							sb.append(result[3]);
							sb.append("\n");
						}
					}
				}
			}
			return sb.toString();
		}
	}
	
	public class UnitExtractionResponse {
		private int start;
		private int end;
		private String relatedConceptName;
		private String relatedConceptURI;
		private String unitName;
		private String unitText;
		private String unitURI;
		private EquationVariableContextResponse unitContext;
		
		public UnitExtractionResponse(int st, int nd, String cName, String cURI, String uName, String uText, String uURI, EquationVariableContextResponse evcr) {
			setStart(st);
			setEnd(nd);
			setRelatedConceptName(cName);
			setRelatedConceptURI(cURI);
			setUnitName(uName);
			setUnitText(uText);
			setUnitURI(uURI);
			setUnitContext(evcr);
		}
		
		public UnitExtractionResponse() {
			// TODO Auto-generated constructor stub
		}

		public String toString() {
			StringBuilder sb = new StringBuilder();
			sb.append("start: ");
			sb.append(getStart());
			sb.append("\nend: ");
			sb.append(getEnd());
			sb.append("\nrelatedConceptName: ");
			sb.append(getRelatedConceptName());
			sb.append("\nrelatedConceptURI: ");
			sb.append(getRelatedConceptURI());
			sb.append("\nunitName: ");
			sb.append(getUnitName());
			sb.append("\nunitText: ");
			sb.append(getUnitText());
			sb.append("\nunitURI: ");
			sb.append(getUnitURI());
			return sb.toString();
		}

		public int getStart() {
			return start;
		}

		public void setStart(int start) {
			this.start = start;
		}

		public int getEnd() {
			return end;
		}

		public void setEnd(int end) {
			this.end = end;
		}

		public String getRelatedConceptName() {
			return relatedConceptName;
		}

		public void setRelatedConceptName(String relatedConceptName) {
			this.relatedConceptName = relatedConceptName;
		}

		public String getRelatedConceptURI() {
			return relatedConceptURI;
		}

		public void setRelatedConceptURI(String relatedConceptURI) {
			this.relatedConceptURI = relatedConceptURI;
		}

		public String getUnitName() {
			return unitName;
		}

		public void setUnitName(String unitName) {
			this.unitName = unitName;
		}

		public String getUnitText() {
			return unitText;
		}

		public void setUnitText(String unitText) {
			this.unitText = unitText;
		}

		public String getUnitURI() {
			return unitURI;
		}

		public void setUnitURI(String unitURI) {
			this.unitURI = unitURI;
		}

		public EquationVariableContextResponse getUnitContext() {
			return unitContext;
		}

		public void setUnitContext(EquationVariableContextResponse unitContext) {
			this.unitContext = unitContext;
		}
	}
	
	public TextProcessingServiceInterface(String serviceBaseUri) {
		setTextServiceURL(serviceBaseUri);
	}
	
	private String getTextServiceURL() {
		return textServiceURL;
	}

	private void setTextServiceURL(String baseUrl) {
		String host = "vesuvius-dev.crd.ge.com";	// default
//		String host = "vesuvius063.crd.ge.com";	// default
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
	 * @return -- String[3], where first is the localityURI of the graph, 
	 * 			the second is the format of the serialized graph, e.g., "n3", and 
	 * 			the third is serialization of the triples returned
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
				String[] results = new String[3];
				results[0] = locality;
				results[1] = format.toUpperCase();
				triples = triples.trim();
				triples = triples.startsWith("b") ? triples.substring(1) : triples;
				if (triples.startsWith("'") && triples.endsWith("'")) {
					triples = triples.substring(1, triples.length() - 1);
				}
				triples = triples.replace("\\n", "\n");
				triples = triples.replace("\\\\", "\\");
				results[2] = triples;
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
	 * Method to upload a domain ontology to the text service.
	 * @return -- ?
	 * @throws IOException
	 */
	public String uploadDomainOntology(String localityUri, String domainBaseUri, String ontologyAsString) throws IOException {
		if (!domainBaseUri.endsWith("#")) {
			domainBaseUri = domainBaseUri + "#";
		}
		logger.debug("Uploading domain ontology with baseURI'" + domainBaseUri + "' for locality '" + localityUri + "'");
		String uploadDomainOntologyServiceURL = getTextServiceURL() + "uploadDomainOntology";
		URL serviceUrl = new URL(uploadDomainOntologyServiceURL);			
		JsonObject json = new JsonObject();
		json.addProperty("localityURI", localityUri);
		json.addProperty("baseURI", domainBaseUri);
		json.addProperty("ontologyAsString", ontologyAsString);
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
	 * @param text --  the source text to be processed
	 * @param locality -- the URI of the model to be used as context for the extraction
	 * @return -- an array int[2], 0th element being the number of concepts found in the text, 1st element being the number of equations found in the text
	 * @throws ConfigurationException
	 * @throws IOException
	 */
	public int[] processText(String text, String locality) throws ConfigurationException, IOException {
		String textToTripleServiceURL = getTextServiceURL() + "text2triples";
		URL serviceUrl = new URL(textToTripleServiceURL);			
		JsonObject json = new JsonObject();
		json.addProperty("localityURI", locality);
		json.addProperty("text", text);
		logger.debug(json.toString());
		try {
			String response = makeConnectionAndGetResponse(serviceUrl, json);
			logger.debug(response);
			if (response != null && response.length() > 0) {
				if (logger.isDebugEnabled()) {
					logger.debug(serviceUrl);
					logger.debug(json);
				}
				System.out.println(response);
				System.out.println(serviceUrl);
				System.out.println(json);
//				System.out.println(response);
				System.out.println("\n");
				System.out.flush();
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
		}
		catch (Throwable t) {
			logger.error(serviceUrl);
			logger.error(json);
			logger.error(JsonServiceInterface.aggregateExceptionMessage(t));
			logger.error("\n");
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
	public EquationVariableContextResponse equationVariableContext(String name, String locality) throws InvalidInputException, IOException {
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
			List<String[]> results = new ArrayList<String[]>();
			if (rslts.isJsonArray()) {
				for (JsonElement rslt : rslts.getAsJsonArray()) {
					JsonElement eqStr = rslt.getAsJsonObject().get("equationString");
					JsonElement vn = rslt.getAsJsonObject().get("variableName");
					JsonElement lbl = rslt.getAsJsonObject().get("entityLabel");
					JsonElement uri = rslt.getAsJsonObject().get("entityURI");
					String[] use = new String[4];
					if (vn != null) {
						use[0] = eqStr != null ? eqStr.getAsString() : null;
						use[1] = vn != null ? vn.getAsString() : null;
						use[2] = lbl != null ? lbl.getAsString() : null;
						use[3] = uri != null ? uri.getAsString() : null;
					}
					results.add(use);
				}
			}
			String msgStr = null;
			if (msg instanceof JsonPrimitive) {
				if (msg.getAsJsonPrimitive().isString()) {
					msgStr = msg.getAsJsonPrimitive().getAsString();
				}
				else {
					msgStr = msg.toString();
				}
			}
			EquationVariableContextResponse evcr = new EquationVariableContextResponse(msgStr, results);
			return evcr;
		}
		return null;
	}

	public List<UnitExtractionResponse> unitExtraction(String text, String locality) throws IOException, InvalidInputException {
		if (text == null) {
			throw new InvalidInputException("Text cannot be null");
		}
		if (locality == null) {
			throw new InvalidInputException("Locality cannot be null");
		}
		String unitExtractionServiceURL = getTextServiceURL() + "unitextraction";
		URL serviceUrl = new URL(unitExtractionServiceURL);			
		JsonObject json = new JsonObject();
		json.addProperty("localityURI", locality);
		json.addProperty("text", text);
//		logger.debug(text);
		String response = makeConnectionAndGetResponse(serviceUrl, json);
//		logger.debug(response);
		if (response != null && response.length() > 0) {
			JsonElement je = new JsonParser().parse(response);
			logger.debug(je.toString());
			if (je.isJsonArray()) {
				List<UnitExtractionResponse> results = new ArrayList<UnitExtractionResponse>();
				for (JsonElement el : je.getAsJsonArray()) {
					UnitExtractionResponse uer = new UnitExtractionResponse();
					uer.setRelatedConceptName(el.getAsJsonObject().get("relatedConceptName").getAsString());
					uer.setRelatedConceptURI(el.getAsJsonObject().get("relatedConceptURI").getAsString());
					uer.setUnitName(el.getAsJsonObject().get("unitName").getAsString());
					uer.setUnitText(el.getAsJsonObject().get("unitText").getAsString());
					uer.setUnitURI(el.getAsJsonObject().get("unitURI").getAsString());
					uer.setStart(Integer.parseInt(el.getAsJsonObject().get("start").getAsString().trim()));
					uer.setEnd(Integer.parseInt(el.getAsJsonObject().get("end").getAsString().trim()));
//	TODO			uer.setUnitContext(unitContext);
					results.add(uer);
				}
				return results;
			}
		}
		return null;
	}
}

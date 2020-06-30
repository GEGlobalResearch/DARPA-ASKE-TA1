package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * Class to provide interface to the KChain service, sending inputs and receiving outputs as serialized JSON
 * @author 200005201
 *
 */
public class KChainServiceInterface extends JsonServiceInterface {
    private static final String DARPA_ASKE_KCHAIN_SERVICE_URL_FRAGMENT = "/darpa/aske/kchain/";

	private String kchainServiceURL = null;

	public KChainServiceInterface(String serviceBaseUri) {
		setKchainServiceURL(serviceBaseUri);
	}

	/**
	 * Method to build a KChain computational graph model
	 * @param modelUri--model URI
	 * @param equationModel--the Python script for a physics-based model
	 * @param dataLocation--the data location for a data-driven model
	 * @param inputs--a String array containing the inputs
	 * @param outputs--a String array containing the outputs
	 * @return--true if sucessful else false
	 * @throws IOException
	 */
	public Object[] buildCGModel(String modelUri, String equationModel, String dataLocation, List<String[]> inputs, List<String[]> outputs) throws IOException {
		/*
		{
		  "inputVariables": [
		    {
		      "name": "string",
		      "type": "string",
		      "value": "string"
		    }
		  ],
		  "outputVariables": [
		    {
		      "name": "string",
		      "type": "string",
		      "value": "string"
		    }
		  ],
		  "dataLocation": "string",
		  "equationModel": "string",
		  "modelName": "string"
		}
		 */
		JsonObject json = generateRequestJson(modelUri, equationModel, dataLocation, inputs, outputs);

		String jsonResponse = buildCGModel(json);

		/*
		{
		  "modelType": "string",
		  "trainedState": true,
		  "metagraphLocation": "string"
		}
		 */
		JsonElement je = new JsonParser().parse(jsonResponse);
		Object[] returnValues = new Object[3];
		if (je.isJsonObject()) {
			JsonObject jobj = je.getAsJsonObject();
			String modelType = jobj.get("modelType").getAsString();
			returnValues[0] = modelType;
			String metagraphLocation = jobj.get("metagraphLocation").getAsString();
			returnValues[1] = metagraphLocation;
			boolean trained = jobj.get("trainedState").getAsBoolean();	
			returnValues[2] = trained;
		}
		else {
			throw new IOException("Unexpected response: " + je.toString());
		}
		return returnValues;
	}

	public String buildCGModel(JsonObject json) throws MalformedURLException, IOException {
		logger.debug(json.toString());

		//				String host = "3.39.122.224";
		//				String host = "3.1.176.139";

		String buildServiceURL = getKchainServiceURL() + "build";
		URL serviceUrl = new URL(buildServiceURL);			

		String jsonResponse = makeConnectionAndGetResponse(serviceUrl, json);

		logger.debug(jsonResponse);
		return jsonResponse;
	}

	private JsonObject generateRequestJson(String modelUri, String equationModel, String dataLocation,
			List<String[]> inputs, List<String[]> outputs) {
		JsonObject json = new JsonObject();
		json.addProperty("modelName", modelUri);
		if (equationModel != null) {
			json.addProperty("equationModel", equationModel);
		}
		if (dataLocation != null) {
			json.addProperty("dataLocation", dataLocation);
		}
		JsonArray jarrin = new JsonArray();
		json.add("inputVariables", jarrin);
		if (inputs != null) {
			for (int i = 0; i < inputs.size(); i++) {
				String[] input = inputs.get(i);
				JsonObject inputj = new JsonObject();
				inputj.addProperty("name", input[0]);
				inputj.addProperty("type", input[1]);
				if (input.length > 2) {
					inputj.addProperty("value", input[2]);
				}
				jarrin.add(inputj);
			}
		}
		JsonArray jarrout = new JsonArray();
		json.add("outputVariables", jarrout);
		if (outputs != null) {
			for (String[] output : outputs) {
				JsonObject outputj = new JsonObject();
				outputj.addProperty("name", output[0]);
				outputj.addProperty("type", output[1]);
				jarrout.add(outputj);
			}
		}
		
		logger.debug(json.toString());
		return json;
	}
	
	public boolean addCGModeltoExistingKGModel(String modelUri, String modelType, String metagraphLocation, Double[] dfd) {
		// TODO Auto-generated method stub
		
		return true;
	}
	
	public List<List<String[]>> evalCGModel(String modelUri, List<String[]> inputs, List<String[]> outputs) throws IOException {
		/*
		{
		  "inputVariables": [
		    {
		      "name": "string",
		      "type": "string",
		      "value": "string"
		    }
		  ],
		  "outputVariables": [
		    {
		      "name": "string",
		      "type": "string",
		      "value": "string"
		    }
		  ],
		  "modelName": "string"
		}
		 */
		JsonObject json = generateRequestJson(modelUri, null, null, inputs, outputs);

		String jsonResponse = evalCGModel(json);
		
		/*
		{
		  "outputVariables": [
		    {
		      "name": "string",
		      "type": "string",
		      "value": "string"
		    }
		  ]
		}
 */
		JsonElement je = new JsonParser().parse(jsonResponse);
		
		if (!je.isJsonObject()) {
			throw new IOException("Unexpected response: " + je.toString());
		}
		
		JsonObject evalResults = je.getAsJsonObject();
		JsonArray ovars = evalResults.get("outputVariables").getAsJsonArray();
		List<String[]> results = new ArrayList<String[]>();
		for (JsonElement ovar : ovars) {
			String[] aValue = new String[3];
			aValue[0] = ovar.getAsJsonObject().get("name").getAsString();
			aValue[1] = ovar.getAsJsonObject().get("type").getAsString();
			aValue[2] = ovar.getAsJsonObject().get("value").getAsString();	
			results.add(aValue);
		}
		JsonElement du = evalResults.get("defaultsUsed");
		List<String[]> defaultValues = null;
		if (du != null) {
			JsonArray defsUsed = du.getAsJsonArray();
			if (defsUsed != null) {
				defaultValues = new ArrayList<String[]>();
				for (JsonElement defobj : defsUsed) {
					String[] aDefault = new String[2];
					aDefault[0] = defobj.getAsJsonObject().get("name").getAsString();
					aDefault[1] = defobj.getAsJsonObject().get("value").getAsString();
					defaultValues.add(aDefault);
				}
			}
		}
		// return results: 
		List<List<String[]>> retLists = new ArrayList<List<String[]>>();
		retLists.add(results);
		if (defaultValues != null) {
			retLists.add(defaultValues);
		}
		return retLists;
	}

	public String evalCGModel(JsonObject json) throws MalformedURLException, IOException {
		String evalServiceURL = getKchainServiceURL() + "evaluate";
		URL serviceUrl = new URL(evalServiceURL);	
		
		logger.debug(json.toString());
		
		String jsonResponse = makeConnectionAndGetResponse(serviceUrl, json);
		
		logger.debug(jsonResponse);
		return jsonResponse;
	}
	
	private String getKchainServiceURL() {
		return kchainServiceURL;
	}

	private void setKchainServiceURL(String baseUrl) {
		String host = "vesuvius-dev.crd.ge.com";	// default
		int port = 12345;			// default
		if (baseUrl != null) {
			kchainServiceURL = baseUrl + DARPA_ASKE_KCHAIN_SERVICE_URL_FRAGMENT;
		}
		else {
			kchainServiceURL = "http://" + host + ":" + port + DARPA_ASKE_KCHAIN_SERVICE_URL_FRAGMENT;
		}
	}

}

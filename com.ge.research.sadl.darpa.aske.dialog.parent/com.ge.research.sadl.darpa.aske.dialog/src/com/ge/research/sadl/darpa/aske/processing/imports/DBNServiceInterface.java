package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
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
public class DBNServiceInterface extends JsonServiceInterface {

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
	public boolean executeDBN(String modelUri, String equationModel, String dataLocation, List<String[]> inputs, List<String[]> outputs) throws IOException {
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
		//				String host = "3.39.122.224";
		//				String host = "3.1.176.139";
		String host = "vesuvius-dev.crd.ge.com";
		//				String host = "3.39.122.58";
		int port = 12345;
		String dbnServiceURL = "http://" + host + ":" + port + "/process";

		URL serviceUrl = new URL(dbnServiceURL);			

		JsonObject json = generateRequestJson(modelUri, equationModel, dataLocation, inputs, outputs);
		
		String jsonResponse = makeConnectionAndGetResponse(serviceUrl, json);

		System.out.println(jsonResponse);

		/*
		{
		  "modelType": "string",
		  "trainedState": true,
		  "metagraphLocation": "string"
		}
		 */
		JsonElement je = new JsonParser().parse(jsonResponse);
		if (je.isJsonObject()) {
			JsonObject jobj = je.getAsJsonObject();
			String modelType = jobj.get("modelType").getAsString();
			String metagraphLocation = jobj.get("metagraphLocation").getAsString();
			boolean trained = jobj.get("trainedState").getAsBoolean();	

			Double[] dfd = null;
			//		JsonElement dfelement = jobj.get("degreeFitness");
			//		if (dfelement != null) {
			//			JsonArray df = dfelement.getAsJsonArray();
			//			dfd = new Double[df.size()];
			//			int idx = 0;
			//			for (JsonElement arrel : df) {
			//				dfd[idx++] = arrel.getAsDouble();
			//			}
			//		}
			//				 add to KG: 
			return addCGModeltoExistingKGModel(modelUri, modelType, metagraphLocation, dfd);
		}
		else {
			throw new IOException("Unexpected response: " + je.toString());
		}
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
		JsonArray jarrout = new JsonArray();
		json.add("outputVariables", jarrout);
		for (String[] output : outputs) {
			JsonObject outputj = new JsonObject();
			outputj.addProperty("name", output[0]);
			outputj.addProperty("type", output[1]);
			jarrout.add(outputj);
		}
		
		System.out.println(json.toString());
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

//		String host = "3.39.122.224";
//		String host = "3.1.176.139";
		String host = "vesuvius-dev.crd.ge.com";
//		String host = "3.39.122.58";
		int port = 12345;
		String kchainServiceURL = "http://" + host + ":" + port + "/darpa/aske/kchain/";
		String evalServiceURL = kchainServiceURL + "evaluate";
		URL serviceUrl = new URL(evalServiceURL);			

		JsonObject json = generateRequestJson(modelUri, null, null, inputs, outputs);
		
		String jsonResponse = makeConnectionAndGetResponse(serviceUrl, json);
		
		System.out.println(jsonResponse);
		
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
	
}

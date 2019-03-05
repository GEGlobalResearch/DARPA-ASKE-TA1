package com.ge.research.sadl.darpa.aske.tests.kchain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

@Ignore
public class KChainServiceTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
	@Ignore
	public void test_01() throws IOException {
		String nlpServiceURL = "http://vesuvius-test.crd.ge.com:9080/kcud/";
		String text = "Barack Obama was the 44th president of the United States from 2009 to 2017"; 
		
		String phraseServiceURL = nlpServiceURL + "chunkSelPhraseType?text=" + URLEncoder.encode(text, "UTF-8") + "&phraseType=NP";
		
		URL url = new URL(phraseServiceURL);			
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setRequestMethod("GET");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));			
		String response = "", jsonResponse = "";
				
		while((response = br.readLine()) != null){
			jsonResponse = jsonResponse + response;
		}
		
		System.out.println(jsonResponse);
	}

	@Test
	@Ignore
	public void test_02() throws IOException {
		String serviceIP = "3.39.120.21";
		String kchainServiceURL = "http://" + serviceIP + ":8080/kchain/";
		
		String requestString = "{\r\n" + 
				"\"inputVariableNames\" : [\"a\", \"b\"],\r\n" + 
				"\"outputVariableNames\" : [\"c\"],\r\n" + 
				"\"dataLocation\" : \"http://ge.com/data\",\r\n" + 
				"\"equationModel\" : \"c = a + b\",\r\n" + 
				"\"modelName\" : \"http://com.research.ge/darpa/aske/answer/test_02/binaryadd\"\r\n" + 
				"}"; 
		
		System.out.print(requestString + "\n\n");
		
		String buildServiceURL = kchainServiceURL + "build?requestString=" + URLEncoder.encode(requestString, "UTF-8");
		
		URL url = new URL(buildServiceURL);			
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setRequestMethod("GET");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));			
		String response = "", jsonResponse = "";
				
		while((response = br.readLine()) != null){
			jsonResponse = jsonResponse + response;
		}
		
		System.out.println(jsonResponse);
	}

	@Test
	public void test_03() throws IOException {
		String[] inputVariableNames = {"a", "b"};
		String[] outputVariableNames = {"c"};
		String dataLocation = "ht;tp://ge.com/data";
		String equationModel = "c = a + b";
//		String modelUri = "http://com.research.ge/darpa/aske/answer/test_02/binaryadd";
		String modelUri = "binaryadd";
		// add to KG: 
		buildCGModel(modelUri, inputVariableNames, outputVariableNames, dataLocation, equationModel);
	}
	
	@Test
	public void testEval_01() throws IOException {
		Map<String, Double[]> inputs = new HashMap<String, Double[]>();
		Double[] avals = {1.0, 1.1};
		inputs.put("a", avals);
		Double[] bvals = {1.1, 2.2};
		inputs.put("b", bvals);
		String[] outputVariableNames = {"c", "d"};
//		String modelUri = "http://com.research.ge/darpa/aske/answer/test_02/binaryadd";
		String modelUri = "binaryadd";
		// add to KG: 
		evalCGModel(modelUri, inputs, outputVariableNames);
	}
	
	private Map<String, Double[]> evalCGModel(String modelUri, Map<String, Double[]> inputs, String[] outputVariableNames) throws IOException {
		String host = "3.39.120.21";
		int port = 8080;
		String kchainServiceURL = "http://" + host + ":" + port + "/kchain/";
		/*
		 * {
"modelName" : "URIString",
"inputs" :�
� [
��� {
��� "name" : "a",
��� "values" : [1.0, 1.1]
��� },
��� {
��� "name" : "b",
��� "values" : [1.1, 2.2]
��� }
� ],
"outputVarNames" : ["c", "d"]
}
		 */
		JsonObject json = new JsonObject();
		json.addProperty("modelName", modelUri);
		Iterator<String> keyitr = inputs.keySet().iterator();
		while (keyitr.hasNext()) {
			String varName = keyitr.next();
			Double[] vals = inputs.get(varName);
			JsonArray jarrin = new JsonArray();
			JsonObject input = new JsonObject();
			input.addProperty("name", varName);
			JsonArray aVals = new JsonArray();
			for (Double val : vals) {
				aVals.add(val);
			}
			input.add("values", aVals);
			jarrin.add(input);
		}
		
		JsonArray jarrout = new JsonArray();
		for (String outvar : outputVariableNames) {
			jarrout.add(outvar);
		}
		json.add("outputVarNames", jarrout);
		
		String requestString = json.toString(); 	
		String buildServiceURL = kchainServiceURL + "eval?requestString=" + URLEncoder.encode(requestString, "UTF-8");
		
		URL url = new URL(buildServiceURL);			
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));			
		String response = "", jsonResponse = "";
				
		while((response = br.readLine()) != null){
			jsonResponse = jsonResponse + response;
		}
		
		System.out.println(jsonResponse);
/*
{
� "outputs" :�
� [
��� {
��� "name" : "c",
��� "values" : [1.0; 2.0]
��� },
��� {
��� "name" : "d",
��� "values" : [1.1; 2.2]
��� }
� ]
}
 */
		Map<String, Double[]> results = new HashMap<String, Double[]>();
		JsonElement je = new JsonParser().parse(jsonResponse);
		JsonObject jobj = je.getAsJsonObject();
		JsonArray outputs = jobj.get("outputs").getAsJsonArray();
		for (JsonElement outputel : outputs) {
			JsonObject output = outputel.getAsJsonObject();
			String outputName = output.get("name").getAsString();
			JsonArray outputValues = output.get("values").getAsJsonArray();
			Double[] vals = new Double[outputValues.size()];
			int idx = 0;
			for (JsonElement arrel : outputValues) {
				vals[idx++] = arrel.getAsDouble();
			}
			results.put(outputName, vals);
		}
		// return results: 
		return results;
	}

	public boolean buildCGModel(String modelUri, String[] inputVariableNames, String[] outputVariableNames, String dataLocation,
			String pythonModelScript) throws IOException {
		String host = "3.39.120.21";
		int port = 8080;
		String kchainServiceURL = "http://" + host + ":" + port + "/kchain/";
		JsonObject json = new JsonObject();
		JsonArray jarrin = new JsonArray();
		for (String inputVar : inputVariableNames) {
			jarrin.add(inputVar);
		}
		JsonArray jarrout = new JsonArray();
		for (String outvar : outputVariableNames) {
			jarrout.add(outvar);
		}
		json.add("inputVariableNames", jarrin);
		json.add("outputVariableNames", jarrout);
		json.addProperty("dataLocation", dataLocation);
		json.addProperty("equationModel", pythonModelScript);
		json.addProperty("modelName", modelUri);
		
		String requestString = json.toString(); 	
		String buildServiceURL = kchainServiceURL + "build?requestString=" + URLEncoder.encode(requestString, "UTF-8");
		
		URL url = new URL(buildServiceURL);			
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		connection.setRequestMethod("GET");
		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));			
		String response = "", jsonResponse = "";
				
		while((response = br.readLine()) != null){
			jsonResponse = jsonResponse + response;
		}
		
		System.out.println(jsonResponse);
		
		JsonElement je = new JsonParser().parse(jsonResponse);
		JsonObject jobj = je.getAsJsonObject();
		String modelType = jobj.get("modelType").getAsString();
		String metagraphLocation = jobj.get("metagraphLocation").getAsString();
		boolean trained = jobj.get("trainedState").getAsBoolean();	
		Double[] dfd = null;
		JsonElement dfelement = jobj.get("degreeFitness");
		if (dfelement != null) {
			JsonArray df = dfelement.getAsJsonArray();
			dfd = new Double[df.size()];
			int idx = 0;
			for (JsonElement arrel : df) {
				dfd[idx++] = arrel.getAsDouble();
			}
		}
		// add to KG: 
		return addCGModeltoExistingKGModel(modelUri, modelType, metagraphLocation, dfd);
	}

	/**
	 * Method to add CG information returned from call to build to the existing KG model.
	 * @param modelUri
	 * @param modelType
	 * @param metagraphLocation
	 * @param dfd
	 * @return
	 */
	public boolean addCGModeltoExistingKGModel(String modelUri, String modelType, String metagraphLocation, Double[] dfd) {
		// TODO Auto-generated method stub
		
		return true;
	}
	
	public String addKGModel(String localname) {
		// create KG model
		String modelUri = null;
		return modelUri;
	}

}

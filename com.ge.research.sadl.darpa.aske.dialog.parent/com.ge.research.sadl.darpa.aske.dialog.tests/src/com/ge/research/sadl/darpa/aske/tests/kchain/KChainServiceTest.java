package com.ge.research.sadl.darpa.aske.tests.kchain;

import static org.junit.Assert.*;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;

import org.junit.Before;
import org.junit.Test;

public class KChainServiceTest {

	@Before
	public void setUp() throws Exception {
	}

	@Test
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

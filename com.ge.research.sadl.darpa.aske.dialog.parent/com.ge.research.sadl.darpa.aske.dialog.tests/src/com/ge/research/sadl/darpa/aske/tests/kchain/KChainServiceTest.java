package com.ge.research.sadl.darpa.aske.tests.kchain;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class KChainServiceTest {

	@Before
	public void setUp() throws Exception {
	}

	@Ignore
	@Test
	public void testBuild_01() throws IOException {
		/*
{
  "dataLocation": "../Datasets/Force_dataset.csv",
  "inputVariables": [
    {
      "name": "Mass",
      "type": "double"
    },
    {
      "name": "Acceleration",
      "type": "double"
    }
  ],
  "modelName": "Newtons2ndLawModel",
  "outputVariables": [
    {
      "name": "Force",
      "type": "double"
    }
  ]
}
		 */
		List<String[]> inputs = new ArrayList<String[]>();
		String[] input1 = new String[2];
		input1[0] = "Mass";
		input1[1] = "double";
		inputs.add(input1);
		String[] input2 = new String[2];
		input2[0] = "Acceleration";
		input2[1] = "double";
		inputs.add(input2);
		List<String[]> outputs = new ArrayList<String[]>();
		String[] output1 = new String[2];
		output1[0] = "Force";
		output1[1] = "double";
		outputs.add(output1);

		String dataLocation = "../Datasets/Force_dataset.csv";
		String equationModel =null;
//		String modelUri = "http://com.research.ge/darpa/aske/answer/test_02/binaryadd";
		String modelUri = "Newtons2ndLawModel";
		// add to KG: 
		buildCGModel(modelUri, equationModel, dataLocation, inputs, outputs);
	}

	@Ignore
	@Test
	public void testBuild_02() throws IOException {
/*
{
  "equationModel": "Force = Mass*Acceleration",
  "inputVariables": [
    {
      "name": "Mass",
      "type": "double"
    },
    {
      "name": "Acceleration",
      "type": "double"
    }
  ],
  "modelName": "Newtons2ndLawModel",
  "outputVariables": [
    {
      "name": "Force",
      "type": "double"
    }
  ]
}
 */
		List<String[]> inputs = new ArrayList<String[]>();
		String[] input1 = new String[2];
		input1[0] = "Mass";
		input1[1] = "double";
		inputs.add(input1);
		String[] input2 = new String[2];
		input2[0] = "Acceleration";
		input2[1] = "double";
		inputs.add(input2);
		List<String[]> outputs = new ArrayList<String[]>();
		String[] output1 = new String[2];
		output1[0] = "Force";
		output1[1] = "double";
		outputs.add(output1);

		String dataLocation = null;
		String equationModel = "Force = Mass*Acceleration";
//		String modelUri = "http://com.research.ge/darpa/aske/answer/test_02/binaryadd";
		String modelUri = "Newtons2ndLawModelPB";
		// add to KG: 
		buildCGModel(modelUri, equationModel, dataLocation, inputs, outputs);
	}
	
	@Ignore
	@Test
	public void testEval_01() throws IOException {
		/*
{
  "inputVariables": [
    {
      "name": "Mass",
      "type": "double",
      "value": "[1.0]"
    },
    {
      "name": "Acceleration",
      "type": "double",
      "value": "[0.5]"
    }
  ],
  "modelName": "Newtons2ndLawModel",
  "outputVariables": [
    {
      "name": "Force",
      "type": "double"
    }
  ]
}		 
		 */
		List<String[]> inputs = new ArrayList<String[]>();
		String[] input1 = new String[3];
		input1[0] = "Mass";
		input1[1] = "double";
		input1[2] = "[1.0]";
		inputs.add(input1);
		String[] input2 = new String[3];
		input2[0] = "Acceleration";
		input2[1] = "double";
		input2[2] = "[0.5]";
		inputs.add(input2);
		List<String[]> outputs = new ArrayList<String[]>();
		String[] output1 = new String[2];
		output1[0] = "Force";
		output1[1] = "double";
		outputs.add(output1);
//		String modelUri = "http://com.research.ge/darpa/aske/answer/test_02/binaryadd";
		String modelUri = "Newtons2ndLawModel";
		// add to KG: 
		evalCGModel(modelUri, inputs, outputs);
	}
	
	@Ignore
	@Test
	public void testEval_02() throws IOException {
		/*
{
  "inputVariables": [
    {
      "name": "Mass",
      "type": "double",
      "value": "[1.0]"
    },
    {
      "name": "Acceleration",
      "type": "double",
      "value": "[0.5]"
    }
  ],
  "modelName": "Newtons2ndLawModel",
  "outputVariables": [
    {
      "name": "Force",
      "type": "double"
    }
  ]
}		 
		 */
		List<String[]> inputs = new ArrayList<String[]>();
		String[] input1 = new String[3];
		input1[0] = "Mass";
		input1[1] = "double";
		input1[2] = "[1.0]";
		inputs.add(input1);
		String[] input2 = new String[3];
		input2[0] = "Acceleration";
		input2[1] = "double";
		input2[2] = "[0.5]";
		inputs.add(input2);
		List<String[]> outputs = new ArrayList<String[]>();
		String[] output1 = new String[2];
		output1[0] = "Force";
		output1[1] = "double";
		outputs.add(output1);
//		String modelUri = "http://com.research.ge/darpa/aske/answer/test_02/binaryadd";
		String modelUri = "Newtons2ndLawModelPB";
		// add to KG: 
		evalCGModel(modelUri, inputs, outputs);
	}
	private List<String[]> evalCGModel(String modelUri, List<String[]> inputVariables, List<String[]> outputVariables) throws IOException {
		String host = "3.39.127.101";
		int port = 12345;
		String kchainServiceURL = "http://" + host + ":" + port + "/darpa/aske/kchain/";
		/*
		 * 
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
		JsonObject json = new JsonObject();
		json.addProperty("modelName", modelUri);
		JsonArray inputVars = new JsonArray();
		json.add("inputVariables", inputVars);
		Iterator<String[]> ivitr = inputVariables.iterator();
		while (ivitr.hasNext()) {
			String[] ivvals = ivitr.next();
			JsonObject input = new JsonObject();
			input.addProperty("name", ivvals[0]);
			input.addProperty("type", ivvals[1]);
			input.addProperty("value", ivvals[2]);
			inputVars.add(input);
		}
		
		JsonArray jarrout = new JsonArray();
		json.add("outputVariables", jarrout);
		Iterator<String[]> ovitr = outputVariables.iterator();
		while (ovitr.hasNext()) {
			String[] ovvals = ovitr.next();
			JsonObject output = new JsonObject();
			output.addProperty("name", ovvals[0]);
			output.addProperty("type", ovvals[1]);
			jarrout.add(output);
		}
		
		String evalServiceURL = kchainServiceURL + "evaluate";
		URL serviceUrl = new URL(evalServiceURL);			

		String jsonResponse = makeConnectionAndGetResponse(serviceUrl, json);
		System.out.println(jsonResponse);
		JsonObject evalResults = new JsonParser().parse(jsonResponse).getAsJsonObject();
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
		JsonArray ovars = evalResults.get("outputVariables").getAsJsonArray();
		List<String[]> results = new ArrayList<String[]>();
		for (JsonElement ovar : ovars) {
			String[] aValue = new String[3];
			aValue[0] = ovar.getAsJsonObject().get("name").getAsString();
			aValue[1] = ovar.getAsJsonObject().get("type").getAsString();
			aValue[2] = ovar.getAsJsonObject().get("value").getAsString();	
			results.add(aValue);
		}
		// return results: 
		return results;
	}

	public boolean buildCGModel(String modelUri, String equationModel, String dataLocation, List<String[]> inputs, List<String[]> outputs) throws IOException {
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
		String host = "3.39.127.101";
		int port = 12345;
		String kchainServiceURL = "http://" + host + ":" + port + "/darpa/aske/kchain/";
		
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
		for (String[] input : inputs) {
			JsonObject inputj = new JsonObject();
			inputj.addProperty("name", input[0]);
			inputj.addProperty("type", input[1]);
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
		
		String buildServiceURL = kchainServiceURL + "build";
		URL serviceUrl = new URL(buildServiceURL);			

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

	private String makeConnectionAndGetResponse(URL url, JsonObject jsonObject) {
		String response = "";
		try {
			HttpURLConnection connection = (HttpURLConnection) url.openConnection();                     
			connection.setDoOutput(true);
			connection.setRequestMethod("POST"); 
			connection.setRequestProperty("Content-Type", "application/json");

			OutputStream outputStream = connection.getOutputStream();
			outputStream.write(jsonObject.toString().getBytes());
			outputStream.flush();

			try {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));                                     
				String output = "";
				while((output = br.readLine()) != null) 
					response = response + output;                 
				outputStream.close();
				br.close();
			}
			catch (Exception e) {
				System.out.println("Error reading response: " + e.getMessage());
			}
			connection.disconnect();
		} catch (Exception e) {
			System.out.println(jsonObject.toString());
			e.printStackTrace();
		}
		return response;
	}

}

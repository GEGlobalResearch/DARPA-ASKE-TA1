/********************************************************************** 
 * Note: This license has also been called the "New BSD License" or 
 * "Modified BSD License". See also the 2-clause BSD License.
 *
 * Copyright © 2018-2019 - General Electric Company, All Rights Reserved
 * 
 * Project: ANSWER, developed with the support of the Defense Advanced 
 * Research Projects Agency (DARPA) under Agreement  No.  HR00111990006. 
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
package com.ge.research.sadl.darpa.aske.tests.kchain;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.ge.research.sadl.darpa.aske.processing.imports.JsonServiceInterface;
import com.ge.research.sadl.darpa.aske.processing.imports.KChainServiceInterface;
import com.ge.research.sadl.reasoner.utils.SadlUtils;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class KChainServiceTest {
	protected static final Logger logger = LoggerFactory.getLogger(KChainServiceTest.class);
	
	private String kchainServiceBaseURL;

	@Before
	public void setUp() throws Exception {
		String host = "vesuvius-dev.crd.ge.com";
		//		String host = "3.39.122.58";
		int port = 12345;
		kchainServiceBaseURL = "http://" + host + ":" + port;
	}

//	@Ignore
	@Test
	public void testBuildEval_01() throws IOException {
		/*
{
  "dataLocation": "../Datasets/Force_dataset.csv",
  "inputVariables": [
    {
      "name": "Mass",
      "type": "double",
     },
    {
      "name": "Acceleration",
      "type": "double",
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
		KChainServiceInterface kcsi = new KChainServiceInterface(kchainServiceBaseURL);
		Object[] bResults = kcsi.buildCGModel(modelUri, equationModel, dataLocation, inputs, outputs);

		/*
{
  "inputVariables": [
    {						// if missing use default if provided
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
		List<String[]> eInputs = new ArrayList<String[]>();
		String[] eInput1 = new String[3];
		eInput1[0] = "Mass";
		eInput1[1] = "double";
		eInput1[2] = "[1.0]";
		eInputs.add(eInput1);
		String[] eInput2 = new String[3];
		eInput2[0] = "Acceleration";
		eInput2[1] = "double";
		eInput2[2] = "[0.5]";
		eInputs.add(eInput2);
		List<String[]> eOutputs = new ArrayList<String[]>();
		String[] eOutput1 = new String[2];
		eOutput1[0] = "Force";
		eOutput1[1] = "double";
		eOutputs.add(eOutput1);
		// evaluate 
		List<List<String[]>> eResults = kcsi.evalCGModel(modelUri, eInputs, eOutputs);

	}

	//	@Ignore
	@Test
	public void testBuildEval_02() throws IOException {
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
		KChainServiceInterface kcsi = new KChainServiceInterface(kchainServiceBaseURL);
		Object[] bResults = kcsi.buildCGModel(modelUri, equationModel, dataLocation, inputs, outputs);

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
		List<String[]> eInputs = new ArrayList<String[]>();
		String[] eInput1 = new String[3];
		eInput1[0] = "Mass";
		eInput1[1] = "double";
		eInput1[2] = "[1.0]";
		eInputs.add(eInput1);
		String[] eInput2 = new String[3];
		eInput2[0] = "Acceleration";
		eInput2[1] = "double";
		eInput2[2] = "[0.5]";
		eInputs.add(eInput2);
		List<String[]> eOutputs = new ArrayList<String[]>();
		String[] eOutput1 = new String[2];
		eOutput1[0] = "Force";
		eOutput1[1] = "double";
		eOutputs.add(eOutput1);
		// evaluate 
		List<List<String[]>> eResults = kcsi.evalCGModel(modelUri, eInputs, eOutputs);
	}

//	public List<List<String[]>> evalCGModel(String modelUri, List<String[]> inputVariables, List<String[]> outputVariables) throws IOException {
//		//		String host = "3.39.122.224";
//		//		String host = "3.1.176.139";
//		String host = "vesuvius-dev.crd.ge.com";
//		//		String host = "3.39.122.58";
//		int port = 12345;
//		String kchainServiceURL = "http://" + host + ":" + port + "/darpa/aske/kchain/";
//		/*
//		 * 
//{
//  "inputVariables": [
//    {
//      "name": "string",
//      "type": "string",
//      "value": "string"
//    }
//  ],
//  "outputVariables": [
//    {
//      "name": "string",
//      "type": "string",
//      "value": "string"
//    }
//  ],
//  "modelName": "string"
//}
//		 */
//		JsonObject json = new JsonObject();
//		json.addProperty("modelName", modelUri);
//		JsonArray inputVars = new JsonArray();
//		json.add("inputVariables", inputVars);
//		Iterator<String[]> ivitr = inputVariables.iterator();
//		while (ivitr.hasNext()) {
//			String[] ivvals = ivitr.next();
//			JsonObject input = new JsonObject();
//			input.addProperty("name", ivvals[0]);
//			input.addProperty("type", ivvals[1]);
//			input.addProperty("value", ivvals[2]);
//			inputVars.add(input);
//		}
//
//		JsonArray jarrout = new JsonArray();
//		json.add("outputVariables", jarrout);
//		Iterator<String[]> ovitr = outputVariables.iterator();
//		while (ovitr.hasNext()) {
//			String[] ovvals = ovitr.next();
//			JsonObject output = new JsonObject();
//			output.addProperty("name", ovvals[0]);
//			output.addProperty("type", ovvals[1]);
//			jarrout.add(output);
//		}
//
//		String evalServiceURL = kchainServiceURL + "evaluate";
//		URL serviceUrl = new URL(evalServiceURL);			
//
//		String jsonResponse = makeConnectionAndGetResponse(serviceUrl, json);
//		logger.debug(jsonResponse);
//		JsonElement je = new JsonParser().parse(jsonResponse);
//		if (!je.isJsonObject()) {
//			throw new IOException("Unexpected response: " + je.toString());
//		}
//
//		JsonObject evalResults = je.getAsJsonObject();
//		/*
//{
//  "outputVariables": [
//    {
//      "name": "string",
//      "type": "string",
//      "value": "string"
//    }
//  ]
//}
//		 */
//		JsonArray ovars = evalResults.get("outputVariables").getAsJsonArray();
//		List<String[]> results = new ArrayList<String[]>();
//		for (JsonElement ovar : ovars) {
//			String[] aValue = new String[3];
//			aValue[0] = ovar.getAsJsonObject().get("name").getAsString();
//			aValue[1] = ovar.getAsJsonObject().get("type").getAsString();
//			aValue[2] = ovar.getAsJsonObject().get("value").getAsString();	
//			results.add(aValue);
//		}
//		JsonElement du = evalResults.get("defaultsUsed");
//		List<String[]> defaultValues = null;
//		if (du != null) {
//			JsonArray defsUsed = du.getAsJsonArray();
//			if (defsUsed != null) {
//				defaultValues = new ArrayList<String[]>();
//				for (JsonElement defobj : defsUsed) {
//					String[] aDefault = new String[2];
//					aDefault[0] = defobj.getAsJsonObject().get("name").getAsString();
//					aDefault[1] = defobj.getAsJsonObject().get("value").getAsString();
//					defaultValues.add(aDefault);
//				}
//			}
//		}
//		// return results: 
//		List<List<String[]>> retLists = new ArrayList<List<String[]>>();
//		retLists.add(results);
//		if (defaultValues != null) {
//			retLists.add(defaultValues);
//		}
//		return retLists;
//	}

	//	@Ignore
	@Test
	public void testBuildEval_03() throws IOException {
		List<String[]> inputs = new ArrayList<String[]>();
		String[] input1 = new String[3];
		input1[0] = "Mass";
		input1[1] = "double";
		input1[2] = "1.5";
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
		String modelUri = "Newtons2ndLawModelPB2";
		// add to KG: 
		KChainServiceInterface kcsi = new KChainServiceInterface(kchainServiceBaseURL);
		kcsi.buildCGModel(modelUri, equationModel, dataLocation, inputs, outputs);

		List<String[]> eInputs = new ArrayList<String[]>();
		//		String[] eInput1 = new String[3];
		//		eInput1[0] = "Mass";
		//		eInput1[1] = "double";
		//		eInput1[2] = "[1.0]";
		//		eInputs.add(eInput1);
		String[] eInput2 = new String[3];
		eInput2[0] = "Acceleration";
		eInput2[1] = "double";
		eInput2[2] = "[0.5]";
		eInputs.add(eInput2);
		List<String[]> eOutputs = new ArrayList<String[]>();
		String[] eOutput1 = new String[2];
		eOutput1[0] = "Force";
		eOutput1[1] = "double";
		eOutputs.add(eOutput1);
		//		String modelUri = "http://com.research.ge/darpa/aske/answer/test_02/binaryadd";
		//		String modelUri = "Newtons2ndLawModelPB";
		// add to KG: 
		kcsi.evalCGModel(modelUri, eInputs, eOutputs);
	}

	//	@Ignore
	@Test
	public void testBuildEval_04() throws IOException {
		String modelUri = "CAL_SOS";
		KChainServiceInterface kcsi = new KChainServiceInterface(kchainServiceBaseURL);
		{
			List<String[]> inputs = new ArrayList<String[]>();
			String[] input1 = new String[2];
			input1[0] = "T";
			input1[1] = "double";
			inputs.add(input1);
			String[] input2 = new String[2];
			input2[0] = "G";
			input2[1] = "double";
			inputs.add(input2);
			String[] input3 = new String[2];
			input3[0] = "R";
			input3[1] = "double";
			inputs.add(input3);
			String[] input4 = new String[2];
			input4[0] = "Q";
			input4[1] = "double";
			inputs.add(input4);
			List<String[]> outputs = new ArrayList<String[]>();
			String[] output1 = new String[2];
			output1[0] = "CAL_SOS";
			output1[1] = "double";
			outputs.add(output1);

			String dataLocation = null;
			String equationModel = "WOW = 1 + (G - 1) / (1 + (G - 1) * tf.math.pow((Q / T), 2) * tf.math.exp(Q / T) / tf.math.pow((tf.math.exp(Q / T) - 1), 2))\n    " + 
					"CAL_SOS = (tf.math.sqrt(32.174 * T * R * WOW))\n";
			// add to KG: 
			kcsi.buildCGModel(modelUri, equationModel, dataLocation, inputs, outputs);
		}
		//		List<String[]> eInputs = new ArrayList<String[]>();
		//		String[] eInput1 = new String[3];
		//		eInput1[0] = "Mass";
		//		eInput1[1] = "double";
		//		eInput1[2] = "[1.0]";
		//		eInputs.add(eInput1);
		//		String[] eInput2 = new String[3];
		//		eInput2[0] = "Acceleration";
		//		eInput2[1] = "double";
		//		eInput2[2] = "[0.5]";
		//		eInputs.add(eInput2);
		//		List<String[]> eOutputs = new ArrayList<String[]>();
		//		String[] eOutput1 = new String[2];
		//		eOutput1[0] = "Force";
		//		eOutput1[1] = "double";
		//		eOutputs.add(eOutput1);
		////		String modelUri = "http://com.research.ge/darpa/aske/answer/test_02/binaryadd";
		////		String modelUri = "Newtons2ndLawModelPB";
		//		// add to KG: 
		{
			List<String[]> inputs = new ArrayList<String[]>();
			String[] input1 = new String[3];
			input1[0] = "T";
			input1[1] = "double";
			input1[2] = "508.788";
			inputs.add(input1);
			String[] input2 = new String[3];
			input2[0] = "G";
			input2[1] = "double";
			input2[2] = "1.4";
			inputs.add(input2);
			String[] input3 = new String[3];
			input3[0] = "R";
			input3[1] = "double";
			input3[2] = "53.3";
			inputs.add(input3);
			String[] input4 = new String[3];
			input4[0] = "Q";
			input4[1] = "double";
			input4[2] = "5500";
			inputs.add(input4);
			List<String[]> outputs = new ArrayList<String[]>();
			String[] output1 = new String[2];
			output1[0] = "CAL_SOS";
			output1[1] = "double";
			outputs.add(output1);

			List<List<String[]>> results = kcsi.evalCGModel(modelUri, inputs, outputs);
			Iterator<List<String[]>> resultsItr = results.iterator();
			while (resultsItr.hasNext()) {
				List<String[]> result = resultsItr.next();
				Iterator<String[]> resultItr = result.iterator();

				while (resultItr.hasNext()) {
					String[] resultArr = resultItr.next();
					logger.debug(resultArr[1] + " " + resultArr[0] + " " + resultArr[2]);
				}
			}
		}
	}

	//	@Ignore
	@Test
	public void testBuildJsonGetResponse_02() throws IOException {
		/*
{
  "dataLocation": "../Datasets/Force_dataset.csv",
  "inputVariables": [
    {
      "name": "Mass",
      "type": "double",
     },
    {
      "name": "Acceleration",
      "type": "double",
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
		
	File jsonFile = new File("resources/sample2.json");
	assertTrue(jsonFile.exists());
	SadlUtils su = new SadlUtils();
	String json = su.fileToString(jsonFile);
//		JsonObject jsonObject = JSON.parse(json);
	JsonParser jp = new JsonParser();
	JsonElement je = jp.parse(json);
	JsonObject jsonCache = je.getAsJsonObject();
//		JsonElement elt = jsonCache.get(key);
//		JsonObject generatedObj = elt.getAsJsonObject();

	// add to KG: 
	try {
		KChainServiceInterface kcsi = new KChainServiceInterface(kchainServiceBaseURL);
		String bResults = kcsi.buildCGModel(jsonCache);
		System.out.println(bResults);
	}
	catch (Throwable t) {
		String msg = "Build failed with exception: " + JsonServiceInterface.aggregateExceptionMessage(t);
		fail(msg);
	}
		/*
{
  "inputVariables": [
    {						// if missing use default if provided
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
	}

	//	@Ignore
	@Test
	public void testBuildJsonGetResponse_01() throws IOException {
		/*
{
  "dataLocation": "../Datasets/Force_dataset.csv",
  "inputVariables": [
    {
      "name": "Mass",
      "type": "double",
     },
    {
      "name": "Acceleration",
      "type": "double",
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
		
	File jsonFile = new File("resources/sample.json");
	assertTrue(jsonFile.exists());
	SadlUtils su = new SadlUtils();
	String json = su.fileToString(jsonFile);
//		JsonObject jsonObject = JSON.parse(json);
	JsonParser jp = new JsonParser();
	JsonElement je = jp.parse(json);
	JsonObject jsonCache = je.getAsJsonObject();
//		JsonElement elt = jsonCache.get(key);
//		JsonObject generatedObj = elt.getAsJsonObject();

	// add to KG: 
	try {
		KChainServiceInterface kcsi = new KChainServiceInterface(kchainServiceBaseURL);
		String bResults = kcsi.buildCGModel(jsonCache);
		System.out.println(bResults);
	}
	catch (Throwable t) {
		String msg = "Build failed with exception: " + JsonServiceInterface.aggregateExceptionMessage(t);
		fail(msg);
	}
		/*
{
  "inputVariables": [
    {						// if missing use default if provided
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
	}

//	@Ignore
	@Test
	public void testBuildEval_Turbo_getGama() throws IOException {
		String modelUri = "Turbo_getGama";
		List<String[]> inputs = new ArrayList<String[]>();
		String[] input1 = new String[2];
		input1[0] = "temp";
		input1[1] = "float";
		inputs.add(input1);
		String[] input2 = new String[2];
		input2[0] = "opt";
		input2[1] = "integer";
		inputs.add(input2);
		List<String[]> outputs = new ArrayList<String[]>();
		String[] output1 = new String[2];
		output1[0] = "Turbo_getGama";
		output1[1] = "float";
		outputs.add(output1);

		String dataLocation = null;
		String equationModel = 
				"#  Utility to get gamma as a function of temp\n" + 
						"    a = -7.6942651e-13\n" + 
						"    b = 1.3764661e-08\n" + 
						"    c = -7.8185709e-05\n" + 
						"    d = 1.436914\n" + 
						"    if opt == 0:\n" + 
						"        number = 1.4\n" + 
						"    else:\n" + 
						"        number = a * temp * temp * temp + b * temp * temp + c * temp + d\n" +
						"    Turbo_getGama = number\n";
		// add to KG: 
		KChainServiceInterface kcsi = new KChainServiceInterface(kchainServiceBaseURL);
		try {
			Object[] buildResults = kcsi.buildCGModel(modelUri, equationModel, dataLocation, inputs, outputs);
		}
		catch (Exception e) {
			System.err.println(JsonServiceInterface.aggregateExceptionMessage(e));
		}
		
		List<String[]> evalInputs = new ArrayList<String[]>();
		String[] evalInput1 = new String[3];
		evalInput1[0] = "temp";
		evalInput1[1] = "float";
		evalInput1[2] = "508.788";
		evalInputs.add(evalInput1);
		String[] evalInput2 = new String[3];
		evalInput2[0] = "opt";
		evalInput2[1] = "integer";
		evalInput2[2] = "0";
		evalInputs.add(evalInput2);
		List<String[]> evalOutputs = new ArrayList<String[]>();
		String[] evalOutput1 = new String[2];
		evalOutput1[0] = "Turbo_getGama";
		evalOutput1[1] = "float";
		evalOutputs.add(evalOutput1);

		List<List<String[]>> evalResults = kcsi.evalCGModel(modelUri, evalInputs, evalOutputs);
		Iterator<List<String[]>> resultsItr = evalResults.iterator();
		while (resultsItr.hasNext()) {
			List<String[]> result = resultsItr.next();
			Iterator<String[]> resultItr = result.iterator();

			while (resultItr.hasNext()) {
				String[] resultArr = resultItr.next();
				logger.debug(resultArr[1] + " " + resultArr[0] + " " + resultArr[2]);
				System.out.println(resultArr[1] + " " + resultArr[0] + " " + resultArr[2]);
				assertEquals("Turbo_getGama", resultArr[0]);
				assertEquals("float", resultArr[1]);
				assertEquals("[1.4]", resultArr[2]);
			}
		}
	}

//	@Ignore
	@Test
	public void testBuildEval_Turbo_getGama2() throws IOException {
		String modelUri = "getGama";
		List<String[]> inputs = new ArrayList<String[]>();
		String[] input1 = new String[2];
		input1[0] = "temp";
		input1[1] = "double";
		inputs.add(input1);
		List<String[]> outputs = new ArrayList<String[]>();
		String[] output1 = new String[2];
		output1[0] = "getGama";
		output1[1] = "double";
		outputs.add(output1);

		String dataLocation = null;
		String equationModel = 
				"opt = 0\n" +
						"    a = -7.6942651e-13\n" + 
						"    b = 1.3764661e-08\n" + 
						"    c = -7.8185709e-05\n" + 
						"    d = 1.436914\n" + 
						"    if opt == 0:\n" + 
						"        number = 1.4\n" + 
						"    else:\n" + 
						"        number = a * temp * temp * temp + b * temp * temp + c * temp + d\n" +
						"    getGama = number\n";
		// add to KG: 
		String host = "vesuvius-dev.crd.ge.com";
		//		String host = "3.39.122.58";
		int port = 12345;
		String kchainServiceURL = "http://" + host + ":" + port;
		KChainServiceInterface kcsi = new KChainServiceInterface(kchainServiceURL);
		try {
			Object[] buildResults = kcsi.buildCGModel(modelUri, equationModel, dataLocation, inputs, outputs);
			
			fail();
			
			List<String[]> evalInputs = new ArrayList<String[]>();
			String[] evalInput1 = new String[3];
			evalInput1[0] = "temp";
			evalInput1[1] = "double";
			evalInput1[2] = "508.788";
			evalInputs.add(evalInput1);
			String[] evalInput2 = new String[3];
			evalInput2[0] = "opt";
			evalInput2[1] = "integer";
			evalInput2[2] = "0";
			evalInputs.add(evalInput2);
			List<String[]> evalOutputs = new ArrayList<String[]>();
			String[] evalOutput1 = new String[2];
			evalOutput1[0] = "Turbo_getGama";
			evalOutput1[1] = "double";
			evalOutputs.add(evalOutput1);
	
			List<List<String[]>> evalResults = kcsi.evalCGModel(modelUri, evalInputs, evalOutputs);
			Iterator<List<String[]>> resultsItr = evalResults.iterator();
			while (resultsItr.hasNext()) {
				List<String[]> result = resultsItr.next();
				Iterator<String[]> resultItr = result.iterator();
	
				while (resultItr.hasNext()) {
					String[] resultArr = resultItr.next();
					logger.debug(resultArr[1] + " " + resultArr[0] + " " + resultArr[2]);
					System.out.println(resultArr[1] + " " + resultArr[0] + " " + resultArr[2]);
					assertEquals("Turbo_getGama", resultArr[0]);
					assertEquals("double", resultArr[1]);
					assertEquals("[1.4]", resultArr[2]);
				}
			}
		}
		catch (IOException e) {
			assertEquals("Service call failed", e.getMessage());
		}
	}

	@Test
	public void testCAL_SOS() {
		System.out.println(CAL_SOS(592.0, 1.4, 53.3, 5500.0));
	}

	public double CAL_SOS (double T, double G, double R, double Q) {
		double WOW = 1 + (G - 1) / (1 + (G - 1) * Math.pow((Q / T), 2) *
				Math.exp(Q / T) / Math.pow((Math.exp(Q / T) - 1), 2));
		return (Math.sqrt(/*32.174 **/ T * R * WOW));
	}

	@Test
	public void testEquation_2() {
		System.out.println(equation2(286.0, 273.15, 1.4, 3056.0));
	}

	public double equation2 (double R, double T, double G, double Q) {
		//		a = tf.math.pow(R * T *  (  1 + ( gamma-1 ) / ( 1 + ( gamma-1 ) *  ( tf.math.pow( theta/T,2) *  tf.math.exp( theta/T ) /  tf.math.pow( tf.math.exp( theta/T ) - 1,2 ))  ) ), 1/2)
		double a = Math.pow(R * T *  (  1 + ( G-1 ) / ( 1 + ( G-1 ) *  ( Math.pow( Q/T,2) *  Math.exp( Q/T ) /  Math.pow( Math.exp( Q/T ) - 1,2 ))  ) ), .5);
		//		double a = Math.pow(R * T *  (  1.0 + ( G-1.0 ) / ( 1.0 + ( G-1.0 ) *  ( Math.pow( Q/T,2) *  Math.exp( Q/T ) /  Math.pow( Math.exp( Q/T ) - 1.0,2 ))  ) ), 1/2);
		return a;
	}

//	public boolean buildCGModel(String modelUri, String equationModel, String dataLocation, List<String[]> inputs, List<String[]> outputs) throws IOException {
//		/*
//	{
//	  "inputVariables": [
//	    {
//	      "name": "string",
//	      "type": "string",
//	      "value": "string"
//	    }
//	  ],
//	  "outputVariables": [
//	    {
//	      "name": "string",
//	      "type": "string",
//	      "value": "string"
//	    }
//	  ],
//	  "dataLocation": "string",
//	  "equationModel": "string",
//	  "modelName": "string"
//	}
//		 */
//		//		String host = "3.39.122.224";
//		//		String host = "3.1.176.139";
//		String host = "vesuvius-dev.crd.ge.com";
//		//		String host = "3.39.122.58";
//		int port = 12345;
//		String kchainServiceURL = "http://" + host + ":" + port + "/darpa/aske/kchain/";
//
//		JsonObject json = new JsonObject();
//		json.addProperty("modelName", modelUri);
//		if (equationModel != null) {
//			json.addProperty("equationModel", equationModel);
//		}
//		if (dataLocation != null) {
//			json.addProperty("dataLocation", dataLocation);
//		}
//		JsonArray jarrin = new JsonArray();
//		json.add("inputVariables", jarrin);
//		for (String[] input : inputs) {
//			JsonObject inputj = new JsonObject();
//			inputj.addProperty("name", input[0]);
//			inputj.addProperty("type", input[1]);
//			if (input.length > 2) {
//				inputj.addProperty("value", input[2]);
//			}
//			jarrin.add(inputj);
//		}
//		JsonArray jarrout = new JsonArray();
//		json.add("outputVariables", jarrout);
//		for (String[] output : outputs) {
//			JsonObject outputj = new JsonObject();
//			outputj.addProperty("name", output[0]);
//			outputj.addProperty("type", output[1]);
//			jarrout.add(outputj);
//		}
//
//		logger.debug(json.toString());
//
//		String buildServiceURL = kchainServiceURL + "build";
//		URL serviceUrl = new URL(buildServiceURL);			
//
//		String jsonResponse = makeConnectionAndGetResponse(serviceUrl, json);
//
//		logger.debug(jsonResponse);
//
//		/*
//{
//  "modelType": "string",
//  "trainedState": true,
//  "metagraphLocation": "string"
//}
//		 */
//		JsonElement je = new JsonParser().parse(jsonResponse);
//		if (je.isJsonObject()) {
//			JsonObject jobj = je.getAsJsonObject();
//			String modelType = jobj.get("modelType").getAsString();
//			String metagraphLocation = jobj.get("metagraphLocation").getAsString();
//			boolean trained = jobj.get("trainedState").getAsBoolean();	
//
//			Double[] dfd = null;
//			//		JsonElement dfelement = jobj.get("degreeFitness");
//			//		if (dfelement != null) {
//			//			JsonArray df = dfelement.getAsJsonArray();
//			//			dfd = new Double[df.size()];
//			//			int idx = 0;
//			//			for (JsonElement arrel : df) {
//			//				dfd[idx++] = arrel.getAsDouble();
//			//			}
//			//		}
//			//		 add to KG: 
//			return addCGModeltoExistingKGModel(modelUri, modelType, metagraphLocation, dfd);
//		}
//		else {
//			throw new IOException("Unexpected response: " + je.toString());
//		}
//	}

//	/**
//	 * Method to add CG information returned from call to build to the existing KG model.
//	 * @param modelUri
//	 * @param modelType
//	 * @param metagraphLocation
//	 * @param dfd
//	 * @return
//	 */
//	public boolean addCGModeltoExistingKGModel(String modelUri, String modelType, String metagraphLocation, Double[] dfd) {
//		// TODO Auto-generated method stub
//
//		return true;
//	}

//	public String addKGModel(String localname) {
//		// create KG model
//		String modelUri = null;
//		return modelUri;
//	}

//	private String makeConnectionAndGetResponse(URL url, JsonObject jsonObject) {
//		String response = "";
//		try {
//			HttpURLConnection connection = (HttpURLConnection) url.openConnection();                     
//			connection.setDoOutput(true);
//			connection.setRequestMethod("POST"); 
//			connection.setRequestProperty("Content-Type", "application/json");
//
//			OutputStream outputStream = connection.getOutputStream();
//			outputStream.write(jsonObject.toString().getBytes());
//			outputStream.flush();
//
//			try {
//				BufferedReader br = new BufferedReader(
//						new InputStreamReader(connection.getInputStream()));                                     
//				String output = "";
//				while((output = br.readLine()) != null) 
//					response = response + output;                 
//				outputStream.close();
//				br.close();
//			}
//			catch (Exception e) {
//				logger.debug("Error reading response: " + e.getMessage());
//			}
//			connection.disconnect();
//		} catch (Exception e) {
//			logger.debug(jsonObject.toString());
//			e.printStackTrace();
//		}
//		return response;
//	}

}

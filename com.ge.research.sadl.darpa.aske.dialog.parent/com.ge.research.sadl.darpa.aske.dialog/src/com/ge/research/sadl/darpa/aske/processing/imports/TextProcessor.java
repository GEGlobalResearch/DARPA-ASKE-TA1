package com.ge.research.sadl.darpa.aske.processing.imports;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.List;
import java.util.Map;

public class TextProcessor {

	private Map<String, String> preferences;
	
	private List<File> textFiles;

	public TextProcessor(Map<String, String> preferences) {
		this.setPreferences(preferences);
	}

	public String process(String inputIdentifier, String text, String locality) throws MalformedURLException, UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
		String baseServiceUrl = "http://vesuvius063.crd.ge.com:4200/v1/";
		
		String textToTripleServiceURL = baseServiceUrl + "text2triples";
		URL serviceUrl = new URL(textToTripleServiceURL);			
		if (text == null) {
			text = "Hello world";
		}
		if (locality == null) {
			locality = "NYI";
		}
		JsonObject json = new JsonObject();
		json.addProperty("locality", locality);
		json.addProperty("text", text);
	
		String response = makeConnectionAndGetResponse(serviceUrl, json);
		System.out.println(response);
		if (response != null && response.length() > 0) {
			JsonElement je = new JsonParser().parse(response);
			JsonArray allSentences = je.getAsJsonArray();
			if (allSentences != null) {
				for (JsonElement sent : allSentences) {
					JsonObject jobj = sent.getAsJsonObject();
					JsonElement dfelement = jobj.get("triples");
					if (dfelement != null) {
						JsonArray df = dfelement.getAsJsonArray();
						double confScore;
						String subject;
						String predicate;
						String object;
						int idx = 0;
						for (JsonElement arrel : df) {
							JsonObject elobj = arrel.getAsJsonObject();
							confScore = elobj.get("confScore").getAsDouble();
							subject = elobj.get("subject").getAsString();
							predicate = elobj.get("predicate").getAsString();
							object = elobj.get("object").getAsString();
							String msg = "Returned Triple: " + subject + " " + predicate + " " + object;
							System.out.println(msg);
							sb.append(msg);
						}
					}
				}
			}
		}
		else {
			System.err.println("No response received from service " + textToTripleServiceURL);
		}
		return sb.toString();
	}
	
	public void testKChain() throws MalformedURLException, UnsupportedEncodingException {

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

			BufferedReader br = new BufferedReader(
					new InputStreamReader(connection.getInputStream()));                                     
			String output = "";
			while((output = br.readLine()) != null) 
				response = response + output;                 
			outputStream.close();
			br.close();
			connection.disconnect();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return response;
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
			textFiles.addAll(txtFiles);
		}
		else {
			setTextFiles(txtFiles);
		}
	}

	public List<File> getTextFiles() {
		return textFiles;
	}

	public void setTextFiles(List<File> textFiles) {
		this.textFiles = textFiles;
	}

}

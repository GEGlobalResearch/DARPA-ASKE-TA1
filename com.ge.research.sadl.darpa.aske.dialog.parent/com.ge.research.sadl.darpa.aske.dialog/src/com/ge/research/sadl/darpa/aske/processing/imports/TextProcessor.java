package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class TextProcessor {

	private Map<String, String> preferences;
	
	private List<File> textFiles;

	public TextProcessor(Map<String, String> preferences) {
		this.setPreferences(preferences);
	}

	public String process(String inputIdentifier, String text, String locality) throws MalformedURLException, UnsupportedEncodingException {
		StringBuilder sb = new StringBuilder();
//		String baseServiceUrl = "http://vesuvius-dev.crd.ge.com:4200/darpa/aske/";		// dev environment for stable development of other components
		String baseServiceUrl = "http://vesuvius-dev.crd.ge.com:4200/darpa/aske/";		// test environment for service development
		
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
//		System.out.println(response);
		if (response != null && response.length() > 0) {
//			JsonElement je = new JsonParser().parse(response);
//			JsonArray allSentences = je.getAsJsonArray();
//			if (allSentences != null) {
//				for (JsonElement sent : allSentences) {
//					JsonObject jobj = sent.getAsJsonObject();
//					JsonElement dfelement = jobj.get("triples");
//					if (dfelement != null) {
//						JsonArray df = dfelement.getAsJsonArray();
//						double confScore;
//						String subject;
//						String predicate;
//						String object;
//						int idx = 0;
//						for (JsonElement arrel : df) {
//							JsonObject elobj = arrel.getAsJsonObject();
//							confScore = elobj.get("confScore").getAsDouble();
//							subject = elobj.get("subject").getAsString();
//							predicate = elobj.get("predicate").getAsString();
//							object = elobj.get("object").getAsString();
//							String msg = "Returned Triple: " + subject + " " + predicate + " " + object;
//							System.out.println(msg);
//							sb.append(msg);
//						}
//					}
//				}
//			}
			JsonArray sentences = new JsonParser().parse(response).getAsJsonArray();
			if (sentences != null) {
				for (JsonElement element : sentences) {
					if (element != null) {
						JsonObject sentence = element.getAsJsonObject();
						String originalText = sentence.get("text").getAsString();
						System.out.println("Extracted from text:");
						JsonArray concepts = sentence.get("concepts").getAsJsonArray();
						for (JsonElement concept : concepts) {
							String matchingText = concept.getAsJsonObject().get("string").getAsString();
							int startInOrigText = concept.getAsJsonObject().get("start").getAsInt();
							int endInOrigText = concept.getAsJsonObject().get("end").getAsInt();
							double extractionConfidence = concept.getAsJsonObject().get("extractionConfScore").getAsDouble();
							System.out.println("  Match in substring '" + matchingText + "(" + startInOrigText + "," + endInOrigText + "):");
							JsonArray triples = concept.getAsJsonObject().get("triples").getAsJsonArray();
							for (JsonElement triple : triples) {
								String subject = triple.getAsJsonObject().get("subject").getAsString();
								String predicate = triple.getAsJsonObject().get("predicate").getAsString();
								String object = triple.getAsJsonObject().get("object").getAsString();
								double tripleConfidenceScore = triple.getAsJsonObject().get("tripleConfScore").getAsDouble();
								System.out.println("     <" + subject + ", " + predicate + ", " + object + "> (" + tripleConfidenceScore + ")");
							}
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

	public void addFile(File txtFile) {
		if (textFiles != null) {
			textFiles = new ArrayList<File>();
		}
		textFiles.add(txtFile);
	}

	public List<File> getTextFiles() {
		return textFiles;
	}

	public void setTextFiles(List<File> textFiles) {
		this.textFiles = textFiles;
	}

}

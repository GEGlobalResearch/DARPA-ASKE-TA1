package com.research.ge.darpa.aske.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

public class NLPServices {
		
	public static String nlpServiceURL = "http://vesuvius-test.crd.ge.com:9080/kcud/";
	//public static String nlpServiceURL = "http://localhost:9080/kcud/";
	
	public List<String> textToSentences(String text) throws Exception {
		List<String> sentences = new ArrayList<String>();
		
		String urlStr = nlpServiceURL + "breakTextIntoSentences?text=" + URLEncoder.encode(text, "UTF-8");
		
		URL url = new URL(urlStr);			
		HttpURLConnection connection = (HttpURLConnection) url.openConnection();
		
		connection.setRequestMethod("GET");
		
		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));			
		String response = "", jsonResponse = "";
				
		while((response = br.readLine()) != null){
			jsonResponse = jsonResponse + response;
		}
		
		JsonElement jsonElement = new JsonParser().parse(jsonResponse);
		JsonArray jsonArray = jsonElement.getAsJsonArray();
		
		for(JsonElement elem : jsonArray) {
			sentences.add(elem.getAsString());
		}
		
		return sentences;
	}
}

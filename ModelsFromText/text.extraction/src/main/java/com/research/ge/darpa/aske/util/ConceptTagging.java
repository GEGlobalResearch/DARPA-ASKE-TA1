package com.research.ge.darpa.aske.util;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

public class ConceptTagging {
	
	private String nlpServiceURL = "http://vesuvius-test.crd.ge.com:9080/kcud/";
	private static Dictionary dictionary;
	
	public ConceptTagging(String ontologyFile) throws IOException {
		dictionary = new Dictionary();
		List<String> owlLineList = new ArrayList<String>();		
		BufferedReader b = new BufferedReader(new FileReader(ontologyFile));
		String l = "";
		while ((l = b.readLine()) != null)
			owlLineList.add(l);
		b.close();
		dictionary.setOwlLineList(owlLineList);
	}
	
	public List<String> runConceptTagging(String text) throws Exception {
		String nlpConceptTaggingServiceURL = nlpServiceURL + "/conceptTaggingJSON";
		
		URL connectionURL = new URL(nlpConceptTaggingServiceURL);
		HttpURLConnection connection = (HttpURLConnection) connectionURL.openConnection();
		connection.setDoOutput(true);
		connection.setRequestMethod("POST");
		connection.setRequestProperty("Content-Type", "application/json");
		
		Gson gson = new Gson();
		InputInfo inputInfo = new InputInfo();
		inputInfo.addDictObj(dictionary);
		inputInfo.addParagraph(text);		
		inputInfo.setTaxonomyDatasetURL(null);
		
		String input = gson.toJson(inputInfo, InputInfo.class);
			
		OutputStream outputStream = connection.getOutputStream();
		outputStream.write(input.getBytes());
		outputStream.flush();
		BufferedReader br = new BufferedReader(new InputStreamReader(connection.getInputStream()));			
		String responseJSonString = "";
		String line = "";
		while ((line = br.readLine()) != null)
			responseJSonString += line;
		
	
		Type listType = new TypeToken<List<OutputInfo>>() {}.getType();
		List<OutputInfo> outputInfoList = new Gson().fromJson(responseJSonString, listType);		
		return conceptTaggingHelper(outputInfoList);
	}
	
	private List<String> conceptTaggingHelper(List<OutputInfo> outputInfoList) {
		List<String> taggedConcepts = new ArrayList<String>();
		if(outputInfoList != null){
			for(OutputInfo outInfo : outputInfoList) {
				List<Concept> concepts = outInfo.getConceptInfoList();
				for(Concept concept : concepts) {
					String conceptMapperTag = concept.getName().trim();
					if(conceptMapperTag != null)
						taggedConcepts.add(conceptMapperTag);
				}
			}
		}
		return taggedConcepts;
	}

}

package com.ge.research.sadl.darpa.aske.tests.kchain;

import static org.junit.Assert.*;

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
		String serviceIP = "3.39.120.21";
		String kchainServiceURL = "http://" + serviceIP + ":8080/kchain/";
		
		String requestString = "{\r\n" + 
				"[\"a\", \"b\"],\r\n" + 
				"[\"c\"],\r\n" + 
				"\"http://ge.com/data\",\r\n" + 
				"\"c = a + b\",\r\n" + 
				"\"http://com.research.ge/darpa/aske/answer/test_02/binaryadd\"\r\n" + 
				"}"; 
		
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

}

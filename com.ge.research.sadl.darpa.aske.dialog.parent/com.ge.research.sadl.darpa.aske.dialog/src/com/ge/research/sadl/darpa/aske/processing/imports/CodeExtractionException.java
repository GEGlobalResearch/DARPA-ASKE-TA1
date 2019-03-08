package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class CodeExtractionException extends Exception {
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public CodeExtractionException(String msg) {
		super(msg);
	}
	
}

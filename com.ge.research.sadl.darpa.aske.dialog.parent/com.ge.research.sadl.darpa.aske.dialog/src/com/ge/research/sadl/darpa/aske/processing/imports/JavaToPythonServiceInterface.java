package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.IOException;
import java.net.ConnectException;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import org.eclipse.emf.ecore.xml.type.internal.DataValue.URI;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class JavaToPythonServiceInterface extends JsonServiceInterface {
    private static final String DARPA_ASKE_J2P_SERVICE_URL_FRAGMENT = "/darpa/aske/";

	private String j2PServiceURL = null;

	public JavaToPythonServiceInterface(String serviceBaseUri) {
		setJ2PServiceURL(serviceBaseUri);
	}
	
	public String translateMethodJavaToPython(String className, String methodCode) throws IOException {
		String translateMethodServiceURL = getJ2PServiceUrl() + "translate/method/";
		URL serviceUrl = new URL(translateMethodServiceURL);			

		JsonObject json = new JsonObject();
		json.addProperty("className", className);
		String encodedMethodCode = URI.encode(methodCode);
		json.addProperty("methodCode", encodedMethodCode);
		
		logger.debug(json.toString());
	
		try {
			String response = makeConnectionAndGetResponse(serviceUrl, json);
			String pythonCode = null;
			logger.debug(response);
			if (response != null && response.length() > 0) {
				JsonElement je = new JsonParser().parse(response);
				if (je.isJsonObject()) {
					JsonObject jobj = je.getAsJsonObject();
					JsonElement status = jobj.get("status");
					logger.debug("Status: " + status.getAsString());
					if (!status.getAsString().equalsIgnoreCase("SUCCESS")) {
						throw new IOException("Method translation failed: " + status.getAsString());
					}
					pythonCode = jobj.get("code").getAsString();
					logger.debug(pythonCode);
	
				}
				else if (je instanceof JsonPrimitive) {
					String status = ((JsonPrimitive)je).getAsString();
					logger.debug(status);
				}
				return pythonCode;
			}
			else {
				throw new IOException("No response received from service " + translateMethodServiceURL);
			}
		}
		catch (IOException e) {
			if (e.getCause() instanceof ConnectException) {
				throw new IOException(e.getMessage() + "(URL=" + translateMethodServiceURL + ")", e.getCause());
			}
			else {
				throw e;
			}
		}
	}
	
	public String translateExpressionJavaToPython(String className, String methodName, String exprCode) throws IOException {
		String translateExpressionServiceURL = getJ2PServiceUrl() + "translate/expression/";
		URL serviceUrl = new URL(translateExpressionServiceURL);			

		JsonObject json = new JsonObject();
		json.addProperty("className", className);
		json.addProperty("methodName", methodName);
		json.addProperty("exprCode", exprCode);
		
		logger.debug(json.toString());
	
		try {
			String response = makeConnectionAndGetResponse(serviceUrl, json);
			String pythonCode = null;
			logger.debug(response);
			if (response != null && response.length() > 0) {
				JsonElement je = new JsonParser().parse(response);
				if (je.isJsonObject()) {
					JsonObject jobj = je.getAsJsonObject();
					JsonElement status = jobj.get("status");
					logger.debug("Status: " + status.getAsString());
					if (!status.getAsString().equalsIgnoreCase("SUCCESS")) {
						throw new IOException("Method translation failed: " + status.getAsString());
					}
					pythonCode = jobj.get("code").getAsString();
					logger.debug(pythonCode);
	
				}
				else if (je instanceof JsonPrimitive) {
					String status = ((JsonPrimitive)je).getAsString();
					logger.debug(status);
				}
				return pythonCode;
			}
			else {
				throw new IOException("No response received from service " + translateExpressionServiceURL);
			}
		}
		catch (IOException e) {
			if (e.getCause() instanceof ConnectException) {
				throw new IOException(e.getMessage() + "(URL=" + translateExpressionServiceURL + ")", e.getCause());
			}
			else {
				throw e;
			}
		}
	}
	
	private String getJ2PServiceUrl() {
		return j2PServiceURL;
	}

	private void setJ2PServiceURL(String baseUrl) {
		String host = "vesuvius-dev.crd.ge.com";	// default
		int port = 19092;			// default
		if (baseUrl != null) {
			j2PServiceURL = baseUrl + DARPA_ASKE_J2P_SERVICE_URL_FRAGMENT;
		}
		else {
			j2PServiceURL = "http://" + host + ":" + port + DARPA_ASKE_J2P_SERVICE_URL_FRAGMENT;
		}
	}

}

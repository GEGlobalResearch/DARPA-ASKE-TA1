package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.IOException;
import java.net.URL;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonPrimitive;

public class JavaToPythonServiceInterface extends JsonServiceInterface {
    private static final String DARPA_ASKE_J2P_SERVICE_URL_FRAGMENT = "/darpa/aske/";

	private String j2PServiceURL = null;

	public String translateMethodJavaToPython(String className, String methodCode) throws IOException {
		String translateMethodServiceURL = getJ2PServiceUrl() + "translate/method/";
		URL serviceUrl = new URL(translateMethodServiceURL);			

		JsonObject json = new JsonObject();
		json.addProperty("className", className);
		json.addProperty("methodCode", methodCode);
		
		logger.debug(json.toString());
	
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
		}
		else {
			throw new IOException("No response received from service " + translateMethodServiceURL);
		}
		return pythonCode;
	}
	
	public JavaToPythonServiceInterface(String serviceBaseUri) {
		setJ2PServiceURL(serviceBaseUri);
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

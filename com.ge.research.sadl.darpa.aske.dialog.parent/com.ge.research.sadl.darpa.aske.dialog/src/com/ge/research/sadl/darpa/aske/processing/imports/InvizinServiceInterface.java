package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

/**
 * 
 * Class to provide interface to the KChain service, sending inputs and receiving outputs as serialized JSON
 * @author 200005201
 *
 */
public class InvizinServiceInterface extends JsonServiceInterface {
    private static final String DARPA_ASKE_INVIZIN_SERVICE_URL_FRAGMENT = "/darpa/aske/invizin/";

	private String invizinServiceURL = null;

	public InvizinServiceInterface(String serviceBaseUri) {
		setInvizinServiceURL(serviceBaseUri);
	}

	/**
	 * Method to visualize KChain computational graph sensitivity analysis
	 * @param json--visualization input
	 * @return--the URL to the visualization
	 * @throws IOException
	 */
	public String visualize(JsonObject json) throws IOException {

		String jsonResponse = visualize2(json);

//		JsonElement je = new JsonParser().parse(jsonResponse);
//		if (je.isJsonObject()) {
//			JsonObject jobj = je.getAsJsonObject();
//			String visualizationUrl = jobj.get("url").getAsString();
//			return visualizationUrl;
//		}
//		else {
//			throw new IOException("Unexpected response: " + je.toString());
//		}

	return jsonResponse;
	}

	public String visualize2(JsonObject json) throws MalformedURLException, IOException {
		logger.debug(json.toString());

		String buildServiceURL = getInvizinServiceURL() + "visualize";
		URL serviceUrl = new URL(buildServiceURL);			

		String jsonResponse = makeConnectionAndGetResponse(serviceUrl, json);

		logger.debug(jsonResponse);
		return jsonResponse;
	}

	private String getInvizinServiceURL() {
		return invizinServiceURL;
	}
	
	

	private void setInvizinServiceURL(String baseUrl) {
		String host = "vesuvius-dev.crd.ge.com";	// default
		int port = 12345;			// default
		if (baseUrl != null) {
			invizinServiceURL = baseUrl + DARPA_ASKE_INVIZIN_SERVICE_URL_FRAGMENT;
		}
		else {
			invizinServiceURL = "http://" + host + ":" + port + DARPA_ASKE_INVIZIN_SERVICE_URL_FRAGMENT;
		}
	}

}

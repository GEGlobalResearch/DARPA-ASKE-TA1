package com.ge.research.sadl.darpa.aske.processing.imports;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

import org.apache.log4j.Logger;

import com.google.gson.JsonObject;

public class JsonServiceInterface {
	static final Logger logger = Logger.getLogger (JsonServiceInterface.class) ;
    

	public JsonServiceInterface() {
		super();
	}

	/**
	 * Method to open a connection to the KChain service at the input URL with the given JSON object specfying inputs and 
	 * get a response
	 * @param url--the URL of the service
	 * @param jsonObject--the inputs to the service
	 * @throws IOException 
	 * @return--the response as a serialized JSON object
	 */
	protected String makeConnectionAndGetResponse(URL url, JsonObject jsonObject) throws IOException {
		String response = "";
		HttpURLConnection connection = null;
		OutputStream outputStream = null;
		try {
			connection = (HttpURLConnection) url.openConnection();
			connection.setDoOutput(true);
			connection.setRequestMethod("POST"); 
			connection.setRequestProperty("Content-Type", "application/json");

			outputStream = connection.getOutputStream();
			outputStream.write(jsonObject.toString().getBytes());
			outputStream.flush();
		} catch (IOException e1) {
			logger.debug("Error opening connection: " + e1.getMessage());
			throw new IOException("Error opening connection", e1);
		}  
		finally {
			if (outputStream != null) {
				outputStream.close();
			}
		}

		if (connection != null) {
			try {
				BufferedReader br = new BufferedReader(
						new InputStreamReader(connection.getInputStream()));                                     
				String output = "";
				while((output = br.readLine()) != null) 
					response = response + output;                 
				//				outputStream.close();
				br.close();
			}
			catch (Exception e) {
				logger.debug("Error reading response: " + e.getMessage());
				throw new IOException("Service call failed", e);
			}
			finally {
				connection.disconnect();
			}
		}
		return response;
	}

	/**
	 * Method to rollup nested messages into a single message for display
	 * @param t
	 * @return
	 */
	public static String aggregateExceptionMessage(Throwable t) {
		String msg = t.getMessage();
		if (t.getCause() != null && !t.getCause().equals(t)) {
			msg += " " + aggregateExceptionMessage(t.getCause());
		}
		return msg;
		
	}
}
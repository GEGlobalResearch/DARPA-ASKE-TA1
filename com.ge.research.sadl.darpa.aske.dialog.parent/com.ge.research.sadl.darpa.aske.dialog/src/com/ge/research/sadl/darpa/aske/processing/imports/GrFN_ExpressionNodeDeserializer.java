package com.ge.research.sadl.darpa.aske.processing.imports;

import java.lang.reflect.Type;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;

public class GrFN_ExpressionNodeDeserializer implements JsonDeserializer<GrFN_ExpressionNode> {

	@Override
	public GrFN_ExpressionNode deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context)
			throws JsonParseException {

		String type = null;
		double dvalue;
		String svalue;
		
		JsonObject jsonObject = json.getAsJsonObject();

        JsonElement jvalue = jsonObject.get("value");
        try {
        	dvalue = jvalue.getAsDouble();
        	type = "double";
//        	return
        } catch (Exception e) {
			// TODO: handle exception
		}
        
        try {
            svalue = jvalue.getAsString();
            type = "string";
        }catch (Exception e) {
			// TODO: handle exception
		}
		
		return null;
	}

}

package com.perfectomobile.jenkins.utils;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class JsonParsingUtils {

	private static JSONParser parser = new JSONParser();
	
	public String getElement(String jsonStringObject, String element) throws ParseException{
		
		String resultElement = null;
		
		Object obj = parser.parse(jsonStringObject);
		
		JSONObject jsonObject = (JSONObject) obj;
		 
		resultElement = (String) jsonObject.get(element);
		
		return resultElement;
	}
	
	public JSONArray getElements(String jsonStringObject, String element) throws ParseException{
		
		Object obj = parser.parse(jsonStringObject);
		
		JSONObject jsonObject = (JSONObject) obj;
		
		// loop array
		JSONArray resultElements = (JSONArray) jsonObject.get(element);
		
		return resultElements;
	}

}

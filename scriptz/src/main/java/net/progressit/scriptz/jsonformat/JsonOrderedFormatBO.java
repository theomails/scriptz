package net.progressit.scriptz.jsonformat;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class JsonOrderedFormatBO {
	@SuppressWarnings("unchecked")
	public String orderAndFormatJson(String inputJson, boolean prettyPrint, boolean serializeNulls) {
		inputJson = inputJson.trim();
		
		GsonBuilder gb = new GsonBuilder();
		if(prettyPrint)  gb.setPrettyPrinting();
		if(serializeNulls)  gb.serializeNulls();
		Gson g = gb.create();
		
		if(inputJson.startsWith("{")) {
			Map<String,Object> jsonData = new Gson().fromJson(inputJson, Map.class);
			Map<String,Object> outOrderedData = getOrderedCopy(jsonData);
			return g.toJson(outOrderedData);
		}else if(inputJson.startsWith("[")) {
			List<Object> jsonData = new Gson().fromJson(inputJson, List.class);
			List<Object> outOrderedData = getWithChildContentsOrdered(jsonData);
			return g.toJson(outOrderedData);
		}else {
			throw new RuntimeException("Unknown start character " + inputJson.substring(0, Math.min(1, inputJson.length())));
		}
	}

	@SuppressWarnings({ "unchecked" })
	private Map<String, Object> getOrderedCopy(Map<String, Object> jsonData) {
		//Res container
		Map<String,Object> outOrderedData = new LinkedHashMap<String, Object>();
		
		//Sort keys
		Set<String> keys = jsonData.keySet();
		List<String> orderedKeys = new ArrayList<>(keys.size());
		orderedKeys.addAll(keys);
		Collections.sort(orderedKeys);
		
		for(String key:orderedKeys) {
			System.out.println(key);
			Object val = jsonData.get(key);
			if(val instanceof Map) {
				Map<String,Object> valMap = (Map<String,Object>) val;
				Map<String,Object> outOrderedVal = getOrderedCopy(valMap);
				outOrderedData.put(key, outOrderedVal); //Copy the ordered map.
			} else if (val instanceof List) {
				List<Object> valList = (List<Object>) val;
				List<Object> outOrderedVal = getWithChildContentsOrdered(valList);
				outOrderedData.put(key, outOrderedVal); //Copy the children-ordered list.
			} else {
				outOrderedData.put(key, val); //Copy the same item.
			}
			System.out.println("End: " + key);
		}
		return outOrderedData;
	}
	
	@SuppressWarnings({ "unchecked" })
	private List<Object> getWithChildContentsOrdered(List<Object> jsonArray){
		//Res container
		List<Object> outOrderedData = new ArrayList<Object>(jsonArray.size());
				
		//Sort children
		for(int i=0;i<jsonArray.size();i++) {
			Object valInner = jsonArray.get(i);
			
			if(valInner instanceof Map) {
				Map<String,Object> valMap = (Map<String,Object>) valInner;
				Map<String,Object> outOrderedVal = getOrderedCopy(valMap);
				outOrderedData.add(outOrderedVal); //Replace this map in the list with ordered one.
			}else if(valInner instanceof List){
				List<Object> valList = (List<Object>) valInner;
				List<Object> outOrderedVal = getWithChildContentsOrdered(valList);
				outOrderedData.add(outOrderedVal); //Replace this map in the list with ordered one.
			}else {
				outOrderedData.add(valInner);
			}
		}	
		
		return outOrderedData;
	}
}

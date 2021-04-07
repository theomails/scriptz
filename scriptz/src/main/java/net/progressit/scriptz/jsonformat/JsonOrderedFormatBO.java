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
		Map<String,Object> jsonData = new Gson().fromJson(inputJson, Map.class);
		Map<String,Object> outOrderedData = getOrdered(jsonData);
		GsonBuilder gb = new GsonBuilder();
		if(prettyPrint)  gb.setPrettyPrinting();
		if(serializeNulls)  gb.serializeNulls();
		Gson g = gb.create();
		return g.toJson(outOrderedData);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private Map<String, Object> getOrdered(Map<String, Object> jsonData) {
		Map<String,Object> outOrderedData = new LinkedHashMap<String, Object>();
		Set<String> keys = jsonData.keySet();
		List<String> orderedKeys = new ArrayList<>(keys.size());
		orderedKeys.addAll(keys);
		Collections.sort(orderedKeys);
		for(String key:orderedKeys) {
			System.out.println(key);
			Object val = jsonData.get(key);
			if(val instanceof Map) {
				Map<String,Object> valMap = (Map<String,Object>) val;
				Map<String,Object> outOrderedVal = getOrdered(valMap);
				outOrderedData.put(key, outOrderedVal); //Copy the ordered map.
			} else if (val instanceof List) {
				outOrderedData.put(key, val); //Copy the same list.
				List valList = (List) val;
				for(int i=0;i<valList.size();i++) {
					Object valInner = valList.get(0);
					if(valInner instanceof Map) {
						Map<String,Object> valMap = (Map<String,Object>) valInner;
						Map<String,Object> outOrderedVal = getOrdered(valMap);
						valList.set(i, outOrderedVal); //Replace this map in the list with ordered one.
					}
				}
			} else {
				outOrderedData.put(key, val); //Copy the same item.
			}
			System.out.println("End: " + key);
		}
		return outOrderedData;
	}
}

package models;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class EventObject {

	public String appId;
	public String channelName;
	public Map<String, List<String>> filters;
	public String message;

	public EventObject(String appId, String channelName, String filters, String message) {
		this(appId, channelName, ingestFilters(filters), message);
	}

	public EventObject(String appId, String channelName, Map<String, List<String>> filters, String message) {
		this.appId = appId;
		this.channelName = channelName;
		this.filters = filters;
		this.message = message;
	}

	public static Map<String, List<String>> ingestFilters(String strFilters) {
		Map<String, List<String>> filters = new HashMap<String, List<String>>();
		if (strFilters == null) {
			return filters;
		}
		JsonObject obj = new JsonParser().parse(strFilters).getAsJsonObject();
		for (Map.Entry<String, JsonElement> elem : obj.entrySet()) {
			List<String> filterList = new ArrayList<String>();
			for (JsonElement filterElem : elem.getValue().getAsJsonArray()) {
				filterList.add(filterElem.getAsString());
			}
			filters.put(elem.getKey(), filterList);
		}
		return filters;
	}

	public boolean checkChannel(String appId, String channelName) {
		return this.appId.equals(appId) && this.channelName.equals(channelName);
	}

	public boolean checkFilters(Map<String, List<String>> listenerFilters) {
		if (filters == null || filters.isEmpty()) {
			return true;
		}

		if (listenerFilters == null || listenerFilters.isEmpty()) {
			return false;
		}

		List<Boolean> res = new ArrayList<Boolean>();
		for (Map.Entry<String, List<String>> filter : filters.entrySet()) {
			List<String> listenerFilterList = listenerFilters.get(filter.getKey());
			List<String> messageFilterList = filter.getValue();
			if (messageFilterList != null && !messageFilterList.isEmpty()) {
				if (listenerFilterList == null || listenerFilterList.isEmpty()) {
					return false;
				}
				boolean ok = false;
				for (String messageFilterElement : messageFilterList) {
					if (listenerFilterList.contains(messageFilterElement)) {
						ok = true;
					}
				}
				res.add(ok);
			}
		}

		return !res.isEmpty() && !res.contains(false);
	}

	public boolean check(String appId, String channelName, String filters) {
		return checkChannel(appId, channelName) && checkFilters(ingestFilters(filters));
	}

	@Override
	public String toString() {
		return toString(true);
	}

	public String toString(boolean withMessage) {
		return "EventObject(" + appId + "/" + channelName + "," + filters + (withMessage ? "," + message : "") + ")";
	}

}

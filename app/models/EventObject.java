package models;

import play.Logger;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class EventObject {

	public String appId;
	public String channelName;
	public Map<String, List<String>> filters;
	public String message;

	public EventObject(String appId, String channelName, Map<String, List<String>> filters, String message) {
		this.appId = appId;
		this.channelName = channelName;
		this.filters = filters;
		this.message = message;
	}

	public boolean checkChannel(String appId, String channelName) {
		return this.appId.equals(appId) && this.channelName.equals(channelName);
	}

	public boolean matches(Map<String, List<String>> listenerFilters) {
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

}

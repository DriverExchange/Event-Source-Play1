package controllers;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import models.EventManager;
import models.EventObject;
import org.jcoffeescript.JCoffeeScriptCompileException;
import play.Logger;
import play.Play;
import play.libs.Codec;
import play.libs.F.Either;
import play.libs.F.Promise;
import play.libs.F.Timeout;
import play.modules.coffee.CoffeePlugin;
import play.mvc.Controller;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Events extends Controller {

	public static void dxesjs() throws JCoffeeScriptCompileException {
		File coffeeFile = Play.getFile("public/javascripts/dxes.coffee");
		if (!coffeeFile.exists()) {
			notFound();
		}
		if (!Play.mode.isDev()) {
			response.cacheFor("1h");
		}
		response.contentType = "text/javascript";
		renderText(CoffeePlugin.compileCoffee(coffeeFile));
	}

	protected static Map<String, List<String>> ingestFilters(String strFilters) {
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

	protected static boolean checkSignature(String appId, String strFilters, String signature) {
		String appSecret = Play.configuration.getProperty("dxes." + appId + ".appSecret");
		if (appSecret == null) {
			Logger.warn("No appSecret defined for appId: %s", appId);
			return false;
		}
		return Codec.hexMD5(strFilters + appSecret).equals(signature);
	}

	protected static void apiAuth(String appId) {
		String appAuthToken = Play.configuration.getProperty("dxes." + appId + ".appAuthToken");
		if (appAuthToken == null) {
			Logger.warn("No appAuthToken defined for appId: %s", appId);
			forbidden();
		}
		String key = "Basic " + Codec.encodeBASE64(appId + ":" + appAuthToken);
		if(!request.headers.get("authorization").values.get(0).equals(key)) {
			unauthorized();
		}
	}

	public static void publish(String appId, String channelName, String filters, String message) {
		apiAuth(appId);
		if (message == null) {
			badRequest();
		}
		EventObject event = new EventObject(appId, channelName, ingestFilters(filters), message);
		EventManager.instance.event.publish(event);
	}

	public static void subscribeJsonp(String appId, String channelName, String filters, String signature, String callback) throws InterruptedException {
		response.contentType = "text/javascript";

		Either<EventObject, Timeout> eventObjectOrTimeout = null;

		if (!checkSignature(appId, filters, signature)) {
			response.status = 400;
			Logger.warn("The filters do not match the signature (appId: %s, channelName: %s)", appId, channelName);
			renderText("The filters do not match the signature");
		}

		Map<String, List<String>> listenerFilters = ingestFilters(filters);

		while (eventObjectOrTimeout == null
			|| !eventObjectOrTimeout._1.isDefined()
			|| !eventObjectOrTimeout._1.get().checkChannel(appId, channelName)
			|| !eventObjectOrTimeout._1.get().matches(listenerFilters)) {

			eventObjectOrTimeout = await(Promise.waitEither(EventManager.instance.event.nextEvent(), new Timeout(Codec.UUID(), 60 * 1000)));
			if (eventObjectOrTimeout._2.isDefined()) {
				renderText(callback + "(\"timeout\");\r\n");
			}

		}

		renderText(callback + "(\"success\"," + eventObjectOrTimeout._1.get().message + ");\r\n");
	}

}

package models;

import play.libs.F;

public class EventManager {

	public static EventManager instance = new EventManager();

	public final F.EventStream<EventObject> event = new F.EventStream<EventObject>();

}

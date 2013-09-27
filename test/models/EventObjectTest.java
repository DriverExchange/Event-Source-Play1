package models;

import org.junit.Test;
import play.test.UnitTest;

public class EventObjectTest extends UnitTest {


	@Test
	public void testCheckFilters() {

		assertTrue(
			new EventObject("app1", "chan1", "{\"id\":[\"1\", \"2\", \"3\", \"123\", \"4\"]}", "{}")
				.checkFilters(EventObject.ingestFilters("{\"id\":[\"123\"],\"group\":[\"abc\",\"def\"]}")));

		assertFalse(
			new EventObject("app1", "chan1", "{\"id\":[\"1\", \"2\", \"3\", \"123\", \"4\"]}", "{}")
				.checkFilters(EventObject.ingestFilters("{\"id\":[\"888\"],\"group\":[\"abc\",\"def\"]}")));

		assertFalse(
			new EventObject("app1", "chan1", "{\"id\":[\"1\", \"2\", \"3\", \"123\", \"4\"], \"group\":[\"def\"]}", "{}")
				.checkFilters(EventObject.ingestFilters("{\"id\":[\"888\"],\"group\":[\"abc\",\"def\"]}")));

		assertTrue(
			new EventObject("app1", "chan1", "{\"group\":[\"def\"]}", "{}")
				.checkFilters(EventObject.ingestFilters("{\"id\":[\"888\"],\"group\":[\"abc\",\"def\"]}")));

		assertTrue(
			new EventObject("app1", "chan1", "{}", "{}")
				.checkFilters(EventObject.ingestFilters("{\"id\":[\"888\"],\"group\":[\"abc\",\"def\"]}")));

		assertTrue(
			new EventObject("app1", "chan1", "{}", "{}")
				.checkFilters(EventObject.ingestFilters("{}")));

		assertFalse(
			new EventObject("app1", "chan1", "{\"group\":[\"def\"]}", "{}")
				.checkFilters(EventObject.ingestFilters("{}")));

	}

	@Test
	public void testCheckChannel() {

		assertTrue(new EventObject("app1", "chan1", "{}", "{}").checkChannel("app1", "chan1"));
		assertFalse(new EventObject("app1", "chan1", "{}", "{}").checkChannel("app1", "chan2"));
		assertFalse(new EventObject("app1", "chan1", "{}", "{}").checkChannel("app2", "chan1"));
		assertFalse(new EventObject("app1", "chan1", "{}", "{}").checkChannel("app2", "chan2"));

	}

	@Test
	public void testCheck() {

		assertTrue(
			new EventObject("app1", "chan1", "{\"id\":[\"1\", \"2\", \"3\", \"123\", \"4\"]}", "{}")
				.check("app1", "chan1", "{\"id\":[\"123\"],\"group\":[\"abc\",\"def\"]}"));

		assertFalse(
			new EventObject("app1", "chan1", "{\"id\":[\"1\", \"2\", \"3\", \"123\", \"4\"]}", "{}")
				.check("app1", "chan2", "{\"id\":[\"123\"],\"group\":[\"abc\",\"def\"]}"));

		assertFalse(
			new EventObject("app1", "chan1", "{\"id\":[\"1\", \"2\", \"3\", \"123\", \"4\"]}", "{}")
				.check("app2", "chan2", "{\"id\":[\"123\"],\"group\":[\"abc\",\"def\"]}"));

		assertFalse(
			new EventObject("app1", "chan1", "{\"id\":[\"1\", \"2\", \"3\", \"123\", \"4\"]}", "{}")
				.check("app1", "chan1", "{\"id\":[\"888\"],\"group\":[\"abc\",\"def\"]}"));

		assertFalse(
			new EventObject("app1", "chan1", "{\"id\":[\"1\", \"2\", \"3\", \"123\", \"4\"]}", "{}")
				.check("app1", "chan1", "{}"));

		assertTrue(
			new EventObject("app1", "chan1", "{}", "{}")
				.check("app1", "chan1", "{\"id\":[\"888\"],\"group\":[\"abc\",\"def\"]}"));

		assertFalse(
			new EventObject("app1", "chan1", "{}", "{}")
				.check("app1", "chan2", "{\"id\":[\"888\"],\"group\":[\"abc\",\"def\"]}"));

	}

}

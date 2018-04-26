package com.chrisdmilner.webapp;

import org.json.JSONException;
import org.json.JSONObject;

/*
 * Fact
 *
 * Stores a single piece of information mined from a user's profile in a name-value pair. Also includes a source Fact
 * which can be used to trace the source of this Fact back to its root.
 *
 * */
public class Fact<T> {

	private String name;		// The name of the fact. 
	private T value;			// The value of the fact.
	private Fact source;		// The primary source of the fact.

	public Fact(String name, T value, Fact source) {
		this.name = name;
		this.value = value;
		this.source = source;
	}

	public String getName() {
		return name;
	}

	public T getValue() {
		return value;
	}

	public Fact getSource() {
		return source;
	}

	// Converts the Fact to a human readable version for command line output.
	public String toString() {
		String out = "";

		if (value instanceof String) {
			out = name + ": " + value;
		} else {
			out = name + ": (Object) " + value.toString();
		}

		if (source != null) {
			out += " <- " + source.toString();
		}

		return out;
	}

	// Converts the Fact to a JSON representation.
	public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("value", value.toString());

        if (source != null) json.put("source", source.toJSON());
        else json.put("source", new JSONObject());

        return json;
	}

}
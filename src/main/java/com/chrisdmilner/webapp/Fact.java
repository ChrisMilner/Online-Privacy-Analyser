package com.chrisdmilner.webapp;

import org.json.JSONException;
import org.json.JSONObject;

// Stores a fact extracted by the Miner.
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

	public JSONObject toJSON() throws JSONException {
        JSONObject json = new JSONObject();
        json.put("name", name);
        json.put("value", value.toString());

        if (source != null) json.put("source", source.toJSON());
        else json.put("source", new JSONObject());

        return json;
	}

}
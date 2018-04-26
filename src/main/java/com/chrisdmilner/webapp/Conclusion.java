package com.chrisdmilner.webapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;

/*
 * Conclusion
 *
 * Stores a single piece of information that the system has concluded as its best option. Also stores a list of its
 * sources.
 *
 * */
public class Conclusion<T> {

	private String name;			    // The name of the conclusion.
	private T value;					// The value of the conclusion.
	private double confidence;		    // How confident the Analyser is in the conclusion (0 - 1).
	private ArrayList<Fact> sources;	// An array of the sources which contributed to the conclusion.

	public Conclusion(String name, T value, double confidence, ArrayList<Fact> sources) {
		this.name = name;
		this.value = value;
		this.confidence = Math.round(confidence * 10000d) / 10000d;
		this.sources = sources;
	}

	public String getName() {
		return name;
	}

	public T getValue() {
		return value;
	}

	public double getConfidence() {
		return confidence;
	}

	public void setConfidence(double confidence) {
		this.confidence = Math.round(confidence * 10000d) / 10000d;
	}

	public ArrayList<Fact> getSources() {
		return sources;
	}

	public void addSources(ArrayList<Fact> fs) {
	    this.sources.addAll(fs);
    }

    // Converts to a string for command line output.
	public String toString() {
		String out = name + " : " + value + " (" + confidence + ")\n";

        for (Fact f : sources)
            out += "   " + f + "\n";

		return out;
	}

	// Converts the conclusion to a JSON format.
	public JSONObject toJSON() throws JSONException {
		JSONObject json = new JSONObject();
		json.put("name", name);
		json.put("value", value);
		json.put("confidence", confidence);

        JSONArray srcs = new JSONArray();
        for (Fact s : sources)
            srcs.put(s.toJSON());

        json.put("sources", srcs);

        return json;
	}

}
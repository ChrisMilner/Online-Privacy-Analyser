package com.chrisdmilner.webapp;

import java.util.ArrayList;

// Stores a belief concluded on by the Analyser
public class Conclusion {

	private String name;			// The name of the conclusion.
	private String value;			// The value of the conclusion.
	private double confidence;		// How confident the Analyser is in the conclusion (0 - 1).
	private ArrayList<Fact> sources;		// An array of the sources which contributed to the conclusion.

	public Conclusion(String name, String value, double confidence, ArrayList<Fact> sources) {
		this.name = name;
		this.value = value;
		this.confidence = confidence;
		this.sources = sources;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public double getConfidence() {
		return confidence;
	}

	public ArrayList<Fact> getSources() {
		return sources;
	}

	// Converts the conclusion to a JSON format.
	public String toString() {
		String out = "{\"name\":\"" + name + "\", \"value\":\"" + value + "\", \"confidence\":" + confidence + ", \"sources\":[";
		
		for (int i = 0; i < sources.size(); i++) {
			out += "\"" + sources.get(i) +"\"";
			if (i < sources.size() - 1) out += ", ";
		}
		out += "]}";

		return out;
	}

}
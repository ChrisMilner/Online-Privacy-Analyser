package com.chrisdmilner.webapp;

// Stores a belief concluded on by the Analyser
public class Conclusion {

	private String name;			// The name of the conclusion.
	private String value;			// The value of the conclusion.
	private double confidence;		// How confident the Analyser is in the conclusion (0 - 1).
	private String[] sources;		// An array of the sources which contributed to the conclusion.

	public Conclusion(String name, String value, double confidence, String[] sources) {
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

	public String[] getSources() {
		return sources;
	}

	// Converts the conclusion to a JSON format.
	public String toString() {
		String out = "{\"name\":\"" + name + "\", \"value\":\"" + value + "\", \"confidence\":" + confidence + ", \"sources\":[";
		
		for (int i = 0; i < sources.length; i++) {
			out += "\"" + sources[i] +"\"";
			if (i < sources.length - 1) out += ", ";
		}
		out += "]}";

		return out;
	}

}
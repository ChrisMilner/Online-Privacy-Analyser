package com.chrisdmilner.webapp;

import java.util.ArrayList;

// Stores a belief concluded on by the Analyser
public class Conclusion<T> {

	private String name;			// The name of the conclusion.
	private T value;			// The value of the conclusion.
	private double confidence;		// How confident the Analyser is in the conclusion (0 - 1).
	private ArrayList<Fact> sources;		// An array of the sources which contributed to the conclusion.

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

	public String toString() {
		String out = name + " : " + value + " (" + confidence + ")\n";

        for (Fact f : sources)
            out += "   " + f.toString() + "\n";

		return out;
	}

	// Converts the conclusion to a JSON format.
	public String toJSON() {
		String out = "{\"name\":\"" + name + "\", \"value\":\"" + value + "\", \"confidence\":" + confidence + ", \"sources\":[";
		
		for (int i = 0; i < sources.size(); i++) {
			out += sources.get(i).toJSON();
			if (i < sources.size() - 1) out += ", ";
		}
		out += "]}";

		return out;
	}

}
package com.chrisdmilner.webapp;

// Stores a fact extracted by the Miner.
public class Fact<T> {

	private String name;		// The name of the fact. 
	private T value;			// The value of the fact.
	private String source;		// The primary source of the fact e.g. Twitter.
	private String subSource;	// The sub source of the fact e.g. UserProfile.

	public Fact(String name, T value, String source, String subSource) {
		this.name = name;
		this.value = value;
		this.source = source;
		this.subSource = subSource;
	}

	public String getName() {
		return name;
	}

	public T getValue() {
		return value;
	}

	public String getSource() {
		return source;
	}

	public String getSubSource() {
		return subSource;
	}

	public String getSourceString() {
		return source + "." + subSource;
	}

	public String toString() {
		String out = "";

		if (value instanceof String) {
			out = name + ": " + value + "\t" + source + "." + subSource;
		} else {
			out = name + ": (Object) " + value.toString() + "\t" + source + "." + subSource;
		}
		return out;
	}

}
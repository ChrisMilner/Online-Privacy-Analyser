package com.chrisdmilner.webapp;

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
			out = name + ": " + value + "\t" + source.toString();
		} else {
			out = name + ": (Object) " + value.toString() + "\t" + source.toString();
		}
		return out;
	}

}
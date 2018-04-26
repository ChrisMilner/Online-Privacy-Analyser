package com.chrisdmilner.webapp;

import java.util.ArrayList;

/*
 * Factbook
 *
 * Stores a list of facts. Also contains a selection of helper function for interacting with the list.
 *
 * */
public class FactBook {

	private ArrayList<Fact> facts;		// The list of the Facts.

	public FactBook() {
		facts = new ArrayList<Fact>();
	}

	public void addFact(Fact f) {
		facts.add(f);
	}

	// Adds all of the facts in a factbook to this one.
	public void addFactBook(FactBook f) {
		facts.addAll(f.getFactsAsArrayList());
	}

	public ArrayList<Fact> getFactsAsArrayList() {
		return facts;
	}

	// Returns only the Facts from the list with the given Fact name.
	public ArrayList<Fact> getFactsWithName(String name) {
	    ArrayList<Fact> fs = new ArrayList<>();
	    for (Fact fact : facts) {
	        if (fact.getName().equals(name)) fs.add(fact);
        }
        return fs;
    }

	public int noOfFacts() {
		return facts.size();
	}

	// Converts the Factbook to a human readable version for command line output.
	public String toString() {
		String out = "";

		for (int i = 0; i < facts.size(); i++) {
			out += facts.get(i).toString() + "\n";
		}

		return out;
	}

}
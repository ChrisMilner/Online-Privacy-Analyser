package com.chrisdmilner.webapp;

import java.util.ArrayList;

// Contains functions related to general data mining
public class Miner {

	public static void main(String[] args) {
		if (args.length != 3) {
			System.err.println("ERROR incorrect number of arguments. You must give three argument: a Facebook, Twitter and Reddit URL.");
			System.exit(1);
		}

		mine(args[0],args[1],args[2]);
	}

	// Controls all of the mining function, passes data to the anaalyser and outputs conclusions to a file.
	public static String mine(String fb, String tw, String rd) {

		FactBook twf = new FactBook();
		if (!tw.equals("")) {
			String twName = tw.substring(tw.lastIndexOf("/") + 1);
			twf = TwitterMiner.mine(twName);
		}

		FactBook fbf = new FactBook();
		if (!fb.equals("")) {
			String fbId = fb.substring(fb.lastIndexOf("=") + 1);
			fbf = FacebookMiner.mine(fbId);
		}

		FactBook rdf = new FactBook();
		if (!rd.equals("")) {
			String rdName = rd.substring(rd.lastIndexOf("user/") + 5);
			rdf = RedditMiner.mine(rdName);
		}

		// Combine the facts from each miner.
		twf.addFactBook(fbf);
		twf.addFactBook(rdf);
		
		// Print out the facts.
		System.out.println("\nFacts:");
		if (twf.noOfFacts() <= 0) System.out.println("No Facts Found");
		else System.out.print(twf.toString());

		// Give the facts to the analyser and record the conclusions.
		ArrayList<Conclusion> conclusions = Analyser.analyse(twf);

		// Convert conclusions to JSON for transporting.
        return conclusionsToJSON(conclusions);

		// Write the conclusions to a file.
		// writeConclusionsToFile(id, conclusions);
	}

	private static String conclusionsToJSON(ArrayList<Conclusion> c) {
        String json = "";

	    json += "{\"conclusions\":[";
        for (int i = 0; i < c.size(); i++) {
            json += c.get(i).toString();
            if (i < c.size() - 1) json += ",";
        }
        json += "]}";

        return json;
    }

	// Writes a list of conclusions into a file in JSON format.
	private static void writeConclusionsToFile(int id, ArrayList<Conclusion> c) {
		ArrayList<String> lines = new ArrayList<String>();
		
		// Create the JSON files lines.
		lines.add("{\"conclusions\":[");
		for (int i = 0; i < c.size(); i++) {
			lines.add(c.get(i).toString());
			if (i < c.size() - 1) lines.set(i, lines.get(i) + ",");
		}
		lines.add("]}");

		// Output the list of lines to a file
		Util.outputToFile("../outputs/" + id + ".data", lines);
	}

}
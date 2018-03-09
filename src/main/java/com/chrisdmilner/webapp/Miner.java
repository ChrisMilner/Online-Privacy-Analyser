package com.chrisdmilner.webapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.util.ArrayList;

// Contains functions related to general data mining
public class Miner {

	// Controls all of the mining function, passes data to the anaalyser and outputs conclusions to a file.
	public static String mine(String fb, String tw, String rd, String at) {

		FactBook twf = new FactBook();
		if (!tw.equals("")) {
			String twName = tw.substring(tw.lastIndexOf("/") + 1);
			twf = TwitterMiner.mine(twName);
		}

		FactBook fbf = new FactBook();
		if (!(fb.equals("") && at.equals(""))) {
			String fbId = "";
			if (!fb.equals("")) fbId = fb.substring(fb.lastIndexOf("=") + 1);
			fbf = FacebookMiner.mine(fbId, at);
		}

		FactBook rdf = new FactBook();
		if (!rd.equals("")) {
			String rdName = rd.substring(rd.lastIndexOf("user/") + 5);
			if (rdName.charAt(rdName.length() - 1) == '/')
			    rdName = rdName.substring(0, rdName.length() - 1);

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

	protected static String conclusionsToJSON(ArrayList<Conclusion> cs) {
        JSONObject json;
        try {
            json = new JSONObject();
            JSONArray conclusions = new JSONArray();

            for (Conclusion c : cs)
                conclusions.put(c.toJSON());

            json.put("conclusions", conclusions);
        } catch (JSONException e) {
            System.err.println("ERROR converting conclusions to JSON");
            e.printStackTrace();
            return null;
        }

        return json.toString();
	}

	// Writes a list of conclusions into a file in JSON format.
//	private static void writeConclusionsToFile(int id, ArrayList<Conclusion> c) {
//		ArrayList<String> lines = new ArrayList<>();
//
//		// Create the JSON files lines.
//		lines.add("{\"conclusions\":[");
//		for (int i = 0; i < c.size(); i++) {
//			lines.add(c.get(i).toJSON());
//			if (i < c.size() - 1) lines.set(i, lines.get(i) + ",");
//		}
//		lines.add("]}");
//
//		// Output the list of lines to a file
//		Util.outputToFile("../outputs/" + id + ".data", lines);
//	}

}
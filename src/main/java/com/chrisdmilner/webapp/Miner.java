package com.chrisdmilner.webapp;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.omg.Messaging.SYNC_WITH_TRANSPORT;

import java.util.ArrayList;

/*
 * Miner
 *
 * Handles all the individual miners. Combines their data then passes it to the analyser
 *
 * */
public class Miner {

	// Controls all of the mining function, passes data to the analyser and outputs conclusions to a file.
	public static String mine(String fb, String tw, String rd, String at) {

		// Get the Twitter profile information and call the miner.
		FactBook twf = new FactBook();
		if (!tw.equals("")) {
			String twName = tw.substring(tw.lastIndexOf("/") + 1);
			twf = TwitterMiner.mine(twName);
		}

		// Get the Facebook profile information and call the miner.
		FactBook fbf = new FactBook();
		if (!(fb.equals("") && at.equals(""))) {
			String fbId = "";
			if (!fb.equals("")) fbId = fb.substring(fb.lastIndexOf("=") + 1);
			fbf = FacebookMiner.mine(fbId, at);
		}

		// Get the Reddit profile information and call the miner.
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

}
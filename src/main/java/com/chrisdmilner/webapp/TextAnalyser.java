package com.chrisdmilner.webapp;

import java.util.ArrayList;

/*
 * Text Analyser
 *
 * Uses the Keyword Analyser to detect keywords and parses location data tagged to the posts.
 *
 * */
public class TextAnalyser {

    // Analyses all of the user's posts.
    public static ArrayList<Conclusion> analyse(FactBook f) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();

        ArrayList<Fact> posts = f.getFactsWithName("Posted");

        // Use the keyword analyser on each post and parse it into a correctly tagged fact.
        for (Fact post : posts) {
            MinedPost mp = (MinedPost) post.getValue();
            if (mp.getContent() == null) continue;

            ArrayList<String> keywords = KeywordTextAnalyser.analyse(mp.getContent());
            String conclusionName = "Keyword Shared";
            if (mp.isByUser()) conclusionName = "Keyword Posted";

            for (String kw : keywords) {
                ArrayList<Fact> sources = new ArrayList<>();
                sources.add(post);
                conclusions.add(new Conclusion<>(conclusionName, kw, 1, sources));
            }

            if (mp.isByUser()) conclusions.addAll(analysePost(post));
        }

        return conclusions;
    }

    // Analyses a post looking for location information in metadata.
    protected static ArrayList<Conclusion> analysePost(Fact post) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();
        MinedPost mp = (MinedPost) post.getValue();

        // Add the location as a fact if it exists, if not then add the place if it is available.
        if (mp.getLocation() != null) {
            ArrayList<Fact> sources = new ArrayList<>();
            sources.add(post);
            String coord = mp.getLocation().getLatitude() + ", " + mp.getLocation().getLongitude();
            System.out.println(coord);
            conclusions.add(new Conclusion<>("Location", coord, Analyser.getConfidenceFromSource(post), sources));
        } else if (mp.getPlace() != null) {
            ArrayList<Fact> sources = new ArrayList<>();
            sources.add(post);
            String place = mp.getPlace().getFullName() + ", " + mp.getPlace().getCountry();
            conclusions.add(new Conclusion<>("Location", place, Analyser.getConfidenceFromSource(post), sources));
        }

        return conclusions;
    }
}

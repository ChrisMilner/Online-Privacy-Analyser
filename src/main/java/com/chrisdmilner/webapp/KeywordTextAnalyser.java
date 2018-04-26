package com.chrisdmilner.webapp;

import java.util.ArrayList;

/*
 * Keyword Text Analyser
 *
 * Looks for keywords in all of the users text posts and adds them as facts if they are found.
 *
 * */
public class KeywordTextAnalyser {

    public static ArrayList<String> analyse(String post) {
        ArrayList<String> keywordsFound = new ArrayList<>();

        // Remove all none letters and convert to lower case to make it case insensitive.
        post = post.replaceAll("[^a-zA-Z ]", "").toLowerCase();

        // Split the post into string using spaces.
        String[] words = post.split("\\s+");

        // Read in all the keywords.
        ArrayList<String> keywords = Util.readResourceFileLines("data/keywords.csv");
        keywords.addAll(Util.readResourceFileLines("data/swearwords.csv"));

        // Check if the word matches any of the keywords.
        for (String word : words) {
            for (String keyword : keywords) {
                if (word.equals(keyword)) {
                    System.out.println("      Found Keyword: " + keyword);
                    keywordsFound.add(keyword);
                }
            }
        }

        return keywordsFound;
    }
}

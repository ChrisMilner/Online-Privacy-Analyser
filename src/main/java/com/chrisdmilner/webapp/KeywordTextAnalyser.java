package com.chrisdmilner.webapp;

import java.util.ArrayList;

public class KeywordTextAnalyser {

    public static ArrayList<String> analyse(String post) {
        ArrayList<String> keywordsFound = new ArrayList<>();

        post = post.replaceAll("[^a-zA-Z ]", "").toLowerCase();
        String[] words = post.split("\\s+");

        ArrayList<String> keywords = Util.readResourceFileLines("data/keywords.csv");
        keywords.addAll(Util.readResourceFileLines("data/swearwords.csv"));

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

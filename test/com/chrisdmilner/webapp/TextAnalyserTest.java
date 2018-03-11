package com.chrisdmilner.webapp;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class TextAnalyserTest {

    @Test
    public void analysePost() {

//        ArrayList<Conclusion> concs = TextAnalyser.analysePost("A car is a vehicle", null);
//        System.out.println(concs.toString() + "\n");
//
//        concs = TextAnalyser.analysePost("Just went to France in one", null);
//        System.out.println(concs.toString() + "\n");
//
//        concs = TextAnalyser.analysePost("A bike is also a vehicle", null);
//        System.out.println(concs.toString() + "\n");
//
//        concs = TextAnalyser.analysePost("Krispy Kreme doughnuts are very tasty", null);
//        System.out.println(concs.toString() + "\n");
//
//        concs = TextAnalyser.analysePost("Just two weeks then I am off to North Korea", null);
//        System.out.println(concs.toString() + "\n");
    }

    @Test
    public void lemmatizeVerb() {
        String lemma = TextAnalyser.lemmatizeVerb("lived", "VBN");
        assertEquals("live", lemma);

        lemma = TextAnalyser.lemmatizeVerb("buying", "VBG");
        assertEquals("buy", lemma);

        lemma = TextAnalyser.lemmatizeVerb("go", "VB");
        assertEquals("go", lemma);

        lemma = TextAnalyser.lemmatizeVerb("place", "VB");
        assertEquals("place", lemma);

        lemma = TextAnalyser.lemmatizeVerb("are", "VBP");
        assertEquals("be", lemma);

        lemma = TextAnalyser.lemmatizeVerb("is", "VBZ");
        assertEquals("be", lemma);

        lemma = TextAnalyser.lemmatizeVerb("bought", "VBN");
        assertEquals("buy", lemma);
    }
}
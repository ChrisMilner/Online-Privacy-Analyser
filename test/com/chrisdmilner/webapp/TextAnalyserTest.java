package com.chrisdmilner.webapp;

import org.junit.Test;

import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

import static org.junit.Assert.*;

public class TextAnalyserTest {

    @Test
    public void analysePost() {
        String[][] spo = TextAnalyser.analysePost("A car is a vehicle");
        assertEquals("A car", spo[0][0]);
        assertEquals("is", spo[0][1]);
        assertEquals("a vehicle", spo[0][2]);

        spo = TextAnalyser.analysePost("Just went to France in one");
        assertEquals("I", spo[0][0]);
        assertEquals("went", spo[0][1]);
        assertEquals("France", spo[0][2]);

        spo = TextAnalyser.analysePost("A bike is also a vehicle");
        assertEquals("A bike", spo[0][0]);
        assertEquals("is", spo[0][1]);
        assertEquals("a vehicle", spo[0][2]);

        spo = TextAnalyser.analysePost("Krispy Kreme doughnuts are very tasty");
        assertEquals("Krispy Kreme doughnuts", spo[0][0]);
        assertEquals("are", spo[0][1]);
        assertEquals("very tasty", spo[0][2]);
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
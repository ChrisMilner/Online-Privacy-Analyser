package com.chrisdmilner.webapp;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class TextAnalyserTest {

    @Test
    public void analysePost() {
        Fact fbRoot = new Fact<>("Facebook Account", "19374837249", null);

        MinedPost mp = new MinedPost(null, null, null, null, "I watch football.", true);
        ArrayList<Conclusion> results = TextAnalyser.analysePost(new Fact<>("Tweet", mp, fbRoot));

        assertEquals(1, results.size());
        assertEquals("watch football", results.get(0).getValue());

        mp = new MinedPost(null, null, null, null, "I love watching football. Also I live in England.", true);
        results = TextAnalyser.analysePost(new Fact<>("Tweet", mp, fbRoot));

        assertEquals(2, results.size());
        assertEquals("Love: Watching Football", results.get(0).getValue());
        assertEquals("Live: England", results.get(1).getValue());

        mp = new MinedPost(null, null, null, null, "Jeremy Corbyn is my favourite politician.", true);
        results = TextAnalyser.analysePost(new Fact<>("Tweet", mp, fbRoot));

        assertEquals(1, results.size());
        assertEquals("Favourite Politician: Jeremy Corbyn", results.get(0).getValue());
    }
}
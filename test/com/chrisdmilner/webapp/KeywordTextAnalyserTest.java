package com.chrisdmilner.webapp;

import org.junit.Test;

import java.util.ArrayList;

import static org.junit.Assert.*;

public class KeywordTextAnalyserTest {

    @Test
    public void analyse() {
        ArrayList<String> s = KeywordTextAnalyser.analyse("This is a harmless post.");
        assertTrue(s.isEmpty());

        s = KeywordTextAnalyser.analyse("I'm going to attack people.");
        assertEquals(1, s.size());
        assertEquals("attack", s.get(0));

        s = KeywordTextAnalyser.analyse("I'm going to murder people! Fuck.");
        assertEquals(2, s.size());
        assertEquals("murder", s.get(0));
        assertEquals("fuck", s.get(1));

        s = KeywordTextAnalyser.analyse("Some hours later, as I was down on my knees, my blindfold was removed and I found myself looking up at the training sergeant-major.\n'Am I binned?' I said pitifully.\n'No, you nugget. Get back on the helicopter and don't fuck up.'\nI'd caught him in a good mood. An ex-Household Division man himself, he was delighted to see his old lot doing so well.");
        assertEquals(1, s.size());
        assertEquals("fuck", s.get(0));
    }
}
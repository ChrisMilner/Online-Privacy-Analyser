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
    }
}
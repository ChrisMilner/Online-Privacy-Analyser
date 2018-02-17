package com.chrisdmilner.webapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;

import static org.junit.Assert.*;

public class AnalyserTest {

    @org.junit.Test
    public void analyse() {
        FactBook fb = new FactBook();
        fb.addFact(new Fact<>("Name", "chrism", "Twitter", "UserProfile"));
        fb.addFact(new Fact<>("Name", "Christopher David Milner", "Facebook", "UserProfile"));
        fb.addFact(new Fact<>("Name", "ChrisDMilner97", "Twitter", "UserProfile"));
        fb.addFact(new Fact<>("Name", "Chris Milner", "Twitter", "UserProfile"));
        fb.addFact(new Fact<>("Name", "Radioactive1997", "Reddit", "UserProfile"));
        fb.addFact(new Fact<>("First Name", "Christopher", "Facebook", "UserProfile"));
        fb.addFact(new Fact<>("Last Name", "Milner", "Facebook", "UserProfile"));
        fb.addFact(new Fact<>("Image URL", "http://example.com/image.png", "Facebook", "Photos"));

        try {
            DateFormat df = new SimpleDateFormat("dd/mm/yyyy");
            fb.addFact(new Fact<>("Max Birth Date", df.parse("17/08/2005"), "Facebook", "UserProfile"));
            fb.addFact(new Fact<>("Min Birth Date", df.parse("17/08/1995"), "Facebook", "UserProfile"));
            fb.addFact(new Fact<>("Birth Year", df.parse("01/01/1997"), "Facebook", "UserProfile"));
        } catch (ParseException e) {
            System.err.println("ERROR parsing the test date(s)");
            e.printStackTrace();
        }

        assertTrue(Analyser.analyse(fb).size() > 0);
    }
}
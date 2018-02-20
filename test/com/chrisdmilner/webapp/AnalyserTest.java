package com.chrisdmilner.webapp;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class AnalyserTest {

    @org.junit.Test
    public void analyse() throws Exception {
        Fact fbRoot = new Fact<>("Facebook User ID", "100008177116719", null);
        Fact twRoot = new Fact<>("Twitter Handle", "ChrisDMilner", null);
        Fact rdRoot = new Fact<>("Reddit User Name", "Radioactive1997", null);

        FactBook fb = new FactBook();
        fb.addFact(new Fact<>("Name", "chrism", twRoot));
        fb.addFact(new Fact<>("Name", "Christopher David Milner", fbRoot));
        fb.addFact(new Fact<>("Name", "ChrisDMilner97", twRoot));
        fb.addFact(new Fact<>("Name", "Chris Milner", twRoot));
        fb.addFact(new Fact<>("Name", "Radioactive1997", rdRoot));
        fb.addFact(new Fact<>("First Name", "Christopher", fbRoot));
        fb.addFact(new Fact<>("Last Name", "Milner", fbRoot));
        fb.addFact(new Fact<>("Image URL", "http://example.com/image.png", fbRoot));

        try {
            DateFormat df = new SimpleDateFormat("dd/mm/yyyy");
            fb.addFact(new Fact<>("Max Birth Date", df.parse("17/08/2005"), fbRoot));
            fb.addFact(new Fact<>("Min Birth Date", df.parse("17/08/1995"), fbRoot));
            fb.addFact(new Fact<>("Birth Year", df.parse("01/01/1997"), fbRoot));
        } catch (ParseException e) {
            System.err.println("ERROR parsing the test date(s)");
            e.printStackTrace();
        }

        ArrayList<Conclusion> conclusions = Analyser.analyse(fb);
        assertTrue(conclusions.size() > 0);

        for (Conclusion c : conclusions)
            System.out.println(c.toString());

        // Test if the generated JSON is valid.
        String json = Miner.conclusionsToJSON(conclusions);
        System.out.println(json);

        new JSONObject(json);
    }
}
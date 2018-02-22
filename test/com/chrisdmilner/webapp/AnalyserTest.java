package com.chrisdmilner.webapp;

import org.json.JSONObject;
import org.junit.Test;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

public class AnalyserTest {

    @Test
    public void analyse() throws Exception {
        // Create the test data.
        Fact fbRoot = new Fact<>("Facebook Account", "100008177116719", null);
        Fact twRoot = new Fact<>("Twitter Account", "ChrisDMilner", null);
        Fact rdRoot = new Fact<>("Reddit Account", "Radioactive1997", null);

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
            DateFormat df = new SimpleDateFormat("dd/MM/yyyy");
            fb.addFact(new Fact<>("Max Birth Date", df.parse("17/08/2005"), fbRoot));
            fb.addFact(new Fact<>("Min Birth Date", df.parse("17/08/1995"), fbRoot));
            fb.addFact(new Fact<>("Birth Year", df.parse("01/01/1997"), fbRoot));
        } catch (ParseException e) {
            System.err.println("ERROR parsing the test date(s)");
            e.printStackTrace();
        }

        // Test that the analyser runs correctly and returns some conclusions.
        ArrayList<Conclusion> conclusions = Analyser.analyse(fb);
        assertTrue(conclusions.size() > 0);

        for (Conclusion c : conclusions)
            System.out.println(c.toString());

        // Test if the generated JSON is valid.
        String json = Miner.conclusionsToJSON(conclusions);
        System.out.println(json);

        new JSONObject(json);
    }

    @Test
    public void nameAnalysis() throws Exception {
        // Create the test data.
        Fact fbRoot = new Fact<>("Facebook Account", "100008177116719", null);

        FactBook f = new FactBook();
        f.addFact(new Fact<>("Name","Chris Milner", fbRoot));
        f.addFact(new Fact<>("Name","ChrisDMilner97", fbRoot));
        f.addFact(new Fact<>("Name","Chris_milner", fbRoot));
        f.addFact(new Fact<>("Name","chris-m", fbRoot));
        f.addFact(new Fact<>("Name","Christopher David Milner", fbRoot));
        f.addFact(new Fact<>("Name", "christopher1997", fbRoot));
        f.addFact(new Fact<>("Name", "chris08", fbRoot));
        f.addFact(new Fact<>("Name","Chris.Milner.97", fbRoot));

        // Cases to handle (hopefully).
        // f.addFact(new Fact<>("name", "Mr Chris Milner", fbRoot));

        Analyser.analyseName(f);

        ArrayList<Fact> fn = f.getFactsWithName("First Name");
        assertEquals(8, fn.size());
        assertEquals("Chris", fn.get(0).getValue());
        assertEquals("Chris", fn.get(1).getValue());
        assertEquals("Chris", fn.get(2).getValue());
        assertEquals("chris", fn.get(3).getValue());
        assertEquals("Christopher", fn.get(4).getValue());
        assertEquals("christopher", fn.get(5).getValue());
        assertEquals("chris", fn.get(6).getValue());
        assertEquals("Chris", fn.get(7).getValue());

        ArrayList<Fact> mn = f.getFactsWithName("Middle Name");
        assertEquals(2, mn.size());
        assertEquals("D", mn.get(0).getValue());
        assertEquals("David", mn.get(1).getValue());

        ArrayList<Fact> ln = f.getFactsWithName("Last Name");
        assertEquals(6, ln.size());
        assertEquals("Milner", ln.get(0).getValue());
        assertEquals("Milner", ln.get(1).getValue());
        assertEquals("milner", ln.get(2).getValue());
        assertEquals("m", ln.get(3).getValue());
        assertEquals("Milner", ln.get(4).getValue());
        assertEquals("Milner", ln.get(5).getValue());

        ArrayList<Fact> bd = f.getFactsWithName("Birth Year");
        DateFormat df = new SimpleDateFormat("yyyy");

        assertEquals(4, bd.size());
        assertEquals(df.parse("1997"), bd.get(0).getValue());
        assertEquals(df.parse("1997"), bd.get(1).getValue());
        assertEquals(df.parse("2008"), bd.get(2).getValue());
        assertEquals(df.parse("1997"), bd.get(3).getValue());

        // Conclusion Analyser
        ArrayList<Conclusion> c = Analyser.analyseNameParts(f);

        assertEquals(3, c.size());
        assertTrue(c.get(0).getValue().equals("Chris") || c.get(0).getValue().equals("Christopher"));
        assertEquals("David", c.get(1).getValue());
        assertEquals("Milner", c.get(2).getValue());
    }

    @Test
    public void testWithFacebook() throws Exception {
        FactBook f = FacebookMiner.mine("100008177116719","EAACEdueP1ygBAD2dSZAPieZAmFIcWaB2qVT2C5RtGmKmvgRpMY8UEXODyFUFYGVMC0ZCZCuNVdmnZCYqGx0dwXVjXDsxpuRVA1PXJXkPiz17PMMLB4pNZAlmeRCnSz3wf4oW1OdMbEZBMMovdZATZCQW8f27cHZCBKYM0ZD");

        ArrayList<Conclusion> conclusions = Analyser.analyse(f);
        assertTrue("Some Conclusions have been returned.", conclusions.size() > 0);

        for (Conclusion c : conclusions) {
            if (c.getName().equals("First Name"))
                assertTrue("Name is Chris", c.getValue().equals("Chris") || c.getValue().equals("Christopher"));
            else if (c.getName().equals("Middle Name"))
                assertTrue("Middle Name is David", c.getValue().equals("David") || c.getValue().equals("D"));
            else if (c.getName().equals("Last Name"))
                assertEquals("Milner", c.getValue());
            else if (c.getName().equals("Gender"))
                assertEquals("Male", c.getValue());
        }
    }

    @Test
    public void analyseBirthDate() throws Exception {
        // Create the test data.
        Fact fbRoot = new Fact<>("Facebook Account", "100008177116719", null);
        Fact twRoot = new Fact<>("Twitter Account", "ChrisDMilner", null);
        Fact rdRoot = new Fact<>("Reddit Account", "Radioactive1997", null);

        FactBook f = new FactBook();

        DateFormat df = new SimpleDateFormat("yyyy");
        f.addFact(new Fact<>("Birth Year", df.parse("1997"), fbRoot));
        f.addFact(new Fact<>("Birth Year", df.parse("1997"), twRoot));
        f.addFact(new Fact<>("Birth Year", df.parse("1999"), rdRoot));
        f.addFact(new Fact<>("Max Birth Date", df.parse("2005"), rdRoot));
        f.addFact(new Fact<>("Max Birth Date", df.parse("2012"), rdRoot));
        f.addFact(new Fact<>("Min Birth Date", df.parse("1996"), rdRoot));
        f.addFact(new Fact<>("Min Birth Date", df.parse("1990"), rdRoot));

        ArrayList<Conclusion> conclusions = Analyser.analyseBirthDate(f);
        assertEquals("1997", conclusions.get(0).getValue());
        assertEquals("20 - 21", conclusions.get(1).getValue());
    }

    @Test
    public void smallFunctionTests() throws Exception {
        assertEquals("chris", Analyser.removeNumbers("chris97"));
    }

}
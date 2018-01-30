package com.chrisdmilner.webapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;

public class Analyser {

    public static void main(String[] args) {
        FactBook fb = new FactBook();
        fb.addFact(new Fact<>("Name", "Christopher David Milner", "Facebook", "UserProfile"));
        fb.addFact(new Fact<>("Name", "ChrisDMilner97", "Twitter", "UserProfile"));
        fb.addFact(new Fact<>("Name", "Chris Milner", "Twitter", "UserProfile"));
        fb.addFact(new Fact<>("Name", "Radioactive1997", "Reddit", "UserProfile"));
        fb.addFact(new Fact<>("First Name", "Christopher", "Facebook", "UserProfile"));
        fb.addFact(new Fact<>("Last Name", "Milner", "Facebook", "UserProfile"));

        try {
            DateFormat df = new SimpleDateFormat("dd/mm/yyyy");
            fb.addFact(new Fact<>("Created Account", df.parse("17/08/2005"), "Facebook", "UserProfile"));
        } catch (ParseException e) {
            System.err.println("ERROR parsing the test date(s)");
            e.printStackTrace();
        }

        analyse(fb);
    }

    public static ArrayList<Conclusion> analyse(FactBook f) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();

        System.out.println("Running Analyser");

        analyseName(f);
        conclusions.addAll(analyseNameParts(f));
        conclusions.addAll(analyseBirthDate(f));

        System.out.println("Number of conclusions: " + conclusions.size());
        return conclusions;
    }

    private static void analyseName(FactBook fb) {
        ArrayList<Fact> names = fb.getFactsWithName("Name");

        for (Fact name : names) {
            String value = (String) name.getValue();
            String[] tokens;

            // Divide by spaces
            if (value.contains(" ")) {
                tokens = value.split("\\s+");
                addNameTokens(fb, tokens, name);
            } else {
                // Divide by punctuation
                tokens = value.split("[_.-]");
                if (tokens.length > 1)
                    addNameTokens(fb, tokens, name);
                else {
                    // Divide by capitals
                    tokens = value.split("(?=\\p{Upper})");
                    if (tokens.length > 1) addNameTokens(fb, tokens, name);
                    else fb.addFact(new Fact<>("First Name", value, name.getSource(), name.getSubSource()));
                }
            }

            String[] nums = value.split("[^0-9]");
            DateFormat df = new SimpleDateFormat("yyyy");
            for (String num : nums) {
                if (num.length() == 0) continue;

                int intnum = Integer.parseInt(num);
                try {
                    if (num.length() == 2) {
                        if (intnum > 20)
                            fb.addFact(new Fact<>("Birth Year", df.parse("19" + num), name.getSource(), name.getSubSource()));
                        else
                            fb.addFact(new Fact<>("Birth Year", df.parse("20" + num), name.getSource(), name.getSubSource()));
                    } else if (num.length() == 4 && intnum > 1900 && intnum <= 2017)
                        fb.addFact(new Fact<>("Birth Year", df.parse(num), name.getSource(), name.getSubSource()));
                } catch (ParseException e) {
                    System.err.println("ERROR converting date to correct format: yyyy");
                    e.printStackTrace();
                }
            }
        }
    }

    private static void addNameTokens(FactBook fb, String[] tokens, Fact source) {
        fb.addFact(new Fact<>("First Name", tokens[0], source.getSource(), source.getSubSource()));
        fb.addFact(new Fact<>("Last Name", tokens[tokens.length - 1], source.getSource(), source.getSubSource()));

        if (tokens.length > 2) {
            for (int i = 1; i < tokens.length - 1; i++)
                fb.addFact(new Fact<>("Middle Name", tokens[i], source.getSource(), source.getSubSource()));
        }
    }

    private static ArrayList<Conclusion> analyseNameParts(FactBook f) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();

        String[] nameParts = {"First Name", "Middle Name", "Last Name"};
        double confidence;
        HashSet<String> sources = new HashSet<>();

        for (String namePart : nameParts) {
            ArrayList<Fact> part = f.getFactsWithName(namePart);

            if (part.isEmpty()) continue;

            ArrayList<Double> initConfidences = new ArrayList<Double>();
            for (int i = 0; i < part.size(); i++)
                initConfidences.add(getConfidenceFromSource(part.get(i).getSource(), part.get(i).getSubSource()));

            confidence = initConfidences.get(0);
            sources.add(part.get(0).getSourceString());

            if (part.size() > 1) {
                String val1, val2;
                while (part.size() != 1) {
                    val1 = ((String) part.get(0).getValue()).toLowerCase();
                    val2 = ((String) part.get(1).getValue()).toLowerCase();

                    if (val1.equals(val2) || val1.contains(val2) || val2.contains(val1)) {
                        sources.add(part.get(1).getSourceString());
                        part.remove(1);
                        if (confidence >= initConfidences.get(1))
                            confidence += ((1 - confidence) * initConfidences.get(1));
                        else
                            confidence = initConfidences.get(1) + ((1- initConfidences.get(1)) * confidence);
                    } else {
                        if (confidence >= initConfidences.get(1)) {
                            part.remove(1);
                            confidence *= (1 - initConfidences.get(1));
                        } else {
                            part.remove(0);
                            confidence = initConfidences.get(1) * (1 - confidence);
                            sources.clear();
                            sources.add(part.get(0).getSourceString());
                        }
                    }

                    if (part.size() > 1) initConfidences.remove(1);
                }
            }

            System.out.println(namePart + ": " + part.get(0).getValue() + "  Confidence: " + confidence);
            String[] sourceArr = sources.toArray(new String[sources.size()]);
            conclusions.add(new Conclusion(namePart, (String) part.get(0).getValue(), confidence, sourceArr));
        }

        return conclusions;
    }

    private static ArrayList<Conclusion> analyseBirthDate(FactBook f) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();

        ArrayList<Fact> createdDates = f.getFactsWithName("Created Account");
        HashSet<String> sources = new HashSet<>();
        Date maxDate = null;

        if (!createdDates.isEmpty()) {
            while (createdDates.size() > 1) {
                Date d1 = (Date) createdDates.get(0).getValue();
                Date d2 = (Date) createdDates.get(1).getValue();
                if (d2.before(d1)) createdDates.remove(0);
                else createdDates.remove(1);
            }
            maxDate = (Date) createdDates.get(0).getValue();
        }

        ArrayList<Fact> birthDates = f.getFactsWithName("Birth Year");

        if (maxDate != null) {
            for (int i = birthDates.size() - 1; i >= 0; i--)
                if (((Date) birthDates.get(i).getValue()).after(maxDate)) birthDates.remove(i);
        }

        if (!birthDates.isEmpty()) {

            ArrayList<Double> initConfidences = new ArrayList<>();
            for (int i = 0; i < birthDates.size(); i++)
                initConfidences.add(getConfidenceFromSource(birthDates.get(i).getSource(), birthDates.get(i).getSubSource()));

            double confidence = initConfidences.get(0);

            sources.add(birthDates.get(0).getSourceString());

            while (birthDates.size() > 1) {
                Date y1 = (Date) birthDates.get(0).getValue();
                Date y2 = (Date) birthDates.get(1).getValue();

                if (y1.equals(y2)) {
                    confidence += ((1 - confidence) * initConfidences.get(1));
                    sources.add(birthDates.get(1).getSourceString());
                    birthDates.remove(1);
                } else {
                    sources.clear();
                    if (confidence >= initConfidences.get(1)) {
                        birthDates.remove(1);
                        confidence *= (1 - initConfidences.get(1));
                    } else {
                        birthDates.remove(0);
                        confidence = initConfidences.get(1) * (1 - confidence);
                    }
                    sources.add(birthDates.get(0).getSourceString());
                }

                initConfidences.remove(1);
            }

            if (maxDate != null) sources.add(createdDates.get(0).getSourceString());

            String[] sourceArr = sources.toArray(new String[sources.size()]);
            Calendar c = Calendar.getInstance();
            c.setTime((Date) birthDates.get(0).getValue());
            String year = Integer.toString(c.get(Calendar.YEAR));
            System.out.println("Birth Year: " + year + "  Confidence: " + confidence);
            conclusions.add(new Conclusion("Birth Year", year, confidence, sourceArr));
        }

        return conclusions;
    }

    private static double getConfidenceFromSource(String source, String subsource) {
        double confidence = 0;

        switch (source) {
            case "Twitter": confidence = 0.8; break;
            case "Facebook": confidence = 0.95; break;
            case "Reddit": confidence = 0.4; break;
        }

        switch (subsource) {
            case "UserProfile": confidence *= 1.0; break;
            case "Tweets": confidence *= 0.7; break;
            case "Followers/Following": confidence *= 0.7; break;
            case "CommentHistory": confidence *= 0.5; break;
            case "PostHistory": confidence *= 0.7; break;
        }

        return confidence;
    }

}

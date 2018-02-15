package com.chrisdmilner.webapp;

import facebook4j.Facebook;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Analyser {

    public static void main(String[] args) {
        FactBook fb = new FactBook();
        fb.addFact(new Fact<>("Name", "chrism", "Twitter", "UserProfile"));
        fb.addFact(new Fact<>("Name", "Christopher David Milner", "Facebook", "UserProfile"));
//        fb.addFact(new Fact<>("Name", "ChrisDMilner97", "Twitter", "UserProfile"));
        fb.addFact(new Fact<>("Name", "Chris Milner", "Twitter", "UserProfile"));
//        fb.addFact(new Fact<>("Name", "Radioactive1997", "Reddit", "UserProfile"));
        fb.addFact(new Fact<>("First Name", "Christopher", "Facebook", "UserProfile"));
        fb.addFact(new Fact<>("Last Name", "Milner", "Facebook", "UserProfile"));
        fb.addFact(new Fact<>("Image URL", "http://example.com/image.png", "Facebook", "Photos"));

        try {
            DateFormat df = new SimpleDateFormat("dd/mm/yyyy");
            fb.addFact(new Fact<>("Max Birth Date", df.parse("17/08/2005"), "Facebook", "UserProfile"));
            fb.addFact(new Fact<>("Min Birth Date", df.parse("17/08/1995"), "Facebook", "UserProfile"));
//            fb.addFact(new Fact<>("Birth Year", df.parse("01/01/1997"), "Facebook", "UserProfile"));
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
        conclusions.addAll(analyseImages(f));

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
                    else fb.addFact(new Fact<>("First Name", removeNonLetters(value), name.getSource(), name.getSubSource()));
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
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = removeNonLetters(tokens[i]);
//            System.out.println(tokens[i]);
        }

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

            // Sorts the facts by their confidence.
            Collections.sort(part, Comparator.comparing(item -> initConfidences.get(part.indexOf(item))));
            Collections.reverse(part);
            initConfidences.sort(Comparator.reverseOrder());

            confidence = initConfidences.get(0);
            sources.add(part.get(0).getSourceString());

            if (part.size() > 1) {
                String val1, val2;
                while (part.size() != 1) {
                    val1 = ((String) part.get(0).getValue()).toLowerCase();
                    val2 = ((String) part.get(1).getValue()).toLowerCase();

                    System.out.println("Comparing " + val1 + " and " + val2);

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

                    System.out.println("Took " + part.get(0).getValue() + " Confidence: " + confidence);

                    if (part.size() > 1) initConfidences.remove(1);
                }
            }

            ArrayList<String> names;
            if (namePart.equals("Last Name"))
                names = Util.readResourceFileLines("data/lastnames.csv");
            else
                names = Util.readResourceFileLines("data/firstnames.csv");

            String candidateName = (String) part.get(0).getValue();
            for (String name : names) {
                if (candidateName.equals(name)) {
                    confidence += ((1 - confidence) * 0.8);
                    break;
                }
            }

            System.out.println(namePart + ": " + part.get(0).getValue() + "  Confidence: " + confidence);
            String[] sourceArr = sources.toArray(new String[sources.size()]);
            conclusions.add(new Conclusion(namePart, candidateName, confidence, sourceArr));
        }

        return conclusions;
    }

    private static String removeNonLetters(String s) {
        return s.replaceAll("[^a-zA-Z]", "");
    }

    private static ArrayList<Conclusion> analyseBirthDate(FactBook f) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();
        HashSet<String> sources = new HashSet<>();

        // Get the max and min birth dates.
        Fact maxFact = getMaxBirthDate(f);
        Fact minFact = getMinBirthDate(f);
        Date maxDate = null;
        Date minDate = null;
        if (maxFact != null) maxDate = (Date) maxFact.getValue();
        if (minFact != null) minDate = (Date) minFact.getValue();

        ArrayList<Fact> birthYears = f.getFactsWithName("Birth Year");

        // Ignore all possible birth years if they occur after the max date or before the min date.
        for (int i = birthYears.size() - 1; i >= 0; i--) {
            if (maxFact != null && ((Date) birthYears.get(i).getValue()).after(maxDate))
                birthYears.remove(i);
            else if (minFact != null && ((Date) birthYears.get(i).getValue()).before(minDate))
                birthYears.remove(i);
        }

        // Combine the remaining birth years to get a final answer.
        if (!birthYears.isEmpty()) {

            ArrayList<Double> initConfidences = new ArrayList<>();
            for (int i = 0; i < birthYears.size(); i++)
                initConfidences.add(getConfidenceFromSource(birthYears.get(i).getSource(), birthYears.get(i).getSubSource()));

            double confidence = initConfidences.get(0);

            sources.add(birthYears.get(0).getSourceString());

            while (birthYears.size() > 1) {
                Calendar y1 = Calendar.getInstance();
                Calendar y2 = Calendar.getInstance();
                y1.setTime((Date) birthYears.get(0).getValue());
                y2.setTime((Date) birthYears.get(1).getValue());

//                System.out.println("Confidence: " + confidence);
//                System.out.println("Comparing " + y1 + " and " + y2);

                if (y1.get(Calendar.YEAR) == y2.get(Calendar.YEAR)) {
                    confidence += ((1 - confidence) * initConfidences.get(1));
                    sources.add(birthYears.get(1).getSourceString());
                    birthYears.remove(1);
                } else {
                    if (confidence >= initConfidences.get(1)) {
                        birthYears.remove(1);
                        confidence *= (1 - initConfidences.get(1));
                    } else {
                        sources.clear();
                        birthYears.remove(0);
                        confidence = initConfidences.get(1) * (1 - confidence);
                    }
                    sources.add(birthYears.get(0).getSourceString());
                }

                initConfidences.remove(1);
            }

            if (maxFact != null) sources.add(maxFact.getSourceString());
            if (minFact != null) sources.add(minFact.getSourceString());

            // Convert Date to String.
            Calendar c = Calendar.getInstance();
            c.setTime((Date) birthYears.get(0).getValue());
            int yearInt = c.get(Calendar.YEAR);
            String year = Integer.toString(yearInt);

            // Get sources as string array.
            String[] sourceArr = sources.toArray(new String[sources.size()]);

            conclusions.add(new Conclusion("Birth Year", year, confidence, sourceArr));
            System.out.println("Birth Year: " + year + "  Confidence: " + confidence);

            c = Calendar.getInstance();
            int maxAge = (c.get(Calendar.YEAR) - yearInt);
            String age = (maxAge - 1) + " - " + maxAge;

            conclusions.add(new Conclusion("Age", age, confidence, sourceArr));
            System.out.println("Age: " + age + "  Confidence: " + confidence);
        } else if (maxFact != null && minFact != null) {
            double confidence = getConfidenceFromSource(maxFact.getSubSource(), maxFact.getSubSource());
            confidence *= getConfidenceFromSource(minFact.getSource(), minFact.getSubSource());
            sources.add(maxFact.getSourceString());
            sources.add(minFact.getSourceString());
            String[] sourceArr = sources.toArray(new String[sources.size()]);

            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            c1.setTime(minDate);
            c2.setTime(maxDate);
            int y1 = c1.get(Calendar.YEAR);
            int y2 = c2.get(Calendar.YEAR);

            String year = (y1 - 1) + " - " + y2;

            conclusions.add(new Conclusion("Birth Year", year, confidence, sourceArr));
            System.out.println("Birth Year: " + year + "  Confidence: " + confidence);

            Calendar c = Calendar.getInstance();
            int currYear = c.get(Calendar.YEAR);
            String age = (currYear - y2) + " - " + (currYear - y1);

            conclusions.add(new Conclusion("Age", age, confidence, sourceArr));
            System.out.println("Age: " + age + "  Confidence: " + confidence);
        }

        return conclusions;
    }

    private static Fact getMaxBirthDate(FactBook f) {
        // Get the latest possible dates that could be their birth date.
        ArrayList<Fact> maxBDays = f.getFactsWithName("Max Birth Date");

        // Get the earliest of the max dates.
        Fact maxFact = null;
        if (!maxBDays.isEmpty()) {
            while (maxBDays.size() > 1) {
                Date d1 = (Date) maxBDays.get(0).getValue();
                Date d2 = (Date) maxBDays.get(1).getValue();
                if (d2.before(d1)) maxBDays.remove(0);
                else maxBDays.remove(1);
            }
            maxFact = maxBDays.get(0);
        }

        return maxFact;
    }

    private static Fact getMinBirthDate(FactBook f) {
        // Get the earliest possible dates that could be their birth date.
        ArrayList<Fact> minBDays = f.getFactsWithName("Min Birth Date");

        // Get the latest of the min dates.
        Fact minFact = null;
        if (!minBDays.isEmpty()) {
            while (minBDays.size() > 1) {
                Date d1 = (Date) minBDays.get(0).getValue();
                Date d2 = (Date) minBDays.get(1).getValue();
                if (d2.after(d1)) minBDays.remove(0);
                else minBDays.remove(1);
            }
            minFact = minBDays.get(0);
        }

        return minFact;
    }

    private static ArrayList<Conclusion> analyseImages(FactBook f) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();

        ArrayList<Fact> imageURLs = f.getFactsWithName("Image URL");

        for (Fact url : imageURLs) {
            double confidence =  getConfidenceFromSource(url.getSource(), url.getSubSource());
            String[] sources = new String[] { url.getSourceString() };
            conclusions.add(new Conclusion("Image URL", (String) url.getValue(), confidence, sources));
        }

        return  conclusions;
    }

    private static double getConfidenceFromSource(String source, String subsource) {
        double confidence = 0;

        switch (source) {
            case "Twitter": confidence = 0.6; break;
            case "Facebook": confidence = 0.95; break;
            case "Reddit": confidence = 0.2; break;
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

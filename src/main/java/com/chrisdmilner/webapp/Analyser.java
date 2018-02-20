package com.chrisdmilner.webapp;

import java.lang.reflect.Array;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

public class Analyser {

    // TODO:
    //  - Education
    //  - Text Posts
    //  - Locations
    //  - Email(s)
    //  - Gender
    //  - Interests
    //  - Politics
    //  - Religion
    //  - Sexuality
    //  - Partner
    //  - Jobs
    //  - Lanuages
    //  - Friends

    private final static int CURRENT_YEAR = 2018;

    public static ArrayList<Conclusion> analyse(FactBook f) {
        System.out.println("\n - STARTING ANALYSER - \n");

        ArrayList<Conclusion> conclusions = new ArrayList<>();

        System.out.println("   Breaking down Name Parts");
        analyseName(f);

        System.out.println("   Analysing the Name Parts");
        conclusions.addAll(analyseNameParts(f));

        System.out.println("   Analysing Birth Dates");
        conclusions.addAll(analyseBirthDate(f));

        System.out.println("   Analysing Images");
        conclusions.addAll(analyseImages(f));

        System.out.println("\n - ANALYSER FINISHED - \n");

        return conclusions;
    }

    private static void analyseName(FactBook fb) {
        ArrayList<Fact> names = fb.getFactsWithName("Name");

        for (Fact name : names) {
            String[] tokens = tokeniseName(name);
            addNameTokens(fb, tokens, name);
            findBirthYear(fb, name);
        }
    }

    private static String[] tokeniseName(Fact<String> name) {
        String value = (String) name.getValue();
        ArrayList<String> tokens = new ArrayList<>();

        // Divide by spaces
        if (value.contains(" ")) {
            tokens.addAll(Arrays.asList(value.split("\\s+")));
        } else {
            // Divide by punctuation
            if (value.split("[_.-]").length > 1) {
                tokens.addAll(Arrays.asList(value.split("[_.-]")));
            } else {
                // Divide by capitals
                tokens.addAll(Arrays.asList(value.split("(?=\\p{Upper})")));
            }
        }

        for (String token : tokens) {
            if (token.equals("")) tokens.remove(token);
        }

        return tokens.toArray(new String[tokens.size()]);
    }

    private static void findBirthYear(FactBook fb, Fact<String> name) {
        String[] nums = name.getValue().split("[^0-9]");
        DateFormat df = new SimpleDateFormat("yyyy");
        for (String num : nums) {
            if (num.length() == 0) continue;

            int intnum = Integer.parseInt(num);
            try {
                if (num.length() == 2) {
                    if (intnum > (CURRENT_YEAR - 2000))
                        fb.addFact(new Fact<>("Birth Year", df.parse("19" + num), name));
                    else
                        fb.addFact(new Fact<>("Birth Year", df.parse("20" + num), name));
                } else if (num.length() == 4 && intnum > 1900 && intnum <= CURRENT_YEAR)
                    fb.addFact(new Fact<>("Birth Year", df.parse(num), name));
            } catch (ParseException e) {
                System.err.println("ERROR converting date to correct format: yyyy");
                e.printStackTrace();
            }
        }
    }

    private static void addNameTokens(FactBook fb, String[] tokens, Fact source) {
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = removeNonLetters(tokens[i]);
        }

        fb.addFact(new Fact<>("First Name", tokens[0], source));

        if (tokens.length == 1) return;

        fb.addFact(new Fact<>("Last Name", tokens[tokens.length - 1], source));

        if (tokens.length > 2) {
            for (int i = 1; i < tokens.length - 1; i++)
                fb.addFact(new Fact<>("Middle Name", tokens[i], source));
        }
    }

    private static ArrayList<Conclusion> analyseNameParts(FactBook f) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();

        String[] nameParts = {"First Name", "Middle Name", "Last Name"};

        Conclusion c;
        for (String namePart : nameParts) {
            c = analyseNamePart(f, namePart);
            if (c != null) conclusions.add(c);
        }

        return conclusions;
    }

    private static Conclusion analyseNamePart(FactBook f, String namePart) {
        System.out.println("\nAnalysing " + namePart);

        // Get the facts for the given name part.
        ArrayList<Fact> names = f.getFactsWithName(namePart);
        if (names.isEmpty()) return null;

        // Add the initial source fact to the first conclusion candidate.
        ArrayList<ArrayList> sources = new ArrayList<>();
        sources.add(new ArrayList<Fact>());
        sources.get(0).add(names.get(0));

        int i = 1;
        boolean similar;
        while (i < names.size()) {
            System.out.println("Comparing new name " + names.get(i).getValue());
            similar = false;
            for (int j = 0; j < i; j++) {
                System.out.println("Comparing with " + names.get(j).getValue());

                String name1 = (String) names.get(i).getValue();
                String name2 = (String) names.get(j).getValue();

                if (name1.equals(name2)) {
                    sources.get(j).add(names.get(i));
                    names.remove(i);
                    similar = true;
                    break;
                } else if (name1.contains(name2)) {
                    similar = true;
                    boolean decision = decideBetweenNames(namePart, name1, name2);
                    if (decision) {
                        sources.remove(j);
                        sources.add(new ArrayList<Fact>());
                        sources.get(i-1).add(names.get(i));
                        names.remove(j);
                    } else {
                        sources.get(j).add(names.get(i));
                        names.remove(i);
                    }
                    break;
                } else if (name2.contains(name1)) {
                    similar = true;
                    boolean decision = decideBetweenNames(namePart, name2, name1);
                    if (decision) {
                        sources.get(j).add(names.get(i));
                        names.remove(i);
                    } else {
                        sources.remove(j);
                        sources.add(new ArrayList<Fact>());
                        sources.get(i-1).add(names.get(i));
                        names.remove(j);
                    }
                    break;
                }
            }

            if (!similar) {
                sources.add(new ArrayList<Fact>());
                sources.get(i).add(names.get(i));
                i++;
            }
        }

        double[] confidences = new double[names.size()];
        double conf;
        double totalConf = 0;
        for (int j = 0; j < confidences.length; j++) {
            conf = 0;
            for (Fact fact : (ArrayList<Fact>) sources.get(j)) {
                conf += getConfidenceFromSource(fact);
            }
            totalConf += conf;
            confidences[j] = conf;
        }

        // Divide a confidence of 1 between all the options.
        double greatestConf = 0;
        int greatestIndex = -1;
        for (int j = 0; j < confidences.length; j++) {
            confidences[j] = confidences[j] / totalConf;
            if (confidences[j] > greatestConf) {
                greatestConf = confidences[j];
                greatestIndex = j;
            }
        }

        return new Conclusion(namePart, (String) names.get(greatestIndex).getValue(), greatestConf, sources.get(greatestIndex));
    }

    private static boolean decideBetweenNames(String namePart, String n1, String n2) {
        boolean n1Recognised = isNameRecognised(namePart, n1);
        boolean n2Recognised = isNameRecognised(namePart, n2);

        // If both are recognisable or both unrecognisable then take the first one (the larger).
        if ((!n1Recognised && !n2Recognised) || (n1Recognised && n2Recognised)) return true;
        // If the smaller is the only recognisable one take the smaller. Otherwise take the larger.
        else return n1Recognised;
    }

    private static boolean isNameRecognised(String namePart, String potentialName) {
        ArrayList<String> names;
        if (namePart.equals("Last Name"))
            names = Util.readResourceFileLines("data/lastnames.csv");
        else
            names = Util.readResourceFileLines("data/firstnames.csv");

        for (String name : names) {
            if (potentialName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    private static String removeNonLetters(String s) {
        return s.replaceAll("[^a-zA-Z]", "");
    }

    private static ArrayList<Conclusion> analyseBirthDate(FactBook f) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();
        ArrayList<Fact> sources = new ArrayList<>();

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
                initConfidences.add(getConfidenceFromSource(birthYears.get(i).getSource()));

            double confidence = initConfidences.get(0);

            sources.add(birthYears.get(0));

            while (birthYears.size() > 1) {
                Calendar y1 = Calendar.getInstance();
                Calendar y2 = Calendar.getInstance();
                y1.setTime((Date) birthYears.get(0).getValue());
                y2.setTime((Date) birthYears.get(1).getValue());

//                System.out.println("Confidence: " + confidence);
//                System.out.println("Comparing " + y1 + " and " + y2);

                if (y1.get(Calendar.YEAR) == y2.get(Calendar.YEAR)) {
                    confidence += ((1 - confidence) * initConfidences.get(1));
                    sources.add(birthYears.get(1));
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
                    sources.add(birthYears.get(0));
                }

                initConfidences.remove(1);
            }

            if (maxFact != null) sources.add(maxFact);
            if (minFact != null) sources.add(minFact);

            // Convert Date to String.
            Calendar c = Calendar.getInstance();
            c.setTime((Date) birthYears.get(0).getValue());
            int yearInt = c.get(Calendar.YEAR);
            String year = Integer.toString(yearInt);

            conclusions.add(new Conclusion("Birth Year", year, confidence, sources));
//            System.out.println("   Birth Year: " + year + "  Confidence: " + confidence);

            c = Calendar.getInstance();
            int maxAge = (c.get(Calendar.YEAR) - yearInt);
            String age = (maxAge - 1) + " - " + maxAge;

            conclusions.add(new Conclusion("Age", age, confidence, sources));
//            System.out.println("   Age: " + age + "  Confidence: " + confidence);
        } else if (maxFact != null && minFact != null) {
            double confidence = getConfidenceFromSource(maxFact.getSource());
            confidence *= getConfidenceFromSource(minFact.getSource());

            sources.add(maxFact);
            sources.add(minFact);

            Calendar c1 = Calendar.getInstance();
            Calendar c2 = Calendar.getInstance();
            c1.setTime(minDate);
            c2.setTime(maxDate);
            int y1 = c1.get(Calendar.YEAR);
            int y2 = c2.get(Calendar.YEAR);

            String year = (y1 - 1) + " - " + y2;

            conclusions.add(new Conclusion("Birth Year", year, confidence, sources));
//            System.out.println("   Birth Year: " + year + "  Confidence: " + confidence);

            Calendar c = Calendar.getInstance();
            int currYear = c.get(Calendar.YEAR);
            String age = (currYear - y2) + " - " + (currYear - y1);

            conclusions.add(new Conclusion("Age", age, confidence, sources));
//            System.out.println("   Age: " + age + "  Confidence: " + confidence);
        } else if (maxFact != null) {
            Calendar c1 = Calendar.getInstance();
            int currYear = c1.get(Calendar.YEAR);

            Calendar c2 = Calendar.getInstance();
            c2.setTime(maxDate);
            int maxYear = c2.get(Calendar.YEAR);

            int year = currYear - maxYear;
            String age = year + " or Over";

            double confidence = getConfidenceFromSource(maxFact.getSource());

            sources.add(maxFact);
            conclusions.add(new Conclusion("Age", age, confidence, sources));
//            System.out.println("   Age: " + age + "  Confidence: " + confidence);
        } else if (minFact != null) {
            Calendar c1 = Calendar.getInstance();
            int currYear = c1.get(Calendar.YEAR);

            Calendar c2 = Calendar.getInstance();
            c2.setTime(minDate);
            int maxYear = c2.get(Calendar.YEAR);

            int year = currYear - maxYear;
            String age = year + " or Under";

            double confidence = getConfidenceFromSource(minFact.getSource());

            sources.add(minFact);
            conclusions.add(new Conclusion("Age", age, confidence, sources));
//            System.out.println("   Age: " + age + "  Confidence: " + confidence);
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
            double confidence =  getConfidenceFromSource(url.getSource());
            ArrayList<Fact> sources = new ArrayList<>();
            sources.add(url);
            conclusions.add(new Conclusion("Image URL", (String) url.getValue(), confidence, sources));
        }

        return  conclusions;
    }

    private static double getConfidenceFromSource(Fact source) {
        double confidence = 1;

        // TODO: Add all possible fact names to switch statement.

        Fact s = source;
        while (s != null) {
            switch (s.getName()) {
                case "Facebook User ID":    confidence *= 0.95; break;
                case "Twitter Handle":      confidence *= 0.7; break;
                case "Reddit User Name":    confidence *= 0.4; break;

                default: confidence *= 1;
            }

            s = s.getSource();
        }

        return confidence;
    }

}

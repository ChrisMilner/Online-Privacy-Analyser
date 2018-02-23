package com.chrisdmilner.webapp;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

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

        System.out.println("   Analysing Gender");
        conclusions.addAll(analyseGender(f));

        System.out.println("\n - ANALYSER FINISHED - \n");

        return conclusions;
    }

    protected static void analyseName(FactBook fb) {
        ArrayList<Fact> names = fb.getFactsWithName("Name");

        for (Fact<String> name : names) {
            String[] tokens = tokeniseName(name);
            addNameTokens(fb, tokens, name);
            findBirthYear(fb, name);
        }
    }

    private static String[] tokeniseName(Fact<String> name) {
        String value = Util.removeNumbers(name.getValue());
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
            if (token.isEmpty() || token.equals(" ")) tokens.remove(token);
        }

        return tokens.toArray(new String[tokens.size()]);
    }

    private static void addNameTokens(FactBook fb, String[] tokens, Fact source) {
        // TODO: Handle title e.g. Mr

        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = Util.removeNonLetters(tokens[i]);
        }

        fb.addFact(new Fact<>("First Name", tokens[0], source));

        if (tokens.length == 1) return;

        fb.addFact(new Fact<>("Last Name", tokens[tokens.length - 1], source));

        if (tokens.length > 2) {
            for (int i = 1; i < tokens.length - 1; i++)
                fb.addFact(new Fact<>("Middle Name", tokens[i], source));
        }
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

    protected static ArrayList<Conclusion> analyseNameParts(FactBook f) {
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
        // TODO: Handle multiple middle names.

        // Get the facts for the given name part.
        ArrayList<Fact> names = f.getFactsWithName(namePart);
        if (names.isEmpty()) return null;

        ArrayList<Conclusion> conclusions = factsToConclusions(names);
        combineEqualConclusions(conclusions);
        combineContainedNameConclusions(conclusions);
        return getHighestConfidenceConclusion(conclusions);
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

    protected static ArrayList<Conclusion> analyseBirthDate(FactBook f) {
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
        birthYears = removeDatesOutsideRange(birthYears, minDate, maxDate);

        if (maxFact != null) sources.add(maxFact);
        if (minFact != null) sources.add(minFact);

        // Combine the remaining birth years to get a final answer.
        if (!birthYears.isEmpty()) {
            ArrayList<Conclusion> cs = factsToConclusions(birthYears);
            combineEqualConclusions(cs);
            Conclusion bestYear = getHighestConfidenceConclusion(cs);
            sources.addAll(bestYear.getSources());

            int yearInt = Util.getYearFromDate((Date) bestYear.getValue());
            String year = Integer.toString(yearInt);
            conclusions.add(new Conclusion<>("Birth Year", year, bestYear.getConfidence(), sources));

            int maxAge = Util.getCurrentYear() - yearInt;
            String age = (maxAge - 1) + " - " + maxAge;
            conclusions.add(new Conclusion<>("Age", age, bestYear.getConfidence(), sources));
        } else if (maxFact != null && minFact != null) {
            double confidence = getConfidenceFromSource(maxFact.getSource());
            confidence *= getConfidenceFromSource(minFact.getSource());

            int y1 = Util.getYearFromDate(minDate);
            int y2 = Util.getYearFromDate(maxDate);
            String year = y1 + " - " + y2;
            conclusions.add(new Conclusion<>("Birth Year", year, confidence, sources));

            int currYear = Util.getCurrentYear();
            String age = (currYear - y2) + " - " + (currYear - y1);
            conclusions.add(new Conclusion<>("Age", age, confidence, sources));
        } else if (maxFact != null) {
            int currYear = Util.getCurrentYear();
            int maxYear = Util.getYearFromDate(maxDate);

            int minAge = currYear - maxYear;
            String age = minAge + " or Over";
            double confidence = getConfidenceFromSource(maxFact.getSource());
            conclusions.add(new Conclusion<>("Age", age, confidence, sources));
        } else if (minFact != null) {
            int currYear = Util.getCurrentYear();
            int maxYear = Util.getYearFromDate(minDate);

            int maxAge = currYear - maxYear;
            String age = maxAge + " or Under";
            double confidence = getConfidenceFromSource(minFact.getSource());
            conclusions.add(new Conclusion<>("Age", age, confidence, sources));
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

    private static ArrayList<Fact> removeDatesOutsideRange(ArrayList<Fact> dates, Date min, Date max) {
        Date d;
        for (int i = dates.size() - 1; i >= 0; i--) {
            d = (Date) dates.get(i).getValue();

            if (max != null && d.after(max))
                dates.remove(i);
            else if (min != null && d.before(min))
                dates.remove(i);
        }
        return dates;
    }

    private static ArrayList<Conclusion> analyseImages(FactBook f) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();

        ArrayList<Fact> imageURLs = f.getFactsWithName("Image URL");

        for (Fact url : imageURLs) {
            double confidence =  getConfidenceFromSource(url.getSource());
            ArrayList<Fact> sources = new ArrayList<>();
            sources.add(url);
            conclusions.add(new Conclusion<>("Image URL", (String) url.getValue(), confidence, sources));
        }

        return  conclusions;
    }

    private static ArrayList<Conclusion> analyseGender(FactBook f) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();

        ArrayList<Fact> sources = new ArrayList<>();
        ArrayList<Fact> genders = f.getFactsWithName("Gender");

        if (genders.isEmpty()) return conclusions;

        for (int i = 0; i < genders.size(); i++) {
            for (int j = 0; j < i; j++) {
                String g1 = (String) genders.get(i).getValue();
                String g2 = (String) genders.get(j).getValue();
                if (g1.equals(g2) || g1.charAt(0) == g2.charAt(0)) {
                    if (g1.length() > g2.length()) {
                        genders.remove(j);
                    } else {
                        genders.remove(i);
                    }
                    i--;
                }
            }
        }

        double confidence = 1;
        String gender = Util.uppercaseFirstLetter((String) genders.get(0).getValue());
        conclusions.add(new Conclusion<>("Gender", gender, confidence, sources));
        return conclusions;
    }

    private static ArrayList<Conclusion> factsToConclusions(ArrayList<Fact> facts) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();

        // Convert facts to conclusions, set initial confidences and get the total confidence.
        double total = 0;
        double incorrectProb = 1.0;
        for (Fact f : facts) {
            ArrayList<Fact> sources = new ArrayList<>();
            sources.add(f);
            double confidence = getConfidenceFromSource(f.getSource());
            total += confidence;
            incorrectProb *= (1.0 - confidence);
            conclusions.add(new Conclusion<>(f.getName(), f.getValue(), confidence, sources));
        }

        // Normalise the confidences.
        for (Conclusion c : conclusions) {
            c.setConfidence((c.getConfidence() / total) * (1 - incorrectProb));
        }

        return conclusions;
    }

    private static void combineEqualConclusions(ArrayList<Conclusion> conclusions) {
        // Compare all conclusions and combine the duplicates.
        for (int i = 1; i < conclusions.size(); i++) {
            for (int j = 0; j < i; j++) {
                Object s1 = conclusions.get(i).getValue();
                Object s2 = conclusions.get(j).getValue();
                if (s1.equals(s2)) {
                    conclusions.get(j).addSources(conclusions.get(i).getSources());
                    conclusions.get(j).setConfidence(conclusions.get(j).getConfidence() + conclusions.get(i).getConfidence());
                    conclusions.remove(i--);
                }
            }
        }
    }

    private static void combineContainedNameConclusions(ArrayList<Conclusion> conclusions) {
        for (int i = 1; i < conclusions.size(); i++) {
            for (int j = 0; j < i; j++) {
                String s1 = (String) conclusions.get(i).getValue();
                String s2 = (String) conclusions.get(j).getValue();
                boolean decision = false;
                boolean contains = false;
                if (s1.contains(s2)) {
                    contains = true;
                    decision = decideBetweenNames(conclusions.get(i).getName(), s1, s2);
                } else if (s2.contains(s1)) {
                    contains = true;
                    decision = decideBetweenNames(conclusions.get(i).getName(), s2, s1);
                }

                if (contains) {
                    if (decision) {
                        conclusions.get(i).addSources(conclusions.get(j).getSources());
                        conclusions.get(i).setConfidence(conclusions.get(i).getConfidence() + conclusions.get(j).getConfidence());
                        conclusions.remove(j);
                    } else {
                        conclusions.get(j).addSources(conclusions.get(i).getSources());
                        conclusions.get(j).setConfidence(conclusions.get(j).getConfidence() + conclusions.get(i).getConfidence());
                        conclusions.remove(i);
                    }
                    i--; j--;
                }
            }
        }
    }

    private static boolean decideBetweenNames(String namePart, String n1, String n2) {
        boolean n1Recognised = isNameRecognised(namePart, n1);
        boolean n2Recognised = isNameRecognised(namePart, n2);

        // If both are recognisable or both unrecognisable then take the first one (the larger).
        if ((!n1Recognised && !n2Recognised) || (n1Recognised && n2Recognised)) return true;
        // If the smaller is the only recognisable one take the smaller. Otherwise take the larger.
        else return n1Recognised;
    }

    private static Conclusion getHighestConfidenceConclusion(ArrayList<Conclusion> conclusions) {
        if (conclusions.isEmpty()) return null;

        double highestConfidence = 0;
        int index = -1;
        for (int i = 0; i < conclusions.size(); i++) {
            if (conclusions.get(i).getConfidence() > highestConfidence) {
                highestConfidence = conclusions.get(i).getConfidence();
                index = i;
            }
        }

        return conclusions.get(index);
    }

    private static double getConfidenceFromSource(Fact source) {
        double confidence = 1;

        // TODO: Add all possible fact names to switch statement.

        Fact s = source;
        while (s != null) {
            switch (s.getName()) {
                case "Facebook Account":    confidence *= 0.95; break;
                case "Twitter Account":      confidence *= 0.7; break;
                case "Reddit Account":    confidence *= 0.4; break;

                default: confidence *= 1;
            }

            s = s.getSource();
        }

        return confidence;
    }

}

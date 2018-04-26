package com.chrisdmilner.webapp;

import java.io.FileNotFoundException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/*
 * Analyser Class
 *
 * Contains functions for analysing a list of facts and converting it to a list of conclusions.
 *
 * */
public class Analyser {

    private static int CURRENT_YEAR;
    private static double FB_ROOT_CONF;
    private static double TW_ROOT_CONF;
    private static double RD_ROOT_CONF;

    // The root function of the class which calls all of the other functions. Analyses a Factbook (list of facts) and
    // returns a list of conclusions.
    public static ArrayList<Conclusion> analyse(FactBook f) {

        // Intialise the analyser.
        System.out.println("\n - STARTING ANALYSER - \n");
        setRootConfidences();
        CURRENT_YEAR = Util.getCurrentYear();

        System.out.println("   Analysing Posts");
        ArrayList<Conclusion> conclusions = new ArrayList<>(TextAnalyser.analyse(f));

        System.out.println("   Breaking down Name Parts");
        analyseName(f);

        System.out.println("   Analysing the Name Parts");
        conclusions.addAll(analyseNameParts(f));

        System.out.println("   Analysing Account Creation Dates");
        analyseCreatedAtDates(f);

        System.out.println("   Analysing Gender");
        Conclusion gender = decideBetweenFacts(f, "Gender");
        if (gender != null) conclusions.add(gender);

        System.out.println("   Analysing Birth Dates");
        conclusions.addAll(analyseBirthYear(f));
        Conclusion month = analyseBirthMonth(f);
        if (month != null) conclusions.add(month);
        Conclusion day = analyseBirthDay(f);
        if (day != null) conclusions.add(day);

        System.out.println("   Analysing Emails");
        conclusions.addAll(getFactsAsConclusions(f,"Email"));

        System.out.println("   Analysing Relationships");
        Conclusion relationship = decideBetweenFacts(f, "Relationship Status");
        if (relationship != null) conclusions.add(relationship);

        System.out.println("   Analysing Religion");
        Conclusion religion = analyseReligionPolitics(f, "Religion");
        if (religion != null) conclusions.add(religion);

        System.out.println("   Analysing Politics");
        Conclusion politics = analyseReligionPolitics(f, "Politics");
        if (politics != null) conclusions.add(politics);

        System.out.println("   Analysing Work and Education");
        conclusions.addAll(analyseWorkEducation(f));

        System.out.println("   Analysing Locations");
        conclusions.addAll(getFactsAsConclusions(f, "Location"));

        System.out.println("   Analysing Images");
        conclusions.addAll(getFactsAsConclusions(f,"Image URL"));

        System.out.println("\n - ANALYSER FINISHED - \n");

        return conclusions;
    }

    // Breaks full names down into their separate parts and look for birth year information.
    protected static void analyseName(FactBook fb) {
        // Get all of the full name facts.
        ArrayList<Fact> names = fb.getFactsWithName("Name");

        // Break down each full name and add the name parts as new facts.
        for (Fact<String> name : names) {
            String[] tokens = tokeniseName(name);
            addNameTokens(fb, tokens, name);

            // Look for numbers which could be the user's birth year.
            findBirthYear(fb, name);
        }
    }

    // Break down a full name into its constituent, first, middle and last names.
    private static String[] tokeniseName(Fact<String> name) {
        // Get rid of any numbers in the name.
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

        // Remove any empty tokens.
        for (String token : tokens) {
            if (token.isEmpty() || token.equals(" ")) tokens.remove(token);
        }

        return tokens.toArray(new String[tokens.size()]);
    }

    // Takes a list of token parts of a name and adds them as separate facts.
    private static void addNameTokens(FactBook fb, String[] tokens, Fact source) {
        // Remove anything that isn't letters from all of the tokens.
        for (int i = 0; i < tokens.length; i++) {
            tokens[i] = Util.removeNonLetters(tokens[i]);
        }

        // Add the first token as the first name.
        fb.addFact(new Fact<>("First Name", tokens[0], source));

        if (tokens.length == 1) return;

        // Add the last token as a last name.
        fb.addFact(new Fact<>("Last Name", tokens[tokens.length - 1], source));

        // Add any tokens in between as middle names.
        if (tokens.length > 2) {
            for (int i = 1; i < tokens.length - 1; i++)
                fb.addFact(new Fact<>("Middle Name", tokens[i], source));
        }
    }

    // Look for a birth year in a full name and add it as a fact if it exists.
    private static void findBirthYear(FactBook fb, Fact<String> name) {
        // Get all the number parts of the name.
        String[] nums = name.getValue().split("[^0-9]");

        DateFormat df = new SimpleDateFormat("yyyy");
        for (String num : nums) {
            // Ignore empty tokens.
            if (num.length() == 0) continue;

            // Get the number as an integer.
            int intnum = Integer.parseInt(num);
            try {
                // If the number is two digits then treat it as a last two digits of a year e.g. 19(97).
                if (num.length() == 2) {
                    // If the number is greater than the current year then add 19 in front otherwise add 20.
                    if (intnum > (CURRENT_YEAR - 2000))
                        fb.addFact(new Fact<>("Birth Year", df.parse("19" + num), name));
                    else
                        fb.addFact(new Fact<>("Birth Year", df.parse("20" + num), name));
                } else if (num.length() == 4 && intnum > 1900 && intnum <= CURRENT_YEAR) {
                    fb.addFact(new Fact<>("Birth Year", df.parse(num), name));
                }
            } catch (ParseException e) {
                System.err.println("ERROR converting date to correct format: yyyy");
                e.printStackTrace();
            }
        }
    }

    // Converts account creation dates to dates that the person must have been born before.
    private static void analyseCreatedAtDates(FactBook f) {
        ArrayList<Fact> dates = f.getFactsWithName("Account Created Date");

        for (Fact date : dates) {
            f.addFact(new Fact<>("Max Birth Date", date.getValue(), date));
        }
    }

    // For all of the different name parts it calls the analyse function on them.
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

    // Analyses a set of facts about a given name part e.g. first name.
    private static Conclusion analyseNamePart(FactBook f, String namePart) {
        // Get the facts for the given name part.
        ArrayList<Fact> names = f.getFactsWithName(namePart);
        if (names.isEmpty()) return null;

        // Combine equal names or names that are a substring of another name. Then get the remaining name with the
        // greatest confidence.
        ArrayList<Conclusion> conclusions = factsToConclusions(names);
        combineEqualConclusions(conclusions);
        combineContainedNameConclusions(conclusions);
        return getHighestConfidenceConclusion(conclusions);
    }

    // Checks a name against a list of possible names in the same category.
    private static boolean isNameRecognised(String namePart, String potentialName) {
        // Gets the relevant name list.
        ArrayList<String> names;
        if (namePart.equals("Last Name"))
            names = Util.readResourceFileLines("data/lastnames.csv");
        else
            names = Util.readResourceFileLines("data/firstnames.csv");

        // Checks if the given name appears in the list.
        for (String name : names) {
            if (potentialName.equals(name)) {
                return true;
            }
        }
        return false;
    }

    // Filters the facts relating to the user's brith date down to most specific value or range we can get.
    protected static ArrayList<Conclusion> analyseBirthYear(FactBook f) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();
        ArrayList<Fact> sources = new ArrayList<>();

        // Get the max and min birth dates.
        Fact maxFact = getMaxBirthDate(f);
        Fact minFact = getMinBirthDate(f);
        Date maxDate = null;
        Date minDate = null;
        if (maxFact != null) maxDate = (Date) maxFact.getValue();
        if (minFact != null) minDate = (Date) minFact.getValue();

        // Get the birth year facts and remove dates outside of the min-max range.
        ArrayList<Fact> birthYears = f.getFactsWithName("Birth Year");
        birthYears = removeDatesOutsideRange(birthYears, minDate, maxDate);

        // Add the min and max facts as sources.
        if (maxFact != null) sources.add(maxFact);
        if (minFact != null) sources.add(minFact);

        // Combine the remaining birth years to get a final answer.
        if (!birthYears.isEmpty()) {
            // Combine all of the potential birth year, then choose the year with the highest confidence.
            ArrayList<Conclusion> cs = factsToConclusions(birthYears);
            combineEqualConclusions(cs);
            Conclusion bestYear = getHighestConfidenceConclusion(cs);
            sources.addAll(bestYear.getSources());

            // Get that year as a string and add it as a conclusion.
            int yearInt = Util.getYearFromDate((Date) bestYear.getValue());
            String year = Integer.toString(yearInt);
            conclusions.add(new Conclusion<>("Birth Year", year, bestYear.getConfidence(), sources));

            // Use the year to get an age range and add that as a conclusion.
            int maxAge = Util.getCurrentYear() - yearInt;
            String age = (maxAge - 1) + " - " + maxAge;
            conclusions.add(new Conclusion<>("Age", age, bestYear.getConfidence(), sources));
        } else if (maxFact != null && minFact != null) {
            // Combine the confidences of the min and max date facts.
            double confidence = getConfidenceFromSource(maxFact.getSource());
            confidence *= getConfidenceFromSource(minFact.getSource());

            // Add the year and age range as a string and add them as conclusions.
            int y1 = Util.getYearFromDate(minDate);
            int y2 = Util.getYearFromDate(maxDate);
            String year = y1 + " - " + y2;
            conclusions.add(new Conclusion<>("Birth Year", year, confidence, sources));

            int currYear = Util.getCurrentYear();
            String age = (currYear - y2) + " - " + (currYear - y1);
            conclusions.add(new Conclusion<>("Age", age, confidence, sources));
        } else if (maxFact != null) {
            // Get the max year and use that to get a min age and add that as a conclusion.
            int currYear = Util.getCurrentYear();
            int maxYear = Util.getYearFromDate(maxDate);

            int minAge = currYear - maxYear;
            String age = minAge + " or Over";
            double confidence = getConfidenceFromSource(maxFact.getSource());
            conclusions.add(new Conclusion<>("Age", age, confidence, sources));
        } else if (minFact != null) {
            // Get the min year and use that to get a max age and add that as a conclusion.
            int currYear = Util.getCurrentYear();
            int maxYear = Util.getYearFromDate(minDate);

            int maxAge = currYear - maxYear;
            String age = maxAge + " or Under";
            double confidence = getConfidenceFromSource(minFact.getSource());
            conclusions.add(new Conclusion<>("Age", age, confidence, sources));
        }

        return conclusions;
    }

    // Gets the oldest max birth date e.g. the most restrictive value.
    private static Fact getMaxBirthDate(FactBook f) {
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

    // Gets the most recent min birth date e.g. the most restrictive value.
    private static Fact getMinBirthDate(FactBook f) {
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

    // Removes dates from a list where they are outside of the min and max values.
    private static ArrayList<Fact> removeDatesOutsideRange(ArrayList<Fact> dates, Date min, Date max) {
        // Check each date to see if it is outside the range.
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

    // Formats all of the months into a standard form then uses the decide function to get a final conclusion.
    private static Conclusion analyseBirthMonth(FactBook f) {
        ArrayList<Fact> months = f.getFactsWithName("Birth Month");
        ArrayList<Fact> correctMonths = new ArrayList<>();

        // Convert each month to the standardised form and remove nulls.
        for (int i = 0; i < months.size(); i++) {
            String month = getMonthName((String) months.get(i).getValue());
            if (month != null)
                correctMonths.add(new Fact<>("Birth Month", month, months.get(i)));
        }

        return decideBetweenFacts(correctMonths);
    }

    // Converts the different forms of a month into a standard form.
    private static String getMonthName(String month) {
        int num;

        // If the month contains numbers then treat it as a number month.
        if (month.matches(".*\\d+.*")) {
            num = Integer.parseInt(month);
            if (num < 1 || num > 12) return null;
        } else {
            // Convert text months to numbers.
            month = month.toLowerCase();
            if (month.contains("jan"))
                num = 1;
            else if (month.contains("feb"))
                num = 2;
            else if (month.contains("mar"))
                num = 3;
            else if (month.contains("apr"))
                num = 4;
            else if (month.contains("may"))
                num = 5;
            else if (month.contains("jun"))
                num = 6;
            else if (month.contains("jul"))
                num = 7;
            else if (month.contains("aug"))
                num = 8;
            else if (month.contains("sep"))
                num = 9;
            else if (month.contains("oct"))
                num = 10;
            else if (month.contains("nov"))
                num = 11;
            else if (month.contains("dec"))
                num = 12;
            else return null;
        }

        // Convert the number version of the month back to a full month.
        switch (num) {
            case 1: return "January";
            case 2: return "February";
            case 3: return "March";
            case 4: return "April";
            case 5: return "May";
            case 6: return "June";
            case 7: return "July";
            case 8: return "August";
            case 9: return "September";
            case 10: return "October";
            case 11: return "November";
            case 12: return "December";
        }

        return null;
    }

    // Combines the birth day facts into a single conclusion.
    private static Conclusion analyseBirthDay(FactBook f) {
        ArrayList<Fact> days = f.getFactsWithName("Birth Day");

        // Parses each of the day numbers and removes any outside of the 1-31 range.
        for (int i = 0; i < days.size(); i++) {
            int num = Integer.parseInt((String) days.get(i).getValue());
            if (num < 1 || num > 31) days.remove(i);
        }

        // Decide on the final conclusion.
        return decideBetweenFacts(days);
    }

    // Combines the facts for religion or politics into a final conclusion.
    private static Conclusion analyseReligionPolitics(FactBook f, String name) {

        // Get the relevant facts.
        ArrayList<Fact> facts = f.getFactsWithName(name);
        ArrayList<Fact> correctedFacts = new ArrayList<>();

        // Remove anything in brackets from the facts as well as excess whitespace.
        for (Fact fact : facts) {
            String value = ((String) fact.getValue()).replaceAll("\\(.*\\)", "").trim();
            correctedFacts.add(new Fact<>(name, value, fact));
        }

        // Combine the reformatted facts.
        return decideBetweenFacts(correctedFacts);
    }

    // Combine the facts for work and education into a final conclusion.
    private static ArrayList<Conclusion> analyseWorkEducation(FactBook f) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();

        // Get all the work and education facts and add them into the same list.
        ArrayList<Fact> periods = f.getFactsWithName("Work");
        periods.addAll(f.getFactsWithName("Education"));

        // For each work or education.
        for (Fact p : periods) {
            MinedPeriod mp = (MinedPeriod) p.getValue();

            // Format the mined period into a string representation.
            String value = "";
            if (mp.getDiscipline() != null) {
                value += mp.getDiscipline();
                if (mp.getInstitution() != null) {
                    value += " at " + mp.getInstitution();
                }
            } else if (mp.getInstitution() != null) {
                value += mp.getInstitution();
            }

            // Add the year range to the string if available.
            if (mp.getStartYear() != null && !mp.getStartYear().equals("0000-00") && mp.getEndYear() != null && !mp.getEndYear().equals("0000-00")) {
                value += " (" + mp.getStartYear() + " - " + mp.getEndYear() + ")";
            } else if (mp.getStartYear() != null && !mp.getStartYear().equals("0000-00")) {
                value += " (" + mp.getStartYear() + " - Present)";
            } else if (mp.getEndYear() != null && !mp.getEndYear().equals("0000-00")) {
                value += " (" + mp.getEndYear() + ")";
            }

            // Get the confidences and add the newly formatted conclusions.
            ArrayList<Fact> sources = new ArrayList<>();
            sources.add(p);
            double confidence =  getConfidenceFromSource(p);
            conclusions.add(new Conclusion<>("Location", mp.getInstitution(), confidence, sources));
            conclusions.add(new Conclusion<>(p.getName(), value, confidence, sources));
        }

        return conclusions;
    }

    // Converts facts into conclusions.
    private static ArrayList<Conclusion> getFactsAsConclusions(FactBook f, String factName) {
        ArrayList<Conclusion> conclusions = new ArrayList<>();
        ArrayList<Fact> facts = f.getFactsWithName(factName);

        // Parses each fact into a conclusion with the fact as its only sources.
        for (Fact fact : facts) {
            double confidence =  getConfidenceFromSource(fact);
            ArrayList<Fact> sources = new ArrayList<>();
            sources.add(fact);
            conclusions.add(new Conclusion<>(fact.getName(), (String) fact.getValue(), confidence, sources));
        }

        return conclusions;
    }

    // Performs the data combining algorithm on a list of facts.
    private static Conclusion decideBetweenFacts(ArrayList<Fact> facts) {
        if (facts.isEmpty()) return null;

        ArrayList<Conclusion> conclusions = factsToConclusions(facts);
        combineEqualConclusions(conclusions);
        Conclusion<String> c = getHighestConfidenceConclusion(conclusions);

        // Uppercase the first letter to make it more visually appealing and format into a Conclusion.
        String value = Util.uppercaseFirstLetter(c.getValue());
        return new Conclusion<>(c.getName(), value, c.getConfidence(), c.getSources());
    }

    // Overloaded method where name of a set of facts is given instead of a list.
    private static Conclusion decideBetweenFacts(FactBook f, String factName) {
        ArrayList<Fact> facts = f.getFactsWithName(factName);
        return decideBetweenFacts(facts);
    }

    // Converts facts to conclusions and normalises their confidences.
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

            // Calulate the probability of none of the values being correct.
            incorrectProb *= (1.0 - confidence);
            conclusions.add(new Conclusion<>(f.getName(), f.getValue(), confidence, sources));
        }

        // Normalise the confidences.
        for (Conclusion c : conclusions) {
            c.setConfidence((c.getConfidence() / total) * (1 - incorrectProb));
        }

        return conclusions;
    }

    // Combines any conclusions with equal values.
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

    // Combines any conclusions with equal values or values which are a substring of the other.
    private static void combineContainedNameConclusions(ArrayList<Conclusion> conclusions) {
        // Compare each pair of values.
        for (int i = 1; i < conclusions.size(); i++) {
            for (int j = 0; j < i; j++) {
                String s1 = (String) conclusions.get(i).getValue();
                String s2 = (String) conclusions.get(j).getValue();
                boolean decision = false;
                boolean contains = false;

                // Check if either contains the other.
                if (s1.contains(s2)) {
                    contains = true;
                    decision = decideBetweenNames(conclusions.get(i).getName(), s1, s2);
                } else if (s2.contains(s1)) {
                    contains = true;
                    decision = !decideBetweenNames(conclusions.get(i).getName(), s2, s1);
                }

                // If one contains the other then remove the correct conclusion.
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

                    // Move the loops back one as an item has been moved.
                    i--; j--;
                }
            }
        }
    }

    // Decide which name is more representative of the pair.
    private static boolean decideBetweenNames(String namePart, String n1, String n2) {
        // Check if either name is contained in the list of names.
        boolean n1Recognised = isNameRecognised(namePart, n1);
        boolean n2Recognised = isNameRecognised(namePart, n2);

        // If both are recognisable or both unrecognisable then take the first one (the larger).
        if ((!n1Recognised && !n2Recognised) || (n1Recognised && n2Recognised)) return true;
        // If the smaller is the only recognisable one take the smaller. Otherwise take the larger.
        else return n1Recognised;
    }

    // Choose the conclusion with the highest confidence.
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

    // Gets the confidence of a conclusion based on a source.
    protected static double getConfidenceFromSource(Fact source) {
        double confidence = 1;

        Fact s = source;
        while (s != null) {
            switch (s.getName()) {
                case "Facebook Account":    confidence *= FB_ROOT_CONF; break;
                case "Twitter Account":      confidence *= TW_ROOT_CONF; break;
                case "Reddit Account":    confidence *= RD_ROOT_CONF; break;

                default: confidence *= 1;
            }

            s = s.getSource();
        }

        return confidence;
    }

    // Reads in the root confidences from the file.
    private static void setRootConfidences() {
        // Read the files, if it fails then use the default values.
        try {
            String file = Util.readFileToString(Util.getResourceURI() + "properties/confidence.properties");

            FB_ROOT_CONF = Double.parseDouble(Util.getConfigParameter(file, "fb="));
            TW_ROOT_CONF = Double.parseDouble(Util.getConfigParameter(file, "tw="));
            RD_ROOT_CONF = Double.parseDouble(Util.getConfigParameter(file, "rd="));
        } catch (FileNotFoundException e) {
            System.err.println("ERROR: Confidence Properties File Not Found");
            e.printStackTrace();
            System.out.println("Using default values instead.");

            FB_ROOT_CONF = 0.95;
            TW_ROOT_CONF = 0.7;
            RD_ROOT_CONF = 0.4;
        }


        System.out.println("   Root Confidences Intialised");
        System.out.println("      Facebook: " + FB_ROOT_CONF);
        System.out.println("      Twitter: " + TW_ROOT_CONF);
        System.out.println("      Reddit: " + RD_ROOT_CONF);
    }

}

package com.chrisdmilner.webapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Scanner;

/*
 * Util
 *
 * A collection of utility functions that are used throughout the project, largely for I/O tasks.
 *
 * */
public class Util {

    // Outputs a list of string lines to a file at the given path.
    public static void outputToFile(String path, ArrayList<String> lines) {
        File file = null;
        try {
            file = new File(path);
            file.createNewFile();
            Files.write(file.toPath(), lines, Charset.forName("UTF-8"));
        } catch (IOException e) {
            System.err.println("ERROR outputting to file: " + file.getPath());
            e.printStackTrace();
        }
    }

    // Check if a file at the given path exists.
    public static boolean fileExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    // Reads a file at the given path into a list of lines or throws an error if it doesn't exist.
    public static ArrayList<String> readFileLines(String path) throws FileNotFoundException{
        Scanner s = null;
        s = new Scanner(new File(path));

        // Make a list and read in each line until there are no more.
        ArrayList<String> list = new ArrayList<String>();
        while (s.hasNextLine()) list.add(s.nextLine());
        s.close();

        return list;
    }

    // Reads the files at a given path into a single string or throws an error if the files doesn't exist.
    public static String readFileToString(String path) throws FileNotFoundException {
        Scanner s = null;
        s = new Scanner(new File(path));
        return s.useDelimiter("\\A").next();
    }

    // Removes the file at the given path.
    public static boolean deleteFile(String path) {
        File file = new File(path);
        return file.delete();
    }

    // Retrieves the information in the config file as a string.
    public static String getAPIConfigFile() {
        String props = "";
        try {
            props = readFileToString(getResourceURI() + "properties/config.properties");
        } catch (FileNotFoundException e) {
            System.err.println("ERROR: The API configuration file appears to be missing.");
            e.printStackTrace();
            System.exit(1);
        }
        return props;
    }

    // Read a file in the resources directory as a list of its lines.
    public static ArrayList<String> readResourceFileLines(String path) {
        ArrayList<String> file = new ArrayList<>();
        try {
            file = readFileLines(getResourceURI() + path);
        } catch (FileNotFoundException e) {
            System.err.println("ERROR: The resource file '" + getResourceURI() + path + "' cannot be found.");
            e.printStackTrace();
            System.exit(1);
        }
        return file;
    }

    // Get the URI to the resource URL. It varies depending on the operating system.
    public static String getResourceURI() {
        if ((new File(".").getAbsolutePath()).equals("/."))
            return System.getProperty("user.dir") + "opt/tomcat/webapps/analyserwebapp/WEB-INF/classes/";
        else
            return System.getProperty("user.dir") + "/src/main/resources/";
    }

    // Gets a specific configuration parameter from a file.
    public static String getConfigParameter(String file, String param) {
        int start = file.lastIndexOf(param) + param.length();
        if (start != -1) return file.substring(start, file.indexOf('\n', start));
        return "";
    }

    // Parses a date in the given string format into a date structure.
    public static Date parseDate(String date, String format) {
        Date d = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat(format);
            d = df.parse(date);
        } catch (ParseException e) {
            System.err.println("ERROR parsing a date. Not in the expected " + format + " format.");
            e.printStackTrace();
        }

        return d;
    }

    // Gets just the year section of a date as an integer.
    public static int getYearFromDate(Date d) {
        Calendar y = Calendar.getInstance();
        y.setTime(d);
        return y.get(Calendar.YEAR);
    }

    // Gets the current year as an integer.
    public static int getCurrentYear() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR);
    }

    // Puts the first letter of a string in uppercase.
    public static String uppercaseFirstLetter(String s) {
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

    // Removes anything that itsn't a letter from a string.
    public static String removeNonLetters(String s) {
        return s.replaceAll("[^a-zA-Z]", "");
    }

    // Removes all numbers from a string.
    public static String removeNumbers(String s) {
        return s.replaceAll("[0-9]", "");
    }

}

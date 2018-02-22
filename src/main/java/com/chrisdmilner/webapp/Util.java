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

public class Util {

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

    public static boolean fileExists(String path) {
        File file = new File(path);
        return file.exists();
    }

    public static ArrayList<String> readFileLines(String path) throws FileNotFoundException{
        Scanner s = null;
        s = new Scanner(new File(path));

        ArrayList<String> list = new ArrayList<String>();
        while (s.hasNextLine()) list.add(s.nextLine());
        s.close();

        return list;
    }

    public static String readFileToString(String path) throws FileNotFoundException {
        Scanner s = null;
        s = new Scanner(new File(path));
        return s.useDelimiter("\\A").next();
    }

    public static boolean deleteFile(String path) {
        File file = new File(path);
        return file.delete();
    }

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

    public static String getResourceURI() {
        if ((new File(".").getAbsolutePath()).equals("/."))
            return System.getProperty("user.dir") + "opt/tomcat/webapps/analyserwebapp/WEB-INF/classes/";
        else
            return System.getProperty("user.dir") + "/src/main/resources/";
    }

    public static String getConfigParameter(String file, String param) {
        int start = file.lastIndexOf(param) + param.length();
        if (start != -1) return file.substring(start, file.indexOf('\n', start));
        return "";
    }

    public static Date parseDate(String date, String format) {
        Date d = null;
        try {
            SimpleDateFormat df = new SimpleDateFormat("yyyy");
            d = df.parse(date);
        } catch (ParseException e) {
            System.err.println("ERROR parsing a date. Not in the expected " + format + " format.");
            e.printStackTrace();
        }

        return d;
    }

    public static int getYearFromDate(Date d) {
        Calendar y = Calendar.getInstance();
        y.setTime(d);
        return y.get(Calendar.YEAR);
    }

    public static int getCurrentYear() {
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR);
    }

    public static String uppercaseFirstLetter(String s) {
        return s.substring(0,1).toUpperCase() + s.substring(1);
    }

}

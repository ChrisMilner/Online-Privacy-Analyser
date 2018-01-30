package com.chrisdmilner.webapp;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.util.ArrayList;
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
            props = Util.readFileToString("/media/chris/Data/Dropbox/Uni Work/year3/CS310 - Project/analyserwebapp/src/main/resources/properties/config.properties");
        } catch (FileNotFoundException e) {
            System.err.println("ERROR: No the API configuration file appears to be missing.");
            e.printStackTrace();
            System.exit(1);
        }
        return props;
    }

    public static String getConfigParameter(String file, String param) {
        int start = file.lastIndexOf(param) + param.length();
        if (start != -1) return file.substring(start, file.indexOf('\n', start));
        return "";
    }

}

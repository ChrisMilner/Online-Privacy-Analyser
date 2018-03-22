package com.chrisdmilner.webapp.servlets;

import com.chrisdmilner.webapp.Util;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.FileNotFoundException;
import java.util.ArrayList;

/*
 * Update Servlet
 *
 * Handles the updating of the root confidences based on user feedback. Takes in the number of correct and incorrect
 * conclusions from each root source, calculates the percentage accuracy and then updates the root confidences to move
 * closer to those accuracies.
 *
 * */
public class Update extends HttpServlet {

    // The portion of the difference changed on each update.
    private static final double UPDATE_RATE = 0.1;

    // Handles the GET requests from the client containing the accuracy values.
    public void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse) {

        // Read the correct and inccorrect values from the request.
        System.out.println("Session Ended");
        System.out.println("Updating Confidences");
        double fbc = Double.parseDouble(httpServletRequest.getParameter("fbc"));
        double fbw = Double.parseDouble(httpServletRequest.getParameter("fbw"));
        System.out.println("   Facebook: Correct=" + fbc + " Wrong=" + fbw);

        double twc = Double.parseDouble(httpServletRequest.getParameter("twc"));
        double tww = Double.parseDouble(httpServletRequest.getParameter("tww"));
        System.out.println("   Twitter: Correct=" + twc + " Wrong=" + tww);

        double rdc = Double.parseDouble(httpServletRequest.getParameter("rdc"));
        double rdw = Double.parseDouble(httpServletRequest.getParameter("rdw"));
        System.out.println("   Reddit: Correct=" + rdc + " Wrong=" + rdw);

        // Convert the values into accuracies. Set the accuracy to -1 if their are zero values for a source.
        double fbPercent = -1;
        if ((fbc + fbw) != 0) fbPercent = fbc / (fbc + fbw);
        double twPercent = -1;
        if ((twc + tww) != 0) twPercent = twc / (twc + tww);
        double rdPercent = -1;
        if ((rdc + rdw) != 0) rdPercent = rdc / (rdc + rdw);
        System.out.println("   Percents: FB="+fbPercent+" TW="+twPercent+" RD="+rdPercent);

        // Get the values from the file containing the root confidences.
        double fbr;
        double twr;
        double rdr;
        String path = Util.getResourceURI() + "properties/confidence.properties";
        try {
            String file = Util.readFileToString(path);

            fbr = Double.parseDouble(Util.getConfigParameter(file, "fb="));
            twr = Double.parseDouble(Util.getConfigParameter(file, "tw="));
            rdr = Double.parseDouble(Util.getConfigParameter(file, "rd="));
        } catch (FileNotFoundException e) {
            System.err.println("ERROR: Confidence Properties File Not Found");
            e.printStackTrace();
            return;
        }

        // Calculate the updated confidences.
        double newFB = fbr;
        if (fbPercent != -1) newFB += (fbPercent - fbr) * UPDATE_RATE;
        double newTW = twr;
        if (twPercent != -1) newTW += (twPercent - twr) * UPDATE_RATE;
        double newRD = rdr;
        if (rdPercent != -1) newRD += (rdPercent - rdr) * UPDATE_RATE;

        System.out.println("Updated Confidences: FB="+newFB+" TW="+newTW+" RD="+newRD);

        // Write the confidences back to the file.
        ArrayList<String> lines = new ArrayList<>();
        lines.add("fb="+newFB);
        lines.add("tw="+newTW);
        lines.add("rd="+newRD);
        lines.add("");

        Util.outputToFile(path, lines);
    }
}
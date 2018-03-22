package com.chrisdmilner.webapp.servlets;

import com.chrisdmilner.webapp.Miner;
import com.chrisdmilner.webapp.Util;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;

/*
* Analyse Servlet
*
* Handles user profile submission. Takes the location of the profile file as input, calls the miner and analyser and
* then sends an appropriate response back to the client.
*
* */
public class Analyse extends HttpServlet {

    // Handles the GET requests from the client containing the user's details.
    public void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws IOException {

        // Get the user's id and the path to the file and use them to get the contents of the user's file.
        String id = httpServletRequest.getParameter("id");
        String path = httpServletRequest.getParameter("path");
        String params = Util.readFileToString(path + "output/" + id + ".data");

        // Extract the profile data from the file.
        String fb = Util.getConfigParameter(params, "fb=");
        String tw = Util.getConfigParameter(params, "tw=");
        String rd = Util.getConfigParameter(params, "rd=");
        String at = Util.getConfigParameter(params, "at=");

        // Call the miner, which then calls the analyser, and get the returned JSON object.
        String json;
        try {
            json = Miner.mine(fb, tw, rd, at);
        } catch (Exception e) {
            // If there is an error then respond "Failed" to the client.
            e.printStackTrace();
            httpServletResponse.getWriter().write("Failed");
            httpServletResponse.getWriter().flush();
            httpServletResponse.getWriter().close();
            return;
        }

        // Output the resulting JSON to a file.
        ArrayList<String> jsonLines = new ArrayList<>();
        jsonLines.add(json);
        Util.outputToFile(path + "output/" + id + ".data", jsonLines);

        // Send a successful response to the client.
        httpServletResponse.getWriter().write("DONE");
        httpServletResponse.getWriter().flush();
        httpServletResponse.getWriter().close();
    }
}
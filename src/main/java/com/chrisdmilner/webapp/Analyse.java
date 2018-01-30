package com.chrisdmilner.webapp;

import javax.servlet.http.*;
import java.io.IOException;
import java.util.ArrayList;

public class Analyse extends HttpServlet {
    public void doGet(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse)
            throws IOException {

        String id = httpServletRequest.getParameter("id");
        String path = httpServletRequest.getParameter("path");

        String params = Util.readFileToString(path + "output/" + id + ".data");

        String fb = Util.getConfigParameter(params, "fb=");
        String tw = Util.getConfigParameter(params, "tw=");
        String rd = Util.getConfigParameter(params, "rd=");

        String json;
        try {
            json = Miner.mine(fb, tw, rd);
        } catch (Exception e) {
            httpServletResponse.getWriter().write("Failed");
            httpServletResponse.getWriter().flush();
            httpServletResponse.getWriter().close();
            return;
        }

        System.out.println(json);

        ArrayList<String> jsonLines = new ArrayList<>();
        jsonLines.add(json);
        Util.outputToFile(path, jsonLines);

        httpServletResponse.getWriter().write("DONE");
        httpServletResponse.getWriter().flush();
        httpServletResponse.getWriter().close();
    }
}
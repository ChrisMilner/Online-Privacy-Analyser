<%@ page import="com.chrisdmilner.webapp.Util" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONException" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.io.FileNotFoundException" %>
<!doctype html>

<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="description" content="A tool for analysing your online privacy and the information you put out there.">
    <meta name="author" content="Chris Milner">
    <title>Results - Online Privacy Analyser</title>

    <script src="js/results.js"></script>
    <link rel="stylesheet" href="css/style.css">
    <link href="https://fonts.googleapis.com/css?family=Roboto" rel="stylesheet">
    <link rel="shortcut icon" href="images/favicon.ico" type="images/x-icon">
    <link rel="icon" href="images/favicon.ico" type="image/x-icon">
</head>

<body>
    <h1 id="title">Your Results</h1>

    <%

        boolean fileFound = true;
        String id = request.getParameter("id");
        String path = request.getParameter("path");
        String json = "";
        try {
            json = Util.readFileToString(path + "output/" + id + ".data");
            if (!Util.deleteFile(path + "output/" + id + ".data"))
                System.err.println("Failed to delete the Data file " + id);
        } catch (FileNotFoundException e) {
            fileFound = false;
        }

        if (fileFound) {

    %>

    <table id="conclusions">
        <tr>
            <th>Name</th>
            <th>Value</th>
            <th>Confidence</th>
            <th>Sources</th>
        </tr>

        <%
        try {

            JSONArray conclusions = new JSONObject(json).getJSONArray("conclusions");

            JSONObject curr;
            String name;
            String value;
            JSONArray srcs;
            for (int i = 0; i < conclusions.length(); i++) {
                curr = conclusions.getJSONObject(i);
                name = curr.getString("name");
                value = curr.getString("value");
                srcs = curr.getJSONArray("sources");
            %>

            <tr data-srcno=<%= srcs.length() %>>
                <td><%= name %></td>
                <%
                    if (name.equals("Image URL")) {
                        %> <td><a href=<%= value %>>Link</a></td> <%
                    } else {
                        %> <td><%= value %></td> <%
                    }
                %>
                <td><%= curr.getString("confidence") %></td>
                <td class="sources">Expand</td>
            </tr>

            <%
                for (int j = 0; j < srcs.length(); j++) {
                    JSONObject src = srcs.getJSONObject(j);
                    name = src.getString("name");
                    value = src.getString("value");
                    %>

                    <tr class="hidden sublevel1" data-srcno="1">
                        <td><%= name %></td>
                        <%
                            if (name.equals("Image URL")) {
                                %> <td><a href=<%= value %>>Link</a></td> <%
                            } else {
                                %> <td><%= value %></td> <%
                            }
                        %>
                        <td>-</td>
                        <td class="sources">Expand</td>
                    </tr>

                    <%

                    JSONObject subsrc = src;
                    while (subsrc.getJSONObject("source").has("source")) {
                        subsrc = subsrc.getJSONObject("source");

                        int srcno = 1;
                        String sourceText = "Expand";
                        if (!subsrc.getJSONObject("source").has("source")){
                            srcno = 0;
                            sourceText = "You";
                        }

                        name = subsrc.getString("name");
                        value = subsrc.getString("value");

                        %>

                        <tr class="hidden sublevel2" data-srcno=<%= srcno %>>
                            <td><%= name %></td>
                            <%
                                if (name.equals("Image URL")) {
                                    %> <td><a href=<%= value %>>Link</a></td> <%
                                } else {
                                    %> <td><%= value %></td> <%
                                }
                            %>
                            <td>-</td>
                            <td class="sources"><%= sourceText %></td>
                        </tr>

                        <%
                    }
                }
            %>

            <%

            }

        } catch (JSONException e) {
            System.err.println("ERROR decoding the conclusion JSON.");
            e.printStackTrace();
        }

        %>
    </table>

    <%

        } else {

            %>

            <div id="error">
                <h2>Error: No data found with this ID</h2>
                <p>This error may have occurred if you reloaded. We take your privacy seriously so we do not retain any of your data. So if you want to see your data again you'll have to go from the start.</p>
            </div>

            <%

        }

    %>

</body>
</html>
<%@ page import="com.chrisdmilner.webapp.Util" %>
<%@ page import="org.json.JSONArray" %>
<%@ page import="org.json.JSONException" %>
<%@ page import="org.json.JSONObject" %>
<%@ page import="java.io.FileNotFoundException" %>
<%@ page import="java.util.ArrayList" %>
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
            <th>Correct?</th>
            <th>Sources</th>
        </tr>

        <%
        try {

            JSONArray conclusions = new JSONObject(json).getJSONArray("conclusions");

            ArrayList<JSONObject> images = new ArrayList<JSONObject>();
            ArrayList<JSONObject> keywords = new ArrayList<JSONObject>();
            ArrayList<JSONObject> sharedKeywords = new ArrayList<JSONObject>();

            JSONObject curr;
            String name;
            String value;
            String confidence;
            JSONArray srcs;
            for (int i = 0; i < conclusions.length(); i++) {
                curr = conclusions.getJSONObject(i);
                name = curr.getString("name");

                if (name.equals("Image URL")) {
                    images.add(curr);
                    continue;
                } else if (name.equals("Keyword Posted")) {
                    keywords.add(curr);
                    continue;
                } else if (name.equals("Keyword Shared")) {
                    sharedKeywords.add(curr);
                    continue;
                }

                value = curr.getString("value");
                confidence = (Double.parseDouble(curr.getString("confidence")) * 100) + "%";
                srcs = curr.getJSONArray("sources");
            %>

            <tr data-srcno=<%= srcs.length() %>>
                <td><%= name %></td>
                <td><%= value %></td>
                <td><%= confidence %></td>
                <td class="correct yes">Yes</td>
                <td class="sources">Expand</td>
            </tr>

            <%
                for (int j = 0; j < srcs.length(); j++) {
                    JSONObject src = srcs.getJSONObject(j);
                    name = src.getString("name");

                    value = src.getString("value");
                    %>

                    <tr class="hidden" style ="background-color:rgba(0,0,0,0.05)" data-srcno="1">
                        <td><%= name %></td>
                        <td><%= value %></td>
                        <td>-</td>
                        <td>-</td>
                        <td class="sources">Expand</td>
                    </tr>

                    <%

                    JSONObject subsrc = src;
                    int depth = 1;
                    while (subsrc.getJSONObject("source").has("source")) {
                        depth++;
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

                        <tr class="hidden" style ="background-color:rgba(0,0,0,<%= depth * 0.05 %>)" data-srcno=<%= srcno %>>
                            <td><%= name %></td>
                            <td><%= value %></td>
                            <td>-</td>
                            <td>-</td>
                            <td class="sources"><%= sourceText %></td>
                        </tr>

                        <%
                    }
                }

            }
            %>

            </table>
            <h2 class="subtitle">Images</h2>

            <table id="images">
            <%

            for (int i = 0; i < Math.ceil(images.size() / 4.0); i++) {

                %> <tr> <%

                for (int j = 0; j < Math.min(4, images.size() - (i*4)); j++) {
                    JSONObject image = images.get((i*4) + j);
                    JSONArray sources = image.getJSONArray("sources");

                    %>

                    <td><img src=<%= image.getString("value") %> data-sources=<%= "'" + sources.toString() + "'" %>></td>

                    <%

                }

                    %></tr> <%
            }

            %>

            </table>
            <h2 class="subtitle">Posted Keywords</h2>
            <%

                if (keywords.isEmpty()){
                    %>
                    <p class="none">None</p>
                    <%
                }

                for (int i = 0; i < keywords.size(); i++) {
                    JSONObject kw = keywords.get(i);
                    JSONObject primarySource = (JSONObject) kw.getJSONArray("sources").get(0);
                    String keyword = (String) kw.get("value");
                    String fullText = primarySource.getString("value");

                    int start = fullText.toLowerCase().indexOf(keyword);
                    int end = start + keyword.length();

                    String before = fullText.substring(0, start);
                    String after = fullText.substring(end);

                    String rootSource = primarySource.getJSONObject("source").getString("name");
                    String source = "Facebook Posts";
                    if (rootSource.equals("Twitter Account"))
                        source = "Twitter Feed";
                    else if (rootSource.equals("Reddit Account"))
                        source = "Reddit Posts/Comments";

                    %>
                    <div class="keyword-section">
                        <div class="keyword"><%= keyword %></div>
                        <div class="full-text"><%= before %><mark><%= keyword %></mark><%= after %></div>
                        <p class="source-text">from your <%= source %></p>
                    </div>
                    <%
                }

            %>

            <h2 class="subtitle">Shared Keywords</h2>
            <%

                if (sharedKeywords.isEmpty()){
                    %>
                    <p class="none">None</p>
                    <%
                }

                for (int i = 0;i < sharedKeywords.size(); i++) {
                    JSONObject kw = sharedKeywords.get(i);
                    JSONObject primarySource = (JSONObject) kw.getJSONArray("sources").get(0);
                    String keyword = (String) kw.get("value");
                    String fullText = primarySource.getString("value");

                    int start = fullText.indexOf(keyword);
                    int end = start + keyword.length();

                    String before = fullText.substring(0, start);
                    String after = fullText.substring(end);

                    String rootSource = primarySource.getJSONObject("source").getString("name");
                    String source = "Facebook Posts";
                    if (rootSource.equals("Twitter Account"))
                        source = "Twitter Feed";
                    else if (rootSource.equals("Reddit Account"))
                        source = "Reddit Posts/Comments";

                    %>
                    <div class="keyword-section">
                        <div class="keyword"><%= keyword %></div>
                        <div class="full-text"><%= before %><mark><%= keyword %></mark><%= after %></div>
                        <p class="source-text">from your <%= source %></p>
                    </div>
                    <%
                }

            %>

            <%

        } catch (JSONException e) {
            System.err.println("ERROR decoding the conclusion JSON.");
            e.printStackTrace();
        } catch (Exception e) {
            System.err.println("ERROR formatting conclusions");
            e.printStackTrace();
        }

        %>

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

    <div class="image-details hidden">
        <div id="image-container">
            <img src="">
        </div>
        <div id="close-btn"><p>Close</p></div>
        <div id="image-sources">
            <h3>Sources</h3>
            <table id="image-source-table">
                <tr><th>Name</th><th>Value</th></tr>
            </table>
        </div>
    </div>

    <div class="footer">
        <a href="privacy.html">Privacy Policy</a>
    </div>
</body>
</html>
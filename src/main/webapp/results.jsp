<%@ page import="com.chrisdmilner.webapp.Util" %>
<%@ page import="org.json.*" %>
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
        String path = "/media/chris/Data/Dropbox/Uni Work/year3/CS310 - Project/analyserwebapp/src/main/output/" + id + ".data";
        String json = "";
        try {
            json = Util.readFileToString(path);
            Util.deleteFile(path);
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
            for (int i = 0; i < conclusions.length(); i++) {
                curr = conclusions.getJSONObject(i);
            %>

            <tr>
                <td><%= curr.getString("name") %></td>
                <td><%= curr.getString("value") %></td>
                <td><%= curr.getString("confidence") %></td>
                <td class="sources" id=<%= i %>>Expand</td>
            </tr>

            <div class="source-list" id=<%= "list-" + i %>>
                <h2>Sources:</h2>
                <ul>
                <%

                    JSONArray sources = curr.getJSONArray("sources");
                    for (int j = 0; j < sources.length(); j++) {
                        %> <li> <%= sources.get(j) %> </li> <%
                    }

                %>
                </ul>
                <h3>Hide</h3>
            </div>

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
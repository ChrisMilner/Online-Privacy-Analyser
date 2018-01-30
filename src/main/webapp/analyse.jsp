<%@ page import="com.chrisdmilner.webapp.Util" %>
<%@ page import="java.util.ArrayList" %>
<%@ page import="java.util.Random" %>
<%

    String fb = request.getParameter("facebook-url");
    String tw = request.getParameter("twitter-url");
    String rd = request.getParameter("reddit-url");

    ArrayList<String> lines = new ArrayList<String>();
    if (fb != null) lines.add("fb=" + fb);
    if (tw != null) lines.add("tw=" + tw);
    if (rd != null) lines.add("rd=" + rd);

    Random rand = new Random();
    int id = rand.nextInt(100000);
    String path = config.getServletContext().getRealPath("/");
    while (Util.fileExists(path + "output/" + id + ".data")) id = rand.nextInt(100000);

    Util.outputToFile(path  +  "output/" + id + ".data", lines);

%>

<!doctype html>

<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="description" content="A tool for analysing your online privacy and the information you put out there.">
    <meta name="author" content="Chris Milner">
    <title>Analysing...</title>

    <script src="js/analyse.js"></script>
    <link rel="stylesheet" href="css/style.css">
    <link href="https://fonts.googleapis.com/css?family=Roboto" rel="stylesheet">
    <link rel="shortcut icon" href="images/favicon.ico" type="images/x-icon">
    <link rel="icon" href="images/favicon.ico" type="image/x-icon">
</head>

<body data-id=<%= id %> data-path=<%= path %>>
    <div id="spinner"></div>
    <h1 id="loading-text">Analysing your data</h1>
</body>
</html>

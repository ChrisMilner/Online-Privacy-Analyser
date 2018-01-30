<!doctype html>

<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="description" content="A tool for analysing your online privacy and the information you put out there.">
    <meta name="author" content="Chris Milner">
    <title>Online Privacy Analyser</title>

    <link rel="stylesheet" href="css/style.css">
    <link href="https://fonts.googleapis.com/css?family=Roboto" rel="stylesheet">
    <link rel="shortcut icon" href="images/favicon.ico" type="images/x-icon">
    <link rel="icon" href="images/favicon.ico" type="image/x-icon">
</head>

<body>
    <div id="content">
        <h1 id="title">Analyse your Online Privacy</h1>
        <p id="description">This tool was created as part of a Computer Science university project. This tool aims to demonstrate just how much personal information about you is stored online. We hope this tool will encourage and help you to be more careful when sharing online.</p>

        <%

            boolean error = request.getParameter("error") != null && request.getParameter("error").equals("true");

            if (error) {
                %>
                    <p id="exception">An error occurred while processing your data. Please try again.</p>
                <%
            }

        %>

        <form id="start-points" action="analyse.jsp" method="POST">
            <h2 id="instructions">To begin input a link to as many or as few (minimum 1) of the categories below. Then click the "Analyse" button below:</h2>
            <h3 class="label">Facebook:</h3>
            <input type="text" name="facebook-url"><br />
            <h3 class="label">Twitter:</h3>
            <input type="text" name="twitter-url"><br />
            <h3 class="label">Reddit:</h3>
            <input type="text" name="reddit-url"><br />
            <input type="submit" name="analyse">
        </form>
    </div>
</body>
</html>
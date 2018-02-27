<!doctype html>

<html lang="en">
<head>
    <meta charset="utf-8">
    <meta name="description" content="A tool for analysing your online privacy and the information you put out there.">
    <meta name="author" content="Chris Milner">
    <title>Online Privacy Analyser</title>

    <script src="js/index.js"></script>
    <link rel="stylesheet" href="css/style.css">
    <link href="https://fonts.googleapis.com/css?family=Roboto" rel="stylesheet">
    <link rel="shortcut icon" href="images/favicon.ico" type="images/x-icon">
    <link rel="icon" href="images/favicon.ico" type="image/x-icon">
</head>

<body>
    <script>

        window.fbAsyncInit = function() {
            FB.init({
                appId            : '145646225970984',
                autoLogAppEvents : true,
                xfbml            : true,
                version          : 'v2.12'
            });
        };

        (function(d, s, id){
            var js, fjs = d.getElementsByTagName(s)[0];
            if (d.getElementById(id)) {return;}
            js = d.createElement(s); js.id = id;
            js.src = "https://connect.facebook.net/en_US/sdk.js";
            fjs.parentNode.insertBefore(js, fjs);
        }(document, 'script', 'facebook-jssdk'));

    </script>
    <div id="content">
        <h1 id="title">Analyse your Online Privacy</h1>
        <div id="description">
            <p>This tool was created as part of a Computer Science university project. It aims to demonstrate just how much personal information about you is stored online. We hope this tool will encourage and help you to be more careful when sharing online. We take your privacy very seriously and encourage you to read our <a href="privacy.html">privacy policy</a>.</p>
        </div>

        <%

            boolean error = request.getParameter("error") != null && request.getParameter("error").equals("true");

            if (error) {
                %>
                    <p id="exception">An error occurred while processing your data. Please try again.</p>
                <%
            }

        %>
        <div id="start-points">
            <form action="analyse.jsp" method="POST">
                <h2 id="instructions">To begin input a link to as many or as few (minimum 1) of the categories below. Then click the "Analyse" button below:</h2>
                <h3 class="label">Facebook:</h3>
                <input type="text" name="facebook-url"><br />
                <h2>or</h2>
                <div id="fb-login-div">
                    <h4>Select the data which is set to public on your profile</h4>
                    <h4><a>Why do I have to do this?</a></h4>
                    <table id="fb-perms">
                        <tr>
                            <td><input type="checkbox" id="perm-email">Email</td>
                            <td><input type="checkbox" id="perm-hometown">Hometown</td>
                            <td><input type="checkbox" id="perm-religion-politics">Religion/Politics</td>
                        </tr>
                        <tr>
                            <td><input type="checkbox" id="perm-relationships">Relationships</td>
                            <td><input type="checkbox" id="perm-likes">Likes</td>
                            <td><input type="checkbox" id="perm-work">Work History</td>
                        </tr>
                        <tr>
                            <td><input type="checkbox" id="perm-about-me">About Me</td>
                            <td><input type="checkbox" id="perm-location">Location</td>
                            <td><input type="checkbox" id="perm-places">Places</td>
                        </tr>
                        <tr>
                            <td><input type="checkbox" id="perm-bday">Birthday</td>
                            <td><input type="checkbox" id="perm-photos">Photos</td>
                            <td><input type="checkbox" id="perm-videos">Videos</td>
                        </tr>
                        <tr>
                            <td><input type="checkbox" id="perm-education">Education</td>
                            <td><input type="checkbox" id="perm-posts">Posts</td>
                            <td><input type="checkbox" id="perm-website">Website</td>
                        </tr>
                        <tr>
                            <td><input type="checkbox" id="perm-friends">Friends</td>
                            <td><input type="checkbox" id="perm-relationship">Relationship</td>
                        </tr>
                    </table>
                    <input type="button" name="fb-login" id="fb-login" value="Login with Facebook"><br />
                    <p id="login-fail-text">Facebook authentication failed. Please try again.</p>
                    <p id="login-success-text">Facebook account authenticated.</p>
                </div>
                <h3 class="label">Twitter:</h3>
                <input type="text" name="twitter-url"><br />
                <h3 class="label">Reddit:</h3>
                <input type="text" name="reddit-url"><br />
                <input type="submit" name="analyse"><br />
                <input type="hidden" name="access-token" id="access-token">
            </form>
        </div>
    </div>

    <div class="footer">
        <a href="privacy.html">Privacy Policy</a>
    </div>
</body>
</html>
window.onload = function () {
    var btn = document.getElementById("fb-login");

    // Add the Facebook login function.
    btn.onclick = function () {
        console.log("Login button pressed");

        var perms = getFBPermissions();
        console.log(perms);

        FB.login(function (response) {
            console.log("Got a response");
            console.log(response);

            // Display message on success or failure.
            if (response.status === "connected") {
                console.log("FB Login Successful");
                document.getElementById("access-token").value = response.authResponse.accessToken;
                document.getElementById("login-success-text").classList.add("show");
            } else {
                console.log("FB Login Failed");
                document.getElementById("login-fail-text").classList.add("show");
            }
        }, {scope: perms});
    };

    // Interpret the checkboxes ticked by the user as a list of permissions.
    function getFBPermissions() {
        var string = "public_profile,";

        if (document.getElementById("perm-email").checked) string += "email,";
        if (document.getElementById("perm-hometown").checked) string += "user_hometown,";
        if (document.getElementById("perm-religion-politics").checked) string += "user_religion_politics,";

        if (document.getElementById("perm-relationships").checked) string += "user_relationships,";
        if (document.getElementById("perm-likes").checked) string += "user_likes,";
        if (document.getElementById("perm-work").checked) string += "user_work_history,";

        if (document.getElementById("perm-about-me").checked) string += "user_about_me,";
        if (document.getElementById("perm-location").checked) string += "user_location,";
        if (document.getElementById("perm-places").checked) string += "user_tagged_places,";

        if (document.getElementById("perm-bday").checked) string += "user_birthday,";
        if (document.getElementById("perm-photos").checked) string += "user_photos,";
        if (document.getElementById("perm-videos").checked) string += "user_videos,";

        if (document.getElementById("perm-education").checked) string += "user_education_history,";
        if (document.getElementById("perm-posts").checked) string += "user_posts,";
        if (document.getElementById("perm-website").checked) string += "user_website,";

        if (document.getElementById("perm-friends").checked) string += "user_friends,";
        if (document.getElementById("perm-relationship").checked) string += "user_relationship_details,";

        string = string.slice(0, -1);
        return string;
    }
};
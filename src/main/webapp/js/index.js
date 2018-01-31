window.onload = function () {
    var btn = document.getElementById("fb-login");

    btn.onclick = function () {
        console.log("Login button pressed");
        FB.login(function (response) {
            console.log("Got a response");
            console.log(response);
        }, {scope: 'public_profile,email'});
    };
};
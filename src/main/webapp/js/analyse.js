window.onload = function () {
    var body = document.getElementsByTagName("BODY")[0];
    var id = body.getAttribute("data-id");
    var path = body.getAttribute("data-path");

	var xhttp = new XMLHttpRequest();

	xhttp.onreadystatechange = function() {
    	if (this.readyState === 4 && this.status === 200) {
    		console.log("REPLY");
    		console.log(this.responseText);
    		if (this.responseText === "DONE")
                window.location.replace("results.jsp?id=" + id + "&path=" + path);
    		else
    		    window.location.replace("index.jsp?error=true")
    	}
  	};
	console.log(path);
	xhttp.open("GET", "analyse?id=" + id + "&path=" + path, true);
	xhttp.send();
};

var OPEN_SOURCE_TEXT = "Expand";
var CLOSE_SOURCE_TEXT = "Close";
var NO_SOURCE_TEXT = "You";

window.onload = function () {
    var sources = document.getElementsByClassName("sources");
    for (var i = 0; i < sources.length; i++) {
        if (sources[i].getAttribute("data-srcno") === "0") continue;

        sources[i].onclick = function () {
            var row = this.parentElement;

            if (this.innerHTML === NO_SOURCE_TEXT)
                return;
            else if (this.innerHTML === OPEN_SOURCE_TEXT)
                openSources(row);
            else
                closeSources(row);
        }
    }

    var images = document.getElementsByTagName("img");
    for (i = 0; i < images.length; i++) {
        images[i].onclick = function () {
            var details = document.getElementsByClassName("image-details")[0];
            var largeImage = details.getElementsByTagName("img")[0];
            var sourceTable = document.getElementById("image-source-table");
            var sourceJSON = JSON.parse(this.getAttribute("data-sources"));

            var rowIndex = 1;
            for (var j = 0; j < sourceJSON.length; j++) {
                addImageSource(sourceTable, rowIndex++, sourceJSON[j].name, sourceJSON[j].value);

                var subsource = sourceJSON[j].source;
                var rowDepth = 1;
                while (subsource.source != null) {
                    var row = addImageSource(sourceTable, rowIndex++, subsource.name, subsource.value);
                    row.style.backgroundColor = "rgba(0,0,0," + (rowDepth * 0.1) + ")";
                    subsource = subsource.source;
                }
            }

            largeImage.setAttribute("src", this.getAttribute("src"));
            details.classList.remove("hidden");
        }
    }

    document.getElementById("close-btn").onclick = function () {
        var details = document.getElementsByClassName("image-details")[0];
        var sources = document.getElementById("image-source-table");
        details.classList.add("hidden");

        while (sources.getElementsByTagName("tr").length > 1) {
            sources.deleteRow(1);
        }
    };

    var correctCells = document.getElementsByClassName("correct");
    for (i = 0; i < correctCells.length; i++) {
        correctCells[i].onclick = function () {
            if (this.classList.contains("yes")) {
                this.classList.remove("yes");
                this.innerHTML = "No";
            } else {
                this.classList.add("yes");
                this.innerHTML = "Yes";
            }
        }
    }
};

var map;
var geocoder;
function myMap() {
    var mapOptions = {
        center: new google.maps.LatLng(52.4, -1.5),
        zoom: 2,
        mapTypeId: google.maps.MapTypeId.HYBRID
    };

    map = new google.maps.Map(document.getElementById("map"), mapOptions);
    geocoder = new google.maps.Geocoder();

    var locations = document.getElementsByClassName("location-data");
    for (var i = 0; i < locations.length; i++) {
        addMarkerAtAddress(locations[i].getAttribute("data-addr"));
    }
}

function addMarkerAtAddress(address) {
    geocoder.geocode( { 'address': address}, function(results, status) {
        if (status == 'OK') {
            console.log(results[0].types);
            if (!results[0].types.includes("country") && !results[0].types.includes("administrative_area_level_1")) {
                var marker = new google.maps.Marker({
                    map: map,
                    position: results[0].geometry.location
                });
            }
        } else {
            console.log('Geocode was not successful for the following reason: ' + status);
        }
    });
}

window.onbeforeunload = function() {
    var fbc = 0;
    var fbw = 0;
    var twc = 0;
    var tww = 0;
    var rdc = 0;
    var rdw = 0;

    var correctCells = document.getElementsByClassName("correct");
    for (var i = 0; i < correctCells.length; i++) {
        var cell = correctCells[i];
        var row = cell.parentElement;
        var noOfSources = row.getAttribute("data-srcno");
        var increase = 1 / noOfSources;
        var correct = cell.classList.contains("yes");

        var srcRow = row.nextElementSibling;
        for (var j = 0; j < noOfSources; j++) {
            // Trace source to root
            var nestedSrcNo = Number(srcRow.getAttribute("data-srcno"));
            var nestedRow = srcRow;
            while (nestedSrcNo !== 0) {
                nestedRow = nestedRow.nextElementSibling;
                nestedSrcNo = Number(nestedRow.getAttribute("data-srcno"));
            }

            var rootText = nestedRow.cells[0].innerHTML;
            if (rootText === "Facebook Account") {
                if (correct) fbc += increase;
                else fbw += increase;
            } else if (rootText === "Twitter Account") {
                if (correct) twc += increase;
                else tww += increase;
            } else if (rootText === "Reddit Account") {
                if (correct) rdc += increase;
                else rdw += increase;
            }

            srcRow = nestedRow.nextElementSibling;
        }
    }

    var xhttp = new XMLHttpRequest();
    xhttp.open("GET", "update?fbc="+fbc+"&fbw="+fbw+"&twc="+twc+"&tww="+tww+"&rdc="+rdc+"&rdw="+rdw, true);
    xhttp.send();
};

function openSources(row) {
    row.getElementsByClassName("sources")[0].innerHTML = CLOSE_SOURCE_TEXT;

    var srcno = row.getAttribute("data-srcno");

    row = row.nextElementSibling;
    row.classList.add("top");
    for (var i = 0; i < srcno; i++) {
        row.classList.remove("hidden");

        var subsrcno = Number(row.getAttribute("data-srcno"));
        row = row.nextElementSibling;
        for (var j = 0; j < subsrcno; j++) {
            subsrcno += Number(row.getAttribute("data-srcno"));
            row = row.nextElementSibling;
        }
    }
}

function closeSources(row) {
    var srcno = row.getAttribute("data-srcno");

    if (srcno == 0) {
        row.getElementsByClassName("sources")[0].innerHTML = NO_SOURCE_TEXT;
        return;
    } else
        row.getElementsByClassName("sources")[0].innerHTML = OPEN_SOURCE_TEXT;

    row = row.nextElementSibling;
    for (var i = 0; i < srcno; i++) {
        closeSources(row);
        row.classList.add("hidden");

        var subsrcno = Number(row.getAttribute("data-srcno"));
        row = row.nextElementSibling;
        for (var j = 0; j < subsrcno; j++) {
            subsrcno += Number(row.getAttribute("data-srcno"));
            row = row.nextElementSibling;
        }
    }
}

function addImageSource(table, index, name, value) {
    var row = table.insertRow(index);
    row.insertCell(0).innerHTML = name;

    if (name === "Image URL") {
        value = "<a href='" + value + "'>Link</a>";
    }

    row.insertCell(1).innerHTML = value;
    return row;
}

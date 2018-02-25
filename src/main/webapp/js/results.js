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
    }
};

function openSources(row) {
    row.getElementsByClassName("sources")[0].innerHTML = CLOSE_SOURCE_TEXT;

    var srcno = row.getAttribute("data-srcno");

    row = row.nextElementSibling;
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

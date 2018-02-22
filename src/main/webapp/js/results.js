var OPEN_SOURCE_TEXT = "Expand";
var CLOSE_SOURCE_TEXT = "Close";

window.onload = function () {
    var sources = document.getElementsByClassName("sources")
    for (var i = 0; i < sources.length; i++) {
        if (sources[i].getAttribute("data-srcno") === "0") continue;

        sources[i].onclick = function () {
            var row = this.parentElement;

            if (this.innerHTML === OPEN_SOURCE_TEXT)
                openSources(row);
            else
                closeSources(row);
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
    row.getElementsByClassName("sources")[0].innerHTML = OPEN_SOURCE_TEXT;

    var srcno = row.getAttribute("data-srcno");

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

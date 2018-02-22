var showingSources = false;

window.onload = function () {
    var sources = document.getElementsByClassName("sources")
    for (var i = 0; i < sources.length; i++) {
        if (sources[i].getAttribute("data-srcno") === "0") continue;

        sources[i].onclick = function () {
            var row = this.parentElement;
            var srcno = row.getAttribute("data-srcno");

            row = row.nextElementSibling;
            for (var i = 0; i < srcno; i++) {
                row.classList.remove("hidden");

                var subsrcno = Number(row.getAttribute("data-srcno"));
                row = row.nextElementSibling;
                for (var j = 0; j < subsrcno; j++) {
                    subsrcno += Number(row.getAttribute("data-srcno"));
                    console.log(subsrcno);
                    row = row.nextElementSibling;
                }
            }
        }
    }
};

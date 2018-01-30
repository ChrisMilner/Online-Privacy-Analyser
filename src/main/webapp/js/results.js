var showingSources = false;

window.onload = function () {
    var sources = document.getElementsByClassName("sources")
    for (var i = 0; i < sources.length; i++) {
        sources[i].onclick = function () {
            if (showingSources) return;
            var list = document.getElementById("list-" + this.id);
            list.classList.add("centered");
            showingSources = true;
        };

        var close = document.getElementById("list-" + sources[i].id).getElementsByTagName("h3")[0];
        close.onclick = function () {
            var list = this.parentElement;
            list.classList.remove("centered");
            showingSources = false;
        };
    }
};

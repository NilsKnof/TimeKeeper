let jsonData;

document.getElementById('file-input').addEventListener('change', readSingleFile, false);

function readSingleFile(e) {
    var file = e.target.files[0];
    if (!file) {
        return;
    }
    var reader = new FileReader();
    reader.onload = function(e) {
        jsonData = JSON.parse(e.target.result);
        document.getElementById("upload").hidden = true;
        document.getElementById("upload").style.display = 'none';
        document.getElementById("wrapper").style.display = "flex";
        document.getElementById("div_MD").style.display = "flex";
        document.getElementById("div_day_year").style.display = "flex";
        document.getElementById("div_show_all").style.display = "flex";
        showChart();
    };
    reader.readAsText(file);
}
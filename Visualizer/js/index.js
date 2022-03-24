let btnMonth = document.getElementById("btn_month");
let btnDay = document.getElementById("btn_day");
let divDayYear = document.getElementById("div_day_year");
let btnShowAll = document.getElementById("div_show_all");
let canvas = document.getElementById('myBarChart');
let label = [];
let barChart;
let months = ["January", "February", "March", "April", "May", "June", "July", "August", "September", "October", "November", "December"];
let jsonDataSorted = [];
let dataset = [];
let year = new Date().getFullYear();
let colors = ['rgba(34, 39, 122, 1)', 'rgba(49, 70, 173, 1)', 'rgba(64, 102, 224, 1)',
              'rgba(124, 146, 239, 1)', 'rgba(185, 191, 255, 1)', 'rgba(162, 153, 222, 1)',
              'rgba(139, 116, 189, 1)', 'rgba(130, 99, 179, 1)', 'rgba(121, 83, 169, 1)',
              'rgba(111, 67, 161, 1)', 'rgba(102, 51, 153, 1)', 'rgba(128, 0, 128, 1)', ];

let minYear, maxYear;

function showChart() {
    calcMinMax();
    initYearButtons();
    setMonthChart();
}

function calcMinMax() {
    let cache = new Date(jsonData[0]["startDate"]);
    minYear = maxYear = cache.getFullYear();
    for (let i = 0; i < jsonData.length; i++) {
        let temp = new Date(jsonData[i]["startDate"]).getFullYear();
        if (temp < minYear) minYear = temp;
        if (temp > maxYear) maxYear = temp;
    }
}

btnMonth.addEventListener("click", function (e) {
    divDayYear.style.visibility = "hidden";
    year = new Date().getFullYear();
    setMonthChart();
});

btnDay.addEventListener("click", function (e) {
    divDayYear.style.visibility = 'visible';
    year = new Date().getFullYear();
    setDayChart();
});

btnShowAll.addEventListener("click", function (e) {
    console.log(barChart.data.datasets.length);
    for (let i = 0; i < barChart.data.datasets.length; i++) {
        barChart.getDatasetMeta(i).hidden = false;
    }
    barChart.update();
})

function initYearButtons() {
    let div = document.getElementById("div_day_year");
    for (let i = minYear; i <= maxYear; i++) {
        let button = document.createElement('Button');
        button.id = "" + i;
        button.innerText = i + "";
        div.appendChild(button);
    }
    div.addEventListener("click", function (e) {
        year = parseInt(e.target.id);
        setDayChart();
    })
}

function setDayChart() {
    label = [];
    dataset = [];
    jsonDataSorted = [];
    for (let i = 0; i < 31; i++) {
        label[i] = i+1 + "";
    }
    for (let i = 0; i < 12; i++) {
        jsonDataSorted[i] = [];
    }

    for (let i = 0; i < jsonData.length; i++) {
        let startDate = new Date(jsonData[i]["startDate"]);
        if (startDate.getFullYear() === year)
            jsonDataSorted[startDate.getMonth()][jsonDataSorted[startDate.getMonth()].length] = jsonData[i];
    }

    for (let i = 0; i < 12; i++) {
        let durationCache = [];
        for (let k = 0; k < 31; k++) {
            durationCache[k] = 0;
        }
        for (let j = 0; j < jsonDataSorted[i].length; j++) {
            let startDate = new Date(jsonDataSorted[i][j]["startDate"]);
            let duration = jsonDataSorted[i][j]["duration"].split(':').map(Number);
            durationCache[startDate.getDate()-1] += duration[0] + (duration[1] / 60);
        }
        dataset[i] = {
            label: months[i],
            barPercentage: 0.5,
            backgroundColor: colors[i],
            minBarLength: 2,
            data: durationCache,
        };
    }
    if (barChart !== undefined)
        barChart.destroy();
    barChart = new Chart(canvas, {
        type: 'bar',
        data: {
            labels: label,
            datasets: dataset
        },
        options: {
            scales: {
                x: {
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    }
                },
                y: {
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    }
                }
            }
        }
    });

    for (let i = 0; i < months.length; i++) {
        barChart.getDatasetMeta(i).hidden = true;
    }
    barChart.getDatasetMeta(new Date().getMonth().valueOf()).hidden = false;
    barChart.update();
}

function setMonthChart() {
    label = [];
    dataset = [];
    jsonDataSorted = [];
    for (let i = 0; i < 12; i++) {
        label[i] = months[i] + "";
    }

    for (let i = 0; i <= (maxYear - minYear); i++) {
        jsonDataSorted[i] = [];
    }

    for (let i = 0; i < jsonData.length; i++) {
        let startDate = new Date(jsonData[i]["startDate"]);
        jsonDataSorted[startDate.getFullYear()-minYear][jsonDataSorted[startDate.getFullYear()-minYear].length] = jsonData[i];
    }

    for (let i = 0; i <= (maxYear - minYear); i++) {
        let durationCache = [];
        for (let k = 0; k < 12; k++) {
            durationCache[k] = 0;
        }
        for (let j = 0; j < jsonDataSorted[i].length; j++) {
            let startDate = new Date(jsonDataSorted[i][j]["startDate"]);
            let duration = jsonDataSorted[i][j]["duration"].split(':').map(Number);
            durationCache[startDate.getMonth()] += duration[0] + (duration[1] / 60);
        }
        dataset[i] = {
            label: i+minYear + "",
            barPercentage: 0.5,
            backgroundColor: colors[(i*2)%12],
            minBarLength: 2,
            data: durationCache,
        };
    }
    if (barChart !== undefined)
        barChart.destroy();
    barChart = new Chart(canvas, {
        type: 'bar',
        data: {
            labels: label,
            datasets: dataset
        },
        options: {
            scales: {
                x: {
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    }
                },
                y: {
                    grid: {
                        color: 'rgba(255, 255, 255, 0.1)'
                    }
                }
            }
        }
    });

    for (let i = 0; i <= (maxYear - minYear); i++) {
        barChart.getDatasetMeta(i).hidden = true;
    }
    let date = new Date();
    barChart.getDatasetMeta(date.getFullYear()-minYear).hidden = false;
    barChart.update();
}
let stats = [];
let graph = null;

function getPlayerStats(name) {
    stats = [];
    const data = {"gamemode": "skyblock", "subgamemode": "Papaya", "name": name, "uuid": UUID, "all": true};

    $.ajax({

        url: "api/stats",
        method: "GET",
        data: data,
        dataType: "json",
        success: function (res) {
            if (res == null || res.stats == null || res.stats.length < 2)
                return;

            for (let i = 1; i < res.stats.length; i++) {
                const current_stat = res.stats[i];
                const stat_last = res.stats[i - 1];

                const ts = current_stat.timestamp + " EST";

                addValue(ts, getDifference(current_stat.value, stat_last.value), {"timestamp": ts});
            }
            console.log(res)
            createGraph();
        }

    });
}

function addValue(date, value, extra = {}) {
    const options = {
        timeZone: 'America/New_York',
    };

    const d = new Date(date).toLocaleDateString(undefined, options);

    let found = false;

    for (let i = 0; i < stats.length; i++) {
        if (stats[i].x === d) {
            found = true;
            stats[i] = {x: d, y: stats[i].y + value, extra: extra};
            break;
        }
    }

    if (!found) {
        stats.push({x: d, y: value, extra: extra});
}
}

function getDifference(val1 = 0, val2 = 0) {
    return val1 - val2;
}

function createGraph() {
    if (graph != null)
        graph.destroy();
    graph = new Chart("myChart", {
        type: "line",
        data: {
            labels: getXValues(stats),
            datasets: [{
                    label: 'Dataset 2',
                    fill: true,
                    lineTension: 0.0,
                    pointRadius: 0,
                    hoverRadius: 0,
                    backgroundColor: "rgb(120,250,0, 0.25)",
                    borderColor: "rgb(120,250,0, 1.0)",
                    data: getYValues(stats)
                }]
        },
        options: {
            animation: {
                duration: 0
            },
            scales: {
                y: {
                    offset: true,
                    min: getMinYValue(stats) < 0 ? getMinYValue(stats) : 0,
                    ticks: {
                        callback: function (value, index, array) {
                            return (Math.abs(value) < 1000000) ? (Math.abs(value) < 1000) ? value : value / 1000 + 'K' : value / 1000000 + 'M';
                        }
                    }
                },
            },
            plugins: {
                tooltip: {
                    callbacks: {
                        title: function (t, d) {
                            return null;
                        },
                        label: function (context) {
                            let value = context.raw || 0;
                            return ((value >= 0 ? " +" : " -") + ((Math.abs(value) < 1000000) ? (Math.abs(value) < 1000) ? value : value / 1000 + 'K' : value / 1000000 + 'M'));
                        }
                    },
                    interaction: {
                        mode: 'nearest',
                    },
                    intersect: false,
                    //displayColors: false,
                    bodyFont: {
                        size: 16.5
                    },
                }
            },

        }
    });
}

function getMaxYValue(json) {
    let max = json[0].y;
    for (let i = 0; i < json.length; i++) {
        if (max < json[i].y)
            max = json[i].y;
    }
    return max;
}

function getMinYValue(json) {
    let min = json[0].y;
    for (let i = 0; i < json.length; i++) {
        if (min > json[i].y)
            min = json[i].y;
    }
    return min;
}

function getXValues(json) {
    let x = [];
    for (let i = 0; i < json.length; i++) {
        x.push(json[i].x);
    }
    return x;
}

function getYValues(json) {
    let y = [];
    for (let i = 0; i < json.length; i++) {
        y.push(json[i].y);
    }
    return y;
}
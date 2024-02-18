let stats = [];

function getPlayerStats(uuid) {
    stats = [];
    const data = {"gamemode": "skyblock", "name": "SKILL_COMBAT", "subgamemode": "Papaya", "uuid": uuid, "all": true};

    $.ajax({

        url: "api/stats",
        method: "GET",
        data: data,
        //data: $("#searchform :input").filter(function() { return $(this).val() !== ''; }).serialize(),
        dataType: "json",
        success: function (res) {
            if (res == null || res.stats == null)
                return;

            for (let i = 0; i < res.stats.length; i++) {
                const stat = res.stats[i];
                const next_stat = res.stats[i+1];
                if(next_stat == null) break;
                addValue(stat.utc_timestamp, getDifference(stat.value, next_stat.value));   
            }

            createGraph();
        }

    });
}

function addValue(date, value) {
    const d = new Date(date).toLocaleDateString();
    
    let found = false;
    
    for(let i = 0; i < stats.length; i++) {
        if(stats[i].x === d) {
            found = true;
            stats[i] = { x: d, y: stats[i].y + value };
            break;
        }
    }

    if(!found) {
        stats.push({x: d, y: value });
    }
}

function getDifference(val1 = 0, val2 = 0) {
    return Math.abs(val1 - val2);
}

function createGraph() {
    new Chart("myChart", {
        type: "line",
        data: {
            labels: getXValues(stats),
            datasets: [{
                    pointRadius: 2,
                    pointBackgroundColor: "rgb(0,0,255)",
                    data: getYValues(stats)
                }]
        },
        options: {
            legend: {display: false},
            scales: {
                xAxes: [{ticks: {min: 40, max: 160}}],
                yAxes: [{ticks: {min: getMinYValue(stats) - 10, max: getMaxYValue(stats) + 10}}],
            }
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
var po = org.polymaps;
var map;
var unlNetwork;
var ukNetwork;
var states;
var counties;

var yearsBetween = 1900 - 1840;

var pathWidth = 367;
var initialPositionOnPath;

var selectedYear = 1840;

var visibleLines;

$(document).ready(function() {
    displayYear = 1840;
	
    var pathMin = 0;
    var pathMax = yearsBetween;
    var range = pathMax - pathMin;

    var initialPosition = 0;

    // set up initial position
    initialPositionOnPath = (((initialPosition - pathMin) / range) * (pathWidth * 2))
    - pathWidth;

    $('#number').css('left', initialPositionOnPath);
    $('#cursor').css('left', initialPositionOnPath);
    // $('#number').html(initialPosition);

    // draggable cursor
    $('#cursor')
    .draggable(
    {
        containment : "parent",
        axis : "x",
        grid : [ 7, 0 ],
        drag : function() {
            var percentOnPath = (parseFloat($(
                '#cursor').css('left')) + pathWidth)
            / (pathWidth * 2);

            $('#number').css('left',
                $('#cursor').css('left'));
            // $('#number').html(new_date.toDateString());
							
            $("#dateText").html('');
            $("#dateText").html(parseInt(1840+(yearsBetween*percentOnPath)));
        },
        stop : function() {
            var percentOnPath = (parseFloat($(
                '#cursor').css('left')) + pathWidth)
            / (pathWidth * 2);
							
            var year = parseInt(1840+(yearsBetween*percentOnPath));
            year = (year % 5) >= 2.5 ? parseInt(year / 5) * 5 + 5 : parseInt(year / 5) * 5;
            if ( year != selectedYear ) {
                selectedLines = new Array();
                lineColors = new Object();
                selectedYear = year;
                updateBoundaries(year);
                updateNetwork(year);
                $("#displayYear").html();
                $("#displayYear").html(selectedYear);
            }
        }
    });

    selectedLines = new Array();
    lineColors = new Object();
    map = po.map()
    .container(document.getElementById("map").appendChild(po.svg("svg")))
    .center({
        lat: 39, 
        lon: -96
    })
    .zoom(4)
    .add(po.interact());
			
    map.add(po.image()
        .url(po.url("http://{S}tile.cloudmade.com"
            + "/1a1b06b230af4efdbb989ea99e9841af" // http://cloudmade.com/register
            + "/20760/256/{Z}/{X}/{Y}.png")
        .hosts(["a.", "b.", "c.", ""])));
		
    map.container().setAttribute("class", "Test");
		
    updateBoundaries(1840);
    updateNetwork(1840);
});

function changeNetwork() {
    updateNetwork(selectedYear);
}

function updateBoundaries(year) {
    year = (Math.floor(year/10)*10);

    if ( year > 1870 ) {
        year = 1870;
    }

    var tmp = po.geoJson().url("../rest/boundaries/counties/"+year+".json?B={B}&Z={Z}").id("counties");
    if ( counties ) { 
        map.remove(counties);
    }
    map.add(tmp);
    counties = tmp;

    tmp = po.geoJson().url("../rest/boundaries/states/"+year+".json?B={B}&Z={Z}").id("states");
    if ( states ) {
        map.remove(states);
    }

    map.add(tmp);
    states = tmp;
}

function loadNetwork(e) {
    for ( var i = 0; i < e.features.length; i++) {
        var f = e.features[i], c = f.element;
        var name = e.features[i].data.properties.name;
        c.setAttribute("fille", "none");
        c.setAttribute("shape-rendering", "crispEdges")
        c.setAttribute("stroke-opacity", "0.75");
        c.setAttribute("stroke-width", 2);
        if ( $.inArray(name, selectedLines) > -1 ) {
            c.setAttribute("stroke", lineColors[name]);
        } else {
            c.setAttribute("stroke", "#000000");
        }
        
        if ( !visibleLines[name]) {
            visibleLines[name] = new Array();
        }
        visibleLines[name].push(c);
    }
}

var selectedLines;
var lineColors;
function toggleSelect(name) {
    var color = "";
    if ( $.inArray(name, selectedLines) > -1 ) {
        color = "#000000";
        selectedLines = $.grep(selectedLines, function(value) {
            return value != name;
        });
    } else {
        color = $("#highlightColor").val();
        lineColors[name] = color;
        selectedLines.push(name);
    }
    
    if ( visibleLines[name] ) { 
        len = visibleLines[name].length;
        for ( i=0; i<len; i++ ) {
            visibleLines[name][i].setAttribute("stroke", color);
        }
        if ( color == "#000000") {
            color = "transparent";
        }
        highlights[name].css("background-color", color);
    }
}

var highlights;
function didMoveMap() { 
    var extent = map.extent();
    var bounds = extent[0].lat+","+extent[0].lon+","+extent[1].lat+","+extent[1].lon;
    var authority =  $("#scale").val() == "national" ? 1 : 2;
    
    var url = "../rest/railroad/network/"+bounds+"/"+selectedYear+"/names.json?authority="+authority;
    
    $.getJSON(url, function(data) { 
        $("#owners").empty();
        if ( data && data.railroad ) {
            var tbl = $("<table>");
            tbl.css("width", "80%");
            var row = $("<tr>");
            var col = $("<th>");
            row.append(col);
            col = $("<th>");
            col.html("Railroad Name");
            row.append(col);
            col = $("<th>");
            col.css("text-align", "right");
            col.html("Miles of Track");
            row.append(col);
            tbl.append(row);

            $.each(data.railroad, function(i, railroad) {
                row = $("<tr>");
                col = $("<td>");
                if ( $.inArray(railroad.name, selectedLines) > -1 ) {
                    col.css("background-color", lineColors[railroad.name]);
                }
                highlights[railroad.name] = col;
                row.append(col);
                col = $("<td>");
                col.html("<a href=\"javascript:toggleSelect('"+railroad.name+"')\">"+railroad.name+"</a>");
                row.append(col);
                col = $("<td>");
                col.css("text-align", "right");
                col.html(railroad.trackMiles);
                row.append(col);
                tbl.append(row);
            });
            $("#owners").append(tbl);
        }
    });
}

function updateNetwork(year) {
    visibleLines = new Object();
    highlights = new Object();
    
    var unl_year;
    if ( year == 1860 ) {
        unl_year = 1861;
        selectedYear = 1861;
    } else { 
        unl_year = year;
    }

    if ( unlNetwork ) { 
        map.remove(unlNetwork);
    }

    if ( ukNetwork ) { 
        map.remove(ukNetwork);
    }

    if ( $("#scale").val() == "national" ) {
        unlNetwork = po.geoJson().url("../rest/railroad/network/1?year="+unl_year).tile(false).on("load", loadNetwork).id("unlRail").on("move", didMoveMap)
        map.add(unlNetwork);
        if ( ukNetwork ) {
            map.remove(ukNetwork);
            map.center({
                lat: 39, 
                lon: -96
            });
            map.zoom(4);
        } else { 
            didMoveMap();
        }
    }

    if ( $("#scale").val() == "regional" ) {
        ukNetwork = po.geoJson().url("../rest/railroad/network/2?year="+year).tile(false).on("load", loadNetwork).id("ukRail").on("move", didMoveMap);
        map.add(ukNetwork);
        if ( unlNetwork ) {
            map.remove(unlNetwork);
            map.center({
                lat: 40.691582716761815, 
                lon: -77.32974221027891
            });
            map.zoom(6.595000000000006);    
        } else { 
            didMoveMap();
        }
    }
}
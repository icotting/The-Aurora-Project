var po = org.polymaps;
var map;
var unlNetwork;
var ukNetwork;
var states;
var counties;
var displayYear;
var mapType = "slave";
var railYear = 1840;
var distributionChart;
var shiftChart;

var yearsBetween = 1870 - 1840;

var pathWidth = 367;
var initialPositionOnPath;

var features = new Array();
var selected_year = 1840;

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
							
							if ( year != selected_year ) {
								selected_year = year;		
							}
						}
					});
	
	map = po.map()
		.container(document.getElementById("map").appendChild(po.svg("svg")))
	    .center({lat: 39, lon: -96})
		 .zoom(4)
		.add(po.interact()).on("move", function() { 
			drawCharts(railYear);
		});
	
		map.add(po.image()
		.url(po.url("http://{S}tile.cloudmade.com"
		+ "/1a1b06b230af4efdbb989ea99e9841af" // http://cloudmade.com/register
		+ "/20760/256/{Z}/{X}/{Y}.png")
		.hosts(["a.", "b.", "c.", ""])));
    });
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
								if ( year == 1870 ) {
									updateBoundaries(1860);
								}
								updateBoundaries(year);
								updateNetwork(year);			
							}
						}
					});
	
	
	  distributionChart = new Highcharts.Chart({
	      chart: {
	         renderTo: 'populationPie',
	         plotBackgroundColor: null,
	         plotBorderWidth: null,
	         plotShadow: false
	      },
	      title: {
	         text: "Population Distribution"
	      },
	      tooltip: {
	         formatter: function() {
	            return '<b>'+ this.point.name +'</b>: '+ this.y+"%";
	         }
	      },
	      plotOptions: {
	         pie: {
	            allowPointSelect: true,
	            cursor: 'pointer',
	            dataLabels: {
	               enabled: false
	            },
	            showInLegend: true
	         }
	      },
	       series: [{
	         type: 'pie',
	         name: 'Population Distribution',
	         data: [
	            ['Free Persons', 0], 
	            ['Slaves', 0],
	            ['Other', 0]
	         ]
	      }]
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
		
		updateBoundaries(1840);
		updateNetwork(1840);
});

$(function() {

});

function drawCharts(railYear) {

	var extent = map.extent();
	var bounds = extent[0].lat+","+extent[0].lon+","+extent[1].lat+","+extent[1].lon;
	
	var url = "../rest/boundaries/"+(map.zoom() >= 6 ? "US_COUNTY" : "US_STATE")+"/"+displayYear+"/"+bounds+"/"+map.zoom()+"/stats.json?railYear="+railYear;
	
	var url2 = "../rest/boundaries/"+bounds+"/railPopulationShift.json";
	$.ajax({
		url: url2, 
		dataType: 'json', 
		success: function(json) {
						
			shiftChart = new Highcharts.Chart({
			      chart: {
			         renderTo: 'shiftChart',
			         zoomType: 'xy'
			      },
			      title: {
			         text: 'Population Shifts by Decade and Rail Network Growth'
			      },
			      subtitle: {
			         text: 'Source: University of Nebraska - Lincoln'
			      },
			      xAxis: [{
			         categories: ['1840', '1845', '1850', '1855', '1860']
			      }],
			      yAxis: [{ // Primary yAxis
			         labels: {
			            formatter: function() {
			               return addCommas(this.value);
			            },
			            style: {
			               color: '#89A54E'
			            }
			         },
			         title: {
			            text: 'Miles',
			            style: {
			               color: '#89A54E'
			            }
			         }
			      }, { // Secondary yAxis
			         title: {
			            text: 'Population',
			            style: {
			               color: '#4572A7'
			            }
			         },
			         labels: {
			            formatter: function() {
			               return addCommas(this.value);
			            },
			            style: {
			               color: '#4572A7'
			            }
			         },
			         opposite: true
			      }],
			      tooltip: {
			         formatter: function() {
			            return ''+
			               this.x +': '+ this.y +
			               (this.series.name == 'Miles of Track' ? ' Miles' : '');
			         }
			      },
			      legend: {
			         layout: 'vertical',
			         align: 'left',
			         x: 120,
			         verticalAlign: 'top',
			         y: 100,
			         floating: true,
			         backgroundColor: '#FFFFFF'
			      },
			      series: [{
			         name: 'Free Persons',
			         type: 'column',
			         yAxis: 1,
			         data: [json.timeSeries.population[0].freePersons,
			                 0,
			                 json.timeSeries.population[1].freePersons,
			                 0,
			                 json.timeSeries.population[2].freePersons]
			      }, 
			      {
				         name: 'Slaves',
				         type: 'column',
				         yAxis: 1,
				         data: [json.timeSeries.population[0].slaves,
				                 0,
				                 json.timeSeries.population[1].slaves,
				                 0,
				                 json.timeSeries.population[2].slaves]
				      
				  },
			      {
				         name: 'Other',
				         type: 'column',
				         yAxis: 1,
				         data: [json.timeSeries.population[0].other,
				                 0,
				                 json.timeSeries.population[1].other,
				                 0,
				                 json.timeSeries.population[2].other]
				      
				  },				  
			      {
			         name: 'Miles of Track',
			         type: 'spline',
			         data: [json.trackLengths[0].trackLength,
			                json.trackLengths[1].trackLength,
			                json.trackLengths[2].trackLength,
			                json.trackLengths[3].trackLength,
			                json.trackLengths[4].trackLength] 
			      }]
			   });			
		}
	});
	
	$.ajax({
		  url: url,
		  dataType: 'json',
		  success: function(json) {
			  
			  var total_pop = json.totals.free+json.totals.slave+json.totals.other;
			  var free_p = (json.totals.free/total_pop)*100;
			  free_p = Math.round(free_p*100)/100;

			  var slave_p = (json.totals.slave/total_pop)*100;
			  slave_p = Math.round(slave_p*100)/100;
			  
			  var other_p = (json.totals.other/total_pop)*100;
			  other_p = Math.round(other_p*100)/100;
			  
			  var ddata = [
				            ['Free Persons', free_p], 
				            ['Slaves', slave_p],
				            ['Other', other_p]
				         ];
			  distributionChart.series[0].setData(ddata, true);
			  distributionChart.setTitle({ text: displayYear+" Population Distribution"});
			  
			  var tbl = "<tr><th>Region</th><th>Free Population</th><th>Slave Population</th><th>Other Population</th><th>Miles of Track</th></tr>";
			  for ( var i=0; i<json.regions.length; i++ ) {
				  tbl += "<tr><td style=\"font-weight: bold;\">";
				  tbl += json.regions[i];
				  tbl += "</td><td>";
				  tbl += addCommas(json.regionTotals.free[i]);
				  tbl += "</td><td>";
				  tbl += addCommas(json.regionTotals.slave[i]);
				  tbl += "</td><td>";
				  tbl += addCommas(json.regionTotals.other[i]);
				  tbl += "</td><td>";
				  tbl += addCommas(json.regionTotals.trackmiles[i]);
				  tbl += "</td></tr>";
			  }
			  
			  tbl += "<tr><td style=\"border-top: 1px solid black; font-style: italic; font-weight: bold\">Totals</td>";
			  tbl += "<td style=\"border-top: 1px solid black\">";
			  tbl += addCommas(json.totals.free);
			  tbl += "</td><td style=\"border-top: 1px solid black\">";
			  tbl += addCommas(json.totals.slave);
			  tbl += "</td><td style=\"border-top: 1px solid black\">";
			  tbl += addCommas(json.totals.other);
			  tbl += "</td><td style=\"border-top: 1px solid black\">";
			  tbl += addCommas(json.totals.trackmiles);			  
			  tbl += "</td></tr>";
			  
			  $("#poptbl").html('');
			  $("#poptbl").html(tbl);
		  }
		});
}

function updateBoundaries(year) {
	railYear = year;
	round_year = (Math.floor(year/10)*10);
	displayYear = round_year;
	drawCharts(year);
	
	var tmp = po.geoJson().url("../rest/boundaries/counties/"+round_year+".json?B={B}&Z={Z}&population=true").id("counties").on("load", loadLabels).on("show", loadLabels);
	if ( counties ) { 
		map.remove(counties);
	}
	map.add(tmp);
	counties = tmp;
	
	tmp = po.geoJson().url("../rest/boundaries/states/"+round_year+".json?B={B}&Z={Z}&population=true").id("states").on("load", loadLabels).on("show", loadLabels);
	if ( states ) {
		map.remove(states);
	}
	
	map.add(tmp);
	states = tmp;	
}

function loadLabels(e) {	
	for (var i = 0; i < e.features.length; i++) {
		features.push(e.features[i]);
	}
	paintBoundaries(mapType);
}

function paintBoundaries(type) {
	
	$(".slaveLabel").hide();
	$(".popLabelCounty").hide();
	$(".popLabelState").hide();
	$(".inSlaveryLabel").hide();
	
	mapType = type;
	for (var i = 0; i < features.length; i++) {
		var feature = features[i];
				
		if ( map.zoom() >= 6 && feature.data.properties.boundaryType == 'US State' ) {
			feature.element.setAttribute("style", "fill: none;");
			feature.element.setAttribute("style", "stroke-width: 2px;");
		} else {
			if ( mapType == "slave") {
				$(".slaveLabel").show();
				var pop = feature.data.properties.slavePopulation;
				if ( pop == 0 ) {
					feature.element.setAttribute("style", "fill: white;");
				} else if ( pop >= 1 && pop < 1000 ) { 
					feature.element.setAttribute("style", "fill: rgb(222,235,247)");
				} else if ( pop >= 1000 && pop < 5000 ) {
					feature.element.setAttribute("style", "fill: rgb(198,219,239)");
				} else if ( pop >= 5000 && pop < 10000 ) {
					feature.element.setAttribute("style", "fill: rgb(158,202,225)");
				} else if ( pop >= 10000 && pop < 25000 ) {
					feature.element.setAttribute("style", "fill: rgb(107,174,214)");
				} else if ( pop >= 25000 && pop < 50000 ) {
					feature.element.setAttribute("style", "fill: rgb(66,146,198)");	
				} else if ( pop >= 50000 && pop < 100000 ) {
					feature.element.setAttribute("style", "fill: rgb(33,113,181)");
				} else if ( pop >= 100000 && pop < 250000 ) {
					feature.element.setAttribute("style", "fill: rgb(8,81,156)");			
				} else if ( pop >= 250000 ) {
					feature.element.setAttribute("style", "fill: rgb(8,48,107)");
				}
			} else if ( mapType == 'free' || mapType == 'population' ) {
				var pop = (mapType == 'free') ? feature.data.properties.freePopulation : feature.data.properties.totalPopulation;
				
				if ( map.zoom() >= 6 ) { // county population ranges
					$(".popLabelCounty").show();
					if ( pop >= 0 && pop < 5000 ) {
						feature.element.setAttribute("style", "fill: white;");
					} else if ( pop >= 5000 && pop < 10000 ) { 
						feature.element.setAttribute("style", "fill: rgb(222,235,247)");
					} else if ( pop >= 10000 && pop < 20000 ) {
						feature.element.setAttribute("style", "fill: rgb(198,219,239)");
					} else if ( pop >= 20000 && pop < 30000 ) {
						feature.element.setAttribute("style", "fill: rgb(158,202,225)");
					} else if ( pop >= 30000 && pop < 40000 ) {
						feature.element.setAttribute("style", "fill: rgb(107,174,214)");
					} else if ( pop >= 40000 && pop < 50000 ) {
						feature.element.setAttribute("style", "fill: rgb(66,146,198)");	
					} else if ( pop >= 50000 && pop < 60000 ) {
						feature.element.setAttribute("style", "fill: rgb(33,113,181)");
					} else if ( pop >= 60000 && pop < 70000 ) {
						feature.element.setAttribute("style", "fill: rgb(8,81,156)");			
					} else if ( pop >= 70000 ) {
						feature.element.setAttribute("style", "fill: rgb(8,48,107)");
					}
				} else { // state population ranges 
					$(".popLabelState").show();
					if ( pop >= 0 && pop < 50000 ) {
						feature.element.setAttribute("style", "fill: white;");
					} else if ( pop >= 50000 && pop < 100000 ) { 
						feature.element.setAttribute("style", "fill: rgb(222,235,247)");
					} else if ( pop >= 100000 && pop < 200000 ) {
						feature.element.setAttribute("style", "fill: rgb(198,219,239)");
					} else if ( pop >= 200000 && pop < 300000 ) {
						feature.element.setAttribute("style", "fill: rgb(158,202,225)");
					} else if ( pop >= 300000 && pop < 400000 ) {
						feature.element.setAttribute("style", "fill: rgb(107,174,214)");
					} else if ( pop >= 400000 && pop < 500000 ) {
						feature.element.setAttribute("style", "fill: rgb(66,146,198)");	
					} else if ( pop >= 500000 && pop < 600000 ) {
						feature.element.setAttribute("style", "fill: rgb(33,113,181)");
					} else if ( pop >= 600000 && pop < 700000 ) {
						feature.element.setAttribute("style", "fill: rgb(8,81,156)");			
					} else if ( pop >= 700000 ) {
						feature.element.setAttribute("style", "fill: rgb(8,48,107)");
					}
				}
			} else if ( mapType == 'inSlavery') {
				$(".inSlaveryLabel").show();
				var pop = (feature.data.properties.slavePopulation / feature.data.properties.totalPopulation) * 100;
				if ( pop >= 0 && pop < 5 ) {
					feature.element.setAttribute("style", "fill: white;");
				} else if ( pop >= 5 && pop < 10 ) { 
					feature.element.setAttribute("style", "fill: rgb(222,235,247)");
				} else if ( pop >= 10 && pop < 15 ) {
					feature.element.setAttribute("style", "fill: rgb(198,219,239)");
				} else if ( pop >= 15 && pop < 20 ) {
					feature.element.setAttribute("style", "fill: rgb(158,202,225)");
				} else if ( pop >= 20 && pop < 25 ) {
					feature.element.setAttribute("style", "fill: rgb(107,174,214)");
				} else if ( pop >= 25 && pop < 30 ) {
					feature.element.setAttribute("style", "fill: rgb(66,146,198)");	
				} else if ( pop >= 30 && pop < 40 ) {
					feature.element.setAttribute("style", "fill: rgb(33,113,181)");
				} else if ( pop >= 40 && pop < 50 ) {
					feature.element.setAttribute("style", "fill: rgb(8,81,156)");			
				} else if ( pop >= 50 ) {
					feature.element.setAttribute("style", "fill: rgb(8,48,107)");
				}
			}
		}
	}   
}

function updateNetwork(year) {
	$("#waitIndicator").show();

	var unl_year;
	if ( year == 1860 ) {
		unl_year = 1861;
	} else { 
		unl_year = year;
	}

	if ( unlNetwork ) { 
		map.remove(unlNetwork);
	}
	
	if ( ukNetwork ) { 
		map.remove(ukNetwork);
	}

	unlNetwork = po.geoJson().url("../rest/railroad/network/1?year="+unl_year).tile(false).on("load", load).id("unlRail");
	map.add(unlNetwork);
	
	ukNetwork = po.geoJson().url("../rest/railroad/network/2?year="+year).tile(false).on("load", load).id("ukRail");
	map.add(ukNetwork);

	$("#waitIndicator").hide();
}

function load(e) {
	  for (var i = 0; i < e.features.length; i++) {
	    var feature = e.features[i];
	    feature.element.setAttribute("class", "railClass");
	  }
	}

function addCommas(nStr) {
	nStr += '';
	x = nStr.split('.');
	x1 = x[0];
	x2 = x.length > 1 ? '.' + x[1] : '';
	var rgx = /(\d+)(\d{3})/;
	while (rgx.test(x1)) {
		x1 = x1.replace(rgx, '$1' + ',' + '$2');
	}
	return x1 + x2;
}
var po = org.polymaps;
var map;

var DAY = 1000 * 60 * 60 * 24;

var baseStartDate = new Date(1865, 5, 8);
var baseEndDate = new Date(1867, 7, 4);
var daysBetween = (baseEndDate.getTime() - baseStartDate.getTime()) / DAY;

var startDate = baseStartDate;
var endDate = baseStartDate;

var pathWidth = 367;
var initialPositionOnPath;

var currentLayer;
var days = 730;
var nodes;
var selectedLayer;

var fromDate;
var toDate;
var selectedDate;

$(document)
		.ready(
				function() {

					var pathMin = 0;
					var pathMax = daysBetween;
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
											var new_date = new Date(
													baseStartDate.getTime()
															+ (parseInt(percentOnPath
																	* range
																	+ pathMin) * DAY));

											$('#number').css('left',
													$('#cursor').css('left'));
											// $('#number').html(new_date.toDateString());
											plotForDate(new_date);
										},
										stop : function() {
											var percentOnPath = (parseFloat($(
													'#cursor').css('left')) + pathWidth)
													/ (pathWidth * 2);
											var new_date = new Date(
													baseStartDate.getTime()
															+ (parseInt(percentOnPath
																	* range
																	+ pathMin) * DAY));

											updateLayer(new_date);
										}
									});

					map = po.map().container(
							document.getElementById("map").appendChild(
									po.svg("svg"))).center({
						lat : 39,
						lon : -96
					}).zoom(4).add(po.interact());

					map
							.add(po
									.image()
									.url(
											po
													.url(
															"http://{S}tile.cloudmade.com"
																	+ "/1a1b06b230af4efdbb989ea99e9841af" // http://cloudmade.com/register
																	+ "/20760/256/{Z}/{X}/{Y}.png")
													.hosts(
															[ "a.", "b.", "c.",
																	"" ])));

					// map.add(po.geoJson().url("../rest/boundaries/states/1860.json?B={B}&Z={Z}").id("states").on("load",
					 //function(){
					 
					 
					  //}) );
					 
					updateLayer(baseStartDate);
				});

function updateLayer(date) {

	selectedDate = date;
	nodes = new Object();
	selectedNode = null;

	if (currentLayer)
		map.remove(currentLayer);

	fromDate = new Date(date.getTime() - (days / 2) * 24 * 60 * 60 * 1000);
	toDate = new Date(date.getTime() + (days / 2) * 24 * 60 * 60 * 1000);

	$("#from").html('');
	$("#from").html(fromDate.toDateString());
	$("to").html('');
	$("#to").html(toDate.toDateString());

	currentLayer = po.geoJson().url(
			"../rest/freedman/hiringOffices.json?date="
					+ fromDate.format('yyyy-MM-dd') + "&to="
					+ toDate.format('yyyy-MM-dd')).on("load", plotOffices);
	map.add(currentLayer);
}

var unlNetwork;
function updateRail() {
    var tmp;
    if ( $("#rn").val() != "none" ) {
        tmp = po.geoJson().url("../rest/railroad/network/1?year="+$("#rn").val()).tile(false).on("load", loadNetwork).id("unlRail");
    }
    
    if ( unlNetwork ) {
        map.remove(unlNetwork);
    }
    
    map.add(tmp);
    unlNetwork = tmp;
}

function loadNetwork(e) {
    for ( var i = 0; i < e.features.length; i++) {
        var f = e.features[i], c = f.element;
        var name = e.features[i].data.properties.name;
        c.setAttribute("fille", "none");
        c.setAttribute("shape-rendering", "crispEdges")
        c.setAttribute("stroke-opacity", "0.75");
        c.setAttribute("stroke-width", 2);
        c.setAttribute("stroke", "#000000");

        if ( !visibleLines[name]) {
            visibleLines[name] = new Array();
        }
        visibleLines[name].push(c);
    }
}


var selectedNode;

function selectPlace(p) {
	if (!p) {
		return;
	}

	if (selectedNode)
            selectedNode.setAttribute("fill", "#ffffff");

	selectedNode = p;
	selectedNode.setAttribute("fill", "#708496");

	if (selectedLayer)
		map.remove(selectedLayer);

	$("#selected_place").html("");
	$("#selected_place").html(nodes[selectedNode.id].placeName);
	
	selectedLayer = po.geoJson().url(
			"../rest/freedman/destinations/" + nodes[selectedNode.id].placeId
					+ ".json?date=" + fromDate.format('yyyy-mm-dd') + "&to="
					+ toDate.format('yyyy-mm-dd')).on("load", plotDestinations);
	
	$.getJSON("./rest/freedman/contracts/from/" + nodes[selectedNode.id].placeId
					+ "/"+fromDate.format('yyyy-mm-dd')+".json?to="
					+ toDate.format('yyyy-mm-dd'), function(data) {
					
		var html = "";
		var dest = -1;
		var place_list = new Array();

		var gender_counts = new Object();
		gender_counts['male'] = 0;
		gender_counts['female']= 0;

		var worker_counts = new Object();
		
		if ( !data ) { 
			data = { contract: new Array() };
		}
		
		$.each(data.contract, function(index, contract) { 
			
			var name = contract.worker.firstName+" "+contract.worker.lastName;

			if (contract.destination.id != dest) {
				
				html += "<a name="+contract.destination.id+"/>";
				if ( dest != -1 ) {
					html += "<div style=\"padding-top: 10px;\"><small><a href=\"javascript:scroll(0,0)\">Back to top</a></small></div>";
				}
				
				place_list.push(contract.destination);
				html += "<h3>To "+contract.destination.fullPlaceName+"</h3>";
				dest = contract.destination.id;
			}
			
			gender_counts[contract.worker.gender]++;

			if ( worker_counts[contract.position] ) {
				if ( worker_counts[contract.position][contract.worker.gender] ) {
					worker_counts[contract.position][contract.worker.gender].count++;
				} else {
					worker_counts[contract.position][contract.worker.gender] = {
							count: 1, 
							pay: 0
					};
				}
			} else { 
				var obj = new Object();
				obj[contract.worker.gender] = {
					count: 1,
					pay: 0
				};
				
				worker_counts[contract.position] = obj;
			}
			
			if ( parseInt(contract.rateOfPay) ) {
				worker_counts[contract.position][contract.worker.gender].pay += parseInt(contract.rateOfPay);
			}
			
			var cdate = (contract.contractTime == -1) ? "Unknown" : new Date(contract.contractDate).format("mmm dd, yyyy");
			var aged = (parseInt(contract.ageAtContractOutset) > 0) ? ", age "+contract.ageAtContractOutset : "";
			
			html += "<table style=\"width: 500px; margin-bottom: 20px;\">";
			html += "<tr><td colspan=\"4\">Contract Issued on "+cdate+" to "+name+aged+"</td></tr>";
			
			var duration = "";
			if ( contract.lengthOfServiceMonths && contract.lengthOfServiceMonths != "unspecified") { 
				duration = " for "+contract.lengthOfServiceMonths+" months";
			}
			
			html += "<tr><td colspan=\"4\">Employed as "+contract.position+duration+"</td></tr>";
			if (contract.employerAgent)
				html += "<tr><td>Employer Agent:</td><td colspan=\"3\">"+contract.employerAgent+"</td></tr>";
			
			html += "<tr>";
			html += "<td>Worker Gender:</td><td style=\"width: 75px;\">"+contract.worker.gender+"</td><td>Work Class:</td><td style=\"width: 150px;\">"+contract.workClass+"</td></tr>";
			html += "<tr><td>Rate of pay:</td><td style=\"width: 75px;\">"+contract.rateOfPay+"</td><td>Remuneration:</td><td style=\"width: 150px;\">"+contract.renumeration+"</td></tr>";
			
			if (contract.comments) {
				html += "<tr><td>Comments:</td>";
				html += "<td colspan=\"3\">"+contract.comments+"</td></tr>";
			}
			
			html += "</table>";
			
		});	
		
		var positions = new Array();
		var series = new Array();
		
		series[0] = {
				type: 'bar',
		        name: 'Men',
		        data: new Array()
		};
		
		series[1] = {
				type: 'bar',
		        name: 'Women',
		        data: new Array()
		};
		
		series[2] = {
				type: 'spline',
		        name: 'Pay for Men',
		        data: new Array()
		};

		series[3] = {
				type: 'spline',
		        name: 'Pay for Women',
		        data: new Array()
		};
		
		series[4] = {
		         type: 'pie',
		         name: 'Gender Distribution',
		         data: [{
		            name: 'Men',
		            y: gender_counts['male'],
		            color: Highcharts.getOptions().colors[0]
		         }, {
		            name: 'Women',
		            y: gender_counts['female'],
		            color: Highcharts.getOptions().colors[1]
		         }],
		         center: [500, 450],
		         size: 100,
		         showInLegend: false,
		         dataLabels: {
		            enabled: false
		         }
		};
		
		var i=0;
		$.each(worker_counts, function(key, value) {
			positions[i] = key;
			var men;
			var women;
			var mpay = 0;
			var wpay = 0;
			
			if ( value['male'] ) {
				men = value['male'].count;
				mpay = value['male'].pay;
			} else 
				men = 0;
			
			if ( value['female'] ) { 
				women = value['female'].count;
				wpay = value['female'].pay;
			}
			else 
				women = 0;
			
			series[0].data.push(men);
			series[1].data.push(women);
			series[2].data.push((men == 0) ? 0 : mpay/men);
			series[3].data.push((women == 0) ? 0 : wpay/women);
			i++;
		});
		
		new Highcharts.Chart({
		      chart: {
		         renderTo: 'workerChart'
		      },
		      title: {
		         text: nodes[selectedNode.id].placeName+' Worker Statistics'
		      },
		      xAxis: {
		         categories: positions
		      },
		      tooltip: {
		         formatter: function() {
		            var s;
		            if (this.point.name) { // the pie chart
		               s = ''+
		                  this.point.name +': '+ this.y;
		            } else {
		               s = ''+
		                  this.x  +': '+ this.y;
		            }
		            return s;
		         }
		      },
		      labels: {
		         items: [{
		            html: 'Gender Distribution',
		            style: {
		               left: '440px',
		               top: '370px',
		               color: 'black'            
		            }
		         }]
		      },
		      series: series
		   });
		
		
		$("#contract_list").html("");
		$("#contract_list").html(html);
		
		html = "<ul id=\"plist\">";
		
		$.each(place_list, function(index, place) { 
			html += "<li><a href=#"+place.id+">"+place.fullPlaceName+"</a>";
		});
		html += "</ul>";
		$("#place_list").html("");
		$("#place_list").html(html);
	});
	
	map.add(selectedLayer);
}

function plotDestinations(e) {
	for ( var i = 0; i < e.features.length; i++) {
		var f = e.features[i], c = f.element;

		var tile = e.tile, g = tile.element;
		var count = e.features[i].data.properties.contractCount;

		c.setAttribute("r", Math.pow(2, tile.zoom - 11) * (10 * count));
		c.setAttribute("fill", "#6B966A");
		c.setAttribute("fill-opacity", "0.65");
		c.setAttribute("stroke-width", 2);
		c.setAttribute("stroke", "#272727");
	}
}

var targetMap = new Object();

function selectFromLink(place) {
    selectPlace(targetMap[place]);
}

function plotOffices(e) {
        $("#hiringOffices").html("");
	for ( var i = 0; i < e.features.length; i++) {
		var f = e.features[i], c = f.element;
		g = f.element = po.svg("image");

		var tile = e.tile, g = tile.element;

		var count = e.features[i].data.properties.contractCount;

		c.setAttribute("r", Math.pow(2, tile.zoom - 11) * (10 * count));
		c.setAttribute("fill", "#ffffff");
		c.setAttribute("fill-opacity", "0.8");
		c.setAttribute("stroke-width", 2);
		c.setAttribute("stroke", "#272727");

		nodes[i] = e.features[i].data.properties;
		c.id = i;
                
                targetMap[e.features[i].data.properties.placeName] = c;
                $("#hiringOffices").append("<a href=\"javascript:selectFromLink('"+e.features[i].data.properties.placeName+"')\">"+e.features[i].data.properties.placeName+"</a><br/>");
		if (!selectedNode)
			selectPlace(c);

		c.onclick = function(e) {
			selectPlace(e.target);
		};
	}
}

$(function() {
	$("#datepicker").datepicker(
			{
				onSelect : function(dateText, inst) {
					var date_obj = $("#datepicker").datepicker("getDate");
					var slide_position = (date_obj.getTime() - baseStartDate
							.getTime())
							/ DAY;

					var percent_on_path = slide_position / (daysBetween + 1);
					if (percent_on_path < 0) {
						percent_on_path = 0;
					}
					if (percent_on_path > 1) {
						percent_on_path = 1;
					}

					// 764 is the global width of the timeline image
					var left = initialPositionOnPath + (729 * percent_on_path);
					$("#cursor").css('left', left);

					plotForDate(date_obj);
					updateLayer(date_obj);
				},
				defaultDate : baseStartDate
			});
	$("#dateText").html($("#datepicker").datepicker("getDate").toDateString());
});

function updateDays() {
	days = parseInt($("#dspan").val()) * 30;
	updateLayer(selectedDate);
	selectPlace(selectedNode);
}

function updateText() {
	plotForDate($("#datepicker").datepicker("getDate"));
}

function plotForDate(date) {
	$("#dateText").html('');
	$("#dateText").html(date.toDateString());
	$("#datepicker").datepicker("setDate", date);
}
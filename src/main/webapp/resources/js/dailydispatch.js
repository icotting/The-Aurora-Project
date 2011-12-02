var po = org.polymaps;
var map;
var currentLayer;
var nodes;
var selectedDate;
var selectedNode;

var DAY = 1000 * 60 * 60 * 24;

var baseStartDate = new Date(1860, 10, 1);
var baseEndDate = new Date(1865, 11, 31);
var daysBetween = (baseEndDate.getTime() - baseStartDate.getTime()) / DAY;

var startDate = baseStartDate;
var endDate = baseStartDate;

var pathWidth = 367;
var initialPositionOnPath;

$(document).ready(
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
									$("#datepicker").datepicker("setDate", new_date);
									$("#dateText").html('');
									$("#dateText").html(new_date.toDateString());
									// $('#number').html(new_date.toDateString());
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
									
									plotForDate(new_date);
									
								}
							});
			
			
			
			map = po.map().container(
					document.getElementById("map").appendChild(po.svg("svg")))
					.center({
						lat : 30,
						lon : 0
					}).zoom(2).add(po.interact());

			map.add(po.image().url(
					po.url(
							"http://{S}tile.cloudmade.com"
									+ "/1a1b06b230af4efdbb989ea99e9841af" // http://cloudmade.com/register
									+ "/20760/256/{Z}/{X}/{Y}.png").hosts(
							[ "a.", "b.", "c.", "" ])));
			
			plotForDate(baseStartDate);

		});

/*
$(function() {
	$("#slider").slider(
			{
				range : false,
				min : 0,
				max : daysBetween,
				value : 0,
				stop : function(event, ui) {
					var new_date = new Date(baseStartDate.getTime()
							+ (ui.value * DAY));
					plotForDate(new_date);
				},
				slide : function(event, ui) {
					var new_date = new Date(baseStartDate.getTime()
							+ (ui.value * DAY));
					$("#dateText").html('');
					$("#dateText").html(new_date.toDateString());
				}
			});
});
*/

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
				},
				defaultDate : baseStartDate
			});
	$("#dateText").html($("#datepicker").datepicker("getDate").toDateString());
});

function updateText() {
	plotForDate($("#datepicker").datepicker("getDate"));
}

function plotForDate(date) {
	$("#chart").html('');
	$("#text").html('');
	$("#dateText").html(date.toDateString());
	$("#datepicker").datepicker("setDate", date);
	selectedDate = date;
	selectedNode = null;
	
	var to_date;

	if ($.trim($("#daySpan").val()) != "") {
		var days = parseInt($("#daySpan").val());
		if (days > 0) {
			to_date = new Date(date.getTime() + days * 24 * 60 * 60 * 1000);
		}
	}
	
	if (currentLayer)
		map.remove(currentLayer);

	nodes = new Object();
	
	var url = "../rest/dispatchXML/places/geo/dates/" + date.format('yyyy-mm-dd')
			+ ".json";

	if (to_date) {
		url += "?to=" + to_date.format('yyyy-mm-dd');
	}

	if ($.trim($("#constraint").val()) != "") {
		url += "&constraint=" + $("#constraint").val();
	}

	currentLayer = po.geoJson().url(url).on("load", plotPlaces);
	map.add(currentLayer);
}

function plotPlaces(e) {
	for ( var i = 0; i < e.features.length; i++) {
		var f = e.features[i], c = f.element;
		g = f.element = po.svg("image");

		var tile = e.tile, g = tile.element;

		var count = e.features[i].data.properties.referenceCount;		

		var r;
		if (count > 0
				&& count < 5) {
			r = 500;
		} else if (count > 5
				&& count < 10) {
			r = 700;
		} else if (count > 10
				&& count < 15) {
			r = 800;
		} else if (count > 15
				&& count < 20) {
			r = 900;
		} else if (count > 20
				&& count < 25) {
			r = 1000;
		} else if (count > 25
				&& count < 30) {
			r = 1200;
		} else if (count > 30
				&& count < 35) {
			r = 1500;
		} else if (count > 35
				&& count < 40) {
			r = 1800;
		} else if (count > 40
				&& count < 45) {
			r = 2000;
		} else if (count > 50) {
			r = 2200;
		} else {
			r = 2500;
		}
		
		
		if ( e.features[i].data.properties.placeKey == 'tgn,7013964') {
			c.setAttribute("r", 0); // ignore richmond		
		} else {
			c.setAttribute("r", Math.pow(2, tile.zoom - 11) * r);
		}
		c.setAttribute("fill", "#ffffff");
		c.setAttribute("fill-opacity", "0.8");
		c.setAttribute("stroke-width", 2);
		c.setAttribute("stroke", "#272727");

		nodes[i] = e.features[i].data.properties;
		c.id = i;

		if (!selectedNode && e.features[i].data.properties.placeKey != 'tgn,7013964' ) {
			chartPlace(c, selectedDate);
			selectedNode = c;
		}

		c.onclick = function(e) {
			chartPlace(e.target, selectedDate);
		};
	}
}

/*
function plotForDate(date) {

	$("#chart").html('');
	$("#text").html('');

	$("#dateText").html('');
	$("#dateText").html(date.toDateString());

	$.each(routes, function(i, route) {
		route.setMap(null);
	});

	$.each(markers, function(i, marker) {
		marker.setMap(null);
	});

	$("#datepicker").datepicker("setDate", date);

	var to_date;

	if ($.trim($("#daySpan").val()) != "") {
		var days = parseInt($("#daySpan").val());
		if (days > 0) {
			to_date = new Date(date.getTime() + days * 24 * 60 * 60 * 1000);
		}
	}

	var url = "../rest/dispatchXML/places/dates/" + date.format('yyyy-mm-dd')
			+ ".json";

	if (to_date) {
		url += "?to=" + to_date.format('yyyy-mm-dd');
	}

	if ($.trim($("#constraint").val()) != "") {
		url += "&constraint=" + $("#constraint").val();
	}

	$.getJSON(url, function(data) {
		if (data) {
			var bounds = new google.maps.LatLngBounds();
			chartPlace(data.references[0].placeKey, date);
			$.each(data.references, function(i, reference) {
				if (parseFloat(reference.latitude) != -1) {
					var color;
					var weight;
					if (reference.referenceCount > 0
							&& reference.referenceCount < 5) {
						color = "#952c2c";
						weight = 1;
					} else if (reference.referenceCount > 5
							&& reference.referenceCount < 10) {
						color = "#95942c";
						weight = 2;
					} else if (reference.referenceCount > 10
							&& reference.referenceCount < 15) {
						color = "#67952c";
						weight = 2;
					} else if (reference.referenceCount > 15
							&& reference.referenceCount < 20) {
						color = "#2c9533";
						weight = 3;
					} else if (reference.referenceCount > 20
							&& reference.referenceCount < 25) {
						color = "#2c9582";
						weight = 3;
					} else if (reference.referenceCount > 25
							&& reference.referenceCount < 30) {
						color = "#2c6f95";
						weight = 4;
					} else if (reference.referenceCount > 30
							&& reference.referenceCount < 35) {
						color = "#2c4f95";
						weight = 4;
					} else if (reference.referenceCount > 35
							&& reference.referenceCount < 40) {
						color = "#382c95";
						weight = 5;
					} else if (reference.referenceCount > 40
							&& reference.referenceCount < 45) {
						color = "#652c95";
						weight = 5;
					} else if (reference.referencecount > 50) {
						color = "#952c8c";
						weight = 6;
					} else {
						weight: 7;
						color = "#ffffff";
					}

					var route = new google.maps.Polyline({
						strokeColor : "#000000",
						strokeOpacity : 1.0,
						strokeWeight : weight
					});

					var path = route.getPath();
					path.push(new google.maps.LatLng(37.540700, -77.433654)); // this
					// is
					// Richmond,
					// VA
					var point = new google.maps.LatLng(
							parseFloat(reference.latitude),
							parseFloat(reference.longitude));
					path.push(point);
					bounds.extend(point);
					routes.push(route);
					route.setMap(map);

					var circle = new google.maps.Circle({
						map : map,
						center : point,
						radius : 200,
						strokeColor : '#000000',
						fillColor : '#000000',
						strokeWeight : 2,
						fillOpacity : .60
					});

					var marker = new google.maps.Marker({
						position : point,
						map : map,
						title : "A place",
						icon : 'resources/images/template/marker1.png'
					});
					circle.bindTo('center', marker, 'position');

					markers.push(marker);
					google.maps.event.addListener(marker, 'click', function() {
						chartPlace(reference.placeKey, date);
					});
				}
			});

			map.fitBounds(bounds);
			// map.setZoom(map.getZoom()+1);
		}
	});
}
*/

function chartPlace(p, date) {

	var placeKey = nodes[p.id].placeKey
	
	if (selectedNode)
		selectedNode.setAttribute("fill", "#ffffff");

	selectedNode = p;
	selectedNode.setAttribute("fill", "#708496");
	
	var end_date;

	if ($.trim($("#daySpan").val()) != "") {
		var days = parseInt($("#daySpan").val());
		if (days > 60) {
			end_date = new Date(date.getTime() + days * 24 * 60 * 60 * 1000);
		} else if (days > 0) {
			end_date = new Date(date.getTime() + (DAY * 60));
		}
	}

	if (!end_date) {
		end_date = new Date(date.getTime() + (DAY * 60));
	}

	var url = "../rest/dispatchXML/places/" + placeKey + ".json?rangeStart="
			+ date.format('yyyy-mm-dd') + "&rangeEnd="
			+ end_date.format('yyyy-mm-dd');

	$.getJSON(url, function(json) {
		if (json) {

			var series = new Array();
			var arr = new Array();
			$.each(json.references.referenceCounts, function(i, count) {
				arr.push(parseInt(count));
			});

			series.push({
				name : json.references.placeName,
				data : arr
			});

			var chart = new Highcharts.Chart({
				chart : {
					renderTo : 'chart',
					defaultSeriesType : 'line',
					marginRight : 0
				},
				title : {
					text : 'Instances of references to '
							+ json.references.placeName
				},
				subtitle : {
					text : 'Source: Richmond Daily Dispatch XML from '
							+ date.format('mmm dd, yyyy') + " to "
							+ end_date.format('mmm dd, yyyy')
				},
				tooltip : {
					formatter : function() {
						return '' + this.series.name + ' referenced ' + this.y
								+ ' times on ' + this.x;
					}
				},
				xAxis : {
					categories : json.dates,
					labels : {
						enabled : false
					},
					title : {
						text : null
					}
				},
				yAxis : {
					min : 0,
					title : {
						text : 'Number of References',
						align : 'high'
					}
				},
				plotOptions : {
					bar : {
						dataLabels : {
							enabled : true
						}
					}
				},
				legend : {
					enabled : false,
					layout : 'vertical',
					align : 'right',
					verticalAlign : 'top',
					x : 0,
					y : 100,
					borderWidth : 1,
					backgroundColor : '#FFFFFF',
					width : 300
				},
				credits : {
					enabled : false
				},
				series : series
			});
		} else {
			$("#results").append("No results for " + placeKey);
		}
	});

	renderText(placeKey, date);
}

function renderText(placeKey, date) {

	var to_date;

	if ($.trim($("#daySpan").val()) != "") {
		var days = parseInt($("#daySpan").val());
		if (days > 0) {
			to_date = new Date(date.getTime() + days * 24 * 60 * 60 * 1000);
		}
	}

	var url = "../rest/dispatchXML/places/" + placeKey + "/text/"
			+ date.format('yyyy-mm-dd') + ".html";

	if (to_date) {
		url += "?to=" + to_date.format('yyyy-mm-dd');
	}

	if ($.trim($("#constraint").val()) != "") {
		url += "&constraint=" + $("#constraint").val();
	}

	$.ajax({
		type : "GET",
		url : url,
		success : function(data) {
			$("#text").html('');
			$("#text").html(data);
		}
	});
}

function clearConstraints() {
	$("#constraint").val('');
	$("#daySpan").val('');
	updateText();
}
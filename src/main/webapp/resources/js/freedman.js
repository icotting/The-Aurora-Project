var po = org.polymaps;
var map;

var DAY = 1000*60*60*24;

var baseStartDate = new Date(1865,5,8);
var baseEndDate = new Date(1867,7,4);
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

$(document).ready(function() {

	var pathMin = 0;
	var pathMax = daysBetween;
	var range = pathMax - pathMin;
	
	var initialPosition = 0;

	//set up initial position
	initialPositionOnPath = (((initialPosition-pathMin)/range) * (pathWidth*2)) - pathWidth;		
	
	$('#number').css('left', initialPositionOnPath);
	$('#cursor').css('left', initialPositionOnPath);
	//$('#number').html(initialPosition);

	//draggable cursor
	$('#cursor').draggable(
	{ 
		containment: "parent", 
		axis: "x", grid: [7, 0],
		drag: function() 
		{
			var percentOnPath = (parseFloat($('#cursor').css('left'))+pathWidth)/(pathWidth*2);
			var new_date = new Date(baseStartDate.getTime()+(parseInt(percentOnPath*range + pathMin)*DAY));
			
			$('#number').css('left', $('#cursor').css('left'));
			//$('#number').html(new_date.toDateString());
			plotForDate(new_date);
		}, 
		stop: function() {
			var percentOnPath = (parseFloat($('#cursor').css('left'))+pathWidth)/(pathWidth*2);
			var new_date = new Date(baseStartDate.getTime()+(parseInt(percentOnPath*range + pathMin)*DAY));

			updateLayer(new_date);
		}
	});
	
	map = po.map()
	.container(document.getElementById("map").appendChild(po.svg("svg")))
    .center({lat: 39, lon: -96})
	 	.zoom(4)
	.add(po.interact());
		
	map.add(po.image()
	.url(po.url("http://{S}tile.cloudmade.com"
	+ "/1a1b06b230af4efdbb989ea99e9841af" // http://cloudmade.com/register
	+ "/20760/256/{Z}/{X}/{Y}.png")
	.hosts(["a.", "b.", "c.", ""])));
	
	/*map.add(po.geoJson().url("../rest/boundaries/states/1860.json?B={B}&Z={Z}").id("states").on("load", function(){
		
		
		
	}) ); */
	
	updateLayer(baseStartDate);
});

function updateLayer(date) {

	selectedDate = date;
	nodes = new Object();
	selectedNode = null;
	
	if ( currentLayer ) 
		map.remove(currentLayer);
	
	fromDate = new Date(date.getTime() - (days/2)*24*60*60*1000);
	toDate = new Date(date.getTime() + (days/2)*24*60*60*1000);
	
	$("#from").html('');
	$("#from").html(fromDate.toDateString());
	$("to").html('');
	$("#to").html(toDate.toDateString());
	
	currentLayer = po.geoJson().url("../rest/freedman/hiringOffices.json?date="+fromDate.format('yyyy-MM-dd')+"&to="+toDate.format('yyyy-MM-dd')).on("load", plotOffices);
	map.add(currentLayer);
}

var selectedNode;

function selectPlace(p) {
	
		if ( !p ) { return; }
		
   		if ( selectedNode ) 
   			selectedNode.setAttribute("fill", "#ffffff");
   		
   		selectedNode = p;
   		selectedNode.setAttribute("fill", "#708496");
   		
   		if ( selectedLayer ) 
   			map.remove(selectedLayer);
   		
   		selectedLayer = po.geoJson().url("../rest/freedman/destinations/"+nodes[selectedNode.id].placeId+
   				".json?date="+fromDate.format('yyyy-MM-dd')+"&to="+toDate.format('yyyy-MM-dd')).on("load", plotDestinations);
   		map.add(selectedLayer);
}

function plotDestinations(e) {
	  for (var i = 0; i < e.features.length; i++) {
		    var f = e.features[i],
		        c = f.element;
		       	
		        var tile = e.tile, g = tile.element;
		        var count = e.features[i].data.properties.contractCount;

		       	c.setAttribute("r", Math.pow(2, tile.zoom - 11) * (10*count));
		       	c.setAttribute("fill", "#6B966A");
		       	c.setAttribute("fill-opacity", "0.65");
		       	c.setAttribute("stroke-width", 2);
		       	c.setAttribute("stroke", "#272727");
	  }
}

function plotOffices(e) {
	  for (var i = 0; i < e.features.length; i++) {
		    var f = e.features[i],
		        c = f.element;
		       	g = f.element = po.svg("image");
		       	
		        var tile = e.tile, g = tile.element;
		       	
		        var count = e.features[i].data.properties.contractCount;

		       	c.setAttribute("r", Math.pow(2, tile.zoom - 11) * (15*count));
		       	c.setAttribute("fill", "#ffffff");
		       	c.setAttribute("fill-opacity", "0.8");
		       	c.setAttribute("stroke-width", 2);
		       	c.setAttribute("stroke", "#272727");
		        
		       	nodes[i] = e.features[i].data.properties;
		       	c.id = i;
		       	
		       	if ( !selectedNode )
		       		selectPlace(c);
		       	
		       	c.onclick = function(e) { selectPlace(e.target); };
		       	
		   /* g.setAttributeNS(po.ns.xlink, "href", "./resources/images/cursor.png");
		    g.setAttribute("width", 17);
		    g.setAttribute("height", 27);
		    g.setAttribute("x", -8);
		    g.setAttribute("y", -8);
		    g.setAttribute("transform", c.getAttribute("transform"));
		    
		    c.parentNode.replaceChild(g, c); */
	  }
}

$(function() {
	$( "#datepicker" ).datepicker({
		 onSelect: function(dateText, inst) { 
			var date_obj = $("#datepicker").datepicker("getDate");
			var slide_position = (date_obj.getTime()-baseStartDate.getTime())/DAY;
			
			var percent_on_path = slide_position/(daysBetween+1);
			if ( percent_on_path < 0 ) { percent_on_path = 0; }
			if ( percent_on_path > 1 ) { percent_on_path = 1; }
			
			// 764 is the global width of the timeline image
			var left = initialPositionOnPath+(729*percent_on_path);
			$("#cursor").css('left', left);
			
			plotForDate(date_obj);
			updateLayer(date_obj);
		}, 
		defaultDate: baseStartDate
	});
	$("#dateText").html($("#datepicker").datepicker("getDate").toDateString());
});

function updateDays() {
	days = parseInt($("#dspan").val())*30;
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
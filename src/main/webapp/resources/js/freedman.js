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
	
	
	map.add(po.geoJson().url("../rest/freedman/hiringOffices.json").on("load", plotOffices));
});

function plotOffices(e) {
	  for (var i = 0; i < e.features.length; i++) {
		    var f = e.features[i],
		        c = f.element;
		       	g = f.element = po.svg("image");
		    
		    g.setAttributeNS(po.ns.xlink, "href", "./resources/images/cursor.png");
		    g.setAttribute("width", 17);
		    g.setAttribute("height", 27);
		    g.setAttribute("x", -8);
		    g.setAttribute("y", -8);
		    g.setAttribute("transform", c.getAttribute("transform"));
		   
		    
		    //c.parentNode.appendChild(g);
		    //c.parentNode.replaceChild(g, c);
		    
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
		}, 
		defaultDate: baseStartDate
	});
	$("#dateText").html($("#datepicker").datepicker("getDate").toDateString());
});

function updateText() {
	plotForDate($("#datepicker").datepicker("getDate"));
}

function plotForDate(date) { 	
	$("#dateText").html('');
	$("#dateText").html(date.toDateString());
	$("#datepicker").datepicker("setDate", date);
}
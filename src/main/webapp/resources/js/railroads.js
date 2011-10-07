var po = org.polymaps;
var map;
var unlNetwork;
var ukNetwork;
var states;
var counties;

$(document).ready(function() {
	map = po.map()
		.container(document.getElementById("map").appendChild(po.svg("svg")))
	    .center({lat: 39, lon: -96})
		 	.zoom(4)
		.add(po.interact())
		.add(po.hash());
			
		map.add(po.image()
		.url(po.url("http://{S}tile.cloudmade.com"
		+ "/1a1b06b230af4efdbb989ea99e9841af" // http://cloudmade.com/register
		+ "/20760/256/{Z}/{X}/{Y}.png")
		.hosts(["a.", "b.", "c.", ""])));
		
		map.container().setAttribute("class", "Test");
		
		/*map.add(po.image()
		    .url(po.url("http://{S}tile.cloudmade.com"
		    + "/1a1b06b230af4efdbb989ea99e9841af" // http://cloudmade.com/register
		    + "/998/256/{Z}/{X}/{Y}.png")
		    .hosts(["a.", "b.", "c.", ""])));
		*/
		/*map.add(po.image()
		    .url("http://s3.amazonaws.com/com.modestmaps.bluemarble/{Z}-r{Y}-c{X}.jpg"));
			*/
	
		updateBoundaries(1845);
		updateNetwork(1845);
		
		//$.getJSON('../rest/railroad/network?year=1865', function(data) { 
		//	map.add(po.geoJson().features(data));
		//});
		
		//map.add(po.compass().pan("none"));
});

$(function() {
	$( "#slider" ).slider({
		range: false,
		step: 5,
		min: 1845,
		max: 1900,
		value: 1845,
		stop: function( event, ui ) {
			
		}, 
		slide: function( event, ui ) { 
			$("#dateText").html('');
			$("#dateText").html(ui.value);
			updateBoundaries(ui.value);
			updateNetwork(ui.value);

		}
	});
});

function change() {
	map.container().setAttribute("class", "Test2");
}

function changeNetwork() {
	updateNetwork($( "#slider" ).slider('value'));
}

function updateBoundaries(year) {
	year = (Math.floor(year/10)*10);
	
	if ( year > 1870 ) { year = 1870; }
	
	var tmp = po.geoJson().url("../rest/boundaries/counties/"+year+".json?B={B}&Z={Z}").id("counties").on("load", loadLabels);
	if ( counties ) { 
		map.remove(counties);
	}
	map.add(tmp);
	counties = tmp;
	
	tmp = po.geoJson().url("../rest/boundaries/states/"+year+".json?B={B}&Z={Z}").id("states").on("load", loadLabels);
	if ( states ) {
		map.remove(states);
	}
	
	map.add(tmp);
	states = tmp;
}

function loadLabels(e) {

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

	if ( $("#unlRail").prop("checked") == true ) {
		unlNetwork = po.geoJson().url("../rest/railroad/network/1?year="+unl_year).tile(false).on("load", load).id("unlRail");
		map.add(unlNetwork);
	}
	
	if ( $("#ukRail").prop("checked") == true ) {
		ukNetwork = po.geoJson().url("../rest/railroad/network/2?year="+year).tile(false).id("ukRail");
		map.add(ukNetwork);
	}
	$("#waitIndicator").hide();
}

function load(e) {
	  for (var i = 0; i < e.features.length; i++) {
	    var feature = e.features[i];
	    feature.element.setAttribute("class", "redColor");
	  }
	}
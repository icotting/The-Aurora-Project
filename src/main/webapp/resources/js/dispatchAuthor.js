var places;
var default_center = new google.maps.LatLng(44, -50);
var default_zoom = 3;
var map;
var prevMarker;
var geocoder; 

$(document).ready(function() {
	loadPlaces();

	var myOptions = {
		zoom : default_zoom,
		center : default_center,
		mapTypeId : google.maps.MapTypeId.TERRAIN,
		disableDefaultUI : false
	};

	geocoder = new google.maps.Geocoder();
	map = new google.maps.Map(document.getElementById("map"), myOptions);
});

function loadPlaces() {
	$("#placeCol").html('Loading Place Data ...');
	$.getJSON("../rest/author/dispatchXML/unreviewed.json", function(data) {
		if (data) {
			$("#placeCol").html("<h3>Places</h3><ul id=\"placeNames\"></ul>");
			places = data;
			$.each(data.place, function(i, place) {
				$("#placeNames").append(
						'<li id=\"placeLink' + i
								+ '\"><div class="placeName"><a href="javascript:loadData(' + i
								+ ')">' + place.name + '</a></div> ('
								+ place.legacyId + ')');
			});
		}
	});
}

function updateCoord() { 
	if ( prevMarker ) { 
		var point = new google.maps.LatLng(parseFloat($("#lat").val()),
				parseFloat($("#lon").val()));
		prevMarker.setPosition(point);
		map.setCenter(point);
	}
}

function loadData(index) {
	var place = places.place[index];
	$("#infoKey").html(place.legacyId);

	$("#infoName").html(place.name);
	
	$("#goecodeLink").html("<a href=\"javascript:autoGeoCode("+index+")\">Auto Geocode</a>");
	
	$("#lat").val(place.latitude);
	$("#lon").val(place.longitude);
	
	$("#postButton").html("<input type=\"button\" value=\"Update Place Record\" onclick=\"postUpdate('"+place.id+"', "+index+")\"/>");
		
	var point = new google.maps.LatLng(parseFloat(place.latitude),
			parseFloat(place.longitude));
	if (prevMarker) {
		prevMarker.setMap(null);
	}

	prevMarker = new google.maps.Marker({
		position : point,
		map : map,
		title : "A place",
		draggable : true
	});

	google.maps.event.addListener(prevMarker, 'dragend', function() {
		$("#lat").val(this.getPosition().lat());
		$("#lon").val(this.getPosition().lng());
	});

	map.setZoom(default_zoom);
	map.setCenter(point);
	
	$("#occurrences").html("");
	var html = "";

	if ( $.isArray(place.occurrences) ) {
		$.each(place.occurrences, function(i, occurrence) {
			html += processOccurrence(occurrence);
		});		
	} else if ( place.occurrences ) { 
		html += processOccurrence(place.occurrences);
	}

	$("#occurrences").html(html);	
}

function processOccurrence(occurrence) { 
	var html = "<div class=\"occurrence\">";
	html += "<b>"+occurrence.actualText+"</b><br/>";
	html += "Appearing in volume: "+occurrence.sentence.volume+"<br/>";
	html += "In sentence number: "+occurrence.sentence.number+"<br>";
	html += occurrence.sentence.text;		
	
	if ( $.isArray(occurrence.alternatePlaces) ) {
		html += "<div class=\"alt\">Alternative Places:<br/>";
		$.each(occurrence.alternatePlaces, function(i, place) {
			html += processAltPlace(place);
		});	
		html += "</div>"
	} else if ( occurrence.alternatePlaces ) { 
		html += "<div class=\"alt\">Alternate Place: ";
		html += processAltPlace(occurrence.alternatePlaces);
		html += "</div>";
	}
	
	html += "</div>";		
	
	return html;
}

function processAltPlace(place) {
	
	var html = "<div><a href=\"javascript:displayPlace('";
	html += place;
	html += "')\">";
	html += place;
	html += "</a></div>";
	return html;
}

function displayPlace(place) {
	$.getJSON("../rest/author/dispatchXML/place/"+place+".json", function(data) {
		var html = "<div>Place name: ";
		html += data.name;
		html += "</div>";
		html += "<div style=\"margin-top: 15px;\">";
		if ( $.isArray(data.occurrences) ) {
			$.each(data.occurrences, function(i, occurrence) {
				html += processOccurrence(occurrence);
			});		
		} else if ( data.occurrences ) { 
			html += processOccurrence(data.occurrences);
		} else {
			html += "This place does not occurr in the text.";
		}
		html += "</div>";
		
		$("#stage").html("");
		$("#stage").html(html);
		$("#stage").dialog({title: "Alternate Place", resizable: false, modal: true, width: 480, height: 320});
	});
}


function autoGeoCode(index) {
	var place = places.place[index];
    geocoder.geocode( { 'address': place.name}, function(results, status) {
        if (status == google.maps.GeocoderStatus.OK) {
        	var loc = results[0].geometry.location;
        	prevMarker.setOptions({position: loc});
        	map.setCenter(loc);
        	map.setZoom(7);
    		$("#lat").val(loc.lat());
    		$("#lon").val(loc.lng());
        }
    });
}

function postUpdate(id, index) {
	
	var post_data = { 
			placeId: id,
			notes: $("#notes").val(), 
			latitude: parseFloat($("#lat").val()),
			longitude: parseFloat($("#lon").val()),
			action: $('input:radio[name=option]:checked').val()
		};
	
	if ( post_data.action == null ) { 
		alert('You must select an action for this update.');
	} else {
		$.post("../rest/author/dispatchXML/update", post_data, function(data) { location.reload(true); });
	}
}
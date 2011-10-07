/* Created on Jan 30, 2011 */
package edu.unl.cig.aurora;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.mongodb.DBObject;

import edu.unl.cig.aurora.model.PlaceReference;

/**
 * @author Ian Cottingham
 * 
 */
public class MongoPlaceFactory {

	private static final DateTimeFormatter FORMAT = DateTimeFormat
			.forPattern("yyyy-MM-dd");


	public static PlaceReference placeReferenceFromMongoRow(DBObject row) {
		DBObject coords = (DBObject)row.get("loc");
		
		double lat = ( coords.get("lat") instanceof Double ) ? (Double)coords.get("lat") : (Integer)coords.get("lat");
		double lon = ( coords.get("lon") instanceof Double ) ? (Double)coords.get("lon") : (Integer)coords.get("lon");
		
		DateTime d = FORMAT.parseDateTime((String)row.get("date"));
		
		return new PlaceReference((String) row
				.get("place"), (String) row.get("text"), (Integer) row
				.get("refCount"),
				d, (String) row
						.get("placeKey"), lat, lon);
	}
}

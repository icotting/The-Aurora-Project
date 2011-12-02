/* Created on Oct 11, 2010 */
package edu.unl.cig.aurora.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormatter;

/**
 * @author Ian Cottingham
 *
 */
@XmlRootElement(name="placeReferences")
public class PlaceCollection {

	private List<String> dates;
	private List<PlaceReference> references;
	
	public List<String> getDates() {
		return dates;
	}
	
	public void setDates(List<String> dates) {
		this.dates = dates;
	}
	
	public void setDates(List<DateTime> dates, DateTimeFormatter format) { 
		this.dates = new ArrayList<String>();
		for ( DateTime d : dates ) { 
			this.dates.add(format.print(d));
		}
	}
	
	public List<PlaceReference> getReferences() {
		return references;
	}
	
	public void setReferences(List<PlaceReference> references) {
		this.references = references;
	}
	
	public String toGeoJsonString() { 
		
		/* sort by reference size so that when displayed on a map the 
		 * small circles will appear inside of the larger ones */
		Collections.sort(this.references, new Comparator<PlaceReference>() {
			@Override
			public int compare(PlaceReference arg0,
					PlaceReference arg1) {
				if ( arg0.getReferenceCount() > arg1.getReferenceCount() ) {
					return -1;
				} else if ( arg0.getReferenceCount() < arg1.getReferenceCount() ) {
					return 1;
				} else { 
					return 0;
				}
			}
			
		});
		
		StringBuffer json = new StringBuffer("{ \"type\": \"FeatureCollection\", \"features\": [");
		
		int len = this.references.size();
		for (int i=0; i<len; i++) {
			if ( i > 0 ) {
				json.append(",");
			}
			json.append(this.references.get(i).toGeoJsonString());
		}
		
		json.append("]}");
		return json.toString();
	}
}

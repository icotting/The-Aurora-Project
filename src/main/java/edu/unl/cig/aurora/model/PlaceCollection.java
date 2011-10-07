/* Created on Oct 11, 2010 */
package edu.unl.cig.aurora.model;

import java.util.ArrayList;
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
}

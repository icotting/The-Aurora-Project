package edu.unl.cig.aurora.model;

import java.util.Set;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import org.joda.time.DateTime;

import com.mongodb.DBObject;

//TODO: make this immutable 
@XmlRootElement(name = "placeReference")
public class PlaceReference implements Comparable<PlaceReference> {

	private String placeName;
	private Integer referenceCount;
	private DateTime referenceDate;
	private String actualText;
	private String placeKey;
	private int[] referenceCounts; // this field holds counts for each date in a
									// PlaceCollection
	private double latitude;
	private double longitude;
	
	public PlaceReference() { }
	
	public PlaceReference(String placeName, String actualText,
			Integer referenceCount, DateTime referenceDate, String placeKey,
			double latitude, double longitude) {
		this.placeName = placeName;
		this.referenceCount = referenceCount;
		this.referenceDate = referenceDate;
		this.actualText = actualText;
		this.placeKey = placeKey;
		this.latitude = latitude;
		this.longitude = longitude;
	}
	
	public String getPlaceName() {
		return placeName;
	}

	public void setPlaceName(String placeName) {
		this.placeName = placeName;
	}

	public Integer getReferenceCount() {
		return referenceCount;
	}

	public void setReferenceCount(Integer referenceCount) {
		this.referenceCount = referenceCount;
	}

	@XmlTransient
	public DateTime getReferenceDate() {
		return referenceDate;
	}

	public void setReferenceDate(DateTime referenceDate) {
		this.referenceDate = referenceDate;
	}

	public String getActualText() {
		return actualText;
	}

	public void setActualText(String actualText) {
		this.actualText = actualText;
	}

	public String getPlaceKey() {
		return placeKey;
	}

	public void setPlaceKey(String placeKey) {
		this.placeKey = placeKey;
	}

	public void incrementCount(int count) {
		this.referenceCount += count;
	}

	public int[] getReferenceCounts() {
		return referenceCounts;
	}

	public void setReferenceCounts(int[] referenceCounts) {
		this.referenceCounts = referenceCounts;
	}
	
	public double getLatitude() {
		return latitude;
	}

	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}

	public double getLongitude() {
		return longitude;
	}

	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}

	@Override
	public int compareTo(PlaceReference o) {

		if (this.placeKey == null || this.referenceDate == null) {
			return -1;
		} else if (o.getPlaceKey() == null || o.getReferenceDate() == null) {
			return 1;
		}

		if (this.placeKey.equals(o.getPlaceKey())
				&& this.referenceDate.equals(o.getReferenceDate())) {
			return 0;
		} else if (this.referenceDate.equals(o.getReferenceDate())) {
			return this.placeKey.compareTo(o.getPlaceKey());
		} else {
			return this.referenceDate.compareTo(o.getReferenceDate());
		}

	}

	// TODO: this should be reconsidered for something faster
	public PlaceReference extractFromSet(Set<PlaceReference> refs) {
		if (!refs.contains(this)) {
			throw new RuntimeException(
					"The element was not contained in the set");
		}

		for (PlaceReference ref : refs) {
			if (ref.equals(this)) {
				return ref;
			}
		}

		throw new RuntimeException("The element was not contained in the set");
	}

	protected PlaceReference copyWithNewDate(DateTime newDate) {
		PlaceReference ref;
		try {
			ref = (PlaceReference) this.clone();
		} catch (Exception e) {
			// this won't happen
			throw new RuntimeException(e.getMessage());
		}

		ref.setReferenceDate(newDate);
		return ref;
	}

	@Override
	public int hashCode() {
		return (this.referenceDate.hashCode() + ":" + this.placeKey.hashCode())
				.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof PlaceReference)) {
			return false;
		}

		PlaceReference test_obj = (PlaceReference) obj;

		return this.referenceDate.equals(test_obj.getReferenceDate())
				&& this.placeKey.equals(test_obj.getPlaceKey());
	}

	@Override
	protected Object clone() throws CloneNotSupportedException {
		PlaceReference ref = new PlaceReference(this.placeName,
				this.actualText, this.referenceCount, this.referenceDate,
				this.placeKey, this.latitude, this.longitude);
		ref.setReferenceCounts(this.referenceCounts);

		return ref;
	}
}

/* Created on Feb 7, 2011 */
package edu.unl.cig.aurora.model.dispatch;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ian Cottingham
 * 
 */
@Entity
@XmlRootElement
public class Place {
	
	private long id;
	private String legacyId;
	private String name;
	private List<PlaceOccurrence> occurrences;
	private float latitude;
	private float longitude;
	private boolean reviewed;
	private boolean deleted;
	private ReviewNote note;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	@XmlID
	public String getLegacyId() {
		return legacyId;
	}
	
	public void setLegacyId(String legacyId) {
		this.legacyId = legacyId;
	}
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public float getLatitude() {
		return latitude;
	}

	public void setLatitude(float latitude) {
		this.latitude = latitude;
	}

	public float getLongitude() {
		return longitude;
	}

	public void setLongitude(float longitude) {
		this.longitude = longitude;
	}

	@OneToMany(mappedBy="place", cascade={CascadeType.ALL})
	public List<PlaceOccurrence> getOccurrences() {
		return occurrences;
	}

	public void setOccurrences(List<PlaceOccurrence> occurrences) {
		this.occurrences = occurrences;
	}

	@OneToOne(mappedBy="place")
	public ReviewNote getNote() {
		return note;
	}

	public void setNote(ReviewNote note) {
		this.note = note;
	}

	public boolean isReviewed() {
		return reviewed;
	}

	public void setReviewed(boolean reviewed) {
		this.reviewed = reviewed;
	}

	public boolean isDeleted() {
		return deleted;
	}

	public void setDeleted(boolean deleted) {
		this.deleted = deleted;
	}
}

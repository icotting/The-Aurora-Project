/* Created on Feb 14, 2011 */
package edu.unl.cig.aurora.model.dispatch;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Ian Cottingham 
 *
 */
@Entity
public class ReviewNote {

	private long id;
	private String comment;
	private String action;
	private Date reviewedOn;
	
	private Place place;
	private PlaceOccurrence occurrence;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getComment() {
		return comment;
	}
	
	public void setComment(String comment) {
		this.comment = comment;
	}
	
	public String getAction() {
		return action;
	}
	
	public void setAction(String action) {
		this.action = action;
	}
	
	@Temporal(TemporalType.TIMESTAMP)
	public Date getReviewedOn() {
		return reviewedOn;
	}
	
	public void setReviewedOn(Date reviewedOn) {
		this.reviewedOn = reviewedOn;
	}

	@OneToOne
	@XmlTransient
	public Place getPlace() {
		return place;
	}

	public void setPlace(Place place) {
		this.place = place;
	}

	@OneToOne
	@XmlTransient
	public PlaceOccurrence getOccurrence() {
		return occurrence;
	}

	public void setOccurrence(PlaceOccurrence occurrence) {
		this.occurrence = occurrence;
	}
}

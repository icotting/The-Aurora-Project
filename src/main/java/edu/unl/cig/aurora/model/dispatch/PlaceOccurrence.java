/* Created on Feb 7, 2011 */
package edu.unl.cig.aurora.model.dispatch;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinTable;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Ian Cottingham
 * 
 */
@Entity
@XmlRootElement
public class PlaceOccurrence {

	private long id;
	private Place place;
	private String actualText;
	private Sentence sentence;
	private List<Place> alternatePlaces;
	private boolean reviewed;
	private ReviewNote note;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	@XmlTransient
	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
	public Place getPlace() {
		return place;
	}
	
	public void setPlace(Place place) {
		this.place = place;
	}

	public String getActualText() {
		return actualText;
	}
	
	public void setActualText(String actualText) {
		this.actualText = actualText;
	}

	@ManyToOne(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
	public Sentence getSentence() {
		return sentence;
	}

	public void setSentence(Sentence sentence) {
		this.sentence = sentence;
	}

	@ManyToMany(cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
	@JoinTable(name="ALT_PLACES")
	@XmlIDREF
	public List<Place> getAlternatePlaces() {
		return alternatePlaces;
	}

	public void setAlternatePlaces(List<Place> alternatePlaces) {
		this.alternatePlaces = alternatePlaces;
	}

	@OneToOne(mappedBy="occurrence")
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
	
}

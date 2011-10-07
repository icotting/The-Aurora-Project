/* Created on Feb 7, 2011 */
package edu.unl.cig.aurora.model.dispatch;

import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlIDREF;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Ian Cottingham
 * 
 */
@Entity
@XmlRootElement
public class Sentence {

	private long id;
	private int number;
	private String text;
	private List<PlaceOccurrence> places;
	private DispatchVolume volume;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public int getNumber() {
		return number;
	}
	
	public void setNumber(int number) {
		this.number = number;
	}
	
	@ManyToOne
	@XmlIDREF
	public DispatchVolume getVolume() {
		return volume;
	}

	public void setVolume(DispatchVolume volume) {
		this.volume = volume;
	}

	@Column(columnDefinition="longtext")
	public String getText() {
		return text;
	}
	
	public void setText(String text) {
		this.text = text;
	}

	@OneToMany(mappedBy="sentence", cascade={CascadeType.PERSIST, CascadeType.MERGE, CascadeType.REFRESH, CascadeType.DETACH})
	@XmlTransient
	public List<PlaceOccurrence> getPlaces() {
		return places;
	}

	public void setPlaces(List<PlaceOccurrence> places) {
		this.places = places;
	}
	
	public void addText(String text) { 
		if ( text == null ) { 
			return;
		} else if ( this.text == null ) { 
			this.text = text;
		} else {
			this.text += text;
		}
	}
}

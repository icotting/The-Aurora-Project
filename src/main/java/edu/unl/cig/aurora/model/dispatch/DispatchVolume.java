/* Created on Feb 16, 2011 */
package edu.unl.cig.aurora.model.dispatch;

import java.util.Date;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.xml.bind.annotation.XmlID;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ian Cottingham 
 *
 */
@Entity
@XmlRootElement
public class DispatchVolume {

	private long id;
	private Date volumeDate;
	private String fileName;
	private List<Sentence> sentences;
	
	@Id
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	@Temporal(TemporalType.DATE)
	public Date getVolumeDate() {
		return volumeDate;
	}
	
	public void setVolumeDate(Date volumeDate) {
		this.volumeDate = volumeDate;
	}
	
	@XmlID
	public String getFileName() {
		return fileName;
	}
	
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	
	@OneToMany(mappedBy="volume", cascade=CascadeType.ALL)
	public List<Sentence> getSentences() {
		return sentences;
	}
	
	public void setSentences(List<Sentence> sentences) {
		this.sentences = sentences;
	}
}

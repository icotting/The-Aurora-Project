/* Created on May 29, 2011 */
package edu.unl.cig.aurora.model.freedmen;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Ian Cottingham 
 *
 */
@Entity
@XmlRootElement
public class ContractDestination {

	private long id;
	private String township;
	private String county;
	private String state;
	private float latitude; 
	private float longitude;
	private List<Contract> contracts;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getTownship() {
		return township;
	}
	
	public void setTownship(String township) {
		this.township = township;
	}
	
	public String getCounty() {
		return county;
	}
	
	public void setCounty(String county) {
		this.county = county;
	}
	
	public String getState() {
		return state;
	}
	
	public void setState(String state) {
		this.state = state;
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
	
	@XmlElement(name="fullPlaceName")
	public String getFullPlaceName() { 
		
		StringBuffer sb = new StringBuffer();
		if ( this.township != null && !this.township.trim().equals("") ) { 
			sb.append(this.township+", ");
		}
		if ( this.county != null && !this.county.trim().equals("") ) { 
			sb.append(this.county+", ");
		}
		sb.append(this.state);
		
		return sb.toString();
		
	}
	
	@OneToMany(mappedBy="destination", fetch=FetchType.LAZY)
	@XmlTransient //TODO: this should be done by a ref for more flexibility
	public List<Contract> getContracts() {
		return contracts;
	}
	
	public void setContracts(List<Contract> contracts) {
		this.contracts = contracts;
	}
	
	@Override
	public boolean equals(Object obj) { 
		if ( !(obj instanceof ContractDestination) ) { 
			return false;
		} else {
			ContractDestination comp = (ContractDestination)obj;
			
			return (comp.getLatitude() == this.latitude) && (comp.getLongitude() == this.longitude) && (comp.getState().equals(this.state)) && 
					(comp.getCounty().equals(this.county)) && (comp.getTownship().equals(this.township));
		}
	}
}

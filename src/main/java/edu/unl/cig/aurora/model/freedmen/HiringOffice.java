/* Created on May 29, 2011 */
package edu.unl.cig.aurora.model.freedmen;

import java.util.List;

import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Ian Cottingham 
 *
 */
@Entity
@XmlRootElement
public class HiringOffice {

	private long id;
	private String name;
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

	@OneToMany(mappedBy="office", fetch=FetchType.LAZY)
	@XmlTransient //TODO: this should be done by a ref for more flexibility
	public List<Contract> getContracts() {
		return contracts;
	}

	public void setContracts(List<Contract> contracts) {
		this.contracts = contracts;
	}
	
	@Override 
	public boolean equals(Object obj) { 
		if ( !(obj instanceof HiringOffice) ) { 
			return false;
		} else { 
			HiringOffice comp = (HiringOffice)obj;
			
			float lat_diff = Math.abs(this.latitude - comp.getLatitude());
			float lon_diff = Math.abs(this.longitude - comp.getLongitude());
			
			return (lat_diff <= 0.001) && (lon_diff <= 0.001) && comp.getName().trim().equals(this.getName().trim());
		}
	}
}

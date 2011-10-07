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
public class Worker {

	private long id;
	private String firstName;
	private String lastName;
	private String label;
	private String gender;
	private List<Contract> contracts;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public String getFirstName() {
		return firstName;
	}
	
	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}
	
	public String getLastName() {
		return lastName;
	}
	
	public void setLastName(String lastName) {
		this.lastName = lastName;
	}
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}
	
	public String getGender() {
		return gender;
	}
	
	public void setGender(String gender) {
		this.gender = gender;
	}
	
	@OneToMany(mappedBy="worker", fetch=FetchType.LAZY)
	@XmlTransient
	public List<Contract> getContracts() {
		return contracts;
	}
	
	public void setContracts(List<Contract> contracts) {
		this.contracts = contracts;
	}
	
	@Override
	public boolean equals(Object obj) { 
		if ( !(obj instanceof Worker) ) { 
			return false;
		} else { 
			return this.id == ((Worker)obj).getId();
		}
	}
}

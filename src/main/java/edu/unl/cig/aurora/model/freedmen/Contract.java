/* Created on May 29, 2011 */
package edu.unl.cig.aurora.model.freedmen;

import java.util.Date;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ian Cottingham 
 *
 */
@Entity
@XmlRootElement
public class Contract {

	private long id;
	private Worker worker;
	private int ageAtContractOutset;
	private HiringOffice office;
	private ContractDestination destination;
	private String position;
	private String workClass;
	private String lengthOfServiceMonths;
	private String rateOfPay;
	private String renumeration;
	private String comments;
	private String employerAgent;
	private Date contractDate;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	@ManyToOne
	public Worker getWorker() {
		return worker;
	}
	
	public void setWorker(Worker worker) {
		this.worker = worker;
	}
	
	public int getAgeAtContractOutset() {
		return ageAtContractOutset;
	}
	
	public void setAgeAtContractOutset(int ageAtContractOutset) {
		this.ageAtContractOutset = ageAtContractOutset;
	}
	
	@ManyToOne
	public HiringOffice getOffice() {
		return office;
	}
	
	public void setOffice(HiringOffice office) {
		this.office = office;
	}
	
	@ManyToOne
	public ContractDestination getDestination() {
		return destination;
	}
	
	public void setDestination(ContractDestination destination) {
		this.destination = destination;
	}
	
	public String getPosition() {
		return position;
	}
	
	public void setPosition(String position) {
		this.position = position;
	}
	
	public String getWorkClass() {
		return workClass;
	}
	
	public void setWorkClass(String workClass) {
		this.workClass = workClass;
	}
	
	public String getLengthOfServiceMonths() {
		return lengthOfServiceMonths;
	}
	
	public void setLengthOfServiceMonths(String lengthOfServiceMonths) {
		this.lengthOfServiceMonths = lengthOfServiceMonths;
	}
	
	public String getRateOfPay() {
		return rateOfPay;
	}
	
	public void setRateOfPay(String rateOfPay) {
		this.rateOfPay = rateOfPay;
	}
	
	public String getRenumeration() {
		return renumeration;
	}
	
	public void setRenumeration(String renumeration) {
		this.renumeration = renumeration;
	}
	
	public String getComments() {
		return comments;
	}
	
	public void setComments(String comments) {
		this.comments = comments;
	}
	
	@Transient
	public long getContractTime() { 
		return (this.contractDate == null) ? -1l : this.contractDate.getTime();
	}
	
	@Temporal(TemporalType.DATE)
	public Date getContractDate() {
		return contractDate;
	}
	
	public void setContractDate(Date contractDate) {
		this.contractDate = contractDate;
	}

	public String getEmployerAgent() {
		return employerAgent;
	}

	public void setEmployerAgent(String employerAgent) {
		this.employerAgent = employerAgent;
	}	

	@Override
	public boolean equals(Object obj) { 
		if ( !(obj instanceof Contract) ) { 
			return false;
		} else { 
			return this.id == ((Contract)obj).getId();
		}
	}
}

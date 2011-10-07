/* Created on Jun 1, 2011 */
package edu.unl.cig.aurora.model.railroad;

import java.util.List;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

/**
 * @author Ian Cottingham 
 *
 */
@Entity
public class Depot {

	private long id;
	private long legacyId;
	private String nearbyTown;
	private String county;
	private String state;
	private float elevation;
	private float mileMarker;
	private boolean mapEvidence;
	private String additionalInfo;
	private String comments;
	private List<DepotMetaDatum> metaData;
	
	@Id
	@GeneratedValue(strategy=GenerationType.SEQUENCE)
	public long getId() {
		return id;
	}
	
	public void setId(long id) {
		this.id = id;
	}
	
	public long getLegacyId() {
		return legacyId;
	}

	public void setLegacyId(long legacyId) {
		this.legacyId = legacyId;
	}

	public String getNearbyTown() {
		return nearbyTown;
	}
	
	public void setNearbyTown(String nearbyTown) {
		this.nearbyTown = nearbyTown;
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
	
	public float getElevation() {
		return elevation;
	}
	
	public void setElevation(float elevation) {
		this.elevation = elevation;
	}
	
	public float getMileMarker() {
		return mileMarker;
	}
	
	public void setMileMarker(float mileMarker) {
		this.mileMarker = mileMarker;
	}
	
	public boolean isMapEvidence() {
		return mapEvidence;
	}
	
	public void setMapEvidence(boolean mapEvidence) {
		this.mapEvidence = mapEvidence;
	}
	
	@Column(columnDefinition="mediumtext")
	public String getAdditionalInfo() {
		return additionalInfo;
	}
	
	public void setAdditionalInfo(String additionalInfo) {
		this.additionalInfo = additionalInfo;
	}
	
	@Column(columnDefinition="mediumtext")
	public String getComments() {
		return comments;
	}
	
	public void setComments(String comments) {
		this.comments = comments;
	}

	@OneToMany(mappedBy="depot")
	public List<DepotMetaDatum> getMetaData() {
		return metaData;
	}

	public void setMetaData(List<DepotMetaDatum> metaData) {
		this.metaData = metaData;
	}
}

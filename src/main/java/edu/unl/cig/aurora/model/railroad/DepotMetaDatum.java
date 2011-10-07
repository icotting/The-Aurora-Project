/* Created on Jun 1, 2011 */
package edu.unl.cig.aurora.model.railroad;

import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.ManyToOne;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.swing.text.html.FormSubmitEvent.MethodType;

/**
 * @author Ian Cottingham 
 *
 */
@Entity
public class DepotMetaDatum {

	/* 	* This class models that from the UK depot dataset.  While suitable for 
	 	* holding the data, it is not the most normalized data structure. Several
	 	* of the data fields should be considered for being broken into unique 
	 	* objects to create a more normalized data structure. 
	 	* 
	 	* IC - 2011.06.01
	 */
	
	private long id;
	private long legacyId;
	private String name;
	private String trafficType;
	private String stationType;
	private DepotMetaType type;
	
	private String buildType;
	private String architect;
	private String builder;
	private String builderTown;
	private String builderState;
	
	private String damageDescription; 
	
	private String startMonth;
	private int startYear;
	private String startYearAuthority;
	private Date startDate;
	
	private String endMonth;
	private int endYear;
	private String endYearAuthority;
	private Date endDate;
	
	private float constructionCost;
	private float rebuildCost;
	private float damageCost;
	private String additionalInfo;
	private String comments;
	
	private Depot depot;
	private boolean type_set;
	
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

	public String getTrafficType() {
		return trafficType;
	}

	public void setTrafficType(String trafficType) {
		this.trafficType = trafficType;
	}

	public String getStationType() {
		return stationType;
	}

	public void setStationType(String stationType) {
		this.stationType = stationType;
	}

	public DepotMetaType getType() {
		return type;
	}

	public void setType(DepotMetaType type) {
		if ( type_set ) { 
			if ( this.type == DepotMetaType.RENAMED && type == DepotMetaType.REOPENING ) {
				this.type = DepotMetaType.REOPENED_WITH_NEW_NAME;
			} else if ( this.type == DepotMetaType.TEMPORARILY_CLOSING && type == DepotMetaType.DAMAGED ) {
				this.type = DepotMetaType.TEMPORARILY_CLOSING_DUE_TO_DAMAGE;
			} else {
				System.out.println("A type has already been set for this meta record: "+this.type+" trying to set "+type);
			}
		} else { 
			this.type = type;
		}
		type_set = true;
	}

	public String getBuildType() {
		return buildType;
	}

	public void setBuildType(String buildType) {
		this.buildType = buildType;
	}

	public String getArchitect() {
		return architect;
	}

	public void setArchitect(String architect) {
		this.architect = architect;
	}

	public String getBuilder() {
		return builder;
	}

	public void setBuilder(String builder) {
		this.builder = builder;
	}

	public String getBuilderTown() {
		return builderTown;
	}

	public void setBuilderTown(String builderTown) {
		this.builderTown = builderTown;
	}

	public String getBuilderState() {
		return builderState;
	}

	public void setBuilderState(String builderState) {
		this.builderState = builderState;
	}

	public String getDamageDescription() {
		return damageDescription;
	}

	public void setDamageDescription(String damageDescription) {
		this.damageDescription = damageDescription;
	}

	public String getStartMonth() {
		return startMonth;
	}

	public void setStartMonth(String startMonth) {
		this.startMonth = startMonth;
	}

	public int getStartYear() {
		return startYear;
	}

	public void setStartYear(int startYear) {
		this.startYear = startYear;
	}

	public String getStartYearAuthority() {
		return startYearAuthority;
	}

	public void setStartYearAuthority(String startYearAuthority) {
		this.startYearAuthority = startYearAuthority;
	}

	@Temporal(TemporalType.DATE)
	public Date getStartDate() {
		return startDate;
	}

	public void setStartDate(Date startDate) {
		this.startDate = startDate;
	}

	public String getEndMonth() {
		return endMonth;
	}

	public void setEndMonth(String endMonth) {
		this.endMonth = endMonth;
	}

	public int getEndYear() {
		return endYear;
	}

	public void setEndYear(int endYear) {
		this.endYear = endYear;
	}

	public String getEndYearAuthority() {
		return endYearAuthority;
	}

	public void setEndYearAuthority(String endYearAuthority) {
		this.endYearAuthority = endYearAuthority;
	}

	@Temporal(TemporalType.DATE)
	public Date getEndDate() {
		return endDate;
	}

	public void setEndDate(Date endDate) {
		this.endDate = endDate;
	}

	public float getConstructionCost() {
		return constructionCost;
	}

	public void setConstructionCost(float constructionCost) {
		this.constructionCost = constructionCost;
	}

	public float getRebuildCost() {
		return rebuildCost;
	}

	public void setRebuildCost(float rebuildCost) {
		this.rebuildCost = rebuildCost;
	}

	public float getDamageCost() {
		return damageCost;
	}

	public void setDamageCost(float damageCost) {
		this.damageCost = damageCost;
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

	@ManyToOne
	public Depot getDepot() {
		return depot;
	}

	public void setDepot(Depot depot) {
		this.depot = depot;
	}

	public long getLegacyId() {
		return legacyId;
	}

	public void setLegacyId(long legacyId) {
		this.legacyId = legacyId;
	}
}

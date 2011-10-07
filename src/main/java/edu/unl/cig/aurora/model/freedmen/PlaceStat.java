/* Created on Jun 9, 2011 */
package edu.unl.cig.aurora.model.freedmen;

import java.util.List;

import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ian Cottingham 
 *
 */
@XmlRootElement
public class PlaceStat {

	private float latitude;
	private float longitude;
	private String name;
	private long refId;
	private int type; // 0 office 1 destination for now - this should be fixed later
	
	private int contractCount; 
	
	private List<NVPair<Integer>> positionCounts;
	private List<NVPair<Integer>> genderCounts;
	private List<NVPair<Integer>> workerClassCounts;

	private List<NVPair<Float>> positionAveragePay;
	private List<NVPair<Float>> genderAveragePay;
	private List<NVPair<Float>> workerClassAveragePay;
		
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
	
	public int getContractCount() {
		return contractCount;
	}
	
	public void setContractCount(int contractCount) {
		this.contractCount = contractCount;
	}
	
	public List<NVPair<Integer>> getPositionCounts() {
		return positionCounts;
	}
	
	public void setPositionCounts(List<NVPair<Integer>> positionCounts) {
		this.positionCounts = positionCounts;
	}
	
	public List<NVPair<Integer>> getGenderCounts() {
		return genderCounts;
	}
	
	public void setGenderCounts(List<NVPair<Integer>> genderCounts) {
		this.genderCounts = genderCounts;
	}
	
	public List<NVPair<Integer>> getWorkerClassCounts() {
		return workerClassCounts;
	}
	
	public void setWorkerClassCounts(List<NVPair<Integer>> workerClassCounts) {
		this.workerClassCounts = workerClassCounts;
	}
	
	public List<NVPair<Float>> getPositionAveragePay() {
		return positionAveragePay;
	}
	
	public void setPositionAveragePay(List<NVPair<Float>> positionAveragePay) {
		this.positionAveragePay = positionAveragePay;
	}
	
	public List<NVPair<Float>> getGenderAveragePay() {
		return genderAveragePay;
	}
	
	public void setGenderAveragePay(List<NVPair<Float>> genderAveragePay) {
		this.genderAveragePay = genderAveragePay;
	}
	
	public List<NVPair<Float>> getWorkerClassAveragePay() {
		return workerClassAveragePay;
	}
	
	public void setWorkerClassAveragePay(List<NVPair<Float>> workerClassAveragePay) {
		this.workerClassAveragePay = workerClassAveragePay;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public long getRefId() {
		return refId;
	}

	public void setRefId(long refId) {
		this.refId = refId;
	}

	public int getType() {
		return type;
	}

	public void setType(int type) {
		this.type = type;
	}
}

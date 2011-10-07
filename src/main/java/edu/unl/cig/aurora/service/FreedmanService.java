/* Created on Jun 9, 2011 */
package edu.unl.cig.aurora.service;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import edu.unl.cig.aurora.model.freedmen.Contract;
import edu.unl.cig.aurora.model.freedmen.ContractDestination;
import edu.unl.cig.aurora.model.freedmen.HiringOffice;
import edu.unl.cig.aurora.model.freedmen.NVPair;
import edu.unl.cig.aurora.model.freedmen.PlaceStat;

/**
 * @author Ian Cottingham
 * 
 */
@Path("/freedman")
@ManagedBean
public class FreedmanService {

	private static final Logger LOG = Logger.getLogger(DispatchService.class
			.getName());

	private static final DateFormat FORMAT = new SimpleDateFormat("yyyy-MM-dd");

	@PersistenceContext(unitName = "AuroraUnit", type = PersistenceContextType.TRANSACTION)
	private EntityManager manager;

	@GET
	@Path("/hiringOffices.json")
	@Produces(MediaType.APPLICATION_JSON)
	public String getHiringOffices() {
		
		List<HiringOffice> offices = manager.createQuery("select h from HiringOffice as h").getResultList();

		StringBuffer geo_json = new StringBuffer("{ \"type\": \"FeatureCollection\", \"features\": [");
		
		boolean first = true;
		for ( HiringOffice office : offices ) { 
			
			if ( !first ) { 
				geo_json.append(",");
			} else {
				first = false;
			}
			
			geo_json.append("{\"type\" : \"Feature\",");
			geo_json.append("\"geometry\": {\"type\": \"Point\", \"coordinates\": [");
			geo_json.append(String.format("%f, %f", office.getLongitude(), office.getLatitude()));
			geo_json.append("]}, \"properties\": {");
			geo_json.append(String.format("\"placeName\":\"%s\"", office.getName()));
			geo_json.append("}");
			geo_json.append("}");
		}
		geo_json.append("]}");
		
		return geo_json.toString();
	}
	
	@GET
	@Path("/contracts/{date}.json")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Contract> getContractsForDate(@PathParam("date") String date,
			@QueryParam("to") String toDate) throws ParseException {

		Date from_date = FORMAT.parse(date);
		Date to_date = (toDate == null) ? from_date : FORMAT.parse(
				toDate);

		List<Contract> contracts = manager
				.createQuery(
						"select c from Contract as c where c.contractDate between ?1 and ?2")
				.setParameter(1, from_date)
				.setParameter(2, to_date).getResultList();

		return contracts;
	}

	@GET
	@Path("/contracts/from/{officeId}/{date}.json")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Contract> getContractsFromHiringOfficeForDate(
			@PathParam("officeId") long officeId,
			@PathParam("date") String date, @QueryParam("to") String toDate) throws ParseException {

		HiringOffice office = manager.find(HiringOffice.class, officeId);

		if (office == null) {
			throw new RuntimeException("No hiring office found for id "
					+ officeId);
		}

		Date from_date = FORMAT.parse(date);
		Date to_date = (toDate == null) ? from_date : FORMAT.parse(
				toDate);
		
		List<Contract> contracts = manager
				.createQuery(
						"select c from Contract as c where c.office = ?1 and c.contractDate between ?2 and ?3")
				.setParameter(1, office)
				.setParameter(2, from_date)
				.setParameter(3, to_date).getResultList();

		return contracts;
	}

	@GET
	@Path("/contracts/to/{destinationId}/{date}.json")
	@Produces(MediaType.APPLICATION_JSON)
	public List<Contract> getContractsToDestinationForDate(
			@PathParam("destinationId") long destId,
			@PathParam("date") String date, @QueryParam("to") String toDate) throws ParseException {

		ContractDestination destination = manager.find(
				ContractDestination.class, destId);

		if (destination == null) {
			throw new RuntimeException("No destination found for id " + destId);
		}

		Date from_date = FORMAT.parse(date);
		Date to_date = (toDate == null) ? from_date : FORMAT.parse(
				toDate);

		List<Contract> contracts = manager
				.createQuery(
						"select c from Contract as c where c.destination = ?1 and c.contractDate between ?2 and ?3")
				.setParameter(1, destination)
				.setParameter(2, from_date)
				.setParameter(3, to_date).getResultList();

		return contracts;
	}

	@GET
	@Path("/stats/destination/{destinationId}/{date}.json")
	@Produces(MediaType.APPLICATION_JSON)
	public PlaceStat getDestinationStatsForDate(
			@PathParam("destinationId") long destId,
			@PathParam("date") String date, @QueryParam("to") String toDate) throws ParseException {

		ContractDestination destination = manager.find(
				ContractDestination.class, destId);

		if (destination == null) {
			throw new RuntimeException("No destination found for id " + destId);
		}

		PlaceStat stat = new PlaceStat();
		String name = destination.getState();
		if (destination.getCounty() != null
				&& !destination.getCounty().trim().equals("")) {
			name += " / " + destination.getCounty();
		}

		if (destination.getTownship() != null
				&& !destination.getTownship().trim().equals("")) {
			name += " / " + destination.getTownship();
		}
		stat.setName(name);
		stat.setType(1);
		stat.setRefId(destination.getId());

		stat.setLatitude(destination.getLatitude());
		stat.setLongitude(destination.getLongitude());

		List<Contract> contracts = getContractsToDestinationForDate(destId,
				date, toDate);
		stat.setContractCount(contracts.size());

		for (Contract c : contracts) {
			updateStatCounts(stat, c);
		}

		return stat;
	}

	@GET
	@Path("/stats/office/{officeId}/{date}.json")
	@Produces(MediaType.APPLICATION_JSON)
	public PlaceStat getHiringOfficeStatsForDate(
			@PathParam("officeId") long officeId,
			@PathParam("date") String date, @QueryParam("to") String toDate) throws ParseException {

		HiringOffice office = manager.find(HiringOffice.class, officeId);

		if (office == null) {
			throw new RuntimeException("No hiring office found for id "
					+ officeId);
		}
		PlaceStat stat = new PlaceStat();
		stat.setName(office.getName());
		stat.setType(0);
		stat.setRefId(office.getId());

		List<Contract> contracts = getContractsFromHiringOfficeForDate(
				officeId, date, toDate);
		stat.setContractCount(contracts.size());
		stat.setLatitude(office.getLatitude());
		stat.setLongitude(office.getLongitude());

		for (Contract c : contracts) {
			updateStatCounts(stat, c);
		}

		return stat;
	}

	// TODO: this needs to be generalized, badly!
	// seriously, this method is a complete mess
	private void updateStatCounts(PlaceStat stat, Contract contract) {
		if (stat.getPositionCounts() == null) {
			stat.setPositionCounts(new ArrayList<NVPair<Integer>>());
		}

		if (stat.getGenderCounts() == null) {
			stat.setGenderCounts(new ArrayList<NVPair<Integer>>());
		}

		if (stat.getWorkerClassCounts() == null) {
			stat.setWorkerClassCounts(new ArrayList<NVPair<Integer>>());
		}

		if (stat.getPositionAveragePay() == null) {
			stat.setPositionAveragePay(new ArrayList<NVPair<Float>>());
		}

		if (stat.getGenderAveragePay() == null) {
			stat.setGenderAveragePay(new ArrayList<NVPair<Float>>());
		}

		if (stat.getWorkerClassAveragePay() == null) {
			stat.setWorkerClassAveragePay(new ArrayList<NVPair<Float>>());
		}

		NVPair<Integer> position = new NVPair<Integer>();
		position.setName(contract.getPosition());
		int pos = -1;
		if ((pos = stat.getPositionCounts().indexOf(position)) > -1) {
			stat.getPositionCounts().get(pos)
					.setValue(stat.getPositionCounts().get(pos).getValue() + 1);
		} else {
			position.setValue(1);
			stat.getPositionCounts().add(position);
		}

		NVPair<Integer> gender = new NVPair<Integer>();
		gender.setName(contract.getWorker().getGender());
		pos = -1;
		if ((pos = stat.getGenderCounts().indexOf(gender)) > -1) {
			stat.getGenderCounts().get(pos)
					.setValue(stat.getGenderCounts().get(pos).getValue() + 1);
		} else {
			gender.setValue(1);
			stat.getGenderCounts().add(gender);
		}

		NVPair<Integer> worker_class = new NVPair<Integer>();
		worker_class.setName(contract.getWorkClass());
		pos = -1;
		if ((pos = stat.getWorkerClassCounts().indexOf(worker_class)) > -1) {
			stat.getWorkerClassCounts()
					.get(pos)
					.setValue(
							stat.getWorkerClassCounts().get(pos).getValue() + 1);
		} else {
			worker_class.setValue(1);
			stat.getWorkerClassCounts().add(worker_class);
		}

		// set the total for averages
		NVPair<Float> position_avg = new NVPair<Float>();
		position_avg.setName(contract.getPosition());
		pos = -1;
		if ((pos = stat.getPositionAveragePay().indexOf(position_avg)) > -1) {
			try {
				Float value = stat.getPositionAveragePay().get(pos).getValue();
				if (value != null) {
					stat.getPositionAveragePay()
							.get(pos)
							.setValue(
									stat.getPositionAveragePay().get(pos)
											.getValue()
											+ Float.parseFloat(contract
													.getRateOfPay()));
					stat.getPositionAveragePay().get(pos).increment();
				} else {
					stat.getPositionAveragePay().get(pos).setValue(0f);
				}
			} catch (NumberFormatException nfe) {

			}
		} else {
			position.setValue(0);
			stat.getPositionAveragePay().add(position_avg);
		}

		NVPair<Float> gender_avg = new NVPair<Float>();
		gender_avg.setName(contract.getWorker().getGender());
		pos = -1;
		if ((pos = stat.getGenderAveragePay().indexOf(gender_avg)) > -1) {
			try {
				Float value = stat.getGenderAveragePay().get(pos).getValue();
				if (value != null) {
					stat.getGenderAveragePay()
							.get(pos)
							.setValue(
									stat.getGenderAveragePay().get(pos)
											.getValue()
											+ Float.parseFloat(contract
													.getRateOfPay()));
					stat.getGenderAveragePay().get(pos).increment();
				} else {
					stat.getGenderAveragePay().get(pos).setValue(0f);
				}
			} catch (NumberFormatException nfe) {

			}
		} else {
			position.setValue(0);
			stat.getGenderAveragePay().add(gender_avg);
		}

		NVPair<Float> worker_avg = new NVPair<Float>();
		worker_avg.setName(contract.getWorkClass());
		pos = -1;
		if ((pos = stat.getWorkerClassAveragePay().indexOf(worker_avg)) > -1) {
			try {
				Float value = stat.getWorkerClassAveragePay().get(pos)
						.getValue();
				if (value != null) {
					stat.getWorkerClassAveragePay()
							.get(pos)
							.setValue(
									stat.getWorkerClassAveragePay().get(pos)
											.getValue()
											+ Float.parseFloat(contract
													.getRateOfPay()));
					stat.getWorkerClassAveragePay().get(pos).increment();
				} else {
					stat.getWorkerClassAveragePay().get(pos).setValue(0f);
				}
			} catch (NumberFormatException nfe) {

			}
		} else {
			position.setValue(0);
			stat.getWorkerClassAveragePay().add(worker_avg);
		}

	}
}

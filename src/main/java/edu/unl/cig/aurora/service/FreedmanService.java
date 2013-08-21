/* Created on Jun 9, 2011 */
package edu.unl.cig.aurora.service;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Logger;

import javax.annotation.ManagedBean;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

import edu.unl.cig.aurora.model.freedmen.Contract;
import edu.unl.cig.aurora.model.freedmen.ContractDestination;
import edu.unl.cig.aurora.model.freedmen.HiringOffice;
import edu.unl.cig.aurora.model.freedmen.Worker;
import java.io.BufferedReader;
import java.io.FileReader;
import java.text.ParseException;
import java.util.Comparator;
import javax.inject.Inject;
import javax.transaction.UserTransaction;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

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

    @Inject
    private UserTransaction tx;
    
    @GET
    @Path("/import")
    public void importData() {

        ArrayList<HiringOffice> hiringOffices = new ArrayList<HiringOffice>();
        ArrayList<ContractDestination> destinations = new ArrayList<ContractDestination>();
        DateTimeFormatter FORMAT = DateTimeFormat.forPattern("yyyy-MM-dd");
        
        String line = "";
        String[] line_parts = new String[0];

        try {

            BufferedReader reader = new BufferedReader(new FileReader("/Users/iancottingham/Desktop/freedman.csv"));
            tx.begin();
            while ((line = reader.readLine()) != null) {
                line = line.replaceAll("\"", "");
                line = line + ",eol";
                line = line.replaceAll("\"", "");
                line_parts = line.split(",");

                HiringOffice office = new HiringOffice();
                office.setName(line_parts[0]);
                office.setLatitude(Float.parseFloat(line_parts[1]));
                office.setLongitude(Float.parseFloat(line_parts[2]));

                if (!hiringOffices.contains(office)) {
                    hiringOffices.add(office);
                } else {
                    office = hiringOffices.get(hiringOffices.indexOf(office)); // get the office that has contracts associated with it
                }

                ContractDestination dest = new ContractDestination();
                dest.setCounty(line_parts[11]);
                try {
                    dest.setLatitude(Float.parseFloat(line_parts[13].trim()));
                    dest.setLongitude(Float.parseFloat(line_parts[14].trim()));
                } catch (NumberFormatException nfe) {
                }
                dest.setState(line_parts[12]);
                dest.setTownship(line_parts[10]);

                if (!destinations.contains(dest)) {
                    destinations.add(dest);
                } else {
                    dest = destinations.get(destinations.indexOf(dest));
                }

                Worker worker = new Worker();
                worker.setFirstName(line_parts[5]);
                worker.setGender(line_parts[4]);
                worker.setLabel(line_parts[7]);
                worker.setLastName(line_parts[6]);

                Contract contract = new Contract();
                try {
                    contract.setAgeAtContractOutset(Integer.parseInt(line_parts[8]));
                } catch (NumberFormatException nfe) {
                }

                contract.setComments(line_parts[19]);
                contract.setContractDate(FORMAT.parseDateTime(line_parts[3]).toDate());
                contract.setEmployerAgent(line_parts[9]);
                contract.setLengthOfServiceMonths(line_parts[17]);
                contract.setPosition(line_parts[15]);
                contract.setRateOfPay(line_parts[18]);
                contract.setRenumeration(line_parts[20]);
                contract.setWorkClass(line_parts[16]);

                contract.setWorker(worker);
                contract.setOffice(office);
                contract.setDestination(dest);

                if (dest.getContracts() == null) {
                    dest.setContracts(new ArrayList<Contract>());
                }
                dest.getContracts().add(contract);

                if (worker.getContracts() == null) {
                    worker.setContracts(new ArrayList<Contract>());
                }
                worker.getContracts().add(contract);

                if (office.getContracts() == null) {
                    office.setContracts(new ArrayList<Contract>());
                }
                office.getContracts().add(contract);
                manager.persist(worker);
                manager.persist(contract);
            }
            
            for (HiringOffice office : hiringOffices) {
                manager.persist(office);
            }
            
            for (ContractDestination dest : destinations) {
                manager.persist(dest);
            }
            tx.commit();
            
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @GET
    @Path("/hiringOffices.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String getHiringOffices(@QueryParam("date") String date, @QueryParam("to") String toDate) throws ParseException {

        List<HiringOffice> offices = manager.createQuery("select h from HiringOffice as h").getResultList();

        Date from_date = FORMAT.parse(date);
        Date to_date = (toDate == null) ? from_date : FORMAT.parse(
                toDate);

        List<ContractContainer> containers = new ArrayList<ContractContainer>();

        for (HiringOffice office : offices) {
            List<Long> contracts = manager
                    .createQuery(
                    "select c.id from Contract as c where c.contractDate between ?1 and ?2 and c.office = ?3")
                    .setParameter(1, from_date)
                    .setParameter(2, to_date).setParameter(3, office).getResultList();

            ContractContainer container = new ContractContainer();
            container.setOffice(office);
            container.setIssuedContracts(contracts.size());

            containers.add(container);
        }

        Collections.sort(containers, new Comparator<ContractContainer>() {
            @Override
            public int compare(ContractContainer arg0, ContractContainer arg1) {
                if (arg0.getIssuedContracts() > arg1.getIssuedContracts()) {
                    return -1;
                } else if (arg0.getIssuedContracts() < arg1.getIssuedContracts()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });

        StringBuffer geo_json = new StringBuffer("{ \"type\": \"FeatureCollection\", \"features\": [");

        boolean first = true;
        for (ContractContainer container : containers) {

            if (!first) {
                geo_json.append(",");
            } else {
                first = false;
            }

            geo_json.append(container);

        }
        geo_json.append("]}");
        return geo_json.toString();
    }

    @GET
    @Path("/destinations/{officeId}.json")
    @Produces(MediaType.APPLICATION_JSON)
    public String getDestinationsFor(@PathParam("officeId") long officeId, @QueryParam("date") String date, @QueryParam("to") String toDate) throws ParseException {

        Date from_date = FORMAT.parse(date);
        Date to_date = (toDate == null) ? from_date : FORMAT.parse(
                toDate);

        HiringOffice office = manager.find(HiringOffice.class, officeId);

        List<Contract> contracts = manager
                .createQuery(
                "select c from Contract as c where c.contractDate between ?1 and ?2 and c.office = ?3")
                .setParameter(1, from_date)
                .setParameter(2, to_date).setParameter(3, office).getResultList();
        HashMap<Long, DestinationContainer> destinations = new HashMap<Long, DestinationContainer>();
        for (Contract c : contracts) {

            DestinationContainer container = destinations.get(c.getDestination().getId());
            if (container == null) {
                container = new DestinationContainer();
                container.setDestination(c.getDestination());
                container.setSentContracts(1);
                destinations.put(c.getDestination().getId(), container);
            } else {
                container.setSentContracts(container.getSentContracts() + 1);
            }
        }
        ArrayList<DestinationContainer> containers = new ArrayList<FreedmanService.DestinationContainer>(destinations.values());

        Collections.sort(containers,
                new Comparator<DestinationContainer>() {
            @Override
            public int compare(DestinationContainer arg0,
                    DestinationContainer arg1) {
                if (arg0.getSentContracts() > arg1.getSentContracts()) {
                    return -1;
                } else if (arg0.getSentContracts() < arg1.getSentContracts()) {
                    return 1;
                } else {
                    return 0;
                }
            }
        });


        StringBuffer geo_json = new StringBuffer("{ \"type\": \"FeatureCollection\", \"features\": [");
        boolean first = true;
        for (DestinationContainer dest : containers) {

            if (!first) {
                geo_json.append(",");
            } else {
                first = false;
            }

            geo_json.append(dest);
        }

        geo_json.append(
                "]}");
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

        if (office
                == null) {
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

        Collections.sort(contracts,
                new Comparator<Contract>() {
            @Override
            public int compare(Contract arg0, Contract arg1) {

                if (arg0.getDestination().getState().equals(arg1.getDestination().getState())) {
                    return arg0.getDestination().getFullPlaceName().compareTo(arg1.getDestination().getFullPlaceName());
                } else {
                    return arg0.getDestination().getState().compareTo(arg1.getDestination().getState());
                }
            }
        });


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

        if (destination
                == null) {
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

    protected class ContractContainer {

        private HiringOffice office;
        private int issuedContracts;

        public HiringOffice getOffice() {
            return office;
        }

        public void setOffice(HiringOffice office) {
            this.office = office;
        }

        public int getIssuedContracts() {
            return issuedContracts;
        }

        public void setIssuedContracts(int issuedContracts) {
            this.issuedContracts = issuedContracts;
        }

        @Override
        public String toString() {
            StringBuffer geo_json = new StringBuffer();

            geo_json.append("{\"type\" : \"Feature\",");
            geo_json.append("\"geometry\": {\"type\": \"Point\", \"coordinates\": [");
            geo_json.append(String.format("%f, %f", office.getLongitude(), office.getLatitude()));
            geo_json.append("]}, \"properties\": {");
            geo_json.append(String.format("\"placeName\":\"%s\",", office.getName()));
            geo_json.append(String.format("\"contractCount\":%d,", this.issuedContracts));
            geo_json.append(String.format("\"placeId\":%d", this.office.getId()));
            geo_json.append("}");
            geo_json.append("}");

            return geo_json.toString();
        }
    };

    protected class DestinationContainer {

        private ContractDestination destination;
        private int sentContracts;

        public ContractDestination getDestination() {
            return destination;
        }

        public void setDestination(ContractDestination destination) {
            this.destination = destination;
        }

        public int getSentContracts() {
            return sentContracts;
        }

        public void setSentContracts(int sentContracts) {
            this.sentContracts = sentContracts;
        }

        @Override
        public String toString() {
            StringBuffer geo_json = new StringBuffer();

            geo_json.append("{\"type\" : \"Feature\",");
            geo_json.append("\"geometry\": {\"type\": \"Point\", \"coordinates\": [");
            geo_json.append(String.format("%f, %f", this.getDestination().getLongitude(), this.getDestination().getLatitude()));
            geo_json.append("]}, \"properties\": {");
            geo_json.append(String.format("\"placeName\":\"%s %s %s\",", this.getDestination().getTownship(), this.getDestination().getCounty(), this.getDestination().getState()));
            geo_json.append(String.format("\"contractCount\":%d,", this.getSentContracts()));
            geo_json.append(String.format("\"placeId\":%d", this.getDestination().getId()));
            geo_json.append("}");
            geo_json.append("}");

            return geo_json.toString();
        }
    };
}

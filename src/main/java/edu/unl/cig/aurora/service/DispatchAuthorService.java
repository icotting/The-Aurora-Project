/* Created on Jan 30, 2011 */
package edu.unl.cig.aurora.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.persistence.PersistenceContextType;
import javax.sql.DataSource;
import javax.transaction.UserTransaction;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import edu.unl.cig.aurora.model.dispatch.Place;
import edu.unl.cig.aurora.model.dispatch.PlaceOccurrence;
import edu.unl.cig.aurora.model.dispatch.ReviewNote;

/**
 * @author Ian Cottingham
 * 
 */
@Path("author/dispatchXML")
@ManagedBean
public class DispatchAuthorService {

	@Resource(name="jdbc/aurora")
	DataSource source;
	
	@PersistenceContext(unitName="AuroraUnit", type=PersistenceContextType.TRANSACTION)
	EntityManager manager;
	
	@Resource
	UserTransaction tx;
	
	@POST
	@Produces("application/json")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/update")
	public String updateReference(@FormParam("placeId") long placeId, @FormParam("notes") String notes,
			@FormParam("latitude") float latitude, @FormParam("longitude") float longitude, @FormParam("action") String action) {


		try {
			tx.begin();
			Place place = manager.find(Place.class, placeId);
			ReviewNote note = new ReviewNote();
			note.setPlace(place);
			note.setAction(action);
			note.setComment(notes);
			manager.persist(note);
			
			
			place.setNote(note);	
			place.setReviewed(true);
			place.setLatitude(latitude);
			place.setLongitude(longitude);
			
			if ( action.equals("Delete Point") ) {
				place.setDeleted(true);
			}
			
			manager.merge(place);
			tx.commit();
			
		} catch (Exception e) {
			try { 
				tx.rollback();
			} catch ( Exception ex ) { System.err.println("WARNING: could not roll back a transaction."); }
		
			e.printStackTrace(System.err);
			RuntimeException re = new RuntimeException(
					"could not execute the query");
			re.initCause(e);
			throw re;
		}

		return "{\"result\": \"success\"}";
	}
	
	@PUT
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Path("/useAlternatePlace")
	public void switchAlternatePlace(@FormParam("occurrenceId") long occurrenceId, @FormParam("alternatePlaceId") long alternatePlaceId) {
		
		try {
			PlaceOccurrence occurrence = manager.find(PlaceOccurrence.class, occurrenceId);
			Place current_place = occurrence.getPlace();
			int len = occurrence.getAlternatePlaces().size();
			int index = -1;
			
			for ( int i=0; i<len; i++ ) {
				if ( occurrence.getAlternatePlaces().get(i).getId() == alternatePlaceId ) {
					index = i;
					break;
				}
			}
			
			if ( index == -1 ) { 
				throw new Exception(alternatePlaceId+" was not an alternate place for the occurrence "+occurrenceId);
			} else { 
				Place alt = occurrence.getAlternatePlaces().remove(index);
				occurrence.getAlternatePlaces().add(current_place);
				occurrence.setPlace(alt);
				
				manager.merge(occurrence);
			}
			
		} catch ( Exception e ) { 
			RuntimeException re = new RuntimeException("could not swap places for "+alternatePlaceId+" on occurrence "+occurrenceId);
			re.initCause(e);
			throw re;
		}
		
	}
	
	@GET
	@Produces("application/json")
	@Path("/place/{place}.json")
	public Place getPlaceForLegacyId(@PathParam("place") String legacyId) {
		try {
			List<Place> places = manager.createQuery("select p from Place as p where p.legacyId = ?1").setParameter(1, legacyId).getResultList();
			if ( places.size() != 1 ) { 
				throw new Exception("the result list contained "+places.size()+" places");
			} else { 
				return places.get(0);
			}
		} catch ( Exception e ) { 
			RuntimeException re = new RuntimeException("could not get a place for legacy ID "+legacyId);
			re.initCause(e);
			throw re;
		}
	}
	
	@GET
	@Produces("application/json")
	@Path("/unreviewed.json")
	public List<Place> unreviewedReferences() {
		
		List<Long> place_ids = new ArrayList<Long>();
		
		Connection conn = null;
		try {
			conn = source.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select ID from PLACE where REVIEWED = 0 limit 15");
			ResultSet rs = stmt.executeQuery();
			while ( rs.next() ) { 
				place_ids.add(rs.getLong(1));
			}
			
		} catch ( Exception e ) { 
			RuntimeException re = new RuntimeException("could not get the ids");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception ex ) { /* should log this as a warning */ }
		}
		
		ArrayList<Place> places = new ArrayList<Place>();
		for ( long id : place_ids ) { 
			places.add(manager.find(Place.class, id));
		}
		
		return places;
	}

}

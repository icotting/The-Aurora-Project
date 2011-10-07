package edu.unl.cig.aurora.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.Date;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.ManagedBean;
import javax.annotation.Resource;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;

import org.joda.time.DateTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import edu.unl.cig.aurora.model.PlaceCollection;
import edu.unl.cig.aurora.model.PlaceCollectionBuilder;
import edu.unl.cig.aurora.model.PlaceCollectionBuilder.RollupRule;
import edu.unl.cig.aurora.model.PlaceReference;

@Path("dispatchXML")
@ManagedBean
public class DispatchService {

	private static final Logger LOG = Logger.getLogger(DispatchService.class.getName());
	private static final String BASE_QUERY = "select *, count(pl.name) as ref_count from Place pl " +
					"inner join PlaceOccurrence plo on plo.place_id = pl.id inner join Sentence s on " +
					"plo.sentence_id = s.id inner join DispatchVolume dv on dv.id = s.volume_id ";
	
	private static final DateTimeFormatter FORMAT = DateTimeFormat
	.forPattern("yyyy-MM-dd");
	
	@Resource(name="jdbc/aurora")
	private DataSource source;
	
	@GET
	@Produces("application/json")
	@Path("/places/names/{place}.json")
	public PlaceCollection getPlaces(@PathParam("place") String placeName,
			@QueryParam("rangeStart") String rangeStartDate,
			@QueryParam("rangeEnd") String rangeEndDate, @QueryParam("fullResults") boolean returnFull) {

		Connection conn = null;
		try {
			conn = source.getConnection();
			PreparedStatement stmt = conn.prepareStatement(BASE_QUERY+" where pl.reviewed = 1 and pl.name like ? and dv.volumedate >= ? and dv.volumedate <= ? group by dv.id ");
			
			stmt.setString(1, "%"+placeName+"%");
			
			if ( rangeStartDate != null && rangeEndDate != null ) {
				stmt.setString(2, rangeStartDate);
				stmt.setString(3, rangeEndDate);
			} else {
				
			}
			
			ResultSet results = stmt.executeQuery();
			
			PlaceCollectionBuilder builder = new PlaceCollectionBuilder();
			while ( results.next() ) { 
				builder.addPlaceReference(extractReferenceFromRow(results));
			}
			
			return returnFull ? builder.getCollection() : builder.getRolledUpCollection();
			
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(
					"could not execute the query");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception e ) { 
				LOG.log(Level.WARNING, "could not close a connection", e);
			}
		}
	}

	@GET
	@Produces("application/json")
	@Path("/places/dates/{date}.json")
	public PlaceCollection getPlacesForDate(@PathParam("date") String date,
			@QueryParam("to") String toDate, @QueryParam("flatten") boolean flatten, @QueryParam("constraint") String constraint) {

		Connection conn = null;
		try {
			conn = source.getConnection();
			PreparedStatement stmt;
			
			String query_constraint = ( constraint == null ) ? "" : " and s.text like ?";
			
			if ( toDate != null ) { 
				stmt = conn.prepareStatement(BASE_QUERY+" where pl.reviewed = 1 and dv.volumedate >= ? and dv.volumedate <= ?"+query_constraint+" group by pl.name, dv.id");
				stmt.setString(1, date);
				stmt.setString(2, toDate);
				if ( constraint != null ) {
					stmt.setString(3, "%"+constraint+"%");
				}
			} else { 
				stmt = conn.prepareStatement(BASE_QUERY+" where "+query_constraint+"pl.reviewed = 1 and dv.volumedate = ?"+query_constraint+" group by pl.name, dv.id");
				stmt.setString(1, date);
				if ( constraint != null ) {
					stmt.setString(2, "%"+constraint+"%");
				}
			}
			
			ResultSet results = stmt.executeQuery();
			
			PlaceCollectionBuilder builder = new PlaceCollectionBuilder();
			while ( results.next() ) { 
				builder.addPlaceReference(extractReferenceFromRow(results));
			}
			
			return ( flatten ) ?  builder.getRolledUpCollection(RollupRule.SINGLE_VALUE) : builder.getCollection();

		} catch (Exception e) {
			RuntimeException re = new RuntimeException(
					"could not execute the query");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception e ) { 
				LOG.log(Level.WARNING, "could not close a connection", e);
			}
		}
	}
	
	@GET
	@Produces(MediaType.TEXT_HTML)
	@Path("/places/{place}/text/{date}.html")
	public String getPlaceTextForPlaceDate(@PathParam("place") String placeId, @PathParam("date") String date,
			@QueryParam("to") String toDate, @QueryParam("constraint") String constraint) {

		Connection conn = null;
		try {
			conn = source.getConnection();
			
			String query_constraint = ( constraint == null ) ? "" : " and s.text like ?";
			
			PreparedStatement stmt = conn.prepareStatement("select s.text, s.number, po.actualtext,dv.volumedate,p.name from sentence s " +
					"inner join placeoccurrence po on po.sentence_id = s.id inner join place p on po.place_id = p.id inner join " +
					"dispatchvolume dv on s.volume_id = dv.id where p.legacyid = ? and dv.volumedate >= ? and dv.volumedate <= ?"+
					query_constraint+" order by dv.volumedate");
			
			stmt.setString(1, placeId);
			stmt.setString(2, date);
			if ( toDate != null ) { 
				stmt.setString(3, toDate);
			} else {
				stmt.setString(3, date);
			}
			
			if ( constraint != null ) {
				stmt.setString(4, "%"+constraint+"%");
			}		
						
			ResultSet results = stmt.executeQuery();
			StringBuffer sb = null;
			
			Date last_seen = null;
			Date comp;
			String sentence;
			Set<Integer> sentence_seen = null;
			
			while ( results.next() ) { 
				comp = results.getDate(4);
				if ( sb == null ) { 
					sentence_seen = new TreeSet<Integer>();
					sb = new StringBuffer("<h1>Text results for place ");
					sb.append(results.getString(5));
					sb.append("</h1>");
				}
				
				if ( last_seen == null || (comp.compareTo(last_seen) != 0) ) {
					sb.append("<h3>References for date ");
					sb.append(comp.toString());
					sb.append("</h3>");
					last_seen = comp;
				}
				
				if ( !sentence_seen.contains(results.getInt(2)) ) {
					sentence = results.getString(1);
					sentence = sentence.replaceAll(results.getString(3), "<span class=\"placehighlight\">"+results.getString(3)+"</span>");
					if ( constraint != null ) {
						sentence = sentence.replaceAll(constraint, "<span class=\"constrainthighlight\">"+constraint+"</span>");				
					}
					
					sb.append("<p>");
					sb.append(sentence);
					sb.append("<br/><strong>Sentence number: ");
					sb.append(results.getInt(2));
					sb.append("</strong>");
					sb.append("</p>");
					
					sentence_seen.add(results.getInt(2));
				}
			}
			
			return sb.toString();

		} catch (Exception e) {
			RuntimeException re = new RuntimeException(
					"could not execute the query");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception e ) { 
				LOG.log(Level.WARNING, "could not close a connection", e);
			}
		}
	}
	
	@GET
	@Produces("application/json")
	@Path("/places/{key}.json")
	public PlaceCollection getPlacesForKey(@PathParam("key") String placeKey,
			@QueryParam("rangeStart") String rangeStartDate,
			@QueryParam("rangeEnd") String rangeEndDate, @QueryParam("fullResults") boolean returnFull) {

		Connection conn = null;
		try {
			conn = source.getConnection();
			PreparedStatement stmt = conn.prepareStatement(BASE_QUERY+" where pl.reviewed = 1 and pl.legacyid like ? and " +
					"dv.volumedate >= ? and dv.volumedate <= ? group by dv.id ");
			
			stmt.setString(1, "%"+placeKey+"%");
			
			if ( rangeStartDate != null && rangeEndDate != null ) {
				stmt.setString(2, rangeStartDate);
				stmt.setString(3, rangeEndDate);
			} else {
				
			}
			
			ResultSet results = stmt.executeQuery();
			
			PlaceCollectionBuilder builder = new PlaceCollectionBuilder();
			while ( results.next() ) { 
				builder.addPlaceReference(extractReferenceFromRow(results));
			}
			
			return returnFull ? builder.getCollection() : builder.getRolledUpCollection();
			
		} catch (Exception e) {
			RuntimeException re = new RuntimeException(
					"could not execute the query");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception e ) { 
				LOG.log(Level.WARNING, "could not close a connection", e);
			}
		}
	}
	
	private PlaceReference extractReferenceFromRow(ResultSet results) throws Exception { 
		return new PlaceReference(results.getString("pl.name"), results.getString("plo.actualtext"), 
				results.getInt("ref_count"), new DateTime(results.getDate("dv.volumedate").getTime()), 
				results.getString("pl.legacyid"), results.getDouble("pl.latitude"), 
				results.getDouble("pl.longitude"));
	}
}

/* Created on Aug 1, 2011 */
package edu.unl.cig.aurora.service;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.HashMap;
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

import edu.unl.cig.aurora.model.gis.BoundingBox;
import edu.unl.cig.aurora.model.gis.Coordinate;
import edu.unl.cig.aurora.model.gis.Line;

/**
 * @author Ian Cottingham
 * 
 */
@Path("/boundaries")
@ManagedBean
public class BoundaryService {

	private static final Logger LOG = Logger.getLogger(BoundaryService.class
			.getName());

	@Resource(name = "jdbc/aurora")
	private DataSource source;

	@GET
	@Path("/{B}/railPopulationShift.json")
	@Produces(MediaType.APPLICATION_JSON)
	public String getShiftData(@PathParam("B") String bounds) {
		StringBuffer sb = new StringBuffer("{\"timeSeries\" : { \"population\" : [");
		
		Connection conn = null;
		try { 
			
			conn = source.getConnection();
			BoundingBox bbox = BoundingBox.boxFromPolyMapBounds(bounds);

			PreparedStatement p_query = conn.prepareStatement("select sum(SLAVE_POPULATION), sum(NON_SLAVE_POPULATION), sum(OTHER_POPULATION), pd.YEAR " +
					"from POPULATION_DATA pd inner join NHGIS_BOUNDARIES nb on pd.NHGIS_BOUNDARY = nb.ID where COUNTY_NAME is not null " +
					"and Intersects(GEO, PolygonFromText('"+bbox.toSqlString()+"')) group by pd.YEAR");
			
			PreparedStatement t_query = conn.prepareStatement("select sum(TRACK_MILES), cr.YEAR from COUNTY_RAIL cr inner join NHGIS_BOUNDARIES " +
					"nb on nb.ID = cr.COUNTY_ID where COUNTY_NAME is not null and Intersects(GEO, PolygonFromText('"+bbox.toSqlString()+"')) group by cr.YEAR");
				
			ResultSet p_res = p_query.executeQuery();
			int index = 0;
			while ( p_res.next() ) { 
				if ( index++ > 0 ) { 
					sb.append(",");
				}
				sb.append("{ \"year\":");
				sb.append(p_res.getInt(4));
				sb.append(", \"freePersons\" : ");
				sb.append(p_res.getInt(2));
				sb.append(", \"slaves\" : ");
				sb.append(p_res.getInt(1));
				sb.append(", \"other\" : ");
				sb.append(p_res.getInt(3));
				sb.append(", \"total\" : ");
				sb.append(p_res.getInt(1)+p_res.getInt(2)+p_res.getInt(3));
				sb.append("}");
			}
			
			sb.append("]");
			sb.append("}");
			sb.append(", \"trackLengths\" : [");
			
			index = 0;
			ResultSet t_res = t_query.executeQuery();
			while ( t_res.next() ) {
				if ( index++ > 0 ) { 
					sb.append(",");
				}
				sb.append("{ \"year\" : ");
				sb.append(t_res.getInt(2));
				sb.append(", \"trackLength\" : ");
				sb.append(t_res.getFloat(1));
				sb.append("}");
			}
			
			sb.append("]");
			sb.append("}");
			
		} catch ( Exception e ) { 
			LOG.log(Level.SEVERE, "An error occurred building the chart data", e);
			RuntimeException re = new RuntimeException("Could not create the chart data");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception ex ) { 
				LOG.log(Level.WARNING, "Could not close a connection", ex);
			}
		}
			
		return sb.toString();
	}
	
	@GET
	@Path("/{type}/{year}/{B}/{Z}/stats.json")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCountyChartData(@PathParam("type") BoundaryType type, @PathParam("year") int year,
			@PathParam("B") String bounds, @PathParam("Z") float zoomLevel, @QueryParam("railYear") int railYear) {

		Connection conn = null;
		try { 
			BoundingBox bbox = BoundingBox.boxFromPolyMapBounds(bounds);

			String query_string;
			String track_qstring;
			
			if ( type == BoundaryType.US_COUNTY ) {
				query_string = "select SLAVE_POPULATION, NON_SLAVE_POPULATION,OTHER_POPULATION,STATE_NAME,COUNTY_NAME from POPULATION_DATA pd " +
					"inner join NHGIS_BOUNDARIES nb on pd.NHGIS_BOUNDARY = nb.ID where COUNTY_NAME is not null and pd.YEAR = ? and " +
					"Intersects(GEO, PolygonFromText('"+bbox.toSqlString()+"'))";
				
				track_qstring = "select distinct(cr.COUNTY_ID),TRACK_MILES,COUNTY_NAME,STATE_NAME from COUNTY_RAIL cr inner join NHGIS_BOUNDARIES nb on nb.ID = cr.COUNTY_ID " +
						"where cr.YEAR = ? and COUNTY_NAME is not null and Intersects(GEO, PolygonFromText('"+bbox.toSqlString()+"'))";
				
			} else { 
				query_string = "select sum(SLAVE_POPULATION), sum(NON_SLAVE_POPULATION), sum(OTHER_POPULATION),STATE_NAME from POPULATION_DATA pd " +
						"inner join NHGIS_BOUNDARIES nb on pd.NHGIS_BOUNDARY = nb.ID where pd.YEAR = ? and " +
						"Intersects(GEO, PolygonFromText('"+bbox.toSqlString()+"')) group by STATE_NAME";
			
				track_qstring = "select distinct(cr.COUNTY_ID),sum(TRACK_MILES),STATE_NAME from COUNTY_RAIL cr inner join NHGIS_BOUNDARIES nb on nb.ID = cr.COUNTY_ID where " +
						"cr.YEAR = ? and COUNTY_NAME is not null and Intersects(GEO, PolygonFromText('"+bbox.toSqlString()+"')) group by STATE_NAME";
			}
			
			
			conn = source.getConnection();
			
			HashMap<String, Integer> track_lengths = new HashMap<String, Integer>();
			
			PreparedStatement track_query = conn.prepareStatement(track_qstring);
			track_query.setInt(1, railYear);
			ResultSet track_data = track_query.executeQuery();
			while ( track_data.next() ) { 
				String region_name = (type == BoundaryType.US_COUNTY) ? track_data.getString(3)+" County "+track_data.getString(4) : track_data.getString(3);
				track_lengths.put(region_name, track_data.getInt(2));
			}
			
			
			PreparedStatement query = conn.prepareStatement(query_string);
			query.setInt(1, year);

			ResultSet rs = query.executeQuery();
			
			int free_total = 0;
			int slave_total = 0;
			int other_total = 0;
			float track_total = 0;
			ArrayList<Integer> free_pops = new ArrayList<Integer>();
			ArrayList<Integer> slave_pops = new ArrayList<Integer>();
			ArrayList<Integer> other_pops = new ArrayList<Integer>();
			ArrayList<Integer> track_miles = new ArrayList<Integer>();
			ArrayList<String> regions = new ArrayList<String>();

			while ( rs.next() ) { 
				String region_name = (type == BoundaryType.US_COUNTY ? (rs.getString(5)+" County "+rs.getString(4)) : rs.getString(4));
				regions.add(region_name);
				
				free_pops.add(rs.getInt(2));
				slave_pops.add(rs.getInt(1));
				other_pops.add(rs.getInt(3));
				if ( track_lengths.get(region_name) != null ) {
					track_miles.add(track_lengths.get(region_name));
				} else {
					track_miles.add(0);
				}
				
				free_total += rs.getInt(2);
				slave_total += rs.getInt(1);
				other_total += rs.getInt(3);
				
				if ( track_lengths.get(region_name) != null ) {
					track_total += track_lengths.get(region_name);
				}
			}
			
			StringBuffer sb = new StringBuffer("{\"totals\": {");
			sb.append("\"free\":");
			sb.append(free_total);
			sb.append(",\"slave\":");
			sb.append(slave_total);
			sb.append(",\"other\":");
			sb.append(other_total);
			sb.append(",\"trackmiles\":");
			sb.append(track_total);
			sb.append("},");
			sb.append("\"regions\":[");
			
			int index = 0;
			for ( String region : regions ) { 
				if ( index++ > 0 ) { // if it isn't the first element, append a comma
					sb.append(",");
				}
				sb.append("\"");
				sb.append(region);
				sb.append("\"");
			}
			sb.append("],");
			sb.append("\"regionTotals\": {");
			sb.append("\"free\":[");
			
			index = 0;
			for ( Integer pop : free_pops ) {
				if ( index++ > 0 ) {
					sb.append(",");
				}
				sb.append(pop);
			}
			sb.append("],");
			sb.append("\"slave\":[");
			
			index = 0;
			for ( Integer pop : slave_pops ) {
				if ( index++ > 0 ) {
					sb.append(",");
				}
				sb.append(pop);
			}
			sb.append("],");
			sb.append("\"other\":[");
			
			index = 0;
			for ( Integer pop : other_pops ) {
				if ( index++ > 0 ) {
					sb.append(",");
				}
				sb.append(pop);
			}
			sb.append("],");
			sb.append("\"trackmiles\":[");
			
			index = 0;
			for ( Integer length : track_miles ) {
				if ( index++ > 0 ) {
					sb.append(",");
				}
				sb.append(length);
			}
			sb.append("]");
			sb.append("}}");

			return sb.toString();
		} catch ( Exception e ) { 
			LOG.log(Level.SEVERE, "An error occurred building the chart data", e);
			RuntimeException re = new RuntimeException("Could not create the chart data");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception ex ) { 
				LOG.log(Level.WARNING, "Could not close a connection", ex);
			}
		}
	}
	
	
	
	@GET
	@Path("/counties/{year}.json")
	@Produces(MediaType.APPLICATION_JSON)
	public String getCounties(@PathParam("year") int year,
			@QueryParam("B") String bounds, @QueryParam("Z") float zoomLevel) {

		if (zoomLevel >= 6) {
			try {
				return checkCache(year, bounds, (int) zoomLevel, "county");
			} catch (CacheMissException ce) {
				return queryFeature(year, bounds, zoomLevel, true);
			}
		} else {
			return "{ \"type\": \"FeatureCollection\",\"features\": []}";
		}

	}

	@GET
	@Path("/states/{year}.json")
	@Produces(MediaType.APPLICATION_JSON)
	public String getStates(@PathParam("year") int year,
			@QueryParam("B") String bounds, @QueryParam("Z") float zoomLevel) {

		try {
			return checkCache(year, bounds, (int) zoomLevel, "state");
		} catch (CacheMissException ce) {
			return queryFeature(year, bounds, zoomLevel, false);
		}
	}

	private String checkCache(int year, String bounds, int zoom, String type)
			throws CacheMissException {

		try {
			File cache_file = new File(System.getProperty("java.io.tmpdir")
					+ File.separator + "edu.unl.cig.aurora.TileCache"
					+ File.separator + type + File.separator + (int) zoom
					+ File.separator + bounds + File.separator + year + ".json");
			
			if (cache_file.exists()) {
				BufferedReader reader = new BufferedReader(new FileReader(
						cache_file));
				StringBuffer sb = new StringBuffer();
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line);
				}
				reader.close();
				return sb.toString();
			}
		} catch (Exception e) {
			LOG.log(Level.WARNING,
					"Could not query the cache, the tile will be computed", e);
			throw new CacheMissException();
		}
		throw new CacheMissException();
	}

	private String queryFeature(final int year, final String bounds,
			final float zoomLevel, final boolean county) {

		final StringBuffer sb = new StringBuffer(
				"{ \"type\": \"FeatureCollection\",\"features\": [");
		Connection conn = null;

		try {
			conn = source.getConnection();

			String query_string = county ? "select ID,COUNTY_NAME,STATE_NAME from NHGIS_BOUNDARIES where YEAR = ? and "
					+ "COUNTY_NAME IS NOT NULL and Intersects(GEO, ?) group by ID"
					: "select ID,STATE_NAME from NHGIS_BOUNDARIES where YEAR = ? and COUNTY_NAME "
							+ "IS NULL and Intersects(GEO, ?) group by ID";

			PreparedStatement feature_query = conn
					.prepareStatement(query_string);
			feature_query.setInt(1, year);

			BoundingBox bbox = BoundingBox.boxFromPolyMapBounds(bounds);
			ResultSet geo_code = conn.createStatement().executeQuery(
					"select PolygonFromText(\"" + bbox.toSqlString() + "\")");
			if (geo_code.next()) {
				feature_query.setObject(2, geo_code.getObject(1));
			}
			PreparedStatement poly_query = conn
					.prepareStatement("select COORDS from BOUNDARY_COORDS where BELONGS_TO = ?");

			ResultSet features = feature_query.executeQuery();
			boolean first_feature = true;

			while (features.next()) {
				poly_query.setLong(1, features.getLong(1));
				if (!first_feature) {
					sb.append(", ");
				} else {
					first_feature = false;
				}

				sb.append("{ \"type\": \"Feature\", \"geometry\":{ ");
				sb.append("\"type\": \"Polygon\", \"coordinates\":[");

				ResultSet boundaries = poly_query.executeQuery();
				boolean has_boundary = boundaries.next();

				while (has_boundary) {
					sb.append("[");
					boolean first = true;
					for (Coordinate coord : this.getLineCoordinates(
							boundaries.getString(1), (int) zoomLevel)) {
						if (!first) {
							sb.append(",");
						} else {
							first = false;
						}

						sb.append("[");
						sb.append(coord.getLongitude());
						sb.append(",");
						sb.append(coord.getLatitude());
						sb.append("]");
					}

					sb.append("]");
					if ((has_boundary = boundaries.next())) {
						sb.append(",");
					}
				}

				sb.append("]},");
				sb.append("\"properties\": {");
				sb.append("\"featureName\":\"");
				sb.append(features.getString(2));
				sb.append("\", ");
				sb.append("\"boundaryType\":\""
						+ (county ? "US County" : "US State") + "\"");

				addPopulationProperties(county ? features.getString(3)
						: features.getString(2), county ? features.getString(2)
						: null, year, sb, conn);

				sb.append("}}");
				boundaries.close();
			}

			sb.append("]}");

			new Thread(new Runnable() {

				@Override
				public void run() {
					try {
						File cache_dir = new File(System
								.getProperty("java.io.tmpdir")
								+ File.separator
								+ "edu.unl.cig.aurora.TileCache"
								+ File.separator
								+ (county ? "county" : "state")
								+ File.separator
								+ (int) zoomLevel
								+ File.separator + bounds);

						if (!cache_dir.exists()) {
							cache_dir.mkdirs();
						}
						PrintWriter cache = new PrintWriter(new File(cache_dir
								.getAbsolutePath()
								+ File.separator
								+ year
								+ ".json"));

						cache.print(sb.toString());
						cache.flush();
						cache.close();
					} catch (Exception e) {
						LOG.log(Level.WARNING,
								"Could not write the cache due to the following exception:",
								e);
					}
				}
			}).start();

		} catch (Exception e) {
			LOG.log(Level.SEVERE, "Could not query the state boundaries", e);
			RuntimeException re = new RuntimeException(
					"Error querying GIS boundary");
			re.initCause(e);
			throw re;
		} finally {
			try {
				conn.close();
			} catch (Exception ex) {
				LOG.log(Level.WARNING, "Could not close a connection", ex);
			}
		}

		return sb.toString();
	}

	private ArrayList<Coordinate> getLineCoordinates(String coordString,
			int zoomLevel) {

		Line l = new Line(coordString, true);
		float tol;

		switch (zoomLevel) {
		case 0:
			tol = 400f;
			break;
		case 1:
			tol = 200f;
			break;
		case 2:
			tol = 100f;
			break;
		case 3:
			tol = 50f;
			break;
		case 4:
			tol = 25f;
			break;
		case 5:
			tol = 12.5f;
			break;
		case 6:
			tol = 6.25f;
			break;
		case 7:
			tol = 3.125f;
			break;
		case 8:
			tol = 1.56f;
			break;
		case 9:
			tol = 0.753f;
			break;
		case 10:
			tol = 0.375f;
			break;
		default:
			tol = 0f;
		}

		return l.simplify(tol);
	}

	/*
	 * This method takes a connection so that, if an error occurrs, it can be
	 * closed by the finally block of the caller
	 */
	private void addPopulationProperties(String stateName, String countyName,
			int year, StringBuffer sb, Connection conn) throws Exception {

		PreparedStatement stmt;
		if (countyName == null && stateName.contains("Territory")) {
			stmt = conn
					.prepareStatement("select NON_SLAVE_POPULATION, SLAVE_POPULATION, OTHER_POPULATION, TOTAL_POPULATION from POPULATION_DATA "
							+ "pd inner join NHGIS_BOUNDARIES b on pd.NHGIS_BOUNDARY = b.ID  where b.STATE_NAME = ? and pd.YEAR = ?");

			stmt.setString(1, stateName);
			stmt.setInt(2, year);

		} else if (countyName == null) {
			stmt = conn
					.prepareStatement("select sum(NON_SLAVE_POPULATION), sum(SLAVE_POPULATION), sum(OTHER_POPULATION), sum(TOTAL_POPULATION) from POPULATION_DATA "
							+ "pd inner join NHGIS_BOUNDARIES b on pd.NHGIS_BOUNDARY = b.ID  where b.STATE_NAME = ? and pd.YEAR = ?");

			stmt.setString(1, stateName);
			stmt.setInt(2, year);
		} else {
			stmt = conn
					.prepareStatement("select NON_SLAVE_POPULATION, SLAVE_POPULATION, OTHER_POPULATION, TOTAL_POPULATION from POPULATION_DATA "
							+ "pd inner join NHGIS_BOUNDARIES b on pd.NHGIS_BOUNDARY = b.ID  where b.STATE_NAME = ? and b.COUNTY_NAME = ? and pd.YEAR = ?");

			stmt.setString(1, stateName);
			stmt.setString(2, countyName);
			stmt.setInt(3, year);
		}

		ResultSet rs = stmt.executeQuery();
		if (rs.next()) {
			sb.append(", \"freePopulation\":");
			sb.append(rs.getInt(1));
			sb.append(", \"slavePopulation\":");
			sb.append(rs.getInt(2));
			sb.append(", \"otherPopulation\":");
			sb.append(rs.getInt(3));
			sb.append(", \"totalPopulation\":");
			sb.append(rs.getInt(4));
		}
	}

	protected class CacheMissException extends Exception {

		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

	}
	
	protected enum BoundaryType {
		US_STATE,
		US_COUNTY;
	}
}

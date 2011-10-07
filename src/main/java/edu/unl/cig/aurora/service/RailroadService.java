/* Created on Jun 8, 2011 */
package edu.unl.cig.aurora.service;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
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

/**
 * @author Ian Cottingham 
 *
 */
@Path("/railroad")
@ManagedBean
public class RailroadService {

	private static final Logger LOG = Logger.getLogger(RailroadService.class.getName());
	
	@Resource(name="jdbc/aurora")
	private DataSource source;
	
	@GET
	@Path("/network/{authority}")
	@Produces(MediaType.APPLICATION_JSON)
	public String railNetworkForYears(@PathParam("authority") int authority, @QueryParam("year") int year) {

		Connection conn = null;
		try { 
			
			conn = source.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select COORD_STRING,OWNER from RAIL_GIS where GIS_YEAR = ? and AUTHORITY = ?");
			stmt.setInt(1, year);
			
			switch ( authority ) { 
				case 1:
					stmt.setString(2, "University of Nebraska");
					break;
				case 2:
					stmt.setString(2, "University of Portsmouth");
					break;
				default:
					stmt.setString(2, null);
			}
			
			ResultSet rs = stmt.executeQuery();
			StringBuffer sb = new StringBuffer("{\"type\":\"FeatureCollection\",\"features\":[");
			
			while ( rs.next() ) { 
				String[] coords = rs.getString(1).split(" ");
				sb.append("{\"type\":\"Feature\", \"class\":\"");
				sb.append("redColor");
				sb.append("\",\"geometry\":{\"coordinates\":[\n");
				for ( int i=0; i<coords.length; i++ ) {
					String coord = coords[i];
					if ( !(coord.trim().equals("")) ) {
						sb.append("[");
						sb.append(coord);
						sb.append("]");
						
						if ( i < coords.length-1 ) { 
							sb.append(",");
						}
					}
				}
				sb.append("], \"type\":\"LineString\"}},");
			}
			sb.replace(sb.length()-1, sb.length(), "");
			sb.append("]}");
			
			return sb.toString();
		} catch ( Exception e ) { 
			RuntimeException re = new RuntimeException("Could not generate the GIS");
			re.initCause(e);
			throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception e ) { LOG.log(Level.WARNING, "Could not close a connection", e); }
		}
	}
}

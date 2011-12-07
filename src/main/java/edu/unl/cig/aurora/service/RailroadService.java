/* Created on Jun 8, 2011 */
package edu.unl.cig.aurora.service;

import edu.unl.cig.aurora.model.gis.BoundingBox;
import edu.unl.cig.aurora.model.railroad.Railroad;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.List;
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
        @Path("/network/{B}/{year}/names.json")
        @Produces(MediaType.APPLICATION_JSON)
        public List<Railroad> railNetworkNames(@PathParam("B") String bounds, @PathParam("year") int year, @QueryParam("authority") int authority) { 
            ArrayList<Railroad> ret = new ArrayList<Railroad>();
            BoundingBox bbox = BoundingBox.boxFromPolyMapBounds(bounds);

            Connection conn = null;
            try {  
                    conn = source.getConnection();
                    String q = "select RAILROAD_NAME,sum(MILES) as len from RAIL_GIS where GIS_YEAR = ? and AUTHORITY = ? and MBRWithin(GEO, PolygonFromText('"+bbox.toSqlString()+"')) = 1 group by RAILROAD_NAME order by RAILROAD_NAME";
                    PreparedStatement stmt = conn.prepareStatement(q);
                    stmt.setInt(1, year);
                    stmt.setString(2,  (authority == 1 ) ? "University of Nebraska" : "University of Portsmouth");
                    
                    ResultSet rs = stmt.executeQuery();
                    while ( rs.next() ) { 
                        if ( !rs.getString(1).equals("NAME") && rs.getDouble(2) > 0.001 ) {
                         ret.add(new Railroad(rs.getDouble(2), rs.getString(1)));
                        }
                    }
                    
            } catch ( Exception e ) { 
                LOG.log(Level.SEVERE, "Could not query the network data", e);
                RuntimeException re = new RuntimeException("Error getting network data", e);
                throw re;
            } finally { 
                try {
                conn.close();
                } catch ( Exception e ) { 
                    LOG.log(Level.WARNING, "Could not close a connection", e);
                }
            }
            
            return ret;
        }
        
	@GET
	@Path("/network/{authority}")
	@Produces(MediaType.APPLICATION_JSON)
	public String railNetworkForYears(@PathParam("authority") int authority, @QueryParam("year") int year) {

		Connection conn = null;
		try { 
			
			conn = source.getConnection();
			PreparedStatement stmt = conn.prepareStatement("select COORD_STRING,RAILROAD_NAME,NOTES,MILES from RAIL_GIS where GIS_YEAR = ? and AUTHORITY = ?");
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
			StringBuilder sb = new StringBuilder("{\"type\":\"FeatureCollection\",\"features\":[");
			
			while ( rs.next() ) { 
				String[] coords = rs.getString(1).split(" ");
				sb.append("{\"type\":\"Feature\",\"geometry\":{\"coordinates\":[\n");
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
				sb.append("], \"type\":\"LineString\"},\"properties\": {");
                                sb.append("\"name\":\"");
                                sb.append(rs.getString(2));
                                sb.append("\",\"miles\":");
                                sb.append(rs.getFloat(4));
                                sb.append("}},");
			}
			sb.replace(sb.length()-1, sb.length(), "");
			sb.append("]}");
			return sb.toString();
		} catch ( Exception e ) { 
                    RuntimeException re = new RuntimeException("Could not generate the GIS", e);
                    throw re;
		} finally { 
			try { 
				conn.close();
			} catch ( Exception e ) { LOG.log(Level.WARNING, "Could not close a connection", e); }
		}
	}
}

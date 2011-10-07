/* Created on Oct 11, 2010 */
package edu.unl.cig.aurora;

import java.net.UnknownHostException;
import java.util.List;

import com.mongodb.DB;
import com.mongodb.Mongo;

/**
 * @author Ian Cottingham
 *
 * NOTE: this class should only be used when the Mongo instance is running locally.  
 *
 */
public class LocalMongoDriver {

	public static final String DATABASE_NAME = "DispatchXML";
	public static final String COUNT_COLLECTION = "placeCounts";
	
	private static final LocalMongoDriver _instance = new LocalMongoDriver();
	private final Mongo mongo;
	
	protected LocalMongoDriver() {
		try {
			mongo = new Mongo("localhost");
		} catch ( UnknownHostException uhe ) { 
			RuntimeException re = new RuntimeException("Could not connect to the local mongo instance");
			re.initCause(uhe);
			throw re;
		}
	}
	
	public static LocalMongoDriver getInstance() {		
		return _instance;
	}
	
	public DB getDatabase(String dbName) { 
		return mongo.getDB(dbName);
	}
	
	public List<String> getDatabaseList() { 
		return mongo.getDatabaseNames();
	}
}

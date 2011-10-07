/* Created on Aug 1, 2011 */
package edu.unl.cig.aurora.model.gis;

/**
 * @author Ian Cottingham 
 *
 */
public class Coordinate {


	public static final float DEGREE_MILE = 69.04f;
	public static final float DEGREE_FOOT = 364531.2f;

	public static final float FEET_PER_MILE = 5280f;
	public static final float EARTH_RADIUS_MILES = 3961.3f;

	private final double latitude;
	private final double longitude;

	private boolean marked;
	
	public static Coordinate coordFromKmlPoint(String coord) {
		String[] coords = coord.split(",");
		return new Coordinate(Float.parseFloat(coords[0]), Float.parseFloat(coords[1]));
	}
	
	public Coordinate(double latitude, double longitude) {
		this.latitude = latitude;
		this.longitude = longitude;
	}

	public Coordinate(String coord) { 
		String[] c = coord.split(",");
		this.latitude = Double.valueOf(c[1]);
		this.longitude = Double.valueOf(c[0]);
	}
	
	public double getLatitude() {
		return latitude;
	}

	public double getLongitude() {
		return longitude;
	}
	
	public void mark() { 
		this.marked = true;
	}
	
	public boolean isMarked() { 
		return this.marked;
	}

	/**
	 * 
	 * @param feet
	 *            the number of feet for the comparison
	 * @param coord
	 *            the coordinate to compare
	 * 
	 * @return true if this coordinate is within feet feet of coord false if not
	 */
	public boolean isWithin(double feet, Coordinate coord) {
		return ( coord.distanceFrom(this) * FEET_PER_MILE <= feet );
	}

	public double distanceFrom(Coordinate coord) {

		double lat1 = Math.toRadians(coord.getLatitude());
		double lon1 = Math.toRadians(coord.getLongitude());
		double lat2 = Math.toRadians(getLatitude());
		double lon2 = Math.toRadians(getLongitude());

		double dlon = (lon2 - lon1);
		double dlat = (lat2 - lat1);

		double a = (Math.sin(dlat / 2)) * (Math.sin(dlat / 2))
				+ (Math.cos(lat1) * Math.cos(lat2) * (Math.sin(dlon / 2)))
				* (Math.cos(lat1) * Math.cos(lat2) * (Math.sin(dlon / 2)));

		double c = 2 * Math.asin(Math.min(1.0, Math.sqrt(a)));
		double miles = EARTH_RADIUS_MILES * c;

		return miles;
	}

	@Override
	public String toString() {
		return ("(" + this.latitude + ", " + this.longitude + ")");
	}

	@Override
	public boolean equals(Object obj) {
		if (obj == this) {
			return true;
		} else if (obj instanceof Coordinate) {
			Coordinate comp = (Coordinate) obj;
			return (comp.getLatitude() == this.getLatitude() && comp
					.getLongitude() == this.getLongitude());
		} else {
			return false;
		}
	}

	@Override
	public int hashCode() {
		return this.toString().hashCode();
	}
}

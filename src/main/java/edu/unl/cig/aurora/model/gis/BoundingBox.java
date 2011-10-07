/* Created on Aug 1, 2011 */
package edu.unl.cig.aurora.model.gis;

import java.awt.geom.Rectangle2D;

/**
 * @author Ian Cottingham
 * 
 */
public class BoundingBox {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private double west;
	private double north;
	private double east;
	private double south;

	public BoundingBox() {
	}

	public BoundingBox(double north, double west, double south, double east) {
		this.west = west;
		this.north = north;
		this.east = east;
		this.south = south;
	}

	public double getEast() {
		return east;
	}

	public double getSouth() {
		return south;
	}

	public double getWest() {
		return west;
	}

	public double getNorth() {
		return north;
	}

	public void setEast(float east) {
		this.east = east;
	}

	public void setNorth(float north) {
		this.north = north;
	}

	public void setSouth(float south) {
		this.south = south;
	}

	public void setWest(float west) {
		this.west = west;
	}

	@Override
	public String toString() {
		return west + " " + south + " " + east + " " + north;
	}

	public String toSqlString() {
		StringBuffer poly_buffer = new StringBuffer("Polygon((");
		poly_buffer.append(this.getEast());
		poly_buffer.append(" ");
		poly_buffer.append(this.getNorth());
		poly_buffer.append(", ");
		poly_buffer.append(this.getEast());
		poly_buffer.append(" ");
		poly_buffer.append(this.getSouth());
		poly_buffer.append(", ");
		poly_buffer.append(this.getWest());
		poly_buffer.append(" ");
		poly_buffer.append(this.getSouth());
		poly_buffer.append(", ");
		poly_buffer.append(this.getWest());
		poly_buffer.append(" ");
		poly_buffer.append(this.getNorth());
		poly_buffer.append(", ");
		poly_buffer.append(this.getEast());
		poly_buffer.append(" ");
		poly_buffer.append(this.getNorth());
		poly_buffer.append("))");

		return poly_buffer.toString();
	}

	public boolean contains(BoundingBox box) {
		// Validate the parameter
		if (box == null)
			return false;
		BoundingBox targetBox = box;

		/*
		 * if the box north,east is less than or equal to this north, east and
		 * the box north,east is greater than this south,west then the upper
		 * right point is in this Box. if the targetBox south,west is greater
		 * than or equal to this south,west and the targetBox south,west is less
		 * than or requal to this north,east then the lower left point is in
		 * this Box. When both upper right point and lower left point are in
		 * this Box, this Box contain the box.
		 */
		if ((this.north >= targetBox.north && this.east >= targetBox.east)
				&& (targetBox.north >= this.south && targetBox.east >= this.west)
				&& (this.south <= targetBox.south && this.west <= targetBox.west)
				&& (targetBox.south <= this.north && targetBox.west <= this.east)) {
			return true;
		}

		return false;
	}

	public boolean validate() {
		if (this.north < this.south
				|| this.east < this.west
				|| (this.north == 0 && this.south == 0 && this.east == 0 && this.west == 0))
			return false;

		return true;
	}

	/**
	 * 
	 * This method is only safe for coordinates in the northern hemisphere. It
	 * DOES NOT use the Haversine formula to do this computation.
	 * 
	 * @param lat
	 *            - the latitude of the center point for the box
	 * @param lon
	 *            - the longitude of the center point for the box
	 * @param distance
	 *            - the distance in miles to extend the point
	 * @return
	 */
	public static BoundingBox boxFromPoint(float lat, float lon, float distance) {
		float extend_by = (distance / Coordinate.DEGREE_MILE) / 2;
		return new BoundingBox(lat + extend_by, lon - extend_by, lat
				- extend_by, lon + extend_by);
	}

	public static BoundingBox boxFromPolyMapBounds(String bounds) {
		String[] bound_arry = bounds.split(",");
		return new BoundingBox(Float.parseFloat(bound_arry[2]),
				Float.parseFloat(bound_arry[1]),
				Float.parseFloat(bound_arry[0]),
				Float.parseFloat(bound_arry[3]));
	}

	/*
	 * see http://mapki.com/wiki/Tile_utility_code_in_Javafor basis of
	 * implementation
	 */
	public static BoundingBox boxFromGoogleMapTiles(int x, int y, int zoom) {
		int tile_count = 1 << (17 - zoom);
		double width = 360.0 / tile_count;
		double west = -180 + (x * width);

		double height_merc = 1.0 / tile_count;
		double top_lat = y * height_merc;

		double south = Math.toDegrees((2 * Math.atan(Math.exp(Math.PI
				* (1 - (2 * (top_lat + height_merc))))))
				- (Math.PI / 2));

		double north = Math.toDegrees((2 * Math.atan(Math.exp(Math.PI
				* (1 - (2 * top_lat)))))
				- (Math.PI / 2));

		double east = west + width;
		System.out.println("NE="+north+","+east);
		System.out.println("SW="+south+", "+west);
		return new BoundingBox(north, west, south, east);
	}
}

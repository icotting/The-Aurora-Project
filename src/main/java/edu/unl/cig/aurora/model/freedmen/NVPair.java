/* Created on Jun 9, 2011 */
package edu.unl.cig.aurora.model.freedmen;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * @author Ian Cottingham 
 *
 */
@XmlRootElement
public class NVPair<T> {

	private String name;
	private int count;
	private float average;
	private T value;
	
	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public T getValue() {
		return value;
	}
	
	public void setValue(T value) {
		this.value = value;
	}
	
	public void increment() { count++; }

	public boolean equals(Object obj) { 
		if ( !(obj instanceof NVPair<?>) ) {
			return false;
		} else { 
			return ((NVPair<?>)obj).getName().equals(this.name);
		}
	}

	public float getAverage() {
		try { 
			if ( count == 0 ) { return -1; }
			
			return (Float)value / count;
		} catch ( ClassCastException cce ) { 
			return -1;
		} catch ( NullPointerException npe ) { 
			return -1;
		}
	}

	public void setAverage(float average) {
		this.average = average;
	}
	
	
}

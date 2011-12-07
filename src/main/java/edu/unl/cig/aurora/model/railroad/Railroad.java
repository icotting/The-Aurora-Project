package edu.unl.cig.aurora.model.railroad;

import javax.xml.bind.annotation.XmlRootElement;

/**
 *
 * @author icotting
 */
@XmlRootElement
public class Railroad {

    private double trackMiles;
    private String name;

    public Railroad() { } 
    
    public Railroad(double trackMiles, String name) { 
        this.trackMiles = trackMiles; 
        this.name = name;
    }
    
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public double getTrackMiles() {
        return trackMiles;
    }

    public void setTrackMiles(double trackMiles) {
        this.trackMiles = trackMiles;
    }
}

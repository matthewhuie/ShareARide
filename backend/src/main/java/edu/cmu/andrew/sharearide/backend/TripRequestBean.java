package edu.cmu.andrew.sharearide.backend;

/**
 * this class corresponds to the Trip_Request
 * table in the MySql instance in Google Cloud SQL
 */

public class TripRequestBean {

    private int tripId;

    private int requestId;

    private double actualDistance;

    public TripRequestBean(){

    }

    public TripRequestBean(int tripId, int requestId){
        this.requestId = requestId;
        this.tripId = tripId;
    }


    public double getActualDistance() {
        return actualDistance;
    }

    public void setActualDistance(double actualDistance) {
        this.actualDistance = actualDistance;
    }


    public int getTripId () {
        return tripId;
    }

    public void setTripId (int tripId) {
        this.tripId = tripId;
    }

    public int getRequestId () {
        return requestId;
    }

    public void setRequestId (int requestId) {
        this.requestId = requestId;
    }

}
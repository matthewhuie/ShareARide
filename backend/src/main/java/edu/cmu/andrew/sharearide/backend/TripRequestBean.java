package edu.cmu.andrew.sharearide.backend;

import java.util.ArrayList;

public class TripRequestBean {

    private int tripId;
    private ArrayList<RequestBean> requests = new ArrayList<>();
    private double actualDistance;

    public double getActualDistance() {
        return actualDistance;
    }

    public void setActualDistance(double actualDistance) {
        this.actualDistance = actualDistance;
    }

    public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public ArrayList<RequestBean> getRequests() {
        return requests;
    }

    public void setRequests(ArrayList<RequestBean> requests) {
        this.requests = requests;
    }
}
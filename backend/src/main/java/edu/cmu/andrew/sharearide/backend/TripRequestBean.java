package edu.cmu.andrew.sharearide.backend;

public class TripRequestBean {

  private int tripId;

  private int requestId;

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
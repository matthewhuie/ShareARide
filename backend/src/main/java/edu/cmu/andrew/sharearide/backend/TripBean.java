package edu.cmu.andrew.sharearide.backend;

public class TripBean {

    private int tripId;
    private int driverUserId;
    private int numOfRiders;
    private boolean isActive;
    private boolean hasEnded;

    public TripBean(){

    }

  public TripBean (int driverUserId) {
    this.driverUserId = driverUserId;
    numOfRiders = 0;
    isActive = true;
    hasEnded = false;
  }

  public int getTripId() {
        return tripId;
    }

    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    public int getDriverUserId() {
        return driverUserId;
    }

    public void setDriverUserId(int driverUserId) {
        this.driverUserId = driverUserId;
    }

    public int getNumOfRiders() {
        return numOfRiders;
    }

    public void setNumOfRiders(int numOfRiders) {
        this.numOfRiders = numOfRiders;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public boolean isHasEnded() {
        return hasEnded;
    }

    public void setHasEnded(boolean hasEnded) {
        this.hasEnded = hasEnded;
    }

}
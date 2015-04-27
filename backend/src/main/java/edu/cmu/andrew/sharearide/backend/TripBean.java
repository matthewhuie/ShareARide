package edu.cmu.andrew.sharearide.backend;

public class TripBean {

    private int tripId;
    private int driverUserId;
    private int numOfRiders;
    private int isActive;
    private int hasEnded;

    public TripBean(){

    }

  public TripBean (int driverUserId) {
    this.driverUserId = driverUserId;
    numOfRiders = 0;
    isActive = 1;
    hasEnded = 0;
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

  public int getIsActive () {
    return isActive;
  }

  public void setIsActive (int isActive) {
    this.isActive = isActive;
  }

  public int getHasEnded () {
    return hasEnded;
  }

  public void setHasEnded (int hasEnded) {
    this.hasEnded = hasEnded;
  }
}
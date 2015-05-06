package edu.cmu.andrew.sharearide.backend;

/**
 * this class corresponds to the trip
 * table in the MySql instance in Google Cloud SQL
 */

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

    /**
     * getter for the trip_id column in the trip bean
     * @return
     */

  public int getTripId() {
        return tripId;
    }

    /**
     * setter for the trip_id column in the trip bean
     * @param tripId
     */
    public void setTripId(int tripId) {
        this.tripId = tripId;
    }

    /**
     * getter for the driver_user_id column in the trip bean
     * @return
     */

    public int getDriverUserId() {
        return driverUserId;
    }

    /**
     * setter for the driver_user_id column in the trip bean
     * @param driverUserId
     */

    public void setDriverUserId(int driverUserId) {
        this.driverUserId = driverUserId;
    }

    /**
     * getter for the num_riders column in the trip bean
     * @return
     */

    public int getNumOfRiders() {
        return numOfRiders;
    }

    /**
     * setter for the driver_user_id column in the trip bean
     * @param numOfRiders
     */

    public void setNumOfRiders(int numOfRiders) {
        this.numOfRiders = numOfRiders;
    }

    /**
     * getter for the is_active column in the trip bean- indicates whether
     * the trip is still active or not
     * @return
     */

  public int getIsActive () {
    return isActive;
  }

    /**
     * setter for the is_active column in the trip bean
     * @param isActive
     */
  public void setIsActive (int isActive) {
    this.isActive = isActive;
  }

    /**
     * getter for the is_ended column
     * @return
     */
  public int getHasEnded () {
    return hasEnded;
  }

    /**
     * setter for the is_ended column
     * @param hasEnded
     */
  public void setHasEnded (int hasEnded) {
    this.hasEnded = hasEnded;
  }
}
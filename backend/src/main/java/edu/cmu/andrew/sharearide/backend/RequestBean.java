package edu.cmu.andrew.sharearide.backend;

/**
 * this class corresponds to the Request
 * table in the MySql instance in Google Cloud SQL
 */

public class RequestBean {

    private int requestId;
    private int passUserId;
    private double srcLongitude;
    private double srcLatitude;
    private double dstLongitude;
    private double dstLatitude;
    private double fare;
    private float passRating;
    private float driverRating;
    private String startTime;
    private String endTime;
    private int isServed;
    private double distanceEstimated;
    private int numOfRiders;
    private double estimatedTime;
    private double actualDistance;
    private double actualDuration;
    private double estimatedFare;

    public RequestBean(){

    }

    /**
     * bean construtor which constructs the request object
     * with the values provided
     * @param passUserId
     * @param srcLongitude
     * @param srcLatitude
     * @param dstLongitude
     * @param dstLatitude
     * @param riders
     */
    public RequestBean(int passUserId, double srcLongitude, double srcLatitude, double dstLongitude, double dstLatitude,int riders) {
        this.passUserId = passUserId;
        this.srcLongitude = srcLongitude;
        this.srcLatitude = srcLatitude;
        this.dstLongitude = dstLongitude;
        this.dstLatitude = dstLatitude;
        this.numOfRiders = riders;
        this.isServed = 0;
    }

    /**
     * gets the number of riders for the request
     * @return
     */

    public int getNumOfRiders() {
        return numOfRiders;
    }

    /**
     * sets the number of riders for the request
     * @param numOfRiders
     */

    public void setNumOfRiders(int numOfRiders) {
        this.numOfRiders = numOfRiders;
    }

    /**
     * gets the estimated time to fulfill the request
     * @return
     */

    public double getEstimatedTime() {
        return estimatedTime;
    }

    /**
     * sets the estimated time to fulfill the request
     * @param estimatedTime
     */
    public void setEstimatedTime(double estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    /**
     * gets the unique id of  the request
     * @return
     */
    public int getRequestId() {
        return requestId;
    }

    /**
     * sets the unique id of  the request
     * @param requestId
     */

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    /**
     * gets the unique id of passenger user requesting  the ride
     * @return
     */

    public int getPassUserId() {
        return passUserId;
    }

    /**
     * sets the unique id of passenger user requesting  the ride
     * @param passUserId
     */

    public void setPassUserId(int passUserId) {
        this.passUserId = passUserId;
    }

    /**
     * gets the pick up longitude of the request
     * @return
     */

    public double getSrcLongitude() {
        return srcLongitude;
    }

    /**
     * sets the pick up longitude of the request
     * @param srcLongitude
     */

    public void setSrcLongitude(double srcLongitude) {
        this.srcLongitude = srcLongitude;
    }

    /**
     * gets the pick up latitude of the request
     * @return
     */

    public double getSrcLatitude() {
        return srcLatitude;
    }

    /**
     * sets the pick up latitude of the request
     * @param srcLatitude
     */

    public void setSrcLatitude(double srcLatitude) {
        this.srcLatitude = srcLatitude;
    }

    /**
     * gets the destination longitude of the request
     * @return
     */

    public double getDstLongitude() {
        return dstLongitude;
    }

    /**
     * sets the destination longitude of the request
     * @param dstLongitude
     */

    public void setDstLongitude(double dstLongitude) {
        this.dstLongitude = dstLongitude;
    }

    /**
     * gets the destination latitude of the request
     * @return
     */

    public double getDstLatitude() {
        return dstLatitude;
    }

    /**
     * sets the destination latitude of the request
     * @param dstLatitude
     */

    public void setDstLatitude(double dstLatitude) {
        this.dstLatitude = dstLatitude;
    }

    /**
     * gets the actual fare for the request
     * @return
     */

    public double getFare() {
        return fare;
    }

    /**
     * sets the actual fare for the request
     * @param fare
     */

    public void setFare(double fare) {
        this.fare = fare;
    }

    /**
     * gets the passenger rating for the request
     * @return
     */

    public float getPassRating() {
        return passRating;
    }

    /**
     * sets the passenger rating for the request
     * @param passRating
     */

    public void setPassRating(float passRating) {
        this.passRating = passRating;
    }

    /**
     * gets the driver rating for the request
     * @return
     */

    public float getDriverRating() {
        return driverRating;
    }

    /**
     * sets the driver rating for the request
     * @param driverRating
     */

    public void setDriverRating(float driverRating) {
        this.driverRating = driverRating;
    }

    /**
     * gets the start time in milliseconds for the request
     * @return
     */

    public String getStartTime() {
        return startTime;
    }

    /**
     * sets the start time in milliseconds for the request
     * @param startTime
     */

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    /**
     * gets the end time in milliseconds for the request
     * @return
     */

    public String getEndTime() {
        return endTime;
    }

    /**
     * sets the end time in milliseconds for the request
     * @param endTime
     */
    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    /**
     * gets the value whether the request is served or not
     * @return
     */
    public int isServed() {
        return isServed;
    }

    /**
     * sets the value whether the request is served or not
     * @param isServed
     */
    public void setServed(int isServed) {
        this.isServed = isServed;
    }

    /**
     * gets the estimated distance for the request
     * @return
     */

  public double getDistanceEstimated () {
    return distanceEstimated;
  }

    /**
     * sets the estimated distance for the request
     * @param distanceEstimated
     */
  public void setDistanceEstimated (double distanceEstimated) {
    this.distanceEstimated = distanceEstimated;
  }

    /**
     * gets the actual distance travelled for the request
     * @return
     */

  public double getActualDistance () {
    return actualDistance;
  }

    /**
     * sets the actual distance travelled for the request
     * @param actualDistance
     */

  public void setActualDistance (double actualDistance) {
    this.actualDistance = actualDistance;
  }

    /**
     * gets the actual duration of the request
     * @return
     */

  public double getActualDuration () {
    return actualDuration;
  }

    /**
     * sets the actual duration of the request
     * @param actualDuration
     */
  public void setActualDuration (double actualDuration) {
    this.actualDuration = actualDuration;
  }

    /**
     * gets the estimated fare of the request
     * @return
     */

    public double getEstimatedFare() {
        return estimatedFare;
    }

    /**
     * sets estimated fare of the request
     * @param estimatedFare
     */
    public void setEstimatedFare(double estimatedFare) {
        this.estimatedFare = estimatedFare;
    }


}
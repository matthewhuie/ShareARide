package edu.cmu.andrew.sharearide.backend;


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

    public double getEstimatedFare() {
        return estimatedFare;
    }

    public void setEstimatedFare(double estimatedFare) {
        this.estimatedFare = estimatedFare;
    }

    private double estimatedFare;

    public RequestBean(){

    }

    public RequestBean(int passUserId, int isServed){
        this.passUserId = passUserId;
        this.isServed = isServed;
    }

    public RequestBean(int requestId,double fare,float passRating){
        this.requestId = requestId;
        this.fare = fare;
        this.passRating = passRating;
    }

    public RequestBean(int requestId,float driverRating){
        this.requestId = requestId;
        //this.fare = fare;
        this.driverRating = driverRating;
        this.isServed = 0;
    }

    public RequestBean(int passUserId, double srcLongitude, double srcLatitude, double dstLongitude, double dstLatitude,int riders) {
        this.passUserId = passUserId;
        this.srcLongitude = srcLongitude;
        this.srcLatitude = srcLatitude;
        this.dstLongitude = dstLongitude;
        this.dstLatitude = dstLatitude;
        this.numOfRiders = riders;
        this.isServed = 0;
    }

    public int getNumOfRiders() {
        return numOfRiders;
    }

    public void setNumOfRiders(int numOfRiders) {
        this.numOfRiders = numOfRiders;
    }


    public double getEstimatedTime() {
        return estimatedTime;
    }

    public void setEstimatedTime(double estimatedTime) {
        this.estimatedTime = estimatedTime;
    }

    public int getRequestId() {
        return requestId;
    }

    public void setRequestId(int requestId) {
        this.requestId = requestId;
    }

    public int getPassUserId() {
        return passUserId;
    }

    public void setPassUserId(int passUserId) {
        this.passUserId = passUserId;
    }

    public double getSrcLongitude() {
        return srcLongitude;
    }

    public void setSrcLongitude(double srcLongitude) {
        this.srcLongitude = srcLongitude;
    }

    public double getSrcLatitude() {
        return srcLatitude;
    }

    public void setSrcLatitude(double srcLatitude) {
        this.srcLatitude = srcLatitude;
    }

    public double getDstLongitude() {
        return dstLongitude;
    }

    public void setDstLongitude(double dstLongitude) {
        this.dstLongitude = dstLongitude;
    }

    public double getDstLatitude() {
        return dstLatitude;
    }

    public void setDstLatitude(double dstLatitude) {
        this.dstLatitude = dstLatitude;
    }

    public double getFare() {
        return fare;
    }

    public void setFare(double fare) {
        this.fare = fare;
    }

    public float getPassRating() {
        return passRating;
    }

    public void setPassRating(float passRating) {
        this.passRating = passRating;
    }

    public float getDriverRating() {
        return driverRating;
    }

    public void setDriverRating(float driverRating) {
        this.driverRating = driverRating;
    }

    public String getStartTime() {
        return startTime;
    }

    public void setStartTime(String startTime) {
        this.startTime = startTime;
    }

    public String getEndTime() {
        return endTime;
    }

    public void setEndTime(String endTime) {
        this.endTime = endTime;
    }

    public int isServed() {
        return isServed;
    }

    public void setServed(int isServed) {
        this.isServed = isServed;
    }

  public double getDistanceEstimated () {
    return distanceEstimated;
  }

  public void setDistanceEstimated (double distanceEstimated) {
    this.distanceEstimated = distanceEstimated;
  }

  public double getActualDistance () {
    return actualDistance;
  }

  public void setActualDistance (double actualDistance) {
    this.actualDistance = actualDistance;
  }
}
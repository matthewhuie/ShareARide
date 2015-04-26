package edu.cmu.andrew.sharearide.backend;

import java.sql.Timestamp;

public class RequestBean {

    private int requestId;
    private int passUserId;
    private double srcLongitude;
    private double srcLatitude;
    private double dstLongitude;
    private double dstLatitude;
    private double fare;
    private Timestamp latestTime;
    private float passRating;
    private float driverRating;
    private Timestamp startTime;
    private Timestamp endTime;
    private boolean isServed;
    private double distanceEstimated;
    private double actualDistance;

    public RequestBean(){

    }

    public RequestBean(int passUserId, boolean isServed){
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
        this.isServed = false;
    }

    public RequestBean(int passUserId, double srcLongitude, double srcLatitude, double dstLongitude, double dstLatitude) {
        this.passUserId = passUserId;
        this.srcLongitude = srcLongitude;
        this.srcLatitude = srcLatitude;
        this.dstLongitude = dstLongitude;
        this.dstLatitude = dstLatitude;
        //this.startTime = startTime;
        this.isServed = false;
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

    public Timestamp getLatestTime() {
        return latestTime;
    }

    public void setLatestTime(Timestamp latestTime) {
        this.latestTime = latestTime;
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

    public Timestamp getStartTime() {
        return startTime;
    }

    public void setStartTime(Timestamp startTime) {
        this.startTime = startTime;
    }

    public Timestamp getEndTime() {
        return endTime;
    }

    public void setEndTime(Timestamp endTime) {
        this.endTime = endTime;
    }

    public boolean isServed() {
        return isServed;
    }

    public void setServed(boolean isServed) {
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
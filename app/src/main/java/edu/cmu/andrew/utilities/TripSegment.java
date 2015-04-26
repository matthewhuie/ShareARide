package edu.cmu.andrew.utilities;

/**
 * Created by matthewhuie on 15-04-26.
 */
public class TripSegment {

  private double sLatitude;
  private double sLongitude;
  private double dLatitude;
  private double dLongitude;
  private int distance;
  private int duration;
  private int[] passengers;

  public double getsLatitude () {
    return sLatitude;
  }

  public void setsLatitude (double sLatitude) {
    this.sLatitude = sLatitude;
  }

  public double getsLongitude () {
    return sLongitude;
  }

  public void setsLongitude (double sLongitude) {
    this.sLongitude = sLongitude;
  }

  public double getdLatitude () {
    return dLatitude;
  }

  public void setdLatitude (double dLatitude) {
    this.dLatitude = dLatitude;
  }

  public double getdLongitude () {
    return dLongitude;
  }

  public void setdLongitude (double dLongitude) {
    this.dLongitude = dLongitude;
  }

  public int getDistance () {
    return distance;
  }

  public void setDistance (int distance) {
    this.distance = distance;
  }

  public int getDuration () {
    return duration;
  }

  public void setDuration (int duration) {
    this.duration = duration;
  }

  public int[] getPassengers () {
    return passengers;
  }

  public void setPassengers (int[] passengers) {
    this.passengers = passengers;
  }
}

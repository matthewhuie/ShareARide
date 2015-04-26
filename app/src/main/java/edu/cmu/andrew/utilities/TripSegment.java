package edu.cmu.andrew.utilities;

import com.google.android.gms.maps.model.LatLng;

/**
 * Created by matthewhuie on 15-04-26.
 */
public class TripSegment {

  private int segmentID;
  private LatLng source;
  private LatLng destination;
  private int distance;
  private int duration;
  private int[] passengers;
  private boolean isCompleted;

  public TripSegment (int segmentID, LatLng source, LatLng destination, int distance, int duration, int[] passengers, boolean isCompleted) {
    this.segmentID = segmentID;
    this.source = source;
    this.destination = destination;
    this.distance = distance;
    this.duration = duration;
    this.passengers = passengers;
    this.isCompleted = isCompleted;
  }

  public int getSegmentID () {
    return segmentID;
  }

  public void setSegmentID (int segmentID) {
    this.segmentID = segmentID;
  }

  public LatLng getSource () {
    return source;
  }

  public void setSource (LatLng source) {
    this.source = source;
  }

  public LatLng getDestination () {
    return destination;
  }

  public void setDestination (LatLng destination) {
    this.destination = destination;
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

  public boolean isCompleted () {
    return isCompleted;
  }

  public void setCompleted (boolean isCompleted) {
    this.isCompleted = isCompleted;
  }
}

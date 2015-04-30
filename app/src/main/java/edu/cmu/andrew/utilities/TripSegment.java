package edu.cmu.andrew.utilities;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * Created by matthewhuie on 15-04-26.
 */
public class TripSegment {

  private int segmentID;
  private LatLng source;
  private LatLng destination;
  private double distance;
  private double duration;
  private List<Integer> requests;
  private boolean isCompleted;

  public TripSegment (int segmentID, LatLng source, LatLng destination, double distance, double duration, List<Integer> requests) {
    this.segmentID = segmentID;
    this.source = source;
    this.destination = destination;
    this.distance = distance;
    this.duration = duration;
    this.requests = requests;
    isCompleted = false;
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

  public double getDistance () {
    return distance;
  }

  public void setDistance (double distance) {
    this.distance = distance;
  }

  public double getDuration () {
    return duration;
  }

  public void setDuration (double duration) {
    this.duration = duration;
  }

  public List<Integer> getRequests () {
    return requests;
  }

  public void setRequests (List<Integer> requests) {
    this.requests = requests;
  }

  public boolean isCompleted () {
    return isCompleted;
  }

  public void setCompleted (boolean isCompleted) {
    this.isCompleted = isCompleted;
  }
}

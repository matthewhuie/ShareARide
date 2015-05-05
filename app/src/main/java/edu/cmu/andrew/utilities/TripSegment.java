package edu.cmu.andrew.utilities;

import com.google.android.gms.maps.model.LatLng;

import java.util.List;

/**
 * TripSegment is a helper class that represents each individual trip segment.
 */
public class TripSegment {

  /**
   * This segment's unique ID
   */
  private int segmentID;

  /**
   * This segment's source location
   */
  private LatLng source;

  /**
   * This segment's destination location
   */
  private LatLng destination;

  /**
   * This segment's accumulated distance
   */
  private double distance;

  /**
   * This segment's total duration
   */
  private double duration;

  /**
   * A list of requests that are part of this segment
   */
  private List<Integer> requests;

  /**
   * Signifies whether this segment has been completed
   */
  private boolean isCompleted;

  /**
   * Creates a new incomplete TripSegment
   *
   * @param segmentID   the segment ID
   * @param source      the source location
   * @param destination the destination location
   * @param distance    the accumulated distance
   * @param duration    the total duration
   * @param requests    the list of requests that are part of this segment
   */
  public TripSegment (int segmentID, LatLng source, LatLng destination, double distance, double duration, List<Integer> requests) {
    this.segmentID = segmentID;
    this.source = source;
    this.destination = destination;
    this.distance = distance;
    this.duration = duration;
    this.requests = requests;
    isCompleted = false;
  }

  /**
   * Gets the destination location
   *
   * @return the destination location
   */
  public LatLng getDestination () {
    return destination;
  }

  /**
   * Sets the destination location
   *
   * @param destination the destination location
   */
  public void setDestination (LatLng destination) {
    this.destination = destination;
  }

  /**
   * Gets the accumulated distance
   *
   * @return the accumulated distance
   */
  public double getDistance () {
    return distance;
  }

  /**
   * Gets the total duration
   *
   * @return the total duration
   */
  public double getDuration () {
    return duration;
  }

  /**
   * Gets the list of requests on this trip segment
   *
   * @return the list of requests on this trip segment
   */
  public List<Integer> getRequests () {
    return requests;
  }

  /**
   * Sets whether this trip segment has completed
   *
   * @param isCompleted whether this trip segment has completed
   */
  public void setCompleted (boolean isCompleted) {
    this.isCompleted = isCompleted;
  }
}

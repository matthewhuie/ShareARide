package edu.cmu.andrew.sharearide;

import android.app.Fragment;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.andrew.utilities.GPSTracker;

/**
 * SARActivity is the main activity of the Share-A-Ride application.
 * It is implemented as a FragmentActivity and handles the many different fragments of the application.
 */
public class SARActivity extends FragmentActivity {

  /**
   * The base URL of all the APIs used
   */
  public final String GEOCODE_BASE_URL = "https://maps.googleapis.com/maps/api/geocode/xml?address=";
  public final String REV_GEOCODE_BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";
  public final String UBER_PRICE_BASE_URL = "https://api.uber.com/v1/estimates/price?";
  public final String DIRECTION_BASE_URL = "https://maps.googleapis.com/maps/api/directions/json?";
  public final String GOOGLE_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/";

  /**
   * The conversion units used against Google Directions API
   */
  public final double MeterToMile = 0.000621371;
  public final double SecToMin = 60;

  /**
   * The number of riders and mood type of the request
   */
  public int numOfRiders;
  public int moodType = 0;

  /**
   * The list of fragments and position of fragments within this instance
   */
  private List<Fragment> fragments;
  private int position;

  /**
   * The GPSTracker object for this user
   */
  private GPSTracker mGPS;

  /**
   * This user's destination
   */
  private String destination;

  /**
   * This user's username
   */
  private String username;

  /**
   * This user's user ID
   */
  private int userID;

  /**
   * This Activity's onCreate method
   *
   * @param savedInstanceState the saved instance state
   */
  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate (savedInstanceState);
    setContentView (R.layout.activity_passenger);

    /** Creates a location tracker for this user */
    mGPS = new GPSTracker (this);

    /** Initiates the fragments for this user */
    initFragments ();
  }

  /**
   * Sets the current fragment to the fragment in the specified position
   *
   * @param position the fragment to switch to
   */
  private void setFragment (int position) {
    getFragmentManager ().beginTransaction ()
        .replace (R.id.fragmentLayout, fragments.get (position))
        .addToBackStack (null)
        .commit ();
  }

  /**
   * Initializes this user with the LoginFragment
   */
  private void initFragments () {
    position = 0;
    fragments = new ArrayList<> ();
    fragments.add (new LoginFragment ());
    setFragment (position);
  }

  /**
   * Sets the fragments up for a passenger
   */
  public void initPassenger () {
    fragments.add (new PassengerInputFragment ());
    fragments.add (new PassengerMapFragment ());
  }

  /**
   * Sets the fragments up for a driver
   */
  public void initDriver () {
    fragments.add (new DriverMapFragment ());
  }

  /**
   * Geocodes a given latitude and longitude to a location name
   *
   * @param latitude  a given latitude
   * @param longitude a given longitude
   * @return the string notation of the location
   */
  public String geocode (double latitude, double longitude) {
    Geocoder geoCoder = new Geocoder (this);
    List<Address> places = null;
    try {
      places = geoCoder.getFromLocation (latitude, longitude, 1);
    } catch (IOException ioe) {
    }
    return (places.isEmpty () ? null : places.get (0).getAddressLine (0));
  }

  /**
   * Switches to the next fragment of this activity
   */
  public void nextFragment () {
    setFragment (++position);
  }

  /**
   * Switches back to the previous fragment of this activity, or re-initializes if back to start
   */
  public void previousFragment () {
    if (position > 1) {
      setFragment (--position);
    } else {
      initFragments ();
    }
  }

  /**
   * Gets the latitude of this user
   *
   * @return the latitude of this user
   */
  public double getLatitude () {
    return mGPS.getLatitude ();
  }

  /**
   * Gets the longitude of this user
   *
   * @return the longitude of this user
   */
  public double getLongitude () {
    return mGPS.getLongitude ();
  }

  /**
   * Gets the location name of this user
   *
   * @return the location name of this user
   */
  public String getLocationName () {
    return geocode (getLatitude (), getLongitude ());
  }

  /**
   * Gets the destination of this user
   *
   * @return the destination of this user
   */
  public String getDestination () {
    return destination;
  }

  /**
   * Sets the destination of this user
   *
   * @param destination the destination of this user
   */
  public void setDestination (String destination) {
    this.destination = destination;
  }

  /**
   * Gets the number of riders of this user
   *
   * @return the number of riders of this user
   */
  public int getNumOfRiders () {
    return numOfRiders;
  }

  /**
   * Sets the number of riders of this user
   *
   * @param numOfRiders the number of riders of this user
   */
  public void setNumOfRiders (int numOfRiders) {
    this.numOfRiders = numOfRiders;
  }

  /**
   * Gets the username of this user
   *
   * @return the username of this user
   */
  public String getUsername () {
    return username;
  }

  /**
   * Sets the username of this user
   *
   * @param username the username of this user
   */
  public void setUsername (String username) {
    this.username = username;
  }

  /**
   * Gets the user ID of this user
   *
   * @return the user ID of this user
   */
  public int getUserID () {
    return userID;
  }

  /**
   * Sets the user ID of this user
   *
   * @param userID
   */
  public void setUserID (int userID) {
    this.userID = userID;
  }

  /**
   * This Activity's onBackPressed method will go back to the previous fragment
   */
  @Override
  public void onBackPressed () {
    if (position != 0) {
      previousFragment ();
    } else {
      super.onBackPressed ();
    }
  }

  /**
   * Gets the remote JSON object
   *
   * @param url the remote URL
   * @return the remote JSON object as a String
   */
  public String getRemoteJSON (String url) {
    String json = null;
    try {
      HttpClient httpClient = new DefaultHttpClient ();
      HttpGet priceRequest = new HttpGet (url);
      HttpResponse httpResult = httpClient.execute (priceRequest);
      json = EntityUtils.toString (httpResult.getEntity (), "UTF-8");
    } catch (MalformedURLException e) {
      Log.i ("MalformedURLerror: ", e.toString ());
    } catch (IOException ioe) {
      Log.i ("IOerror: ", ioe.toString ());
    }

    return json;
  }
}

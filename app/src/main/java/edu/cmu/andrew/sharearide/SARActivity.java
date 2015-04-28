package edu.cmu.andrew.sharearide;

import android.app.Fragment;
import android.location.Address;
import android.location.Geocoder;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
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

import edu.cmu.andrew.sharearide.backend.shareARideApi.model.MessageBean;
import edu.cmu.andrew.utilities.EndPointManager;
import edu.cmu.andrew.utilities.GPSTracker;

public class SARActivity extends FragmentActivity {

  private List<Fragment> fragments;
  private int position;
  private GPSTracker mGPS;
  private String destination;
  private String username;
  private int userID;
  public int numOfRiders;

  public final String GEOCODE_BASE_URL = "https://maps.googleapis.com/maps/api/geocode/xml?address=";
  public final String REV_GEOCODE_BASE_URL = "https://maps.googleapis.com/maps/api/geocode/json?latlng=";
  public final String UBER_PRICE_BASE_URL = "https://api.uber.com/v1/estimates/price?";
  public final String DIRECTION_BASE_URL = "https://maps.googleapis.com/maps/api/directions/json?";
  public final String GOOGLE_AUTOCOMPLETE_URL = "https://maps.googleapis.com/maps/api/place/autocomplete/";
  public final double MeterToMile = 0.000621371;
  public final double SecToMin = 60;


  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate (savedInstanceState);
    setContentView (R.layout.activity_passenger);

    mGPS = new GPSTracker (this);

    initFragments ();
  }

  private void setFragment (int position) {
    getFragmentManager ().beginTransaction ()
        .replace (R.id.fragmentLayout, fragments.get (position))
        .addToBackStack (null)
        .commit ();
  }

  private void initFragments () {
    position = 0;
    fragments = new ArrayList<> ();
    fragments.add (new LoginFragment ());
    setFragment (position);
  }

  public void initPassenger () {
    fragments.add (new PassengerInputFragment ());
    fragments.add (new PassengerMapFragment ());
  }

  public void initDriver () {
    fragments.add (new DriverMapFragment ());
  }

  public String geocode (double latitude, double longitude) {
    Geocoder geoCoder = new Geocoder (this);
    List<Address> places = null;
    try {
      places = geoCoder.getFromLocation (latitude, longitude, 1);
    } catch (IOException ioe) {
    }
    return (places.isEmpty () ? null : places.get (0).getAddressLine (0));
  }

  public void nextFragment () {
    setFragment (++position);
  }

  public void previousFragment () {
    if (position > 1) {
      setFragment (--position);
    } else {
      initFragments ();
    }
  }

  public double getLatitude () {
    return mGPS.getLatitude ();
  }

  public double getLongitude () {
    return mGPS.getLongitude ();
  }

  public String getLocationName () {
    return geocode (getLatitude (), getLongitude ());
  }

  public void setDestination (String destination) {
    this.destination = destination;
  }

  public String getDestination () {
    return destination;
  }

    public void setNumOfRiders (int numOfRiders) {
        this.numOfRiders = numOfRiders;
    }

    public int getNumOfRiders () {
        return numOfRiders;
    }

  public String getUsername () {
    return username;
  }

  public void setUsername (String username) {
    this.username = username;
  }

  public int getUserID () {
    return userID;
  }

  public void setUserID (int userID) {
    this.userID = userID;
  }

  @Override
  public void onBackPressed () {
    if (position != 0) {
      previousFragment ();
    } else {
      super.onBackPressed ();
    }
  }


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

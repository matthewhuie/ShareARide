package edu.cmu.andrew.sharearide;

import android.app.Fragment;
import android.location.Address;
import android.location.Geocoder;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.api.GoogleApiClient;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.andrew.utilities.GPSTracker;

public class SARActivity extends FragmentActivity {

  private GoogleApiClient mGoogleApiClient;
  private String locationName;
  private List<Fragment> fragments;
  private int position;
  private GPSTracker mGPS;
  private String destination;

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate (savedInstanceState);
    setContentView (R.layout.activity_passenger);

    mGPS = new GPSTracker (this);

    position = 0;
    fragments = new ArrayList<> ();
    fragments.add (new LoginFragment ());

    setFragment (position);
  }

  private void setFragment (int position) {
    getFragmentManager ().beginTransaction ()
        .replace (R.id.fragmentLayout, fragments.get (position))
        .addToBackStack (null)
        .commit ();
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
    setFragment (--position);
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

  @Override
  public void onBackPressed () {
    previousFragment ();
  }
}

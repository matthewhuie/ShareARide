package edu.cmu.andrew.sharearide;

import android.app.Fragment;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.andrew.utilities.GPSTracker;

public class SARActivity extends FragmentActivity {
    //implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

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
    fragments.add (new PassengerInputFragment ());
    fragments.add (new PassengerMapFragment ());

    setFragment (position);
  }

  private void setFragment (int position) {
    getFragmentManager ().beginTransaction ()
        .replace (R.id.fragmentLayout, fragments.get (position))
        .addToBackStack (null)
        .commit ();
  }

  private String geocode (double latitude, double longitude) {
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

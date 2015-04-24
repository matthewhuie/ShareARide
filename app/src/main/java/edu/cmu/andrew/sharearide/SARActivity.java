package edu.cmu.andrew.sharearide;

import android.app.Fragment;
import android.app.FragmentTransaction;
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

public class SARActivity extends FragmentActivity
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  private GoogleApiClient mGoogleApiClient;
  private Location mLastLocation;
  private double latitude;
  private double longitude;
  private String locationName;
  private List<Fragment> pFragments;
  private LoginFragment lf;
  private PassengerInputFragment pif;

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate (savedInstanceState);
    setContentView (R.layout.activity_passenger);

    buildGoogleApiClient ();

    pFragments = new ArrayList<> ();
    pFragments.add (new LoginFragment ());
    pFragments.add (new PassengerInputFragment ());
    pFragments.add (new PassengerMapFragment ());

    setFragment (pFragments.get (0));
  }

  public void setFragment (Fragment fragment) {
    getFragmentManager ().beginTransaction ()
        .replace (R.id.fragmentLayout, fragment)
        .addToBackStack ("")
        .commit ();
  }

  public void nextPFragment () {
    int position = pFragments.indexOf (getFragmentManager ().findFragmentById (R.id.fragmentLayout));
    setFragment (pFragments.get (position + 1));
  }

  protected synchronized void buildGoogleApiClient () {
    mGoogleApiClient = new GoogleApiClient.Builder (this)
        .addConnectionCallbacks (this)
        .addOnConnectionFailedListener (this)
        .addApi (LocationServices.API)
        .build ();
  }

  @Override
  public void onConnectionSuspended (int cause) {
    // The connection has been interrupted.
    // Disable any UI components that depend on Google APIs
    // until onConnected() is called.
  }

  @Override
  public void onConnectionFailed (ConnectionResult result) {
    // This callback is important for handling errors that
    // may occur while attempting to connect with Google.
    //
    // More about this in the next section.
  }

  @Override
  public void onConnected (Bundle connectionHint) {
    mLastLocation = LocationServices.FusedLocationApi.getLastLocation (
        mGoogleApiClient);
    if (mLastLocation != null) {
      latitude = mLastLocation.getLatitude ();
      longitude = mLastLocation.getLongitude ();

      // Gets the current location place name
      Geocoder geoCoder = new Geocoder (this);
      List<Address> places = null;
      try {
        places = geoCoder.getFromLocation (latitude, longitude, 1);
      } catch (IOException ioe) {
      }
      locationName = (places.isEmpty () ? null : places.get (0).getAddressLine (0));
      //pif.setLocation (locationName);
    }
  }

  public double getLatitude () {
    return latitude;
  }

  public double getLongitude () {
    return longitude;
  }

  @Override
  public void onStart () {
    super.onStart ();
    mGoogleApiClient.connect ();
  }

  @Override
  public void onStop () {
    mGoogleApiClient.disconnect ();
    super.onStop ();
  }
}

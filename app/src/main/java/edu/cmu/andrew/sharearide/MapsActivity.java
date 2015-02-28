package edu.cmu.andrew.sharearide;

import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import java.io.IOException;
import java.util.List;

public class MapsActivity extends FragmentActivity
    implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

  private final boolean DEBUG = true;

  private GoogleMap mMap; // Might be null if Google Play services APK is not available.
  private GoogleApiClient mGoogleApiClient;
  private Location mLastLocation;
  private double latitude;
  private double longitude;

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate (savedInstanceState);
    setContentView (R.layout.activity_maps);
    buildGoogleApiClient ();
    setUpMapIfNeeded ();
  }

  @Override
  protected void onResume () {
    super.onResume ();
    setUpMapIfNeeded ();
  }

  @Override
  protected void onStart() {
    super.onStart();
    mGoogleApiClient.connect();
  }

  @Override
  protected void onStop() {
    mGoogleApiClient.disconnect();
    super.onStop();
  }

  /**
   * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
   * installed) and the map has not already been instantiated.. This will ensure that we only ever
   * call {@link #setUpMap()} once when {@link #mMap} is not null.
   * <p/>
   * If it isn't installed {@link SupportMapFragment} (and
   * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
   * install/update the Google Play services APK on their device.
   * <p/>
   * A user can return to this FragmentActivity after following the prompt and correctly
   * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
   * have been completely destroyed during this process (it is likely that it would only be
   * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
   * method in {@link #onResume()} to guarantee that it will be called.
   */
  private void setUpMapIfNeeded () {
    // Do a null check to confirm that we have not already instantiated the map.
    if (mMap == null) {
      // Try to obtain the map from the SupportMapFragment.
      mMap = ((SupportMapFragment) getSupportFragmentManager ().findFragmentById (R.id.map))
          .getMap ();
      // Check if we were successful in obtaining the map.
      if (mMap != null) {
        setUpMap ();
      }
    }
  }

  /**
   * This is where we can add markers or lines, add listeners or move the camera. In this case, we
   * just add a marker near Africa.
   * <p/>
   * This should only be called once and when we are sure that {@link #mMap} is not null.
   */
  private void setUpMap () {
    mMap.setMyLocationEnabled (true);
  }

  protected synchronized void buildGoogleApiClient() {
    mGoogleApiClient = new GoogleApiClient.Builder(this)
        .addConnectionCallbacks(this)
        .addOnConnectionFailedListener(this)
        .addApi(LocationServices.API)
        .build();
  }

  @Override
  public void onConnected (Bundle connectionHint) {
    mLastLocation = LocationServices.FusedLocationApi.getLastLocation(
        mGoogleApiClient);
    if (mLastLocation != null) {
      latitude = mLastLocation.getLatitude ();
      longitude = mLastLocation.getLongitude();

      // Gets the current location place name
      Geocoder geoCoder = new Geocoder (this);
      List <Address> places = null;
      try {
        places = geoCoder.getFromLocation (latitude, longitude, 1);
      } catch (IOException ioe) { }
      String thisPlace = (places.isEmpty() ? null : places.get (0).getAddressLine (0));

      // Prints current location to TextView
      ((TextView) findViewById (R.id.my_location)).setText ("Current location: " + thisPlace);
      mMap.moveCamera(CameraUpdateFactory.newLatLngZoom (new LatLng (latitude, longitude), 13));
    }
  }

  @Override
  public void onConnectionSuspended(int cause) {
    // The connection has been interrupted.
    // Disable any UI components that depend on Google APIs
    // until onConnected() is called.
  }

  @Override
  public void onConnectionFailed(ConnectionResult result) {
    // This callback is important for handling errors that
    // may occur while attempting to connect with Google.
    //
    // More about this in the next section.
  }
}

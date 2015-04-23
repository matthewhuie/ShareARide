package edu.cmu.andrew.sharearide;


import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.AsyncTask;
import android.os.Handler;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.TextView;
import android.widget.Toast;

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

import edu.cmu.andrew.sharearide.backend.shareARideApi.ShareARideApi;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.MessageBean;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.UserBean;
import edu.cmu.andrew.utilities.EndPointManager;


public class DriverHome extends FragmentActivity implements GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.
    private GoogleApiClient mGoogleApiClient;
    private Location mLastLocation;
    private double latitude;
    private double longitude;
    public final Handler handler = new Handler();
    private ShareARideApi apiInstance = null;
    String userName="";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_home);
        buildGoogleApiClient ();
        setUpMapIfNeeded ();
        pollForMessages();
    }

    private void pollForMessages() {
        Intent myIntent = getIntent (); // gets the previously created intent
        userName = myIntent.getStringExtra ("userName");
        Sync sync = new Sync(call,60*1000);

    }


    public class Sync {
        Runnable task;

        public Sync(Runnable task, long time) {
            this.task = task;
            handler.removeCallbacks(task);
            handler.postDelayed(task, time);
        }
    }

    final private Runnable call = new Runnable() {
        public void run() {
            //This is where my sync code will be, but for testing purposes I only have a Log statement
            //will run every 20 seconds
            new PollTask().execute(userName);
            handler.postDelayed(call,20*1000);
        }
    };

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


    private void setUpMapIfNeeded () {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager ().findFragmentById (R.id.driver_map))
                    .getMap ();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap ();
            }
        }
    }


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
            ((TextView) findViewById (R.id.driver_location)).setText ("Current location: " + thisPlace);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * async task to poll message table
     */
    class PollTask extends AsyncTask<String, Void, MessageBean> {

        @Override
        protected MessageBean doInBackground (String... data) {
            MessageBean mb = new MessageBean ();
            try {
                if (apiInstance == null) {  // Only do this once
                    apiInstance = EndPointManager.getEndpointInstance ();
                }
                mb = apiInstance.pollMessage(data[0]).execute ();
            } catch (IOException e) {
                e.printStackTrace ();
            }
            return mb;

        }

        @Override
        protected void onPostExecute (MessageBean result) {
            //Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            //  System.out.println(result[0]);
            //System.out.println(result[1]);

            Toast.makeText(DriverHome.this, result.getMessage(), Toast.LENGTH_LONG).show ();
        }

    }

}

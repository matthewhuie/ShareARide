package edu.cmu.andrew.sharearide;

import android.app.Fragment;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import edu.cmu.andrew.sharearide.backend.shareARideApi.model.MessageBean;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.RequestBean;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.TripBean;
import edu.cmu.andrew.utilities.MapHelper;
import edu.cmu.andrew.utilities.EndPointManager;
import edu.cmu.andrew.utilities.TripSegment;

public class DriverMapFragment extends Fragment {

  private GoogleMap mMap; // Might be null if Google Play services APK is not available.
  private RelativeLayout mLayout;
  private SARActivity mContext;
  private TextView mMapText;
  private TextView mMapSecondaryText;
  private List<LatLng> directions;
  private List<TripSegment> trip;
  private int currentTrip;
  public final Handler handler = new Handler ();


  @Override
  public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mContext = (SARActivity) super.getActivity ();
    mLayout = (RelativeLayout) inflater.inflate (R.layout.activity_driver_map, container, false);
    mMapText = (TextView) mLayout.findViewById (R.id.driver_map_text);
    mMapSecondaryText = (TextView) mLayout.findViewById (R.id.driver_map_secondary_text);

    directions = new ArrayList<> ();
    currentTrip = -1;
    setUpMapIfNeeded ();
    initTrip ();
    return mLayout;
  }

  @Override
  public void onResume () {
    super.onResume ();
    setUpMapIfNeeded ();
  }

  /**
   * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
   * installed) and the map has not already been instantiated.. This will ensure that we only ever
   * call {@link #setUpMap()} once when {@link #mMap} is not null.
   * <p/>
   * If it isn't installed {@link com.google.android.gms.maps.SupportMapFragment} (and
   * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
   * install/update the Google Play services APK on their device.
   * <p/>
   * A user can return to this FragmentActivity after following the prompt and correctly
   * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
   * have been completely destroyed during this process (it is likely that it would only be
   * stopped or paused), {@link #onCreate(android.os.Bundle)} may not be called again so we should call this
   * method in {@link #onResume()} to guarantee that it will be called.
   */
  private void setUpMapIfNeeded () {
    // Do a null check to confirm that we have not already instantiated the map.
    if (mMap == null) {
      // Try to obtain the map from the SupportMapFragment.
      mMap = ((SupportMapFragment) mContext.getSupportFragmentManager ().findFragmentById (R.id.driver_map))
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
    mMap.moveCamera (CameraUpdateFactory.newLatLngZoom (
        new LatLng (mContext.getLatitude (), mContext.getLongitude ()), 15));
    mMap.setMyLocationEnabled (true);
  }

  private void initTrip () {
    trip = new ArrayList<TripSegment> ();
    new AsyncTask<Integer, Void, Void> (){
      @Override
      protected Void doInBackground(Integer... params) {
        try {
          EndPointManager.getEndpointInstance ().endPreviousTrips(params[0]).execute ();
        } catch (IOException e) {
          e.printStackTrace ();
        }
        return null;
      }

      @Override
      protected void onPostExecute (Void v) {
        updateTripRiders (0);
      }
    }.execute (mContext.getUserID ());
  }

  private void updateTripRiders (int numOfRiders) {
    new AsyncTask<Integer, Void, Void> (){

      @Override
      protected Void doInBackground(Integer... params) {
        TripBean trip = new TripBean ();
        try {
          trip = EndPointManager.getEndpointInstance ().updateTrip(params[0], params[1]).execute ();
        } catch (IOException e) {
          e.printStackTrace ();
        }

        currentTrip = trip.getTripId ();

        return null;
      }

      @Override
      protected void onPostExecute (Void v) {
        pollForMessages ();
      }
    }.execute (mContext.getUserID (), numOfRiders);
  }

  private void endTrip () {
    new AsyncTask<Integer, Void, Void> (){
      @Override
      protected Void doInBackground(Integer... params) {
        try {
          EndPointManager.getEndpointInstance ().endTrip(params[0]).execute ();
        } catch (IOException e) {
          e.printStackTrace ();
        }

        currentTrip = -1;

        return null;
      }
    }.execute (mContext.getUserID ());
    // *** calculate remaining fare

    trip = null;
  }

  private void readMessage (MessageBean mb) {
    String message = mb.getMessage ();
    int requestID = mb.getRequestId ();

    if (message.equals ("New Request")) {
      new AsyncTask<Integer, Void, RequestBean> (){
        @Override
        protected RequestBean doInBackground(Integer... params) {
          try {
            return EndPointManager.getEndpointInstance ().getRequest (params[0]).execute ();

          } catch (IOException e) {
            e.printStackTrace ();
          }
          return null;
        }

        @Override
        protected void onPostExecute (RequestBean rb) {
          startRequest (rb);
        }
      }.execute (requestID);
    }
  }

  private void startRequest (RequestBean rb) {
    LatLng rSrc = new LatLng (rb.getSrcLatitude (), rb.getSrcLongitude ());
    LatLng rDst = new LatLng (rb.getDstLatitude (), rb.getDstLongitude ());

    List<LatLng> paths = new ArrayList<> ();
    paths.add (rSrc);
    paths.add (rDst);

    updateTripRiders (rb.getNumOfRiders ());
    List<Integer> requests = new ArrayList<> ();
    if (trip.size () > 0) {
      TripSegment previous = trip.get (trip.size () - 1);
      paths.add (previous.getDestination ());
      previous.setDestination (rSrc);
      previous.setCompleted (true);

      requests.addAll (previous.getRequests ());
      requests.add (new Integer (rb.getRequestId ()));
    }

    for (TripSegment ts : trip) {
      if (! ts.isCompleted ()) {
        paths.add (ts.getDestination ());
      }
    }

    LatLng[] ll = new LatLng[paths.size ()];
    ll = paths.toArray (ll);
    new NextRouteTask (requests).execute (ll);
  }

  private void finishRequest (RequestBean rb) {
    LatLng rDst = new LatLng (rb.getDstLatitude (), rb.getDstLongitude ());

    List<LatLng> paths = new ArrayList<> ();
    paths.add (rDst);
    TripSegment previous = trip.get (trip.size () - 1);
    previous.setCompleted (true);

    List<Integer> passengers = previous.getRequests ();
    passengers.remove (new Integer (rb.getPassUserId ()));
    if (passengers.size () == 0) {
      endTrip ();
    } else {
      for (TripSegment ts : trip) {
        if (! ts.isCompleted ()) {
          paths.add (ts.getDestination ());
        }
      }

      LatLng[] ll = new LatLng[paths.size ()];
      ll = paths.toArray (ll);
      new NextRouteTask (passengers).execute (ll);
    }
  }

  class NextRouteTask extends AsyncTask <LatLng, Void, JSONArray> {

    List<Integer> requests;

    public NextRouteTask (List<Integer> requests) {
      this.requests = requests;
    }

    @Override
    protected JSONArray doInBackground (LatLng... data) {
      String key = "key=" + getString (R.string.google_maps_places_key);
      int minTimeDistance = Integer.MAX_VALUE;
      int minDistance = 0;
      int minTime = 0;
      LatLng minDestination = null;
      JSONArray minSteps = null;
      String origin = "origin=" + data[0].latitude + "," + data[0].longitude + "&";

      for (int i = 1; i < data.length; i++) {
        String destination = "destination=" + data[i].latitude + "," + data[i].longitude + "&";
        String json = mContext.getRemoteJSON (mContext.DIRECTION_BASE_URL + origin + destination + key);

        try {
          JSONObject routeObject = new JSONObject (json);
          JSONObject route = (JSONObject) routeObject.getJSONArray ("routes").get (0);
          JSONObject leg = (JSONObject) route.getJSONArray ("legs").get (0);

          int distance = Integer.parseInt (leg.getJSONObject ("distance").get ("value").toString ());
          int duration = Integer.parseInt (leg.getJSONObject ("duration").get ("value").toString ());

          int timeDistance = distance * duration;
          if (timeDistance < minTimeDistance) {
            minTimeDistance = timeDistance;
            minDistance = distance;
            minTime = duration;
            minDestination = data[i];
            minSteps = leg.getJSONArray ("steps");
          }
        } catch (JSONException jsone) {
          Log.i ("Hit the JSON error: ", jsone.toString ());
        }
      }

      trip.add (new TripSegment (trip.size (), data [0], minDestination, minDistance, minTime, requests));

      return minSteps;
    }

    @Override
    protected void onPostExecute (JSONArray steps) {
      try {
        String polyline;

        for (int i = 0; i < steps.length (); i++) {
          JSONObject step = (JSONObject) steps.get (i);
          polyline = ((JSONObject) step.get ("polyline")).get ("points").toString ();

          directions.addAll (MapHelper.decodePolyline (polyline));
        }
      } catch (JSONException jsone) {
        Log.i ("Hit the JSON error: ", jsone.toString ());
      }

      mMap.clear ();
      mMap.addPolyline (new PolylineOptions ()
          .addAll (directions)
          .width (10)
          .color (Color.rgb (1, 169, 212)));
    }

  }

  /**
   * async task to poll message table
   */
  class PollTask extends AsyncTask<Integer, Void, MessageBean> {

    @Override
    protected MessageBean doInBackground (Integer... data) {
      MessageBean mb = new MessageBean ();
      try {
        mb = EndPointManager.getEndpointInstance ().pollMessage(data[0]).execute ();
      } catch (IOException e) {
        e.printStackTrace ();
      }
      return mb;
    }

    @Override
    protected void onPostExecute (MessageBean result) {
      if (result != null) readMessage (result);
    }

  }

  public void pollForMessages() {
    Sync sync = new Sync(call,1);

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
      mMapText.setText ("Waiting for requests...");
      new PollTask().execute (mContext.getUserID ());
      mMap.moveCamera (CameraUpdateFactory.newLatLngZoom (
          new LatLng (mContext.getLatitude (), mContext.getLongitude ()), 15));
      handler.postDelayed (call, 20 * 1000);
    }
  };
}

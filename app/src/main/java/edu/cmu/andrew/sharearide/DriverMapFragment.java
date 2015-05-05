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
import android.widget.Button;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import edu.cmu.andrew.sharearide.backend.shareARideApi.model.MessageBean;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.RequestBean;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.TripBean;
import edu.cmu.andrew.utilities.DirectionsJSONParser;
import edu.cmu.andrew.utilities.EndPointManager;
import edu.cmu.andrew.utilities.PricingAlgorithm;
import edu.cmu.andrew.utilities.TripSegment;

/**
 * DriverMapFragment is the main fragment for a driver of the platform.
 * It displays the map for navigation and appropriate text for input requests.
 */
public class DriverMapFragment extends Fragment {

  /**
   * The Google Map object
   */
  private GoogleMap mMap;

  /**
   * This fragment's layout
   */
  private RelativeLayout mLayout;

  /**
   * Ths main activity
   */
  private SARActivity mContext;

  /**
   * The main TextView of this fragment
   */
  private TextView mMapText;

  /**
   * The main submit button of this fragment
   */
  private Button mMapButton;

  /**
   * The directions from source to destination
   */
  private List<LatLng> directions;

  /**
   * The list of TripSegments within this trip
   */
  private List<TripSegment> trip;

  /**
   * The current TripSegment ID
   */
  private int currentTrip;

  /**
   * The handle for polling operations
   */
  public final Handler handler = new Handler ();

  /**
   * A list of probably destinations for the routing algorithm
   */
  private HashMap<RequestBean, LatLng> destinations;


  /**
   * This fragment's onCreateView method
   *
   * @param inflater           the LayoutInflater
   * @param container          the ViewGroup
   * @param savedInstanceState the Bundle
   * @return the created view
   */
  @Override
  public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mContext = (SARActivity) super.getActivity ();
    mLayout = (RelativeLayout) inflater.inflate (R.layout.activity_driver_map, container, false);
    mMapText = (TextView) mLayout.findViewById (R.id.driver_map_text);
    mMapButton = (Button) mLayout.findViewById (R.id.driver_map_button);

    /** Initiates the map */
    setUpMapIfNeeded ();

    /** Initiates the trip */
    initTrip ();

    return mLayout;
  }

  /**
   * The fragment's onResume method
   */
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

  /**
   * Places a marker where the passenger is
   *
   * @param latlng the passenger's location
   */
  private void setUpPassLocation (LatLng latlng) {
    if (mMap != null) {
      mMap.moveCamera (CameraUpdateFactory.newLatLngZoom (latlng, 13));
      Marker marker_destination = mMap.addMarker (new MarkerOptions ()
          .position (latlng)
          .icon (BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_BLUE))
          .title ("Pick up your passenger here!"));
    }
  }

  /**
   * Places a marker where the current destination is
   *
   * @param latlng the current destination location
   */
  private void setUpDestination (LatLng latlng) {
    if (mMap != null) {
      mMap.moveCamera (CameraUpdateFactory.newLatLngZoom (latlng, 13));
      Marker marker_destination = mMap.addMarker (new MarkerOptions ()
          .position (latlng)
          .title ("Current destination"));
    }
  }

  /**
   * Initiates the trip for this driver
   */
  private void initTrip () {
    /** Resets all parameters needed for a trip */
    trip = new ArrayList<TripSegment> ();
    directions = new ArrayList<> ();
    destinations = new HashMap<> ();
    currentTrip = -1;
    mMapButton.setVisibility (View.INVISIBLE);
    mMapText.setText (getString (R.string.request_message));
    mMap.clear ();

    /** Ends all previous trips to ensure a clean trip */
    new AsyncTask<Integer, Void, Void> () {
      @Override
      protected Void doInBackground (Integer... params) {
        try {
          EndPointManager.getEndpointInstance ().endPreviousTrips (params[0]).execute ();
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

  /**
   * Updates the number of riders within this trip
   *
   * @param numOfRiders the number of riders within this trip
   */
  private void updateTripRiders (int numOfRiders) {
    new AsyncTask<Integer, Void, Void> () {

      @Override
      protected Void doInBackground (Integer... params) {
        TripBean trip = new TripBean ();
        try {

          /** Updates the trip with the specified number of riders */
          trip = EndPointManager.getEndpointInstance ().updateTrip (params[0], params[1]).execute ();
        } catch (IOException e) {
          e.printStackTrace ();
        }

        /** Sets the current trip ID */
        currentTrip = trip.getTripId ();

        return null;
      }

      @Override
      protected void onPostExecute (Void v) {

        /** Once the trip is initiated, poll for possible requests */
        pollForMessages ();
      }
    }.execute (mContext.getUserID (), numOfRiders);
  }

  /**
   * Ends the current trip and resets this driver for a new trip
   */
  private void endTrip () {
    new AsyncTask<Integer, Void, Void> () {
      @Override
      protected Void doInBackground (Integer... params) {
        try {
          EndPointManager.getEndpointInstance ().endTrip (params[0]).execute ();
        } catch (IOException e) {
          e.printStackTrace ();
        }

        currentTrip = -1;

        return null;
      }
    }.execute (mContext.getUserID ());

    initTrip ();
  }

  /**
   * Reads an incoming message for a passenger request
   *
   * @param mb the incoming message
   */
  private void readMessage (MessageBean mb) {
    String message = mb.getMessage ();
    int requestID = mb.getRequestId ();

    /** Respond accordingly if it is a new request */
    if (message.equals ("New Request")) {

      /** Retrieves the RequestBean for the incoming request */
      new AsyncTask<Integer, Void, RequestBean> () {
        @Override
        protected RequestBean doInBackground (Integer... params) {
          try {
            return EndPointManager.getEndpointInstance ().getRequest (params[0]).execute ();

          } catch (IOException e) {
            e.printStackTrace ();
          }
          return null;
        }

        @Override
        protected void onPostExecute (RequestBean rb) {

          /** Initiates routing algorithm to the requests's destination */
          toRequest (rb);
        }
      }.execute (requestID);
    }
  }

  /**
   * Performs the routing algorithm from the current location to the new request's source
   *
   * @param rb the new request's RequestBean
   */
  private void toRequest (RequestBean rb) {
    /** Sets the source to driver's current location and destination to the request's source */
    LatLng rSrc = new LatLng (mContext.getLatitude (), mContext.getLongitude ());
    LatLng rDst = new LatLng (rb.getSrcLatitude (), rb.getSrcLongitude ());

    /** Adds the above source and destination to the list of possible paths */
    List<LatLng> paths = new ArrayList<> ();
    paths.add (rSrc);
    paths.add (rDst);

    updateTripRiders (rb.getNumOfRiders ());

    /** Updates the list of requests within the new TripSegment */
    List<Integer> requests = new ArrayList<> ();
    requests.add (new Integer (rb.getRequestId ()));

    /** If there is a previous TripSegment, end it and start a new one */
    if (trip.size () > 0) {
      TripSegment previous = trip.get (trip.size () - 1);
      paths.add (previous.getDestination ());
      previous.setDestination (rSrc);
      previous.setCompleted (true);

      /** Grabs the previous list of requests from the previous TripSegment */
      requests.addAll (previous.getRequests ());

    }

    /** Converts the list of possible paths to an array */
    LatLng[] ll = new LatLng[paths.size ()];
    ll = paths.toArray (ll);

    /** Starts routing algorithm for here to source of request */
    new ToRequestTask (requests, rb).execute (ll);
  }

  /**
   * Performs the routing algorithm from the source to the best destination
   *
   * @param rb the new request's RequestBean
   */
  private void startRequest (RequestBean rb) {
    /** Sets the source to request's source and destination to the request's destination */
    LatLng rSrc = new LatLng (rb.getSrcLatitude (), rb.getSrcLongitude ());
    LatLng rDst = new LatLng (rb.getDstLatitude (), rb.getDstLongitude ());

    /** Places this destination in the list of destinations */
    destinations.put (rb, rDst);

    /** Adds the above source and destination to the list of possible paths */
    List<LatLng> paths = new ArrayList<> ();
    paths.add (rSrc);

    /** Gets the previous list of requests */
    List<Integer> requests = new ArrayList<> (trip.get (trip.size () - 1).getRequests ());

    TripSegment previous = trip.get (trip.size () - 1);
    previous.setDestination (rSrc);
    previous.setCompleted (true);

    double pastFare = PricingAlgorithm.calcTripSegmentPrice (previous);
    for (int request : previous.getRequests ()) {
      new UpdateFareTask (pastFare).execute (request);
      new UpdateDistanceTimeTask (previous.getDistance (),
          previous.getDuration ()).execute (request);
    }

    paths.addAll (destinations.values ());

    /** Converts the list of possible paths to an array */
    LatLng[] ll = new LatLng[paths.size ()];
    ll = paths.toArray (ll);

    /** Starts routing algorithm for source to best destination */
    new NextRouteTask (requests, rb).execute (ll);
  }

  private void finishRequest (RequestBean rb) {
    LatLng rDst = new LatLng (rb.getDstLatitude (), rb.getDstLongitude ());

    destinations.remove (rb);

    /** Adds the above source and destination to the list of possible paths */
    List<LatLng> paths = new ArrayList<> ();
    paths.add (rDst);

    TripSegment previous = trip.get (trip.size () - 1);
    previous.setCompleted (true);

    double pastFare = PricingAlgorithm.calcTripSegmentPrice (previous);
    for (int request : previous.getRequests ()) {
      new UpdateFareTask (pastFare).execute (request);
      new UpdateDistanceTimeTask (previous.getDistance (),
          previous.getDuration ()).execute (request);
    }

    List<Integer> requests = previous.getRequests ();
    requests.remove (new Integer (rb.getRequestId ()));

    new UpdateFareTask (PricingAlgorithm.calcFinalPrice (
        rb.getDistanceEstimated (), rb.getEstimatedTime (),
        rb.getActualDistance () + previous.getDistance (),
        rb.getActualDuration () + previous.getDuration ())
    ).execute (rb.getRequestId ());

    if (requests.size () == 0) {
      endTrip ();
    } else {
      /**for (TripSegment ts : trip) {
       if (! ts.isCompleted ()) {
       paths.add (ts.getDestination ());
       }
       }*/
      paths.addAll (destinations.values ());
      System.out.println (destinations);
      System.out.println (paths);

      /** Converts the list of possible paths to an array */
      LatLng[] ll = new LatLng[paths.size ()];
      ll = paths.toArray (ll);

      /** Starts routing algorithm for destination to next best destination */
      new NextRouteTask (requests, (RequestBean) destinations.keySet ().iterator ().next ()).execute (ll);
    }

    new AsyncTask<RequestBean, Void, Void> () {
      @Override
      protected Void doInBackground (RequestBean... data) {
        try {
          RequestBean rb = data[0];
          EndPointManager.getEndpointInstance ().createMessage (rb.getPassUserId (), "End Request", rb.getRequestId ()).execute ();
        } catch (IOException ioe) {
        }
        return null;
      }
    }.execute (rb);
  }

  class ToRequestTask extends AsyncTask<LatLng, Void, DirectionsJSONParser> {

    List<Integer> requests;
    String username;
    RequestBean rb;

    public ToRequestTask (List<Integer> requests, RequestBean rb) {
      this.requests = requests;
      this.rb = rb;
    }

    @Override
    protected DirectionsJSONParser doInBackground (LatLng... request) {
      try {
        String origin = "origin=" + request[0].latitude + "," + request[0].longitude + "&";
        String destination = "destination=" + request[1].latitude + "," + request[1].longitude + "&";
        String key = "key=" + getString (R.string.google_maps_places_key);
        String json = mContext.getRemoteJSON (mContext.DIRECTION_BASE_URL + origin + destination + key);

        DirectionsJSONParser parser = new DirectionsJSONParser (json, request[0], request[1]);
        username = (EndPointManager.getEndpointInstance ().getUserByID (rb.getPassUserId ()).execute ()).getUserName ();

        return parser;
      } catch (JSONException jsone) {
      } catch (IOException ioe) {
      }

      return null;
    }

    @Override
    protected void onPostExecute (DirectionsJSONParser parser) {
      try {
        List<LatLng> directions = parser.getPolyline ();
        LatLng dest = parser.getDestination ();
        LatLng source = parser.getSource ();
        trip.add (new TripSegment (trip.size (), source, dest,
            parser.getDistance () * mContext.MeterToMile, parser.getDuration () / mContext.SecToMin, requests));

        mMap.clear ();
        mMap.addPolyline (new PolylineOptions ()
            .addAll (directions)
            .width (10)
            .color (Color.rgb (1, 169, 212)));

        setUpDestination (dest);
        setUpPassLocation (source);

        updateMapText ("Picking up " + username);
        updateButton (true, rb);
      } catch (JSONException jsone) {
      }
    }
  }

  class NextRouteTask extends AsyncTask<LatLng, Void, DirectionsJSONParser> {

    List<Integer> requests;
    int userID;
    boolean isPickUp;
    String username;
    RequestBean rb;

    public NextRouteTask (List<Integer> requests, RequestBean rb) {
      this.requests = requests;
      this.userID = rb.getPassUserId ();
      this.rb = rb;
    }

    @Override
    protected DirectionsJSONParser doInBackground (LatLng... data) {
      String key = "key=" + getString (R.string.google_maps_places_key);
      double minTimeDistance = Double.MAX_VALUE;
      DirectionsJSONParser minParser = null;
      String origin = "origin=" + data[0].latitude + "," + data[0].longitude + "&";

      for (int i = 1; i < data.length; i++) {
        String destination = "destination=" + data[i].latitude + "," + data[i].longitude + "&";
        String json = mContext.getRemoteJSON (mContext.DIRECTION_BASE_URL + origin + destination + key);

        try {
          rb = EndPointManager.getEndpointInstance ().getRequest (rb.getRequestId ()).execute ();
          DirectionsJSONParser directions = new DirectionsJSONParser (json, data[0], data[i]);

          double distance = directions.getDistance ();
          double duration = directions.getDuration ();
          double timeDistance = distance * duration;
          if (timeDistance < minTimeDistance) {
            minTimeDistance = timeDistance;
            minParser = directions;
          }

          username = (EndPointManager.getEndpointInstance ().getUserByID (userID).execute ()).getUserName ();
        } catch (JSONException jsone) {
          Log.i ("Hit the JSON error: ", jsone.toString ());
        } catch (IOException ioe) {
          Log.i ("Hit the IO error: ", ioe.toString ());
        }
      }

      return minParser;
    }

    @Override
    protected void onPostExecute (DirectionsJSONParser parser) {
      try {
        List<LatLng> directions = parser.getPolyline ();
        LatLng destination = parser.getDestination ();
        LatLng source = parser.getSource ();
        trip.add (new TripSegment (trip.size (), parser.getSource (), parser.getDestination (),
            parser.getDistance () * mContext.MeterToMile, parser.getDuration () / mContext.SecToMin, requests));

        mMap.clear ();
        mMap.addPolyline (new PolylineOptions ()
            .addAll (directions)
            .width (10)
            .color (Color.rgb (1, 169, 212)));

        updateMapText ("Dropping off " + username);
        updateButton (false, rb);

        setUpDestination (destination);
        setUpPassLocation (source);
        Log.i ("Hit the JSON error: ", String.valueOf (destination.latitude) + String.valueOf (destination.longitude));


      } catch (JSONException jsone) {
        Log.i ("Hit the JSON error: ", jsone.toString ());
      }
    }

  }

  class UpdateFareTask extends AsyncTask<Integer, Void, Void> {

    double fareToAdd;

    public UpdateFareTask (double fareToAdd) {
      this.fareToAdd = fareToAdd;
    }

    @Override
    protected Void doInBackground (Integer... data) {
      try {
        EndPointManager.getEndpointInstance ().updateFare (data[0], fareToAdd).execute ();
      } catch (IOException e) {
        e.printStackTrace ();
      }

      return null;
    }
  }

  class UpdateDistanceTimeTask extends AsyncTask<Integer, Void, Void> {

    double distanceToAdd;
    double timeToAdd;

    public UpdateDistanceTimeTask (double distanceToAdd, double timeToAdd) {
      this.distanceToAdd = distanceToAdd;
      this.timeToAdd = timeToAdd;
    }

    @Override
    protected Void doInBackground (Integer... data) {
      try {
        EndPointManager.getEndpointInstance ().updateDistanceTime (data[0], distanceToAdd, timeToAdd).execute ();
      } catch (IOException e) {
        e.printStackTrace ();
      }

      return null;
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
        mb = EndPointManager.getEndpointInstance ().pollMessage (data[0]).execute ();
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

  public void pollForMessages () {
    Sync sync = new Sync (call, 1);

  }

  public class Sync {
    Runnable task;

    public Sync (Runnable task, long time) {
      this.task = task;
      handler.removeCallbacks (task);
      handler.postDelayed (task, time);
    }
  }

  final private Runnable call = new Runnable () {
    public void run () {
      //This is where my sync code will be, but for testing purposes I only have a Log statement
      //will run every 20 seconds
      new PollTask ().execute (mContext.getUserID ());
      new AsyncTask<Integer, Void, Void> () {
        @Override
        protected Void doInBackground (Integer... data) {
          try {
            EndPointManager.getEndpointInstance ().updateLocation (data[0],
                mContext.getLatitude (), mContext.getLongitude ()).execute ();
          } catch (IOException ioe) {
          }
          return null;
        }
      };
      mMap.moveCamera (CameraUpdateFactory.newLatLngZoom (
          new LatLng (mContext.getLatitude (), mContext.getLongitude ()), 15));
      handler.postDelayed (call, 20 * 1000);
    }
  };

  private void updateMapText (String text) {
    mMapText.setText (text);
  }

  private void updateButton (boolean isPickedUp, RequestBean rb) {
    mMapButton.setClickable (true);
    mMapButton.setBackgroundColor (getResources ().getColor (R.color.material_red_500));
    mMapButton.setVisibility (View.VISIBLE);
    mMapButton.setText (getString (R.string.complete_task_text));
    mMapButton.setOnClickListener (new ActionOnClick (isPickedUp, rb));
  }

  private void disableButton () {
    mMapButton.setClickable (false);
    mMapButton.setText (getString (R.string.task_completed_text));
    mMapButton.setBackgroundColor (getResources ().getColor (R.color.material_red_900));
  }

  class ActionOnClick implements View.OnClickListener {
    boolean isPickedUp;
    RequestBean rb;

    public ActionOnClick (boolean isPickedUp, RequestBean rb) {
      this.isPickedUp = isPickedUp;
      this.rb = rb;
    }

    @Override
    public void onClick (View v) {
      disableButton ();
      if (isPickedUp) {
        startRequest (rb);
      } else {
        finishRequest (rb);
      }
    }
  }
}

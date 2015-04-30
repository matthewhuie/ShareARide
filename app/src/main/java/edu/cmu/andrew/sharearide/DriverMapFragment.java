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
import java.util.List;

import edu.cmu.andrew.sharearide.backend.shareARideApi.model.MessageBean;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.RequestBean;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.TripBean;
import edu.cmu.andrew.utilities.DirectionsJSONParser;
import edu.cmu.andrew.utilities.EndPointManager;
import edu.cmu.andrew.utilities.PricingAlgorithm;
import edu.cmu.andrew.utilities.TripSegment;

public class DriverMapFragment extends Fragment {

  private GoogleMap mMap; // Might be null if Google Play services APK is not available.
  private RelativeLayout mLayout;
  private SARActivity mContext;
  private TextView mMapText;
  private Button mMapButton;
  private List<LatLng> directions;
  private List<TripSegment> trip;
  private int currentTrip;
  public final Handler handler = new Handler ();


  @Override
  public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mContext = (SARActivity) super.getActivity ();
    mLayout = (RelativeLayout) inflater.inflate (R.layout.activity_driver_map, container, false);
    mMapText = (TextView) mLayout.findViewById (R.id.driver_map_text);
    mMapButton = (Button) mLayout.findViewById (R.id.driver_map_button);

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

    private void setUpPassLocation (LatLng latlng) {
        if (mMap != null) {
            mMap.moveCamera (CameraUpdateFactory.newLatLngZoom (latlng, 13));
            Marker marker_destination = mMap.addMarker (new MarkerOptions()
                    .position (latlng)
                    .icon (BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_BLUE))
                    .title ("Pick up your passenger here!"));
        }
    }

    private void setUpDestination (LatLng latlng) {
        if (mMap != null) {
            mMap.moveCamera (CameraUpdateFactory.newLatLngZoom (latlng, 13));
            Marker marker_destination = mMap.addMarker (new MarkerOptions ()
                    .position (latlng)
                    .title ("Current destination"));
        }
    }

  private void initTrip () {
    trip = new ArrayList<TripSegment> ();
    directions = new ArrayList<> ();
    currentTrip = -1;
    mMapButton.setVisibility (View.INVISIBLE);
    mMapText.setText (getString(R.string.request_message));
    mMap.clear();
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

    initTrip ();
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
          toRequest (rb);
        }
      }.execute (requestID);
    }
  }

  private void toRequest (RequestBean rb) {
    LatLng rSrc = new LatLng (mContext.getLatitude (), mContext.getLongitude ());
    LatLng rDst = new LatLng (rb.getSrcLatitude (), rb.getSrcLongitude ());

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

      double pastFare = PricingAlgorithm.calcTripSegmentPrice (previous);
      UpdateFareTask uft = new UpdateFareTask (pastFare);
      UpdateDistanceTimeTask udtt = new UpdateDistanceTimeTask (previous.getDistance () * mContext.MeterToMile,
          previous.getDuration () / mContext.SecToMin);
      for (int request : previous.getRequests ()) {
        uft.execute (request);
        udtt.execute (request);
      }
    }

    LatLng[] ll = new LatLng[paths.size ()];
    ll = paths.toArray (ll);

    new ToRequestTask (requests, rb).execute(ll);
  }

  private void startRequest (RequestBean rb) {
    LatLng rSrc = new LatLng (rb.getSrcLatitude (), rb.getSrcLongitude ());
    LatLng rDst = new LatLng (rb.getDstLatitude (), rb.getDstLongitude ());

    List<LatLng> paths = new ArrayList<> ();
    paths.add (rSrc);
    paths.add (rDst);
    List<Integer> requests = new ArrayList<> (trip.get (trip.size () - 1).getRequests ());

    TripSegment previous = trip.get (trip.size () - 1);
    previous.setDestination (rSrc);
    previous.setCompleted (true);

    double pastFare = PricingAlgorithm.calcTripSegmentPrice (previous);
    UpdateFareTask uft = new UpdateFareTask (pastFare);
    UpdateDistanceTimeTask udtt = new UpdateDistanceTimeTask (previous.getDistance () * mContext.MeterToMile,
        previous.getDuration () / mContext.SecToMin);
    for (int request : previous.getRequests ()) {
      uft.execute (request);
      udtt.execute (request);
    }

    for (TripSegment ts : trip) {
      if (! ts.isCompleted ()) {
        paths.add (ts.getDestination ());
      }
    }

    LatLng[] ll = new LatLng[paths.size ()];
    ll = paths.toArray (ll);
    new NextRouteTask (requests, rb).execute (ll);
  }

  private void finishRequest (RequestBean rb) {
    LatLng rDst = new LatLng (rb.getDstLatitude (), rb.getDstLongitude ());

    List<LatLng> paths = new ArrayList<> ();
    paths.add (rDst);
    TripSegment previous = trip.get (trip.size () - 1);
    previous.setCompleted (true);

    double pastFare = PricingAlgorithm.calcTripSegmentPrice (previous);
    UpdateFareTask uft = new UpdateFareTask (pastFare);
    UpdateDistanceTimeTask udtt = new UpdateDistanceTimeTask (previous.getDistance () * mContext.MeterToMile,
        previous.getDuration () / mContext.SecToMin);
    for (int request : previous.getRequests ()) {
      uft.execute (request);
      udtt.execute (request);
    }

    List<Integer> requests = previous.getRequests ();
    requests.remove (new Integer (rb.getRequestId ()));

    new UpdateFareTask (PricingAlgorithm.calcFinalPrice (
        rb.getDistanceEstimated (), rb.getEstimatedTime (),
        rb.getActualDistance () + previous.getDistance () * mContext.MeterToMile,
        0 + previous.getDuration () / mContext.SecToMin)
    ).execute (rb.getRequestId ());

    if (requests.size () == 0) {
      endTrip ();
    } else {
      for (TripSegment ts : trip) {
        if (! ts.isCompleted ()) {
          paths.add (ts.getDestination ());
        }
      }

      LatLng[] ll = new LatLng[paths.size ()];
      ll = paths.toArray (ll);
      new NextRouteTask (requests, rb).execute (ll);
    }

    new AsyncTask<RequestBean, Void, Void> () {
      @Override
      protected Void doInBackground (RequestBean... data) {
        try {
          RequestBean rb = data[0];
          EndPointManager.getEndpointInstance ().createMessage (rb.getPassUserId (), "End Request", rb.getRequestId ()).execute ();
        } catch (IOException ioe) {}
        return null;
      }
    }.execute(rb);
  }

  class ToRequestTask extends AsyncTask <LatLng, Void, DirectionsJSONParser> {

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
      } catch(JSONException jsone){}
      catch(IOException ioe){}

      return null;
    }

      @Override
      protected void onPostExecute (DirectionsJSONParser parser){
        try {
        List<LatLng> directions = parser.getPolyline ();
        LatLng dest = parser.getDestination ();
        LatLng source = parser.getSource ();
        trip.add (new TripSegment (trip.size (), source, dest,
            parser.getDistance (), parser.getDuration (), requests));

        mMap.clear ();
        mMap.addPolyline (new PolylineOptions ()
            .addAll (directions)
            .width (10)
            .color (Color.rgb (1, 169, 212)));

        setUpDestination (dest);
        setUpPassLocation (source);

        updateMapText ("Picking up " + username);
        updateButton (true, rb);
      } catch(JSONException jsone){}
    }
  }

  class NextRouteTask extends AsyncTask <LatLng, Void, DirectionsJSONParser> {

    List<Integer> requests;
    int userID;
    boolean isPickUp;
    String username;
    RequestBean rb;

    public NextRouteTask (List<Integer> requests, RequestBean rb) {
      this.requests = requests;
      this.userID = rb.getPassUserId ();
      this.isPickUp = isPickUp;
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
          DirectionsJSONParser directions = new DirectionsJSONParser (json, data [0], data [i]);

          double distance = directions.getDistance () * mContext.MeterToMile;
          double duration = directions.getDuration () / mContext.SecToMin;
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
        List <LatLng> directions = parser.getPolyline ();
        LatLng destination = parser.getDestination ();
        LatLng source = parser.getSource ();
        trip.add (new TripSegment (trip.size (), parser.getSource (), parser.getDestination (),
            parser.getDistance (), parser.getDuration (), requests));

        mMap.clear ();
        mMap.addPolyline (new PolylineOptions ()
            .addAll (directions)
            .width (10)
            .color (Color.rgb (1, 169, 212)));

        updateMapText ("Dropping off " + username);
        updateButton (false, rb);

        setUpDestination(destination);
        setUpPassLocation(source);
        Log.i ("Hit the JSON error: ", String.valueOf(destination.latitude)+String.valueOf(destination.longitude));


      } catch (JSONException jsone) {
        Log.i ("Hit the JSON error: ", jsone.toString ());
      }
    }

  }

  class UpdateFareTask extends AsyncTask <Integer, Void, Void> {

    double fareToAdd;

    public UpdateFareTask (double fareToAdd) {
      this.fareToAdd = fareToAdd;
    }

    @Override
    protected Void doInBackground (Integer... data) {
      try {
        EndPointManager.getEndpointInstance ().updateFare(data[0], fareToAdd).execute ();
      } catch (IOException e) {
        e.printStackTrace ();
      }

      return null;
    }
  }

  class UpdateDistanceTimeTask extends AsyncTask <Integer, Void, Void> {

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
      new PollTask().execute (mContext.getUserID ());
      new AsyncTask<Integer, Void, Void> () {
        @Override
        protected Void doInBackground (Integer... data) {
          try {
            EndPointManager.getEndpointInstance ().updateLocation (data[0],
                mContext.getLatitude (), mContext.getLongitude ()).execute ();
          } catch (IOException ioe) {}
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
//    mMapButton.setText ("Complete Task");
    mMapButton.setText (getString(R.string.complete_task_text));
    mMapButton.setOnClickListener (new ActionOnClick (isPickedUp, rb));
  }

  private void disableButton () {
    mMapButton.setClickable (false);
//    mMapButton.setText ("Task Completed!");
    mMapButton.setText (getString(R.string.task_completed_text));
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

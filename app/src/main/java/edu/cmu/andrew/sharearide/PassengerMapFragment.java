package edu.cmu.andrew.sharearide;

import android.app.Fragment;
import android.content.Intent;
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
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.PolylineOptions;

import org.json.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.json.JSONObject;

import java.io.IOException;
import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import edu.cmu.andrew.sharearide.backend.shareARideApi.ShareARideApi;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.MessageBean;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.RequestBean;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.UserBean;

import edu.cmu.andrew.sharearide.backend.shareARideApi.model.UserBeanCollection;
import edu.cmu.andrew.utilities.DirectionsJSONParser;
import edu.cmu.andrew.utilities.EndPointManager;
import edu.cmu.andrew.utilities.PricingAlgorithm;

public class PassengerMapFragment extends Fragment {

  private GoogleMap mMap; // Might be null if Google Play services APK is not available.
  private static ShareARideApi myApiService = null;
  private double latitude;
  private double longitude;
    int request_id = 0;
  private double dest_latitude = 0;
  private double dest_longitude = 0;
  private RelativeLayout mLayout;
    DecimalFormat df = new DecimalFormat ("'$'0.00");
    DecimalFormat df1 = new DecimalFormat("#.##");
  private SARActivity mContext;
  private int numOfRiders;
    double estimatedDistance = 0.0;
    double estimatedDuration = 0.0;
  double estimatedFare = 0.0;
  private List<LatLng> directions;
  private TextView mMapText;
  private TextView mMapSecondaryText;
  private long startTime;
  private long endTime;
    public final Handler handler = new Handler ();

    @Override
  public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mContext = (SARActivity) super.getActivity ();
    mLayout = (RelativeLayout) inflater.inflate (R.layout.activity_passenger_map, container, false);

      directions = new ArrayList<> ();
    mMapText = (TextView) mLayout.findViewById (R.id.pass_map_text);
      mMapSecondaryText = (TextView) mLayout.findViewById (R.id.pass_map_secondary_text);
      latitude = mContext.getLatitude ();
    longitude = mContext.getLongitude();
      setUpMapIfNeeded ();
        selectDriver();
        pollForMessages();
    return mLayout;
  }

    private void pollForMessages() {

        Sync sync = new Sync(call,10*1000);
    }


    final private Runnable call = new Runnable() {
        public void run() {
            //This is where my sync code will be, but for testing purposes I only have a Log statement
            //will run every 2 seconds
            new AsyncPoll().execute(mContext.getUserID());
            handler.postDelayed(call,10*1000);
        }
    };

    public class Sync {
        Runnable task;

        public Sync(Runnable task, long time) {
            this.task = task;
            handler.removeCallbacks(task);
            handler.postDelayed(task, time);
        }
    }

    private class AsyncPoll extends AsyncTask <Integer, Void, RequestBean> {


        @Override
        protected RequestBean doInBackground(Integer... params) {
            MessageBean mb = new MessageBean ();
            RequestBean rb = new RequestBean();
            try {
                mb = EndPointManager.getEndpointInstance ().pollMessage(params[0]).execute ();

                if(mb!=null && mb.getMessage().equalsIgnoreCase("End Request")){
                    //update end time
                    myApiService.updateEndTime(mb.getRequestId()).execute();
                    //get request row -
                    rb =  myApiService.getRequest(mb.getRequestId()).execute();
                }else{
                   rb = myApiService.getRequest(request_id).execute();
                }


            } catch (IOException e) {
                e.printStackTrace ();
            }
            return rb;
        }

        protected void onPostExecute (RequestBean rb) {

            StringBuilder sb = new StringBuilder();

            mMapText.setText(getString(R.string.estimated_fare) + " " + df.format(estimatedFare)
                + "\n" + getString(R.string.max_time) + " " + df1.format(estimatedDuration) + " " + getString(R.string.minutes));


              sb.append(getString(R.string.actual_fare)).append(" ").append(df.format(rb.getFare())).append("\n");
              sb.append(getString(R.string.actual_distance)).append(" ").append(df1.format(rb.getActualDistance())).append(" ").append(getString(R.string.miles)).append("\n");
              sb.append(getString(R.string.travel_time)).append(df1.format(rb.getActualDuration())).append("\n").append(" ").append(getString(R.string.minutes));

                 mMapSecondaryText.setText(sb.toString());

        }


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
      mMap = ((SupportMapFragment) mContext.getSupportFragmentManager ().findFragmentById (R.id.map))
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
    mMap.moveCamera (CameraUpdateFactory.newLatLngZoom (new LatLng (latitude, longitude), 13));
  }

  private void setUpDestination (double dest_latitude, double dest_longitude, String destination) {
    Log.i ("add marker", "method executed");
    if (mMap != null) {
      Log.i ("map not null", "method executed");
      System.out.println ("In setUpDestionation" + dest_latitude + dest_longitude);
      mMap.moveCamera (CameraUpdateFactory.newLatLngZoom (new LatLng (dest_latitude, dest_longitude), 13));
      Marker marker_destination = mMap.addMarker (new MarkerOptions ()
          .position (new LatLng (dest_latitude, dest_longitude))
          .title (getString(R.string.your_destination) + destination));
    }
  }

  private void setUpDirection () {
    Log.i ("add polyline", "method executed");
    if (mMap != null) {
      mMap.clear ();
      mMap.addPolyline (new PolylineOptions ()
          .addAll (directions)
          .width (10)
          .color (Color.rgb (1, 169, 212)));
    }
  }

  private void setUpDriverLocation (double driver_latitude, double driver_longitude, String minDurTxt) {
    Log.i ("add marker", "method executed");
    if (mMap != null) {
      Log.i ("map not null", "method executed");
      System.out.println ("In setUpDriverLocation" + driver_latitude + driver_longitude);
      mMap.moveCamera (CameraUpdateFactory.newLatLngZoom (new LatLng (driver_latitude, driver_longitude), 13));
      Marker marker_destination = mMap.addMarker (new MarkerOptions ()
          .position (new LatLng (driver_latitude, driver_longitude))
          .icon (BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_GREEN))
          .title (getString(R.string.driver_text_1) + minDurTxt + getString(R.string.driver_text_2)));
    }
  }


  public void selectDriver () {
    String pickUpLocation = mContext.getLocationName();
    String destinationTxt = mContext.getDestination();
       numOfRiders = mContext.getNumOfRiders();

      System.out.println(pickUpLocation+ " pickUpLocation");
      System.out.println(destinationTxt+ " destinationTxt");

    //cannot make http request in main thread, has to create a asyn helper thread
    //calculatePriceAndTime(destinationTxt);

    new AsyncGooglePlaceSearch ().execute (pickUpLocation, destinationTxt);


    //aditi code for inserting request in request table
    Intent myIntent = mContext.getIntent (); // gets the previously created intent
    String userName = mContext.getUsername();

      System.out.println(userName+ " username");
      try {

          if(myApiService==null)
              myApiService = EndPointManager.getEndpointInstance();

      } catch (Exception e) {
          e.printStackTrace();
      }
      new EndpointsAsyncTask ().execute (pickUpLocation, userName);

    //Intent intent = new Intent(this,DriverSelected.class);
    //startActivity(intent);

  }


  private class AsyncGooglePlaceSearch extends
      AsyncTask<String, Void, String[]> {

    private String destination;
    private String pickUpLocation;
    private List<LatLng> latlngRoute = new ArrayList<> ();

    @Override
    protected String[] doInBackground (String... urls) {
      pickUpLocation = urls[0];
      destination = urls[1];
      return calculatePriceAndTime (pickUpLocation, destination);
    }

    @Override
    protected void onPostExecute (String[] estimates) {
      setUpDirection ();
      setUpDestination (dest_latitude, dest_longitude, destination);





        df1.setRoundingMode(RoundingMode.DOWN);

      mMapText.setText(getString(R.string.estimated_fare) + " " + df.format(estimatedFare)
              + "\n" + getString(R.string.accumulated_fare) + " " + df.format(estimatedFare)
              + "\n" + getString(R.string.max_time) + " " + df1.format(estimatedDuration) + " " + getString(R.string.minutes));
    }

    private String[] calculatePriceAndTime (String originTxt, String destinationTxt) {

      String[] estimates = new String[3];


      if (destinationTxt != null) {
        getLocation (destinationTxt);
        getDirection(destinationTxt);
        String origin = "origin=" + "start_latitude=" + latitude + "&start_longitude=" + longitude + "&";
        String destination = "end_latitude=" + dest_latitude + "&end_longitude=" + dest_longitude + "&";
        String url = mContext.UBER_PRICE_BASE_URL + origin + destination + "&server_token=" + getString (R.string.uber_api_key);

        try {

          String json = mContext.getRemoteJSON (url);
          JSONObject priceObject = new JSONObject (json);
          JSONArray allPrice = priceObject.getJSONArray ("prices");

          String[] estimateForProduct = new String[6];
          double lowestEstimateForProduct = 1000.0;
          int lowestPriceIndex = 0;

          for (int i = 0; i < allPrice.length (); i++) {
            JSONObject priceForEachProduct = (JSONObject) allPrice.get (i);
            System.out.println (priceForEachProduct.get ("estimate"));
            estimateForProduct[i] = priceForEachProduct.get ("estimate").toString ();
            if (Double.valueOf (priceForEachProduct.get ("low_estimate").toString ()) < lowestEstimateForProduct) {
              lowestEstimateForProduct = Double.valueOf (priceForEachProduct.get ("low_estimate").toString ());
              lowestPriceIndex = i;
            }
          }

          int lowestPriceDuration = Integer.valueOf (((JSONObject) allPrice.get (lowestPriceIndex)).get ("duration").toString ());
          String durationMin = String.valueOf (lowestPriceDuration / 60);
          String durationSec = String.valueOf (lowestPriceDuration % 60);

          estimates[0] = '$' + String.valueOf (lowestEstimateForProduct);
          estimates[1] = durationMin + " minutes " + durationSec + " seconds";
          estimates[2] = durationMin + " minutes " + durationSec + " seconds";

        } catch (org.json.JSONException jsone) {
          Log.i ("Hit the JSON error: ", jsone.toString ());
        }

      }


      return estimates;

    }

    private void getLocation (String destinationTxt) {

      String url = mContext.GEOCODE_BASE_URL + destinationTxt.replaceAll (" ", "+") + "&key=" + getString (R.string.google_maps_places_key);
      Document doc = getRemoteXML (url);
      doc.getDocumentElement ().normalize ();
      NodeList nl = doc.getElementsByTagName ("result");

      if (nl.getLength () != 0) {
        Node n = nl.item (0);
        Element e = (Element) n;
        dest_latitude = Double.valueOf (e.getElementsByTagName ("lat").item (0).getTextContent ());
        dest_longitude = Double.valueOf (e.getElementsByTagName ("lng").item (0).getTextContent ());

      }

    }


    private void getDirection (String destinationTxt) {
      String url = mContext.DIRECTION_BASE_URL + "origin=" + mContext.getLatitude() + "," + mContext.getLongitude() + "&destination=" + destinationTxt.replaceAll (" ", "+") + "&key=" + getString (R.string.google_maps_places_key);

      try {
        String json = mContext.getRemoteJSON (url);
        DirectionsJSONParser parser = new DirectionsJSONParser (json);

        estimatedDistance = parser.getDistance () * mContext.MeterToMile;
        estimatedDuration = parser.getDuration () / mContext.SecToMin;
        estimatedFare = PricingAlgorithm.calcMaximumPrice (estimatedDistance, estimatedDuration);

        directions = parser.getPolyline ();


      } catch (org.json.JSONException jsone) {
        Log.i ("Hit the JSON error: ", jsone.toString ());
      }


    }


    private Document getRemoteXML (String url) {
      //Log.i("******", url);
      try {
        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance ();
        DocumentBuilder db = dbf.newDocumentBuilder ();
        InputSource is = new InputSource (url);
        return db.parse (is);
      } catch (Exception e) {
        Log.i ("Hit the error: ", e.toString ());
        return null;
      }
    }


  }


  class EndpointsAsyncTask extends AsyncTask<String, Void, String[]> {

    private String pickUpLocation;
    private String userName;


    @Override
    protected String[] doInBackground (String... urls) {
      String[] taxiSearchingResult = new String[4];

        pickUpLocation = urls[0];
        userName = urls[1];
        try {
      myApiService = EndPointManager.getEndpointInstance ();
            Log.i ("end point created: ", myApiService.toString());
      MessageBean mb = myApiService.createNewRequest (userName, longitude, latitude, dest_longitude, dest_latitude,numOfRiders,estimatedDistance,estimatedDuration,estimatedFare).execute();
      request_id = mb.getRequestId();

      //change the 0 to slider
      UserBeanCollection taxis = queryTaxi (0);
      Log.i ("Taxi list: ", taxis.toString ());

            System.out.println("number of riders " + numOfRiders);
            taxiSearchingResult = taxiSearching(taxis, request_id, numOfRiders);

            myApiService.createMessage (Integer.parseInt (taxiSearchingResult[3]), mb.getMessage (), request_id).execute();
            System.out.println(taxiSearchingResult + " taxiSearchingResult");
        }
        catch (IOException ioe) {
            Log.i ("Hit the IO error: ", ioe.toString ());
        }
      return taxiSearchingResult;

    }

    @Override
    protected void onPostExecute (String[] result) {
      //Toast.makeText(context, result, Toast.LENGTH_LONG).show();
      setUpDriverLocation (Double.parseDouble (result[0]), Double.parseDouble (result[1]), result[2]);
    }

    private UserBeanCollection queryTaxi (int numOfRiders) {

      try {
        Log.i ("In queryTaxi : ", " executed");
        UserBeanCollection usc = myApiService.getAvailableDrivers (numOfRiders).execute ();
        Log.i ("Query result : ", usc.toString ());
        return usc;

      } catch (IOException e) {
        Log.i ("Query result error: ", e.getMessage ().toString ());
        return new UserBeanCollection ();
      }


    }

    private String[] taxiSearching (UserBeanCollection taxis, int request_id, int numOfRiders) throws IOException {
      double taxiLatitude = 0;
      double taxiLongitude = 0;
      double minTaxiLatitude = 0;
      double minTaxiLongitude = 0;
      String taxiPlaceTxt = "";
      int minDriverID = 0;
      int driverID = 0;
      int minDuration = 100000;
      int durationVal = 0;

      if (taxis.getItems () != null) {
        for (UserBean taxi : taxis.getItems ()) {
          taxiLatitude = taxi.getLatitude ();
          taxiLongitude = taxi.getLongitude ();
          driverID = taxi.getUserID ();

          LatLng currTaxi = new LatLng (taxiLatitude, taxiLongitude);

          String place_url = mContext.REV_GEOCODE_BASE_URL + taxiLatitude + "," + taxiLongitude + "&key=" + getString (R.string.google_maps_places_key);


          try {

            String json = mContext.getRemoteJSON (place_url);
            JSONObject routeObject = new JSONObject (json);
            JSONArray results = routeObject.getJSONArray ("results");
            JSONObject result = (JSONObject) results.get (0);
            taxiPlaceTxt = result.get ("formatted_address").toString ();


          } catch (org.json.JSONException jsone) {
            Log.i ("Hit the JSON error: ", jsone.toString ());
          }


          if (!taxiPlaceTxt.equals ("")) {

            String url = mContext.DIRECTION_BASE_URL + "origin=" + mContext.getLatitude() + "," + mContext.getLongitude() + "&destination=" + taxiLatitude + "," + taxiLongitude + "&key=" + getString (R.string.google_maps_places_key);


            try {

              String json = mContext.getRemoteJSON (url);
              JSONObject routeObject = new JSONObject (json);
              JSONArray routes = routeObject.getJSONArray ("routes");
              JSONObject route = (JSONObject) routes.get (0);
              JSONArray legs = route.getJSONArray ("legs");
              JSONObject leg = (JSONObject) legs.get (0);
              JSONObject duration = (JSONObject) leg.get ("duration");
              durationVal = Integer.valueOf (duration.get ("value").toString ());

            } catch (org.json.JSONException jsone) {
              Log.i ("Hit the JSON error: ", jsone.toString ());
            }

          }
          if (durationVal != 0 && durationVal < minDuration) {
            minDuration = durationVal;
            minTaxiLatitude = taxiLatitude;
            minTaxiLongitude = taxiLongitude;
            minDriverID = driverID;
          }

        }
      }

      //Perform taxi searching duties on backend
      myApiService.taxiSearching (request_id, minDriverID, numOfRiders).execute();
      return new String[] {String.valueOf (minTaxiLatitude), String.valueOf (minTaxiLongitude), String.valueOf (durationVal/60),String.valueOf(minDriverID)};


    }


  }


}

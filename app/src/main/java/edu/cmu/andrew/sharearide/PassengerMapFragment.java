package edu.cmu.andrew.sharearide;

import android.app.Fragment;
import android.content.Intent;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import java.net.MalformedURLException;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.util.EntityUtils;
import org.json.JSONArray;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import edu.cmu.andrew.sharearide.backend.shareARideApi.ShareARideApi;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.MessageBean;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.TripBean;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.UserBean;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.RequestBean;

import edu.cmu.andrew.sharearide.backend.shareARideApi.model.UserBeanCollection;
import edu.cmu.andrew.utilities.EndPointManager;
import edu.cmu.andrew.utilities.GPSTracker;

public class PassengerMapFragment extends Fragment {

  private GoogleMap mMap; // Might be null if Google Play services APK is not available.
  private static ShareARideApi myApiService = null;
  private double latitude;
  private double longitude;
  private double dest_latitude = 0;
  private double dest_longitude = 0;
  private RelativeLayout mLayout;
  private SARActivity mContext;
    private int numOfRiders;

  @Override
  public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mContext = (SARActivity) super.getActivity ();
    mLayout = (RelativeLayout) inflater.inflate (R.layout.activity_passenger_map, container, false);
    
    latitude = mContext.getLatitude ();
    longitude = mContext.getLongitude();
      setUpMapIfNeeded ();
        selectDriver();
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

  private void setUpDestination (double dest_latitude, double dest_longitude, String pickUpLocation, String destination) {
    Log.i ("add marker", "method executed");
    if (mMap != null) {
      Log.i ("map not null", "method executed");
      System.out.println ("In setUpDestionation" + dest_latitude + dest_longitude);
      mMap.moveCamera (CameraUpdateFactory.newLatLngZoom (new LatLng (dest_latitude, dest_longitude), 13));
      //Marker marker_origin = mMap.addMarker(new MarkerOptions()
      //      .position(new LatLng(latitude, longitude))
      //    .title("Your pickup location: " + pickUpLocation));
      Marker marker_destination = mMap.addMarker (new MarkerOptions ()
          .position (new LatLng (dest_latitude, dest_longitude))
          .title ("Your destination: " + destination));
    }
  }

  private void setUpDirection (List<LatLng> latlngRoute) {
    Log.i ("add polyline", "method executed");
    if (mMap != null) {
      Log.i ("map not null", "method executed");
      for (int i = 0; i < latlngRoute.size () - 1; i++) {
        Polyline line = mMap.addPolyline (new PolylineOptions ()
            .add (latlngRoute.get (i), latlngRoute.get (i + 1))
            .width (10)
            .color (Color.rgb (1, 169, 212)));
      }
    }
  }

  private void setUpDriverLocation (double driver_latitude, double driver_longitude, String minDurTxt) {
    Log.i ("add marker", "method executed");
    if (mMap != null) {
      Log.i ("map not null", "method executed");
      System.out.println ("In setUpDriverLocation" + driver_latitude + driver_longitude);
      mMap.moveCamera (CameraUpdateFactory.newLatLngZoom (new LatLng (driver_latitude, driver_longitude), 13));
      //Marker marker_origin = mMap.addMarker(new MarkerOptions()
      //      .position(new LatLng(latitude, longitude))
      //    .title("Your pickup location: " + pickUpLocation));
      Marker marker_destination = mMap.addMarker (new MarkerOptions ()
          .position (new LatLng (driver_latitude, driver_longitude))
          .icon (BitmapDescriptorFactory.defaultMarker (BitmapDescriptorFactory.HUE_GREEN))
          .title ("Your driver is " + minDurTxt + " away from you"));
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
      //ip.placeReady(place);
      //((TextView) mLayout.findViewById (R.id.my_location)).setText ("Lowest Price: " + estimates[0] + "\n" + "Time to Destination: " + estimates[1] + "\n" + "Time to Pickup: " + estimates[2]);
      //(mLayout.findViewById (R.id.requestMainLayout)).setVisibility (View.INVISIBLE);
      setUpDestination (dest_latitude, dest_longitude, pickUpLocation, destination);
      setUpDirection (latlngRoute);
    }

    private String[] calculatePriceAndTime (String originTxt, String destinationTxt) {

      String[] estimates = new String[3];


      if (destinationTxt != null) {

        //First get the destination coordinates and display on Google Map
        getLocation (destinationTxt);
        getDirection (originTxt, destinationTxt);
        Log.d ("coordinates", dest_latitude + " " + dest_longitude);
        //Then calculate price for the whole journey and the time for the uber driver to pick up the passenger
        String url = mContext.UBER_PRICE_BASE_URL + "start_latitude=" + latitude + "&start_longitude=" + longitude + "&end_latitude=" + dest_latitude + "&end_longitude=" + dest_longitude + "&server_token=" + getString (R.string.uber_api_key);
        Log.i ("URL for Uber API", url);
        //String url = "https://api.uber.com/v1/estimates/price?start_latitude=37.625732&start_longitude=-122.377807&end_latitude=37.785114&end_longitude=-122.406677&server_token=" + getString(R.string.uber_api_key);

        try {

          String json = mContext.getRemoteJSON (url);
          JSONObject priceObject = new JSONObject (json);
          //System.out.println(priceObject.get("prices"));
          JSONArray allPrice = priceObject.getJSONArray ("prices");

          String[] estimateForProduct = new String[6];
          //Double[] lowEstimateForProduct = new Double[6];
          double lowestEstimateForProduct = 1000.0;
          int lowestPriceIndex = 0;

          for (int i = 0; i < allPrice.length (); i++) {
            //System.out.println(allPrice.get(i));
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

          //Log.i("price info: ", priceObject.get("estimate").toString());
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
      //String url = "https://maps.googleapis.com/maps/api/geocode/xml?address=1600+Amphitheatre+Parkway,+Mountain+View,+CA&key="+ R.string.google_maps_key;
      Document doc = getRemoteXML (url);

      doc.getDocumentElement ().normalize ();

      NodeList nl = doc.getElementsByTagName ("result");

      if (nl.getLength () != 0) {
        Node n = nl.item (0);
        Element e = (Element) n;
        dest_latitude = Double.valueOf (e.getElementsByTagName ("lat").item (0).getTextContent ());
        dest_longitude = Double.valueOf (e.getElementsByTagName ("lng").item (0).getTextContent ());

      }

      System.out.println ("************" + dest_latitude + dest_longitude);

    }


    private void getDirection (String originTxt, String destinationTxt) {
      String url = mContext.DIRECTION_BASE_URL + "origin=" + originTxt.replaceAll (" ", "+") + "&destination=" + destinationTxt.replaceAll (" ", "+") + "&key=" + getString (R.string.google_maps_places_key);
      Log.i ("URL for Direction", url);

      LatLng origin = new LatLng (latitude, longitude);
      latlngRoute.add (origin);

      try {

        String json = mContext.getRemoteJSON (url);
        JSONObject routeObject = new JSONObject (json);
        //System.out.println(priceObject.get("prices"));
        JSONArray routes = routeObject.getJSONArray ("routes");
        //Each element of the routes array contains a single result from the specified origin and destination.
        JSONObject route = (JSONObject) routes.get (0);
        JSONArray legs = route.getJSONArray ("legs");
        //For routes that contain no waypoints, the route will consist of a single "leg
        JSONObject leg = (JSONObject) legs.get (0);
        JSONArray steps = leg.getJSONArray ("steps");


        double lat = 0;
        double lng = 0;

        for (int i = 0; i < steps.length (); i++) {
          System.out.println (steps.get (i));
          JSONObject step = (JSONObject) steps.get (i);
          //System.out.println(priceForEachProduct.get("estimate"));
          JSONObject start_location = (JSONObject) step.get ("start_location");
          lat = Double.valueOf (start_location.get ("lat").toString ());
          lng = Double.valueOf (start_location.get ("lng").toString ());
          LatLng latlngPoint = new LatLng (lat, lng);
          Log.i ("lat info for each step: ", start_location.get ("lat").toString ());
          latlngRoute.add (latlngPoint);
        }

        LatLng dest = new LatLng (dest_latitude, dest_longitude);
        latlngRoute.add (dest);

        //Log.i("price info: ", priceObject.get("estimate").toString());

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
      String[] taxiSearchingResult = new String[3];
      int request_id = 0;
        pickUpLocation = urls[0];
        userName = urls[1];
        try {
      myApiService = EndPointManager.getEndpointInstance ();
      MessageBean request = myApiService.createNewRequest (userName, latitude, longitude, dest_latitude, dest_longitude,numOfRiders).execute();
      request_id = request.getRequestId();

      //change the 0 to slider
      UserBeanCollection taxis = queryTaxi (0);
      Log.i ("Taxi list: ", taxis.toString ());



            taxiSearchingResult = taxiSearching(taxis, request_id, 0);
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
      int minDuration = 1000;
      int durationVal = 0;
      String minDurTxt = "";
      String durationTxt = "";

      if (taxis.getItems () != null) {
        for (UserBean taxi : taxis.getItems ()) {

          taxiLatitude = taxi.getLatitude ();

          taxiLongitude = taxi.getLongitude ();

          Log.i ("latlng: ", String.valueOf (taxiLatitude));
          Log.i ("latlng: ", String.valueOf (taxiLongitude));
          driverID = taxi.getUserID ();

          LatLng currTaxi = new LatLng (taxiLatitude, taxiLongitude);

          String place_url = mContext.REV_GEOCODE_BASE_URL + taxiLatitude + "," + taxiLongitude + "&key=" + getString (R.string.google_maps_places_key);

          Log.i ("url: ", place_url.toString ());

          try {

            String json = mContext.getRemoteJSON (place_url);
            JSONObject routeObject = new JSONObject (json);
            //System.out.println(priceObject.get("prices"));
            JSONArray results = routeObject.getJSONArray ("results");
            //Each element of the routes array contains a single result from the specified origin and destination.
            JSONObject result = (JSONObject) results.get (0);
            taxiPlaceTxt = result.get ("formatted_address").toString ();
            Log.i ("taxiPlaceTxt ", taxiPlaceTxt);


          } catch (org.json.JSONException jsone) {
            Log.i ("Hit the JSON error: ", jsone.toString ());
          }


          if (!taxiPlaceTxt.equals ("")) {

            String url = mContext.DIRECTION_BASE_URL + "origin=" + pickUpLocation.replaceAll (" ", "+") + "&destination=" + taxiPlaceTxt.replaceAll (" ", "+") + "&key=" + getString (R.string.google_maps_places_key);


            try {

              String json = mContext.getRemoteJSON (url);
              JSONObject routeObject = new JSONObject (json);
              //System.out.println(priceObject.get("prices"));
              JSONArray routes = routeObject.getJSONArray ("routes");
              //Each element of the routes array contains a single result from the specified origin and destination.
              JSONObject route = (JSONObject) routes.get (0);
              JSONArray legs = route.getJSONArray ("legs");
              //For routes that contain no waypoints, the route will consist of a single "leg
              JSONObject leg = (JSONObject) legs.get (0);
              JSONObject duration = (JSONObject) leg.get ("duration");
              durationVal = Integer.valueOf (duration.get ("value").toString ());
              durationTxt = duration.get ("text").toString ();

            } catch (org.json.JSONException jsone) {
              Log.i ("Hit the JSON error: ", jsone.toString ());
            }

          }
          if (durationVal != 0 && durationVal < minDuration) {
            minDuration = durationVal;
            minDurTxt = durationTxt;
            minTaxiLatitude = taxiLatitude;
            minTaxiLongitude = taxiLongitude;
            minDriverID = driverID;
          }

        }
      }

      //get tripId related to a driver from trip table
      TripBean trip = myApiService.getTrip(minDriverID).execute();
      int tripId = trip.getTripId();
      //get requestId related to a user
      //RequestBean request = myApiService.getRequest(userName).execute();
      //int requestId = request.getRequestId();
      //insert a new row into trip table
      myApiService.updateTripRequest(tripId, request_id);
      //update the request in request table
      myApiService.fulfillRequest(request_id);
      //update the trip in trip table
      myApiService.updateTrip(minDriverID, numOfRiders);

      Log.i ("minDriver Location ", String.valueOf (minTaxiLatitude) + String.valueOf (minTaxiLongitude) + minDurTxt);
      return new String[] {String.valueOf (minTaxiLatitude), String.valueOf (minTaxiLongitude), minDurTxt};


    }


  }


}

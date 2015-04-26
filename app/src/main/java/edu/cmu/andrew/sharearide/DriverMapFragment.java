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
import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import edu.cmu.andrew.sharearide.backend.shareARideApi.ShareARideApi;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.RequestBean;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.TripBean;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.UserBean;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.UserBeanCollection;
import edu.cmu.andrew.utilities.EndPointManager;
import edu.cmu.andrew.utilities.TripSegment;

public class DriverMapFragment extends Fragment {

  private GoogleMap mMap; // Might be null if Google Play services APK is not available.
  private double sLatitude;
  private double sLongitude;
  private RelativeLayout mLayout;
  private SARActivity mContext;
  private List<LatLng> directions;
  private List<TripSegment> trip;

  @Override
  public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mContext = (SARActivity) super.getActivity ();
    mLayout = (RelativeLayout) inflater.inflate (R.layout.activity_passenger_map, container, false);

    sLatitude = mContext.getLatitude ();
    sLongitude = mContext.getLongitude ();
    directions = new ArrayList<> ();
    setUpMapIfNeeded ();

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
    mMap.moveCamera (CameraUpdateFactory.newLatLngZoom (new LatLng (sLatitude, sLongitude), 13));
    getDirections (40, -80);
  }

  private void getNextDestination () {

  }

  private void getDirections (double dLatitude, double dLongitude) {
    Log.i ("add marker", "method executed");
    if (mMap != null) {
      Log.i ("map not null", "method executed");
      System.out.println ("In setUpDestination" + dLatitude + dLongitude);

      new DirectionsTask ().execute (String.valueOf (dLatitude), String.valueOf (dLongitude));

      mMap.addMarker (new MarkerOptions ()
          .position (new LatLng (sLatitude, sLongitude)));

      mMap.addMarker (new MarkerOptions ()
          .position (new LatLng (dLatitude, dLongitude)));

    }
  }

  class DirectionsTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground (String... data) {
      String origin = "origin=" + sLatitude + "," + sLongitude + "&";
      String destination = "destination=" + data[0] + "," + data[1] + "&";
      String key = "key=" + getString (R.string.google_maps_places_key);
      String url = mContext.DIRECTION_BASE_URL + origin + destination + key;

      return mContext.getRemoteJSON (url);

    }

    @Override
    protected void onPostExecute (String json) {
      try {
        JSONObject routeObject = new JSONObject (json);
        JSONArray routes = routeObject.getJSONArray ("routes");
        JSONObject route = (JSONObject) routes.get (0);
        System.out.println (route.toString ());
        JSONArray legs = route.getJSONArray ("legs");
        JSONObject leg = (JSONObject) legs.get (0);
        JSONArray steps = leg.getJSONArray ("steps");

        String polyline;

        for (int i = 0; i < steps.length (); i++) {
          JSONObject step = (JSONObject) steps.get (i);
          polyline = ((JSONObject) step.get ("polyline")).get ("points").toString ();

          directions.addAll (decodePoly (polyline));
        }
      } catch (JSONException jsone) {
        Log.i ("Hit the JSON error: ", jsone.toString ());
      }

      mMap.addPolyline (new PolylineOptions ()
          .addAll (directions)
          .width (10)
          .color (Color.rgb (1, 169, 212)));
    }

  }

  private List<LatLng> decodePoly (String encoded) {
    List<LatLng> poly = new ArrayList<LatLng> ();
    int index = 0, len = encoded.length ();
    int lat = 0, lng = 0;

    while (index < len) {
      int b, shift = 0, result = 0;
      do {
        b = encoded.charAt (index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);
      int dlat = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
      lat += dlat;

      shift = 0;
      result = 0;
      do {
        b = encoded.charAt (index++) - 63;
        result |= (b & 0x1f) << shift;
        shift += 5;
      } while (b >= 0x20);
      int dlng = ((result & 1) != 0 ? ~(result >> 1) : (result >> 1));
      lng += dlng;

      LatLng ll = new LatLng((((double) lat / 1E5)),(((double) lng / 1E5)));
      poly.add (ll);
    }

    return poly;
  }
}

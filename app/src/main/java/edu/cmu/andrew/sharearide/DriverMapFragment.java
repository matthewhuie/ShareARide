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

public class DriverMapFragment extends Fragment {

  private GoogleMap mMap; // Might be null if Google Play services APK is not available.
  private double sLatitude;
  private double sLongitude;
  private RelativeLayout mLayout;
  private SARActivity mContext;
  private List<LatLng> directions;

  @Override
  public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mContext = (SARActivity) super.getActivity ();
    mLayout = (RelativeLayout) inflater.inflate (R.layout.activity_passenger_map, container, false);

    sLatitude = mContext.getLatitude ();
    sLongitude = mContext.getLongitude ();
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
    getDirections (-80, 40.6975);
    mMap.addPolyline (new PolylineOptions ()
        .addAll (directions)
        .width (10)
        .color (Color.rgb (1, 169, 212)));
  }

  private void getDirections (double dLatitude, double dLongitude) {
    Log.i ("add marker", "method executed");
    if (mMap != null) {
      Log.i ("map not null", "method executed");
      System.out.println ("In setUpDestination" + dLatitude + dLongitude);

      String origin = "origin=" + sLatitude + "," + sLongitude + "&";
      String destination = "destination=" + dLatitude + "," + dLongitude + "&";
      String key = "key=" + getString (R.string.google_maps_places_key);

      String url = mContext.DIRECTION_BASE_URL + origin + destination + key;

      directions.add (new LatLng (sLatitude, sLongitude));
      try {
        String json = mContext.getRemoteJSON (url);
        JSONObject routeObject = new JSONObject (json);
        JSONArray routes = routeObject.getJSONArray ("routes");
        JSONObject route = (JSONObject) routes.get (0);
        JSONArray legs = route.getJSONArray ("legs");
        JSONObject leg = (JSONObject) legs.get (0);
        JSONArray steps = leg.getJSONArray ("steps");

        double latitude = 0;
        double longitude = 0;

        for (int i = 0; i < steps.length (); i++) {
          JSONObject step = (JSONObject) steps.get (i);
          latitude = Double.valueOf (step.get ("lat").toString ());
          longitude = Double.valueOf (step.get ("lng").toString ());
          directions.add (new LatLng (latitude, longitude));
        }
        directions.add (new LatLng (dLatitude, dLongitude));

      } catch (JSONException jsone) {
        Log.i ("Hit the JSON error: ", jsone.toString ());
      }

      mMap.addMarker (new MarkerOptions ()
          .position (new LatLng (dLatitude, dLongitude))
          .title ("Destination: " + destination));
    }
  }

}

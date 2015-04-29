package edu.cmu.andrew.utilities;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by matthewhuie on 15-04-29.
 */
public class DirectionsJSONParser {

  String json;
  JSONObject leg;
  JSONArray steps;
  LatLng source;
  LatLng destination;

  public DirectionsJSONParser (String json) throws JSONException {
    this (json, null, null);
  }

  public DirectionsJSONParser (String json, LatLng source, LatLng destination) throws JSONException {
    this.json = json;
    this.source = source;
    this.destination = destination;
    JSONObject routeObject = new JSONObject (json);
    JSONObject route = (JSONObject) routeObject.getJSONArray ("routes").get (0);
    leg = (JSONObject) route.getJSONArray ("legs").get (0);
    steps = leg.getJSONArray ("steps");
  }

  public int getDistance () throws JSONException {
    return Integer.parseInt (leg.getJSONObject ("distance").get ("value").toString ());
  }

  public int getDuration () throws JSONException {
    return Integer.parseInt (leg.getJSONObject ("duration").get ("value").toString ());
  }

  public List<LatLng> getPolyline () throws JSONException {
    String polyline;
    List <LatLng> directions = new ArrayList<> ();

    for (int i = 0; i < steps.length (); i++) {
      JSONObject step = (JSONObject) steps.get (i);
      polyline = ((JSONObject) step.get ("polyline")).get ("points").toString ();

      directions.addAll (decodePolyline (polyline));
    }

    return directions;
  }

  public LatLng getSource () {
    return source;
  }

  public LatLng getDestination () {
    return destination;
  }

  private List<LatLng> decodePolyline (String encoded) {
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

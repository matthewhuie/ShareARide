package edu.cmu.andrew.utilities;

import com.google.android.gms.maps.model.LatLng;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * DirectionsJSONParser is a helper class that parses Google Directions API output.
 */
public class DirectionsJSONParser {

  /**
   * The JSON from the API results
   */
  String json;

  /**
   * The result's first leg
   */
  JSONObject leg;

  /**
   * The result's first leg's steps
   */
  JSONArray steps;

  /**
   * The source location
   */
  LatLng source;

  /**
   * The destination location
   */
  LatLng destination;

  /**
   * Creates a DirectionJSONParser without specific location details
   *
   * @param json JSON from API results
   * @throws JSONException
   */
  public DirectionsJSONParser (String json) throws JSONException {
    this (json, null, null);
  }

  /**
   * Creates a DirectionJSONParser with location details
   *
   * @param json        JSON from API results
   * @param source      the source location
   * @param destination the destination location
   * @throws JSONException
   */
  public DirectionsJSONParser (String json, LatLng source, LatLng destination) throws JSONException {
    this.json = json;
    this.source = source;
    this.destination = destination;
    JSONObject routeObject = new JSONObject (json);
    JSONObject route = (JSONObject) routeObject.getJSONArray ("routes").get (0);
    leg = (JSONObject) route.getJSONArray ("legs").get (0);
    steps = leg.getJSONArray ("steps");
  }

  /**
   * Gets the distance of the result
   *
   * @return the distance of the result
   * @throws JSONException
   */
  public int getDistance () throws JSONException {
    return Integer.parseInt (leg.getJSONObject ("distance").get ("value").toString ());
  }

  /**
   * Gets the duration of the result
   *
   * @return the duration of the result
   * @throws JSONException
   */
  public int getDuration () throws JSONException {
    return Integer.parseInt (leg.getJSONObject ("duration").get ("value").toString ());
  }

  /**
   * Gets the polyline from the result
   *
   * @return the polyline from the result
   * @throws JSONException
   */
  public List<LatLng> getPolyline () throws JSONException {
    String polyline;
    List<LatLng> directions = new ArrayList<> ();

    /** Gets the encoded polyline from each step, then decodes them */
    for (int i = 0; i < steps.length (); i++) {
      JSONObject step = (JSONObject) steps.get (i);
      polyline = ((JSONObject) step.get ("polyline")).get ("points").toString ();

      /** Creates a list of all decoded polyline data */
      directions.addAll (decodePolyline (polyline));
    }

    return directions;
  }

  /**
   * Gets the source location
   *
   * @return the source location
   */
  public LatLng getSource () {
    return source;
  }

  /**
   * Gets the destination location
   *
   * @return the destination location
   */
  public LatLng getDestination () {
    return destination;
  }

  /**
   * Decodes an encoded polyline, as specified in developers.google.com/maps/documentation/utilities/polylinealgorithm
   *
   * @param encoded the encoded polyline
   * @return the decoded polyline
   */
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

      LatLng ll = new LatLng ((((double) lat / 1E5)), (((double) lng / 1E5)));
      poly.add (ll);
    }

    return poly;
  }
}

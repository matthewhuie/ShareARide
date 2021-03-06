package edu.cmu.andrew.sharearide;

import android.app.Fragment;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.SimpleAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.List;

import edu.cmu.andrew.utilities.CustomAutoCompleteTextView;
import edu.cmu.andrew.utilities.PlaceJSONParser;

/**
 * The class takes care of all the activities that need to
 * happen on the passenger input screen
 */

public class PassengerInputFragment extends Fragment {

  private SARActivity mContext;
  private ScrollView mLayout;
  private SeekBar mRiders;
  private TextView mRidersOutput;
  private Button mNext;
  private TextView mLocation;

    // the custom auto-complete tag which uses the places API
  AutoCompleteTextView atvPlaces;

    /**
     * this method contains all activities that need to
     * happen when the fragment loads
     * @param inflater
     * @param container
     * @param savedInstanceState
     * @return
     */
  @Override
  public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mContext = (SARActivity) super.getActivity ();
    mLayout = (ScrollView) inflater.inflate (R.layout.activity_passenger_input, container, false);

    mRiders = (SeekBar) mLayout.findViewById (R.id.ridersInput);
    mRidersOutput = (TextView) mLayout.findViewById (R.id.ridersOutput);
    mRidersOutput.setText ("1");

    mLocation = ((TextView) mLayout.findViewById (R.id.currentLocationText));
    //set the current location in a text view
    mLocation.setText (mContext.getLocationName ());

    mContext.moodType = (((Spinner) mLayout.findViewById(R.id.moodInput)).getSelectedItemPosition());

      //build the custom autocomplete tag and loads it with values
    buildAutoComplete ();

    //add listener to the seek bar where user can select the number of riders
    mRiders.setOnSeekBarChangeListener (new SeekBar.OnSeekBarChangeListener () {
      @Override
      public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser) {
        mRidersOutput.setText (String.valueOf (progress + 1));
      }

      @Override
      public void onStartTrackingTouch (SeekBar seekBar) {
      }

      @Override
      public void onStopTrackingTouch (SeekBar seekBar) {
      }
    });

    mNext = (Button) mLayout.findViewById (R.id.passInputNext);
      //add listener for the Request a ride button
    mNext.setOnClickListener (new View.OnClickListener () {
      @Override
      public void onClick (View v) {

         mContext.setDestination (atvPlaces.getText ().toString ());
          mContext.setNumOfRiders(mRiders.getProgress() + 1);
            mContext.nextFragment ();
      }
    });

    return mLayout;
  }

    /**
     * builds the custom auto-complete tag
     */
  private void buildAutoComplete () {
    atvPlaces = (CustomAutoCompleteTextView) mLayout.findViewById (R.id.whereToGoInput);
    //atvPlaces.setThreshold (1);

    atvPlaces.addTextChangedListener (new TextWatcher () {
        //execute the PlacesTask when the text is changed in the text box
      @Override
      public void onTextChanged (CharSequence s, int start, int before, int count) {
          new PlacesTask ().execute (s.toString ());
      }

      @Override
      public void beforeTextChanged (CharSequence s, int start, int count,
                                     int after) {
        // TODO Auto-generated method stub
      }

      @Override
      public void afterTextChanged (Editable s) {
        // TODO Auto-generated method stub
      }
    });
  }

  /**
   *  class to parse the Google Places in JSON format
   */
  private class ParserTask extends AsyncTask<String, Integer, List<HashMap<String, String>>> {

    JSONObject jObject;

    @Override
    protected List<HashMap<String, String>> doInBackground (String... jsonData) {

      List<HashMap<String, String>> places = null;

      PlaceJSONParser placeJsonParser = new PlaceJSONParser ();

      try {
        jObject = new JSONObject (jsonData[0]);

        // Getting the parsed data as a List construct
        places = placeJsonParser.parse (jObject);

      } catch (Exception e) {
        Log.d ("Exception", e.toString ());
      }
      return places;
    }

    @Override
    protected void onPostExecute (List<HashMap<String, String>> result) {

      String[] from = new String[] {"description"};
      int[] to = new int[] {android.R.id.text1};

      // Creating a SimpleAdapter for the AutoCompleteTextView
      SimpleAdapter adapter = new SimpleAdapter (mContext, result, android.R.layout.simple_list_item_1, from, to);

      // Setting the adapter
      atvPlaces.setAdapter (adapter);
      atvPlaces.showDropDown ();
    }
  }

  // Fetches all places from GooglePlaces AutoComplete Web Service
  private class PlacesTask extends AsyncTask<String, Void, String> {

    @Override
    protected String doInBackground (String... place) {
      // For storing data from web service
      String data = "";
      String input = "";

      try {
        input = "input=" + URLEncoder.encode (place[0], "utf-8");

      } catch (UnsupportedEncodingException e1) {
        e1.printStackTrace ();
      }

      // place type to be searched
      String types = "types=address";

      // Sensor enabled
      String sensor = "sensor=true";

      // Building the parameters to the web service
      String parameters = input + "&" + types + "&" + sensor + "&key=" + getString (R.string.google_maps_places_key);

      // Output format
      String output = "json";

      // Building the url to the web service
      String url = mContext.GOOGLE_AUTOCOMPLETE_URL + output + "?" + parameters;

      try {
        // Fetching the data from we service
        data = downloadUrl (url);
      } catch (Exception e) {
        Log.d ("Background Task", e.toString ());
      }
      System.out.println (data + " data");
      return data;
    }

    @Override
    protected void onPostExecute (String result) {
      super.onPostExecute (result);

      // Starting Parsing the JSON string returned by Web Service
      new ParserTask().execute (result);
    }
  }

  /**
   * A method to download json data from url
   */
  private String downloadUrl (String strUrl) throws IOException {
    String data = "";
    InputStream iStream = null;
    HttpURLConnection urlConnection = null;
    try {
      URL url = new URL (strUrl);

      // Creating an http connection to communicate with url
      urlConnection = (HttpURLConnection) url.openConnection ();

      // Connecting to url
      urlConnection.connect ();

      // Reading data from url
      iStream = urlConnection.getInputStream ();

      BufferedReader br = new BufferedReader (new InputStreamReader (iStream));

      StringBuffer sb = new StringBuffer ();

      String line = "";
      while ((line = br.readLine ()) != null) {
        sb.append (line);
      }

      data = sb.toString ();

      br.close ();

    } catch (Exception e) {
      Log.d ("Exception while downloading url", e.toString ());
    } finally {
      iStream.close ();
      urlConnection.disconnect ();
    }
    return data;
  }
}

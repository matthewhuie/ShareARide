package edu.cmu.andrew.sharearide;

import android.app.Activity;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.SeekBar;
import android.widget.TextView;


public class Passenger extends Activity {

  private SeekBar mRiders;
  private TextView mRidersOutput;

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate (savedInstanceState);
    setContentView (R.layout.activity_passenger);

    mRiders = (SeekBar) findViewById (R.id.ridersInput);
    mRidersOutput = (TextView) findViewById (R.id.ridersOutput);
    mRidersOutput.setText ("1");

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
  }

}

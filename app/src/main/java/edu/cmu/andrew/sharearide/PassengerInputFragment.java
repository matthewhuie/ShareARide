package edu.cmu.andrew.sharearide;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.SeekBar;
import android.widget.TextView;


public class PassengerInputFragment extends Fragment {

  private Passenger mContext;
  private ScrollView mLayout;
  private SeekBar mRiders;
  private TextView mRidersOutput;
  private Button mNext;

  @Override
  public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mContext = (Passenger) super.getActivity ();
    mLayout = (ScrollView) inflater.inflate (R.layout.activity_passenger_input, container, false);

    mRiders = (SeekBar) mLayout.findViewById (R.id.ridersInput);
    mRidersOutput = (TextView) mLayout.findViewById (R.id.ridersOutput);
    mRidersOutput.setText ("1");

    mRiders.setOnSeekBarChangeListener (new SeekBar.OnSeekBarChangeListener () {
      @Override
      public void onProgressChanged (SeekBar seekBar, int progress, boolean fromUser) {
        mRidersOutput.setText (String.valueOf (progress + 1));
      }

      @Override
      public void onStartTrackingTouch (SeekBar seekBar) {}

      @Override
      public void onStopTrackingTouch (SeekBar seekBar) {}
    });

    mNext = (Button) mLayout.findViewById (R.id.passInputNext);
    mNext.setOnClickListener (new View.OnClickListener () {
      @Override
      public void onClick (View v) {
        mContext.setFragment (new PassengerMapFragment ());
      }
    });

    return mLayout;
  }

  public void setLocation (String location) {
    ((TextView) mLayout.findViewById (R.id.currentLocationText)).setText (location);
  }
}

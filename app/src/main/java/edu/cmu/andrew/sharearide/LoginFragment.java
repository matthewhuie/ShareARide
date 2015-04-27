package edu.cmu.andrew.sharearide;

import android.app.Fragment;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import edu.cmu.andrew.sharearide.backend.shareARideApi.ShareARideApi;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.UserBean;
import edu.cmu.andrew.utilities.EndPointManager;


public class LoginFragment extends Fragment {

  private SARActivity mContext;
  private RelativeLayout mLayout;
  private String userType;
  private Button mButton;
  private String secret;
  private ShareARideApi apiInstance = null;
  private String message = "";

  @Override
  public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mContext = (SARActivity) super.getActivity ();
    mLayout = (RelativeLayout) inflater.inflate (R.layout.activity_login, container, false);
    mButton = (Button) mLayout.findViewById (R.id.button);

    mButton.setOnClickListener (new View.OnClickListener () {
      @Override
      public void onClick (View v) {
        mContext.setUsername (((EditText) mLayout.findViewById (R.id.username)).getText ().toString ());
        secret = computeMD5 (((EditText) mLayout.findViewById (R.id.password)).getText ().toString ());
        userType = (((Spinner) mLayout.findViewById (R.id.userType)).getSelectedItem ().toString ()).split (" ") [3];

        disableButton ();
        new LoginTask ().execute (mContext.getUsername (), secret,
            String.valueOf (mContext.getLatitude ()),
            String.valueOf (mContext.getLongitude ()), userType);
      }
    });

    resetButton ();

    return mLayout;
  }

  private void disableButton () {
    mButton.setText ("Please wait...");
    mButton.setBackgroundColor (getResources ().getColor (R.color.material_red_900));
    mButton.setClickable (false);
  }

  private void resetButton () {
    mButton.setText ("Log in");
    mButton.setBackgroundColor (getResources ().getColor (R.color.material_red_700));
    mButton.setClickable (true);
  }

  private String computeMD5 (String raw) {
    try {
      MessageDigest md = MessageDigest.getInstance ("MD5");
      md.update (raw.getBytes ());
      byte[] digest = md.digest ();
      StringBuffer sb = new StringBuffer ();
      for (byte b : digest) {
        sb.append (String.format ("%02x", b & 0xff));
      }
      return sb.toString ();
    } catch (NoSuchAlgorithmException nsae) {
      nsae.printStackTrace ();
    }
    return "";
  }

  class LoginTask extends AsyncTask<String, Void, UserBean> {

    @Override
    protected UserBean doInBackground (String... data) {
      UserBean user = new UserBean ();
      try {
        user = EndPointManager.getEndpointInstance ().userLogin (data[0], data[1],
            Double.parseDouble (data[2]),
            Double.parseDouble (data[3]), data[4]).execute ();
      } catch (IOException e) {
        e.printStackTrace ();
      }
      return user;

    }

    @Override
    protected void onPostExecute (UserBean result) {
      if (result != null) {
        message = "Authentication successful!";
        if (userType.equals ("Passenger")) {
          mContext.initPassenger ();
        } else {
          mContext.initDriver ();
        }

        mContext.setUserID (result.getUserID ());

        mContext.nextFragment ();
      } else {
        message = "Invalid username/password!";
        resetButton ();
      }
      Toast.makeText (mContext, message, Toast.LENGTH_SHORT).show ();
    }

  }

}

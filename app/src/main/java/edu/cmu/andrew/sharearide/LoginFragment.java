package edu.cmu.andrew.sharearide;

import android.app.Fragment;
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

import edu.cmu.andrew.sharearide.backend.shareARideApi.model.UserBean;
import edu.cmu.andrew.utilities.EndPointManager;

/**
 * LoginFragment is the fragment that handles the login activities for users.
 * It is implemented as a fragment and is called when the application is opened.
 */
public class LoginFragment extends Fragment {

  /**
   * The main activity
   */
  private SARActivity mContext;

  /**
   * The layout of this fragment
   */
  private RelativeLayout mLayout;

  /**
   * The submit button of this fragment
   */
  private Button mButton;


  /**
   * The user type of this user
   */
  private int userType;

  /**
   * The user's secret
   */
  private String secret;

  /**
   * The success message of the login
   */
  private String message = "";

  /**
   * This Fragment's onCreateView method
   *
   * @param inflater           the LayoutInflater
   * @param container          the ViewGroup
   * @param savedInstanceState the Bundle
   * @return the created view
   */
  @Override
  public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mContext = (SARActivity) super.getActivity ();
    mLayout = (RelativeLayout) inflater.inflate (R.layout.activity_login, container, false);
    mButton = (Button) mLayout.findViewById (R.id.button);

    /** The button will perform login operations when clicked */
    mButton.setOnClickListener (new View.OnClickListener () {
      @Override
      public void onClick (View v) {
        mContext.setUsername (((EditText) mLayout.findViewById (R.id.username)).getText ().toString ());
        secret = computeMD5 (((EditText) mLayout.findViewById (R.id.password)).getText ().toString ());
        userType = (((Spinner) mLayout.findViewById (R.id.userType)).getSelectedItemPosition ());

        String strUserType = userType == 0 ? "Passenger" : "Driver";

        /** Disables the button after it is clicked once */
        disableButton ();

        /** Performs a login task on the backend */
        new LoginTask ().execute (mContext.getUsername (), secret,
            String.valueOf (mContext.getLatitude ()),
            String.valueOf (mContext.getLongitude ()), strUserType);
      }
    });

    /** Resets the button once login has been completed */
    resetButton ();

    return mLayout;
  }

  /**
   * Disables the submit button by making it unclickable and changing its text
   */
  private void disableButton () {
    mButton.setText (R.string.wait_message);
    mButton.setBackgroundColor (getResources ().getColor (R.color.material_red_900));
    mButton.setClickable (false);
  }

  /**
   * Resets the button by restoring its text and making it clickable
   */
  private void resetButton () {
    mButton.setText (R.string.login);
    mButton.setBackgroundColor (getResources ().getColor (R.color.material_red_700));
    mButton.setClickable (true);
  }

  /**
   * Computes the MD5 hash of a given raw string
   *
   * @param raw the given raw string
   * @return the MD5 hash of the given raw string
   */
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

  /**
   * The AsyncTask that handles the user login, accepting username, secret, location, and user type as inputs
   */
  class LoginTask extends AsyncTask<String, Void, UserBean> {

    /**
     * Contacts the backend to request authentication
     *
     * @param data the user's username, secret, location, and user type
     * @return the UserBean associated with the user
     */
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

    /**
     * Initiates the correct Fragments for an authenticated user
     *
     * @param result the associated UserBean from the login process
     */
    @Override
    protected void onPostExecute (UserBean result) {
      if (result != null) {
        message = "Authentication successful!";
        if (userType == 0) {
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

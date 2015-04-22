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
  private Spinner mUserType;
  private Button mButton;
  private String username;
  private String secret;
  private ShareARideApi apiInstance = null;
  private String message = "";

  @Override
  public View onCreateView (LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
    mContext = (SARActivity) super.getActivity ();
    mLayout = (RelativeLayout) inflater.inflate (R.layout.activity_login, container, false);

    mUserType = (Spinner) mLayout.findViewById (R.id.userType);
    mButton = (Button) mLayout.findViewById (R.id.button);

    mButton.setOnClickListener (new View.OnClickListener () {
      @Override
      public void onClick (View v) {
        username = ((EditText) mLayout.findViewById (R.id.username)).getText ().toString ();
        secret = computeMD5 (((EditText) mLayout.findViewById (R.id.password)).getText ().toString ());

        new LoginTask ().execute (username, secret);
      }
    });
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
        if (apiInstance == null) {  // Only do this once
          apiInstance = EndPointManager.getEndpointInstance ();
        }
        user = apiInstance.userLogin (data[0], data[1]).execute ();
      } catch (IOException e) {
        e.printStackTrace ();
      }
      return user;

    }

    @Override
    protected void onPostExecute (UserBean result) {
      //Toast.makeText(context, result, Toast.LENGTH_LONG).show();
      //  System.out.println(result[0]);
      //System.out.println(result[1]);
      if (result != null) {
        message = "Authentication successful!";
        Intent loginIntent = new Intent (mContext, (result.getUserType ().equals ("Passenger") ? Passenger.class : DriverHome.class));
        loginIntent.putExtra ("username", username);
        startActivity (loginIntent);
      } else {
        message = "Invalid username/password!";
      }
      Toast.makeText (mContext, message, Toast.LENGTH_SHORT).show ();
    }

  }

}

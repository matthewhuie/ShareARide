package edu.cmu.andrew.sharearide;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import edu.cmu.andrew.sharearide.backend.shareARideApi.ShareARideApi;
import edu.cmu.andrew.sharearide.backend.shareARideApi.model.UserBean;
import edu.cmu.andrew.utilities.EndPointManager;


public class Login extends Activity {

  private Spinner mUserType;
  private Button mButton;
  private String username;
  private String password;
  private ShareARideApi apiInstance = null;
  private EndPointManager managerInstance = null;
  private String message = "";

  @Override
  protected void onCreate (Bundle savedInstanceState) {
    super.onCreate (savedInstanceState);
    setContentView (R.layout.activity_login);

    mUserType = (Spinner) findViewById (R.id.userType);
    mButton = (Button) findViewById (R.id.button);

    mButton.setOnClickListener (new View.OnClickListener () {
      @Override
      public void onClick (View v) {
        authenticate ();
        if (String.valueOf (mUserType.getSelectedItem ()).endsWith ("Passenger")) {
          if (success) {
            Toast t = Toast.makeText (ct, message, Toast.LENGTH_SHORT);
            t.show ();
            Intent loginIntent = new Intent (Login.this, Passenger.class);
            loginIntent.putExtra ("username", username);
            startActivity (loginIntent);
          } else {
            Toast t = Toast.makeText (Login.this, message, Toast.LENGTH_LONG);
            t.show ();
          }
        } else {
          if (success) {
            Intent loginIntent = new Intent (Login.this, DriverHome.class);
            loginIntent.putExtra ("username", username);
            startActivity (loginIntent);
          } else {
            Toast t = Toast.makeText (Login.this, message, Toast.LENGTH_LONG);
            t.show ();
          }
        }
      }
    });
  }

  private void authenticate () {

    //try {
    username = ((EditText) findViewById (R.id.username)).getText ().toString ();
    password = ((EditText) findViewById (R.id.password)).getText ().toString ();

    new LoginTask ().execute (username, computeMD5 (password));


    //   UserBean ub = apiInstance.getUser(userName).execute();

    //if(success){

    //}
    //  }

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
    protected UserBean doInBackground (String... urls) {
      UserBean user = new UserBean ();
      try {
        if (apiInstance == null) {  // Only do this once

          // managerInstance = new EndPointManager();
          apiInstance = EndPointManager.getEndpointInstance ();

        }

        //  System.out.println(urls[0] + "usrls");
        user = apiInstance.userLogin (urls[0], urls[1]).execute ();
      } catch (IOException e) {
        e.printStackTrace ();
      }
      // Log.i("Taxi list: ", taxis.toString());

      return user;

    }

    @Override
    protected void onPostExecute (UserBean result) {
      //Toast.makeText(context, result, Toast.LENGTH_LONG).show();
      //  System.out.println(result[0]);
      //System.out.println(result[1]);
      if (result != null) {
        message = "Authentication successful!";
        Intent loginIntent = new Intent (Login.this, (result.getUserType ().equals ("Passenger") ? Passenger.class : DriverHome.class));
        loginIntent.putExtra ("username", username);
        startActivity (loginIntent);
      } else {
        message = "Invalid username/password!";
      }
      Toast.makeText (Login.this, message, Toast.LENGTH_SHORT).show ();
    }

  }

}

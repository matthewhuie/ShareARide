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
    private boolean success = false;
    private String message = "";
    private Context ct;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserType = (Spinner) findViewById (R.id.userType);
        mButton = (Button) findViewById (R.id.button);

      mButton.setOnClickListener (new View.OnClickListener () {
        @Override
        public void onClick (View v) {
            ct = getApplicationContext();
            authenticate ();
            if (String.valueOf (mUserType.getSelectedItem ()).endsWith ("Passenger")) {
                if(success) {
                    Toast t = Toast.makeText(ct,message,Toast.LENGTH_SHORT);
                    t.show();
                    Intent loginIntent = new Intent(Login.this, Passenger.class);
                    loginIntent.putExtra("username", username);
                    startActivity(loginIntent);
                }else{
                    Toast t = Toast.makeText(Login.this,message,Toast.LENGTH_LONG);
                    t.show();
                }
            } else {
                if(success) {
                    Intent loginIntent = new Intent(Login.this, DriverHome.class);
                    loginIntent.putExtra("username", username);
                    startActivity(loginIntent);
                }else{
                    Toast t = Toast.makeText(Login.this,message,Toast.LENGTH_LONG);
                    t.show();
                }
            }
        }
      });
    }

    private void authenticate (){

        //try {
        username = ((EditText)findViewById(R.id.username)).getText().toString();
        password = ((EditText)findViewById(R.id.password)).getText().toString();

            new EndpointsAsyncTask().execute(username);


     //   UserBean ub = apiInstance.getUser(userName).execute();

            //if(success){

            //}
      //  }

    }

  private String computeMD5 (String raw) {
    try {
      MessageDigest md = MessageDigest.getInstance ("MD5");
      md.update (raw.getBytes ());
      byte[] digest = md.digest();
      StringBuffer sb = new StringBuffer();
      for (byte b : digest) {
        sb.append(String.format("%02x", b & 0xff));
      }
      return sb.toString ();
    } catch (NoSuchAlgorithmException nsae) {
      nsae.printStackTrace ();
    }
    return "";
  }

    class EndpointsAsyncTask extends AsyncTask<String, Void, String[]> {

         @Override
        protected String[] doInBackground(String... urls) {
             UserBean user = new UserBean();
             try {
            if(apiInstance == null) {  // Only do this once

                managerInstance = new EndPointManager();
                apiInstance = managerInstance.getEndpointInstance();

            }

               //  System.out.println(urls[0] + "usrls");
              user = apiInstance.getUser(urls[0]).execute();
             } catch (IOException e) {
                 e.printStackTrace();
             }
            // Log.i("Taxi list: ", taxis.toString());

            return new String[]{user.getSecret(),user.getUserName()};

        }

        @Override
        protected void onPostExecute(String[] result) {
            //Toast.makeText(context, result, Toast.LENGTH_LONG).show();
            System.out.println(result[0]);
            System.out.println(result[1]);
            if(result!=null && result[0]!=null){
                if(result[0].equals(computeMD5(password))){
                    success = true;
                    message = "Authentication Successful";
                }else{
                    success = false;
                    message = "Incorrect Username/Password combination";
                }
            }else{
                success = false;
                message = "Username not found";
            }
        }



    }

}

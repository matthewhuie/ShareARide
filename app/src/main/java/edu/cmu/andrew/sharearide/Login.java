package edu.cmu.andrew.sharearide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Login extends Activity {

    private Spinner mUserType;
    private Button mButton;
    private String userName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserType = (Spinner) findViewById (R.id.userType);
        mButton = (Button) findViewById (R.id.button);
        userName = ((EditText)findViewById(R.id.userName)).getText().toString();
      mButton.setOnClickListener (new View.OnClickListener () {
        @Override
        public void onClick (View v) {
            if (String.valueOf (mUserType.getSelectedItem ()).endsWith ("Passenger")) {
                Intent loginIntent = new Intent(Login.this,PassengerHome.class);
                loginIntent.putExtra("userName",userName);
                startActivity(loginIntent);
            } else {
                Intent loginIntent = new Intent(Login.this,DriverHome.class);
                loginIntent.putExtra("userName",userName);
                startActivity(loginIntent);
            }
        }
      });
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
}

package edu.cmu.andrew.sharearide;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Spinner;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


public class Login extends Activity {

    private Spinner mUserType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        mUserType = (Spinner) findViewById (R.id.userType);
    }

    public void login(View view){
      if (String.valueOf (mUserType.getSelectedItem ()).endsWith ("Passenger"))
        startActivity (new Intent(this,PassengerHome.class));
      else
        startActivity (new Intent(this,DriverHome.class));
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

package edu.cmu.andrew.sharearide;

import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.RadioButton;


public class Login extends ActionBarActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
    }

    public void login(View view){
      RadioButton passRadio = (RadioButton) findViewById (R.id.passRadio);
      if (passRadio.isChecked ())
        startActivity (new Intent(this,PassengerHome.class));
      else
        startActivity (new Intent(this,DriverHome.class));
    }
}

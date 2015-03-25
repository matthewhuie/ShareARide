package edu.cmu.andrew.sharearide;

import android.app.Activity;
import android.content.Intent;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;


public class DriverPassenger extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_driver_passenger);
    }

    public void login (View view) {
        Intent intent = new Intent(this,MapsActivity.class);
        startActivity(intent);

    }
}

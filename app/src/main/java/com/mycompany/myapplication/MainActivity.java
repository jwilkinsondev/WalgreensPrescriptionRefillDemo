package com.mycompany.myapplication;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public final static String EXTRA_MESSAGE = "com.mycompany.myfirstapp.MESSAGE";
    public final static String EXTRA_LATITUDE = "com.mycompany.myfirstapp.LATITUDE";
    public final static String EXTRA_LONGITUDE = "com.mycompany.myfirstapp.LONGITUDE";

    private final static int MY_PERMISSION_ACCESS_COARSE_LOCATION = 1234;
    private static final String TAG = MainActivity.class.getSimpleName();
    public LocationService locationService = null;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // check for permissions
        if ( ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED ) {
            ActivityCompat.requestPermissions(this, new String[]{android.Manifest.permission.ACCESS_COARSE_LOCATION},
                   MY_PERMISSION_ACCESS_COARSE_LOCATION);
        }
        else {
            // make sure to start locationService
            this.locationService = LocationService.getLocationManager(getApplicationContext());
        }
        setContentView(R.layout.activity_main);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults){
        Log.d(TAG, String.format("requestCode = %d", requestCode));
        if (requestCode == MY_PERMISSION_ACCESS_COARSE_LOCATION){
            this.locationService = LocationService.getLocationManager(getApplicationContext());
        }
    }


    /** Launch web view */
    public void launchWebView(View view) {

        Intent intent = new Intent(this, WebViewActivity.class);
        if(this.locationService == null || (this.locationService.latitude == 0 && this.locationService.longitude == 0)){
            // make sure we have a valid location before trying to launch the web view
            int duration = Toast.LENGTH_SHORT;
            Toast toast = Toast.makeText(this.getApplicationContext(), "Don't have a valid location", duration);
            return;
        }
        else {
            // pass in the coordinates and launch web view activity
            intent.putExtra(EXTRA_LATITUDE, this.locationService.latitude);
            intent.putExtra(EXTRA_LONGITUDE, this.locationService.longitude);
            startActivity(intent);
        }
    }
}

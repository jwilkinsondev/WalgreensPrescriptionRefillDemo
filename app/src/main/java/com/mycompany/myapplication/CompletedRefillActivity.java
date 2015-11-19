package com.mycompany.myapplication;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;

public class CompletedRefillActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        // since the webpage from Walgreens is having issues, for now, handle the responses by routing back to the main activity
        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        startActivity(mainActivityIntent);

        setContentView(R.layout.activity_completed_refill);
    }
}

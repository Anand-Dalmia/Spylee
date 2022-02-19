package com.spylee;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationManager;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.Toast;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;
import com.spylee.model.Guardian;
import com.spylee.model.Person;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class BackgroundActivity extends AppCompatActivity {
    DatabaseReference databaseReference;
    public static final String MyPREFERENCES = "MyPrefs" ;
    SharedPreferences sharedpreferences;
    FusedLocationProviderClient fusedLocationClient;
    Intent intentThatCalled;
    public double latitude;
    public double longitude;
    public LocationManager locationManager;
    public Criteria criteria;
    public String bestProvider;
    Guardian guardian;
    List<Guardian> guardians;

    @Override
    protected void onPause() {
        super.onPause();
        databaseReference = DbRef.getDbRef().child("persons");
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        if (sharedpreferences.getString("id", null) != null) {
            databaseReference.child(sharedpreferences.getString("id", null)).addValueEventListener(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                    Person person = dataSnapshot.getValue(Person.class);
                   assert person != null;
                    if (person.getStatus().equals("I Lost My Mobile")) {
                        try {
                          //  getLocation2();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }

                    } else {

                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {

                }
            });
        }
        }
        void getLocation2 () throws Exception {

        }

}

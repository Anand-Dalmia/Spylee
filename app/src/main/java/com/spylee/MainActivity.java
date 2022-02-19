package com.spylee;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Criteria;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.nfc.Tag;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.tasks.OnSuccessListener;
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

public class MainActivity extends BackgroundActivity implements LocationListener {
    FusedLocationProviderClient fusedLocationClient;
    Intent intentThatCalled;
    public double latitude;
    public double longitude;
    public LocationManager locationManager;
    public Criteria criteria;
    public String bestProvider;
    private static final int REQUEST_READ_PHONE_STATE = 1;
    private final int MY_PERMISSIONS_REQUEST_SEND_SMS = 1;
    private final String SENT = "SMS_SENT";
    private final String DELIVERED = "SMS_DELIVERED";
    PendingIntent sentPI, deliveredPI;
    DatabaseReference databaseReference;
    public static final String MyPREFERENCES = "MyPrefs";
    SharedPreferences sharedpreferences;
    List<Guardian> guardians;
    private static final int PERMISSION_SEND_SMS = 123;
    ArrayList<Long> phoneNumbers = new ArrayList<Long>();
    Button register,login,help;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        register=findViewById(R.id.register);
        login=findViewById(R.id.login);
        help=findViewById(R.id.help);
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        // SimpleMail.sendEmail("dhaatrisolutions2@gmail.com","Hii", "Hello");
        databaseReference = DbRef.getDbRef().child("guardians");
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        guardians = new ArrayList<Guardian>();
        requestSmsPermission();
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
        int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE);
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) MainActivity.this, new String[]{Manifest.permission.SEND_SMS},
                    MY_PERMISSIONS_REQUEST_SEND_SMS);
        } else if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions((Activity) MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
               } else {


        }
        try {
            getLocation("none");
        } catch (Exception e) {

        }
        startService(new Intent(MainActivity.this, MyFireBaseService.class));
        register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,RegisterActivity.class));
            }
        });
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(MainActivity.this,LoginActivity.class));
            }
        });
        help.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    help();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onBackPressed() {

    }

    void login(View view) {
        Intent intent = new Intent(getApplicationContext(), LoginActivity.class);
        startActivity(intent);
    }

    void register(View view) {
        Intent intent = new Intent(getApplicationContext(), RegisterActivity.class);
        startActivity(intent);
    }

    void help() throws Exception {
        getLocation("help");
    }

    @Override
    public void onLocationChanged(Location location) {
        locationManager.removeUpdates(this);

        //open the map:
        latitude = location.getLatitude();
        longitude = location.getLongitude();


    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }

    public static boolean isLocationEnabled(Context context) {
        //...............
        return true;
    }

    @SuppressLint("MissingPermission")
    protected void getLocation(final String status) throws IOException {
        try {
            if (isLocationEnabled(MainActivity.this)) {
                locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
                criteria = new Criteria();
                bestProvider = String.valueOf(locationManager.getBestProvider(criteria, true)).toString();
                //You can still do this if you like, you might get lucky:

                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1);
                if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                    // TODO: Consider calling
                    //    ActivityCompat#requestPermissions
                    // here to request the missing permissions, and then overriding
                    //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                    //                                          int[] grantResults)
                    // to handle the case where the user grants the permission. See the documentation
                    // for ActivityCompat#requestPermissions for more details.

                }


                Location location = locationManager.getLastKnownLocation(bestProvider);
                if (location != null) {
                    Log.e("TAG", "GPS is on");
                    latitude = location.getLatitude();
                    longitude = location.getLongitude();
                    Geocoder geocoder;
                    List<Address> addresses;
                    geocoder = new Geocoder(this, Locale.getDefault());

                    addresses = geocoder.getFromLocation(latitude, longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                    final String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                    String city = addresses.get(0).getLocality();
                    String state = addresses.get(0).getAdminArea();
                    String country = addresses.get(0).getCountryName();
                    String postalCode = addresses.get(0).getPostalCode();
                    String knownName = addresses.get(0).getFeatureName();

                    if (sharedpreferences.getString("id", null) == null) {
                        Toast.makeText(MainActivity.this, "Please Login", Toast.LENGTH_SHORT).show();
                    } else {
                        databaseReference.orderByChild("personId").equalTo(sharedpreferences.getString("id", null)).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                                guardians = new ArrayList<Guardian>();
                                for (DataSnapshot dataSnapshot1 : dataSnapshot.getChildren()) {
                                    Guardian guardian = dataSnapshot1.getValue(Guardian.class);
                                    guardians.add(guardian);
                                    String subject = "";
                                    String message = "";
                                    if (status.equals("help")) {
                                        subject = sharedpreferences.getString("name", null) + " is In Emergency";
                                        message = sharedpreferences.getString("name", null) + " is In Emergency Please Find her/him . " + sharedpreferences.getString("name", null) + " is in " + address + ". For Perfect Location Find in Google Maps with Latitude " + latitude + " and Longitudes " + longitude;
                                    } else if (status.equals("lost")) {
                                        subject = sharedpreferences.getString("name", null) + "  Device is Traced";
                                        message = sharedpreferences.getString("name", null) + " Device is at " + address + ". For Perfect Location Find in Google Maps with Latitude " + latitude + " and Longitudes " + longitude;

                                    } else {
                                        return;
                                    }
                                    SimpleMail.sendEmail(guardian.getEmail(), subject, message);
                                    SendMail sendMail = new SendMail(MainActivity.this,guardian.getEmail(),subject,message);
                                    sendMail.execute();
                                    int permissionCheck = ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_PHONE_STATE);
                                    if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.SEND_SMS)
                                            != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions((Activity) MainActivity.this, new String[]{Manifest.permission.SEND_SMS},
                                                MY_PERMISSIONS_REQUEST_SEND_SMS);
                                    } else if (permissionCheck != PackageManager.PERMISSION_GRANTED) {
                                        ActivityCompat.requestPermissions((Activity) MainActivity.this, new String[]{Manifest.permission.READ_PHONE_STATE}, REQUEST_READ_PHONE_STATE);
                                        SmsManager sms = SmsManager.getDefault();
                                        String msg = "welcome hero";
                                        sms.sendTextMessage("9666665154", null, msg, sentPI, deliveredPI);
                                        callPhoneNumber();
                                    } else {
                                        SmsManager sms = SmsManager.getDefault();
                                        String msg = sharedpreferences.getString("name", null)+" is in Emergency  at " + address;
                                        sms.sendTextMessage(guardian.getPhone(), null, msg, sentPI, deliveredPI);
                                        callPhoneNumber();
                                    }
                                }


                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError databaseError) {

                            }
                        });
                    }


                } else {
                    //This is what you need:

                    locationManager.requestLocationUpdates(bestProvider, 1000, 0, this);
                }
            } else {
                //prompt user to enable location....
                //.................
            }
        } catch (Exception e) {
            Log.d("errrrr", e.toString());
        }
    }

    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 1: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
    }

    public boolean checkLocationPermission() {
        String permission = "android.permission.ACCESS_FINE_LOCATION";
        int res = this.checkCallingOrSelfPermission(permission);
        return (res == PackageManager.PERMISSION_GRANTED);
    }

    private void requestSmsPermission() {

        // check permission is given
        if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.READ_CONTACTS) != PackageManager.PERMISSION_GRANTED) {
            // request permission (see result in onRequestPermissionsResult() method)
            ActivityCompat.requestPermissions(MainActivity.this,
                    new String[]{Manifest.permission.SEND_SMS},
                    PERMISSION_SEND_SMS);
        } else {

        }


    }

    String a = "hii";

    public void makeCall() {

        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
                Intent callIntent = new Intent(Intent.ACTION_CALL);
                try {
                    if (Build.VERSION.SDK_INT > 22) {
                        if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                            // TODO: Consider calling

                            ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 101);

                            return;
                        }
                        callIntent.setData(Uri.parse("tel:" + "9666665154"));
                        startActivity(callIntent);

                    } else {
                        callIntent.setData(Uri.parse("tel:" + "9666665154"));
                        startActivity(callIntent);
                    }
                } catch (Exception ex) {
                    ex.printStackTrace();
                    Log.e("call error", ex.toString());
                }

                try {
                    Thread.sleep(60000);
                } catch (Exception e) {
                    Log.e("call error", e.toString());
                }

            }
        });


    }

    public void callPhoneNumber() {
        runOnUiThread(new Runnable() {
            @Override
            public synchronized void run() {
        for (int i = 0; i < guardians.size(); i++) {

            try {
                if (Build.VERSION.SDK_INT > 22) {
                    if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
                        // TODO: Consider calling

                        ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.CALL_PHONE}, 101);

                        return;
                    }

                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" + guardians.get(i).getPhone()));
                    startActivity(callIntent);

                } else {
                    Intent callIntent = new Intent(Intent.ACTION_CALL);
                    callIntent.setData(Uri.parse("tel:" +guardians.get(i).getPhone()));
                    startActivity(callIntent);
                }
            } catch (Exception ex) {
                ex.printStackTrace();
            } try{
                Thread.sleep(60000);
            }catch (Exception e){
                Log.e("call error",e.toString());
            }
        }
    }
});}}
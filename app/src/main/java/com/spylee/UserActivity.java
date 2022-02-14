package com.spylee;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentManager;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DatabaseReference;

public class UserActivity extends BackgroundActivity
        implements NavigationView.OnNavigationItemSelectedListener {
    TextView nav_header_name,nav_header_phone,nav_header_email;
    SharedPreferences sharedpreferences;


    public static final String MyPREFERENCES = "MyPrefs" ;
    FragmentManager fragmentManager;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        sharedpreferences = getSharedPreferences(MyPREFERENCES, Context.MODE_PRIVATE);
        fragmentManager =getSupportFragmentManager();
        fragmentManager.beginTransaction().replace(R.id.user_container,new UserHome()).commit();


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        View header=navigationView.getHeaderView(0);
        nav_header_name = (TextView)header.findViewById(R.id.nav_header_name);
        nav_header_phone = (TextView)header.findViewById(R.id.nav_header_phone);
        nav_header_email = (TextView)header.findViewById(R.id.nav_header_email);
        nav_header_name.setText(sharedpreferences.getString("name",null));
        nav_header_phone.setText(sharedpreferences.getString("phone",null));
        nav_header_email.setText(sharedpreferences.getString("email",null));

    }

    @Override
    public void onBackPressed() {

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.user, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        FragmentManager fragmentManager = getSupportFragmentManager();
        int id = item.getItemId();

        if (id == R.id.nav_Home) {
            fragmentManager.beginTransaction().replace(R.id.user_container,new UserHome()).commit();

        } else if (id == R.id.nav_add_guardian) {

            fragmentManager.beginTransaction().replace(R.id.user_container,new AddGuardian()).commit();

        } else if (id == R.id.nav_view_guardian) {
            fragmentManager.beginTransaction().replace(R.id.user_container,new ViewGuardian()).commit();

        } else if (id == R.id.nav_logout) {
            SharedPreferences.Editor editor = sharedpreferences.edit();
            editor.clear();
            editor.commit();
            finish();

            Intent intent= new Intent(UserActivity.this,MainActivity.class);
            startActivity(intent);

        } else if (id == R.id.nav_update_status) {

            fragmentManager.beginTransaction().replace(R.id.user_container,new UpdateStatus()).commit();
        }
        else if (id == R.id.nav_back) {
            Intent intent= new Intent(UserActivity.this,MainActivity.class);
            startActivity(intent);
        }
        else if (id == R.id.nav_trace) {
            fragmentManager.beginTransaction().replace(R.id.user_container,new Trace()).commit();
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

}

package com.dg.apptabletteandroid;

import android.app.ActivityManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.util.Log;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.dg.apptabletteandroid.Daemon.ServiceAdmin;
import com.dg.apptabletteandroid.Data.Profil;
import com.dg.apptabletteandroid.Profils.ProfilsManager;
import com.dg.apptabletteandroid.fragments.MapFragment_;
import com.dg.apptabletteandroid.fragments.ProfilFragment;
import com.google.android.gms.maps.model.Marker;

public class Main2Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{

    private FragmentManager fragmentManager;
    private ProfilsManager profilsManager;
    private ServiceReceiver serviceReceiver;

    public final static String ACTION_FROM_SERVICE = "action.from.service";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        fragmentManager = new FragmentManager();
        profilsManager = new ProfilsManager(getPreferences(Context.MODE_PRIVATE));

        if (getIntent().getStringExtra("WAKE_UP") != null && getIntent().getStringExtra("WAKE_UP").equals("NEWSESSION"))
        {
            String idTel = getIntent().getStringExtra("IDTEL");
            Fragment profilFragment = ProfilFragment.newInstance(profilsManager,true,idTel);
            fragmentManager.pushFragment(profilFragment,this);
        }
        else
        {
            Fragment mapFragment = MapFragment_.newInstance();
            fragmentManager.pushFragment(mapFragment,this);
        }




        if (! isMyServiceRunning(ServiceAdmin.class))
        {
            Log.e("GEN NEW SERVICE","GEN AT ONCREATE");
            WakefulBroadcastReceiver wakeful = new WakefulReceiver();
            Intent intent = new Intent(Main2Activity.this,ServiceAdmin.class);
            wakeful.startWakefulService(this,intent);
        }

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void onBackPressed()
    {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main2, menu);
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
        int id = item.getItemId();

        if (id == R.id.nav_profils)
        {
            Fragment fragProfils = ProfilFragment.newInstance(profilsManager);
            fragmentManager.pushFragment(fragProfils,Main2Activity.this);
            // Handle the camera action
        }
        else if (id == R.id.nav_carte)
        {
            Fragment fragmap = MapFragment_.newInstance();
            fragmentManager.pushFragment(fragmap,Main2Activity.this);
        }
        else if (id == R.id.nav_slideshow)
        {

        } else if (id == R.id.nav_manage) {

        } else if (id == R.id.nav_share) {

        } else if (id == R.id.nav_send) {

        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onResume()
    {
        super.onResume();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Main2Activity.ACTION_FROM_SERVICE);
        this.serviceReceiver = new ServiceReceiver();
        registerReceiver(serviceReceiver,intentFilter);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        unregisterReceiver(serviceReceiver);
    }

    public void addProfilFollowed(Profil p)
    {

    }

    public void removeProfilFollowed(Profil p)
    {

    }

    public void drawMarkers()
    {

    }

    public void findProfilByMarker(Marker m)
    {

    }


    /**
     * ServiceReceiver -> recoit les messages du service
     */
    private class ServiceReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0,Intent arg1)
        {

            if (arg1.hasExtra("ALL_PROFILES"))
            {
                String allProfiles = arg1.getStringExtra("ALL_PROFILES");
                Log.e("ALL_PROFILES",allProfiles);
                profilsManager.setAllProfils(getPreferences(Context.MODE_PRIVATE),allProfiles);
            }

            else if (arg1.hasExtra("UPDATE"))
            {
                // update d'un profil suivi
            }

            else if (arg1.hasExtra("STOPPROMENADE"))
            {
                String idTel = arg1.getStringExtra("STOPPROMENADE");
                profilsManager.removeProfilOnPromenade(idTel);
                // synchronisation des tablettes (nouvelle promenade, nouveau profil)
            }
        }
    }
    
}

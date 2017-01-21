package com.dg.apptabletteandroid;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.Fragment;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.provider.Settings;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;

import com.dg.apptabletteandroid.Daemon.ServiceAdmin;
import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.Profils.ProfilsManager;
import com.dg.apptabletteandroid.fragments.BlankFragment;
import com.dg.apptabletteandroid.fragments.Map.MapFragment_;
import com.dg.apptabletteandroid.fragments.Profils.ProfilFragment;
import com.google.android.gms.maps.model.Marker;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

public class Main2Activity extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener
{

    private FragmentManager fragmentManager;
    public static ProfilsManager profilsManager;
    private ServiceReceiver serviceReceiver;

    public final static String ACTION_FROM_SERVICE = "action.from.service";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main2);
        if(profilsManager == null){
            profilsManager = new ProfilsManager(getPreferences(Context.MODE_PRIVATE));
        }
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);


        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);


        fragmentManager = new FragmentManager();

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
        } else if(fragmentManager.getCurrentFragment() != null) {
            ((BlankFragment)fragmentManager.getCurrentFragment()).onBackPressed();
        }
        else {
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
        Intent intentBackground = new Intent();
        intentBackground.setAction(ServiceAdmin.ACTION_FROM_ACTIVITY);
        intentBackground.putExtra("ACTIVITY_BACKGROUND","FOREGROUND");
        sendBroadcast(intentBackground);
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Main2Activity.ACTION_FROM_SERVICE);
        this.serviceReceiver = new ServiceReceiver();
        registerReceiver(serviceReceiver,intentFilter);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        Intent intentBackground = new Intent();
        intentBackground.setAction(ServiceAdmin.ACTION_FROM_ACTIVITY);
        intentBackground.putExtra("ACTIVITY_BACKGROUND","BACKGROUND");
        sendBroadcast(intentBackground);
        unregisterReceiver(serviceReceiver);
    }

    public void followProfil(Profil p)
    {
        String idTel = profilsManager.findIdTelByProfil(p);
        if (idTel != null)
        {
            Intent intent = new Intent();
            intent.setAction(ServiceAdmin.ACTION_FROM_ACTIVITY);
            intent.putExtra("FOLLOW_SESSION","");
            intent.putExtra("IDTEL",idTel);
            sendBroadcast(intent);
        }
        else
        {
            throw new RuntimeException("profil: "+p.makeSignature()+" has not a idTel");
        }


    }

    public void unfollowProfil(Profil p)
    {
        String idTel = profilsManager.findIdTelByProfil(p);
        if (idTel != null)
        {
            Intent intent = new Intent();
            intent.setAction(ServiceAdmin.ACTION_FROM_ACTIVITY);
            intent.putExtra("UNFOLLOW_SESSION","");
            intent.putExtra("IDTEL",idTel);
            sendBroadcast(intent);
        }
        else
        {
            throw new RuntimeException("profil: "+p.makeSignature()+" has not a idTel");
        }
    }

    public void drawMarkers()
    {

    }

    public void findProfilByMarker(Marker m)
    {

    }
    
    public void pushFragmentFromActivity(Fragment frag) {
        fragmentManager.pushFragment(frag,Main2Activity.this);
    }

    public void displayDialogAlert() {
        AlertDialog.Builder builder = new AlertDialog.Builder(Main2Activity.this);
        builder.setMessage("Vous n'êtes pas connecté à Internet\n" +
                "Connectez vous !");
        builder.setCancelable(true);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //dialog.dismiss();
                Intent intent = new Intent(Settings.ACTION_WIFI_SETTINGS);
                startActivity(intent);
            }
        });

        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        AlertDialog  alert = builder.create();
        alert.show();
        alert.getWindow().setLayout(350, 170);
    }

    /**
     * ServiceReceiver -> recoit les messages du service
     */
    private class ServiceReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0,Intent arg1)
        {

            // recuperations de tous les profils
            if (arg1.hasExtra("ALL_PROFILES"))
            {
                String allProfiles = arg1.getStringExtra("ALL_PROFILES");
                Log.e("ALL_PROFILES",allProfiles);
                profilsManager.setAllProfils(getPreferences(Context.MODE_PRIVATE),allProfiles);
            }

            // indique que un patient a changé de positions
            else if (arg1.hasExtra("UPDATE"))
            {
                // update d'un profil suivi
                String message = arg1.getStringExtra("UPDATE");
                String[] parametres = message.split("\\*");
                String idTel = parametres[0];
                double longitude = Double.parseDouble(parametres[1]);
                double latitude = Double.parseDouble(parametres[2]);
                Profil profil = profilsManager.getProfilOnPromenade().get(idTel);
                if (profil == null)
                {
                    return; // si un update d'un profil est recu avant une synchro, ne pas en tenir compte sinon NullPtrException.
                }

                Log.d("Size", String.valueOf(profilsManager.getProfilOnPromenade().size()));
                profil.setLongitude(longitude);
                profil.setLatitude(latitude);
                if (fragmentManager.getCurrentFragment() instanceof MapFragment_)
                {
                    MapFragment_ mapFragment_ = (MapFragment_) fragmentManager.getCurrentFragment();
                    Log.e(String.valueOf(profil == null),"CC");
                    mapFragment_.update(profil);
                }

            }

            // indique que une promenade se termine pour cet idTel
            else if (arg1.hasExtra("STOPPROMENADE"))
            {
                String idTel = arg1.getStringExtra("STOPPROMENADE");
                if (fragmentManager.getCurrentFragment() instanceof MapFragment_)
                {
                    Profil profilStopped = profilsManager.getProfilOnPromenade().get(idTel);
                    ((MapFragment_) fragmentManager.getCurrentFragment()).removeProfil(profilStopped);
                }
                profilsManager.removeProfilOnPromenade(idTel);
                // synchronisation des tablettes (nouvelle promenade, nouveau profil)
            }

            // indique que un patient est en promenade et est suivi par au moins un assistant.
            else if (arg1.hasExtra("NWPROMENADE"))
            {
                String[] params = arg1.getStringArrayExtra("NWPROMENADE");
                String idTel = params[0];
                String nom = params[1];
                String prenom = params[2];
                Log.e("synch",nom+" "+prenom);
                Profil profilSelected = profilsManager.getProfil(nom,prenom);
                profilsManager.addProfilOnPromenade(idTel,profilSelected);
                Log.e("SYNCH_NW prom ok ?",String.valueOf(profilSelected != null));
            }

            // notif recu du service si la tablette est pas connecté a Internet
            else if(arg1.hasExtra("TABNOTCO")) {
                Log.e("ABAB", "Activity received signal");
                displayDialogAlert();
            }

            else if(arg1.hasExtra("NWPROFIL")) {  //NWPROFIL_nom*prenom*susceptibleDeFranchirLaBarriere
                String[] params = arg1.getStringArrayExtra("NWPROFIL");
                String nom = params[0];
                String prenom = params[1];
                String barriere = params[2];
                Boolean barriereBool;
                if(barriere.equals("BarriereNormal")) {
                    barriereBool = false;
                } else {
                    barriereBool = true;
                }
                profilsManager.getAllProfils().add(new Profil(nom, prenom, barriereBool));

            }

            else if(arg1.hasExtra("RMPROFIL"))
            { // nom*prenom
                String[] params = arg1.getStringArrayExtra("RMPROFIL");
                String nom = params[0];
                String prenom = params[1];
                Log.e("SUPR", prenom + " " + nom);
                profilsManager.removeProfile(prenom,nom);

            }

            else if(arg1.hasExtra("MODIFPROFIL")) {  //SYNCH$MODIFPROFIL_oldNom,prenom,Barriere*newNom,prenom,Barriere
                String[] params = arg1.getStringArrayExtra("MODIFPROFIL");
                String oldNom = params[0].split(",")[0];
                String oldPrenom = params[0].split(",")[1];

                String newNom = params[1].split(",")[0];
                String newPrenom = params[1].split(",")[1];
                String barriere = params[1].split(",")[2];

                profilsManager.removeProfile(oldPrenom, oldNom);
                Boolean barriereBool;
                if(barriere.equals("BarriereNormal")) {
                    barriereBool = false;
                } else {
                    barriereBool = true;
                }
                profilsManager.getAllProfils().add(new Profil(newNom, newPrenom, barriereBool));
            }

            // True si onReceive est appellé lors d'une procedure de synchronisation de l'activité suite à son retour en premier plan
            if (arg1.hasExtra("SYNCH_ACTIVITY"))
            {
                // pour indiquer que l'intent a été bien recu.
                String addr_receiver = arg1.getStringExtra("SYNCH_ACTIVITY");
                Intent intentcheck = new Intent();
                intentcheck.setAction(addr_receiver);
                Log.e("CHECK ACTIVITY","INTENT SYNCH");
                intentcheck.putExtra("CHECK",true);
                sendBroadcast(intentcheck);
            }
        }
    }

    public ProfilsManager getProfilsManager() {
        return profilsManager;
    }
    
}

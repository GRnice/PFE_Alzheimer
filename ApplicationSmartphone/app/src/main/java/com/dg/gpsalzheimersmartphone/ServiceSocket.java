package com.dg.gpsalzheimersmartphone;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.Toast;

import static com.dg.gpsalzheimersmartphone.MainActivity.android_id;

public class ServiceSocket extends Service implements LocationListener
{
    private CommunicationServer comm;
    final static String ACTION_SEND_TO_ACTIVITY = "DATA_TO_ACTIVITY";
    final static String ACTION_RECEIVE_FROM_SERVER = "RECEIVE_FROM_SERVER";
    private MyReceiver myReceiver;
    private OkReceiver okReceiver;
    private boolean gpsOn;

    public ServiceSocket()
    {
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId)
    {

        if (comm == null)
        {
            comm = new CommunicationServer();
        }

        comm.setActionIntent(ACTION_SEND_TO_ACTIVITY);
        comm.setService(this);
        comm.start();
        myReceiver = new MyReceiver();
        okReceiver = new OkReceiver();


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.ACTION_SEND_TO_SERVICE);
        registerReceiver(myReceiver, intentFilter);
        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_RECEIVE_FROM_SERVER);
        registerReceiver(okReceiver, intentFilter);
        super.onStartCommand(intent,flags,startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        Log.e("DEAD","DEAD");
        if (gpsOn)
        {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,1,0);
            lm.removeUpdates(this);

        }
        unregisterReceiver(myReceiver);
        unregisterReceiver(okReceiver);
        comm.interrupt();
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        return null;
    }

    @Override
    public void onLocationChanged(Location location)
    {
        comm.sendMessage("POSITION*" + String.valueOf(location.getLongitude()) +
                "*" +
                String.valueOf(location.getLatitude()));
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras)
    {
    }

    @Override
    public void onProviderEnabled(String provider)
    {

    }

    @Override
    public void onProviderDisabled(String provider)
    {

    }

    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            boolean startSuivi = arg1.getBooleanExtra("STARTSUIVI*" + android_id, false);
            boolean messageContinue = arg1.getBooleanExtra("CONTINUE*" + android_id, false);
            if(startSuivi){
                ServiceSocket.this.comm.sendMessage("STARTSUIVI*" + android_id);
            }
            if (messageContinue)
            {
                ServiceSocket.this.comm.sendMessage("CONTINUE*" + android_id);
            }




            boolean killApp = arg1.getBooleanExtra("KILL",false);
            if (killApp)
            {
                ServiceSocket.this.stopSelf();
            }
        }

    }

    private class OkReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            boolean startGps = arg1.getBooleanExtra("OKPROMENADE",false);

            if (startGps)
            {
                gpsOn = true;
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,1,0);
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,500,0,ServiceSocket.this);
            }
            else if (!startGps)
            {
                gpsOn = false;
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                lm.removeUpdates(ServiceSocket.this);
            }
        }

    }

}

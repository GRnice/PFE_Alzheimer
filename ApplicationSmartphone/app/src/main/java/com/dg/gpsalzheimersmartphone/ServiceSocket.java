package com.dg.gpsalzheimersmartphone;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.GpsStatus;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

public class ServiceSocket extends Service implements LocationListener
{
    private CommunicationServer comm;
    final static String ACTION_SEND_TO_ACTIVITY = "DATA_TO_ACTIVITY";
    private MyReceiver myReceiver;
    private String username;
    private boolean gpsOn;

    public ServiceSocket()
    {
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId)
    {
        this.username = intent.getStringExtra("username");

        if (comm == null)
        {
            comm = new CommunicationServer();
        }

        comm.setActionIntent(ACTION_SEND_TO_ACTIVITY);
        comm.setService(this);
        comm.start();
        myReceiver = new MyReceiver();

        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.ACTION_SEND_TO_SERVICE);
        registerReceiver(myReceiver, intentFilter);
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
        comm.sendMessage("POSITION*3*"+
                this.username+"*"+
                String.valueOf(location.getLatitude())+"*"+
                String.valueOf(location.getLongitude()));
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
            String message = arg1.getStringExtra("DATAPASSED");
            if (message != null)
            {
                ServiceSocket.this.comm.sendMessage(message);
            }

            boolean startGps = arg1.getBooleanExtra("STARTGPS",false);
            if (startGps)
            {
                gpsOn = true;
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,1,0);
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0,ServiceSocket.this);
            }
            else if (startGps == false)
            {
                gpsOn = false;
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                lm.removeUpdates(ServiceSocket.this);
            }

            boolean killApp = arg1.getBooleanExtra("KILL",false);
            if (killApp)
            {
                ServiceSocket.this.stopSelf();
            }
        }

    }
}

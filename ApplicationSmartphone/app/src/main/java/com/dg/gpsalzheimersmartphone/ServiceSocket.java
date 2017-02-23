package com.dg.gpsalzheimersmartphone;

import android.Manifest;
import android.app.AlertDialog;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import java.io.IOException;
import java.util.Timer;
import java.util.TimerTask;

import static com.dg.gpsalzheimersmartphone.CommunicationServer.STOPSUIVI;
import static com.dg.gpsalzheimersmartphone.MainActivity.android_id;

public class ServiceSocket extends Service implements LocationListener, SensorEventListener
{
    public static final String STARTSUIVI = "STARTSUIVI";
    public static final String CONTINUE = "CONTINUE";
    public static final String OKPROMENADE = "OKPROMENADE";
    public static final String POSITION = "POSITION";
    public static final String IMMOBILE = "IMMOBILE";
    public static final String SEPARATOR = "*";

    final public static String ACTION_SEND_TO_ACTIVITY = "DATA_TO_ACTIVITY";
    final public static String ACTION_RECEIVE_FROM_SERVER = "RECEIVE_FROM_SERVER";
    final public static String MESSAGE_FROM_SERVICE = "DATA_FROM_SERVICE";
    //Delay du premier envoie de location
    public static final int DELAY = 2000;
    //Période de temps entre envoie de deux updates
    public static final int PERIOD = 5000;
    public static long maxTime;
    private CommunicationServer comm;
    private ClientReceiver clientReceiver;
    private ServerReceiver serverReceiver;
    private NetworkChangeReceiver networkChangeReceiver;
    private BatteryChangeReceiver batteryChangeReceiver;
    private boolean onPromenade;

    private static final String TAG = "Nombre Pas";
    private SensorManager mSensorManagerCountStep;
    private Sensor mSensor;
    private float valeurCompteur = 0;
    private long lastUpdate = 0;
    private float lastValeurCompteur;
    public static Location currentLocation;
    public static Timer timer;

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

        clientReceiver = new ClientReceiver();
        serverReceiver = new ServerReceiver();
        networkChangeReceiver = new NetworkChangeReceiver();
        batteryChangeReceiver = new BatteryChangeReceiver();


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.ACTION_SEND_TO_SERVER);
        registerReceiver(clientReceiver, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_RECEIVE_FROM_SERVER);
        registerReceiver(serverReceiver, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(NetworkChangeReceiver.CONNECTIVITY_CHANGED);
        registerReceiver(networkChangeReceiver, intentFilter);

        IntentFilter ifilter = new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
        registerReceiver(batteryChangeReceiver, ifilter);

        //accelerometre

        mSensorManagerCountStep = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        mSensor = mSensorManagerCountStep.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManagerCountStep.registerListener(this, mSensor, SensorManager.SENSOR_DELAY_NORMAL);

        super.onStartCommand(intent,flags,startId);
        return START_STICKY;
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent) {
        if(onPromenade){
            Sensor mySensor = sensorEvent.sensor;

            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER) {
                float x =  Math.abs(sensorEvent.values[0]);
                float y =  Math.abs(sensorEvent.values[1]);
                float z =  Math.abs(sensorEvent.values[2]);
                long curTime = System.currentTimeMillis();
                float ompteur = (((x*x) + (y*y) + (z*z)) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH));
                if (ompteur >= 1.34) {
                    valeurCompteur += 1;
                }
                if ((curTime - lastUpdate) > maxTime) {
                    if(lastValeurCompteur != 0){
                        if((valeurCompteur - lastValeurCompteur) <5) {
                            //Alerte Immobile
                            comm.sendMessage(IMMOBILE);
                        }
                    }
                    lastUpdate = curTime;
                    lastValeurCompteur = valeurCompteur;
                }
                Log.d(TAG,Float.toString(valeurCompteur));
            }
        }
    }


    @Override
    public void onAccuracyChanged(Sensor mSensor, int accuracy) {

    }
    @Override
    public void onDestroy()
    {
        Log.e("DEAD","DEAD");
        comm.interrupt();
        if (onPromenade)
        {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,1,0);
            lm.removeUpdates(this);
        }
        mSensorManagerCountStep.unregisterListener(this);
        unregisterReceiver(clientReceiver);
        unregisterReceiver(serverReceiver);
        unregisterReceiver(networkChangeReceiver);
        unregisterReceiver(batteryChangeReceiver);
        if(timer != null){
            timer.cancel();
        }
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
        currentLocation = location;
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

    /**
     * ClientReceiver , envoie des messages au serveur
     * STARTSUIVI
     * CONTINUE
     * STOPSUIVI
     *
     * action -> MainActivity.ACTION_SEND_TO_SERVER
     */
    private class ClientReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {

            boolean startSuivi = arg1.getBooleanExtra(STARTSUIVI + SEPARATOR + android_id, false);
            boolean messageContinue = arg1.getBooleanExtra(CONTINUE + SEPARATOR + android_id, false);
            boolean stopSuivi = arg1.getBooleanExtra(STOPSUIVI, false);

            if(stopSuivi){
                ServiceSocket.this.comm.sendMessage(STOPSUIVI);
                if(timer != null){
                    timer.cancel();
                }
            }
            if(startSuivi){
                ServiceSocket.this.comm.sendMessage(STARTSUIVI + SEPARATOR + android_id);
            }
            if (messageContinue)
            {
                ServiceSocket.this.comm = new CommunicationServer();
                ServiceSocket.this.comm.setActionIntent(ACTION_SEND_TO_ACTIVITY);
                ServiceSocket.this.comm.setService(ServiceSocket.this);
                ServiceSocket.this.comm.start();
                try {
                    //Wait for input and output to be initialised in comm object
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                ServiceSocket.this.comm.sendMessage(CONTINUE + SEPARATOR + android_id);
            }



        }

    }

    /**
     * ServerReceiver , recoit les messages venants du serveur
     * OKPROMENADE
     * STOPSUIVI
     *
     * ACTION_RECEIVE_FROM_SERVER
     */
    private class ServerReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context arg0, Intent arg1) {

            boolean startGps = arg1.getBooleanExtra(OKPROMENADE,false);
            boolean stop = arg1.getBooleanExtra(STOPSUIVI, false);

            if(stop){
                onPromenade = false;
                Intent messageForActivity = new Intent();
                messageForActivity.setAction(ServiceSocket.MESSAGE_FROM_SERVICE);
                messageForActivity.putExtra("KILL",true);
                sendBroadcast(messageForActivity);
                ServiceSocket.this.stopSelf();
                try {
                    ServiceSocket.this.comm.getSocket().close();
                } catch (IOException e) {
                    Log.e("Socket", "Oops");
                    e.printStackTrace();
                }
            }

            if (startGps)
            {
                onPromenade = true;
                timer = new Timer();
                timer.schedule(new TimerTask() {
                    @Override
                    public void run() {
                        if(currentLocation != null && onPromenade && networkChangeReceiver.connected){
                            comm.sendMessage(POSITION + SEPARATOR + String.valueOf(currentLocation.getLongitude()) +
                                    SEPARATOR +
                                    String.valueOf(currentLocation.getLatitude()) + SEPARATOR + batteryChangeReceiver.level);
                        }
                    }
                }, DELAY, PERIOD);
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                if(!lm.isProviderEnabled(LocationManager.GPS_PROVIDER)){
                    Intent intent = new Intent();
                    intent.setAction(ServiceSocket.MESSAGE_FROM_SERVICE);
                    intent.putExtra("ALERTGPS", true);
                    sendBroadcast(intent);
                }
                checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,1,0);
                lm.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,1000,0,ServiceSocket.this);

            }
            else if (!startGps)
            {
                LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                lm.removeUpdates(ServiceSocket.this);
                if(timer != null){
                    timer.cancel();
                }
            }
        }

    }

    /**
     * NetworkChangeReceiver , écoute les changements d'états
     */
    public class NetworkChangeReceiver extends BroadcastReceiver {

        public static final String CONNECTIVITY_CHANGED = "android.net.conn.CONNECTIVITY_CHANGE";
        private boolean connected = true;

        @Override
        public void onReceive(final Context context, final Intent intent) {
            int status = NetworkUtil.getConnectivityStatusString(context);
            if (CONNECTIVITY_CHANGED.equals(intent.getAction()))
            {
                if(status==NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                    connected = false;
                    ServiceSocket.this.comm.interrupt();
                    Intent intent1 = new Intent();
                    intent1.setAction(ACTION_RECEIVE_FROM_SERVER);
                    intent1.putExtra(OKPROMENADE, false);
                    sendBroadcast(intent1);
                }
                else if(status==NetworkUtil.NETWORK_STATUS_MOBILE || status== NetworkUtil.NETWORK_STATUS_WIFI)
                {
                    if(!connected && onPromenade){
                        Intent intent1 = new Intent();
                        intent1.setAction(MainActivity.ACTION_SEND_TO_SERVER);
                        intent1.putExtra("CONTINUE*" + android_id, true);
                        sendBroadcast(intent1);
                        connected = true;
                    }
                }
            }
        }
    }

    public class BatteryChangeReceiver extends BroadcastReceiver {
        public int level;
        @Override
        public void onReceive(Context context, Intent intent) {
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        }
    }




}

package com.dg.gpsalzheimersmartphone;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
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



import static com.dg.gpsalzheimersmartphone.CommunicationServer.CONNECTED;
import static com.dg.gpsalzheimersmartphone.CommunicationServer.STOPSUIVI;
import static com.dg.gpsalzheimersmartphone.MainActivity.android_id;

public class ServiceSocket extends Service implements LocationListener,
        SensorEventListener
{
    public static final String STARTSUIVI = "STARTSUIVI";
    public static final String CONTINUE = "CONTINUE";
    public static final String OKPROMENADE = "OKPROMENADE";
    public static final String POSITION = "POSITION";
    public static final String IMMOBILE = "IMMOBILE";
    public static final String IMMOBILESTOP = "IMMOBILE-STOP";
    public static final String SEPARATOR = "*";

    final public static String ACTION_SEND_TO_ACTIVITY = "DATA_TO_ACTIVITY";
    final public static String ACTION_RECEIVE_FROM_SERVER = "RECEIVE_FROM_SERVER";
    final public static String MESSAGE_FROM_SERVICE = "DATA_FROM_SERVICE";

    public static long maxTime;
    private CommunicationServer comm;
    private ClientReceiver clientReceiver;
    private ServerReceiver serverReceiver;
    private NetworkChangeReceiver networkChangeReceiver;
    private BatteryChangeReceiver batteryChangeReceiver;
    private boolean identifiedByServer;
    private boolean onPromenade;
    private boolean connectionEtablishedWithServer;



    private float valeurCompteur = 0;
    private long lastUpdate = 0;
    private float lastValeurCompteur;
    public Location currentLocation;
    private ScheduleSender schedSender;

    public ServiceSocket()
    {
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId)
    {
        clientReceiver = new ClientReceiver();
        serverReceiver = new ServerReceiver();
        networkChangeReceiver = new NetworkChangeReceiver();
        batteryChangeReceiver = new BatteryChangeReceiver();

        identifiedByServer = false;

        schedSender = new ScheduleSender();
        currentLocation = new Location("dummyprovider");
        currentLocation.setLatitude(43.6122565);
        currentLocation.setLongitude(7.0793731);

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

        super.onStartCommand(intent,flags,startId);
        return START_NOT_STICKY;
    }
    @Override
    public void onSensorChanged(SensorEvent sensorEvent)
    {
        if(onPromenade)
        {
            Sensor mySensor = sensorEvent.sensor;

            if (mySensor.getType() == Sensor.TYPE_ACCELEROMETER)
            {
                float x =  Math.abs(sensorEvent.values[0]);
                float y =  Math.abs(sensorEvent.values[1]);
                float z =  Math.abs(sensorEvent.values[2]);
                long curTime = System.currentTimeMillis();
                float ompteur = (((x*x) + (y*y) + (z*z)) / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH));
                if (ompteur >= 1.34)
                {
                    valeurCompteur += 1;
                }
                if ((curTime - lastUpdate) > maxTime) {
                    if(lastValeurCompteur != 0){
                        if(( (valeurCompteur - lastValeurCompteur) < 5) && connectionEtablishedWithServer)
                        {
                            //Alerte Immobile
                            comm.sendMessage(IMMOBILE);
                        }else if (connectionEtablishedWithServer)
                        {
                            comm.sendMessage(IMMOBILESTOP);
                        }
                    }
                    lastUpdate = curTime;
                    lastValeurCompteur = valeurCompteur;
                }
                Log.d("valeurCompteur",Float.toString(valeurCompteur));
            }
        }
    }

    public void endTask(CommunicationServer commnw,boolean success)
    {
        if (success)
        {
            this.comm = commnw;
            Intent messageForActivity = new Intent();
            messageForActivity.setAction(ServiceSocket.MESSAGE_FROM_SERVICE);
            messageForActivity.putExtra("SERVICECONNECTION",true);
            sendBroadcast(messageForActivity);

        }
        else
        {
            if (networkChangeReceiver.connectedNetwork)
            {
                CommunicationServer comm = new CommunicationServer(10000);
                comm.setService(this);
                comm.start();
            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor mSensor, int accuracy)
    {

    }

    @Override
    public void onDestroy()
    {
        Log.e("DEAD","DEAD");
        if (comm != null)
        {
            comm.interrupt(); // arret du socket
        }

        if (onPromenade) // si en promenade on ne solicite plus le GPS
        {
            LocationManager lm = (LocationManager) ServiceSocket.this.getSystemService(LOCATION_SERVICE);
            lm.removeUpdates(ServiceSocket.this);
            schedSender.stopRemainder(this);
        }

        unregisterReceiver(clientReceiver); // arret des 4 broadcast receivers
        unregisterReceiver(serverReceiver);
        unregisterReceiver(networkChangeReceiver);
        unregisterReceiver(batteryChangeReceiver);

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
        Log.wtf("location","LAT:"+location.getLatitude()+" LONG:"+location.getLongitude());
        currentLocation = location;
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

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
     * Envoie la position courante et le niveau de batterie du tracker au serveur
     */
    public void sendUpdate()
    {
        if (connectionEtablishedWithServer)
        {
            comm.sendMessage(POSITION + SEPARATOR + String.valueOf(currentLocation.getLongitude()) +
                    SEPARATOR +
                    String.valueOf(currentLocation.getLatitude()) + SEPARATOR + batteryChangeReceiver.level);
        }
    }

    /**
     * ClientReceiver , receptionne les messages provenants de MainActivity
     * STARTSUIVI
     * CONTINUE
     * STOPSUIVI
     *
     * action -> MainActivity.ACTION_SEND_TO_SERVER
     */
    private class ClientReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            boolean startSuivi = arg1.getBooleanExtra(STARTSUIVI + SEPARATOR + android_id, false);
            boolean messageContinue = arg1.getBooleanExtra(CONTINUE + SEPARATOR + android_id, false);
            boolean stopSuivi = arg1.getBooleanExtra(STOPSUIVI, false);

            if(stopSuivi && connectionEtablishedWithServer)
            {
                ServiceSocket.this.comm.sendMessage(STOPSUIVI);
                identifiedByServer = false;
            }
            if(startSuivi && connectionEtablishedWithServer)
            {
                ServiceSocket.this.comm.sendMessage(STARTSUIVI + SEPARATOR + android_id);
                Intent messageForActivity = new Intent();
                messageForActivity.setAction(ServiceSocket.MESSAGE_FROM_SERVICE);
                messageForActivity.putExtra("DEMANDESUIVISENT","");
                sendBroadcast(messageForActivity);
            }
            if (messageContinue && connectionEtablishedWithServer)
            {
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

            if (arg1.hasExtra(CONNECTED)) // si on recoit CONNECTED, alors on est bien enregistré chez le serveur
            {
                connectionEtablishedWithServer = true;
                if (identifiedByServer) // si identifié par le serveur alors on envoie CONTINUE
                {
                    ServiceSocket.this.comm.sendMessage(CONTINUE + SEPARATOR + android_id);
                }
            }

            else if(stop) // si stop du serveur on coupe tout
            {
                ServiceSocket.this.comm.interrupt();
                onPromenade = false;
                identifiedByServer = false;
                schedSender.stopRemainder(ServiceSocket.this);
                Intent messageForActivity = new Intent();
                messageForActivity.setAction(ServiceSocket.MESSAGE_FROM_SERVICE);
                messageForActivity.putExtra("KILL",true);
                sendBroadcast(messageForActivity);
            }

            else if (startGps) // OKPROMENADE
            {
                identifiedByServer = true;
                onPromenade = true;
                Log.e("LOCATIONON","§§");
                schedSender.startRemainder(ServiceSocket.this);
                LocationManager lm = (LocationManager) ServiceSocket.this.getSystemService(LOCATION_SERVICE);
                lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,0,0,ServiceSocket.this);
                Intent messageForActivity = new Intent();
                messageForActivity.setAction(ServiceSocket.MESSAGE_FROM_SERVICE);
                messageForActivity.putExtra("OKPROMENADE","");
                sendBroadcast(messageForActivity);

            }
            else if (!startGps)
            {
                Log.e("LOCATIONOFF","**");
                schedSender.stopRemainder(ServiceSocket.this);
                LocationManager lm = (LocationManager) ServiceSocket.this.getSystemService(LOCATION_SERVICE);
                lm.removeUpdates(ServiceSocket.this);
            }
        }

    }

    /**
     * NetworkChangeReceiver , écoute les changements d'états
     */
    public class NetworkChangeReceiver extends BroadcastReceiver {

        public static final String CONNECTIVITY_CHANGED = "android.net.conn.CONNECTIVITY_CHANGE";
        public boolean connectedNetwork = false;

        @Override
        public void onReceive(final Context context, final Intent intent)
        {
            int status = NetworkUtil.getConnectivityStatusString(context);
            if (CONNECTIVITY_CHANGED.equals(intent.getAction()))
            {
                if(status==NetworkUtil.NETWORK_STATUS_NOT_CONNECTED)
                {
                    Intent messageForActivity = new Intent();
                    messageForActivity.setAction(ServiceSocket.MESSAGE_FROM_SERVICE);
                    messageForActivity.putExtra("SERVICECONNECTION",false);
                    sendBroadcast(messageForActivity);

                    connectedNetwork = false;
                    connectionEtablishedWithServer = false;
                    if (ServiceSocket.this.comm != null)
                    {
                        ServiceSocket.this.comm.interrupt();
                        comm = null;

                    }
                    Intent intent1 = new Intent();
                    intent1.setAction(ACTION_RECEIVE_FROM_SERVER);
                    intent1.putExtra(OKPROMENADE, false);
                    sendBroadcast(intent1);
                }
                else if(status==NetworkUtil.NETWORK_STATUS_MOBILE || status == NetworkUtil.NETWORK_STATUS_WIFI)
                {
                    connectedNetwork = true;
                    CommunicationServer comm = new CommunicationServer();
                    comm.setService(ServiceSocket.this);
                    comm.start();
                }
            }
        }
    }

    public class BatteryChangeReceiver extends BroadcastReceiver
    {
        public int level;
        @Override
        public void onReceive(Context context, Intent intent)
        {
            level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        }
    }




}

package com.dg.gpsalzheimersmartphone;

import android.Manifest;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

import static com.dg.gpsalzheimersmartphone.CommunicationServer.STOPSUIVI;
import static com.dg.gpsalzheimersmartphone.MainActivity.android_id;

public class ServiceSocket extends Service implements LocationListener
{
    public static final String STARTSUIVI = "STARTSUIVI";
    public static final String CONTINUE = "CONTINUE";
    public static final String OKPROMENADE = "OKPROMENADE";
    public static final String POSITION = "POSITION";
    public static final String SEPARATOR = "*";

    final public static String ACTION_SEND_TO_ACTIVITY = "DATA_TO_ACTIVITY";
    final public static String ACTION_RECEIVE_FROM_SERVER = "RECEIVE_FROM_SERVER";
    final public static String MESSAGE_FROM_SERVICE = "DATA_FROM_SERVICE";

    private CommunicationServer comm;
    private ClientReceiver clientReceiver;
    private ServerReceiver serverReceiver;
    private NetworkChangeReceiver networkChangeReceiver;
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

        clientReceiver = new ClientReceiver();
        serverReceiver = new ServerReceiver();
        networkChangeReceiver = new NetworkChangeReceiver();


        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(MainActivity.ACTION_SEND_TO_SERVER);
        registerReceiver(clientReceiver, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(ACTION_RECEIVE_FROM_SERVER);
        registerReceiver(serverReceiver, intentFilter);

        intentFilter = new IntentFilter();
        intentFilter.addAction(NetworkChangeReceiver.CONNECTIVITY_CHANGED);
        registerReceiver(networkChangeReceiver, intentFilter);

        super.onStartCommand(intent,flags,startId);
        return START_STICKY;
    }

    @Override
    public void onDestroy()
    {
        Log.e("DEAD","DEAD");
        comm.interrupt();
        if (gpsOn)
        {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            checkPermission(Manifest.permission.ACCESS_FINE_LOCATION,1,0);
            lm.removeUpdates(this);
        }
        unregisterReceiver(clientReceiver);
        unregisterReceiver(serverReceiver);
        unregisterReceiver(networkChangeReceiver);
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
        comm.sendMessage(POSITION + SEPARATOR + String.valueOf(location.getLongitude()) +
                SEPARATOR +
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
                Intent messageForActivity = new Intent();
                messageForActivity.setAction(ServiceSocket.MESSAGE_FROM_SERVICE);
                messageForActivity.putExtra("KILL",true);
                sendBroadcast(messageForActivity);
                ServiceSocket.this.stopSelf();
            }

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
                    if(!connected){
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

}

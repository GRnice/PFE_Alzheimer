package com.dg.gpsalzheimersmartphone;

import android.Manifest;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private int etat = 0;
//    private MyReceiver myReceiver;
    public final static String ACTION_SEND_TO_SERVICE = "DATA_TO_SERVICE";
    public static String android_id;
    private NetworkChangeReceiver networkChangeReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        if (! isMyServiceRunning(ServiceSocket.class))
        {
            Log.e("GEN NEW SERVICE","GEN AT ONCREATE");
            WakefulBroadcastReceiver wakeful = new WakefulReceiver();
            Intent intent = new Intent(MainActivity.this,ServiceSocket.class);
            intent.putExtra("username","Rémy");
            wakeful.startWakefulService(this,intent);
        }

        final Button buttonSwitchConnexion = (Button) findViewById(R.id.button);
        final Button buttonKillService = (Button) findViewById(R.id.button2);

        buttonKillService.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                Intent intent = new Intent();
                intent.setAction(ACTION_SEND_TO_SERVICE);
                intent.putExtra("KILL",true);
                sendBroadcast(intent);
                MainActivity.this.finish();
            }
        });

        buttonSwitchConnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etat == 0)
                {
                    Intent intent = new Intent();
                    intent.setAction(ACTION_SEND_TO_SERVICE);
                    intent.putExtra("STARTSUIVI*" + android_id, true);
                    sendBroadcast(intent);

                    buttonSwitchConnexion.setText("DESACTIVER LE SUIVI");
                    etat = 1;
                }
                else
                {
                    etat = 0;
                    Intent intent = new Intent();
                    intent.setAction(ACTION_SEND_TO_SERVICE);
                    sendBroadcast(intent);
                    buttonSwitchConnexion.setText("ACTIVER LE SUIVI");
                }
            }
        });
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
    protected void onStart()
    {
        super.onStart();
        if (! isMyServiceRunning(ServiceSocket.class))
        {
            Log.e("GEN NEW SERVICE","GEN AT START");
            WakefulBroadcastReceiver wakeful = new WakefulReceiver();
            Intent intent = new Intent(MainActivity.this,ServiceSocket.class);
            wakeful.startWakefulService(this,intent);
        }
//        myReceiver = new MyReceiver();
//        IntentFilter intentFilter = new IntentFilter();
//        intentFilter.addAction(ServiceSocket.ACTION_SEND_TO_ACTIVITY);
//        registerReceiver(myReceiver, intentFilter);
        networkChangeReceiver = new NetworkChangeReceiver();
        IntentFilter intentFilter1 = new IntentFilter();
        intentFilter1.addAction(NetworkChangeReceiver.CONNECTIVITY_CHANGED);
        registerReceiver(networkChangeReceiver, intentFilter1);
    }

    @Override
    protected void onDestroy()
    {

        super.onDestroy();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
//        unregisterReceiver(myReceiver);
        unregisterReceiver(networkChangeReceiver);
    }


//    private class MyReceiver extends BroadcastReceiver {
//
//        @Override
//        public void onReceive(Context arg0, Intent arg1) {
//            // TODO Auto-generated method stub
//
//            boolean datapassed = arg1.getBooleanExtra("OKPROMENADE", false);
//
//            if(datapassed){
//                Toast.makeText(MainActivity.this,
//                        "Triggered by Service!\n"
//                                + "Data passed, device id: " + android_id,
//                        Toast.LENGTH_LONG).show();
//            }
//
//        }
//
//    }

    private class NetworkChangeReceiver extends BroadcastReceiver {

        public static final String CONNECTIVITY_CHANGED = "android.net.conn.CONNECTIVITY_CHANGE";
        private boolean connected;

        @Override
        public void onReceive(final Context context, final Intent intent) {
            int status = NetworkUtil.getConnectivityStatusString(context);
            if (CONNECTIVITY_CHANGED.equals(intent.getAction())) {
                if(status==NetworkUtil.NETWORK_STATUS_NOT_CONNECTED){
                    connected = false;
                }else {
                    connected = true;
                }
                if(!connected && status==NetworkUtil.NETWORK_STATUS_MOBILE){
                    Intent intent1 = new Intent();
                    intent1.setAction(MainActivity.ACTION_SEND_TO_SERVICE);
                    intent1.putExtra("CONTINUE*" + android_id, true);
                    sendBroadcast(intent1);
                }
            }
        }
    }

}

package com.dg.gpsalzheimersmartphone;

import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

    private int etat = 0;
    private MyReceiver myReceiver;
    final static String ACTION_SEND_TO_SERVICE = "DATA_TO_SERVICE";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

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
                    intent.putExtra("DATAPASSED","STARTSUIVI*1*Rémy");
                    intent.putExtra("STARTGPS",true);
                    sendBroadcast(intent);

                    buttonSwitchConnexion.setText("DESACTIVER LE SUIVI");
                    etat = 1;
                }
                else
                {
                    etat = 0;
                    Intent intent = new Intent();
                    intent.setAction(ACTION_SEND_TO_SERVICE);
                    intent.putExtra("DATAPASSED","STOPSUIVI*1*Rémy");
                    intent.putExtra("STARTGPS",false);
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
        myReceiver = new MyReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ServiceSocket.ACTION_SEND_TO_ACTIVITY);
        registerReceiver(myReceiver, intentFilter);
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
        unregisterReceiver(myReceiver);
    }


    private class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            // TODO Auto-generated method stub

            String datapassed = arg1.getStringExtra("DATAPASSED");

            Toast.makeText(MainActivity.this,
                    "Triggered by Service!\n"
                            + "Data passed: " + datapassed,
                    Toast.LENGTH_LONG).show();

        }

    }
}

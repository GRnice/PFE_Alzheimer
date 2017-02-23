package com.dg.gpsalzheimersmartphone;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.provider.Settings;
import android.support.v4.content.WakefulBroadcastReceiver;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import static com.dg.gpsalzheimersmartphone.CommunicationServer.STOPSUIVI;

public class MainActivity extends AppCompatActivity {

    public static final String STARTSUIVI = "STARTSUIVI";
    public final static String ACTION_SEND_TO_SERVER = "DATA_TO_SERVICE";
    private int etat = 0;
    private Button buttonSwitchConnexion;
    private ServiceListenerReceiver serviceListener;

    public static String android_id;

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        serviceListener = new ServiceListenerReceiver();
        registerNewReceiver(serviceListener,ServiceSocket.MESSAGE_FROM_SERVICE);

        android_id = Settings.Secure.getString(getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        buttonSwitchConnexion = (Button) findViewById(R.id.button);

        if (! isMyServiceRunning(ServiceSocket.class))
        {
            Log.e("GEN NEW SERVICE","GEN AT ONCREATE");
            WakefulBroadcastReceiver wakeful = new WakefulReceiver();
            Intent intent = new Intent(MainActivity.this,ServiceSocket.class);
            intent.putExtra("username","Rémy");
            wakeful.startWakefulService(this,intent);
        }


        buttonSwitchConnexion.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(etat == 0)
                {
                    Intent intent = new Intent();
                    intent.setAction(ACTION_SEND_TO_SERVER);
                    intent.putExtra(STARTSUIVI + "*" + android_id, true);
                    sendBroadcast(intent);

                    buttonSwitchConnexion.setText("DESACTIVER LE SUIVI");
                    etat = 1;
                }
                else
                {
                    etat = 0;
                    Intent intent = new Intent();
                    intent.setAction(ACTION_SEND_TO_SERVER);
                    intent.putExtra(STOPSUIVI,true);
                    sendBroadcast(intent);
                }
            }
        });
    }

    public void registerNewReceiver(BroadcastReceiver receiver,String action)
    {
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ServiceSocket.MESSAGE_FROM_SERVICE);
        registerReceiver(receiver,intentFilter);
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

        buttonSwitchConnexion = (Button) findViewById(R.id.button);
        if (etat == 1)
        {
            buttonSwitchConnexion.setText("DESACTIVER LE SUIVI");
        }
    }

    @Override
    protected void onDestroy()
    {
        unregisterReceiver(serviceListener);
        super.onDestroy();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
    }

    /**
     * Service Listener écoute le service, si l'application se termine ou non
     */
    public class ServiceListenerReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context context, Intent intent)
        {
            boolean mustKill = intent.getBooleanExtra("KILL",false);
            boolean showAlert = intent.getBooleanExtra("ALERTGPS", false);

            if (mustKill)
            {
                MainActivity.this.finishAffinity();
            }
            if(showAlert){
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setMessage("Vous devez activer le GPS. Voulez-vous l'activer maintenant?")
                        .setCancelable(false)
                        .setPositiveButton("Oui", new DialogInterface.OnClickListener() {
                            public void onClick(@SuppressWarnings("unused") final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
                            }
                        })
                        .setNegativeButton("Non", new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog, @SuppressWarnings("unused") final int id) {
                                dialog.cancel();
                            }
                        });
                AlertDialog alert = builder.create();
                alert.show();
            }
        }
    }



}

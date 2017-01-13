package com.dg.apptabletteandroid.Daemon;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.preference.PreferenceManager;
import android.util.Log;

import com.dg.apptabletteandroid.AlertManager;
import com.dg.apptabletteandroid.Communication.CommunicationServer;
import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.NetworkUtil;
import com.dg.apptabletteandroid.Profils.ProfilsManager;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Queue;

/**
 * Ce service recoit tous les messages provenants du serveur
 */
public class ServiceAdmin extends Service
{
    CommunicationServer comm; // le thread qui lancera un socket dédié à l'écoute du serveur
    ServerReceiver serverReceiver; // ce receiver attrapera tous les messages provenants du serveur
    ActivityReceiver activityReceiver; // ce receiver recevra tous les messages provenants de l'activité
    AlertManager alertManager; // le manager des alertes
    DataKeeper dataKeeper; // stocke tous les messages recus en attendant que l'activite revienne en premier plan

    boolean activity_is_on_background; // true -> application en background ; false -> application au premier plan

    public static String ACTION_FROM_SERVER = "action.from.server";
    public static String ACTION_FROM_ACTIVITY = "action.from.activity";

    public ServiceAdmin()
    {
    }

    @Override
    public int onStartCommand(Intent intent,int flags,int startId) {

        if (comm == null) {
            comm = new CommunicationServer();
        }

        this.alertManager = new AlertManager();

        this.dataKeeper = new DataKeeper();
        comm.setActionIntent(ACTION_FROM_SERVER);
        comm.setService(this);
        comm.start();

        serverReceiver = new ServerReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ServiceAdmin.ACTION_FROM_SERVER);
        registerReceiver(serverReceiver, intentFilter);

        activityReceiver = new ActivityReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(ServiceAdmin.ACTION_FROM_ACTIVITY);
        registerReceiver(activityReceiver,intentFilter);

        Log.e("CHECK SERVICE","RUN");
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy()
    {
        unregisterReceiver(serverReceiver);
        unregisterReceiver(activityReceiver);
        comm.interrupt(); // arret du socket
    }

    @Override
    public IBinder onBind(Intent intent)
    {
        // TODO: Return the communication channel to the service.
        throw new UnsupportedOperationException("Not yet implemented");
    }


    /**
     * ServerReceiver , recoit les messages venants du serveur
     * NEWSESSION
     * UPDATE
     */
    private class ServerReceiver extends BroadcastReceiver
    {

        @Override
        public void onReceive(Context arg0, Intent arg1)
        {
            /**
             * Parsing du message provenant du serveur
             */
            String message = arg1.getStringExtra("MESSAGE");
            if (message == null) return;

            String[] tabMessage = message.split("\\$"); // syntaxe d'un message -> "Entete$contenu"
            String header = tabMessage[0]; // l'entete
            Log.e("HEADER",header);
            Log.e("ALL",message);
            String content = tabMessage[1];
            switch(header)
            {
                case "PROFILES":
                {
                    Log.e("ALL_PROFILES",content);
                    Intent intent = new Intent();
                    intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                    intent.putExtra("ALL_PROFILES",content);
                    if (activity_is_on_background)
                    {
                        dataKeeper.addData(intent);
                    }
                    else
                    {
                        sendBroadcast(intent);
                    }

                    break;
                }

                case "SYNCH":
                {

                    String[] dataParams = content.split("\\_");
                    String typeSynch = dataParams[0];

                    Log.e("ARGSSYNCH",dataParams[1]);

                    switch(typeSynch) {
                        // SYNCH syntaxe -> SYNCH$NWPROMENADE_idTel*nom*prenom
                        case "NWPROMENADE": {
                            String[] args = dataParams[1].split("\\*");
                            Intent intent = new Intent();
                            intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                            intent.putExtra("NWPROMENADE", args);
                            if (activity_is_on_background) {
                                dataKeeper.addData(intent);
                            } else {
                                sendBroadcast(intent);
                            }
                            break;
                        }

                        // SYNCH syntaxe -> SYNCH$NWPROFIL_nom*prenom*susceptibleDeFranchirLaBarriere
                        case "NWPROFIL": {
                            String[] args = dataParams[1].split("\\*");
                            Intent intent = new Intent();
                            intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                            intent.putExtra("NWPROFIL", args);
                            if (activity_is_on_background) {
                                dataKeeper.addData(intent);
                            } else {
                                sendBroadcast(intent);
                            }
                            break;
                        }

                        // SYNCH syntaxe -> SYNCH$RMPROFIL_nom*prenom
                        case "RMPROFIL": {
                            String[] args = dataParams[1].split("\\*");
                            Intent intent = new Intent();
                            intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                            intent.putExtra("RMPROFIL", args);

                            if (activity_is_on_background) {
                                dataKeeper.addData(intent);
                            } else {
                                sendBroadcast(intent);
                            }
                            break;
                        }

                        case "MODIFPROFIL": { //SYNCH$MODIFPROFIL_oldNom,prenom,Barriere*newNom,prenom,Barriere
                            String[] args = dataParams[1].split("\\*");
                            Intent intent = new Intent();
                            intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                            intent.putExtra("MODIFPROFIL", args);

                            if (activity_is_on_background) {
                                dataKeeper.addData(intent);
                            } else {
                                sendBroadcast(intent);
                            }
                            break;
                        }

                        // SYNCH syntaxe -> SYNCH$STOPPROMENADE_idTel
                        case "STOPPROMENADE":
                        {
                            alertManager.removeListening(dataParams[1]); // params est idTel ici
                            Log.e("STOP PROMENADE",dataParams[1]); // idtel
                            Intent intent = new Intent();
                            intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                            intent.putExtra("STOPPROMENADE",dataParams[1]);
                            if (activity_is_on_background)
                            {
                                dataKeeper.addData(intent);
                            }
                            else
                            {
                                sendBroadcast(intent);
                            }
                            break;
                        }
                    }


                    // SYNCH syntaxe -> SYNCH$CHANGEPROFIL_nom*prenom

                    break;
                }

                case "UPDATE":
                {
                    // METTRE A JOUR UN PROFIL SUIVI
                    // UPDATE$idTel*longitude*latitude
                    Log.e("UPDATE",content);
                    if (! activity_is_on_background)
                    {
                        Intent intent = new Intent();
                        intent.putExtra("UPDATE", content);
                        intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                        sendBroadcast(intent);
                    }

                    /**
                     * a completer
                     */
                    break;
                }

                case "NEWSESSION":
                {
                    // SELECTIONNER UN PROFIL
                    Log.e("NEWSESSION",content);
                    alertManager.notifNewSession(getBaseContext(),content);
                    break;
                }
            }
        }

    }

    /**
     * ActivityReceiver, recoit les messages venants de l'activite
     */
    private class ActivityReceiver extends BroadcastReceiver
    {
        @Override
        public void onReceive(Context arg0,Intent arg1)
        {

            if (arg1.hasExtra("ACTIVITY_BACKGROUND"))
            {

                activity_is_on_background = arg1.getStringExtra("ACTIVITY_BACKGROUND").equals("BACKGROUND");

                if (activity_is_on_background)
                {
                    dataKeeper.subscrive(ServiceAdmin.this);

                }
                else
                {
                    dataKeeper.publish(ServiceAdmin.this);
                }
            }

            if (arg1.hasExtra("FOLLOW_NEW_SESSION"))
            {
                String idTel = arg1.getStringExtra("IDTEL");
                String prenom = arg1.getStringExtra("PRENOM");
                String nom = arg1.getStringExtra("NOM");
                Log.e("FOLLOW_NEW_SESSION",idTel);
                comm.sendMessage("FOLLOW$"+idTel+"*"+prenom+"*"+nom);
                alertManager.addListening(idTel); // AlertManager ecoutera les alertes provenants du serveur
            }

            if(arg1.hasExtra("ADDPROFIL")) {
                String newProfil = arg1.getStringExtra("ADDPROFIL");
                Log.d("ADDPROFIL",newProfil);
                comm.sendMessage("ADDPROFIL$" + newProfil);
            }

            if(arg1.hasExtra("SUPPRPROFIL")) {
                String rmProfils =  arg1.getStringExtra("SUPPRPROFIL");
                Log.d("SUPPRPROFIL", rmProfils);
                comm.sendMessage("SUPPRPROFIL$" + rmProfils);
            }

            if(arg1.hasExtra("MODIFPROFIL")) {
                String modifiedProfil = arg1.getStringExtra("MODIFPROFIL");  // ancienProfil*nouveauProfil
                Log.d("MODIFPROFIL", modifiedProfil);
                comm.sendMessage("MODIFPROFIL$" + modifiedProfil);

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
                if(status==NetworkUtil.NETWORK_STATUS_NOT_CONNECTED)
                {
                    if (connected)
                    {
                        // couper le socket
                    }

                }
                else if(status==NetworkUtil.NETWORK_STATUS_MOBILE || status== NetworkUtil.NETWORK_STATUS_WIFI)
                {
                    if(!connected)
                    {
                        // relancer le socket
                    }
                }
            }
        }
    }
}

package com.dg.apptabletteandroid.Daemon;

import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.os.IBinder;
import android.util.Log;

import com.dg.apptabletteandroid.AlertManager;
import com.dg.apptabletteandroid.Communication.CommunicationServer;
import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.NetworkUtil;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * Ce service recoit tous les messages provenants du serveur
 */
public class ServiceAdmin extends Service
{
    CommunicationServer comm; // le thread qui lancera un socket dédié à l'écoute du serveur
    ServerReceiver serverReceiver; // ce receiver attrapera tous les messages provenants du serveur
    ActivityReceiver activityReceiver; // ce receiver recevra tous les messages provenants de l'activité
    NetworkChangeReceiver networkReceiver; // ce receiver recevra l'etat de la connection de la tablette
    AlertManager alertManager; // le manager des alertes
    DataKeeper dataKeeper; // stocke tous les messages recus en attendant que l'activite revienne en premier plan
    private String token;

    public boolean establishedConnexionWithServer = false;

    boolean activity_is_on_background; // true -> application en background ; false -> application au premier plan

    public static String ACTION_FROM_SERVER = "action.from.server";
    public static String ACTION_FROM_ACTIVITY = "action.from.activity";

    public ServiceAdmin()
    {
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        this.alertManager = new AlertManager();

        this.dataKeeper = new DataKeeper();

        serverReceiver = new ServerReceiver();
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(ServiceAdmin.ACTION_FROM_SERVER);
        registerReceiver(serverReceiver, intentFilter);

        activityReceiver = new ActivityReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(ServiceAdmin.ACTION_FROM_ACTIVITY);
        registerReceiver(activityReceiver, intentFilter);

        networkReceiver = new NetworkChangeReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(networkReceiver, intentFilter);

        token = null;

        Log.e("CHECK SERVICE", "RUN");
        return START_NOT_STICKY;
    }

    public void endTask(CommunicationServer nwcomm,boolean success)
    {
        if (success)
        {
            establishedConnexionWithServer = true;
            this.comm = nwcomm;

        }
        else if (networkReceiver.connected)
        {
            Log.e("retry after 5s","!");
            nwcomm = new CommunicationServer(10000);
            nwcomm.setActionIntent(ServiceAdmin.ACTION_FROM_SERVER);
            nwcomm.setService(ServiceAdmin.this);
            nwcomm.start();
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
        unregisterReceiver(serverReceiver);
        unregisterReceiver(activityReceiver);
        unregisterReceiver(networkReceiver);
        if (comm != null)
        {
            comm.interrupt(); // arret du socket
        }
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
    private class ServerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context arg0, Intent arg1) {
            /**
             * Parsing du message provenant du serveur
             */
            String messagebrut = arg1.getStringExtra("MESSAGE");
            if (messagebrut == null) return;
            String[] allMessages = messagebrut.split("\\r?\\n");
            for (String message : allMessages)
            {
                boolean broadcastAlerte = false;
                Log.e("MESSAGERECEIVED",message);
                String[] tabMessage = message.split("\\$"); // syntaxe d'un message -> "Entete$contenu"
                String header = tabMessage[0]; // l'entete
                String content = tabMessage[1];
                if (tabMessage[0].equals("BROADCASTALERT"))
                {
                    broadcastAlerte = true;
                    header = tabMessage[1];
                    content = tabMessage[2];
                }

                Log.e("HEADER", header);
                Log.e("ALL", message);

                switch (header) {

                    case "TOKEN":
                    {
                        Log.e("TOKEN-RCV",content);
                        if (token != null)
                        {
                            Log.e("SENDCONTINUE","j");
                            ServiceAdmin.this.comm.sendMessage("CONTINUE$"+token);
                        }
                        establishedConnexionWithServer = true;
                        token = content;
                        break;
                    }
                    case "PROFILES": {
                        Log.e("ALL_PROFILES", content);

                        Intent intent = new Intent();
                        intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                        intent.putExtra("ALL_PROFILES", content);
                        if (activity_is_on_background) {
                            dataKeeper.addData(intent);
                        } else {
                            sendBroadcast(intent);
                        }

                        break;
                    }

                    case "UNFOLLOW":
                    {
                        String[] dataParams = content.split("\\_");
                        String idTel = dataParams[1];
                        String response = dataParams[0];
                        if (response.equals("ALLOW")) // UNFOLLOW OK
                        {
                            Log.e("UNFOLLOW AUTHORIZED", content);
                            Intent intent = new Intent();
                            intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                            intent.putExtra("UNFOLLOW_AUTHORIZED", "");
                            intent.putExtra("IDTEL", idTel);
                            alertManager.removeListening(idTel); // desabonnement aux alertes
                            if (activity_is_on_background)
                            { // si en background alors on notifiera l'activity à son retour.
                                dataKeeper.addData(intent);
                            } else {
                                sendBroadcast(intent);
                            }
                        }
                        else // UNFOLLOW INTERDICT
                        {

                        }
                        break;
                    }
                    case "ALERT":
                    {
                        String[] dataParams = content.split("\\_");
                        String typeAlert = dataParams[0];
                        String idTel = dataParams[1];

                        Log.e("ARGSALERT",typeAlert);

                        switch (typeAlert)
                        {
                            case "STARTHORSZONE":
                            {
                                alertManager.notifHorsZone(broadcastAlerte,getBaseContext(),idTel);
                                Intent intent = new Intent();
                                intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                                intent.putExtra("HORSZONE",true);
                                intent.putExtra("IDTEL", idTel);
                                if (activity_is_on_background)
                                {
                                    dataKeeper.addData(intent);
                                } else {
                                    sendBroadcast(intent);
                                }
                                break;
                            }

                            case "STOPHORSZONE":
                            {
                                Intent intent = new Intent();
                                intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                                intent.putExtra("HORSZONE",false);
                                intent.putExtra("IDTEL", idTel);
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

                            case "STARTBATTERY":
                            {
                                alertManager.notifBattery(broadcastAlerte,getBaseContext(), idTel);
                                Intent intent = new Intent();
                                intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                                intent.putExtra("STARTBATTERYLOW","");
                                intent.putExtra("IDTEL", idTel);
                                Log.e("STARTBATTERY","INTENTGEN");
                                if (activity_is_on_background)
                                {
                                    dataKeeper.addData(intent);
                                } else {
                                    sendBroadcast(intent);
                                }
                                break;
                            }

                            case "STOPBATTERY":
                            {
                                Intent intent = new Intent();
                                intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                                intent.putExtra("STOPBATTERYLOW","");
                                intent.putExtra("IDTEL", idTel);
                                if (activity_is_on_background)
                                {
                                    dataKeeper.addData(intent);
                                } else {
                                    sendBroadcast(intent);
                                }
                                break;
                            }

                            case "DURATION":
                            {
                                alertManager.notifPromenadeTimeout(broadcastAlerte,getBaseContext(), idTel);
                                Intent intent = new Intent();
                                intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                                intent.putExtra("PROMENADE_TIMEOUT","");
                                intent.putExtra("IDTEL", idTel);
                                if (activity_is_on_background)
                                {
                                    dataKeeper.addData(intent);
                                } else {
                                    sendBroadcast(intent);
                                }
                                break;
                            }

                            case "STARTTIMEOUTUPDATE":
                            {
                                alertManager.notifTimeoutUpdate(broadcastAlerte,getBaseContext(), idTel);
                                Intent intent = new Intent();
                                intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                                intent.putExtra("UPDATE_TIMEOUT_START","");
                                intent.putExtra("IDTEL", idTel);
                                if (activity_is_on_background)
                                {
                                    dataKeeper.addData(intent);
                                } else {
                                    sendBroadcast(intent);
                                }
                                break;
                            }

                            case "STOPTIMEOUTUPDATE":
                            {
                                Intent intent = new Intent();
                                intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                                intent.putExtra("UPDATE_TIMEOUT_STOP","");
                                intent.putExtra("IDTEL", idTel);
                                if (activity_is_on_background)
                                {
                                    dataKeeper.addData(intent);
                                } else {
                                    sendBroadcast(intent);
                                }
                                break;
                            }

                            case "IMMOBILE":
                            {
                                alertManager.notifImmobile(broadcastAlerte,getBaseContext(), idTel);
                                Intent intent = new Intent();
                                intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                                intent.putExtra("IMMOBILE_START","");
                                intent.putExtra("IDTEL", idTel);
                                if (activity_is_on_background)
                                {
                                    dataKeeper.addData(intent);
                                } else {
                                    sendBroadcast(intent);
                                }
                                break;
                            }

                            case "STOPIMMOBILITE":
                            {
                                Intent intent = new Intent();
                                intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                                intent.putExtra("STOPIMMOBILITE","");
                                intent.putExtra("IDTEL", idTel);
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
                    }

                    case "SYNCH": {

                        String[] dataParams = content.split("\\_");
                        String typeSynch = dataParams[0];

                        Log.e("ARGSSYNCH", dataParams[1]);

                        switch (typeSynch) {
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

                            case "SYNCH-CONTINUE":
                            {
                                String[] allProfilsEnPromenade = null;
                                Intent intent = new Intent();
                                intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                                intent.putExtra("SYNCHCONTINUE", "");

                                if (dataParams[1].equals("NONE"))
                                {
                                    intent.putExtra("SYNCH-NOPROMENADES",true);
                                }
                                else
                                {
                                    allProfilsEnPromenade = dataParams[1].split("\\*");
                                    intent.putExtra("SYNCH-ALLPROFILSPROMENADE",allProfilsEnPromenade);
                                }

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
                            case "STOPPROMENADE": {
                                alertManager.removeListening(dataParams[1]); // params est idTel ici
                                Log.e("STOP PROMENADE", dataParams[1]); // idtel
                                Intent intent = new Intent();
                                intent.setAction(Main2Activity.ACTION_FROM_SERVICE);
                                intent.putExtra("STOPPROMENADE", dataParams[1]);
                                if (activity_is_on_background) {
                                    dataKeeper.addData(intent);
                                } else {
                                    sendBroadcast(intent);
                                }
                                break;
                            }
                        }

                        break;
                    }

                    case "UPDATE": {
                        // METTRE A JOUR UN PROFIL SUIVI
                        // UPDATE$idTel*longitude*latitude
                        Log.e("UPDATE", content);
                        Intent intent = new Intent();
                        String idTel = content.split("\\*")[0];
                        Log.e("Temps Restant !",content.split("\\*")[4]);
                        intent.putExtra("UPDATE", content);
                        intent.setAction(Main2Activity.ACTION_FROM_SERVICE);

                        if (activity_is_on_background)
                        {
                            dataKeeper.addPosition(idTel,intent);
                        }
                        else
                        {
                            sendBroadcast(intent);
                        }

                        /**
                         * a completer
                         */
                        break;
                    }

                    case "NEWSESSION": {
                        // SELECTIONNER UN PROFIL
                        Log.e("NEWSESSION", content);
                        alertManager.notifNewSession(getBaseContext(), content);
                        break;
                    }

                    case "STOPNEWSESSION": {
                        NotificationManager notifManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

                        String idTel = content;
                        for(String alert : (ArrayList<String>)AlertManager.idTelAlertes.get(idTel).clone()){
                            if(alert.contains("NEWSESSION")){
                                String[] alertID = alert.split("\\$");
                                String idNotif = alertID[1];
                                notifManager.cancel(Integer.parseInt(idNotif));
                                AlertManager.idTelAlertes.get(idTel).remove(alert);
                            }
                        }
                    }
                }
            }
        }

    }

    /**
     * ActivityReceiver, recoit les messages venants de l'activite
     */
    private class ActivityReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context arg0, Intent arg1) {

            if (arg1.hasExtra("ACTIVITY_BACKGROUND")) {

                activity_is_on_background = arg1.getStringExtra("ACTIVITY_BACKGROUND").equals("BACKGROUND");

                if (activity_is_on_background) {
                    dataKeeper.subscrive(ServiceAdmin.this);

                } else {
                    dataKeeper.publish(ServiceAdmin.this);
                }
            }

            if (arg1.hasExtra("QUIT"))
            {
                stopSelf();
            }

            if (arg1.hasExtra("STOP_SOUND"))
            {
                Log.e("yoo","stopSOUND");
                alertManager.stopAudio();
            }


            if (arg1.hasExtra("FOLLOW_NEW_SESSION")) {
                String idTel = arg1.getStringExtra("IDTEL");
                String prenom = arg1.getStringExtra("PRENOM");
                String nom = arg1.getStringExtra("NOM");
                String duree = arg1.getStringExtra("DURATION");
                String maxImmobile = arg1.getStringExtra("MAXIMMOBILITE");
                boolean barriere = arg1.getBooleanExtra("FRANCHISSEMENTBARRIERE",false);
                Log.e("FOLLOW_NEW_SESSION", idTel);
                Log.e("DUREE", duree);


                comm.sendMessage("FOLLOW$" + idTel + "*" + prenom + "*" + nom + "*" + duree + "*" + maxImmobile + "*" + String.valueOf(barriere));
                alertManager.addListening(idTel); // AlertManager ecoutera les alertes provenants du serveur
            }

            if (arg1.hasExtra("FOLLOW_SESSION_OVERTAKE"))
            {
                String idTel = arg1.getStringExtra("IDTEL");
                Log.e("FOLLOW_SESSION_OVERTAKE",idTel);
                alertManager.addListening(idTel);
                // on envoie pas de follow vu que on follow deja... pas besoin d'incrementer nbFollower du tracker associé à idTel
            }

            if (arg1.hasExtra("FOLLOW_SESSION"))
            {
                String idTel = arg1.getStringExtra("IDTEL");
                Log.e("FOLLOW_SESSION",idTel);
                comm.sendMessage("FOLLOW$"+idTel);
                alertManager.addListening(idTel);
            }

            if (arg1.hasExtra("UNFOLLOW_SESSION"))
            {
                String idTel = arg1.getStringExtra("IDTEL");
                Log.e("UNFOLLOW_SESSION",idTel);
                comm.sendMessage("UNFOLLOW$"+idTel);
            }

            if(arg1.hasExtra("ADDPROFIL")) {
                String newProfil = arg1.getStringExtra("ADDPROFIL");
                Log.d("ADDPROFIL", newProfil);
                try {
                    comm.sendMessage("ADDPROFIL$" + newProfil);
                } catch (NullPointerException e) {  // Bancal, cas ou la tablette n'est pas connectée, A Definir plus bas !!
                    // Log.e("connected ", dataKeeper.)
                    // dataKeeper.subscrive(ServiceAdmin.this);
                    //  dataKeeper.addData(arg1);
                }

              /*if(networkReceiver != null && networkReceiver.connected) {
                   String newProfil = arg1.getStringExtra("ADDPROFIL");
                   Log.d("ADDPROFIL", newProfil);
                   comm.sendMessage("ADDPROFIL$" + newProfil);
                } else { } */
            }

            if (arg1.hasExtra("SUPPRPROFIL")) {
                String rmProfils = arg1.getStringExtra("SUPPRPROFIL");
                Log.d("SUPPRPROFIL", rmProfils);
                try {
                    comm.sendMessage("SUPPRPROFIL$" + rmProfils);
                } catch (NullPointerException e) {
                } // Bancal
            }

            if (arg1.hasExtra("MODIFPROFIL")) {
                String modifiedProfil = arg1.getStringExtra("MODIFPROFIL");  // ancienProfil*nouveauProfil
                Log.d("MODIFPROFIL", modifiedProfil);
                comm.sendMessage("MODIFPROFIL$" + modifiedProfil);

            }

            if(arg1.hasExtra("CHECKALERT")) {
                String idTel = arg1.getStringExtra("CHECKALERT");  // ancienProfil*nouveauProfil
                comm.sendMessage("CHECKALERT$" + idTel);
            }
        }
    }


    /**
     * NetworkChangeReceiver , écoute les changements d'états
     */
    public class NetworkChangeReceiver extends BroadcastReceiver {

        public static final String CONNECTIVITY_CHANGED = "android.net.conn.CONNECTIVITY_CHANGE";
        public boolean connected = false;


        @Override
        public void onReceive(final Context context, final Intent intent) {
            int status = NetworkUtil.getConnectivityStatusString(context);
            if (CONNECTIVITY_CHANGED.equals(intent.getAction())) {
                if (status == NetworkUtil.NETWORK_STATUS_NOT_CONNECTED) {
                    Log.e("ABAB", "not connected");
                    establishedConnexionWithServer = false;
                    if (connected && comm != null)
                    {
                        // couper le socket
                        comm.interrupt();
                        comm = null;
                        // notify activity
                        Intent intentForActivity = new Intent();
                        intentForActivity.setAction(Main2Activity.ACTION_FROM_SERVICE);
                        intentForActivity.putExtra("TABNOTCO", "TABNOTCO");

                        if (activity_is_on_background)
                        {
                            dataKeeper.addData(intentForActivity);
                        } else {
                            sendBroadcast(intentForActivity);  // A TEST
                        }

                    }
                    connected = false;

                } else if (status == NetworkUtil.NETWORK_STATUS_MOBILE || status == NetworkUtil.NETWORK_STATUS_WIFI) {
                    Log.e("ABAB", "connected");

                    if (!connected)
                    {
                        if (token != null)
                        {
                            alertManager.clear();// clear car lors d'un continu on se resynchronisera totalement
                        }
                        CommunicationServer comm = new CommunicationServer();
                        comm.setActionIntent(ServiceAdmin.ACTION_FROM_SERVER);
                        comm.setService(ServiceAdmin.this);
                        comm.start();

                    }
                    connected = true;
                }
            }
        }
    }
}


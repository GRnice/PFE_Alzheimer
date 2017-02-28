package com.dg.apptabletteandroid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Vibrator;
import android.support.v4.app.NotificationCompat;


import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.Profils.ProfilsManager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Remy on 15/12/2016.
 */

public class AlertManager
{
    private ArrayList<String> listOfIdTelOnListening; // liste des idTels a écouter
    private MediaPlayer mediaPlayer;
    public static HashMap<String, ArrayList<String>> idTelAlertes = new HashMap<>();

    public AlertManager()
    {
        this.listOfIdTelOnListening = new ArrayList<>();
        mediaPlayer = new MediaPlayer();
    }

    /**
     * alertManager ecoutera les alertes pour cet idTel
     * @param idTel
     */
    public void addListening(String idTel)
    {
        if (!listOfIdTelOnListening.contains(idTel))
        {
            this.listOfIdTelOnListening.add(idTel);
        }
    }

    /**
     * alertManager n'ecoutera plus les alertes pour cet idTel
     * @param idTel
     */
    public void removeListening(String idTel)
    {
        if (listOfIdTelOnListening.contains(idTel))
        {
            this.listOfIdTelOnListening.remove(idTel);
        }
    }

    private void sendNotification(Context context,String idTel,int idicone,String typeNotif,String titre,String message, boolean makeSound)
    {
        Intent intent = new Intent(context,Main2Activity.class);
        intent.putExtra("WAKE_UP",typeNotif);
        intent.putExtra("IDTEL",idTel);
        int idNotif = (int)System.currentTimeMillis();
        if(idTelAlertes.get(idTel) == null){
            ArrayList<String> alertes = new ArrayList<>();
            alertes.add(typeNotif + "$" + idNotif);
            idTelAlertes.put(idTel, alertes);
        }else{
            idTelAlertes.get(idTel).add(typeNotif + "$" + idNotif);
        }
        intent.putExtra("IDNOTIF",idNotif);

        PendingIntent pintent = PendingIntent.getActivity(context,idNotif,intent,0);
        NotificationCompat.Builder mBuild =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(idicone)
                        .setContentTitle(titre)
                        .setContentIntent(pintent)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setContentText(message);

        Notification notif = mBuild.build();

        notif.flags |= Notification.FLAG_AUTO_CANCEL;


        // Sets an ID for the notification
        int mNotificationId = idNotif;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, notif);
//        RingtoneManager mRing = new RingtoneManager(context);
//        int mNumberOfRingtones = mRing.getCursor().getCount();
//        Uri mRingToneUri = mRing.getRingtoneUri((int) (Math.random() * mNumberOfRingtones));

        if(makeSound) {
            Uri defaultRingtoneUri = RingtoneManager.getActualDefaultRingtoneUri(context.getApplicationContext(), RingtoneManager.TYPE_RINGTONE);


            try {
                mediaPlayer.stop();
                mediaPlayer = new MediaPlayer();
                mediaPlayer.setDataSource(context, defaultRingtoneUri);
                mediaPlayer.prepare();
                mediaPlayer.start(); // no need to call prepare(); create() does that for you
            } catch (IOException e) {
                e.printStackTrace();
            }


        }

        Vibrator vibrator = (Vibrator) context.getSystemService(Context.VIBRATOR_SERVICE);
        vibrator.vibrate(2000);

    }

    public void stopAudio()
    {
        mediaPlayer.stop();
    }

    public boolean listen(String idTel)
    {
        return (listOfIdTelOnListening.contains(idTel));
    }

    public void notifNewSession(Context context,String idTel)
    {
        sendNotification(context,idTel,R.drawable.ic_report_problem_white,"NEWSESSION","Nouvelle promenade , id dispositif : "+idTel.substring(0,6),
                "Configurer la promenade ?",false);
    }


    public void notifHorsZone(boolean broadcastAlert,Context context,String idTel)
    {
        if (!broadcastAlert && !listen(idTel)) return;
        sendNotification(context,idTel,R.drawable.ic_report_problem_white,"ALERTEHORSZONE","ALERTE - HORS ZONE","Hors zone detecté ! cliquez pour désactiver l'alerte", true);
    }

    public void notifBattery(boolean broadcastAlert,Context context, String idTel) {
        if (!broadcastAlert && !listen(idTel)) return;
        Profil profil = Main2Activity.profilsManager.getProfilOnPromenade(idTel);
        sendNotification(context,idTel,R.drawable.ic_battery_alert,"ALERTEBATTERY","ALERTE - BATTERIE","Battery de " + profil.getPrenom() + " " + profil.getNom() + " est faible!", true);
    }
    public void notifImmobile(boolean broadcastAlert,Context context, String idTel)
    {
        if (!broadcastAlert && !listen(idTel)) return;
        Profil profil = Main2Activity.profilsManager.getProfilOnPromenade(idTel);
        profil.setImmobile(true);
        sendNotification(context,idTel,R.drawable.ic_report_problem_white,"ALERTEIMMOBILE","ALERTE - IMMOBILITE",profil.getPrenom() + " " + profil.getNom() + " est immobile!\"", true);

    }

    public void notifPromenadeTimeout(boolean broadcastAlert,Context baseContext, String idTel) {
        if (!broadcastAlert && !listen(idTel)) return;
        Profil profil = Main2Activity.profilsManager.getProfilOnPromenade(idTel);
        sendNotification(baseContext,idTel,R.drawable.ic_directions_walk,"ALERTEPROMENADETIMEOUT","ALERTE - PROMENADE TERMINEE",profil.getPrenom() + " " + profil.getNom() + " a terminé la promenade", true);
    }

    public void notifTimeoutUpdate(boolean broadcastAlert,Context baseContext, String idTel)
    {
        if (!broadcastAlert && !listen(idTel)) return;
        Profil profil = Main2Activity.profilsManager.getProfilOnPromenade(idTel);
        sendNotification(baseContext,idTel,R.drawable.ic_report_problem_white,"ALERTETIMEOUTUPDATE","ALERTE - PERTE SUIVI",profil.getPrenom() + " " + profil.getNom() + " ne répond plus depuis au moins 20 secondes !", true);
    }

    public void clear()
    {
        listOfIdTelOnListening.clear();
    }
}

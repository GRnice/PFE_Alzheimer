package com.dg.apptabletteandroid;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;


import java.io.IOException;
import java.util.ArrayList;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Remy on 15/12/2016.
 */

public class AlertManager
{
    private ArrayList<String> listOfIdTelOnListening; // liste des idTels a écouter

    public AlertManager()
    {
        this.listOfIdTelOnListening = new ArrayList<>();
    }

    /**
     * alertManager ecoutera les alertes pour cet idTel
     * @param idTel
     */
    public void addListening(String idTel)
    {
        this.listOfIdTelOnListening.add(idTel);
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

    public boolean listen(String idTel)
    {
        return (listOfIdTelOnListening.contains(idTel));
    }

    public void notifNewSession(Context context,String idTel)
    {

        Intent intent = new Intent(context,Main2Activity.class);
        intent.putExtra("WAKE_UP","NEWSESSION");
        intent.putExtra("IDTEL",idTel);

        PendingIntent pintent = PendingIntent.getActivity(context,(int) System.currentTimeMillis(),intent,0);
        NotificationCompat.Builder mBuild =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_report_problem_white)
                        .setContentTitle("Demande d'un suivi")
                        .setContentIntent(pintent)
                        .setPriority(Notification.PRIORITY_HIGH)
                        .setAutoCancel(true)
                        .setContentText("Nouvelle promenade, configurer ?");

        Notification notif = mBuild.build();

        notif.flags |= Notification.FLAG_AUTO_CANCEL;


        // Sets an ID for the notification
        int mNotificationId = (int) System.currentTimeMillis();
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, notif);
    }


    public void notifHorsZone(Context context,String idTel)
    {
        if (!listen(idTel)) return;
        Intent intent = new Intent(context,Main2Activity.class);
        intent.putExtra("WAKE_UP","ALERTE");
        intent.putExtra("IDTEL",idTel);

        PendingIntent pintent = PendingIntent.getActivity(context,(int) System.currentTimeMillis(),intent,0);
        NotificationCompat.Builder mBuild =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_report_problem_white)
                        .setContentTitle("ALERTE - HORS ZONE")
                        .setContentIntent(pintent)
                        .setAutoCancel(true)
                        .setPriority(Notification.PRIORITY_MAX)
                        .setContentText("Hors zone detecté ! cliquez pour désactiver l'alerte");

        Notification notif = mBuild.build();

        notif.flags |= Notification.FLAG_AUTO_CANCEL;


        // Sets an ID for the notification
        int mNotificationId = (int) System.currentTimeMillis();
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, notif);
        RingtoneManager mRing = new RingtoneManager(context);
        int mNumberOfRingtones = mRing.getCursor().getCount();
        Uri mRingToneUri = mRing.getRingtoneUri((int) (Math.random() * mNumberOfRingtones));
        MediaPlayer mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(context, mRingToneUri);
            mediaPlayer.prepare();
            mediaPlayer.start(); // no need to call prepare(); create() does that for you
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}

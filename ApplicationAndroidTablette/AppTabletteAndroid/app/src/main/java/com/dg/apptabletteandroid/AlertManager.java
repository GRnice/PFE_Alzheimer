package com.dg.apptabletteandroid;

import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat;

import static android.content.Context.NOTIFICATION_SERVICE;

/**
 * Created by Remy on 15/12/2016.
 */

public class AlertManager
{
    public AlertManager()
    {

    }

    public void notifNewSession(Context context,String idTracker)
    {
        Intent intent = new Intent(context,Main2Activity.class);
        intent.putExtra("WAKE_UP","NEWSESSION");
        intent.putExtra("IDTEL",idTracker);

        PendingIntent pintent = PendingIntent.getActivity(context,(int) System.currentTimeMillis(),intent,0);
        NotificationCompat.Builder mBuild =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.ic_report_problem)
                        .setContentTitle("My notification")
                        .setContentIntent(pintent)
                        .setContentText("Nouvelle promenade, configurer ?");

        Notification notif = mBuild.build();

        notif.flags |= Notification.FLAG_AUTO_CANCEL;


        // Sets an ID for the notification
        int mNotificationId = 1;
        // Gets an instance of the NotificationManager service
        NotificationManager mNotifyMgr = (NotificationManager) context.getSystemService(NOTIFICATION_SERVICE);
        // Builds the notification and issues it.
        mNotifyMgr.notify(mNotificationId, notif);
    }
}

package com.dg.gpsalzheimersmartphone;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.job.JobInfo;
import android.app.job.JobParameters;
import android.app.job.JobScheduler;
import android.app.job.JobService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.Calendar;
import java.util.List;

import static android.os.Build.VERSION.SDK;
import static android.os.Build.VERSION.SDK_INT;

/**
 * Created by Remy on 27/02/2017.
 */

/**
 * Cette classe fontionne comme une Alarme,activée, elle appelle chaque 10 secondes la methode sendUpdate de ServiceSocket.
 */
public class ScheduleSender extends BroadcastReceiver
{
    private PendingIntent pendingIntent;
    private boolean alarmSet;
    private static ServiceSocket service;


    public ScheduleSender()
    {
        super();
        alarmSet = false;

    }

    /**
     * Demarrer l'alarme
     * @param serviceSocket
     */
    public void startRemainder(ServiceSocket serviceSocket)
    {
        if (alarmSet)
        {
            stopRemainder(serviceSocket);
        }
        alarmSet = true;

        Intent alarmIntent = new Intent(serviceSocket, ScheduleSender.class);
        service = serviceSocket;
        pendingIntent = PendingIntent.getBroadcast(serviceSocket, 0, alarmIntent, 0);

        AlarmManager manager = (AlarmManager) serviceSocket.getSystemService(Context.ALARM_SERVICE);
        manager.setRepeating(AlarmManager.RTC_WAKEUP,System.currentTimeMillis(),10000,pendingIntent); // forcé à 60000 sur android 5.1 !!
        // Voir la classe JobScheduler du SDK
    }

    /**
     * Couper l'alarme
     * @param serviceSocket
     */
    public void stopRemainder(ServiceSocket serviceSocket)
    {
        alarmSet = false;
        AlarmManager manager = (AlarmManager) serviceSocket.getSystemService(Context.ALARM_SERVICE);
        manager.cancel(pendingIntent);
    }

    /**
     * onReceive appellé toutes les 10 secondes.
     * @param context
     * @param intent
     */
    @Override
    public void onReceive(Context context, Intent intent)
    {
        Log.e("WAKEUP-ALARM","NOW");

        if (ScheduleSender.service != null)
        {
            Log.e("WAKEUP-ALARM","NOT NULL");
            ScheduleSender.service.sendUpdate();
        }

    }
}

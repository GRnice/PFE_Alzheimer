package com.dg.gpsalzheimersmartphone;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.support.v4.content.WakefulBroadcastReceiver;

public class WakefulReceiver extends WakefulBroadcastReceiver {
    public WakefulReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent)
    {
        startWakefulService(context,intent);
    }
}

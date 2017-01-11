package com.dg.apptabletteandroid.Daemon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.location.Location;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by Remy on 10/01/2017.
 */

public class DataKeeper
{
    private ArrayList<Intent> tampon;
    private ArrayList<Intent> informations;
    private AtomicBoolean onPublish;
    private ActivityReceiverChecking activityReceiverChecking;

    public DataKeeper()
    {
        onPublish = new AtomicBoolean();
        onPublish.set(false);
        this.tampon = new ArrayList<>();
        this.informations = new ArrayList<>();
    }

    public void addData(Intent message)
    {
        Log.e("addData","dataaa");
        this.informations.add(message);
    }



    public void subscrive(Context ctx)
    {
        if (onPublish.get())
        {
            try
            {
                ctx.unregisterReceiver(activityReceiverChecking);
            }
            catch(Exception err)
            {

            }
            onPublish.set(false);
        }
    }


    public void publish(Context ctx)
    {
        activityReceiverChecking = new ActivityReceiverChecking(ctx,informations.iterator());
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.checkingActivity");
        ctx.registerReceiver(activityReceiverChecking,intentFilter);
    }

    private class ActivityReceiverChecking extends BroadcastReceiver
    {

        private Context ctx;
        private Iterator<Intent> informations;

        public ActivityReceiverChecking(Context ctx,Iterator<Intent> informations)
        {
            super();
            this.ctx = ctx;
            onPublish.set(true);
            this.informations = informations;
            this.onReceive(null,null);
        }

        @Override
        public void onReceive(Context context, Intent intent)
        {
            if (this.informations.hasNext())
            {
                Log.e("SEND","INTENT SYNCH");
                Intent aIntent =  this.informations.next();
                aIntent.putExtra("SYNCH_ACTIVITY","android.checkingActivity");
                ctx.sendBroadcast(aIntent);
                this.informations.remove();
            }
            else
            {
                try
                {
                    ctx.unregisterReceiver(this);
                }
                catch(Exception err)
                {

                }

                onPublish.set(false);
            }
        }
    }

}

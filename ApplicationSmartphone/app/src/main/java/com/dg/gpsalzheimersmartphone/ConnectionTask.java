package com.dg.gpsalzheimersmartphone;

import android.os.AsyncTask;

import static com.dg.gpsalzheimersmartphone.MainActivity.android_id;

/**
 * Created by Remy on 28/02/2017.
 */

public class ConnectionTask extends AsyncTask
{
    ServiceSocket serv;
    public ConnectionTask(ServiceSocket service)
    {
        super();
        serv = service;
    }
    @Override
    protected Object doInBackground(Object[] params)
    {
        int tryMax = 3;

        CommunicationServer comm = new CommunicationServer();
        comm.setActionIntent(ServiceSocket.ACTION_SEND_TO_ACTIVITY);
        comm.setService(serv);
        comm.start();

        while (tryMax >= 0)
        {
            try
            {
                //Wait for input and output to be initialised in comm object
                Thread.sleep(3000);
            }
            catch (InterruptedException e)
            {
                e.printStackTrace();
            }

            if (comm.isReady())
            {
                serv.endTask(comm,true);
                return 0;
            }
            tryMax--;
        }
        comm.interrupt();
        serv.endTask(comm,false);
        return 1;
    }
}

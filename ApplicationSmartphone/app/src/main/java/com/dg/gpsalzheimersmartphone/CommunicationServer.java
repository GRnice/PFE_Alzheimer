package com.dg.gpsalzheimersmartphone;


import android.app.Service;

import android.content.Intent;
import android.util.Log;


import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.PrintWriter;
import java.net.Socket;

import static com.dg.gpsalzheimersmartphone.ServiceSocket.maxTime;

/**
 *
 * Class used to manage the socket (IO) with the server
 * Connect to SOCKET_ADDR, set receivers and emit events
 *
 * Created by Team PFE 2016-2017 Cheebane-Dib-Giangrasso-Hossam
 */

public class CommunicationServer extends Thread implements Runnable
{

    public static final String SOCKET_ADDR = "192.168.1.13";

    public static final int PORT = 3000;
    public static final String OKPROMENADE = "OKPROMENADE";
    public static final String STOPSUIVI = "STOPSUIVI";
    public static final String CONNECTED = "CONNECTED";
    private Socket m_sock;
    private BufferedReader input;
    private PrintWriter output;
    private String actionIntent;
    private Service service;
    boolean run;


    public CommunicationServer()
    {
        super();
    }

    public synchronized void setActionIntent(String action)
    {
        actionIntent = action;
    }

    public synchronized boolean isReady()
    {
        return m_sock != null;
    }

    public synchronized void setService(Service ser)
    {
        this.service = ser;
    }
    @Override
    public void run()
    {
        String line;

        try
        {
            m_sock = new Socket(SOCKET_ADDR, PORT);
        }
        catch (IOException e)
        {

            e.printStackTrace();
            return;
        }

        try
        {
            input = new BufferedReader(new InputStreamReader(m_sock.getInputStream()));
            output = new PrintWriter(m_sock.getOutputStream(),true);
        } catch (IOException e)
        {
            e.printStackTrace();
        }
        this.run = true;
        Intent intent;
        while(this.run)
        {
            intent = new Intent();
            intent.setAction(ServiceSocket.ACTION_RECEIVE_FROM_SERVER);
            try
            {
                line = input.readLine();

                if (line != null)
                {
                    if(line.equals(STOPSUIVI)){
                        intent.putExtra(STOPSUIVI, true);
                    }
                    else if (line.equals(CONNECTED))
                    {
                        intent.putExtra(CONNECTED,"");
                    }
                    else if(line.contains(OKPROMENADE))
                    {
                        if(line.contains("*")){
                            String []args= line.split("\\*");
                            maxTime = Long.parseLong(args[1])*60*1000;
                        }

                        intent.putExtra(OKPROMENADE, true);
                    }
                    synchronized (this.service)
                    {
                        this.service.sendBroadcast(intent);
                    }
                    Log.e("RECEIVE THIS -> ",line);
                }

            } catch (IOException e)
            {
                e.printStackTrace();
                break;
            }
        }

    }

    public synchronized boolean sendMessage(String message)
    {
        if (this.output == null)
        {
            return false;
        }
        this.output.println(message);
        return true;
    }

    public synchronized void deconnect()
    {
        if (this.m_sock == null)
        {
            return;
        }
        try {
            this.m_sock.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void interrupt()
    {
        // liberer la ressource
        this.run = false;
        this.deconnect();
        super.interrupt();
    }

    public synchronized Socket getSocket() {
        return m_sock;
    }
}


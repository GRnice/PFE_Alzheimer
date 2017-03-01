package com.dg.apptabletteandroid.Communication;

/**
 * Created by Remy on 15/12/2016.
 */

import android.app.Service;

import android.content.Intent;
import android.util.Log;


import com.dg.apptabletteandroid.Daemon.ServiceAdmin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import java.io.PrintWriter;
import java.net.Socket;

/**
 *
 * Class used to manage the socket (IO) with the server
 * Connect to SOCKET_ADDR, set receivers and emit events
 *
 * Created by Team PFE 2016-2017 Cheebane-Dib-Giangrasso-Hossam
 */
public class CommunicationServer extends Thread implements Runnable
{
    public static final String SOCKET_ADDR = "192.168.1.13"; // "31.220.57.38"; // VM

    public static final int PORT = 3100;
    private Socket m_sock;
    private BufferedReader input;
    private PrintWriter output;
    private String actionIntent;
    private ServiceAdmin service;
    int delayBeforeConnection;
    boolean run;

    public CommunicationServer()
    {
        super();
        delayBeforeConnection = 0;
    }

    public CommunicationServer(int delayMs)
    {
        delayBeforeConnection = delayMs;
    }

    public synchronized void setActionIntent(String action)
    {
        actionIntent = action;
    }


    public synchronized void setService(ServiceAdmin ser)
    {
        this.service = ser;
    }


    @Override
    public void run()
    {
        String line;
        try
        {
            Thread.sleep(delayBeforeConnection);
        }
        catch (InterruptedException e)
        {
            e.printStackTrace();
        }
        try
        {
            m_sock = new Socket(SOCKET_ADDR, PORT);
        }
        catch (IOException e)
        {
            service.endTask(this,false);
            e.printStackTrace();
            return;
        }
        service.endTask(this,true);
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
            intent.setAction(actionIntent);

            try
            {
                line = input.readLine();

                if (line != null)
                {
                    intent.putExtra("MESSAGE",line);
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

    public synchronized boolean sendMessage(String message) throws NullPointerException
    {
        if (output == null) return false;
        this.output.println(message);
        return true;
    }

    public synchronized void deconnect()
    {
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

    public synchronized Socket getSocket()
    {
        return m_sock;
    }

    public boolean isReady()
    {
        return m_sock != null;
    }
}


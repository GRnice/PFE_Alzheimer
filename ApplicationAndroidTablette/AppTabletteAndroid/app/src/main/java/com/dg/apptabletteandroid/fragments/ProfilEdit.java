package com.dg.apptabletteandroid.fragments;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dg.apptabletteandroid.Profils.ProfilsManager;
import com.dg.apptabletteandroid.R;

/**
 * Created by dominiquedib on 04/01/2017.
 */

public class ProfilEdit extends Fragment {

    // add profile attribut
    //onBACK je pull le fragment

    public ProfilEdit() {}

    public static ProfilEdit newInstance()
    {
        ProfilEdit fragment = new ProfilEdit();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profil_edit, container, false);
        return view;
    }


    @Override
    public void onAttach(Context context)
    {
        super.onAttach(context);
    }

    @Override
    public void onDetach()
    {
        super.onDetach();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();
    }

    @Override
    public void onPause()
    {
        super.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
    }

}

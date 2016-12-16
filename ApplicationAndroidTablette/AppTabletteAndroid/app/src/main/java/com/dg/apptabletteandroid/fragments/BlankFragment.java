package com.dg.apptabletteandroid.fragments;


import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dg.apptabletteandroid.R;


public class BlankFragment extends Fragment
{

    /**
     * Je sais j'ai craqué mais je voulais pas me prendre la tete x)
     */
    public BlankFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_blank, container, false);
    }

}

package com.dg.apptabletteandroid.fragments.Parametres;

import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.dg.apptabletteandroid.Daemon.ServiceAdmin;
import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.R;

/**
 * Created by Remy on 25/01/2017.
 */

public class ParameterFragment extends Fragment
{
    private Button btnOk;

    public ParameterFragment()
    {
        // Required empty public constructor
    }

    public static ParameterFragment newInstance()
    {
        ParameterFragment fragment = new ParameterFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_parameters, container, false);
        btnOk = (Button) view.findViewById(R.id.forceCloseApp);
        btnOk.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                Intent intentServiceSocket = new Intent(getActivity(),ServiceAdmin.class);
                getActivity().stopService(intentServiceSocket);
                ((Main2Activity) getActivity()).getProfilsManager().clearPromenade();
                getActivity().finish();
            }
        });
        return view;
    }
}

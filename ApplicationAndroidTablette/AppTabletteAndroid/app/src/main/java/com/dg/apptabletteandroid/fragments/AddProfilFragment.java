package com.dg.apptabletteandroid.fragments;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.dg.apptabletteandroid.Daemon.ServiceAdmin;
import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.R;

import java.util.ArrayList;
import java.util.Set;

/**
 * Created by dominiquedib on 13/01/2017.
 */

public class AddProfilFragment extends BlankFragment {

    public AddProfilFragment() {}


    public static AddProfilFragment newInstance()
    {
        AddProfilFragment fragment = new AddProfilFragment();
        return fragment;
    }


    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
    }


    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profil_add, container, false);

        final EditText prenom = (EditText) view.findViewById(R.id.editNameTextField);
        final EditText nom = (EditText) view.findViewById(R.id.editLastNameTextField);
        final Button buttonBarriere = (Button) view.findViewById(R.id.barriereButton);
        Button addButton = (Button) view.findViewById(R.id.buttonModifier);

        buttonBarriere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(buttonBarriere.getText().toString().equals("Oui")) {
                    buttonBarriere.setText("Non");

                } else {
                    buttonBarriere.setText("Oui");
                }
            }
        });

        final boolean barriereBool;
        if(buttonBarriere.getText().toString().equals("Oui")) {
            barriereBool = true;
        }
        else {
            barriereBool = false;
        }


        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String firstName = prenom.getText().toString();
                String lastName = nom.getText().toString();
                Log.d("New", nom + " " + prenom);
                Profil newProfile = new Profil(lastName, firstName, barriereBool);
                ((Main2Activity)getActivity()).getProfilsManager().getAllProfils().add(newProfile);

                // activity me donne le shared preference
                SharedPreferences shared = ((Main2Activity)getActivity()).getPreferences(Context.MODE_PRIVATE);
                ArrayList<Profil> listProfils = ((Main2Activity)getActivity()).getProfilsManager().getAllProfils();
                ((Main2Activity)getActivity()).getProfilsManager().updateList(shared, listProfils);

                // send au service
                Intent intent = new Intent();
                intent.setAction(ServiceAdmin.ACTION_FROM_ACTIVITY);
                intent.putExtra("ADDPROFIL", newProfile.makeSignature()); // nom,prenom,BarriereNormal
                getActivity().sendBroadcast(intent);

                onBackPressed();  // retourne au fragment list de tous les profil


            }
        });


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

    /**
     * Back pressed send from activity.
     *
     * @return if event is consumed, it will return true.
     */
    @Override
    public void onBackPressed() {
        Log.d("AAA", "BACKPressed ProfileEdit");

        Fragment fragProfils = ProfilFragment.newInstance(((Main2Activity)getActivity()).getProfilsManager());
        ((Main2Activity) getActivity()).pushFragmentFromActivity(fragProfils);
    }

}

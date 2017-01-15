package com.dg.apptabletteandroid.fragments.Profils;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.dg.apptabletteandroid.Daemon.ServiceAdmin;
import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.R;
import com.dg.apptabletteandroid.fragments.BlankFragment;

import java.util.ArrayList;

/**
 * Created by dominiquedib on 04/01/2017.
 */

public class ProfilEdit extends BlankFragment {
    private Profil profilSelected;

    public ProfilEdit() {}

    public ProfilEdit(Profil pr) {
        this.profilSelected = pr;
    }


    public static ProfilEdit newInstance(Profil pr)
    {
        ProfilEdit fragment = new ProfilEdit(pr);
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

        final EditText prenom = (EditText) view.findViewById(R.id.editNameTextField);
        final EditText nom = (EditText) view.findViewById(R.id.editLastNameTextField);
        final Button buttonBarriere = (Button) view.findViewById(R.id.barriereButton);
        Button modifButton = (Button) view.findViewById(R.id.buttonModifier);

        prenom.setText(this.profilSelected.getPrenom());
        nom.setText(this.profilSelected.getNom());

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


        modifButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = prenom.getText().toString();
                String lastName = nom.getText().toString();
                Log.d("New", name + " " + lastName);
                Profil oldProfil = new Profil(profilSelected.getNom(), profilSelected.getPrenom(), profilSelected.getSusceptibleDeFranchirLaBarriere());

                profilSelected.setNom(lastName);
                profilSelected.setPrenom(name);
                profilSelected.susceptibleDeFranchirLaBarriere(barriereBool);

                // update sharedPreference
                SharedPreferences shared = ((Main2Activity)getActivity()).getPreferences(Context.MODE_PRIVATE);
                ArrayList<Profil> listProfils = ((Main2Activity)getActivity()).getProfilsManager().getAllProfils();
                ((Main2Activity)getActivity()).getProfilsManager().updateList(shared, listProfils);

                // send au service
                // oldProfil*newProfil
                Intent intent = new Intent();
                intent.setAction(ServiceAdmin.ACTION_FROM_ACTIVITY);
                intent.putExtra("MODIFPROFIL", oldProfil.makeSignature()+ "*" + profilSelected.makeSignature());
                getActivity().sendBroadcast(intent);


                onBackPressed();  // retourn au fragment précedent (list des profil)


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

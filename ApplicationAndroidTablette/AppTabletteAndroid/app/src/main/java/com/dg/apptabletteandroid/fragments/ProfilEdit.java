package com.dg.apptabletteandroid.fragments;

import android.content.Context;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.dg.apptabletteandroid.Data.Profil;
import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.Profils.ProfilsManager;
import com.dg.apptabletteandroid.R;

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
                profilSelected.setNom(lastName);
                profilSelected.setPrenom(name);
                // Set pr la barriere
                // activity me donne le shared preference
                //((Main2Activity)getActivity()).getProfilManager().changeList(,);
                onBackPressed();

                 // send au serveur apres
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

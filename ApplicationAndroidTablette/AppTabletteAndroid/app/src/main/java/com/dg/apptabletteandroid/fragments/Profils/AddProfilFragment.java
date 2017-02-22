package com.dg.apptabletteandroid.fragments.Profils;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;

import com.dg.apptabletteandroid.Daemon.ServiceAdmin;
import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.R;
import com.dg.apptabletteandroid.fragments.BlankFragment;

import java.util.ArrayList;

/**
 * Created by dominiquedib on 13/01/2017.
 */

public class AddProfilFragment extends BlankFragment {

    private AdapterSpinnerAvatar adapterSpinnerAvatar;
    private ArrayList<Integer> arrayAvatars;
    private Profil profilModif;

    public AddProfilFragment() {}


    public static AddProfilFragment newInstance(@Nullable Profil profilModifier)
    {
        AddProfilFragment fragment = new AddProfilFragment();
        fragment.profilModif = profilModifier;
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
        final Spinner spinnerSelectAvatar = (Spinner) view.findViewById(R.id.spinner_select_avatar);
        Button addButton = (Button) view.findViewById(R.id.buttonModifier);
        Button cancelButton = (Button) view.findViewById(R.id.buttonAnnulerAjout);

        arrayAvatars = new ArrayList<>();
        arrayAvatars.add(R.drawable.avatar_bleu);
        arrayAvatars.add(R.drawable.avatar_rouge);
        arrayAvatars.add(R.drawable.avatar_vert);

        adapterSpinnerAvatar = new AdapterSpinnerAvatar(getActivity(),R.layout.item_adapter_avatar_listing,arrayAvatars);
        adapterSpinnerAvatar.setDropDownViewResource(R.layout.item_adapter_avatar_listing);
        spinnerSelectAvatar.setAdapter(adapterSpinnerAvatar);
        spinnerSelectAvatar.getSelectedItemPosition();

        buttonBarriere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(buttonBarriere.getText().toString().equals("OUI")) {
                    buttonBarriere.setText("NON");

                } else {
                    buttonBarriere.setText("OUI");
                }
            }
        });

        if (profilModif != null)
        {
            getActivity().setTitle("Modifier un profil");
            nom.setText(profilModif.getNom());
            prenom.setText(profilModif.getPrenom());
            buttonBarriere.setText("NON");
            if (profilModif.getSusceptibleDeFranchirLaBarriere())
            {
                buttonBarriere.setText("OUI");

            }
            addButton.setText("MODIFIER");
            int spinnerPosition = arrayAvatars.indexOf(profilModif.getIdRessourcesAvatar());
            spinnerSelectAvatar.setSelection(spinnerPosition);

        }
        else
        {
            getActivity().setTitle("Creer un profil");
        }




        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {  // traiter le cas du offline .. !! recheck Service Admin a la fin

                String newPrenom = prenom.getText().toString();
                String newNom = nom.getText().toString();
                Log.d("New", nom + " " + prenom);
                int idAvatar = arrayAvatars.get(spinnerSelectAvatar.getSelectedItemPosition());
                Log.e("idAvatar",String.valueOf(idAvatar));
                final boolean barriereBool;

                if(buttonBarriere.getText().toString().equals("OUI")) {
                    barriereBool = true;
                }
                else {
                    barriereBool = false;
                }


                if (profilModif != null)
                {
                    Profil oldProfil = new Profil(profilModif.getNom(),
                            profilModif.getPrenom(),
                            profilModif.getSusceptibleDeFranchirLaBarriere(),
                            profilModif.getIdRessourcesAvatar());

                    profilModif.setPrenom(newPrenom);
                    profilModif.setNom(newNom);
                    profilModif.susceptibleDeFranchirLaBarriere(barriereBool);
                    profilModif.setIdAvatar(idAvatar);

                    // update sharedPreference
                    SharedPreferences shared = ((Main2Activity) getActivity()).getPreferences(Context.MODE_PRIVATE);
                    ArrayList<Profil> listProfils = ((Main2Activity)getActivity()).getProfilsManager().getAllProfils();
                    ((Main2Activity)getActivity()).getProfilsManager().updateList(shared, listProfils);
                    Intent intent = new Intent();
                    intent.setAction(ServiceAdmin.ACTION_FROM_ACTIVITY);
                    intent.putExtra("MODIFPROFIL", oldProfil.makeSignature()+ "*" + profilModif.makeSignature());
                    getActivity().sendBroadcast(intent);

                }
                else
                {
                    Profil newProfile = new Profil(newNom,newPrenom,barriereBool,idAvatar);
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
                }

                onBackPressed();  // retourn au fragment précedent (list des profil)

            }
        });

        cancelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onBackPressed();
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

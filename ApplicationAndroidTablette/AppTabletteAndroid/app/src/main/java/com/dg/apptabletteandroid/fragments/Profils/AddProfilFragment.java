package com.dg.apptabletteandroid.fragments.Profils;

import android.app.Fragment;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.dg.apptabletteandroid.Daemon.ServiceAdmin;
import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.NetworkUtil;
import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.R;
import com.dg.apptabletteandroid.fragments.BlankFragment;

import java.util.ArrayList;

import static android.R.color.holo_green_light;
import static android.R.color.holo_red_dark;

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
        TextView titre =(TextView)view.findViewById(R.id.titre);
        TextView ajouter=(TextView)view.findViewById(R.id.add);
        arrayAvatars = new ArrayList<>();
        arrayAvatars.add(R.drawable.avatar_bleu);
        arrayAvatars.add(R.drawable.avatar_rouge);
        arrayAvatars.add(R.drawable.avatar_vert);
        arrayAvatars.add(R.drawable.avatar_orange);
        adapterSpinnerAvatar = new AdapterSpinnerAvatar(getActivity(),R.layout.item_adapter_avatar_listing,arrayAvatars);
        adapterSpinnerAvatar.setDropDownViewResource(R.layout.item_adapter_avatar_listing);
        spinnerSelectAvatar.setAdapter(adapterSpinnerAvatar);
        spinnerSelectAvatar.getSelectedItemPosition();
        buttonBarriere.setBackgroundColor(Color.rgb(153,204,0));
        buttonBarriere.setText("Pas susceptible de franchir la barrière");

        buttonBarriere.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(((ColorDrawable)buttonBarriere.getBackground()).getColor() == Color.RED )  {
                    buttonBarriere.setBackgroundColor(Color.rgb(153,204,0));
                    buttonBarriere.setText("Pas susceptible de franchir la barrière");

                } else {
                    buttonBarriere.setBackgroundColor(Color.RED);
                    buttonBarriere.setText("SUSCEPTIBLE DE FRANCHIR LA BARRIÈRE");
                }
            }
        });

        if (profilModif != null)
        {
            getActivity().setTitle("Modifier un profil");
            nom.setText(profilModif.getNom());
            prenom.setText(profilModif.getPrenom());
            if (!profilModif.getSusceptibleDeFranchirLaBarriere())
            {
                buttonBarriere.setBackgroundColor(Color.rgb(153,204,0));
                buttonBarriere.setText("Pas susceptible de franchir la barrière");

            }else{              buttonBarriere.setBackgroundColor(Color.RED);
                buttonBarriere.setText("SUSCEPTIBLE DE FRANCHIR LA BARRIÈRE");
            }
            addButton.setText("MODIFIER");
            titre.setText("Modifier un profil");
            int spinnerPosition = arrayAvatars.indexOf(profilModif.getIdRessourcesAvatar());
            spinnerSelectAvatar.setSelection(spinnerPosition);

        }
        else
        {
            getActivity().setTitle("Creer un profil");
        }

        addButton.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {  // traiter le cas du offline .. !! recheck Service Admin a la fin

                if (NetworkUtil.getConnectivityStatus(getActivity()) == 0)
                {
                    Toast.makeText(getActivity(),"Connectez vous au réseau",Toast.LENGTH_LONG).show();
                    return;
                }
                String newPrenom = prenom.getText().toString();
                String newNom = nom.getText().toString();
                Log.d("New", nom + " " + prenom);
                int idAvatar = arrayAvatars.get(spinnerSelectAvatar.getSelectedItemPosition());
                Log.e("idAvatar",String.valueOf(idAvatar));
                final boolean barriereBool;
                if(buttonBarriere.getText().toString().equals("SUSCEPTIBLE DE FRANCHIR LA BARRIÈRE")) {
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
                    if (oldProfil.getSusceptibleDeFranchirLaBarriere())
                    {
                        buttonBarriere.setText("SUSCEPTIBLE DE FRANCHIR LA BARRIÈRE");
                        buttonBarriere.setBackgroundColor(Color.RED);}
                    else {
                        buttonBarriere.setBackgroundColor(Color.rgb(153,204,0));
                        buttonBarriere.setText("Pas susceptible de franchir la barrière");
                    }
                    profilModif.setPrenom(newPrenom);
                    profilModif.setNom(newNom);
                    profilModif.susceptibleDeFranchirLaBarriere(barriereBool);
                    if (profilModif.getSusceptibleDeFranchirLaBarriere())
                    {
                        buttonBarriere.setText("SUSCEPTIBLE DE FRANCHIR LA BARRIÈRE");
                        buttonBarriere.setBackgroundColor(Color.RED);}
                    else {
                        buttonBarriere.setBackgroundColor(Color.rgb(153,204,0));
                        buttonBarriere.setText("Pas susceptible de franchir la barrière");
                    }
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

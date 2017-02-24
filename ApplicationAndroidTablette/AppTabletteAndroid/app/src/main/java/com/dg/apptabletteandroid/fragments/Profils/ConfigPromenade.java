package com.dg.apptabletteandroid.fragments.Profils;


import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.telephony.PhoneNumberUtils;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dg.apptabletteandroid.Daemon.ServiceAdmin;
import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.Profils.ProfilsManager;
import com.dg.apptabletteandroid.R;
import com.dg.apptabletteandroid.fragments.BlankFragment;
import com.dg.apptabletteandroid.fragments.Map.MapFragment_;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link ConfigPromenade#newInstance} factory method to
 * create an instance of this fragment.
 */
public class ConfigPromenade extends BlankFragment {



    private ProfilsManager profManager;
    private Profil profilSelected;
    private String idTel;

    public ConfigPromenade() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @return A new instance of fragment ConfigPromenade.
     */
    // TODO: Rename and change types and number of parameters
    public static ConfigPromenade newInstance(ProfilsManager profilManager,Profil profilSelected, String idTel) {
        ConfigPromenade fragment = new ConfigPromenade();
        fragment.idTel = idTel;
        fragment.profilSelected = profilSelected;
        fragment.profManager = profilManager;

        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        final View v = inflater.inflate(R.layout.fragment_config_promenade, container, false);
        TextView nomprenom = (TextView) v.findViewById(R.id.textViewConfigomPrenom);
        String nomPrenom = profilSelected.getPrenom() + "" + profilSelected.getNom();
        ImageView image =(ImageView)v.findViewById(R.id.imageProfilSelected);
        image.setImageDrawable(getResources().getDrawable(profilSelected.getIdRessourcesAvatar()));
        nomprenom.setText(nomPrenom);

        Button back = (Button) v.findViewById(R.id.button_back_config_prom);
        back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                ProfilFragment profilFragment = ProfilFragment.newInstance(profManager,true,idTel);
                ( (Main2Activity) getActivity()).pushFragmentFromActivity(profilFragment);
                return;
            }
        });

        Button valider = (Button) v.findViewById(R.id.button_valid_promenade);
        valider.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view)
            {
                EditText duration = (EditText) v.findViewById(R.id.entry_temps_promenade);

                try{
                    Integer.parseInt(duration.getText().toString());
                }
                catch(NumberFormatException err)
                {
                    Toast.makeText(getActivity(),"Durée de la promenade incorrecte",Toast.LENGTH_LONG).show();
                    return;
                }
                EditText max =(EditText) v.findViewById(R.id.entry_temps_immobilite);
                Intent intent = new Intent();
                intent.setAction(ServiceAdmin.ACTION_FROM_ACTIVITY);
                intent.putExtra("FOLLOW_NEW_SESSION","");
                intent.putExtra("IDTEL",idTel);
                intent.putExtra("NOM",profilSelected.getNom());
                intent.putExtra("PRENOM",profilSelected.getPrenom());
                intent.putExtra("DURATION",duration.getText().toString());
                intent.putExtra("MAXIMMOBILITE",max.getText().toString());
                getActivity().sendBroadcast(intent);
               int  time = Integer.parseInt(duration.getText().toString());
                profilSelected.setTempsRestant(time*60);
                profilSelected.setEstSuiviParMoi(true);
                profManager.addProfilOnPromenade(idTel,profilSelected);
                MapFragment_ fragMap = MapFragment_.newInstance();
                (  (Main2Activity) getActivity()).pushFragmentFromActivity(fragMap);
                return;
            }
        });
        return v;
    }

}

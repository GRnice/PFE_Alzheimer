package com.dg.apptabletteandroid.fragments.Profils;


import android.app.Fragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.dg.apptabletteandroid.Daemon.ServiceAdmin;
import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.R;

import java.util.ArrayList;

/**
 * Created by Remy on 14/12/2016.
 */

/**
 * ArrayAdapter pour personnaliser chaque item de la listview du fragment ProfilFragment
 */
public class AdapterListing extends ArrayAdapter<Profil> {
    private boolean selectionProfilPourPromenade;

    private Main2Activity act;


    public AdapterListing(Main2Activity context, int resource, ArrayList<Profil> objects,boolean selectionProfilPourPromenade) {
        super(context, resource, objects);
        act = context;
        this.selectionProfilPourPromenade = selectionProfilPourPromenade;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.item_adapter_profil_listing, parent, false);
        final TextView nomPrenom = (TextView) rowView.findViewById(R.id.prenomNomItem);
        final ImageView avatar = (ImageView) rowView.findViewById(R.id.photoProfil);

        // ImageView image = (ImageView) rowView.findViewById(R.id.photoProfil);
        ImageButton editButton = (ImageButton) rowView.findViewById(R.id.buttonEdit);
        ImageButton supprButton = (ImageButton) rowView.findViewById(R.id.buttonSuppr);

        if (selectionProfilPourPromenade)
        {
            editButton.setVisibility(View.INVISIBLE);
            supprButton.setVisibility(View.INVISIBLE);
        }

        final Profil profil = getItem(position);
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), profil.getIdRessourcesAvatar());
        avatar.setImageBitmap(bitmap);
        nomPrenom.setText(profil.getPrenom()+" "+profil.getNom());


        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (!act.isConnected())
                {
                    Toast.makeText(getContext(),"Connectez vous au réseau pour modifier le profil",Toast.LENGTH_LONG).show();
                    return;
                }

                Fragment fragEditProfil = AddProfilFragment.newInstance(profil);
                ((Main2Activity) getContext()).pushFragmentFromActivity(fragEditProfil);
            }
        });

        supprButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v)
            {

                if (!act.isConnected())
                {
                    Toast.makeText(getContext(),"Connectez vous au réseau",Toast.LENGTH_LONG).show();
                    return;
                }

                String fullName = profil.getPrenom() + " " + profil.getNom();
                AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
                builder.setMessage("Confirmez la suppression de:\n\n" + "\t\t" + fullName);
                builder.setCancelable(true);

                builder.setPositiveButton("Confirmer", new DialogInterface.OnClickListener() {

                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        deleteProfil(profil);
                        dialog.dismiss();
                    }

                });

                builder.setNegativeButton("Annuler", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });

                AlertDialog  alert = builder.create();
                alert.show();
                alert.getWindow().setLayout(350, 170);
            }
        });

        return rowView;
    }

    public void deleteProfil(Profil profil) {
        ((Main2Activity)getContext()).getProfilsManager().getAllProfils().remove(profil);
        Fragment fragProfils = ProfilFragment.newInstance(((Main2Activity)getContext()).getProfilsManager());  //(profilsManager);

        SharedPreferences shared = ((Main2Activity)getContext()).getPreferences(Context.MODE_PRIVATE);

        ArrayList<Profil> listProfils = ((Main2Activity)getContext()).getProfilsManager().getAllProfils();
        ((Main2Activity) getContext()).getProfilsManager().updateList(shared, listProfils);
        ((Main2Activity) getContext()).pushFragmentFromActivity(fragProfils);

        // send au service
        Intent intent = new Intent();
        intent.setAction(ServiceAdmin.ACTION_FROM_ACTIVITY);

        // nom,prenom,BarriereNormal
        intent.putExtra("SUPPRPROFIL", profil.makeSignature());
        ((Main2Activity)getContext()).sendBroadcast(intent);
    }


}
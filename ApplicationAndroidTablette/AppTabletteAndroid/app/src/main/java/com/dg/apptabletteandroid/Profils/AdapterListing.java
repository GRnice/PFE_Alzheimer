package com.dg.apptabletteandroid.Profils;


import android.app.Fragment;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import com.dg.apptabletteandroid.Data.Profil;
import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.R;
import com.dg.apptabletteandroid.fragments.MapFragment_;
import com.dg.apptabletteandroid.fragments.ProfilEdit;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Remy on 14/12/2016.
 */

/**
 * ArrayAdapter pour personnaliser chaque item de la listview du fragment ProfilFragment
 */
public class AdapterListing extends ArrayAdapter<Profil> {
    public AdapterListing(Context context, int resource, ArrayList<Profil> objects) {
        super(context, resource, objects);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.item_adapter_profil_listing, parent, false);
        final TextView nomPrenom = (TextView) rowView.findViewById(R.id.prenomNomItem);
        ImageView image = (ImageView) rowView.findViewById(R.id.photoProfil);
        Button editButton = (Button) rowView.findViewById(R.id.buttonEdit);

        final Profil profil = getItem(position);
        nomPrenom.setText(profil.getPrenom()+" "+profil.getNom());


        editButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Fragment fragEditProfil = ProfilEdit.newInstance(profil);
                ((Main2Activity) getContext()).pushFragmentFromActivity(fragEditProfil);
            }
        });


        return rowView;
    }
}

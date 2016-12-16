package com.dg.apptabletteandroid.Profils;


import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dg.apptabletteandroid.Data.Profil;
import com.dg.apptabletteandroid.R;

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
        View rowView = inflater.inflate(R.layout.item_adapter_profil_listing, parent, false);
        TextView nomPrenom = (TextView) rowView.findViewById(R.id.prenomNomItem);
        ImageView image = (ImageView) rowView.findViewById(R.id.photoProfil);

        Profil profil = getItem(position);

        nomPrenom.setText(profil.getPrenom()+" "+profil.getNom());

        return rowView;
    }
}

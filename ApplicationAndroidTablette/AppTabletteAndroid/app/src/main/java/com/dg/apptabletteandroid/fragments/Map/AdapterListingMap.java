package com.dg.apptabletteandroid.fragments.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Remy on 15/01/2017.
 */

public class AdapterListingMap extends ArrayAdapter {
    public static List<View> detailsList = new ArrayList<>();
    public static List<View> iconsList = new ArrayList<>();
    private Main2Activity act;
    public ArrayList<Profil> profils;

    public AdapterListingMap(Main2Activity context, int resource, ArrayList<Profil> objects) {
        super(context, resource, objects);
        act = context;
        profils=objects;
        detailsList.clear();
        iconsList.clear();
    }
    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);

        View rowView = convertView;
        if(rowView == null)
        {
            rowView = inflater.inflate(R.layout.item_profil_en_promenade, parent, false);
        }

        View detailView = rowView.findViewById(R.id.details);
        View iconView = rowView.findViewById(R.id.icons);
        if(!iconsList.contains(iconView)){
            iconsList.add(iconView);
        }
        if(!detailsList.contains(detailView)){
            detailsList.add(detailView);
        }
        final Profil profil = profils.get(position);
        TextView textView = (TextView) rowView.findViewById(R.id.nom_profil);
        textView.setText(profil.getNom()+" \n"+profil.getPrenom());
        TextView batteryTextView = (TextView) rowView.findViewById(R.id.details).findViewById(R.id.batteryDetails);
        batteryTextView.setText(String.valueOf(profil.getBattery()));
        ImageView batteryIcons = (ImageView) rowView.findViewById(R.id.batteryIcons);
        ImageView batteryIcon = (ImageView) rowView.findViewById(R.id.batteryIcon);
        if(profil.getBattery() < 21){
            batteryTextView.setTextColor(Color.RED);
            batteryIcon.setImageResource(R.drawable.low);
            batteryIcons.setImageResource(R.drawable.low);
        }else if(profil.getBattery() < 51){
            batteryIcon.setImageResource(R.drawable.medium);
            batteryIcons.setImageResource(R.drawable.medium);
        }
        ImageView imageView = (ImageView) rowView.findViewById(R.id.imageSuivre);
        if (profil.estSuiviParMoi())
        {
            imageView.setImageDrawable(act.getResources().getDrawable(R.drawable.pas_suivre)); // alors afficher l'icone plus suivre
        }
        else
        {
            imageView.setImageDrawable(act.getResources().getDrawable(R.drawable.suivre)); // alors afficher l'icone suivre
        }
        imageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(profil.estSuiviParMoi()) // si je le suis
                {
                    act.unfollowProfil(profil); // alors j'afficherai suivre puisque à cet instant je ne suis pas
                    ((ImageView) v).setImageDrawable(act.getResources().getDrawable(R.drawable.suivre));
                }
                else{
                    act.followProfil(profil); // et inversement
                    ((ImageView) v).setImageDrawable(act.getResources().getDrawable(R.drawable.pas_suivre));
                }
                profil.setEstSuiviParMoi(!profil.estSuiviParMoi());
            }
        });

        return rowView;
    }
}
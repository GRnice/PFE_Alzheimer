package com.dg.apptabletteandroid.fragments.Map;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.support.annotation.NonNull;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;

import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.R;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by Remy on 15/01/2017.
 */

public class AdapterListingMap extends ArrayAdapter
{
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

    public ArrayList<Profil> getProfils() {
        return profils;
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

        ImageView imageProfil = (ImageView) rowView.findViewById(R.id.itemOnPromenadeAvatar);

        View detailView = rowView.findViewById(R.id.details);

        View iconView = rowView.findViewById(R.id.icons);

        if(!iconsList.contains(iconView)){
            iconsList.add(iconView);
        }
        if(!detailsList.contains(detailView)){
            detailsList.add(detailView);
        }
        final Profil profil = profils.get(position);
        Bitmap bitmap = BitmapFactory.decodeResource(getContext().getResources(), profil.getIdRessourcesAvatar());
        imageProfil.setImageBitmap(bitmap);
        if(profil.isEnVueDetail()){
            detailView.setVisibility(View.VISIBLE);
            iconView.setVisibility(View.GONE);
        }else{
            detailView.setVisibility(View.GONE);
            iconView.setVisibility(View.VISIBLE);
        }

        //donnee profil
        TextView textView = (TextView) rowView.findViewById(R.id.nom_profil);
        textView.setText(profil.getNom()+" \n"+profil.getPrenom());
        TextView t=(TextView)rowView.findViewById(R.id.texteTime);
        TextView duree=(TextView)rowView.findViewById(R.id.timming);
        TextView dureeD=(TextView)rowView.findViewById(R.id.duree);
        ImageView time=(ImageView)rowView.findViewById(R.id.timeout);
        ImageView clock=(ImageView)rowView.findViewById(R.id.clockTime);
        ImageView clockOut=(ImageView)rowView.findViewById(R.id.clockTimeout);
        TableRow alerteImmobilite=(TableRow)rowView.findViewById(R.id.alerteImmobile);
        TableRow alertePerte=(TableRow)rowView.findViewById(R.id.alertePerte);
        TableRow alerteHorsZone=(TableRow)rowView.findViewById(R.id.alerteHorszone);
        ImageView alerte =(ImageView)rowView.findViewById(R.id.itemOnPromenadeAlert);

        alerte.setVisibility(View.GONE);
        rowView.setBackgroundColor(Color.WHITE);

        duree.setText(profil.getTempsRestant()+"m");
        dureeD.setText(profil.getTempsRestant()+"m");
        int d =profil.getTempsRestant();
        time.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {

            }
        });
        if(d<=0){
            duree.setVisibility(View.GONE);
            time.setVisibility(View.VISIBLE);
            clock.setVisibility(View.GONE);

            t.setText("Promenade Terminée");
            t.setTextColor(Color.RED);
            clockOut.setVisibility(View.VISIBLE);
            alerte.setVisibility(View.VISIBLE);

            dureeD.setVisibility(View.GONE);
        }
        else if (d <60){
            duree.setText(profil.getTempsRestant()+"s");
            dureeD.setText(profil.getTempsRestant()+"s");


        }else{
            d= d /60;
            int s= profil.getTempsRestant()%60;
            duree.setText(d+"m");
            dureeD.setText(d+"m"+s+"s");

        }

        TextView batteryTextView = (TextView) rowView.findViewById(R.id.details).findViewById(R.id.batteryDetails);
        batteryTextView.setText(String.valueOf(profil.getBattery()) + "%");
        ImageView batteryIcons = (ImageView) rowView.findViewById(R.id.batteryIcons);
        ImageView batteryIcon = (ImageView) rowView.findViewById(R.id.batteryIcon);

        Log.e("UPDATELISTINMAP","bat -> "+profil.batteryIsLow());

        if(profil.getBattery() < 51)
        {
            batteryIcon.setImageResource(R.drawable.medium);
            batteryIcons.setImageResource(R.drawable.medium);
        }

        // jaune
        // si profil immobile -> yellow
        if(profil.isImmobile()){
            alerte.setVisibility(View.VISIBLE);
            rowView.setBackgroundColor(Color.YELLOW);
            alerteImmobilite.setVisibility(View.VISIBLE);
        }
        if(profil.getTempsRestant() <= 0){
            rowView.setBackgroundColor(Color.YELLOW);
        }
        // jaune
        if (profil.batteryIsLow())
        {
            alerte.setVisibility(View.VISIBLE);
//            Log.e("batteryLow","yellow");
            rowView.setBackgroundColor(Color.YELLOW);
            batteryIcon.setImageResource(R.drawable.low);
            batteryIcons.setImageResource(R.drawable.low);
        }

        if (profil.isHorsZone())
        {
            alerte.setVisibility(View.VISIBLE);
            alerteHorsZone.setVisibility(View.VISIBLE);
            rowView.setBackgroundColor(Color.argb(128,255,80,41));
        }
        if (profil.updateIsTimeout())
        {
            alerte.setVisibility(View.VISIBLE);
            alertePerte.setVisibility(View.VISIBLE);
            rowView.setBackgroundColor(Color.argb(128,255,80,41));
        }






        final ImageView imageView = (ImageView) rowView.findViewById(R.id.imageSuivre);
        if (profil.estSuiviParMoi())
        {
            imageView.setImageDrawable(act.getResources().getDrawable(R.drawable.suivre)); // alors afficher l'icone suivre
        }
        else
        {
            imageView.setImageDrawable(act.getResources().getDrawable(R.drawable.pas_suivre)); // alors afficher l'icone plus suivre
        }

        imageView.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (!act.isConnected()) // si on est pas connecté on ignore le clic
                {
                    Toast.makeText(act,"Connexion au réseau nécessaire",Toast.LENGTH_LONG).show();
                    return;
                }
                if(profil.estSuiviParMoi() && act.isConnected()) // si je le suis
                {

                    act.unfollowProfil(profil); // alors j'afficherai suivre puisque à cet instant je ne suis pas
//                    imageView.setImageDrawable(act.getResources().getDrawable(R.drawable.suivre)); // alors afficher l'icone suivre
                }
                else if (act.isConnected())
                {
                    act.followProfil(profil); // et inversement
                    imageView.setImageDrawable(act.getResources().getDrawable(R.drawable.suivre)); // alors afficher l'icone plus suivre
                }
            }
        });

        return rowView;
    }
}
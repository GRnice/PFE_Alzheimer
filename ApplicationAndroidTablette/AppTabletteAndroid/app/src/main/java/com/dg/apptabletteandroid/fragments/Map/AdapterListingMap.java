package com.dg.apptabletteandroid.fragments.Map;

import android.app.Activity;
import android.app.Fragment;
import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by Remy on 15/01/2017.
 */

public class AdapterListingMap extends ArrayAdapter {
    public List<View> views = new ArrayList<>();
    private Activity act;

    public AdapterListingMap(Activity context, int resource, ArrayList<Profil> objects) {
        super(context, resource, objects);
        act = context;
    }



    @NonNull
    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        LayoutInflater inflater = (LayoutInflater) super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        View rowView = inflater.inflate(R.layout.item_profil_en_promenade, parent, false);

        if(rowView == null)
        {
            rowView = inflater.inflate(R.layout.item_profil_en_promenade, parent, false);
        }
        View view = rowView.findViewById(R.id.details);
        views.add(view);
        return rowView;
    }
}
package com.dg.apptabletteandroid.fragments.Profils;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;

import com.dg.apptabletteandroid.R;

import java.util.ArrayList;

/**
 * Created by Remy on 29/01/2017.
 */

public class AdapterSpinnerAvatar extends ArrayAdapter<Integer>
{
    private Context context;
    private ArrayList<Integer> itemList;
    public AdapterSpinnerAvatar(Context context, int textViewResourceId,ArrayList<Integer> itemList) {
        super(context, textViewResourceId,itemList);
        this.context=context;
        this.itemList=itemList;
    }


    @Override
    public View getView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.item_adapter_avatar_listing, parent, false);
        ImageView image = (ImageView) rowView.findViewById(R.id.image_view_adapter_avatar_);
        image.setImageBitmap(BitmapFactory.decodeResource(this.context.getResources(), this.itemList.get(position)));
        return rowView;
    }

    @Override
    public View getDropDownView(int position, View convertView, ViewGroup parent)
    {
        LayoutInflater inflater = (LayoutInflater) super.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        final View rowView = inflater.inflate(R.layout.item_adapter_avatar_listing, parent, false);
        ImageView image = (ImageView) rowView.findViewById(R.id.image_view_adapter_avatar_);
        image.setImageBitmap(BitmapFactory.decodeResource(this.context.getResources(), this.itemList.get(position)));
        return rowView;
    }
}

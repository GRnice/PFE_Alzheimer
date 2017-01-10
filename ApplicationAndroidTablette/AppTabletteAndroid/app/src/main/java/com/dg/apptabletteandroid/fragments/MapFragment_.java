package com.dg.apptabletteandroid.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dg.apptabletteandroid.Data.Profil;
import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.Profils.ProfilsManager;
import com.dg.apptabletteandroid.R;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.sql.SQLData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class MapFragment_ extends Fragment
{
    MapView mMapView;
    private GoogleMap googleMap;
    private static HashMap<Profil, Marker> profilsAffiches = new HashMap<>();
    private static Bitmap bitmap;

    public MapFragment_()
    {
        // Required empty public constructor
    }

    public static MapFragment_ newInstance()
    {
        MapFragment_ fragment = new MapFragment_();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.avatar);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_map, container, false);
        mMapView = (MapView) view.findViewById(R.id.mapView);
        mMapView.onCreate(savedInstanceState);
        mMapView.onResume(); // needed to get the map to display immediately


        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                int res = getActivity().checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                // For showing a move to my location button
                googleMap.setMyLocationEnabled(false);
                LatLng sophia = new LatLng(43.6155793,7.0696861);
                CameraPosition cameraPosition = new CameraPosition.Builder().target(sophia).zoom(10).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
                Log.d("Map", String.valueOf(profilsAffiches.size()));
                for(Map.Entry<Profil, Marker> entry : profilsAffiches.entrySet()){
                    Profil profil = entry.getKey();
                    Marker marker = entry.getValue();
                    profilsAffiches.put(profil, googleMap.addMarker(new MarkerOptions().position(marker.getPosition()).title(profil.getPrenom() + profil.getNom()).icon(BitmapDescriptorFactory.fromBitmap(bitmap))));
                }
                if (res != PackageManager.PERMISSION_GRANTED)
                {
                    return;
                }


                /* For dropping a marker at a point on the Map
                googleMap.addMarker(new MarkerOptions().position(sydney).title("Marker Title").snippet("Marker Description"));
                */

                /* For zooming automatically to the location of the marker

                */
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
        this.mMapView.onDestroy();
    }

    @Override
    public void onPause()
    {
        super.onPause();
        this.mMapView.onPause();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        this.mMapView.onResume();
    }

    public void updateMap(Profil profil){
        LatLng latLng = new LatLng(profil.getLatitude(), profil.getLongitude());
        if ((profilsAffiches.get(profil) == null)){
            Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(profil.getPrenom() + profil.getNom()).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            profilsAffiches.put(profil, marker);
        }else{
            Marker marker = profilsAffiches.get(profil);
            marker.setPosition(latLng);
        }
    }


}

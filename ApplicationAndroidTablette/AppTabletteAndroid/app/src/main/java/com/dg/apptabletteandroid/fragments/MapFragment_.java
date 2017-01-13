package com.dg.apptabletteandroid.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.Profils.Profil;
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

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class MapFragment_ extends BlankFragment
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
                ProfilsManager profilsManager = ((Main2Activity) getActivity()).getProfilsManager();
                Set<Profil> allProfilsAffiches = profilsAffiches.keySet();
                Collection<Profil> allProfilsOnPromenade = profilsManager.getProfilOnPromenade().values();
                for (Profil profil : allProfilsAffiches)
                {
                    if (! allProfilsOnPromenade.contains(profil))
                    {
                        profilsAffiches.get(profil).remove();
                        profilsAffiches.remove(profil);
                    }
                }


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


    /**
     * Back pressed send from activity.
     *
     * @return if event is consumed, it will return true.
     */
    @Override
    public void onBackPressed() {
        Log.d("AAA", "BACKPressed Map");
    }

    // Met à jour la map
    public void refresh()
    {
        ProfilsManager profilsManager = ((Main2Activity) getActivity()).getProfilsManager();
        Set<Profil> allProfilsAffiches = profilsAffiches.keySet();
        Collection<Profil> allProfilsOnPromenade = profilsManager.getProfilOnPromenade().values();
        for (Profil profil : allProfilsAffiches)
        {
            if (! allProfilsOnPromenade.contains(profil))
            {
                profilsAffiches.get(profil).remove();
                profilsAffiches.remove(profil);
            }
        }

        googleMap.clear();

        Iterator<Profil> profilsPromenade = profilsAffiches.keySet().iterator();
        while(profilsPromenade.hasNext())
        {
            Profil profil = profilsPromenade.next();
            profilsAffiches.get(profil).remove();
            LatLng latLng = new LatLng(profil.getLatitude(), profil.getLongitude());
            Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(profil.getPrenom() + profil.getNom()).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            profilsAffiches.put(profil,marker);
        }
    }

    // ajout ou mise à jour du marker d'un profil
    public void update(Profil profil)
    {
        LatLng latLng = new LatLng(profil.getLatitude(), profil.getLongitude());
        if ((profilsAffiches.get(profil) == null)){
            Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(profil.getPrenom() + profil.getNom()).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            profilsAffiches.put(profil, marker);
        }else{
            Marker marker = profilsAffiches.get(profil);
            marker.setPosition(latLng);
        }

    }

    // suppression d'un profil a afficher
    public void removeProfil(Profil profilStopped)
    {
        if (profilsAffiches.containsKey(profilStopped))
        {
            profilsAffiches.get(profilStopped).remove();
            profilsAffiches.remove(profilStopped);
            this.refresh();
        }

    }

}

package com.dg.apptabletteandroid.fragments.Map;

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
import android.widget.AdapterView;
import android.widget.ListView;

import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.Profils.ProfilOnPromenadeManager;
import com.dg.apptabletteandroid.R;
import com.dg.apptabletteandroid.fragments.BlankFragment;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;


public class MapFragment_ extends BlankFragment
{
    MapView mMapView;
    private GoogleMap googleMap;

    private static Bitmap bitmap;
    private ListView listView;
    private ProfilOnPromenadeManager profilsManager;
    private ProfilGroupManager profilsGroupManager;

    public MapFragment_()
    {
        // Required empty public constructor
    }

    public static MapFragment_ newInstance()
    {
        MapFragment_ fragment = new MapFragment_();
        fragment.profilsGroupManager = ProfilGroupManager.newInstance();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {

      //map
        super.onCreate(savedInstanceState);
        bitmap = BitmapFactory.decodeResource(getResources(), R.drawable.avatar);
        profilsManager = ((Main2Activity) getActivity()).getProfilsManager();
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


        final AdapterListingMap customAdapter = new AdapterListingMap((Main2Activity) getActivity()
                ,R.layout.item_profil_en_promenade
                ,new ArrayList<>(profilsManager.getAllProfilsOnPromenade().values()));  // que les profils

        listView = (ListView) view.findViewById(R.id.listProfilsOnProm);
        listView.setAdapter(customAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                View lesDetails = customAdapter.detailsList.get(i);
                View icons = customAdapter.iconsList.get(i) ;
                if(lesDetails.getVisibility() == View.GONE)
                {
                    lesDetails.setVisibility(View.VISIBLE);
                    icons.setVisibility(View.GONE);
                }else {
                    lesDetails.setVisibility(View.GONE);
                    icons.setVisibility(View.VISIBLE);
                }

                for(View v : customAdapter.detailsList){
                    if(!v.equals(lesDetails))
                    {
                        v.setVisibility(View.GONE);
                    }
                }
                for (View v : customAdapter.iconsList){
                    if(!v.equals(icons)){
                        v.setVisibility(View.VISIBLE);
                    }
                }


            }

        });
        

        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                HashMap<String,Profil> allProfilsOnPromenade = profilsManager.getAllProfilsOnPromenade();

                int res = getActivity().checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                // For showing a move to my location button
                googleMap.setMyLocationEnabled(false);
                LatLng sophia = new LatLng(43.6155793,7.0696861);
                CameraPosition cameraPosition = new CameraPosition.Builder().target(sophia).zoom(12).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));

                for(String idTel : allProfilsOnPromenade.keySet()){

                    Profil profil = allProfilsOnPromenade.get(idTel);
                    Marker marker = profil.getMarker();

                  /*  if (profilsGroupManager.getHashProfilMarker().containsKey(profil)) {  // si il est deja dans un group
                        marker = profilsGroupManager.getHashProfilMarker().get(profil);
                        marker.setPosition(new LatLng(profil.getLatitude(), profil.getLongitude()));
                        int nbPersonnesParGroupe = profilsGroupManager.getHashMarkerGroupProfil().get(profil).size();
                        marker.setTitle(String.valueOf(nbPersonnesParGroupe));
                    }*/

                    // check check
                    if (marker != null) {

                        profil.setMarker(googleMap.addMarker(new MarkerOptions().position(marker.getPosition()).title(profil.getPrenom() + profil.getNom()).icon(BitmapDescriptorFactory.fromBitmap(bitmap))));
                    }

                }

                if (res != PackageManager.PERMISSION_GRANTED)
                {
                    return;
                }
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


    public void refresh()
    {
        refreshMap();
        refreshListe();
    }

    // mise à jour de la listeView
    private void refreshListe()
    {
        profilsManager = ((Main2Activity) getActivity()).getProfilsManager();
        ArrayList<Profil> profilsOnPromenade = new ArrayList<>(profilsManager.getAllProfilsOnPromenade().values());

        final AdapterListingMap customAdapter = new AdapterListingMap((Main2Activity)getActivity()
                ,R.layout.item_profil_en_promenade
                ,profilsOnPromenade);
        listView.setAdapter(customAdapter);
    }

    // Met à jour la map
    private void refreshMap()
    {
        googleMap.clear();

        Iterator<Profil> profilsPromenade = profilsManager.getAllProfilsOnPromenade().values().iterator();
        while(profilsPromenade.hasNext())
        {
            Profil profil = profilsPromenade.next();
            LatLng latLng = new LatLng(profil.getLatitude(), profil.getLongitude());
            Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(profil.getPrenom() + profil.getNom()).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
        }

        refreshListe();
    }

    // ajout ou mise à jour du marker d'un profil
    public void update(Profil profil)
    {
        LatLng latLng = new LatLng(profil.getLatitude(), profil.getLongitude());
        Marker marker;

        if (profil.getMarker() == null)
        {
            marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(profil.getPrenom() + profil.getNom()).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            profil.setMarker(marker);
        }

        else
        {

           /* profilsGroupManager.onUpdate(profil, profilsManager.getAllProfilsOnPromenade());
            if (profilsGroupManager.getHashProfilMarker().containsKey(profil))
            {
                Log.e("AAAA", "BBB");
                marker = profilsGroupManager.getHashProfilMarker().get(profil);
                marker.setPosition(latLng);

                int nbPersonnesParGroupe = profilsGroupManager.getHashMarkerGroupProfil().get(profil).size();
                marker.setTitle(String.valueOf(nbPersonnesParGroupe));
            }*/
           // else
           // {
                Log.e("AAAA", "CCC");
                marker = profil.getMarker();
                marker.setPosition(latLng);
         //   }

        }

        // profil manager group

    }


}

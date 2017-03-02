package com.dg.apptabletteandroid.fragments.Map;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;

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
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polygon;
import com.google.android.gms.maps.model.PolygonOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Random;


public class MapFragment_ extends BlankFragment
{
    MapView mMapView;
    private GoogleMap googleMap;

    private ListView listView;
    private Button synchRefreshTitre;
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

        profilsManager = ((Main2Activity) getActivity()).getProfilsManager();
    }


    private void limitCentre() {
        // Instantiates a new Polygon object and adds points to define a rectangle
        PolygonOptions rectOptions = new PolygonOptions()
                .add(new LatLng(43.612736, 7.079357),
                        new LatLng(43.612669, 7.079243),
                        new LatLng(43.612522, 7.079179),
                        new LatLng(43.612432, 7.079147),
                        new LatLng(43.612366, 7.079048),
                        new LatLng(43.612304, 7.078982),
                        new LatLng(43.612246, 7.078964),
                        new LatLng(43.611973, 7.079333),
                        new LatLng(43.611956, 7.079453),
                        new LatLng(43.612008, 7.079529),
                        new LatLng(43.612101, 7.079949),
                        new LatLng(43.612274, 7.080104),
                        new LatLng(43.612373, 7.080015),
                        new LatLng(43.612492, 7.079833),
                        new LatLng(43.612475, 7.079789), // coint gauche avant de la bariere
                        new LatLng(43.612555, 7.079675),  // coint droit avant de la bariere
                        new LatLng(43.612600, 7.079692),
                        new LatLng(43.612721, 7.079515))
                .strokeColor(Color.BLUE)
                .strokeWidth(2);

        //  Get back the mutable Polygon
        Polygon limitDuCentre = googleMap.addPolygon(rectOptions);

        // affichage Bariere
        /*CircleOptions c = new CircleOptions()
                                .center(new LatLng(43.61273, 7.079525))
                                .radius(2.1255215857234537)  // In meters
                                .strokeColor(Color.MAGENTA)
                                .strokeWidth(1);
        googleMap.addCircle(c);*/

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

        synchRefreshTitre = (Button) view.findViewById(R.id.btnListViewTitre);
        synchRefreshTitre.setText("Profils en promenade ("+profilsManager.getAllProfilsOnPromenade().size()+")");
        synchRefreshTitre.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {

            }
        });

        getActivity().setTitle("Carte");
        final AdapterListingMap customAdapter = new AdapterListingMap((Main2Activity) getActivity()
                ,R.layout.item_profil_en_promenade
                ,new ArrayList<>(profilsManager.getAllProfilsOnPromenade().values()));  // que les profils

        listView = (ListView) view.findViewById(R.id.listProfilsOnProm);
        listView.setAdapter(customAdapter);

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int position, long l)
            {
                View lesDetails = customAdapter.detailsList.get(position);
                View icons = customAdapter.iconsList.get(position);
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
                    if(!v.equals(icons)) {
                        v.setVisibility(View.VISIBLE);
                    }

                    Profil profil = customAdapter.getProfils().get(position);
                    profil.setEnVueDetail(!profil.isEnVueDetail());
                    Iterator<Profil> iteratorProm = profilsManager.getAllProfilsOnPromenade().values().iterator();
                    while(iteratorProm.hasNext())
                    {
                        Profil pr = iteratorProm.next();
                        if(pr.getPrenom().equals(profil.getPrenom()) && pr.getNom().equals(profil.getNom())) {
                            googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(pr.getLatitude(), pr.getLongitude()), 19));
                            break;
                        }
                    }
                }
            }

        });




        mMapView.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap mMap) {
                googleMap = mMap;
                googleMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE);
                limitCentre();

                HashMap<String,Profil> allProfilsOnPromenade = profilsManager.getAllProfilsOnPromenade();

                int res = getActivity().checkCallingOrSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION);
                // For showing a move to my location button
                googleMap.setMyLocationEnabled(false);
                googleMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {

                    @Override
                    public void onMapClick(LatLng latLng) {
                        minimizeAll();  // minimiz all the expanded details
                    }
                });

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {  // maximize

                    boolean single = false;
                    @Override
                    public boolean onMarkerClick(Marker marker)
                    {
                        if(single) {
                            minimizeAll();
                            single = false;
                        }

                        if(marker.getSnippet() != null) {  // dans un groupe
                            String[] allNames = marker.getSnippet().split("\n");

                            for (int i = 0; i < allNames.length; i++) {
                                expandDetails(allNames[i]);
                            }
                        }
                        else {  // un individu, je recupere le titre
                            single = true;
                            minimizeAll();
                            expandDetails(marker.getTitle());

                        }
                        return false;
                    }
                });


                LatLng centre = new LatLng(43.612248, 7.079400);
                CameraPosition cameraPosition = new CameraPosition.Builder().target(centre).zoom(17).build();
                googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));


                // ce marqueur permet de savoir si le marker d'un groupe a deja ete posé ou non
                int marqueurRecherche = new Random().nextInt();
                for(String idTel : allProfilsOnPromenade.keySet())
                {
                    Profil profil = allProfilsOnPromenade.get(idTel);
                    Marker marker = profil.getMarker();
                    if (marker != null)
                    {
                        profilsGroupManager.onUpdate(profil,new ArrayList<>(allProfilsOnPromenade.values()));
                        Group group = profilsGroupManager.dansUnGroupe(profil);
                        if (group != null && group.getMarqueurRecherche() != marqueurRecherche)
                        {
                            group.setMarqueurRecherche(marqueurRecherche);
                            profil = group.getAnyProfil();
                            marker = profil.getMarker();
                            Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(), group.getDrawable());
                            LatLng latlng = new LatLng(profil.getLatitude(),profil.getLongitude());
                            String messageMarker = group.stringifyForMarker();

                            marker = googleMap.addMarker(new MarkerOptions().position(latlng).title("Groupe").snippet(messageMarker).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                            marker.showInfoWindow();
                            //profil.setMarker(googleMap.addMarker(new MarkerOptions().position(marker.getPosition()).title(profil.getPrenom() + " " + profil.getNom()).icon(BitmapDescriptorFactory.fromBitmap(bitmap))));
                            profil.setMarker(marker);

                            googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
//7.0800824 , 43.6131763
                                @Override
                                public View getInfoWindow(Marker arg0) {
                                    return null;
                                }

                                @Override
                                public View getInfoContents(Marker marker) {

                                    LinearLayout info = new LinearLayout(getActivity().getApplicationContext());
                                    info.setOrientation(LinearLayout.VERTICAL);

                                    TextView title = new TextView(getActivity().getApplicationContext());
                                    title.setTextColor(Color.BLACK);
                                    title.setGravity(Gravity.CENTER);
                                    title.setTypeface(null, Typeface.BOLD);
                                    title.setText(marker.getTitle());

                                    TextView snippet = new TextView(getActivity().getApplicationContext());
                                    snippet.setTextColor(Color.GRAY);
                                    snippet.setText(marker.getSnippet());

                                    info.addView(title);
                                    info.addView(snippet);

                                    return info;
                                }
                            });
                        }
                        else if (group == null)
                        {
                            Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(), profil.getIdRessourcesAvatar());
                            LatLng latLngProfil = new LatLng(profil.getLatitude(), profil.getLongitude());
                            marker = googleMap.addMarker(new MarkerOptions().position(latLngProfil).title(profil.getPrenom() + " " + profil.getNom()).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));

                            marker.showInfoWindow();
                            profil.setMarker(marker);
                        }
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

    private void expandDetails(String TitleOrSnippet) {
        String prenom = TitleOrSnippet.split(" ")[0];
        String nom = TitleOrSnippet.split(" ")[1];

        int i = 0;
        Iterator<Profil> iteratorProm = profilsManager.getAllProfilsOnPromenade().values().iterator();
        while(iteratorProm.hasNext()) {
            Profil pr = iteratorProm.next();
            if(pr.getPrenom().equals(prenom) && pr.getNom().equals(nom)) {
                View details = ((AdapterListingMap) listView.getAdapter()).detailsList.get(i);
                View icons = ((AdapterListingMap) listView.getAdapter()).iconsList.get(i);
                details.setVisibility(View.VISIBLE);
                icons.setVisibility(View.GONE);
            }
            i++;
        }
    }

    private void minimizeAll() {
        int index = 0;
        Iterator<Profil> iteratorPromenade = profilsManager.getAllProfilsOnPromenade().values().iterator();
        while (iteratorPromenade.hasNext()) {

            if ((AdapterListingMap) listView.getAdapter() != null) {
                View details = ((AdapterListingMap) listView.getAdapter()).detailsList.get(index);
                View icons = ((AdapterListingMap) listView.getAdapter()).iconsList.get(index);
                details.setVisibility(View.GONE);
                icons.setVisibility(View.VISIBLE);
                iteratorPromenade.next();
                index++;
            }
        }
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
    public void onBackPressed()
    {
        Log.d("AAA", "BACKPressed Map");
    }

    public ProfilGroupManager getProfilsGroupManager()
    {
        return profilsGroupManager;
    }

    public void refresh()
    {
        if (googleMap != null && listView != null)
        {
            refreshMap();
            refreshListe();
        }
    }


    // mise à jour de la listeView
    private void refreshListe()
    {
        profilsManager = ((Main2Activity) getActivity()).getProfilsManager();
        ArrayList<Profil> profilsOnPromenade = new ArrayList<>(profilsManager.getAllProfilsOnPromenade().values());
        synchRefreshTitre.setText("Profils en promenade ("+profilsOnPromenade.size()+")");
        final AdapterListingMap customAdapter = new AdapterListingMap((Main2Activity)getActivity()
                ,R.layout.item_profil_en_promenade
                ,profilsOnPromenade);
        listView.setAdapter(customAdapter);
    }

    // Met à jour la map
    private void refreshMap()
    {
        googleMap.clear();
        limitCentre();


        Iterator<Profil> profilsPromenade = profilsManager.getAllProfilsOnPromenade().values().iterator();
        int marqueurRecherche = new Random().nextInt();

        while(profilsPromenade.hasNext())
        {
            Profil profil = profilsPromenade.next();
            if (profil.getMarker() == null) continue;

            profilsGroupManager.onUpdate(profil,new ArrayList<>(profilsManager.getAllProfilsOnPromenade().values()));

            Group group = profilsGroupManager.dansUnGroupe(profil);
            if (group != null && group.getMarqueurRecherche() != marqueurRecherche)
            {
                group.setMarqueurRecherche(marqueurRecherche);
                profil = group.getAnyProfil();
                LatLng latLng = new LatLng(profil.getLatitude(), profil.getLongitude());
                Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(), group.getDrawable());
                String messageMarker = group.stringifyForMarker();
                Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng).title("Groupe").snippet(messageMarker).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                marker.showInfoWindow();
                profil.setMarker(marker);

                googleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

                    @Override
                    public View getInfoWindow(Marker arg0) {
                        return null;
                    }

                    @Override
                    public View getInfoContents(Marker marker) {

                        LinearLayout info = new LinearLayout(getActivity().getApplicationContext());
                        info.setOrientation(LinearLayout.VERTICAL);

                        TextView title = new TextView(getActivity().getApplicationContext());
                        title.setTextColor(Color.BLACK);
                        title.setGravity(Gravity.CENTER);
                        title.setTypeface(null, Typeface.BOLD);
                        title.setText(marker.getTitle());

                        TextView snippet = new TextView(getActivity().getApplicationContext());
                        snippet.setTextColor(Color.GRAY);
                        snippet.setText(marker.getSnippet());

                        info.addView(title);
                        info.addView(snippet);

                        return info;
                    }
                });

            }
            else if (group == null)
            {
                LatLng latLng = new LatLng(profil.getLatitude(), profil.getLongitude());
                Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(), profil.getIdRessourcesAvatar());
                Marker marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(profil.getPrenom() + " " + profil.getNom()).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
                marker.showInfoWindow();
                profil.setMarker(marker);
            }
        }


    }

    // ajout ou mise à jour du marker d'un profil
    public void update(Profil profil)
    {
        LatLng latLng = new LatLng(profil.getLatitude(), profil.getLongitude());
        Marker marker;

        if (profil.getMarker() == null) // si marker a null alors il n'est pas dans un groupe !
        {
            Bitmap bitmap = BitmapFactory.decodeResource(getActivity().getResources(), profil.getIdRessourcesAvatar());
            marker = googleMap.addMarker(new MarkerOptions().position(latLng).title(profil.getPrenom() + " " +  profil.getNom()).icon(BitmapDescriptorFactory.fromBitmap(bitmap)));
            marker.showInfoWindow();
            profil.setMarker(marker);
        }
        refresh();
    }

}

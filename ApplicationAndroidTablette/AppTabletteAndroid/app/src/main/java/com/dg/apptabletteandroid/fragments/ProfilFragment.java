package com.dg.apptabletteandroid.fragments;


import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.dg.apptabletteandroid.Daemon.ServiceAdmin;
import com.dg.apptabletteandroid.Data.Profil;
import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.Profils.AdapterListing;
import com.dg.apptabletteandroid.Profils.ProfilsManager;
import com.dg.apptabletteandroid.R;


public class ProfilFragment extends Fragment
{
    private ListView listView;
    private ProfilsManager profilsManager;
    private String idTel;
    private boolean selectionProfilNewSession;

    public ProfilFragment()
    {
        // Required empty public constructor
    }

    public void setProfilsManager(ProfilsManager profilsManager)
    {
        this.profilsManager = profilsManager;
    }

    public static ProfilFragment newInstance(ProfilsManager profilsManager)
    {
        ProfilFragment fragment = new ProfilFragment();
        fragment.selectionProfilNewSession = false;
        fragment.setProfilsManager(profilsManager);
        return fragment;
    }

    public static ProfilFragment newInstance(ProfilsManager profilsManager,boolean newsession,String idTel)
    {
        ProfilFragment fragment = new ProfilFragment();
        fragment.selectionProfilNewSession = newsession;
        fragment.idTel = idTel;
        fragment.setProfilsManager(profilsManager);
        return fragment;
    }

    @Override
    public void onStart()
    {
        super.onStart();
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_profil, container, false);
        this.listView = (ListView) view.findViewById(R.id.listingprofils);
        Log.e("sizeallprofilarray",String.valueOf(profilsManager.getAllProfils().size()));
        AdapterListing adapterListing = new AdapterListing(getActivity(),R.layout.item_adapter_profil_listing,profilsManager.getAllProfils());
        this.listView.setAdapter(adapterListing);


        if (this.selectionProfilNewSession)
        {
            this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    AdapterListing adapterListing = (AdapterListing) listView.getAdapter();
                    Profil profilselected = adapterListing.getItem(i);
                    profilsManager.addProfilOnPromenade(idTel,profilselected);
                    Intent intent = new Intent();
                    intent.setAction(ServiceAdmin.ACTION_FROM_ACTIVITY);
                    intent.putExtra("FOLLOW_NEW_SESSION","");
                    intent.putExtra("IDTEL",idTel);
                    intent.putExtra("NOM",profilselected.getNom());
                    intent.putExtra("PRENOM",profilselected.getPrenom());
                    getActivity().sendBroadcast(intent);
                }
            });
        }

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


}

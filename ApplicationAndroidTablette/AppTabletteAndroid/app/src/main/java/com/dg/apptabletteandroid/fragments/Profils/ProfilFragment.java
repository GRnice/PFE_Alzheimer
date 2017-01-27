package com.dg.apptabletteandroid.fragments.Profils;


import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.app.Fragment;
import android.support.design.widget.FloatingActionButton;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;

import com.dg.apptabletteandroid.Daemon.ServiceAdmin;
import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.Main2Activity;
import com.dg.apptabletteandroid.Profils.ProfilsManager;
import com.dg.apptabletteandroid.R;
import com.dg.apptabletteandroid.fragments.BlankFragment;
import com.dg.apptabletteandroid.fragments.Map.MapFragment_;

import java.util.ArrayList;
import java.util.HashSet;


public class ProfilFragment extends BlankFragment
{
    private ListView listView;
    private ProfilsManager profilsManager;
    private String idTel;
    private boolean selectionProfilNewSession;
    private WorkerListingProfil worker;

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
        ArrayList<Profil> listprofilFilter = new ArrayList<>(profilsManager.getAllProfils());
        listprofilFilter.removeAll(profilsManager.getAllProfilsOnPromenade().values());
        AdapterListing adapterListing = new AdapterListing(getActivity(),R.layout.item_adapter_profil_listing,listprofilFilter);
        this.listView.setAdapter(adapterListing);

        FloatingActionButton fab = (FloatingActionButton) getActivity().findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragmentAdd = AddProfilFragment.newInstance();
                ((Main2Activity) getActivity()).pushFragmentFromActivity(fragmentAdd);
            }
        });


        if (this.selectionProfilNewSession)
        {
            this.listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
                @Override
                public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                    AdapterListing adapterListing = (AdapterListing) listView.getAdapter();
                    Profil profilselected = adapterListing.getItem(i);
                    profilselected.setEstSuiviParMoi(true);
                    profilsManager.addProfilOnPromenade(idTel,profilselected);

                    Intent intent = new Intent();
                    intent.setAction(ServiceAdmin.ACTION_FROM_ACTIVITY);
                    intent.putExtra("FOLLOW_NEW_SESSION","");
                    intent.putExtra("IDTEL",idTel);
                    intent.putExtra("NOM",profilselected.getNom());
                    intent.putExtra("PRENOM",profilselected.getPrenom());
                    getActivity().sendBroadcast(intent);
                    Fragment fragmap = MapFragment_.newInstance();
                    ((Main2Activity) getActivity()).pushFragmentFromActivity(fragmap);
                }
            });
        }

        EditText inputProfilSearch = (EditText) view.findViewById(R.id.editTextForSearchingProfil);
        inputProfilSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (worker != null)
                {
                    worker.cancel(true);
                }
                worker = new WorkerListingProfil();
                worker.setText(s.toString());
                worker.execute();
            }
        });

        return view;
    }

    public void updateListing(AdapterListing adapterListingBoutiques)
    {
        if (listView !=  null)
        {
            listView.setAdapter(adapterListingBoutiques);
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

    /**
     * Back pressed send from activity.
     *
     * @return if event is consumed, it will return true.
     */
    @Override
    public void onBackPressed() {
        Log.d("AAA", "BACKPressed profile");
        Fragment fragmap = MapFragment_.newInstance();
        ((Main2Activity) getActivity()).pushFragmentFromActivity(fragmap);
    }


    private class WorkerListingProfil extends AsyncTask
    {
        ArrayList<Profil> listProfilSelected;
        String text;

        public void setText(String text)
        {
            this.text = text;
        }

        @Override
        protected Object doInBackground(Object[] words)
        {
            listProfilSelected = new ArrayList<>();

            for(Profil b : profilsManager.getAllProfils())
            {
                if (b.getNom().contains(this.text) || b.getPrenom().contains(this.text))
                {
                    listProfilSelected.add(b);
                }
            }
            return null;

        }

        protected void onPostExecute(Object result)
        {
            final AdapterListing listingBoutiques = new AdapterListing(
                    getActivity(),R.layout.item_adapter_profil_listing,listProfilSelected );
            updateListing(listingBoutiques);
        }
    }


}

package com.dg.apptabletteandroid.Profils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dg.apptabletteandroid.Daemon.ServiceAdmin;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * Created by Remy on 14/12/2016.
 */


/**
 * Le manager des profils, ils sont stockés dans les préferences de l'application
 * Acces aux profils via l'objet SharedPreferences.
 */
public class ProfilsManager
{
    private ArrayList<Profil> profilArrayList;
    private HashMap<String,Profil> profilOnPromenade;

    /*
    Un profil sous la forme d'une string existe sous la forme suivante:
        "nom,prenom,BarriereAlerte" si susceptible de franchir la barriere
        "nom,prenom,BarriereNormal" si non susceptible de franchir la barriere
     */

    public void setAllProfils(SharedPreferences sharedPreferences,String allSignaturesProfils)
    {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        HashSet<String> ensembleProfils = new HashSet<>();
        String[] lesProfils = allSignaturesProfils.split("\\*");

        this.profilArrayList.clear();

        for(String uneSignature : lesProfils)
        {
            ensembleProfils.add(uneSignature);
            Profil unProfil = Profil.buildProfilFromSignature(uneSignature);
            profilArrayList.add(unProfil);
        }
        Log.e("strsetTTT", Arrays.deepToString(ensembleProfils.toArray()));
        editor.remove("profils");
        editor.putStringSet("profils",ensembleProfils);
        editor.commit();
    }



    public void addProfilOnPromenade(String idTel,Profil prof)
    {
        this.profilOnPromenade.put(idTel,prof);
        Log.d("idTel", idTel);
    }

    public void removeProfile(String prenom, String nom) {
        for(int i = 0; i < profilArrayList.size(); i++) {
            if(profilArrayList.get(i).getPrenom().equals(prenom) && profilArrayList.get(i).getNom().equals(nom)) {
                profilArrayList.remove(profilArrayList.get(i));
                return;
            }
        }
    }


    public void removeProfilOnPromenade(String idTel)
    {
        Log.e("ProfilManager",""+idTel+" is removed");
        this.profilOnPromenade.remove(idTel);
    }

    public ArrayList<Profil> getAllProfils()
    {
        return this.profilArrayList;
    }

    public Profil getProfil(String nom,String prenom)
    {
        for (Profil profil : profilArrayList)
        {
            if (profil.getNom().equals(nom) && profil.getPrenom().equals(prenom))
            {
                return profil;
            }
        }

        return null;
    }

    public ProfilsManager(SharedPreferences sharedPreferences)
    {
        this.profilArrayList = new ArrayList<>(); // contiendra tous les profils
        this.profilOnPromenade = new HashMap<>();
        Set<String> allProfils = sharedPreferences.getStringSet("profils",null);
        if (allProfils != null)
        {
            Iterator<String> iterProfil = allProfils.iterator();

            while(iterProfil.hasNext())
            {
                String profilsign = iterProfil.next();
                Log.e("unProfil-profilmanager",profilsign);
                profilArrayList.add(Profil.buildProfilFromSignature(profilsign));
            }
        }
    }

    public void updateList(SharedPreferences sharedPreferences, ArrayList<Profil> listProfils) {
        SharedPreferences.Editor editor = sharedPreferences.edit();
        HashSet<String> newEnsembleProfils = new HashSet<>();

        for(Profil elt: listProfils) {
                newEnsembleProfils.add(elt.makeSignature());
        }

        editor.remove("profils");
        editor.putStringSet("profils",newEnsembleProfils);
        editor.commit();
    }

    @Nullable
    public String findIdTelByProfil(Profil p)
    {
        HashMap<String,Profil> allProfils = this.getProfilOnPromenade();
        Set<String> allIdTel = allProfils.keySet();
        Iterator<String> allIdTelIter = allIdTel.iterator();
        while (allIdTelIter.hasNext())
        {
            String idTel = allIdTelIter.next();
            if (allProfils.get(idTel).equals(p))
            {
                // si p == allProfils.get(idTel), si le profil p à suivre est egal au profil, alors idTel est sa clé
                return idTel;
            }
        }
        return null;
    }

    public HashMap<String, Profil> getProfilOnPromenade() {
        return profilOnPromenade;
    }

}

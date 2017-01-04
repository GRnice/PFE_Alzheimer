package com.dg.apptabletteandroid.Profils;

import android.content.SharedPreferences;
import android.util.Log;

import com.dg.apptabletteandroid.Data.Profil;

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

}

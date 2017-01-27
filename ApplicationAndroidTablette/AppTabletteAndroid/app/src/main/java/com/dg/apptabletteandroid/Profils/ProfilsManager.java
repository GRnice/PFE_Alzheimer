package com.dg.apptabletteandroid.Profils;

import android.content.Intent;
import android.content.SharedPreferences;
import android.support.annotation.Nullable;
import android.util.Log;

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
public class ProfilsManager implements ProfilOnPromenadeManager
{
    private ArrayList<Profil> profilArrayList;
    private HashMap<String,Profil> profilOnPromenade;

    /*
    Un profil sous la forme d'une string existe sous la forme suivante:
        "nom,prenom,BarriereAlerte" si susceptible de franchir la barriere
        "nom,prenom,BarriereNormal" si non susceptible de franchir la barriere
     */

    // CONSTRUCTEUR
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

    /////////////////////// SETTER ///////////////////////

    /**
     * Sauvegarde en memoire les profils
     * @param sharedPreferences
     * @param allSignaturesProfils
     */
    public void saveAllProfils(SharedPreferences sharedPreferences, String allSignaturesProfils)
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

    /**
     * Ajoute un profil à la liste des profils en promenade
     * @param idTel
     * @param prof
     */
    public void addProfilOnPromenade(String idTel,Profil prof)
    {
        this.profilOnPromenade.put(idTel,prof);
        Log.d("idTel", idTel);
    }

    /**
     * Retire un profil à la liste des profils en promenade
     * @param idTel
     */
    public void removeProfilOnPromenade(String idTel)
    {
        Log.e("ProfilManager",""+idTel+" is removed");
        if (this.profilOnPromenade.containsKey(idTel))
        {
            this.profilOnPromenade.get(idTel).setMarker(null);
            this.profilOnPromenade.get(idTel).setEstSuiviParMoi(false);
            this.profilOnPromenade.get(idTel).setImmobile(false);
            this.profilOnPromenade.remove(idTel);
        }

    }

    /**
     * Retourne une ArrayList contenant tous les profils en promenade
     * @return ArrayList<Profil>
     */
    public HashMap<String,Profil> getAllProfilsOnPromenade()
    {
        return this.profilOnPromenade;
    }

    /**
     * Retire un profil de la liste des profils
     * @param prenom
     * @param nom
     */
    public void removeProfil(String prenom, String nom)
    {
        for(int i = 0; i < profilArrayList.size(); i++)
        {
            if(profilArrayList.get(i).getPrenom().equals(prenom) && profilArrayList.get(i).getNom().equals(nom)) {
                profilArrayList.remove(profilArrayList.get(i));
                return;
            }
        }
    }

    //////////////////////////////// GETTER ////////////////////////////////


    /**
     * Retourne la liste de tous les profils
     * @return ArrayList<Profil>
     */
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

    /**
     * Retourne un profil en promenade ayant comme identifiant idTel
     * @param idTel
     * @return Profil
     */
    @Nullable
    public Profil getProfilOnPromenade(String idTel)
    {
        if (profilOnPromenade.containsKey(idTel))
        {
            return profilOnPromenade.get(idTel);
        }
        return null;
    }

    /**
     *
     * @param sharedPreferences
     * @param listProfils
     */
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

    /**
     * Retourne l'identifiant d'un profil en promenade
     * @param profil
     * @return String, peut etre null
     */
    @Nullable
    public String findIdTelByProfil(Profil profil)
    {
        Set<String> allIdTel = profilOnPromenade.keySet();
        Iterator<String> allIdTelIter = allIdTel.iterator();
        while (allIdTelIter.hasNext())
        {
            String idTel = allIdTelIter.next();
            if (profilOnPromenade.get(idTel).equals(profil)) // Entry.getKey()
            {
                // si le profil p à suivre est egal au profil, alors idTel est sa clé
                return idTel;
            }
        }
        return null;
    }


}

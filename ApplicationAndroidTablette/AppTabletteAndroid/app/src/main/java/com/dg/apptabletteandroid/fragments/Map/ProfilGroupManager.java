package com.dg.apptabletteandroid.fragments.Map;

import android.util.Log;

import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.Profils.ProfilsManager;
import com.google.android.gms.maps.model.Marker;
import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by dominiquedib on 27/01/2017.
 */

public class ProfilGroupManager {
    private HashMap<Profil, Marker> hashProfilMarker;
    private HashMap<Marker, ArrayList<Profil>> hashMarkerGroupProfil;

    public ProfilGroupManager() {
        hashMarkerGroupProfil = new HashMap<>();
        hashProfilMarker = new HashMap<>();
    }

    public static ProfilGroupManager newInstance() {
        ProfilGroupManager groupManager = new ProfilGroupManager();
        return groupManager;
    }

    public void onUpdate(Profil profile, HashMap<String, Profil> allProfilsOnPromenade){
        if (allProfilsOnPromenade.values().size() == 0) {
            return;
        }

        if(hashProfilMarker.containsKey(profile)) {  // si P est dans un Groupe
            ArrayList<Profil> listProcheDeProfile = hashMarkerGroupProfil.get(hashProfilMarker.get(profile));

            ArrayList<Profil> listNewProche = new ArrayList<>();
            ArrayList<Profil> listFarToProfile = new ArrayList<>();

            for(Profil profilProche : listProcheDeProfile) {
                if(!profile.equals(profilProche)) {
                    if(isInside(profile, profilProche)) {
                        Log.e("AAAA", "isInside");
                        listNewProche.add(profilProche);
                    }
                    else {
                        listFarToProfile.add(profilProche);
                    }
                }
            }

            if(listNewProche.size() == listProcheDeProfile.size()) {  // si profile est proche de tous les profils qui etaient deja proche a lui
                return;
            }

            else
            {
                // sinon ben je quitte le groupe.
                    hashProfilMarker.remove(profile);  // je retire le profile des hash
                    listProcheDeProfile.remove(profile);
            }

        }

        else
        {  // P n'est pas dans un groupe
            for(Profil profilOthers : allProfilsOnPromenade.values()) {  // pr tous les profils != profile
                if(!profilOthers.equals(profile)) {
                    if(isInside(profile, profilOthers)) {  // profile est proche d'un group
                        Log.e("AAAA", "else de inside");
                        hashProfilMarker.put(profile, profilOthers.getMarker());
                      //  profile.setMarker();
                        hashMarkerGroupProfil.get(profile.getMarker()).add(profilOthers);
                        break;
                    }
                }
            }
        }

}


    public boolean isInside(Profil selectedProfil, Profil otherProfil) {
        selectedProfil.generateRaduis();
        double longitude = otherProfil.getLongitude() - selectedProfil.getLongitude();
        double latitude = otherProfil.getLatitude() - selectedProfil.getLatitude();
        double res =  Math.sqrt( Math.pow(longitude, 2.0) + Math.pow(latitude, 2.0));
        return res < selectedProfil.getRayon();
    }

   public HashMap<Marker, ArrayList<Profil>> getHashMarkerGroupProfil() {
       return hashMarkerGroupProfil;
   }

    public HashMap<Profil, Marker> getHashProfilMarker() {
        return hashProfilMarker;
    }
}

package com.dg.apptabletteandroid;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.annotation.BoolRes;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dg.apptabletteandroid.Daemon.ServiceAdmin;
import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.Profils.ProfilsManager;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Remy on 21/02/2017.
 */

public class RefitAgent
{
    /**
     * Synchronise Main2Activity suite à la réponse du serveur au CONTINUE transmis par la tablette
     * @param m l'activité
     * @param profilsOnPromenade liste des profils en promenades avec l'etat de chaque variable(batterie,temps promenade,alertes...)
     */
    public static void fix(Main2Activity m,String[] profilsOnPromenade)
    {

        if (profilsOnPromenade != null)
        {
            synchPromenades(m,profilsOnPromenade);
        }
        else
        {
            // si aucune promenade on fait un simple clear.
            ProfilsManager profilsManager = m.getProfilsManager();
            profilsManager.getAllProfilsOnPromenade().clear();
        }
    }

    private static void synchPromenades(Main2Activity m,String[] profilsOnPromenadeServeur)
    {
        ProfilsManager prof = m.getProfilsManager();
        HashMap<String,Profil> allProfilsOnPromenade = prof.getAllProfilsOnPromenade();
        HashSet<String> allIdTelServ = new HashSet<>();
        for (int i = 0 ; i < profilsOnPromenadeServeur.length ; i++)
        {
            String[] unProfilEnPromenadeServ = profilsOnPromenadeServeur[i].split("\\:");
            String idTelProfilEnPromenadeServeur = unProfilEnPromenadeServ[1];
            allIdTelServ.add(idTelProfilEnPromenadeServeur);
            if (allProfilsOnPromenade.containsKey(idTelProfilEnPromenadeServeur)) // si IDTEL existe
            {
                // si l'ancienne liste contient cet id
                String nom = unProfilEnPromenadeServ[3];
                String prenom = unProfilEnPromenadeServ[5];
                Profil p = prof.getProfil(nom,prenom);

                if (p != null) // si le profil n'existe pas, on a un profil à cet adresse idtel
                {
                    // à ce profil on le synchronise avec les infos de cette promenade + l'info follow contenue dans
                    // l'ancien objet de la prom -> allProfilsOnPromenade.get(idTelProfilEnPromenadeServeur)
                    p = synchProfilEnPromenade(m,p,allProfilsOnPromenade.get(idTelProfilEnPromenadeServeur),unProfilEnPromenadeServ);
                    prof.removeProfilOnPromenade(idTelProfilEnPromenadeServeur); // je supprime l'ancien objet
                    prof.addProfilOnPromenade(idTelProfilEnPromenadeServeur,p);
                }
                else // si ce profil n'existe pas alors il faut supprimer la promenade attachée à ce profil supprimé.
                {
                    prof.removeProfilOnPromenade(idTelProfilEnPromenadeServeur); // je supprime l'ancien objet
                }
            }
            else // CREATION d'une promenade
            {
                // cet id n'existe pas donc il faut l'ajouter, avec son profil associé.
                String nom = unProfilEnPromenadeServ[3];
                String prenom = unProfilEnPromenadeServ[5];
                Profil p = prof.getProfil(nom,prenom);
                p = synchProfilEnPromenade(m,p,null,unProfilEnPromenadeServ);
                prof.addProfilOnPromenade(idTelProfilEnPromenadeServeur,p);
            }
        }

        // intersection des idtels donnés par le serveur et ceux existants sur la tablette.
        allProfilsOnPromenade.keySet().retainAll(allIdTelServ);

    }

    private static Profil synchProfilEnPromenade(Main2Activity m, Profil enPromenade, @Nullable Profil profilObsolete, String[] profilSent)
    {
        /*IDTEL:777:NOM:Tallarida:PRENOM:Laurie:POSITION:(7.419617, 43.735383):BATTERY:20:
        ISHORSZONE:False:BATTERYISLOW:True:TIMEOUTPROMENADE:False:
        UPDATETIMEOUT:True:DUREEPROMENADE:1487692402
         */

        String idTel = profilSent[1];
        String nom = profilSent[3];
        String prenom = profilSent[5];
        String position = profilSent[7];
        String niveauBatterie = profilSent[9];
        String isHorsZone = profilSent[11];
        String batteryIsLow = profilSent[13];
        String isTimeoutPromenade = profilSent[15];
        String isUpdateTimeout = profilSent[17];
        String tempsRestant = profilSent[19];
        // appliquer les modifs de profilSent à enPromenade

        if (profilObsolete != null)
        {

            Log.e("Nom prenom fix old",profilObsolete.getNom()+" "+profilObsolete.getPrenom());
            Log.e("EstSuiviParMoi", String.valueOf(profilObsolete.estSuiviParMoi()));
        }
        else
        {
            Log.e("fix old NULL","!");
        }
        if (profilObsolete != null && profilObsolete.estSuiviParMoi() && profilObsolete.getPrenom().equals(prenom) && profilObsolete.getNom().equals(nom))
        {
            Log.e("Nom prenom fix old",profilObsolete.getNom()+" "+profilObsolete.getPrenom());
            enPromenade.setEstSuiviParMoi(true);
            Intent intentFollow = new Intent();
            intentFollow.setAction(ServiceAdmin.ACTION_FROM_ACTIVITY);
            intentFollow.putExtra("FOLLOW_SESSION_OVERTAKE","");
            intentFollow.putExtra("IDTEL",idTel);
            enPromenade.setMarker(profilObsolete.getMarker());
            m.sendBroadcast(intentFollow);
        }

        String[] latlong = position.split("\\(|\\)|\\,");
        Log.e("CONTENU LATLONG",Arrays.deepToString(latlong));
        if (latlong.length == 3)
        {
            enPromenade.setLatLong(Double.valueOf(latlong[2]),Double.valueOf(latlong[1])); // 48 -> latitude , 7 -> longitude
        }
        else
        {
            enPromenade.setLatLong(43.612248,7.079400); // 48 -> latitude , 7 -> longitude
        }

        enPromenade.setBattery(Integer.valueOf(niveauBatterie));
        enPromenade.setHorsZone(Boolean.valueOf(isHorsZone));
        enPromenade.setBatteryLow(Boolean.valueOf(batteryIsLow));
        enPromenade.setTempsRestant(Integer.parseInt(tempsRestant));
        enPromenade.setUpdateOut(Boolean.valueOf(isUpdateTimeout));
        return enPromenade;
    }
}

package com.dg.apptabletteandroid;

import android.content.Intent;
import android.support.annotation.BoolRes;
import android.support.annotation.Nullable;
import android.util.Log;

import com.dg.apptabletteandroid.Daemon.ServiceAdmin;
import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.Profils.ProfilsManager;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;

/**
 * Created by Remy on 21/02/2017.
 */

public class RefitAgent
{
    public static void fix(Main2Activity m,String[] allProfils,String[] profilsOnPromenade)
    {

        if (profilsOnPromenade != null)
        {
            synchPromenades(m,profilsOnPromenade);
        }
        else
        {
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
            if (allProfilsOnPromenade.containsKey(idTelProfilEnPromenadeServeur))
            {
                // si l'ancienne liste contient cet id
                String nom = unProfilEnPromenadeServ[3];
                String prenom = unProfilEnPromenadeServ[5];
                Profil p = prof.getProfil(nom,prenom);

                if (p != null) // si le profil n'existe pas, on a un profil à cet adresse idtel
                {
                    p = synchProfilEnPromenade(m,p,allProfilsOnPromenade.get(idTelProfilEnPromenadeServeur),unProfilEnPromenadeServ);
                    prof.removeProfilOnPromenade(idTelProfilEnPromenadeServeur); // je supprime l'ancien objet
                    prof.addProfilOnPromenade(idTelProfilEnPromenadeServeur,p);
                }
                else
                {
                    prof.removeProfilOnPromenade(idTelProfilEnPromenadeServeur); // je supprime l'ancien objet
                }
            }
            else
            {
                // cet id n'existe pas donc il faut l'ajouter
                String nom = unProfilEnPromenadeServ[3];
                String prenom = unProfilEnPromenadeServ[5];
                Profil p = prof.getProfil(nom,prenom);
                p = synchProfilEnPromenade(m,p,null,unProfilEnPromenadeServ);
                prof.addProfilOnPromenade(idTelProfilEnPromenadeServeur,p);
            }
        }

        allProfilsOnPromenade.keySet().retainAll(allIdTelServ); // intersection, on conserve seulement les idtels donnés par le serveur.


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
        String dureePromenade = profilSent[19];
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
            intentFollow.putExtra("FOLLOW_SESSION","");
            intentFollow.putExtra("IDTEL",idTel);
            m.sendBroadcast(intentFollow);
        }

        String[] latlong = position.split("\\(|\\)|\\,");
        Log.e("CONTENU LATLONG",Arrays.deepToString(latlong));
        enPromenade.setLatLong(Double.valueOf(latlong[2]),Double.valueOf(latlong[1])); // 48 -> latitude , 7 -> longitude
        enPromenade.setBattery(Integer.valueOf(niveauBatterie));
        enPromenade.setHorsZone(Boolean.valueOf(isHorsZone));
        enPromenade.setBatteryLow(Boolean.valueOf(batteryIsLow));
        enPromenade.setTempsRestant(Integer.parseInt(dureePromenade));
        enPromenade.setUpdateOut(Boolean.valueOf(isUpdateTimeout));
        return enPromenade;
    }
}

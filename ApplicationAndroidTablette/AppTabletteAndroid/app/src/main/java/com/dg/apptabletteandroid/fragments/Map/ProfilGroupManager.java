package com.dg.apptabletteandroid.fragments.Map;

import android.support.annotation.Nullable;
import android.util.Log;

import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.Profils.ProfilsManager;
import com.google.android.gms.maps.model.Marker;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;

/**
 * Created by dominiquedib on 27/01/2017.
 */

public class ProfilGroupManager
{

    private HashSet<Group> setOfGroups;

    public ProfilGroupManager() {
        setOfGroups = new HashSet<>();
    }

    public static ProfilGroupManager newInstance() {
        ProfilGroupManager groupManager = new ProfilGroupManager();
        return groupManager;
    }

    /**
     * Retourne un objet Group si le profil appartient à ce Group, sinon null
     * @param p
     * @return
     */
    @Nullable
    public Group dansUnGroupe(Profil p)
    {
        Iterator<Group> iterGroupe = setOfGroups.iterator();
        Group unGroupe;
        while (iterGroupe.hasNext())
        {
            unGroupe = iterGroupe.next();
            if (unGroupe.contains(p))
            {
                return unGroupe;
            }
        }
        return null;
    }


    /**
     * Indique si le profil est voisin avec tous les membres du groupe.
     * @return
     */
    private boolean voisinAvecToutLeGroupe(Profil leProfil,Group leGroupe)
    {
        return leGroupe.mayBeAdded(leProfil);
    }

    /**
     * Retourne un profil voisin, null si pas de voisin
     * @param profil
     * @param profilsEnPromenade
     * @return
     */
    @Nullable
    private Profil aUnVoisin(Profil profil,ArrayList<Profil> profilsEnPromenade) {
        for (Profil voisin : profilsEnPromenade) {
            if (!voisin.equals(profil) && isInside(profil, voisin)) {
                return voisin;
            }
        }
        return null;
    }

    public void onRemoveProfil(Profil p)
    {
        Iterator<Group> iterGroupe = setOfGroups.iterator();
        Group unGroupe;
        while (iterGroupe.hasNext())
        {
            unGroupe = iterGroupe.next();
            if (unGroupe.contains(p))
            {
                unGroupe.removeProfil(p);
                if (unGroupe.getSize() == 1)
                {
                    iterGroupe.remove();
                }
            }
        }

    }

    public void onUpdate(Profil profile, ArrayList<Profil> allProfilsOnPromenade)
    {
        Group unGroupe;
        Profil voisin;
        Log.e("ONUPDATE","-----");
        if ((unGroupe = dansUnGroupe(profile)) != null)
        {
            if (! voisinAvecToutLeGroupe(profile,unGroupe))
            {
                unGroupe.removeProfil(profile);
                if (unGroupe.getSize() == 1)
                {
                    setOfGroups.remove(unGroupe);
                }
            }
        }
        else
        {
            if ((voisin = aUnVoisin(profile,allProfilsOnPromenade)) != null)
            {
                if ((unGroupe = dansUnGroupe(voisin)) != null)
                {
                    if (unGroupe.mayBeAdded(profile))
                    {
                        unGroupe.addProfil(profile);
                    }
                }
                else
                {
                    Group nwGroup = new Group();
                    nwGroup.addProfil(profile);
                    nwGroup.addProfil(voisin);
                    setOfGroups.add(nwGroup);
                    Log.e("NWGROUPE","-----");
                }
            }
        }

    }


    public static boolean isInside(Profil selectedProfil, Profil otherProfil)
    {
        selectedProfil.generateRaduis();
        double longitude = otherProfil.getLongitude() - selectedProfil.getLongitude();
        double latitude = otherProfil.getLatitude() - selectedProfil.getLatitude();
        double res =  Math.sqrt( Math.pow(longitude, 2.0) + Math.pow(latitude, 2.0));
        return res < selectedProfil.getRayon();
    }

}

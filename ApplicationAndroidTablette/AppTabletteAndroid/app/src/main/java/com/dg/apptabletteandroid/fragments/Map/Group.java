package com.dg.apptabletteandroid.fragments.Map;

import com.dg.apptabletteandroid.Profils.Profil;
import com.google.android.gms.maps.model.Marker;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

/**
 * Created by Remy on 01/02/2017.
 */

public class Group
{
    private HashSet<Profil> ensembleProfil;
    private int marqueurRecherche;

    public Group()
    {
        ensembleProfil = new HashSet<>();
    }

    public void setMarqueurRecherche(int marqueurRecherche)
    {
        this.marqueurRecherche = marqueurRecherche;
    }

    public int getMarqueurRecherche()
    {
        return marqueurRecherche;
    }

    /**
     * Retourne l'ensemble des profils associé à ce groupe.
     * @return
     */
    public Iterator<Profil> getProfils()
    {
        return ensembleProfil.iterator();
    }

    /**
     * Retourne la taille du groupe.
     * @return
     */
    public int getSize()
    {
        return ensembleProfil.size();
    }

    public Profil getAnyProfil()
    {
        return getProfils().next();
    }

    /**
     * Ajoute ce profil au groupe
     * @param prof
     */
    public void addProfil(Profil prof)
    {
        if (!ensembleProfil.contains(prof))
        {
            ensembleProfil.add(prof);
        }
    }

    /**
     * Indique si ce profil appartient à ce groupe
     * @param prof
     * @return
     */
    public boolean contains(Profil prof)
    {
        return ensembleProfil.contains(prof);
    }

    /**
     * Retire ce profil du groupe
     * @param prof
     */
    public void removeProfil(Profil prof)
    {
        if (ensembleProfil.contains(prof))
        {
            ensembleProfil.remove(prof);
        }
    }

    /**
     * Indique si ce profil est voisin avec tous les membres du groupe.
     * @param prof
     * @return
     */
    public boolean mayBeAdded(Profil prof)
    {
        Iterator<Profil> profilsDuGroupe = getProfils();
        while (profilsDuGroupe.hasNext())
        {
            if (! ProfilGroupManager.isInside(profilsDuGroupe.next(),prof))
            {
                return false;
            }
        }
        return true;
    }

    public String stringifyForMarker()
    {
        Iterator<Profil> iter = getProfils();
        StringBuilder sb = new StringBuilder();
        Profil p;
        while(iter.hasNext())
        {
            p = iter.next();
            sb.append(p.getPrenom()+" "+p.getNom()+"\n");
        }

        return sb.toString();
    }

}

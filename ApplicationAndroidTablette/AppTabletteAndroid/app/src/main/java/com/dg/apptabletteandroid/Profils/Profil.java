package com.dg.apptabletteandroid.Profils;

import android.support.annotation.Nullable;

import com.google.android.gms.maps.model.Marker;

/**
 * Created by Remy on 08/12/2016.
 */

public class Profil
{
    public static Profil buildProfilFromSignature(String signature)
    {
        // "nom,prenom,BarrierAlerte" ou "nom,prenom,BarrierNormal"
        Profil prof = new Profil();
        String[] tabProfil = signature.split(",");


        prof.setNom(tabProfil[0]);
        prof.setPrenom(tabProfil[1]);
        prof.susceptibleDeFranchirLaBarriere(tabProfil[2].equals("BarriereAlerte"));
        return prof;
    }


    private String nom = null;
    private String prenom = null;
    private boolean suivi = false; // si ce profil est suivi par au moins une personne
    private boolean suiviParTabletteCourante = false; // si la tablette courante suis ce profil
    private boolean susceptibleDeFranchirLaBarriere = false;
    private double longitude;
    private double latitude;
    private Marker marker;
    private int battery;
    private boolean immobile = false;

    public Profil(String nom,String prenom,boolean susceptibleDeFranchirLaBarriere)
    {
        this.nom = nom;
        this.prenom = prenom;
        this.susceptibleDeFranchirLaBarriere = susceptibleDeFranchirLaBarriere;
        this.marker = null;
    }

    public Profil()
    {

    }

    public void susceptibleDeFranchirLaBarriere(boolean valeur)
    {
        this.susceptibleDeFranchirLaBarriere = valeur;
    }

    public Boolean getSusceptibleDeFranchirLaBarriere() {
        return susceptibleDeFranchirLaBarriere;
    }

    public String getPrenom()
    {
        return prenom;
    }

    public String getNom()
    {
        return nom;
    }

    @Nullable
    public Marker getMarker()
    {
        return marker;
    }

    public boolean estSuivi()
    {
        return suivi;
    }

    public boolean estSuiviParMoi()
    {
        return suiviParTabletteCourante;
    }

    public void setEstSuiviParMoi(boolean etat)
    {
        suiviParTabletteCourante = etat;
    }

    public void setPrenom(String prenom)
    {
        this.prenom = prenom;
    }

    public void setNom(String nom)
    {
        this.nom = nom;
    }

    public String makeSignature()
    {
        StringBuilder sb = new StringBuilder(this.nom+","+this.prenom+",");

        if (susceptibleDeFranchirLaBarriere)
        {
            sb.append("BarriereAlerte");
        }
        else
        {
            sb.append("BarriereNormal");
        }

        return sb.toString();
    }

    public void setLatLong(double lat,double longitude)
    {
        latitude = lat;
        this.longitude = longitude;
    }

    public void setMarker(Marker markerP)
    {
        marker = markerP;
    }

    public double getLongitude() {
        return longitude;
    }

    public double getLatitude() {
        return latitude;
    }

    public int getBattery() {
        return battery;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public boolean isImmobile() {
        return immobile;
    }

    public void setImmobile(boolean immobile) {
        this.immobile = immobile;
    }
}

package com.dg.apptabletteandroid.Profils;

import android.support.annotation.Nullable;
import android.util.Log;

import com.google.android.gms.maps.model.Marker;


/**
 * Created by Remy on 08/12/2016.
 */

public class Profil
{


    public static Profil buildProfilFromSignature(String signature)
    {
        // "nom,prenom,idRessourceAvatar,BarrierAlerte" ou "nom,prenom,idRessourceAvatar,BarrierNormal"
        Profil prof = new Profil();
        String[] tabProfil = signature.split(",");

        Log.e(signature,"SIGNATURE");
        prof.setNom(tabProfil[0]);
        prof.setPrenom(tabProfil[1]);
        prof.idRessourcesAvatar = Integer.valueOf(tabProfil[2]);
        prof.susceptibleDeFranchirLaBarriere(tabProfil[3].equals("BarriereAlerte"));
        return prof;
    }


    private String nom = null;
    private String prenom = null;
    private int tempsRestant = 10000;
    private int idRessourcesAvatar;
    private boolean horsZone = false;
    private boolean suivi = false; // si ce profil est suivi par au moins une personne
    private boolean suiviParTabletteCourante = false; // si la tablette courante suis ce profil
    private boolean susceptibleDeFranchirLaBarriere = false;
    private boolean immobile = false; // si il est immobile
    private boolean batteryLow = false; // si la batterie est faible
    private boolean updateOut = false; // si le profil n'emet plus depuis un certains temps
    private double longitude;
    private double longitudeExtrem;  // les set en fct de long lat
    private double latitude;
    private double latitudeExtrem;
    private double rayon;
    private Marker marker;
    private int battery;


    public Profil(String nom,String prenom,boolean susceptibleDeFranchirLaBarriere,int idAvatar)
    {
        this.nom = nom;
        this.prenom = prenom;
        this.idRessourcesAvatar = idAvatar;
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

    public int getIdRessourcesAvatar()
    {
        return idRessourcesAvatar;
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

    public void setHorsZone(boolean state)
    {
        horsZone = state;
    }

    public boolean isHorsZone()
    {
        return horsZone;
    }

    public String makeSignature()
    {
        StringBuilder sb = new StringBuilder(this.nom+","+this.prenom+","+String.valueOf(this.idRessourcesAvatar)+",");

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

    public void setIdAvatar(int id)
    {
        idRessourcesAvatar = id;
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


    public double getLatitudeExtrem() {
        return latitudeExtrem;
    }

    public double getLongitudeExtrem() {
        return longitudeExtrem;
    }

    public double generateRaduis() {
        longitudeExtrem = longitude + 0.0001;
        latitudeExtrem = latitude + 0.0001;

        rayon = Math.sqrt( Math.pow(longitudeExtrem - longitude, 2.0) + Math.pow(latitudeExtrem - latitude, 2.0) );
        return rayon;
    }
    public double getRayon() {
        return rayon;
    }

    public int getBattery() {
        return battery;
    }

    public int getTempsRestant()
    {
        return tempsRestant;
    }

    public void setBattery(int battery) {
        this.battery = battery;
    }

    public boolean isImmobile() {
        return immobile;
    }

    public boolean batteryIsLow()
    {
        return batteryLow;
    }

    public boolean updateIsTimeout()
    {
        return updateOut;
    }

    public void setImmobile(boolean immobile) {
        this.immobile = immobile;
    }

    public void setBatteryLow(boolean batteryLow) {
        this.batteryLow = batteryLow;
    }

    public void setTempsRestant(int tempsRestant) {
        this.tempsRestant = tempsRestant;
    }

    public void setUpdateOut(boolean updateOut) {
        this.updateOut = updateOut;
    }
}

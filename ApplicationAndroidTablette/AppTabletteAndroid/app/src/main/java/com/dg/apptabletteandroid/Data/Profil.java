package com.dg.apptabletteandroid.Data;

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

    public Profil(String nom,String prenom,boolean susceptibleDeFranchirLaBarriere)
    {
        this.nom = nom;
        this.prenom = prenom;
        this.susceptibleDeFranchirLaBarriere = susceptibleDeFranchirLaBarriere;
    }

    public Profil()
    {

    }

    public void susceptibleDeFranchirLaBarriere(boolean valeur)
    {
        this.susceptibleDeFranchirLaBarriere = valeur;
    }

    public String getPrenom()
    {
        return prenom;
    }

    public String getNom()
    {
        return nom;
    }

    public boolean estSuivi()
    {
        return suivi;
    }

    public boolean estSuiviParMoi()
    {
        return suiviParTabletteCourante;
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
}

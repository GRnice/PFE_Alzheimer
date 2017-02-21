package com.dg.apptabletteandroid.Profils;

import com.google.android.gms.maps.model.Marker;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

/**
 * Created by Remy on 24/01/2017.
 */
public interface ProfilOnPromenadeManager
{
    public void addProfilOnPromenade(String idTel,Profil prof);
    public void removeProfilOnPromenade(String idTel);
    public HashMap<String,Profil> getAllProfilsOnPromenade();
    public Profil getProfilOnPromenade(String idTel);
    public String findIdTelByProfil(Profil p);

    void unfollow(Profil profilUnfollow);
}

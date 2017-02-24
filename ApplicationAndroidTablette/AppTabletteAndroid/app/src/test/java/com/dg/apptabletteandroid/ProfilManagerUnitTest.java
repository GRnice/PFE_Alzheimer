package com.dg.apptabletteandroid;

import android.util.Log;

import com.dg.apptabletteandroid.Profils.Profil;
import com.dg.apptabletteandroid.Profils.ProfilsManager;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * Created by Remy on 24/02/2017.
 */

public class ProfilManagerUnitTest
{
    @Test
    public void addProfil() throws Exception
    {
        ProfilsManager profilsManager = new ProfilsManager();
        assertTrue(profilsManager.getAllProfilsOnPromenade().size() == 0);
        Profil mock = new Profil();
        mock.setPrenom("Prenom");
        mock.setNom("Nom");
        profilsManager.addProfilOnPromenade("123456789",mock);
        assertTrue(profilsManager.getAllProfilsOnPromenade().size() == 1);
        profilsManager.removeProfilOnPromenade("123456789");
        assertTrue(profilsManager.getAllProfilsOnPromenade().size() == 0);
        profilsManager.unfollow(mock);
        assertTrue(mock.getTempsRestant() == 10000);
        assertTrue(!mock.estSuiviParMoi());
    }
}

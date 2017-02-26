package com.dg.apptabletteandroid;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;
import android.content.Intent;
import android.support.design.widget.FloatingActionButton;

import com.dg.apptabletteandroid.Daemon.ServiceAdmin;
import com.dg.apptabletteandroid.fragments.Profils.ProfilFragment;

/**
 * Created by Remy on 14/12/2016.
 */


/**
 * Ce manager permet de passer d'un fragment à un autre
 */
public class FragmentManager
{
    Fragment fragmentCourant = null;

    public FragmentManager()
    {

    }

    public Fragment getCurrentFragment()
    {
        return fragmentCourant;
    }

    /**
     * Cette fonction pose le nouveau fragment dans le layout de l'activité passée en parametre
     * @param frag
     * @param act
     */
    public void pushFragment(Fragment frag, Activity act)
    {
        Intent intentStopSound = new Intent();
        intentStopSound.setAction(ServiceAdmin.ACTION_FROM_ACTIVITY);
        intentStopSound.putExtra("STOP_SOUND","");
        act.sendBroadcast(intentStopSound);

        FragmentTransaction ft = act.getFragmentManager().beginTransaction();
        if (fragmentCourant != null)
        {
            ft.remove(fragmentCourant);
            ft.commit();
        }
        FragmentTransaction ft2 = act.getFragmentManager().beginTransaction();

        ft2.replace(R.id.fragment,frag);
        ft2.commit();
        fragmentCourant = frag;

    }
}

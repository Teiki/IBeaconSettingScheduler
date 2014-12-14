package fr.teiki.estimoteibeacon;

import android.content.Context;
import android.content.SharedPreferences;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;

import java.util.HashSet;
import java.util.Set;

import fr.teiki.estimoteibeacon.Module.MySoundManager;


/**
 * Created by antoinegaltier on 07/12/14.
 */
public class MyPreferenceManager {


    static SharedPreferences preferences;

    public static void MyPreferenceManagerSetter(Context ctx){
        if (preferences == null)
            preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
    }

    public static Set<String> getAssociatedActionSet(Context ctx, Beacon beacon){
        MyPreferenceManagerSetter(ctx);
        return preferences.getStringSet(beacon.getMacAddress(),null);
    }

    public static void addAction(Context ctx, Beacon beacon, String key){
        MyPreferenceManagerSetter(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> set  = preferences.getStringSet(beacon.getMacAddress(),null);
        if (set == null)
            set = new HashSet<>();
        if (!set.contains(BeaconSettingsActivity.KEY_DONT_DISTURB))
            set.add(BeaconSettingsActivity.KEY_DONT_DISTURB);

        editor.putStringSet(beacon.getMacAddress(), set);
        editor.apply();
    }

    public static void removeAction(Context ctx, Beacon beacon, String key){
        MyPreferenceManagerSetter(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> set  = preferences.getStringSet(beacon.getMacAddress(),null);
        if (set != null) {
            if (set.contains(BeaconSettingsActivity.KEY_DONT_DISTURB)) {
                set.remove(BeaconSettingsActivity.KEY_DONT_DISTURB);
                editor.putStringSet(beacon.getMacAddress(), set);
                editor.apply();
            }
        }
    }


    public static void executeActions(Context ctx, Beacon beacon){
        MyPreferenceManagerSetter(ctx);
        Set<String> set  = preferences.getStringSet(beacon.getMacAddress(),null);
        if (set != null){
            for (String action : set){
                if (action.equals(BeaconSettingsActivity.KEY_DONT_DISTURB)) {
                    float distance = preferences.getFloat(beacon.getMacAddress()+action, (float) 0.01);
                    if (Utils.computeAccuracy(beacon)<distance)
                        MySoundManager.sound_cut(ctx, true);
                    else
                        MySoundManager.sound_cut(ctx, false);
                }
            }
        }
    }

    public static void desactivateActions(Context ctx, Beacon beacon){
        MyPreferenceManagerSetter(ctx);
        Set<String> set  = preferences.getStringSet(beacon.getMacAddress(),null);
        if (set != null){
            for (String action : set){
                if (action.equals(BeaconSettingsActivity.KEY_DONT_DISTURB)){
                    MySoundManager.sound_cut(ctx, false);
                }
            }
        }
    }

    public static void updateDistanceAction(Context ctx, Beacon beacon, String action, float distance){
        MyPreferenceManagerSetter(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        if (preferences.getFloat(beacon.getMacAddress()+action,-1) == -1)
            editor.remove(beacon.getMacAddress()+action);

        editor.putFloat(beacon.getMacAddress()+action,distance);

        editor.apply();
    }
}

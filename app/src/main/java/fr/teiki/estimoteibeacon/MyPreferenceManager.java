package fr.teiki.estimoteibeacon;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import fr.teiki.estimoteibeacon.Module.MySoundManager;
import fr.teiki.estimoteibeacon.Module.MyWifiManager;


/**
 * Created by antoinegaltier on 07/12/14.
 */
public class MyPreferenceManager {

    private final static String START = "start";
    private final static String END = "end";


    private static Stack<Double> distance_stack = new Stack<>();

    public static Set<String> getAssociatedActionSet(Context ctx, Beacon beacon){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        return preferences.getStringSet(beacon.getMacAddress(),null);
    }

    public static String getAssociatedActionParam(Context ctx, Beacon beacon, String key){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        return preferences.getString(beacon.getMacAddress() + key, null);
    }

    public static int getAssociatedActionState(Context ctx, Beacon beacon, String key){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        return preferences.getInt(beacon.getMacAddress() + key, -1);
    }

    public static String getAssociatedActivationMode(Context ctx, Beacon beacon, String action){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        return preferences.getString(beacon.getMacAddress() + action +BeaconSettingsActivity.ACTIVATION_MODE, "");
    }

    public static float[] getAssociatedPerimeter(Context ctx, Beacon beacon, String action, String zone){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        float start = preferences.getFloat(beacon.getMacAddress()+action+BeaconSettingsActivity.PERIMETRE+zone+START,99);
        float end = preferences.getFloat(beacon.getMacAddress()+action+BeaconSettingsActivity.PERIMETRE+zone+END,-1);
        return new float[]{start,end};
    }

    public static int getAssociatedNotificationParam(Context ctx, Beacon beacon, String action, String zone){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        return preferences.getInt(beacon.getMacAddress()+action+zone,-1);
    }

    public static List<String> getListSavedBeacon(Context ctx){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        Map<String,?> map =  preferences.getAll();
        List<String> res = new ArrayList<>();
        for (Map.Entry<String, ?> entry : map.entrySet()){
            if (entry != null && entry.getValue() instanceof Set<?>){
                res.add(entry.getKey());
            }
        }
        return res;
    }

    public static void addAction(Context ctx, Beacon beacon, String key, int radio){
        addAction(ctx,beacon,key);

        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        if (preferences.getInt(beacon.getMacAddress()+key,-1) != -1)
            editor.remove(beacon.getMacAddress()+key);
        editor.putInt(beacon.getMacAddress() + key, radio);

        editor.apply();
    }

    public static void addAction(Context ctx, Beacon beacon, String key, String packagename){
        addAction(ctx,beacon,key);

        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        if (preferences.getString(beacon.getMacAddress()+key,null) != null)
            editor.remove(beacon.getMacAddress()+key);
        editor.putString(beacon.getMacAddress() + key, packagename);

        editor.apply();
    }

    public static void addAction(Context ctx, Beacon beacon, String key){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> set  = preferences.getStringSet(beacon.getMacAddress(),null);
        if (set == null)
            set = new HashSet<>();
        if (!set.contains(key))
            set.add(key);

        editor.putStringSet(beacon.getMacAddress(), set);
        editor.apply();
    }

    public static void removeAction(Context ctx, Beacon beacon, String key){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> set  = preferences.getStringSet(beacon.getMacAddress(),null);
        if (set != null) {
            if (set.contains(key)) {
                set.remove(key);
                editor.putStringSet(beacon.getMacAddress(), set);
                editor.commit();
            }
        }
    }


    public static void executeActions(Context ctx, Beacon beacon){
        updateDistanceAverage(beacon);
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        Set<String> set  = preferences.getStringSet(beacon.getMacAddress(),null);
        if (set != null){
            for (String action : set){

                if (action.equals(BeaconSettingsActivity.KEY_SOUND_MODE)) {
                    int notification_mode;
                    if (preferences.getString(beacon.getMacAddress()+action+BeaconSettingsActivity.ACTIVATION_MODE,"").equals(BeaconSettingsActivity.REGION)) {
                        notification_mode = preferences.getInt(beacon.getMacAddress() + action + Utils.computeProximity(beacon).name(), -1);
                        if (notification_mode != -1){
                            MySoundManager.change_notification_mode(ctx, true, notification_mode);
                        }
                        else
                            MySoundManager.change_notification_mode(ctx, false, notification_mode);
                    }
                    else {
                        boolean action_activate = false;
                        for (String perimetre : BeaconSettingsActivity.PERIMETRES) {
                            if (getDistanceAverage() >= preferences.getFloat(beacon.getMacAddress() + action + perimetre + START, 99) &&
                                    getDistanceAverage() <= preferences.getFloat(beacon.getMacAddress() + action + perimetre + END, -1)) {
                                action_activate = true;
                                notification_mode = preferences.getInt(beacon.getMacAddress() + action + perimetre, -1);
                                MySoundManager.change_notification_mode(ctx, true, notification_mode);
                                break;
                            }
                        }
                        if (action_activate)
                            MySoundManager.change_notification_mode(ctx, false, -1);
                    }


                }
//                else if (action.equals(BeaconSettingsActivity.KEY_WIFI_STATE)) {
//                    boolean action_activate = false;
//                    if (preferences.getString(beacon.getMacAddress()+action+BeaconSettingsActivity.ACTIVATION_MODE,"").equals(BeaconSettingsActivity.REGION)){
//                        if (preferences.getString(beacon.getMacAddress()+action+Utils.computeProximity(beacon).name(),"").equals())
//                            action_activate = true;
//                    }
//                    int radioid = preferences.getInt(beacon.getMacAddress()+action,R.id.wifi_desactivate);
//                    MyWifiManager.changeWifiState(ctx,action_activate,radioid);
////                    if (getDistanceAverage()<distance)
////                        MyWifiManager.changeWifiState(ctx,true,radioid);
////                    else
////                        MyWifiManager.changeWifiState(ctx,false,radioid);
//                }
            }
        }
    }

    public static void executeSingleActions(Context ctx, Beacon beacon){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        Set<String> set  = preferences.getStringSet(beacon.getMacAddress(),null);
        if (set != null){
            for (String action : set){
                if (action.equals(BeaconSettingsActivity.KEY_LAUNCH_APP)) {
                    float distance = preferences.getFloat(beacon.getMacAddress()+BeaconSettingsActivity.KEY_DISTANCE+action, (float) 1);
                    String packagename = preferences.getString(beacon.getMacAddress() + action, null);
                    if (packagename != null && Utils.computeAccuracy(beacon)<distance) {
                        Intent mIntent = ctx.getPackageManager().getLaunchIntentForPackage(packagename);
                        if (mIntent != null) {
                            ctx.startActivity(mIntent);
                        }
                    }
                }
            }
        }
    }


    public static void updatePerimeterAction(Context ctx, Beacon beacon, String action, String zone, float start, float end){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(beacon.getMacAddress()+action+BeaconSettingsActivity.ACTIVATION_MODE,BeaconSettingsActivity.PERIMETRE);
        editor.putFloat(beacon.getMacAddress()+action+zone+START,start);
        editor.putFloat(beacon.getMacAddress()+action+zone+END,end);

        editor.apply();
    }

    public static void updateRegionAction(Context ctx, Beacon beacon, String action, String region){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(beacon.getMacAddress()+action+BeaconSettingsActivity.ACTIVATION_MODE,BeaconSettingsActivity.REGION);
        editor.putString(beacon.getMacAddress()+action+BeaconSettingsActivity.REGION,region);

        editor.apply();
    }

    public static double getDistanceAverage(){
        if (distance_stack != null) {
            double res = 0;
            for (double distance : distance_stack) {
                res += distance;
            }
            return res / distance_stack.size();
        }
        return 100;
    }

    public static void updateDistanceAverage(Beacon beacon){
        if (distance_stack.size() > 10)
            distance_stack.setSize(10);
        distance_stack.push(Utils.computeAccuracy(beacon));
    }
}

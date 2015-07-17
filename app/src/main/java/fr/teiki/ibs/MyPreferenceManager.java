package fr.teiki.ibs;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.radiusnetworks.ibeacon.IBeacon;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Stack;

import fr.teiki.ibs.Module.MySoundManager;
import fr.teiki.ibs.util.BleUtil;


/**
 * Created by antoinegaltier on 07/12/14.
 */
public class MyPreferenceManager {

    private final static String START = "start";
    private final static String END = "end";
    private final static String BEACON_NAME = "BEACON_NAME";


    private static Stack<Double> distance_stack = new Stack<>();

    public static Set<String> getAssociatedActionSet(Context ctx, String macaddr_beacon){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        return preferences.getStringSet(macaddr_beacon, null);
    }

    public static String getAssociatedActionParam(Context ctx, String macaddr_beacon, String key){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        return preferences.getString(macaddr_beacon + key, null);
    }

    public static int getAssociatedActionState(Context ctx, String macaddr_beacon, String key){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        return preferences.getInt(macaddr_beacon + key, -1);
    }

    public static String getAssociatedActivationMode(Context ctx, String macaddr_beacon, String action){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        return preferences.getString(macaddr_beacon + action +BeaconSettingsActivity.ACTIVATION_MODE, "");
    }

    public static float[] getAssociatedPerimeter(Context ctx, String macaddr_beacon, String action, String zone){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        float start = preferences.getFloat(macaddr_beacon+action+BeaconSettingsActivity.PERIMETRE+zone+START,99);
        float end = preferences.getFloat(macaddr_beacon + action + BeaconSettingsActivity.PERIMETRE + zone + END, -1);
        return new float[]{start,end};
    }

    public static int getAssociatedNotificationParam(Context ctx, String macaddr_beacon, String action, String zone){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        return preferences.getInt(macaddr_beacon+action+zone,-1);
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

    public static void addAction(Context ctx, String macaddr_beacon, String key, int radio){
        addAction(ctx,macaddr_beacon,key);

        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        if (preferences.getInt(macaddr_beacon + key, -1) != -1)
            editor.remove(macaddr_beacon+key);
        editor.putInt(macaddr_beacon + key, radio);

        editor.apply();
    }

    public static void addAction(Context ctx, String macaddr_beacon, String key, String packagename){
        addAction(ctx,macaddr_beacon,key);

        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        if (preferences.getString(macaddr_beacon+key,null) != null)
            editor.remove(macaddr_beacon + key);
        editor.putString(macaddr_beacon + key, packagename);

        editor.apply();
    }

    public static void addAction(Context ctx, String macaddr_beacon, String key){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> set  = preferences.getStringSet(macaddr_beacon,null);
        if (set == null)
            set = new HashSet<>();
        if (!set.contains(key))
            set.add(key);

        editor.putStringSet(macaddr_beacon, set);
        editor.apply();
    }

    public static void removeAction(Context ctx, String macaddr_beacon, String key){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        Set<String> set  = preferences.getStringSet(macaddr_beacon,null);
        if (set != null) {
            if (set.contains(key)) {
                set.remove(key);
                editor.putStringSet(macaddr_beacon, set);
                editor.commit();
            }
        }
    }


    public static void executeActions(Context ctx, String macaddr_beacon, int rssi, byte[] scanRecord){
        IBeacon beacon = IBeacon.fromScanData(scanRecord, rssi);
        if (beacon==null)
            return;
        updateDistanceAverage(beacon);
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        Set<String> set  = preferences.getStringSet(macaddr_beacon,null);
        if (set != null){
            for (String action : set){

                if (action.equals(BeaconSettingsActivity.KEY_SOUND_MODE)) {
                    int notification_mode;
                    if (preferences.getString(macaddr_beacon+action+BeaconSettingsActivity.ACTIVATION_MODE,"").equals(BeaconSettingsActivity.REGION)) {
                        notification_mode = preferences.getInt(macaddr_beacon + action + BleUtil.computeProximity(beacon).name(), -1);
                        if (notification_mode != -1){
                            MySoundManager.change_notification_mode(ctx, true, notification_mode);
                        }
                        else
                            MySoundManager.change_notification_mode(ctx, false, notification_mode);
                    }
                    else {
                        boolean action_activate = false;
                        for (String perimetre : BeaconSettingsActivity.PERIMETRES) {
                            if (getDistanceAverage() >= preferences.getFloat(macaddr_beacon + action + perimetre + START, 99) &&
                                    getDistanceAverage() <= preferences.getFloat(macaddr_beacon + action + perimetre + END, -1)) {
                                action_activate = true;
                                notification_mode = preferences.getInt(macaddr_beacon + action + perimetre, -1);
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

//    public static void executeSingleActions(Context ctx, Beacon beacon){
//        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
//        Set<String> set  = preferences.getStringSet(beacon.getMacAddress(),null);
//        if (set != null){
//            for (String action : set){
//                if (action.equals(BeaconSettingsActivity.KEY_LAUNCH_APP)) {
//                    float distance = preferences.getFloat(beacon.getMacAddress()+BeaconSettingsActivity.KEY_DISTANCE+action, (float) 1);
//                    String packagename = preferences.getString(beacon.getMacAddress() + action, null);
//                    if (packagename != null && Utils.computeAccuracy(beacon)<distance) {
//                        Intent mIntent = ctx.getPackageManager().getLaunchIntentForPackage(packagename);
//                        if (mIntent != null) {
//                            ctx.startActivity(mIntent);
//                        }
//                    }
//                }
//            }
//        }
//    }


    public static void updateActivationMode(Context ctx, String macaddr_beacon, String action, String mode){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putString(macaddr_beacon+action+BeaconSettingsActivity.ACTIVATION_MODE,mode);
        editor.apply();
    }

    public static void updatePerimeterZone(Context ctx, String macaddr_beacon, String action, String perimeter, float start, float end){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putFloat(macaddr_beacon+action+perimeter+START,start);
        editor.putFloat(macaddr_beacon + action + perimeter + END, end);

        editor.apply();
    }

    public static void updateZoneAction(Context ctx, String macaddr_beacon, String action, String zone, int notification_mode){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putInt(macaddr_beacon + action + zone, notification_mode);

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

    public static void updateDistanceAverage(IBeacon beacon){
        if (distance_stack.size() > 10)
            distance_stack.setSize(10);
        distance_stack.push(BleUtil.computeAccuracy(beacon));
    }

    public static String getBeaconName(Context ctx, String macaddr_beacon){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        return preferences.getString(macaddr_beacon+BEACON_NAME,"");
    }

    public static void setBeaconName(Context ctx, String macaddr_beacon, String name){
        SharedPreferences preferences = android.preference.PreferenceManager.getDefaultSharedPreferences(ctx);
        SharedPreferences.Editor editor = preferences.edit();

        editor.putString(macaddr_beacon + BEACON_NAME, name);

        editor.apply();
    }
}

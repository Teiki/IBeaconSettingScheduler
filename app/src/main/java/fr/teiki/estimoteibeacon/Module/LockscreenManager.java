package fr.teiki.estimoteibeacon.Module;

import android.app.Activity;
import android.app.ActivityManager;
import android.app.admin.DevicePolicyManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by antoinegaltier on 15/12/14.
 */
public class LockscreenManager {

    public static void lockscreen(Activity ctx){
//        ComponentName compName = new ComponentName(ctx, MyAdmin.class);
//
//        Intent intent = new Intent(DevicePolicyManager
//                .ACTION_ADD_DEVICE_ADMIN);
//        intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN,
//                compName);
//        intent.putExtra(DevicePolicyManager.EXTRA_ADD_EXPLANATION,
//                "Additional text explaining why this needs to be added.");
//        ctx.startActivityForResult(intent, 0);
//
//        DevicePolicyManager deviceManger = (DevicePolicyManager)ctx.getSystemService(
//                Context.DEVICE_POLICY_SERVICE);
//
//
//        ActivityManager activityManager = (ActivityManager)ctx.getSystemService(
//                Context.ACTIVITY_SERVICE);
//        if (deviceManger.isAdminActive(compName))
//            deviceManger.lockNow();

        DevicePolicyManager mDPM = (DevicePolicyManager) ctx.getSystemService(Context.DEVICE_POLICY_SERVICE);
        mDPM.lockNow();

    }
}

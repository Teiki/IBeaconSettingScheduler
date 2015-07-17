package fr.teiki.ibs.Module;

import android.content.Context;
import android.media.AudioManager;

import fr.teiki.ibs.BeaconService;
import fr.teiki.ibs.BeaconSettingsActivity;

/**
 * Created by antoinegaltier on 08/12/14.
 */
public class MySoundManager {

    private static int old_mode = -1;

    public static void change_notification_mode(Context ctx, boolean b, int mode){
        AudioManager amanager = (AudioManager)ctx.getSystemService(ctx.AUDIO_SERVICE);
        if (b) {
            if (amanager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                if (old_mode == -1) {
                    old_mode = amanager.getRingerMode();
                    if (mode>=0 && mode<BeaconSettingsActivity.NOTIFICATION_MODE.length)
                        amanager.setRingerMode(BeaconSettingsActivity.NOTIFICATION_MODE[mode]);
                }
            }
        }
        else{
            if (old_mode != -1) {
                amanager.setRingerMode(old_mode);
                old_mode = -1;
            }
        }
    }



}

package fr.teiki.estimoteibeacon.Module;

import android.content.Context;
import android.media.AudioManager;

/**
 * Created by antoinegaltier on 08/12/14.
 */
public class MySoundManager {

    private static int old_mode = -1;

    public static void sound_cut(Context ctx, boolean b){
        AudioManager amanager = (AudioManager)ctx.getSystemService(ctx.AUDIO_SERVICE);
        if (b) {
            if (amanager.getRingerMode() != AudioManager.RINGER_MODE_SILENT) {
                if (old_mode == -1)
                    old_mode = amanager.getRingerMode();
                amanager.setRingerMode(AudioManager.RINGER_MODE_SILENT);
            }
        }
        else{
            if (old_mode != -1)
                if (amanager.getRingerMode() == AudioManager.RINGER_MODE_SILENT)
                    amanager.setRingerMode(old_mode);
        }
    }

}

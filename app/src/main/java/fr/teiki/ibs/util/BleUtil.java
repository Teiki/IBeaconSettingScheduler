package fr.teiki.ibs.util;

import android.annotation.SuppressLint;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.pm.PackageManager;

import com.radiusnetworks.ibeacon.IBeacon;


public class BleUtil {


    /** check if BLE Supported device */
    public static boolean isBLESupported(Context context) {
        return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH_LE);
    }

    /** get BluetoothManager */
    @SuppressLint("ServiceCast")
    public static BluetoothManager getManager(Context context) {
        return (BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE);
    }

    public enum Proximity {
        UNKNOWN,
        IMMEDIATE,
        NEAR,
        FAR;

    }

    public static double computeAccuracy(IBeacon beacon) {
        if(beacon.getRssi() == 0) {
            return -1.0D;
        } else {
            double ratio = (double)beacon.getRssi() / (double)beacon.getTxPower();
            double rssiCorrection = 0.96D + Math.pow((double)Math.abs(beacon.getRssi()), 3.0D) % 10.0D / 150.0D;
            return ratio <= 1.0D?Math.pow(ratio, 9.98D) * rssiCorrection:(0.103D + 0.89978D * Math.pow(ratio, 7.71D)) * rssiCorrection;
        }
    }

    public static Proximity proximityFromAccuracy(double accuracy) {
        return accuracy < 0.0D?Proximity.UNKNOWN:(accuracy < 0.5D?Proximity.IMMEDIATE:(accuracy <= 3.0D?Proximity.NEAR:Proximity.FAR));
    }

    public static Proximity computeProximity(IBeacon beacon) {
        return proximityFromAccuracy(computeAccuracy(beacon));
    }
}

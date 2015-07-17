package fr.teiki.ibs;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;


import java.util.ArrayList;

import fr.teiki.ibs.util.BleUtil;
import fr.teiki.ibs.util.ScannedDevice;

/**
 * Created by antoinegaltier on 07/12/14.
 */
public class MyiBeaconAdapter extends BaseAdapter {

    private ArrayList<ScannedDevice> devices;
    private LayoutInflater inflater;


    public MyiBeaconAdapter(Context context, ArrayList<ScannedDevice> devices) {
        this.inflater = LayoutInflater.from(context);
        this.devices = devices;
    }


    public void addDeconnectedBeacons(Context ctx){
        for (String adress : MyPreferenceManager.getListSavedBeacon(ctx)){
            boolean is_online = false;
            for (ScannedDevice sd : devices){
                if (sd.getDevice().getAddress().equals(adress))
                    is_online = true;
            }
            if (!is_online) {
                ScannedDevice device = new ScannedDevice(adress);
                devices.add(device);
            }
        }
    }



    //    public void replaceWith(Context ctx, Collection<IBeacon> newBeacons) {
//        this.beacons.clear();
//        this.beacons.addAll(newBeacons);
//        addDeconnectedBeacons(ctx);
//        notifyDataSetChanged();
//    }

    @Override
    public int getCount() {
        return devices.size();
    }

    @Override
    public ScannedDevice getItem(int position) {
        return devices.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View view, ViewGroup parent) {
        view = inflateIfRequired(view, position, parent);
        bind(getItem(position), view);
        return view;
    }

    private void bind(ScannedDevice device, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (device.getIBeacon() != null) {
            holder.macTextView.setText(String.format("Name: %s (%.2fm)", device.getDisplayName(), MyPreferenceManager.getDistanceAverage()));
            holder.majorTextView.setText("Major: " + device.getIBeacon().getMajor());
            holder.minorTextView.setText("Minor: " + device.getIBeacon().getMinor());
            holder.measuredPowerTextView.setText("TxPower: " + device.getIBeacon().getTxPower());
            holder.rssiTextView.setText("RSSI: " + device.getIBeacon().getRssi());
            holder.zoneTextView.setText("Zone: " + BleUtil.computeProximity(device.getIBeacon()).name());
        }
        else {
            holder.macTextView.setText(String.format("Name: %s (%s)", device.getDisplayName(), "This is not an IBeacon"));
            holder.majorTextView.setText("");
            holder.minorTextView.setText("");
            holder.measuredPowerTextView.setText("");
            holder.rssiTextView.setText("");
            holder.zoneTextView.setText("");
        }
    }

    private View inflateIfRequired(View view, int position, ViewGroup parent) {
        if (view == null) {
            view = inflater.inflate(R.layout.device_item, null);
            view.setTag(new ViewHolder(view));
        }
        return view;
    }

    private static class ViewHolder {
        final TextView macTextView;
        final TextView majorTextView;
        final TextView minorTextView;
        final TextView measuredPowerTextView;
        final TextView rssiTextView;
        final TextView zoneTextView;

        ViewHolder(View view) {
            macTextView = (TextView) view.findViewWithTag("mac");
            majorTextView = (TextView) view.findViewWithTag("major");
            minorTextView = (TextView) view.findViewWithTag("minor");
            measuredPowerTextView = (TextView) view.findViewWithTag("mpower");
            rssiTextView = (TextView) view.findViewWithTag("rssi");
            zoneTextView = (TextView) view.findViewWithTag("zone");
        }
    }
}

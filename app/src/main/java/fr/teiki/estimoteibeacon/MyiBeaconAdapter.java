package fr.teiki.estimoteibeacon;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.estimote.sdk.Beacon;
import com.estimote.sdk.Utils;

import java.util.ArrayList;
import java.util.Collection;

/**
 * Created by antoinegaltier on 07/12/14.
 */
public class MyiBeaconAdapter extends BaseAdapter {

    private ArrayList<Beacon> beacons;
    private LayoutInflater inflater;

    private static final String DEFAULT_UUID = "aaaaaaaa-aaaa-aaaa-aaaa-aaaaaaaaaaaa";

    public MyiBeaconAdapter(Context context) {
        this.inflater = LayoutInflater.from(context);
        this.beacons = new ArrayList<Beacon>();
        addDeconnectedBeacons(context);
    }

    private void addDeconnectedBeacons(Context ctx){
        for (String uuid : MyPreferenceManager.getListSavedBeacon(ctx)){
            boolean is_online = false;
            for (Beacon b : beacons){
                if (b.getMacAddress().equals(uuid))
                    is_online = true;
            }
            if (!is_online) {
                Beacon newBeacon = new Beacon(DEFAULT_UUID, "", uuid, 0, 0, 0, 0);
                beacons.add(newBeacon);
            }
        }
    }

    public void replaceWith(Context ctx, Collection<Beacon> newBeacons) {
        this.beacons.clear();
        this.beacons.addAll(newBeacons);
        addDeconnectedBeacons(ctx);
        notifyDataSetChanged();
    }

    @Override
    public int getCount() {
        return beacons.size();
    }

    @Override
    public Beacon getItem(int position) {
        return beacons.get(position);
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

    private void bind(Beacon beacon, View view) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (beacon.getProximityUUID().equals(DEFAULT_UUID)){
            holder.macTextView.setText(String.format("MAC: %s (%s)", beacon.getMacAddress(), "Déconnecté"));
        }
        else {
            holder.macTextView.setText(String.format("MAC: %s (%.2fm)", beacon.getMacAddress(), MyPreferenceManager.getDistanceAverage()));
            holder.majorTextView.setText("Major: " + beacon.getMajor());
            holder.minorTextView.setText("Minor: " + beacon.getMinor());
            holder.measuredPowerTextView.setText("MPower: " + beacon.getMeasuredPower());
            holder.rssiTextView.setText("RSSI: " + beacon.getRssi());
            holder.zoneTextView.setText("Zone: " + Utils.computeProximity(beacon).name());
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

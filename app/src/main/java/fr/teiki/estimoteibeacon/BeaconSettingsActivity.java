package fr.teiki.estimoteibeacon;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;

import com.estimote.sdk.Beacon;

import java.util.Set;


public class BeaconSettingsActivity extends ActionBarActivity implements CompoundButton.OnCheckedChangeListener{

    public static final String KEY_DONT_DISTURB = "dont_disturb";
    public static final String KEY_IBEACON = "beacon_object";
    public static final String ACTION_NAME = "action_name";


    private Beacon beacon;
    private String beacon_adress;

    private Switch dont_disturb_switch;
    private Button dont_disturb_distance_button;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        beacon = getIntent().getParcelableExtra(KEY_IBEACON);
        if (beacon == null)
            finish();
        beacon_adress = beacon.getMacAddress();

        dont_disturb_switch = (Switch) findViewById(R.id.switch_dont_disturb);

        dont_disturb_distance_button = (Button) findViewById(R.id.distance_dont_disturb);
        dont_disturb_distance_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BeaconSettingsActivity.this, DistanceBeaconActivity.class);
                intent.putExtra(BeaconSettingsActivity.KEY_IBEACON, beacon);
                intent.putExtra(BeaconSettingsActivity.ACTION_NAME, KEY_DONT_DISTURB);
                startActivity(intent);
            }
        });

        initializeSettings();
    }

    private void initializeSettings() {
        Set<String> set = MyPreferenceManager.getAssociatedActionSet(this,beacon);
        for (String action : set){
            if (action.equals(KEY_DONT_DISTURB)){
                dont_disturb_switch.setChecked(true);
                dont_disturb_distance_button.setVisibility(View.VISIBLE);
            }
        }

        dont_disturb_switch.setOnCheckedChangeListener(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.equals(dont_disturb_switch)){

            if (isChecked) {
                dont_disturb_distance_button.setVisibility(View.VISIBLE);
                MyPreferenceManager.addAction(this,beacon,KEY_DONT_DISTURB);

            }
            else {
                dont_disturb_distance_button.setVisibility(View.GONE);
                MyPreferenceManager.removeAction(this, beacon, KEY_DONT_DISTURB);
            }
        }
    }
}

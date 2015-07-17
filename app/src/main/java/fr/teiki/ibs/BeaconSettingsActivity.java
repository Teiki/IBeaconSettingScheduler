package fr.teiki.ibs;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.Switch;


import java.util.Set;

import fr.teiki.ibs.Module.MyApplicationList;


public class BeaconSettingsActivity extends ActionBarActivity implements CompoundButton.OnCheckedChangeListener, RadioGroup.OnCheckedChangeListener, Button.OnClickListener{

    public static final String KEY_SOUND_MODE = "sound_mode";
    public static final String KEY_SOUND_NORMAL = "normal_sound";
    public static final String KEY_SOUND_VIBRATE = "vibrate_sound";
    public static final String KEY_SOUND_PRIORITY= "priority_sound";
    public static final String KEY_LOCKSCREEN= "lockscreen";
    public static final String KEY_LAUNCH_APP = "launch_app";
    public static final String KEY_IBEACON = "beacon_object";
    public static final String KEY_WIFI_STATE = "wifi_state";
    public static final String ACTION_NAME = "action_name";
    public static final String APP_NAME = "app_name";
    public static final String KEY_DISTANCE = "distance";
    public static final int[] NOTIFICATION_MODE = {AudioManager.RINGER_MODE_NORMAL,AudioManager.RINGER_MODE_VIBRATE,AudioManager.RINGER_MODE_SILENT};
    public static final int DEFAULT_SOUND_MODE = 2;
    public static final String ACTIVATION_MODE = "ACTIVATION_MODE";
    public static final String REGION = "Région";
    public static final String PERIMETRE = "Périmètre";
    public static final String[] REGIONS = {"IMMEDIATE","NEAR","FAR"};
    public static final String[] PERIMETRES = {"PERIMETRE_1","PERIMETRE_2","PERIMETRE_3"};
    public static final RadioGroup[] NOTIFICATION_RADIO_GROUPS = new RadioGroup[3];
    public static final CheckBox[] REGIONS_CHECKBOX = new CheckBox[3];


    //private Beacon beacon;
    private String macaddr_beacon;

    private Switch notification_mode_switch;
//    private RadioGroup group_notification;
//    private Button notification_mode_distance_button;
    private Switch launch_app_switch;
    private Button launch_app_distance_button;
    private Switch wifi_state_switch;
    private Button wifi_state_distance_button;
    private RadioGroup group_wifi_state;

    private RadioGroup notification_activation_mode;
    private RadioGroup immediate_sound;
    private RadioGroup near_sound;
    private RadioGroup far_sound;
    private CheckBox immediate;
    private CheckBox near;
    private CheckBox far;
    private Button perimeter_1;
    private Button perimeter_2;
    private Button perimeter_3;
    private Button save_beacon_name;
    private EditText beacon_name;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        //beacon = getIntent().getParcelableExtra(KEY_IBEACON);

        macaddr_beacon = getIntent().getStringExtra(KEY_IBEACON);
        if (macaddr_beacon == null)
            finish();

        notification_mode_switch = (Switch) findViewById(R.id.switch_notification);
        launch_app_switch = (Switch) findViewById(R.id.switch_launch_app);
        launch_app_distance_button = (Button) findViewById(R.id.distance_launch_app);
        wifi_state_switch = (Switch) findViewById(R.id.switch_wifi_state);
        wifi_state_distance_button = (Button) findViewById(R.id.distance_wifi_state);
        group_wifi_state = (RadioGroup) findViewById(R.id.radiogroup_wifi);

        notification_activation_mode = (RadioGroup) findViewById(R.id.activation_mode);
        immediate_sound = (RadioGroup) findViewById(R.id.immediate_sound);
        near_sound = (RadioGroup) findViewById(R.id.near_sound);
        far_sound = (RadioGroup) findViewById(R.id.far_sound);
        immediate = (CheckBox) findViewById(R.id.immediate);
        near = (CheckBox) findViewById(R.id.near);
        far = (CheckBox) findViewById(R.id.far);
        perimeter_1 = (Button) findViewById(R.id.perimeter_1);
        perimeter_2 = (Button) findViewById(R.id.perimeter_2);
        perimeter_3 = (Button) findViewById(R.id.perimeter_3);
        beacon_name = (EditText) findViewById(R.id.beacon_name);
        save_beacon_name = (Button) findViewById(R.id.save_beacon_name);

        NOTIFICATION_RADIO_GROUPS[0] = immediate_sound;
        NOTIFICATION_RADIO_GROUPS[1] = near_sound;
        NOTIFICATION_RADIO_GROUPS[2] = far_sound;
        REGIONS_CHECKBOX[0] = immediate;
        REGIONS_CHECKBOX[1] = near;
        REGIONS_CHECKBOX[2] = far;


        initializeSettings();

        notification_activation_mode.setOnCheckedChangeListener(this);
        immediate_sound.setOnCheckedChangeListener(this);
        near_sound.setOnCheckedChangeListener(this);
        far_sound.setOnCheckedChangeListener(this);

        immediate.setOnCheckedChangeListener(this);
        near.setOnCheckedChangeListener(this);
        far.setOnCheckedChangeListener(this);


        launch_app_distance_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BeaconSettingsActivity.this, DistanceBeaconActivity.class);
                intent.putExtra(BeaconSettingsActivity.KEY_IBEACON, macaddr_beacon);
                intent.putExtra(BeaconSettingsActivity.ACTION_NAME, KEY_LAUNCH_APP);
                startActivity(intent);
            }
        });

        wifi_state_distance_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BeaconSettingsActivity.this, DistanceBeaconActivity.class);
                intent.putExtra(BeaconSettingsActivity.KEY_IBEACON, macaddr_beacon);
                intent.putExtra(BeaconSettingsActivity.ACTION_NAME, KEY_WIFI_STATE);
                startActivity(intent);
            }
        });

    }

    private void initializeSettings() {
        Set<String> set = MyPreferenceManager.getAssociatedActionSet(this,macaddr_beacon);
        if (set != null) {
            for (String action : set) {
                if (action.equals(KEY_SOUND_MODE)) {
                    notification_mode_switch.setChecked(true);
                    findViewById(R.id.notification_options).setVisibility(View.VISIBLE);
                    displayNotificationRules();
                } else if (action.equals(KEY_LAUNCH_APP)) {
                    launch_app_switch.setChecked(true);
                    launch_app_distance_button.setVisibility(View.VISIBLE);
                    displayAppToLaunch();
                } else if (action.equals(KEY_WIFI_STATE)) {
                    wifi_state_switch.setChecked(true);
                    wifi_state_distance_button.setVisibility(View.VISIBLE);
                    group_wifi_state.setVisibility(View.VISIBLE);
                    displayWifiStateChoosen();
                }
            }
        }

        launch_app_switch.setOnCheckedChangeListener(this);
        notification_mode_switch.setOnCheckedChangeListener(this);
        wifi_state_switch.setOnCheckedChangeListener(this);
        group_wifi_state.setOnCheckedChangeListener(this);

        beacon_name.setText(MyPreferenceManager.getBeaconName(getApplicationContext(), macaddr_beacon));
        save_beacon_name.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MyPreferenceManager.setBeaconName(getApplicationContext(),macaddr_beacon,beacon_name.getText().toString());
            }
        });
    }

    private void displayAppToLaunch(){
        if (MyPreferenceManager.getAssociatedActionSet(getApplicationContext(),macaddr_beacon).contains(KEY_LAUNCH_APP)){
            String packagename = MyPreferenceManager.getAssociatedActionParam(getApplicationContext(), macaddr_beacon, KEY_LAUNCH_APP);
            launch_app_switch.setText(launch_app_switch.getText()+" : "+packagename);
        }
        else {
            launch_app_switch.setText(R.string.launch_app);
        }
    }

    private void displayWifiStateChoosen(){
        if (MyPreferenceManager.getAssociatedActionSet(getApplicationContext(),macaddr_beacon).contains(KEY_WIFI_STATE)) {
            int state = MyPreferenceManager.getAssociatedActionState(getApplicationContext(), macaddr_beacon, KEY_WIFI_STATE);
            if (state != -1)
                group_wifi_state.check(state);
        }
    }

    private int getNotificationIndexFromTab(int notification_mode){
        int i = 0;
        while(notification_mode != NOTIFICATION_MODE[i] && i < NOTIFICATION_MODE.length)
            i++;
        if (i >= NOTIFICATION_MODE.length)
            return -1;
        else
            return i;
    }

    private void displayNotificationRules(){
        if (MyPreferenceManager.getAssociatedActionSet(getApplicationContext(),macaddr_beacon).contains(KEY_SOUND_MODE)) {
            String activation_mode = MyPreferenceManager.getAssociatedActivationMode(getApplicationContext(),macaddr_beacon,KEY_SOUND_MODE);
            if (activation_mode.equals(REGION)){
                notification_activation_mode.check(R.id.region_mode);
            }
            else if (activation_mode.equals(PERIMETRE)){
                notification_activation_mode.check(R.id.perimeter_mode);
            }
            for (int i = 0; i < REGIONS.length; i ++) {
                int notification_mode = MyPreferenceManager.getAssociatedNotificationParam(getApplicationContext(), macaddr_beacon, KEY_SOUND_MODE, REGIONS[i]);
                if (notification_mode != -1) {
                    REGIONS_CHECKBOX[i].setChecked(true);
                    int index = getNotificationIndexFromTab(notification_mode);
                    if (index != -1)
                        NOTIFICATION_RADIO_GROUPS[i].check(NOTIFICATION_RADIO_GROUPS[i].getChildAt(index).getId());
                }
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.equals(notification_mode_switch)){

            if (isChecked) {
                findViewById(R.id.notification_options).setVisibility(View.VISIBLE);
                MyPreferenceManager.addAction(this, macaddr_beacon, KEY_SOUND_MODE, NOTIFICATION_MODE[DEFAULT_SOUND_MODE]);

            }
            else {
                findViewById(R.id.notification_options).setVisibility(View.GONE);
                MyPreferenceManager.removeAction(this, macaddr_beacon, KEY_SOUND_MODE);
            }
        }
        else if (buttonView.equals(launch_app_switch)){

            if (isChecked) {
                Intent intent = new Intent(BeaconSettingsActivity.this, MyApplicationList.class);
                startActivityForResult(intent,0);
            }
            else {
                launch_app_distance_button.setVisibility(View.GONE);
                MyPreferenceManager.removeAction(this, macaddr_beacon, KEY_LAUNCH_APP);
                displayAppToLaunch();
            }
        }
        else if (buttonView.equals(wifi_state_switch)){

            if (isChecked) {
                group_wifi_state.setVisibility(View.VISIBLE);
                wifi_state_distance_button.setVisibility(View.VISIBLE);
                MyPreferenceManager.addAction(this, macaddr_beacon, KEY_WIFI_STATE);
            }
            else {
                wifi_state_distance_button.setVisibility(View.GONE);
                group_wifi_state.setVisibility(View.GONE);
                MyPreferenceManager.removeAction(this, macaddr_beacon, KEY_WIFI_STATE);
            }
        }
        else if (buttonView.equals(immediate)){
            if (isChecked){
                immediate_sound.check(0);
            }
            else {
                MyPreferenceManager.removeAction(getApplicationContext(),macaddr_beacon,REGIONS[0]);
            }
        }
        else if (buttonView.equals(near)){
            if (isChecked){
                near_sound.check(0);
            }
            else {
                MyPreferenceManager.removeAction(getApplicationContext(),macaddr_beacon,REGIONS[1]);
            }
        }
        else if (buttonView.equals(far)){
            if (isChecked){
                far_sound.check(0);
            }
            else {
                MyPreferenceManager.removeAction(getApplicationContext(),macaddr_beacon,REGIONS[2]);
            }
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK && requestCode == 0){
            launch_app_distance_button.setVisibility(View.VISIBLE);
            MyPreferenceManager.addAction(this,macaddr_beacon,KEY_LAUNCH_APP,data.getStringExtra(APP_NAME));
            displayAppToLaunch();
        }

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group.equals(group_wifi_state)){
            MyPreferenceManager.addAction(getApplicationContext(), macaddr_beacon, KEY_WIFI_STATE, checkedId);
        }
        else if (group.equals(notification_activation_mode)){
            if (checkedId == R.id.region_mode){
                far.setText(R.string.far);
                near.setText(R.string.near);
                immediate.setText(R.string.immediate);

                perimeter_1.setVisibility(View.GONE);
                perimeter_2.setVisibility(View.GONE);
                perimeter_3.setVisibility(View.GONE);

                MyPreferenceManager.updateActivationMode(getApplicationContext(),macaddr_beacon,KEY_SOUND_MODE,REGION);
                //displayNotificationRules();
            }
            else if (checkedId == R.id.perimeter_mode){
                far.setText("");
                near.setText("");
                immediate.setText("");

                perimeter_1.setVisibility(View.VISIBLE);
                perimeter_2.setVisibility(View.VISIBLE);
                perimeter_3.setVisibility(View.VISIBLE);

                MyPreferenceManager.updateActivationMode(getApplicationContext(),macaddr_beacon,KEY_SOUND_MODE,PERIMETRE);
                //displayNotificationRules();
            }
        }
        else if (group.equals(immediate_sound)){

            MyPreferenceManager.updateZoneAction(getApplicationContext(),macaddr_beacon,KEY_SOUND_MODE,REGIONS[0],group.indexOfChild(findViewById(checkedId)));
        }
        else if (group.equals(near_sound)){

            MyPreferenceManager.updateZoneAction(getApplicationContext(),macaddr_beacon,KEY_SOUND_MODE,REGIONS[1],group.indexOfChild(findViewById(checkedId)));
        }
        else if (group.equals(far_sound)){

            MyPreferenceManager.updateZoneAction(getApplicationContext(),macaddr_beacon,KEY_SOUND_MODE,REGIONS[2],group.indexOfChild(findViewById(checkedId)));
        }
    }

//    private void getNotificationParam(RadioGroup group, int id){
//        for (int i; i <= group.child)
//    }


    @Override
    public void onClick(View buttonView) {
        if (buttonView.equals(immediate)){

        }
    }
}

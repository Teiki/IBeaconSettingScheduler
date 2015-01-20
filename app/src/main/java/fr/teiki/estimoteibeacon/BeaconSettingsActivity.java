package fr.teiki.estimoteibeacon;

import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.estimote.sdk.Beacon;

import java.util.Set;

import fr.teiki.estimoteibeacon.Module.MyApplicationList;


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
    public static final int[] PERIMETRES = {"PERIMETRE_1","PERIMETRE_2","PERIMETRE_3"};


    private Beacon beacon;

    private Switch notification_mode_switch;
//    private RadioGroup group_notification;
//    private Button notification_mode_distance_button;
    private Switch launch_app_switch;
    private Button launch_app_distance_button;
    private Switch wifi_state_switch;
    private Button wifi_state_distance_button;
    private RadioGroup group_wifi_state;

    private RadioGroup notification_activation_mode;
    private RadioGroup near_sound;
    private RadioGroup intermediate_sound;
    private RadioGroup far_sound;
    private CheckBox near;
    private CheckBox intermediate;
    private CheckBox far;
    private Button perimeter_1;
    private Button perimeter_2;
    private Button perimeter_3;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_beacon_settings);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        beacon = getIntent().getParcelableExtra(KEY_IBEACON);
        if (beacon == null)
            finish();

        notification_mode_switch = (Switch) findViewById(R.id.switch_notification);
        launch_app_switch = (Switch) findViewById(R.id.switch_launch_app);
        launch_app_distance_button = (Button) findViewById(R.id.distance_launch_app);
        wifi_state_switch = (Switch) findViewById(R.id.switch_wifi_state);
        wifi_state_distance_button = (Button) findViewById(R.id.distance_wifi_state);
        group_wifi_state = (RadioGroup) findViewById(R.id.radiogroup_wifi);

        notification_activation_mode = (RadioGroup) findViewById(R.id.activation_mode);
        near_sound = (RadioGroup) findViewById(R.id.near_sound);
        intermediate_sound = (RadioGroup) findViewById(R.id.intermediate_sound);
        far_sound = (RadioGroup) findViewById(R.id.far_sound);
        near = (CheckBox) findViewById(R.id.near);
        intermediate = (CheckBox) findViewById(R.id.intermediate);
        far = (CheckBox) findViewById(R.id.far);
        perimeter_1 = (Button) findViewById(R.id.perimeter_1);
        perimeter_2 = (Button) findViewById(R.id.perimeter_2);
        perimeter_3 = (Button) findViewById(R.id.perimeter_3);

        notification_activation_mode.setOnCheckedChangeListener(this);



        launch_app_distance_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BeaconSettingsActivity.this, DistanceBeaconActivity.class);
                intent.putExtra(BeaconSettingsActivity.KEY_IBEACON, beacon);
                intent.putExtra(BeaconSettingsActivity.ACTION_NAME, KEY_LAUNCH_APP);
                startActivity(intent);
            }
        });

        wifi_state_distance_button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(BeaconSettingsActivity.this, DistanceBeaconActivity.class);
                intent.putExtra(BeaconSettingsActivity.KEY_IBEACON, beacon);
                intent.putExtra(BeaconSettingsActivity.ACTION_NAME, KEY_WIFI_STATE);
                startActivity(intent);
            }
        });

        initializeSettings();
    }

    private void initializeSettings() {
        Set<String> set = MyPreferenceManager.getAssociatedActionSet(this,beacon);
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
        //group_notification.setOnCheckedChangeListener(this);
    }

    private void displayAppToLaunch(){
        if (MyPreferenceManager.getAssociatedActionSet(getApplicationContext(),beacon).contains(KEY_LAUNCH_APP)){
            String packagename = MyPreferenceManager.getAssociatedActionParam(getApplicationContext(), beacon, KEY_LAUNCH_APP);
            launch_app_switch.setText(launch_app_switch.getText()+" : "+packagename);
        }
        else {
            launch_app_switch.setText(R.string.launch_app);
        }
    }

    private void displayWifiStateChoosen(){
        if (MyPreferenceManager.getAssociatedActionSet(getApplicationContext(),beacon).contains(KEY_WIFI_STATE)) {
            int state = MyPreferenceManager.getAssociatedActionState(getApplicationContext(), beacon, KEY_WIFI_STATE);
            if (state != -1)
                group_wifi_state.check(state);
        }
    }

    private void displayNotificationRules(){
        if (MyPreferenceManager.getAssociatedActionSet(getApplicationContext(),beacon).contains(KEY_SOUND_MODE)) {
            String activation_mode = MyPreferenceManager.getAssociatedActivationMode(getApplicationContext(),beacon,KEY_SOUND_MODE);
            if (activation_mode.equals(REGION)){
                notification_activation_mode.check(R.id.region_mode);
                int notification_mode = MyPreferenceManager.getAssociatedNotificationParam(getApplicationContext(),beacon,KEY_SOUND_MODE,REGIONS[0]);

            }
            else {
                notification_activation_mode.check(R.id.perimeter_mode);
            }



            int state = MyPreferenceManager.getAssociatedActionState(getApplicationContext(), beacon, KEY_SOUND_MODE);
            if (state == NOTIFICATION_MODE[0]) {
                normal_sound.setChecked(true);
                findViewById(R.id.normal_buttons).setVisibility(View.VISIBLE);
                ((TextView)findViewById(R.id.normal_sound_activation)).setText(
                        MyPreferenceManager.getAssociatedActivationMode(getApplicationContext(),beacon,KEY_SOUND_MODE) +
                        " : " +
                        MyPreferenceManager.getAssociatedActivationParam(getApplicationContext(), beacon, KEY_SOUND_MODE)); //Utiliser des clés diffèrentes selon le mode de notif
            }
            else if (state == NOTIFICATION_MODE[1]) {
                vibrate_sound.setChecked(true);
                findViewById(R.id.vibrate_buttons).setVisibility(View.VISIBLE);
            }
            else if (state == NOTIFICATION_MODE[2]) {
                priority_sound.setChecked(true);
                findViewById(R.id.priority_buttons).setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        if (buttonView.equals(notification_mode_switch)){

            if (isChecked) {
                findViewById(R.id.notification_options).setVisibility(View.VISIBLE);
                MyPreferenceManager.addAction(this, beacon, KEY_SOUND_MODE, NOTIFICATION_MODE[DEFAULT_SOUND_MODE]);

            }
            else {
                findViewById(R.id.notification_options).setVisibility(View.VISIBLE);
                MyPreferenceManager.removeAction(this, beacon, KEY_SOUND_MODE);
            }
        }
        else if (buttonView.equals(launch_app_switch)){

            if (isChecked) {
                Intent intent = new Intent(BeaconSettingsActivity.this, MyApplicationList.class);
                startActivityForResult(intent,0);
            }
            else {
                launch_app_distance_button.setVisibility(View.GONE);
                MyPreferenceManager.removeAction(this, beacon, KEY_LAUNCH_APP);
                displayAppToLaunch();
            }
        }
        else if (buttonView.equals(wifi_state_switch)){

            if (isChecked) {
                group_wifi_state.setVisibility(View.VISIBLE);
                wifi_state_distance_button.setVisibility(View.VISIBLE);
                MyPreferenceManager.addAction(this, beacon, KEY_WIFI_STATE);
            }
            else {
                wifi_state_distance_button.setVisibility(View.GONE);
                group_wifi_state.setVisibility(View.GONE);
                MyPreferenceManager.removeAction(this, beacon, KEY_WIFI_STATE);
            }
        }
        else if (buttonView.equals(normal_sound)){
            if (isChecked) {
                findViewById(R.id.normal_buttons).setVisibility(View.VISIBLE);
                MyPreferenceManager.addAction(this, beacon, KEY_SOUND_MODE, NOTIFICATION_MODE[0]);
            }
            else {
                findViewById(R.id.normal_buttons).setVisibility(View.GONE);
                MyPreferenceManager.removeAction(this, beacon, KEY_SOUND_MODE + NOTIFICATION_MODE[0]);
            }
        }
        else if (buttonView.equals(vibrate_sound)){
            if (isChecked) {
                findViewById(R.id.vibrate_buttons).setVisibility(View.VISIBLE);
                MyPreferenceManager.addAction(this, beacon, KEY_SOUND_MODE, NOTIFICATION_MODE[1]);
            }
            else {
                findViewById(R.id.vibrate_buttons).setVisibility(View.GONE);
                MyPreferenceManager.removeAction(this, beacon, KEY_SOUND_MODE + NOTIFICATION_MODE[1]);
            }
        }
        else if (buttonView.equals(priority_sound)){
            if (isChecked) {
                findViewById(R.id.priority_buttons).setVisibility(View.VISIBLE);
                MyPreferenceManager.addAction(this, beacon, KEY_SOUND_MODE, NOTIFICATION_MODE[2]);
            }
            else {
                findViewById(R.id.priority_buttons).setVisibility(View.GONE);
                MyPreferenceManager.removeAction(this, beacon, KEY_SOUND_MODE + NOTIFICATION_MODE[2]);
            }
        }
//        else if (buttonView.equals(lockscreen_switch)){
//
//            if (isChecked) {
//                Intent intent = new   Intent(DevicePolicyManager.ACTION_ADD_DEVICE_ADMIN);
//                ComponentName mDeviceAdminSample = new ComponentName(this, MyAdmin.class);
//                intent.putExtra(DevicePolicyManager.EXTRA_DEVICE_ADMIN, mDeviceAdminSample);
//                startActivity(intent);
//                lockscreen_distance_button.setVisibility(View.VISIBLE);
//                MyPreferenceManager.addAction(this, beacon, KEY_LOCKSCREEN);
//            }
//            else {
//                lockscreen_distance_button.setVisibility(View.GONE);
//                MyPreferenceManager.removeAction(this, beacon, KEY_LOCKSCREEN);
//            }
//        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if(resultCode == RESULT_OK && requestCode == 0){
            launch_app_distance_button.setVisibility(View.VISIBLE);
            MyPreferenceManager.addAction(this,beacon,KEY_LAUNCH_APP,data.getStringExtra(APP_NAME));
            displayAppToLaunch();
        }

    }

    @Override
    public void onCheckedChanged(RadioGroup group, int checkedId) {
        if (group.equals(group_wifi_state)){
            MyPreferenceManager.addAction(getApplicationContext(), beacon, KEY_WIFI_STATE, checkedId);
        }
        else if (group.equals(notification_activation_mode)){
            if (checkedId == 0){
                far.setText(R.string.far);
                intermediate.setText(R.string.intermediate);
                near.setText(R.string.near);

                perimeter_1.setVisibility(View.INVISIBLE);
                perimeter_2.setVisibility(View.INVISIBLE);
                perimeter_3.setVisibility(View.INVISIBLE);
            }
            else {
                far.setText("");
                intermediate.setText("");
                near.setText("");

                perimeter_1.setVisibility(View.VISIBLE);
                perimeter_2.setVisibility(View.VISIBLE);
                perimeter_3.setVisibility(View.VISIBLE);
            }
        }
    }


    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.normal_region){
            Intent intent = new Intent(BeaconSettingsActivity.this, DistanceBeaconActivity.class);
                intent.putExtra(BeaconSettingsActivity.KEY_IBEACON, beacon);
                intent.putExtra(BeaconSettingsActivity.ACTION_NAME, KEY_SOUND_MODE);
                intent.putExtra(BeaconSettingsActivity.KEY_SOUND_MODE, NOTIFICATION_MODE[0]);
                startActivity(intent);
        }
        else if (v.getId() == R.id.vibrate_region){
            Intent intent = new Intent(BeaconSettingsActivity.this, DistanceBeaconActivity.class);
            intent.putExtra(BeaconSettingsActivity.KEY_IBEACON, beacon);
            intent.putExtra(BeaconSettingsActivity.ACTION_NAME, KEY_SOUND_MODE);
            intent.putExtra(BeaconSettingsActivity.KEY_SOUND_MODE, NOTIFICATION_MODE[1]);
            startActivity(intent);
        }
        else if (v.getId() == R.id.priority_region){
            Intent intent = new Intent(BeaconSettingsActivity.this, DistanceBeaconActivity.class);
            intent.putExtra(BeaconSettingsActivity.KEY_IBEACON, beacon);
            intent.putExtra(BeaconSettingsActivity.ACTION_NAME, KEY_SOUND_MODE);
            intent.putExtra(BeaconSettingsActivity.KEY_SOUND_MODE, NOTIFICATION_MODE[2]);
            startActivity(intent);
        }
    }
}

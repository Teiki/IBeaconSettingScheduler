package fr.teiki.ibs.Module;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import fr.teiki.ibs.BeaconSettingsActivity;
import fr.teiki.ibs.R;

public class MyApplicationList extends ActionBarActivity {

    private ListView listview;
    private ApplicationListAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_my_application_list);

        listview = (ListView) findViewById(R.id.list);

        setApplicationList(getApplicationList());

        listview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent returnIntent = new Intent();
                returnIntent.putExtra(BeaconSettingsActivity.APP_NAME,adapter.getPackageName(position));
                setResult(RESULT_OK,returnIntent);
                finish();
            }
        });
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_my_application_list, menu);
        return true;
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


    private List<ApplicationInfo> getApplicationList(){
        final PackageManager pm = getPackageManager();

        List<ApplicationInfo> packages = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        List<ApplicationInfo> appInfoList = new ArrayList<ApplicationInfo>();
        for (ApplicationInfo info : packages) {
            if (pm.getLaunchIntentForPackage(info.packageName) != null) {
                appInfoList.add(info);
            }
        }

        Collections.sort(appInfoList, new ApplicationInfo.DisplayNameComparator(pm));
        return appInfoList;
    }

    private void setApplicationList(List<ApplicationInfo> list){
        adapter = new ApplicationListAdapter(this,list,getPackageManager());
        listview.setAdapter(adapter);
    }
}

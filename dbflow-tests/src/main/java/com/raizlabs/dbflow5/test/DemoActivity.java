package com.raizlabs.dbflow5.test;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.raizlabs.dbflow5.config.DatabaseConfig;
import com.raizlabs.dbflow5.config.FlowConfig;
import com.raizlabs.dbflow5.config.FlowManager;
import com.raizlabs.dbflow5.database.AndroidSQLiteOpenHelper;
import com.raizlabs.dbflow5.prepackaged.PrepackagedDB;


public class DemoActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_demo);

        FlowManager.init(new FlowConfig.Builder(getApplicationContext())
            .database(
                DatabaseConfig.builder(PrepackagedDB.class,
                    (dbFlowDatabase, databaseHelperListener) -> new AndroidSQLiteOpenHelper(getApplicationContext(), dbFlowDatabase, databaseHelperListener))
                    .databaseName("prepackaged")
                    .build())
            .build());
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_demo, menu);
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
}

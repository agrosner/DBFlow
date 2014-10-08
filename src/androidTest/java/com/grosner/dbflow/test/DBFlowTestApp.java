package com.grosner.dbflow.test;

import android.app.Application;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.config.FlowManager;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class DBFlowTestApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        DBConfiguration.Builder configurationBuilder
                = new DBConfiguration.Builder().databaseName("test.db").databaseVersion(1);
        FlowManager.getInstance().initialize(this, configurationBuilder.create());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        FlowManager.getInstance().destroy();
    }
}

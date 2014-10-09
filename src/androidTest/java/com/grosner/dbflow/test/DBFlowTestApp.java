package com.grosner.dbflow.test;

import android.app.Application;

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

        FlowManager.setContext(this);
        FlowManager.setMultiTable(true);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}

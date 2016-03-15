package com.raizlabs.android.dbflow.test;

import android.app.Application;

import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Description:
 */
public class DBFlowTestApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V);
        FlowManager.init(new FlowConfig.Builder(this).build());
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }
}

package com.raizlabs.android.dbflow

import android.app.Application

import com.raizlabs.android.dbflow.config.FlowConfig
import com.raizlabs.android.dbflow.config.FlowLog
import com.raizlabs.android.dbflow.config.FlowManager

/**
 * Description:
 */
class DBFlowTestApp : Application() {

    override fun onCreate() {
        super.onCreate()
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V)
        FlowManager.init(FlowConfig.Builder(this).build())
    }

    override fun onTerminate() {
        super.onTerminate()
    }
}

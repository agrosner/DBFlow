package com.raizlabs.android.dbflow.test;

import android.test.AndroidTestCase;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;

/**
 * Description:
 */
public abstract class FlowTestCase extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FlowManager.init(getContext());
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.I);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FlowManager.destroy();
    }
}

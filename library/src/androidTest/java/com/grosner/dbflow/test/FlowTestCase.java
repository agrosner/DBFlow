package com.grosner.dbflow.test;

import android.test.AndroidTestCase;
import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public abstract class FlowTestCase extends AndroidTestCase {

    protected abstract String getDBName();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FlowManager.setContext(getContext());
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.I);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FlowManager.destroy();
    }
}

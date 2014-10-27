package com.grosner.dbflow.test;

import android.test.AndroidTestCase;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.test.structure.TestModel1;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public abstract class FlowTestCase extends AndroidTestCase {

    protected FlowManager mManager;

    protected abstract String getDBName();

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FlowManager.setContext(getContext());
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.I);
        DBConfiguration.Builder configurationBuilder
                = new DBConfiguration.Builder().databaseName(getDBName()).databaseVersion(1)
                .addModelClasses(TestModel1.class);
        mManager = new FlowManager();
        modifyConfiguration(configurationBuilder);
        mManager.initialize(configurationBuilder.create());
    }

    protected abstract void modifyConfiguration(DBConfiguration.Builder builder);

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        getContext().deleteDatabase(mManager.getDbConfiguration().getDatabaseName());
        mManager.destroy();
    }
}

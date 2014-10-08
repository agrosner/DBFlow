package com.grosner.dbflow.test;

import android.test.AndroidTestCase;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.config.FlowManager;

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
        DBConfiguration.Builder configurationBuilder
                = new DBConfiguration.Builder().databaseName(getDBName()).databaseVersion(1);
        mManager = new FlowManager();
        modifyConfiguration(configurationBuilder);
        mManager.initialize(getContext(), configurationBuilder.create());
    }

    protected void modifyConfiguration(DBConfiguration.Builder builder) {

    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        getContext().deleteDatabase(mManager.getDbConfiguration().getDatabaseName());
        mManager.destroy();
    }
}

package com.grosner.dbflow.test.sql;

import android.test.AndroidTestCase;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.migration.AlterTableMigration;
import com.grosner.dbflow.test.FlowTestCase;
import com.grosner.dbflow.test.structure.TestModel1;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class MigrationTest extends AndroidTestCase {

    FlowManager mManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FlowManager.setContext(getContext());
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.I);
        DBConfiguration.Builder configurationBuilder
                = new DBConfiguration.Builder().databaseName("migration").databaseVersion(1)
                .addModelClasses(TestModel1.class);
        mManager = new FlowManager();
        mManager.initialize(configurationBuilder.create());
    }


    public void testMigration() {

        AlterTableMigration<TestModel1> alterTableMigration = new AlterTableMigration<TestModel1>(mManager, TestModel1.class, 2);
        alterTableMigration.renameFrom("TestModel");

        assertEquals();
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        getContext().deleteDatabase(mManager.getDbConfiguration().getDatabaseName());
        mManager.destroy();
    }
}

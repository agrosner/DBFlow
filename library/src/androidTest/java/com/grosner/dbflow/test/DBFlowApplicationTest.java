package com.grosner.dbflow.test;

import android.test.ApplicationTestCase;
import com.grosner.dbflow.test.list.ListDatabase;
import com.grosner.dbflow.test.sql.MigrationDatabase;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class DBFlowApplicationTest extends ApplicationTestCase<DBFlowTestApp> {

    public DBFlowApplicationTest() {
        super(DBFlowTestApp.class);
    }

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        createApplication();
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        getContext().deleteDatabase(TestDatabase.NAME);
        getContext().deleteDatabase(ListDatabase.NAME);
        getContext().deleteDatabase(MigrationDatabase.NAME);
    }
}

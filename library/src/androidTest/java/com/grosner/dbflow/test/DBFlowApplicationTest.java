package com.grosner.dbflow.test;

import android.test.ApplicationTestCase;

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
}

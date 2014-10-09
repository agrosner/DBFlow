package com.grosner.dbflow.test.flowmanager;

import android.test.AndroidTestCase;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.test.structure.TestModel1;
import com.grosner.dbflow.test.utils.AssertUtils;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class FlowManagerTest extends AndroidTestCase {

    private DBConfiguration mConfiguration;

    private FlowManager mManager;

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        DBConfiguration.Builder configurationBuilder
                = new DBConfiguration.Builder().databaseName("flowmanager").databaseVersion(1)
                .addModelClasses(TestModel1.class);
        mManager = new FlowManager();
        mManager.initialize(mConfiguration = configurationBuilder.create());
    }

    public void testManager() {
        assertEquals(mConfiguration, mManager.getDbConfiguration());
        assertNotNull(FlowManager.getTypeConverterForClass(String.class));
        mManager.destroy();

        assertNull(mManager.getDbConfiguration());
        assertNull(mManager.getStructure());
        assertNull(mManager.getHelper());
        assertFalse(mManager.isInitialized());
    }

}

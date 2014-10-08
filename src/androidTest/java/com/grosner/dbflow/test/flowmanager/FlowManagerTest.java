package com.grosner.dbflow.test.flowmanager;

import android.test.AndroidTestCase;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.config.FlowManager;
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
                = new DBConfiguration.Builder().databaseName("flowmanager.db").databaseVersion(1);
        mManager = new FlowManager();
        mManager.initialize(getContext(), mConfiguration = configurationBuilder.create());
    }

    public void testManager() {
        assertEquals(mConfiguration, mManager.getDbConfiguration());
        assertNotNull(mManager.getTypeConverterForClass(String.class));
        mManager.destroy();

        AssertUtils.assertThrowsException(IllegalStateException.class, new Runnable() {
            @Override
            public void run() {
                mManager.getContext();
            }
        });
        assertNull(mManager.getDbConfiguration());
        assertNull(mManager.getStructure());
        assertNull(mManager.getHelper());
        assertFalse(mManager.isInitialized());
    }

}

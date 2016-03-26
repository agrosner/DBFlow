package com.raizlabs.android.dbflow.test.config;

import android.os.Build;

import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.runtime.BaseTransactionManager;
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.OpenHelper;
import com.raizlabs.android.dbflow.test.BuildConfig;
import com.raizlabs.android.dbflow.test.ShadowContentResolver2;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.customhelper.CustomOpenHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricGradleTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Description: Tests to ensure DBFlow is set up properly.
 */
@RunWith(RobolectricGradleTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP, shadows = {ShadowContentResolver2.class})
public class ConfigIntegrationTest {

    private FlowConfig.Builder builder;

    @Before
    public void setup() {
        builder = new FlowConfig.Builder(RuntimeEnvironment.application);
    }


    @Test
    public void test_flowConfig() {
        FlowManager.init(builder
                .openDatabasesOnInit(true)
                .build());

        FlowConfig config = FlowManager.getConfig();
        assertNotNull(config);
        assertEquals(config.openDatabasesOnInit(), true);
        assertTrue(config.databaseConfigMap().isEmpty());
        assertTrue(config.databaseHolders().isEmpty());
    }

    @Test
    public void test_databaseConfig() {

        final DatabaseHelperListener helperListener = new DatabaseHelperListener() {
            @Override
            public void onOpen(DatabaseWrapper database) {
            }

            @Override
            public void onCreate(DatabaseWrapper database) {
            }

            @Override
            public void onUpgrade(DatabaseWrapper database, int oldVersion, int newVersion) {
            }
        };

        final DatabaseConfig.OpenHelperCreator openHelperCreator = new DatabaseConfig.OpenHelperCreator() {
            @Override
            public OpenHelper createHelper(DatabaseDefinition databaseDefinition, DatabaseHelperListener helperListener) {
                return new CustomOpenHelper(databaseDefinition, helperListener);
            }
        };

        final DatabaseConfig.TransactionManagerCreator managerCreator = new DatabaseConfig.TransactionManagerCreator() {
            @Override
            public BaseTransactionManager createManager(DatabaseDefinition databaseDefinition) {
                return new TestTransactionManager(databaseDefinition);
            }
        };

        FlowManager.init(builder
                .addDatabaseConfig(new DatabaseConfig.Builder(TestDatabase.class)
                        .helperListener(helperListener)
                        .openHelper(openHelperCreator)
                        .transactionManagerCreator(managerCreator)
                        .build())
                .build());

        FlowConfig flowConfig = FlowManager.getConfig();
        assertNotNull(flowConfig);

        DatabaseConfig databaseConfig = flowConfig.databaseConfigMap().get(TestDatabase.class);
        assertNotNull(databaseConfig);

        assertEquals(databaseConfig.transactionManagerCreator(), managerCreator);
        assertEquals(databaseConfig.databaseClass(), TestDatabase.class);
        assertEquals(databaseConfig.helperCreator(), openHelperCreator);
        assertEquals(databaseConfig.helperListener(), helperListener);
        assertTrue(databaseConfig.tableConfigMap().isEmpty());
    }
}

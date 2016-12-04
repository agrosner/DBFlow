package com.raizlabs.android.dbflow.test.config;

import android.os.Build;

import com.raizlabs.android.dbflow.config.DatabaseConfig;
import com.raizlabs.android.dbflow.config.DatabaseDefinition;
import com.raizlabs.android.dbflow.config.FlowConfig;
import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.config.TableConfig;
import com.raizlabs.android.dbflow.runtime.BaseTransactionManager;
import com.raizlabs.android.dbflow.sql.queriable.ListModelLoader;
import com.raizlabs.android.dbflow.sql.queriable.SingleModelLoader;
import com.raizlabs.android.dbflow.sql.saveable.ModelSaver;
import com.raizlabs.android.dbflow.structure.ModelAdapter;
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;
import com.raizlabs.android.dbflow.structure.database.OpenHelper;
import com.raizlabs.android.dbflow.test.BuildConfig;
import com.raizlabs.android.dbflow.test.ShadowContentResolver2;
import com.raizlabs.android.dbflow.test.TestDatabase;
import com.raizlabs.android.dbflow.test.customhelper.CustomOpenHelper;
import com.raizlabs.android.dbflow.test.structure.TestModel1;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Description: Tests to ensure DBFlow is set up properly.
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = Build.VERSION_CODES.LOLLIPOP, shadows = {ShadowContentResolver2.class})
public class ConfigIntegrationTest {

    private FlowConfig.Builder builder;

    @Before
    public void setup() {
        FlowManager.reset();
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V);
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

        final CustomOpenHelperCreator openHelperCreator = new CustomOpenHelperCreator();
        final CustomTransactionManagerCreator managerCreator = new CustomTransactionManagerCreator();

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


        DatabaseDefinition databaseDefinition = FlowManager.getDatabase(TestDatabase.class);
        assertEquals(databaseDefinition.getTransactionManager(),
            managerCreator.getTestTransactionManager());
        assertEquals(databaseDefinition.getHelper(), openHelperCreator.getCustomOpenHelper());
    }

    @Test
    public void test_tableConfig() {

        ListModelLoader<TestModel1> customListModelLoader = new ListModelLoader<>(TestModel1.class);
        SingleModelLoader<TestModel1> singleModelLoader = new SingleModelLoader<>(TestModel1.class);
        ModelSaver<TestModel1> modelSaver = new ModelSaver<>();

        FlowManager.init(builder
            .addDatabaseConfig(new DatabaseConfig.Builder(TestDatabase.class)
                .addTableConfig(new TableConfig.Builder<>(TestModel1.class)
                    .singleModelLoader(singleModelLoader)
                    .listModelLoader(customListModelLoader)
                    .modelAdapterModelSaver(modelSaver)
                    .build())
                .build())
            .build());

        FlowConfig flowConfig = FlowManager.getConfig();
        assertNotNull(flowConfig);

        DatabaseConfig databaseConfig = flowConfig.databaseConfigMap().get(TestDatabase.class);
        assertNotNull(databaseConfig);

        //noinspection unchecked
        TableConfig<TestModel1> config = databaseConfig.tableConfigMap().get(TestModel1.class);
        assertNotNull(config);

        assertEquals(config.listModelLoader(), customListModelLoader);
        assertEquals(config.singleModelLoader(), singleModelLoader);

        ModelAdapter<TestModel1> modelAdapter = FlowManager.getModelAdapter(TestModel1.class);
        assertEquals(modelAdapter.getListModelLoader(), customListModelLoader);
        assertEquals(modelAdapter.getSingleModelLoader(), singleModelLoader);
        assertEquals(modelAdapter.getModelSaver(), modelSaver);
    }

    private static class CustomTransactionManagerCreator implements DatabaseConfig.TransactionManagerCreator {

        public TestTransactionManager testTransactionManager;

        @Override
        public BaseTransactionManager createManager(DatabaseDefinition databaseDefinition) {
            testTransactionManager = new TestTransactionManager(databaseDefinition);
            return testTransactionManager;
        }

        public TestTransactionManager getTestTransactionManager() {
            return testTransactionManager;
        }
    }

    private static class CustomOpenHelperCreator implements DatabaseConfig.OpenHelperCreator {

        private CustomOpenHelper customOpenHelper;

        @Override
        public OpenHelper createHelper(DatabaseDefinition databaseDefinition, DatabaseHelperListener helperListener) {
            customOpenHelper = new CustomOpenHelper(databaseDefinition, helperListener);
            return customOpenHelper;
        }

        public CustomOpenHelper getCustomOpenHelper() {
            return customOpenHelper;
        }
    }
}

package com.raizlabs.android.dbflow.test.config

import android.os.Build
import com.raizlabs.android.dbflow.config.*
import com.raizlabs.android.dbflow.runtime.BaseTransactionManager
import com.raizlabs.android.dbflow.sql.queriable.ListModelLoader
import com.raizlabs.android.dbflow.sql.queriable.SingleModelLoader
import com.raizlabs.android.dbflow.sql.saveable.ModelSaver
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import com.raizlabs.android.dbflow.structure.database.OpenHelper
import com.raizlabs.android.dbflow.test.BuildConfig
import com.raizlabs.android.dbflow.test.ShadowContentResolver2
import com.raizlabs.android.dbflow.test.TestDatabase
import com.raizlabs.android.dbflow.test.customhelper.CustomOpenHelper
import com.raizlabs.android.dbflow.test.structure.TestModel1
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

/**
 * Description: Tests to ensure DBFlow is set up properly.
 */
@RunWith(RobolectricTestRunner::class)
@Config(constants = BuildConfig::class, sdk = intArrayOf(Build.VERSION_CODES.LOLLIPOP),
    shadows = arrayOf(ShadowContentResolver2::class))
class ConfigIntegrationTest {

    private lateinit var builder: FlowConfig.Builder

    @Before
    fun setup() {
        FlowManager.reset()
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V)
        builder = FlowConfig.Builder(RuntimeEnvironment.application)
    }


    @Test
    fun test_flowConfig() {
        FlowManager.init(builder
            .openDatabasesOnInit(true)
            .build())

        val config = FlowManager.getConfig()
        assertNotNull(config)
        assertEquals(config.openDatabasesOnInit(), true)
        assertTrue(config.databaseConfigMap().isEmpty())
        assertTrue(config.databaseHolders().isEmpty())
    }

    @Test
    fun test_databaseConfig() {

        val helperListener = object : DatabaseHelperListener {
            override fun onOpen(database: DatabaseWrapper) {
            }

            override fun onCreate(database: DatabaseWrapper) {
            }

            override fun onUpgrade(database: DatabaseWrapper, oldVersion: Int, newVersion: Int) {
            }
        }

        val openHelperCreator = CustomOpenHelperCreator()
        val managerCreator = CustomTransactionManagerCreator()

        FlowManager.init(builder
            .addDatabaseConfig(DatabaseConfig.Builder(TestDatabase::class.java)
                .helperListener(helperListener)
                .openHelper(openHelperCreator)
                .transactionManagerCreator(managerCreator)
                .build())
            .build())

        val flowConfig = FlowManager.getConfig()
        assertNotNull(flowConfig)

        val databaseConfig = flowConfig.databaseConfigMap()[TestDatabase::class.java] as DatabaseConfig
        assertNotNull(databaseConfig)

        assertEquals(databaseConfig.transactionManagerCreator(), managerCreator)
        assertEquals(databaseConfig.databaseClass(), TestDatabase::class.java)
        assertEquals(databaseConfig.helperCreator(), openHelperCreator)
        assertEquals(databaseConfig.helperListener(), helperListener)
        assertTrue(databaseConfig.tableConfigMap().isEmpty())


        val databaseDefinition = FlowManager.getDatabase(TestDatabase::class.java)
        assertEquals(databaseDefinition.transactionManager,
            managerCreator.testTransactionManager)
        assertEquals(databaseDefinition.helper, openHelperCreator.customOpenHelper)
    }

    @Test
    fun test_tableConfig() {

        val customListModelLoader = ListModelLoader(TestModel1::class.java)
        val singleModelLoader = SingleModelLoader(TestModel1::class.java)
        val modelSaver = ModelSaver<TestModel1>()

        FlowManager.init(builder
            .addDatabaseConfig(DatabaseConfig.Builder(TestDatabase::class.java)
                .addTableConfig(TableConfig.Builder(TestModel1::class.java)
                    .singleModelLoader(singleModelLoader)
                    .listModelLoader(customListModelLoader)
                    .modelAdapterModelSaver(modelSaver)
                    .build())
                .build())
            .build())

        val flowConfig = FlowManager.getConfig()
        assertNotNull(flowConfig)

        val databaseConfig = flowConfig.databaseConfigMap()[TestDatabase::class.java] as DatabaseConfig
        assertNotNull(databaseConfig)


        val config = databaseConfig.tableConfigMap()[TestModel1::class.java] as TableConfig
        assertNotNull(config)

        assertEquals(config.listModelLoader(), customListModelLoader)
        assertEquals(config.singleModelLoader(), singleModelLoader)

        val modelAdapter = FlowManager.getModelAdapter(TestModel1::class.java)
        assertEquals(modelAdapter.listModelLoader, customListModelLoader)
        assertEquals(modelAdapter.singleModelLoader, singleModelLoader)
        assertEquals(modelAdapter.modelSaver, modelSaver)
    }

    private class CustomTransactionManagerCreator : DatabaseConfig.TransactionManagerCreator {

        lateinit var testTransactionManager: TestTransactionManager

        override fun createManager(databaseDefinition: DatabaseDefinition): BaseTransactionManager {
            testTransactionManager = TestTransactionManager(databaseDefinition)
            return testTransactionManager
        }
    }

    private class CustomOpenHelperCreator : DatabaseConfig.OpenHelperCreator {

        lateinit var customOpenHelper: CustomOpenHelper
            private set

        override fun createHelper(databaseDefinition: DatabaseDefinition, helperListener: DatabaseHelperListener): OpenHelper {
            customOpenHelper = CustomOpenHelper(databaseDefinition, helperListener)
            return customOpenHelper
        }
    }
}

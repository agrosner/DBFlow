package com.raizlabs.android.dbflow.config

import com.nhaarman.mockito_kotlin.mock
import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.SimpleModel
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.runtime.BaseTransactionManager
import com.raizlabs.android.dbflow.sql.queriable.ListModelLoader
import com.raizlabs.android.dbflow.sql.queriable.SingleModelLoader
import com.raizlabs.android.dbflow.sql.saveable.ModelSaver
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener
import com.raizlabs.android.dbflow.structure.database.OpenHelper
import com.raizlabs.android.dbflow.structure.database.transaction.ITransactionQueue
import junit.framework.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Description:
 */
class ConfigIntegrationTest : BaseUnitTest() {

    private lateinit var builder: FlowConfig.Builder

    @Before
    fun setup() {
        FlowManager.reset()
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V)
        builder = FlowConfig.Builder(context)
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

        val helperListener = mock<DatabaseHelperListener>()

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

        val customListModelLoader = ListModelLoader(SimpleModel::class.java)
        val singleModelLoader = SingleModelLoader(SimpleModel::class.java)
        val modelSaver = ModelSaver<SimpleModel>()

        FlowManager.init(builder
            .addDatabaseConfig(DatabaseConfig.Builder(TestDatabase::class.java)
                .addTableConfig(TableConfig.Builder(SimpleModel::class.java)
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


        val config = databaseConfig.tableConfigMap()[SimpleModel::class.java] as TableConfig
        assertNotNull(config)

        assertEquals(config.listModelLoader(), customListModelLoader)
        assertEquals(config.singleModelLoader(), singleModelLoader)

        val modelAdapter = FlowManager.getModelAdapter(SimpleModel::class.java)
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

        val customOpenHelper = mock<OpenHelper>()

        override fun createHelper(databaseDefinition: DatabaseDefinition, helperListener: DatabaseHelperListener): OpenHelper {
            return customOpenHelper
        }
    }
}

class TestTransactionManager(databaseDefinition: DatabaseDefinition)
    : BaseTransactionManager(mock<ITransactionQueue>(), databaseDefinition)


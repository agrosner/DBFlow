package com.raizlabs.android.dbflow.config

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.sql.queriable.ListModelLoader
import com.raizlabs.android.dbflow.sql.queriable.SingleModelLoader
import com.raizlabs.android.dbflow.sql.saveable.ModelSaver
import org.junit.Assert.*
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
        assertEquals(config.openDatabasesOnInit, true)
        assertTrue(config.databaseConfigMap.isEmpty())
        assertTrue(config.databaseHolders.isEmpty())
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

        val databaseConfig = flowConfig.databaseConfigMap[TestDatabase::class.java] as DatabaseConfig
        assertNotNull(databaseConfig)


        val config = databaseConfig.tableConfigMap[SimpleModel::class.java] as TableConfig
        assertNotNull(config)

        assertEquals(config.listModelLoader, customListModelLoader)
        assertEquals(config.singleModelLoader, singleModelLoader)

        val modelAdapter = FlowManager.getModelAdapter(SimpleModel::class.java)
        assertEquals(modelAdapter.listModelLoader, customListModelLoader)
        assertEquals(modelAdapter.singleModelLoader, singleModelLoader)
        assertEquals(modelAdapter.modelSaver, modelSaver)
    }

}



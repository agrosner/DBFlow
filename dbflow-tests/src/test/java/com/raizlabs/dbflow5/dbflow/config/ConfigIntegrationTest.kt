package com.raizlabs.dbflow5.dbflow.config

import com.raizlabs.dbflow5.config.DatabaseConfig
import com.raizlabs.dbflow5.config.FlowConfig
import com.raizlabs.dbflow5.config.FlowLog
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.dbflow.BaseUnitTest
import com.raizlabs.dbflow5.dbflow.TestDatabase
import com.raizlabs.dbflow5.adapter.queriable.ListModelLoader
import com.raizlabs.dbflow5.adapter.queriable.SingleModelLoader
import com.raizlabs.dbflow5.adapter.saveable.ModelSaver
import com.raizlabs.dbflow5.config.TableConfig
import com.raizlabs.dbflow5.dbflow.models.SimpleModel
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



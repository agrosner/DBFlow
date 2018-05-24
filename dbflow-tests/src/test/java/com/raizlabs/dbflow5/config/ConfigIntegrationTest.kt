package com.raizlabs.dbflow5.config

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.TestDatabase
import com.raizlabs.dbflow5.adapter.queriable.ListModelLoader
import com.raizlabs.dbflow5.adapter.queriable.SingleModelLoader
import com.raizlabs.dbflow5.adapter.saveable.ModelSaver
import com.raizlabs.dbflow5.database.AndroidSQLiteOpenHelper
import com.raizlabs.dbflow5.models.SimpleModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
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
        val config = builder
            .openDatabasesOnInit(true)
            .build()
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
            .database(DatabaseConfig.Builder(TestDatabase::class.java,
                AndroidSQLiteOpenHelper.createHelperCreator(context))
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

        val modelAdapter = SimpleModel::class.modelAdapter
        assertEquals(modelAdapter.listModelLoader, customListModelLoader)
        assertEquals(modelAdapter.singleModelLoader, singleModelLoader)
        assertEquals(modelAdapter.modelSaver, modelSaver)
    }

}



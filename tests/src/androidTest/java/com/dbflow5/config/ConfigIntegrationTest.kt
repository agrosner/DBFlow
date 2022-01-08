package com.dbflow5.config

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.adapter.queriable.ListModelLoader
import com.dbflow5.adapter.queriable.SingleModelLoader
import com.dbflow5.adapter.saveable.ModelSaver
import com.dbflow5.database.AndroidSQLiteOpenHelper
import com.dbflow5.models.SimpleModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Description:
 */
class ConfigIntegrationTest : BaseUnitTest() {

    @Before
    fun setup() {
        FlowManager.reset()
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V)
    }

    @Test
    fun test_flowConfig() {
        val config = flowConfig(context) {
            openDatabasesOnInit(true)
        }
        assertEquals(config.openDatabasesOnInit, true)
        assertTrue(config.databaseConfigMap.isEmpty())
        assertTrue(config.databaseHolders.isEmpty())
    }

    @Test
    fun test_tableConfig() {

        val customListModelLoader = ListModelLoader(SimpleModel::class)
        val singleModelLoader = SingleModelLoader(SimpleModel::class)
        val modelSaver = ModelSaver<SimpleModel>()

        FlowManager.init(
            flowConfig(context) {
                database<TestDatabase>({
                    table<SimpleModel> {
                        singleModelLoader(singleModelLoader)
                        listModelLoader(customListModelLoader)
                        modelAdapterModelSaver(modelSaver)
                    }
                }, AndroidSQLiteOpenHelper.createHelperCreator(context))
            })

        val flowConfig = FlowManager.getConfig()
        assertNotNull(flowConfig)

        val databaseConfig = flowConfig.databaseConfigMap[TestDatabase::class] as DatabaseConfig
        assertNotNull(databaseConfig)


        val config = databaseConfig.tableConfigMap[SimpleModel::class] as TableConfig
        assertNotNull(config)

        assertEquals(config.listModelLoader, customListModelLoader)
        assertEquals(config.singleModelLoader, singleModelLoader)

        val modelAdapter = SimpleModel::class.modelAdapter
        assertEquals(modelAdapter.listModelLoader, customListModelLoader)
        assertEquals(modelAdapter.singleModelLoader, singleModelLoader)
        assertEquals(modelAdapter.modelSaver, modelSaver)
    }

}



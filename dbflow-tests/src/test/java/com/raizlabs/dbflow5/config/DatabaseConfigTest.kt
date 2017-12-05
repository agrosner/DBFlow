package com.raizlabs.dbflow5.config

import com.nhaarman.mockito_kotlin.mock
import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.TestDatabase
import com.raizlabs.dbflow5.database.DatabaseHelperListener
import com.raizlabs.dbflow5.database.OpenHelper
import com.raizlabs.dbflow5.transaction.BaseTransactionManager
import org.junit.Assert
import org.junit.Before
import org.junit.Test


/**
 * Description:
 */
class DatabaseConfigTest : BaseUnitTest() {

    private lateinit var builder: FlowConfig.Builder

    @Before
    fun setup() {
        FlowManager.reset()
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V)
        builder = FlowConfig.Builder(context)
    }

    @Test
    fun test_databaseConfig() {

        val helperListener = mock<DatabaseHelperListener>()
        val customOpenHelper = mock<OpenHelper>()

        val openHelperCreator: ((DBFlowDatabase, DatabaseHelperListener?) -> OpenHelper) = { _, _ ->
            customOpenHelper
        }
        var testTransactionManager: TestTransactionManager? = null
        val managerCreator: (DBFlowDatabase) -> BaseTransactionManager = { databaseDefinition ->
            testTransactionManager = TestTransactionManager(databaseDefinition)
            testTransactionManager!!
        }

        FlowManager.init(builder
            .database(DatabaseConfig.Builder(TestDatabase::class.java)
                .databaseName("Test")
                .helperListener(helperListener)
                .openHelper(openHelperCreator)
                .transactionManagerCreator(managerCreator)
                .build())
            .build())

        val flowConfig = FlowManager.getConfig()
        Assert.assertNotNull(flowConfig)

        val databaseConfig = flowConfig.databaseConfigMap[TestDatabase::class.java]!!
        Assert.assertEquals("Test", databaseConfig.databaseName)
        Assert.assertEquals(".db", databaseConfig.databaseExtensionName)
        Assert.assertEquals(databaseConfig.transactionManagerCreator, managerCreator)
        Assert.assertEquals(databaseConfig.databaseClass, TestDatabase::class.java)
        Assert.assertEquals(databaseConfig.openHelperCreator, openHelperCreator)
        Assert.assertEquals(databaseConfig.helperListener, helperListener)
        Assert.assertTrue(databaseConfig.tableConfigMap.isEmpty())


        val databaseDefinition = database<TestDatabase>()
        Assert.assertEquals(databaseDefinition.transactionManager, testTransactionManager)
        Assert.assertEquals(databaseDefinition.helper, customOpenHelper)
    }

    @Test
    fun test_EmptyName() {
        FlowManager.init(builder
            .database(DatabaseConfig.Builder(TestDatabase::class.java)
                .databaseName("Test")
                .extensionName("")
                .build())
            .build())

        val databaseConfig = FlowManager.getConfig().databaseConfigMap[TestDatabase::class.java]!!
        Assert.assertEquals("Test", databaseConfig.databaseName)
        Assert.assertEquals("", databaseConfig.databaseExtensionName)
    }

}

class TestTransactionManager(databaseDefinition: DBFlowDatabase)
    : BaseTransactionManager(mock(), databaseDefinition)
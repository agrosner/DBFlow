package com.dbflow5.config

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.database.AndroidSQLiteOpenHelper
import com.dbflow5.database.DatabaseCallback
import com.dbflow5.database.OpenHelper
import com.dbflow5.transaction.BaseTransactionManager
import com.nhaarman.mockitokotlin2.mock
import org.junit.Before
import org.junit.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue


/**
 * Description:
 */
class DatabaseConfigTest : BaseUnitTest() {

    @Before
    fun setup() {
        FlowManager.reset()
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.V)
    }

    @Test
    fun test_databaseConfig() {

        val helperListener = object : DatabaseCallback {}
        val customOpenHelper = mock<OpenHelper>()

        val openHelperCreator = OpenHelperCreator { _, _ ->
            customOpenHelper
        }
        lateinit var testTransactionManager: TestTransactionManager
        val managerCreator = TransactionManagerCreator { databaseDefinition ->
            testTransactionManager = TestTransactionManager(databaseDefinition)
            testTransactionManager
        }

        FlowManager.init(context) {
            database<TestDatabase>({
                databaseName("Test")
                helperListener(helperListener)
                transactionManagerCreator(managerCreator)
            }, openHelperCreator)
        }

        val flowConfig = FlowManager.getConfig()
        val databaseConfig = flowConfig.databaseConfigMap[TestDatabase::class]!!
        assertEquals("Test", databaseConfig.databaseName)
        assertEquals(".db", databaseConfig.databaseExtensionName)
        assertEquals(databaseConfig.transactionManagerCreator, managerCreator)
        assertEquals(databaseConfig.databaseClass, TestDatabase::class)
        assertEquals(databaseConfig.openHelperCreator, openHelperCreator)
        assertEquals(databaseConfig.callback, helperListener)
        assertTrue(databaseConfig.tableConfigMap.isEmpty())


        val databaseDefinition = database<TestDatabase>()
        assertEquals(databaseDefinition.transactionManager, testTransactionManager)
        assertEquals(databaseDefinition.openHelper, customOpenHelper)
    }

    @Test
    fun test_EmptyName() {
        FlowManager.init(context) {
            database<TestDatabase>({
                databaseName("Test")
                extensionName("")
            }, AndroidSQLiteOpenHelper.createHelperCreator(context))
        }

        val databaseConfig = FlowManager.getConfig().databaseConfigMap[TestDatabase::class]!!
        assertEquals("Test", databaseConfig.databaseName)
        assertEquals("", databaseConfig.databaseExtensionName)
    }

}

class TestTransactionManager(databaseDefinition: DBFlowDatabase) :
    BaseTransactionManager(mock(), databaseDefinition)
package com.dbflow5.config

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.database.AndroidSQLiteOpenHelper
import com.dbflow5.database.DatabaseCallback
import com.dbflow5.database.OpenHelper
import com.dbflow5.transaction.TransactionDispatcher
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.test.TestCoroutineDispatcher
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
        lateinit var testTransactionManager: TransactionDispatcher
        val managerCreator = TransactionDispatcherFactory {
            testTransactionManager = TransactionDispatcher(TestCoroutineDispatcher())
            testTransactionManager
        }

        FlowManager.init(context) {
            database<TestDatabase>({
                databaseName("Test")
                helperListener(helperListener)
                transactionDispatcherFactory(managerCreator)
            }, openHelperCreator)
        }

        val flowConfig = FlowManager.getConfig()
        val databaseConfig = flowConfig.databaseConfigMap[TestDatabase::class]!!
        assertEquals("Test", databaseConfig.databaseName)
        assertEquals(".db", databaseConfig.databaseExtensionName)
        assertEquals(databaseConfig.transactionDispatcherFactory, managerCreator)
        assertEquals(databaseConfig.databaseClass, TestDatabase::class)
        assertEquals(databaseConfig.openHelperCreator, openHelperCreator)
        assertEquals(databaseConfig.callback, helperListener)
        assertTrue(databaseConfig.tableConfigMap.isEmpty())


        val databaseDefinition = database<TestDatabase>()
        assertEquals(databaseDefinition.dispatcher, testTransactionManager)
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
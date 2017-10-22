package com.raizlabs.android.dbflow.config

import com.nhaarman.mockito_kotlin.mock
import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.runtime.BaseTransactionManager
import com.raizlabs.android.dbflow.structure.database.DatabaseHelperListener
import com.raizlabs.android.dbflow.structure.database.OpenHelper
import com.raizlabs.android.dbflow.structure.database.transaction.ITransactionQueue
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

        val openHelperCreator = CustomOpenHelperCreator()
        val managerCreator = CustomTransactionManagerCreator()

        FlowManager.init(builder
            .addDatabaseConfig(DatabaseConfig.Builder(TestDatabase::class.java)
                .databaseName("Test")
                .helperListener(helperListener)
                .openHelper(openHelperCreator)
                .transactionManagerCreator(managerCreator)
                .build())
            .build())

        val flowConfig = FlowManager.getConfig()
        Assert.assertNotNull(flowConfig)

        val databaseConfig = flowConfig.getDatabaseConfigMap()[TestDatabase::class.java]!!
        Assert.assertEquals("Test", databaseConfig.databaseName)
        Assert.assertEquals(".db", databaseConfig.databaseExtensionName)
        Assert.assertEquals(databaseConfig.getTransactionManagerCreator(), managerCreator)
        Assert.assertEquals(databaseConfig.getDatabaseClass(), TestDatabase::class.java)
        Assert.assertEquals(databaseConfig.getOpenHelperCreator(), openHelperCreator)
        Assert.assertEquals(databaseConfig.getHelperListener(), helperListener)
        Assert.assertTrue(databaseConfig.getTableConfigMap().isEmpty())


        val databaseDefinition = FlowManager.getDatabase(TestDatabase::class.java)
        Assert.assertEquals(databaseDefinition.transactionManager,
            managerCreator.testTransactionManager)
        Assert.assertEquals(databaseDefinition.helper, openHelperCreator.customOpenHelper)
    }

    @Test
    fun test_EmptyName() {
        FlowManager.init(builder
            .addDatabaseConfig(DatabaseConfig.Builder(TestDatabase::class.java)
                .databaseName("Test")
                .extensionName("")
                .build())
            .build())

        val databaseConfig = FlowManager.getConfig().getDatabaseConfigMap()[TestDatabase::class.java]!!
        Assert.assertEquals("Test", databaseConfig.databaseName)
        Assert.assertEquals("", databaseConfig.databaseExtensionName)
    }

    class CustomTransactionManagerCreator : DatabaseConfig.TransactionManagerCreator {

        lateinit var testTransactionManager: TestTransactionManager

        override fun createManager(databaseDefinition: DatabaseDefinition): BaseTransactionManager {
            testTransactionManager = TestTransactionManager(databaseDefinition)
            return testTransactionManager
        }
    }

    class CustomOpenHelperCreator : DatabaseConfig.OpenHelperCreator {

        val customOpenHelper = mock<OpenHelper>()

        override fun createHelper(databaseDefinition: DatabaseDefinition, helperListener: DatabaseHelperListener): OpenHelper {
            return customOpenHelper
        }
    }

}

class TestTransactionManager(databaseDefinition: DatabaseDefinition)
    : BaseTransactionManager(mock<ITransactionQueue>(), databaseDefinition)
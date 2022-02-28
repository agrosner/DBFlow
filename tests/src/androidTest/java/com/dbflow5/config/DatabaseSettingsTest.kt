package com.dbflow5.config

import androidx.test.platform.app.InstrumentationRegistry
import com.dbflow5.TestDatabase_Database
import com.dbflow5.database.DatabaseCallback
import com.dbflow5.database.OpenHelper
import com.dbflow5.database.OpenHelperCreator
import com.dbflow5.database.transaction.TransactionDispatcherFactory
import com.dbflow5.transaction.TransactionDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import org.junit.Test
import org.mockito.kotlin.mock
import kotlin.test.assertEquals


class DatabaseSettingsTest {

    @Test
    fun test_databaseConfig() {
        val databaseCallback = object : DatabaseCallback {}
        val customOpenHelper = mock<OpenHelper>()

        val openHelperCreator = OpenHelperCreator { _, _ ->
            customOpenHelper
        }
        lateinit var testTransactionManager: TransactionDispatcher
        val dispatcherFactory = TransactionDispatcherFactory {
            testTransactionManager = TransactionDispatcher(StandardTestDispatcher())
            testTransactionManager
        }

        val db =
            TestDatabase_Database.create(InstrumentationRegistry.getInstrumentation().targetContext) {
                copy(
                    name = "Test",
                    databaseCallback = databaseCallback,
                    transactionDispatcherFactory = dispatcherFactory,
                    openHelperCreator = openHelperCreator,
                )
            }
        // force initialize it.
        db.transactionDispatcher

        assertEquals("Test", db.databaseName)
        assertEquals(".db", db.databaseExtensionName)
        assertEquals(testTransactionManager, db.transactionDispatcher)
        assertEquals(customOpenHelper, db.openHelper)
    }

    @Test
    fun test_EmptyName() {
        val db =
            TestDatabase_Database.create(InstrumentationRegistry.getInstrumentation().targetContext) {
                copy(
                    name = "Test",
                    databaseExtensionName = "",
                )
            }
        assertEquals("Test", db.databaseName)
        assertEquals("", db.databaseExtensionName)
    }
}
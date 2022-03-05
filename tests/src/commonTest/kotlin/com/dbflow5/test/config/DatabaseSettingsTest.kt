package com.dbflow5.test.config

import com.dbflow5.database.DatabaseCallback
import com.dbflow5.database.OpenHelperCreator
import com.dbflow5.database.transaction.TransactionDispatcherFactory
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.fakes.FakeOpenHelper
import com.dbflow5.test.helpers.platformSettings
import com.dbflow5.transaction.TransactionDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlin.test.Test
import kotlin.test.assertEquals


class DatabaseSettingsTest {

    @Test
    fun test_databaseConfig() {
        val databaseCallback = object : DatabaseCallback {}

        val fakeOpenHelper = FakeOpenHelper()
        val openHelperCreator = OpenHelperCreator { _, _ ->
            fakeOpenHelper
        }
        lateinit var testTransactionManager: TransactionDispatcher
        val dispatcherFactory = TransactionDispatcherFactory {
            testTransactionManager = TransactionDispatcher(StandardTestDispatcher())
            testTransactionManager
        }

        val db =
            TestDatabase_Database.create(platformSettings = platformSettings()) {
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
        assertEquals(fakeOpenHelper, db.openHelper)
    }

    @Test
    fun test_EmptyName() {
        val db =
            TestDatabase_Database.create(platformSettings = platformSettings()) {
                copy(
                    name = "Test",
                    databaseExtensionName = "",
                )
            }
        assertEquals("Test", db.databaseName)
        assertEquals("", db.databaseExtensionName)
    }
}

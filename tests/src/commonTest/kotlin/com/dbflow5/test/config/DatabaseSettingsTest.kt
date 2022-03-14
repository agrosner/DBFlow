package com.dbflow5.test.config

import com.dbflow5.database.DatabaseCallback
import com.dbflow5.database.OpenHelperCreator
import com.dbflow5.database.transaction.TransactionDispatcherFactory
import com.dbflow5.runtime.ModelNotifier
import com.dbflow5.runtime.ModelNotifierFactory
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.fakes.FakeOpenHelper
import com.dbflow5.test.helpers.platformSettings
import com.dbflow5.transaction.TransactionDispatcher
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.TestScope
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

        val modelNotifier = ModelNotifier(notificationScope = TestScope())
        val modelNotifierFactory = ModelNotifierFactory {
            modelNotifier
        }

        val db =
            TestDatabase_Database.create(platformSettings = platformSettings()) {
                copy(
                    name = "Test",
                    databaseCallback = databaseCallback,
                    transactionDispatcherFactory = dispatcherFactory,
                    openHelperCreator = openHelperCreator,
                    modelNotifierFactory = modelNotifierFactory,
                )
            }
        // force initialize it.
        db.transactionDispatcher

        assertEquals("Test", db.databaseName)
        assertEquals(".db", db.databaseExtensionName)
        assertEquals(testTransactionManager, db.transactionDispatcher)
        assertEquals(fakeOpenHelper, db.openHelper)
        assertEquals(modelNotifier, db.modelNotifier)
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

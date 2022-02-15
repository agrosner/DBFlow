package com.dbflow5.contentobserver

import android.net.Uri
import androidx.test.core.app.ApplicationProvider
import com.dbflow5.DBFlowInstrumentedTestRule
import com.dbflow5.DemoApp
import com.dbflow5.TABLE_QUERY_PARAM
import com.dbflow5.TestTransactionDispatcherFactory
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.database.AndroidSQLiteOpenHelper
import com.dbflow5.database.scope.WritableDatabaseScope
import com.dbflow5.query2.delete
import com.dbflow5.runtime.ContentNotification
import com.dbflow5.runtime.ContentNotificationListener
import com.dbflow5.runtime.ContentResolverNotifier
import com.dbflow5.runtime.FlowContentObserver
import com.dbflow5.structure.ChangeAction
import com.nhaarman.mockitokotlin2.argumentCaptor
import com.nhaarman.mockitokotlin2.doReturn
import com.nhaarman.mockitokotlin2.mock
import kotlinx.coroutines.delay
import kotlinx.coroutines.test.TestCoroutineDispatcher
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import kotlin.test.assertIs

class ContentObserverTest {

    @JvmField
    @Rule
    var dblflowTestRule = DBFlowInstrumentedTestRule.create {
        database<ContentObserverDatabase>({
            modelNotifier { ContentResolverNotifier(DemoApp.context, "com.grosner.content", it) }
            transactionDispatcherFactory(TestTransactionDispatcherFactory(TestCoroutineDispatcher()))
        }, AndroidSQLiteOpenHelper.createHelperCreator(ApplicationProvider.getApplicationContext()))
    }

    val contentUri = "com.grosner.content"

    private lateinit var user: User

    @Before
    fun setupUser() = runBlockingTest {
        database<ContentObserverDatabase>().writableTransaction {
            userAdapter.delete().execute()
        }
        user = User(5, "Something", 55)
    }

    @Test
    fun content_notification_ModelChange_validateUri() {
        val database = database<ContentObserverDatabase>()
        val notification = ContentNotification.ModelChange(
            user,
            database.userAdapter,
            ChangeAction.DELETE,
            contentUri,
        )
        val uri = notification.uri
        assertEquals(uri.authority, contentUri)
        assertEquals(
            database.userAdapter.name,
            uri.getQueryParameter(TABLE_QUERY_PARAM)
        )
        assertEquals(uri.fragment, ChangeAction.DELETE.name)
        assertEquals(Uri.decode(uri.getQueryParameter(User_Table.id.query)), "5")
        assertEquals(
            Uri.decode(uri.getQueryParameter(User_Table.name.query)),
            "Something"
        )
    }

    @Test
    fun testSpecificUrlInsert() = runBlockingTest {
        assertProperConditions(ChangeAction.INSERT) { user -> userAdapter.insert(user) }
    }

    @Test
    fun testSpecificUrlUpdate() = runBlockingTest {
        assertProperConditions(ChangeAction.UPDATE) { user ->
            userAdapter.update(user.copy(age = 56))
        }
    }

    @Test
    fun testSpecificUrlSave() = runBlockingTest {
        // insert on SAVE
        assertProperConditions(ChangeAction.INSERT) { user -> userAdapter.save(user.copy(age = 57)) }
    }

    @Test
    fun testSpecificUrlDelete() = runBlockingTest {
        database<ContentObserverDatabase>().writableTransaction {
            userAdapter.save(user)
        }
        assertProperConditions(ChangeAction.DELETE) { user -> userAdapter.delete(user) }
    }

    private suspend fun assertProperConditions(
        action: ChangeAction,
        userFunc: suspend WritableDatabaseScope<ContentObserverDatabase>.(User) -> Unit
    ) {
        val contentObserver = FlowContentObserver(contentUri)
        // use latch to wait for result asynchronously.

        val capture = argumentCaptor<ContentNotification<Any>>()
        val listener = mock<ContentNotificationListener<Any>> {
            on { onChange(capture.capture()) } doReturn Unit
        }
        contentObserver.addListener(listener)
        contentObserver.registerForContentChanges(DemoApp.context.contentResolver, User::class)

        database<ContentObserverDatabase>().writableTransaction {
            userFunc(user)
        }
        while (capture.allValues.isEmpty()) {
            delay(100L)
        }

        contentObserver.removeListener(listener)
        contentObserver.unregisterForContentChanges(DemoApp.context.contentResolver)

        val notification =
            assertIs<ContentNotification.ModelChange<*>>(
                capture.lastValue,
                "Type is not ModelChange."
            )
        val ops = notification.changedFields
        assertEquals(2, ops.size)
        assertEquals(ops[0].key, User_Table.id.query)
        assertEquals(ops[1].key, User_Table.name.query)
        assertEquals(ops[1].value, "Something")
        assertEquals(ops[0].value, "5")
        assertEquals(action, notification.action)

    }
}

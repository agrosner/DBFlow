package com.raizlabs.android.dbflow.contentobserver

import android.net.Uri
import com.raizlabs.android.dbflow.BaseInstrumentedUnitTest
import com.raizlabs.android.dbflow.DemoApp
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.config.databaseForTable
import com.raizlabs.android.dbflow.config.tableName
import com.raizlabs.android.dbflow.contentobserver.User_Table.id
import com.raizlabs.android.dbflow.contentobserver.User_Table.name
import com.raizlabs.android.dbflow.runtime.FlowContentObserver
import com.raizlabs.android.dbflow.TABLE_QUERY_PARAM
import com.raizlabs.android.dbflow.getNotificationUri
import com.raizlabs.android.dbflow.query.SQLOperator
import com.raizlabs.android.dbflow.query.delete
import com.raizlabs.android.dbflow.structure.ChangeAction
import com.raizlabs.android.dbflow.structure.delete
import com.raizlabs.android.dbflow.structure.insert
import com.raizlabs.android.dbflow.structure.save
import com.raizlabs.android.dbflow.structure.update
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class ContentObserverTest : BaseInstrumentedUnitTest() {

    val contentUri = "com.grosner.content"

    private lateinit var user: User

    @Before
    fun setupUser() {
        databaseForTable<User> {
            (delete() from User::class).execute()
        }
        user = User(5, "Something", 55)
    }

    @Test
    fun testSpecificUris() {
        val conditionGroup = FlowManager.getModelAdapter(User::class.java)
                .getPrimaryConditionClause(user)
        val uri = getNotificationUri(contentUri,
                User::class.java, ChangeAction.DELETE,
                conditionGroup.conditions.toTypedArray())

        assertEquals(uri.authority, contentUri)
        assertEquals(tableName<User>(), uri.getQueryParameter(TABLE_QUERY_PARAM))
        assertEquals(uri.fragment, ChangeAction.DELETE.name)
        assertEquals(Uri.decode(uri.getQueryParameter(Uri.encode(id.query))), "5")
        assertEquals(Uri.decode(uri.getQueryParameter(Uri.encode(name.query))), "Something")
    }

    @Test
    fun testSpecificUrlInsert() {
        assertProperConditions(ChangeAction.INSERT, { it.insert() })
    }

    @Test
    fun testSpecificUrlUpdate() {
        assertProperConditions(ChangeAction.UPDATE, { it.apply { age = 56 }.update() })

    }

    @Test
    fun testSpecificUrlSave() {
        // insert on SAVE
        assertProperConditions(ChangeAction.INSERT, { it.apply { age = 57 }.save() })
    }

    @Test
    fun testSpecificUrlDelete() {
        user.save()
        assertProperConditions(ChangeAction.DELETE, { it.delete() })
    }

    private fun assertProperConditions(action: ChangeAction, userFunc: (User) -> Unit) {
        val contentObserver = FlowContentObserver(contentUri)
        val countDownLatch = CountDownLatch(1)
        val mockOnModelStateChangedListener = MockOnModelStateChangedListener(countDownLatch)
        contentObserver.addModelChangeListener(mockOnModelStateChangedListener)
        contentObserver.registerForContentChanges(DemoApp.context, User::class.java)

        userFunc(user)
        countDownLatch.await()

        val ops = mockOnModelStateChangedListener.operators!!
        assertEquals(2, ops.size)
        assertEquals(ops[0].columnName(), id.query)
        assertEquals(ops[1].columnName(), name.query)
        assertEquals(ops[1].value(), "Something")
        assertEquals(ops[0].value(), "5")
        assertEquals(action, mockOnModelStateChangedListener.action)

        contentObserver.removeModelChangeListener(mockOnModelStateChangedListener)
        contentObserver.unregisterForContentChanges(DemoApp.context)
    }

    class MockOnModelStateChangedListener(val countDownLatch: CountDownLatch)
        : FlowContentObserver.OnModelStateChangedListener {

        var action: ChangeAction? = null
        var operators: Array<SQLOperator>? = null


        override fun onModelStateChanged(table: Class<*>?, action: ChangeAction,
                                         primaryKeyValues: Array<SQLOperator>) {
            this.action = action
            operators = primaryKeyValues
            countDownLatch.countDown()
        }
    }
}
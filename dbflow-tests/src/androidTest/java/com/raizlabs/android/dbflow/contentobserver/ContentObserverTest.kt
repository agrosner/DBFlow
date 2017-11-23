package com.raizlabs.android.dbflow.contentobserver

import android.net.Uri
import com.raizlabs.android.dbflow.BaseInstrumentedUnitTest
import com.raizlabs.android.dbflow.DemoApp
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.contentobserver.User_Table.id
import com.raizlabs.android.dbflow.contentobserver.User_Table.name
import com.raizlabs.android.dbflow.runtime.FlowContentObserver
import com.raizlabs.android.dbflow.sql.getNotificationUri
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.SQLOperator
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.delete
import com.raizlabs.android.dbflow.structure.insert
import com.raizlabs.android.dbflow.structure.save
import com.raizlabs.android.dbflow.structure.update
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class ContentObserverTest : BaseInstrumentedUnitTest() {

    private lateinit var user: User

    @Before
    fun setupUser() {
        Delete.table(User::class.java)
        user = User(5, "Something", 55)
    }

    @Test
    fun testSpecificUris() {
        val conditionGroup = FlowManager.getModelAdapter(User::class.java)
                .getPrimaryConditionClause(user)
        val uri = getNotificationUri(User::class.java, BaseModel.Action.DELETE,
                conditionGroup.conditions.toTypedArray())

        assertEquals(uri.authority, FlowManager.getTableName(User::class.java))
        assertEquals(uri.fragment, BaseModel.Action.DELETE.name)
        assertEquals(Uri.decode(uri.getQueryParameter(Uri.encode(id.query))), "5")
        assertEquals(Uri.decode(uri.getQueryParameter(Uri.encode(name.query))), "Something")
    }

    @Test
    fun testSpecificUrlInsert() {
        assertProperConditions(BaseModel.Action.INSERT, { it.insert() })
    }

    @Test
    fun testSpecificUrlUpdate() {
        user.save()
        assertProperConditions(BaseModel.Action.UPDATE, { it.apply { age = 56 }.update() })

    }

    @Test
    fun testSpecificUrlSave() {
        // insert on SAVE
        assertProperConditions(BaseModel.Action.INSERT, { it.apply { age = 57 }.save() })
    }

    @Test
    fun testSpecificUrlDelete() {
        user.save()
        assertProperConditions(BaseModel.Action.DELETE, { it.delete() })
    }

    private fun assertProperConditions(action: BaseModel.Action, userFunc: (User) -> Unit) {
        val contentObserver = FlowContentObserver()
        val countDownLatch = CountDownLatch(1)
        val mockOnModelStateChangedListener = MockOnModelStateChangedListener(countDownLatch)
        contentObserver.addModelChangeListener(mockOnModelStateChangedListener)
        contentObserver.registerForContentChanges(DemoApp.context, User::class.java)

        userFunc(user)
        countDownLatch.await()

        val ops = mockOnModelStateChangedListener.operators!!
        assertTrue(ops.size == 2)
        assertEquals(ops[0].columnName(), id.query)
        assertEquals(ops[1].columnName(), name.query)
        assertEquals(ops[1].value(), "Something")
        assertEquals(ops[0].value(), "5")
        assertEquals(action, mockOnModelStateChangedListener.action)

        contentObserver.removeModelChangeListener(mockOnModelStateChangedListener)
        contentObserver.unregisterForContentChanges(DemoApp.context)
    }

    class MockOnModelStateChangedListener(val countDownLatch: CountDownLatch) : FlowContentObserver.OnModelStateChangedListener {

        var action: BaseModel.Action? = null
        var operators: Array<SQLOperator>? = null


        override fun onModelStateChanged(table: Class<*>?, action: BaseModel.Action,
                                         primaryKeyValues: Array<SQLOperator>) {
            this.action = action
            operators = primaryKeyValues
            countDownLatch.countDown()
        }
    }
}
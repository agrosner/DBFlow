package com.dbflow5.contentobserver

import android.net.Uri
import com.dbflow5.BaseUnitTest
import com.dbflow5.DemoApp
import com.dbflow5.TABLE_QUERY_PARAM
import com.dbflow5.config.databaseForTable
import com.dbflow5.config.modelAdapter
import com.dbflow5.config.tableName
import com.dbflow5.contentobserver.User_Table.id
import com.dbflow5.contentobserver.User_Table.name
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.getNotificationUri
import com.dbflow5.query.SQLOperator
import com.dbflow5.query.delete
import com.dbflow5.runtime.FlowContentObserver
import com.dbflow5.structure.ChangeAction
import com.dbflow5.structure.delete
import com.dbflow5.structure.insert
import com.dbflow5.structure.save
import com.dbflow5.structure.update
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import java.util.concurrent.CountDownLatch

class ContentObserverTest : BaseUnitTest() {

    val contentUri = "com.grosner.content"

    private lateinit var user: User

    @Before
    fun setupUser() {
        databaseForTable<User> { dbFlowDatabase ->
            delete<User>().execute(dbFlowDatabase)
        }
        user = User(5, "Something", 55)
    }

    @Test
    fun testSpecificUris() {
        val conditionGroup = User::class.modelAdapter
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
        //assertProperConditions(ChangeAction.INSERT) { user, db -> user.insert(db) }
    }

    @Test
    fun testSpecificUrlUpdate() {
       // assertProperConditions(ChangeAction.UPDATE) { user, db -> user.apply { age = 56 }.update(db) }

    }

    @Test
    fun testSpecificUrlSave() {
        // insert on SAVE
        //assertProperConditions(ChangeAction.INSERT) { user, db -> user.apply { age = 57 }.save(db) }
    }

    @Test
    fun testSpecificUrlDelete() {
       // user.save(databaseForTable<User>())
       // assertProperConditions(ChangeAction.DELETE) { user, db -> user.delete(db) }
    }

    private fun assertProperConditions(action: ChangeAction, userFunc: (User, DatabaseWrapper) -> Unit) {
        val contentObserver = FlowContentObserver(contentUri)
        val countDownLatch = CountDownLatch(1)
        val mockOnModelStateChangedListener = MockOnModelStateChangedListener(countDownLatch)
        contentObserver.addModelChangeListener(mockOnModelStateChangedListener)
        contentObserver.registerForContentChanges(DemoApp.context, User::class.java)

        userFunc(user, databaseForTable<User>())
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
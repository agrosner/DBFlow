package com.raizlabs.android.dbflow.contentobserver

import android.net.Uri
import com.raizlabs.android.dbflow.DemoApp
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.kotlinextensions.delete
import com.raizlabs.android.dbflow.kotlinextensions.insert
import com.raizlabs.android.dbflow.kotlinextensions.save
import com.raizlabs.android.dbflow.kotlinextensions.update
import com.raizlabs.android.dbflow.runtime.FlowContentObserver
import com.raizlabs.android.dbflow.sql.SqlUtils
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.SQLOperator
import com.raizlabs.android.dbflow.structure.BaseModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.Callable
import java.util.concurrent.CountDownLatch

class ContentObserverTest {

    @Test
    fun testSpecificUris() {

        val model = User(5, "Something", 55)
        val conditionGroup = FlowManager.getModelAdapter(User::class.java)
            .getPrimaryConditionClause(model)
        val uri = SqlUtils.getNotificationUri(User::class.java, BaseModel.Action.DELETE,
            conditionGroup.conditions.toTypedArray())

        assertEquals(uri.authority, FlowManager.getTableName(User::class.java))
        assertEquals(uri.fragment, BaseModel.Action.DELETE.name)
        assertEquals(Uri.decode(uri.getQueryParameter(Uri.encode(User_Table.id.query))), "5")
        assertEquals(Uri.decode(uri.getQueryParameter(Uri.encode(User_Table.name.query))), "Something")
    }

    @Test
    fun testSpecificUrlNotifications() {

        Delete.table(User::class.java)

        val countDownLatch = CountDownLatch(1)

        val contentObserver = FlowContentObserver()
        val mockOnModelStateChangedListener = MockOnModelStateChangedListener(countDownLatch)
        contentObserver.addModelChangeListener(mockOnModelStateChangedListener)
        contentObserver.registerForContentChanges(DemoApp.context, User::class.java)
        val model = User(3, "Something", 55)
        model.insert()
        countDownLatch.await()

        // inserting
        assertTrue(mockOnModelStateChangedListener.conditions[0].size == 2)
        val conditions1 = mockOnModelStateChangedListener.conditions[0]
        assertEquals(conditions1[0].columnName(), User_Table.name.query)
        assertEquals(conditions1[1].columnName(), User_Table.id.query)
        assertEquals(conditions1[0].value(), "Something")
        assertEquals(conditions1[1].value(), "3")

        model.age = 56
        model.update()
        countDownLatch.await()

        // updating
        assertTrue(mockOnModelStateChangedListener.conditions[1].size == 2)
        val conditions2 = mockOnModelStateChangedListener.conditions[1]
        assertEquals(conditions2[0].columnName(), User_Table.name.query)
        assertEquals(conditions2[1].columnName(), User_Table.id.query)
        assertEquals(conditions2[0].value(), "Something")
        assertEquals(conditions2[1].value(), "3")

        model.age = 57
        model.save()
        countDownLatch.await()

        // save
        assertTrue(mockOnModelStateChangedListener.conditions[2].size == 2)
        val conditions3 = mockOnModelStateChangedListener.conditions[2]
        assertEquals(conditions3[0].columnName(), User_Table.name.query)
        assertEquals(conditions3[1].columnName(), User_Table.id.query)
        assertEquals(conditions3[0].value(), "Something")
        assertEquals(conditions3[1].value(), "3")


        model.delete()
        countDownLatch.await()

        // delete
        assertTrue(mockOnModelStateChangedListener.conditions[3].size == 2)
        val conditions4 = mockOnModelStateChangedListener.conditions[3]
        assertEquals(conditions4[0].columnName(), User_Table.name.query)
        assertEquals(conditions4[1].columnName(), User_Table.id.query)
        assertEquals(conditions4[0].value(), "Something")
        assertEquals(conditions4[1].value(), "3")

    }

    class MockOnModelStateChangedListener(val countDownLatch: CountDownLatch) : FlowContentObserver.OnModelStateChangedListener {

        val methodCalled = arrayOf(false, false, false, false)
        val methodCalls: Array<Callable<Boolean>?> = arrayOfNulls(4)
        val conditions: Array<Array<SQLOperator>> = arrayOf()

        init {
            for (i in methodCalls.indices) {
                val finalI = i
                methodCalls[i] = Callable { methodCalled[finalI] }
            }
        }

        override fun onModelStateChanged(table: Class<*>?, action: BaseModel.Action,
                                         primaryKeyValues: Array<SQLOperator>) {
            when (action) {
                BaseModel.Action.CHANGE -> methodCalls.indices.forEach { i ->
                    try {
                        methodCalled[i] = true
                        conditions[i] = primaryKeyValues
                        methodCalls[i]!!.call()
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
                BaseModel.Action.SAVE -> try {
                    conditions[2] = primaryKeyValues
                    methodCalled[2] = true
                    methodCalls[2]!!.call()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                BaseModel.Action.DELETE -> try {
                    conditions[3] = primaryKeyValues
                    methodCalled[3] = true
                    methodCalls[3]!!.call()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                BaseModel.Action.INSERT -> try {
                    conditions[0] = primaryKeyValues
                    methodCalled[0] = true
                    methodCalls[0]!!.call()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

                BaseModel.Action.UPDATE -> try {
                    conditions[1] = primaryKeyValues
                    methodCalled[1] = true
                    methodCalls[1]!!.call()
                } catch (e: Exception) {
                    e.printStackTrace()
                }

            }
            countDownLatch.countDown()
        }
    }
}
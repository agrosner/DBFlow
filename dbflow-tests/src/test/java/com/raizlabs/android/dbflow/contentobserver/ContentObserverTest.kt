package com.raizlabs.android.dbflow.contentobserver

import android.net.Uri

import com.jayway.awaitility.Duration
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.runtime.FlowContentObserver
import com.raizlabs.android.dbflow.sql.SqlUtils
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.FlowTestCase
import com.raizlabs.android.dbflow.structure.TestModel1
import com.raizlabs.android.dbflow.structure.TestModel1_Table

import org.junit.Test
import org.robolectric.RuntimeEnvironment

import java.util.concurrent.Callable
import java.util.concurrent.TimeUnit

import com.jayway.awaitility.Awaitility.await
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue

class ContentObserverTest : FlowTestCase() {

    @Test
    fun testNotificationUri() {

        val notificationUri = SqlUtils.getNotificationUri(TestModel1::class.java, BaseModel.Action.SAVE, TestModel1_Table.name.query, "this is a %test")
        assertEquals(notificationUri.authority, FlowManager.getTableName(TestModel1::class.java))
        assertEquals(notificationUri.fragment, BaseModel.Action.SAVE.name)
        assertEquals(Uri.decode(notificationUri.getQueryParameter(Uri.encode(TestModel1_Table.name.query))), "this is a %test")
    }

    @Test
    fun testContentObserver() {
        Delete.table(TestModel1::class.java)

        val flowContentObserver = FlowContentObserver()
        flowContentObserver.registerForContentChanges(RuntimeEnvironment.application, TestModel1::class.java)


        val onModelStateChangedListener = MockOnModelStateChangedListener()

        flowContentObserver.addModelChangeListener(onModelStateChangedListener)

        val testModel1 = TestModel1()
        testModel1.name = "Name"

        for (i in 0..onModelStateChangedListener.methodCalls.size - 1) {
            if (i == 0) {
                testModel1.insert()
            } else if (i == 1) {
                testModel1.update()
            } else if (i == 2) {
                testModel1.save()
            } else {
                testModel1.delete()
            }
            await().timeout(Duration.FIVE_SECONDS).until<Any>(onModelStateChangedListener.methodCalls[i])
            assertTrue(onModelStateChangedListener.methodcalled[i])
        }

        flowContentObserver.removeModelChangeListener(onModelStateChangedListener)
        flowContentObserver.unregisterForContentChanges(RuntimeEnvironment.application)

        Delete.table(TestModel1::class.java)
    }

    @Test
    fun testContentObserverTransaction() {
        Delete.table(TestModel1::class.java)

        val flowContentObserver = FlowContentObserver()
        flowContentObserver.registerForContentChanges(context, TestModel1::class.java)
        flowContentObserver.setNotifyAllUris(true)

        val mockOnModelStateChangedListener = MockOnModelStateChangedListener()
        flowContentObserver.addModelChangeListener(mockOnModelStateChangedListener)

        val testModel1 = TestModel1()
        testModel1.name = "Name"

        flowContentObserver.beginTransaction()

        testModel1.insert()
        testModel1.update()
        testModel1.save()
        testModel1.delete()

        await().atMost(Duration.FIVE_SECONDS).ignoreExceptions().until<Any> { true }

        // not saved
        assertFalse(mockOnModelStateChangedListener.methodcalled[0])

        // not deleted
        assertFalse(mockOnModelStateChangedListener.methodcalled[1])

        // not inserted
        assertFalse(mockOnModelStateChangedListener.methodcalled[2])

        // not updated
        assertFalse(mockOnModelStateChangedListener.methodcalled[3])

        flowContentObserver.endTransactionAndNotify()

        await().atMost(5, TimeUnit.SECONDS).until<Any>(mockOnModelStateChangedListener.methodCalls[0])
        assertTrue(mockOnModelStateChangedListener.methodcalled[0])

        await().atMost(5, TimeUnit.SECONDS).until<Any>(mockOnModelStateChangedListener.methodCalls[1])
        assertTrue(mockOnModelStateChangedListener.methodcalled[1])

        await().atMost(5, TimeUnit.SECONDS).until<Any>(mockOnModelStateChangedListener.methodCalls[2])
        assertTrue(mockOnModelStateChangedListener.methodcalled[2])

        await().atMost(5, TimeUnit.SECONDS).until<Any>(mockOnModelStateChangedListener.methodCalls[3])
        assertTrue(mockOnModelStateChangedListener.methodcalled[3])

        flowContentObserver.removeModelChangeListener(mockOnModelStateChangedListener)

        flowContentObserver.unregisterForContentChanges(RuntimeEnvironment.application)

        Delete.table(TestModel1::class.java)
    }
}

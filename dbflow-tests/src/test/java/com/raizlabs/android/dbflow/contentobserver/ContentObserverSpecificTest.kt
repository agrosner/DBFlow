package com.raizlabs.android.dbflow.contentobserver

import android.net.Uri
import com.jayway.awaitility.Awaitility.await
import com.jayway.awaitility.Duration
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.runtime.FlowContentObserver
import com.raizlabs.android.dbflow.sql.SqlUtils
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.FlowTestCase
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.robolectric.RuntimeEnvironment
import java.util.concurrent.TimeUnit

class ContentObserverSpecificTest : FlowTestCase() {

    @Test
    fun testSpecificUris() {

        val model = ContentObserverModel()
        model.id = 5
        model.name = "Something"
        model.somethingElse = "SomethingElse"
        val conditionGroup = FlowManager.getModelAdapter(ContentObserverModel::class.java)
            .getPrimaryConditionClause(model)
        val uri = SqlUtils.getNotificationUri(ContentObserverModel::class.java, BaseModel.Action.DELETE,
            conditionGroup.conditions.toTypedArray())

        assertEquals(uri.authority, FlowManager.getTableName(ContentObserverModel::class.java))
        assertEquals(uri.fragment, BaseModel.Action.DELETE.name)
        assertEquals(Uri.decode(uri.getQueryParameter(Uri.encode(ContentObserverModel_Table.id.query))), "5")
        assertEquals(Uri.decode(uri.getQueryParameter(Uri.encode(ContentObserverModel_Table.name.query))), "Something")
    }

    @Test
    fun testSpecificUrlNotifications() {

        Delete.table(ContentObserverModel::class.java)

        val contentObserver = FlowContentObserver()
        val mockOnModelStateChangedListener = MockOnModelStateChangedListener()
        contentObserver.addModelChangeListener(mockOnModelStateChangedListener)
        contentObserver.registerForContentChanges(RuntimeEnvironment.application, ContentObserverModel::class.java)
        val model = ContentObserverModel()
        model.id = 3
        model.name = "Something"
        model.somethingElse = "SomethingElse"
        model.insert()

        await().atMost(Duration.FIVE_SECONDS).until<Any>(mockOnModelStateChangedListener.methodCalls[0])

        // inserting
        assertTrue(mockOnModelStateChangedListener.conditions[0].size == 2)
        val conditions1 = mockOnModelStateChangedListener.conditions[0]
        assertEquals(conditions1[0].columnName(), ContentObserverModel_Table.name.query)
        assertEquals(conditions1[1].columnName(), ContentObserverModel_Table.id.query)
        assertEquals(conditions1[0].value(), "Something")
        assertEquals(conditions1[1].value(), "3")

        model.somethingElse = "SomethingElse2"
        model.update()

        await().atMost(Duration.FIVE_SECONDS).until<Any>(mockOnModelStateChangedListener.methodCalls[1])

        // updating
        assertTrue(mockOnModelStateChangedListener.conditions[1].size == 2)
        val conditions2 = mockOnModelStateChangedListener.conditions[1]
        assertEquals(conditions2[0].columnName(), ContentObserverModel_Table.name.query)
        assertEquals(conditions2[1].columnName(), ContentObserverModel_Table.id.query)
        assertEquals(conditions2[0].value(), "Something")
        assertEquals(conditions2[1].value(), "3")

        model.somethingElse = "Something3"
        model.save()
        await().atMost(5, TimeUnit.SECONDS).until<Any>(mockOnModelStateChangedListener.methodCalls[2])

        // save
        assertTrue(mockOnModelStateChangedListener.conditions[2].size == 2)
        val conditions3 = mockOnModelStateChangedListener.conditions[2]
        assertEquals(conditions3[0].columnName(), ContentObserverModel_Table.name.query)
        assertEquals(conditions3[1].columnName(), ContentObserverModel_Table.id.query)
        assertEquals(conditions3[0].value(), "Something")
        assertEquals(conditions3[1].value(), "3")


        model.delete()
        await().atMost(5, TimeUnit.SECONDS).until<Any>(mockOnModelStateChangedListener.methodCalls[3])

        // delete
        assertTrue(mockOnModelStateChangedListener.conditions[3].size == 2)
        val conditions4 = mockOnModelStateChangedListener.conditions[3]
        assertEquals(conditions4[0].columnName(), ContentObserverModel_Table.name.query)
        assertEquals(conditions4[1].columnName(), ContentObserverModel_Table.id.query)
        assertEquals(conditions4[0].value(), "Something")
        assertEquals(conditions4[1].value(), "3")

    }
}

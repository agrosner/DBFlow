package com.raizlabs.android.dbflow.test.sql

import android.database.sqlite.SQLiteException

import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.test.FlowTestCase

import org.junit.Before
import org.junit.Test

import java.util.concurrent.atomic.AtomicLong

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.fail

/**
 * Description: Test to ensure that passing null to non-null fields does not cause a NPE and that it
 * will fail.
 */
class BoxedValueTest : FlowTestCase() {
    private var testObject: BoxedModel? = null

    @Before
    @Throws(Exception::class)
    fun setUp() {
        testObject = BoxedModel()
        testObject!!.id = SEQUENCE_ID.andIncrement
    }

    @Test
    fun testBoxedValues_nullId() {
        testObject!!.id = null
        assertCannotSaveModel()
    }

    @Test
    fun testBoxedValues_integerFieldNotNull() {
        testObject!!.integerFieldNotNull = null
        assertCannotSaveModel()
    }

    @Test
    fun testBoxedValues_integerField() {
        testObject!!.integerField = null
        assertCanSaveModel()
        loadModel()
        assertNull(testObject!!.integerField)
    }

    @Test
    fun testBoxedValues_stringFieldNotNull() {
        testObject!!.stringFieldNotNull = null
        assertCannotSaveModel()
    }

    @Test
    fun testBoxedValues_stringField() {
        testObject!!.stringField = null
        assertCanSaveModel()
        loadModel()
        assertNull(testObject!!.stringField)
    }

    private fun loadModel() {
        testObject = Select()
                .from(BoxedModel::class.java)
                .where(BoxedModel_Table.id.eq(testObject!!.id))
                .querySingle()
    }

    private fun assertCannotSaveModel() {
        try {
            testObject!!.save()
            fail("Was able to save model")
        } catch (s: SQLiteException) {
            // not null should fail
            assertEquals(s.message, "Cannot execute for last inserted row ID")
        }

    }

    private fun assertCanSaveModel() {
        try {
            testObject!!.save()
        } catch (s: SQLiteException) {
            s.printStackTrace(System.err)
            fail("Was unable to save model: " + s.message)
        }

    }

    companion object {

        private val SEQUENCE_ID = AtomicLong()
    }

}

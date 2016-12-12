package com.raizlabs.android.dbflow.test.sql

import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.test.FlowTestCase

import org.junit.Test

class DefaultModelTest : FlowTestCase() {

    @Test
    fun testDefaultModel() {
        Delete.table(DefaultModel::class.java)
        val defaultModel = DefaultModel()
        defaultModel.name = "Test"
        defaultModel.save()
    }
}

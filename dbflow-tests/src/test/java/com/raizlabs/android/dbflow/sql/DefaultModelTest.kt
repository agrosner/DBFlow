package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.FlowTestCase

import org.junit.Test

class DefaultModelTest : FlowTestCase() {

    @Test
    fun testDefaultModel() {
        Delete.table(com.raizlabs.android.dbflow.sql.DefaultModel::class.java)
        val defaultModel = com.raizlabs.android.dbflow.sql.DefaultModel()
        defaultModel.name = "Test"
        defaultModel.save()
    }
}

package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.assertEquals
import com.raizlabs.android.dbflow.config.database
import com.raizlabs.android.dbflow.models.java.JavaModelView
import org.junit.Test

class ModelViewTest : BaseUnitTest() {

    @Test
    fun validateModelViewQuery() = database(TestDatabase::class) {
        assertEquals("SELECT `id` AS `authorId`,`first_name` || ' ' || `last_name` AS `authorName` FROM `Author`",
                AuthorView.getQuery(this))
    }

    @Test
    fun validateJavaModelViewQuery() = database(TestDatabase::class) {
        assertEquals("SELECT `first_name` AS `firstName`,`id` AS `id`", JavaModelView.getQuery(this))
    }
}
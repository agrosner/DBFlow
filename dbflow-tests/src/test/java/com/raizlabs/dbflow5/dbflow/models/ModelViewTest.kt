package com.raizlabs.dbflow5.dbflow.models

import com.raizlabs.dbflow5.dbflow.BaseUnitTest
import com.raizlabs.dbflow5.dbflow.TestDatabase
import com.raizlabs.dbflow5.dbflow.assertEquals
import com.raizlabs.dbflow5.config.database
import com.raizlabs.dbflow5.dbflow.models.java.JavaModelView
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
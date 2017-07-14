package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.assertEquals
import com.raizlabs.android.dbflow.models.java.JavaModelView
import org.junit.Test

class ModelViewTest : BaseUnitTest() {

    @Test
    fun validateModelViewQuery() {
        assertEquals("SELECT `id` AS `authorId`,`first_name` || ' ' || `last_name` AS `authorName` FROM `Author`",
            AuthorView.query)
    }

    @Test
    fun validateJavaModelViewQuery() {
        assertEquals("SELECT `first_name` AS `firstName`,`id` AS `id`", JavaModelView.QUERY)
    }
}
package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.assertEquals
import com.dbflow5.models.java.JavaModelView
import org.junit.Test

class ModelViewTest : BaseUnitTest() {

    @Test
    fun validateModelViewQuery() {
        "SELECT `id` AS `authorId`,`first_name` || ' ' || `last_name` AS `authorName` FROM `Author`"
                .assertEquals(AuthorView.getQuery())
    }

    @Test
    fun validateJavaModelViewQuery() {
        "SELECT `first_name` AS `firstName`,`id` AS `id`".assertEquals(JavaModelView.getQuery())
    }
}
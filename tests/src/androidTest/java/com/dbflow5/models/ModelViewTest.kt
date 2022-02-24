package com.dbflow5.models

import com.dbflow5.TestDatabase_Database
import com.dbflow5.assertEquals
import com.dbflow5.authorAdapter
import com.dbflow5.models.java.JavaModelView
import com.dbflow5.test.DatabaseTestRule
import org.junit.Rule
import org.junit.Test

class ModelViewTest {

    @get:Rule
    val dbRule = DatabaseTestRule(TestDatabase_Database::create)

    @Test
    fun validateModelViewQuery() = dbRule {
        "SELECT `id` AS `authorId`, `first_name` || ' ' || `last_name` AS `authorName` FROM `Author`"
            .assertEquals(AuthorView.getQuery(authorAdapter))
    }

    @Test
    fun validateJavaModelViewQuery() = dbRule {
        "SELECT `first_name` AS `firstName`, `id` AS `id` FROM `Author`"
            .assertEquals(JavaModelView.getQuery(authorAdapter))
    }
}
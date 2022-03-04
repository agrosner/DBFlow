package com.dbflow5.models

import com.dbflow5.TestDatabase_Database
import com.dbflow5.assertEquals
import com.dbflow5.authorViewAdapter
import com.dbflow5.javaModelViewAdapter
import com.dbflow5.test.DatabaseTestRule
import org.junit.Rule
import org.junit.Test

class ModelViewTest {

    
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateModelViewQuery() = dbRule {
        "SELECT `id` AS `authorId`, `first_name` || ' ' || `last_name` AS `authorName` FROM `Author`"
            .assertEquals(authorViewAdapter.creationSQL)
    }

    @Test
    fun validateJavaModelViewQuery() = dbRule {
        "SELECT `first_name` AS `firstName`, `id` AS `id` FROM `Author`"
            .assertEquals(javaModelViewAdapter.creationSQL)
    }
}
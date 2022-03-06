package com.dbflow5.test.usecases

import com.dbflow5.query.select
import com.dbflow5.test.Author
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.assertEquals
import kotlin.test.Test
import kotlin.test.assertEquals

class ModelViewTest {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateModelViewQuery() = dbRule {
        ("CREATE VIEW `AuthorView` " +
            "AS " +
            "SELECT " +
            "`id` AS `authorId`, " +
            "`first_name` || ' ' || `last_name` AS `authorName` " +
            "FROM `Author`")
            .assertEquals(authorViewAdapter.creationSQL)
    }

    @Test
    fun validateCanGetView() = dbRule.runTest {
        val author = Author(
            id = 1,
            firstName = "Andrew",
            lastName = "Grosner"
        ).run { authorAdapter.save(this) }

        val authorView = authorViewAdapter.select().single()

        assertEquals("${author.firstName} ${author.lastName}", authorView.authorName)
    }
}

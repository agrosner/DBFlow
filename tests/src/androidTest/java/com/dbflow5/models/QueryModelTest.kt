package com.dbflow5.models

import com.dbflow5.TestDatabase_Database
import com.dbflow5.authorAdapter
import com.dbflow5.authorNameQuery
import com.dbflow5.blogAdapter
import com.dbflow5.query.innerJoin
import com.dbflow5.query.select
import com.dbflow5.test.DatabaseTestRule
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

/**
 * Description: Tests to ensure we can load a Query model from the DB
 */
class QueryModelTest {

    @get:Rule
    val dbRule = DatabaseTestRule(TestDatabase_Database::create)

    @Test
    fun testCanLoadAuthorBlogs() = dbRule.runBlockingTest {
        val authorModel = Author(0, "Andrew", "Grosner")
        authorAdapter.save(authorModel)
        val blogModel = Blog(0, "My First Blog", authorModel)
        blogAdapter.save(blogModel)

        assert(authorAdapter.exists(authorModel))
        assert(blogAdapter.exists(blogModel))

        val result: AuthorNameQuery = (blogAdapter.select(
            Blog_Table.name.withTable().`as`("blogName"),
            Blog_Table.id.withTable().`as`("authorId"),
            Blog_Table.id.withTable().`as`("blogId")
        ) innerJoin
            authorAdapter on (Blog_Table.author_id.withTable() eq Blog_Table.id.withTable()))
            .single(authorNameQuery)
        assertEquals(authorModel.id, result.authorId)
        assertEquals(blogModel.id, result.blogId)
    }
}

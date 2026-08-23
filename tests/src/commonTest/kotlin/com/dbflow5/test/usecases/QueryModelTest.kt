package com.dbflow5.test.usecases

import com.dbflow5.query.innerJoin
import com.dbflow5.query.select
import com.dbflow5.test.Author
import com.dbflow5.test.AuthorNameQuery
import com.dbflow5.test.Blog
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.TestRule
import com.dbflow5.test.author_id
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

/**
 * Description: Tests to ensure we can load a Query model from the DB
 */
class QueryModelTest: TestRule() {

    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun testCanLoadAuthorBlogs() = dbRule.runTest {
        val authorModel = Author(0, "Andrew", "Grosner")
            .run { authorAdapter.save(this) }
        val blogModel = Blog(
            id = 0,
            name = "My First Blog",
            author = authorModel,
        ).run { blogAdapter.save(this) }
        assertTrue(authorAdapter.exists(authorModel))
        assertTrue(blogAdapter.exists(blogModel))

        val result: AuthorNameQuery = (blogAdapter.select(
            Blog.name.withTable().`as`("blogName"),
            Blog.id.withTable().`as`("authorId"),
            Blog.id.withTable().`as`("blogId")
        ) innerJoin
            authorAdapter on (Blog.author_id.withTable() eq Blog.id.withTable()))
            .single(authorNameQueryAdapter)
        assertEquals(authorModel.id, result.authorId)
        assertEquals(blogModel.id, result.blogId)
    }
}

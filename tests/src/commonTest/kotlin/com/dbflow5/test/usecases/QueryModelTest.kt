package com.dbflow5.test.usecases

import com.dbflow5.query.innerJoin
import com.dbflow5.query.select
import com.dbflow5.test.Author
import com.dbflow5.test.AuthorNameQuery
import com.dbflow5.test.Blog
import com.dbflow5.test.Blog_Table
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.authorAdapter
import com.dbflow5.test.authorNameQueryAdapter
import com.dbflow5.test.blogAdapter
import kotlin.test.Test
import kotlin.test.assertEquals

/**
 * Description: Tests to ensure we can load a Query model from the DB
 */
class QueryModelTest {

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
        assert(authorAdapter.exists(authorModel))
        assert(blogAdapter.exists(blogModel))

        val result: AuthorNameQuery = (blogAdapter.select(
            Blog_Table.name.withTable().`as`("blogName"),
            Blog_Table.id.withTable().`as`("authorId"),
            Blog_Table.id.withTable().`as`("blogId")
        ) innerJoin
            authorAdapter on (Blog_Table.author_id.withTable() eq Blog_Table.id.withTable()))
            .single(authorNameQueryAdapter)
        assertEquals(authorModel.id, result.authorId)
        assertEquals(blogModel.id, result.blogId)
    }
}

package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.authorAdapter
import com.dbflow5.blogAdapter
import com.dbflow5.config.database
import com.dbflow5.config.writableTransaction
import com.dbflow5.query.select
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description: Tests to ensure we can load a Query model from the DB
 */
class QueryModelTest : BaseUnitTest() {

    @Test
    fun testCanLoadAuthorBlogs() = runBlockingTest {
        database<TestDatabase>().writableTransaction {
            val authorModel = Author(0, "Andrew", "Grosner")
            authorAdapter.save(authorModel)
            val blogModel = Blog(0, "My First Blog", authorModel)
            blogAdapter.save(blogModel)

            assert(authorAdapter.exists(authorModel))
            assert(blogAdapter.exists(blogModel))

            val result: AuthorNameQuery = (select(
                Blog_Table.name.withTable().`as`("blogName"),
                Blog_Table.id.withTable().`as`("authorId"),
                Blog_Table.id.withTable().`as`("blogId")
            ) from Blog::class innerJoin
                Author::class on (Blog_Table.author_id.withTable() eq Blog_Table.id.withTable()))
                .requireCustomSingle(AuthorNameQuery::class)
            assertEquals(authorModel.id, result.authorId)
            assertEquals(blogModel.id, result.blogId)
        }
    }
}

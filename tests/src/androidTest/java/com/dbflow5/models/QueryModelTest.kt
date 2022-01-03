package com.dbflow5.models

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.query.select
import com.dbflow5.structure.exists
import com.dbflow5.structure.save
import kotlinx.coroutines.test.runBlockingTest
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Description: Tests to ensure we can load a Query model from the DB
 */
class QueryModelTest : BaseUnitTest() {

    @Test
    fun testCanLoadAuthorBlogs() = runBlockingTest {
        database<TestDatabase> { db ->
            val author = Author(0, "Andrew", "Grosner")
            author.save(db)
            val blog = Blog(0, "My First Blog", author)
            blog.save(db)

            assert(author.exists(db))
            assert(blog.exists(db))

            val result = (select(
                Blog_Table.name.withTable().`as`("blogName"),
                Blog_Table.id.withTable().`as`("authorId"),
                Blog_Table.id.withTable().`as`("blogId")
            ) from Blog::class innerJoin
                Author::class on (Blog_Table.author_id.withTable() eq Blog_Table.id.withTable()))
                .queryCustomSingle(AuthorNameQuery::class.java, db)!!
            assertEquals(author.id, result.authorId)
            assertEquals(blog.id, result.blogId)
        }
    }
}

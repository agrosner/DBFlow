package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.delete
import com.dbflow5.query.select
import com.dbflow5.structure.save
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DeleteTest : BaseUnitTest() {

    @Test
    fun validateQuery() {
        assertEquals("DELETE ", delete().query)
    }

    @Test
    fun validateDeletion() {
        database<TestDatabase> { db ->
            SimpleModel("name").save(db)
            delete<SimpleModel>().execute(db)
            assertFalse((select from SimpleModel::class).hasData(db))
        }
    }

    @Test
    fun validateDeletionWithQuery() {
        database<TestDatabase> { db ->
            SimpleModel("name").save(db)
            SimpleModel("another name").save(db)

            val where = delete<SimpleModel>().where(SimpleModel_Table.name.`is`("name"))
            assertEquals("DELETE FROM `SimpleModel` WHERE `name`='name'", where.query.trim())
            where.execute(db)

            assertEquals(1, (select from SimpleModel::class).queryList(db).size)
        }
    }
}
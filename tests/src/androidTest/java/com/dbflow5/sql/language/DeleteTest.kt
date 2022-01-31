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
        database<TestDatabase> {
            SimpleModel("name").save(this.db)
            delete<SimpleModel>().execute(this.db)
            assertFalse((select from SimpleModel::class).hasData(this.db))
        }
    }

    @Test
    fun validateDeletionWithQuery() {
        database<TestDatabase> {
            SimpleModel("name").save(this.db)
            SimpleModel("another name").save(this.db)

            val where = delete<SimpleModel>().where(SimpleModel_Table.name.`is`("name"))
            assertEquals("DELETE FROM `SimpleModel` WHERE `name`='name'", where.query.trim())
            where.execute(this.db)

            assertEquals(1, (select from SimpleModel::class).queryList(this.db).size)
        }
    }
}
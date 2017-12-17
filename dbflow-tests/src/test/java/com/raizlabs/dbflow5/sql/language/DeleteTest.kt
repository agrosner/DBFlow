package com.raizlabs.dbflow5.sql.language

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.models.SimpleModel_Table
import com.raizlabs.dbflow5.query.delete
import com.raizlabs.dbflow5.query.list
import com.raizlabs.dbflow5.query.select
import com.raizlabs.dbflow5.structure.save
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DeleteTest : BaseUnitTest() {

    @Test
    fun validateQuery() {
        databaseForTable<SimpleModel> {
            assertEquals("DELETE ", delete().query)
        }
    }

    @Test
    fun validateDeletion() {
        databaseForTable<SimpleModel> {
            SimpleModel("name").save()
            delete<SimpleModel>().execute(this)
            assertFalse((select from SimpleModel::class).hasData(this))
        }
    }

    @Test
    fun validateDeletionWithQuery() {
        databaseForTable<SimpleModel> {
            SimpleModel("name").save()
            SimpleModel("another name").save()

            val where = delete<SimpleModel>().where(SimpleModel_Table.name.`is`("name"))
            assertEquals("DELETE FROM `SimpleModel` WHERE `name`='name'", where.query.trim())
            where.execute(this)

            assertEquals(1, (select from SimpleModel::class).list.size)
        }
    }
}
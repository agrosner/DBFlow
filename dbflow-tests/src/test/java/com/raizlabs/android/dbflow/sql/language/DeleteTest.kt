package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.kotlinextensions.delete
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.list
import com.raizlabs.android.dbflow.kotlinextensions.save
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test

class DeleteTest : BaseUnitTest() {

    @Test
    fun validateQuery() {
        assertEquals("DELETE ", Delete().query)
    }

    @Test
    fun validateDeletion() {
        SimpleModel("name").save()
        delete<SimpleModel>().execute()
        assertFalse((select from SimpleModel::class).hasData())
    }

    @Test
    fun validateDeletionWithQuery() {
        SimpleModel("name").save()
        SimpleModel("another name").save()

        val where = delete<SimpleModel>().where(SimpleModel_Table.name.`is`("name"))
        assertEquals("DELETE FROM `SimpleModel` WHERE `name`='name'", where.query.trim())
        where.execute()

        assertEquals(1, (select from SimpleModel::class).list.size)
    }
}
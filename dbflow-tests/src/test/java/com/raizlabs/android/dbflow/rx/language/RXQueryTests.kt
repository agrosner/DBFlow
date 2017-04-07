package com.raizlabs.android.dbflow.rx.language

import android.database.Cursor
import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.insert
import com.raizlabs.android.dbflow.kotlinextensions.save
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table.name
import com.raizlabs.android.dbflow.rx.kotlinextensions.rx
import com.raizlabs.android.dbflow.rx.kotlinextensions.rxBaseQueriable
import com.raizlabs.android.dbflow.sql.language.SQLite.selectCountOf
import com.raizlabs.android.dbflow.sql.language.property.Property
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RXQueryTests : BaseUnitTest() {

    @Test
    fun testCanQuery() {
        SimpleModel("Name").save()

        var cursor: Cursor? = null
        (select from SimpleModel::class).rx()
            .query()
            .subscribe {
                cursor = it
            }

        assertEquals(1, cursor!!.count)
        cursor!!.close()
    }

    @Test
    fun testCanCompileStatement() {
        var databaseStatement: DatabaseStatement? = null
        (insert<SimpleModel>().columnValues(name.`is`("name")))
            .rxBaseQueriable().compileStatement()
            .subscribe {
                databaseStatement = it
            }
        assertNotNull(databaseStatement)
        databaseStatement!!.close()
    }

    @Test
    fun testCountMethod() {
        SimpleModel("name").save()
        SimpleModel("name2").save()
        var count = 0L
        (selectCountOf(Property.ALL_PROPERTY) from SimpleModel::class).rx()
            .count().subscribe {
            count = it
        }

        assertEquals(2, count)
    }

    @Test
    fun testInsertMethod() {
        var count = 0L
        (insert<SimpleModel>().columnValues(name.eq("name")))
            .rxBaseQueriable()
            .count()
            .subscribe {
                count = it
            }

        assertEquals(1, count)
    }

    @Test
    fun testExecuteUpdateDelete() {

    }
}
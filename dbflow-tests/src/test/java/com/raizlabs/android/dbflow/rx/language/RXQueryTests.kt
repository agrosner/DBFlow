package com.raizlabs.android.dbflow.rx.language

import android.database.Cursor
import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.config.writableDatabaseForTable
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table.name
import com.raizlabs.android.dbflow.sql.language.insert
import com.raizlabs.android.dbflow.sql.language.property.Property
import com.raizlabs.android.dbflow.sql.language.select
import com.raizlabs.android.dbflow.sql.language.selectCountOf
import com.raizlabs.android.dbflow.structure.database.DatabaseStatement
import com.raizlabs.android.dbflow.structure.save
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class RXQueryTests : BaseUnitTest() {

    @Test
    fun testCanQuery() {
        writableDatabaseForTable<SimpleModel> {
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
    }

    @Test
    fun testCanCompileStatement() {
        writableDatabaseForTable<SimpleModel> {
            var databaseStatement: DatabaseStatement? = null
            (insert<SimpleModel>().columnValues(name.`is`("name")))
                    .rxBaseQueriable().compileStatement()
                    .subscribe {
                        databaseStatement = it
                    }
            assertNotNull(databaseStatement)
            databaseStatement!!.close()
        }
    }

    @Test
    fun testCountMethod() {
        writableDatabaseForTable<SimpleModel> {
            SimpleModel("name").save()
            SimpleModel("name2").save()
            var count = 0L
            (selectCountOf(Property.ALL_PROPERTY) from SimpleModel::class).rx()
                    .longValue().subscribe {
                count = it
            }

            assertEquals(2, count)
        }
    }

    @Test
    fun testInsertMethod() {
        writableDatabaseForTable<SimpleModel> {
            var count = 0L
            (insert<SimpleModel>().columnValues(name.eq("name")))
                    .rxBaseQueriable()
                    .executeInsert()
                    .subscribe {
                        count = it
                    }

            assertEquals(1, count)
        }
    }

    @Test
    fun testExecuteUpdateDelete() {

    }
}
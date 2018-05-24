package com.raizlabs.dbflow5.rx2.query

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.database.DatabaseStatement
import com.raizlabs.dbflow5.database.FlowCursor
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.models.SimpleModel_Table.name
import com.raizlabs.dbflow5.query.insert
import com.raizlabs.dbflow5.query.property.Property
import com.raizlabs.dbflow5.query.select
import com.raizlabs.dbflow5.query.selectCountOf
import com.raizlabs.dbflow5.rx2.transaction.asMaybe
import com.raizlabs.dbflow5.rx2.transaction.asSingle
import com.raizlabs.dbflow5.structure.save
import org.junit.Assert.assertEquals
import org.junit.Test

class RXQueryTests : BaseUnitTest() {

    @Test
    fun testCanQuery() {
        SimpleModel("Name").save()

        var cursor: FlowCursor? = null
        databaseForTable<SimpleModel>()
            .beginTransactionAsync { (select from SimpleModel::class).cursor(it) }
            .asMaybe()
            .subscribe {
                cursor = it
            }

        assertEquals(1, cursor!!.count)
        cursor!!.close()
    }

    @Test
    fun testCanCompileStatement() {
        var databaseStatement: DatabaseStatement? = null
        databaseForTable<SimpleModel>()
            .beginTransactionAsync {
                insert<SimpleModel>().columnValues(name.`is`("name")).compileStatement(it)
            }.asSingle()
            .subscribe { statement ->
                databaseStatement = statement
            }
        databaseStatement!!.close()
    }

    @Test
    fun testCountMethod() {
        SimpleModel("name").save()
        SimpleModel("name2").save()
        var count = 0L
        databaseForTable<SimpleModel>()
            .beginTransactionAsync {
                (selectCountOf(Property.ALL_PROPERTY) from SimpleModel::class).longValue(it)
            }
            .asSingle()
            .subscribe { value ->
                count = value
            }

        assertEquals(2, count)
    }

    @Test
    fun testInsertMethod() {
        var count = 0L
        databaseForTable<SimpleModel>()
            .beginTransactionAsync {
                (insert<SimpleModel>().columnValues(name.eq("name"))).executeInsert(it)
            }.asSingle()
            .subscribe { c ->
                count = c
            }

        assertEquals(1, count)
    }

}
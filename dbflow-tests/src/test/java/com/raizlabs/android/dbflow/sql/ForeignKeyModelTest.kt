package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.annotation.Column
import com.raizlabs.android.dbflow.annotation.ForeignKey
import com.raizlabs.android.dbflow.annotation.ForeignKeyAction
import com.raizlabs.android.dbflow.annotation.PrimaryKey
import com.raizlabs.android.dbflow.annotation.Table
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.language.Delete
import com.raizlabs.android.dbflow.sql.language.Insert
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.FlowTestCase
import com.raizlabs.android.dbflow.TestDatabase
import com.raizlabs.android.dbflow.structure.TestModel1

import org.junit.Test

/**
 * Description:
 */
class ForeignKeyModelTest : FlowTestCase() {


    @Test
    fun testInsertAndUpdate() {

        Delete.table(FkParent::class.java)
        Delete.table(FkRelated::class.java)

        // Test insert
        val parent = FkParent()
        parent.related = FkRelated()
        parent.save()

        // Test update parent with new related
        parent.related = FkRelated()
        parent.save()
    }

    @Table(database = TestDatabase::class)
    class FkParent : BaseModel() {
        @Column
        @PrimaryKey(autoincrement = true)
        var id: Int = 0

        @Column
        @ForeignKey(onUpdate = ForeignKeyAction.CASCADE, onDelete = ForeignKeyAction.CASCADE, saveForeignKeyModel = true)
        var related: FkRelated? = null
    }

    @Table(database = TestDatabase::class)
    class FkRelated : BaseModel() {
        @Column
        @PrimaryKey(autoincrement = true)
        var id: Int = 0

        @Column
        var value: String? = null
    }
}

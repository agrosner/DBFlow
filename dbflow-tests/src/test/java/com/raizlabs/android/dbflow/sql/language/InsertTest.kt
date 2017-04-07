package com.raizlabs.android.dbflow.sql.language

import android.content.ContentValues
import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.TwoColumnModel
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.id
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.name
import com.raizlabs.android.dbflow.kotlinextensions.from
import com.raizlabs.android.dbflow.kotlinextensions.insert
import com.raizlabs.android.dbflow.kotlinextensions.select
import com.raizlabs.android.dbflow.kotlinextensions.set
import com.raizlabs.android.dbflow.sql.language.property.IProperty
import org.junit.Assert.assertEquals
import org.junit.Test

class InsertTest : BaseUnitTest() {

    @Test
    fun validateInsert() {
        assertEquals("INSERT INTO `SimpleModel` VALUES('something')", insert<SimpleModel>().values("something").query.trim())
    }

    @Test
    fun validateInsertOr() {
        assertEquals("INSERT OR REPLACE INTO `SimpleModel` VALUES('something')", insert<SimpleModel>().orReplace().values("something").query.trim())
        assertEquals("INSERT OR FAIL INTO `SimpleModel` VALUES('something')", insert<SimpleModel>().orFail().values("something").query.trim())
        assertEquals("INSERT OR IGNORE INTO `SimpleModel` VALUES('something')", insert<SimpleModel>().orIgnore().values("something").query.trim())
        assertEquals("INSERT OR REPLACE INTO `SimpleModel` VALUES('something')", insert<SimpleModel>().orReplace().values("something").query.trim())
        assertEquals("INSERT OR ROLLBACK INTO `SimpleModel` VALUES('something')", insert<SimpleModel>().orRollback().values("something").query.trim())
        assertEquals("INSERT OR ABORT INTO `SimpleModel` VALUES('something')", insert<SimpleModel>().orAbort().values("something").query.trim())
    }

    @Test
    fun validateInsertProjection() {
        assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
            insert<TwoColumnModel>().columns(name, id).values("name", "id").query.trim())
    }

    @Test
    fun validateSelect() {
        assertEquals("INSERT INTO `TwoColumnModel` SELECT * FROM `SimpleModel`",
            insert<TwoColumnModel>().select(select from SimpleModel::class).query.trim())
    }

    @Test
    fun validateColumns() {
        assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
            insert<TwoColumnModel>().asColumns().values("name", "id").query.trim())
        assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
            insert<TwoColumnModel>().columns("name", "id").values("name", "id").query.trim())
        assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
            insert<TwoColumnModel>().columns(arrayListOf(name, id) as List<IProperty<IProperty<*>>>).values("name", "id").query.trim())
    }

    @Test
    fun validateColumnValues() {
        assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 0)",
            insert<TwoColumnModel>().columnValues(name.eq("name"), id.eq(0)).query.trim())
        assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 0)",
            insert<TwoColumnModel>().columnValues(Operator.op<String>(NameAlias.builder("name").build()).eq("name"),
                id.eq(0)).query.trim())
        assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 0)",
            insert<TwoColumnModel>().columnValues(OperatorGroup.clause().andAll(name.eq("name"), id.eq(0))).query.trim())

        val contentValues = ContentValues()
        contentValues["name"] = "name"
        contentValues["id"] = 0.toInt()

        assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 0)",
            insert<TwoColumnModel>().columnValues(contentValues).query.trim())

    }
}
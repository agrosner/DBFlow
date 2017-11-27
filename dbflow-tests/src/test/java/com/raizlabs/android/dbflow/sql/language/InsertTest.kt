package com.raizlabs.android.dbflow.sql.language

import android.content.ContentValues
import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.config.databaseForTable
import com.raizlabs.android.dbflow.database.set
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.TwoColumnModel
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.id
import com.raizlabs.android.dbflow.models.TwoColumnModel_Table.name
import com.raizlabs.android.dbflow.query.NameAlias
import com.raizlabs.android.dbflow.query.Operator
import com.raizlabs.android.dbflow.query.OperatorGroup
import com.raizlabs.android.dbflow.query.insert
import com.raizlabs.android.dbflow.query.select
import org.junit.Assert.assertEquals
import org.junit.Test

class InsertTest : BaseUnitTest() {

    @Test
    fun validateInsert() {
        databaseForTable<SimpleModel> {
            assertEquals("INSERT INTO `SimpleModel` VALUES('something')",
                    insert<SimpleModel>().values("something").query.trim())
        }
    }

    @Test
    fun validateInsertOr() {
        databaseForTable<SimpleModel> {
            assertEquals("INSERT OR REPLACE INTO `SimpleModel` VALUES('something')",
                    insert<SimpleModel>().orReplace().values("something").query.trim())
            assertEquals("INSERT OR FAIL INTO `SimpleModel` VALUES('something')",
                    insert<SimpleModel>().orFail().values("something").query.trim())
            assertEquals("INSERT OR IGNORE INTO `SimpleModel` VALUES('something')",
                    insert<SimpleModel>().orIgnore().values("something").query.trim())
            assertEquals("INSERT OR REPLACE INTO `SimpleModel` VALUES('something')",
                    insert<SimpleModel>().orReplace().values("something").query.trim())
            assertEquals("INSERT OR ROLLBACK INTO `SimpleModel` VALUES('something')",
                    insert<SimpleModel>().orRollback().values("something").query.trim())
            assertEquals("INSERT OR ABORT INTO `SimpleModel` VALUES('something')",
                    insert<SimpleModel>().orAbort().values("something").query.trim())
        }
    }

    @Test
    fun validateInsertProjection() {
        databaseForTable<SimpleModel> {
            assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
                    insert<TwoColumnModel>().columns(name, id).values("name", "id").query.trim())
        }
    }

    @Test
    fun validateSelect() {
        databaseForTable<SimpleModel> {
            assertEquals("INSERT INTO `TwoColumnModel` SELECT * FROM `SimpleModel`",
                    insert<TwoColumnModel>().select(select from SimpleModel::class).query.trim())
        }
    }

    @Test
    fun validateColumns() {
        databaseForTable<SimpleModel> {
            assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
                    insert<TwoColumnModel>().asColumns().values("name", "id").query.trim())
            assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
                    insert<TwoColumnModel>().columns("name", "id").values("name", "id").query.trim())
            assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
                    insert<TwoColumnModel>().columns(listOf(name, id)).values("name", "id").query.trim())
        }
    }

    @Test
    fun validateColumnValues() {
        databaseForTable<SimpleModel> {
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
}
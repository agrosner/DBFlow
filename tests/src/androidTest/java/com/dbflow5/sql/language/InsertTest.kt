package com.dbflow5.sql.language

import android.content.ContentValues
import com.dbflow5.BaseUnitTest
import com.dbflow5.assertEquals
import com.dbflow5.config.databaseForTable
import com.dbflow5.database.set
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TwoColumnModel_Table.id
import com.dbflow5.models.TwoColumnModel_Table.name
import com.dbflow5.query.NameAlias
import com.dbflow5.query.Operator
import com.dbflow5.query.OperatorGroup
import com.dbflow5.query.insertInto
import com.dbflow5.query.select
import org.junit.Assert.assertEquals
import org.junit.Test

class InsertTest : BaseUnitTest() {

    @Test
    fun validateInsert() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            assertEquals("INSERT INTO `SimpleModel` VALUES('something')",
                insertInto<SimpleModel>().values("something").query.trim())
        }
    }

    @Test
    fun validateInsertOr() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            assertEquals("INSERT OR REPLACE INTO `SimpleModel` VALUES('something')",
                insertInto<SimpleModel>().orReplace().values("something").query.trim())
            assertEquals("INSERT OR FAIL INTO `SimpleModel` VALUES('something')",
                insertInto<SimpleModel>().orFail().values("something").query.trim())
            assertEquals("INSERT OR IGNORE INTO `SimpleModel` VALUES('something')",
                insertInto<SimpleModel>().orIgnore().values("something").query.trim())
            assertEquals("INSERT OR REPLACE INTO `SimpleModel` VALUES('something')",
                insertInto<SimpleModel>().orReplace().values("something").query.trim())
            assertEquals("INSERT OR ROLLBACK INTO `SimpleModel` VALUES('something')",
                insertInto<SimpleModel>().orRollback().values("something").query.trim())
            assertEquals("INSERT OR ABORT INTO `SimpleModel` VALUES('something')",
                insertInto<SimpleModel>().orAbort().values("something").query.trim())
        }
    }

    @Test
    fun validateQuestionIntention() {
        databaseForTable<SimpleModel> { db ->
            "INSERT INTO `SimpleModel` VALUES('?')"
                .assertEquals(insertInto<SimpleModel>().values("?"))
        }
    }

    @Test
    fun validateInsertProjection() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
                insertInto<TwoColumnModel>().columns(name, id).values("name", "id").query.trim())
        }
    }

    @Test
    fun validateSelect() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            assertEquals("INSERT INTO `TwoColumnModel` SELECT * FROM `SimpleModel`",
                insertInto<TwoColumnModel>().select(select from SimpleModel::class).query.trim())
        }
    }

    @Test
    fun validateColumns() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
                insertInto<TwoColumnModel>().asColumns().values("name", "id").query.trim())
            assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
                insertInto<TwoColumnModel>().columns("name", "id").values("name", "id").query.trim())
            assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
                insertInto<TwoColumnModel>().columns(listOf(name, id)).values("name", "id").query.trim())
        }
    }

    @Test
    fun validateColumnValues() {
        databaseForTable<SimpleModel> { dbFlowDatabase ->
            assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 0)",
                insertInto<TwoColumnModel>().columnValues(name.eq("name"), id.eq(0)).query.trim())
            assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 0)",
                insertInto<TwoColumnModel>().columnValues(Operator.op<String>(NameAlias.builder("name").build()).eq("name"),
                    id.eq(0)).query.trim())
            assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 0)",
                insertInto<TwoColumnModel>().columnValues(group = OperatorGroup.clause().andAll(name.eq("name"), id.eq(0))).query.trim())

            val contentValues = ContentValues()
            contentValues["name"] = "name"
            contentValues["id"] = 0.toInt()

            assertEquals("INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 0)",
                insertInto<TwoColumnModel>().columnValues(contentValues).query.trim())

        }
    }
}
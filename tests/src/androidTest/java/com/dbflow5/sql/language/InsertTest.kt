package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.assertEquals
import com.dbflow5.config.database
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.NameAlias
import com.dbflow5.query2.ColumnValue
import com.dbflow5.query2.insert
import com.dbflow5.query2.operations.Operation
import com.dbflow5.query2.operations.operator
import com.dbflow5.query2.orAbort
import com.dbflow5.query2.orFail
import com.dbflow5.query2.orIgnore
import com.dbflow5.query2.orReplace
import com.dbflow5.query2.orRollback
import com.dbflow5.query2.select
import org.junit.Assert.assertEquals
import org.junit.Test

class InsertTest : BaseUnitTest() {

    private val simpleModelAdapter
        get() = database<TestDatabase>().simpleModelAdapter

    private val twoColumnModelAdapter
        get() = database<TestDatabase>().twoColumnModelAdapter

    @Test
    fun validateInsert() {
        assertEquals(
            "INSERT INTO `SimpleModel` VALUES('something')",
            simpleModelAdapter.insert().values("something").query.trim()
        )
    }

    @Test
    fun validateInsertOr() {
        assertEquals(
            "INSERT OR REPLACE INTO `SimpleModel` VALUES('something')",
            simpleModelAdapter.insert().orReplace().values("something").query.trim()
        )
        assertEquals(
            "INSERT OR FAIL INTO `SimpleModel` VALUES('something')",
            simpleModelAdapter.insert().orFail().values("something").query.trim()
        )
        assertEquals(
            "INSERT OR IGNORE INTO `SimpleModel` VALUES('something')",
            simpleModelAdapter.insert().orIgnore().values("something").query.trim()
        )
        assertEquals(
            "INSERT OR REPLACE INTO `SimpleModel` VALUES('something')",
            simpleModelAdapter.insert().orReplace().values("something").query.trim()
        )
        assertEquals(
            "INSERT OR ROLLBACK INTO `SimpleModel` VALUES('something')",
            simpleModelAdapter.insert().orRollback().values("something").query.trim()
        )
        assertEquals(
            "INSERT OR ABORT INTO `SimpleModel` VALUES('something')",
            simpleModelAdapter.insert().orAbort().values("something").query.trim()
        )
    }

    @Test
    fun validateQuestionIntention() {
        "INSERT INTO `SimpleModel` VALUES('?')"
            .assertEquals(simpleModelAdapter.insert().values("?"))
    }

    @Test
    fun validateInsertProjection() {
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 0)",
            twoColumnModelAdapter.insert(
                TwoColumnModel_Table.name.eq("name"),
                TwoColumnModel_Table.id.eq(0)
            ).query.trim()
        )
    }

    @Test
    fun validateSelect() {
        assertEquals(
            "INSERT INTO `TwoColumnModel` SELECT * FROM `SimpleModel`",
            twoColumnModelAdapter.insert().select(simpleModelAdapter.select()).query.trim()
        )
    }

    @Test
    fun validateColumns() {
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
            twoColumnModelAdapter.insert(
                ColumnValue(
                    twoColumnModelAdapter.getProperty("name"),
                    "name"
                ),
                ColumnValue(
                    twoColumnModelAdapter.getProperty("id"),
                    "id"
                )
            ).query.trim()
        )
    }

    @Test
    fun validateColumnValues() {
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 0)",
            twoColumnModelAdapter.insert(
                TwoColumnModel_Table.name.eq("name"),
                TwoColumnModel_Table.id.eq(0)
            ).query.trim()
        )
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 0)",
            twoColumnModelAdapter.insert(
                operator(
                    NameAlias.builder("name").build(),
                    Operation.Equals,
                    "name"
                ),
                TwoColumnModel_Table.id.eq(0)
            ).query.trim()
        )
    }
}
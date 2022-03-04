package com.dbflow5.test.sql.language

import com.dbflow5.test.TestDatabase_Database
import com.dbflow5.test.TwoColumnModel_Table
import com.dbflow5.query.ColumnValue
import com.dbflow5.query.NameAlias
import com.dbflow5.query.insert
import com.dbflow5.query.operations.Operation
import com.dbflow5.query.operations.operator
import com.dbflow5.query.orAbort
import com.dbflow5.query.orFail
import com.dbflow5.query.orIgnore
import com.dbflow5.query.orReplace
import com.dbflow5.query.orRollback
import com.dbflow5.query.select
import com.dbflow5.test.simpleModelAdapter
import com.dbflow5.test.DatabaseTestRule
import com.dbflow5.test.assertEquals
import com.dbflow5.test.twoColumnModelAdapter
import kotlin.test.Test
import kotlin.test.assertEquals

class InsertTest {

    
    val dbRule = DatabaseTestRule(TestDatabase_Database)

    @Test
    fun validateInsert() = dbRule {
        assertEquals(
            "INSERT INTO `SimpleModel` VALUES('something')",
            simpleModelAdapter.insert().values("something").query.trim()
        )
    }

    @Test
    fun validateInsertOr() = dbRule {
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
    fun validateQuestionIntention() = dbRule {
        "INSERT INTO `SimpleModel` VALUES('?')"
            .assertEquals(simpleModelAdapter.insert().values("?"))
    }

    @Test
    fun validateInsertProjection() = dbRule {
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 0)",
            twoColumnModelAdapter.insert(
                TwoColumnModel_Table.name.eq("name"),
                TwoColumnModel_Table.id.eq(0)
            ).query.trim()
        )
    }

    @Test
    fun validateSelect() = dbRule {
        assertEquals(
            "INSERT INTO `TwoColumnModel` SELECT * FROM `SimpleModel`",
            twoColumnModelAdapter.insert().select(simpleModelAdapter.select()).query.trim()
        )
    }

    @Test
    fun validateColumns() = dbRule {
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
    fun validateColumnValues() = dbRule {
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
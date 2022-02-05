package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.assertEquals
import com.dbflow5.config.database
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.NameAlias
import com.dbflow5.query.Operator
import com.dbflow5.query.OperatorGroup
import com.dbflow5.query.insertInto
import com.dbflow5.query.select
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
            simpleModelAdapter.insertInto().values("something").query.trim()
        )
    }

    @Test
    fun validateInsertOr() {
        assertEquals(
            "INSERT OR REPLACE INTO `SimpleModel` VALUES('something')",
            simpleModelAdapter.insertInto().orReplace().values("something").query.trim()
        )
        assertEquals(
            "INSERT OR FAIL INTO `SimpleModel` VALUES('something')",
            simpleModelAdapter.insertInto().orFail().values("something").query.trim()
        )
        assertEquals(
            "INSERT OR IGNORE INTO `SimpleModel` VALUES('something')",
            simpleModelAdapter.insertInto().orIgnore().values("something").query.trim()
        )
        assertEquals(
            "INSERT OR REPLACE INTO `SimpleModel` VALUES('something')",
            simpleModelAdapter.insertInto().orReplace().values("something").query.trim()
        )
        assertEquals(
            "INSERT OR ROLLBACK INTO `SimpleModel` VALUES('something')",
            simpleModelAdapter.insertInto().orRollback().values("something").query.trim()
        )
        assertEquals(
            "INSERT OR ABORT INTO `SimpleModel` VALUES('something')",
            simpleModelAdapter.insertInto().orAbort().values("something").query.trim()
        )
    }

    @Test
    fun validateQuestionIntention() {
        "INSERT INTO `SimpleModel` VALUES('?')"
            .assertEquals(simpleModelAdapter.insertInto().values("?"))
    }

    @Test
    fun validateInsertProjection() {
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
            twoColumnModelAdapter.insertInto().columns(
                TwoColumnModel_Table.name,
                TwoColumnModel_Table.id
            )
                .values("name", "id").query.trim()
        )
    }

    @Test
    fun validateSelect() {
        assertEquals(
            "INSERT INTO `TwoColumnModel` SELECT * FROM `SimpleModel`",
            twoColumnModelAdapter.insertInto().select(select from simpleModelAdapter).query.trim()
        )
    }

    @Test
    fun validateColumns() {
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
            twoColumnModelAdapter.insertInto().asColumns().values("name", "id").query.trim()
        )
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
            twoColumnModelAdapter.insertInto().columns("name", "id")
                .values("name", "id").query.trim()
        )
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
            twoColumnModelAdapter.insertInto().columns(
                listOf(
                    TwoColumnModel_Table.name,
                    TwoColumnModel_Table.id
                )
            ).values("name", "id").query.trim()
        )
    }

    @Test
    fun validateColumnValues() {
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 0)",
            twoColumnModelAdapter.insertInto().columnValues(
                TwoColumnModel_Table.name.eq("name"),
                TwoColumnModel_Table.id.eq(0)
            ).query.trim()
        )
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 0)",
            twoColumnModelAdapter.insertInto().columnValues(
                Operator.op<String>(NameAlias.builder("name").build()).eq("name"),
                TwoColumnModel_Table.id.eq(0)
            ).query.trim()
        )
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 0)",
            twoColumnModelAdapter.insertInto().columnValues(
                group = OperatorGroup.clause()
                    .andAll(
                        TwoColumnModel_Table.name.eq("name"),
                        TwoColumnModel_Table.id.eq(0)
                    )
            ).query.trim()
        )

    }
}
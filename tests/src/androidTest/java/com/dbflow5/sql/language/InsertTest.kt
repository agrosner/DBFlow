package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.assertEquals
import com.dbflow5.models.SimpleModel
import com.dbflow5.models.TwoColumnModel
import com.dbflow5.models.TwoColumnModel_Table
import com.dbflow5.query.*
import org.junit.Assert.assertEquals
import org.junit.Test

class InsertTest : BaseUnitTest() {

    @Test
    fun validateInsert() {
        assertEquals(
            "INSERT INTO `SimpleModel` VALUES('something')",
            insertInto<SimpleModel>().values("something").query.trim()
        )
    }

    @Test
    fun validateInsertOr() {
        assertEquals(
            "INSERT OR REPLACE INTO `SimpleModel` VALUES('something')",
            insertInto<SimpleModel>().orReplace().values("something").query.trim()
        )
        assertEquals(
            "INSERT OR FAIL INTO `SimpleModel` VALUES('something')",
            insertInto<SimpleModel>().orFail().values("something").query.trim()
        )
        assertEquals(
            "INSERT OR IGNORE INTO `SimpleModel` VALUES('something')",
            insertInto<SimpleModel>().orIgnore().values("something").query.trim()
        )
        assertEquals(
            "INSERT OR REPLACE INTO `SimpleModel` VALUES('something')",
            insertInto<SimpleModel>().orReplace().values("something").query.trim()
        )
        assertEquals(
            "INSERT OR ROLLBACK INTO `SimpleModel` VALUES('something')",
            insertInto<SimpleModel>().orRollback().values("something").query.trim()
        )
        assertEquals(
            "INSERT OR ABORT INTO `SimpleModel` VALUES('something')",
            insertInto<SimpleModel>().orAbort().values("something").query.trim()
        )
    }

    @Test
    fun validateQuestionIntention() {
        "INSERT INTO `SimpleModel` VALUES('?')"
            .assertEquals(insertInto<SimpleModel>().values("?"))
    }

    @Test
    fun validateInsertProjection() {
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
            insertInto<TwoColumnModel>().columns(TwoColumnModel_Table.name, TwoColumnModel_Table.id)
                .values("name", "id").query.trim()
        )
    }

    @Test
    fun validateSelect() {
        assertEquals(
            "INSERT INTO `TwoColumnModel` SELECT * FROM `SimpleModel`",
            insertInto<TwoColumnModel>().select(select from SimpleModel::class).query.trim()
        )
    }

    @Test
    fun validateColumns() {
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
            insertInto<TwoColumnModel>().asColumns().values("name", "id").query.trim()
        )
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
            insertInto<TwoColumnModel>().columns("name", "id").values("name", "id").query.trim()
        )
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 'id')",
            insertInto<TwoColumnModel>().columns(
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
            insertInto<TwoColumnModel>().columnValues(
                TwoColumnModel_Table.name.eq("name"),
                TwoColumnModel_Table.id.eq(0)
            ).query.trim()
        )
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 0)",
            insertInto<TwoColumnModel>().columnValues(
                Operator.op<String>(NameAlias.builder("name").build()).eq("name"),
                TwoColumnModel_Table.id.eq(0)
            ).query.trim()
        )
        assertEquals(
            "INSERT INTO `TwoColumnModel`(`name`, `id`) VALUES('name', 0)",
            insertInto<TwoColumnModel>().columnValues(
                group = OperatorGroup.clause()
                    .andAll(
                        TwoColumnModel_Table.name.eq("name"),
                        TwoColumnModel_Table.id.eq(0)
                    )
            ).query.trim()
        )

    }
}
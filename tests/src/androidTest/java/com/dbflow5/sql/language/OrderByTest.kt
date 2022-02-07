package com.dbflow5.sql.language

import com.dbflow5.BaseUnitTest
import com.dbflow5.annotation.Collate
import com.dbflow5.assertEquals
import com.dbflow5.models.SimpleModel_Table
import com.dbflow5.query.OrderBy
import com.dbflow5.query.nameAlias
import org.junit.Test

class OrderByTest : BaseUnitTest() {

    @Test
    fun validateBasicOrderBy() {
        "`name` ASC".assertEquals(OrderBy.fromProperty(SimpleModel_Table.name).ascending())
    }

    @Test
    fun validateDescendingOrderBy() {
        "`name` DESC".assertEquals(OrderBy.fromNameAlias("name".nameAlias).descending())
    }

    @Test
    fun validateCollate() {
        "`name` COLLATE RTRIM ASC".assertEquals(
            OrderBy.fromProperty(SimpleModel_Table.name).ascending() collate Collate.RTRIM
        )
    }

    @Test
    fun validateCustomOrdrBy() {
        "`name` ASC This is custom".assertEquals(OrderBy.fromString("`name` ASC This is custom"))
    }
}
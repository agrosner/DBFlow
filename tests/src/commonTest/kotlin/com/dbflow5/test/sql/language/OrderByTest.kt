package com.dbflow5.test.sql.language

import com.dbflow5.annotation.Collate
import com.dbflow5.query.nameAlias
import com.dbflow5.query.orderBy
import com.dbflow5.test.SimpleModel
import com.dbflow5.test.TestRule
import com.dbflow5.test.assertEquals
import kotlin.test.Test

class OrderByTest : TestRule() {

    @Test
    fun validateBasicOrderBy() {
        "`name` ASC".assertEquals(orderBy(SimpleModel.name).asc())
    }

    @Test
    fun validateDescendingOrderBy() {
        "`name` DESC".assertEquals(orderBy("name".nameAlias).desc())
    }

    @Test
    fun validateCollate() {
        "`name` COLLATE RTRIM ASC".assertEquals(
            (orderBy(SimpleModel.name) collate Collate.RTrim).asc()
        )
    }
}

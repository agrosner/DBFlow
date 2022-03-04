package com.dbflow5.test.sql.language

import com.dbflow5.annotation.Collate
import com.dbflow5.test.assertEquals
import com.dbflow5.test.SimpleModel_Table
import com.dbflow5.query.nameAlias
import com.dbflow5.query.orderBy
import kotlin.test.Test

class OrderByTest {

    @Test
    fun validateBasicOrderBy() {
        "`name` ASC".assertEquals(orderBy(SimpleModel_Table.name).asc())
    }

    @Test
    fun validateDescendingOrderBy() {
        "`name` DESC".assertEquals(orderBy("name".nameAlias).desc())
    }

    @Test
    fun validateCollate() {
        "`name` COLLATE RTRIM ASC".assertEquals(
            (orderBy(SimpleModel_Table.name) collate Collate.RTrim).asc()
        )
    }
}

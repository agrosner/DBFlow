package com.raizlabs.dbflow5.sql.language

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.annotation.Collate
import com.raizlabs.dbflow5.assertEquals
import com.raizlabs.dbflow5.models.SimpleModel_Table.name
import com.raizlabs.dbflow5.query.OrderBy
import com.raizlabs.dbflow5.query.collate
import com.raizlabs.dbflow5.query.nameAlias
import org.junit.Test

class OrderByTest : BaseUnitTest() {


    @Test
    fun validateBasicOrderBy() {
        "`name` ASC".assertEquals(OrderBy.fromProperty(name).ascending())
    }

    @Test
    fun validateDescendingOrderBy() {
        "`name` DESC".assertEquals(OrderBy.fromNameAlias("name".nameAlias).descending())
    }

    @Test
    fun validateCollate() {
        "`name` COLLATE RTRIM ASC".assertEquals(OrderBy.fromProperty(name).ascending() collate Collate.RTRIM)
    }

    @Test
    fun validateCustomOrdrBy() {
        "`name` ASC This is custom".assertEquals(OrderBy.fromString("`name` ASC This is custom"))
    }
}
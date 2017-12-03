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
        assertEquals("`name` ASC", OrderBy.fromProperty(name).ascending())
    }

    @Test
    fun validateDescendingOrderBy() {
        assertEquals("`name` DESC", OrderBy.fromNameAlias("name".nameAlias).descending())
    }

    @Test
    fun validateCollate() {
        assertEquals("`name` COLLATE RTRIM ASC", OrderBy.fromProperty(name).ascending() collate Collate.RTRIM)
    }

    @Test
    fun validateCustomOrdrBy() {
        assertEquals("`name` ASC This is custom", OrderBy.fromString("`name` ASC This is custom"))
    }
}
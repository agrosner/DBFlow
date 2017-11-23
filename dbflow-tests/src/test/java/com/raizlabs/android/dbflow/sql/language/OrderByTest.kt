package com.raizlabs.android.dbflow.sql.language

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.annotation.Collate
import com.raizlabs.android.dbflow.assertEquals
import com.raizlabs.android.dbflow.models.SimpleModel_Table.name
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
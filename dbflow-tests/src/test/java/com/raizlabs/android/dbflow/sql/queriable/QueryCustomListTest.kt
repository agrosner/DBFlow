package com.raizlabs.android.dbflow.sql.queriable

import com.raizlabs.android.dbflow.sql.language.SQLite.*
import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.kotlinextensions.save
import com.raizlabs.android.dbflow.models.Author
import com.raizlabs.android.dbflow.models.Author_Table.*
import org.junit.Assert
import org.junit.Test

class QueryCustomListTest : BaseUnitTest() {

    @Test
    fun test() {

        Author ().apply {
            firstName = "bob"
            lastName = "ray"
        }.save()

        Author ().apply {
            firstName = "bill"
            lastName = "ray"
        }.save()

        Author ().apply {
            firstName = "frank"
            lastName = "sina"
        }.save()

        val authorIds =
            select(id)
            .from(Author::class.java)
            .where(last_name.eq("ray"))
            .queryCustomList({ flowCursor -> flowCursor.getIntOrDefault(0) })

        Assert.assertEquals(2, authorIds.size)
    }
}
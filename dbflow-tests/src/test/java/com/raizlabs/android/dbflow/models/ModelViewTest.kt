package com.raizlabs.android.dbflow.models

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.assertEquals
import org.junit.Test

class ModelViewTest : BaseUnitTest() {

    @Test
    fun validateModelViewQuery() {
        assertEquals("SELECT `id` as `authorId`,`first_name` || ' ' || `last_name` AS `authorName` FROM `Author`",
            AuthorView.query)
    }
}
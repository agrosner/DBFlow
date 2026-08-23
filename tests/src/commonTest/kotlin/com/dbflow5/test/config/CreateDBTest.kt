package com.dbflow5.test.config

import com.dbflow5.database.createDB
import com.dbflow5.test.TestDatabase
import com.dbflow5.test.helpers.platformSettings
import kotlin.test.Test
import kotlin.test.assertEquals

class CreateDBTest {

    @Test
    fun createDB_opensGeneratedDatabase() {
        val db = createDB<TestDatabase>(platformSettings()) {
            copy(name = "Created", inMemory = true)
        }
        assertEquals("Created", db.databaseName)
        db.close()
    }
}

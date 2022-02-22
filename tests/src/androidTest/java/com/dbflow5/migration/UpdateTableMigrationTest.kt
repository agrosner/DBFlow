package com.dbflow5.migration

import com.dbflow5.BaseUnitTest
import com.dbflow5.TestDatabase
import com.dbflow5.config.database
import com.dbflow5.models.SimpleModel_Table
import org.junit.Test

/**
 * Description:
 */

class UpdateTableMigrationTest : BaseUnitTest() {


    @Test
    fun testUpdateMigrationQuery() {
        database<TestDatabase> {
            val update = UpdateTableMigration { db.simpleModelAdapter }
            update.set(SimpleModel_Table.name.eq("yes"))
            update.migrate(db)
        }
    }
}

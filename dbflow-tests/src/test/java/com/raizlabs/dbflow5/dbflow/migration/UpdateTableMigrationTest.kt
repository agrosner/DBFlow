package com.raizlabs.dbflow5.dbflow.migration

import com.raizlabs.dbflow5.dbflow.BaseUnitTest
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.dbflow.models.SimpleModel
import com.raizlabs.dbflow5.dbflow.models.SimpleModel_Table
import com.raizlabs.dbflow5.migration.UpdateTableMigration
import org.junit.Test

/**
 * Description:
 */

class UpdateTableMigrationTest : BaseUnitTest() {


    @Test
    fun testUpdateMigrationQuery() {
        val update = UpdateTableMigration(SimpleModel::class.java)
        update.set(SimpleModel_Table.name.eq("yes"))
        update.migrate(databaseForTable<SimpleModel>())
    }
}

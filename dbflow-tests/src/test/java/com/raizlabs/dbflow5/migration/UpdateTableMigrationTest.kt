package com.raizlabs.dbflow5.migration

import com.raizlabs.dbflow5.BaseUnitTest
import com.raizlabs.dbflow5.config.databaseForTable
import com.raizlabs.dbflow5.models.SimpleModel
import com.raizlabs.dbflow5.models.SimpleModel_Table
import org.junit.Test

/**
 * Description:
 */

class UpdateTableMigrationTest : BaseUnitTest() {


    @Test
    fun testUpdateMigrationQuery() {
        val update = UpdateTableMigration(SimpleModel::class)
        update.set(SimpleModel_Table.name.eq("yes"))
        update.migrate(databaseForTable<SimpleModel>())
    }
}

package com.raizlabs.android.dbflow.sql.migration

import com.raizlabs.android.dbflow.BaseUnitTest
import com.raizlabs.android.dbflow.config.databaseForTable
import com.raizlabs.android.dbflow.models.SimpleModel
import com.raizlabs.android.dbflow.models.SimpleModel_Table
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

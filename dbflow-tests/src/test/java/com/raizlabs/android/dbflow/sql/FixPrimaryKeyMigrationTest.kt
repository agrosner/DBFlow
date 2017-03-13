package com.raizlabs.android.dbflow.sql

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.language.SQLite
import com.raizlabs.android.dbflow.structure.ModelAdapter
import com.raizlabs.android.dbflow.FlowTestCase
import com.raizlabs.android.dbflow.global.GlobalModel
import com.raizlabs.android.dbflow.global.GlobalModel_Table
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Description:
 */
class FixPrimaryKeyMigrationTest : FlowTestCase() {

    lateinit var fixPrimaryKeyMigration: FixPrimaryKeyMigration<GlobalModel>
    lateinit var modelAdapter: ModelAdapter<*>

    @Before
    fun setUpMigration() {
        fixPrimaryKeyMigration = object : FixPrimaryKeyMigration<GlobalModel>() {
            override val tableClass: Class<GlobalModel>
                get() = GlobalModel::class.java
        }
        modelAdapter = FlowManager.getModelAdapter(fixPrimaryKeyMigration.tableClass)
    }


    @Test
    fun test_validateTableInformationQuery() {
        assertEquals("SELECT sql FROM sqlite_master WHERE name='GlobalModel'", fixPrimaryKeyMigration.selectTableQuery.query)
        assertEquals("CREATE TABLE IF NOT EXISTS `GlobalModel`(`id` INTEGER,`name` TEXT, PRIMARY KEY(`id`)" + ");", modelAdapter.creationQuery)
        assertTrue(fixPrimaryKeyMigration.validateCreationQuery(modelAdapter.creationQuery.replace("IF NOT EXISTS ", "")))
    }

    @Test
    fun test_validateTempCreationQuery() {
        assertEquals("CREATE TABLE IF NOT EXISTS `GlobalModel_temp`(`id` INTEGER,`name` TEXT, PRIMARY KEY(`id`)" + ");", fixPrimaryKeyMigration.tempCreationQuery)
    }

    @Test
    fun test_validateInsertTransferQuery() {
        assertEquals("INSERT INTO `GlobalModel_temp`(`id`, `name`) SELECT `id`,`name` FROM `GlobalModel`", fixPrimaryKeyMigration.insertTransferQuery)
    }

    @Test
    fun test_validateMigration() {
        var globalModel = GlobalModel()
        globalModel.name = "Test"
        globalModel.save()

        assertTrue(globalModel.id > 0)

        fixPrimaryKeyMigration.migrate(FlowManager.getDatabaseForTable(GlobalModel::class.java).writableDatabase)


        globalModel = SQLite.select().from(GlobalModel::class.java)
            .where(GlobalModel_Table.id.eq(globalModel.id)).querySingle()!!
        assertNotNull(globalModel)
    }
}

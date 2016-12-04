package com.raizlabs.android.dbflow.test.sql

import android.database.Cursor

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.sql.SQLiteType
import com.raizlabs.android.dbflow.sql.language.Select
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration
import com.raizlabs.android.dbflow.sql.migration.IndexMigration
import com.raizlabs.android.dbflow.sql.migration.UpdateTableMigration
import com.raizlabs.android.dbflow.test.FlowTestCase

import org.junit.Test

import java.util.Arrays

import junit.framework.Assert.assertFalse
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue

class MigrationTest : FlowTestCase() {

    @Test
    fun testMigration() {

        val columnNames = Arrays.asList("`fraction` REAL", "`time` INTEGER", "`name2` TEXT", "`number` INTEGER", "`blobby` BLOB")
        val columns = Arrays.asList("fraction", "time", "name2", "number", "blobby")

        val renameMigration = AlterTableMigration(MigrationModel::class.java).renameFrom("TestModel")
        renameMigration.onPreMigrate()
        assertEquals("ALTER TABLE `TestModel` RENAME TO `MigrationModel`", renameMigration.renameQuery)
        renameMigration.onPostMigrate()

        val alterTableMigration = AlterTableMigration(MigrationModel::class.java)
        alterTableMigration.addColumn(SQLiteType.REAL, "fraction")
                .addColumn(SQLiteType.INTEGER, "time")
                .addColumn(SQLiteType.TEXT, "name2")
                .addColumn(SQLiteType.INTEGER, "number")
                .addColumn(SQLiteType.BLOB, "blobby")
        alterTableMigration.onPreMigrate()

        val columnDefinitions = alterTableMigration.columnDefinitions
        for (i in columnDefinitions.indices) {
            assertEquals("ALTER TABLE `MigrationModel` ADD COLUMN " + columnNames[i], columnDefinitions[i])
        }

        alterTableMigration.migrate(FlowManager.getDatabaseForTable(MigrationModel::class.java).writableDatabase)

        // test the column sizes
        val cursor = Select().from(MigrationModel::class.java).where().query()
        assertTrue(cursor!!.columnNames.size == columnNames.size + 2)

        try {
            Thread.sleep(200)
        } catch (e: InterruptedException) {
        }

        // make sure column exists now
        for (i in columns.indices) {
            assertTrue(cursor.getColumnIndex(columns[i]) != -1)
        }
        cursor.close()

        alterTableMigration.onPostMigrate()
    }

    @Test
    fun testUpdateMigration() {
        val updateTableMigration = UpdateTableMigration(MigrationModel::class.java)
                .set(MigrationModel_Table.name.`is`("test")).where(MigrationModel_Table.name.`is`("notTest"))
        updateTableMigration.onPreMigrate()

        assertEquals("UPDATE `MigrationModel` SET `name`='test' WHERE `name`='notTest'", updateTableMigration
                .updateStatement.query.trim { it <= ' ' })

        updateTableMigration.migrate(FlowManager.getDatabaseForTable(MigrationModel::class.java).writableDatabase)
        updateTableMigration.onPostMigrate()
    }

    @Test
    fun testSqlFile() {
        val migrationModel = MigrationModel()
        migrationModel.name = "test"
        migrationModel.save()
        val cursor = Select().from(MigrationModel::class.java).query()
        assertTrue(cursor!!.moveToFirst())

        val addedColumIndex = cursor.getColumnIndex("addedColumn")
        assertFalse(addedColumIndex == -1)

        cursor.close()
        // broken with junit tests
    }

    fun testIndexMigration() {
        val indexMigration = object : IndexMigration<TestModel3>(TestModel3::class.java) {
            override fun getName(): String {
                return "MyIndex"
            }
        }
                .addColumn(TestModel3_Table.type)
        assertEquals("CREATE INDEX IF NOT EXISTS `MyIndex` ON `TestModel32`(`type`)", indexMigration.indexQuery.trim { it <= ' ' })
    }

}

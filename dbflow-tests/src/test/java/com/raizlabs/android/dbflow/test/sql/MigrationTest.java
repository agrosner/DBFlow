package com.raizlabs.android.dbflow.test.sql;

import android.database.Cursor;
import android.database.sqlite.SQLiteException;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.SQLiteType;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;
import com.raizlabs.android.dbflow.sql.migration.IndexMigration;
import com.raizlabs.android.dbflow.sql.migration.UpdateTableMigration;
import com.raizlabs.android.dbflow.test.FlowTestCase;

import org.junit.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class MigrationTest extends FlowTestCase {

    @Test
    public void testMigration() {

        List<String> columnNames = Arrays.asList("`fraction` REAL", "`time` INTEGER", "`name2` TEXT", "`number` INTEGER", "`blobby` BLOB");
        List<String> columns = Arrays.asList("fraction", "time", "name2", "number", "blobby");

        AlterTableMigration<MigrationModel> renameMigration = new AlterTableMigration<>(MigrationModel.class).renameFrom("TestModel");
        renameMigration.onPreMigrate();
        assertEquals("ALTER TABLE `TestModel` RENAME TO `MigrationModel`", renameMigration.getRenameQuery());
        renameMigration.onPostMigrate();

        AlterTableMigration<MigrationModel> alterTableMigration = new AlterTableMigration<>(MigrationModel.class);
        alterTableMigration.addColumn(SQLiteType.REAL, "fraction")
                .addColumn(SQLiteType.INTEGER, "time")
                .addColumn(SQLiteType.TEXT, "name2")
                .addColumn(SQLiteType.INTEGER, "number")
                .addColumn(SQLiteType.BLOB, "blobby");
        alterTableMigration.onPreMigrate();

        List<String> columnDefinitions = alterTableMigration.getColumnDefinitions();
        for (int i = 0; i < columnDefinitions.size(); i++) {
            assertEquals("ALTER TABLE `MigrationModel` ADD COLUMN " + columnNames.get(i), columnDefinitions.get(i));
        }

        try {
            alterTableMigration.migrate(FlowManager.getDatabaseForTable(MigrationModel.class).getWritableDatabase());
        } catch (SQLiteException e) {
            if (e.getMessage().startsWith("duplicate column name: fraction (code 1):")) {
                // ignore since we've already added the column.
            } else {
                // some other issue
                throw new RuntimeException(e);
            }
        }

        // test the column sizes
        Cursor cursor = new Select().from(MigrationModel.class).where().query();
        assertTrue(cursor.getColumnNames().length == columnNames.size() + 2);

        // make sure column exists now
        for (int i = 0; i < columns.size(); i++) {
            assertTrue(cursor.getColumnIndex(columns.get(i)) != -1);
        }

        alterTableMigration.onPostMigrate();
    }

    @Test
    public void testUpdateMigration() {
        UpdateTableMigration<MigrationModel> updateTableMigration
                = new UpdateTableMigration<>(MigrationModel.class)
                .set(MigrationModel_Table.name.is("test")).where(MigrationModel_Table.name.is("notTest"));
        updateTableMigration.onPreMigrate();

        assertEquals("UPDATE `MigrationModel` SET `name`='test' WHERE `name`='notTest'", updateTableMigration.getQuery().trim());

        updateTableMigration.migrate(FlowManager.getDatabaseForTable(MigrationModel.class).getWritableDatabase());
        updateTableMigration.onPostMigrate();
    }

    @Test
    public void testSqlFile() {
        MigrationModel migrationModel = new MigrationModel();
        migrationModel.setName("test");
        migrationModel.save();
        Cursor cursor = new Select().from(MigrationModel.class).query();
        assertTrue(cursor.moveToFirst());

        int addedColumIndex = cursor.getColumnIndex("addedColumn");
        assertFalse(addedColumIndex == -1);
    }

    public void testIndexMigration() {
        IndexMigration<TestModel3> indexMigration
                = new IndexMigration<>("MyIndex", TestModel3.class)
                .addColumn(TestModel3_Table.type);
        assertEquals("CREATE INDEX IF NOT EXISTS `MyIndex` ON `TestModel32`(`type`)", indexMigration.getIndexQuery().trim());
    }

}

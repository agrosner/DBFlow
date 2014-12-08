package com.raizlabs.android.dbflow.test.sql;

import android.database.Cursor;
import android.test.AndroidTestCase;

import com.raizlabs.android.dbflow.config.FlowLog;
import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.sql.language.Select;
import com.raizlabs.android.dbflow.sql.builder.Condition;
import com.raizlabs.android.dbflow.sql.migration.AlterTableMigration;
import com.raizlabs.android.dbflow.sql.migration.UpdateTableMigration;

import java.util.Arrays;
import java.util.List;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
public class MigrationTest extends AndroidTestCase {

    @Override
    protected void setUp() throws Exception {
        super.setUp();
        FlowManager.init(getContext());
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.I);
    }


    public void testMigration() {

        List<String> columnNames = Arrays.asList("fraction REAL", "time INTEGER", "name2 TEXT", "number INTEGER", "blobby BLOB");

        AlterTableMigration<MigrationModel> renameMigration = new AlterTableMigration<>(MigrationModel.class).renameFrom("TestModel");
        renameMigration.onPreMigrate();
        assertEquals("ALTER TABLE TestModel RENAME TO MigrationModel", renameMigration.getRenameQuery());
        renameMigration.onPostMigrate();

        AlterTableMigration<MigrationModel> alterTableMigration = new AlterTableMigration<>(MigrationModel.class);
        alterTableMigration.addColumn(float.class, "fraction")
                .addColumn(long.class, "time")
                .addColumn(String.class, "name2")
                .addColumn(int.class, "number")
                .addColumn(byte[].class, "blobby");
        alterTableMigration.onPreMigrate();

        List<String> columnDefinitions = alterTableMigration.getColumnDefinitions();
        for(int i = 0; i < columnDefinitions.size(); i++) {
            assertEquals("ALTER TABLE MigrationModel ADD COLUMN " + columnNames.get(i), columnDefinitions.get(i));
        }

        alterTableMigration.migrate(FlowManager.getDatabaseForTable(MigrationModel.class).getWritableDatabase());

        // test the column sizes
        Cursor cursor = new Select().from(MigrationModel.class).where().query();
        String[] columns = cursor.getColumnNames();
        assertTrue(columns.length == columnNames.size()+2);

        // make sure column exists now
        for(int i = 0; i < columnNames.size(); i++) {
            assertTrue(cursor.getColumnIndex(columnNames.get(i).split(" ")[0]) != -1);
        }

        alterTableMigration.onPostMigrate();
    }

    public void testUpdateMigration() {
        UpdateTableMigration<MigrationModel> updateTableMigration
                = new UpdateTableMigration<>(MigrationModel.class)
                .set(Condition.column("name").is("test")).where(Condition.column("name").is("notTest"));
        updateTableMigration.onPreMigrate();

        assertEquals("UPDATE MigrationModel SET name='test' WHERE name='notTest'", updateTableMigration.getQuery().trim());

        updateTableMigration.migrate(FlowManager.getDatabaseForTable(MigrationModel.class).getWritableDatabase());
        updateTableMigration.onPostMigrate();
    }

    public void testSqlFile() {
        MigrationModel migrationModel = new MigrationModel();
        migrationModel.name = "test";
        migrationModel.save(false);
        Cursor cursor = new Select().from(MigrationModel.class).query();
        assertTrue(cursor.moveToFirst());

        int addedColumIndex = cursor.getColumnIndex("addedColumn");
        assertFalse(addedColumIndex == -1);
    }

    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        getContext().deleteDatabase(MigrationDatabase.NAME);
        FlowManager.destroy();
    }
}

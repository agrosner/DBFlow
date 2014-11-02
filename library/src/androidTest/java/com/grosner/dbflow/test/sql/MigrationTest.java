package com.grosner.dbflow.test.sql;

import android.database.Cursor;
import android.test.AndroidTestCase;

import com.grosner.dbflow.config.DBConfiguration;
import com.grosner.dbflow.config.FlowLog;
import com.grosner.dbflow.config.FlowManager;
import com.grosner.dbflow.sql.language.Select;
import com.grosner.dbflow.sql.builder.Condition;
import com.grosner.dbflow.sql.migration.AlterTableMigration;
import com.grosner.dbflow.sql.migration.UpdateTableMigration;
import com.grosner.dbflow.test.structure.TestModel1;

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
        FlowManager.setContext(getContext());
        FlowLog.setMinimumLoggingLevel(FlowLog.Level.I);
    }


    public void testMigration() {

        List<String> columnNames = Arrays.asList("fraction REAL", "time INTEGER", "name2 TEXT", "number INTEGER", "blobby BLOB");

        AlterTableMigration<TestModel1> renameMigration = new AlterTableMigration<TestModel1>(TestModel1.class, 2).renameFrom("TestModel");
        renameMigration.onPreMigrate();
        assertEquals("ALTER TABLE TestModel RENAME TO TestModel1", renameMigration.getRenameQuery());
        renameMigration.onPostMigrate();

        AlterTableMigration<TestModel1> alterTableMigration = new AlterTableMigration<TestModel1>(TestModel1.class, 2);
        alterTableMigration.addColumn(float.class, "fraction")
                .addColumn(long.class, "time")
                .addColumn(String.class, "name2")
                .addColumn(int.class, "number")
                .addColumn(byte[].class, "blobby");
        alterTableMigration.onPreMigrate();

        List<String> columnDefinitions = alterTableMigration.getColumnDefinitions();
        for(int i = 0; i < columnDefinitions.size(); i++) {
            assertEquals("ALTER TABLE TestModel1 ADD COLUMN " + columnNames.get(i), columnDefinitions.get(i));
        }

        alterTableMigration.migrate(FlowManager.getManagerForTable(TestModel1.class).getWritableDatabase());

        // test the column sizes
        Cursor cursor = new Select().from(TestModel1.class).where().query();
        String[] columns = cursor.getColumnNames();
        assertTrue(columns.length == columnNames.size()+1);

        // make sure column exists now
        for(int i = 0; i < columnNames.size(); i++) {
            assertTrue(cursor.getColumnIndex(columnNames.get(i).split(" ")[0]) != -1);
        }

        alterTableMigration.onPostMigrate();
    }

    public void testUpdateMigration() {
        UpdateTableMigration<TestModel1> updateTableMigration
                = new UpdateTableMigration<TestModel1>(TestModel1.class, 2)
                .set(Condition.column("name").is("test")).where(Condition.column("name").is("notTest"));
        updateTableMigration.onPreMigrate();

        assertEquals("UPDATE TestModel1 SET name = 'test' WHERE name = 'notTest'", updateTableMigration.getQuery().trim());

        updateTableMigration.migrate(FlowManager.getManagerForTable(TestModel1.class).getWritableDatabase());
        updateTableMigration.onPostMigrate();
    }


    @Override
    protected void tearDown() throws Exception {
        super.tearDown();
        FlowManager.destroy();
    }
}

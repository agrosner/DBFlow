package com.raizlabs.android.dbflow.test.sql;

import android.database.sqlite.SQLiteDatabase;

import com.raizlabs.android.dbflow.sql.migration.Migration;
import com.raizlabs.android.dbflow.test.TestDatabase;

/**
 * Description:
 */
@com.raizlabs.android.dbflow.annotation.Migration(version = 1, databaseName = TestDatabase.NAME, priority = 1)
public class TestMigration implements Migration {
    @Override
    public void onPreMigrate() {

    }

    @Override
    public void migrate(SQLiteDatabase database) {

    }

    @Override
    public void onPostMigrate() {

    }
}

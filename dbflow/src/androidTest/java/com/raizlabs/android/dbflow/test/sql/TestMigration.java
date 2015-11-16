package com.raizlabs.android.dbflow.test.sql;

import android.database.sqlite.SQLiteDatabase;

import com.raizlabs.android.dbflow.sql.migration.Migration;
import com.raizlabs.android.dbflow.test.TestDatabase;

@com.raizlabs.android.dbflow.annotation.Migration(version = 1, database = TestDatabase.class, priority = 1)
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

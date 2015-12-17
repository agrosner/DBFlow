package com.raizlabs.android.dbflow;

import android.database.sqlite.SQLiteDatabase;

import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;

/**
 * Description:
 */
@Migration(version = 2, database = AppDatabase.class)
public class Migration1 extends BaseMigration {

    @Override
    public void migrate(SQLiteDatabase database) {

    }
}

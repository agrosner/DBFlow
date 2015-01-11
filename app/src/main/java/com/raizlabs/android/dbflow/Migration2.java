package com.raizlabs.android.dbflow;

import android.database.sqlite.SQLiteDatabase;
import com.raizlabs.android.dbflow.annotation.Migration;
import com.raizlabs.android.dbflow.sql.migration.BaseMigration;

/**
 * Description:
 */
@Migration(version = 3, databaseName = "App")
public class Migration2 extends BaseMigration {
    @Override
    public void migrate(SQLiteDatabase database) {

    }
}

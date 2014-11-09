package com.grosner.dbflow.app;

import android.database.sqlite.SQLiteDatabase;
import com.grosner.dbflow.annotation.Migration;
import com.grosner.dbflow.sql.migration.BaseMigration;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description:
 */
@Migration(version = 2, databaseName = "App")
public class Migration1 extends BaseMigration {

    @Override
    public void migrate(SQLiteDatabase database) {

    }
}

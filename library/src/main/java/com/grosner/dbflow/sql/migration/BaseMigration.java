package com.grosner.dbflow.sql.migration;

import android.database.sqlite.SQLiteDatabase;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Provides the base implementation of {@link com.grosner.dbflow.sql.migration.Migration} with
 * only {@link #migrate(android.database.sqlite.SQLiteDatabase)} needing to be implemented.
 */
public abstract class BaseMigration implements Migration {


    @Override
    public void onPreMigrate() {

    }

    @Override
    public abstract void migrate(SQLiteDatabase database);

    @Override
    public void onPostMigrate() {

    }
}

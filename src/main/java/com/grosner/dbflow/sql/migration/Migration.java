package com.grosner.dbflow.sql.migration;

import android.database.sqlite.SQLiteDatabase;

/**
 * Author: andrewgrosner
 * Contributors: { }
 * Description: Called when the Database is migrating. We can perform custom migrations here.
 */
public interface Migration {

    /**
     * Returns the DB migration version
     * @return
     */
    public int getNewVersion();

    /**
     * Called before we migrate data
     */
    public void onPreMigrate();

    /**
     * Perform your migrations here
     * @param database
     */
    public void migrate(SQLiteDatabase database);

    /**
     * Called after the migration completes
     */
    public void onPostMigrate();
}

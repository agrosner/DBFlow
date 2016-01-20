package com.raizlabs.android.dbflow;

import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

/**
 * Description: Provides callbacks for {@link android.database.sqlite.SQLiteOpenHelper} methods
 */
public interface DatabaseHelperListener {

    /**
     * Called when the DB is opened
     *
     * @param database The database that is opened
     */
    void onOpen(DatabaseWrapper database);

    /**
     * Called when the DB is created
     *
     * @param database The database that is created
     */
    void onCreate(DatabaseWrapper database);

    /**
     * Called when the DB is upgraded.
     *  @param database   The database that is upgraded
     * @param oldVersion The previous DB version
     * @param newVersion The new DB version
     */
    void onUpgrade(DatabaseWrapper database, int oldVersion, int newVersion);


}

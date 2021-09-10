package com.raizlabs.android.dbflow.structure.database;

import androidx.annotation.NonNull;

/**
 * Description: Provides callbacks for {@link OpenHelper} methods
 */
public interface DatabaseHelperListener {

    /**
     * Called when the DB is opened
     *
     * @param database The database that is opened
     */
    void onOpen(@NonNull DatabaseWrapper database);

    /**
     * Called when the DB is created
     *
     * @param database The database that is created
     */
    void onCreate(@NonNull DatabaseWrapper database);

    /**
     * Called when the DB is upgraded.
     *
     * @param database   The database that is upgraded
     * @param oldVersion The previous DB version
     * @param newVersion The new DB version
     */
    void onUpgrade(@NonNull DatabaseWrapper database, int oldVersion, int newVersion);

    /**
     * Called when DB is downgraded. Note that this may not be supported by all implementations of the DB.
     *
     * @param databaseWrapper The database downgraded.
     * @param oldVersion      The old. higher version.
     * @param newVersion      The new lower version.
     */
    void onDowngrade(@NonNull DatabaseWrapper databaseWrapper, int oldVersion, int newVersion);
}

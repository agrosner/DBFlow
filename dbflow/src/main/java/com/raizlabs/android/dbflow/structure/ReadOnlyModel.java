package com.raizlabs.android.dbflow.structure;

import com.raizlabs.android.dbflow.sql.migration.Migration;
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper;

public interface ReadOnlyModel {

    /**
     * Loads from the database the most recent version of the model based on it's primary keys.
     */
    void load();

    /**
     * Loads from the database the most recent version of the model based on it's primary keys.
     *
     * @param wrapper Database object to use. Useful for {@link Migration} classes.
     */
    void load(DatabaseWrapper wrapper);

    /**
     * @return true if this object exists in the DB. It combines all of it's primary key fields
     * into a SELECT query and checks to see if any results occur.
     */
    boolean exists();

    /**
     * @return true if this object exists in the DB. It combines all of it's primary key fields
     * into a SELECT query and checks to see if any results occur.
     * @param wrapper Database object to use. Useful for {@link Migration} classes.
     */
    boolean exists(DatabaseWrapper wrapper);
}

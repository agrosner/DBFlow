package com.raizlabs.android.dbflow.structure

import com.raizlabs.android.dbflow.sql.migration.Migration
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

interface ReadOnlyModel {

    /**
     * Loads from the database the most recent version of the model based on it's primary keys.
     */
    fun load()

    /**
     * Loads from the database the most recent version of the model based on it's primary keys.
     *
     * @param wrapper Database object to use. Useful for [Migration] classes.
     */
    fun load(wrapper: DatabaseWrapper)

    /**
     * @return true if this object exists in the DB. It combines all of it's primary key fields
     * into a SELECT query and checks to see if any results occur.
     */
    fun exists(): Boolean

    /**
     * @param wrapper Database object to use. Useful for [Migration] classes.
     * @return true if this object exists in the DB. It combines all of it's primary key fields
     * into a SELECT query and checks to see if any results occur.
     */
    fun exists(wrapper: DatabaseWrapper): Boolean
}

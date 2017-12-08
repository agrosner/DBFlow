package com.raizlabs.dbflow5.adapter

import com.raizlabs.dbflow5.config.DBFlowDatabase
import com.raizlabs.dbflow5.database.DatabaseWrapper

/**
 * Description: The base class for a [T] adapter that defines how it interacts with the DB.
 */
abstract class ModelViewAdapter<T : Any>(databaseDefinition: DBFlowDatabase)
    : RetrievalAdapter<T>(databaseDefinition) {

    /**
     * @return a string of the query that is used to create this model view.
     */
    abstract fun getCreationQuery(wrapper: DatabaseWrapper): String

    /**
     * @return The name of this view in the database
     */
    abstract val viewName: String
}

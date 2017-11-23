package com.raizlabs.android.dbflow.structure

import com.raizlabs.android.dbflow.config.DatabaseDefinition

/**
 * Description: The base class for a [T] adapter that defines how it interacts with the DB.
 */
abstract class ModelViewAdapter<T : Any>(databaseDefinition: DatabaseDefinition)
    : InstanceAdapter<T>(databaseDefinition) {

    /**
     * @return a string of the query that is used to create this model view.
     */
    abstract val creationQuery: String

    /**
     * @return The name of this view in the database
     */
    abstract val viewName: String
}

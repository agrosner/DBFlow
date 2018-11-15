package com.dbflow5.adapter

import com.dbflow5.config.DBFlowDatabase

/**
 * Description: The base class for a [T] adapter that defines how it interacts with the DB.
 */
abstract class ModelViewAdapter<T : Any>(databaseDefinition: DBFlowDatabase)
    : RetrievalAdapter<T>(databaseDefinition), CreationAdapter {

    /**
     * @return The name of this view in the database
     */
    abstract val viewName: String
}

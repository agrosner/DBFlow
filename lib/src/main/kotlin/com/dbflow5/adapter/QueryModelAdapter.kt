package com.dbflow5.adapter

import com.dbflow5.annotation.QueryModel
import com.dbflow5.config.DBFlowDatabase

/**
 * Description: The baseclass for adapters to [QueryModel] that defines how it interacts with the DB. The
 * where query is not defined here, rather its determined by the cursor used.
 */
abstract class QueryModelAdapter<T : Any>(databaseDefinition: DBFlowDatabase)
    : RetrievalAdapter<T>(databaseDefinition)

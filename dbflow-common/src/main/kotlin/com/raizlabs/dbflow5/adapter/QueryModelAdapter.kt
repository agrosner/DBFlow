package com.raizlabs.dbflow5.adapter

import com.raizlabs.dbflow5.annotation.QueryModel
import com.raizlabs.dbflow5.database.DBFlowDatabase
import com.raizlabs.dbflow5.database.DatabaseWrapper
import com.raizlabs.dbflow5.query.OperatorGroup

/**
 * Description: The baseclass for adapters to [QueryModel] that defines how it interacts with the DB. The
 * where query is not defined here, rather its determined by the cursor used.
 */
abstract class QueryModelAdapter<T : Any>(databaseDefinition: DBFlowDatabase)
    : RetrievalAdapter<T>(databaseDefinition) {

    override fun getPrimaryConditionClause(model: T): OperatorGroup {
        throw UnsupportedOperationException("QueryModels cannot check for existence")
    }

    override fun exists(model: T, databaseWrapper: DatabaseWrapper): Boolean {
        throw UnsupportedOperationException("QueryModels cannot check for existence")
    }
}

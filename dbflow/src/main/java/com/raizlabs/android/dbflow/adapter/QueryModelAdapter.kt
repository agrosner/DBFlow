package com.raizlabs.android.dbflow.adapter

import com.raizlabs.android.dbflow.annotation.QueryModel
import com.raizlabs.android.dbflow.config.DatabaseDefinition
import com.raizlabs.android.dbflow.query.OperatorGroup
import com.raizlabs.android.dbflow.database.DatabaseWrapper

/**
 * Description: The baseclass for adapters to [QueryModel] that defines how it interacts with the DB. The
 * where query is not defined here, rather its determined by the query used.
 */
abstract class QueryModelAdapter<T : Any>(databaseDefinition: DatabaseDefinition)
    : InstanceAdapter<T>(databaseDefinition) {

    override fun getPrimaryConditionClause(model: T): OperatorGroup {
        throw UnsupportedOperationException("QueryModels cannot check for existence")
    }

    override fun exists(model: T): Boolean {
        throw UnsupportedOperationException("QueryModels cannot check for existence")
    }

    override fun exists(model: T, databaseWrapper: DatabaseWrapper): Boolean {
        throw UnsupportedOperationException("QueryModels cannot check for existence")
    }
}

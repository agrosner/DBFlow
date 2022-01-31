package com.dbflow5.database.scope

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.config.DBFlowDatabase

/**
 * Description:
 */
class DatabaseScopeImpl<DB : DBFlowDatabase>(override val db: DBFlowDatabase) : DatabaseScope<DB> {

    override fun <T : Any> ModelAdapter<T>.save(model: T): Result<T> {
        return save(model, db)
    }

    override fun <T : Any> ModelAdapter<T>.insert(model: T): Result<T> {
        return insert(model, db)
    }

    override fun <T : Any> ModelAdapter<T>.update(model: T): Result<T> {
        return update(model, db)
    }

    override fun <T : Any> ModelAdapter<T>.delete(model: T): Result<T> {
        return delete(model, db)
    }

    override fun <T : Any> ModelAdapter<T>.exists(model: T): Boolean {
        return exists(model, db)
    }

    override fun <T : Any> ModelAdapter<T>.load(model: T): T? {
        return load(model, db)
    }
}
package com.dbflow5.adapter

import com.dbflow5.adapter.queriable.ListModelLoader
import com.dbflow5.adapter.queriable.SingleModelLoader
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.FlowManager
import com.dbflow5.config.TableConfig
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor
import com.dbflow5.query.OperatorGroup
import com.dbflow5.query.select
import com.dbflow5.query.selectCountOf

/**
 * Description: Provides a base retrieval class for all [Model] backed
 * adapters.
 */
abstract class RetrievalAdapter<T : Any>(databaseDefinition: DBFlowDatabase) {

    /**
     * Overrides the default implementation and allows you to provide your own implementation. Defines
     * how a single [T] is loaded.
     *
     * @param singleModelLoader The loader to use.
     */
    private var _singleModelLoader: SingleModelLoader<T>? = null

    var singleModelLoader: SingleModelLoader<T>
        get() = _singleModelLoader ?: (tableConfig?.singleModelLoader
            ?: createSingleModelLoader()).also { _singleModelLoader = it }
        set(value) {
            this._singleModelLoader = value
        }
    /**
     * @return A new [ListModelLoader], caching will override this loader instance.
     */
    /**
     * Overrides the default implementation and allows you to provide your own implementation. Defines
     * how a list of [T] are loaded.
     *
     * @param listModelLoader The loader to use.
     */
    private var _listModelLoader: ListModelLoader<T>? = null

    var listModelLoader: ListModelLoader<T>
        get() = _listModelLoader ?: (tableConfig?.listModelLoader
            ?: createListModelLoader()).also { _listModelLoader = it }
        set(value) {
            this._listModelLoader = value
        }

    protected val tableConfig: TableConfig<T>? by lazy {
        FlowManager.getConfig()
            .getConfigForDatabase(databaseDefinition.associatedDatabaseClassFile)
            ?.getTableConfigForTable(table)
    }

    /**
     * @return the model class this adapter corresponds to
     */
    abstract val table: Class<T>

    /**
     * @return A new instance of a [SingleModelLoader]. Subsequent calls do not cache
     * this object so it's recommended only calling this in bulk if possible.
     */
    val nonCacheableSingleModelLoader: SingleModelLoader<T>
        get() = SingleModelLoader(table)

    /**
     * @return A new instance of a [ListModelLoader]. Subsequent calls do not cache
     * this object so it's recommended only calling this in bulk if possible.
     */
    val nonCacheableListModelLoader: ListModelLoader<T>
        get() = ListModelLoader(table)

    /**
     * Returns a new [model] based on the object passed in. Will not overwrite existing object.
     */
    open suspend fun load(model: T, databaseWrapper: DatabaseWrapper): T? =
        nonCacheableSingleModelLoader.load(
            databaseWrapper,
            (select
                from table.kotlin
                where getPrimaryConditionClause(model)).query
        )

    /**
     * Converts the specified [FlowCursor] into a new [T]
     *
     * @param cursor The cursor to load into the model
     * @param wrapper The database instance to use.
     */
    abstract suspend fun loadFromCursor(cursor: FlowCursor, wrapper: DatabaseWrapper): T

    /**
     * @param model The model to query values from
     * @return True if it exists as a row in the corresponding database table
     */
    open suspend fun exists(model: T, databaseWrapper: DatabaseWrapper): Boolean = selectCountOf()
        .from(table)
        .where(getPrimaryConditionClause(model))
        .hasData(databaseWrapper)

    /**
     * @param model The primary condition clause.
     * @return The clause that contains necessary primary conditions for this table.
     */
    abstract fun getPrimaryConditionClause(model: T): OperatorGroup

    /**
     * @return A new [ListModelLoader], caching will override this loader instance.
     */
    protected open fun createListModelLoader(): ListModelLoader<T> = ListModelLoader(table)

    /**
     * @return A new [SingleModelLoader], caching will override this loader instance.
     */
    protected open fun createSingleModelLoader(): SingleModelLoader<T> = SingleModelLoader(table)

}
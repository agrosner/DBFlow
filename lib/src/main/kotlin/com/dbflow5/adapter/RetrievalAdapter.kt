package com.dbflow5.adapter

import com.dbflow5.adapter.queriable.ListModelLoader
import com.dbflow5.adapter.queriable.SingleModelLoader
import com.dbflow5.config.FlowManager
import com.dbflow5.config.TableConfig
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor
import com.dbflow5.query.OperatorGroup
import com.dbflow5.query.select
import com.dbflow5.query.selectCountOf
import kotlin.reflect.KClass

/**
 * Description: Provides a base retrieval class for all [Model] backed
 * adapters.
 */
abstract class RetrievalAdapter<T : Any> {

    val singleModelLoader: SingleModelLoader<T> by lazy {
        tableConfig?.singleModelLoader ?: SingleModelLoader(table)
    }

    val listModelLoader: ListModelLoader<T> by lazy {
        tableConfig?.listModelLoader ?: ListModelLoader(table)
    }

    protected val tableConfig: TableConfig<T>? by lazy {
        FlowManager.getConfig().getConfigForTable(table)
    }

    /**
     * @return the model class this adapter corresponds to
     */
    abstract val table: KClass<T>

    /**
     * Returns a new [model] based on the object passed in. Will not overwrite existing object.
     */
    fun loadSingle(model: T, databaseWrapper: DatabaseWrapper): T? =
        singleModelLoader.load(
            databaseWrapper,
            (select
                from table
                where getPrimaryConditionClause(model)).query
        )

    fun loadSingle(databaseWrapper: DatabaseWrapper, query: String) =
        singleModelLoader.load(databaseWrapper, query)

    fun loadSingle(flowCursor: FlowCursor?, databaseWrapper: DatabaseWrapper) =
        singleModelLoader.load(flowCursor, databaseWrapper)

    fun loadList(databaseWrapper: DatabaseWrapper, query: String) =
        listModelLoader.load(databaseWrapper, query)

    fun loadList(flowCursor: FlowCursor?, databaseWrapper: DatabaseWrapper) =
        listModelLoader.load(flowCursor, databaseWrapper)

    /**
     * Converts the specified [FlowCursor] into a new [T]
     *
     * @param cursor The cursor to load into the model
     * @param wrapper The database instance to use.
     */
    abstract fun loadFromCursor(cursor: FlowCursor, wrapper: DatabaseWrapper): T

    /**
     * @param model The model to query values from
     * @return True if it exists as a row in the corresponding database table
     */
    open fun exists(model: T, databaseWrapper: DatabaseWrapper): Boolean = selectCountOf()
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
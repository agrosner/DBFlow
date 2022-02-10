package com.dbflow5.adapter

import com.dbflow5.adapter.queriable.ListModelLoader
import com.dbflow5.adapter.queriable.SingleModelLoader
import com.dbflow5.config.FlowManager
import com.dbflow5.config.TableConfig
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor
import com.dbflow5.query.HasTable
import com.dbflow5.query2.operations.OperatorGroup
import kotlin.reflect.KClass

/**
 * Description: Provides a base retrieval class for all [Model] backed
 * adapters.
 */
abstract class RetrievalAdapter<T : Any> : HasTable<T> {

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
    abstract override val table: KClass<T>

    suspend fun loadSingle(databaseWrapper: DatabaseWrapper, query: String) =
        singleModelLoader.load(databaseWrapper, query)

    suspend fun loadSingle(flowCursor: FlowCursor, databaseWrapper: DatabaseWrapper) =
        singleModelLoader.load(flowCursor, databaseWrapper)

    suspend fun loadList(databaseWrapper: DatabaseWrapper, query: String) =
        listModelLoader.load(databaseWrapper, query)

    suspend fun loadList(flowCursor: FlowCursor, databaseWrapper: DatabaseWrapper) =
        listModelLoader.load(flowCursor, databaseWrapper)

    /**
     * Converts the specified [FlowCursor] into a new [T]
     *
     * @param cursor The cursor to load into the model
     * @param wrapper The database instance to use.
     */
    abstract suspend fun loadFromCursor(cursor: FlowCursor, wrapper: DatabaseWrapper): T

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
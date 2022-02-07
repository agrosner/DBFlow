package com.dbflow5.adapter

import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.config.FlowManager
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.database.FlowCursor
import com.dbflow5.query.OperatorGroup
import com.dbflow5.query.select
import com.dbflow5.query.selectCountOf
import kotlin.reflect.KClass

/**
 * Description: Represents an adapter that can both retrieve and create in DB.
 */
abstract class SQLObjectAdapter<TModel : Any> : RetrievalAdapter<TModel>(), CreationAdapter {
    /**
     * Returns a new [model] based on the object passed in. Will not overwrite existing object.
     */
    suspend fun loadSingle(model: TModel, databaseWrapper: DatabaseWrapper): TModel? =
        singleModelLoader.load(
            databaseWrapper,
            (select
                from this
                where getPrimaryConditionClause(model)).query
        )


    /**
     * @param model The model to query values from
     * @return True if it exists as a row in the corresponding database table
     */
    open fun exists(model: TModel, databaseWrapper: DatabaseWrapper): Boolean = selectCountOf()
        .where(getPrimaryConditionClause(model))
        .hasData(databaseWrapper)
}

@InternalDBFlowApi
fun <T : Any> makeLazySQLObjectAdapter(
    sqlObject: KClass<T>,
): SQLObjectAdapter<T> = LazySQLObjectAdapter(sqlObject)

/**
 * Used by inner library classes.
 * Lazily looks up sqlObject from [FlowManager] to get around initialization on top-level
 * Properties or classes that need an adapter.
 */
internal class LazySQLObjectAdapter<T : Any>(
    sqlObject: KClass<T>
) : SQLObjectAdapter<T>() {
    private val realAdapter by lazy { FlowManager.getSQLObjectAdapter(sqlObject) }

    override val creationQuery: String
        get() = realAdapter.creationQuery
    override val name: String
        get() = realAdapter.name
    override val type: ObjectType
        get() = realAdapter.type
    override val table: KClass<T>
        get() = realAdapter.table

    override suspend fun loadFromCursor(cursor: FlowCursor, wrapper: DatabaseWrapper): T =
        realAdapter.loadFromCursor(cursor, wrapper)

    override fun getPrimaryConditionClause(model: T): OperatorGroup =
        realAdapter.getPrimaryConditionClause(model)
}

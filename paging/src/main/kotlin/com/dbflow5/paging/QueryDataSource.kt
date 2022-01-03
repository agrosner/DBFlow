package com.dbflow5.paging

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.FlowManager
import com.dbflow5.observing.OnTableChangedObserver
import com.dbflow5.query.ModelQueriable
import com.dbflow5.query.Select
import com.dbflow5.query.Transformable
import com.dbflow5.query.WhereBase
import com.dbflow5.query.extractFrom
import com.dbflow5.query.selectCountOf

/**
 * Bridges the [ModelQueriable] into a [PositionalDataSource] that loads a [ModelQueriable].
 */
class QueryDataSource<T : Any, TQuery>
internal constructor(
    private val transformable: TQuery,
    private val database: DBFlowDatabase
) : PositionalDataSource<T>() where TQuery : Transformable<T>, TQuery : ModelQueriable<T> {

    private val associatedTables: Set<Class<*>> = transformable.extractFrom()?.associatedTables
        ?: setOf(transformable.table)

    private val onTableChangedObserver =
        object : OnTableChangedObserver(associatedTables.toList()) {
            override fun onChanged(tables: Set<Class<*>>) {
                if (tables.isNotEmpty()) {
                    invalidate()
                }
            }
        }

    init {
        if (transformable is WhereBase<*> && transformable.queryBuilderBase !is Select) {
            throw IllegalArgumentException("Cannot pass a non-SELECT cursor into this data source.")
        }

        val db = FlowManager.getDatabaseForTable(associatedTables.first())
        // force initialize the db
        db.writableDatabase

        val observer = db.tableObserver
        // From could be part of many joins, so we register for all affected tables here.
        observer.addOnTableChangedObserver(onTableChangedObserver)

    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<T>) {
        database.transact { db ->
            val result =
                transformable.constrain(params.startPosition.toLong(), params.loadSize.toLong())
                    .queryList(db)
            callback.onResult(result)
        }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<T>) {
        database.transact { db ->
            val count = selectCountOf().from(transformable).longValue(db)
            val max = when {
                params.requestedLoadSize >= count - 1 -> count.toInt()
                else -> params.requestedLoadSize
            }
            val list = transformable.constrain(params.requestedStartPosition.toLong(), max.toLong())
                .queryList(db)
            callback.onResult(list, params.requestedStartPosition, count.toInt())
        }
    }

    class Factory<T : Any, TQuery>
    internal constructor(
        private val transformable: TQuery,
        private val database: DBFlowDatabase
    ) : DataSource.Factory<Int, T>() where TQuery : Transformable<T>, TQuery : ModelQueriable<T> {
        override fun create(): DataSource<Int, T> = QueryDataSource(transformable, database)
    }

    companion object {
        @JvmStatic
        fun <T : Any, TQuery> newFactory(transformable: TQuery, database: DBFlowDatabase)
            where TQuery : Transformable<T>, TQuery : ModelQueriable<T> =
            Factory(transformable, database)
    }
}

fun <T : Any, TQuery> TQuery.toDataSourceFactory(database: DBFlowDatabase)
    where TQuery : Transformable<T>, TQuery : ModelQueriable<T> =
    QueryDataSource.newFactory(this, database)
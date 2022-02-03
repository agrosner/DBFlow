package com.dbflow5.paging

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.beginTransactionAsync
import com.dbflow5.observing.OnTableChangedObserver
import com.dbflow5.query.ModelQueriable
import com.dbflow5.query.Select
import com.dbflow5.query.Transformable
import com.dbflow5.query.WhereBase
import com.dbflow5.query.extractFrom
import com.dbflow5.query.selectCountOf
import kotlin.reflect.KClass

/**
 * Bridges the [ModelQueriable] into a [PositionalDataSource] that loads a [ModelQueriable].
 */
class QueryDataSource<T : Any, TQuery>
internal constructor(
    private val db: DBFlowDatabase,
    private val transformable: TQuery,
) : PositionalDataSource<T>() where TQuery : Transformable<T>, TQuery : ModelQueriable<T> {

    private val associatedTables: Set<KClass<*>> = transformable.extractFrom()?.associatedTables
        ?: setOf(transformable.table)

    private val onTableChangedObserver =
        object : OnTableChangedObserver(associatedTables.toList()) {
            override fun onChanged(tables: Set<KClass<*>>) {
                if (tables.isNotEmpty()) {
                    invalidate()
                }
            }
        }

    init {
        if (transformable is WhereBase<*> && transformable.queryBuilderBase !is Select) {
            throw IllegalArgumentException("Cannot pass a non-SELECT cursor into this data source.")
        }

        // force initialize the db
        db.writableDatabase

        val observer = db.tableObserver
        // From could be part of many joins, so we register for all affected tables here.
        observer.addOnTableChangedObserver(onTableChangedObserver)

    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<T>) {
        db.beginTransactionAsync {
            transformable.constrain(params.startPosition.toLong(), params.loadSize.toLong())
                .queryList(db)
        }.enqueue { _, list -> callback.onResult(list) }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<T>) {
        db.beginTransactionAsync { selectCountOf().from(transformable).longValue(db) }
            .enqueue { _, count ->
                val max = when {
                    params.requestedLoadSize >= count - 1 -> count.toInt()
                    else -> params.requestedLoadSize
                }
                db.beginTransactionAsync {
                    transformable.constrain(params.requestedStartPosition.toLong(), max.toLong())
                        .queryList(db)
                }.enqueue { _, list ->
                    callback.onResult(list, params.requestedStartPosition, count.toInt())
                }
            }
    }

    class Factory<T : Any, TQuery>
    internal constructor(
        private val transformable: TQuery,
        private val database: DBFlowDatabase
    ) : DataSource.Factory<Int, T>() where TQuery : Transformable<T>, TQuery : ModelQueriable<T> {
        override fun create(): DataSource<Int, T> = QueryDataSource(database, transformable)
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
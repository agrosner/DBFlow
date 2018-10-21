package com.dbflow5.paging

import android.arch.paging.DataSource
import android.arch.paging.PositionalDataSource
import com.dbflow5.config.DBFlowDatabase
import com.dbflow5.config.FlowManager
import com.dbflow5.query.ModelQueriable
import com.dbflow5.query.Select
import com.dbflow5.query.Transformable
import com.dbflow5.query.WhereBase
import com.dbflow5.query.constrain
import com.dbflow5.query.extractFrom
import com.dbflow5.query.selectCountOf
import com.dbflow5.runtime.TableNotifierRegister
import com.dbflow5.runtime.setListener

/**
 * Bridges the [ModelQueriable] into a [PositionalDataSource] that loads a [ModelQueriable].
 */
class QueryDataSource<T : Any, TQuery>
internal constructor(private val transformable: TQuery,
                     private val database: DBFlowDatabase)
    : PositionalDataSource<T>() where TQuery : Transformable<T>, TQuery : ModelQueriable<T> {

    private val register: TableNotifierRegister = FlowManager.newRegisterForTable(transformable.table)

    private val associatedTables: Set<Class<*>> = transformable.extractFrom()?.associatedTables
            ?: setOf(transformable.table)

    init {
        if (transformable is WhereBase<*> && transformable.queryBuilderBase !is Select) {
            throw IllegalArgumentException("Cannot pass a non-SELECT cursor into this data source.")
        }

        // From could be part of many joins, so we register for all affected tables here.
        for (table in associatedTables) {
            register.register(table)
        }

        register.setListener { _, _ -> invalidate() }
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<T>) {
        database.beginTransactionAsync { db ->
            transformable.constrain(params.startPosition.toLong(), params.loadSize.toLong())
                    .queryList(db)
        }.execute { _, list -> callback.onResult(list) }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<T>) {
        database.beginTransactionAsync { db -> selectCountOf().from(transformable).longValue(db) }
                .execute { _, count ->
                    val max = when {
                        params.requestedLoadSize >= count - 1 -> count.toInt()
                        else -> params.requestedLoadSize
                    }
                    database.beginTransactionAsync { db ->
                        transformable.constrain(params.requestedStartPosition.toLong(), max.toLong()).queryList(db)
                    }.execute { _, list ->
                        callback.onResult(list, params.requestedStartPosition, count.toInt())
                    }
                }
    }

    class Factory<T : Any, TQuery>
    internal constructor(private val transformable: TQuery,
                         private val database: DBFlowDatabase)
        : DataSource.Factory<Int, T>() where TQuery : Transformable<T>, TQuery : ModelQueriable<T> {
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
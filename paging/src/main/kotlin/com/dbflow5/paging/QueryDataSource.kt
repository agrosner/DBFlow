package com.dbflow5.paging

import androidx.paging.DataSource
import androidx.paging.PositionalDataSource
import com.dbflow5.adapter.DBRepresentable
import com.dbflow5.database.GeneratedDatabase
import com.dbflow5.database.beginTransactionAsync
import com.dbflow5.observing.OnTableChangedObserver
import com.dbflow5.query.Constrainable
import com.dbflow5.query.ExecutableQuery
import com.dbflow5.query.HasAdapter
import com.dbflow5.query.HasAssociatedAdapters
import com.dbflow5.query.SelectResult
import com.dbflow5.query.selectCountOf

/**
 * Enables taking an [ExecutableQuery] of type [SelectResult] and paginating its results.
 */
class QueryDataSource<Table : Any, Q>
internal constructor(
    private val db: GeneratedDatabase,
    private val executableQuery: Q,
) : PositionalDataSource<Table>()
    where Q : ExecutableQuery<SelectResult<Table>>,
          Q : HasAssociatedAdapters,
          Q : HasAdapter<Table, DBRepresentable<Table>>,
          Q : Constrainable<Table, SelectResult<Table>, Q, *> {

    private val onTableChangedObserver =
        OnTableChangedObserver(executableQuery.associatedAdapters) {
            invalidate()
        }

    init {
        // force initialize the db
        db.writableDatabase

        val observer = db.tableObserver
        // From could be part of many joins, so we register for all affected tables here.
        observer.addOnTableChangedObserver(onTableChangedObserver)
    }

    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<Table>) {
        db.beginTransactionAsync {
            executableQuery.constrain(params.startPosition.toLong(), params.loadSize.toLong())
                .list()
        }.enqueue { _, list -> callback.onResult(list) }
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<Table>) {
        db.beginTransactionAsync { executableQuery.adapter.selectCountOf().execute(db).value }
            .enqueue { _, count ->
                val max = when {
                    params.requestedLoadSize >= count - 1 -> count.toInt()
                    else -> params.requestedLoadSize
                }
                db.beginTransactionAsync {
                    executableQuery.constrain(params.requestedStartPosition.toLong(), max.toLong())
                        .list()
                }.enqueue { _, list ->
                    callback.onResult(list, params.requestedStartPosition, count.toInt())
                }
            }
    }

    class Factory<Table : Any, Q>
    internal constructor(
        private val executableQuery: Q,
        private val database: GeneratedDatabase
    ) : DataSource.Factory<Int, Table>()
        where Q : ExecutableQuery<SelectResult<Table>>,
              Q : HasAssociatedAdapters,
              Q : HasAdapter<Table, DBRepresentable<Table>>,
              Q : Constrainable<Table, SelectResult<Table>, Q, *> {
        override fun create(): DataSource<Int, Table> = QueryDataSource(database, executableQuery)
    }

    companion object {
        @JvmStatic
        fun <Table : Any, Q> newFactory(executableQuery: Q, database: GeneratedDatabase)
            where Q : ExecutableQuery<SelectResult<Table>>,
                  Q : HasAssociatedAdapters,
                  Q : HasAdapter<Table, DBRepresentable<Table>>,
                  Q : Constrainable<Table, SelectResult<Table>, Q, DBRepresentable<Table>> =
            Factory(executableQuery, database)
    }
}

fun <Table : Any, Q>
    Q.toDataSourceFactory(database: GeneratedDatabase)
    where Q : ExecutableQuery<SelectResult<Table>>,
          Q : HasAdapter<Table, DBRepresentable<Table>>,
          Q : HasAssociatedAdapters,
          Q : Constrainable<Table, SelectResult<Table>, Q, DBRepresentable<Table>> =
    QueryDataSource.newFactory(this, database)

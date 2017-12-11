package com.raizlabs.dbflow5.paging

import android.arch.paging.DataSource
import android.arch.paging.PositionalDataSource
import com.raizlabs.dbflow5.config.DBFlowDatabase
import com.raizlabs.dbflow5.query.ModelQueriable

/**
 * Bridges the [ModelQueriable] into a [PositionalDataSource] that loads a [ModelQueriable].
 */
class ModelQueriableDataSource<T : Any>
internal constructor(private val modelQueriable: ModelQueriable<T>,
                     private val database: DBFlowDatabase)
    : PositionalDataSource<T>() {
    override fun loadRange(params: LoadRangeParams, callback: LoadRangeCallback<T>) {
        database.executeTransactionAsync({ modelQueriable.cursorList() },
            success = { _, paramCursorList ->
                paramCursorList.use { cursorList ->
                    val list = mutableListOf<T>()
                    val max = when {
                        params.loadSize >= cursorList.count - 1 -> cursorList.count.toInt()
                        else -> params.loadSize
                    }
                    (params.startPosition until params.startPosition + max).mapTo(list) { cursorList[it] }
                    callback.onResult(list)
                    cursorList.close()
                }
            })
    }

    override fun loadInitial(params: LoadInitialParams, callback: LoadInitialCallback<T>) {
        database.executeTransactionAsync({ modelQueriable.cursorList() },
            success = { _, paramCursorList ->
                paramCursorList.use { cursorList ->
                    val max = when {
                        params.requestedLoadSize >= cursorList.count - 1 -> cursorList.count.toInt()
                        else -> params.requestedLoadSize
                    }
                    val list = mutableListOf<T>()
                    (params.requestedStartPosition until params.requestedStartPosition + max).mapTo(list) { cursorList[it] }
                    callback.onResult(list, params.requestedStartPosition, cursorList.count.toInt())
                }
            })
    }

    class Factory<T : Any>
    internal constructor(private val modelQueriable: ModelQueriable<T>,
                         private val database: DBFlowDatabase) : DataSource.Factory<Int, T> {
        override fun create(): DataSource<Int, T> = ModelQueriableDataSource(modelQueriable, database)
    }

    companion object {
        @JvmStatic
        fun <T : Any> newFactory(modelQueriable: ModelQueriable<T>,
                                 database: DBFlowDatabase) =
            Factory(modelQueriable, database)
    }
}

fun <T : Any> ModelQueriable<T>.toDataSourceFactory(database: DBFlowDatabase) =
    ModelQueriableDataSource.newFactory(this, database)
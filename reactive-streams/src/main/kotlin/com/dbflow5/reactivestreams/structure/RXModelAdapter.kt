package com.dbflow5.reactivestreams.structure

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.config.modelAdapter
import com.dbflow5.database.DatabaseWrapper
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single
import kotlinx.coroutines.rx3.rxCompletable
import kotlinx.coroutines.rx3.rxSingle

/**
 * Description: Wraps most [ModelAdapter] modification operations into RX-style constructs.
 */
class RXModelAdapter<T : Any> internal constructor(private val modelAdapter: ModelAdapter<T>) :
    RXRetrievalAdapter<T>(modelAdapter) {

    constructor(table: Class<T>) : this(table.modelAdapter)

    fun save(model: T, databaseWrapper: DatabaseWrapper): Single<Result<T>> =
        rxSingle { modelAdapter.save(model, databaseWrapper) }

    fun saveAll(models: Collection<T>, databaseWrapper: DatabaseWrapper): Completable =
        rxCompletable {
            modelAdapter.saveAll(models, databaseWrapper)
        }

    fun insert(model: T, databaseWrapper: DatabaseWrapper): Single<Result<T>> =
        rxSingle { modelAdapter.insert(model, databaseWrapper) }

    fun insertAll(
        models: Collection<T>,
        databaseWrapper: DatabaseWrapper
    ): Completable = rxCompletable {
        modelAdapter.insertAll(models, databaseWrapper)
    }

    fun update(model: T, databaseWrapper: DatabaseWrapper): Single<Result<T>> =
        rxSingle { modelAdapter.update(model, databaseWrapper) }

    fun updateAll(
        models: Collection<T>,
        databaseWrapper: DatabaseWrapper
    ): Single<Result<Collection<T>>> =
        rxSingle {
            modelAdapter.updateAll(models, databaseWrapper)
        }

    fun delete(model: T, databaseWrapper: DatabaseWrapper): Single<Result<T>> =
        rxSingle { modelAdapter.delete(model, databaseWrapper) }

    fun deleteAll(
        models: Collection<T>,
        databaseWrapper: DatabaseWrapper
    ): Single<Result<Collection<T>>> =
        rxSingle {
            modelAdapter.deleteAll(models, databaseWrapper)
        }

    companion object {

        @JvmStatic
        fun <T : Any> from(modelAdapter: ModelAdapter<T>): RXModelAdapter<T> =
            RXModelAdapter(modelAdapter)

        @JvmStatic
        fun <T : Any> from(table: Class<T>): RXModelAdapter<T> = RXModelAdapter(table)
    }
}

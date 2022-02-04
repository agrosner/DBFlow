package com.dbflow5.reactivestreams.structure

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.database.DatabaseWrapper
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Completable.fromCallable
import io.reactivex.rxjava3.core.Single

/**
 * Description: Wraps most [ModelAdapter] modification operations into RX-style constructs.
 */
class RXModelAdapter<T : Any> internal constructor(private val modelAdapter: ModelAdapter<T>) :
    RXRetrievalAdapter<T>(modelAdapter) {

    fun save(model: T, databaseWrapper: DatabaseWrapper): Single<Result<T>> =
        Single.fromCallable { modelAdapter.save(model, databaseWrapper) }

    fun saveAll(models: Collection<T>, databaseWrapper: DatabaseWrapper): Completable =
        fromCallable {
            modelAdapter.saveAll(models, databaseWrapper)
            null
        }

    fun insert(model: T, databaseWrapper: DatabaseWrapper): Single<Result<T>> =
        Single.fromCallable { modelAdapter.insert(model, databaseWrapper) }

    fun insertAll(
        models: Collection<T>,
        databaseWrapper: DatabaseWrapper
    ): Completable = fromCallable {
        modelAdapter.insertAll(models, databaseWrapper)
        null
    }

    fun update(model: T, databaseWrapper: DatabaseWrapper): Single<Result<T>> =
        Single.fromCallable { modelAdapter.update(model, databaseWrapper) }

    fun updateAll(models: Collection<T>, databaseWrapper: DatabaseWrapper): Completable =
        fromCallable {
            modelAdapter.updateAll(models, databaseWrapper)
            null
        }

    fun delete(model: T, databaseWrapper: DatabaseWrapper): Single<Result<T>> =
        Single.fromCallable { modelAdapter.delete(model, databaseWrapper) }

    fun deleteAll(models: Collection<T>, databaseWrapper: DatabaseWrapper): Completable =
        fromCallable {
            modelAdapter.deleteAll(models, databaseWrapper)
            null
        }

    companion object {

        @JvmStatic
        fun <T : Any> from(modelAdapter: ModelAdapter<T>): RXModelAdapter<T> =
            RXModelAdapter(modelAdapter)

    }
}

package com.raizlabs.android.dbflow.rx.structure

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.structure.ModelAdapter
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper
import rx.Completable
import rx.Single

/**
 * Description: Wraps most [ModelAdapter] modification operations into RX-style constructs.
 */
class RXModelAdapter<T : Any> internal constructor(private val modelAdapter: ModelAdapter<T>)
    : RXRetrievalAdapter<T>(modelAdapter) {

    constructor(table: Class<T>) : this(FlowManager.getModelAdapter<T>(table))

    fun save(model: T): Single<Boolean> = Single.fromCallable { modelAdapter.save(model) }

    fun save(model: T, databaseWrapper: DatabaseWrapper): Single<Boolean> =
            Single.fromCallable { modelAdapter.save(model, databaseWrapper) }

    fun saveAll(models: Collection<T>): Completable = Completable.fromCallable {
        modelAdapter.saveAll(models)
        null
    }

    fun saveAll(models: Collection<T>, databaseWrapper: DatabaseWrapper): Completable =
            Completable.fromCallable {
                modelAdapter.saveAll(models, databaseWrapper)
                null
            }

    fun insert(model: T): Single<Long> = Single.fromCallable { modelAdapter.insert(model) }

    fun insert(model: T, databaseWrapper: DatabaseWrapper): Single<Long> =
            Single.fromCallable { modelAdapter.insert(model, databaseWrapper) }

    fun insertAll(models: Collection<T>): Completable = Completable.fromCallable {
        modelAdapter.insertAll(models)
        null
    }

    fun insertAll(models: Collection<T>,
                  databaseWrapper: DatabaseWrapper): Completable = Completable.fromCallable {
        modelAdapter.insertAll(models, databaseWrapper)
        null
    }

    fun update(model: T): Single<Boolean> = Single.fromCallable { modelAdapter.update(model) }

    fun update(model: T, databaseWrapper: DatabaseWrapper): Single<Boolean> =
            Single.fromCallable { modelAdapter.update(model, databaseWrapper) }

    fun updateAll(models: Collection<T>): Completable = Completable.fromCallable {
        modelAdapter.updateAll(models)
        null
    }

    fun updateAll(models: Collection<T>, databaseWrapper: DatabaseWrapper): Completable =
            Completable.fromCallable {
                modelAdapter.updateAll(models, databaseWrapper)
                null
            }

    fun delete(model: T): Single<Boolean> = Single.fromCallable { modelAdapter.delete(model) }

    fun delete(model: T, databaseWrapper: DatabaseWrapper): Single<Boolean> =
            Single.fromCallable { modelAdapter.delete(model, databaseWrapper) }

    fun deleteAll(models: Collection<T>): Completable = Completable.fromCallable {
        modelAdapter.deleteAll(models)
        null
    }

    fun deleteAll(models: Collection<T>, databaseWrapper: DatabaseWrapper): Completable =
            Completable.fromCallable {
                modelAdapter.deleteAll(models, databaseWrapper)
                null
            }

    companion object {

        @JvmStatic
        fun <T : Any> from(modelAdapter: ModelAdapter<T>): RXModelAdapter<T> =
                RXModelAdapter(modelAdapter)

        @JvmStatic
        fun <T : Any> from(table: Class<T>): RXModelAdapter<T> = RXModelAdapter(table)
    }
}

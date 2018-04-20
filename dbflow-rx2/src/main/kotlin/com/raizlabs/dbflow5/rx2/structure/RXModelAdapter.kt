package com.raizlabs.dbflow5.rx2.structure

import com.raizlabs.dbflow5.adapter.ModelAdapter
import com.raizlabs.dbflow5.config.modelAdapter
import com.raizlabs.dbflow5.database.DatabaseWrapper
import io.reactivex.Completable
import io.reactivex.Completable.fromCallable
import io.reactivex.Single
import kotlin.reflect.KClass

/**
 * Description: Wraps most [ModelAdapter] modification operations into RX-style constructs.
 */
class RXModelAdapter<T : Any> internal constructor(private val modelAdapter: ModelAdapter<T>)
    : RXRetrievalAdapter<T>(modelAdapter) {

    internal constructor(table: KClass<T>) : this(table.modelAdapter)

    fun save(model: T, databaseWrapper: DatabaseWrapper): Single<Boolean> =
        Single.fromCallable { modelAdapter.save(model, databaseWrapper) }

    fun saveAll(models: Collection<T>, databaseWrapper: DatabaseWrapper): Completable =
        fromCallable {
            modelAdapter.saveAll(models, databaseWrapper)
            null
        }

    fun insert(model: T, databaseWrapper: DatabaseWrapper): Single<Long> =
        Single.fromCallable { modelAdapter.insert(model, databaseWrapper) }

    fun insertAll(models: Collection<T>,
                  databaseWrapper: DatabaseWrapper): Completable = fromCallable {
        modelAdapter.insertAll(models, databaseWrapper)
        null
    }

    fun update(model: T, databaseWrapper: DatabaseWrapper): Single<Boolean> =
        Single.fromCallable { modelAdapter.update(model, databaseWrapper) }

    fun updateAll(models: Collection<T>, databaseWrapper: DatabaseWrapper): Completable =
        fromCallable {
            modelAdapter.updateAll(models, databaseWrapper)
            null
        }

    fun delete(model: T, databaseWrapper: DatabaseWrapper): Single<Boolean> =
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

        @JvmStatic
        fun <T : Any> from(table: KClass<T>): RXModelAdapter<T> = RXModelAdapter(table)

        @JvmStatic
        fun <T : Any> from(table: Class<T>): RXModelAdapter<T> = RXModelAdapter(table.kotlin)
    }
}

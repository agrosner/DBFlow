package com.dbflow5.reactivestreams.structure

import com.dbflow5.adapter.ModelAdapter
import com.dbflow5.annotation.ColumnIgnore
import com.dbflow5.config.FlowManager
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.structure.BaseModel
import com.dbflow5.structure.InvalidDBConfiguration
import io.reactivex.rxjava3.core.Completable
import io.reactivex.rxjava3.core.Single

/**
 * Description: Similar to [BaseModel] with RX constructs. Extend this for convenience methods.
 */
open class BaseRXModel {

    /**
     * @return The associated [ModelAdapter]. The [FlowManager]
     * may throw a [InvalidDBConfiguration] for this call if this class
     * is not associated with a table, so be careful when using this method.
     */
    @delegate:ColumnIgnore
    @delegate:Transient
    private val rxModelAdapter: RXModelAdapter<BaseRXModel> by lazy { RXModelAdapter(javaClass) }

    fun save(databaseWrapper: DatabaseWrapper): Single<Boolean> =
            rxModelAdapter.save(this, databaseWrapper)

    fun load(databaseWrapper: DatabaseWrapper): Completable =
            rxModelAdapter.load(this, databaseWrapper)

    fun delete(databaseWrapper: DatabaseWrapper): Single<Boolean> =
            rxModelAdapter.delete(this, databaseWrapper)

    fun update(databaseWrapper: DatabaseWrapper): Single<Boolean> =
            rxModelAdapter.update(this, databaseWrapper)

    fun insert(databaseWrapper: DatabaseWrapper): Single<Long> =
            rxModelAdapter.insert(this, databaseWrapper)

    fun exists(databaseWrapper: DatabaseWrapper): Single<Boolean> =
            rxModelAdapter.exists(this, databaseWrapper)
}

fun <T : Any> T.rxSave(databaseWrapper: DatabaseWrapper): Single<Boolean> =
        RXModelAdapter(javaClass).save(this, databaseWrapper)

fun <T : Any> T.rxLoad(databaseWrapper: DatabaseWrapper): Completable =
        RXModelAdapter(javaClass).load(this, databaseWrapper)

fun <T : Any> T.rxDelete(databaseWrapper: DatabaseWrapper): Single<Boolean> =
        RXModelAdapter(javaClass).delete(this, databaseWrapper)

fun <T : Any> T.rxUpdate(databaseWrapper: DatabaseWrapper): Single<Boolean> =
        RXModelAdapter(javaClass).update(this, databaseWrapper)

fun <T : Any> T.rxInsert(databaseWrapper: DatabaseWrapper): Single<Long> =
        RXModelAdapter(javaClass).insert(this, databaseWrapper)

fun <T : Any> T.rxExists(databaseWrapper: DatabaseWrapper): Single<Boolean> =
        RXModelAdapter(javaClass).exists(this, databaseWrapper)
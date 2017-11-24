package com.raizlabs.android.dbflow.rx.structure

import com.raizlabs.android.dbflow.annotation.ColumnIgnore
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.structure.BaseModel
import com.raizlabs.android.dbflow.structure.InvalidDBConfiguration
import com.raizlabs.android.dbflow.structure.ModelAdapter
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

import rx.Completable
import rx.Single

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

    fun DatabaseWrapper.save(): Single<Boolean> =
            rxModelAdapter.save(this@BaseRXModel, this)

    fun DatabaseWrapper.load(): Completable =
            rxModelAdapter.load(this@BaseRXModel, this)

    fun DatabaseWrapper.delete(): Single<Boolean> =
            rxModelAdapter.delete(this@BaseRXModel, this)

    fun DatabaseWrapper.update(): Single<Boolean> =
            rxModelAdapter.update(this@BaseRXModel, this)

    fun DatabaseWrapper.insert(): Single<Long> =
            rxModelAdapter.insert(this@BaseRXModel, this)

    fun DatabaseWrapper.exists(): Single<Boolean> =
            rxModelAdapter.exists(this@BaseRXModel, this)
}

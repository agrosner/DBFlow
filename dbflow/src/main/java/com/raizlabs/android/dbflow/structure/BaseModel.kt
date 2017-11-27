package com.raizlabs.android.dbflow.structure

import com.raizlabs.android.dbflow.annotation.ColumnIgnore
import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

/**
 * Description: The base implementation of [Model]. It is recommended to use this class as
 * the base for your [Model], but it is not required.
 */
@Deprecated("No subclass needed. Use extension methods instead")
open class BaseModel : Model {

    /**
     * @return The associated [ModelAdapter]. The [FlowManager]
     * may throw a [InvalidDBConfiguration] for this call if this class
     * is not associated with a table, so be careful when using this method.
     */
    @delegate:ColumnIgnore
    @delegate:Transient
    val modelAdapter: ModelAdapter<BaseModel> by lazy { FlowManager.getModelAdapter(javaClass) }

    override fun load(wrapper: DatabaseWrapper) {
        modelAdapter.load(this, wrapper)
    }

    override fun save(wrapper: DatabaseWrapper): Boolean = wrapper.saveModel()

    override fun delete(wrapper: DatabaseWrapper): Boolean = wrapper.deleteModel()

    override fun update(wrapper: DatabaseWrapper): Boolean = modelAdapter.update(this, wrapper)

    override fun insert(wrapper: DatabaseWrapper): Long = modelAdapter.insert(this, wrapper)

    override fun exists(wrapper: DatabaseWrapper): Boolean = modelAdapter.exists(this, wrapper)

    protected fun DatabaseWrapper.saveModel(): Boolean = modelAdapter.save(this@BaseModel, this)

    protected fun DatabaseWrapper.deleteModel(): Boolean = modelAdapter.delete(this@BaseModel, this)

    protected fun DatabaseWrapper.updateModel(): Boolean = modelAdapter.update(this@BaseModel, this)

    protected fun DatabaseWrapper.insertModel(): Long = modelAdapter.insert(this@BaseModel, this)

    protected fun DatabaseWrapper.existsModel(): Boolean = modelAdapter.exists(this@BaseModel, this)

}

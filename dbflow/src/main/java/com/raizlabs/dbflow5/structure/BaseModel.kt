package com.raizlabs.dbflow5.structure

import com.raizlabs.dbflow5.adapter.ModelAdapter
import com.raizlabs.dbflow5.annotation.ColumnIgnore
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.config.modelAdapter
import com.raizlabs.dbflow5.database.DatabaseWrapper

/**
 * Description: The base implementation of [Model]. It is recommended to use this class as
 * the base for your [Model], but it is not required.
 */
open class BaseModel : Model {

    /**
     * @return The associated [ModelAdapter]. The [FlowManager]
     * may throw a [InvalidDBConfiguration] for this call if this class
     * is not associated with a table, so be careful when using this method.
     */
    @delegate:ColumnIgnore
    @delegate:Transient
    val modelAdapter: ModelAdapter<BaseModel> by lazy { javaClass.modelAdapter }

    @Suppress("UNCHECKED_CAST")
    override fun <T> load(wrapper: DatabaseWrapper): T? = modelAdapter.load(this, wrapper) as T?

    override fun save(wrapper: DatabaseWrapper): Boolean = modelAdapter.save(this, wrapper)

    override fun delete(wrapper: DatabaseWrapper): Boolean = modelAdapter.delete(this, wrapper)

    override fun update(wrapper: DatabaseWrapper): Boolean = modelAdapter.update(this, wrapper)

    override fun insert(wrapper: DatabaseWrapper): Long = modelAdapter.insert(this, wrapper)

    override fun exists(wrapper: DatabaseWrapper): Boolean = modelAdapter.exists(this, wrapper)
}

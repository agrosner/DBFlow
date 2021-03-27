package com.dbflow5.structure

import com.dbflow5.adapter.RetrievalAdapter
import com.dbflow5.config.FlowManager
import com.dbflow5.database.DatabaseWrapper

/**
 * Description: A convenience class for [ReadOnlyModel].
 */
abstract class NoModificationModel : ReadOnlyModel {

    @delegate:Transient
    private val retrievalAdapter: RetrievalAdapter<NoModificationModel> by lazy { FlowManager.getRetrievalAdapter(javaClass) }

    override fun exists(wrapper: DatabaseWrapper): Boolean = retrievalAdapter.exists(this, wrapper)

    @Suppress("UNCHECKED_CAST")
    override fun <T> load(wrapper: DatabaseWrapper): T? = retrievalAdapter.load(this, wrapper) as T?

}

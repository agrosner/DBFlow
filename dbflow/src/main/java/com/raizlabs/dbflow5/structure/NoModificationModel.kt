package com.raizlabs.dbflow5.structure

import com.raizlabs.dbflow5.adapter.RetrievalAdapter
import com.raizlabs.dbflow5.config.FlowManager
import com.raizlabs.dbflow5.database.DatabaseWrapper

/**
 * Description: A convenience class for [ReadOnlyModel].
 */
abstract class NoModificationModel : ReadOnlyModel {

    @delegate:Transient
    private val retrievalAdapter: RetrievalAdapter<NoModificationModel> by lazy { FlowManager.getInstanceAdapter(javaClass) }

    override fun exists(wrapper: DatabaseWrapper): Boolean = retrievalAdapter.exists(this, wrapper)

    override fun load(wrapper: DatabaseWrapper) {
        retrievalAdapter.load(this, wrapper)
    }

    /**
     * Gets thrown when an operation is not valid for the SQL View
     */
    internal class InvalidSqlViewOperationException(detailMessage: String) : RuntimeException(detailMessage)
}

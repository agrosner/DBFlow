package com.raizlabs.android.dbflow.structure

import com.raizlabs.android.dbflow.config.FlowManager
import com.raizlabs.android.dbflow.structure.database.DatabaseWrapper

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

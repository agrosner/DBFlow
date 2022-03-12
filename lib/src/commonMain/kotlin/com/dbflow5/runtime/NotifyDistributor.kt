package com.dbflow5.runtime

import com.dbflow5.annotation.opts.InternalDBFlowApi
import com.dbflow5.database.DatabaseWrapper
import com.dbflow5.mpp.ensureNeverFrozen
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlin.native.concurrent.ThreadLocal

interface NotifyDistributor {

    fun <Table : Any> onChange(
        db: DatabaseWrapper,
        notification: ModelNotification<Table>
    )

    @ThreadLocal
    companion object : NotifyDistributor {
        private var notifyDistributor: NotifyDistributor = NotifyDistributorImpl()

        override fun <Table : Any> onChange(
            db: DatabaseWrapper,
            notification: ModelNotification<Table>
        ) = notifyDistributor.onChange(db, notification)

        /**
         * Used for testing, swaps the implementation.
         */
        @InternalDBFlowApi
        fun setNotifyDistributor(notifyDistributor: NotifyDistributor) {
            this.notifyDistributor = notifyDistributor
        }
    }
}

/**
 * Description: Distributes notifications to the [ModelNotifier].
 */
data class NotifyDistributorImpl
@InternalDBFlowApi constructor(
    private val scope: CoroutineScope = CoroutineScope(Dispatchers.Main),
) : NotifyDistributor {

    init {
        ensureNeverFrozen()
    }

    override fun <Table : Any> onChange(
        db: DatabaseWrapper,
        notification: ModelNotification<Table>
    ) {
        val modelNotifier = db.generatedDatabase.modelNotifier
        scope.launch {
            modelNotifier.onChange(notification)
        }
    }
}

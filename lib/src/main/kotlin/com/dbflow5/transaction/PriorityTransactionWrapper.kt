package com.dbflow5.transaction

import androidx.annotation.IntDef
import com.dbflow5.database.DatabaseWrapper

/**
 * Constructs a [PriorityTransactionWrapper] from an [ITransaction] specifying the priority.
 */
fun ITransaction<*>.withPriority(@PriorityTransactionWrapper.Priority
                                 priority: Int = PriorityTransactionWrapper.PRIORITY_NORMAL): PriorityTransactionWrapper =
        PriorityTransactionWrapper(this, priority)

/**
 * Description: Provides transaction with priority. Meant to be used in a [PriorityTransactionQueue].
 */
class PriorityTransactionWrapper(private val transaction: ITransaction<*>,
                                 private val priority: Int = PriorityTransactionWrapper.PRIORITY_NORMAL)
    : ITransaction<Unit>, Comparable<PriorityTransactionWrapper> {

    @Suppress("RemoveEmptyPrimaryConstructor")
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(PRIORITY_LOW, PRIORITY_NORMAL, PRIORITY_HIGH, PRIORITY_UI)
    annotation class Priority()

    override fun execute(databaseWrapper: DatabaseWrapper) {
        transaction.execute(databaseWrapper)
    }

    override fun compareTo(other: PriorityTransactionWrapper): Int = other.priority - priority

    companion object {

        /**
         * Low priority requests, reserved for non-essential tasks
         */
        const val PRIORITY_LOW = 0

        /**
         * The main of the requests, good for when adding a bunch of
         * data to the DB that the app does not access right away (default).
         */
        const val PRIORITY_NORMAL = 1

        /**
         * Reserved for tasks that will influence user interaction, such as displaying data in the UI
         * some point in the future (not necessarily right away)
         */
        const val PRIORITY_HIGH = 2

        /**
         * Reserved for only immediate tasks and all forms of fetching that will display on the UI
         */
        const val PRIORITY_UI = 5
    }

}

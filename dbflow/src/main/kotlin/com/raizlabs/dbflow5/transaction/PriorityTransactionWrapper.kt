package com.raizlabs.dbflow5.transaction

import android.support.annotation.IntDef

import com.raizlabs.dbflow5.database.DatabaseWrapper

/**
 * Description: Provides transaction with priority. Meant to be used in a [PriorityTransactionQueue].
 */
class PriorityTransactionWrapper(private val priority: Int, private val transaction: ITransaction<*>)
    : ITransaction<Unit>, Comparable<PriorityTransactionWrapper> {

    @Suppress("RemoveEmptyPrimaryConstructor")
    @Retention(AnnotationRetention.SOURCE)
    @IntDef(PRIORITY_LOW.toLong(), PRIORITY_NORMAL.toLong(), PRIORITY_HIGH.toLong(), PRIORITY_UI.toLong())
    annotation class Priority()

    internal constructor(builder: Builder) : this(
        priority = if (builder.priority == 0) {
            PRIORITY_NORMAL
        } else {
            builder.priority
        },
        transaction = builder.transaction)

    override fun execute(databaseWrapper: DatabaseWrapper) {
        transaction.execute(databaseWrapper)
    }

    override fun compareTo(other: PriorityTransactionWrapper): Int = other.priority - priority

    class Builder(internal val transaction: ITransaction<*>) {
        internal var priority: Int = 0

        /**
         * Sets a [Priority] that orders this transaction.
         */
        fun priority(@Priority priority: Int) = apply {
            this.priority = priority
        }

        fun build(): PriorityTransactionWrapper = PriorityTransactionWrapper(this)
    }

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

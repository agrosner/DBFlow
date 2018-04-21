package com.raizlabs.dbflow5.transaction

import com.raizlabs.dbflow5.database.DatabaseWrapper

/**
 * Description: Provides transaction with priority. Meant to be used in a [PriorityTransactionQueue].
 */
class PriorityTransactionWrapper(private val priority: Int, private val transaction: ITransaction<*>)
    : ITransaction<Unit>, Comparable<PriorityTransactionWrapper> {

    internal constructor(builder: Builder) : this(
        priority = builder.priority.priority,
        transaction = builder.transaction)

    override fun execute(databaseWrapper: DatabaseWrapper) {
        transaction.execute(databaseWrapper)
    }

    override fun compareTo(other: PriorityTransactionWrapper): Int = other.priority - priority

    class Builder(internal val transaction: ITransaction<*>) {
        internal var priority: Priority = Priority.PRIORITY_LOW

        /**
         * Sets a [Priority] that orders this transaction.
         */
        fun priority(priority: Priority) = apply {
            this.priority = priority
        }

        fun build(): PriorityTransactionWrapper = PriorityTransactionWrapper(this)
    }

    enum class Priority(val priority: Int) {

        /**
         * Low priority requests, reserved for non-essential tasks
         */
        PRIORITY_LOW(0),

        /**
         * The main of the requests, good for when adding a bunch of
         * data to the DB that the app does not access right away (default).
         */
        PRIORITY_NORMAL(1),

        /**
         * Reserved for tasks that will influence user interaction, such as displaying data in the UI
         * some point in the future (not necessarily right away)
         */
        PRIORITY_HIGH(2),

        /**
         * Reserved for only immediate tasks and all forms of fetching that will display on the UI
         */
        PRIORITY_UI(5);

    }

}

package com.dbflow5.observing

import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.atomic

/**
 * Description: Keeps track of how many observers are registered on a [TableObserver] at a given time.
 */
class ObservingTableTracker(tableCount: Int) {

    enum class Operation {
        None,
        Add,
        Remove
    }

    private val observerPerTable = LongArray(tableCount)
    private val previousTriggerStates = BooleanArray(tableCount)

    // caches the changes in a fixed array.
    private val triggerStateChanges = Array(tableCount) { Operation.None }

    private var needsSync by atomic(false)
    private var pendingSync by atomic(false)

    internal fun onAdded(tableIds: IntArray): Boolean =
        adjustObserverCount(tableIds, 1L, 0L)

    internal fun onRemoved(tableIds: IntArray): Boolean =
        adjustObserverCount(tableIds, -1L, 1L)

    private fun adjustObserverCount(
        tableIds: IntArray, value: Long,
        countToSync: Long
    ): Boolean {
        var syncTriggers = false
        tableIds.forEach { tableId ->
            val count = observerPerTable[tableId]
            observerPerTable[tableId] = count + value
            if (count == countToSync) {
                syncTriggers = true
                needsSync = true
            }
        }
        return syncTriggers
    }

    internal fun syncCompleted() {
        pendingSync = false
    }

    val tablesToSync: Array<Operation>?
        get() {
            if (!needsSync || pendingSync) {
                return null
            }

            observerPerTable.forEachIndexed { index, observerCount ->
                val hasObservers = observerCount > 0
                if (hasObservers != previousTriggerStates[index]) {
                    triggerStateChanges[index] = if (hasObservers) Operation.Add else Operation.None
                } else {
                    triggerStateChanges[index] = Operation.None
                }
                previousTriggerStates[index] = hasObservers
            }
            pendingSync = true
            needsSync = false
            return triggerStateChanges
        }
}

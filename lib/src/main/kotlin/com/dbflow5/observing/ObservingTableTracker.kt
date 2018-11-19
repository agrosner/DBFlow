package com.dbflow5.observing

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


    internal fun onAdded(tableIds: Array<Int>): Boolean {
        var syncTriggers = false
        synchronized(this) {
            tableIds.forEach { tableId ->
                val count = observerPerTable[tableId]
                observerPerTable[tableId] = count + 1
                if (count == 0) {
                    syncTriggers = true
                }
            }
        }
    }

}
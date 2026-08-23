package com.dbflow5.observing

import com.dbflow5.adapter.DBRepresentable

/**
 * Description:
 */
data class OnTableChangedObserver(
    internal val tables: List<DBRepresentable<*>>,
    /**
     * Called when a table or set of tables are invalidated in the DB.
     */
    internal val onChanged: (tables: Set<DBRepresentable<*>>) -> Unit
)

class OnTableChangedObserverWithIds(
    internal val observer: OnTableChangedObserver,
    internal val tableIds: IntArray
) {

    private var singleTableSet: MutableSet<DBRepresentable<*>>? = when {
        tableIds.size == 1 -> mutableSetOf(observer.tables[0])
        else -> null
    }

    internal fun notifyTables(invalidationStatus: BooleanArray) {
        var invalidatedTables: MutableSet<DBRepresentable<*>>? = null
        tableIds.forEachIndexed { index, tableId ->
            if (invalidationStatus[tableId]) {
                val singleTableSet = singleTableSet
                if (singleTableSet != null) {
                    invalidatedTables = singleTableSet
                } else {
                    if (invalidatedTables == null) {
                        invalidatedTables = mutableSetOf()
                    }
                    invalidatedTables!!.add(observer.tables[index])
                }
            }
        }
        invalidatedTables
            ?.takeIf { it.isNotEmpty() }
            ?.let { observer.onChanged(it) }
    }
}
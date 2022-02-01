package com.dbflow5.observing

import kotlin.reflect.KClass

/**
 * Description:
 */
abstract class OnTableChangedObserver(internal val tables: List<KClass<*>>) {

    /**
     * Called when a table or set of tables are invalidated in the DB.
     */
    abstract fun onChanged(tables: Set<KClass<*>>)

}

class OnTableChangedObserverWithIds(
    internal val observer: OnTableChangedObserver,
    internal val tableIds: IntArray
) {

    private var singleTableSet: MutableSet<KClass<*>>? = when {
        tableIds.size == 1 -> mutableSetOf(observer.tables[0])
        else -> null
    }

    internal fun notifyTables(invalidationStatus: BooleanArray) {
        var invalidatedTables: MutableSet<KClass<*>>? = null
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
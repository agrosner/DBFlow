package com.dbflow5.observing

/**
 * Description:
 */
abstract class OnTableChangedObserver(internal val tables: List<Class<*>>) {

    /**
     * Called when a table or set of tables are invalidated in the DB.
     */
    abstract fun onChanged(tables: Set<Class<*>>)

}

class OnTableChangedObserverWithIds(internal val observer: OnTableChangedObserver,
                                    internal val tableIds: IntArray) {

    private var singleTableSet: MutableSet<Class<*>>? = when {
        tableIds.size == 1 -> mutableSetOf(observer.tables[0])
        else -> null
    }

    internal fun notifyTables(invalidationStatus: BooleanArray) {
        var invalidatedTables: MutableSet<Class<*>>? = null
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
        invalidatedTables?.let { observer.onChanged(it) }
    }
}
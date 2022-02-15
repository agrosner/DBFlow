package com.dbflow5.runtime

/**
 * Interface for when a generic change on a table occurs.
 */
fun interface OnTableChangedListener<Table : Any> {

    /**
     * Called when table changes.
     */
    fun onTableChanged(notification: ModelNotification.TableChange<Table>)
}

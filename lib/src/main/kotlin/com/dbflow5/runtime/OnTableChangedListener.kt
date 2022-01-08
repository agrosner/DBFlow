package com.dbflow5.runtime

import com.dbflow5.structure.ChangeAction
import kotlin.reflect.KClass

/**
 * Interface for when a generic change on a table occurs.
 */
fun interface OnTableChangedListener {

    /**
     * Called when table changes.
     *
     * @param table The table that has changed. NULL unless version of app is JellyBean.
     * or higher.
     * @param action       The action that occurred.
     */
    fun onTableChanged(table: KClass<*>?, action: ChangeAction)
}

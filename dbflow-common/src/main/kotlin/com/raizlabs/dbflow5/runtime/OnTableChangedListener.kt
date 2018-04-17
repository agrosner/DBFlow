package com.raizlabs.dbflow5.runtime

import kotlin.reflect.KClass
import com.raizlabs.dbflow5.structure.ChangeAction

/**
 * Interface for when a generic change on a table occurs.
 */
interface OnTableChangedListener {

    /**
     * Called when table changes.
     *
     * @param table The table that has changed. NULL unless version of app is JellyBean.
     * or higher.
     * @param action       The action that occurred.
     */
    fun onTableChanged(table: KClass<*>?, action: ChangeAction)
}

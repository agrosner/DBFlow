package com.raizlabs.android.dbflow.runtime

import android.os.Build

import com.raizlabs.android.dbflow.structure.ChangeAction

/**
 * Interface for when a generic change on a table occurs.
 */
interface OnTableChangedListener {

    /**
     * Called when table changes.
     *
     * @param table The table that has changed. NULL unless version of app is [Build.VERSION_CODES.JELLY_BEAN]
     * or higher.
     * @param action       The action that occurred.
     */
    fun onTableChanged(table: Class<*>?, action: ChangeAction)
}

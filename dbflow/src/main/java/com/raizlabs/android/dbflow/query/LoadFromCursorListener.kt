package com.raizlabs.android.dbflow.query

import android.database.Cursor

import com.raizlabs.android.dbflow.structure.Model

/**
 * Description: Marks a [Model] as listening to [Cursor]
 * events for custom handling when loading from the DB.
 */
interface LoadFromCursorListener {

    /**
     * Called when the [Model] is loaded from the DB.
     *
     * @param cursor The cursor that is loaded.
     */
    fun onLoadFromCursor(cursor: Cursor)
}

package com.dbflow5.query

import com.dbflow5.database.FlowCursor
import com.dbflow5.structure.Model

/**
 * Description: Marks a Model as listening to [FlowCursor]
 * events for custom handling when loading from the DB.
 */
interface LoadFromCursorListener {

    /**
     * Called when the [Model] is loaded from the DB.
     *
     * @param cursor The cursor that is loaded.
     */
    fun onLoadFromCursor(cursor: FlowCursor)
}

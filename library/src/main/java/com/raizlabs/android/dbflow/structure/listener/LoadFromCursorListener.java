package com.raizlabs.android.dbflow.structure.listener;

import android.database.Cursor;

/**
 * Description: Marks a {@link com.raizlabs.android.dbflow.structure.Model} as listening to {@link android.database.Cursor}
 * events for custom handling when loading from the DB.
 */
public interface LoadFromCursorListener {

    /**
     * Called when the {@link com.raizlabs.android.dbflow.structure.Model} is loaded from the DB.
     *
     * @param cursor The cursor that is loaded.
     */
    void onLoadFromCursor(Cursor cursor);
}

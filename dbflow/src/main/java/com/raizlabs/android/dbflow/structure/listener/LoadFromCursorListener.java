package com.raizlabs.android.dbflow.structure.listener;

import android.database.Cursor;
import android.support.annotation.NonNull;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Marks a {@link Model} as listening to {@link Cursor}
 * events for custom handling when loading from the DB.
 */
public interface LoadFromCursorListener {

    /**
     * Called when the {@link Model} is loaded from the DB.
     *
     * @param cursor The cursor that is loaded.
     */
    void onLoadFromCursor(@NonNull Cursor cursor);
}

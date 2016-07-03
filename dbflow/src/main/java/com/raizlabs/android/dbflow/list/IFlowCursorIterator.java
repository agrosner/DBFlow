package com.raizlabs.android.dbflow.list;

import android.database.Cursor;

import com.raizlabs.android.dbflow.structure.Model;

/**
 * Description: Simple interface that allows you to iterate a {@link Cursor}.
 */
public interface IFlowCursorIterator<TModel extends Model> {

    /**
     * @return Count of the {@link Cursor}.
     */
    int getCount();

    /**
     * @param position The position within the {@link Cursor} to retrieve and convert into a {@link TModel}
     */
    TModel getItem(long position);

    /**
     * @return The cursor.
     */
    Cursor cursor();
}

package com.raizlabs.android.dbflow.list;

import android.database.Cursor;

import java.io.Closeable;
import java.io.IOException;

/**
 * Description: Simple interface that allows you to iterate a {@link Cursor}.
 */
public interface IFlowCursorIterator<TModel> extends Closeable {

    /**
     * @return Count of the {@link Cursor}.
     */
    long getCount();

    /**
     * @param position The position within the {@link Cursor} to retrieve and convert into a {@link TModel}
     */
    TModel getItem(long position);

    /**
     * @return The cursor.
     */
    Cursor cursor();

    /**
     * @return Can iterate the {@link Cursor}.
     */
    FlowCursorIterator<TModel> iterator();

    /**
     * @return Can iterate the {@link Cursor}. Specifies a starting location + count limit of results.
     */
    FlowCursorIterator<TModel> iterator(int startingLocation, int limit);

    @Override
    void close() throws IOException;
}

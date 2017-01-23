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

    @Override
    void close() throws IOException;
}

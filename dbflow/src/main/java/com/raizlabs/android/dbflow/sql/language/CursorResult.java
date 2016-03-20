package com.raizlabs.android.dbflow.sql.language;

import android.database.Cursor;

import com.raizlabs.android.dbflow.config.FlowManager;
import com.raizlabs.android.dbflow.structure.InstanceAdapter;
import com.raizlabs.android.dbflow.structure.Model;

import java.io.Closeable;
import java.util.List;

/**
 * Description: A class that contains a {@link Cursor} and handy methods for retrieving data from it.
 * You must close this object post use via {@link #close()}.
 */
public class CursorResult<TModel extends Model> implements Closeable {

    private final InstanceAdapter<?, TModel> retrievalAdapter;
    private final Cursor cursor;

    @SuppressWarnings("unchecked")
    CursorResult(Class<TModel> modelClass, Cursor cursor) {
        this.cursor = cursor;
        retrievalAdapter = FlowManager.getInstanceAdapter(modelClass);
    }

    /**
     * @return A {@link List} of items from this object. You must call {@link #close()} when finished.
     */
    public List<TModel> toList() {
        return retrievalAdapter.getListModelLoader().convertToData(cursor, null);
    }

    /**
     * @return The first {@link TModel} of items from the contained {@link Cursor}. You must call {@link #close()} when finished.
     */
    public TModel toModel() {
        return retrievalAdapter.getSingleModelLoader().convertToData(cursor, null);
    }

    public long count() {
        return cursor.getCount();
    }

    public Cursor getCursor() {
        return cursor;
    }

    @Override
    public void close() {
        cursor.close();
    }
}
